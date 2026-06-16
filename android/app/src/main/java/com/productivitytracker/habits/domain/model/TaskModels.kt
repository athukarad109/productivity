package com.productivitytracker.habits.domain.model

enum class RecordType {
    PLAN,
    ACTUAL,
}

data class DayTask(
    val id: String,
    val date: String,
    val recordType: RecordType,
    val name: String,
    val categoryName: String,
    val startTime: String = "",
    val endTime: String = "",
    val duration: Int = 0,
    val notes: String = "",
    val sortOrder: Int = 0,
)

data class ComparisonData(
    val date: String,
    val planned: List<DayTask>,
    val actual: List<DayTask>,
    val score: Int,
    val matchedMinutes: Int,
    val totalPlannedMinutes: Int,
    val totalActualMinutes: Int,
    val missedTasks: List<DayTask>,
    val unplannedTasks: List<DayTask>,
)

enum class TaskCardVariant {
    PLANNED,
    ACTUAL,
    MISSED,
    UNPLANNED,
}
