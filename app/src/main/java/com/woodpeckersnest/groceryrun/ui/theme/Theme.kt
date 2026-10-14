package com.woodpeckersnest.groceryrun.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    primaryContainer = OrangeContainer,
    onPrimaryContainer = OnColorLight,
    secondary = OchreSecondary,
    onSecondary = Color.White,
    secondaryContainer = OchreContainer,
    onSecondaryContainer = OnColorLight,
    background = SurfaceLight,
    onBackground = OnColorLight,
    surface = SurfaceLight,
    onSurface = OnColorLight
)

private val DarkColors = darkColorScheme(
    primary = OrangePrimaryDark,
    onPrimary = Color.White,
    primaryContainer = OrangePrimaryDark,
    onPrimaryContainer = OnColorDark,
    secondary = OchreSecondary,
    onSecondary = Color.White,
    secondaryContainer = OchreSecondary,
    onSecondaryContainer = OnColorDark,
    background = SurfaceDark,
    onBackground = OnColorDark,
    surface = SurfaceDark,
    onSurface = OnColorDark
)

// App-wide Material3 theme. Dynamic color (Android 12+) is
// intentionally disabled so the orange/ochre brand identity is
// always shown consistently, regardless of device wallpaper.
@Composable
fun GroceryRunTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}