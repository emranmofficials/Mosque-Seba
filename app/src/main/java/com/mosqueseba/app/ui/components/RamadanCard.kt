package com.mosqueseba.app.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mosqueseba.app.data.model.*
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel

private enum class RamadanTab(val titleBn: String, val icon: ImageVector) {
    TODAY("আজকের সূচি", Icons.Default.WbTwilight),
    CALENDAR("৩০ দিনের সূচি", Icons.Default.CalendarMonth),
    DUAS("গুরুত্বপূর্ণ দোয়া", Icons.Default.AutoStories),
    MASAIL("মাসায়েল ও ফিতরা", Icons.Default.MenuBook),
    NOTICE("মসজিদ নোটিশ", Icons.Default.Campaign)
}

/**
 * Modern Dashboard Card displaying today's Holy Ramadan status, Sehri & Iftar timetable,
 * live countdown badge, fasting progress bar, and fast access to Ramadan Hub.
 */
@Composable
fun RamadanDashboardCard(
    onOpenHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    var selectedDistrict by remember { mutableStateOf(RamadanEngine.bangladeshDistricts.first()) }
    val todayStatus = remember(selectedDistrict) {
        RamadanEngine.getTodayStatus(selectedDistrict)
    }

    Surface(
        onClick = onOpenHub,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.5.dp,
            if (isDark) MosqueNightBorder else MosqueGold.copy(alpha = 0.65f)
        ),
        shadowElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Title & Ashra Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(MosqueGoldLight, MosqueGold, MosqueGoldDeep)
                                )
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = MosqueEmeraldDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "পবিত্র মাহে রমজানুল মুবারক",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark
                        )
                        Text(
                            text = if (todayStatus.isRamadanNow) {
                                todayStatus.hijriDateFormatted
                            } else {
                                "আর মাত্র ${toBengaliDigits(todayStatus.daysUntilRamadan.toString())} দিন বাকি • প্রস্তুতি গ্রহণ করুন"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MosqueGold.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = todayStatus.ashra.titleBn,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MosqueGoldDeep,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Timetable Cards: Sehri & Iftar Side by Side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Sehri Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, MosqueEmeraldPrimary.copy(alpha = 0.35f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WbTwilight,
                                contentDescription = null,
                                tint = MosqueEmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "সেহরির শেষ সময়",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = todayStatus.todaySehri,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MosqueEmeraldPrimary
                        )
                    }
                }

                // Iftar Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) MosqueNightSurfaceVariant else MosqueGold.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = null,
                                tint = MosqueGoldDeep,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ইফতারের সময়",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = todayStatus.todayIftar,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MosqueGoldDeep
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Countdown & Progress Strip
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isDark) MosqueNightSurfaceVariant.copy(alpha = 0.6f) else MosqueBgLight,
                border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HourglassBottom,
                            contentDescription = null,
                            tint = MosqueGoldDeep,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${todayStatus.nextMilestoneBn}: ${todayStatus.countdownTextBn}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "পূর্ণাঙ্গ হাব",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MosqueEmeraldPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MosqueEmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Comprehensive Full Modal Dialog for the Holy Month of Ramadan.
 * Segmented into 5 tabs:
 * 1. আজকের সূচি (Today's Timetable & District Offset)
 * 2. ৩০ দিনের সূচি (30-day Calendar divided into 3 Ashras)
 * 3. গুরুত্বপূর্ণ দোয়া (8 Essential Ramadan Duas with Arabic, Translation & Copy)
 * 4. মাসায়েল ও ফিতরা (Fasting Masail & Interactive Fitrah Calculator)
 * 5. মসজিদ নোটিশ (1-Tap draft notice generation to Mosque Notice Board)
 */
@Composable
fun RamadanHubDialog(
    viewModel: MosqueViewModel,
    onDismiss: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val mosque by viewModel.mosque.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()

    var selectedTab by remember { mutableStateOf(RamadanTab.TODAY) }
    var selectedDistrict by remember { mutableStateOf(RamadanEngine.bangladeshDistricts.first()) }
    var showDistrictMenu by remember { mutableStateOf(false) }

    val todayStatus = remember(selectedDistrict) {
        RamadanEngine.getTodayStatus(selectedDistrict)
    }
    val full30Days = remember(selectedDistrict) {
        RamadanEngine.get30DaySchedule(selectedDistrict)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Green Sanctuary Header Banner
                Surface(
                    color = Color.Transparent,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MosqueEmeraldDarkGradient)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MosqueGold.copy(alpha = 0.25f))
                                        .border(1.dp, MosqueGold, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DarkMode,
                                        contentDescription = null,
                                        tint = MosqueGoldLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "মাহে রমজানুল মুবারক হাব",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "সেহরি-ইফতারের সূচি, মাসায়েল, দোয়া ও ফিতরা সহায়িকা",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MosqueGoldLight
                                    )
                                }
                            }

                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = Color.White)
                            }
                        }
                    }
                }

                // Segmented Tab Navigation Row
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
                    shadowElevation = 2.dp
                ) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        edgePadding = 8.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MosqueEmeraldPrimary,
                        indicator = { tabPositions ->
                            if (selectedTab.ordinal < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                                    color = MosqueEmeraldPrimary,
                                    height = 3.dp
                                )
                            }
                        }
                    ) {
                        RamadanTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTab = tab },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (isSelected) MosqueEmeraldPrimary else if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = tab.titleBn,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MosqueEmeraldPrimary else if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // Tab Contents
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (selectedTab) {
                        RamadanTab.TODAY -> TodayTimetableView(
                            todayStatus = todayStatus,
                            district = selectedDistrict,
                            districts = RamadanEngine.bangladeshDistricts,
                            onDistrictChange = { selectedDistrict = it },
                            onGoToNotice = { selectedTab = RamadanTab.NOTICE },
                            onGoToDuas = { selectedTab = RamadanTab.DUAS }
                        )

                        RamadanTab.CALENDAR -> ThirtyDayCalendarView(
                            scheduleList = full30Days,
                            todayDay = todayStatus.currentRamadanDay,
                            district = selectedDistrict
                        )

                        RamadanTab.DUAS -> RamadanDuasView(
                            duas = RamadanEngine.getEssentialDuas(),
                            onCopy = { text, title ->
                                clipboardManager.setText(AnnotatedString(text))
                                Toast.makeText(context, "$title কপি করা হয়েছে।", Toast.LENGTH_SHORT).show()
                            }
                        )

                        RamadanTab.MASAIL -> MasailAndFitrahView(
                            masailList = RamadanEngine.getFastingMasail(),
                            fitrahItems = RamadanEngine.getFitrahGuidelines()
                        )

                        RamadanTab.NOTICE -> RamadanNoticeGeneratorView(
                            viewModel = viewModel,
                            mosqueName = mosque?.nameBn ?: "বায়তুল জান্নাত জামে মসজিদ",
                            district = selectedDistrict,
                            canPublish = currentRole.canPublishNotices()
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// Sub-View 1: Today Timetable & District Switcher
// ============================================================
@Composable
private fun TodayTimetableView(
    todayStatus: TodayRamadanStatus,
    district: BangladeshDistrictOffset,
    districts: List<BangladeshDistrictOffset>,
    onDistrictChange: (BangladeshDistrictOffset) -> Unit,
    onGoToNotice: () -> Unit,
    onGoToDuas: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    var showDistrictMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // District Selector Bar
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MosqueEmeraldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "জেলা ও অঞ্চল ভিত্তিক সময়সূচি",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                            )
                            Text(
                                text = "${district.nameBn} (সমন্বয়: ${if (district.sehriOffsetMinutes >= 0) "+${district.sehriOffsetMinutes}" else "${district.sehriOffsetMinutes}"} মি.)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark
                            )
                        }
                    }

                    Box {
                        OutlinedButton(
                            onClick = { showDistrictMenu = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            border = BorderStroke(1.dp, MosqueEmeraldPrimary.copy(alpha = 0.5f))
                        ) {
                            Text("জেলা পরিবর্তন", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }

                        DropdownMenu(
                            expanded = showDistrictMenu,
                            onDismissRequest = { showDistrictMenu = false }
                        ) {
                            districts.forEach { dist ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${dist.nameBn} (${if (dist.sehriOffsetMinutes >= 0) "+${dist.sehriOffsetMinutes}" else "${dist.sehriOffsetMinutes}"} মি.)",
                                            fontWeight = if (dist.nameBn == district.nameBn) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onDistrictChange(dist)
                                        showDistrictMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Today Live Banner
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "আজকের পবিত্র দিন",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                            )
                            Text(
                                text = todayStatus.hijriDateFormatted,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MosqueGold.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, MosqueGold)
                        ) {
                            Text(
                                text = todayStatus.ashra.titleBn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MosqueGoldDeep,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Next Milestone Chip
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MosqueEmeraldSoft.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MosqueEmeraldPrimary.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = MosqueEmeraldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "পরবর্তী নির্ধারিত ক্ষণ: ${todayStatus.nextMilestoneBn}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                                )
                                Text(
                                    text = todayStatus.countdownTextBn,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Fasting Timeline Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MosqueEmeraldPrimary.copy(alpha = 0.35f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "সেহরির শেষ সময়",
                                    fontSize = 11.sp,
                                    color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = todayStatus.todaySehri,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueEmeraldPrimary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) MosqueNightSurfaceVariant else MosqueGold.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "ইফতারের সময়",
                                    fontSize = 11.sp,
                                    color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = todayStatus.todayIftar,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueGoldDeep
                                )
                            }
                        }
                    }
                }
            }
        }

        // Current Ashra Virtue Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = MosqueGoldDeep, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${todayStatus.ashra.titleBn} (${todayStatus.ashra.subtitleBn})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MosqueGoldDeep
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = todayStatus.ashra.descriptionBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                        border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "এই দশকের বিশেষ প্রার্থনা:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldPrimary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = todayStatus.ashra.duaArabic,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = todayStatus.ashra.duaTranslation,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Quick Navigation Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onGoToDuas,
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("রমজানের দোয়া", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onGoToNotice,
                    border = BorderStroke(1.dp, MosqueGoldDeep),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = MosqueGoldDeep, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("মসজিদ নোটিশ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MosqueGoldDeep)
                }
            }
        }
    }
}

