package com.mosqueseba.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalIsDarkTheme = staticCompositionLocalOf { false }

private val MosqueLightColorScheme = lightColorScheme(
    primary = MosqueEmeraldPrimary,
    onPrimary = MosqueSurface,
    primaryContainer = MosqueEmeraldLight,
    onPrimaryContainer = MosqueSurface,
    secondary = MosqueGold,
    onSecondary = MosqueTextPrimary,
    secondaryContainer = MosqueGoldLight,
    onSecondaryContainer = MosqueTextPrimary,
    tertiary = MosqueGreenAccent,
    background = MosqueBgLight,
    onBackground = MosqueTextPrimary,
    surface = MosqueSurface,
    onSurface = MosqueTextPrimary,
    surfaceVariant = MosqueCardBg,
    onSurfaceVariant = MosqueTextSecondary,
    outline = MosqueCardBorder
)

private val MosqueDarkColorScheme = darkColorScheme(
    primary = MosqueGreenAccent,
    onPrimary = MosqueEmeraldDark,
    primaryContainer = MosqueEmeraldPrimary,
    onPrimaryContainer = MosqueNightTextPrimary,
    secondary = MosqueGold,
    onSecondary = MosqueEmeraldDark,
    secondaryContainer = Color(0xFF3A2D0B),
    onSecondaryContainer = MosqueGoldLight,
    tertiary = MosqueGreenAccent,
    background = MosqueNightBg,
    onBackground = MosqueNightTextPrimary,
    surface = MosqueNightSurface,
    onSurface = MosqueNightTextPrimary,
    surfaceVariant = MosqueNightSurfaceVariant,
    onSurfaceVariant = MosqueNightTextSecondary,
    outline = MosqueNightBorder
)

@Composable
fun MosqueCommitteeTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MosqueDarkColorScheme else MosqueLightColorScheme
    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MosqueTypography,
            content = content
        )
    }
}
