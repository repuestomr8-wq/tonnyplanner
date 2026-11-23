package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TonnyState
import com.example.ui.components.TonnyAvatarImage
import com.example.ui.theme.TonnyBg
import com.example.ui.theme.TonnyBorder
import com.example.ui.theme.TonnyIndigo
import com.example.ui.theme.TonnyIndigoSoft
import com.example.ui.theme.TonnyMint
import com.example.ui.theme.TonnySurface
import com.example.ui.theme.TonnySurfaceElevated
import com.example.ui.theme.TonnyTextMuted
import com.example.ui.theme.TonnyTextPrimary
import com.example.ui.theme.TonnyTextSecondary
import com.example.ui.viewmodel.FocusSessionState
import com.example.utils.DateTimeUtils
import java.util.Locale

@Composable
fun FocusModeScreen(
    session: FocusSessionState,
    onSelectPreset: (Int) -> Unit,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onCompleteTask: () -> Unit,
    onCloseFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onCloseFocus)

    val minutes = session.remainingSeconds / 60
    val seconds = session.remainingSeconds % 60
    val formattedClock = String.format(Locale.US, "%02d:%02d", minutes, seconds)
    val potentialXp = if (DateTimeUtils.isCurrentlyMorning()) 15 else 10

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TonnyBg)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("focus_mode_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCloseFocus,
                modifier = Modifier
                    .testTag("focus_back_button")
                    .minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Salir de Modo Foco",
                    tint = TonnyTextPrimary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(TonnyIndigoSoft)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "MODO FOCO",
                    style = MaterialTheme.typography.labelMedium,
                    color = TonnyIndigo
                )
            }

            Spacer(modifier = Modifier.size(48.dp))
        }

        // Tonny Focused Illustration
        TonnyAvatarImage(
            state = TonnyState.FOCUSED,
            size = 132.dp
        )

        // Active Task Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = TonnySurface,
            border = BorderStroke(1.dp, TonnyIndigo.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "ENFOCADO EN",
                    style = MaterialTheme.typography.labelSmall,
                    color = TonnyTextMuted
                )
                Text(
                    text = session.task.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TonnyTextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Recompensa al completar: +$potentialXp XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = TonnyMint
                )
            }
        }

        // Timer Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = TonnySurfaceElevated,
            border = BorderStroke(1.dp, TonnyBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 26.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = formattedClock,
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 56.sp),
                    color = TonnyTextPrimary,
                    modifier = Modifier.testTag("focus_timer_display")
                )

                // Presets: 15, 25, 45 min
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(15, 25, 45).forEach { preset ->
                        FilterChip(
                            selected = session.presetMinutes == preset,
                            onClick = { onSelectPreset(preset) },
                            label = { Text("$preset min") },
                            modifier = Modifier.testTag("focus_preset_$preset"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TonnyIndigoSoft,
                                selectedLabelColor = TonnyIndigo
                            )
                        )
                    }
                }

                // Start / Pause / Reset controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onResetTimer,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("focus_reset_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, TonnyBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            tint = TonnyTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reiniciar", color = TonnyTextSecondary)
                    }

                    Button(
                        onClick = onToggleTimer,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(50.dp)
                            .testTag("focus_start_pause_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TonnyIndigo,
                            contentColor = TonnyBg
                        )
                    ) {
                        Icon(
                            imageVector = if (session.isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (session.isRunning) "Pausar" else "Iniciar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f, fill = false))

        // Primary Complete Task CTA
        Button(
            onClick = onCompleteTask,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("focus_complete_task_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TonnyMint,
                contentColor = TonnyBg
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Completar tarea (+$potentialXp XP)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
