package com.acoustics.calculator.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ===================================================================
// 🎨 V2.0 DARK MODE FIRST — Cyberpunk / Neon / Glassmorphism
// The light theme is kept for accessibility but the app shines in dark mode
// ===================================================================

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DeepBlue,
    primaryContainer = Color(0xFF003544),
    onPrimaryContainer = Color(0xFF4DB8FF), // NeonCyanLight
    secondary = NeonPink,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3A0020),
    onSecondaryContainer = NeonPinkLight,
    tertiary = NeonPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF2D0040),
    onTertiaryContainer = NeonPurpleLight,
    background = Color(0xFF0A0A0F),
    onBackground = Color(0xFFE8E8F0),
    surface = SurfaceDark,
    onSurface = Color(0xFFE8E8F0),
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = Color(0xFFB0B0C0),
    outline = Color(0xFF3A3A5C),
    outlineVariant = Color(0xFF2A2A4C),
    error = NeonRed,
    onError = Color.White,
    errorContainer = Color(0xFF4A0010),
    onErrorContainer = NeonRed,
    inversePrimary = Color(0xFF006080),
    inverseSurface = Color(0xFFE8E8F0),
    inverseOnSurface = Color(0xFF0A0A0F),
    surfaceTint = NeonCyan,
    scrim = Color(0xCC0A0A0F)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006080),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC2E8FF),
    onPrimaryContainer = Color(0xFF001E2A),
    secondary = Color(0xFFA0005C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9E6),
    onSecondaryContainer = Color(0xFF3A0020),
    tertiary = Color(0xFF7A00B0),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF0D9FF),
    onTertiaryContainer = Color(0xFF2D0040),
    background = Color(0xFFF8F8FF),
    onBackground = Color(0xFF1A1A2E),
    surface = Color.White,
    onSurface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFFE0E0F0),
    onSurfaceVariant = Color(0xFF4A4A5C),
    outline = Color(0xFF74748C),
    outlineVariant = Color(0xFFC4C4D8),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    inversePrimary = Color(0xFF83CFFF),
    inverseSurface = Color(0xFF1A1A2E),
    inverseOnSurface = Color(0xFFF0F0F8),
    surfaceTint = Color(0xFF006080),
    scrim = Color(0xCCF8F8FF)
)

@Composable
fun AcousticTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
