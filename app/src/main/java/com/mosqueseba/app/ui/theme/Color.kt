package com.mosqueseba.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// bKash-অনুপ্রাণিত ডিজাইন সিস্টেম (আসল bKash অ্যাপের SVG এসেট থেকে নেওয়া প্যালেট)
// ব্র্যান্ড pink #E2136E, গাঢ় #AD054F, হালকা #EE2A7B, সফট #FDEFF4
val MosqueEmeraldPrimary = Color(0xFFE2136E) // bKash Brand Pink
val MosqueEmeraldDark = Color(0xFFAD054F)   // Deep bKash Pink (header / dark surface)
val MosqueEmeraldLight = Color(0xFFEE2A7B)  // Vibrant Pink
val MosqueEmeraldSoft = Color(0xFFFDEFF4)   // Soft Pink Tint
val MosqueGreenAccent = Color(0xFFF472B6)   // Rose Pink Accent (dark theme primary)

// Rose-gold accents — আগের সোনালি অ্যাকসেন্টের জায়গায় bKash-এর হালকা গোলাপি
val MosqueGold = Color(0xFFF472B6)           // Rose Accent
val MosqueGoldDeep = Color(0xFFDB2777)       // Deep Rose
val MosqueGoldDark = Color(0xFFBE185D)       // Dark Rose
val MosqueGoldLight = Color(0xFFFCE7F3)      // Light Pink Shimmer
val MosqueGoldSoft = Color(0xFFFFF1F6)       // Soft Pink Ivory

// ক্লিন হোয়াইট/গ্রে ক্যানভাস (bKash-এরcarte-স্টাইল)
val MosqueBgLight = Color(0xFFF7F7F7)
val MosqueSurface = Color(0xFFFFFFFF)
val MosqueCardBg = Color(0xFFFFFFFF)
val MosqueCardBorder = Color(0xFFEBEBEB)
val MosqueCardBorderSubtle = Color(0xFFF3F4F6)
val MosqueCardBorderGold = Color(0xFFFCE7F3)
val MosqueBorderGold = Color(0xFFFCE7F3)
val MosqueGoldSubtle = Color(0xFFFCE7F3)

// টাইপোগ্রাফি ও ইঙ্ক (bKash UI-র #273339 / #505050)
val MosqueTextPrimary = Color(0xFF273339)
val MosqueTextDark = Color(0xFF202020)
val MosqueTextSecondary = Color(0xFF505050)
val MosqueTextMuted = Color(0xFF9A9A9A)

// ডার্ক মোড — গাঢ় রোজ-ব্ল্যাক ক্যানভাস, গোলাপি অ্যাকসেন্ট
val MosqueNightBg = Color(0xFF150912)
val MosqueNightSurface = Color(0xFF24101C)
val MosqueNightSurfaceVariant = Color(0xFF321728)
val MosqueNightBorder = Color(0xFF5A2440)
val MosqueNightInputBorder = Color(0xFF9D3469) // ইনপুট বক্সের জন্য উজ্জ্বল বর্ডার
val MosqueNightTextPrimary = Color(0xFFFFF1F6)
val MosqueNightTextSecondary = Color(0xFFFBCFE8)
val MosqueNightTextMuted = Color(0xFFC99BB3)
val MosqueNightSecondaryContainer = Color(0xFF4A1233)

// স্ট্যাটাস কালার (bKash SVG প্যালেট অনুযায়ী)
val MosqueUrgentRed = Color(0xFFD0021B)
val MosqueWarningAmber = Color(0xFFFFAB00)
val MosqueSuccessGreen = Color(0xFF01846C)
val MosqueInfoBlue = Color(0xFF0284C7)

// রোল-নির্দিষ্ট অ্যাকসেন্ট (bKash-স্টাইলে প্রেসিডেন্ট pink, বাকিরা আলাদা চিন্ন)
val RolePresidentAccent = Color(0xFFE2136E)
val RoleTreasurerAccent = Color(0xFF01846C)
val RoleSecretaryAccent = Color(0xFF2563EB)
val RoleImamAccent = Color(0xFFAD054F)
val RoleMusalliAccent = Color(0xFF0D9488)
val RolePublicityAccent = Color(0xFF7C3AED)
val RoleOfficeAccent = Color(0xFF0369A1)
val RoleWelfareEduAccent = Color(0xFF15803D)
val RoleAdvisoryAccent = Color(0xFFBE185D)

// প্রধান ব্র্যান্ড গ্রেডিয়েন্ট
val MosqueEmeraldGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFAD054F),
        Color(0xFFE2136E),
        Color(0xFFEE2A7B)
    )
)

val MosqueEmeraldDarkGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF6B0235),
        Color(0xFFAD054F),
        Color(0xFFE2136E)
    )
)

val MosqueGoldGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFCE7F3),
        Color(0xFFF472B6),
        Color(0xFFDB2777)
    )
)

val IslamicMehrabHeaderGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF5C0230),
        Color(0xFFAD054F),
        Color(0xFFE2136E)
    )
)

val IslamicGoldTrimGradient = Brush.horizontalGradient(
    colors = listOf(
        Color.Transparent,
        Color(0xFFF472B6).copy(alpha = 0.8f),
        Color(0xFFFBCFE8),
        Color(0xFFF472B6).copy(alpha = 0.8f),
        Color.Transparent
    )
)

// রোজ-গোল্ড শিমার
val RoyalGoldShimmerGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFBE185D),
        Color(0xFFFBCFE8),
        Color(0xFFDB2777),
        Color(0xFFFCE7F3),
        Color(0xFFAD054F)
    )
)

// ব্র্যান্ড পিংক শিমার
val RoyalEmeraldShimmerGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF6B0235),
        Color(0xFFAD054F),
        Color(0xFFE2136E),
        Color(0xFFF472B6),
        Color(0xFFE2136E)
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
        Color(0xFF4A0124),
        Color(0xFFAD054F),
        Color(0xFFE2136E),
        Color(0xFFF472B6)
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
