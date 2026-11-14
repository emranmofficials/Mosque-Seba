package com.mosqueseba.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.data.model.UserRole
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
    val auditLogsCount: Int = 0
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Emerald-Gold Hero Header
        Surface(
            color = Color.Transparent,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MosqueEmeraldDarkGradient)
            ) {
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "আরও সেবা ও অপারেশন",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "মসজিদ পরিচালনা, দপ্তর ও সেবামূলক মডিউলসমূহ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MosqueGoldLight.copy(alpha = 0.9f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MosqueGold.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.65f))
                        ) {
                            Text(
                                text = currentRole.titleBn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MosqueGoldLight,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MoreHeaderSummaryPill(
                            modifier = Modifier.weight(1f),
                            label = "সক্রিয় মডিউল",
                            value = "${toBengaliDigits(allRoleModules.size.toString())} টি",
                            accent = MosqueGoldLight
                        )
                        MoreHeaderSummaryPill(
                            modifier = Modifier.weight(1f),
                            label = "আসন্ন সভা ও ইভেন্ট",
                            value = "${toBengaliDigits((liveCounts.upcomingMeetingsCount + liveCounts.upcomingEventsCount).toString())} টি",
                            accent = Color(0xFF6EE7B7)
                        )
                        MoreHeaderSummaryPill(
                            modifier = Modifier.weight(1f),
                            label = "অপেক্ষমাণ কাজ",
                            value = "${toBengaliDigits(pendingTotal.toString())} টি",
                            accent = if (pendingTotal > 0) Color(0xFFFCD34D) else Color(0xFF6EE7B7)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .background(IslamicGoldTrimGradient)
                )
            }
        }

        // 2. Search & Category Filter Card
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "মডিউল খুঁজুন (যেমন: মাদরাসা, ক্যাশবক্স, রোস্টার)",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "অনুসন্ধান",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "অনুসন্ধান মুছুন",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MosqueCardBorderGold
                    ),
                    singleLine = true,
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
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = "${category.labelBn} (${toBengaliDigits(count.toString())})",
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MosqueEmeraldPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("more_filter_${category.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // 3. Quick Access Grid + Categorized Module Cards
        LazyColumn(
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .widthIn(max = 840.dp)
        ) {
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
                    items(quickAccessModules.chunked(2)) { pair ->
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

                // Categorized Sections
                groupedModules.forEach { (category, mods) ->
                    item(key = "header_${category.name}") {
                        Spacer(modifier = Modifier.height(4.dp))
                        MoreSectionHeader(
                            title = category.labelBn,
                            badgeText = "${toBengaliDigits(mods.size.toString())} টি"
                        )
                    }
                    items(mods, key = { "${category.name}_${it.key}" }) { mod ->
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

@Composable
private fun MoreHeaderSummaryPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.1f),
        border = BorderStroke(0.8.dp, MosqueGold.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
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
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(primaryColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = primaryColor
            )
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft
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
    Card(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 104.dp)
            .testTag("more_quick_${module.key}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MosqueCardBorderGold.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(module.iconColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = module.icon,
                        contentDescription = module.title,
                        tint = module.iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                module.badgeText?.let { badge ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            module.badgeIsAlert -> MosqueWarningAmber.copy(alpha = 0.14f)
                            isDark -> MosqueNightSurfaceVariant
                            else -> MosqueEmeraldSoft
                        }
                    ) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (module.badgeIsAlert) MosqueWarningAmber else primaryColor,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
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

    return when (currentRole) {
        UserRole.TREASURER -> listOf(
            OperationModuleEntry("staff", "ইমাম ও কর্মচারী বেতন ব্যবস্থাপনা", "পেশ ইমাম, মুয়াজ্জিন ও খাদেমদের বেতন বিতরণ ও পে-রোল", Icons.Default.Badge, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, staffBadge),
            OperationModuleEntry("chanda", "চাঁদা আদায় ও রসিদ বহি", "সকল পরিশোধিত চাঁদার তালিকা ও ডিজিটাল কিউআর রসিদ", Icons.Default.ReceiptLong, MosqueEmeraldPrimary, OperationCategory.FINANCE, chandaBadge),
            OperationModuleEntry("madrasa", "মাদরাসা ও মক্তব ফি আদায়", "হিফজ ও মক্তব শিক্ষার্থীদের মাসিক বেতন ও ফি কালেকশন", Icons.Default.School, MosqueEmeraldPrimary, OperationCategory.EDUCATION_STAFF, madrasaBadge),
            OperationModuleEntry("meetings", "কমিটি সভা ও আর্থিক রিপোর্ট", "কমিটি সভার তারিখ ও সভায় মাসিক আর্থিক রিপোর্ট পেশ", Icons.Default.EventAvailable, MosqueGoldDeep, OperationCategory.ADMIN, meetingsBadge, counts.upcomingMeetingsCount > 0),
            OperationModuleEntry("notices", "নোটিশ বোর্ড ও ঘোষণা", "মসজিদের জরুরি বিজ্ঞপ্তি ও সাধারণ নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.ADMIN),
            OperationModuleEntry("audit", "স্বচ্ছতা ও সিস্টেম অডিট লগ", "সকল আর্থিক অনুমোদন ও ডাটা পরিবর্তনের অপরিবর্তনীয় রেকর্ড", Icons.Default.FactCheck, MosqueWarningAmber, OperationCategory.FINANCE, auditBadge),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও নির্দেশিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.ASSISTANT_TREASURER -> listOf(
            OperationModuleEntry("staff", "ইমাম ও কর্মচারী বেতন ব্যবস্থাপনা", "পেশ ইমাম, মুয়াজ্জিন ও খাদেমদের বেতন বিতরণ ও পে-রোল", Icons.Default.Badge, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, staffBadge),
            OperationModuleEntry("chanda", "চাঁদা আদায় ও রসিদ বহি", "সকল পরিশোধিত চাঁদার তালিকা ও ডিজিটাল কিউআর রসিদ", Icons.Default.ReceiptLong, MosqueEmeraldPrimary, OperationCategory.FINANCE, chandaBadge),
            OperationModuleEntry("madrasa", "মাদরাসা ও মক্তব ফি আদায়", "হিফজ ও মক্তব শিক্ষার্থীদের মাসিক ফি কালেকশন", Icons.Default.School, MosqueEmeraldPrimary, OperationCategory.EDUCATION_STAFF, madrasaBadge),
            OperationModuleEntry("meetings", "কমিটি সভা ও আর্থিক রিপোর্ট", "কমিটি সভার তারিখ ও আর্থিক রিপোর্ট প্রস্তুতি", Icons.Default.EventAvailable, MosqueGoldDeep, OperationCategory.ADMIN, meetingsBadge, counts.upcomingMeetingsCount > 0),
            OperationModuleEntry("notices", "নোটিশ বোর্ড ও ঘোষণা", "মসজিদের জরুরি বিজ্ঞপ্তি ও সাধারণ নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.ADMIN),
            OperationModuleEntry("audit", "আর্থিক অডিট ও ভাউচার লগ", "সকল চাঁদা ও বেতন ভাউচারের অপরিবর্তনীয় রেকর্ড", Icons.Default.FactCheck, MosqueWarningAmber, OperationCategory.FINANCE, auditBadge),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির সকল পদ ও দায়িত্বের তালিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.GENERAL_SECRETARY -> listOf(
            OperationModuleEntry("notices", "নোটিশ বোর্ড ও ঘোষণা", "জরুরি ও সাধারণ বিজ্ঞপ্তি প্রকাশ এবং পরিচালনা", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.ADMIN),
            OperationModuleEntry("roster", "ওয়াক্ত ও নামাজের দায়িত্ব বণ্টন", "ইমাম, মুয়াজ্জিন ও খাদেমের ডিউটি রোস্টার তদারকি", Icons.Default.EventNote, MosqueGoldDeep, OperationCategory.EDUCATION_STAFF, rosterBadge),
            OperationModuleEntry("food", "ইমাম ও খাদেমদের খাবার শিডিউল", "দৈনিক খাবার বণ্টন ও দাতাদের তালিকা", Icons.Default.Restaurant, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, foodBadge),
            OperationModuleEntry("staff", "ইমাম ও কর্মচারী তালিকা ও খেদমত", "মসজিদের ইমাম, মুয়াজ্জিন ও খাদেমদের সার্ভিস রেকর্ড ও তদারকি", Icons.Default.Badge, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, staffBadge),
            OperationModuleEntry("madrasa", "মাদরাসা ও মক্তব পরিচালনা", "ছাত্র ভর্তি, দৈনিক হাজিরা ও মক্তব-হিফজ কার্যক্রম তদারকি", Icons.Default.School, MosqueEmeraldPrimary, OperationCategory.EDUCATION_STAFF, madrasaBadge),
            OperationModuleEntry("cashbox", "দান বাক্স (ক্যাশবক্স) সাক্ষ্য", "দান বাক্স খোলার সময় উপস্থিত সাক্ষী নিশ্চিতকরণ ও পর্যবেক্ষণ", Icons.Default.Inventory2, MosqueGoldDeep, OperationCategory.FINANCE, cashboxBadge, counts.pendingCashboxCount > 0),
            OperationModuleEntry("sharia_advisory", "শরিয়াহ পর্যালোচনা ও উপদেষ্টা পরিষদ", "ইমামের শরিয়াহ মতামত ও উপদেষ্টা পরিষদের পরামর্শ সমন্বয়", Icons.Default.AccountBalance, RoleAdvisoryAccent, OperationCategory.ADMIN, advisoryBadge, counts.activeAdvisoryCount > 0),
            OperationModuleEntry("publicity", "প্রচার, দেয়াল উক্তি ও স্মার্ট বোর্ড", "মসজিদের দেয়াল লিখন/উক্তি ও স্মার্ট বোর্ড পরিচালনা", Icons.Default.ViewCarousel, RolePublicityAccent, OperationCategory.ADMIN, publicityBadge),
            OperationModuleEntry("welfare", "সমাজকল্যাণ, মেডিকেল ক্যাম্প ও যাকাত", "যাকাত ও ফেতরা বিতরণ, মেডিকেল ক্যাম্প এবং ত্রাণ তদারকি", Icons.Default.Favorite, MosqueEmeraldPrimary, OperationCategory.WELFARE_EVENTS, welfareBadge),
            OperationModuleEntry("events", "মাহফিল ও বিশেষ অনুষ্ঠান", "বাৎসরিক ওয়াজ, শবে কদর, ইফতার মাহফিল ও বিশেষ আয়োজন", Icons.Default.Celebration, MosqueGoldDeep, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("audit", "স্বচ্ছতা ও সিস্টেম অডিট লগ", "সকল প্রশাসনিক ও আর্থিক কার্যক্রমের অপরিবর্তনীয় রেকর্ড", Icons.Default.FactCheck, MosqueWarningAmber, OperationCategory.ADMIN, auditBadge),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির সকল পদ ও দায়িত্বের বিস্তারিত তালিকা", Icons.Default.VerifiedUser, MosqueEmeraldPrimary, OperationCategory.INFO)
        )
        UserRole.ASSISTANT_SECRETARY -> listOf(
            OperationModuleEntry("meetings", "কমিটি সভা ও এজেন্ডা রেজুলেশন", "সাধারণ সম্পাদকের নির্দেশনায় সভা আহ্বান ও কার্যবিবরণী সংরক্ষণ", Icons.Default.Event, MosqueGoldDeep, OperationCategory.ADMIN, meetingsBadge, counts.upcomingMeetingsCount > 0),
            OperationModuleEntry("roster", "ওয়াক্ত ও নামাজের দায়িত্ব বণ্টন", "ইমাম, মুয়াজ্জিন ও খাদেমের ডিউটি রোস্টার তদারকি", Icons.Default.EventNote, MosqueEmeraldPrimary, OperationCategory.EDUCATION_STAFF, rosterBadge),
            OperationModuleEntry("staff", "ইমাম ও কর্মচারী তালিকা ও খেদমত", "মসজিদের ইমাম, মুয়াজ্জিন ও খাদেমদের সার্ভিস রেকর্ড ও তদারকি", Icons.Default.Badge, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, staffBadge),
            OperationModuleEntry("food", "ইমাম ও খাদেমদের খাবার শিডিউল", "দৈনিক খাবার বণ্টন ও দাতাদের তালিকা সমন্বয়", Icons.Default.Restaurant, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, foodBadge),
            OperationModuleEntry("sharia_advisory", "শরিয়াহ পর্যালোচনা ও উপদেষ্টা পরিষদ", "ইমামের শরিয়াহ মতামত ও উপদেষ্টা পরিষদের পরামর্শ পর্যালোচনা", Icons.Default.AccountBalance, RoleAdvisoryAccent, OperationCategory.ADMIN, advisoryBadge, counts.activeAdvisoryCount > 0),
            OperationModuleEntry("madrasa", "মক্তব ও মাদরাসা কার্যক্রম", "শিক্ষার্থী তালিকা ও মক্তব-হিফজ কার্যক্রম সমন্বয়", Icons.Default.School, MosqueEmeraldPrimary, OperationCategory.EDUCATION_STAFF, madrasaBadge),
            OperationModuleEntry("welfare", "সমাজকল্যাণ ও যাকাত তদারকি", "যাকাত-ফেতরা ও দুস্থ সহায়তা আবেদন সমন্বয়", Icons.Default.Favorite, MosqueEmeraldPrimary, OperationCategory.WELFARE_EVENTS, welfareBadge),
            OperationModuleEntry("events", "মাহফিল ও বিশেষ অনুষ্ঠান", "মসজিদের অনুষ্ঠান ও স্বেচ্ছাসেবক সমন্বয়", Icons.Default.Celebration, MosqueGoldDeep, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির সকল পদ ও দায়িত্বের বিস্তারিত তালিকা", Icons.Default.VerifiedUser, MosqueEmeraldPrimary, OperationCategory.INFO),
            OperationModuleEntry("prayer", "পূর্ণ নামাজের সময়সূচি ও ওয়াক্ত", "আজান ও জামাতের বিস্তারিত সূচি ও ক্যালেন্ডার", Icons.Default.AccessTime, MosqueEmeraldLight, OperationCategory.INFO)
        )
        UserRole.ORGANIZING_SECRETARY -> listOf(
            OperationModuleEntry("notices", "নোটিশ বোর্ড ও ঘোষণা", "সাংগঠনিক বিজ্ঞপ্তি ও মুসল্লি আহ্বান", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.ADMIN),
            OperationModuleEntry("welfare", "সমাজকল্যাণ ও যাকাত তহবিল", "যাকাত ও ফেতরা বিতরণ, দুস্থ সহায়তা এবং যুব সমাজকল্যাণ", Icons.Default.Favorite, MosqueEmeraldPrimary, OperationCategory.WELFARE_EVENTS, welfareBadge),
            OperationModuleEntry("food", "ইমাম ও খাদেমদের খাবার শিডিউল", "মুসল্লিদের জায়গির ও খাবার দায়িত্ব বণ্টন", Icons.Default.Restaurant, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, foodBadge),
            OperationModuleEntry("roster", "ওয়াক্ত ও জামাত দায়িত্ব রোস্টার", "জুমা, নামাজ ও মসজিদ শৃঙ্খলায় দায়িত্ব সমন্বয়", Icons.Default.EventNote, MosqueGoldDeep, OperationCategory.EDUCATION_STAFF, rosterBadge),
            OperationModuleEntry("madrasa", "মক্তব ও মাদরাসা কার্যক্রম", "এলাকার শিশু-কিশোরদের ধর্মীয় শিক্ষা ও অভিভাবক সংযোগ", Icons.Default.School, MosqueEmeraldPrimary, OperationCategory.EDUCATION_STAFF, madrasaBadge),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির সকল পদ ও দায়িত্বের বিস্তারিত তালিকা", Icons.Default.VerifiedUser, MosqueEmeraldPrimary, OperationCategory.INFO),
            OperationModuleEntry("prayer", "পূর্ণ নামাজের সময়সূচি ও ওয়াক্ত", "আজান ও জামাতের বিস্তারিত সূচি ও ক্যালেন্ডার", Icons.Default.AccessTime, MosqueEmeraldLight, OperationCategory.INFO)
        )
        UserRole.PUBLICITY_SECRETARY -> listOf(
            OperationModuleEntry("meetings", "কমিটি সভা ও আলোচ্যসূচি", "কমিটির আসন্ন সভা, এজেন্ডা ও গৃহীত সিদ্ধান্তসমূহ", Icons.Default.Event, MosqueGoldDeep, OperationCategory.ADMIN, meetingsBadge, counts.upcomingMeetingsCount > 0),
            OperationModuleEntry("members", "কমিটি ও সদস্য তালিকা", "কমিটি সদস্য ও মুসল্লিদের পরিচিতি ও তথ্য", Icons.Default.Groups, MosqueEmeraldPrimary, OperationCategory.ADMIN),
            OperationModuleEntry("roster", "ওয়াক্ত ও ইমাম-মুয়াজ্জিন রোস্টার", "জুমা, ওয়াক্ত ও খতিব/ইমাম দায়িত্ব সূচি", Icons.Default.EventNote, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, rosterBadge),
            OperationModuleEntry("madrasa", "মক্তব ও মাদরাসা তথ্য", "মাদরাসা ভর্তি ও ধর্মীয় শিক্ষা প্রচার সমন্বয়", Icons.Default.School, MosqueEmeraldPrimary, OperationCategory.EDUCATION_STAFF, madrasaBadge),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির সকল পদ ও দায়িত্বের বিস্তারিত তালিকা", Icons.Default.VerifiedUser, MosqueEmeraldPrimary, OperationCategory.INFO),
            OperationModuleEntry("prayer", "পূর্ণ নামাজের সময়সূচি ও ওয়াক্ত", "আজান ও জামাতের বিস্তারিত সূচি ও ক্যালেন্ডার", Icons.Default.AccessTime, MosqueEmeraldLight, OperationCategory.INFO)
        )
        UserRole.OFFICE_SECRETARY -> listOf(
            OperationModuleEntry("members", "কমিটি ও সদস্য নথিপত্র", "কার্যনির্বাহী ও সাধারণ সদস্যদের পরিচিতি ও তথ্য ফাইল", Icons.Default.Groups, MosqueEmeraldPrimary, OperationCategory.ADMIN),
            OperationModuleEntry("audit", "স্বচ্ছতা ও সিস্টেম অডিট লগ", "সকল নথিপত্র ও প্রশাসনিক কার্যক্রমের অপরিবর্তনীয় রেকর্ড", Icons.Default.FactCheck, MosqueWarningAmber, OperationCategory.ADMIN, auditBadge),
            OperationModuleEntry("sharia_advisory", "শরিয়াহ ও উপদেষ্টা নথি", "ইমামের শরিয়াহ মতামত ও উপদেষ্টা পরিষদের রেজুলেশন ফাইল", Icons.Default.AccountBalance, RoleAdvisoryAccent, OperationCategory.ADMIN, advisoryBadge, counts.activeAdvisoryCount > 0),
            OperationModuleEntry("staff", "ইমাম ও কর্মচারী সার্ভিস রেকর্ড", "পেশ ইমাম, মুয়াজ্জিন ও খাদেমদের নিয়োগ ও বেতন নথি", Icons.Default.Badge, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, staffBadge),
            OperationModuleEntry("events", "অনুষ্ঠান ও মাহফিল আর্কাইভ", "মসজিদের বাৎসরিক মাহফিল ও বিশেষ অনুষ্ঠানের বিবরণ", Icons.Default.Celebration, MosqueGoldDeep, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির সকল পদ ও দায়িত্বের বিস্তারিত তালিকা", Icons.Default.VerifiedUser, MosqueEmeraldPrimary, OperationCategory.INFO),
            OperationModuleEntry("prayer", "পূর্ণ নামাজের সময়সূচি ও ওয়াক্ত", "আজান ও জামাতের বিস্তারিত সূচি ও ক্যালেন্ডার", Icons.Default.AccessTime, MosqueEmeraldLight, OperationCategory.INFO)
        )
        UserRole.WELFARE_EDUCATION_SECRETARY -> listOf(
            OperationModuleEntry("events", "মেডিকেল ক্যাম্প, মাহফিল ও সেমিনার", "ধর্মীয় শিক্ষা অনুষ্ঠান, ফ্রি মেডিকেল ক্যাম্প ও ত্রাণ আয়োজন", Icons.Default.Celebration, MosqueGoldDeep, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("meetings", "কমিটি সভা ও কার্যবিবরণী", "কমিটির আসন্ন সভা, এজেন্ডা ও কল্যাণমূলক সিদ্ধান্ত", Icons.Default.EventAvailable, MosqueEmeraldPrimary, OperationCategory.ADMIN, meetingsBadge, counts.upcomingMeetingsCount > 0),
            OperationModuleEntry("sharia_advisory", "শরিয়াহ নীতি ও উপদেষ্টা পরামর্শ", "যাকাত-ফেতরা ও ধর্মীয় শিক্ষায় ইমাম ও উপদেষ্টাদের নির্দেশনা", Icons.Default.AccountBalance, RoleAdvisoryAccent, OperationCategory.ADMIN, advisoryBadge, counts.activeAdvisoryCount > 0),
            OperationModuleEntry("food", "শিক্ষার্থী ও স্টাফ খাবার সূচি", "দৈনিক খাবার বণ্টন ও জায়গির দাতাদের তালিকা", Icons.Default.Restaurant, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, foodBadge),
            OperationModuleEntry("roster", "তালিম ও নামাজ দায়িত্ব রোস্টার", "ইমাম-মুয়াজ্জিনদের ওয়াক্ত ও তালিম সূচি", Icons.Default.EventNote, MosqueEmeraldDark, OperationCategory.EDUCATION_STAFF, rosterBadge),
            OperationModuleEntry("members", "সদস্য ও অভিভাবক পরিষদ", "কমিটি সদস্য, দাতা ও মুসল্লিদের পরিচিতি", Icons.Default.Groups, MosqueEmeraldPrimary, OperationCategory.ADMIN),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির সকল পদ ও দায়িত্বের বিস্তারিত তালিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO),
            OperationModuleEntry("prayer", "পূর্ণ নামাজের সময়সূচি ও ওয়াক্ত", "আজান ও জামাতের বিস্তারিত সূচি ও ক্যালেন্ডার", Icons.Default.AccessTime, MosqueEmeraldLight, OperationCategory.INFO)
        )
        UserRole.EXECUTIVE_MEMBER -> listOf(
            OperationModuleEntry("notices", "নোটিশ বোর্ড ও ঘোষণা", "মসজিদের জরুরি বিজ্ঞপ্তি ও সাধারণ নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.ADMIN),
            OperationModuleEntry("events", "জুমা, ঈদ শৃঙ্খলা ও অনুষ্ঠান", "জামাত শৃঙ্খলা ও মাহফিল ব্যবস্থাপনায় সহায়তা", Icons.Default.Celebration, MosqueGoldDeep, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("transparency", "মসজিদ স্বচ্ছ তহবিল ও হিসাব", "আয়-ব্যয়, চাঁদা ও ক্রয়-বিক্রয়ের উন্মুক্ত আর্থিক বিবরণী", Icons.Default.AccountBalanceWallet, MosqueSuccessGreen, OperationCategory.FINANCE),
            OperationModuleEntry("audit", "স্বচ্ছতা ও সিস্টেম অডিট লগ", "সকল দান বাক্স সাক্ষ্য, ক্রয় ও অনুমোদনের অপরিবর্তনীয় রেকর্ড", Icons.Default.FactCheck, MosqueWarningAmber, OperationCategory.FINANCE, auditBadge),
            OperationModuleEntry("welfare", "সমাজকল্যাণ ও যাকাত তহবিল", "যাকাত, চিকিৎসা ও দুস্থ সহায়তা কার্যক্রম পর্যবেক্ষণ", Icons.Default.Favorite, MosqueEmeraldPrimary, OperationCategory.WELFARE_EVENTS, welfareBadge),
            OperationModuleEntry("madrasa", "মক্তব ও হিফজ শিক্ষা", "মসজিদভিত্তিক শিক্ষা ও শিক্ষার্থীদের অগ্রগতি", Icons.Default.School, MosqueEmeraldPrimary, OperationCategory.EDUCATION_STAFF, madrasaBadge),
            OperationModuleEntry("roster", "ওয়াক্ত ও নামাজের দায়িত্ব রোস্টার", "ইমাম, মুয়াজ্জিন ও খাদেমের ডিউটি সূচি", Icons.Default.EventNote, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, rosterBadge),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির সকল পদ ও দায়িত্বের বিস্তারিত তালিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO),
            OperationModuleEntry("prayer", "পূর্ণ নামাজের সময়সূচি ও ওয়াক্ত", "আজান ও জামাতের বিস্তারিত সূচি ও ক্যালেন্ডার", Icons.Default.AccessTime, MosqueEmeraldLight, OperationCategory.INFO)
        )
        UserRole.ADVISORY_BOARD -> listOf(
            OperationModuleEntry("meetings", "কমিটি সভা ও এজেন্ডা", "কমিটি সভার আলোচ্যসূচি ও রেজুলেশন পর্যবেক্ষণ", Icons.Default.EventAvailable, MosqueGoldDeep, OperationCategory.ADMIN, meetingsBadge, counts.upcomingMeetingsCount > 0),
            OperationModuleEntry("audit", "স্বচ্ছতা ও সিস্টেম অডিট লগ", "সকল আর্থিক অনুমোদন ও তহবিলের স্বচ্ছ অডিট পর্যবেক্ষণ", Icons.Default.FactCheck, MosqueWarningAmber, OperationCategory.FINANCE, auditBadge),
            OperationModuleEntry("members", "মসজিদ কমিটি ও সদস্য পরিষদ", "সকল স্তরের দায়িত্বশীল ও সাধারণ সদস্যদের পরিচিতি", Icons.Default.Groups, MosqueEmeraldPrimary, OperationCategory.ADMIN),
            OperationModuleEntry("office_docs", "ওয়াকফ দলিল, চুক্তি ও নথি", "মসজিদের জমি, চুক্তিপত্র ও দাপ্তরিক নথিপত্র পর্যবেক্ষণ", Icons.Default.FolderShared, RoleOfficeAccent, OperationCategory.ADMIN, docsBadge, counts.pendingDocsCount > 0),
            OperationModuleEntry("cashbox", "দান বাক্স (ক্যাশবক্স) ও ব্যাংক জমা", "সাপ্তাহিক ও মাসিক ক্যাশবক্স গণনা এবং ব্যাংক জমার স্বচ্ছ রেকর্ড", Icons.Default.Inventory2, MosqueGoldDeep, OperationCategory.FINANCE, cashboxBadge),
            OperationModuleEntry("events", "উন্নয়ন প্রকল্প, মাহফিল ও অনুষ্ঠান", "মসজিদ সম্প্রসারণ, ওয়াজ ও বিশেষ আয়োজন তদারকি", Icons.Default.Celebration, MosqueEmeraldPrimary, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("welfare", "সমাজকল্যাণ, যাকাত ও ত্রাণ", "যাকাত বিতরণ, মেডিকেল ক্যাম্প ও দুস্থ সহায়তা কার্যক্রম", Icons.Default.Favorite, MosqueSuccessGreen, OperationCategory.WELFARE_EVENTS, welfareBadge),
            OperationModuleEntry("madrasa", "মক্তব ও মাদরাসা শিক্ষা", "মসজিদভিত্তিক নূরানী ও হিফজ বিভাগের অগ্রগতি", Icons.Default.School, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, madrasaBadge),
            OperationModuleEntry("notices", "নোটিশ বোর্ড ও ঘোষণা", "কমিটি কর্তৃক প্রকাশিত সকল বিজ্ঞপ্তি", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.INFO),
            OperationModuleEntry("prayer", "পূর্ণ নামাজের সময়সূচি ও ওয়াক্ত", "আজান ও জামাতের বিস্তারিত সূচি ও ক্যালেন্ডার", Icons.Default.AccessTime, MosqueEmeraldLight, OperationCategory.INFO)
        )
        UserRole.IMAM -> listOf(
            OperationModuleEntry("meetings", "কমিটি সভা ও রেজুলেশন", "কমিটির সকল সিদ্ধান্ত ইসলামিক নীতিমালায় পর্যালোচনা", Icons.Default.EventAvailable, MosqueGoldDeep, OperationCategory.ADMIN, meetingsBadge, counts.upcomingMeetingsCount > 0),
            OperationModuleEntry("events", "মাহফিল, খুতবা ও তালিম আয়োজন", "বাৎসরিক ওয়াজ, শবে কদর ও দ্বীনি অনুষ্ঠান পরিচালনা", Icons.Default.Celebration, MosqueEmeraldPrimary, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("welfare", "সমাজকল্যাণ ও যাকাত নির্দেশনা", "যাকাত, ফেতরা ও দুস্থ সহায়তায় শরিয়াহ সমন্বয়", Icons.Default.Favorite, MosqueSuccessGreen, OperationCategory.WELFARE_EVENTS, welfareBadge),
            OperationModuleEntry("notices", "নোটিশ বোর্ড ও ঘোষণা", "মসজিদের জরুরি বিজ্ঞপ্তি ও সাধারণ নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.ADMIN),
            OperationModuleEntry("food", "ইমাম ও খাদেমদের খাবার শিডিউল", "দৈনিক দুপুর ও রাতের খাবার বণ্টন ও দাতাদের তালিকা", Icons.Default.Restaurant, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, foodBadge),
            OperationModuleEntry("staff", "আমার বেতন ও সার্ভিস বিবরণী", "পেশ ইমামের মাসিক বেতন স্লিপ ও খেদমত রেকর্ড", Icons.Default.Badge, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, staffBadge),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির সকল পদ ও দায়িত্বের বিস্তারিত তালিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO),
            OperationModuleEntry("prayer", "পূর্ণ নামাজের সময়সূচি ও ওয়াক্ত", "আজান ও জামাতের বিস্তারিত সূচি ও ক্যালেন্ডার", Icons.Default.AccessTime, MosqueEmeraldLight, OperationCategory.INFO)
        )
        UserRole.MUAZZIN -> listOf(
            OperationModuleEntry("prayer", "পূর্ণ নামাজের সময়সূচি ও আজান", "৫ ওয়াক্ত আজান ও জামাতের বিস্তারিত সূচি", Icons.Default.AccessTime, MosqueEmeraldPrimary, OperationCategory.INFO),
            OperationModuleEntry("roster", "ওয়াক্ত ও নামাজের দায়িত্ব বণ্টন", "ইমাম ও মুয়াজ্জিনদের ৫ ওয়াক্তের ডিউটি রোস্টার", Icons.Default.EventNote, MosqueGoldDeep, OperationCategory.EDUCATION_STAFF, rosterBadge),
            OperationModuleEntry("food", "ইমাম ও মুয়াজ্জিনের খাবার শিডিউল", "দৈনিক দুপুর ও রাতের খাবার বণ্টন ও দাতাদের তালিকা", Icons.Default.Restaurant, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, foodBadge),
            OperationModuleEntry("madrasa", "মক্তব ও তালিম সহায়তা", "মক্তব ও নূরানী বিভাগের পাঠদান ও হাজিরা", Icons.Default.School, MosqueEmeraldDark, OperationCategory.EDUCATION_STAFF, madrasaBadge)
        )
        UserRole.KHADEM -> listOf(
            OperationModuleEntry("food", "খাদেম ও স্টাফ খাবার শিডিউল", "দৈনিক দুপুর ও রাতের খাবার বণ্টন ও দাতাদের তালিকা", Icons.Default.Restaurant, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, foodBadge),
            OperationModuleEntry("roster", "মসজিদ খেদমত ও পরিচ্ছন্নতা রোস্টার", "অজুখানা, ফ্যান-লাইট ও জেনারেটর রক্ষণাবেক্ষণ সূচি", Icons.Default.EventNote, MosqueGoldDeep, OperationCategory.EDUCATION_STAFF, rosterBadge),
            OperationModuleEntry("notices", "মসজিদ নোটিশ ও নির্দেশনা", "কমিটি কর্তৃক প্রকাশিত নির্দেশনা ও বিজ্ঞপ্তি", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.ADMIN),
            OperationModuleEntry("prayer", "পূর্ণ নামাজের সময়সূচি ও ওয়াক্ত", "আজান ও জামাতের বিস্তারিত সূচি", Icons.Default.AccessTime, MosqueEmeraldLight, OperationCategory.INFO)
        )
        UserRole.GENERAL_MEMBER -> listOf(
            OperationModuleEntry("events", "মাহফিল ও বিশেষ অনুষ্ঠান", "বাৎসরিক ওয়াজ, ইফতার মাহফিল ও বিশেষ দ্বীনি আয়োজন", Icons.Default.Celebration, MosqueGoldDeep, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("welfare", "সমাজকল্যাণ ও যাকাত আবেদন", "যাকাত, চিকিৎসা ও দুস্থ সহায়তার জন্য আবেদনপত্র দাখিল", Icons.Default.Favorite, MosqueEmeraldPrimary, OperationCategory.WELFARE_EVENTS, welfareBadge),
            OperationModuleEntry("food", "ইমাম-খাদেম খাবার পালা তালিকা", "সদস্যদের দৈনিক মেহমানদারি ও খাবারের পালা দেখুন", Icons.Default.Restaurant, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, foodBadge),
            OperationModuleEntry("madrasa", "মক্তব ও হিফজ বিভাগ পরিচিতি", "মসজিদভিত্তিক নূরানী ও হিফজ শিক্ষা কার্যক্রম দেখুন", Icons.Default.School, MosqueEmeraldPrimary, OperationCategory.EDUCATION_STAFF, madrasaBadge),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "মসজিদ পরিচালনা কমিটির সকল দায়িত্বশীল ও তাদের কর্তব্য", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO),
            OperationModuleEntry("prayer", "পূর্ণ নামাজের সময়সূচি ও ওয়াক্ত", "আজান ও জামাতের বিস্তারিত সূচি ও ক্যালেন্ডার", Icons.Default.AccessTime, MosqueEmeraldLight, OperationCategory.INFO)
        )
        UserRole.VICE_PRESIDENT -> listOf(
            OperationModuleEntry("events", "উন্নয়ন প্রকল্প, মাহফিল ও অনুষ্ঠান", "বিশেষ উপ-কমিটি, নির্মাণ ও ধর্মীয় অনুষ্ঠান তদারকি", Icons.Default.Engineering, MosqueGoldDeep, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("notices", "নোটিশ বোর্ড ও ঘোষণা", "মসজিদের জরুরি বিজ্ঞপ্তি ও সাধারণ নোটিশ পরিচালনা", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.ADMIN),
            OperationModuleEntry("cashbox", "দান বাক্স (ক্যাশবক্স) ও ব্যাংক জমা", "ক্যাশবক্স গণনা, সাক্ষী নিশ্চিতকরণ ও ব্যাংক জমার অনুমোদন", Icons.Default.Inventory2, MosqueGoldDeep, OperationCategory.FINANCE, cashboxBadge, counts.pendingCashboxCount > 0),
            OperationModuleEntry("chanda", "চাঁদা আদায় ও রসিদ বহি", "সকল পরিশোধিত চাঁদার তালিকা ও ডিজিটাল কিউআর রসিদ", Icons.Default.ReceiptLong, MosqueEmeraldPrimary, OperationCategory.FINANCE, chandaBadge),
            OperationModuleEntry("staff", "ইমাম ও কর্মচারী বেতন ও পে-রোল", "পেশ ইমাম, মুয়াজ্জিন ও খাদেমদের বেতন বিতরণ ও সার্ভিস রেকর্ড", Icons.Default.Badge, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, staffBadge),
            OperationModuleEntry("office_docs", "দপ্তর, দলিল-চুক্তি ও চিঠিপত্র", "ওয়াকফ দলিল, চুক্তি ও আগত-প্রেরিত চিঠিপত্র তদারকি", Icons.Default.FolderShared, RoleOfficeAccent, OperationCategory.ADMIN, docsBadge, counts.pendingDocsCount > 0),
            OperationModuleEntry("sharia_advisory", "শরিয়াহ পর্যালোচনা ও উপদেষ্টা পরিষদ", "ইমামের শরিয়াহ মতামত ও উপদেষ্টা পরিষদের পরামর্শ পর্যালোচনা", Icons.Default.AccountBalance, RoleAdvisoryAccent, OperationCategory.ADMIN, advisoryBadge, counts.activeAdvisoryCount > 0),
            OperationModuleEntry("welfare", "সমাজকল্যাণ ও যাকাত তহবিল", "যাকাত ও ফেতরা বিতরণ, মেডিকেল ক্যাম্প এবং ত্রাণ অনুমোদন", Icons.Default.Favorite, MosqueEmeraldPrimary, OperationCategory.WELFARE_EVENTS, welfareBadge),
            OperationModuleEntry("madrasa", "মাদরাসা ও মক্তব পরিচালনা", "ছাত্র ভর্তি, দৈনিক হাজিরা ও মাদরাসা তদারকি", Icons.Default.School, MosqueEmeraldPrimary, OperationCategory.EDUCATION_STAFF, madrasaBadge),
            OperationModuleEntry("audit", "স্বচ্ছতা ও সিস্টেম অডিট লগ", "সকল আর্থিক অনুমোদন ও ডাটা পরিবর্তনের অপরিবর্তনীয় রেকর্ড", Icons.Default.FactCheck, MosqueWarningAmber, OperationCategory.FINANCE, auditBadge),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও তদারকি", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        else -> listOf(
            // PRESIDENT
            OperationModuleEntry("meetings", "কমিটি সভা ও এজেন্ডা রেজুলেশন", "কমিটির সকল সাধারণ ও জরুরি সভায় সভাপতিত্ব এবং সিদ্ধান্ত চূড়ান্তকরণ", Icons.Default.Event, MosqueGoldDeep, OperationCategory.ADMIN, meetingsBadge, counts.upcomingMeetingsCount > 0),
            OperationModuleEntry("cashbox", "দান বাক্স (ক্যাশবক্স) ও ব্যাংক জমা", "ক্যাশবক্স গণনা, সাক্ষী নিশ্চিতকরণ ও ব্যাংক জমার অনুমোদন", Icons.Default.Inventory2, MosqueGoldDeep, OperationCategory.FINANCE, cashboxBadge, counts.pendingCashboxCount > 0),
            OperationModuleEntry("chanda", "চাঁদা আদায় ও রসিদ বহি", "সকল পরিশোধিত চাঁদার তালিকা ও ডিজিটাল কিউআর রসিদ", Icons.Default.ReceiptLong, MosqueEmeraldPrimary, OperationCategory.FINANCE, chandaBadge),
            OperationModuleEntry("staff", "ইমাম ও কর্মচারী বেতন ও পে-রোল", "পেশ ইমাম, মুয়াজ্জিন ও খাদেমদের বেতন বিতরণ ও সার্ভিস রেকর্ড", Icons.Default.Badge, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, staffBadge),
            OperationModuleEntry("office_docs", "দপ্তর, দলিল-চুক্তি ও চিঠিপত্র", "ওয়াকফ দলিল, চুক্তি ও আগত-প্রেরিত চিঠিপত্রের তদারকি", Icons.Default.FolderShared, RoleOfficeAccent, OperationCategory.ADMIN, docsBadge, counts.pendingDocsCount > 0),
            OperationModuleEntry("madrasa", "মাদরাসা ও মক্তব পরিচালনা", "ছাত্র ভর্তি, দৈনিক হাজিরা, হিফজ ট্র্যাকার ও মাদরাসা ফি তদারকি", Icons.Default.School, MosqueEmeraldPrimary, OperationCategory.EDUCATION_STAFF, madrasaBadge),
            OperationModuleEntry("sharia_advisory", "শরিয়াহ পর্যালোচনা ও উপদেষ্টা পরিষদ", "ইমামের শরিয়াহ ফতোয়া/মতামত ও উপদেষ্টা পরিষদের দিকনির্দেশনা", Icons.Default.AccountBalance, RoleAdvisoryAccent, OperationCategory.ADMIN, advisoryBadge, counts.activeAdvisoryCount > 0),
            OperationModuleEntry("publicity", "প্রচার, দেয়াল উক্তি ও স্মার্ট বোর্ড", "মসজিদের দেয়াল লিখন/উক্তি ও স্মার্ট নোটিশ বোর্ড পরিচালনা", Icons.Default.ViewCarousel, RolePublicityAccent, OperationCategory.ADMIN, publicityBadge),
            OperationModuleEntry("audit", "স্বচ্ছতা ও সিস্টেম অডিট লগ", "সকল আর্থিক অনুমোদন ও ডাটা পরিবর্তনের অপরিবর্তনীয় রেকর্ড", Icons.Default.FactCheck, MosqueWarningAmber, OperationCategory.ADMIN, auditBadge),
            OperationModuleEntry("roster", "ওয়াক্ত ও নামাজের দায়িত্ব বণ্টন", "ইমাম ও মুয়াজ্জিনদের ৫ ওয়াক্তের ডিউটি রোস্টার ও পরিচালনা", Icons.Default.EventNote, MosqueGoldDeep, OperationCategory.EDUCATION_STAFF, rosterBadge),
            OperationModuleEntry("food", "ইমাম ও খাদেমদের খাবার শিডিউল", "দৈনিক দুপুর ও রাতের খাবার বণ্টন ও দায়িত্বপ্রাপ্ত দাতাদের তালিকা", Icons.Default.Restaurant, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, foodBadge),
            OperationModuleEntry("welfare", "সমাজকল্যাণ ও যাকাত তহবিল", "যাকাত ও ফেতরা বিতরণ, মেডিকেল ক্যাম্প এবং ত্রাণ অনুমোদন", Icons.Default.Favorite, MosqueEmeraldPrimary, OperationCategory.WELFARE_EVENTS, welfareBadge),
            OperationModuleEntry("events", "মাহফিল ও বিশেষ অনুষ্ঠান", "বাৎসরিক ওয়াজ, শবে কদর, ইফতার মাহফিল ও বিশেষ আয়োজন", Icons.Default.Campaign, MosqueGoldDeep, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও তদারকি", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
    }
}
