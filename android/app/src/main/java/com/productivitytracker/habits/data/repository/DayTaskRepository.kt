package com.productivitytracker.habits.data.repository

import com.productivitytracker.habits.data.local.dao.DayTaskDao
import com.productivitytracker.habits.data.mapper.toDomain
import com.productivitytracker.habits.data.mapper.toEntity
import com.productivitytracker.habits.domain.ComparisonCalculator
import com.productivitytracker.habits.domain.DateUtils
import com.productivitytracker.habits.domain.model.ComparisonData
import com.productivitytracker.habits.domain.model.DayTask
import com.productivitytracker.habits.domain.model.RecordType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DayTaskRepository @Inject constructor(
    private val dayTaskDao: DayTaskDao,
) {
    fun observePlan(date: String): Flow<List<DayTask>> =
        observe(date, RecordType.PLAN)

    fun observeActual(date: String): Flow<List<DayTask>> =
        observe(date, RecordType.ACTUAL)

    private fun observe(date: String, type: RecordType): Flow<List<DayTask>> =
        dayTaskDao.observeForDate(date, type.name).map { list -> list.map { it.toDomain() } }

    suspend fun getPlan(date: String): List<DayTask> =
        dayTaskDao.getForDate(date, RecordType.PLAN.name).map { it.toDomain() }

    suspend fun getActual(date: String): List<DayTask> =
        dayTaskDao.getForDate(date, RecordType.ACTUAL.name).map { it.toDomain() }

    suspend fun saveTask(task: DayTask) {
        dayTaskDao.upsert(task.toEntity())
    }

    suspend fun deleteTask(taskId: String) {
        dayTaskDao.deleteById(taskId)
    }

    suspend fun replaceAll(date: String, type: RecordType, tasks: List<DayTask>) {
        dayTaskDao.replaceAllForDate(date, type.name, tasks.map { it.toEntity() })
    }

    suspend fun importPlanToActual(date: String): List<DayTask> {
        val planned = getPlan(date)
        val actual = getActual(date)
        if (planned.isEmpty()) return actual

        val toImport = planned.filter { p ->
            actual.none {
                it.id == p.id || it.name.equals(p.name, ignoreCase = true)
            }
        }
        val imported = toImport.map { planTask ->
            planTask.copy(
                id = newTaskId(),
                recordType = RecordType.ACTUAL,
            )
        }
        val merged = actual + imported
        replaceAll(date, RecordType.ACTUAL, merged)
        return merged
    }

    suspend fun compare(date: String): ComparisonData {
        val planned = getPlan(date)
        val actual = getActual(date)
        return ComparisonCalculator.compare(date, planned, actual)
    }

    fun newTaskId(): String = UUID.randomUUID().toString()

    fun formatDate(date: LocalDate = DateUtils.today()): String = DateUtils.format(date)
}
