package com.mosqueseba.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Islamic Modern Design System Palette (মেহরাব ও অ্যারাবেস্ক কালার প্যালেট)
// সমৃদ্ধ ইসলামিক ইমারল্ড #064E3B, গভীর মেহরাব #022C22, উজ্জ্বল #047857, সফট মিন্ট #E6F4EA
val MosqueEmeraldPrimary = Color(0xFF064E3B) // Rich Islamic Emerald
val MosqueEmeraldDark = Color(0xFF022C22)    // Deep Sanctuary Green (header / dark surface)
val MosqueEmeraldLight = Color(0xFF047857)   // Vibrant Prayer Green
val MosqueEmeraldSoft = Color(0xFFE6F4EA)    // Soft Mint Tint
val MosqueGreenAccent = Color(0xFF10B981)    // Crisp Emerald Accent (dark theme primary)

// Royal Islamic Gold Leaf & Minaret Accents — সোনালি গম্বুজ ও মিনার অ্যাকসেন্ট
val MosqueGold = Color(0xFFF59E0B)           // Radiant Dome Gold
val MosqueGoldDeep = Color(0xFFD97706)       // Antique Bronze / Minaret Gold
val MosqueGoldDark = Color(0xFFB45309)       // Deep Royal Ochre
val MosqueGoldLight = Color(0xFFFEF3C7)      // Warm Gold Shimmer
val MosqueGoldSoft = Color(0xFFFFFBEB)       // Soft Creamy Ivory Gold

// ক্লিন ইসলামিক হোয়াইট/মিন্ট ক্যানভাস
val MosqueBgLight = Color(0xFFF8FAF8)
val MosqueSurface = Color(0xFFFFFFFF)
val MosqueCardBg = Color(0xFFFFFFFF)
val MosqueCardBorder = Color(0xFFE2E8F0)
val MosqueCardBorderSubtle = Color(0xFFEEF2F6)
val MosqueCardBorderGold = Color(0xFFF3E8D0)
val MosqueBorderGold = Color(0xFFF3E8D0)
val MosqueGoldSubtle = Color(0xFFF3E8D0)

// টাইপোগ্রাফি ও ইঙ্ক (#0F172A / #475569)
val MosqueTextPrimary = Color(0xFF0F172A)
val MosqueTextDark = Color(0xFF0F172A)
val MosqueTextSecondary = Color(0xFF475569)
val MosqueTextMuted = Color(0xFF64748B) // WCAG compliant on white (placeholder/secondary)

// ডার্ক মোড — ফজর ও এশার উপযোগী নাইট মোড ক্যানভাস
val MosqueNightBg = Color(0xFF071511)
val MosqueNightSurface = Color(0xFF0E231D)
val MosqueNightSurfaceVariant = Color(0xFF143129)
val MosqueNightBorder = Color(0xFF234E41)
val MosqueNightInputBorder = Color(0xFF387162) // ইনপুট বক্সের বর্ডার — 4.38:1 on dark surface
val MosqueNightOnPrimary = Color(0xFF071511)   // ডার্ক থিমে emerald বাটনের উপর টেক্সট
val MosqueNightPrimaryContainer = Color(0xFF047857) // primaryContainer
val MosqueNightTextPrimary = Color(0xFFECFDF5)
val MosqueNightTextSecondary = Color(0xFFCBD5E1)
val MosqueNightTextMuted = Color(0xFF94A3B8)
val MosqueNightSecondaryContainer = Color(0xFF3A2D0B)

// স্ট্যাটাস কালার (হালাল ও ইসলামিক অর্থায়ন অনুযায়ী)
val MosqueUrgentRed = Color(0xFFDC2626)
val MosqueWarningAmber = Color(0xFFF59E0B)
val MosqueSuccessGreen = Color(0xFF047857)
val MosqueInfoBlue = Color(0xFF0284C7)

