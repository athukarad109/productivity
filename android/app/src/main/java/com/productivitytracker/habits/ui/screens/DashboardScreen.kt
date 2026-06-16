package com.productivitytracker.habits.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.productivitytracker.habits.domain.model.DashboardMode
import com.productivitytracker.habits.ui.components.CategoryBarRow
import com.productivitytracker.habits.ui.components.GoalProgressCard
import com.productivitytracker.habits.ui.components.ScoreBarChart
import com.productivitytracker.habits.ui.theme.AppColors
import com.productivitytracker.habits.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    onManageHabits: () -> Unit,
    onManageGoals: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val mode by viewModel.dashboardMode.collectAsStateWithLifecycle()
    val data by viewModel.dashboardData.collectAsStateWithLifecycle()
    val loading by viewModel.isLoading.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            "Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = mode == DashboardMode.WEEKLY,
                onClick = { viewModel.setMode(DashboardMode.WEEKLY) },
                label = { Text("Weekly") },
            )
            FilterChip(
                selected = mode == DashboardMode.MONTHLY,
                onClick = { viewModel.setMode(DashboardMode.MONTHLY) },
                label = { Text("Monthly") },
            )
        }

        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextButton(onClick = onManageHabits) { Text("Habits") }
            TextButton(onClick = onManageGoals) { Text("Goals") }
        }

        if (loading || data == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(color = AppColors.accent)
            }
            return
        }

        val dashboard = data!!

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard(
                        title = "Unified score",
                        value = dashboard.averageUnifiedScore?.let { "$it%" } ?: "—",
                        modifier = Modifier.weight(1f),
                    )
                    SummaryCard(
                        title = "Habits",
                        value = "${(dashboard.habitCompletionRate * 100).toInt()}%",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = AppColors.surface)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Unified daily score", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Average of habit + plan/actual scores",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.textSecondary,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                        ScoreBarChart(dashboard.dailyScores)
                    }
                }
            }

            if (dashboard.categoryBreakdown.isNotEmpty()) {
                item {
                    Text("Category breakdown", fontWeight = FontWeight.SemiBold)
                }
                items(dashboard.categoryBreakdown, key = { it.category }) { item ->
                    Card(colors = CardDefaults.cardColors(containerColor = AppColors.surface)) {
                        CategoryBarRow(item, modifier = Modifier.padding(14.dp))
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text("Goals", fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = onManageGoals) { Text("Manage") }
                }
            }

            if (dashboard.goals.isEmpty()) {
                item {
                    Text(
                        "No goals yet. Tap Goals to set weekly or monthly targets.",
                        color = AppColors.textSecondary,
                    )
                }
            } else {
                items(dashboard.goals, key = { it.goal.id }) { progress ->
                    GoalProgressCard(progress)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = AppColors.textSecondary, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = AppColors.accent)
        }
    }
}
