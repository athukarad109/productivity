package com.productivitytracker.habits.domain

import com.productivitytracker.habits.domain.model.DailyUnifiedScore
import com.productivitytracker.habits.domain.model.Goal
import com.productivitytracker.habits.domain.model.GoalMetric
import com.productivitytracker.habits.domain.model.Habit
import com.productivitytracker.habits.domain.model.HabitLog
import com.productivitytracker.habits.domain.model.LogStatus
import com.productivitytracker.habits.domain.model.DayTask
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

object DashboardCalculator {
    private val labelFormatter = DateTimeFormatter.ofPattern("MMM d")

    fun periodRange(period: com.productivitytracker.habits.domain.model.GoalPeriod, ref: LocalDate = DateUtils.today()): Pair<LocalDate, LocalDate> {
        return when (period) {
            com.productivitytracker.habits.domain.model.GoalPeriod.WEEKLY -> {
                val start = ref.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                start to ref
            }
            com.productivitytracker.habits.domain.model.GoalPeriod.MONTHLY -> {
                val start = ref.withDayOfMonth(1)
                start to ref
            }
        }
    }

    fun dateRange(mode: com.productivitytracker.habits.domain.model.DashboardMode, ref: LocalDate = DateUtils.today()): List<LocalDate> {
        val days = if (mode == com.productivitytracker.habits.domain.model.DashboardMode.WEEKLY) 7 else 30
        val start = ref.minusDays(days.toLong() - 1)
        return DateUtils.daysBetween(start, ref)
    }

    fun unifiedScore(productivityScore: Int?, habitScore: Int?): Int? = when {
        productivityScore != null && habitScore != null -> (productivityScore + habitScore) / 2
        productivityScore != null -> productivityScore
        habitScore != null -> habitScore
        else -> null
    }

    fun habitScoreForDay(
        habits: List<Habit>,
        logsByHabit: Map<Long, HabitLog>,
        date: LocalDate,
    ): Int? {
        val due = habits.filter { ScheduleUtils.isDueOn(it, date) }
        if (due.isEmpty()) return null
        val completed = due.count { habit ->
            StreakCalculator.isCompleted(habit, logsByHabit[habit.id])
        }
        return ((completed.toFloat() / due.size) * 100).toInt()
    }

    fun buildDailyScores(
        dates: List<LocalDate>,
        plannedByDate: Map<String, List<DayTask>>,
        actualByDate: Map<String, List<DayTask>>,
        habits: List<Habit>,
        logsByDateAndHabit: Map<String, Map<Long, HabitLog>>,
    ): List<DailyUnifiedScore> {
        return dates.map { date ->
            val dateStr = DateUtils.format(date)
            val planned = plannedByDate[dateStr].orEmpty()
            val actual = actualByDate[dateStr].orEmpty()
            val comparison = ComparisonCalculator.compare(dateStr, planned, actual)
            val productivityScore = comparison.score.takeIf { comparison.totalPlannedMinutes > 0 }
            val habitScore = habitScoreForDay(habits, logsByDateAndHabit[dateStr].orEmpty(), date)
            DailyUnifiedScore(
                date = dateStr,
                label = date.format(labelFormatter),
                productivityScore = productivityScore,
                habitScore = habitScore,
                unifiedScore = unifiedScore(productivityScore, habitScore),
                plannedMinutes = comparison.totalPlannedMinutes,
                actualMinutes = comparison.totalActualMinutes,
            )
        }
    }

    fun categoryBreakdown(
        dates: List<LocalDate>,
        plannedByDate: Map<String, List<DayTask>>,
        actualByDate: Map<String, List<DayTask>>,
    ): List<com.productivitytracker.habits.domain.model.CategoryBreakdown> {
        val map = mutableMapOf<String, Pair<Int, Int>>()
        for (date in dates) {
            val key = DateUtils.format(date)
            for (task in plannedByDate[key].orEmpty()) {
                val entry = map.getOrDefault(task.categoryName, 0 to 0)
                map[task.categoryName] = entry.first + task.duration to entry.second
            }
            for (task in actualByDate[key].orEmpty()) {
                val entry = map.getOrDefault(task.categoryName, 0 to 0)
                map[task.categoryName] = entry.first to entry.second + task.duration
            }
        }
        return map.entries
            .map { (category, pair) ->
                com.productivitytracker.habits.domain.model.CategoryBreakdown(
                    category = category,
                    plannedMinutes = pair.first,
                    actualMinutes = pair.second,
                )
            }
            .sortedByDescending { it.actualMinutes + it.plannedMinutes }
    }

    fun goalProgress(
        goal: Goal,
        habits: List<Habit>,
        actualTasks: List<DayTask>,
        dailyScores: List<DailyUnifiedScore>,
        habitLogs: List<HabitLog>,
        ref: LocalDate = DateUtils.today(),
    ): com.productivitytracker.habits.domain.model.GoalProgress {
        val (start, end) = periodRange(goal.period, ref)
        val current = when (goal.metric) {
            GoalMetric.CATEGORY_MINUTES -> {
                val category = goal.categoryName.orEmpty()
                actualTasks
                    .filter { task ->
                        val d = DateUtils.parse(task.date)
                        !d.isBefore(start) && !d.isAfter(end) &&
                            task.categoryName.equals(category, ignoreCase = true)
                    }
                    .sumOf { it.duration }
            }
            GoalMetric.HABIT_COMPLETIONS -> {
                val habitId = goal.habitId ?: 0L
                habitLogs.count { log ->
                    log.habitId == habitId &&
                        log.status == LogStatus.DONE &&
                        run {
                            val d = DateUtils.parse(log.date)
                            !d.isBefore(start) && !d.isAfter(end)
                        }
                }
            }
            GoalMetric.AVG_PRODUCTIVITY -> {
                val scores = dailyScores.mapNotNull { row ->
                    val d = DateUtils.parse(row.date)
                    if (!d.isBefore(start) && !d.isAfter(end)) row.unifiedScore else null
                }
                if (scores.isEmpty()) 0 else scores.sum() / scores.size
            }
        }
        val progress = if (goal.targetValue <= 0) 0f else (current.toFloat() / goal.targetValue).coerceIn(0f, 1f)
        return com.productivitytracker.habits.domain.model.GoalProgress(
            goal = goal,
            currentValue = current,
            progress = progress,
        )
    }
}
