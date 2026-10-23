package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AstralColorScheme = darkColorScheme(
    primary = AuroraTeal,
    onPrimary = Color(0xFF042F2E),
    primaryContainer = Color(0xFF134E4A),
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = AstralIndigo,
    onSecondary = Color(0xFF1E1B4B),
    secondaryContainer = Color(0xFF312E81),
    onSecondaryContainer = Color(0xFFE0E7FF),
    tertiary = CosmicViolet,
    onTertiary = Color(0xFF3B0764),
    tertiaryContainer = Color(0xFF581C87),
    onTertiaryContainer = Color(0xFFF3E8FF),
    background = AstralDarkBg,
    onBackground = AstralTextPrimary,
    surface = AstralDarkSurface,
    onSurface = AstralTextPrimary,
    surfaceVariant = AstralDarkSurfaceVariant,
    onSurfaceVariant = AstralTextSecondary,
    outline = AstralDarkSurfaceBorder,
    error = WarningRed,
    onError = Color(0xFF450A0A)
)

@Composable
fun KaizenTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AstralColorScheme,
        typography = Typography,
        content = content
    )
}
