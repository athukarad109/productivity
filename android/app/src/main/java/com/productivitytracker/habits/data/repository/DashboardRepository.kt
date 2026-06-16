package com.productivitytracker.habits.data.repository

import com.productivitytracker.habits.data.local.dao.DayTaskDao
import com.productivitytracker.habits.data.local.dao.HabitDao
import com.productivitytracker.habits.data.local.dao.HabitLogDao
import com.productivitytracker.habits.data.mapper.toDomain
import com.productivitytracker.habits.domain.DashboardCalculator
import com.productivitytracker.habits.domain.DateUtils
import com.productivitytracker.habits.domain.ScheduleUtils
import com.productivitytracker.habits.domain.StreakCalculator
import com.productivitytracker.habits.domain.model.DashboardData
import com.productivitytracker.habits.domain.model.DashboardMode
import com.productivitytracker.habits.domain.model.RecordType
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val dayTaskDao: DayTaskDao,
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val goalRepository: GoalRepository,
) {
    suspend fun load(mode: DashboardMode, ref: LocalDate = DateUtils.today()): DashboardData {
        val dates = DashboardCalculator.dateRange(mode, ref)
        val from = DateUtils.format(dates.first())
        val to = DateUtils.format(dates.last())

        val plannedEntities = dayTaskDao.getBetween(from, to, RecordType.PLAN.name)
        val actualEntities = dayTaskDao.getBetween(from, to, RecordType.ACTUAL.name)
        val plannedByDate = plannedEntities.map { it.toDomain() }.groupBy { it.date }
        val actualByDate = actualEntities.map { it.toDomain() }.groupBy { it.date }

        val habits = habitDao.getAllActiveWithCategory().map { it.toDomain() }
        val allLogs = habitLogDao.getBetween(from, to).map { it.toDomain() }
        val logsByDateAndHabit = allLogs.groupBy { it.date }.mapValues { (_, logs) ->
            logs.associateBy { it.habitId }
        }

        val dailyScores = DashboardCalculator.buildDailyScores(
            dates = dates,
            plannedByDate = plannedByDate,
            actualByDate = actualByDate,
            habits = habits,
            logsByDateAndHabit = logsByDateAndHabit,
        )

        val categoryBreakdown = DashboardCalculator.categoryBreakdown(
            dates = dates,
            plannedByDate = plannedByDate,
            actualByDate = actualByDate,
        )

        var habitDue = 0
        var habitCompleted = 0
        for (date in dates) {
            val logs = logsByDateAndHabit[DateUtils.format(date)].orEmpty()
            for (habit in habits) {
                if (ScheduleUtils.isDueOn(habit, date)) {
                    habitDue++
                    if (StreakCalculator.isCompleted(habit, logs[habit.id])) habitCompleted++
                }
            }
        }
        val habitCompletionRate = if (habitDue == 0) 0f else habitCompleted.toFloat() / habitDue

        val goals = goalRepository.getGoals().map { goal ->
            DashboardCalculator.goalProgress(
                goal = goal,
                habits = habits,
                actualTasks = actualEntities.map { it.toDomain() },
                dailyScores = dailyScores,
                habitLogs = allLogs,
                ref = ref,
            )
        }

        val unifiedScores = dailyScores.mapNotNull { it.unifiedScore }
        val averageUnified = if (unifiedScores.isEmpty()) null else unifiedScores.sum() / unifiedScores.size

        return DashboardData(
            mode = mode,
            dailyScores = dailyScores,
            categoryBreakdown = categoryBreakdown,
            habitCompletionRate = habitCompletionRate,
            goals = goals,
            averageUnifiedScore = averageUnified,
        )
    }
}
