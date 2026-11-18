package com.mosqueseba.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
    secondaryContainer = MosqueNightSecondaryContainer,
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                // অ্যাপের টপ বার ডার্ক এমারেল্ড গ্রীন হওয়ায় সিস্টেম বার আইকন ও ঘড়ি/ব্যাটারি টেক্সট উজ্জ্বল সাদা (Light) হবে
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MosqueTypography,
            content = content
        )
    }
}
