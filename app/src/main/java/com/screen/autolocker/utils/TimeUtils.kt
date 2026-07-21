package com.screen.autolocker.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTime(ms: Long): String {
    val seconds = ms / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60

    return "%02d:%02d".format(minutes, remainingSeconds)
}

fun formatTimeNotification(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return if (minutes > 0) {
        "$minutes minute${if (minutes == 1L) "" else "s"}"
    } else {
        "$seconds second${if (seconds == 1L) "" else "s"}"
    }
}

fun formatLockAtTime(timestamp: Long): String {
    return try {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
    } catch (e: Exception) {
        ""
    }
}