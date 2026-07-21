package com.screen.autolocker.ui.screens

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.screen.autolocker.data.SavedThemePreset
import com.screen.autolocker.ui.components.PermissionItemModel
import com.screen.autolocker.ui.components.ScreenTitleBar
import com.screen.autolocker.ui.screens.settings.BackupSettingsSection
import com.screen.autolocker.ui.screens.settings.DisplaySettingsSection
import com.screen.autolocker.ui.screens.settings.HomeWidgetSettingsSection
import com.screen.autolocker.ui.screens.settings.OverlaySettingsSection
import com.screen.autolocker.ui.screens.settings.PermissionsSettingsSection
import com.screen.autolocker.ui.screens.settings.ThemeOptionUi
import com.screen.autolocker.ui.screens.settings.ThemeSettingsSection
import com.screen.autolocker.ui.theme.AppPalette
import com.screen.autolocker.viewmodel.HistoryViewModel
import com.screen.autolocker.viewmodel.SettingsViewModel
import android.graphics.Color as AndroidColor
import java.util.Locale

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    palette: AppPalette,
    currentTheme: String,
    amoledPolishEnabled: Boolean,
    viewModel: SettingsViewModel,
    historyViewModel: HistoryViewModel,
    isTimerActive: Boolean,
    remaining: Long
) {
    val context = LocalContext.current
    val settingsState by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scrollState = rememberScrollState()
    val powerManager = context.getSystemService(PowerManager::class.java)
    val alarmManager = context.getSystemService(AlarmManager::class.java)

    var isNotificationGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= 33) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }
    var isBatteryGranted by remember {
        mutableStateOf(powerManager.isIgnoringBatteryOptimizations(context.packageName))
    }
    var isAccessibilityGranted by remember { mutableStateOf(isAccessibilityEnabled(context)) }
    var isDeviceAdminGranted by remember { mutableStateOf(com.screen.autolocker.service.LockDeviceAdminReceiver.isAdminEnabled(context)) }
    var isExactAlarmGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }
        )
    }
    var isOverlayEnabled by remember { mutableStateOf(settingsState.overlayEnabled) }
    var pendingOverlayEnable by remember { mutableStateOf(false) }
    var savedThemes by remember(settingsState.savedThemes) { mutableStateOf(settingsState.savedThemes) }
    var customThemeName by remember { mutableStateOf("") }
    var showSaveThemeDialog by remember { mutableStateOf(false) }
    var pendingDeleteTheme by remember { mutableStateOf<SavedThemePreset?>(null) }
    var showCustomThemeControls by remember { mutableStateOf(false) }
    var themeBackupPath by remember { mutableStateOf("") }
    var historyBackupPath by remember { mutableStateOf("") }
    var restoreMessageTheme by remember { mutableStateOf("") }
    var restoreMessageHistory by remember { mutableStateOf("") }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isNotificationGranted = granted
    }


    val initialColor = remember(currentTheme) { themeToColor(currentTheme) }
    var hue by remember(initialColor) { mutableFloatStateOf(initialColor.first) }
    var saturation by remember(initialColor) { mutableFloatStateOf(initialColor.second) }
    var brightness by remember(initialColor) { mutableFloatStateOf(initialColor.third) }

