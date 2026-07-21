package com.screen.autolocker

import android.app.Application
import com.screen.autolocker.history.HistoryRepository
import com.screen.autolocker.crash.CrashReporter
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class AutoLockApp : Application() {

    @Inject
    lateinit var historyRepository: HistoryRepository

    @Inject
    lateinit var crashReporter: CrashReporter

    override fun onCreate() {
        super.onCreate()
        crashReporter.install()
        migrateLegacyHistory()
    }

    private fun migrateLegacyHistory() {
        val legacy = PrefManager.getHistory(this)
        if (legacy.isEmpty()) return
        runBlocking {
            historyRepository.migrateLegacyEntries(legacy)
        }
        PrefManager.clearLegacyHistory(this)
    }
}
