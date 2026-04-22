package com.example.concentrate.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ONUOrange,
    secondary = Orange80,
    tertiary = ONUWhite,
    background = ONUBlack,
    surface = ONUBlack,
    onPrimary = ONUWhite,
    onSecondary = ONUBlack,
    onTertiary = ONUBlack,
    onBackground = ONUWhite,
    onSurface = ONUWhite
)

private val LightColorScheme = lightColorScheme(
    primary = ONUOrange,
    secondary = Orange40,
    tertiary = ONUBlack,
    background = ONUWhite,
    surface = ONUWhite,
    onPrimary = ONUWhite,
    onSecondary = ONUWhite,
    onTertiary = ONUWhite,
    onBackground = ONUBlack,
    onSurface = ONUBlack
)

@Composable
fun ConcentrateTheme(
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
        typography = Typography,
        content = content
    )
}
