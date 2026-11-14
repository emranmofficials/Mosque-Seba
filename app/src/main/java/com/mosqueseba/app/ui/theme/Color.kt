package com.mosqueseba.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Islamic Modern Design System Palette (মেহরাব ও অ্যারাবেস্ক কালার প্যালেট)
val MosqueEmeraldPrimary = Color(0xFF064E3B) // Rich Islamic Emerald
val MosqueEmeraldDark = Color(0xFF022C22)    // Deep Sanctuary Green
val MosqueEmeraldLight = Color(0xFF047857)   // Vibrant Prayer Green
val MosqueEmeraldSoft = Color(0xFFE6F4EA)    // Soft Tinted Emerald
val MosqueGreenAccent = Color(0xFF10B981)

// Royal Islamic Gold Leaf & Bronze Accents
val MosqueGold = Color(0xFFF59E0B)           // Radiant Dome Gold
val MosqueGoldDeep = Color(0xFFD97706)       // Antique Bronze / Minaret Gold
val MosqueGoldDark = Color(0xFFB45309)       // Deep Royal Ochre
val MosqueGoldLight = Color(0xFFFEF3C7)      // Warm Gold Shimmer
val MosqueGoldSoft = Color(0xFFFFFBEB)       // Soft Creamy Ivory Gold

// Islamic Clean Surfaces & Warm Parchment
val MosqueBgLight = Color(0xFFFBF9F5)        // Warm Islamic Parchment Ivory
val MosqueSurface = Color(0xFFFFFFFF)
val MosqueCardBg = Color(0xFFFFFFFF)
val MosqueCardBorder = Color(0xFFE5E7EB)
val MosqueCardBorderSubtle = Color(0xFFF3F4F6)
val MosqueCardBorderGold = Color(0xFFFDE68A)

// Typography & Ink Colors
val MosqueTextPrimary = Color(0xFF0F172A)
val MosqueTextDark = Color(0xFF0F172A)
val MosqueTextSecondary = Color(0xFF475569)
val MosqueTextMuted = Color(0xFF94A3B8)

// Eye-Comfort Night / Dark Theme Palette (ফজর ও এশার উপযোগী নাইট মোড)
val MosqueNightBg = Color(0xFF071511)
val MosqueNightSurface = Color(0xFF0E231D)
val MosqueNightSurfaceVariant = Color(0xFF143129)
val MosqueNightBorder = Color(0xFF234E41)
val MosqueNightTextPrimary = Color(0xFFECFDF5)
val MosqueNightTextSecondary = Color(0xFFCBD5E1)
val MosqueNightTextMuted = Color(0xFF94A3B8)

// Status & Alert Colors
val MosqueUrgentRed = Color(0xFFDC2626)
val MosqueWarningAmber = Color(0xFFD97706)
val MosqueSuccessGreen = Color(0xFF16A34A)
val MosqueInfoBlue = Color(0xFF0284C7)

// Role-Specific Identity Accent Palettes
val RolePresidentAccent = Color(0xFFD97706)     // Royal Executive Gold
val RoleTreasurerAccent = Color(0xFF059669)     // Treasury Ledger Emerald
val RoleSecretaryAccent = Color(0xFF2563EB)     // Secretariat Sapphire Blue
val RoleImamAccent = Color(0xFF047857)          // Sanctuary Mihrab Green
val RoleMusalliAccent = Color(0xFF0D9488)       // Devotional Teal
val RolePublicityAccent = Color(0xFF7C3AED)     // Editorial & Publicity Violet
val RoleOfficeAccent = Color(0xFF0369A1)        // Office Registry Deep Sky
val RoleWelfareEduAccent = Color(0xFF15803D)    // Welfare & Madrasa Forest Green
val RoleAdvisoryAccent = Color(0xFFB45309)      // Advisory Bronze Ochre

// Premium Islamic Mehrab & Minaret Gradients
val MosqueEmeraldGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF022C22),
        Color(0xFF064E3B),
        Color(0xFF047857)
    )
)

val MosqueEmeraldDarkGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF011C15),
        Color(0xFF022C22),
        Color(0xFF064E3B)
    )
)

val MosqueGoldGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFDE68A),
        Color(0xFFF59E0B),
        Color(0xFFD97706)
    )
)

val IslamicMehrabHeaderGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF011812),
        Color(0xFF022C22),
        Color(0xFF064E3B)
    )
)

val IslamicGoldTrimGradient = Brush.horizontalGradient(
    colors = listOf(
        Color.Transparent,
        Color(0xFFF59E0B).copy(alpha = 0.8f),
        Color(0xFFFDE68A),
        Color(0xFFF59E0B).copy(alpha = 0.8f),
        Color.Transparent
    )
)
