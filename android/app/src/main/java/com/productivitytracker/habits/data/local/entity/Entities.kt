package com.productivitytracker.habits.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorArgb: Long,
)

@Entity(
    tableName = "habits",
    indices = [Index("categoryId")],
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val categoryId: Long,
    val iconEmoji: String,
    val scheduleType: String,
    val scheduleDays: String,
    val timesPerWeek: Int,
    val targetType: String,
    val targetValue: Int,
    val reminderHour: Int?,
    val reminderMinute: Int?,
    val archived: Boolean,
    val createdAt: Long,
)

@Entity(
    tableName = "habit_logs",
    indices = [
        Index(value = ["habitId", "date"], unique = true),
        Index("date"),
    ],
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: String,
    val status: String,
    val value: Int,
    val note: String,
)

data class HabitWithCategoryEntity(
    val id: Long,
    val name: String,
    val categoryId: Long,
    val categoryName: String,
    val categoryColorArgb: Long,
    val iconEmoji: String,
    val scheduleType: String,
    val scheduleDays: String,
    val timesPerWeek: Int,
    val targetType: String,
    val targetValue: Int,
    val reminderHour: Int?,
    val reminderMinute: Int?,
    val archived: Boolean,
    val createdAt: Long,
)
