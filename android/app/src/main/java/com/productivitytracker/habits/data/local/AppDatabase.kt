package com.productivitytracker.habits.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.productivitytracker.habits.data.local.dao.CategoryDao
import com.productivitytracker.habits.data.local.dao.DayTaskDao
import com.productivitytracker.habits.data.local.dao.HabitDao
import com.productivitytracker.habits.data.local.dao.HabitLogDao
import com.productivitytracker.habits.data.local.entity.CategoryEntity
import com.productivitytracker.habits.data.local.entity.DayTaskEntity
import com.productivitytracker.habits.data.local.entity.HabitEntity
import com.productivitytracker.habits.data.local.entity.HabitLogEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS day_tasks (
                id TEXT NOT NULL PRIMARY KEY,
                date TEXT NOT NULL,
                recordType TEXT NOT NULL,
                name TEXT NOT NULL,
                categoryName TEXT NOT NULL,
                startTime TEXT NOT NULL,
                endTime TEXT NOT NULL,
                duration INTEGER NOT NULL,
                notes TEXT NOT NULL,
                sortOrder INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_day_tasks_date_recordType ON day_tasks(date, recordType)",
        )
    }
}

@Database(
    entities = [
        CategoryEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        DayTaskEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun dayTaskDao(): DayTaskDao
}
