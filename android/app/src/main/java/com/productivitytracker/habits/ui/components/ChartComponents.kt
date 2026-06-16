package com.productivitytracker.habits.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.productivitytracker.habits.domain.TimeFormat
import com.productivitytracker.habits.domain.model.CategoryBreakdown
import com.productivitytracker.habits.domain.model.DailyUnifiedScore
import com.productivitytracker.habits.domain.model.GoalMetric
import com.productivitytracker.habits.domain.model.GoalProgress
import com.productivitytracker.habits.ui.theme.AppColors

@Composable
fun ScoreBarChart(
    scores: List<DailyUnifiedScore>,
    modifier: Modifier = Modifier,
) {
    if (scores.isEmpty()) {
        Text("No score data yet", color = AppColors.textSecondary)
        return
    }
    val maxScore = 100f
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        scores.forEach { day ->
            val value = (day.unifiedScore ?: 0).toFloat()
            val barHeight = (value / maxScore).coerceIn(0f, 1f)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((120 * barHeight).dp.coerceAtLeast(4.dp))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            when {
                                value >= 80 -> AppColors.success
                                value >= 50 -> AppColors.warning
                                value > 0 -> AppColors.danger
                                else -> AppColors.surfaceVariant
                            },
                        ),
                )
                Text(
                    day.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
fun CategoryBarRow(item: CategoryBreakdown, modifier: Modifier = Modifier) {
    val max = maxOf(item.plannedMinutes, item.actualMinutes, 1)
    Column(modifier = modifier.fillMaxWidth()) {
        Text(item.category, fontWeight = FontWeight.Medium)
        Row(
            modifier = Modifier.padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Plan", style = MaterialTheme.typography.labelSmall, color = AppColors.textSecondary, modifier = Modifier.padding(end = 4.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(AppColors.surfaceVariant),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(item.plannedMinutes / max.toFloat())
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(AppColors.accent),
                )
            }
            Text(TimeFormat.formatMinutes(item.plannedMinutes), style = MaterialTheme.typography.labelSmall, color = AppColors.textSecondary)
        }
        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Actual", style = MaterialTheme.typography.labelSmall, color = AppColors.textSecondary, modifier = Modifier.padding(end = 4.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(AppColors.surfaceVariant),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(item.actualMinutes / max.toFloat())
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(AppColors.success),
                )
            }
            Text(TimeFormat.formatMinutes(item.actualMinutes), style = MaterialTheme.typography.labelSmall, color = AppColors.textSecondary)
        }
    }
}

@Composable
fun GoalProgressCard(progress: GoalProgress, modifier: Modifier = Modifier) {
    val goal = progress.goal
    val valueLabel = when (goal.metric) {
        GoalMetric.CATEGORY_MINUTES -> "${TimeFormat.formatMinutes(progress.currentValue)} / ${TimeFormat.formatMinutes(goal.targetValue)}"
        GoalMetric.HABIT_COMPLETIONS -> "${progress.currentValue} / ${goal.targetValue} times"
        GoalMetric.AVG_PRODUCTIVITY -> "${progress.currentValue}% / ${goal.targetValue}%"
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.surface)
            .padding(14.dp),
    ) {
        Text(goal.name, fontWeight = FontWeight.Medium)
        Text(
            goalSubtitle(goal),
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.textSecondary,
            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
        )
        LinearProgressIndicator(
            progress = { progress.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = AppColors.accent,
            trackColor = AppColors.surfaceVariant,
        )
        Text(
            valueLabel,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.accent,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

private fun goalSubtitle(goal: com.productivitytracker.habits.domain.model.Goal): String {
    val period = goal.period.name.lowercase().replaceFirstChar { it.uppercase() }
    return when (goal.metric) {
        GoalMetric.CATEGORY_MINUTES -> "$period · ${goal.categoryName ?: "Category"} time"
        GoalMetric.HABIT_COMPLETIONS -> "$period · ${goal.habitName ?: "Habit"}"
        GoalMetric.AVG_PRODUCTIVITY -> "$period · avg unified score"
    }
}
