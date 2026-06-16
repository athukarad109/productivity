package com.productivitytracker.habits.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivitytracker.habits.data.repository.DashboardRepository
import com.productivitytracker.habits.data.repository.GoalRepository
import com.productivitytracker.habits.data.repository.HabitRepository
import com.productivitytracker.habits.domain.model.DashboardData
import com.productivitytracker.habits.domain.model.DashboardMode
import com.productivitytracker.habits.domain.model.Goal
import com.productivitytracker.habits.domain.model.GoalPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {

    private val mode = MutableStateFlow(DashboardMode.WEEKLY)
    private val data = MutableStateFlow<DashboardData?>(null)
    private val loading = MutableStateFlow(true)

    val dashboardMode: StateFlow<DashboardMode> = mode
    val dashboardData: StateFlow<DashboardData?> = data
    val isLoading: StateFlow<Boolean> = loading

    init {
        viewModelScope.launch { refresh() }
    }

    fun setMode(newMode: DashboardMode) {
        mode.value = newMode
        viewModelScope.launch { refresh() }
    }

    private suspend fun refresh() {
        loading.value = true
        data.value = dashboardRepository.load(mode.value)
        loading.value = false
    }
}

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val habitRepository: HabitRepository,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {

    val goals = goalRepository.observeGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits = habitRepository.observeAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories = habitRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val progressMap = MutableStateFlow<Map<Long, com.productivitytracker.habits.domain.model.GoalProgress>>(emptyMap())

    val goalProgress: StateFlow<Map<Long, com.productivitytracker.habits.domain.model.GoalProgress>> = progressMap

    init {
        viewModelScope.launch {
            goals.collect { refreshProgress(it) }
        }
    }

    private suspend fun refreshProgress(goalList: List<Goal>) {
        val dashboard = dashboardRepository.load(com.productivitytracker.habits.domain.model.DashboardMode.MONTHLY)
        progressMap.value = dashboard.goals
            .filter { gp -> goalList.any { it.id == gp.goal.id } }
            .associateBy { it.goal.id }
    }

    fun archiveGoal(id: Long) {
        viewModelScope.launch { goalRepository.archiveGoal(id) }
    }
}

@HiltViewModel
class GoalFormViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val habitRepository: HabitRepository,
) : ViewModel() {

    val habits = habitRepository.observeAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories = habitRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun loadGoal(id: Long) = goalRepository.getGoal(id)

    fun save(goal: Goal, onSaved: () -> Unit) {
        viewModelScope.launch {
            goalRepository.saveGoal(goal)
            onSaved()
        }
    }
}
