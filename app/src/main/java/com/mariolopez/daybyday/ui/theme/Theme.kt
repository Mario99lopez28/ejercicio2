package com.mariolopez.daybyday.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = BluePrimaryLight,
    onPrimary = Color.White,
    background = SurfaceLight,
    surface = CardLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    secondary = AccentUpcoming,
    error = DangerRed
)

private val DarkColors = darkColorScheme(
    primary = BluePrimaryDark,
    onPrimary = Color.Black,
    background = SurfaceDark,
    surface = CardDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    secondary = AccentUpcoming,
    error = DangerRed
)

@Composable
fun DayByDayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
