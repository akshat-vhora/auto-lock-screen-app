package com.screen.autolocker.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.screen.autolocker.ui.theme.AppPalette

@Composable
fun BackupSettingsSection(
    palette: AppPalette,
    themeBackupPath: String,
    themeMessage: String,
    historyBackupPath: String,
    historyMessage: String,
    onBackupThemes: () -> Unit,
    onRestoreThemes: () -> Unit,
    onBackupHistory: () -> Unit,
    onRestoreHistory: () -> Unit
) {
    SectionTitle("Backup", palette)
    ThemedCard(palette) {
        Text(
            "Themes",
            color = palette.text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BackupChip("Backup Themes", palette, true, onBackupThemes)
            BackupChip("Restore Themes", palette, false, onRestoreThemes)
        }
        if (themeBackupPath.isNotBlank()) {
            Text(themeBackupPath, color = palette.muted, style = MaterialTheme.typography.bodySmall)
        }
        if (themeMessage.isNotBlank()) {
            Text(themeMessage, color = palette.muted, style = MaterialTheme.typography.bodySmall)
        }

        Text(
            "History Logs",
            color = palette.text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BackupChip("Backup History", palette, true, onBackupHistory)
            BackupChip("Restore History", palette, false, onRestoreHistory)
        }
        if (historyBackupPath.isNotBlank()) {
            Text(historyBackupPath, color = palette.muted, style = MaterialTheme.typography.bodySmall)
        }
        if (historyMessage.isNotBlank()) {
            Text(historyMessage, color = palette.muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun BackupChip(
    label: String,
    palette: AppPalette,
    primary: Boolean,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (primary) palette.primary else palette.surface,
            labelColor = if (primary) palette.buttonText else palette.text
        )
    )
}