fun refreshPermissions() {
        isOverlayEnabled = settingsState.overlayEnabled
        isNotificationGranted = if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        isAccessibilityGranted = isAccessibilityEnabled(context)
        isDeviceAdminGranted = com.screen.autolocker.service.LockDeviceAdminReceiver.isAdminEnabled(context)
        isExactAlarmGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        savedThemes = settingsState.savedThemes
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (pendingOverlayEnable) {
                    val granted = Settings.canDrawOverlays(context)
                    viewModel.applyOverlayEnabled(granted)
                    isOverlayEnabled = granted
                    pendingOverlayEnable = false
                }
                refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        refreshPermissions()
    }

    val previewColor = remember(hue, saturation, brightness) {
        Color.hsv(hue, saturation.coerceIn(0f, 1f), brightness.coerceIn(0f, 1f))
    }

    val themeOptions = remember(savedThemes) {
        listOf(
            ThemeOptionUi("Purple", "Purple", false),
            ThemeOptionUi("Blue", "Blue", false),
            ThemeOptionUi("Sunset", "Sunset", false),
            ThemeOptionUi("Forest", "Forest", false),
            ThemeOptionUi("Black", "#000000", false)
        ) + savedThemes.map { ThemeOptionUi(it.name, it.value, true) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            ScreenTitleBar(
                title = "Settings",
                subtitle = "Theme, backup, and protection controls.",
                palette = palette,
                showStatusBadge = isTimerActive,
                isTimerActive = isTimerActive,
                remaining = remaining
            )

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                ThemeSettingsSection(
                    palette = palette,
                    currentTheme = currentTheme,
                    options = themeOptions,
                    showCustomThemeControls = showCustomThemeControls,
                    previewColor = previewColor,
                    hue = hue,
                    saturation = saturation,
                    brightness = brightness,
                    onThemeChange = viewModel::setTheme,
                    onThemeDelete = { option ->
                        pendingDeleteTheme = savedThemes.firstOrNull {
                            it.name == option.label && it.value == option.value
                        }
                    },
                    onToggleCustomTheme = { showCustomThemeControls = !showCustomThemeControls },
                    onHueChange = { hue = it },
                    onSaturationChange = { saturation = it },
                    onBrightnessChange = { brightness = it },
                    onApplyCustomTheme = { viewModel.setTheme(toHex(previewColor)) },
                    onSaveTheme = { showSaveThemeDialog = true }
                )

                Spacer(Modifier.height(20.dp))

                DisplaySettingsSection(
                    palette = palette,
                    amoledPolishEnabled = amoledPolishEnabled,
                    onAmoledPolishChanged = viewModel::setAmoledPolish
                )

                Spacer(Modifier.height(20.dp))

                BackupSettingsSection(
                    palette = palette,
                    themeBackupPath = themeBackupPath,
                    themeMessage = restoreMessageTheme,
                    historyBackupPath = historyBackupPath,
                    historyMessage = restoreMessageHistory,
                    onBackupThemes = {
                        val result = viewModel.backupThemes()
                        themeBackupPath = result.path.orEmpty()
                        restoreMessageTheme = result.message
                    },
                    onRestoreThemes = {
                        val result = viewModel.restoreThemes()
                        themeBackupPath = result.path.orEmpty()
                        restoreMessageTheme = result.message
                    },
                    onBackupHistory = {
                        val result = historyViewModel.backupHistory()
                        historyBackupPath = result.path.orEmpty()
                        restoreMessageHistory = result.message
                    },
                    onRestoreHistory = {
                        val result = historyViewModel.restoreHistory()
                        historyBackupPath = result.path.orEmpty()
                        restoreMessageHistory = result.message
                    }
                )

                Spacer(Modifier.height(20.dp))

                OverlaySettingsSection(
                    palette = palette,
                    isOverlayEnabled = isOverlayEnabled,
                    onOverlayChanged = { enabled ->
                        if (enabled && !Settings.canDrawOverlays(context)) {
                            pendingOverlayEnable = true
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                            )
                        } else {
                            isOverlayEnabled = enabled
                            viewModel.applyOverlayEnabled(enabled)
                        }
                    }
                )

                Spacer(Modifier.height(20.dp))

                HomeWidgetSettingsSection(palette)

                Spacer(Modifier.height(20.dp))

                PermissionsSettingsSection(
                    palette = palette,
                    items = listOf(
                        PermissionItemModel(
                            title = "Accessibility Lock",
                            description = "Primary locking method. Enables biometric unlock after lock.",
                            granted = isAccessibilityGranted == true,
                            buttonLabel = if (isAccessibilityGranted == true) "Manage" else "Grant",
                            onManageClick = {
                                openAccessibilitySettings(context)
                            }
                        ),
                        PermissionItemModel(
                            title = "Device Admin",
                            description = "Fallback lock if accessibility is disabled (survives force stop).",
                            granted = isDeviceAdminGranted,
                            buttonLabel = if (isDeviceAdminGranted) "Manage" else "Grant",
                            onManageClick = {
                                openDeviceAdminSettings(context)
                            }
                        ),
                        PermissionItemModel(
                            title = "Notifications",
                            description = "Required for active timer and warning notifications.",
                            granted = isNotificationGranted,
                            buttonLabel = if (isNotificationGranted) "Manage" else "Grant",
                            onManageClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isNotificationGranted) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    openNotificationSettings(context)
                                }
                            }
                        ),
                        PermissionItemModel(
                            title = "Background Battery",
                            description = "Required to prevent background killing. Also disable manufacturer battery optimization.",
                            granted = isBatteryGranted,
                            buttonLabel = if (isBatteryGranted) "Manage" else "Grant",
                            onManageClick = {
                                openBatterySettings(
                                    context,
                                    isBatteryGranted
                                )
                            }
                        ),
                        PermissionItemModel(
                            title = "Exact Alarms",
                            description = "Required for precise background lock timing.",
                            granted = isExactAlarmGranted,
                            buttonLabel = if (isExactAlarmGranted) "Manage" else "Grant",
                            onManageClick = {
                                openExactAlarmSettings(context)
                            }
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))
            }
        }

        if (showSaveThemeDialog) {
            val trimmedName = customThemeName.trim()
            val previewHex = toHex(previewColor)
            val hasInvalidSymbols = trimmedName.any { !it.isLetterOrDigit() }
            val nameExists = savedThemes.any { it.name.equals(trimmedName, true) }
            val colorExists = listOf("Purple", "Blue", "Sunset", "Forest", "#000000")
                .map { themeValueToHex(it) }
                .plus(savedThemes.map { it.value.uppercase(Locale.getDefault()) })
                .any { it.equals(previewHex, true) }

            DialogCard(palette = palette, title = "Save Theme") {
                Text(
                    text = "Use only letters and numbers.",
                    color = palette.text,
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = customThemeName,
                    onValueChange = { customThemeName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Theme name") },
                    singleLine = true,
                    isError = trimmedName.isBlank() || hasInvalidSymbols || nameExists || colorExists,
                    colors = textFieldColors(palette)
                )

                when {
                    trimmedName.isBlank() -> DialogError("Theme name cannot be empty.", palette)
                    hasInvalidSymbols -> DialogError(
                        "Theme name must use only letters and numbers.",
                        palette
                    )

                    nameExists -> DialogError("Theme name already exists.", palette)
                    colorExists -> DialogError("Theme color already exists.", palette)
                }

//                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    DialogChip("Cancel", palette.surface, palette.text) {
                        showSaveThemeDialog = false
                        customThemeName = ""
                    }
                    Spacer(Modifier.width(8.dp))
                    DialogChip("Save", palette.primary, palette.buttonText) {
                        if (trimmedName.isNotEmpty() && !hasInvalidSymbols && !nameExists && !colorExists) {
                            viewModel.saveThemePreset(trimmedName, previewHex)
                            viewModel.setTheme(previewHex)
                            customThemeName = ""
                            showSaveThemeDialog = false
                        }
                    }
                }
            }
        }

        pendingDeleteTheme?.let { theme ->
            DialogCard(palette = palette, title = "Delete Theme") {
                Text(
                    text = "Delete ${theme.name} permanently?",
                    color = palette.muted,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    DialogChip("Cancel", palette.surface, palette.text) {
                        pendingDeleteTheme = null
                    }
                    Spacer(Modifier.width(8.dp))
                    DialogChip("Delete", palette.accent, palette.buttonText) {
                        viewModel.deleteThemePreset(theme.name)
                        if (currentTheme == theme.value) {
                            viewModel.setTheme("Purple")
                        }
                        pendingDeleteTheme = null
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogCard(
    palette: AppPalette,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .border(
                    width = 1.dp,
                    color = palette.primary.copy(alpha = 0.28f),
                    shape = RoundedCornerShape(26.dp)
                ),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = palette.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(palette.surface)
                    .padding(2.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(25.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = palette.surface.copy(alpha = 0.98f)
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            title,
                            color = palette.text,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        content()
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogError(text: String, palette: AppPalette) {
    Text(text, color = palette.accent, style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun DialogChip(
    text: String,
    background: Color,
    foreground: Color,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = background,
            labelColor = foreground
        )
    )
}

@Composable
private fun textFieldColors(palette: AppPalette) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = palette.primary,
    unfocusedBorderColor = palette.secondary,
    focusedLabelColor = palette.primary,
    unfocusedLabelColor = palette.muted,
    cursorColor = palette.primary,
    focusedTextColor = palette.text,
    unfocusedTextColor = palette.text,
    errorBorderColor = palette.accent,
    errorLabelColor = palette.accent
)

private fun themeToColor(theme: String): Triple<Float, Float, Float> {
    val color = when (theme) {
        "Blue" -> Color(0xFF5D7CFF)
        "Sunset" -> Color(0xFFF28482)
        "Forest" -> Color(0xFF40916C)
        "Purple" -> Color(0xFFB57EDC)
        else -> parseThemeColor(theme) ?: Color(0xFFB57EDC)
    }
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(color.toArgb(), hsv)
    return Triple(hsv[0], hsv[1], hsv[2])
}

private fun parseThemeColor(theme: String): Color? {
    val input = theme.trim()
    if (input.startsWith("#")) {
        return runCatching { Color(AndroidColor.parseColor(input)) }.getOrNull()
    }
    return null
}

private fun toHex(color: Color): String {
    val red = (color.red * 255).toInt().coerceIn(0, 255)
    val green = (color.green * 255).toInt().coerceIn(0, 255)
    val blue = (color.blue * 255).toInt().coerceIn(0, 255)
    return String.format("#%02X%02X%02X", red, green, blue)
}

private fun themeValueToHex(theme: String): String {
    return when (theme) {
        "Blue" -> "#5D7CFF"
        "Sunset" -> "#F28482"
        "Forest" -> "#40916C"
        "Purple" -> "#B57EDC"
        else -> theme.uppercase(Locale.getDefault())
    }
}

private fun appDetailsIntent(context: Context): Intent {
    return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
}

private fun openAccessibilitySettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
}

private fun openNotificationSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
    } else {
        appDetailsIntent(context)
    }
    runCatching { context.startActivity(intent) }
        .getOrElse { context.startActivity(appDetailsIntent(context)) }
}

private fun openBatterySettings(context: Context, alreadyGranted: Boolean) {
    val manufacturer = Build.MANUFACTURER.lowercase()
    
    val intent = when {
        !alreadyGranted -> {
            Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${context.packageName}")
            )
        }
        manufacturer.contains("samsung") -> {
            Intent().apply {
                component = android.content.ComponentName(
                    "com.samsung.android.lool",
                    "com.samsung.android.sm.ui.battery.BatteryActivity"
                )
            }
        }
        manufacturer.contains("xiaomi") || manufacturer.contains("redmi") -> {
            Intent().apply {
                component = android.content.ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            }
        }
        manufacturer.contains("oneplus") -> {
            Intent().apply {
                component = android.content.ComponentName(
                    "com.oneplus.security",
                    "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
                )
            }
        }
        else -> {
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        }
    }
    runCatching { context.startActivity(intent) }
        .getOrElse { 
            runCatching { 
                context.startActivity(
                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                )
            }.getOrElse { context.startActivity(appDetailsIntent(context)) }
        }
}

private fun openExactAlarmSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
    } else {
        appDetailsIntent(context)
    }
    runCatching { context.startActivity(intent) }
        .getOrElse { context.startActivity(appDetailsIntent(context)) }
}

private fun openDeviceAdminSettings(context: Context) {
    val dm = context.getSystemService(Context.DEVICE_POLICY_SERVICE)
            as android.app.admin.DevicePolicyManager
    val admin = android.content.ComponentName(
        context,
        com.screen.autolocker.service.LockDeviceAdminReceiver::class.java
    )

    val intent = if (dm.isAdminActive(admin)) {
        Intent(Settings.ACTION_SECURITY_SETTINGS)
    } else {
        Intent(android.app.admin.DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(android.app.admin.DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
            putExtra(android.app.admin.DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Required for fallback screen locking when accessibility is disabled.")
        }
    }
    runCatching { context.startActivity(intent) }
        .getOrElse { context.startActivity(appDetailsIntent(context)) }
}

private fun isAccessibilityEnabled(context: Context): Boolean {
    val manager = context.getSystemService(AccessibilityManager::class.java)
    val enabledServices = manager.getEnabledAccessibilityServiceList(
        AccessibilityServiceInfo.FEEDBACK_ALL_MASK
    )

    return enabledServices.any { info ->
        info.resolveInfo.serviceInfo.packageName == context.packageName &&
                info.resolveInfo.serviceInfo.name == "com.screen.autolocker.service.AccessibilityLockService"
    }
}
