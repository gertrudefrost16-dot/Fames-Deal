package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Gold400,
    onPrimary = Navy900,
    primaryContainer = Navy700,
    onPrimaryContainer = Gold400,
    secondary = Gold500,
    onSecondary = Navy900,
    secondaryContainer = Navy600,
    onSecondaryContainer = Gold100,
    tertiary = Gold600,
    background = Navy900,
    onBackground = PureWhite,
    surface = Navy800,
    onSurface = PureWhite,
    surfaceVariant = Navy700,
    onSurfaceVariant = LightSlate,
    outline = Navy600
)

private val LightColorScheme = lightColorScheme(
    primary = Navy800,
    onPrimary = PureWhite,
    primaryContainer = Navy100,
    onPrimaryContainer = Navy900,
    secondary = Gold500,
    onSecondary = Navy900,
    secondaryContainer = Gold100,
    onSecondaryContainer = Gold600,
    tertiary = Gold600,
    background = Navy50,
    onBackground = CharcoalText,
    surface = PureWhite,
    onSurface = CharcoalText,
    surfaceVariant = Navy100,
    onSurfaceVariant = MutedSlate,
    outline = LightSlate
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Preserve custom Navy and Gold theme across all devices
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
