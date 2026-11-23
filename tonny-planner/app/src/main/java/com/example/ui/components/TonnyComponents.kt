package com.example.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.GamificationState
import com.example.model.ItemType
import com.example.model.TaskOrHabit
import com.example.model.TonnyState
import com.example.ui.theme.TonnyAmber
import com.example.ui.theme.TonnyAmberBright
import com.example.ui.theme.TonnyAmberSoft
import com.example.ui.theme.TonnyBg
import com.example.ui.theme.TonnyBorder
import com.example.ui.theme.TonnyIndigo
import com.example.ui.theme.TonnyIndigoSoft
import com.example.ui.theme.TonnyMint
import com.example.ui.theme.TonnyMintSoft
import com.example.ui.theme.TonnySurface
import com.example.ui.theme.TonnySurfaceElevated
import com.example.ui.theme.TonnySurfaceHighlight
import com.example.ui.theme.TonnyTextMuted
import com.example.ui.theme.TonnyTextPrimary
import com.example.ui.theme.TonnyTextSecondary
import com.example.utils.DateTimeUtils
import com.example.utils.DayChip
import java.util.Locale

fun tonnyAccentColor(state: TonnyState): Color = when (state) {
    TonnyState.IDLE -> TonnyAmber
    TonnyState.HAPPY -> TonnyMint
    TonnyState.FOCUSED -> TonnyIndigo
    TonnyState.LEVEL_UP -> TonnyAmberBright
}

fun tonnyDrawableRes(state: TonnyState): Int = when (state) {
    TonnyState.IDLE -> R.drawable.img_tonny_idle
    TonnyState.HAPPY -> R.drawable.img_tonny_happy
    TonnyState.FOCUSED -> R.drawable.img_tonny_focused
    TonnyState.LEVEL_UP -> R.drawable.img_tonny_level_up
}

@Composable
fun TonnyAvatarImage(
    state: TonnyState,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val accent = tonnyAccentColor(state)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(20.dp))
            .background(TonnySurface)
            .border(1.5.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = tonnyDrawableRes(state)),
            contentDescription = "Tonny el capibara en estado ${state.badgeLabel}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(20.dp))
        )
    }
}

@Composable
fun LevelXpHeaderCard(
    gamification: GamificationState,
    modifier: Modifier = Modifier
) {
    val isMorning = DateTimeUtils.isCurrentlyMorning()
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = TonnySurface,
        border = BorderStroke(1.dp, TonnyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(TonnyAmberSoft)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "NIVEL ${gamification.currentLevel}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TonnyAmberBright
                        )
                    }
                    Text(
                        text = "${gamification.totalXp} XP total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TonnyTextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (isMorning) TonnyMintSoft else TonnySurfaceElevated,
                    border = BorderStroke(
                        1.dp,
                        if (isMorning) TonnyMint.copy(alpha = 0.4f) else TonnyBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = if (isMorning) TonnyMint else TonnyTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isMorning) "x1.5 Mañana" else "x1.0 Base",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isMorning) TonnyMint else TonnyTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { gamification.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = if (gamification.tonnyState == TonnyState.LEVEL_UP) TonnyAmberBright else TonnyAmber,
                trackColor = TonnySurfaceHighlight,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${gamification.currentLevelXp} / ${gamification.nextLevelXpRequired} XP",
                    style = MaterialTheme.typography.labelMedium,
                    color = TonnyTextPrimary
                )
                val remaining = (gamification.nextLevelXpRequired - gamification.currentLevelXp).coerceAtLeast(0)
                Text(
                    text = "Faltan $remaining XP para Nivel ${gamification.currentLevel + 1}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TonnyTextMuted
                )
            }
        }
    }
}

@Composable
fun TonnyStatusCard(
    gamification: GamificationState,
    onVoiceShortcutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = tonnyAccentColor(gamification.tonnyState)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = TonnySurface,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TonnyAvatarImage(
                state = gamification.tonnyState,
                size = 72.dp
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Tonny",
                        style = MaterialTheme.typography.titleMedium,
                        color = TonnyTextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(accent.copy(alpha = 0.16f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = gamification.tonnyState.badgeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = gamification.tonnyMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TonnyTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onVoiceShortcutClick,
                modifier = Modifier
                    .testTag("tonny_card_voice_button")
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .background(TonnyAmberSoft)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = "Ir al espacio de voz de Tonny",
                    tint = TonnyAmberBright
                )
            }
        }
    }
}

