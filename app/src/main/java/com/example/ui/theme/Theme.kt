package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DFShieldColorScheme = darkColorScheme(
    primary = ShieldAccentGreen,
    onPrimary = Color(0xFF042111),
    primaryContainer = ShieldAccentGreenContainer,
    onPrimaryContainer = Color(0xFFA6F6CB),
    
    secondary = Color(0xFF86A5C3),
    onSecondary = Color(0xFF0D1D2C),
    secondaryContainer = Color(0xFF1B2C3E),
    onSecondaryContainer = Color(0xFFC7E0FA),
    
    tertiary = ShieldWarning,
    onTertiary = Color(0xFF291800),
    tertiaryContainer = ShieldWarningContainer,
    onTertiaryContainer = Color(0xFFFFDFB0),
    
    error = ShieldDanger,
    onError = Color(0xFF37000B),
    errorContainer = ShieldDangerContainer,
    onErrorContainer = Color(0xFFFFB4AB),
    
    background = ShieldBackground,
    onBackground = ShieldTextPrimary,
    
    surface = ShieldSurface,
    onSurface = ShieldTextPrimary,
    surfaceVariant = ShieldSurfaceCard,
    onSurfaceVariant = ShieldTextSecondary,
    
    outline = ShieldBorder,
    outlineVariant = ShieldBorderLight
)

@Composable
fun DFShieldTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DFShieldColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias for template
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    DFShieldTheme(content = content)
}
