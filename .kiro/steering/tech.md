# Tech Stack

## Language & Platform
- **Kotlin** (code style: official)
- **Android** — minSdk 28, targetSdk/compileSdk 34, JVM target 17

## Build System
- **Gradle** with Kotlin DSL (`build.gradle.kts`)
- AGP 8.6.0, Kotlin Android plugin 1.9.22
- Single module: `:app`
- Release signing configured via `release.properties` + `release.keystore`

## Key Libraries
| Library | Version | Purpose |
|---|---|---|
| Jetpack Compose BOM | 2024.02.01 | UI framework |
| Material3 | (BOM) | Design system |
| `activity-compose` | 1.8.2 | Compose entry point |
| `lifecycle-viewmodel-compose` | 2.7.0 | ViewModel integration |
| `datastore-preferences` | 1.1.1 | Timer state + settings persistence |
| `room` | (check build.gradle) | History session storage |
| `kotlinx-coroutines-android` | 1.7.3 | Async/flow |
| `core-ktx` | 1.12.0 | Android KTX extensions |
| `material` (View) | 1.11.0 | Minimal legacy use |
| `material-icons-extended` | (BOM) | Extended icon set |

## Compose Compiler
- `kotlinCompilerExtensionVersion = "1.5.8"`
- `buildFeatures { compose = true }`

## Build Variants
- `debug` — minification disabled
- `release` — R8/ProGuard enabled (`proguard-android-optimize.txt` + `proguard-rules.pro`), signing from `release.properties`

## Common Commands
```bash
# Assemble debug APK
./gradlew assembleDebug

# Assemble release APK
./gradlew assembleRelease

# Install debug on connected device
./gradlew installDebug

# Run lint
./gradlew lint

# Clean build outputs
./gradlew clean
```
