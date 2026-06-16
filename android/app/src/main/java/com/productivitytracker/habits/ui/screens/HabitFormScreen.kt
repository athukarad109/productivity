package com.productivitytracker.habits.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.productivitytracker.habits.domain.model.Habit
import com.productivitytracker.habits.domain.model.ScheduleType
import com.productivitytracker.habits.domain.model.TargetType
import com.productivitytracker.habits.ui.theme.AppColors
import com.productivitytracker.habits.ui.viewmodel.HabitFormViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitFormScreen(
    habitId: Long?,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    viewModel: HabitFormViewModel = hiltViewModel(),
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableLongStateOf(0L) }
    var iconEmoji by remember { mutableStateOf("✓") }
    var scheduleType by remember { mutableStateOf(ScheduleType.DAILY) }
    var targetType by remember { mutableStateOf(TargetType.BINARY) }
    var targetValue by remember { mutableIntStateOf(1) }
    var timesPerWeek by remember { mutableIntStateOf(3) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderHour by remember { mutableIntStateOf(8) }
    var reminderMinute by remember { mutableIntStateOf(0) }
    var existingId by remember { mutableLongStateOf(0L) }

    LaunchedEffect(categories) {
        if (selectedCategoryId == 0L && categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id
        }
    }

    LaunchedEffect(habitId) {
        if (habitId != null && habitId > 0) {
            val habit = viewModel.loadHabit(habitId) ?: return@LaunchedEffect
            existingId = habit.id
            name = habit.name
            selectedCategoryId = habit.categoryId
            iconEmoji = habit.iconEmoji
            scheduleType = habit.scheduleType
            targetType = habit.targetType
            targetValue = habit.targetValue
            timesPerWeek = habit.timesPerWeek
            reminderEnabled = habit.reminderHour != null
            reminderHour = habit.reminderHour ?: 8
            reminderMinute = habit.reminderMinute ?: 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (habitId == null) "New Habit" else "Edit Habit") },
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
                label = { Text("Habit name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = iconEmoji,
                onValueChange = { if (it.length <= 2) iconEmoji = it },
                label = { Text("Icon (emoji)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Text("Category", fontWeight = FontWeight.Medium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { cat ->
                    val selected = cat.id == selectedCategoryId
                    FilterChip(
                        selected = selected,
                        onClick = { selectedCategoryId = cat.id },
                        label = { Text(cat.name) },
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }

            Text("Schedule", fontWeight = FontWeight.Medium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScheduleType.entries.forEach { type ->
                    FilterChip(
                        selected = scheduleType == type,
                        onClick = { scheduleType = type },
                        label = {
                            Text(
                                when (type) {
                                    ScheduleType.DAILY -> "Daily"
                                    ScheduleType.WEEKDAYS -> "Weekdays"
                                    ScheduleType.WEEKENDS -> "Weekends"
                                    ScheduleType.SPECIFIC_DAYS -> "Custom"
                                    ScheduleType.TIMES_PER_WEEK -> "X/week"
                                },
                            )
                        },
                    )
                }
            }

            if (scheduleType == ScheduleType.TIMES_PER_WEEK) {
                OutlinedTextField(
                    value = timesPerWeek.toString(),
                    onValueChange = { it.toIntOrNull()?.let { v -> timesPerWeek = v.coerceIn(1, 7) } },
                    label = { Text("Times per week") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Text("Target", fontWeight = FontWeight.Medium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TargetType.entries.forEach { type ->
                    FilterChip(
                        selected = targetType == type,
                        onClick = { targetType = type },
                        label = {
                            Text(
                                when (type) {
                                    TargetType.BINARY -> "Yes/No"
                                    TargetType.COUNT -> "Count"
                                    TargetType.DURATION -> "Minutes"
                                },
                            )
                        },
                    )
                }
            }

            if (targetType != TargetType.BINARY) {
                OutlinedTextField(
                    value = targetValue.toString(),
                    onValueChange = { it.toIntOrNull()?.let { v -> targetValue = v.coerceAtLeast(1) } },
                    label = {
                        Text(if (targetType == TargetType.COUNT) "Target count" else "Target minutes")
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Daily reminder")
                Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
            }

            if (reminderEnabled) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = reminderHour.toString(),
                        onValueChange = { it.toIntOrNull()?.let { v -> reminderHour = v.coerceIn(0, 23) } },
                        label = { Text("Hour") },
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = reminderMinute.toString(),
                        onValueChange = { it.toIntOrNull()?.let { v -> reminderMinute = v.coerceIn(0, 59) } },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Button(
                onClick = {
                    if (name.isBlank() || selectedCategoryId == 0L) return@Button
                    val habit = Habit(
                        id = existingId,
                        name = name.trim(),
                        categoryId = selectedCategoryId,
                        iconEmoji = iconEmoji.ifBlank { "✓" },
                        scheduleType = scheduleType,
                        timesPerWeek = timesPerWeek,
                        targetType = targetType,
                        targetValue = targetValue,
                        reminderHour = if (reminderEnabled) reminderHour else null,
                        reminderMinute = if (reminderEnabled) reminderMinute else null,
                    )
                    viewModel.save(habit, onSaved)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && selectedCategoryId != 0L,
            ) {
                Text("Save Habit")
            }
        }
    }
}
