package com.screen.autolocker.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.screen.autolocker.service.LockDeviceAdminReceiver
import android.view.accessibility.AccessibilityManager
import android.accessibilityservice.AccessibilityServiceInfo
import com.screen.autolocker.ui.components.PremiumTimerDial
import com.screen.autolocker.ui.components.ScreenTitleBar
import com.screen.autolocker.ui.theme.AppPalette
import com.screen.autolocker.viewmodel.TimerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TimerScreen(
    palette: AppPalette,
    onOpenSettings: () -> Unit,
    viewModel: TimerViewModel
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val remaining by viewModel.remaining.collectAsState()
    val minutes by viewModel.selectedMinutes.collectAsState()
    val scrollState = rememberScrollState()
    var showPermissionDialog by remember { mutableStateOf(false) }
    var missingPermissions by remember { mutableStateOf(emptyList<String>()) }

    val progressRatio = if (state.isActive && state.totalDurationMs > 0L) {
        remaining.toFloat() / state.totalDurationMs
    } else {
        1f
    }

    val progressColor by animateColorAsState(
        targetValue = if (progressRatio > 0.5f) palette.primary else palette.accent,
        label = "timerProgressColor"
    )

    val buttonScale by animateFloatAsState(
        targetValue = if (state.isActive) 1.03f else 1f,
        label = "buttonScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScreenTitleBar(
                title = "Auto Lock",
                subtitle = if (state.isActive) "Protection timer is running" else "Choose your lock timer",
                palette = palette,
                showStatusBadge = true,
                isTimerActive = state.isActive,
                remaining = remaining
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = palette.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 34.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(274.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(palette.dialSurface, palette.dialSurfaceAlt)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            PremiumTimerDial(
                                color = palette,
                                isActive = state.isActive,
                                minutes = minutes,
                                remaining = remaining,
                                total = state.totalDurationMs,
                                progressColor = progressColor,
                                onMinutesChange = viewModel::updateSelectedMinutes
                            )
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    listOf(5, 10, 15, 20).forEachIndexed { index, preset ->
                        if (index > 0) Spacer(Modifier.width(10.dp))
                        AssistChip(
                            onClick = {
                                viewModel.applyPreset(preset)
                            },
                            label = {
                                Text(
                                    if (state.isActive) "+$preset" else "$preset"
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = palette.surface,
                                labelColor = palette.text
                            )
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))

                if (!state.isActive) {
                    Text(
                        text = "Will lock at ${lockTimePreview(minutes)}",
                        color = palette.muted,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(18.dp))
                }

                if (state.isActive) {
                    FilledTonalButton(
                        onClick = viewModel::togglePause,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = palette.surface,
                            contentColor = palette.text
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            text = if (state.isPaused) "Resume Timer" else "Pause Timer",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                }

                Button(
                    onClick = {
                        if (state.isActive) {
                            viewModel.stopTimer()
                            return@Button
                        }

val hasNotifications = if (Build.VERSION.SDK_INT >= 33) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        } else {
                            true
                        }
                        val isDeviceAdminGranted = LockDeviceAdminReceiver.isAdminEnabled(context)
                        val validation = viewModel.validateBeforeStart(
                            notificationsGranted = hasNotifications,
                            accessibilityGranted = isAccessibilitySystemEnabled(context),
                            deviceAdminGranted = isDeviceAdminGranted
                        )
                        if (!validation.canStart) {
                            missingPermissions = validation.missingPermissions
                            showPermissionDialog = true
                            return@Button
                        }
                        viewModel.startTimer()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .graphicsLayer {
                            scaleX = buttonScale
                            scaleY = buttonScale
                        }
                        .align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isActive) palette.accent else palette.primary,
                        contentColor = palette.buttonText
                    )
                ) {
                    Text(
                        text = if (state.isActive) "STOP" else "START",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(28.dp))
            }
        }

        if (showPermissionDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f),
                contentAlignment = Alignment.Center
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .border(
                            width = 1.dp,
                            color = palette.primary.copy(alpha = 0.28f),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = palette.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 28.dp)
                ) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(2.dp),
                        shape = RoundedCornerShape(27.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = palette.surface.copy(alpha = 0.98f)
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Permissions required",
                                color = palette.text,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "Enable the following before starting the timer:",
                                color = palette.muted
                            )
                            Spacer(Modifier.height(10.dp))
                            missingPermissions.forEach { item ->
                                Text(
                                    text = "- $item",
                                    color = palette.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showPermissionDialog = false }) {
                                    Text("Cancel", color = palette.muted)
                                }
                                Spacer(Modifier.width(4.dp))
                                TextButton(
                                    onClick = {
                                        showPermissionDialog = false
                                        onOpenSettings()
                                    }
                                ) {
                                    Text("Open Settings", color = palette.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun lockTimePreview(minutes: Float): String {
    val lockAt = System.currentTimeMillis() + (minutes.toLong() * 60_000L)
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(lockAt))
}

private fun isAccessibilitySystemEnabled(context: Context): Boolean {
    val manager = context.getSystemService(AccessibilityManager::class.java)
    val enabledServices = manager.getEnabledAccessibilityServiceList(
        AccessibilityServiceInfo.FEEDBACK_ALL_MASK
    )
    return enabledServices.any { info ->
        info.resolveInfo.serviceInfo.packageName == context.packageName &&
                info.resolveInfo.serviceInfo.name == "com.screen.autolocker.service.AccessibilityLockService"
    }
}
