package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.DownloadedMusicView
import com.example.ui.components.FluentFullPlayerModal
import com.example.ui.components.FluentMiniPlayer
import com.example.ui.components.SearchSection
import com.example.ui.components.TopFluentHeader
import com.example.ui.components.TrackItemCard
import com.example.ui.theme.DarkGrey
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.StarkWhite
import com.example.ui.theme.TextMuted

@Composable
fun MonoSoundApp(
    viewModel: MonoSoundViewModel = viewModel()
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedSource by viewModel.selectedSource.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val downloadedTracks by viewModel.downloadedTracks.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    Scaffold(
        containerColor = PitchBlack,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                AnimatedVisibility(
                    visible = playerState.currentTrack != null,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    FluentMiniPlayer(
                        playerState = playerState,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onNextClick = { viewModel.playNext() },
                        onExpandClick = { viewModel.setPlayerExpanded(true) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Header
            TopFluentHeader(
                selectedTab = activeTab,
                onTabSelected = { viewModel.setActiveTab(it) },
                downloadCount = downloadedTracks.size
            )

            if (activeTab == 0) {
                // Discover & Download Tab
                SearchSection(
                    query = searchQuery,
                    onQueryChange = { viewModel.onQueryChange(it) },
                    onSearch = { viewModel.performSearch() },
                    selectedSource = selectedSource,
                    onSourceChange = { viewModel.onSourceChange(it) },
                    isSearching = isSearching
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isSearching && searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = StarkWhite,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = DarkGrey,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No results found",
                                color = StarkWhite,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try searching a different artist or song name",
                                color = TextMuted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = if (searchQuery.isEmpty()) "Trending on ${selectedSource.displayName}" else "Search Results (${searchResults.size})",
                                color = TextMuted,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                            )
                        }

                        items(searchResults, key = { it.id }) { track ->
                            val isCurrentTrack = playerState.currentTrack?.id == track.id
                            TrackItemCard(
                                track = track,
                                downloadStatusFlow = viewModel.getDownloadStatusFlow(track.id),
                                isPlaying = isCurrentTrack && playerState.isPlaying,
                                onPlayClick = { viewModel.playTrack(track) },
                                onDownloadClick = { viewModel.downloadTrack(track) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(100.dp)) // Leave space for mini player
                        }
                    }
                }
            } else {
                // On-Device Library Tab
                DownloadedMusicView(
                    downloadedTracks = downloadedTracks,
                    currentPlayingId = playerState.currentTrack?.id,
                    isPlaying = playerState.isPlaying,
                    onTrackClick = { viewModel.playTrack(it) },
                    onShareClick = { viewModel.shareTrack(it) },
                    onDeleteClick = { viewModel.deleteDownloadedTrack(it) },
                    onGoToSearchClick = { viewModel.setActiveTab(0) }
                )
            }
        }

        // Full Player Modal Sheet
        if (isPlayerExpanded) {
            FluentFullPlayerModal(
                playerState = playerState,
                onDismiss = { viewModel.setPlayerExpanded(false) },
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSeekTo = { viewModel.seekTo(it) },
                onNext = { viewModel.playNext() },
                onPrevious = { viewModel.playPrevious() },
                onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                onCycleRepeat = { viewModel.cycleRepeatMode() },
                onDownloadClick = {
                    playerState.currentTrack?.let { viewModel.downloadTrack(it) }
                }
            )
        }
    }
}
