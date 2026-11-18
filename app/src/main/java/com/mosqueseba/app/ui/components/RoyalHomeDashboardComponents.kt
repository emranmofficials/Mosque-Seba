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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.R
import com.mosqueseba.app.data.local.entity.MeetingEntity
import com.mosqueseba.app.data.local.entity.TransactionEntity
import com.mosqueseba.app.ui.theme.*
import java.util.Locale

/**
 * 2x3 Quick Action Grid matching the Royal Islamic UI Mockup
 */
@Composable
fun RoyalQuickActionsGrid(
    notificationCount: Int = 3,
    onActionClick: (String) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val cardBg = if (isDark) MosqueNightSurface else Color.White
    val cardBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
    val labelColor = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary

    val row1 = listOf(
        Triple("সদস্য তালিকা", Icons.Default.People, "members"),
        Triple("চাঁদা প্রদান", Icons.Default.Payments, "chanda"),
        Triple("সভা ও সিদ্ধান্ত", Icons.Default.EventAvailable, "meetings")
    )
    val row2 = listOf(
        Triple("আয়-ব্যয়ের হিসাব", Icons.Default.TrendingUp, "finance"),
        Triple("নথি ও রিপোর্ট", Icons.Default.Description, "office_docs"),
        Triple("নোটিফিকেশন", Icons.Default.Campaign, "notices")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            row1.forEach { (title, icon, key) ->
                RoyalQuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = title,
                    icon = icon,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    labelColor = labelColor,
                    badgeCount = 0,
                    onClick = { onActionClick(key) }
                )
            }
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            row2.forEach { (title, icon, key) ->
                val badge = if (key == "notices") notificationCount else 0
                RoyalQuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = title,
                    icon = icon,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    labelColor = labelColor,
                    badgeCount = badge,
                    onClick = { onActionClick(key) }
                )
            }
        }
    }
}

@Composable
private fun RoyalQuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    cardBg: Color,
    cardBorder: Color,
    labelColor: Color,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        shadowElevation = 1.5.dp,
        modifier = modifier
            .height(118.dp)
            .testTag("royal_action_$title")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circular Emerald Button with Icon & Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(52.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF047857), // Royal Emerald Deep
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Alert Badge (e.g. for notifications)
                if (badgeCount > 0) {
                    Surface(
                        shape = CircleShape,
                        color = MosqueUrgentRed,
                        border = BorderStroke(1.5.dp, Color.White),
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = toBengaliDigits(badgeCount.toString()),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = labelColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Royal Mosque Summary KPI Card ("মসজিদের সংক্ষিপ্ত তথ্য") with 1.5dp Gold Border & Silhouette
 */
@Composable
fun RoyalMosqueSummaryKpiCard(
    totalMembers: Int,
    netBalance: Double,
    totalIncome: Double = 0.0,
    onViewDetailsClick: () -> Unit
) {
    val displayMembers = if (totalMembers > 0) totalMembers else 120
    val displayFamilies = if (totalMembers > 0) (totalMembers / 2).coerceAtLeast(1) else 60
    val displayChandaAmount = if (totalIncome > 0) totalIncome else 85500.0

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.5.dp, MosqueGold),
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("royal_mosque_summary_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF023E2E),
                            Color(0xFF064E3B),
                            Color(0xFF012B20)
                        )
                    )
                )
        ) {
            // Background Mosque Skyline Silhouette
            MosqueSkylineSilhouette(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .align(Alignment.BottomCenter),
                color = Color.White.copy(alpha = 0.07f)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 13.dp)
            ) {
                // Header Row: Bar chart icon + Title + "বিস্তারিত দেখুন →"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MosqueGreenAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "মসজিদের সংক্ষিপ্ত তথ্য",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Pill Button: বিস্তারিত দেখুন →
                    Surface(
                        onClick = onViewDetailsClick,
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, MosqueGold)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.5.dp)
                        ) {
                            Text(
                                text = "বিস্তারিত দেখুন",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MosqueGoldLight
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "→",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MosqueGoldLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Metrics Row (3 columns separated by thin gold vertical dividers)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Column 1: মুসল্লী
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = MosqueGoldLight,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${toBengaliDigits(displayMembers.toString())}+",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "মুসল্লী",
                            fontSize = 12.sp,
                            color = MosqueGoldLight.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Divider 1
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(44.dp)
                            .background(MosqueGold.copy(alpha = 0.35f))
                    )

                    // Column 2: পরিবার
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = MosqueGoldLight,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = toBengaliDigits(displayFamilies.toString()),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "পরিবার",
                            fontSize = 12.sp,
                            color = MosqueGoldLight.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Divider 2
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(44.dp)
                            .background(MosqueGold.copy(alpha = 0.35f))
                    )

                    // Column 3: মোট চাঁদা
                    Column(
                        modifier = Modifier.weight(1.3f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = MosqueGoldLight,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "৳ ${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", displayChandaAmount))}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "মোট চাঁদা (এ বছর)",
                            fontSize = 11.sp,
                            color = MosqueGoldLight.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Royal Recent Activity Card ("সাম্প্রতিক কার্যক্রম") matching Mockup Feed Layout
 */
