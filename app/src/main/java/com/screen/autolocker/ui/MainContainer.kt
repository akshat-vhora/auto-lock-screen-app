package com.screen.autolocker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.screen.autolocker.ui.screens.HistoryScreen
import com.screen.autolocker.ui.screens.SettingsScreen
import com.screen.autolocker.ui.screens.TimerScreen
import com.screen.autolocker.ui.theme.AppPalette
import com.screen.autolocker.ui.theme.backgroundBrush
import com.screen.autolocker.viewmodel.HistoryViewModel
import com.screen.autolocker.viewmodel.SettingsViewModel
import com.screen.autolocker.viewmodel.TimerViewModel

@Composable
fun MainContainer(
    palette: AppPalette,
    currentTheme: String,
    amoledPolishEnabled: Boolean,
    timerViewModel: TimerViewModel,
    settingsViewModel: SettingsViewModel,
    historyViewModel: HistoryViewModel
) {
    val navController = rememberNavController()
    val backStack = navController.currentBackStackEntryAsState().value
    val selectedScreen = AppScreen.fromRoute(backStack?.destination?.route)
    val timerState by timerViewModel.state.collectAsState()
    val remaining by timerViewModel.remaining.collectAsState()

    fun navigateTo(screen: AppScreen) {
        navController.navigate(screen.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush(palette))
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .graphicsLayer {
                    translationX = 80f
                    translationY = -80f
                    alpha = 0.55f
                }
                .clip(CircleShape)
                .background(palette.secondary)
        )

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent,
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .navigationBarsPadding()
                        .padding(top = 10.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        shape = RoundedCornerShape(30.dp),
                        color = palette.surface.copy(alpha = 0.96f),
                        shadowElevation = 0.dp,
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AppScreen.entries.forEach { screen ->
                                NavItem(
                                    palette = palette,
                                    label = when (screen) {
                                        AppScreen.TIMER -> "Timer"
                                        AppScreen.HISTORY -> "History"
                                        AppScreen.SETTINGS -> "Settings"
                                    },
                                    icon = when (screen) {
                                        AppScreen.TIMER -> Icons.Default.PlayArrow
                                        AppScreen.HISTORY -> Icons.Default.AccessTime
                                        AppScreen.SETTINGS -> Icons.Default.Settings
                                    },
                                    selected = selectedScreen == screen,
                                    onClick = { navigateTo(screen) }
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
                    .consumeWindowInsets(padding)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            ) {
                NavHost(
                    navController = navController,
                    startDestination = AppScreen.TIMER.route
                ) {
                    composable(AppScreen.TIMER.route) {
                        TimerScreen(
                            palette = palette,
                            onOpenSettings = { navigateTo(AppScreen.SETTINGS) },
                            viewModel = timerViewModel
                        )
                    }
                    composable(AppScreen.HISTORY.route) {
                        HistoryScreen(
                            palette = palette,
                            viewModel = historyViewModel,
                            isTimerActive = timerState.isActive,
                            remaining = remaining,
                            onReuseTimer = {
                                timerViewModel.reuseMinutes(it)
                                navigateTo(AppScreen.TIMER)
                            }
                        )
                    }
                    composable(AppScreen.SETTINGS.route) {
                        SettingsScreen(
                            palette = palette,
                            currentTheme = currentTheme,
                            amoledPolishEnabled = amoledPolishEnabled,
                            viewModel = settingsViewModel,
                            historyViewModel = historyViewModel,
                            isTimerActive = timerState.isActive,
                            remaining = remaining
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    palette: AppPalette,
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .width(88.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (selected) palette.primary.copy(alpha = 0.16f) else Color.Transparent)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) palette.primary else palette.muted
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) palette.primary else palette.muted
        )
    }
}
