package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.model.ItemType
import com.example.ui.components.TonnyAvatarImage
import com.example.ui.components.tonnyAccentColor
import com.example.ui.theme.TonnyAmber
import com.example.ui.theme.TonnyAmberBright
import com.example.ui.theme.TonnyAmberSoft
import com.example.ui.theme.TonnyBg
import com.example.ui.theme.TonnyBorder
import com.example.ui.theme.TonnyCoral
import com.example.ui.theme.TonnyCoralSoft
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
import com.example.ui.viewmodel.TonnyPlannerUiState
import com.example.utils.DateTimeUtils
import com.example.utils.SpeechCaptureHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TonnySpaceScreen(
    uiState: TonnyPlannerUiState,
    onListeningChanged: (Boolean) -> Unit,
    onVoiceError: (String?) -> Unit,
    onTranscriptChanged: (String) -> Unit,
    onParseNow: () -> Unit,
    onUpdateDraft: (title: String?, type: ItemType?, date: String?, startTime: String?, endTime: String?) -> Unit,
    onConfirmDraft: () -> Unit,
    onClearDraft: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val speechHelper = remember(context) {
        SpeechCaptureHelper(
            context = context,
            onPartialResult = { partial -> onTranscriptChanged(partial) },
            onFinalResult = { finalText -> onTranscriptChanged(finalText) },
            onErrorMessage = { error -> onVoiceError(error) },
            onListeningChanged = { listening -> onListeningChanged(listening) }
        )
    }

    DisposableEffect(speechHelper) {
        onDispose { speechHelper.destroy() }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            speechHelper.startListening()
        } else {
            onVoiceError("Permiso de micrófono denegado. Puedes escribir tu comando en el campo de texto.")
        }
    }

    val gamification = uiState.gamification
    val accent = tonnyAccentColor(gamification.tonnyState)
    val today = remember { DateTimeUtils.todayIso() }
    val tomorrow = remember { DateTimeUtils.addDays(today, 1) }
    val afterTomorrow = remember { DateTimeUtils.addDays(today, 2) }

    val exampleCommands = remember {
        listOf(
            "Meditar 10 minutos cada mañana a las 7",
            "Enviar informe del proyecto mañana a las 15:30",
            "Leer 20 páginas hoy de 20:00 a 21:00"
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("tonny_space_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Espacio de Tonny",
                style = MaterialTheme.typography.headlineMedium,
                color = TonnyTextPrimary
            )
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = TonnySurface,
                border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TonnyAvatarImage(
                        state = gamification.tonnyState,
                        size = 132.dp
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(accent.copy(alpha = 0.18f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Estado: ${gamification.tonnyState.badgeLabel}",
                            style = MaterialTheme.typography.labelMedium,
                            color = accent
                        )
                    }

                    Text(
                        text = gamification.tonnyMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TonnyTextPrimary
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(TonnySurfaceElevated)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Nivel ${gamification.currentLevel}",
                                style = MaterialTheme.typography.titleSmall,
                                color = TonnyAmberBright
                            )
                            Text(
                                text = "${gamification.currentLevelXp} / ${gamification.nextLevelXpRequired} XP (${gamification.totalXp} XP total)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TonnyTextSecondary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { gamification.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(999.dp)),
                            color = accent,
                            trackColor = TonnySurfaceHighlight,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        // Voice Scheduling Panel
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = TonnySurface,
                border = BorderStroke(1.dp, TonnyBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Programación por Voz",
                                style = MaterialTheme.typography.titleLarge,
                                color = TonnyTextPrimary
                            )
                            Text(
                                text = "Dicta o escribe en lenguaje natural y revisa antes de guardar.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TonnyTextSecondary
                            )
                        }

                        Button(
                            onClick = {
                                if (uiState.isListening) {
                                    speechHelper.stopListening()
                                } else {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasPermission) {
                                        speechHelper.startListening()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            modifier = Modifier
                                .testTag("voice_mic_button")
                                .size(56.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isListening) TonnyCoral else TonnyAmber,
                                contentColor = TonnyBg
                            )
                        ) {
                            Icon(
                                imageVector = if (uiState.isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                                contentDescription = if (uiState.isListening) "Detener dictado" else "Iniciar dictado por voz",
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    if (uiState.isListening) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(TonnyIndigoSoft)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = null,
                                tint = TonnyIndigo
                            )
                            Text(
                                text = "Escuchando en español... habla con naturalidad",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TonnyIndigo
                            )
                        }
                    }

                    if (!uiState.voiceError.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TonnyCoralSoft,
                            border = BorderStroke(1.dp, TonnyCoral.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = uiState.voiceError,
                                style = MaterialTheme.typography.bodySmall,
                                color = TonnyCoral,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.rawTranscript,
                        onValueChange = onTranscriptChanged,
                        label = { Text("Transcripción en tiempo real (editable)") },
                        placeholder = { Text("Ej. Estudiar inglés mañana a las 8 am") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voice_transcript_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TonnyAmber,
                            unfocusedBorderColor = TonnyBorder,
                            focusedTextColor = TonnyTextPrimary,
                            unfocusedTextColor = TonnyTextPrimary
                        )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Ejemplos rápidos:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TonnyTextMuted
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            exampleCommands.forEach { sample ->
                                AssistChip(
                                    onClick = { onTranscriptChanged(sample) },
                                    label = {
                                        Text(
                                            text = sample,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = TonnySurfaceElevated,
                                        labelColor = TonnyTextSecondary
                                    ),
                                    border = BorderStroke(1.dp, TonnyBorder)
                                )
                            }
                        }
                    }

                    if (uiState.rawTranscript.isNotBlank() && uiState.voiceDraft == null) {
                        Button(
                            onClick = onParseNow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("voice_parse_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TonnyIndigo,
                                contentColor = TonnyBg
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Interpretar comando", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        val draft = uiState.voiceDraft
        if (draft != null) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voice_review_card"),
                    shape = RoundedCornerShape(22.dp),
                    color = TonnySurfaceElevated,
                    border = BorderStroke(1.5.dp, TonnyMint.copy(alpha = 0.55f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = TonnyMint,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Revisión antes de guardar",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TonnyTextPrimary
                                )
                            }
                            TextButton(onClick = onClearDraft) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Descartar borrador",
                                    tint = TonnyTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = draft.title,
                            onValueChange = { onUpdateDraft(it, null, null, null, null) },
                            label = { Text("Título detectado") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("voice_draft_title_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TonnyMint,
                                unfocusedBorderColor = TonnyBorder,
                                focusedTextColor = TonnyTextPrimary,
                                unfocusedTextColor = TonnyTextPrimary
                            )
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tipo:",
                                style = MaterialTheme.typography.labelMedium,
                                color = TonnyTextSecondary
                            )
                            ItemType.entries.forEach { type ->
                                FilterChip(
                                    selected = draft.type == type,
                                    onClick = { onUpdateDraft(null, type, null, null, null) },
                                    label = { Text(type.displayLabel) },
                                    modifier = Modifier.testTag("voice_draft_type_${type.apiValue}"),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (type == ItemType.HABIT) TonnyMintSoft else TonnyAmberSoft,
                                        selectedLabelColor = if (type == ItemType.HABIT) TonnyMint else TonnyAmberBright
                                    )
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Fecha: ${DateTimeUtils.formatReadableDate(draft.scheduledDate)} (${draft.scheduledDate})",
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
                                        selected = draft.scheduledDate == dateIso,
                                        onClick = { onUpdateDraft(null, null, dateIso, null, null) },
                                        label = { Text(label) }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = draft.startTime,
                                onValueChange = { onUpdateDraft(null, null, null, it, null) },
                                label = { Text("Hora inicio") },
                                placeholder = { Text("09:00") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("voice_draft_start_time_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TonnyMint,
                                    unfocusedBorderColor = TonnyBorder,
                                    focusedTextColor = TonnyTextPrimary,
                                    unfocusedTextColor = TonnyTextPrimary
                                )
                            )
                            OutlinedTextField(
                                value = draft.endTime,
                                onValueChange = { onUpdateDraft(null, null, null, null, it) },
                                label = { Text("Hora final") },
                                placeholder = { Text("10:00") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("voice_draft_end_time_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TonnyMint,
                                    unfocusedBorderColor = TonnyBorder,
                                    focusedTextColor = TonnyTextPrimary,
                                    unfocusedTextColor = TonnyTextPrimary
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onClearDraft,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, TonnyBorder)
                            ) {
                                Text("Descartar", color = TonnyTextSecondary)
                            }
                            Button(
                                onClick = onConfirmDraft,
                                enabled = draft.title.isNotBlank(),
                                modifier = Modifier
                                    .weight(1.4f)
                                    .testTag("voice_confirm_save_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TonnyMint,
                                    contentColor = TonnyBg
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Confirmar y guardar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}
