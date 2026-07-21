# Project Structure

## Root Layout
```
Screen Auto Locker/
├── app/                        # Single app module
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/screen/autolocker/
│       └── res/
├── build.gradle.kts            # Root build file (plugin declarations only)
├── settings.gradle.kts
├── release.properties          # Signing config (not committed)
└── release.keystore            # Release keystore (not committed)
```

## Source Package: `com.screen.autolocker`
```
autolocker/
├── AutoLockApp.kt              # Application subclass; DI root — owns all repositories + TimerScheduler
├── MainActivity.kt             # Single activity; hosts Compose, observes theme/settings state
├── PrefManager.kt              # Legacy SharedPreferences helper (kept for backup/restore and one-time migration)
│
├── data/
│   ├── TimeRepository.kt       # DataStore ("timer_prefs") — TimerState, TimerRepository, Keys
│   └── SettingsRepository.kt   # DataStore ("settings_prefs") — SettingsState, SavedThemePreset
│
├── history/
│   ├── HistoryDatabase.kt      # Room database (version 1, exportSchema = false)
│   ├── HistoryDao.kt           # Room DAO
│   ├── HistoryEntity.kt        # @Entity(tableName = "history_entries")
│   └── HistoryRepository.kt    # Wraps DAO; exposes Flow<List<HistoryItem>> and Flow<UsageStats>
│
├── timer/
│   └── TimerScheduler.kt       # AlarmManager wrapper for exact-alarm background lock trigger
│
├── service/
│   ├── AutoLockService.kt      # Foreground service; drives countdown loop, fires lock
│   ├── AccessibilityLockService.kt  # Accessibility service; locks via GLOBAL_ACTION_LOCK_SCREEN; exposes companion lockScreen()
│   ├── OverlayWidgetService.kt # SYSTEM_ALERT_WINDOW floating overlay (CountDownTimer + drag support)
│   ├── ActionReceiver.kt       # BroadcastReceiver for notification actions: EXTEND, STOP, WAIT_BUSY
│   ├── TimerNotificationHelper.kt   # Builds/updates timer progress and warning notifications
│   ├── WarningAlarmReceiver.kt # Receives exact alarm for the 30-second warning
│   └── QuickStartWidgetProvider.kt  # Home screen app widget
│
├── ui/
│   ├── AppScreen.kt            # Enum: TIMER, HISTORY, SETTINGS
│   ├── MainContainer.kt        # Root Compose layout; bottom nav bar, screen routing
│   ├── SplashScreen.kt         # Animated splash on cold launch
│   ├── components/
│   │   ├── ActiveTimerBadge.kt # Floating countdown badge shown on History/Settings screens
│   │   ├── PermissionList.kt   # Reusable permission status + grant button list
│   │   └── PremiumTimerDial.kt # Circular dial for timer input and progress display
│   ├── screens/
│   │   ├── TimerScreen.kt      # Main timer UI (TimerViewModel)
│   │   ├── HistoryScreen.kt    # Session history list (HistoryViewModel + TimerViewModel)
│   │   └── SettingsScreen.kt   # Theme picker, overlay, permissions (SettingsViewModel + TimerViewModel)
│   └── theme/
│       └── AppPalette.kt       # AppPalette data class, paletteFor(), backgroundBrush()
│
├── utils/
│   └── TimeUtils.kt            # formatTime(ms) / formatTimeNotification(ms)
│
└── viewmodel/
    ├── TimerViewModel.kt       # Timer state, remaining flow, start/stop/pause/resume/extend/preset
    ├── SettingsViewModel.kt    # Theme, AMOLED polish, overlay, saved theme presets
    └── HistoryViewModel.kt     # Filtered history + usage stats; HistoryFilterType enum
```

## Architecture Patterns
- **MVVM**: ViewModels → Repositories → DataStore / Room
- **Application as DI root**: `AutoLockApp` creates all repositories and `TimerScheduler` as `lateinit var`; ViewModels access them via `app as AutoLockApp`
- **Unidirectional data flow**: UI observes `StateFlow` from ViewModels; user events flow up via lambdas or direct ViewModel calls
- **No navigation library**: Screen routing is manual `rememberSaveable` enum state in `MainActivity` / `MainContainer`
- **Three persistence layers**:
  - `DataStore` (`timer_prefs`) — active timer state, survives process death
  - `DataStore` (`settings_prefs`) — theme, AMOLED, overlay, last minutes, saved presets
  - `Room` (`history_entries`) — session history (migrated from legacy `SharedPreferences` on first launch via `AutoLockApp.migrateLegacyHistory()`)
- **Service communication**: `MainActivity` starts/stops `AutoLockService` via `Intent`; notification actions route through `ActionReceiver` → repositories + `TimerScheduler`
- **Exact alarms**: `TimerScheduler` wraps `AlarmManager`; `WarningAlarmReceiver` handles the warning alarm

## Theming
- All colors flow through `AppPalette` — never hardcode colors in UI composables
- `paletteFor(theme, isDark, amoledPolish)` is the single entry point for palette creation
- Theme string is a named preset (`"Purple"`, `"Blue"`, `"Sunset"`, `"Forest"`, `"#000000"`) or a color expression (`#RRGGBB`, `rgb(...)`, `rgba(...)`)
- Pass `palette: AppPalette` down the composable tree; do not read theme from a global
- `backgroundBrush(palette)` produces the standard vertical gradient used on every screen

## Conventions
- Composable screens receive `palette` and ViewModel(s) as parameters — screens own their ViewModel references
- `PrefManager` is a legacy singleton `object`; always pass `Context` explicitly, never store it; prefer `SettingsRepository` / `HistoryRepository` for new code
- Coroutine scope in services: `CoroutineScope(SupervisorJob() + Dispatchers.Main)` with job cancellation in `onDestroy`
- `HistoryRepository` uses its own `CoroutineScope(SupervisorJob() + Dispatchers.IO)` for fire-and-forget writes
- Notification channel IDs and notification IDs are constants in the companion object of the owning service
- `SettingsRepository.snapshot()` uses `runBlocking` — only call from non-coroutine contexts (e.g. service `onStartCommand`)
- `AccessibilityLockService` exposes a `@Volatile` companion `instance`; always use `lockScreen()` static helper, never access `instance` directly
