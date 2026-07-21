package com.screen.autolocker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.screen.autolocker.backup.BackupRepository
import com.screen.autolocker.backup.BackupResult
import com.screen.autolocker.data.SettingsState
import com.screen.autolocker.data.SettingsRepository
import com.screen.autolocker.service.OverlayServiceController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository,
    private val backupRepository: BackupRepository,
    private val overlayServiceController: OverlayServiceController
) : ViewModel() {

    val state: StateFlow<SettingsState> = repo.state.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SettingsState()
    )

    fun setTheme(theme: String) = viewModelScope.launch { repo.setTheme(theme) }

    fun setAmoledPolish(enabled: Boolean) = viewModelScope.launch { repo.setAmoledPolish(enabled) }

    fun setOverlayEnabled(enabled: Boolean) = viewModelScope.launch { repo.setOverlayEnabled(enabled) }

    fun applyOverlayEnabled(enabled: Boolean) = viewModelScope.launch {
        repo.setOverlayEnabled(enabled)
        overlayServiceController.applyEnabled(enabled)
    }

    fun saveThemePreset(name: String, value: String) =
        viewModelScope.launch { repo.saveThemePreset(name, value) }

    fun deleteThemePreset(name: String) =
        viewModelScope.launch { repo.deleteThemePreset(name) }

    fun backupThemes(): BackupResult = backupRepository.backupThemes()

    fun restoreThemes(): BackupResult = backupRepository.restoreThemes()
}
