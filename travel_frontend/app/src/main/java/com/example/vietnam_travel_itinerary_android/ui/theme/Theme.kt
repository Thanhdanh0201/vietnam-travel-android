package com.example.vietnam_travel_itinerary_android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = VNRed,
    onPrimary = OnPrimaryLight,
    primaryContainer = VNRedContainer,
    onPrimaryContainer = VNRedOnContainer,

    secondary = SlateGray600,
    onSecondary = Color.White,
    secondaryContainer = SlateGray100,
    onSecondaryContainer = SlateGray800,

    tertiary = OrangeAccent,
    onTertiary = Color.White,

    background = BackgroundLight,
    onBackground = OnBackgroundLight,

    surface = SurfaceLight,
    onSurface = SlateGray900,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = SlateGray600,

    outline = SlateGray300,
    outlineVariant = SlateGray200,

    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = VNRedLight,
    onPrimary = OnPrimaryDark,
    primaryContainer = VNRedDark,
    onPrimaryContainer = VNRedContainer,

    secondary = SlateGray400,
    onSecondary = SlateGray900,
    secondaryContainer = SlateGray700,
    onSecondaryContainer = SlateGray200,

    tertiary = OrangeAccent,
    onTertiary = Color.Black,

    background = BackgroundDark,
    onBackground = OnBackgroundDark,

    surface = SurfaceDark,
    onSurface = SlateGray100,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = SlateGray400,

    outline = SlateGray600,
    outlineVariant = SlateGray700,

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

@Composable
fun VietnamTravelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Update system bars
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb() // Ensure Navigation Bar is transparent

            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme // Dark icons in light theme, light icons in dark theme
            insetsController.isAppearanceLightNavigationBars = !darkTheme // Dark navigation icons in light theme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}