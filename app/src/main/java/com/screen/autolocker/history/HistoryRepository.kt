package com.screen.autolocker.history

import com.screen.autolocker.PrefManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

data class HistoryItem(
    val id: Long,
    val minutes: Int,
    val timestamp: Long,
    val status: String,
    val extendedMinutes: Int
)

data class UsageStats(
    val sessionsToday: Int,
    val minutesToday: Int,
    val sessionsWeek: Int,
    val minutesWeek: Int
)

class HistoryRepository(
    database: HistoryDatabase
) {
    private val dao = database.historyDao()

    val history: Flow<List<HistoryItem>> = dao.observeAll().map { list ->
        list.toHistoryItems()
    }

    fun filteredHistory(query: String, statusFilter: String?): Flow<List<HistoryItem>> {
        return dao.observeFiltered(query, statusFilter).map { list ->
            list.toHistoryItems()
        }
    }

    private fun List<HistoryEntity>.toHistoryItems(): List<HistoryItem> {
        return map { entity ->
            HistoryItem(
                id = entity.id,
                minutes = entity.minutes,
                timestamp = entity.timestamp,
                status = entity.status,
                extendedMinutes = entity.extendedMinutes
            )
        }
    }

    val stats: Flow<UsageStats> = history.map { items ->
        val now = Calendar.getInstance()
        val startOfToday = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val startOfWeek = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val today = items.filter { it.timestamp >= startOfToday }
        val week = items.filter { it.timestamp >= startOfWeek }
        UsageStats(
            sessionsToday = today.size,
            minutesToday = today.sumOf { it.minutes },
            sessionsWeek = week.size,
            minutesWeek = week.sumOf { it.minutes }
        )
    }

    suspend fun addEntry(
        minutes: Int,
        status: String,
        extendedMinutes: Int
    ) {
        dao.insert(
            HistoryEntity(
                minutes = minutes,
                timestamp = System.currentTimeMillis(),
                status = status,
                extendedMinutes = extendedMinutes
            )
        )
    }

    suspend fun clear() {
        dao.clear()
    }

    suspend fun snapshot(): List<HistoryItem> {
        return dao.getAll().map { entity ->
            HistoryItem(
                id = entity.id,
                minutes = entity.minutes,
                timestamp = entity.timestamp,
                status = entity.status,
                extendedMinutes = entity.extendedMinutes
            )
        }
    }

    suspend fun replaceAll(items: List<HistoryItem>) {
        dao.clear()
        if (items.isNotEmpty()) {
            dao.insertAll(
                items.map {
                    HistoryEntity(
                        minutes = it.minutes,
                        timestamp = it.timestamp,
                        status = it.status,
                        extendedMinutes = it.extendedMinutes
                    )
                }
            )
        }
    }

    suspend fun migrateLegacyEntries(entries: List<PrefManager.HistoryEntry>) {
        if (entries.isEmpty()) return
        dao.insertAll(
            entries.map {
                HistoryEntity(
                    minutes = it.minutes,
                    timestamp = it.timestamp,
                    status = it.status,
                    extendedMinutes = it.extendedMinutes
                )
            }
        )
    }
}
