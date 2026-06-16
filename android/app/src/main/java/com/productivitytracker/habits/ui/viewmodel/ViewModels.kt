package com.productivitytracker.habits.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivitytracker.habits.data.repository.HabitRepository
import com.productivitytracker.habits.domain.StreakCalculator
import com.productivitytracker.habits.domain.model.HabitWithLog
import com.productivitytracker.habits.domain.model.LogStatus
import com.productivitytracker.habits.domain.model.TargetType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TodayUiState(
    val habits: List<HabitWithLog> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {

    val uiState: StateFlow<TodayUiState> = repository.observeTodayHabits()
        .map { habits ->
            val completed = habits.count { StreakCalculator.isCompleted(it.habit, it.log) }
            TodayUiState(
                habits = habits,
                completedCount = completed,
                totalCount = habits.size,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayUiState())

    fun toggleBinary(habitWithLog: HabitWithLog) {
        viewModelScope.launch {
            val habit = habitWithLog.habit
            val isDone = StreakCalculator.isCompleted(habit, habitWithLog.log)
            if (isDone) {
                repository.clearLog(habit.id)
            } else {
                repository.logHabit(habit, status = LogStatus.DONE)
            }
        }
    }

    fun updateCount(habitWithLog: HabitWithLog, value: Int) {
        viewModelScope.launch {
            val habit = habitWithLog.habit
            val status = when {
                value >= habit.targetValue -> LogStatus.DONE
                value > 0 -> LogStatus.PARTIAL
                else -> LogStatus.SKIPPED
            }
            if (value <= 0) {
                repository.clearLog(habit.id)
            } else {
                repository.logHabit(habit, status = status, value = value)
            }
        }
    }

    fun skip(habitWithLog: HabitWithLog) {
        viewModelScope.launch {
            repository.logHabit(habitWithLog.habit, status = LogStatus.SKIPPED)
        }
    }
}

@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {
    val habits = repository.observeAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun archive(habitId: Long) {
        viewModelScope.launch { repository.archiveHabit(habitId) }
    }
}

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {
    val habits = repository.observeAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun statsFor(habitId: Long) = repository.getHabitStats(habitId)

    suspend fun overallStats() = repository.getOverallStats()
}

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {
    suspend fun load(habitId: Long) = repository.getHabitStats(habitId)

    fun buildHeatmap(
        habit: com.productivitytracker.habits.domain.model.Habit,
        logs: List<com.productivitytracker.habits.domain.model.HabitLog>,
    ) = repository.buildHeatmap(habit, logs, days = 35)
}

@HiltViewModel
class HabitFormViewModel @Inject constructor(
    private val repository: HabitRepository,
) : ViewModel() {
    val categories = repository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun loadHabit(id: Long) = repository.getHabit(id)

    fun save(habit: com.productivitytracker.habits.domain.model.Habit, onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.saveHabit(habit)
            onSaved(id)
        }
    }
}
