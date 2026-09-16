package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TrafficAmberPrimary,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF382900),
    onPrimaryContainer = TrafficAmberLight,
    secondary = TrafficCyanAccent,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF00363D),
    onSecondaryContainer = Color(0xFF80E9FF),
    tertiary = TrafficGreenSafe,
    background = TrafficNavyDark,
    onBackground = TrafficTextPrimary,
    surface = TrafficNavyCard,
    onSurface = TrafficTextPrimary,
    surfaceVariant = TrafficNavyCardElevated,
    onSurfaceVariant = TrafficTextSecondary,
    outline = TrafficNavyCardBorder,
    error = TrafficRedCritical,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFD97706),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    tertiary = Color(0xFF059669),
    background = TrafficLightBg,
    onBackground = TrafficLightText,
    surface = TrafficLightCard,
    onSurface = TrafficLightText,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = TrafficLightBorder,
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun TrafficSenseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // For high-tech command center feel, default to dark command center palette
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    TrafficSenseTheme(darkTheme = darkTheme, content = content)
}
