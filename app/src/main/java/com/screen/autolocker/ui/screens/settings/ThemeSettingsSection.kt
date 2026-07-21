package com.screen.autolocker.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.screen.autolocker.data.SavedThemePreset
import com.screen.autolocker.ui.theme.AppPalette

data class ThemeOptionUi(
    val label: String,
    val value: String,
    val removable: Boolean
)

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ThemeSettingsSection(
    palette: AppPalette,
    currentTheme: String,
    options: List<ThemeOptionUi>,
    showCustomThemeControls: Boolean,
    previewColor: Color,
    hue: Float,
    saturation: Float,
    brightness: Float,
    onThemeChange: (String) -> Unit,
    onThemeDelete: (ThemeOptionUi) -> Unit,
    onToggleCustomTheme: () -> Unit,
    onHueChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onApplyCustomTheme: () -> Unit,
    onSaveTheme: () -> Unit
) {
    SectionTitle("Themes", palette)
    ThemedCard(palette) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            options.forEach { option ->
                if (option.removable) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ThemeChip(
                            palette = palette,
                            selected = currentTheme == option.value,
                            label = option.label,
                            onClick = { onThemeChange(option.value) }
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(palette.primary.copy(alpha = 0.12f))
                                .clickable { onThemeDelete(option) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete ${option.label}",
                                tint = palette.primary
                            )
                        }
                    }
                } else {
                    ThemeChip(
                        palette = palette,
                        selected = currentTheme == option.value,
                        label = option.label,
                        onClick = { onThemeChange(option.value) }
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AssistChip(
                onClick = onToggleCustomTheme,
                label = { Text(if (showCustomThemeControls) "Hide Custom Theme" else "Set Custom Theme") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = palette.surface,
                    labelColor = palette.text
                )
            )
        }

        if (showCustomThemeControls) {
            Text(
                text = "Custom color",
                color = palette.text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(previewColor)
            )

            ColorSlider("Hue", hue, 0f..360f, onHueChange, palette)
            ColorSlider("Saturation", saturation, 0f..1f, onSaturationChange, palette)
            ColorSlider("Brightness", brightness, 0f..1f, onBrightnessChange, palette)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AssistChip(
                    onClick = onApplyCustomTheme,
                    label = { Text("Apply") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = palette.primary,
                        labelColor = palette.buttonText
                    )
                )
                AssistChip(
                    onClick = onSaveTheme,
                    label = { Text("Save Theme") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = palette.surface,
                        labelColor = palette.text
                    )
                )
            }
        }
    }
}

@Composable
fun ThemedCard(
    palette: AppPalette,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = palette.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun SectionTitle(text: String, palette: AppPalette) {
    Text(
        text = text,
        color = palette.text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(14.dp))
}

@Composable
private fun ThemeChip(
    palette: AppPalette,
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) palette.primary else palette.surface,
            labelColor = if (selected) palette.buttonText else palette.text
        )
    )
}

@Composable
private fun ColorSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    palette: AppPalette
) {
    Column {
        Text(label, color = palette.text, style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = palette.primary,
                activeTrackColor = palette.primary,
                inactiveTrackColor = palette.secondary.copy(alpha = 0.45f)
            )
        )
    }
}
