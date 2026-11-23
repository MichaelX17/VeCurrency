package com.example.vecurrency.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = UsdBlue,
    onPrimary = DarkBackground,
    primaryContainer = DarkElevated,
    onPrimaryContainer = TextPrimary,
    secondary = EurPurple,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = TextPrimary,
    tertiary = UsdtYellow,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = ErrorRed,
    onError = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0066CC),
    onPrimary = LightSurface,
    primaryContainer = LightElevated,
    onPrimaryContainer = LightTextPrimary,
    secondary = EurPurple,
    onSecondary = LightSurface,
    secondaryContainer = LightSurface,
    onSecondaryContainer = LightTextPrimary,
    tertiary = UsdtYellow,
    onTertiary = LightSurface,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightCard,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = ErrorRed,
    onError = LightTextPrimary
)

@Composable
fun VeCurrencyTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
