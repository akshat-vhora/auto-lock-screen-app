@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.screen.autolocker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.screen.autolocker.backup.BackupRepository
import com.screen.autolocker.backup.BackupResult
import com.screen.autolocker.history.HistoryItem
import com.screen.autolocker.history.UsageStats
import com.screen.autolocker.history.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HistoryFilterType {
    ALL, SUCCESS, STOPPED, FAILED
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repo: HistoryRepository,
    private val backupRepository: BackupRepository
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(HistoryFilterType.ALL)

    val stats: StateFlow<UsageStats> = repo.stats.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UsageStats(0, 0, 0, 0)
    )

    val history: StateFlow<List<HistoryItem>> = query
        .flatMapLatest { q ->
            filter.flatMapLatest { f ->
                repo.filteredHistory(
                    query = q.trim(),
                    statusFilter = when (f) {
                        HistoryFilterType.ALL -> null
                        HistoryFilterType.SUCCESS -> "Lock successful"
                        HistoryFilterType.STOPPED -> "Stopped"
                        HistoryFilterType.FAILED -> "Lock failed"
                    }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(value: String) {
        query.value = value
    }

    fun setFilter(value: HistoryFilterType) {
        filter.value = value
    }

    fun clearHistory() = viewModelScope.launch { repo.clear() }

    fun backupHistory(): BackupResult = backupRepository.backupHistory()

    fun restoreHistory(): BackupResult = backupRepository.restoreHistory()
}
