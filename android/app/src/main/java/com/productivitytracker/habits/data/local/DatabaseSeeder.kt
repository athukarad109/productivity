package com.productivitytracker.habits.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabaseSeeder {
    private val defaultCategories = listOf(
        "Work" to 0xFF6366F1,
        "Learning" to 0xFF22C55E,
        "Exercise" to 0xFFF59E0B,
        "Personal" to 0xFFEC4899,
        "Health" to 0xFF14B8A6,
        "Social" to 0xFF8B5CF6,
        "Creative" to 0xFFF97316,
        "Admin" to 0xFF94A3B8,
    )

    fun callback(scope: CoroutineScope) = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch(Dispatchers.IO) {
                defaultCategories.forEach { (name, color) ->
                    db.execSQL(
                        "INSERT INTO categories (name, colorArgb) VALUES (?, ?)",
                        arrayOf(name, color),
                    )
                }
            }
        }
    }
}
