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
import androidx.compose.ui.layout.ContentScale
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
    val isDark = LocalIsDarkTheme.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        shadowElevation = 1.dp,
        modifier = modifier
            .height(116.dp)
            .testTag("royal_action_$title")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Modern Soft-Tonal Squircle Container with Icon & Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(50.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft,
                    border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else Color(0xFFD1FAE5)),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (isDark) Color(0xFF34D399) else MosqueEmeraldPrimary,
                            modifier = Modifier.size(22.dp)
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
                            .size(19.dp)
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
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = labelColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Modern Clean Mosque Summary KPI Card ("মসজিদের সংক্ষিপ্ত তথ্য")
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

    val isDark = LocalIsDarkTheme.current
    val surfaceColor = if (isDark) MosqueNightSurface else Color.White
    val borderColor = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
    val primaryText = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary
    val secondaryText = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("royal_mosque_summary_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Soft green bar chart icon + Title + "বিস্তারিত দেখুন ›"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft,
                        border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else Color(0xFFD1FAE5)),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF34D399) else MosqueEmeraldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "মসজিদের সংক্ষিপ্ত তথ্য",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                }

                // Clean Modern Pill Button: বিস্তারিত দেখুন ›
                Surface(
                    onClick = onViewDetailsClick,
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) MosqueNightSurfaceVariant else Color(0xFFF1F5F9)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "বিস্তারিত দেখুন",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFF34D399) else MosqueEmeraldLight
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "›",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF34D399) else MosqueEmeraldLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics Row (3 columns separated by subtle modern dividers)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: মুসল্লী
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${toBengaliDigits(displayMembers.toString())}+",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "মোট মুসল্লী",
                        fontSize = 11.5.sp,
                        color = secondaryText,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Vertical Divider 1
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(if (isDark) MosqueNightBorder else Color(0xFFE2E8F0))
                )

                // Column 2: পরিবার
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = toBengaliDigits(displayFamilies.toString()),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "পরিবার",
                        fontSize = 11.5.sp,
                        color = secondaryText,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Vertical Divider 2
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(if (isDark) MosqueNightBorder else Color(0xFFE2E8F0))
                )

                // Column 3: চলতি তহবিল
                Column(
                    modifier = Modifier.weight(1.1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "৳ ${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", displayChandaAmount))}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF34D399) else MosqueEmeraldLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "চলতি তহবিল",
                        fontSize = 11.5.sp,
                        color = secondaryText,
                        fontWeight = FontWeight.Medium
                    )
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
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft,
                    border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else Color(0xFFD1FAE5)),
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF34D399) else MosqueEmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "সাম্প্রতিক কার্যক্রম",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )
            }

            Surface(
                onClick = onViewAllClick,
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) MosqueNightSurfaceVariant else Color(0xFFF1F5F9)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "আরও দেখুন",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color(0xFF34D399) else MosqueEmeraldLight
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "›",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF34D399) else MosqueEmeraldLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Activities Container Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = surfaceColor,
            border = BorderStroke(1.dp, effectiveBorder),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                // Item 1: চাঁদা প্রদান
                val latestTxn = transactions.firstOrNull()
                val donorTitle = latestTxn?.description?.takeIf { it.isNotBlank() } ?: "চাঁদা প্রদান করেছেন: মোঃ আব্দুল করিম"
                val donorDate = latestTxn?.txnDate?.let { "তারিখ: $it" } ?: "১৮ নভেম্বর ২০২৫, ০৯:২০ AM"
                val chandaAmt = latestTxn?.amount?.let { "৳ ${toBengaliDigits(it.toInt().toString())}" } ?: "৳ ৫০০"

                ActivityRowItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    iconBg = if (isDark) Color(0xFF064E3B) else Color(0xFFE6F4EA),
                    iconTint = if (isDark) Color(0xFF34D399) else Color(0xFF047857),
                    title = donorTitle,
                    subtitle = donorDate,
                    tagText = chandaAmt,
                    tagBg = if (isDark) Color(0xFF064E3B) else Color(0xFFE6F4EA),
                    tagTextColor = if (isDark) Color(0xFF34D399) else Color(0xFF047857),
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
                    iconBg = if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.4f) else Color(0xFFEFF6FF),
                    iconTint = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                    title = "নতুন সদস্য যুক্ত হয়েছেন: মোঃ রফিকুল ইসলাম",
                    subtitle = "১৭ নভেম্বর ২০২৫",
                    tagText = "সদস্য",
                    tagBg = if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.4f) else Color(0xFFEFF6FF),
                    tagTextColor = if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8),
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
                val meetingDate = latestMeeting?.meetingDate?.let { "তারিখ: $it" } ?: "১৫ নভেম্বর ২০২৫"

                ActivityRowItem(
                    icon = Icons.Default.Article,
                    iconBg = if (isDark) Color(0xFF78350F).copy(alpha = 0.4f) else Color(0xFFFFFBEB),
                    iconTint = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                    title = meetingTitle,
                    subtitle = meetingDate,
                    tagText = "সভা",
                    tagBg = if (isDark) Color(0xFF78350F).copy(alpha = 0.4f) else Color(0xFFFFFBEB),
                    tagTextColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
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
    iconTint: Color,
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
        // Soft Squircle Icon Container
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = iconBg,
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
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
                fontWeight = FontWeight.SemiBold,
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
 * Modern Clean Inspirational Islamic Banner ("মসজিদ হলো সমাজের শান্তি ও ঐক্যের কেন্দ্রবিন্দু")
 */
@Composable
fun InspirationalIslamicQuoteBanner(
    quoteText: String = "মসজিদ হলো সমাজের শান্তি ও ঐক্যের কেন্দ্রবিন্দু"
) {
    val isDark = LocalIsDarkTheme.current
    val surfaceColor = if (isDark) MosqueNightSurfaceVariant else Color(0xFFF0FDF4)
    val borderColor = if (isDark) MosqueNightBorder else Color(0xFFDCFCE7)
    val quoteColor = if (isDark) MosqueNightTextPrimary else Color(0xFF065F46)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inspirational_quote_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Emblem / Book Icon
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) MosqueNightSurface else Color.White,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "পবিত্র কুরআন",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Vertical Green Line
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (isDark) Color(0xFF34D399) else Color(0xFF059669))
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Quote Content
            Text(
                text = "“$quoteText”",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = quoteColor,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
