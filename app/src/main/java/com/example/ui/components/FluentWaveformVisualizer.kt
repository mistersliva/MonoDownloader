package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StarkWhite

@Composable
fun FluentWaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = StarkWhite,
    barCount: Int = 24
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")

    // Animated phase offsets for organic sound wave motion
    val anim1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )

    val anim2 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )

    val anim3 by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Canvas(modifier = modifier.height(36.dp).fillMaxWidth()) {
        val width = size.width
        val height = size.height
        val totalSpacing = width * 0.3f
        val barWidth = (width - totalSpacing) / barCount
        val space = totalSpacing / (barCount - 1).coerceAtLeast(1)

        for (i in 0 until barCount) {
            val factor = when (i % 3) {
                0 -> if (isPlaying) anim1 else 0.2f
                1 -> if (isPlaying) anim2 else 0.35f
                else -> if (isPlaying) anim3 else 0.15f
            }

            // Create curved envelope (taller in middle, shorter at edges)
            val distanceRatio = Math.sin((i.toDouble() / barCount.toDouble()) * Math.PI).toFloat()
            val barHeight = (height * 0.85f * factor * distanceRatio).coerceAtLeast(4.dp.toPx())

            val x = i * (barWidth + space)
            val y = (height - barHeight) / 2f

            drawRoundRect(
                color = barColor.copy(alpha = if (isPlaying) 0.9f else 0.35f),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
