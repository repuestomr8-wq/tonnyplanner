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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ui.components.TonnyAvatarImage
import com.example.ui.theme.TonnyAmber
import com.example.ui.theme.TonnyAmberBright
import com.example.ui.theme.TonnyAmberSoft
import com.example.ui.theme.TonnyBg
import com.example.ui.theme.TonnyBorder
import com.example.ui.theme.TonnyCoral
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

@Composable
fun UserScreen(
    uiState: TonnyPlannerUiState,
    onSignInOrCreateAccount: (displayName: String, email: String) -> Unit,
    onSignOut: () -> Unit,
    onUpdatePreferences: (use24Hour: Boolean?, voiceAutoListen: Boolean?, morningTips: Boolean?) -> Unit,
    onReopenOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = uiState.userProfile
    val prefs = profile.preferences
    val onboarding = uiState.onboarding
    var showAuthSheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("user_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Perfil y Ajustes",
                style = MaterialTheme.typography.headlineMedium,
                color = TonnyTextPrimary
            )
        }

        // Profile Card + Auth CTA
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = TonnySurface,
                border = BorderStroke(
                    1.dp,
                    if (profile.isAuthenticated) TonnyMint.copy(alpha = 0.45f) else TonnyBorder
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(if (profile.isAuthenticated) TonnyMintSoft else TonnyAmberSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (profile.isAuthenticated) Icons.Rounded.VerifiedUser else Icons.Rounded.Person,
                                contentDescription = "Avatar de usuario",
                                tint = if (profile.isAuthenticated) TonnyMint else TonnyAmberBright,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                color = TonnyTextPrimary
                            )
                            Text(
                                text = if (profile.isAuthenticated && profile.email.isNotBlank()) {
                                    profile.email
                                } else if (profile.isAuthenticated) {
                                    "Cuenta conectada · Nivel ${uiState.gamification.currentLevel}"
                                } else {
                                    "Modo invitado · Guarda tu progreso con una cuenta"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TonnyTextSecondary
                            )
                        }
                    }

                    if (!profile.isAuthenticated) {
                        Button(
                            onClick = { showAuthSheet = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("user_auth_open_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TonnyAmber,
                                contentColor = TonnyBg
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Login,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Iniciar sesión o crear cuenta", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onSignOut,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("user_sign_out_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, TonnyBorder)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Logout,
                                contentDescription = null,
                                tint = TonnyCoral,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cerrar sesión", color = TonnyCoral)
                        }
                    }
                }
            }
        }

        // Basic Settings Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = TonnySurface,
                border = BorderStroke(1.dp, TonnyBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "AJUSTES BÁSICOS",
                        style = MaterialTheme.typography.labelMedium,
                        color = TonnyTextMuted
                    )

                    SettingToggleRow(
                        icon = Icons.Rounded.Schedule,
                        title = "Formato de hora 24h",
                        subtitle = if (prefs.use24HourFormat) "Ejemplo: 15:30" else "Ejemplo: 3:30 PM",
                        checked = prefs.use24HourFormat,
                        onCheckedChange = { onUpdatePreferences(it, null, null) },
                        testTag = "setting_24h_toggle"
                    )

                    SettingToggleRow(
                        icon = Icons.Rounded.Mic,
                        title = "Dictado en español (Voz)",
                        subtitle = "Captura rápida en el espacio de Tonny",
                        checked = prefs.voiceAutoListenEnabled,
                        onCheckedChange = { onUpdatePreferences(null, it, null) },
                        testTag = "setting_voice_toggle"
                    )

                    SettingToggleRow(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "Avisos de bonus matutino x1.5",
                        subtitle = "Recordar XP extra antes de las 12:00 PM",
                        checked = prefs.morningBonusTipsEnabled,
                        onCheckedChange = { onUpdatePreferences(null, null, it) },
                        testTag = "setting_morning_tips_toggle"
                    )
                }
            }
        }

        // Onboarding Preferences & Reopen Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = TonnySurface,
                border = BorderStroke(1.dp, TonnyBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TonnyAvatarImage(state = uiState.gamification.tonnyState, size = 54.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Preferencias iniciales con Tonny",
                                style = MaterialTheme.typography.titleMedium,
                                color = TonnyTextPrimary
                            )
                            Text(
                                text = if (onboarding.mainGoal.isNotBlank()) {
                                    "Objetivo: ${onboarding.mainGoal} · ${onboarding.dailyMinutes} min/día"
                                } else {
                                    "Personaliza tu objetivo y tiempo diario"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TonnyTextSecondary
                            )
                        }
                    }

                    if (onboarding.discoverySource.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(TonnySurfaceElevated)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Nos conociste por: ${onboarding.discoverySource}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TonnyTextMuted
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onReopenOnboarding,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("user_reopen_onboarding_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, TonnyIndigo.copy(alpha = 0.45f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Quiz,
                            contentDescription = null,
                            tint = TonnyIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Repetir preguntas con Tonny", color = TonnyIndigo)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }

    if (showAuthSheet) {
        AuthModalBottomSheet(
            onDismiss = { showAuthSheet = false },
            onSubmit = { name, email ->
                showAuthSheet = false
                onSignInOrCreateAccount(name, email)
            }
        )
    }
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TonnySurfaceElevated)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TonnySurfaceHighlight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TonnyAmberBright,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TonnyTextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TonnyTextMuted
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = TonnyBg,
                checkedTrackColor = TonnyMint,
                uncheckedThumbColor = TonnyTextSecondary,
                uncheckedTrackColor = TonnySurfaceHighlight
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthModalBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (displayName: String, email: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isRegisterMode by remember { mutableStateOf(true) }
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = TonnySurface,
        contentColor = TonnyTextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (isRegisterMode) "Crear cuenta en Tonny" else "Iniciar sesión",
                style = MaterialTheme.typography.headlineSmall,
                color = TonnyTextPrimary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = isRegisterMode,
                    onClick = { isRegisterMode = true },
                    label = { Text("Crear cuenta") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TonnyAmberSoft,
                        selectedLabelColor = TonnyAmberBright
                    )
                )
                FilterChip(
                    selected = !isRegisterMode,
                    onClick = { isRegisterMode = false },
                    label = { Text("Ya tengo cuenta") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TonnyIndigoSoft,
                        selectedLabelColor = TonnyIndigo
                    )
                )
            }

            if (isRegisterMode) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Nombre o apodo") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_name_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TonnyAmber,
                        unfocusedBorderColor = TonnyBorder,
                        focusedTextColor = TonnyTextPrimary,
                        unfocusedTextColor = TonnyTextPrimary
                    )
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_email_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TonnyAmber,
                    unfocusedBorderColor = TonnyBorder,
                    focusedTextColor = TonnyTextPrimary,
                    unfocusedTextColor = TonnyTextPrimary
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_password_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TonnyAmber,
                    unfocusedBorderColor = TonnyBorder,
                    focusedTextColor = TonnyTextPrimary,
                    unfocusedTextColor = TonnyTextPrimary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = TonnyTextSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val finalName = if (isRegisterMode) {
                            displayName.ifBlank { email.substringBefore("@").ifBlank { "Usuario Tonny" } }
                        } else {
                            email.substringBefore("@").ifBlank { "Usuario Tonny" }
                        }
                        onSubmit(finalName, email)
                    },
                    enabled = email.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.testTag("auth_submit_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TonnyAmber,
                        contentColor = TonnyBg
                    )
                ) {
                    Text(
                        text = if (isRegisterMode) "Registrarme" else "Entrar",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
