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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.productivitytracker.habits.domain.ComparisonCalculator
import com.productivitytracker.habits.domain.TimeFormat
import com.productivitytracker.habits.domain.model.DayTask
import com.productivitytracker.habits.ui.components.DateSelector
import com.productivitytracker.habits.ui.components.DayTaskCard
import com.productivitytracker.habits.ui.theme.AppColors
import com.productivitytracker.habits.ui.viewmodel.LogViewModel

@Composable
fun LogScreen(
    viewModel: LogViewModel = hiltViewModel(),
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val planned by viewModel.plannedTasks.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val date by viewModel.date.collectAsStateWithLifecycle()
    var showForm by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<DayTask?>(null) }

    val total = ComparisonCalculator.totalMinutes(tasks)
    val plannedTotal = ComparisonCalculator.totalMinutes(planned)
    val showImportBanner = planned.isNotEmpty() && tasks.isEmpty()

    Scaffold(
        containerColor = AppColors.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTask = null
                    showForm = true
                },
                containerColor = AppColors.accent,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add task")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                "Log Actual Day",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )
            DateSelector(date = date, onDateChange = viewModel::setDate)

            if (showImportBanner) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.accent.copy(alpha = 0.12f),
                    ),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "You have a plan for this day!",
                            color = AppColors.accent,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            "${planned.size} planned tasks (${TimeFormat.formatMinutes(plannedTotal)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.textSecondary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp),
                        )
                        Button(onClick = { viewModel.importFromPlan() }) {
                            Text("Import from Plan")
                        }
                    }
                }
            }

            if (tasks.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MiniStat("${tasks.size} logged", Modifier.weight(1f))
                    MiniStat(TimeFormat.formatMinutes(total), Modifier.weight(1f))
                }
            }

            if (tasks.isEmpty() && !showImportBanner) {
                Text(
                    "Nothing logged yet. Tap + to record what you did.",
                    color = AppColors.textSecondary,
                    modifier = Modifier.padding(top = 24.dp),
                )
            } else if (tasks.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp),
                ) {
                    items(tasks, key = { it.id }) { task ->
                        DayTaskCard(
                            task = task,
                            onEdit = {
                                editingTask = task
                                showForm = true
                            },
                            onDelete = { viewModel.deleteTask(task.id) },
                        )
                    }
                }
            }
        }
    }

    TaskFormSheet(
        visible = showForm,
        categories = categories,
        initial = editingTask,
        onDismiss = { showForm = false },
        onSave = { name, category, start, end, duration, notes ->
            viewModel.createTask(name, category, start, end, duration, notes, editingTask)
        },
    )
}

@Composable
private fun MiniStat(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
    ) {
        Text(text, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Medium)
    }
}
