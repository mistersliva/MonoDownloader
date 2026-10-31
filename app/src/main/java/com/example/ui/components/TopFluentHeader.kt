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
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Search
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
import com.example.ui.theme.DarkGrey
import com.example.ui.theme.FluentBorder
import com.example.ui.theme.FluentCard
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.StarkWhite
import com.example.ui.theme.TextMuted

@Composable
fun TopFluentHeader(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    downloadCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // App Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(StarkWhite)
                        .border(1.dp, FluentBorder, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "MonoSound Emblem",
                        tint = PitchBlack,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "MONOSOUND",
                        color = StarkWhite,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "SC & YT Audio Downloader",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                    )
                }
            }

            // Quality Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(FluentCard)
                    .border(1.dp, FluentBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "320 KBPS MP3",
                    color = StarkWhite,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dual Mode Tab Switcher: "Search & Download" vs "Device Library"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(23.dp))
                .background(FluentCard)
                .border(1.dp, FluentBorder, RoundedCornerShape(23.dp))
                .padding(4.dp)
                .testTag("main_tabs")
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                TabPill(
                    title = "Search & Download",
                    icon = Icons.Default.Search,
                    isSelected = selectedTab == 0,
                    modifier = Modifier.weight(1f).testTag("tab_search"),
                    onClick = { onTabSelected(0) }
                )

                TabPill(
                    title = "On Phone",
                    icon = Icons.Default.DownloadDone,
                    badgeCount = downloadCount,
                    isSelected = selectedTab == 1,
                    modifier = Modifier.weight(1f).testTag("tab_library"),
                    onClick = { onTabSelected(1) }
                )
            }
        }
    }
}

@Composable
private fun TabPill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    badgeCount: Int? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val bgColor = animateColorAsState(
        targetValue = if (isSelected) StarkWhite else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tab_bg"
    )

    val textColor = animateColorAsState(
        targetValue = if (isSelected) PitchBlack else TextMuted,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tab_text"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(19.dp))
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
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor.value,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = title,
                color = textColor.value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            )

            if (badgeCount != null && badgeCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) PitchBlack else DarkGrey),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$badgeCount",
                        color = StarkWhite,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
