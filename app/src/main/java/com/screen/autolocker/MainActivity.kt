package com.screen.autolocker

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.screen.autolocker.data.SettingsRepository
import com.screen.autolocker.ui.MainContainer
import com.screen.autolocker.ui.PrivacyPolicyScreen
import com.screen.autolocker.ui.SplashScreen
import com.screen.autolocker.ui.theme.paletteFor
import com.screen.autolocker.viewmodel.HistoryViewModel
import com.screen.autolocker.viewmodel.SettingsViewModel
import com.screen.autolocker.viewmodel.TimerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val timerViewModel: TimerViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT
        window.isNavigationBarContrastEnforced = false

        setContent {
            val isDark = isSystemInDarkTheme()
            val settings by settingsViewModel.state.collectAsState()
            val palette = remember(settings.theme, isDark, settings.amoledPolish) {
                paletteFor(settings.theme, isDark, settings.amoledPolish)
            }
            var screen by rememberSaveable { mutableStateOf("SPLASH") }

            when (screen) {
                "SPLASH" -> SplashScreen(palette = palette) {
                    screen = if (settings.privacyAccepted) "MAIN" else "PRIVACY"
                }
                "PRIVACY" -> PrivacyPolicyScreen(
                    palette = palette,
                    onAccept = {
                        runBlocking { settingsRepository.setPrivacyAccepted(true) }
                        screen = "MAIN"
                    },
                    onDecline = {
                        finish()
                    }
                )
                else -> MainContainer(
                    palette = palette,
                    currentTheme = settings.theme,
                    amoledPolishEnabled = settings.amoledPolish,
                    timerViewModel = timerViewModel,
                    settingsViewModel = settingsViewModel,
                    historyViewModel = historyViewModel
                )
            }
        }
    }
}
