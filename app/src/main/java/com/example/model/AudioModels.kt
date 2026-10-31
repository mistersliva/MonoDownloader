package com.example.model

enum class AudioSource(val displayName: String, val shortCode: String, val colorHex: Long) {
    YOUTUBE("YouTube", "YT", 0xFFFFFFFF),
    SOUNDCLOUD("SoundCloud", "SC", 0xFFE0E0E0);

    companion object {
        fun fromString(value: String): AudioSource {
            return if (value.equals("SOUNDCLOUD", ignoreCase = true) || value.equals("SC", ignoreCase = true)) {
                SOUNDCLOUD
            } else {
                YOUTUBE
            }
        }
    }
}

data class TrackItem(
    val id: String,
    val title: String,
    val artist: String,
    val source: AudioSource,
    val durationFormatted: String,
    val durationSeconds: Int,
    val thumbnailUrl: String,
    val streamUrl: String,
    val originalUrl: String,
    val audioBitrate: String = "320 kbps",
    val format: String = "MP3",
    val estimatedSizeMb: Float = 4.2f,
    val isDownloaded: Boolean = false
)

sealed class DownloadStatus {
    object Idle : DownloadStatus()
    object Connecting : DownloadStatus()
    data class Progress(
        val progressPercent: Float,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val speedKbps: Float
    ) : DownloadStatus()
    data class Completed(
        val filePath: String,
        val mediaUriString: String? = null
    ) : DownloadStatus()
    data class Failed(val errorMessage: String) : DownloadStatus()
}

enum class RepeatMode {
    OFF, ALL, ONE
}

data class PlayerState(
    val currentTrack: TrackItem? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val isBuffering: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val repeatMode: RepeatMode = RepeatMode.OFF
) {
    val progressFraction: Float
        get() = if (totalDurationMs > 0) (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
