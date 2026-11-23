package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.TaskOrHabit
import com.example.ui.components.PlannerItemCard
import com.example.ui.theme.TonnyAmber
import com.example.ui.theme.TonnyAmberBright
import com.example.ui.theme.TonnyAmberSoft
import com.example.ui.theme.TonnyBg
import com.example.ui.theme.TonnyBorder
import com.example.ui.theme.TonnySurface
import com.example.ui.theme.TonnySurfaceElevated
import com.example.ui.theme.TonnySurfaceHighlight
import com.example.ui.theme.TonnyTextMuted
import com.example.ui.theme.TonnyTextPrimary
import com.example.ui.theme.TonnyTextSecondary
import com.example.ui.viewmodel.TonnyPlannerUiState
import com.example.utils.DateTimeUtils
import java.util.Locale

@Composable
fun CalendarScreen(
    uiState: TonnyPlannerUiState,
    onSelectDate: (String) -> Unit,
    onShiftWeek: (Int) -> Unit,
    onJumpToToday: () -> Unit,
    onToggleItem: (TaskOrHabit) -> Unit,
    onEditItem: (TaskOrHabit) -> Unit,
    onFocusItem: (TaskOrHabit) -> Unit,
    onDeleteItem: (TaskOrHabit) -> Unit,
    onAddItemAtTime: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val weekDays = remember(uiState.calendarWeekAnchor) {
        DateTimeUtils.getWeekDays(uiState.calendarWeekAnchor)
    }

    val use24h = uiState.userProfile.preferences.use24HourFormat
    val dayItems = uiState.selectedDateItems
    val unscheduledItems = dayItems.filter { DateTimeUtils.parseHourOfDay(it.startTime) == null }
    val itemsByHour = remember(dayItems) {
        dayItems.mapNotNull { item ->
            val hour = DateTimeUtils.parseHourOfDay(item.startTime)
            if (hour != null) hour to item else null
        }.groupBy({ it.first }, { it.second })
    }

    val timelineHours = remember { (6..23).toList() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Calendario",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TonnyTextPrimary
                    )
                    Text(
                        text = DateTimeUtils.formatMonthYear(uiState.calendarWeekAnchor),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TonnyTextSecondary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onJumpToToday,
                        modifier = Modifier.testTag("calendar_today_chip"),
                        shape = RoundedCornerShape(12.dp),
                        color = TonnySurfaceElevated,
                        border = BorderStroke(1.dp, TonnyBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Today,
                                contentDescription = null,
                                tint = TonnyAmberBright,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Hoy",
                                style = MaterialTheme.typography.labelLarge,
                                color = TonnyTextPrimary
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { onAddItemAtTime(null) },
                        modifier = Modifier.testTag("calendar_add_button"),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = TonnyAmberSoft,
                            contentColor = TonnyAmberBright
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Agregar al calendario",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Añadir")
                    }
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = TonnySurface,
                border = BorderStroke(1.dp, TonnyBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onShiftWeek(-1) },
                            modifier = Modifier
                                .testTag("calendar_prev_week")
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                contentDescription = "Semana anterior",
                                tint = TonnyTextSecondary
                            )
                        }

                        Text(
                            text = DateTimeUtils.formatReadableDate(uiState.selectedDate),
                            style = MaterialTheme.typography.titleSmall,
                            color = TonnyTextPrimary
                        )

                        IconButton(
                            onClick = { onShiftWeek(1) },
                            modifier = Modifier
                                .testTag("calendar_next_week")
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = "Semana siguiente",
                                tint = TonnyTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        weekDays.forEach { day ->
                            val isSelected = day.isoDate == uiState.selectedDate
                            val countForDay = uiState.allItems.count { it.scheduledDate == day.isoDate }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("calendar_day_${day.isoDate}")
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSelectDate(day.isoDate) },
                                shape = RoundedCornerShape(14.dp),
                                color = when {
                                    isSelected -> TonnyAmber
                                    day.isToday -> TonnySurfaceElevated
                                    else -> TonnyBg
                                },
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isSelected -> TonnyAmberBright
                                        day.isToday -> TonnyAmber.copy(alpha = 0.4f)
                                        else -> TonnyBorder
                                    }
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = day.dayShort,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) TonnyBg else TonnyTextMuted
                                    )
                                    Text(
                                        text = day.dayNumber,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) TonnyBg else TonnyTextPrimary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(RoundedCornerShape(99.dp))
                                            .background(
                                                when {
                                                    countForDay == 0 -> Color.Transparent
                                                    isSelected -> TonnyBg
                                                    else -> TonnyAmber
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (unscheduledItems.isNotEmpty()) {
            item {
                Text(
                    text = "SIN HORA FIJA",
                    style = MaterialTheme.typography.labelMedium,
                    color = TonnyTextMuted
                )
            }
            items(unscheduledItems, key = { "unsched-${it.id}" }) { item ->
                PlannerItemCard(
                    item = item,
                    use24HourFormat = use24h,
                    onToggle = { onToggleItem(item) },
                    onEditClick = { onEditItem(item) },
                    onFocusClick = { onFocusItem(item) },
                    onDeleteClick = { onDeleteItem(item) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LÍNEA DE TIEMPO",
                    style = MaterialTheme.typography.labelMedium,
                    color = TonnyTextMuted
                )
                Text(
                    text = "${dayItems.size} elementos en el día",
                    style = MaterialTheme.typography.bodySmall,
                    color = TonnyTextSecondary
                )
            }
        }

        items(timelineHours, key = { "hour-$it" }) { hour ->
            val hour24 = String.format(Locale.US, "%02d:00", hour)
            val hourDisplay = DateTimeUtils.formatDisplayTime(hour24, use24h)
            val hourItems = itemsByHour[hour].orEmpty()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = hourDisplay,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (hourItems.isNotEmpty()) TonnyAmberBright else TonnyTextMuted,
                    modifier = Modifier
                        .width(56.dp)
                        .padding(top = 12.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hourItems.isNotEmpty()) {
                        hourItems.forEach { scheduled ->
                            PlannerItemCard(
                                item = scheduled,
                                use24HourFormat = use24h,
                                onToggle = { onToggleItem(scheduled) },
                                onEditClick = { onEditItem(scheduled) },
                                onFocusClick = { onFocusItem(scheduled) },
                                onDeleteClick = { onDeleteItem(scheduled) }
                            )
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onAddItemAtTime(hour24) },
                            shape = RoundedCornerShape(12.dp),
                            color = TonnySurface.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, TonnySurfaceHighlight.copy(alpha = 0.65f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Disponible",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TonnyTextMuted
                                )
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Programar a las $hourDisplay",
                                    tint = TonnyTextMuted,
                                    modifier = Modifier.size(16.dp)
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
