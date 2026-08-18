package com.example.cineverse_mvi_clean_architecture.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

private val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFE8C547),
    onPrimary = Color(0xFF171717),
    secondary = Color(0xFF66D9C3),
    background = Color(0xFF101114),
    surface = Color(0xFF181A20),
    surfaceVariant = Color(0xFF262A33),
    onBackground = Color(0xFFF7F3E8),
    onSurface = Color(0xFFF7F3E8),
    onSurfaceVariant = Color(0xFFC7CAD1),
    error = Color(0xFFFF8A80),
)

private val LightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF7A5B00),
    onPrimary = Color.White,
    secondary = Color(0xFF006B5C),
    background = Color(0xFFFFFBFF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE4E2E8),
    onBackground = Color(0xFF1D1B20),
    onSurface = Color(0xFF1D1B20),
    onSurfaceVariant = Color(0xFF49454F),
    error = Color(0xFFB3261E),
)

@Composable
fun CineVerseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content,
    )
}
