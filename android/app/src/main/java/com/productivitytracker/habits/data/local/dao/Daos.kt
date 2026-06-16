package com.productivitytracker.habits.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.productivitytracker.habits.data.local.entity.CategoryEntity
import com.productivitytracker.habits.data.local.entity.HabitEntity
import com.productivitytracker.habits.data.local.entity.HabitLogEntity
import com.productivitytracker.habits.data.local.entity.HabitWithCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY name")
    suspend fun getAll(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long
}

@Dao
interface HabitDao {
    @Query(
        """
        SELECT h.id, h.name, h.categoryId, c.name AS categoryName, c.colorArgb AS categoryColorArgb,
               h.iconEmoji, h.scheduleType, h.scheduleDays, h.timesPerWeek, h.targetType, h.targetValue,
               h.reminderHour, h.reminderMinute, h.archived, h.createdAt
        FROM habits h
        INNER JOIN categories c ON c.id = h.categoryId
        WHERE h.archived = 0
        ORDER BY h.name
        """,
    )
    fun observeActiveWithCategory(): Flow<List<HabitWithCategoryEntity>>

    @Query(
        """
        SELECT h.id, h.name, h.categoryId, c.name AS categoryName, c.colorArgb AS categoryColorArgb,
               h.iconEmoji, h.scheduleType, h.scheduleDays, h.timesPerWeek, h.targetType, h.targetValue,
               h.reminderHour, h.reminderMinute, h.archived, h.createdAt
        FROM habits h
        INNER JOIN categories c ON c.id = h.categoryId
        WHERE h.id = :habitId
        """,
    )
    suspend fun getWithCategory(habitId: Long): HabitWithCategoryEntity?

    @Query(
        """
        SELECT h.id, h.name, h.categoryId, c.name AS categoryName, c.colorArgb AS categoryColorArgb,
               h.iconEmoji, h.scheduleType, h.scheduleDays, h.timesPerWeek, h.targetType, h.targetValue,
               h.reminderHour, h.reminderMinute, h.archived, h.createdAt
        FROM habits h
        INNER JOIN categories c ON c.id = h.categoryId
        WHERE h.archived = 0
        ORDER BY h.name
        """,
    )
    suspend fun getAllActiveWithCategory(): List<HabitWithCategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: HabitEntity): Long

    @Update
    suspend fun update(habit: HabitEntity)

    @Query("UPDATE habits SET archived = 1 WHERE id = :habitId")
    suspend fun archive(habitId: Long)
}

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun observeForDate(date: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    suspend fun getForDate(date: String): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date >= :fromDate ORDER BY date DESC")
    suspend fun getForHabitSince(habitId: Long, fromDate: String): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getForHabitOnDate(habitId: Long, date: String): HabitLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: HabitLogEntity): Long

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteForHabitOnDate(habitId: Long, date: String)
}
