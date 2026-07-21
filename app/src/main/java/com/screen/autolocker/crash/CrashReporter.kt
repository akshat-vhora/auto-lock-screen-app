package com.screen.autolocker.crash

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashReporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var installed = false

    fun install() {
        if (installed) return
        installed = true
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                val dir = File(context.filesDir, "crash_logs").apply { mkdirs() }
                val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val file = File(dir, "crash_$stamp.log")
                file.writeText(buildString {
                    appendLine("thread=${thread.name}")
                    appendLine("message=${throwable.message.orEmpty()}")
                    appendLine("stacktrace=")
                    append(throwable.stackTraceToString())
                })
            }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }
}
