package com.screen.autolocker.ui.screens.settings

import androidx.compose.runtime.Composable
import com.screen.autolocker.ui.theme.AppPalette

@Composable
fun OverlaySettingsSection(
    palette: AppPalette,
    isOverlayEnabled: Boolean,
    onOverlayChanged: (Boolean) -> Unit
) {
    SectionTitle("Overlay Widget", palette)
    ThemedCard(palette) {
        SettingSwitchRow(
            title = "Floating Timer",
            subtitle = "Shows remaining time over other apps and can be dragged anywhere.",
            checked = isOverlayEnabled,
            palette = palette,
            onChecked = onOverlayChanged
        )
    }
}
