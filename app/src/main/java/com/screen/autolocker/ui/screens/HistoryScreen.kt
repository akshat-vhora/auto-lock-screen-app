package com.screen.autolocker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.border
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.screen.autolocker.ui.components.ScreenTitleBar
import com.screen.autolocker.ui.theme.AppPalette
import com.screen.autolocker.viewmodel.HistoryFilterType
import com.screen.autolocker.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(
    palette: AppPalette,
    viewModel: HistoryViewModel,
    isTimerActive: Boolean,
    remaining: Long,
    onReuseTimer: (Int) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(HistoryFilterType.ALL) }
    var showClearDialog by remember { mutableStateOf(false) }
    val filteredHistory by viewModel.history.collectAsState()
    val stats by viewModel.stats.collectAsState()

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
                title = "History",
                subtitle = "Usage stats and recent lock sessions",
                palette = palette,
                showStatusBadge = isTimerActive,
                isTimerActive = isTimerActive,
                remaining = remaining
            )

            Spacer(Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatsCard("Today", "${stats.sessionsToday} sessions", "${stats.minutesToday} min", palette)
                        StatsCard("This Week", "${stats.sessionsWeek} sessions", "${stats.minutesWeek} min", palette)
                    }
                }

                item { Spacer(Modifier.height(18.dp)) }

                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            viewModel.setQuery(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search history") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = palette.primary,
                            unfocusedBorderColor = palette.secondary,
                            focusedLabelColor = palette.primary,
                            unfocusedLabelColor = palette.muted,
                            cursorColor = palette.primary,
                            focusedTextColor = palette.text,
                            unfocusedTextColor = palette.text
                        )
                    )
                }

                item { Spacer(Modifier.height(14.dp)) }

                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HistoryFilterType.entries.forEach { option ->
                            AssistChip(
                                onClick = {
                                    filter = option
                                    viewModel.setFilter(option)
                                },
                                label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (filter == option) palette.primary else palette.surface,
                                    labelColor = if (filter == option) palette.buttonText else palette.text
                                )
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(20.dp)) }

                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = palette.surface),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            if (filteredHistory.isEmpty()) {
                                Text(
                                    text = "No matching sessions",
                                    color = palette.muted,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                filteredHistory.forEachIndexed { index, entry ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onReuseTimer(entry.minutes) }
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(palette.primary.copy(alpha = 0.16f))
                                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = (index + 1).toString(),
                                                        color = palette.text,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Spacer(Modifier.width(10.dp))

                                                Text(
                                                    text = "${entry.minutes} min",
                                                    color = palette.text,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }

                                            Text(
                                                text = relativeTime(entry.timestamp),
                                                color = palette.muted,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }

                                        Spacer(Modifier.height(3.dp))

                                        Text(
                                            text = entry.status,
                                            color = if (entry.status == "Lock successful") palette.accent else palette.muted,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )

                                        if (entry.extendedMinutes > 0) {
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = "Extended by ${entry.extendedMinutes} min",
                                                color = palette.muted,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }

                                    if (index != filteredHistory.lastIndex) {
                                        HorizontalDivider(
                                            color = palette.text.copy(alpha = 0.14f),
                                            thickness = 1.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(14.dp)) }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showClearDialog = true }
                        ) {
                            Text("Clear History", color = palette.accent)
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }

        if (showClearDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    ,
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
                    colors = CardDefaults.elevatedCardColors(containerColor = palette.surface.copy(alpha = 0.98f)),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Clear History",
                            color = palette.text,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Delete all saved history logs?",
                            color = palette.muted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showClearDialog = false }) {
                                Text("Cancel", color = palette.muted)
                            }
                            TextButton(
                                    onClick = {
                                    viewModel.clearHistory()
                                    showClearDialog = false
                                }
                            ) {
                                Text("Delete", color = palette.accent)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsCard(
    title: String,
    primaryText: String,
    secondaryText: String,
    palette: AppPalette
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = palette.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = palette.muted, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            Text(primaryText, color = palette.text, fontWeight = FontWeight.Bold)
            Text(secondaryText, color = palette.muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun relativeTime(timestamp: Long): String {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = timestamp }
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp)

    return when {
        now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR) -> "Today $time"

        now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) - then.get(Calendar.DAY_OF_YEAR) == 1 -> "Yesterday"

        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(timestamp)
    }
}
