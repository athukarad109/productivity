package com.productivitytracker.habits.domain.model

enum class ScheduleType {
    DAILY,
    WEEKDAYS,
    WEEKENDS,
    SPECIFIC_DAYS,
    TIMES_PER_WEEK,
}

enum class TargetType {
    BINARY,
    COUNT,
    DURATION,
}

enum class LogStatus {
    DONE,
    SKIPPED,
    PARTIAL,
}

data class Category(
    val id: Long = 0,
    val name: String,
    val colorArgb: Long,
)

data class Habit(
    val id: Long = 0,
    val name: String,
    val categoryId: Long,
    val categoryName: String = "",
    val categoryColorArgb: Long = 0xFF6366F1,
    val iconEmoji: String = "✓",
    val scheduleType: ScheduleType = ScheduleType.DAILY,
    val scheduleDays: List<Int> = emptyList(),
    val timesPerWeek: Int = 7,
    val targetType: TargetType = TargetType.BINARY,
    val targetValue: Int = 1,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

data class HabitLog(
    val id: Long = 0,
    val habitId: Long,
    val date: String,
    val status: LogStatus,
    val value: Int = 0,
    val note: String = "",
)

data class HabitWithLog(
    val habit: Habit,
    val log: HabitLog?,
    val isDueToday: Boolean,
)

data class HabitStats(
    val habit: Habit,
    val currentStreak: Int,
    val longestStreak: Int,
    val completionRate7d: Float,
    val logsLast30Days: List<HabitLog>,
)

data class DayHeatmapCell(
    val date: String,
    val status: LogStatus?,
    val isDue: Boolean,
)
