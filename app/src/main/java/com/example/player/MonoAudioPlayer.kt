package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import com.example.model.PlayerState
import com.example.model.RepeatMode
import com.example.model.TrackItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MonoAudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var currentPlaylist: List<TrackItem> = emptyList()

    fun updatePlaylist(tracks: List<TrackItem>) {
        currentPlaylist = tracks
    }

    fun playTrack(track: TrackItem, playlist: List<TrackItem> = emptyList()) {
        if (playlist.isNotEmpty()) {
            currentPlaylist = playlist
        }

        // Release previous player
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.reset()
            it.release()
        }

        _playerState.value = _playerState.value.copy(
            currentTrack = track,
            isPlaying = false,
            isBuffering = true,
            currentPositionMs = 0L,
            totalDurationMs = track.durationSeconds * 1000L
        )

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(track.streamUrl)
                setOnPreparedListener { mp ->
                    _playerState.value = _playerState.value.copy(
                        isPlaying = true,
                        isBuffering = false,
                        totalDurationMs = mp.duration.toLong()
                    )
                    applyPlaybackSpeed(_playerState.value.playbackSpeed)
                    mp.start()
                    startProgressTracker()
                }
                setOnCompletionListener {
                    handleTrackCompletion()
                }
                setOnErrorListener { _, _, _ ->
                    _playerState.value = _playerState.value.copy(
                        isPlaying = false,
                        isBuffering = false
                    )
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            _playerState.value = _playerState.value.copy(
                isPlaying = false,
                isBuffering = false
            )
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _playerState.value = _playerState.value.copy(isPlaying = false)
            stopProgressTracker()
        } else {
            player.start()
            _playerState.value = _playerState.value.copy(isPlaying = true)
            startProgressTracker()
        }
    }

    fun seekTo(positionMs: Long) {
        val player = mediaPlayer ?: return
        val safePos = positionMs.coerceIn(0L, _playerState.value.totalDurationMs)
        player.seekTo(safePos.toInt())
        _playerState.value = _playerState.value.copy(currentPositionMs = safePos)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
        applyPlaybackSpeed(speed)
    }

    private fun applyPlaybackSpeed(speed: Float) {
        val player = mediaPlayer ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (player.isPlaying) {
                    player.playbackParams = player.playbackParams.setSpeed(speed)
                }
            } catch (e: Exception) {
                // Ignore device-specific params failure
            }
        }
    }

    fun cycleRepeatMode() {
        val current = _playerState.value.repeatMode
        val next = when (current) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _playerState.value = _playerState.value.copy(repeatMode = next)
    }

    fun playNext() {
        val current = _playerState.value.currentTrack ?: return
        if (currentPlaylist.isEmpty()) return
        val currentIndex = currentPlaylist.indexOfFirst { it.id == current.id }
        if (currentIndex != -1 && currentIndex < currentPlaylist.size - 1) {
            playTrack(currentPlaylist[currentIndex + 1])
        } else if (currentPlaylist.isNotEmpty()) {
            playTrack(currentPlaylist.first())
        }
    }

    fun playPrevious() {
        val current = _playerState.value.currentTrack ?: return
        if (currentPlaylist.isEmpty()) return
        val currentIndex = currentPlaylist.indexOfFirst { it.id == current.id }
        if (currentIndex > 0) {
            playTrack(currentPlaylist[currentIndex - 1])
        } else if (currentPlaylist.isNotEmpty()) {
            playTrack(currentPlaylist.last())
        }
    }

    private fun handleTrackCompletion() {
        when (_playerState.value.repeatMode) {
            RepeatMode.ONE -> {
                val track = _playerState.value.currentTrack
                if (track != null) {
                    playTrack(track)
                }
            }
            RepeatMode.ALL -> {
                playNext()
            }
            RepeatMode.OFF -> {
                _playerState.value = _playerState.value.copy(
                    isPlaying = false,
                    currentPositionMs = _playerState.value.totalDurationMs
                )
                stopProgressTracker()
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                mediaPlayer?.let { player ->
                    try {
                        if (player.isPlaying) {
                            val pos = player.currentPosition.toLong()
                            val dur = player.duration.toLong().coerceAtLeast(1L)
                            _playerState.value = _playerState.value.copy(
                                currentPositionMs = pos,
                                totalDurationMs = dur
                            )
                        }
                    } catch (e: Exception) {
                        // ignore state sync
                    }
                }
                delay(150)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
    }

    fun release() {
        stopProgressTracker()
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.reset()
            it.release()
        }
        mediaPlayer = null
    }
}
