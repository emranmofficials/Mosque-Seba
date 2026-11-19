package com.mosqueseba.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
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
    onPrimary = MosqueNightOnPrimary,
    primaryContainer = MosqueNightPrimaryContainer,
    onPrimaryContainer = MosqueNightTextPrimary,
    secondary = MosqueGold,
    onSecondary = MosqueNightOnPrimary,
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

// সব স্ক্রিনে একই রাউন্ডেড লুক নিশ্চিত করতে গ্লোবাল শেপ স্কেল
private val MosqueShapes = Shapes(
    extraSmall = RoundedCornerShape(MosqueRadius.Sm),
    small = RoundedCornerShape(MosqueRadius.Md),
    medium = RoundedCornerShape(MosqueRadius.Lg),
    large = RoundedCornerShape(MosqueRadius.Xl),
    extraLarge = RoundedCornerShape(MosqueRadius.Xl)
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
                // ইমারল্ড হেডারের উপর সিস্টেম বার আইকন ও ঘড়ি/ব্যাটারি টেক্সট উজ্জ্বল সাদা রাখা হয়
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MosqueTypography,
            shapes = MosqueShapes,
            content = content
        )
    }
}
