package com.productivitytracker.habits.di

import android.content.Context
import androidx.room.Room
import com.productivitytracker.habits.data.local.AppDatabase
import com.productivitytracker.habits.data.local.DatabaseSeeder
import com.productivitytracker.habits.data.local.MIGRATION_1_2
import com.productivitytracker.habits.data.local.dao.CategoryDao
import com.productivitytracker.habits.data.local.dao.DayTaskDao
import com.productivitytracker.habits.data.local.dao.HabitDao
import com.productivitytracker.habits.data.local.dao.HabitLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppScope(): CoroutineScope = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        scope: CoroutineScope,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "productivity_tracker.db",
    )
        .addCallback(DatabaseSeeder.callback(scope))
        .addMigrations(MIGRATION_1_2)
        .build()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideHabitDao(db: AppDatabase): HabitDao = db.habitDao()

    @Provides
    fun provideHabitLogDao(db: AppDatabase): HabitLogDao = db.habitLogDao()

    @Provides
    fun provideDayTaskDao(db: AppDatabase): DayTaskDao = db.dayTaskDao()
}
