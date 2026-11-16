package com.mosqueseba.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mosqueseba.app.data.model.IslamicCalendarEngine
import com.mosqueseba.app.data.model.IslamicEvent
import com.mosqueseba.app.data.model.IslamicEventCategory
import com.mosqueseba.app.data.model.IslamicEventCountdown
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.AiEventGuidanceUiState
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel

/**
 * Islamic Event Dashboard Banner displaying the next major upcoming/ongoing Islamic event.
 */
@Composable
fun IslamicEventDashboardBanner(
    countdown: IslamicEventCountdown,
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val event = countdown.event

    val badgeBg = when {
        countdown.isToday -> MosqueEmeraldPrimary
        countdown.isOngoing -> MosqueGoldDark
        countdown.isTomorrow -> MosqueWarningAmber
        else -> if (isDark) MosqueNightSurfaceVariant else MosqueGoldSoft
    }
    val badgeTextColor = when {
        countdown.isToday || countdown.isOngoing || countdown.isTomorrow -> Color.White
        else -> if (isDark) MosqueGoldLight else MosqueGoldDark
    }

    MehrabArchCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenCalendar() },
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = if (isDark) MosqueNightBorder else MosqueCardBorderGold,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Category Pill & Countdown Badge
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
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isDark) MosqueNightSurfaceVariant else MosqueGoldLight.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = if (isDark) MosqueGoldLight else MosqueGoldDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "স্মার্ট ইসলামিক ক্যালেন্ডার",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                    )
                }

                // Countdown Status Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = badgeBg,
                    border = BorderStroke(
                        1.dp,
                        if (isDark) MosqueNightBorder else MosqueGoldLight
                    )
                ) {
                    Text(
                        text = countdown.statusTextBn,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Event Title & Hijri Date Range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.titleBn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${event.dateRangeBn} • ${event.category.labelBn}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) MosqueGoldLight else MosqueGoldDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Short Summary
            Text(
                text = event.summaryBn,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDark) MosqueNightTextMuted else MosqueTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Action Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "আমল, ফজিলত ও এআই বয়ান রূপরেখা",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) MosqueEmeraldLight else MosqueEmeraldPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "ক্যালেন্ডার খুলুন",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) MosqueGoldLight else MosqueGoldDark,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = if (isDark) MosqueGoldLight else MosqueGoldDark,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Full-featured interactive Islamic Calendar and AI Event Guidance Hub Dialog.
 */
