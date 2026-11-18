package com.mosqueseba.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.ArabesqueStarMotif
import com.mosqueseba.app.ui.components.MehrabArchCard
import com.mosqueseba.app.ui.components.MosqueSearchBar
import com.mosqueseba.app.ui.components.StaggeredItemEntrance
import com.mosqueseba.app.ui.components.roleIcon
import com.mosqueseba.app.ui.components.toBengaliDigits
import com.mosqueseba.app.ui.theme.*

enum class OperationCategory(val labelBn: String) {
    ALL("সকল"),
    ADMIN("প্রশাসন ও দপ্তর"),
    FINANCE("আর্থিক ও তহবিল"),
    EDUCATION_STAFF("শিক্ষা ও স্টাফ"),
    WELFARE_EVENTS("কল্যাণ ও ইভেন্ট"),
    INFO("মসজিদ ও তথ্য")
}

data class OperationModuleEntry(
    val key: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val category: OperationCategory,
    val badgeText: String? = null,
    val badgeIsAlert: Boolean = false
)

data class MoreLiveCounts(
    val pendingCashboxCount: Int = 0,
    val totalCashboxCount: Int = 0,
    val pendingDocsCount: Int = 0,
    val totalDocsCount: Int = 0,
    val activeAdvisoryCount: Int = 0,
    val totalQuotesCount: Int = 0,
    val upcomingMeetingsCount: Int = 0,
    val totalWelfareCount: Int = 0,
    val totalStaffCount: Int = 0,
    val totalChandaReceipts: Int = 0,
    val totalStudentsCount: Int = 0,
    val totalRosterCount: Int = 0,
    val totalFoodScheduleCount: Int = 0,
    val upcomingEventsCount: Int = 0,
    val auditLogsCount: Int = 0,
    val hasActiveElection: Boolean = false
)

