package com.mosqueseba.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

fun toBengaliDigits(input: String): String {
    val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val sb = StringBuilder(input.length)
    for (ch in input) {
        if (ch in '0'..'9') sb.append(bnDigits[ch - '0']) else sb.append(ch)
    }
    return sb.toString()
}

/** Convert Bengali (or Arabic-Indic) digits to ASCII so toDoubleOrNull() works. */
fun toEnglishDigits(input: String): String {
    val sb = StringBuilder(input.length)
    for (ch in input) {
        val digit = ch.digitToIntOrNull()
        if (digit != null && digit in 0..9) {
            sb.append(('0'.code + digit).toChar())
        } else when (ch) {
            '৳', ' ', ',', '-' -> {} // ignore currency/separators
            else -> sb.append(ch)
        }
    }
    return sb.toString()
}

fun formatCurrentHijriDateBn(): String {
    val hijrah = HijrahDate.now()
    val day = hijrah.get(ChronoField.DAY_OF_MONTH)
    val month = hijrah.get(ChronoField.MONTH_OF_YEAR)
    val year = hijrah.get(ChronoField.YEAR)
    val hijriMonthsBn = listOf(
        "মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি",
        "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শাবান",
        "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
    )
    val monthName = hijriMonthsBn.getOrElse(month - 1) { "" }
    return toBengaliDigits("$day $monthName $year হিজরি")
}

fun formatCurrentGregorianDateBn(): String {
    val today = LocalDate.now()
    val dayName = when (today.dayOfWeek) {
        DayOfWeek.SATURDAY -> "শনিবার"
        DayOfWeek.SUNDAY -> "রবিবার"
        DayOfWeek.MONDAY -> "সোমবার"
        DayOfWeek.TUESDAY -> "মঙ্গলবার"
        DayOfWeek.WEDNESDAY -> "বুধবার"
        DayOfWeek.THURSDAY -> "বৃহস্পতিবার"
        DayOfWeek.FRIDAY -> "শুক্রবার"
    }
    val monthsBn = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )
    val monthName = monthsBn.getOrElse(today.monthValue - 1) { "" }
    return toBengaliDigits("$dayName, ${today.dayOfMonth} $monthName ${today.year}")
}

/**
 * Ultra-Modern Islamic Card Container with Refined Mehrab Curve & Soft Shadows
 */
@Composable
fun MehrabArchCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MosqueCardBorderSubtle,
    elevation: Dp = 1.5.dp,
    showAccentTrim: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val effectiveBorder = if (isDark) MosqueNightBorder else borderColor

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0xFF064E3B).copy(alpha = if (isDark) 0.3f else 0.06f),
                ambientColor = Color(0xFF064E3B).copy(alpha = if (isDark) 0.2f else 0.04f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, effectiveBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (showAccentTrim) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(IslamicGoldTrimGradient)
                )
            }
            content()
        }
    }
}

/**
 * Modern Islamic Section Header & Divider
 */
@Composable
fun RubElHizbDivider(
    modifier: Modifier = Modifier,
    title: String? = null
) {
    val isDark = LocalIsDarkTheme.current
    val primaryText = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary
    val dividerColor = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle

    if (title != null) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(3.5.dp, 14.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MosqueEmeraldLight)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(0.8.dp)
                    .padding(start = 12.dp)
                    .background(dividerColor)
            )
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(0.8.dp)
                .padding(vertical = 4.dp)
                .background(dividerColor)
        )
    }
}

/**
 * Hijri Date & Bismillah Sanctuary Banner
 */
@Composable
fun HijriDateBanner(
    mosqueName: String = "মসজিদ সেবা",
    hijriDate: String = formatCurrentHijriDateBn(),
    gregorianDate: String = formatCurrentGregorianDateBn()
) {
    Surface(
        color = Color(0xFF022C22),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MosqueGold.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF011C15), Color(0xFF022C22), Color(0xFF064E3B))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MosqueGoldLight,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = mosqueName,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = MosqueGold.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MosqueGold.copy(alpha = 0.4f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NightsStay,
                            contentDescription = null,
                            tint = MosqueGoldLight,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = hijriDate,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MosqueGoldLight
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = gregorianDate,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }
    }
}

/**
 * Role-Specific Official Workspace Badge & Command Header
 */
