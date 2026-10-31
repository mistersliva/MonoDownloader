package com.example.downloader

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.AppDatabase
import com.example.data.DownloadedTrackEntity
import com.example.model.DownloadStatus
import com.example.model.TrackItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class AudioDownloader(private val context: Context) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val db = AppDatabase.getDatabase(context)

    // Track active download states by trackId
    private val _downloadStates = ConcurrentHashMap<String, MutableStateFlow<DownloadStatus>>()

    fun getDownloadStatusFlow(trackId: String): StateFlow<DownloadStatus> {
        val flow = _downloadStates.getOrPut(trackId) {
            MutableStateFlow(DownloadStatus.Idle)
        }
        return flow.asStateFlow()
    }

    suspend fun downloadTrack(track: TrackItem): Boolean = withContext(Dispatchers.IO) {
        val stateFlow = _downloadStates.getOrPut(track.id) {
            MutableStateFlow(DownloadStatus.Idle)
        }
        stateFlow.value = DownloadStatus.Connecting

        try {
            val sanitizedTitle = track.title.replace(Regex("[^a-zA-Z0-9._\\- ]"), "").trim()
            val fileName = "${sanitizedTitle.ifEmpty { "song_${track.id}" }}.${track.format.lowercase()}"

            // 1. Destination file in app storage (always accessible & safe)
            val musicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                ?: context.filesDir
            val targetFile = File(musicDir, fileName)

            val request = Request.Builder()
                .url(track.streamUrl)
                .header("User-Agent", "MonoSound-Downloader/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                stateFlow.value = DownloadStatus.Failed("Download server returned code ${response.code}")
                return@withContext false
            }

            val body = response.body
            if (body == null) {
                stateFlow.value = DownloadStatus.Failed("Empty response body from audio stream")
                return@withContext false
            }

            val totalBytes = body.contentLength().let { if (it <= 0) (track.durationSeconds * 40000L) else it }
            var downloadedBytes = 0L

            val inputStream: InputStream = body.byteStream()
            val outputStream: OutputStream = FileOutputStream(targetFile)

            val buffer = ByteArray(8 * 1024)
            var bytesRead: Int
            val startTime = System.currentTimeMillis()

            inputStream.use { input ->
                outputStream.use { output ->
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000f).coerceAtLeast(0.1f)
                        val speedKbps = (downloadedBytes / 1024f) / elapsedSec
                        val progress = if (totalBytes > 0) (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0.5f

                        stateFlow.value = DownloadStatus.Progress(
                            progressPercent = progress,
                            downloadedBytes = downloadedBytes,
                            totalBytes = totalBytes,
                            speedKbps = speedKbps
                        )
                    }
                }
            }

            // 2. Also register into Android MediaStore so phone music player / ringtones can access it!
            var mediaStoreUriString: String? = null
            try {
                mediaStoreUriString = saveToMediaStore(targetFile, track, fileName)
            } catch (e: Exception) {
                // If media store insert fails on older api or restrictions, targetFile is still safe
            }

            // 3. Save into Room Database
            val entity = DownloadedTrackEntity(
                id = track.id,
                title = track.title,
                artist = track.artist,
                source = track.source.name,
                durationFormatted = track.durationFormatted,
                durationSeconds = track.durationSeconds,
                filePath = targetFile.absolutePath,
                mediaStoreUri = mediaStoreUriString,
                fileSize = targetFile.length(),
                thumbnailUrl = track.thumbnailUrl,
                audioBitrate = track.audioBitrate,
                format = track.format
            )
            db.downloadedTrackDao().insertTrack(entity)

            stateFlow.value = DownloadStatus.Completed(
                filePath = targetFile.absolutePath,
                mediaUriString = mediaStoreUriString
            )
            return@withContext true

        } catch (e: Exception) {
            stateFlow.value = DownloadStatus.Failed(e.message ?: "Download encountered an error")
            return@withContext false
        }
    }

    private fun saveToMediaStore(sourceFile: File, track: TrackItem, fileName: String): String? {
        val resolver = context.contentResolver
        val audioCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Audio.Media.TITLE, track.title)
            put(MediaStore.Audio.Media.ARTIST, track.artist)
            put(MediaStore.Audio.Media.ALBUM, "MonoSound (${track.source.displayName})")
            put(MediaStore.Audio.Media.DURATION, track.durationSeconds * 1000L)
            put(MediaStore.Audio.Media.MIME_TYPE, if (track.format.equals("M4A", true)) "audio/mp4" else "audio/mpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Audio.Media.IS_PENDING, 1)
                put(MediaStore.Audio.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MUSIC}/MonoSound")
            }
        }

        val uri = resolver.insert(audioCollection, contentValues) ?: return null

        resolver.openOutputStream(uri)?.use { out ->
            sourceFile.inputStream().use { inStream ->
                inStream.copyTo(out)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }

        return uri.toString()
    }

    suspend fun deleteDownloadedTrack(trackId: String, filePath: String, mediaUriString: String?) = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
            if (!mediaUriString.isNullOrEmpty()) {
                try {
                    context.contentResolver.delete(Uri.parse(mediaUriString), null, null)
                } catch (e: Exception) {
                    // Ignore
                }
            }
            db.downloadedTrackDao().deleteTrackById(trackId)
            _downloadStates[trackId]?.value = DownloadStatus.Idle
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun shareTrackFile(filePath: String, title: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) return

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } else {
                Uri.fromFile(file)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Music File").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (e: Exception) {
            // Error handling
        }
    }
}
