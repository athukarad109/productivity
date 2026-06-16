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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import com.productivitytracker.habits.ui.viewmodel.PlanViewModel

@Composable
fun PlanScreen(
    viewModel: PlanViewModel = hiltViewModel(),
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val date by viewModel.date.collectAsStateWithLifecycle()
    var showForm by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<DayTask?>(null) }

    val total = ComparisonCalculator.totalMinutes(tasks)

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
                androidx.compose.material3.Icon(Icons.Default.Add, contentDescription = "Add task")
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
                "Plan My Day",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )
            DateSelector(date = date, onDateChange = viewModel::setDate)

            if (tasks.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatChip("${tasks.size} tasks", Modifier.weight(1f))
                    StatChip(TimeFormat.formatMinutes(total), Modifier.weight(1f))
                    StatChip("${categories.size} categories", Modifier.weight(1f))
                }
            }

            if (tasks.isEmpty()) {
                Text(
                    "No tasks planned yet. Tap + to add your first task.",
                    color = AppColors.textSecondary,
                    modifier = Modifier.padding(top = 32.dp),
                )
            } else {
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
private fun StatChip(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}
