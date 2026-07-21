package com.screen.autolocker.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerServiceController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun start(minutes: Long) {
        val intent = Intent(context, AutoLockService::class.java).putExtra("time", minutes)
        ContextCompat.startForegroundService(context, intent)
    }

    fun refresh() {
        start(0L)
    }

    fun stop() {
        context.stopService(Intent(context, AutoLockService::class.java))
    }
}
