package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FluentMonochromeDarkColorScheme = darkColorScheme(
    primary = StarkWhite,
    onPrimary = PitchBlack,
    primaryContainer = FluentSurfaceVariant,
    onPrimaryContainer = StarkWhite,
    secondary = OffWhite,
    onSecondary = PitchBlack,
    secondaryContainer = FluentCard,
    onSecondaryContainer = OffWhite,
    tertiary = TextMuted,
    onTertiary = PitchBlack,
    background = PitchBlack,
    onBackground = StarkWhite,
    surface = FluentDark,
    onSurface = StarkWhite,
    surfaceVariant = FluentSurface,
    onSurfaceVariant = OffWhite,
    surfaceTint = StarkWhite,
    outline = FluentBorder,
    outlineVariant = FluentBorderActive,
    inverseSurface = StarkWhite,
    inverseOnSurface = PitchBlack,
)

private val FluentMonochromeLightColorScheme = lightColorScheme(
    primary = PitchBlack,
    onPrimary = StarkWhite,
    primaryContainer = OffWhite,
    onPrimaryContainer = PitchBlack,
    secondary = DarkGrey,
    onSecondary = StarkWhite,
    secondaryContainer = Color(0xFFE4E4E7),
    onSecondaryContainer = PitchBlack,
    tertiary = TextSubtle,
    onTertiary = StarkWhite,
    background = StarkWhite,
    onBackground = PitchBlack,
    surface = Color(0xFFFAFAFA),
    onSurface = PitchBlack,
    surfaceVariant = Color(0xFFF4F4F5),
    onSurfaceVariant = DarkGrey,
    outline = Color(0x33000000),
    outlineVariant = Color(0x66000000),
    inverseSurface = PitchBlack,
    inverseOnSurface = StarkWhite,
)

@Composable
fun MonoSoundTheme(
    darkTheme: Boolean = true, // Default to sleek fluent pitch-black aesthetic
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) FluentMonochromeDarkColorScheme else FluentMonochromeLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
