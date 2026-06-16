package com.productivitytracker.habits.data.mapper

import com.productivitytracker.habits.data.local.entity.DayTaskEntity
import com.productivitytracker.habits.domain.model.DayTask
import com.productivitytracker.habits.domain.model.RecordType

fun DayTaskEntity.toDomain() = DayTask(
    id = id,
    date = date,
    recordType = RecordType.valueOf(recordType),
    name = name,
    categoryName = categoryName,
    startTime = startTime,
    endTime = endTime,
    duration = duration,
    notes = notes,
    sortOrder = sortOrder,
)

fun DayTask.toEntity() = DayTaskEntity(
    id = id,
    date = date,
    recordType = recordType.name,
    name = name,
    categoryName = categoryName,
    startTime = startTime,
    endTime = endTime,
    duration = duration,
    notes = notes,
    sortOrder = sortOrder,
)
