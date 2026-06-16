package com.productivitytracker.habits.domain

import com.productivitytracker.habits.domain.model.Habit
import com.productivitytracker.habits.domain.model.HabitLog
import com.productivitytracker.habits.domain.model.LogStatus
import com.productivitytracker.habits.domain.model.ScheduleType
import com.productivitytracker.habits.domain.model.TargetType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateUtils {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun today(): LocalDate = LocalDate.now()

    fun format(date: LocalDate): String = date.format(formatter)

    fun parse(date: String): LocalDate = LocalDate.parse(date, formatter)

    fun daysBetween(start: LocalDate, end: LocalDate): List<LocalDate> {
        val count = ChronoUnit.DAYS.between(start, end).toInt()
        return (0..count).map { start.plusDays(it.toLong()) }
    }
}

object ScheduleUtils {
    fun isDueOn(habit: Habit, date: LocalDate): Boolean {
        if (habit.archived) return false
        return when (habit.scheduleType) {
            ScheduleType.DAILY -> true
            ScheduleType.WEEKDAYS -> date.dayOfWeek.value in DayOfWeek.MONDAY.value..DayOfWeek.FRIDAY.value
            ScheduleType.WEEKENDS -> date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
            ScheduleType.SPECIFIC_DAYS -> habit.scheduleDays.contains(date.dayOfWeek.value)
            ScheduleType.TIMES_PER_WEEK -> true
        }
    }

    fun scheduleLabel(habit: Habit): String = when (habit.scheduleType) {
        ScheduleType.DAILY -> "Every day"
        ScheduleType.WEEKDAYS -> "Weekdays"
        ScheduleType.WEEKENDS -> "Weekends"
        ScheduleType.SPECIFIC_DAYS -> habit.scheduleDays
            .sorted()
            .joinToString(", ") { dayName(it) }
        ScheduleType.TIMES_PER_WEEK -> "${habit.timesPerWeek}x per week"
    }

    fun targetLabel(habit: Habit): String = when (habit.targetType) {
        TargetType.BINARY -> "Mark complete"
        TargetType.COUNT -> "${habit.targetValue} times"
        TargetType.DURATION -> "${habit.targetValue} minutes"
    }

    private fun dayName(day: Int): String = when (day) {
        1 -> "Mon"
        2 -> "Tue"
        3 -> "Wed"
        4 -> "Thu"
        5 -> "Fri"
        6 -> "Sat"
        7 -> "Sun"
        else -> "?"
    }
}

object StreakCalculator {
    fun isCompleted(habit: Habit, log: HabitLog?): Boolean {
        if (log == null) return false
        return when (log.status) {
            LogStatus.SKIPPED -> false
            LogStatus.DONE -> when (habit.targetType) {
                TargetType.BINARY -> true
                TargetType.COUNT, TargetType.DURATION -> log.value >= habit.targetValue
            }
            LogStatus.PARTIAL -> habit.targetType != TargetType.BINARY && log.value >= habit.targetValue
        }
    }

    fun currentStreak(
        habit: Habit,
        logsByDate: Map<String, HabitLog>,
        today: LocalDate = DateUtils.today(),
    ): Int {
        var streak = 0
        var date = today
        while (true) {
            if (ScheduleUtils.isDueOn(habit, date)) {
                val log = logsByDate[DateUtils.format(date)]
                if (isCompleted(habit, log)) {
                    streak++
                } else if (date == today) {
                    // Today not done yet — don't break streak
                } else {
                    break
                }
            }
            date = date.minusDays(1)
            if (ChronoUnit.DAYS.between(date, today) > 365) break
        }
        return streak
    }

    fun longestStreak(
        habit: Habit,
        logsByDate: Map<String, HabitLog>,
        startDate: LocalDate,
        endDate: LocalDate = DateUtils.today(),
    ): Int {
        var longest = 0
        var current = 0
        for (date in DateUtils.daysBetween(startDate, endDate)) {
            if (ScheduleUtils.isDueOn(habit, date)) {
                val log = logsByDate[DateUtils.format(date)]
                if (isCompleted(habit, log)) {
                    current++
                    longest = maxOf(longest, current)
                } else {
                    current = 0
                }
            }
        }
        return longest
    }

    fun completionRate(
        habit: Habit,
        logsByDate: Map<String, HabitLog>,
        days: Int,
        endDate: LocalDate = DateUtils.today(),
    ): Float {
        val start = endDate.minusDays(days.toLong() - 1)
        var due = 0
        var completed = 0
        for (date in DateUtils.daysBetween(start, endDate)) {
            if (ScheduleUtils.isDueOn(habit, date)) {
                due++
                if (isCompleted(habit, logsByDate[DateUtils.format(date)])) {
                    completed++
                }
            }
        }
        return if (due == 0) 0f else completed.toFloat() / due
    }
}
