package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AudioSource
import com.example.model.TrackItem

@Entity(tableName = "downloaded_tracks")
data class DownloadedTrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val source: String,
    val durationFormatted: String,
    val durationSeconds: Int,
    val filePath: String,
    val mediaStoreUri: String?,
    val fileSize: Long,
    val thumbnailUrl: String,
    val audioBitrate: String = "320 kbps",
    val format: String = "MP3",
    val downloadedAt: Long = System.currentTimeMillis()
) {
    fun toTrackItem(): TrackItem {
        return TrackItem(
            id = id,
            title = title,
            artist = artist,
            source = AudioSource.fromString(source),
            durationFormatted = durationFormatted,
            durationSeconds = durationSeconds,
            thumbnailUrl = thumbnailUrl,
            streamUrl = filePath,
            originalUrl = "",
            audioBitrate = audioBitrate,
            format = format,
            estimatedSizeMb = (fileSize / (1024f * 1024f)).coerceAtLeast(0.1f),
            isDownloaded = true
        )
    }
}
