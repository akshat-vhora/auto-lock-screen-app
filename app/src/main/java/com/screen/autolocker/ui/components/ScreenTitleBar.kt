package com.screen.autolocker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.screen.autolocker.ui.theme.AppPalette
import com.screen.autolocker.utils.formatTime

@Composable
fun ScreenTitleBar(
    title: String,
    subtitle: String,
    palette: AppPalette,
    showStatusBadge: Boolean,
    isTimerActive: Boolean,
    remaining: Long
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = palette.text
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted
            )
        }

        if (showStatusBadge) {
            Spacer(Modifier.width(12.dp))

            Surface(
                shape = CircleShape,
                color = palette.surface,
                tonalElevation = 2.dp,
                shadowElevation = 6.dp
            ) {
                Text(
                    text = if (isTimerActive) formatTime(remaining) else "READY",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = if (isTimerActive) palette.accent else palette.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
