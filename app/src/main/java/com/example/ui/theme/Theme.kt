package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = GrimmDarkBlue,
    onPrimary = Color.White,
    primaryContainer = GrimmDarkBlueLight,
    onPrimaryContainer = Color.White,
    secondary = GrimmLightBlue,
    onSecondary = Color.White,
    secondaryContainer = GrimmLightBlueSoft,
    onSecondaryContainer = GrimmDarkBlue,
    tertiary = GrimmCyan,
    onTertiary = Color.White,
    background = GrimmOffWhite,
    onBackground = GrimmTextPrimary,
    surface = GrimmSurface,
    onSurface = GrimmTextPrimary,
    surfaceVariant = GrimmSurfaceSubtle,
    onSurfaceVariant = GrimmTextSecondary,
    outline = GrimmBorder,
    outlineVariant = GrimmBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = GrimmDarkBlue,
    onPrimaryContainer = Color.White,
    secondary = GrimmLightBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFF93C5FD),
    tertiary = GrimmCyan,
    onTertiary = Color.Black,
    background = Color(0xFF0B1120),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF374151),
    outlineVariant = Color(0xFF1F2937)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
