package com.mosqueseba.app.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    mosqueName: String = "বায়তুল জান্নাত জামে মসজিদ",
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
