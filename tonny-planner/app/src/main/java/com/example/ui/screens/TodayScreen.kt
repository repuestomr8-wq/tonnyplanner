package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.TaskOrHabit
import com.example.model.TonnyState
import com.example.ui.components.HorizontalDateStrip
import com.example.ui.components.LevelXpHeaderCard
import com.example.ui.components.PlannerItemCard
import com.example.ui.components.TonnyAvatarImage
import com.example.ui.components.TonnyStatusCard
import com.example.ui.theme.TonnyAmber
import com.example.ui.theme.TonnyAmberBright
import com.example.ui.theme.TonnyAmberSoft
import com.example.ui.theme.TonnyBg
import com.example.ui.theme.TonnyBorder
import com.example.ui.theme.TonnyMint
import com.example.ui.theme.TonnySurface
import com.example.ui.theme.TonnyTextMuted
import com.example.ui.theme.TonnyTextPrimary
import com.example.ui.theme.TonnyTextSecondary
import com.example.ui.viewmodel.TonnyPlannerUiState
import com.example.utils.DateTimeUtils

@Composable
fun TodayScreen(
    uiState: TonnyPlannerUiState,
    onSelectDate: (String) -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToTonny: () -> Unit,
    onToggleItem: (TaskOrHabit) -> Unit,
    onEditItem: (TaskOrHabit) -> Unit,
    onFocusItem: (TaskOrHabit) -> Unit,
    onDeleteItem: (TaskOrHabit) -> Unit,
    onOpenCreateSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStrip = remember { DateTimeUtils.getHorizontalDateStrip() }
    val habits = uiState.selectedDateHabits
    val tasks = uiState.selectedDateTasks
    val use24h = uiState.userProfile.preferences.use24HourFormat

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("today_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bandeja de Hoy",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TonnyTextPrimary
                    )
                    Text(
                        text = DateTimeUtils.formatReadableDate(uiState.selectedDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TonnyTextSecondary
                    )
                }

                FilledTonalButton(
                    onClick = onOpenCreateSheet,
                    modifier = Modifier.testTag("today_add_button"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = TonnyAmberSoft,
                        contentColor = TonnyAmberBright
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Crear tarea o hábito",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nuevo", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        item {
            LevelXpHeaderCard(gamification = uiState.gamification)
        }

        item {
            TonnyStatusCard(
                gamification = uiState.gamification,
                onVoiceShortcutClick = onNavigateToTonny
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FECHA SELECCIONADA",
                        style = MaterialTheme.typography.labelMedium,
                        color = TonnyTextMuted
                    )

                    Surface(
                        onClick = onNavigateToCalendar,
                        modifier = Modifier.testTag("selected_date_hoy_calendar_link"),
                        shape = RoundedCornerShape(999.dp),
                        color = TonnyAmberSoft,
                        border = BorderStroke(1.dp, TonnyAmber.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = TonnyAmberBright,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Hoy",
                                style = MaterialTheme.typography.labelLarge,
                                color = TonnyAmberBright
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "Abrir en calendario",
                                tint = TonnyAmberBright,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                HorizontalDateStrip(
                    days = dateStrip,
                    selectedDate = uiState.selectedDate,
                    onSelectDate = onSelectDate
                )
            }
        }

        if (habits.isEmpty() && tasks.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("today_empty_state"),
                    shape = RoundedCornerShape(22.dp),
                    color = TonnySurface,
                    border = BorderStroke(1.dp, TonnyBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TonnyAvatarImage(state = TonnyState.IDLE, size = 96.dp)
                        Text(
                            text = "Día despejado con Tonny",
                            style = MaterialTheme.typography.titleLarge,
                            color = TonnyTextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "No tienes tareas ni hábitos programados para ${DateTimeUtils.formatReadableDate(uiState.selectedDate)}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TonnyTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onOpenCreateSheet,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("empty_create_manual_button"),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, TonnyBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    tint = TonnyTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Crear manual", color = TonnyTextPrimary)
                            }

                            Button(
                                onClick = onNavigateToTonny,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("empty_create_voice_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TonnyAmber,
                                    contentColor = TonnyBg
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Mic,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Dictar a Tonny", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            if (habits.isNotEmpty()) {
                item {
                    SectionHeaderRow(
                        title = "Hábitos del día",
                        completedCount = habits.count { it.completed },
                        totalCount = habits.size,
                        accentColor = TonnyMint
                    )
                }
                items(habits, key = { it.id }) { habit ->
                    PlannerItemCard(
                        item = habit,
                        use24HourFormat = use24h,
                        onToggle = { onToggleItem(habit) },
                        onEditClick = { onEditItem(habit) },
                        onFocusClick = { onFocusItem(habit) },
                        onDeleteClick = { onDeleteItem(habit) }
                    )
                }
            }

            if (tasks.isNotEmpty()) {
                item {
                    SectionHeaderRow(
                        title = "Tareas",
                        completedCount = tasks.count { it.completed },
                        totalCount = tasks.size,
                        accentColor = TonnyAmberBright
                    )
                }
                items(tasks, key = { it.id }) { task ->
                    PlannerItemCard(
                        item = task,
                        use24HourFormat = use24h,
                        onToggle = { onToggleItem(task) },
                        onEditClick = { onEditItem(task) },
                        onFocusClick = { onFocusItem(task) },
                        onDeleteClick = { onDeleteItem(task) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun SectionHeaderRow(
    title: String,
    completedCount: Int,
    totalCount: Int,
    accentColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TonnyTextPrimary
        )
        Text(
            text = "$completedCount/$totalCount completados",
            style = MaterialTheme.typography.labelMedium,
            color = accentColor
        )
    }
}
