package com.screen.autolocker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.screen.autolocker.MainActivity
import com.screen.autolocker.R
import com.screen.autolocker.data.TimerState
import com.screen.autolocker.utils.formatLockAtTime
import com.screen.autolocker.utils.formatTimeNotification

class TimerNotificationHelper(
    private val context: Context
) {
    private val notificationManager =
        context.getSystemService(NotificationManager::class.java)

    fun createChannels() {
        val timerChannel = NotificationChannel(
            TIMER_CHANNEL_ID,
            "Auto Lock Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows active lock timer countdown"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(true)
            setSound(null, null)
            enableVibration(false)
            setLightColor(0xFF5D7CFF.toInt())
        }

        val warningChannel = NotificationChannel(
            WARNING_CHANNEL_ID,
            "Lock Warning",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when screen is about to lock"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(true)
            setLightColor(0xFFFF8A65.toInt())
        }

        val failureChannel = NotificationChannel(
            FAILURE_CHANNEL_ID,
            "Lock Failed",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifies when screen lock fails"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(true)
            setLightColor(0xFFFF5252.toInt())
        }

        notificationManager.createNotificationChannel(timerChannel)
        notificationManager.createNotificationChannel(warningChannel)
        notificationManager.createNotificationChannel(failureChannel)
    }

    fun baseNotification(): Notification {
        return NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
            .setContentTitle("Auto Lock")
            .setContentText("Tap to configure timer")
            .setSmallIcon(R.drawable.ic_lock)
            .setColor(0xFF5D7CFF.toInt())
            .setOngoing(true)
            .setContentIntent(openAppIntent())
            .build()
    }

    fun updateTimer(state: TimerState) {
        if (!state.isActive) return
        val remaining = (state.endTime - System.currentTimeMillis()).coerceAtLeast(0L)
        val timeText = formatTimeNotification(remaining)
        val lockAtTime = formatLockAtTime(state.endTime)

        val builder = NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lock)
            .setColor(0xFF5D7CFF.toInt())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(openAppIntent())
            .addAction(R.drawable.ic_lock, "+10 min", action("EXTEND"))
            .addAction(0, "Stop", action("STOP"))

        if (state.isPaused) {
            builder
                .setContentTitle("Timer Paused")
                .setContentText("Tap to resume")
        } else {
            builder
                .setContentTitle("Auto Lock")
                .setContentText("Locks at $lockAtTime ($timeText remaining)")

            if (remaining <= 10_000L) {
                builder
                    .setColor(0xFFFF8A65.toInt())
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
            }
        }

        notificationManager.notify(AutoLockService.NOTIFICATION_ID, builder.build())
    }

    fun showWarning(state: TimerState) {
        val remaining = (state.endTime - System.currentTimeMillis()).coerceAtLeast(0L)
        val timeText = formatTimeNotification(remaining)

        val builder = NotificationCompat.Builder(context, WARNING_CHANNEL_ID)
            .setContentTitle("Locking in $timeText")
            .setContentText("Wrap up what you're doing")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setColor(0xFFFF8A65.toInt())
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_VIBRATE or Notification.DEFAULT_LIGHTS)
            .setContentIntent(openAppIntent())

        if (!state.graceUsed) {
            builder
                .addAction(0, "Add 2 min", action("WAIT_BUSY"))
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Your screen will lock in $timeText.\n\nTap 'Add 2 min' for a one-time 2 minute grace period.")
                        .setBigContentTitle("Screen Locking Soon!")
                )
        } else {
            builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your screen will lock in $timeText.\n\nGrace period already used.")
            )
        }

        notificationManager.notify(AutoLockService.WARNING_NOTIFICATION_ID, builder.build())
    }

    fun cancelWarning() {
        notificationManager.cancel(AutoLockService.WARNING_NOTIFICATION_ID)
    }

    fun showFailure() {
        val builder = NotificationCompat.Builder(context, FAILURE_CHANNEL_ID)
            .setContentTitle("Lock failed")
            .setContentText("Screen didn't lock. Check permissions.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Screen lock failed to complete.\n\nPlease check:\n• Accessibility service enabled\n• Device admin enabled")
            )
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setColor(0xFFFF5252.toInt())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())
            .addAction(0, "Open Settings", action("OPEN_SETTINGS"))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.notify(FAILURE_NOTIFICATION_ID, builder.build())
        } else {
            notificationManager.notify(FAILURE_NOTIFICATION_ID, builder.build())
        }
    }

    private fun action(type: String): PendingIntent {
        val intent = Intent(context, ActionReceiver::class.java).apply { action = type }
        return PendingIntent.getBroadcast(
            context,
            type.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val FAILURE_NOTIFICATION_ID = 102
        private const val TIMER_CHANNEL_ID = "auto_lock_timer"
        private const val WARNING_CHANNEL_ID = "auto_lock_warning"
        private const val FAILURE_CHANNEL_ID = "auto_lock_failure"
    }
}