package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val TonnyDarkColorScheme = darkColorScheme(
    primary = TonnyAmber,
    onPrimary = TonnyBg,
    primaryContainer = TonnyAmberSoft,
    onPrimaryContainer = TonnyAmberBright,
    secondary = TonnyMint,
    onSecondary = TonnyBg,
    secondaryContainer = TonnyMintSoft,
    onSecondaryContainer = TonnyMint,
    tertiary = TonnyIndigo,
    onTertiary = TonnyBg,
    tertiaryContainer = TonnyIndigoSoft,
    onTertiaryContainer = TonnyIndigo,
    background = TonnyBg,
    onBackground = TonnyTextPrimary,
    surface = TonnySurface,
    onSurface = TonnyTextPrimary,
    surfaceVariant = TonnySurfaceElevated,
    onSurfaceVariant = TonnyTextSecondary,
    outline = TonnyBorder,
    outlineVariant = TonnySurfaceHighlight,
    error = TonnyCoral,
    onError = TonnyBg,
    errorContainer = TonnyCoralSoft,
    onErrorContainer = TonnyCoral
)

val TonnyShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TonnyPlannerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TonnyDarkColorScheme,
        typography = TonnyTypography,
        shapes = TonnyShapes,
        content = content
    )
}
