package com.gollan.fadesleeptimer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_sessions")
data class SleepSession(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    val timestamp_end: Long,
    val duration_minutes: Int,
    val day_of_week: String // e.g., "Monday"
)
