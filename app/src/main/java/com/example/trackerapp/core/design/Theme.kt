package com.example.trackerapp.core.design

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * TrackerApp Light Color Scheme
 * Professional, clean, aviation-inspired
 */
private val LightColorScheme = lightColorScheme(
    primary = AirbusBlue,
    onPrimary = BackgroundLight,
    primaryContainer = AirbusBlueLight,
    onPrimaryContainer = TextPrimary,

    secondary = SkyBlue,
    onSecondary = BackgroundLight,
    secondaryContainer = SkyBlueLight,
    onSecondaryContainer = TextPrimary,

    tertiary = Silver,
    onTertiary = TextPrimary,
    tertiaryContainer = SilverLight,
    onTertiaryContainer = TextPrimary,

    error = Error,
    onError = BackgroundLight,
    errorContainer = androidx.compose.ui.graphics.Color(0xFFFFDAD6),
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFF410002),

    background = BackgroundLight,
    onBackground = TextPrimary,

    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMedium,
    onSurfaceVariant = TextSecondary,

    outline = SilverDark,
    outlineVariant = Silver
)

/**
 * TrackerApp Dark Color Scheme
 * Premium, sleek, night-friendly
 */
private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue,
    onPrimary = TextPrimary,
    primaryContainer = AirbusBlueDark,
    onPrimaryContainer = SkyBlueLight,

    secondary = SkyBlueLight,
    onSecondary = TextPrimary,
    secondaryContainer = SkyBlueDark,
    onSecondaryContainer = SkyBlueLight,

    tertiary = SilverLight,
    onTertiary = TextPrimary,
    tertiaryContainer = SilverDark,
    onTertiaryContainer = SilverLight,

    error = androidx.compose.ui.graphics.Color(0xFFFFB4AB),
    onError = androidx.compose.ui.graphics.Color(0xFF690005),
    errorContainer = androidx.compose.ui.graphics.Color(0xFF93000A),
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFFFFDAD6),

    background = BackgroundDark,
    onBackground = TextPrimaryDark,

    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDarkElevated,
    onSurfaceVariant = TextSecondaryDark,

    outline = SilverDark,
    outlineVariant = SilverDark
)

/**
 * TrackerApp Theme
 *
 * Features:
 * - Material Design 3 implementation
 * - Dynamic color support on Android 12+
 * - Full dark mode support
 * - Edge-to-edge display
 * - Aviation-inspired color palette
 *
 * @param darkTheme Whether to use dark theme
 * @param dynamicColor Whether to use dynamic colors (Android 12+)
 * @param content The composable content
 */
@Composable
fun TrackerAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TrackerTypography,
        content = content
    )
}