@Composable
fun MoreOperationsHub(
    currentRole: UserRole,
    liveCounts: MoreLiveCounts,
    onSelectModule: (String) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf(OperationCategory.ALL) }

    val allRoleModules = remember(currentRole, liveCounts) {
        buildRoleOperationModules(currentRole, liveCounts)
    }

    val availableCategories = remember(allRoleModules) {
        listOf(OperationCategory.ALL) + OperationCategory.entries
            .filter { it != OperationCategory.ALL && allRoleModules.any { mod -> mod.category == it } }
    }

    LaunchedEffect(availableCategories) {
        if (selectedCategory !in availableCategories) {
            selectedCategory = OperationCategory.ALL
        }
    }

    val filteredModules = remember(allRoleModules, searchQuery, selectedCategory) {
        allRoleModules.filter { mod ->
            val matchesQuery = searchQuery.isBlank() ||
                mod.title.contains(searchQuery, ignoreCase = true) ||
                mod.description.contains(searchQuery, ignoreCase = true) ||
                mod.category.labelBn.contains(searchQuery, ignoreCase = true)
            val matchesCat = selectedCategory == OperationCategory.ALL || mod.category == selectedCategory
            matchesQuery && matchesCat
        }
    }

    val pendingTotal = liveCounts.pendingCashboxCount +
        liveCounts.pendingDocsCount +
        liveCounts.activeAdvisoryCount

    val quickAccessModules = remember(allRoleModules) {
        allRoleModules.take(4)
    }

    val showQuickAccess = selectedCategory == OperationCategory.ALL &&
        searchQuery.isBlank() &&
        quickAccessModules.size >= 2

    val quickAccessKeys = remember(quickAccessModules, showQuickAccess) {
        if (showQuickAccess) quickAccessModules.map { it.key }.toSet() else emptySet()
    }

    val groupedModules = remember(filteredModules, quickAccessKeys) {
        filteredModules
            .filterNot { it.key in quickAccessKeys }
            .groupBy { it.category }
    }

    val isDark = LocalIsDarkTheme.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
        ) {
            // 1. Premium Role-Aware Control Header with Mehrab & Royal Emerald Shimmer
            item(key = "more_control_header") {
                MehrabArchCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (isDark) MosqueNightBorder else MosqueCardBorderGold.copy(alpha = 0.75f),
                    elevation = 2.dp,
                    showAccentTrim = false
                ) {
                    // Royal Emerald Shimmer Header Strip with Arabesque Star Motif
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RoyalEmeraldShimmerGradient)
                    ) {
                        // Decorative Arabesque Star Motif in top-right background
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 4.dp, end = 8.dp)
                        ) {
                            ArabesqueStarMotif(size = 72.dp, color = MosqueGoldLight.copy(alpha = 0.12f))
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                border = BorderStroke(1.5.dp, RoyalGoldShimmerGradient),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = currentRole.roleIcon(),
                                        contentDescription = null,
                                        tint = MosqueGoldLight,
                                        modifier = Modifier.size(23.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    ArabesqueStarMotif(size = 11.dp, color = MosqueGoldLight)
                                    Text(
                                        text = "আপনার দায়িত্বের ফিচার ও সেবা",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = currentRole.shortSummaryBn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.88f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Royal Gold accent divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(IslamicGoldTrimGradient)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ArabesqueStarMotif(size = 13.dp, color = MosqueGold)
                                Text(
                                    text = "পদভিত্তিক অপারেশন হাব",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isDark) MosqueEmeraldPrimary.copy(alpha = 0.22f) else MosqueEmeraldSoft,
                                border = BorderStroke(1.dp, if (isDark) MosqueGold.copy(alpha = 0.45f) else MosqueGold.copy(alpha = 0.65f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    ArabesqueStarMotif(size = 9.dp, color = MosqueGold)
                                    Text(
                                        text = currentRole.titleBn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) MosqueGoldLight else MosqueEmeraldDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MoreHeaderSummaryPill(
                                modifier = Modifier.weight(1f),
                                label = "সক্রিয় মডিউল",
                                value = "${toBengaliDigits(allRoleModules.size.toString())} টি",
                                accent = MaterialTheme.colorScheme.primary
                            )
                            MoreHeaderSummaryPill(
                                modifier = Modifier.weight(1f),
                                label = "সভা ও ইভেন্ট",
                                value = "${toBengaliDigits((liveCounts.upcomingMeetingsCount + liveCounts.upcomingEventsCount).toString())} টি",
                                accent = if (isDark) MosqueGreenAccent else MosqueSuccessGreen
                            )
                            MoreHeaderSummaryPill(
                                modifier = Modifier.weight(1f),
                                label = "অপেক্ষমাণ কাজ",
                                value = "${toBengaliDigits(pendingTotal.toString())} টি",
                                accent = if (pendingTotal > 0) MosqueWarningAmber else (if (isDark) MosqueGreenAccent else MosqueSuccessGreen),
                                isAlert = pendingTotal > 0
                            )
                        }
                    }
                }
            }

            // 2. Search & Category Filter Card
            item(key = "more_search_filters") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderGold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        MosqueSearchBar(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = "মডিউল খুঁজুন (যেমন: মাদরাসা, ক্যাশবক্স, রোস্টার)...",
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("more_search_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(availableCategories) { category ->
                                val count = if (category == OperationCategory.ALL) {
                                    allRoleModules.size
                                } else {
                                    allRoleModules.count { it.category == category }
                                }
                                val isSelected = selectedCategory == category
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = category },
                                    label = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (isSelected) {
                                                ArabesqueStarMotif(size = 8.dp, color = MosqueGoldLight)
                                            }
                                            Text(
                                                text = "${category.labelBn} (${toBengaliDigits(count.toString())})",
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (isDark) MosqueEmeraldPrimary else MosqueEmeraldPrimary,
                                        selectedLabelColor = Color.White,
                                        containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                                        labelColor = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle,
                                        selectedBorderColor = if (isDark) MosqueGoldLight else MosqueGold
                                    ),
                                    modifier = Modifier.testTag("more_filter_${category.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }

            // 3. Quick Access Grid + Categorized Module Cards
            if (filteredModules.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "কোনো মডিউল পাওয়া যায়নি",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "অনুসন্ধান বা ক্যাটাগরি ফিল্টার পরিবর্তন করে চেষ্টা করুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedCategory = OperationCategory.ALL
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("more_reset_filters_button")
                            ) {
                                Text("ফিল্টার মুছুন", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Quick Access 2x2 Grid when viewing ALL without search
                if (showQuickAccess) {
                    item {
                        MoreSectionHeader(
                            title = "দ্রুত প্রবেশ (কুইক অ্যাক্সেস)",
                            badgeText = "${toBengaliDigits(quickAccessModules.size.toString())} টি"
                        )
                    }
                    itemsIndexed(quickAccessModules.chunked(2)) { index, pair ->
                        StaggeredItemEntrance(index = index) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pair.forEach { mod ->
                                    QuickAccessGridCard(
                                        module = mod,
                                        modifier = Modifier.weight(1f),
                                        onClick = { onSelectModule(mod.key) }
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Categorized Sections
                groupedModules.forEach { (category, mods) ->
                    item(key = "header_${category.name}") {
                        Spacer(modifier = Modifier.height(4.dp))
                        MoreSectionHeader(
                            title = category.labelBn,
                            badgeText = "${toBengaliDigits(mods.size.toString())} টি"
                        )
                    }
                    itemsIndexed(mods, key = { _, it -> "${category.name}_${it.key}" }) { idx, mod ->
                        StaggeredItemEntrance(index = idx) {
                            OperationMenuItem(
                                title = mod.title,
                                description = mod.description,
                                icon = mod.icon,
                                iconColor = mod.iconColor,
                                badgeText = mod.badgeText,
                                badgeIsAlert = mod.badgeIsAlert,
                                onClick = { onSelectModule(mod.key) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreHeaderSummaryPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color,
    isAlert: Boolean = false
) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(11.dp),
        color = if (isAlert) {
            MosqueWarningAmber.copy(alpha = if (isDark) 0.18f else 0.10f)
        } else if (isDark) MosqueNightSurfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            0.9.dp,
            if (isAlert) MosqueWarningAmber.copy(alpha = 0.7f)
            else if (isDark) MosqueNightBorder else MosqueCardBorderGold.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MoreSectionHeader(
    title: String,
    badgeText: String
) {
    val isDark = LocalIsDarkTheme.current
    val primaryColor = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ArabesqueStarMotif(size = 12.dp, color = MosqueGold)
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = primaryColor
            )
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft,
            border = BorderStroke(0.7.dp, MosqueGold.copy(alpha = 0.45f))
        ) {
            Text(
                text = badgeText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun QuickAccessGridCard(
    module: OperationModuleEntry,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val hasAlert = module.badgeIsAlert
    Card(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 104.dp)
            .testTag("more_quick_${module.key}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (hasAlert) 1.5.dp else 1.dp,
            brush = if (hasAlert) RoyalGoldShimmerGradient else Brush.linearGradient(
                listOf(
                    if (isDark) MosqueNightBorder else MosqueCardBorderGold.copy(alpha = 0.5f),
                    if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
                )
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    module.iconColor.copy(alpha = 0.18f),
                                    module.iconColor.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(0.8.dp, module.iconColor.copy(alpha = 0.25f), RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = module.icon,
                        contentDescription = module.title,
                        tint = module.iconColor,
                        modifier = Modifier.size(21.dp)
                    )
                }

                module.badgeText?.let { badge ->
                    Surface(
                        shape = RoundedCornerShape(7.dp),
                        color = when {
                            module.badgeIsAlert -> MosqueWarningAmber.copy(alpha = 0.16f)
                            isDark -> MosqueNightSurfaceVariant
                            else -> MosqueEmeraldSoft
                        },
                        border = BorderStroke(
                            0.7.dp,
                            if (module.badgeIsAlert) MosqueWarningAmber.copy(alpha = 0.6f)
                            else MosqueGold.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            if (module.badgeIsAlert) {
                                ArabesqueStarMotif(size = 8.dp, color = MosqueWarningAmber)
                            }
                            Text(
                                text = badge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (module.badgeIsAlert) MosqueWarningAmber else primaryColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = module.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
        }
    }
}

private fun buildRoleOperationModules(
    currentRole: UserRole,
    counts: MoreLiveCounts
): List<OperationModuleEntry> {
    val cashboxBadge = if (counts.pendingCashboxCount > 0) {
        "${toBengaliDigits(counts.pendingCashboxCount.toString())} টি বাকি"
    } else if (counts.totalCashboxCount > 0) {
        "${toBengaliDigits(counts.totalCashboxCount.toString())} টি"
    } else null

    val docsBadge = if (counts.pendingDocsCount > 0) {
        "${toBengaliDigits(counts.pendingDocsCount.toString())} টি বাকি"
    } else if (counts.totalDocsCount > 0) {
        "${toBengaliDigits(counts.totalDocsCount.toString())} টি"
    } else null

    val advisoryBadge = if (counts.activeAdvisoryCount > 0) {
        "${toBengaliDigits(counts.activeAdvisoryCount.toString())} টি পর্যালোচনায়"
    } else null

    val meetingsBadge = if (counts.upcomingMeetingsCount > 0) {
        "${toBengaliDigits(counts.upcomingMeetingsCount.toString())} টি আসন্ন"
    } else null

    val eventsBadge = if (counts.upcomingEventsCount > 0) {
        "${toBengaliDigits(counts.upcomingEventsCount.toString())} টি আসন্ন"
    } else null

    val chandaBadge = if (counts.totalChandaReceipts > 0) {
        "${toBengaliDigits(counts.totalChandaReceipts.toString())} রসিদ"
    } else null

    val staffBadge = if (counts.totalStaffCount > 0) {
        "${toBengaliDigits(counts.totalStaffCount.toString())} জন"
    } else null

    val madrasaBadge = if (counts.totalStudentsCount > 0) {
        "${toBengaliDigits(counts.totalStudentsCount.toString())} ছাত্র"
    } else null

    val welfareBadge = if (counts.totalWelfareCount > 0) {
        "${toBengaliDigits(counts.totalWelfareCount.toString())} টি"
    } else null

    val rosterBadge = if (counts.totalRosterCount > 0) {
        "${toBengaliDigits(counts.totalRosterCount.toString())} সূচি"
    } else null

    val foodBadge = if (counts.totalFoodScheduleCount > 0) {
        "${toBengaliDigits(counts.totalFoodScheduleCount.toString())} টি"
    } else null

    val publicityBadge = if (counts.totalQuotesCount > 0) {
        "${toBengaliDigits(counts.totalQuotesCount.toString())} টি উক্তি"
    } else null

    val auditBadge = if (counts.auditLogsCount > 0) {
        "${toBengaliDigits(counts.auditLogsCount.toString())} লগ"
    } else null

    val electionBadge = if (counts.hasActiveElection) {
        "তফসিল চলমান"
    } else null

    val baseModules = when (currentRole) {
        UserRole.PRESIDENT -> listOf(
            OperationModuleEntry("meetings", "কমিটি সভা ও আর্থিক রিপোর্ট", "কমিটি সভার তারিখ ও সভায় মাসিক আর্থিক রিপোর্ট পেশ", Icons.Default.EventAvailable, MosqueGoldDeep, OperationCategory.ADMIN, meetingsBadge, counts.upcomingMeetingsCount > 0),
            OperationModuleEntry("election", "কমিটি নির্বাচন ও পুনর্গঠন", "তফসিল, প্রার্থী মনোনয়ন, ভোটার তালিকা ও ফলাফল গেজেট", Icons.Default.HowToVote, MosqueGoldDeep, OperationCategory.ADMIN, electionBadge, counts.hasActiveElection),
            OperationModuleEntry("cashbox", "দান বাক্স (ক্যাশবক্স) সাক্ষ্য", "দান বাক্স খোলার সময় উপস্থিত সাক্ষী নিশ্চিতকরণ ও পর্যবেক্ষণ", Icons.Default.Inventory2, MosqueGoldDeep, OperationCategory.FINANCE, cashboxBadge, counts.pendingCashboxCount > 0),
            OperationModuleEntry("audit", "স্বচ্ছতা ও সিস্টেম অডিট লগ", "সকল আর্থিক অনুমোদন ও ডাটা পরিবর্তনের অপরিবর্তনীয় রেকর্ড", Icons.Default.FactCheck, MosqueWarningAmber, OperationCategory.FINANCE, auditBadge),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও নির্দেশিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.VICE_PRESIDENT -> listOf(
            OperationModuleEntry("meetings", "কমিটি সভা ও আর্থিক রিপোর্ট", "কমিটি সভার তারিখ ও সভায় মাসিক আর্থিক রিপোর্ট পেশ", Icons.Default.EventAvailable, MosqueGoldDeep, OperationCategory.ADMIN, meetingsBadge, counts.upcomingMeetingsCount > 0),
            OperationModuleEntry("election", "কমিটি নির্বাচন ও পুনর্গঠন", "তফসিল, প্রার্থী মনোনয়ন, ভোটার তালিকা ও ফলাফল গেজেট", Icons.Default.HowToVote, MosqueGoldDeep, OperationCategory.ADMIN, electionBadge, counts.hasActiveElection),
            OperationModuleEntry("cashbox", "দান বাক্স (ক্যাশবক্স) সাক্ষ্য", "দান বাক্স খোলার সময় উপস্থিত সাক্ষী নিশ্চিতকরণ ও পর্যবেক্ষণ", Icons.Default.Inventory2, MosqueGoldDeep, OperationCategory.FINANCE, cashboxBadge, counts.pendingCashboxCount > 0),
            OperationModuleEntry("staff", "ইমাম ও কর্মচারী বেতন ব্যবস্থাপনা", "পেশ ইমাম, মুয়াজ্জিন ও খাদেমদের বেতন বিতরণ ও পে-রোল", Icons.Default.Badge, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, staffBadge),
            OperationModuleEntry("audit", "স্বচ্ছতা ও সিস্টেম অডিট লগ", "সকল আর্থিক অনুমোদন ও ডাটা পরিবর্তনের অপরিবর্তনীয় রেকর্ড", Icons.Default.FactCheck, MosqueWarningAmber, OperationCategory.FINANCE, auditBadge),
            OperationModuleEntry("office_docs", "ওয়াকফ দলিল, চুক্তি ও নথি", "মসজিদের জমি, চুক্তিপত্র ও দাপ্তরিক নথিপত্র পর্যবেক্ষণ", Icons.Default.FolderShared, RoleOfficeAccent, OperationCategory.ADMIN, docsBadge, counts.pendingDocsCount > 0),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও নির্দেশিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.GENERAL_SECRETARY -> listOf(
            OperationModuleEntry("meetings", "কমিটি সভা ও আর্থিক রিপোর্ট", "কমিটি সভার তারিখ ও সভায় মাসিক আর্থিক রিপোর্ট পেশ", Icons.Default.EventAvailable, MosqueGoldDeep, OperationCategory.ADMIN, meetingsBadge, counts.upcomingMeetingsCount > 0),
            OperationModuleEntry("members", "কমিটি ও সদস্য তালিকা", "কমিটি সদস্য ও মুসল্লিদের পরিচিতি ও তথ্য", Icons.Default.Groups, MosqueEmeraldPrimary, OperationCategory.ADMIN),
            OperationModuleEntry("staff", "ইমাম ও কর্মচারী বেতন ব্যবস্থাপনা", "পেশ ইমাম, মুয়াজ্জিন ও খাদেমদের বেতন বিতরণ ও পে-রোল", Icons.Default.Badge, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, staffBadge),
            OperationModuleEntry("notices", "নোটিশ বোর্ড ও ঘোষণা", "মসজিদের জরুরি বিজ্ঞপ্তি ও সাধারণ নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.ADMIN),
            OperationModuleEntry("office_docs", "ওয়াকফ দলিল, চুক্তি ও নথি", "মসজিদের জমি, চুক্তিপত্র ও দাপ্তরিক নথিপত্র পর্যবেক্ষণ", Icons.Default.FolderShared, RoleOfficeAccent, OperationCategory.ADMIN, docsBadge, counts.pendingDocsCount > 0),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও নির্দেশিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.ASSISTANT_SECRETARY -> listOf(
            OperationModuleEntry("meetings", "কমিটি সভা ও আর্থিক রিপোর্ট", "কমিটি সভার তারিখ ও সভায় মাসিক আর্থিক রিপোর্ট পেশ", Icons.Default.EventAvailable, MosqueGoldDeep, OperationCategory.ADMIN, meetingsBadge, counts.upcomingMeetingsCount > 0),
            OperationModuleEntry("members", "কমিটি ও সদস্য তালিকা", "কমিটি সদস্য ও মুসল্লিদের পরিচিতি ও তথ্য", Icons.Default.Groups, MosqueEmeraldPrimary, OperationCategory.ADMIN),
            OperationModuleEntry("notices", "নোটিশ বোর্ড ও ঘোষণা", "মসজিদের জরুরি বিজ্ঞপ্তি ও সাধারণ নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.ADMIN),
            OperationModuleEntry("office_docs", "ওয়াকফ দলিল, চুক্তি ও নথি", "মসজিদের জমি, চুক্তিপত্র ও দাপ্তরিক নথিপত্র পর্যবেক্ষণ", Icons.Default.FolderShared, RoleOfficeAccent, OperationCategory.ADMIN, docsBadge, counts.pendingDocsCount > 0),
            OperationModuleEntry("staff", "ইমাম ও কর্মচারী বেতন ব্যবস্থাপনা", "পেশ ইমাম, মুয়াজ্জিন ও খাদেমদের বেতন বিতরণ ও পে-রোল", Icons.Default.Badge, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, staffBadge),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও নির্দেশিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.TREASURER -> listOf(
            OperationModuleEntry("cashbox", "দান বাক্স (ক্যাশবক্স) সাক্ষ্য", "দান বাক্স খোলার সময় উপস্থিত সাক্ষী নিশ্চিতকরণ ও পর্যবেক্ষণ", Icons.Default.Inventory2, MosqueGoldDeep, OperationCategory.FINANCE, cashboxBadge, counts.pendingCashboxCount > 0),
            OperationModuleEntry("chanda", "চাঁদা আদায় ও রসিদ বহি", "সকল পরিশোধিত চাঁদার তালিকা ও ডিজিটাল কিউআর রসিদ", Icons.Default.ReceiptLong, MosqueEmeraldPrimary, OperationCategory.FINANCE, chandaBadge),
            OperationModuleEntry("staff", "ইমাম ও কর্মচারী বেতন ব্যবস্থাপনা", "পেশ ইমাম, মুয়াজ্জিন ও খাদেমদের বেতন বিতরণ ও পে-রোল", Icons.Default.Badge, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, staffBadge),
            OperationModuleEntry("madrasa", "মাদরাসা ও মক্তব ফি আদায়", "হিফজ ও মক্তব শিক্ষার্থীদের মাসিক বেতন ও ফি কালেকশন", Icons.Default.School, MosqueEmeraldPrimary, OperationCategory.EDUCATION_STAFF, madrasaBadge),
            OperationModuleEntry("audit", "স্বচ্ছতা ও সিস্টেম অডিট লগ", "সকল আর্থিক অনুমোদন ও ডাটা পরিবর্তনের অপরিবর্তনীয় রেকর্ড", Icons.Default.FactCheck, MosqueWarningAmber, OperationCategory.FINANCE, auditBadge),
            OperationModuleEntry("meetings", "কমিটি সভা ও আর্থিক রিপোর্ট", "কমিটি সভার তারিখ ও সভায় মাসিক আর্থিক রিপোর্ট পেশ", Icons.Default.EventAvailable, MosqueGoldDeep, OperationCategory.ADMIN, meetingsBadge, counts.upcomingMeetingsCount > 0),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও নির্দেশিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.ASSISTANT_TREASURER -> listOf(
            OperationModuleEntry("cashbox", "দান বাক্স (ক্যাশবক্স) সাক্ষ্য", "দান বাক্স খোলার সময় উপস্থিত সাক্ষী নিশ্চিতকরণ ও পর্যবেক্ষণ", Icons.Default.Inventory2, MosqueGoldDeep, OperationCategory.FINANCE, cashboxBadge, counts.pendingCashboxCount > 0),
            OperationModuleEntry("chanda", "চাঁদা আদায় ও রসিদ বহি", "সকল পরিশোধিত চাঁদার তালিকা ও ডিজিটাল কিউআর রসিদ", Icons.Default.ReceiptLong, MosqueEmeraldPrimary, OperationCategory.FINANCE, chandaBadge),
            OperationModuleEntry("staff", "ইমাম ও কর্মচারী বেতন ব্যবস্থাপনা", "পেশ ইমাম, মুয়াজ্জিন ও খাদেমদের বেতন বিতরণ ও পে-রোল", Icons.Default.Badge, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, staffBadge),
            OperationModuleEntry("audit", "স্বচ্ছতা ও সিস্টেম অডিট লগ", "সকল আর্থিক অনুমোদন ও ডাটা পরিবর্তনের অপরিবর্তনীয় রেকর্ড", Icons.Default.FactCheck, MosqueWarningAmber, OperationCategory.FINANCE, auditBadge),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও নির্দেশিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.ORGANIZING_SECRETARY -> listOf(
            OperationModuleEntry("members", "কমিটি ও সদস্য তালিকা", "কমিটি সদস্য ও মুসল্লিদের পরিচিতি ও তথ্য", Icons.Default.Groups, MosqueEmeraldPrimary, OperationCategory.ADMIN),
            OperationModuleEntry("events", "মাহফিল ও বিশেষ অনুষ্ঠান", "বাৎসরিক ওয়াজ, শবে কদর, ইফতার মাহফিল ও বিশেষ আয়োজন", Icons.Default.Celebration, MosqueGoldDeep, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("meetings", "কমিটি সভা ও আর্থিক রিপোর্ট", "কমিটি সভার তারিখ ও সভায় মাসিক আর্থিক রিপোর্ট পেশ", Icons.Default.EventAvailable, MosqueGoldDeep, OperationCategory.ADMIN, meetingsBadge, counts.upcomingMeetingsCount > 0),
            OperationModuleEntry("notices", "নোটিশ বোর্ড ও ঘোষণা", "মসজিদের জরুরি বিজ্ঞপ্তি ও সাধারণ নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.ADMIN),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও নির্দেশিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.PUBLICITY_SECRETARY -> listOf(
            OperationModuleEntry("publicity", "প্রচার, দেয়াল উক্তি ও স্মার্ট বোর্ড", "মসজিদের দেয়াল লিখন/উক্তি ও স্মার্ট বোর্ড পরিচালনা", Icons.Default.ViewCarousel, RolePublicityAccent, OperationCategory.ADMIN, publicityBadge),
            OperationModuleEntry("notices", "নোটিশ বোর্ড ও ঘোষণা", "মসজিদের জরুরি বিজ্ঞপ্তি ও সাধারণ নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.ADMIN),
            OperationModuleEntry("events", "মাহফিল ও বিশেষ অনুষ্ঠান", "বাৎসরিক ওয়াজ, শবে কদর, ইফতার মাহফিল ও বিশেষ আয়োজন", Icons.Default.Celebration, MosqueGoldDeep, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও নির্দেশিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.OFFICE_SECRETARY -> listOf(
            OperationModuleEntry("office_docs", "ওয়াকফ দলিল, চুক্তি ও নথি", "মসজিদের জমি, চুক্তিপত্র ও দাপ্তরিক নথিপত্র পর্যবেক্ষণ", Icons.Default.FolderShared, RoleOfficeAccent, OperationCategory.ADMIN, docsBadge, counts.pendingDocsCount > 0),
            OperationModuleEntry("meetings", "কমিটি সভা ও আর্থিক রিপোর্ট", "কমিটি সভার তারিখ ও সভায় মাসিক আর্থিক রিপোর্ট পেশ", Icons.Default.EventAvailable, MosqueGoldDeep, OperationCategory.ADMIN, meetingsBadge, counts.upcomingMeetingsCount > 0),
            OperationModuleEntry("notices", "নোটিশ বোর্ড ও ঘোষণা", "মসজিদের জরুরি বিজ্ঞপ্তি ও সাধারণ নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.ADMIN),
            OperationModuleEntry("audit", "স্বচ্ছতা ও সিস্টেম অডিট লগ", "সকল আর্থিক অনুমোদন ও ডাটা পরিবর্তনের অপরিবর্তনীয় রেকর্ড", Icons.Default.FactCheck, MosqueWarningAmber, OperationCategory.FINANCE, auditBadge),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও নির্দেশিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.WELFARE_EDUCATION_SECRETARY -> listOf(
            OperationModuleEntry("welfare", "সমাজকল্যাণ, মেডিকেল ক্যাম্প ও যাকাত", "যাকাত ও ফেতরা বিতরণ, মেডিকেল ক্যাম্প এবং ত্রাণ তদারকি", Icons.Default.Favorite, MosqueEmeraldPrimary, OperationCategory.WELFARE_EVENTS, welfareBadge),
            OperationModuleEntry("madrasa", "মাদরাসা ও মক্তব ফি আদায়", "হিফজ ও মক্তব শিক্ষার্থীদের মাসিক বেতন ও ফি কালেকশন", Icons.Default.School, MosqueEmeraldPrimary, OperationCategory.EDUCATION_STAFF, madrasaBadge),
            OperationModuleEntry("events", "মাহফিল ও বিশেষ অনুষ্ঠান", "বাৎসরিক ওয়াজ, শবে কদর, ইফতার মাহফিল ও বিশেষ আয়োজন", Icons.Default.Celebration, MosqueGoldDeep, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("notices", "নোটিশ বোর্ড ও ঘোষণা", "মসজিদের জরুরি বিজ্ঞপ্তি ও সাধারণ নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.ADMIN),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও নির্দেশিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.EXECUTIVE_MEMBER -> listOf(
            OperationModuleEntry("cashbox", "দান বাক্স (ক্যাশবক্স) সাক্ষ্য", "দান বাক্স খোলার সময় উপস্থিত সাক্ষী নিশ্চিতকরণ ও পর্যবেক্ষণ", Icons.Default.Inventory2, MosqueGoldDeep, OperationCategory.FINANCE, cashboxBadge, counts.pendingCashboxCount > 0),
            OperationModuleEntry("meetings", "কমিটি সভা ও আর্থিক রিপোর্ট", "কমিটি সভার তারিখ ও সভায় মাসিক আর্থিক রিপোর্ট পেশ", Icons.Default.EventAvailable, MosqueGoldDeep, OperationCategory.ADMIN, meetingsBadge, counts.upcomingMeetingsCount > 0),
            OperationModuleEntry("members", "কমিটি ও সদস্য তালিকা", "কমিটি সদস্য ও মুসল্লিদের পরিচিতি ও তথ্য", Icons.Default.Groups, MosqueEmeraldPrimary, OperationCategory.ADMIN),
            OperationModuleEntry("events", "মাহফিল ও বিশেষ অনুষ্ঠান", "বাৎসরিক ওয়াজ, শবে কদর, ইফতার মাহফিল ও বিশেষ আয়োজন", Icons.Default.Celebration, MosqueGoldDeep, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও নির্দেশিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.IMAM -> listOf(
            OperationModuleEntry("sharia_advisory", "শরিয়াহ পর্যালোচনা ও উপদেষ্টা পরিষদ", "ইমামের শরিয়াহ মতামত ও উপদেষ্টা পরিষদের পরামর্শ সমন্বয়", Icons.Default.AccountBalance, RoleAdvisoryAccent, OperationCategory.ADMIN, advisoryBadge, counts.activeAdvisoryCount > 0),
            OperationModuleEntry("roster", "ওয়াক্ত ও নামাজের দায়িত্ব বণ্টন", "ইমাম, মুয়াজ্জিন ও খাদেমের ডিউটি রোস্টার তদারকি", Icons.Default.EventNote, MosqueGoldDeep, OperationCategory.EDUCATION_STAFF, rosterBadge),
            OperationModuleEntry("madrasa", "মাদরাসা ও মক্তব ফি আদায়", "হিফজ ও মক্তব শিক্ষার্থীদের মাসিক বেতন ও ফি কালেকশন", Icons.Default.School, MosqueEmeraldPrimary, OperationCategory.EDUCATION_STAFF, madrasaBadge),
            OperationModuleEntry("events", "মাহফিল ও বিশেষ অনুষ্ঠান", "বাৎসরিক ওয়াজ, শবে কদর, ইফতার মাহফিল ও বিশেষ আয়োজন", Icons.Default.Celebration, MosqueGoldDeep, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("prayer", "পূর্ণ নামাজের সময়সূচি ও ওয়াক্ত", "আজান ও জামাতের বিস্তারিত সূচি ও ক্যালেন্ডার", Icons.Default.AccessTime, MosqueEmeraldLight, OperationCategory.INFO),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও নির্দেশিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.ADVISORY_BOARD -> listOf(
            OperationModuleEntry("sharia_advisory", "শরিয়াহ পর্যালোচনা ও উপদেষ্টা পরিষদ", "ইমামের শরিয়াহ মতামত ও উপদেষ্টা পরিষদের পরামর্শ সমন্বয়", Icons.Default.AccountBalance, RoleAdvisoryAccent, OperationCategory.ADMIN, advisoryBadge, counts.activeAdvisoryCount > 0),
            OperationModuleEntry("audit", "স্বচ্ছতা ও সিস্টেম অডিট লগ", "সকল আর্থিক অনুমোদন ও ডাটা পরিবর্তনের অপরিবর্তনীয় রেকর্ড", Icons.Default.FactCheck, MosqueWarningAmber, OperationCategory.FINANCE, auditBadge),
            OperationModuleEntry("election", "কমিটি নির্বাচন ও পুনর্গঠন", "তফসিল, প্রার্থী মনোনয়ন, ভোটার তালিকা ও ফলাফল গেজেট", Icons.Default.HowToVote, MosqueGoldDeep, OperationCategory.ADMIN, electionBadge, counts.hasActiveElection),
            OperationModuleEntry("members", "কমিটি ও সদস্য তালিকা", "কমিটি সদস্য ও মুসল্লিদের পরিচিতি ও তথ্য", Icons.Default.Groups, MosqueEmeraldPrimary, OperationCategory.ADMIN),
            OperationModuleEntry("office_docs", "ওয়াকফ দলিল, চুক্তি ও নথি", "মসজিদের জমি, চুক্তিপত্র ও দাপ্তরিক নথিপত্র পর্যবেক্ষণ", Icons.Default.FolderShared, RoleOfficeAccent, OperationCategory.ADMIN, docsBadge, counts.pendingDocsCount > 0),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও নির্দেশিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.MUAZZIN -> listOf(
            OperationModuleEntry("prayer", "পূর্ণ নামাজের সময়সূচি ও ওয়াক্ত", "আজান ও জামাতের বিস্তারিত সূচি ও ক্যালেন্ডার", Icons.Default.AccessTime, MosqueEmeraldLight, OperationCategory.INFO),
            OperationModuleEntry("roster", "ওয়াক্ত ও নামাজের দায়িত্ব বণ্টন", "ইমাম, মুয়াজ্জিন ও খাদেমের ডিউটি রোস্টার তদারকি", Icons.Default.EventNote, MosqueGoldDeep, OperationCategory.EDUCATION_STAFF, rosterBadge),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও নির্দেশিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.KHADEM -> listOf(
            OperationModuleEntry("roster", "ওয়াক্ত ও নামাজের দায়িত্ব বণ্টন", "ইমাম, মুয়াজ্জিন ও খাদেমের ডিউটি রোস্টার তদারকি", Icons.Default.EventNote, MosqueGoldDeep, OperationCategory.EDUCATION_STAFF, rosterBadge),
            OperationModuleEntry("prayer", "পূর্ণ নামাজের সময়সূচি ও ওয়াক্ত", "আজান ও জামাতের বিস্তারিত সূচি ও ক্যালেন্ডার", Icons.Default.AccessTime, MosqueEmeraldLight, OperationCategory.INFO),
            OperationModuleEntry("notices", "নোটিশ বোর্ড ও ঘোষণা", "মসজিদের জরুরি বিজ্ঞপ্তি ও সাধারণ নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.ADMIN),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও নির্দেশিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.GENERAL_MEMBER -> listOf(
            OperationModuleEntry("transparency", "মসজিদ স্বচ্ছ তহবিল ও হিসাব", "আয়-ব্যয়, চাঁদা ও ক্রয়-বিক্রয়ের উন্মুক্ত আর্থিক বিবরণী", Icons.Default.AccountBalanceWallet, MosqueSuccessGreen, OperationCategory.FINANCE),
            OperationModuleEntry("notices", "নোটিশ বোর্ড ও ঘোষণা", "মসজিদের জরুরি বিজ্ঞপ্তি ও সাধারণ নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.ADMIN),
            OperationModuleEntry("election", "কমিটি নির্বাচন ও পুনর্গঠন", "তফসিল, প্রার্থী মনোনয়ন, ভোটার তালিকা ও ফলাফল গেজেট", Icons.Default.HowToVote, MosqueGoldDeep, OperationCategory.ADMIN, electionBadge, counts.hasActiveElection),
            OperationModuleEntry("events", "মাহফিল ও বিশেষ অনুষ্ঠান", "বাৎসরিক ওয়াজ, শবে কদর, ইফতার মাহফিল ও বিশেষ আয়োজন", Icons.Default.Celebration, MosqueGoldDeep, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("welfare", "সমাজকল্যাণ, মেডিকেল ক্যাম্প ও যাকাত", "যাকাত ও ফেতরা বিতরণ, মেডিকেল ক্যাম্প এবং ত্রাণ তদারকি", Icons.Default.Favorite, MosqueEmeraldPrimary, OperationCategory.WELFARE_EVENTS, welfareBadge),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও নির্দেশিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        else -> listOf(
            OperationModuleEntry("meetings", "কমিটি সভা ও আর্থিক রিপোর্ট", "কমিটি সভার তারিখ ও সভায় মাসিক আর্থিক রিপোর্ট পেশ", Icons.Default.EventAvailable, MosqueGoldDeep, OperationCategory.ADMIN, meetingsBadge, counts.upcomingMeetingsCount > 0),
            OperationModuleEntry("election", "কমিটি নির্বাচন ও পুনর্গঠন", "তফসিল, প্রার্থী মনোনয়ন, ভোটার তালিকা ও ফলাফল গেজেট", Icons.Default.HowToVote, MosqueGoldDeep, OperationCategory.ADMIN, electionBadge, counts.hasActiveElection),
            OperationModuleEntry("cashbox", "দান বাক্স (ক্যাশবক্স) সাক্ষ্য", "দান বাক্স খোলার সময় উপস্থিত সাক্ষী নিশ্চিতকরণ ও পর্যবেক্ষণ", Icons.Default.Inventory2, MosqueGoldDeep, OperationCategory.FINANCE, cashboxBadge, counts.pendingCashboxCount > 0),
            OperationModuleEntry("audit", "স্বচ্ছতা ও সিস্টেম অডিট লগ", "সকল আর্থিক অনুমোদন ও ডাটা পরিবর্তনের অপরিবর্তনীয় রেকর্ড", Icons.Default.FactCheck, MosqueWarningAmber, OperationCategory.FINANCE, auditBadge),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও নির্দেশিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
    }
    // Common extras are NOT for everyone: each one is shown only to the roles
    // whose duties actually need it. This keeps every interface duty-focused.
    val commonExtras = listOf(
        OperationModuleEntry(
            key = "islamic_calendar",
            title = "ইসলামিক ক্যালেন্ডার ও এআই ইভেন্ট",
            description = "হিজরি দিনপঞ্জি, রমজান, ঈদ ও এআই খুতবা সহায়িকা",
            icon = Icons.Default.Event,
            iconColor = MosqueGoldDark,
            category = OperationCategory.INFO
        ),
        OperationModuleEntry(
            key = "ramadan_hub",
            title = "মাহে রমজান হাব ও সময়সূচি",
            description = "সেহরি ও ইফতারের সূচি, মাসায়েল, দোয়া ও ফিতরা সহায়িকা",
            icon = Icons.Default.DarkMode,
            iconColor = MosqueGoldDeep,
            category = OperationCategory.INFO
        ),
        OperationModuleEntry(
            key = "invitation",
            title = "দাওয়াতনামা ও পোস্টার তৈরি",
            description = "ওয়াজ মাহফিল, ইফতার ও সভার আকর্ষণীয় ব্যানার ও প্রিন্ট",
            icon = Icons.Default.AutoAwesome,
            iconColor = MosqueGoldDark,
            category = OperationCategory.WELFARE_EVENTS
        ),
        OperationModuleEntry(
            key = "cashbox_counter",
            title = "জুমার দানবাক্স ৩-সাক্ষী কাউন্টার",
            description = "নোট বিভাজন গণনা, তাৎক্ষণিক হিসাব ও প্রত্যয়ন ভাউচার",
            icon = Icons.Default.Inventory2,
            iconColor = MosqueEmeraldPrimary,
            category = OperationCategory.FINANCE
        ),
        OperationModuleEntry(
            key = "janaza_alert",
            title = "জরুরি জানাজা ও মৃত্যু সংবাদ",
            description = "মহল্লাবাসীর জন্য তাৎক্ষণিক নোটিশ ও ব্রডকাস্ট",
            icon = Icons.Default.Campaign,
            iconColor = MosqueUrgentRed,
            category = OperationCategory.ADMIN
        ),
        OperationModuleEntry(
            key = "official_seals",
            title = "কর্মকর্তাদের সিলমোহর ও ডিজিটাল স্বাক্ষর",
            description = "পদধারী সদস্য, ইমাম ও মসজিদের অফিসিয়াল সিল এবং স্বাক্ষর ব্যবস্থাপনা",
            icon = Icons.Default.Draw,
            iconColor = MosqueEmeraldPrimary,
            category = OperationCategory.ADMIN
        )
    )
    return baseModules + commonExtras.filter { it.key in commonExtrasForRole(currentRole) }
}

/** Which of the shared utility modules a role actually needs for its duties. */
private fun commonExtrasForRole(role: UserRole): Set<String> = when (role) {
    UserRole.PRESIDENT -> setOf("islamic_calendar", "ramadan_hub", "official_seals")
    UserRole.VICE_PRESIDENT, UserRole.GENERAL_SECRETARY -> setOf("islamic_calendar", "ramadan_hub", "official_seals")
    UserRole.ASSISTANT_SECRETARY, UserRole.OFFICE_SECRETARY -> setOf("islamic_calendar", "ramadan_hub", "official_seals")
    UserRole.TREASURER, UserRole.ASSISTANT_TREASURER -> setOf("islamic_calendar", "ramadan_hub", "cashbox_counter")
    UserRole.PUBLICITY_SECRETARY, UserRole.ORGANIZING_SECRETARY -> setOf("islamic_calendar", "ramadan_hub", "invitation")
    UserRole.WELFARE_EDUCATION_SECRETARY -> setOf("islamic_calendar", "ramadan_hub", "invitation", "janaza_alert")
    UserRole.EXECUTIVE_MEMBER, UserRole.ADVISORY_BOARD -> setOf("islamic_calendar", "ramadan_hub", "cashbox_counter")
    UserRole.IMAM -> setOf("islamic_calendar", "ramadan_hub", "janaza_alert", "invitation")
    UserRole.MUAZZIN, UserRole.KHADEM -> setOf("islamic_calendar", "ramadan_hub", "janaza_alert")
    UserRole.GENERAL_MEMBER -> setOf("islamic_calendar", "ramadan_hub")
}
