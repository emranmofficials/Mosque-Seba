package com.mosqueseba.app.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.R
import com.mosqueseba.app.data.local.entity.MosqueEntity
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.theme.*

@Composable
fun HeroCard(
    mosque: MosqueEntity?,
    onMosqueProfileClick: (() -> Unit)? = null
) {
    val nameBn = mosque?.nameBn ?: "মসজিদ সেবা"
    val address = mosque?.address ?: "ঠিকানা যুক্ত করা হয়নি"
    val committeeId = mosque?.committeeId ?: "কমিটি আইডি"
    val establishedBn = mosque?.establishedYear?.let { toBengaliDigits(it.toString()) } ?: "—"
    val startYearBn = mosque?.committeeStartYear?.let { toBengaliDigits(it.toString()) } ?: "—"
    val endYearBn = mosque?.committeeEndYear?.let { toBengaliDigits(it.toString()) } ?: "—"
    val mosqueLogoBitmap = remember(mosque?.logoUrl) { loadLocalThumbnail(mosque?.logoUrl, 256) }
    val (_, nextPrayerSummary) = remember { computeTodayPrayerSchedule() }
    val skyGradient = remember(nextPrayerSummary.nameBn) { getPrayerSkyGradient(nextPrayerSummary.nameBn) }

    MehrabArchCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onMosqueProfileClick != null) {
                    Modifier.clickable(onClick = onMosqueProfileClick)
                } else Modifier
            ),
        backgroundColor = Color.Transparent,
        borderColor = MosqueCardBorderGold.copy(alpha = 0.65f),
        elevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .background(skyGradient)
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Committee ID Pill with Arabesque Star
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.22f))
                            .border(BorderStroke(0.8.dp, MosqueGold.copy(alpha = 0.45f)), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ArabesqueStarMotif(size = 12.dp, color = MosqueGoldLight)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "কমিটি আইডি: $committeeId",
                            style = MaterialTheme.typography.labelSmall,
                            color = MosqueGoldLight,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Live Next Prayer Ambient Pill
                    Surface(
                        color = Color.Black.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(0.8.dp, MosqueGold.copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MosqueGoldLight,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${nextPrayerSummary.nameBn}: ${nextPrayerSummary.remainingTextBn} বাকি",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mosque Logo & Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.5.dp, MosqueGoldGradient, RoundedCornerShape(12.dp))
                            .background(MosqueEmeraldDark)
                            .padding(2.dp)
                            .testTag("hero_mosque_logo"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (mosqueLogoBitmap != null) {
                            Image(
                                bitmap = mosqueLogoBitmap,
                                contentDescription = "মসজিদের অফিসিয়াল লোগো",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "মসজিদের অফিসিয়াল লোগো",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }
                    }
                    Text(
                        text = nameBn,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Location
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MosqueGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Committee tenure indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "মেয়াদ: $startYearBn — $endYearBn খ্রি.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = if (onMosqueProfileClick != null) "মসজিদ প্রোফাইল ›" else "কার্যনির্বাহী পরিষদ • সক্রিয়",
                        style = MaterialTheme.typography.labelSmall,
                        color = MosqueGoldLight,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Compact Unified Executive Hero Card for President Dashboard — slim height, zero duplicate info, animated feedback.
 */
@Composable
fun ExecutiveMosqueHeroCard(
    mosque: MosqueEntity?,
    currentRole: UserRole = UserRole.PRESIDENT,
    showHijriDate: Boolean = true,
    onDutiesClick: () -> Unit,
    onMosqueProfileClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current

    val nameBn = mosque?.nameBn ?: "মসজিদ সেবা"
    val baseAddress = mosque?.address ?: "ঠিকানা যুক্ত করা হয়নি"
    val fullAddress = remember(baseAddress, mosque?.upazila, mosque?.district) {
        val extra = listOf(mosque?.upazila.orEmpty(), mosque?.district.orEmpty())
            .filter { it.isNotBlank() && !baseAddress.contains(it, ignoreCase = true) }
        if (extra.isNotEmpty()) "$baseAddress (${extra.joinToString(", ")})" else baseAddress
    }
    val committeeId = mosque?.committeeId ?: "কমিটি আইডি"
    val establishedBn = mosque?.establishedYear?.let { toBengaliDigits(it.toString()) } ?: "—"
    val startYearBn = mosque?.committeeStartYear?.let { toBengaliDigits(it.toString()) } ?: "—"
    val endYearBn = mosque?.committeeEndYear?.let { toBengaliDigits(it.toString()) } ?: "—"
    val hijriDate = remember { formatCurrentHijriDateBn() }
    val mosqueLogoBitmap = remember(mosque?.logoUrl) { loadLocalThumbnail(mosque?.logoUrl, 256) }
    val (_, nextPrayerSummary) = remember { computeTodayPrayerSchedule() }
    val skyGradient = remember(nextPrayerSummary.nameBn) { getPrayerSkyGradient(nextPrayerSummary.nameBn) }

    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            kotlinx.coroutines.delay(1600)
            copied = false
        }
    }

    val isDark = LocalIsDarkTheme.current
    val surfaceColor = if (isDark) MosqueNightSurface else Color.White
    val effectiveBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
    val primaryText = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary
    val secondaryText = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary

    Surface(
        onClick = { onMosqueProfileClick?.invoke() },
        shape = RoundedCornerShape(16.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, effectiveBorder),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("executive_hero_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Mosque Photo / Image Container
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MosqueEmeraldDark.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, effectiveBorder),
                modifier = Modifier
                    .size(width = 96.dp, height = 82.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (mosqueLogoBitmap != null) {
                        Image(
                            bitmap = mosqueLogoBitmap,
                            contentDescription = nameBn,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = nameBn,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right: Mosque Name, Location, Established Date & Active Badge
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nameBn,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp))

                // Location with emerald pin
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "ঠিকানা",
                        tint = Color(0xFF047857),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = fullAddress,
                        fontSize = 12.sp,
                        color = secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                // Established Date + Active Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "প্রতিষ্ঠা সাল",
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(13.5.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "স্থাপিত: ${if (establishedBn != "—") establishedBn else "২০২৪"} ইং",
                            fontSize = 11.5.sp,
                            color = secondaryText
                        )
                    }

                    // Green Pill Badge: "সক্রিয়"
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF047857),
                        modifier = Modifier.padding(top = 1.dp)
                    ) {
                        Text(
                            text = "সক্রিয়",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
