package com.screen.autolocker.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.screen.autolocker.data.TimerRepository
import com.screen.autolocker.timer.TimerScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AutoLockService : Service() {
    @Inject lateinit var timerRepository: TimerRepository
    @Inject lateinit var timerScheduler: TimerScheduler
    @Inject lateinit var helper: TimerNotificationHelper

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observerJob: Job? = null
    private var tickerJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        helper.createChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, helper.baseNotification())
        observerJob?.cancel()
        val minutes = intent?.getLongExtra("time", 0L) ?: 0L

        observerJob = scope.launch {
            if (minutes > 0L) {
                val current = timerRepository.state.first()
                if (!current.isActive) {
                    timerRepository.start(minutes)
                }
            }
            timerRepository.state.collectLatest { state ->
                if (!state.isActive) {
                    tickerJob?.cancel()
                    timerScheduler.cancel()
                    helper.cancelWarning()
                    stopSelf()
                    return@collectLatest
                }
                if (!state.isPaused) {
                    timerScheduler.schedule(state.endTime)
                } else {
                    timerScheduler.cancel()
                    helper.cancelWarning()
                }
                helper.updateTimer(state)
                syncTicker(state)
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        observerJob?.cancel()
        tickerJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    private fun syncTicker(state: com.screen.autolocker.data.TimerState) {
        tickerJob?.cancel()
        if (state.isPaused) return
        val remaining = (state.endTime - System.currentTimeMillis()).coerceAtLeast(0L)
        if (remaining > 60_000L) return

        tickerJob = scope.launch {
            while (isActive) {
                val latest = timerRepository.state.first()
                if (!latest.isActive || latest.isPaused) break
                val latestRemaining = (latest.endTime - System.currentTimeMillis()).coerceAtLeast(0L)
                if (latestRemaining > 60_000L) break
                helper.updateTimer(latest)
                if (latestRemaining <= 0L) break
                delay(1000L)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_ID = 100
        const val WARNING_NOTIFICATION_ID = 101
    }
}
