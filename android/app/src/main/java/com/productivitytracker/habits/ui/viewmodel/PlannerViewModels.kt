package com.productivitytracker.habits.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivitytracker.habits.data.repository.DayTaskRepository
import com.productivitytracker.habits.data.repository.HabitRepository
import com.productivitytracker.habits.domain.ComparisonCalculator
import com.productivitytracker.habits.domain.DateUtils
import com.productivitytracker.habits.domain.model.ComparisonData
import com.productivitytracker.habits.domain.model.DayTask
import com.productivitytracker.habits.domain.model.RecordType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val dayTaskRepository: DayTaskRepository,
    private val habitRepository: HabitRepository,
) : ViewModel() {

    private val selectedDate = MutableStateFlow(DateUtils.today())

    val date: StateFlow<LocalDate> = selectedDate

    val tasks = selectedDate.flatMapLatest { date ->
        dayTaskRepository.observePlan(DateUtils.format(date))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories = habitRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun saveTask(task: DayTask) {
        viewModelScope.launch {
            dayTaskRepository.saveTask(task)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            dayTaskRepository.deleteTask(taskId)
        }
    }

    fun createTask(
        name: String,
        categoryName: String,
        startTime: String,
        endTime: String,
        duration: Int,
        notes: String,
        existing: DayTask? = null,
    ) {
        val dateStr = DateUtils.format(selectedDate.value)
        val task = DayTask(
            id = existing?.id ?: dayTaskRepository.newTaskId(),
            date = dateStr,
            recordType = RecordType.PLAN,
            name = name,
            categoryName = categoryName,
            startTime = startTime,
            endTime = endTime,
            duration = duration,
            notes = notes,
            sortOrder = existing?.sortOrder ?: tasks.value.size,
        )
        saveTask(task)
    }
}

@HiltViewModel
class LogViewModel @Inject constructor(
    private val dayTaskRepository: DayTaskRepository,
    private val habitRepository: HabitRepository,
) : ViewModel() {

    private val selectedDate = MutableStateFlow(DateUtils.today())

    val date: StateFlow<LocalDate> = selectedDate

    val tasks = selectedDate.flatMapLatest { date ->
        dayTaskRepository.observeActual(DateUtils.format(date))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val plannedTasks = selectedDate.flatMapLatest { date ->
        dayTaskRepository.observePlan(DateUtils.format(date))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories = habitRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun saveTask(task: DayTask) {
        viewModelScope.launch {
            dayTaskRepository.saveTask(task)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            dayTaskRepository.deleteTask(taskId)
        }
    }

    fun importFromPlan() {
        viewModelScope.launch {
            dayTaskRepository.importPlanToActual(DateUtils.format(selectedDate.value))
        }
    }

    fun createTask(
        name: String,
        categoryName: String,
        startTime: String,
        endTime: String,
        duration: Int,
        notes: String,
        existing: DayTask? = null,
    ) {
        val dateStr = DateUtils.format(selectedDate.value)
        val task = DayTask(
            id = existing?.id ?: dayTaskRepository.newTaskId(),
            date = dateStr,
            recordType = RecordType.ACTUAL,
            name = name,
            categoryName = categoryName,
            startTime = startTime,
            endTime = endTime,
            duration = duration,
            notes = notes,
            sortOrder = existing?.sortOrder ?: tasks.value.size,
        )
        saveTask(task)
    }
}

@HiltViewModel
class CompareViewModel @Inject constructor(
    private val dayTaskRepository: DayTaskRepository,
) : ViewModel() {

    private val selectedDate = MutableStateFlow(DateUtils.today())

    val date: StateFlow<LocalDate> = selectedDate

    val comparisonData: StateFlow<ComparisonData?> = selectedDate
        .flatMapLatest { date ->
            val dateStr = DateUtils.format(date)
            combine(
                dayTaskRepository.observePlan(dateStr),
                dayTaskRepository.observeActual(dateStr),
            ) { planned, actual ->
                ComparisonCalculator.compare(dateStr, planned, actual)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setDate(date: LocalDate) {
        selectedDate.value = date
    }
}
