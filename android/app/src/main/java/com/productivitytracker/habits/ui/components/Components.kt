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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.productivitytracker.habits.domain.ScheduleUtils
import com.productivitytracker.habits.domain.StreakCalculator
import com.productivitytracker.habits.domain.model.Habit
import com.productivitytracker.habits.domain.model.HabitWithLog
import com.productivitytracker.habits.domain.model.LogStatus
import com.productivitytracker.habits.domain.model.TargetType
import com.productivitytracker.habits.ui.theme.AppColors

@Composable
fun ProgressHeader(completed: Int, total: Int, modifier: Modifier = Modifier) {
    val progress = if (total == 0) 0f else completed.toFloat() / total
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$completed / $total habits",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                color = AppColors.accent,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = AppColors.accent,
            trackColor = AppColors.surfaceVariant,
        )
    }
}

@Composable
fun HabitCard(
    item: HabitWithLog,
    onToggle: () -> Unit,
    onIncrement: ((Int) -> Unit)? = null,
    onSkip: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val habit = item.habit
    val completed = StreakCalculator.isCompleted(habit, item.log)
    val categoryColor = Color(habit.categoryColorArgb)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.surface)
            .border(1.dp, AppColors.border, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (completed) AppColors.success.copy(alpha = 0.2f) else AppColors.surfaceVariant)
                .border(2.dp, if (completed) AppColors.success else categoryColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (completed) {
                Icon(Icons.Default.Check, contentDescription = null, tint = AppColors.success)
            } else {
                Text(habit.iconEmoji)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(habit.name, fontWeight = FontWeight.SemiBold)
            Text(
                "${habit.categoryName} · ${ScheduleUtils.targetLabel(habit)}",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.textSecondary,
            )
            if (habit.targetType != TargetType.BINARY && item.log != null) {
                Text(
                    "${item.log.value} / ${habit.targetValue}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.accent,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        when (habit.targetType) {
            TargetType.BINARY -> {
                androidx.compose.material3.TextButton(onClick = onToggle) {
                    Text(if (completed) "Undo" else "Done")
                }
            }
            TargetType.COUNT, TargetType.DURATION -> {
                val current = item.log?.value ?: 0
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    androidx.compose.material3.TextButton(onClick = { onIncrement?.invoke((current - 1).coerceAtLeast(0)) }) {
                        Text("−")
                    }
                    androidx.compose.material3.TextButton(onClick = { onIncrement?.invoke(current + 1) }) {
                        Text("+")
                    }
                }
            }
        }
    }
}

@Composable
fun HabitListItem(
    habit: Habit,
    streak: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryColor = Color(habit.categoryColorArgb)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(AppColors.surface)
            .border(1.dp, AppColors.border, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(categoryColor),
        )
        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(habit.name, fontWeight = FontWeight.Medium)
            Text(
                "${habit.categoryName} · ${ScheduleUtils.scheduleLabel(habit)}",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.textSecondary,
            )
        }
        if (streak > 0) {
            Text("🔥 $streak", color = AppColors.warning, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun HeatmapGrid(cells: List<com.productivitytracker.habits.domain.model.DayHeatmapCell>) {
    val rows = cells.chunked(7)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { cell ->
                    val color = when {
                        !cell.isDue -> AppColors.surfaceVariant.copy(alpha = 0.4f)
                        cell.status == LogStatus.DONE -> AppColors.success
                        cell.status == LogStatus.PARTIAL -> AppColors.warning
                        cell.status == LogStatus.SKIPPED -> AppColors.danger.copy(alpha = 0.6f)
                        else -> AppColors.surfaceVariant
                    }
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color),
                    )
                }
            }
        }
    }
}