// ============================================================
// Sub-View 2: 30 Days Full Calendar View Segmented by Ashra
// ============================================================
@Composable
private fun ThirtyDayCalendarView(
    scheduleList: List<RamadanDaySchedule>,
    todayDay: Int,
    district: BangladeshDistrictOffset
) {
    val isDark = LocalIsDarkTheme.current
    var selectedAshraFilter by remember { mutableStateOf<RamadanAshra?>(null) }

    val filteredList = remember(scheduleList, selectedAshraFilter) {
        if (selectedAshraFilter == null) scheduleList
        else scheduleList.filter { it.ashra == selectedAshraFilter }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Ashra Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedAshraFilter == null,
                    onClick = { selectedAshraFilter = null },
                    label = { Text("পূর্ণাঙ্গ ৩০ দিন", fontSize = 11.sp) }
                )
                RamadanAshra.entries.forEach { ashra ->
                    FilterChip(
                        selected = selectedAshraFilter == ashra,
                        onClick = { selectedAshraFilter = ashra },
                        label = { Text(ashra.titleBn, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Table Header
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MosqueEmeraldPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("রমজান", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                    Text("দশক", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text("সেহরি শেষ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1.4f))
                    Text("ইফতার সময়", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1.4f))
                }
            }
        }

        // 30 Days Items
        items(filteredList, key = { it.day }) { item ->
            val isToday = item.day == todayDay
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = when {
                    isToday -> if (isDark) MosqueGold.copy(alpha = 0.2f) else MosqueGold.copy(alpha = 0.15f)
                    item.day % 2 == 0 -> if (isDark) MosqueNightSurfaceVariant else MosqueBgLight
                    else -> MaterialTheme.colorScheme.surface
                },
                border = BorderStroke(
                    1.dp,
                    if (isToday) MosqueGold else if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1.2f), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.dateBn,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (isToday) MosqueGoldDeep else if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark
                        )
                        if (isToday) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MosqueGold
                            ) {
                                Text("আজ", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MosqueEmeraldDark, modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp))
                            }
                        }
                    }

                    Text(
                        text = item.ashra.titleBn.replace("দশক", "").trim(),
                        fontSize = 11.sp,
                        color = when (item.ashra) {
                            RamadanAshra.RAHMAH -> MosqueSuccessGreen
                            RamadanAshra.MAGHFIRAH -> MosqueInfoBlue
                            RamadanAshra.NAJAT -> MosqueUrgentRed
                        },
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = item.sehriEndTime.replace("ভোর ", ""),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MosqueEmeraldPrimary,
                        modifier = Modifier.weight(1.4f)
                    )

                    Text(
                        text = item.iftarTime.replace("সন্ধ্যা ", ""),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MosqueGoldDeep,
                        modifier = Modifier.weight(1.4f)
                    )
                }
            }
        }
    }
}

