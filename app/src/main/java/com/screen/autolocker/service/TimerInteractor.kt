package com.screen.autolocker.service

import com.screen.autolocker.data.SettingsRepository
import com.screen.autolocker.data.TimerRepository
import com.screen.autolocker.history.HistoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class TimerInteractor @Inject constructor(
    private val timerRepository: TimerRepository,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val timerServiceController: TimerServiceController
) {
    suspend fun start(minutes: Long) {
        settingsRepository.setLastMinutes(minutes.toInt())
        timerServiceController.start(minutes)
    }

    suspend fun stop() {
        val current = timerRepository.state.first()
        if (current.isActive) {
            val totalMinutes = ((current.totalDurationMs + 59_999L) / 60_000L).toInt()
            historyRepository.addEntry(
                minutes = totalMinutes,
                status = "Stopped",
                extendedMinutes = current.extendedMinutes
            )
        }
        timerRepository.stop()
        timerServiceController.stop()
    }

    suspend fun togglePause() {
        val current = timerRepository.state.first()
        if (current.isPaused) {
            timerRepository.resume()
        } else {
            timerRepository.pause()
        }
        timerServiceController.refresh()
    }
}
