package com.screen.autolocker.ui.screens.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.screen.autolocker.ui.theme.AppPalette

@Composable
fun HomeWidgetSettingsSection(palette: AppPalette) {
    SectionTitle("Home Screen Widget", palette)
    ThemedCard(palette) {
        Text(
            text = "Add the Quick Start widget from your launcher to start common timers in one tap.",
            color = palette.muted,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
