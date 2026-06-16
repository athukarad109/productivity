package com.productivitytracker.habits.ui.screens.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.productivitytracker.habits.domain.TimeFormat
import com.productivitytracker.habits.domain.model.Category
import com.productivitytracker.habits.domain.model.DayTask

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskFormSheet(
    visible: Boolean,
    categories: List<Category>,
    initial: DayTask? = null,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        categoryName: String,
        startTime: String,
        endTime: String,
        duration: Int,
        notes: String,
    ) -> Unit,
) {
    if (!visible) return

    var name by remember(initial) { mutableStateOf(initial?.name ?: "") }
    var categoryName by remember(initial, categories) {
        mutableStateOf(initial?.categoryName ?: categories.firstOrNull()?.name.orEmpty())
    }
    var useTimeRange by remember(initial) {
        mutableStateOf(!initial?.startTime.isNullOrBlank() && !initial?.endTime.isNullOrBlank())
    }
    var startTime by remember(initial) { mutableStateOf(initial?.startTime ?: "09:00") }
    var endTime by remember(initial) { mutableStateOf(initial?.endTime ?: "10:00") }
    var duration by remember(initial) { mutableIntStateOf(initial?.duration ?: 60) }
    var notes by remember(initial) { mutableStateOf(initial?.notes ?: "") }

    LaunchedEffect(categories) {
        if (categoryName.isBlank() && categories.isNotEmpty()) {
            categoryName = categories.first().name
        }
    }

    LaunchedEffect(useTimeRange, startTime, endTime) {
        if (useTimeRange) {
            val mins = TimeFormat.minutesBetween(startTime, endTime)
            if (mins > 0) duration = mins
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = if (initial == null) "Add Task" else "Edit Task",
                fontWeight = FontWeight.Bold,
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Task name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

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

            Text("Time entry", fontWeight = FontWeight.Medium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !useTimeRange,
                    onClick = { useTimeRange = false },
                    label = { Text("Duration") },
                )
                FilterChip(
                    selected = useTimeRange,
                    onClick = { useTimeRange = true },
                    label = { Text("Time range") },
                )
            }

            if (useTimeRange) {
                RowFields(
                    startTime = startTime,
                    endTime = endTime,
                    onStartChange = { startTime = it },
                    onEndChange = { endTime = it },
                )
            } else {
                OutlinedTextField(
                    value = duration.toString(),
                    onValueChange = { it.toIntOrNull()?.let { v -> duration = v.coerceAtLeast(1) } },
                    label = { Text("Duration (minutes)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    if (name.isBlank() || categoryName.isBlank()) return@Button
                    val resolvedDuration = if (useTimeRange) {
                        TimeFormat.minutesBetween(startTime, endTime)
                    } else {
                        duration
                    }
                    onSave(
                        name.trim(),
                        categoryName,
                        if (useTimeRange) startTime else "",
                        if (useTimeRange) endTime else "",
                        resolvedDuration,
                        notes.trim(),
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && categoryName.isNotBlank(),
            ) {
                Text(if (initial == null) "Add Task" else "Update Task")
            }

            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun RowFields(
    startTime: String,
    endTime: String,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
) {
    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = startTime,
            onValueChange = onStartChange,
            label = { Text("Start (HH:mm)") },
            modifier = Modifier.weight(1f),
            placeholder = { Text("09:00") },
        )
        OutlinedTextField(
            value = endTime,
            onValueChange = onEndChange,
            label = { Text("End (HH:mm)") },
            modifier = Modifier.weight(1f),
            placeholder = { Text("10:00") },
        )
    }
}
