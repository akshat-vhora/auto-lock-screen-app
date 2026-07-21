# Screen Auto Locker

An Android app that automatically locks your device screen after a set timer. Built with Jetpack Compose and Material 3.

## Features

- **Timer-based auto lock** — set 1–120 minutes, device locks when timer hits zero
- **Live countdown** — real-time remaining display with pause/resume/extend
- **History tracking** — session log with minutes, status, and timestamps
- **Multiple themes** — Purple, Blue, Sunset, Forest
- **AMOLED polish** — true blacks for dark theme
- **Overlay widget** — floating controls on top of other apps
- **Home screen widget** — Quick Start widget to launch/stop timer
- **Backup & restore** — export/import settings via JSON
- **Privacy policy** — first-launch consent flow

## Permissions Required

| Permission | Purpose |
|---|---|
| Accessibility Service | Lock screen automatically (primary method) |
| Device Admin | Lock screen fallback |
| Notifications | Foreground service timer notification |
| Ignore Battery Optimizations | Keep timer running in background |
| Schedule Exact Alarms | Lock at exact time (Android 12+) |
| System Alert Window | Overlay widget |
| Foreground Service | Timer service execution |

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM with Hilt DI
- **Persistence:** Room (history), DataStore (settings, timer state)
- **Min SDK:** 28 / **Target SDK:** 34

## Build

```bash
# Debug APK
./gradlew assembleDebug

# Install on device
./gradlew installDebug

# Release APK (requires release.properties + release.keystore)
./gradlew assembleRelease
```

## License

MIT
