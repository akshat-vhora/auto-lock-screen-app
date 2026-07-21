package com.screen.autolocker.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.screen.autolocker.ui.theme.AppPalette

@Composable
fun DisplaySettingsSection(
    palette: AppPalette,
    amoledPolishEnabled: Boolean,
    onAmoledPolishChanged: (Boolean) -> Unit
) {
    SectionTitle("Display", palette)
    ThemedCard(palette) {
        SettingSwitchRow(
            title = "AMOLED polish",
            subtitle = "Deeper blacks and stronger contrast for black theme.",
            checked = amoledPolishEnabled,
            palette = palette,
            onChecked = onAmoledPolishChanged
        )
    }
}

@Composable
fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    palette: AppPalette,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = palette.text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = palette.muted, style = MaterialTheme.typography.bodySmall)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = palette.buttonText,
                checkedTrackColor = palette.primary,
                uncheckedThumbColor = palette.surface,
                uncheckedTrackColor = palette.secondary.copy(alpha = 0.55f),
                uncheckedBorderColor = palette.muted.copy(alpha = 0.5f)
            )
        )
    }
}
