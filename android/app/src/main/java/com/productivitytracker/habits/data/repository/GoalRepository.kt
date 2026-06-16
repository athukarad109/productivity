package com.productivitytracker.habits.data.repository

import com.productivitytracker.habits.data.local.dao.GoalDao
import com.productivitytracker.habits.data.local.dao.HabitDao
import com.productivitytracker.habits.data.mapper.toDomain
import com.productivitytracker.habits.data.mapper.toEntity
import com.productivitytracker.habits.domain.model.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val habitDao: HabitDao,
) {
    fun observeGoals(): Flow<List<Goal>> =
        goalDao.observeActive().map { entities ->
            entities.map { entity ->
                val habitName = entity.habitId?.let { habitDao.getWithCategory(it)?.name }
                entity.toDomain(habitName)
            }
        }

    suspend fun getGoals(): List<Goal> =
        goalDao.getActive().map { entity ->
            val habitName = entity.habitId?.let { habitDao.getWithCategory(it)?.name }
            entity.toDomain(habitName)
        }

    suspend fun getGoal(id: Long): Goal? {
        val entity = goalDao.getById(id) ?: return null
        val habitName = entity.habitId?.let { habitDao.getWithCategory(it)?.name }
        return entity.toDomain(habitName)
    }

    suspend fun saveGoal(goal: Goal): Long {
        val entity = goal.toEntity()
        return if (goal.id == 0L) goalDao.insert(entity) else {
            goalDao.update(entity)
            goal.id
        }
    }

    suspend fun archiveGoal(id: Long) {
        goalDao.archive(id)
    }
}
