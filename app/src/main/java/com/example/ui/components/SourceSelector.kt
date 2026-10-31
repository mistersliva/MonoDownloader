package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioSource
import com.example.ui.theme.FluentBorder
import com.example.ui.theme.FluentCard
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.StarkWhite
import com.example.ui.theme.TextMuted

@Composable
fun FluentSourceSelector(
    selectedSource: AudioSource,
    onSourceSelected: (AudioSource) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(FluentCard)
            .border(1.dp, FluentBorder, RoundedCornerShape(26.dp))
            .padding(4.dp)
            .testTag("source_selector")
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // YouTube Pill Button
            val isYT = selectedSource == AudioSource.YOUTUBE
            SourcePill(
                title = "YouTube",
                subTag = "YT Audio",
                icon = {
                    Icon(
                        imageVector = Icons.Default.PlayCircleFilled,
                        contentDescription = "YouTube Icon",
                        tint = if (isYT) PitchBlack else StarkWhite,
                        modifier = Modifier.size(20.dp)
                    )
                },
                isSelected = isYT,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("source_youtube_pill"),
                onClick = { onSourceSelected(AudioSource.YOUTUBE) }
            )

            // SoundCloud Pill Button
            val isSC = selectedSource == AudioSource.SOUNDCLOUD
            SourcePill(
                title = "SoundCloud",
                subTag = "SC Cloud",
                icon = {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "SoundCloud Icon",
                        tint = if (isSC) PitchBlack else StarkWhite,
                        modifier = Modifier.size(20.dp)
                    )
                },
                isSelected = isSC,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("source_soundcloud_pill"),
                onClick = { onSourceSelected(AudioSource.SOUNDCLOUD) }
            )
        }
    }
}

@Composable
private fun SourcePill(
    title: String,
    subTag: String,
    icon: @Composable () -> Unit,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val bgColor = animateColorAsState(
        targetValue = if (isSelected) StarkWhite else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pill_bg"
    )

    val textColor = animateColorAsState(
        targetValue = if (isSelected) PitchBlack else TextMuted,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pill_text"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(bgColor.value)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            icon()
            Box(modifier = Modifier.size(8.dp))
            Text(
                text = title,
                color = textColor.value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                )
            )
            Box(modifier = Modifier.size(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) PitchBlack.copy(alpha = 0.12f) else StarkWhite.copy(alpha = 0.12f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = subTag,
                    color = textColor.value,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                )
            }
        }
    }
}
