package com.mosqueseba.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.R

val NotoSansBengaliFontFamily = FontFamily(
    Font(R.font.noto_sans_bengali_regular, FontWeight.Normal),
    Font(R.font.noto_sans_bengali_medium, FontWeight.Medium),
    Font(R.font.noto_sans_bengali_bold, FontWeight.Bold)
)

val LiAdorNoirritFontFamily = FontFamily(
    Font(R.font.li_ador_noirrit_regular, FontWeight.Normal),
    Font(R.font.li_ador_noirrit_bold, FontWeight.Bold)
)

val HindSiliguriFontFamily = FontFamily(
    Font(R.font.hind_siliguri, FontWeight.Normal),
    Font(R.font.hind_siliguri, FontWeight.Medium),
    Font(R.font.hind_siliguri, FontWeight.SemiBold),
    Font(R.font.hind_siliguri, FontWeight.Bold),
    Font(R.font.hind_siliguri, FontWeight.ExtraBold)
)

val MosqueTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = LiAdorNoirritFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = LiAdorNoirritFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = NotoSansBengaliFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = NotoSansBengaliFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = NotoSansBengaliFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = NotoSansBengaliFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NotoSansBengaliFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NotoSansBengaliFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = NotoSansBengaliFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = NotoSansBengaliFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = NotoSansBengaliFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NotoSansBengaliFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)

