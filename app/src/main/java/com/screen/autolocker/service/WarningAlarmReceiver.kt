package com.screen.autolocker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.screen.autolocker.data.TimerRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WarningAlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var timerRepository: TimerRepository
    @Inject lateinit var timerNotificationHelper: TimerNotificationHelper

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val state = timerRepository.state.first()
                if (state.isActive && !state.isPaused && !state.warningShown) {
                    timerRepository.markWarningShown()
                    timerNotificationHelper.showWarning(state)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
