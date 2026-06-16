package com.productivitytracker.habits.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "day_tasks",
    indices = [Index(value = ["date", "recordType"])],
)
data class DayTaskEntity(
    @PrimaryKey val id: String,
    val date: String,
    val recordType: String,
    val name: String,
    val categoryName: String,
    val startTime: String,
    val endTime: String,
    val duration: Int,
    val notes: String,
    val sortOrder: Int,
)
