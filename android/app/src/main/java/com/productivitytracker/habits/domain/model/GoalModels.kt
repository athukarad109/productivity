package com.productivitytracker.habits.domain.model

enum class GoalPeriod {
    WEEKLY,
    MONTHLY,
}

enum class GoalMetric {
    CATEGORY_MINUTES,
    HABIT_COMPLETIONS,
    AVG_PRODUCTIVITY,
}

data class Goal(
    val id: Long = 0,
    val name: String,
    val period: GoalPeriod,
    val metric: GoalMetric,
    val targetValue: Int,
    val categoryName: String? = null,
    val habitId: Long? = null,
    val habitName: String? = null,
    val archived: Boolean = false,
)

data class GoalProgress(
    val goal: Goal,
    val currentValue: Int,
    val progress: Float,
)

enum class DashboardMode {
    WEEKLY,
    MONTHLY,
}

data class DailyUnifiedScore(
    val date: String,
    val label: String,
    val productivityScore: Int?,
    val habitScore: Int?,
    val unifiedScore: Int?,
    val plannedMinutes: Int,
    val actualMinutes: Int,
)

data class CategoryBreakdown(
    val category: String,
    val plannedMinutes: Int,
    val actualMinutes: Int,
)

data class DashboardData(
    val mode: DashboardMode,
    val dailyScores: List<DailyUnifiedScore>,
    val categoryBreakdown: List<CategoryBreakdown>,
    val habitCompletionRate: Float,
    val goals: List<GoalProgress>,
    val averageUnifiedScore: Int?,
)
