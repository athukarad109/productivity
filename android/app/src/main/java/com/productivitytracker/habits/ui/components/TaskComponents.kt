package com.productivitytracker.habits.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.productivitytracker.habits.domain.TimeFormat
import com.productivitytracker.habits.domain.model.DayTask
import com.productivitytracker.habits.domain.model.TaskCardVariant
import com.productivitytracker.habits.ui.theme.AppColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val categoryColors = mapOf(
    "Work" to Color(0xFF6366F1),
    "Learning" to Color(0xFF22C55E),
    "Exercise" to Color(0xFFF59E0B),
    "Personal" to Color(0xFFEC4899),
    "Health" to Color(0xFF14B8A6),
    "Social" to Color(0xFF8B5CF6),
    "Creative" to Color(0xFFF97316),
    "Admin" to Color(0xFF94A3B8),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelector(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }
    val label = date.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"))

    OutlinedButton(
        onClick = { showPicker = true },
        modifier = modifier,
    ) {
        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }

    if (showPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            val picked = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onDateChange(picked)
                        }
                        showPicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
fun DayTaskCard(
    task: DayTask,
    variant: TaskCardVariant = TaskCardVariant.PLANNED,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val categoryColor = categoryColors[task.categoryName] ?: AppColors.accent
    val borderColor = when (variant) {
        TaskCardVariant.MISSED -> AppColors.danger.copy(alpha = 0.5f)
        TaskCardVariant.UNPLANNED -> AppColors.success.copy(alpha = 0.5f)
        else -> AppColors.border
    }
    val bgColor = when (variant) {
        TaskCardVariant.MISSED -> AppColors.danger.copy(alpha = 0.08f)
        TaskCardVariant.UNPLANNED -> AppColors.success.copy(alpha = 0.08f)
        else -> AppColors.surface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(task.name, fontWeight = FontWeight.Medium)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(categoryColor.copy(alpha = 0.2f))
                        .border(1.dp, categoryColor.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(task.categoryName, style = MaterialTheme.typography.labelSmall, color = categoryColor)
                }
                if (variant == TaskCardVariant.MISSED) {
                    BadgeText("Missed", AppColors.danger)
                }
                if (variant == TaskCardVariant.UNPLANNED) {
                    BadgeText("Unplanned", AppColors.success)
                }
            }
            Row(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (task.startTime.isNotBlank() && task.endTime.isNotBlank()) {
                    Text(
                        "${TimeFormat.formatTime12h(task.startTime)} – ${TimeFormat.formatTime12h(task.endTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary,
                    )
                }
                if (task.duration > 0) {
                    Text(
                        TimeFormat.formatMinutes(task.duration),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary,
                    )
                }
            }
            if (task.notes.isNotBlank()) {
                Text(
                    task.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        if (onEdit != null || onDelete != null) {
            Row {
                onEdit?.let {
                    IconButton(onClick = it) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AppColors.textSecondary)
                    }
                }
                onDelete?.let {
                    IconButton(onClick = it) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AppColors.danger)
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeText(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
fun ScoreRing(score: Int, modifier: Modifier = Modifier) {
    val color = when {
        score >= 80 -> AppColors.success
        score >= 50 -> AppColors.warning
        else -> AppColors.danger
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        androidx.compose.material3.CircularProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier.size(120.dp),
            color = color,
            trackColor = AppColors.surfaceVariant,
            strokeWidth = 10.dp,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$score%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("score", style = MaterialTheme.typography.bodySmall, color = AppColors.textSecondary)
        }
    }
}
