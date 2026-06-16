package com.productivitytracker.habits.ui.screens.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.productivitytracker.habits.domain.TimeFormat
import com.productivitytracker.habits.domain.model.TaskCardVariant
import com.productivitytracker.habits.ui.components.DateSelector
import com.productivitytracker.habits.ui.components.DayTaskCard
import com.productivitytracker.habits.ui.components.ScoreRing
import com.productivitytracker.habits.ui.theme.AppColors
import com.productivitytracker.habits.ui.viewmodel.CompareViewModel

@Composable
fun CompareScreen(
    viewModel: CompareViewModel = hiltViewModel(),
) {
    val date by viewModel.date.collectAsStateWithLifecycle()
    val data by viewModel.comparisonData.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            "Daily Comparison",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        )
        DateSelector(date = date, onDateChange = viewModel::setDate)

        val comparison = data
        val hasData = comparison != null &&
            (comparison.planned.isNotEmpty() || comparison.actual.isNotEmpty())

        if (!hasData || comparison == null) {
            Text(
                "No data for this date.\nAdd a plan and log your day first.",
                color = AppColors.textSecondary,
                modifier = Modifier.padding(top = 32.dp),
            )
            return
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp, top = 16.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppColors.surface),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ScoreRing(comparison.score)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SummaryLine("Planned", TimeFormat.formatMinutes(comparison.totalPlannedMinutes), AppColors.accent)
                            SummaryLine("Actual", TimeFormat.formatMinutes(comparison.totalActualMinutes), AppColors.success)
                            SummaryLine("Missed", "${comparison.missedTasks.size}", AppColors.danger)
                            SummaryLine("Unplanned", "${comparison.unplannedTasks.size}", AppColors.warning)
                        }
                    }
                }
            }

            item {
                SectionTitle("Planned (${comparison.planned.size})")
            }
            if (comparison.planned.isEmpty()) {
                item { EmptySection("No plan for this day") }
            } else {
                items(comparison.planned, key = { "p-${it.id}" }) { task ->
                    val missed = comparison.missedTasks.any { it.id == task.id }
                    DayTaskCard(
                        task = task,
                        variant = if (missed) TaskCardVariant.MISSED else TaskCardVariant.PLANNED,
                    )
                }
            }

            item {
                SectionTitle("Actual (${comparison.actual.size})")
            }
            if (comparison.actual.isEmpty()) {
                item { EmptySection("Nothing logged yet") }
            } else {
                items(comparison.actual, key = { "a-${it.id}" }) { task ->
                    val unplanned = comparison.unplannedTasks.any { it.id == task.id }
                    DayTaskCard(
                        task = task,
                        variant = if (unplanned) TaskCardVariant.UNPLANNED else TaskCardVariant.ACTUAL,
                    )
                }
            }

            if (comparison.missedTasks.isNotEmpty() || comparison.unplannedTasks.isNotEmpty()) {
                item {
                    Text(
                        "Day Insights",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            if (comparison.missedTasks.isNotEmpty()) {
                item { Text("Missed", color = AppColors.danger, style = MaterialTheme.typography.bodyMedium) }
                items(comparison.missedTasks, key = { "m-${it.id}" }) { task ->
                    DayTaskCard(task = task, variant = TaskCardVariant.MISSED)
                }
            }
            if (comparison.unplannedTasks.isNotEmpty()) {
                item { Text("Unplanned", color = AppColors.success, style = MaterialTheme.typography.bodyMedium) }
                items(comparison.unplannedTasks, key = { "u-${it.id}" }) { task ->
                    DayTaskCard(task = task, variant = TaskCardVariant.UNPLANNED)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun EmptySection(text: String) {
    Text(text, color = AppColors.textSecondary, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
private fun SummaryLine(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = AppColors.textSecondary, style = MaterialTheme.typography.bodySmall)
        Text(value, color = color, fontWeight = FontWeight.Bold)
    }
}
