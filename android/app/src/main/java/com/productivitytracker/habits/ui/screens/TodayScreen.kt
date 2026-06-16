package com.productivitytracker.habits.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.productivitytracker.habits.ui.components.HabitCard
import com.productivitytracker.habits.ui.components.ProgressHeader
import com.productivitytracker.habits.ui.theme.AppColors
import com.productivitytracker.habits.ui.viewmodel.TodayViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TodayScreen(
    onHabitClick: (Long) -> Unit,
    onManageHabits: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dateLabel = LocalDate.now().format(
        DateTimeFormatter.ofPattern("EEEE, MMM d"),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = onManageHabits) {
                Icon(Icons.Default.List, contentDescription = "Manage habits")
            }
        }
        Text(
            text = dateLabel,
            color = AppColors.textSecondary,
            modifier = Modifier.padding(bottom = 20.dp),
        )

        if (state.totalCount > 0) {
            ProgressHeader(
                completed = state.completedCount,
                total = state.totalCount,
                modifier = Modifier.padding(bottom = 20.dp),
            )
        }

        if (state.habits.isEmpty()) {
            Text(
                text = "No habits due today.\nTap the list icon to add habits.",
                color = AppColors.textSecondary,
                modifier = Modifier.padding(top = 32.dp),
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(state.habits, key = { it.habit.id }) { item ->
                    HabitCard(
                        item = item,
                        onToggle = { viewModel.toggleBinary(item) },
                        onIncrement = { viewModel.updateCount(item, it) },
                        onSkip = { viewModel.skip(item) },
                        onClick = { onHabitClick(item.habit.id) },
                        modifier = Modifier.clickable { onHabitClick(item.habit.id) },
                    )
                }
            }
        }
    }
}
