package com.screen.autolocker.service

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OverlayServiceController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun start() {
        context.startService(Intent(context, OverlayWidgetService::class.java))
    }

    fun stop() {
        context.stopService(Intent(context, OverlayWidgetService::class.java))
    }

    fun applyEnabled(enabled: Boolean) {
        if (enabled) start() else stop()
    }
}
