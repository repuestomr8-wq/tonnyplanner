package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.LevelXpHeaderCard
import com.example.ui.theme.TonnyAmber
import com.example.ui.theme.TonnyAmberBright
import com.example.ui.theme.TonnyAmberSoft
import com.example.ui.theme.TonnyBorder
import com.example.ui.theme.TonnyIndigo
import com.example.ui.theme.TonnyMint
import com.example.ui.theme.TonnyMintSoft
import com.example.ui.theme.TonnySurface
import com.example.ui.theme.TonnySurfaceElevated
import com.example.ui.theme.TonnySurfaceHighlight
import com.example.ui.theme.TonnyTextMuted
import com.example.ui.theme.TonnyTextPrimary
import com.example.ui.theme.TonnyTextSecondary
import com.example.ui.viewmodel.TonnyPlannerUiState
import com.example.utils.DateTimeUtils

@Composable
fun ProgressScreen(
    uiState: TonnyPlannerUiState,
    modifier: Modifier = Modifier
) {
    val metrics = uiState.metrics
    val completedHistory = remember(uiState.allItems) {
        uiState.allItems
            .filter { it.completed }
            .sortedByDescending { it.completedAt ?: it.scheduledDate }
    }

    val totalBonusAndBaseXp = (metrics.morningXpTotal + metrics.baseXpTotal).coerceAtLeast(1)
    val morningXpRatio = metrics.morningXpTotal.toFloat() / totalBonusAndBaseXp.toFloat()
    val baseXpRatio = metrics.baseXpTotal.toFloat() / totalBonusAndBaseXp.toFloat()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("progress_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Dashboard de Progreso",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TonnyTextPrimary
                )
                Text(
                    text = "Tu avance acumulado sin presiones ni rachas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TonnyTextSecondary
                )
            }
        }

        item {
            LevelXpHeaderCard(gamification = uiState.gamification)
        }

        // Tasks vs Habits Summary Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricStatCard(
                    title = "Tareas",
                    value = "${metrics.completedTasks}/${metrics.totalTasks}",
                    subtitle = "Completadas",
                    icon = Icons.Rounded.TaskAlt,
                    accent = TonnyAmberBright,
                    progress = if (metrics.totalTasks == 0) 0f else metrics.completedTasks.toFloat() / metrics.totalTasks.toFloat(),
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Hábitos",
                    value = "${metrics.completedHabits}/${metrics.totalHabits}",
                    subtitle = "Completados",
                    icon = Icons.Rounded.Repeat,
                    accent = TonnyMint,
                    progress = if (metrics.totalHabits == 0) 0f else metrics.completedHabits.toFloat() / metrics.totalHabits.toFloat(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Morning Multiplier x1.5 vs Base x1.0 Breakdown Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = TonnySurface,
                border = BorderStroke(1.dp, TonnyBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = TonnyMint,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "XP Matutina (x1.5) vs Base (x1.0)",
                            style = MaterialTheme.typography.titleMedium,
                            color = TonnyTextPrimary
                        )
                    }

                    // Morning row
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WbSunny,
                                    contentDescription = null,
                                    tint = TonnyMint,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Antes de 12:00 PM (${metrics.morningCompletionsCount} ítems)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TonnyTextPrimary
                                )
                            }
                            Text(
                                text = "${metrics.morningXpTotal} XP",
                                style = MaterialTheme.typography.titleSmall,
                                color = TonnyMint
                            )
                        }
                        LinearProgressIndicator(
                            progress = { morningXpRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp)),
                            color = TonnyMint,
                            trackColor = TonnySurfaceHighlight,
                            strokeCap = StrokeCap.Round
                        )
                    }

                    // Base row
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Desde 12:00 PM (${metrics.baseCompletionsCount} ítems)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TonnyTextSecondary
                            )
                            Text(
                                text = "${metrics.baseXpTotal} XP",
                                style = MaterialTheme.typography.titleSmall,
                                color = TonnyAmberBright
                            )
                        }
                        LinearProgressIndicator(
                            progress = { baseXpRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp)),
                            color = TonnyAmber,
                            trackColor = TonnySurfaceHighlight,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        // Completed History
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial completado",
                    style = MaterialTheme.typography.titleMedium,
                    color = TonnyTextPrimary
                )
                Text(
                    text = "${metrics.completedItems} en total",
                    style = MaterialTheme.typography.labelMedium,
                    color = TonnyTextMuted
                )
            }
        }

        if (completedHistory.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = TonnySurface,
                    border = BorderStroke(1.dp, TonnyBorder)
                ) {
                    Text(
                        text = "Aún no hay completados. Marca una tarea o hábito para empezar a sumar XP.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TonnyTextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(completedHistory, key = { "hist-${it.id}" }) { item ->
                val isMorningBonus = item.xpAwarded >= 15 || DateTimeUtils.isMorningTimestamp(item.completedAt)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = TonnySurfaceElevated,
                    border = BorderStroke(1.dp, TonnyBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = TonnyTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${item.type.displayLabel} · ${DateTimeUtils.formatReadableDate(item.scheduledDate)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TonnyTextMuted
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isMorningBonus) TonnyMintSoft else TonnyAmberSoft)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isMorningBonus) "x1.5" else "x1.0",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isMorningBonus) TonnyMint else TonnyAmberBright
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isMorningBonus) TonnyMint else TonnyIndigo,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "+${item.xpAwarded} XP",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TonnyTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = TonnySurface,
        border = BorderStroke(1.dp, TonnyBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = TonnyTextSecondary
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = TonnyTextPrimary
            )
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = accent,
                trackColor = TonnySurfaceHighlight,
                strokeCap = StrokeCap.Round
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TonnyTextMuted
            )
        }
    }
}
