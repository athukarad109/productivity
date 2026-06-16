package com.productivitytracker.habits.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.productivitytracker.habits.ui.components.HabitListItem
import com.productivitytracker.habits.ui.theme.AppColors
import com.productivitytracker.habits.ui.viewmodel.HabitsViewModel

@Composable
fun HabitsScreen(
    onAddHabit: () -> Unit,
    onHabitClick: (Long) -> Unit,
    viewModel: HabitsViewModel = hiltViewModel(),
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddHabit, containerColor = AppColors.accent) {
                Icon(Icons.Default.Add, contentDescription = "Add habit")
            }
        },
        containerColor = AppColors.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = "Habits",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp),
            )

            if (habits.isEmpty()) {
                Text(
                    text = "No habits yet. Tap + to create your first one.",
                    color = AppColors.textSecondary,
                    modifier = Modifier.padding(top = 24.dp),
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp),
                ) {
                    items(habits, key = { it.id }) { habit ->
                        HabitListItem(
                            habit = habit,
                            onClick = { onHabitClick(habit.id) },
                        )
                    }
                }
            }
        }
    }
}
