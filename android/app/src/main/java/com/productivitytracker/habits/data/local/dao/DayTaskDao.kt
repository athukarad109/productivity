package com.productivitytracker.habits.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.productivitytracker.habits.data.local.entity.DayTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DayTaskDao {
    @Query(
        """
        SELECT * FROM day_tasks
        WHERE date = :date AND recordType = :recordType
        ORDER BY sortOrder ASC, startTime ASC, name ASC
        """,
    )
    fun observeForDate(date: String, recordType: String): Flow<List<DayTaskEntity>>

    @Query(
        """
        SELECT * FROM day_tasks
        WHERE date = :date AND recordType = :recordType
        ORDER BY sortOrder ASC, startTime ASC, name ASC
        """,
    )
    suspend fun getForDate(date: String, recordType: String): List<DayTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<DayTaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: DayTaskEntity)

    @Query("DELETE FROM day_tasks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM day_tasks WHERE date = :date AND recordType = :recordType")
    suspend fun deleteAllForDate(date: String, recordType: String)

    @Transaction
    suspend fun replaceAllForDate(date: String, recordType: String, tasks: List<DayTaskEntity>) {
        deleteAllForDate(date, recordType)
        if (tasks.isNotEmpty()) insertAll(tasks)
    }
}
