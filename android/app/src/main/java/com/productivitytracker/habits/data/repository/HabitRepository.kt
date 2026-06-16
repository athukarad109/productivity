package com.productivitytracker.habits.data.repository

import com.productivitytracker.habits.data.local.dao.CategoryDao
import com.productivitytracker.habits.data.local.dao.HabitDao
import com.productivitytracker.habits.data.local.dao.HabitLogDao
import com.productivitytracker.habits.data.mapper.toDomain
import com.productivitytracker.habits.data.mapper.toEntity
import com.productivitytracker.habits.domain.DateUtils
import com.productivitytracker.habits.domain.ScheduleUtils
import com.productivitytracker.habits.domain.StreakCalculator
import com.productivitytracker.habits.domain.model.Category
import com.productivitytracker.habits.domain.model.DayHeatmapCell
import com.productivitytracker.habits.domain.model.Habit
import com.productivitytracker.habits.domain.model.HabitLog
import com.productivitytracker.habits.domain.model.HabitStats
import com.productivitytracker.habits.domain.model.HabitWithLog
import com.productivitytracker.habits.domain.model.LogStatus
import com.productivitytracker.habits.domain.model.TargetType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
) {
    fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getCategories(): List<Category> =
        categoryDao.getAll().map { it.toDomain() }

    fun observeTodayHabits(date: LocalDate = DateUtils.today()): Flow<List<HabitWithLog>> {
        val dateStr = DateUtils.format(date)
        return combine(
            habitDao.observeActiveWithCategory(),
            habitLogDao.observeForDate(dateStr),
        ) { habits, logs ->
            val logsByHabit = logs.associateBy { it.habitId }
            habits.map { entity ->
                val habit = entity.toDomain()
                HabitWithLog(
                    habit = habit,
                    log = logsByHabit[habit.id]?.toDomain(),
                    isDueToday = ScheduleUtils.isDueOn(habit, date),
                )
            }.filter { it.isDueToday }
        }
    }

    suspend fun getAllActiveHabits(): List<Habit> =
        habitDao.getAllActiveWithCategory().map { it.toDomain() }

    suspend fun getTodayHabits(date: LocalDate = DateUtils.today()): List<HabitWithLog> {
        val habits = habitDao.getAllActiveWithCategory()
        val logsByHabit = habitLogDao.getForDate(DateUtils.format(date)).associateBy { it.habitId }
        return habits.map { entity ->
            val habit = entity.toDomain()
            HabitWithLog(
                habit = habit,
                log = logsByHabit[habit.id]?.toDomain(),
                isDueToday = ScheduleUtils.isDueOn(habit, date),
            )
        }.filter { it.isDueToday }
    }

    fun observeAllHabits(): Flow<List<Habit>> =
        habitDao.observeActiveWithCategory().map { list -> list.map { it.toDomain() } }

    suspend fun getHabit(habitId: Long): Habit? =
        habitDao.getWithCategory(habitId)?.toDomain()

    suspend fun saveHabit(habit: Habit): Long {
        val entity = habit.toEntity()
        return if (habit.id == 0L) {
            habitDao.insert(entity)
        } else {
            habitDao.update(entity)
            habit.id
        }
    }

    suspend fun archiveHabit(habitId: Long) {
        habitDao.archive(habitId)
    }

    suspend fun logHabit(
        habit: Habit,
        date: LocalDate = DateUtils.today(),
        status: LogStatus,
        value: Int = 0,
        note: String = "",
    ) {
        val resolvedValue = when {
            habit.targetType == TargetType.BINARY && status == LogStatus.DONE -> 1
            else -> value
        }
        habitLogDao.upsert(
            HabitLog(
                habitId = habit.id,
                date = DateUtils.format(date),
                status = status,
                value = resolvedValue,
                note = note,
            ).toEntity(),
        )
    }

    suspend fun clearLog(habitId: Long, date: LocalDate = DateUtils.today()) {
        habitLogDao.deleteForHabitOnDate(habitId, DateUtils.format(date))
    }

    suspend fun getHabitStats(habitId: Long): HabitStats? {
        val habit = getHabit(habitId) ?: return null
        val fromDate = DateUtils.format(DateUtils.today().minusDays(120))
        val logs = habitLogDao.getForHabitSince(habitId, fromDate).map { it.toDomain() }
        val logsByDate = logs.associateBy { it.date }
        val startDate = DateUtils.today().minusDays(120)
        return HabitStats(
            habit = habit,
            currentStreak = StreakCalculator.currentStreak(habit, logsByDate),
            longestStreak = StreakCalculator.longestStreak(habit, logsByDate, startDate),
            completionRate7d = StreakCalculator.completionRate(habit, logsByDate, 7),
            logsLast30Days = logs.filter { it.date >= DateUtils.format(DateUtils.today().minusDays(29)) },
        )
    }

    fun buildHeatmap(habit: Habit, logs: List<HabitLog>, days: Int = 35): List<DayHeatmapCell> {
        val logsByDate = logs.associateBy { it.date }
        val end = DateUtils.today()
        val start = end.minusDays(days.toLong() - 1)
        return DateUtils.daysBetween(start, end).map { date ->
            val dateStr = DateUtils.format(date)
            DayHeatmapCell(
                date = dateStr,
                status = logsByDate[dateStr]?.status,
                isDue = ScheduleUtils.isDueOn(habit, date),
            )
        }
    }

    suspend fun getOverallStats(): Pair<Int, Float> {
        val habits = habitDao.getAllActiveWithCategory().map { it.toDomain() }
        if (habits.isEmpty()) return 0 to 0f
        var totalRate = 0f
        for (habit in habits) {
            val fromDate = DateUtils.format(DateUtils.today().minusDays(30))
            val logs = habitLogDao.getForHabitSince(habit.id, fromDate).map { it.toDomain() }
            totalRate += StreakCalculator.completionRate(habit, logs.associateBy { it.date }, 7)
        }
        return habits.size to (totalRate / habits.size)
    }
}
