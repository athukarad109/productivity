package com.productivitytracker.habits.data.mapper

import com.productivitytracker.habits.data.local.entity.GoalEntity
import com.productivitytracker.habits.domain.model.Goal
import com.productivitytracker.habits.domain.model.GoalMetric
import com.productivitytracker.habits.domain.model.GoalPeriod

fun GoalEntity.toDomain(habitName: String? = null) = Goal(
    id = id,
    name = name,
    period = GoalPeriod.valueOf(period),
    metric = GoalMetric.valueOf(metric),
    targetValue = targetValue,
    categoryName = categoryName,
    habitId = habitId,
    habitName = habitName,
    archived = archived,
)

fun Goal.toEntity() = GoalEntity(
    id = id,
    name = name,
    period = period.name,
    metric = metric.name,
    targetValue = targetValue,
    categoryName = categoryName,
    habitId = habitId,
    archived = archived,
)
