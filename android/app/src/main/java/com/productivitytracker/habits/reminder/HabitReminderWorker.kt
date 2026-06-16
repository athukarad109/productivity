package com.productivitytracker.habits.reminder

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.productivitytracker.habits.data.repository.HabitRepository
import com.productivitytracker.habits.domain.DateUtils
import com.productivitytracker.habits.domain.ScheduleUtils
import com.productivitytracker.habits.domain.StreakCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val WORK_NAME = "habit_reminder_check"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<HabitReminderWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}

@HiltWorker
class HabitReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: HabitRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val now = LocalTime.now()
        val today = DateUtils.today()
        val todayHabits = repository.getTodayHabits(today).associateBy { it.habit.id }

        for (habit in repository.getAllActiveHabits()) {
            val hour = habit.reminderHour ?: continue
            val minute = habit.reminderMinute ?: continue
            if (!ScheduleUtils.isDueOn(habit, today)) continue

            val reminder = LocalTime.of(hour, minute)
            val diffMinutes = kotlin.math.abs(
                java.time.Duration.between(reminder, now).toMinutes(),
            )
            if (diffMinutes > 14) continue

            val log = todayHabits[habit.id]?.log
            if (StreakCalculator.isCompleted(habit, log)) continue

            HabitNotifier.show(applicationContext, habit.name, habit.id)
        }
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "habit_reminders"
    }
}