@Composable
fun RoleWorkspaceHeader(
    currentRole: UserRole,
    onDutiesClick: () -> Unit = {},
    onProfileClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (title, subtitle, accentColor, icon) = when (currentRole) {
        UserRole.PRESIDENT, UserRole.VICE_PRESIDENT -> Quadruple(
            "${currentRole.titleBn} — নির্বাহী সুপ্রিম কন্ট্রোল রুম",
            currentRole.shortSummaryBn,
            RolePresidentAccent,
            Icons.Default.VerifiedUser
        )
        UserRole.TREASURER, UserRole.ASSISTANT_TREASURER -> Quadruple(
            "${currentRole.titleBn} — বায়তুল মাল ও ব্যাংকিং দপ্তর",
            currentRole.shortSummaryBn,
            RoleTreasurerAccent,
            Icons.Default.AccountBalanceWallet
        )
        UserRole.GENERAL_SECRETARY, UserRole.ASSISTANT_SECRETARY -> Quadruple(
            "${currentRole.titleBn} — প্রশাসনিক সচিবালয়",
            currentRole.shortSummaryBn,
            RoleSecretaryAccent,
            Icons.Default.FolderShared
        )
        UserRole.ORGANIZING_SECRETARY -> Quadruple(
            "${currentRole.titleBn} — সাংগঠনিক ও মুসল্লি সমন্বয়",
            currentRole.shortSummaryBn,
            RoleSecretaryAccent,
            Icons.Default.Handshake
        )
        UserRole.PUBLICITY_SECRETARY -> Quadruple(
            "প্রচার ও প্রকাশনা সেল",
            currentRole.shortSummaryBn,
            RolePublicityAccent,
            Icons.Default.Campaign
        )
        UserRole.OFFICE_SECRETARY -> Quadruple(
            "দপ্তর, দলিল ও চিঠিপত্র বিভাগ",
            currentRole.shortSummaryBn,
            RoleOfficeAccent,
            Icons.Default.Archive
        )
        UserRole.WELFARE_EDUCATION_SECRETARY -> Quadruple(
            "সমাজকল্যাণ ও ধর্মীয় শিক্ষা বিভাগ",
            currentRole.shortSummaryBn,
            RoleWelfareEduAccent,
            Icons.Default.School
        )
        UserRole.EXECUTIVE_MEMBER -> Quadruple(
            "কার্যনির্বাহী সদস্য — তদারকি ও সাক্ষ্য প্যানেল",
            currentRole.shortSummaryBn,
            RoleSecretaryAccent,
            Icons.Default.Shield
        )
        UserRole.ADVISORY_BOARD -> Quadruple(
            "সম্মানিত উপদেষ্টা পরিষদ",
            currentRole.shortSummaryBn,
            RoleAdvisoryAccent,
            Icons.Default.AccountBalance
        )
        UserRole.IMAM -> Quadruple(
            "খতিব ও ইমাম — পদাধিকারবলে শরিয়াহ উপদেষ্টা",
            currentRole.shortSummaryBn,
            RoleImamAccent,
            Icons.Default.Mosque
        )
        UserRole.MUAZZIN, UserRole.KHADEM -> Quadruple(
            "মসজিদ খেদমত ও নামাজ পরিচালনা পোর্টাল",
            currentRole.shortSummaryBn,
            RoleImamAccent,
            Icons.Default.Mosque
        )
        UserRole.GENERAL_MEMBER -> Quadruple(
            "মুসল্লি খেদমত ও স্বচ্ছ তহবিল পোর্টাল",
            currentRole.shortSummaryBn,
            RoleMusalliAccent,
            Icons.Default.People
        )
    }

    val isDark = LocalIsDarkTheme.current
    val headerTitleColor = if (isDark) MosqueNightTextPrimary else MaterialTheme.colorScheme.primary

    Surface(
        color = accentColor.copy(alpha = if (isDark) 0.12f else 0.08f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.28f)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onDutiesClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, accentColor.copy(alpha = 0.7f), Color.Transparent)
                        )
                    )
            )
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .border(1.5.dp, Color.White.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = headerTitleColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.16f),
                        border = androidx.compose.foundation.BorderStroke(0.6.dp, accentColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "দায়িত্ব",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (onProfileClick != null) {
                        Surface(
                            onClick = onProfileClick,
                            shape = RoundedCornerShape(8.dp),
                            color = MosqueEmeraldPrimary
                        ) {
                            Text(
                                text = "আমার প্রোফাইল",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * Global Islamic Member Avatar with Photo Support & Emerald-Gold Fallback
 */
@Composable
fun MosqueMemberAvatar(
    nameBn: String,
    photoUrl: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    borderStroke: BorderStroke = BorderStroke(1.5.dp, MosqueGold.copy(alpha = 0.85f)),
    shape: androidx.compose.ui.graphics.Shape = CircleShape,
    isActive: Boolean? = null,
    fallbackBgColor: Color = MosqueEmeraldSoft,
    fallbackTextColor: Color = MosqueEmeraldDark
) {
    val photoBitmap = remember(photoUrl) {
        val px = (size.value * 3.5f).toInt().coerceAtLeast(120)
        loadLocalThumbnail(photoUrl, px)
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        Surface(
            shape = shape,
            color = fallbackBgColor,
            border = borderStroke,
            modifier = Modifier.size(size)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (photoBitmap != null) {
                    Image(
                        bitmap = photoBitmap,
                        contentDescription = nameBn,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val initial = nameBn.trim().firstOrNull()?.toString() ?: "স"
                    Text(
                        text = initial,
                        fontSize = (size.value * 0.42f).sp,
                        fontWeight = FontWeight.Bold,
                        color = fallbackTextColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (isActive != null) {
            val dotSize = (size.value * 0.28f).coerceIn(9f, 13f).dp
            Surface(
                shape = CircleShape,
                color = if (isActive) MosqueSuccessGreen else MosqueUrgentRed,
                border = BorderStroke(1.2.dp, Color.White),
                modifier = Modifier
                    .size(dotSize)
                    .offset(x = 1.dp, y = 1.dp)
            ) {}
        }
    }
}

/**
 * 8-pointed Rub-el-Hizb Arabesque Star Motif with elegant dual-square geometry
 */
@Composable
fun ArabesqueStarMotif(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    color: Color = MosqueGold,
    strokeWidth: Dp = 1.2.dp,
    fillAlpha: Float = 0.15f
) {
    Canvas(modifier = modifier.size(size)) {
        val side = this.size.minDimension
        val half = side / 2f
        val squareSide = side * 0.72f
        val offset = (side - squareSide) / 2f
        val strokePx = strokeWidth.toPx()

        // Square 1 (0 deg)
        drawRect(
            color = color.copy(alpha = fillAlpha),
            topLeft = androidx.compose.ui.geometry.Offset(offset, offset),
            size = androidx.compose.ui.geometry.Size(squareSide, squareSide)
        )
        drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(offset, offset),
            size = androidx.compose.ui.geometry.Size(squareSide, squareSide),
            style = Stroke(width = strokePx)
        )

        // Square 2 (45 deg rotated)
        rotate(45f, pivot = androidx.compose.ui.geometry.Offset(half, half)) {
            drawRect(
                color = color.copy(alpha = fillAlpha),
                topLeft = androidx.compose.ui.geometry.Offset(offset, offset),
                size = androidx.compose.ui.geometry.Size(squareSide, squareSide)
            )
            drawRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(offset, offset),
                size = androidx.compose.ui.geometry.Size(squareSide, squareSide),
                style = Stroke(width = strokePx)
            )
        }

        // Center jewel dot
        drawCircle(
            color = color,
            radius = side * 0.09f,
            center = androidx.compose.ui.geometry.Offset(half, half)
        )
    }
}

/**
 * Luxury Circular Prayer Countdown Ring with animated radial progress & gold shimmer
 */
@Composable
fun CircularPrayerCountdownRing(
    prayerName: String,
    remainingMinutes: Long,
    totalWindowMinutes: Long = 180L,
    modifier: Modifier = Modifier,
    size: Dp = 92.dp
) {
    val progress = (1f - (remainingMinutes.toFloat() / totalWindowMinutes.coerceAtLeast(1))).coerceIn(0.05f, 0.98f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "prayerCountdownProgress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "ringShimmer")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 5.5.dp.toPx()
            val diameter = this.size.minDimension - strokeWidth
            val radius = diameter / 2f
            val center = androidx.compose.ui.geometry.Offset(this.size.width / 2f, this.size.height / 2f)

            // Background track (soft golden ring)
            drawCircle(
                color = Color.White.copy(alpha = 0.14f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Outer decorative thin ring
            drawCircle(
                color = MosqueGold.copy(alpha = 0.25f),
                radius = radius + (strokeWidth * 0.85f),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Active animated progress arc (Royal Gold sweep)
            val sweepAngle = animatedProgress * 360f
            drawArc(
                brush = RoyalGoldShimmerGradient,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2f, strokeWidth / 2f),
                size = androidx.compose.ui.geometry.Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Glowing indicator bead at current progress point
            val angleRad = Math.toRadians((sweepAngle - 90f).toDouble())
            val beadX = center.x + (radius * Math.cos(angleRad)).toFloat()
            val beadY = center.y + (radius * Math.sin(angleRad)).toFloat()

            drawCircle(
                color = MosqueGold.copy(alpha = pulseGlow * 0.45f),
                radius = strokeWidth * 1.5f,
                center = androidx.compose.ui.geometry.Offset(beadX, beadY)
            )
            drawCircle(
                color = Color.White,
                radius = strokeWidth * 0.7f,
                center = androidx.compose.ui.geometry.Offset(beadX, beadY)
            )
        }

        // Center remaining time & waqt
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = prayerName,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = MosqueGoldLight
            )
            val hours = remainingMinutes / 60
            val mins = remainingMinutes % 60
            val timeText = if (hours > 0) "${hours}ঘ ${mins}মি" else "${mins} মি."
            Text(
                text = toBengaliDigits(timeText),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "বাকি",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.82f)
            )
        }
    }
}

/**
 * Custom Mehrab Header Curved Arch Shape for Global App Header
 */
class MehrabHeaderShape(private val archDepthDp: Dp = 16.dp) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val archDepthPx = with(density) { archDepthDp.toPx() }
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - archDepthPx)
            cubicTo(
                size.width * 0.70f, size.height,
                size.width * 0.30f, size.height,
                0f, size.height - archDepthPx
            )
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Subtle Islamic Geometric Arabesque Watermark pattern for Headers and Cards
 */
@Composable
fun IslamicGeometricWatermark(
    modifier: Modifier = Modifier,
    patternColor: Color = Color.White.copy(alpha = 0.05f),
    cellSize: Dp = 44.dp
) {
    Canvas(modifier = modifier) {
        val cellPx = cellSize.toPx()
        val cols = (size.width / cellPx).toInt() + 2
        val rows = (size.height / cellPx).toInt() + 2
        val starRadius = cellPx * 0.32f

        for (i in 0 until cols) {
            for (j in 0 until rows) {
                val cx = i * cellPx
                val cy = j * cellPx
                val half = starRadius
                drawRect(
                    color = patternColor,
                    topLeft = androidx.compose.ui.geometry.Offset(cx - half * 0.7f, cy - half * 0.7f),
                    size = androidx.compose.ui.geometry.Size(half * 1.4f, half * 1.4f),
                    style = Stroke(width = 0.75.dp.toPx())
                )
                rotate(45f, pivot = androidx.compose.ui.geometry.Offset(cx, cy)) {
                    drawRect(
                        color = patternColor,
                        topLeft = androidx.compose.ui.geometry.Offset(cx - half * 0.7f, cy - half * 0.7f),
                        size = androidx.compose.ui.geometry.Size(half * 1.4f, half * 1.4f),
                        style = Stroke(width = 0.75.dp.toPx())
                    )
                }
            }
        }
    }
}

/**
 * Subtle Mosque Skyline Silhouette for Background Cards & Quotes
 */
@Composable
fun MosqueSkylineSilhouette(
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = 0.08f)
) {
    Canvas(modifier = modifier.fillMaxWidth()) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(0f, h)
            lineTo(w * 0.15f, h)
            lineTo(w * 0.15f, h * 0.65f)
            cubicTo(w * 0.18f, h * 0.40f, w * 0.26f, h * 0.40f, w * 0.29f, h * 0.65f)
            lineTo(w * 0.29f, h)
            lineTo(w * 0.36f, h)
            lineTo(w * 0.36f, h * 0.25f)
            lineTo(w * 0.38f, h * 0.15f)
            lineTo(w * 0.40f, h * 0.25f)
            lineTo(w * 0.40f, h)
            lineTo(w * 0.45f, h)
            lineTo(w * 0.45f, h * 0.55f)
            cubicTo(w * 0.50f, h * 0.20f, w * 0.62f, h * 0.20f, w * 0.67f, h * 0.55f)
            lineTo(w * 0.67f, h)
            lineTo(w * 0.72f, h)
            lineTo(w * 0.72f, h * 0.25f)
            lineTo(w * 0.74f, h * 0.15f)
            lineTo(w * 0.76f, h * 0.25f)
            lineTo(w * 0.76f, h)
            lineTo(w * 0.81f, h)
            lineTo(w * 0.81f, h * 0.65f)
            cubicTo(w * 0.84f, h * 0.42f, w * 0.92f, h * 0.42f, w * 0.95f, h * 0.65f)
            lineTo(w * 0.95f, h)
            lineTo(w, h)
            close()
        }
        drawPath(path = path, color = color)
    }
}

