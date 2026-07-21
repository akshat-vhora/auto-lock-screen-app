package com.screen.autolocker.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_entries")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val minutes: Int,
    val timestamp: Long,
    val status: String,
    val extendedMinutes: Int
)
