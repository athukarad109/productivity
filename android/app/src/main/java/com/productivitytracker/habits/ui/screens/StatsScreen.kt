package com.productivitytracker.habits.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.productivitytracker.habits.ui.theme.AppColors
import com.productivitytracker.habits.ui.viewmodel.StatsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun StatsScreen(
    onHabitClick: (Long) -> Unit,
    onManageHabits: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    var overallRate by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var streaks by remember { mutableStateOf<Map<Long, Int>>(emptyMap()) }

    LaunchedEffect(habits) {
        loading = true
        val (_, rate) = withContext(Dispatchers.IO) { viewModel.overallStats() }
        overallRate = (rate * 100).toInt()
        val map = mutableMapOf<Long, Int>()
        habits.forEach { habit ->
            val stats = withContext(Dispatchers.IO) { viewModel.statsFor(habit.id) }
            if (stats != null) map[habit.id] = stats.currentStreak
        }
        streaks = map
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "Stats",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
        )
        TextButton(onClick = onManageHabits, modifier = Modifier.padding(bottom = 8.dp)) {
            Text("Manage habits")
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("7-day completion", color = AppColors.textSecondary)
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 8.dp),
                        color = AppColors.accent,
                    )
                } else {
                    Text(
                        text = "$overallRate%",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.accent,
                    )
                    Text(
                        text = "${habits.size} active habits",
                        color = AppColors.textSecondary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        Text(
            text = "Streaks",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp),
        )

        if (habits.isEmpty()) {
            Text("Create habits to see stats.", color = AppColors.textSecondary)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(habits, key = { it.id }) { habit ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
                        onClick = { onHabitClick(habit.id) },
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(habit.name, fontWeight = FontWeight.Medium)
                            Text(
                                text = "🔥 ${streaks[habit.id] ?: 0} day streak",
                                color = AppColors.warning,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
