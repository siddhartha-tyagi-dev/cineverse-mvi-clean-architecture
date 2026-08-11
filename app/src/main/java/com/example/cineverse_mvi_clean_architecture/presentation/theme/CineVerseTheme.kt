package com.example.cineverse_mvi_clean_architecture.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
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

@Composable
fun CineVerseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content,
    )
}
