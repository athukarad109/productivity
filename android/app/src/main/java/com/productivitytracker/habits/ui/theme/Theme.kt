package com.productivitytracker.habits.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    val background = Color(0xFF020617)
    val surface = Color(0xFF0F172A)
    val surfaceVariant = Color(0xFF1E293B)
    val border = Color(0xFF334155)
    val textPrimary = Color(0xFFE2E8F0)
    val textSecondary = Color(0xFF94A3B8)
    val accent = Color(0xFF6366F1)
    val success = Color(0xFF22C55E)
    val warning = Color(0xFFF59E0B)
    val danger = Color(0xFFEF4444)
}

private val DarkColors = darkColorScheme(
    primary = AppColors.accent,
    onPrimary = Color.White,
    background = AppColors.background,
    onBackground = AppColors.textPrimary,
    surface = AppColors.surface,
    onSurface = AppColors.textPrimary,
    surfaceVariant = AppColors.surfaceVariant,
    onSurfaceVariant = AppColors.textSecondary,
    outline = AppColors.border,
)

@Composable
fun ProductivityTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content,
    )
}
