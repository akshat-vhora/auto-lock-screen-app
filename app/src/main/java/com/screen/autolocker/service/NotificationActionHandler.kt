package com.screen.autolocker.service

import android.app.NotificationManager
import android.content.Context
import com.screen.autolocker.data.TimerRepository
import com.screen.autolocker.history.HistoryRepository
import com.screen.autolocker.timer.TimerScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class NotificationActionHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val timerRepository: TimerRepository,
    private val historyRepository: HistoryRepository,
    private val timerScheduler: TimerScheduler,
    private val timerServiceController: TimerServiceController,
    private val timerNotificationHelper: TimerNotificationHelper
) {
    private val notificationManager: NotificationManager
        get() = context.getSystemService(NotificationManager::class.java)

    suspend fun handle(action: String?) {
        when (action) {
            "EXTEND" -> {
                timerRepository.extend()
                val state = timerRepository.state.first()
                if (state.isActive && !state.isPaused) {
                    timerScheduler.schedule(state.endTime)
                    timerNotificationHelper.updateTimer(state)
                }
            }

            "WAIT_BUSY" -> {
                timerRepository.applyGracePeriod()
                notificationManager.cancel(AutoLockService.WARNING_NOTIFICATION_ID)
                val state = timerRepository.state.first()
                if (state.isActive && !state.isPaused) {
                    timerScheduler.schedule(state.endTime)
                    timerNotificationHelper.updateTimer(state)
                }
            }

            "STOP" -> {
                val state = timerRepository.state.first()
                if (state.isActive) {
                    val totalMinutes = ((state.totalDurationMs + 59_999L) / 60_000L).toInt()
                    historyRepository.addEntry(
                        minutes = totalMinutes,
                        status = "Stopped",
                        extendedMinutes = state.extendedMinutes
                    )
                }
                timerScheduler.cancel()
                timerRepository.stop()
                notificationManager.cancel(AutoLockService.WARNING_NOTIFICATION_ID)
                timerServiceController.stop()
            }
        }
    }
}
