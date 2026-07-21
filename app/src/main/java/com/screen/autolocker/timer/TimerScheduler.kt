package com.screen.autolocker.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.screen.autolocker.service.LockAlarmReceiver
import com.screen.autolocker.service.WarningAlarmReceiver

class TimerScheduler(
    private val context: Context
) {
    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(endTime: Long) {
        cancel()
        val warningTime = endTime - WARNING_OFFSET_MS
        if (warningTime > System.currentTimeMillis()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                warningTime,
                warningPendingIntent()
            )
        }
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            endTime,
            lockPendingIntent()
        )
    }

    fun cancel() {
        alarmManager.cancel(warningPendingIntent())
        alarmManager.cancel(lockPendingIntent())
    }

    private fun warningPendingIntent(): PendingIntent {
        val intent = Intent(context, WarningAlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            WARNING_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun lockPendingIntent(): PendingIntent {
        val intent = Intent(context, LockAlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            LOCK_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val WARNING_REQUEST_CODE = 5001
        private const val LOCK_REQUEST_CODE = 5002
        private const val WARNING_OFFSET_MS = 30_000L
    }
}
