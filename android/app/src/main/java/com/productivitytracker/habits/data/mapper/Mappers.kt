package com.productivitytracker.habits.data.mapper

import com.productivitytracker.habits.data.local.entity.CategoryEntity
import com.productivitytracker.habits.data.local.entity.HabitEntity
import com.productivitytracker.habits.data.local.entity.HabitLogEntity
import com.productivitytracker.habits.data.local.entity.HabitWithCategoryEntity
import com.productivitytracker.habits.domain.model.Category
import com.productivitytracker.habits.domain.model.Habit
import com.productivitytracker.habits.domain.model.HabitLog
import com.productivitytracker.habits.domain.model.LogStatus
import com.productivitytracker.habits.domain.model.ScheduleType
import com.productivitytracker.habits.domain.model.TargetType

fun CategoryEntity.toDomain() = Category(id = id, name = name, colorArgb = colorArgb)

fun Category.toEntity() = CategoryEntity(id = id, name = name, colorArgb = colorArgb)

fun HabitWithCategoryEntity.toDomain() = Habit(
    id = id,
    name = name,
    categoryId = categoryId,
    categoryName = categoryName,
    categoryColorArgb = categoryColorArgb,
    iconEmoji = iconEmoji,
    scheduleType = ScheduleType.valueOf(scheduleType),
    scheduleDays = if (scheduleDays.isBlank()) emptyList() else scheduleDays.split(",").map { it.toInt() },
    timesPerWeek = timesPerWeek,
    targetType = TargetType.valueOf(targetType),
    targetValue = targetValue,
    reminderHour = reminderHour,
    reminderMinute = reminderMinute,
    archived = archived,
    createdAt = createdAt,
)

fun Habit.toEntity() = HabitEntity(
    id = id,
    name = name,
    categoryId = categoryId,
    iconEmoji = iconEmoji,
    scheduleType = scheduleType.name,
    scheduleDays = scheduleDays.joinToString(","),
    timesPerWeek = timesPerWeek,
    targetType = targetType.name,
    targetValue = targetValue,
    reminderHour = reminderHour,
    reminderMinute = reminderMinute,
    archived = archived,
    createdAt = createdAt,
)

fun HabitLogEntity.toDomain() = HabitLog(
    id = id,
    habitId = habitId,
    date = date,
    status = LogStatus.valueOf(status),
    value = value,
    note = note,
)

fun HabitLog.toEntity() = HabitLogEntity(
    id = id,
    habitId = habitId,
    date = date,
    status = status.name,
    value = value,
    note = note,
)
