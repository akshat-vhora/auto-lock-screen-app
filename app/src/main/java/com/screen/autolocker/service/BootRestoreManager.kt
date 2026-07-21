package com.screen.autolocker.service

import com.screen.autolocker.data.SettingsRepository
import com.screen.autolocker.data.TimerRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class BootRestoreManager @Inject constructor(
    private val timerRepository: TimerRepository,
    private val settingsRepository: SettingsRepository,
    private val overlayServiceController: OverlayServiceController,
    private val timerServiceController: TimerServiceController
) {
    suspend fun restore() {
        val timerState = timerRepository.state.first()
        val settings = settingsRepository.snapshot()
        if (settings.overlayEnabled) {
            overlayServiceController.start()
        }
        if (timerState.isActive) {
            timerServiceController.refresh()
        }
    }
}
