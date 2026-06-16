package com.productivitytracker.habits.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.productivitytracker.habits.domain.model.Goal
import com.productivitytracker.habits.domain.model.GoalMetric
import com.productivitytracker.habits.domain.model.GoalPeriod
import com.productivitytracker.habits.ui.theme.AppColors
import com.productivitytracker.habits.ui.viewmodel.GoalFormViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GoalFormScreen(
    goalId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: GoalFormViewModel = hiltViewModel(),
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var period by remember { mutableStateOf(GoalPeriod.WEEKLY) }
    var metric by remember { mutableStateOf(GoalMetric.CATEGORY_MINUTES) }
    var targetValue by remember { mutableIntStateOf(300) }
    var categoryName by remember { mutableStateOf("") }
    var habitId by remember { mutableLongStateOf(0L) }
    var existingId by remember { mutableLongStateOf(0L) }

    LaunchedEffect(categories) {
        if (categoryName.isBlank() && categories.isNotEmpty()) {
            categoryName = categories.first().name
        }
    }

    LaunchedEffect(habits) {
        if (habitId == 0L && habits.isNotEmpty()) habitId = habits.first().id
    }

    LaunchedEffect(goalId) {
        if (goalId != null && goalId > 0) {
            val goal = viewModel.loadGoal(goalId) ?: return@LaunchedEffect
            existingId = goal.id
            name = goal.name
            period = goal.period
            metric = goal.metric
            targetValue = goal.targetValue
            categoryName = goal.categoryName.orEmpty()
            habitId = goal.habitId ?: 0L
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (goalId == null) "New Goal" else "Edit Goal") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.background,
                    titleContentColor = AppColors.textPrimary,
                ),
            )
        },
        containerColor = AppColors.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Goal name") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. 5h Learning this week") },
            )

            Text("Period", fontWeight = FontWeight.Medium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GoalPeriod.entries.forEach { p ->
                    FilterChip(
                        selected = period == p,
                        onClick = { period = p },
                        label = { Text(p.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }

            Text("Target type", fontWeight = FontWeight.Medium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = metric == GoalMetric.CATEGORY_MINUTES,
                    onClick = { metric = GoalMetric.CATEGORY_MINUTES },
                    label = { Text("Category time") },
                )
                FilterChip(
                    selected = metric == GoalMetric.HABIT_COMPLETIONS,
                    onClick = { metric = GoalMetric.HABIT_COMPLETIONS },
                    label = { Text("Habit count") },
                )
                FilterChip(
                    selected = metric == GoalMetric.AVG_PRODUCTIVITY,
                    onClick = { metric = GoalMetric.AVG_PRODUCTIVITY },
                    label = { Text("Avg score %") },
                )
            }

            when (metric) {
                GoalMetric.CATEGORY_MINUTES -> {
                    Text("Category", fontWeight = FontWeight.Medium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = categoryName == cat.name,
                                onClick = { categoryName = cat.name },
                                label = { Text(cat.name) },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = targetValue.toString(),
                        onValueChange = { it.toIntOrNull()?.let { v -> targetValue = v.coerceAtLeast(1) } },
                        label = { Text("Target minutes") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                GoalMetric.HABIT_COMPLETIONS -> {
                    Text("Habit", fontWeight = FontWeight.Medium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        habits.forEach { habit ->
                            FilterChip(
                                selected = habitId == habit.id,
                                onClick = { habitId = habit.id },
                                label = { Text(habit.name) },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = targetValue.toString(),
                        onValueChange = { it.toIntOrNull()?.let { v -> targetValue = v.coerceAtLeast(1) } },
                        label = { Text("Target completions") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                GoalMetric.AVG_PRODUCTIVITY -> {
                    OutlinedTextField(
                        value = targetValue.toString(),
                        onValueChange = { it.toIntOrNull()?.let { v -> targetValue = v.coerceIn(1, 100) } },
                        label = { Text("Target average score (%)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        "Unified score blends habit completion and plan vs actual.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary,
                    )
                }
            }

            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    val goal = Goal(
                        id = existingId,
                        name = name.trim(),
                        period = period,
                        metric = metric,
                        targetValue = targetValue,
                        categoryName = if (metric == GoalMetric.CATEGORY_MINUTES) categoryName else null,
                        habitId = if (metric == GoalMetric.HABIT_COMPLETIONS) habitId.takeIf { it > 0 } else null,
                    )
                    viewModel.save(goal, onSaved)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank(),
            ) {
                Text("Save Goal")
            }
        }
    }
}
