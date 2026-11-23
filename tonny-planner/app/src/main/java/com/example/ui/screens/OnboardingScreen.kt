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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.OnboardingAnswers
import com.example.model.TonnyState
import com.example.ui.components.TonnyAvatarImage
import com.example.ui.theme.TonnyAmber
import com.example.ui.theme.TonnyAmberBright
import com.example.ui.theme.TonnyAmberSoft
import com.example.ui.theme.TonnyBg
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

private data class OnboardingOption(
    val id: String,
    val label: String,
    val tag: String? = null,
    val icon: ImageVector,
    val iconTint: Color
)

@Composable
fun OnboardingScreen(
    initialAnswers: OnboardingAnswers,
    onComplete: (discoverySource: String, mainGoal: String, dailyMinutes: Int) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var stepIndex by remember { mutableIntStateOf(0) }
    var selectedSource by remember { mutableStateOf(initialAnswers.discoverySource) }
    var selectedGoal by remember { mutableStateOf(initialAnswers.mainGoal) }
    var selectedMinutes by remember {
        mutableStateOf(if (initialAnswers.dailyMinutes > 0) initialAnswers.dailyMinutes.toString() else "")
    }

    BackHandler {
        if (stepIndex > 0) {
            stepIndex -= 1
        } else {
            onSkip()
        }
    }

    val totalSteps = 3
    val progress = (stepIndex + 1).toFloat() / totalSteps.toFloat()

    val questionText = when (stepIndex) {
        0 -> "¿Quién fue el chismoso que te habló sobre Tonny Planner?"
        1 -> "¿Qué te gustaría lograr primero con Tonny?"
        else -> "¿Cuánto tiempo diario quieres dedicar a planificar?"
    }

    val options = remember(stepIndex) {
        when (stepIndex) {
            0 -> listOf(
                OnboardingOption("TikTok", "TikTok", null, Icons.Rounded.PlayCircle, TonnyIndigo),
                OnboardingOption("YouTube", "YouTube", null, Icons.Rounded.PlayCircle, TonnyAmberBright),
                OnboardingOption("Google", "Búsqueda en Google", null, Icons.Rounded.Search, TonnyMint),
                OnboardingOption("Redes sociales", "Instagram / Facebook", null, Icons.Rounded.Share, TonnyIndigo),
                OnboardingOption("Amigos/Familia", "Amigos o familia", null, Icons.Rounded.Groups, TonnyAmber),
                OnboardingOption("Blog/Comunidad", "Artículo o comunidad", null, Icons.Rounded.Public, TonnyTextSecondary)
            )
            1 -> listOf(
                OnboardingOption("Organizar tareas", "Organizar mis tareas sin estrés", null, Icons.Rounded.TaskAlt, TonnyAmberBright),
                OnboardingOption("Crear hábitos", "Construir hábitos diarios constantes", null, Icons.Rounded.Repeat, TonnyMint),
                OnboardingOption("Modo Foco", "Trabajar por bloques en Modo Foco", null, Icons.Rounded.CenterFocusStrong, TonnyIndigo),
                OnboardingOption("Planificar por voz", "Agendar rápido hablando con Tonny", null, Icons.Rounded.Mic, TonnyAmber)
            )
            else -> listOf(
                OnboardingOption("5", "5 min / día", "Suave", Icons.Rounded.Schedule, TonnyTextSecondary),
                OnboardingOption("10", "10 min / día", "Regular", Icons.Rounded.Schedule, TonnyAmberBright),
                OnboardingOption("15", "15 min / día", "Enfocado", Icons.Rounded.Bolt, TonnyMint),
                OnboardingOption("25", "25 min / día", "Intenso", Icons.Rounded.AutoAwesome, TonnyIndigo)
            )
        }
    }

    val currentSelection = when (stepIndex) {
        0 -> selectedSource
        1 -> selectedGoal
        else -> selectedMinutes
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TonnyBg)
            .systemBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("onboarding_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Progress Header (matches Figma frame hierarchy in dark theme)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {
                    if (stepIndex > 0) stepIndex -= 1 else onSkip()
                },
                modifier = Modifier
                    .testTag("onboarding_back_button")
                    .minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = if (stepIndex > 0) "Pregunta anterior" else "Omitir bienvenida",
                    tint = TonnyTextSecondary
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = TonnyAmber,
                trackColor = TonnySurfaceHighlight,
                strokeCap = StrokeCap.Round
            )

            TextButton(
                onClick = onSkip,
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text("Omitir", color = TonnyTextMuted)
            }
        }

        // Capybara Reading + Speech Bubble Row (adapted from Figma frame to dark Tonny Planner style)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                TonnyAvatarImage(
                    state = TonnyState.FOCUSED,
                    size = 92.dp
                )
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = 4.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(TonnyAmber)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                        contentDescription = "Tonny leyendo",
                        tint = TonnyBg,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomEnd = 18.dp,
                    bottomStart = 6.dp
                ),
                color = TonnySurface,
                border = BorderStroke(1.dp, TonnyBorder)
            ) {
                Text(
                    text = questionText,
                    style = MaterialTheme.typography.titleMedium,
                    color = TonnyTextPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
                )
            }
        }

        // Option Cards List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(options, key = { it.id }) { option ->
                val isSelected = currentSelection == option.id
                Surface(
                    onClick = {
                        when (stepIndex) {
                            0 -> selectedSource = option.id
                            1 -> selectedGoal = option.id
                            else -> selectedMinutes = option.id
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_option_${option.id}"),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) TonnyAmberSoft else TonnySurface,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) TonnyAmber else TonnyBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(TonnySurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = null,
                                tint = if (isSelected) TonnyAmberBright else option.iconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isSelected) TonnyAmberBright else TonnyTextPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        if (option.tag != null) {
                            Text(
                                text = option.tag,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) TonnyAmberBright else TonnyTextMuted
                            )
                        }
                    }
                }
            }
        }

        // Bottom Continue / Finish CTA
        Button(
            onClick = {
                if (stepIndex < totalSteps - 1) {
                    stepIndex += 1
                } else {
                    onComplete(
                        selectedSource.ifBlank { "No especificado" },
                        selectedGoal.ifBlank { "Organizar tareas" },
                        selectedMinutes.toIntOrNull() ?: 15
                    )
                }
            },
            enabled = currentSelection.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_continue_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (stepIndex == totalSteps - 1) TonnyMint else TonnyAmber,
                contentColor = TonnyBg,
                disabledContainerColor = TonnySurfaceHighlight,
                disabledContentColor = TonnyTextMuted
            )
        ) {
            Text(
                text = if (stepIndex == totalSteps - 1) "EMPEZAR CON TONNY" else "CONTINUAR",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
