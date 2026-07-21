package com.screen.autolocker.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("timer_prefs")

object Keys {
    val END_TIME = longPreferencesKey("end_time")
    val ACTIVE = booleanPreferencesKey("active")
    val START_TIME = longPreferencesKey("start_time")
    val TOTAL_DURATION_MS = longPreferencesKey("total_duration_ms")
    val REMAINING_MS = longPreferencesKey("remaining_ms")
    val PAUSED = booleanPreferencesKey("paused")
    val WARNING_SHOWN = booleanPreferencesKey("warning_shown")
    val GRACE_USED = booleanPreferencesKey("grace_used")
    val EXTENDED_MINUTES = intPreferencesKey("extended_minutes")
}

data class TimerState(
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val totalDurationMs: Long = 0L,
    val remainingMs: Long = 0L,
    val warningShown: Boolean = false,
    val graceUsed: Boolean = false,
    val extendedMinutes: Int = 0
)

class TimerRepository(private val context: Context) {

    val state: Flow<TimerState> = context.dataStore.data.map {
        TimerState(
            isActive = it[Keys.ACTIVE] ?: false,
            isPaused = it[Keys.PAUSED] ?: false,
            startTime = it[Keys.START_TIME] ?: 0L,
            endTime = it[Keys.END_TIME] ?: 0L,
            totalDurationMs = it[Keys.TOTAL_DURATION_MS] ?: 0L,
            remainingMs = it[Keys.REMAINING_MS] ?: 0L,
            warningShown = it[Keys.WARNING_SHOWN] ?: false,
            graceUsed = it[Keys.GRACE_USED] ?: false,
            extendedMinutes = it[Keys.EXTENDED_MINUTES] ?: 0
        )
    }

    suspend fun start(minutes: Long) {
        val now = System.currentTimeMillis()
        val durationMs = minutes * 60000
        val end = now + durationMs

        context.dataStore.edit {
            it[Keys.START_TIME] = now
            it[Keys.END_TIME] = end
            it[Keys.ACTIVE] = true
            it[Keys.PAUSED] = false
            it[Keys.TOTAL_DURATION_MS] = durationMs
            it[Keys.REMAINING_MS] = durationMs
            it[Keys.WARNING_SHOWN] = false
            it[Keys.GRACE_USED] = false
            it[Keys.EXTENDED_MINUTES] = 0
        }
    }

    suspend fun extend(minutes: Long = 10) {
        val now = System.currentTimeMillis()
        context.dataStore.edit {
            val addMs = minutes * 60000
            val isPaused = it[Keys.PAUSED] ?: false
            val currentEnd = it[Keys.END_TIME] ?: now
            val currentRemaining = it[Keys.REMAINING_MS] ?: (currentEnd - now).coerceAtLeast(0L)
            val newEnd = if (isPaused) currentEnd else currentEnd + addMs
            val newRemaining = if (isPaused) currentRemaining + addMs else (newEnd - now).coerceAtLeast(0L)
            it[Keys.END_TIME] = newEnd
            it[Keys.ACTIVE] = true
            it[Keys.REMAINING_MS] = newRemaining
            it[Keys.TOTAL_DURATION_MS] = (it[Keys.TOTAL_DURATION_MS] ?: 0L) + addMs
            it[Keys.EXTENDED_MINUTES] = (it[Keys.EXTENDED_MINUTES] ?: 0) + minutes.toInt()
            if (newRemaining > 30_000L) {
                it[Keys.WARNING_SHOWN] = false
            }
        }
    }

    suspend fun markWarningShown() {
        context.dataStore.edit {
            it[Keys.WARNING_SHOWN] = true
        }
    }

    suspend fun applyGracePeriod(minutes: Long = 2) {
        val now = System.currentTimeMillis()
        context.dataStore.edit {
            val addMs = minutes * 60000
            val isPaused = it[Keys.PAUSED] ?: false
            val currentEnd = it[Keys.END_TIME] ?: now
            val currentRemaining = it[Keys.REMAINING_MS] ?: (currentEnd - now).coerceAtLeast(0L)
            val newEnd = if (isPaused) currentEnd else currentEnd + addMs
            val newRemaining = if (isPaused) currentRemaining + addMs else (newEnd - now).coerceAtLeast(0L)
            it[Keys.END_TIME] = newEnd
            it[Keys.ACTIVE] = true
            it[Keys.GRACE_USED] = true
            it[Keys.REMAINING_MS] = newRemaining
            it[Keys.TOTAL_DURATION_MS] = (it[Keys.TOTAL_DURATION_MS] ?: 0L) + addMs
            it[Keys.EXTENDED_MINUTES] = (it[Keys.EXTENDED_MINUTES] ?: 0) + minutes.toInt()
            if (newRemaining > 30_000L) {
                it[Keys.WARNING_SHOWN] = false
            }
        }
    }

    suspend fun pause() {
        val now = System.currentTimeMillis()
        context.dataStore.edit {
            val end = it[Keys.END_TIME] ?: now
            it[Keys.REMAINING_MS] = (end - now).coerceAtLeast(0L)
            it[Keys.PAUSED] = true
            it[Keys.WARNING_SHOWN] = false
        }
    }

    suspend fun resume() {
        val now = System.currentTimeMillis()
        context.dataStore.edit {
            val remaining = it[Keys.REMAINING_MS] ?: 0L
            it[Keys.START_TIME] = now
            it[Keys.END_TIME] = now + remaining
            it[Keys.PAUSED] = false
            it[Keys.WARNING_SHOWN] = false
        }
    }

    suspend fun stop() {
        context.dataStore.edit { it.clear() }
    }
}
