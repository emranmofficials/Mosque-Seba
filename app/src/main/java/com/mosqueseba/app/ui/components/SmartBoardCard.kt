package com.mosqueseba.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.data.local.entity.NoticeEntity
import com.mosqueseba.app.data.local.entity.SmartBoardQuoteEntity
import com.mosqueseba.app.data.model.*
import com.mosqueseba.app.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

private data class SmartBoardSlide(
    val categoryBadge: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badgeText: String? = null,
    val actionText: String? = "বিস্তারিত দেখতে ট্যাপ করুন →",
    val iconTint: Color = Color.Unspecified,
    val iconBgColor: Color = Color.Unspecified,
    val badgeBgColor: Color = Color.Unspecified,
    val badgeTextColor: Color = Color.Unspecified,
    val onClick: (() -> Unit)? = null
)

@Composable
fun SmartBoardCard(
    quotes: List<SmartBoardQuoteEntity> = emptyList(),
    notices: List<NoticeEntity> = emptyList(),
    hijriDayOffset: Int = 0,
    includeDateAndPrayerSlides: Boolean = true,
    onOpenIslamicCalendar: () -> Unit = {},
    onOpenRamadanHub: () -> Unit = {},
    onOpenPrayerSchedule: () -> Unit = {},
    onOpenNotices: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var pageIndex by remember { mutableIntStateOf(0) }
    var currentTimeStr by remember { mutableStateOf("") }
    val (todayPrayers, nextPrayerSummary) = computeTodayPrayerSchedule()
    val nextAzanTime = todayPrayers.firstOrNull { it.isNext }?.azanTime ?: nextPrayerSummary.iqamahTimeBn

    val isDark = LocalIsDarkTheme.current
    val cardBg = if (isDark) MosqueNightSurfaceVariant else MaterialTheme.colorScheme.surface
    val accentEmerald = if (isDark) MosqueGoldLight else MosqueEmeraldPrimary
    val titleColor = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary
    val subtitleColor = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary

    // 1. Upcoming Islamic Event Calculation
    val nextEventCountdown = remember(hijriDayOffset) {
        IslamicCalendarEngine.getNextUpcomingMajorEvent(hijriDayOffset)
    }

    // 2. Ramadan Status Calculation (auto-activated in Ramadan season)
    val ramadanStatus = remember(hijriDayOffset) {
        RamadanEngine.getTodayStatus(RamadanEngine.bangladeshDistricts.first(), hijriDayOffset)
    }

    // 3. Pinned / Latest Published Notice
    val activeNotice = remember(notices) {
        notices.filter { it.status == "published" }
            .let { list -> list.firstOrNull { it.isPinned } ?: list.firstOrNull() }
    }

    // 4. Pinned / Active Quotes
    val pinnedQuotes = remember(quotes) {
        quotes.filter { it.isPinned }.ifEmpty { quotes.take(2) }
    }

    // Dynamic slides assembly
    val slides = remember(
        nextEventCountdown,
        ramadanStatus,
        nextPrayerSummary,
        nextAzanTime,
        activeNotice,
        pinnedQuotes,
        currentTimeStr,
        includeDateAndPrayerSlides,
        isDark
    ) {
        buildList {
            // Slide 1: Islamic Calendar & Major Event
            add(
                SmartBoardSlide(
                    categoryBadge = nextEventCountdown.event.category.labelBn,
                    title = nextEventCountdown.event.titleBn,
                    subtitle = "${nextEventCountdown.event.dateRangeBn} • ${nextEventCountdown.event.summaryBn}",
                    badgeText = nextEventCountdown.statusTextBn,
                    icon = Icons.Default.Event,
                    iconTint = if (nextEventCountdown.isToday || nextEventCountdown.isOngoing) MosqueUrgentRed else if (isDark) MosqueGoldLight else MosqueEmeraldPrimary,
                    iconBgColor = (if (nextEventCountdown.isToday || nextEventCountdown.isOngoing) MosqueUrgentRed else if (isDark) MosqueGoldLight else MosqueEmeraldPrimary).copy(alpha = if (isDark) 0.18f else 0.09f),
                    badgeBgColor = (if (nextEventCountdown.isToday || nextEventCountdown.isOngoing) MosqueUrgentRed else if (isDark) MosqueGoldDark else MosqueEmeraldPrimary).copy(alpha = if (isDark) 0.22f else 0.12f),
                    badgeTextColor = if (nextEventCountdown.isToday || nextEventCountdown.isOngoing) MosqueUrgentRed else if (isDark) MosqueGoldLight else MosqueEmeraldPrimary,
                    actionText = "ক্যালেন্ডার ও আমল দেখতে ট্যাপ করুন →",
                    onClick = onOpenIslamicCalendar
                )
            )

            // Slide 2: Holy Ramadan (Auto-activated when Ramadan is active or approaching in 1..7 days)
            if (ramadanStatus.isRamadanNow) {
                add(
                    SmartBoardSlide(
                        categoryBadge = "মাহে রমজান",
                        title = "পবিত্র মাহে রমজান • ${toBengaliDigits(ramadanStatus.currentRamadanDay.toString())} রমজান",
                        subtitle = "সেহরি শেষ: ${toBengaliDigits(ramadanStatus.todaySehri)} • ইফতার: ${toBengaliDigits(ramadanStatus.todayIftar)} (${ramadanStatus.ashra.titleBn})",
                        badgeText = "${ramadanStatus.nextMilestoneBn}: ${ramadanStatus.countdownTextBn}",
                        icon = Icons.Default.DarkMode,
                        iconTint = if (isDark) MosqueGoldLight else MosqueGoldDeep,
                        iconBgColor = (if (isDark) MosqueGoldLight else MosqueGoldDeep).copy(alpha = if (isDark) 0.18f else 0.1f),
                        badgeBgColor = (if (isDark) MosqueGoldDark else MosqueGold).copy(alpha = if (isDark) 0.24f else 0.14f),
                        badgeTextColor = if (isDark) MosqueGoldLight else MosqueGoldDark,
                        actionText = "সেহরি ও ইফতারের পূর্ণাঙ্গ সূচি দেখুন →",
                        onClick = onOpenRamadanHub
                    )
                )
            } else if (ramadanStatus.daysUntilRamadan in 1..7) {
                add(
                    SmartBoardSlide(
                        categoryBadge = "আসন্ন মাহে রমজান",
                        title = "আসন্ন পবিত্র মাহে রমজানুল মুবারক",
                        subtitle = "আর মাত্র ${toBengaliDigits(ramadanStatus.daysUntilRamadan.toString())} দিন বাকি • সেহরি ও ইফতারের পূর্ণাঙ্গ ক্যালেন্ডার দেখুন",
                        badgeText = "আর ${toBengaliDigits(ramadanStatus.daysUntilRamadan.toString())} দিন",
                        icon = Icons.Default.DarkMode,
                        iconTint = if (isDark) MosqueGoldLight else MosqueGoldDeep,
                        iconBgColor = (if (isDark) MosqueGoldLight else MosqueGoldDeep).copy(alpha = if (isDark) 0.18f else 0.1f),
                        badgeBgColor = (if (isDark) MosqueGoldDark else MosqueGold).copy(alpha = if (isDark) 0.24f else 0.14f),
                        badgeTextColor = if (isDark) MosqueGoldLight else MosqueGoldDark,
                        actionText = "রমজান ক্যালেন্ডার ও প্রস্তুতি →",
                        onClick = onOpenRamadanHub
                    )
                )
            }

            // Slide 3: Prayer & Jamat Schedule
            if (includeDateAndPrayerSlides) {
                add(
                    SmartBoardSlide(
                        categoryBadge = "নামাজ ও জামাত",
                        title = "পরবর্তী জামাত: ${nextPrayerSummary.nameBn} (${nextPrayerSummary.iqamahTimeBn})",
                        subtitle = "আজান: $nextAzanTime • জামাত বাকি ${nextPrayerSummary.remainingTextBn} • ${formatCurrentHijriDateBn()}",
                        badgeText = "বাকি ${nextPrayerSummary.remainingTextBn}",
                        icon = Icons.Default.Schedule,
                        iconTint = if (isDark) MosqueEmeraldLight else MosqueEmeraldPrimary,
                        iconBgColor = (if (isDark) MosqueEmeraldLight else MosqueEmeraldPrimary).copy(alpha = if (isDark) 0.18f else 0.09f),
                        badgeBgColor = (if (isDark) MosqueEmeraldLight else MosqueEmeraldPrimary).copy(alpha = if (isDark) 0.22f else 0.12f),
                        badgeTextColor = if (isDark) MosqueEmeraldLight else MosqueEmeraldPrimary,
                        actionText = "৫ ওয়াক্ত নামাজের পূর্ণ সূচি →",
                        onClick = onOpenPrayerSchedule
                    )
                )
            }

            // Slide 4: Mosque Announcement / Notice (if available)
            if (activeNotice != null) {
                val isUrgent = activeNotice.priority.equals("urgent", ignoreCase = true)
                add(
                    SmartBoardSlide(
                        categoryBadge = if (isUrgent) "জরুরি নোটিশ" else "মসজিদ নোটিশ",
                        title = activeNotice.title,
                        subtitle = activeNotice.content.replace("\n", " ").trim(),
                        badgeText = if (activeNotice.isPinned) "পিনযুক্ত" else if (isUrgent) "জরুরি" else "ঘোষণা",
                        icon = Icons.Default.Campaign,
                        iconTint = if (isUrgent) MosqueUrgentRed else MosqueInfoBlue,
                        iconBgColor = (if (isUrgent) MosqueUrgentRed else MosqueInfoBlue).copy(alpha = if (isDark) 0.18f else 0.09f),
                        badgeBgColor = (if (isUrgent) MosqueUrgentRed else MosqueInfoBlue).copy(alpha = if (isDark) 0.22f else 0.12f),
                        badgeTextColor = if (isUrgent) MosqueUrgentRed else MosqueInfoBlue,
                        actionText = "সকল নোটিশ দেখতে ট্যাপ করুন →",
                        onClick = onOpenNotices
                    )
                )
            }

            // Slide 5: Inspirational Hadith / Wall Quote
            if (pinnedQuotes.isNotEmpty()) {
                pinnedQuotes.forEach { q ->
                    add(
                        SmartBoardSlide(
                            categoryBadge = q.category.ifBlank { "দেয়াল উক্তি" },
                            title = q.title,
                            subtitle = q.message,
                            badgeText = "নসিহত",
                            icon = Icons.Default.FormatQuote,
                            iconTint = if (isDark) MosqueGoldLight else MosqueGoldDeep,
                            iconBgColor = (if (isDark) MosqueGoldLight else MosqueGoldDeep).copy(alpha = if (isDark) 0.18f else 0.09f),
                            badgeBgColor = (if (isDark) MosqueGoldLight else MosqueGoldDeep).copy(alpha = if (isDark) 0.22f else 0.12f),
                            badgeTextColor = if (isDark) MosqueGoldLight else MosqueGoldDark,
                            actionText = "দৈনিক নসিহত ও স্মরণীয় বাণী",
                            onClick = null
                        )
                    )
                }
            } else {
                add(
                    SmartBoardSlide(
                        categoryBadge = "হাদিস ও নসিহত",
                        title = "নিয়মিত জামাতে নামাজ আদায়ের ফজিলত",
                        subtitle = "\"যে ব্যক্তি আল্লাহর উদ্দেশ্যে মসজিদ নির্মাণ করে, আল্লাহ তার জন্য জান্নাতে অনুরূপ একটি গৃহ নির্মাণ করেন।\" (সহিহ বুখারি)",
                        badgeText = "নসিহত",
                        icon = Icons.Default.FormatQuote,
                        iconTint = if (isDark) MosqueGoldLight else MosqueGoldDeep,
                        iconBgColor = (if (isDark) MosqueGoldLight else MosqueGoldDeep).copy(alpha = if (isDark) 0.18f else 0.09f),
                        badgeBgColor = (if (isDark) MosqueGoldLight else MosqueGoldDeep).copy(alpha = if (isDark) 0.22f else 0.12f),
                        badgeTextColor = if (isDark) MosqueGoldLight else MosqueGoldDark,
                        actionText = "দৈনিক নসিহত ও স্মরণীয় বাণী",
                        onClick = null
                    )
                )
            }
        }
    }

    // Auto-rotation every 6 seconds and live clock update
    LaunchedEffect(slides.size) {
        while (true) {
            val now = Date()
            currentTimeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now)
            delay(6000)
            if (slides.isNotEmpty()) {
                pageIndex = (pageIndex + 1) % slides.size
            }
        }
    }

    val safeIndex = if (slides.isEmpty()) 0 else pageIndex % slides.size
    val currentSlide = slides[safeIndex]

    // Pulsing live indicator dot
    val infiniteTransition = rememberInfiniteTransition(label = "LivePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LivePulseAlpha"
    )

    MehrabArchCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = currentSlide.onClick != null) {
                currentSlide.onClick?.invoke()
            },
        backgroundColor = cardBg,
        borderColor = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle,
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Bar: Live Status + Hijri Date + Slide Counter + Prev/Next Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MosqueSuccessGreen.copy(alpha = pulseAlpha))
                    )
                    Text(
                        text = "স্মার্ট ডিজিটাল বোর্ড",
                        style = MaterialTheme.typography.labelSmall,
                        color = titleColor,
                        fontWeight = FontWeight.Bold
                    )
                    if (currentTimeStr.isNotBlank()) {
                        Text(
                            text = "• ${toBengaliDigits(currentTimeStr)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = subtitleColor
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (slides.size > 1) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) MosqueNightSurface else accentEmerald.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = "${toBengaliDigits((safeIndex + 1).toString())}/${toBengaliDigits(slides.size.toString())}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accentEmerald,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (slides.isNotEmpty()) {
                                    pageIndex = (pageIndex - 1 + slides.size) % slides.size
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "পূর্ববর্তী",
                                tint = subtitleColor,
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (slides.isNotEmpty()) {
                                    pageIndex = (pageIndex + 1) % slides.size
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "পরবর্তী",
                                tint = subtitleColor,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body: Animated slide with Icon, Badges, Title, Subtitle, and Interactive Action
            AnimatedContent(
                targetState = safeIndex,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300)) + slideInHorizontally { it / 4 })
                        .togetherWith(fadeOut(animationSpec = tween(200)) + slideOutHorizontally { -it / 4 })
                },
                label = "SmartBoardRotation"
            ) { idx ->
                val slide = slides.getOrElse(idx) { currentSlide }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(slide.iconBgColor)
                            .border(1.dp, slide.iconTint.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = slide.icon,
                            contentDescription = null,
                            tint = slide.iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = slide.badgeBgColor
                            ) {
                                Text(
                                    text = slide.categoryBadge,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                    fontWeight = FontWeight.SemiBold,
                                    color = slide.badgeTextColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            if (!slide.badgeText.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isDark) MosqueNightSurface else MosqueEmeraldSoft
                                ) {
                                    Text(
                                        text = slide.badgeText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) MosqueGoldLight else MosqueEmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = slide.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = slide.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = subtitleColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 17.sp
                        )

                        if (!slide.actionText.isNullOrBlank() && slide.onClick != null) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = slide.actionText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Bold,
                                color = accentEmerald
                            )
                        }
                    }
                }
            }

            // Indicator Dots / Bars at the bottom
            if (slides.size > 1) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in slides.indices) {
                        val isCurrent = i == safeIndex
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .height(3.5.dp)
                                .width(if (isCurrent) 16.dp else 5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (isCurrent) accentEmerald else accentEmerald.copy(alpha = 0.2f)
                                )
                        )
                    }
                }
            }
        }
    }
}
