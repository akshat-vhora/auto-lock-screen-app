package com.screen.autolocker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.screen.autolocker.data.TimerRepository
import com.screen.autolocker.history.HistoryRepository
import com.screen.autolocker.timer.TimerScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LockAlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var timerRepository: TimerRepository
    @Inject lateinit var historyRepository: HistoryRepository
    @Inject lateinit var timerScheduler: TimerScheduler
    @Inject lateinit var timerNotificationHelper: TimerNotificationHelper
    @Inject lateinit var timerServiceController: TimerServiceController

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val state = timerRepository.state.first()
                if (!state.isActive || state.isPaused) return@launch

                val locked = retryLock(context)
                val totalMinutes = ((state.totalDurationMs + 59_999L) / 60_000L).toInt()
                historyRepository.addEntry(
                    minutes = totalMinutes,
                    status = if (locked) "Lock successful" else "Lock failed",
                    extendedMinutes = state.extendedMinutes
                )
                if (!locked) {
                    timerNotificationHelper.showFailure()
                }
                timerNotificationHelper.cancelWarning()
                timerScheduler.cancel()
                timerRepository.stop()
                timerServiceController.stop()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun retryLock(context: Context): Boolean {
        repeat(3) { attempt ->
            if (AccessibilityLockService.lockScreen()) return true
            if (LockDeviceAdminReceiver.lockNow(context)) return true
            if (attempt < 2) Thread.sleep(300)
        }
        return LockDeviceAdminReceiver.lockNow(context)
    }
}