@Composable
fun HorizontalDateStrip(
    days: List<DayChip>,
    selectedDate: String,
    onSelectDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(days, key = { it.isoDate }) { chip ->
            val isSelected = chip.isoDate == selectedDate
            Surface(
                modifier = Modifier
                    .testTag("date_chip_${chip.isoDate}")
                    .width(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelectDate(chip.isoDate) },
                shape = RoundedCornerShape(16.dp),
                color = when {
                    isSelected -> TonnyAmber
                    chip.isToday -> TonnySurfaceElevated
                    else -> TonnySurface
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = when {
                        isSelected -> TonnyAmberBright
                        chip.isToday -> TonnyAmber.copy(alpha = 0.45f)
                        else -> TonnyBorder
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = chip.dayShort,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) TonnyBg else TonnyTextMuted
                    )
                    Text(
                        text = chip.dayNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) TonnyBg else TonnyTextPrimary
                    )
                    if (chip.isToday) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) TonnyBg else TonnyAmber)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PlannerItemCard(
    item: TaskOrHabit,
    use24HourFormat: Boolean,
    onToggle: () -> Unit,
    onEditClick: () -> Unit,
    onFocusClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHabit = item.type == ItemType.HABIT
    val typeAccent = if (isHabit) TonnyMint else TonnyAmber
    val typeContainer = if (isHabit) TonnyMintSoft else TonnyAmberSoft
    val timeRangeLabel = DateTimeUtils.formatTimeRange(item.startTime, item.endTime, use24HourFormat)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("planner_item_${item.id}")
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onEditClick),
        shape = RoundedCornerShape(18.dp),
        color = if (item.completed) TonnySurface.copy(alpha = 0.72f) else TonnySurfaceElevated,
        border = BorderStroke(
            width = 1.dp,
            color = if (item.completed) TonnyMint.copy(alpha = 0.28f) else TonnyBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .testTag("toggle_item_${item.id}")
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (item.completed) TonnyMint else Color.Transparent)
                        .border(
                            width = 2.dp,
                            color = if (item.completed) TonnyMint else typeAccent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.completed) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Marcar como pendiente",
                            tint = TonnyBg,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (item.completed) TonnyTextMuted else TonnyTextPrimary,
                    textDecoration = if (item.completed) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(typeContainer)
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isHabit) Icons.Rounded.Repeat else Icons.Rounded.TaskAlt,
                            contentDescription = null,
                            tint = typeAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = item.type.displayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = typeAccent
                        )
                    }

                    if (!timeRangeLabel.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(TonnySurfaceHighlight)
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = TonnyTextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = timeRangeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = TonnyTextSecondary
                            )
                        }
                    }

                    if (item.completed && item.xpAwarded > 0) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(TonnyMintSoft)
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = TonnyMint,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "+${item.xpAwarded} XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = TonnyMint
                            )
                        }
                    }
                }
            }

            if (!item.completed && item.type == ItemType.TASK) {
                Surface(
                    modifier = Modifier
                        .testTag("focus_button_${item.id}")
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onFocusClick),
                    shape = RoundedCornerShape(12.dp),
                    color = TonnyIndigoSoft,
                    border = BorderStroke(1.dp, TonnyIndigo.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CenterFocusStrong,
                            contentDescription = "Iniciar modo foco",
                            tint = TonnyIndigo,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Foco",
                            style = MaterialTheme.typography.labelLarge,
                            color = TonnyIndigo
                        )
                    }
                }
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .testTag("delete_item_${item.id}")
                    .minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Eliminar ${item.title}",
                    tint = TonnyTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateItemBottomSheet(
    editingItem: TaskOrHabit?,
    initialDate: String,
    initialStartTime: String?,
    initialEndTime: String?,
    use24HourFormat: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (title: String, type: ItemType, scheduledDate: String, startTime: String?, endTime: String?) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember(editingItem) { mutableStateOf(editingItem?.title.orEmpty()) }
    var selectedType by remember(editingItem) { mutableStateOf(editingItem?.type ?: ItemType.TASK) }
    var selectedDate by remember(editingItem, initialDate) { mutableStateOf(editingItem?.scheduledDate ?: initialDate) }
    var startTime by remember(editingItem, initialStartTime) {
        mutableStateOf(editingItem?.startTime ?: initialStartTime.orEmpty())
    }
    var endTime by remember(editingItem, initialEndTime) {
        mutableStateOf(editingItem?.endTime ?: initialEndTime.orEmpty())
    }

    val today = remember { DateTimeUtils.todayIso() }
    val tomorrow = remember { DateTimeUtils.addDays(today, 1) }
    val afterTomorrow = remember { DateTimeUtils.addDays(today, 2) }

    val updateStartTimeAndSuggestEnd: (String) -> Unit = { newStart ->
        startTime = newStart
        val normalized = DateTimeUtils.normalizeTimeHm(newStart)
        endTime = if (normalized != null) {
            DateTimeUtils.addMinutesToTimeHm(normalized, 60).orEmpty()
        } else {
            ""
        }
    }

    val openNativeTimePicker: (String, (String) -> Unit) -> Unit = { currentHm, onPicked ->
        val parsed = DateTimeUtils.normalizeTimeHm(currentHm)?.split(":")
        val initHour = parsed?.getOrNull(0)?.toIntOrNull() ?: 9
        val initMinute = parsed?.getOrNull(1)?.toIntOrNull() ?: 0
        TimePickerDialog(
            context,
            { _, hour, minute ->
                onPicked(String.format(Locale.US, "%02d:%02d", hour, minute))
            },
            initHour,
            initMinute,
            use24HourFormat
        ).show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = TonnySurface,
        contentColor = TonnyTextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (editingItem != null) "Editar elemento" else "Nuevo elemento",
                style = MaterialTheme.typography.headlineSmall,
                color = TonnyTextPrimary
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título de tarea o hábito") },
                placeholder = { Text("Ej. Revisar métricas del sprint") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_title_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TonnyAmber,
                    unfocusedBorderColor = TonnyBorder,
                    focusedTextColor = TonnyTextPrimary,
                    unfocusedTextColor = TonnyTextPrimary
                )
            )

            // Type selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Tipo",
                    style = MaterialTheme.typography.labelMedium,
                    color = TonnyTextSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ItemType.entries.forEach { type ->
                        val active = selectedType == type
                        FilterChip(
                            selected = active,
                            onClick = { selectedType = type },
                            label = { Text(type.displayLabel) },
                            modifier = Modifier.testTag("create_type_${type.apiValue}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (type == ItemType.HABIT) TonnyMintSoft else TonnyAmberSoft,
                                selectedLabelColor = if (type == ItemType.HABIT) TonnyMint else TonnyAmberBright
                            )
                        )
                    }
                }
            }

            // Quick date selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Fecha (${DateTimeUtils.formatReadableDate(selectedDate)})",
                    style = MaterialTheme.typography.labelMedium,
                    color = TonnyTextSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        today to "Hoy",
                        tomorrow to "Mañana",
                        afterTomorrow to "Pasado"
                    ).forEach { (dateIso, label) ->
                        FilterChip(
                            selected = selectedDate == dateIso,
                            onClick = { selectedDate = dateIso },
                            label = { Text(label) },
                            modifier = Modifier.testTag("create_date_$label"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TonnyAmberSoft,
                                selectedLabelColor = TonnyAmberBright
                            )
                        )
                    }
                }
            }

            // Time Range Section: Start Time & End Time
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rango horario (opcional)",
                        style = MaterialTheme.typography.labelMedium,
                        color = TonnyTextSecondary
                    )
                    if (startTime.isNotBlank() || endTime.isNotBlank()) {
                        TextButton(
                            onClick = {
                                startTime = ""
                                endTime = ""
                            }
                        ) {
                            Text("Sin hora", color = TonnyTextMuted)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TimeSelectorCard(
                        label = "Hora de inicio",
                        timeHm = startTime,
                        use24HourFormat = use24HourFormat,
                        testTagPrefix = "start_time",
                        onPickClick = {
                            openNativeTimePicker(startTime.ifBlank { "09:00" }) { picked ->
                                updateStartTimeAndSuggestEnd(picked)
                            }
                        },
                        onStepMinutes = { delta ->
                            val base = DateTimeUtils.normalizeTimeHm(startTime) ?: "09:00"
                            val shifted = DateTimeUtils.addMinutesToTimeHm(base, delta).orEmpty()
                            updateStartTimeAndSuggestEnd(shifted)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    TimeSelectorCard(
                        label = "Hora final",
                        timeHm = endTime,
                        use24HourFormat = use24HourFormat,
                        testTagPrefix = "end_time",
                        onPickClick = {
                            openNativeTimePicker(
                                endTime.ifBlank {
                                    DateTimeUtils.addMinutesToTimeHm(startTime, 60) ?: "10:00"
                                }
                            ) { picked ->
                                endTime = picked
                            }
                        },
                        onStepMinutes = { delta ->
                            val base = DateTimeUtils.normalizeTimeHm(endTime)
                                ?: DateTimeUtils.addMinutesToTimeHm(startTime, 60)
                                ?: "10:00"
                            endTime = DateTimeUtils.addMinutesToTimeHm(base, delta).orEmpty()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Quick start presets
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("08:00", "10:00", "14:00", "17:00", "20:00").forEach { preset ->
                        FilterChip(
                            selected = startTime == preset,
                            onClick = {
                                if (startTime == preset) {
                                    startTime = ""
                                    endTime = ""
                                } else {
                                    updateStartTimeAndSuggestEnd(preset)
                                }
                            },
                            label = { Text(DateTimeUtils.formatDisplayTime(preset, use24HourFormat)) }
                        )
                    }
                }

                // Quick duration helper when start time is chosen
                if (DateTimeUtils.normalizeTimeHm(startTime) != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Duración:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TonnyTextMuted
                        )
                        listOf(30 to "+30 min", 60 to "+1 h", 90 to "+1.5 h").forEach { (mins, label) ->
                            val targetEnd = DateTimeUtils.addMinutesToTimeHm(startTime, mins).orEmpty()
                            FilterChip(
                                selected = endTime == targetEnd,
                                onClick = { endTime = targetEnd },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TonnyMintSoft,
                                    selectedLabelColor = TonnyMint
                                )
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = TonnyTextSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onConfirm(
                            title,
                            selectedType,
                            selectedDate,
                            DateTimeUtils.normalizeTimeHm(startTime),
                            DateTimeUtils.normalizeTimeHm(endTime)
                        )
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.testTag("create_confirm_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TonnyAmber,
                        contentColor = TonnyBg
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (editingItem != null) "Guardar cambios" else "Guardar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TimeSelectorCard(
    label: String,
    timeHm: String,
    use24HourFormat: Boolean,
    testTagPrefix: String,
    onPickClick: () -> Unit,
    onStepMinutes: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val display = DateTimeUtils.normalizeTimeHm(timeHm)?.let {
        DateTimeUtils.formatDisplayTime(it, use24HourFormat)
    } ?: "--:--"

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = TonnySurfaceElevated,
        border = BorderStroke(1.dp, TonnyBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TonnyTextMuted
            )

            Surface(
                onClick = onPickClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("${testTagPrefix}_pick"),
                shape = RoundedCornerShape(10.dp),
                color = TonnySurface,
                border = BorderStroke(1.dp, TonnySurfaceHighlight)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = display,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (display == "--:--") TonnyTextMuted else TonnyAmberBright
                    )
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = "Elegir $label",
                        tint = TonnyTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    onClick = { onStepMinutes(-30) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("${testTagPrefix}_minus"),
                    shape = RoundedCornerShape(8.dp),
                    color = TonnySurfaceHighlight
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "Restar 30 min a $label",
                            tint = TonnyTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text("30m", style = MaterialTheme.typography.labelSmall, color = TonnyTextSecondary)
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    onClick = { onStepMinutes(30) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("${testTagPrefix}_plus"),
                    shape = RoundedCornerShape(8.dp),
                    color = TonnySurfaceHighlight
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Sumar 30 min a $label",
                            tint = TonnyTextPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text("30m", style = MaterialTheme.typography.labelSmall, color = TonnyTextPrimary)
                    }
                }
            }
        }
    }
}
