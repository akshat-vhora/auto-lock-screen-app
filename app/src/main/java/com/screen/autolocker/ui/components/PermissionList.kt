package com.screen.autolocker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.screen.autolocker.ui.theme.AppPalette

data class PermissionItemModel(
    val title: String,
    val description: String,
    val granted: Boolean,
    val buttonLabel: String,
    val onManageClick: () -> Unit
)

@Composable
fun PermissionList(
    palette: AppPalette,
    items: List<PermissionItemModel>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items.forEachIndexed { index, item ->
            PermissionListItem(
                palette = palette,
                item = item
            )

            if (index != items.lastIndex) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = palette.secondary.copy(alpha = 0.35f)
                )
            }
        }
    }
}

@Composable
private fun PermissionListItem(
    palette: AppPalette,
    item: PermissionItemModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        if (item.granted) palette.accent else palette.muted.copy(alpha = 0.55f)
                    )
            )

            Spacer(Modifier.size(12.dp))

            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.text
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.muted
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (item.granted) "Granted" else "Not granted",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.granted) palette.accent else palette.muted
                )
            }
        }

        Spacer(Modifier.size(12.dp))

        Button(
            onClick = item.onManageClick,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.primary,
                contentColor = palette.buttonText
            )
        ) {
            Text(item.buttonLabel)
        }
    }
}
