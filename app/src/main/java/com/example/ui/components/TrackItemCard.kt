package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AudioSource
import com.example.model.DownloadStatus
import com.example.model.TrackItem
import com.example.ui.theme.DarkGrey
import com.example.ui.theme.FluentBorder
import com.example.ui.theme.FluentCard
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.StarkWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TrackItemCard(
    track: TrackItem,
    downloadStatusFlow: StateFlow<DownloadStatus>,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val downloadStatus by downloadStatusFlow.collectAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FluentCard)
            .border(1.dp, FluentBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onPlayClick)
            .padding(14.dp)
            .testTag("track_card_${track.id}")
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Album Art / Thumbnail
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkGrey)
                        .border(1.dp, FluentBorder, RoundedCornerShape(12.dp)),
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
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Play overlay button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PitchBlack.copy(alpha = 0.75f))
                            .border(1.dp, StarkWhite.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = StarkWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Title and Artist
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Source Badge (YT or SC)
                        val isYT = track.source == AudioSource.YOUTUBE
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isYT) StarkWhite else DarkGrey)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = track.source.shortCode,
                                color = if (isYT) PitchBlack else StarkWhite,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
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

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = track.artist,
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Audio Specs & Duration
                    Text(
                        text = "${track.audioBitrate} • ${track.format} • ${track.durationFormatted} • ~${track.estimatedSizeMb} MB",
                        color = TextSubtle,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Download Button / Status Component
                DownloadActionButton(
                    downloadStatus = downloadStatus,
                    onDownloadClick = onDownloadClick,
                    trackId = track.id
                )
            }

            // Live progress bar if actively downloading
            if (downloadStatus is DownloadStatus.Progress) {
                val progress = downloadStatus as DownloadStatus.Progress
                Spacer(modifier = Modifier.height(10.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Downloading audio: ${(progress.progressPercent * 100).toInt()}%",
                            color = StarkWhite,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                        )
                        Text(
                            text = "${String.format("%.1f", progress.speedKbps / 1024f)} MB/s",
                            color = TextMuted,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(DarkGrey)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.progressPercent)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(StarkWhite)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadActionButton(
    downloadStatus: DownloadStatus,
    onDownloadClick: () -> Unit,
    trackId: String
) {
    when (downloadStatus) {
        is DownloadStatus.Idle -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(StarkWhite)
                    .clickable(onClick = onDownloadClick)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("download_button_$trackId"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download Music",
                        tint = PitchBlack,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Get",
                        color = PitchBlack,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp)
                    )
                }
            }
        }

        is DownloadStatus.Connecting -> {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkGrey),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = StarkWhite,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        is DownloadStatus.Progress -> {
            val progress = (downloadStatus as DownloadStatus.Progress).progressPercent
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkGrey),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    color = StarkWhite,
                    strokeWidth = 2.5.dp,
                    trackColor = DarkGrey,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = StarkWhite,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold)
                )
            }
        }

        is DownloadStatus.Completed -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkGrey)
                    .border(1.dp, StarkWhite.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Downloaded to phone",
                        tint = StarkWhite,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Phone",
                        color = StarkWhite,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        is DownloadStatus.Failed -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(PitchBlack)
                    .border(1.dp, StarkWhite, RoundedCornerShape(18.dp))
                    .clickable(onClick = onDownloadClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Retry",
                    color = StarkWhite,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
