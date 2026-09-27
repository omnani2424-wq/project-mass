package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ElectricPurple,
    onPrimary = SurfaceWhite,
    primaryContainer = Color(0xFFF3E8FF),
    onPrimaryContainer = DeepViolet,
    secondary = BrightCyan,
    onSecondary = SurfaceWhite,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = DeepCyan,
    tertiary = NeonMagenta,
    onTertiary = SurfaceWhite,
    tertiaryContainer = Color(0xFFFCE7F3),
    onTertiaryContainer = Color(0xFF831843),
    background = BackgroundWhite,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceLuminous,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceCardBorder,
    outlineVariant = Color(0xFFF1F5F9)
)

private val DarkColorScheme = darkColorScheme(
    primary = ElectricPurpleLight,
    onPrimary = SurfaceWhite,
    primaryContainer = DeepViolet,
    onPrimaryContainer = Color(0xFFF3E8FF),
    secondary = BrightCyan,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = DeepCyan,
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = NeonMagenta,
    onTertiary = SurfaceWhite,
    tertiaryContainer = Color(0xFF831843),
    onTertiaryContainer = Color(0xFFFCE7F3),
    background = Color(0xFF0D0B1E),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF13102B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E1A3D),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF332E59),
    outlineVariant = Color(0xFF1E1A3D)
)

@Composable
fun ProjectMassTheme(
    darkTheme: Boolean = false, // Primarily light per specifications
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
