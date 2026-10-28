package com.mosqueseba.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MosqueColorScheme = lightColorScheme(
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

@Composable
fun MosqueCommitteeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MosqueColorScheme,
        typography = MosqueTypography,
        content = content
    )
}
