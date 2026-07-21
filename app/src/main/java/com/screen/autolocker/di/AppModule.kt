package com.screen.autolocker.di

import android.content.Context
import com.screen.autolocker.crash.CrashReporter
import com.screen.autolocker.data.SettingsRepository
import com.screen.autolocker.data.TimerRepository
import com.screen.autolocker.history.HistoryDatabase
import com.screen.autolocker.history.HistoryRepository
import com.screen.autolocker.service.TimerNotificationHelper
import com.screen.autolocker.timer.TimerScheduler
import com.screen.autolocker.backup.BackupRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHistoryDatabase(@ApplicationContext context: Context): HistoryDatabase {
        return HistoryDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideTimerRepository(@ApplicationContext context: Context): TimerRepository {
        return TimerRepository(context)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideHistoryRepository(database: HistoryDatabase): HistoryRepository {
        return HistoryRepository(database)
    }

    @Provides
    @Singleton
    fun provideTimerScheduler(@ApplicationContext context: Context): TimerScheduler {
        return TimerScheduler(context)
    }

    @Provides
    @Singleton
    fun provideTimerNotificationHelper(@ApplicationContext context: Context): TimerNotificationHelper {
        return TimerNotificationHelper(context)
    }

    @Provides
    @Singleton
    fun provideBackupRepository(
        @ApplicationContext context: Context,
        settingsRepository: SettingsRepository,
        historyRepository: HistoryRepository
    ): BackupRepository {
        return BackupRepository(context, settingsRepository, historyRepository)
    }

    @Provides
    @Singleton
    fun provideCrashReporter(@ApplicationContext context: Context): CrashReporter {
        return CrashReporter(context)
    }
}
