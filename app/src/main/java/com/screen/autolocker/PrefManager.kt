package com.screen.autolocker

import android.content.Context

object PrefManager {

    private const val PREF = "auto_lock_prefs"
    private const val KEY_HISTORY = "history"

    data class HistoryEntry(
        val minutes: Int,
        val timestamp: Long,
        val status: String,
        val extendedMinutes: Int
    )

    fun getHistory(context: Context): List<HistoryEntry> {
        val raw = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_HISTORY, "")
            .orEmpty()

        return raw.split(";;")
            .mapNotNull {
                val parts = it.split("|")
                val minutes = parts.getOrNull(0)?.toIntOrNull()
                val time = parts.getOrNull(1)?.toLongOrNull()
                if (minutes == null || time == null) {
                    null
                } else {
                    val statusField = parts.getOrNull(2)
                    val resolvedStatus = when (statusField) {
                        null -> "Lock successful"
                        "true" -> "Lock successful"
                        "false" -> "Lock failed"
                        else -> statusField
                    }
                    HistoryEntry(
                        minutes = minutes,
                        timestamp = time,
                        status = resolvedStatus,
                        extendedMinutes = parts.getOrNull(3)?.toIntOrNull() ?: 0
                    )
                }
            }
    }

    fun clearLegacyHistory(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_HISTORY)
            .commit()
    }
}