@Composable
fun IslamicCalendarHubDialog(
    viewModel: MosqueViewModel,
    onDismiss: () -> Unit,
    onDraftNotice: (title: String, content: String) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val hijriDayOffset by viewModel.hijriDayOffset.collectAsState()
    val aiGuidanceState by viewModel.aiEventGuidanceState.collectAsState()

    var selectedCategory by remember { mutableStateOf<IslamicEventCategory?>(null) }
    var detailEvent by remember { mutableStateOf<IslamicEvent?>(null) }

    val allCountdowns = remember(hijriDayOffset) {
        IslamicCalendarEngine.getEventCountdowns(hijriDayOffset)
    }

    val filteredEvents = remember(allCountdowns, selectedCategory) {
        if (selectedCategory == null) {
            allCountdowns
        } else {
            allCountdowns.filter { it.event.category == selectedCategory }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MehrabArchCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .fillMaxHeight(0.92f)
                .padding(horizontal = 14.dp, vertical = 16.dp),
            backgroundColor = if (isDark) MosqueNightSurface else MosqueBgLight,
            borderColor = if (isDark) MosqueNightBorder else MosqueCardBorderGold,
            elevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MosqueEmeraldDarkGradient)
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MosqueGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = MosqueGoldLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "স্মার্ট ইসলামিক ক্যালেন্ডার ও ইভেন্ট হাব",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "হিজরি দিনপঞ্জি, মহিমান্বিত দিবস ও এআই বয়ান সহায়িকা",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MosqueEmeraldSoft
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "বন্ধ করুন",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Current Hijri Date & Moon Sighting Adjustment Bar
                    CurrentHijriDateAdjustmentCard(
                        hijriOffset = hijriDayOffset,
                        onOffsetChanged = { newOffset ->
                            viewModel.setHijriDayOffset(newOffset)
                            MosqueToastController.info(
                                "চাঁদ দেখা সমন্বয় হালনাগাদ করা হয়েছে (${if (newOffset > 0) "+$newOffset" else newOffset.toString()} দিন)।",
                                "হিজরি ক্যালেন্ডার"
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filter Chips
                    CategoryFilterChipsRow(
                        selectedCategory = selectedCategory,
                        onSelectCategory = { selectedCategory = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Event List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredEvents, key = { it.event.id }) { item ->
                            IslamicEventListItem(
                                countdown = item,
                                onClick = { detailEvent = item.event }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog for Selected Event
    detailEvent?.let { event ->
        IslamicEventDetailDialog(
            event = event,
            aiGuidanceState = aiGuidanceState,
            onDismiss = {
                detailEvent = null
                viewModel.clearEventAiGuidance()
            },
            onGenerateAiGuidance = {
                viewModel.generateEventAiGuidance(event)
            },
            onDraftNotice = { title, content ->
                detailEvent = null
                viewModel.clearEventAiGuidance()
                onDismiss()
                onDraftNotice(title, content)
            }
        )
    }
}

/**
 * Card showing current formatted Hijri Date and Moon Sighting adjuster chips.
 */
@Composable
private fun CurrentHijriDateAdjustmentCard(
    hijriOffset: Int,
    onOffsetChanged: (Int) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val currentHijriDateStr = remember(hijriOffset) {
        IslamicCalendarEngine.getFormattedCurrentHijriDate(hijriOffset)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueGoldSoft
        ),
        border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderGold)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Brightness4,
                        contentDescription = null,
                        tint = if (isDark) MosqueGoldLight else MosqueGoldDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "আজকের হিজরি তারিখ:",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                    )
                }

                Text(
                    text = currentHijriDateStr,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) MosqueGoldLight else MosqueGoldDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Adjustment row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "চাঁদ দেখা সমন্বয়:",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) MosqueNightTextMuted else MosqueTextMuted
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(-1, 0, 1).forEach { offset ->
                        val isSelected = hijriOffset == offset
                        val label = when (offset) {
                            -1 -> "-১ দিন"
                            1 -> "+১ দিন"
                            else -> "স্বাভাবিক"
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) {
                                if (isDark) MosqueGoldDark else MosqueEmeraldPrimary
                            } else {
                                if (isDark) MosqueNightSurface else Color.White
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color.Transparent else if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
                            ),
                            modifier = Modifier.clickable { onOffsetChanged(offset) }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else if (isDark) MosqueNightTextSecondary else MosqueTextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Filter chips for categories: All, Obligatory, Eid, Blessed Nights, Sunnah Fasts, Historic.
 */
@Composable
private fun CategoryFilterChipsRow(
    selectedCategory: IslamicEventCategory?,
    onSelectCategory: (IslamicEventCategory?) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            FilterChipItem(
                label = "সকল দিবস",
                isSelected = selectedCategory == null,
                onClick = { onSelectCategory(null) }
            )
        }
        items(IslamicEventCategory.values()) { category ->
            FilterChipItem(
                label = category.labelBn,
                isSelected = selectedCategory == category,
                onClick = { onSelectCategory(category) }
            )
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) {
            MosqueEmeraldPrimary
        } else {
            if (isDark) MosqueNightSurfaceVariant else Color.White
        },
        border = BorderStroke(
            1.dp,
            if (isSelected) MosqueEmeraldLight else if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else if (isDark) MosqueNightTextSecondary else MosqueTextSecondary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * Single event list item row.
 */
@Composable
private fun IslamicEventListItem(
    countdown: IslamicEventCountdown,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val event = countdown.event

    val badgeBg = when {
        countdown.isToday -> MosqueEmeraldPrimary
        countdown.isOngoing -> MosqueGoldDark
        countdown.isTomorrow -> MosqueWarningAmber
        else -> if (isDark) MosqueNightSurfaceVariant else MosqueGoldSoft
    }
    val badgeTextColor = when {
        countdown.isToday || countdown.isOngoing || countdown.isTomorrow -> Color.White
        else -> if (isDark) MosqueGoldLight else MosqueGoldDark
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MosqueNightSurfaceVariant else Color.White
        ),
        border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top Row: Title & Countdown Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.titleBn,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${event.dateRangeBn} • ${event.category.labelBn}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) MosqueGoldLight else MosqueGoldDark
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeBg,
                    border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueGoldLight)
                ) {
                    Text(
                        text = countdown.statusTextBn,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = event.summaryBn,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDark) MosqueNightTextMuted else MosqueTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "আমল ও খুতবা রূপরেখা দেখুন",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) MosqueEmeraldLight else MosqueEmeraldPrimary,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = if (isDark) MosqueEmeraldLight else MosqueEmeraldPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Detailed Event Dialog with 4 tabs: Virtues, Sunnah Amals, Khutbah Outline, AI Guidance.
 */
@Composable
private fun IslamicEventDetailDialog(
    event: IslamicEvent,
    aiGuidanceState: AiEventGuidanceUiState,
    onDismiss: () -> Unit,
    onGenerateAiGuidance: () -> Unit,
    onDraftNotice: (title: String, content: String) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("তাৎপর্য ও ফজিলত", "সুন্নাত আমল", "খুতবা রূপরেখা", "🤖 এআই গাইড")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MehrabArchCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .fillMaxHeight(0.88f)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            backgroundColor = if (isDark) MosqueNightSurface else MosqueBgLight,
            borderColor = if (isDark) MosqueNightBorder else MosqueCardBorderGold,
            elevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MosqueEmeraldDarkGradient)
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.titleBn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${event.dateRangeBn} • ${event.category.labelBn}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MosqueEmeraldSoft
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "বন্ধ করুন",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Tab Selector
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = if (isDark) MosqueNightSurfaceVariant else Color.White,
                    contentColor = if (isDark) MosqueGoldLight else MosqueEmeraldPrimary
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }

                // Tab Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        0 -> {
                            // Virtues / Significance
                            Text(
                                text = "কুরআন ও হাদিসের আলোকে তাৎপর্য ও মর্যাদা",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) MosqueGoldLight else MosqueGoldDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = event.defaultSignificanceBn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp
                            )
                        }
                        1 -> {
                            // Sunnah & Nafl Amals
                            Text(
                                text = "এই দিবসে করণীয় বিশেষ নফল আমলসমূহ",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) MosqueGoldLight else MosqueGoldDark
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            event.defaultAmalsBn.forEachIndexed { idx, amal ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MosqueSuccessGreen,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = amal,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                        2 -> {
                            // Khutbah Outline
                            Text(
                                text = "ইমাম ও খতিবদের জন্য খুতবা ও বয়ান রূপরেখা",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) MosqueGoldLight else MosqueGoldDark
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            event.defaultKhutbahPoints.forEach { point ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = null,
                                            tint = if (isDark) MosqueGoldLight else MosqueGoldDark,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = point,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }
                        }
                        3 -> {
                            // AI Guidance Section
                            Text(
                                text = "জেমিনি এআই চালিত পূর্ণাঙ্গ বয়ান ও দিকনির্দেশনা",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) MosqueGoldLight else MosqueGoldDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "কুরআন ও হাদিসের আলোকে খতিব ও মুসল্লিদের জন্য বিস্তারিত পয়েন্টভিত্তিক গাইড।",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) MosqueNightTextMuted else MosqueTextMuted
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            when (aiGuidanceState) {
                                is AiEventGuidanceUiState.Idle -> {
                                    Button(
                                        onClick = onGenerateAiGuidance,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MosqueEmeraldPrimary
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MosqueGoldLight,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "এআই বয়ান ও আমল গাইড তৈরি করুন",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                                is AiEventGuidanceUiState.Loading -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(
                                            color = MosqueEmeraldPrimary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "কুরআন ও সুন্নাহর আলোকে দিকনির্দেশনা প্রস্তুত হচ্ছে...",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                                        )
                                    }
                                }
                                is AiEventGuidanceUiState.Success -> {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "প্রস্তুতকৃত বয়ান ও দিকনির্দেশনা:",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MosqueSuccessGreen
                                            )
                                            TextButton(
                                                onClick = {
                                                    clipboardManager.setText(
                                                        AnnotatedString(aiGuidanceState.fullGuidanceText)
                                                    )
                                                    MosqueToastController.info(
                                                        "দিকনির্দেশনা ক্লিপবোর্ডে কপি করা হয়েছে।",
                                                        "কপি সম্পন্ন"
                                                    )
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "কপি করুন",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isDark) MosqueNightSurfaceVariant else Color.White
                                            ),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
                                            )
                                        ) {
                                            Text(
                                                text = aiGuidanceState.fullGuidanceText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                lineHeight = 20.sp,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }
                                }
                                is AiEventGuidanceUiState.Error -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = aiGuidanceState.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MosqueUrgentRed,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = onGenerateAiGuidance,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MosqueEmeraldPrimary
                                            )
                                        ) {
                                            Text("পুনরায় চেষ্টা করুন")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("বন্ধ করুন")
                    }

                    Button(
                        onClick = {
                            onDraftNotice(event.defaultNoticeTitle, event.defaultNoticeContent)
                        },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MosqueEmeraldPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "নোটিশে খসড়া পাঠান",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
