package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TealAccent,
    secondary = TealSecondary,
    tertiary = AmberWarning,
    background = DarkBackground,
    surface = DarkSurface,
    primaryContainer = Color(0xFF162D4A),
    onPrimaryContainer = Color(0xFFF7F9FB),
    secondaryContainer = Color(0xFF162D4A),
    onSecondaryContainer = Color(0xFFF7F9FB),
    surfaceVariant = Color(0xFF162D4A),
    onSurfaceVariant = Color(0xFFF7F9FB),
    onPrimary = DarkBackground,
    onSecondary = DarkBackground,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    secondary = TealSecondary,
    tertiary = AmberWarning,
    background = LightBackground,
    surface = LightSurface,
    primaryContainer = Color(0xFFD3E3FD),
    onPrimaryContainer = Color(0xFF041E49),
    secondaryContainer = Color(0xFFD3E3FD),
    onSecondaryContainer = Color(0xFF041E49),
    surfaceVariant = Color(0xFFD3E3FD),
    onSurfaceVariant = Color(0xFF041E49),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    outline = LightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
