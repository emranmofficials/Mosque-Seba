package com.mosqueseba.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mosqueseba.app.data.local.entity.SmartBoardQuoteEntity
import com.mosqueseba.app.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

private data class SmartSlide(
    val category: String,
    val title: String,
    val subtitle: String
)

@Composable
fun SmartBoardCard(
    quotes: List<SmartBoardQuoteEntity> = emptyList(),
    includeDateAndPrayerSlides: Boolean = true
) {
    var pageIndex by remember { mutableIntStateOf(0) }
    var currentTimeStr by remember { mutableStateOf("") }
    val (todayPrayers, nextPrayerSummary) = computeTodayPrayerSchedule()
    val nextAzanTime = todayPrayers.firstOrNull { it.isNext }?.azanTime ?: nextPrayerSummary.iqamahTimeBn

    val pinnedQuotes = remember(quotes) {
        quotes.filter { it.isPinned }.ifEmpty { quotes.take(2) }
    }

    val slides = remember(pinnedQuotes, currentTimeStr, nextPrayerSummary, nextAzanTime, includeDateAndPrayerSlides) {
        buildList {
            if (includeDateAndPrayerSlides) {
                add(
                    SmartSlide(
                        category = "DAY",
                        title = formatCurrentHijriDateBn(),
                        subtitle = "${formatCurrentGregorianDateBn()} • সময়: ${toBengaliDigits(currentTimeStr)}"
                    )
                )
                add(
                    SmartSlide(
                        category = "PRAYER",
                        title = "পরবর্তী জামাত: ${nextPrayerSummary.nameBn} (${nextPrayerSummary.iqamahTimeBn})",
                        subtitle = "আজান: $nextAzanTime • বাকি ${nextPrayerSummary.remainingTextBn}"
                    )
                )
            }
            if (pinnedQuotes.isNotEmpty()) {
                pinnedQuotes.forEach { q ->
                    add(
                        SmartSlide(
                            category = q.category,
                            title = q.title,
                            subtitle = q.message
                        )
                    )
                }
            } else {
                add(
                    SmartSlide(
                        category = "দেয়াল উক্তি",
                        title = "নিয়মিত জামাতে নামাজ আদায়ের ফজিলত",
                        subtitle = "\"যে ব্যক্তি আল্লাহর উদ্দেশ্যে মসজিদ নির্মাণ করে, আল্লাহ তার জন্য জান্নাতে গৃহ নির্মাণ করেন।\""
                    )
                )
            }
        }
    }

    // Update time and rotate pages every 5 seconds
    LaunchedEffect(slides.size) {
        while (true) {
            val now = Date()
            currentTimeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now)
            delay(5000)
            if (slides.isNotEmpty()) {
                pageIndex = (pageIndex + 1) % slides.size
            }
        }
    }

    val safeIndex = if (slides.isEmpty()) 0 else pageIndex % slides.size
    val currentSlide = slides[safeIndex]

    val isDark = LocalIsDarkTheme.current
    val cardBg = if (isDark) MosqueNightSurfaceVariant else MosqueGoldSoft
    val accentEmerald = if (isDark) MosqueGoldLight else MosqueEmeraldPrimary
    val titleColor = if (isDark) MosqueGoldLight else MosqueEmeraldDark

    MehrabArchCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = cardBg,
        borderColor = MosqueGold.copy(alpha = 0.5f),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentEmerald)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "স্মার্ট নোটিশ ও দেয়াল বোর্ড",
                        style = MaterialTheme.typography.labelSmall,
                        color = titleColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in slides.indices) {
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(if (i == safeIndex) 16.dp else 6.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (i == safeIndex) accentEmerald
                                    else accentEmerald.copy(alpha = 0.25f)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedContent(
                targetState = safeIndex,
                transitionSpec = {
                    fadeIn() + slideInVertically { it / 2 } togetherWith fadeOut() + slideOutVertically { -it / 2 }
                },
                label = "SmartBoardRotation"
            ) { idx ->
                val slide = slides.getOrElse(idx) { currentSlide }
                val icon = when (slide.category) {
                    "DAY" -> Icons.Default.Event
                    "PRAYER" -> Icons.Default.Schedule
                    "বিশেষ ধর্মীয় দিবস", "জুমার নির্দেশনা" -> Icons.Default.Campaign
                    else -> Icons.Default.FormatQuote
                }
                val iconTint = if (slide.category == "বিশেষ ধর্মীয় দিবস") MosqueUrgentRed else accentEmerald

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = slide.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (slide.category == "বিশেষ ধর্মীয় দিবস") MosqueUrgentRed else titleColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = slide.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
