package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.DownloadedTrackEntity
import com.example.model.AudioSource
import com.example.model.TrackItem
import com.example.ui.theme.DarkGrey
import com.example.ui.theme.FluentBorder
import com.example.ui.theme.FluentCard
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.StarkWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle

@Composable
fun DownloadedMusicView(
    downloadedTracks: List<DownloadedTrackEntity>,
    currentPlayingId: String?,
    isPlaying: Boolean,
    onTrackClick: (TrackItem) -> Unit,
    onShareClick: (DownloadedTrackEntity) -> Unit,
    onDeleteClick: (DownloadedTrackEntity) -> Unit,
    onGoToSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (downloadedTracks.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(FluentCard)
                        .border(1.dp, FluentBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DownloadDone,
                        contentDescription = null,
                        tint = StarkWhite,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "No songs on phone yet",
                    color = StarkWhite,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Search any song from YouTube or SoundCloud and tap 'Get' to download high-quality audio directly into your phone storage.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(StarkWhite)
                        .clickable(onClick = onGoToSearchClick)
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .testTag("explore_songs_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Explore & Download Music",
                        color = PitchBlack,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    } else {
        val totalBytes = downloadedTracks.sumOf { it.fileSize }
        val totalMb = totalBytes / (1024f * 1024f)

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Storage overview pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(FluentCard)
                        .border(1.dp, FluentBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Phone Storage Media",
                                color = TextMuted,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${downloadedTracks.size} Tracks • ${String.format("%.1f", totalMb)} MB Saved",
                                color = StarkWhite,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkGrey)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "OFFLINE READY",
                                color = StarkWhite,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            items(downloadedTracks, key = { it.id }) { trackEntity ->
                val trackItem = trackEntity.toTrackItem()
                val isCurrentPlaying = currentPlayingId == trackEntity.id

                DownloadedTrackRow(
                    track = trackEntity,
                    isCurrentPlaying = isCurrentPlaying,
                    isPlaying = isPlaying,
                    onPlayClick = { onTrackClick(trackItem) },
                    onShareClick = { onShareClick(trackEntity) },
                    onDeleteClick = { onDeleteClick(trackEntity) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(96.dp)) // Padding for bottom player
            }
        }
    }
}

@Composable
private fun DownloadedTrackRow(
    track: DownloadedTrackEntity,
    isCurrentPlaying: Boolean,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isCurrentPlaying) DarkGrey else FluentCard)
            .border(
                1.dp,
                if (isCurrentPlaying) StarkWhite.copy(alpha = 0.5f) else FluentBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onPlayClick)
            .padding(12.dp)
            .testTag("downloaded_row_${track.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkGrey)
                    .border(1.dp, FluentBorder, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (track.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = track.thumbnailUrl,
                        contentDescription = track.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (isCurrentPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PitchBlack.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = StarkWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isYT = track.source.equals("YOUTUBE", true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isYT) StarkWhite else PitchBlack)
                            .border(0.5.dp, StarkWhite.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (isYT) "YT" else "SC",
                            color = if (isYT) PitchBlack else StarkWhite,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        )
                    }

                    Text(
                        text = track.title,
                        color = StarkWhite,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = track.artist,
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                val mb = track.fileSize / (1024f * 1024f)
                Text(
                    text = "${track.durationFormatted} • ${String.format("%.1f", mb)} MB • ${track.format}",
                    color = TextSubtle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                )
            }

            // Share button
            IconButton(
                onClick = onShareClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Audio File",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Delete button
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete File",
                    tint = TextSubtle,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
