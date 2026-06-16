package com.productivitytracker.habits.domain

import com.productivitytracker.habits.domain.model.ComparisonData
import com.productivitytracker.habits.domain.model.DayTask

object TimeFormat {
    fun formatMinutes(mins: Int): String {
        if (mins == 0) return "0m"
        if (mins < 60) return "${mins}m"
        val hours = mins / 60
        val remainder = mins % 60
        return if (remainder > 0) "${hours}h ${remainder}m" else "${hours}h"
    }

    fun formatTime12h(time: String): String {
        if (time.isBlank() || !time.contains(":")) return ""
        val parts = time.split(":")
        val h = parts[0].toIntOrNull() ?: return time
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val period = if (h >= 12) "PM" else "AM"
        val hour = if (h % 12 == 0) 12 else h % 12
        return "$hour:${m.toString().padStart(2, '0')} $period"
    }

    fun minutesBetween(start: String, end: String): Int {
        if (start.isBlank() || end.isBlank()) return 0
        val (sh, sm) = start.split(":").map { it.toIntOrNull() ?: 0 }
        val (eh, em) = end.split(":").map { it.toIntOrNull() ?: 0 }
        return maxOf(0, (eh * 60 + em) - (sh * 60 + sm))
    }
}

object ComparisonCalculator {
    fun totalMinutes(tasks: List<DayTask>): Int =
        tasks.sumOf { it.duration }

    fun matchedMinutes(planned: List<DayTask>, actual: List<DayTask>): Int {
        var matched = 0
        val actualCopy = actual.toMutableList()
        for (p in planned) {
            val idx = actualCopy.indexOfFirst {
                it.categoryName.equals(p.categoryName, ignoreCase = true) &&
                    it.name.equals(p.name, ignoreCase = true)
            }
            if (idx != -1) {
                matched += minOf(p.duration, actualCopy[idx].duration)
                actualCopy.removeAt(idx)
            } else {
                val catIdx = actualCopy.indexOfFirst {
                    it.categoryName.equals(p.categoryName, ignoreCase = true)
                }
                if (catIdx != -1) {
                    matched += (minOf(p.duration, actualCopy[catIdx].duration) * 0.5).toInt()
                    actualCopy.removeAt(catIdx)
                }
            }
        }
        return matched
    }

    fun missedTasks(planned: List<DayTask>, actual: List<DayTask>): List<DayTask> =
        planned.filter { p ->
            actual.none {
                it.name.equals(p.name, ignoreCase = true) &&
                    it.categoryName.equals(p.categoryName, ignoreCase = true)
            }
        }

    fun unplannedTasks(planned: List<DayTask>, actual: List<DayTask>): List<DayTask> =
        actual.filter { a ->
            planned.none {
                it.name.equals(a.name, ignoreCase = true) &&
                    it.categoryName.equals(a.categoryName, ignoreCase = true)
            }
        }

    fun compare(date: String, planned: List<DayTask>, actual: List<DayTask>): ComparisonData {
        val planTotal = totalMinutes(planned)
        val actualTotal = totalMinutes(actual)
        val matched = matchedMinutes(planned, actual)
        val score = if (planTotal > 0) ((matched.toFloat() / planTotal) * 100).toInt() else 0
        return ComparisonData(
            date = date,
            planned = planned,
            actual = actual,
            score = score,
            matchedMinutes = matched,
            totalPlannedMinutes = planTotal,
            totalActualMinutes = actualTotal,
            missedTasks = missedTasks(planned, actual),
            unplannedTasks = unplannedTasks(planned, actual),
        )
    }
}