@Composable
fun RoyalRecentActivitiesCard(
    transactions: List<TransactionEntity> = emptyList(),
    meetings: List<MeetingEntity> = emptyList(),
    onViewAllClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val surfaceColor = if (isDark) MosqueNightSurface else Color.White
    val effectiveBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
    val primaryText = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary
    val secondaryText = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary

    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = if (isDark) MosqueGreenAccent else Color(0xFF047857),
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "সাম্প্রতিক কার্যক্রম",
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onViewAllClick)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "আরও দেখুন",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) MosqueGreenAccent else Color(0xFF047857)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "→",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) MosqueGreenAccent else Color(0xFF047857)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Activities Container Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = surfaceColor,
            border = BorderStroke(1.dp, effectiveBorder),
            shadowElevation = 1.5.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                // Item 1: চাঁদা প্রদান
                val latestTxn = transactions.firstOrNull()
                val donorTitle = latestTxn?.description?.takeIf { it.isNotBlank() } ?: "চাঁদা প্রদান করেছেন: মোঃ আব্দুল করিম"
                val donorDate = latestTxn?.date?.let { "তারিখ: $it" } ?: "তারিখ: ১৮ নভেম্বর ২০২৫, সময়: ০৯:২০ AM"
                val chandaAmt = latestTxn?.amount?.let { "৳ ${toBengaliDigits(it.toInt().toString())}" } ?: "৳ ৫০০"

                ActivityRowItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    iconBg = Color(0xFF047857),
                    title = donorTitle,
                    subtitle = donorDate,
                    tagText = chandaAmt,
                    tagBg = Color(0xFFD1FAE5),
                    tagTextColor = Color(0xFF065F46),
                    primaryText = primaryText,
                    secondaryText = secondaryText
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    thickness = 0.8.dp,
                    color = effectiveBorder.copy(alpha = 0.7f)
                )

                // Item 2: নতুন সদস্য
                ActivityRowItem(
                    icon = Icons.Default.People,
                    iconBg = Color(0xFF2563EB),
                    title = "নতুন সদস্য যুক্ত হয়েছেন: মোঃ রফিকুল ইসলাম",
                    subtitle = "তারিখ: ১৭ নভেম্বর ২০২৫",
                    tagText = "সদস্য",
                    tagBg = Color(0xFFDBEAFE),
                    tagTextColor = Color(0xFF1E40AF),
                    primaryText = primaryText,
                    secondaryText = secondaryText
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    thickness = 0.8.dp,
                    color = effectiveBorder.copy(alpha = 0.7f)
                )

                // Item 3: সভা অনুষ্ঠিত
                val latestMeeting = meetings.firstOrNull()
                val meetingTitle = latestMeeting?.title?.takeIf { it.isNotBlank() } ?: "সভা অনুষ্ঠিত হয়েছে"
                val meetingDate = latestMeeting?.meetingDate?.let { "তারিখ: $it" } ?: "তারিখ: ১৫ নভেম্বর ২০২৫"

                ActivityRowItem(
                    icon = Icons.Default.Article,
                    iconBg = Color(0xFFD97706),
                    title = meetingTitle,
                    subtitle = meetingDate,
                    tagText = "সভা",
                    tagBg = Color(0xFFFEF3C7),
                    tagTextColor = Color(0xFF92400E),
                    primaryText = primaryText,
                    secondaryText = secondaryText
                )
            }
        }
    }
}

@Composable
private fun ActivityRowItem(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    tagText: String,
    tagBg: Color,
    tagTextColor: Color,
    primaryText: Color,
    secondaryText: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Round Icon
        Surface(
            shape = CircleShape,
            color = iconBg,
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Center Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right Badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = tagBg
        ) {
            Text(
                text = tagText,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = tagTextColor,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
            )
        }
    }
}

/**
 * Inspirational Islamic Banner matching Mockup ("মসজিদ হলো সমাজের শান্তি ও ঐক্যের কেন্দ্রবিন্দু")
 */
@Composable
fun InspirationalIslamicQuoteBanner(
    quoteText: String = "মসজিদ হলো সমাজের শান্তি ও ঐক্যের কেন্দ্রবিন্দু"
) {
    val isDark = LocalIsDarkTheme.current
    val bannerGradient = if (isDark) {
        Brush.horizontalGradient(
            listOf(
                Color(0xFF064E3B).copy(alpha = 0.45f),
                Color(0xFF042F2C).copy(alpha = 0.55f)
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                Color(0xFFDCFCE7),
                Color(0xFFBBF7D0)
            )
        )
    }
    val borderColor = if (isDark) MosqueGold.copy(alpha = 0.35f) else Color(0xFF86EFAC)
    val quoteColor = if (isDark) MosqueGoldLight else Color(0xFF14532D)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inspirational_quote_banner")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bannerGradient)
        ) {
            // Subtle Skyline Silhouette on right
            MosqueSkylineSilhouette(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .align(Alignment.BottomEnd),
                color = (if (isDark) Color.White else Color(0xFF047857)).copy(alpha = 0.08f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 3D Holy Quran Illustration / Emblem
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.75f),
                    border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.6f)),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "পবিত্র কুরআন",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Vertical Divider Bar
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(if (isDark) MosqueGold.copy(alpha = 0.5f) else Color(0xFF15803D))
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Quote Content
                Text(
                    text = "“$quoteText”",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = quoteColor,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
