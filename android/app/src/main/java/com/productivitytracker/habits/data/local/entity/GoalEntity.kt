package com.productivitytracker.habits.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "goals",
    indices = [Index("habitId")],
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val period: String,
    val metric: String,
    val targetValue: Int,
    val categoryName: String?,
    val habitId: Long?,
    val archived: Boolean,
)
