package com.productivitytracker.habits.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Slate950 = Color(0xFF020617)
private val Slate900 = Color(0xFF0F172A)
private val Slate800 = Color(0xFF1E293B)
private val Slate700 = Color(0xFF334155)
private val Slate400 = Color(0xFF94A3B8)
private val Slate200 = Color(0xFFE2E8F0)
private val Indigo500 = Color(0xFF6366F1)
private val Green500 = Color(0xFF22C55E)
private val Amber500 = Color(0xFFF59E0B)
private val Red500 = Color(0xFFEF4444)

val AppColors = object {
    val background = Slate950
    val surface = Slate900
    val surfaceVariant = Slate800
    val border = Slate700
    val textPrimary = Slate200
    val textSecondary = Slate400
    val accent = Indigo500
    val success = Green500
    val warning = Amber500
    val danger = Red500
}

private val DarkColors = darkColorScheme(
    primary = Indigo500,
    onPrimary = Color.White,
    background = Slate950,
    onBackground = Slate200,
    surface = Slate900,
    onSurface = Slate200,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    outline = Slate700,
)

@Composable
fun ProductivityTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content,
    )
}
