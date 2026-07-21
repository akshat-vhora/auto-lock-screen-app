package com.screen.autolocker.service

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.screen.autolocker.MainActivity
import com.screen.autolocker.R

class QuickStartWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(appWidgetId, buildViews(context))
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_START_10 -> startTimer(context, 10L)
            ACTION_START_25 -> startTimer(context, 25L)
            ACTION_START_50 -> startTimer(context, 50L)
        }

        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, QuickStartWidgetProvider::class.java))
        if (ids.isNotEmpty()) {
            onUpdate(context, manager, ids)
        }
    }

    private fun buildViews(context: Context): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_quick_start).apply {
            setOnClickPendingIntent(R.id.widget_open_app, activityIntent(context))
            setOnClickPendingIntent(R.id.widget_10, actionIntent(context, ACTION_START_10))
            setOnClickPendingIntent(R.id.widget_25, actionIntent(context, ACTION_START_25))
            setOnClickPendingIntent(R.id.widget_50, actionIntent(context, ACTION_START_50))
        }
    }

    private fun actionIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, QuickStartWidgetProvider::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun activityIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun startTimer(context: Context, minutes: Long) {
        val intent = Intent(context, AutoLockService::class.java).apply {
            putExtra("time", minutes)
        }
        context.startForegroundService(intent)
    }

    companion object {
        private const val ACTION_START_10 = "com.screen.autolocker.widget.START_10"
        private const val ACTION_START_25 = "com.screen.autolocker.widget.START_25"
        private const val ACTION_START_50 = "com.screen.autolocker.widget.START_50"
    }
}
