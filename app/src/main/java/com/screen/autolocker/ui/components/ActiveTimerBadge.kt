package com.screen.autolocker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.screen.autolocker.ui.theme.AppPalette
import com.screen.autolocker.utils.formatTime

@Composable
fun ActiveTimerBadge(
    palette: AppPalette,
    isActive: Boolean,
    remaining: Long,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(palette.surface.copy(alpha = 0.96f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = formatTime(remaining),
            style = MaterialTheme.typography.labelLarge,
            color = palette.accent
        )
    }
}
