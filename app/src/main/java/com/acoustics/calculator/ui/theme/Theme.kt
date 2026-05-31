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

private val LightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = Color.White,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue800,
    secondary = Teal500,
    onSecondary = Color.White,
    secondaryContainer = Teal100,
    onSecondaryContainer = Teal700,
    tertiary = AccentPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE1BEE7),
    onTertiaryContainer = AccentPurple,
    background = Gray50,
    onBackground = Gray900,
    surface = Color.White,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray600,
    outline = Gray400,
    outlineVariant = Gray300,
    error = NonCompliantRed,
    onError = Color.White,
    errorContainer = NonCompliantRedBg,
    onErrorContainer = NonCompliantRed,
    inversePrimary = Blue100,
    inverseSurface = Gray800,
    inverseOnSurface = Gray50
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue100,
    onPrimary = Blue800,
    primaryContainer = Blue600,
    onPrimaryContainer = Blue50,
    secondary = Teal100,
    onSecondary = Teal700,
    secondaryContainer = Teal600,
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFCE93D8),
    onTertiary = AccentPurple,
    tertiaryContainer = AccentPurple,
    onTertiaryContainer = Color(0xFFE1BEE7),
    background = Gray900,
    onBackground = Gray50,
    surface = Gray800,
    onSurface = Gray50,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray400,
    outline = Gray600,
    outlineVariant = Gray700,
    error = NonCompliantRed,
    onError = Color.White,
    errorContainer = NonCompliantRedBg,
    onErrorContainer = NonCompliantRed
)

@Composable
fun AcousticTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