// ============================================================
// Sub-View 3: Authentic Ramadan Duas
// ============================================================
@Composable
private fun RamadanDuasView(
    duas: List<RamadanDuaItem>,
    onCopy: (text: String, title: String) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val categories = remember(duas) { listOf("সকল দোয়া") + duas.map { it.categoryBn }.distinct() }

    val filteredDuas = remember(duas, selectedCategory) {
        if (selectedCategory == null || selectedCategory == "সকল দোয়া") duas
        else duas.filter { it.categoryBn == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Category Filter
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { cat ->
                    val isSel = (selectedCategory == null && cat == "সকল দোয়া") || selectedCategory == cat
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedCategory = if (cat == "সকল দোয়া") null else cat },
                        label = { Text(cat, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Duas Cards
        items(filteredDuas, key = { it.id }) { dua ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Title and Category Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dua.titleBn,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MosqueEmeraldPrimary
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MosqueEmeraldSoft.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = dua.categoryBn,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Arabic Calligraphy Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                        border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = dua.arabicText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark,
                            lineHeight = 28.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Transliteration
                    Text(
                        text = "উচ্চারণ: ${dua.transliterationBn}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MosqueGoldDeep
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bengali Translation
                    Text(
                        text = "অর্থ: ${dua.translationBn}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Reference and Copy Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "সূত্র: ${dua.referenceBn}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                val textToCopy = buildString {
                                    appendLine(dua.titleBn)
                                    appendLine(dua.arabicText)
                                    appendLine("উচ্চারণ: ${dua.transliterationBn}")
                                    appendLine("অর্থ: ${dua.translationBn}")
                                    appendLine("সূত্র: ${dua.referenceBn}")
                                }
                                onCopy(textToCopy, dua.titleBn)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "কপি করুন", tint = MosqueEmeraldPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Sub-View 4: Masail & Interactive Fitrah Calculator
// ============================================================
@Composable
private fun MasailAndFitrahView(
    masailList: List<RamadanMasailCategory>,
    fitrahItems: List<FitrahItem>
) {
    val isDark = LocalIsDarkTheme.current
    var selectedSection by remember { mutableIntStateOf(0) } // 0 = Masail, 1 = Fitrah Calculator
    var familyMemberCount by remember { mutableIntStateOf(4) }
    var selectedFitrahIndex by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Toggle Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    label = { Text("রোজার জরুরি মাসায়েল", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    label = { Text("সদকাতুল ফিতরা হিসাব", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (selectedSection == 0) {
            // Masail Cards
            items(masailList) { masail ->
                val badgeColor = when (masail.iconType) {
                    "warning" -> MosqueUrgentRed
                    "info" -> MosqueInfoBlue
                    "check" -> MosqueSuccessGreen
                    else -> MosqueWarningAmber
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (masail.iconType) {
                                        "warning" -> Icons.Default.Warning
                                        "info" -> Icons.Default.Info
                                        "check" -> Icons.Default.CheckCircle
                                        else -> Icons.Default.HelpOutline
                                    },
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = masail.titleBn,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MosqueCardBorderSubtle)
                        Spacer(modifier = Modifier.height(8.dp))

                        masail.points.forEachIndexed { i, pt ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${toBengaliDigits((i + 1).toString())}. ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = badgeColor
                                )
                                Text(
                                    text = pt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Interactive Fitrah Calculator
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, MosqueGold),
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "পবিত্র সদকাতুল ফিতরা ক্যালকুলেটর",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MosqueGoldDeep
                        )
                        Text(
                            text = "ইসলামিক ফাউন্ডেশন নির্ধারিত ভোগ্যপণ্য ও পরিবারের সদস্য সংখ্যা অনুসারে স্বয়ংক্রিয় হিসাব",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Member count stepper
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "পরিবারের মোট সদস্য সংখ্যা:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (familyMemberCount > 1) familyMemberCount-- },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "কমান", tint = MosqueEmeraldPrimary)
                                }
                                Text(
                                    text = "${toBengaliDigits(familyMemberCount.toString())} জন",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MosqueEmeraldPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                IconButton(
                                    onClick = { familyMemberCount++ },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = "বাড়ান", tint = MosqueEmeraldPrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("ভোগ্যপণ্য নির্বাচন করুন (শরিয়াহ বিকল্প):", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(6.dp))

                        fitrahItems.forEachIndexed { idx, item ->
                            val isSel = selectedFitrahIndex == idx
                            Surface(
                                onClick = { selectedFitrahIndex = idx },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) MosqueEmeraldSoft else if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSel) MosqueEmeraldPrimary else if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${item.commodityBn} (${item.quantityBn})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark
                                        )
                                        Text(
                                            text = "জনপ্রতি প্রায় ৳${toBengaliDigits(item.approxPriceMin.toString())} – ৳${toBengaliDigits(item.approxPriceMax.toString())}",
                                            fontSize = 11.sp,
                                            color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                                        )
                                    }

                                    RadioButton(
                                        selected = isSel,
                                        onClick = { selectedFitrahIndex = idx },
                                        colors = RadioButtonDefaults.colors(selectedColor = MosqueEmeraldPrimary)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Total Calculation Result Box
                        val selectedItem = fitrahItems[selectedFitrahIndex]
                        val minTotal = selectedItem.approxPriceMin * familyMemberCount
                        val maxTotal = selectedItem.approxPriceMax * familyMemberCount

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MosqueGold.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, MosqueGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "আপনার পরিবারের (${toBengaliDigits(familyMemberCount.toString())} জন) জন্য আনুমানিক প্রদেয় ফিতরা:",
                                    fontSize = 12.sp,
                                    color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "৳${toBengaliDigits(minTotal.toString())} — ৳${toBengaliDigits(maxTotal.toString())}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MosqueGoldDeep
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "ঈদের নামাজের পূর্বেই হকদার মিসকিন ও দুস্থদের মাঝে ফিতরা প্রদান করা ওয়াজিব।",
                                    fontSize = 10.sp,
                                    color = MosqueTextMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Sub-View 5: Mosque Notice Generation & 1-Tap Publish
// ============================================================
@Composable
private fun RamadanNoticeGeneratorView(
    viewModel: MosqueViewModel,
    mosqueName: String,
    district: BangladeshDistrictOffset,
    canPublish: Boolean
) {
    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val (draftTitle, draftBody) = remember(mosqueName, district) {
        RamadanEngine.generateRamadanNoticeDraft(mosqueName, district)
    }

    var noticeTitle by remember { mutableStateOf(draftTitle) }
    var noticeBody by remember { mutableStateOf(draftBody) }
    var isPublished by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MosqueEmeraldPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = MosqueEmeraldPrimary)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "মসজিদ রমজান নোটিশ ড্রাফট",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldPrimary
                            )
                            Text(
                                text = "মুসল্লিদের অবগতির জন্য নোটিশ বোর্ডে প্রকাশ করুন অথবা অনুলিপি করুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = noticeTitle,
                        onValueChange = { noticeTitle = it },
                        label = { Text("নোটিশের শিরোনাম") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = noticeBody,
                        onValueChange = { noticeBody = it },
                        label = { Text("বিজ্ঞপ্তির বিবরণ") },
                        minLines = 8,
                        maxLines = 12,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val text = "$noticeTitle\n\n$noticeBody"
                                clipboardManager.setText(AnnotatedString(text))
                                Toast.makeText(context, "রমজান নোটিশ ক্লিপবোর্ডে কপি করা হয়েছে।", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("কপি করুন")
                        }

                        if (canPublish) {
                            Button(
                                onClick = {
                                    viewModel.addNotice(
                                        title = noticeTitle,
                                        content = noticeBody,
                                        author = mosqueName,
                                        priority = "HIGH"
                                    )
                                    isPublished = true
                                    Toast.makeText(context, "নোটিশ বোর্ডে সফলভাবে প্রকাশিত হয়েছে!", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isPublished,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isPublished) "প্রকাশিত হয়েছে ✓" else "নোটিশে প্রকাশ")
                            }
                        }
                    }
                }
            }
        }
    }
}