// রোল-নির্দিষ্ট অ্যাকসেন্ট
val RolePresidentAccent = Color(0xFF064E3B)
val RoleTreasurerAccent = Color(0xFFD97706)
val RoleSecretaryAccent = Color(0xFF0284C7)
val RoleImamAccent = Color(0xFF047857)
val RoleMusalliAccent = Color(0xFF0D9488)
val RolePublicityAccent = Color(0xFF7C3AED)
val RoleOfficeAccent = Color(0xFF0369A1)
val RoleWelfareEduAccent = Color(0xFF15803D)
val RoleAdvisoryAccent = Color(0xFFB45309)

// প্রধান ব্র্যান্ড গ্রেডিয়েন্ট (মেহরাব ও গম্বুজ গ্রেডিয়েন্ট)
val MosqueEmeraldGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF022C22),
        Color(0xFF064E3B),
        Color(0xFF047857)
    )
)

val MosqueEmeraldDarkGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF011A14),
        Color(0xFF022C22),
        Color(0xFF064E3B)
    )
)

val MosqueGoldGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFEF3C7),
        Color(0xFFF59E0B),
        Color(0xFFD97706)
    )
)

val IslamicMehrabHeaderGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF011A14),
        Color(0xFF022C22),
        Color(0xFF064E3B)
    )
)

val IslamicGoldTrimGradient = Brush.horizontalGradient(
    colors = listOf(
        Color.Transparent,
        Color(0xFFF59E0B).copy(alpha = 0.8f),
        Color(0xFFFEF3C7),
        Color(0xFFF59E0B).copy(alpha = 0.8f),
        Color.Transparent
    )
)

// রয়্যাল গোল্ড শিমার
val RoyalGoldShimmerGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFB45309),
        Color(0xFFFEF3C7),
        Color(0xFFD97706),
        Color(0xFFFFFBEB),
        Color(0xFFB45309)
    )
)

// রয়্যাল ইমারল্ড শিমার
val RoyalEmeraldShimmerGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF022C22),
        Color(0xFF064E3B),
        Color(0xFF047857),
        Color(0xFF10B981),
        Color(0xFF064E3B)
    )
)

// ওয়াক্তভিত্তিক আকাশ অ্যাম্বিয়েন্ট (নামাজের সময় বোঝায় — অপরিবর্তিত রাখা হয়েছে)
val WaqtFajrSkyGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0A1128),
        Color(0xFF1C1A3A),
        Color(0xFF38234A),
        Color(0xFF5B304C),
        Color(0xFF8C4A5A)
    )
)

val WaqtDhuhrSkyGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF022C22),
        Color(0xFF064E3B),
        Color(0xFF0F766E),
        Color(0xFF0284C7).copy(alpha = 0.75f)
    )
)

val WaqtAsrSkyGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF1F1610),
        Color(0xFF451A03),
        Color(0xFF78350F),
        Color(0xFFB45309),
        Color(0xFFD97706).copy(alpha = 0.85f)
    )
)

val WaqtMaghribSkyGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF1C0A26),
        Color(0xFF400E32),
        Color(0xFF6B1D38),
        Color(0xFF9A3412),
        Color(0xFFC2410C)
    )
)

val WaqtIshaSkyGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF01140E),
        Color(0xFF031E17),
        Color(0xFF082F26),
        Color(0xFF0F3B32),
        Color(0xFF13282A)
    )
)

fun getPrayerSkyGradient(prayerNameBn: String): Brush = when {
    prayerNameBn.contains("ফজর", ignoreCase = true) -> WaqtFajrSkyGradient
    prayerNameBn.contains("যোহর", ignoreCase = true) || prayerNameBn.contains("জুমুআ", ignoreCase = true) -> WaqtDhuhrSkyGradient
    prayerNameBn.contains("আসর", ignoreCase = true) -> WaqtAsrSkyGradient
    prayerNameBn.contains("মাগরিব", ignoreCase = true) -> WaqtMaghribSkyGradient
    prayerNameBn.contains("ইশা", ignoreCase = true) -> WaqtIshaSkyGradient
    else -> MosqueEmeraldDarkGradient
}
