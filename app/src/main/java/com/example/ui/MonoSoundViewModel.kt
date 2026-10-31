package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DownloadedTrackEntity
import com.example.downloader.AudioDownloader
import com.example.model.AudioSource
import com.example.model.DownloadStatus
import com.example.model.PlayerState
import com.example.model.TrackItem
import com.example.network.MusicSearchService
import com.example.player.MonoAudioPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MonoSoundViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val searchService = MusicSearchService()
    val audioDownloader = AudioDownloader(application)
    val audioPlayer = MonoAudioPlayer(application)

    // UI States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSource = MutableStateFlow(AudioSource.YOUTUBE)
    val selectedSource: StateFlow<AudioSource> = _selectedSource.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchResults = MutableStateFlow<List<TrackItem>>(emptyList())
    val searchResults: StateFlow<List<TrackItem>> = _searchResults.asStateFlow()

    private val _activeTab = MutableStateFlow(0) // 0: Search & Download, 1: On-Device Library
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Downloaded music from local Room database
    val downloadedTracks: StateFlow<List<DownloadedTrackEntity>> = db.downloadedTrackDao()
        .getAllDownloadedTracks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val playerState: StateFlow<PlayerState> = audioPlayer.playerState

    init {
        loadInitialTrending(AudioSource.YOUTUBE)
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSourceChange(source: AudioSource) {
        if (_selectedSource.value != source) {
            _selectedSource.value = source
            performSearch(_searchQuery.value)
        }
    }

    fun setActiveTab(index: Int) {
        _activeTab.value = index
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _isPlayerExpanded.value = expanded
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    fun performSearch(query: String = _searchQuery.value) {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                val results = searchService.search(query, _selectedSource.value)
                _searchResults.value = results
            } catch (e: Exception) {
                _toastMessage.value = "Search error: ${e.message}"
            } finally {
                _isSearching.value = false
            }
        }
    }

    private fun loadInitialTrending(source: AudioSource) {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                val trending = searchService.getTrendingTracks(source)
                _searchResults.value = trending
            } catch (e: Exception) {
                // ignore
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun downloadTrack(track: TrackItem) {
        viewModelScope.launch {
            _toastMessage.value = "Starting download for ${track.title}…"
            val success = audioDownloader.downloadTrack(track)
            if (success) {
                _toastMessage.value = "✓ Downloaded: ${track.title} to phone storage"
            } else {
                _toastMessage.value = "Download failed for ${track.title}"
            }
        }
    }

    fun deleteDownloadedTrack(track: DownloadedTrackEntity) {
        viewModelScope.launch {
            audioDownloader.deleteDownloadedTrack(track.id, track.filePath, track.mediaStoreUri)
            _toastMessage.value = "Deleted ${track.title} from device"
        }
    }

    fun shareTrack(track: DownloadedTrackEntity) {
        audioDownloader.shareTrackFile(track.filePath, track.title)
    }

    fun playTrack(track: TrackItem, playlist: List<TrackItem> = emptyList()) {
        val list = if (playlist.isNotEmpty()) playlist else _searchResults.value
        audioPlayer.playTrack(track, list)
    }

    fun togglePlayPause() {
        audioPlayer.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        audioPlayer.seekTo(positionMs)
    }

    fun setPlaybackSpeed(speed: Float) {
        audioPlayer.setPlaybackSpeed(speed)
    }

    fun cycleRepeatMode() {
        audioPlayer.cycleRepeatMode()
    }

    fun playNext() {
        audioPlayer.playNext()
    }

    fun playPrevious() {
        audioPlayer.playPrevious()
    }

    fun getDownloadStatusFlow(trackId: String): StateFlow<DownloadStatus> {
        return audioDownloader.getDownloadStatusFlow(trackId)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}
