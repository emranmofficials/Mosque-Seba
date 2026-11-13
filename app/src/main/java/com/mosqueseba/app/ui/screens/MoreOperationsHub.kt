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
            .background(MosqueBgLight)
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
            color = Color.White,
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
                            tint = MosqueEmeraldPrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "অনুসন্ধান মুছুন",
                                    tint = MosqueTextMuted
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MosqueEmeraldPrimary,
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
            modifier = Modifier.weight(1f)
        ) {
            if (filteredModules.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, MosqueCardBorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MosqueEmeraldPrimary.copy(alpha = 0.1f),
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = null,
                                        tint = MosqueEmeraldPrimary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "কোনো মডিউল পাওয়া যায়নি",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "অনুসন্ধান বা ক্যাটাগরি ফিল্টার পরিবর্তন করে চেষ্টা করুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = MosqueTextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedCategory = OperationCategory.ALL
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("ফিল্টার মুছুন", color = MosqueEmeraldPrimary, fontWeight = FontWeight.Bold)
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
                    .background(MosqueEmeraldPrimary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MosqueEmeraldDark
            )
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MosqueEmeraldSoft
        ) {
            Text(
                text = badgeText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MosqueEmeraldPrimary,
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
    Card(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 104.dp)
            .testTag("more_quick_${module.key}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        color = if (module.badgeIsAlert) MosqueWarningAmber.copy(alpha = 0.14f) else MosqueEmeraldSoft
                    ) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (module.badgeIsAlert) MosqueWarningAmber else MosqueEmeraldPrimary,
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
                color = MosqueEmeraldDark,
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
        UserRole.TREASURER, UserRole.ASSISTANT_TREASURER -> listOf(
            OperationModuleEntry("staff", "ইমাম ও কর্মচারী বেতন ব্যবস্থাপনা", "পেশ ইমাম, মুয়াজ্জিন ও খাদেমদের বেতন বিতরণ ও পে-রোল", Icons.Default.Badge, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, staffBadge),
            OperationModuleEntry("madrasa", "মাদরাসা ও মক্তব ফি আদায়", "হিফজ ও মক্তব শিক্ষার্থীদের মাসিক বেতন ও ফি কালেকশন", Icons.Default.School, MosqueEmeraldPrimary, OperationCategory.EDUCATION_STAFF, madrasaBadge),
            OperationModuleEntry("notices", "নোটিশ বোর্ড ও ঘোষণা", "মসজিদের জরুরি বিজ্ঞপ্তি ও সাধারণ নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.ADMIN),
            OperationModuleEntry("audit", "স্বচ্ছতা ও সিস্টেম অডিট লগ", "সকল আর্থিক অনুমোদন ও ডাটা পরিবর্তনের অপরিবর্তনীয় রেকর্ড", Icons.Default.FactCheck, MosqueWarningAmber, OperationCategory.ADMIN, auditBadge),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির ১৩টি পদের দায়িত্ব ও পোর্টফোলিও নির্দেশিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.GENERAL_SECRETARY, UserRole.ASSISTANT_SECRETARY -> listOf(
            OperationModuleEntry("notices", "নোটিশ বোর্ড ও ঘোষণা", "জরুরি ও সাধারণ বিজ্ঞপ্তি প্রকাশ এবং পরিচালনা", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.ADMIN),
            OperationModuleEntry("welfare", "সমাজকল্যাণ, মেডিকেল ক্যাম্প ও যাকাত", "যাকাত ও ফেতরা বিতরণ, মেডিকেল ক্যাম্প এবং ত্রাণ তদারকি", Icons.Default.Favorite, MosqueEmeraldPrimary, OperationCategory.WELFARE_EVENTS, welfareBadge),
            OperationModuleEntry("events", "মাহফিল ও বিশেষ অনুষ্ঠান", "বাৎসরিক ওয়াজ, শবে কদর, ইফতার মাহফিল ও বিশেষ আয়োজন", Icons.Default.Celebration, MosqueGoldDeep, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("roster", "ওয়াক্ত ও নামাজের দায়িত্ব বণ্টন", "ইমাম ও মুয়াজ্জিনদের ডিউটি রোস্টার তদারকি", Icons.Default.EventNote, MosqueGoldDeep, OperationCategory.EDUCATION_STAFF, rosterBadge),
            OperationModuleEntry("food", "ইমাম ও খাদেমদের খাবার শিডিউল", "দৈনিক খাবার বণ্টন ও দাতাদের তালিকা", Icons.Default.Restaurant, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, foodBadge),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির সকল পদ ও দায়িত্বের বিস্তারিত তালিকা", Icons.Default.VerifiedUser, MosqueEmeraldPrimary, OperationCategory.INFO)
        )
        UserRole.ORGANIZING_SECRETARY -> listOf(
            OperationModuleEntry("meetings", "কমিটি সভা ও এজেন্ডা", "আসন্ন মিটিং নির্ধারণ ও পূর্ববর্তী রেজুলেশন", Icons.Default.Event, MosqueGoldDeep, OperationCategory.ADMIN, meetingsBadge, counts.upcomingMeetingsCount > 0),
            OperationModuleEntry("welfare", "সমাজকল্যাণ ও যাকাত তহবিল", "যাকাত ও ফেতরা বিতরণ, দুস্থ সহায়তা এবং চিকিৎসা অনুদান", Icons.Default.Favorite, MosqueEmeraldPrimary, OperationCategory.WELFARE_EVENTS, welfareBadge),
            OperationModuleEntry("events", "মাহফিল ও বিশেষ অনুষ্ঠান", "বাৎসরিক ওয়াজ, শবে কদর, ইফতার মাহফিল ও বিশেষ আয়োজন", Icons.Default.Campaign, MosqueGoldDeep, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির সকল পদ ও দায়িত্বের বিস্তারিত তালিকা", Icons.Default.VerifiedUser, MosqueEmeraldPrimary, OperationCategory.INFO),
            OperationModuleEntry("prayer", "পূর্ণ নামাজের সময়সূচি ও ওয়াক্ত", "আজান ও জামাতের বিস্তারিত সূচি ও ক্যালেন্ডার", Icons.Default.AccessTime, MosqueEmeraldLight, OperationCategory.INFO)
        )
        UserRole.PUBLICITY_SECRETARY -> listOf(
            OperationModuleEntry("publicity", "প্রচার, দেয়াল উক্তি ও স্মার্ট বোর্ড", "মসজিদের দেয়াল লিখন/উক্তি, দিকনির্দেশনা ও স্মার্ট বোর্ড প্রকাশনা", Icons.Default.ViewCarousel, RolePublicityAccent, OperationCategory.ADMIN, publicityBadge),
            OperationModuleEntry("events", "মাহফিল ও বিশেষ অনুষ্ঠান", "শবে কদর, শবে বরাত, ঈদে মিলাদুন্নবী ও ওয়াজ মাহফিল প্রচার", Icons.Default.Celebration, MosqueGoldDeep, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির সকল পদ ও দায়িত্বের বিস্তারিত তালিকা", Icons.Default.VerifiedUser, MosqueEmeraldPrimary, OperationCategory.INFO),
            OperationModuleEntry("prayer", "পূর্ণ নামাজের সময়সূচি ও ওয়াক্ত", "আজান ও জামাতের বিস্তারিত সূচি ও ক্যালেন্ডার", Icons.Default.AccessTime, MosqueEmeraldLight, OperationCategory.INFO)
        )
        UserRole.OFFICE_SECRETARY -> listOf(
            OperationModuleEntry("office_docs", "দপ্তর, দলিল-চুক্তি ও চিঠিপত্র", "ওয়াকফ দলিল, ইজারা চুক্তি, আগত চিঠি ও জবাব সংরক্ষণ", Icons.Default.FolderShared, RoleOfficeAccent, OperationCategory.ADMIN, docsBadge, counts.pendingDocsCount > 0),
            OperationModuleEntry("meetings", "কমিটি সভা ও রেজুলেশন", "মিটিংয়ের আলোচ্যসূচি ও রেজুলেশন খাতা", Icons.Default.Event, MosqueGoldDeep, OperationCategory.ADMIN, meetingsBadge, counts.upcomingMeetingsCount > 0),
            OperationModuleEntry("audit", "স্বচ্ছতা ও সিস্টেম অডিট লগ", "সকল নথিপত্র ও প্রশাসনিক কার্যক্রমের অডিট লগ", Icons.Default.FactCheck, MosqueWarningAmber, OperationCategory.ADMIN, auditBadge),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির সকল পদ ও দায়িত্বের বিস্তারিত তালিকা", Icons.Default.VerifiedUser, MosqueEmeraldPrimary, OperationCategory.INFO)
        )
        UserRole.WELFARE_EDUCATION_SECRETARY -> listOf(
            OperationModuleEntry("welfare", "সমাজকল্যাণ, মেডিকেল ক্যাম্প ও ত্রাণ", "যাকাত-সদকা বিতরণ, ফ্রি মেডিকেল ক্যাম্প ও দুর্যোগ ত্রাণ", Icons.Default.Favorite, RoleWelfareEduAccent, OperationCategory.WELFARE_EVENTS, welfareBadge),
            OperationModuleEntry("madrasa", "মক্তব, হিফজ ও নূরানী মাদরাসা", "ধর্মীয় শিক্ষা কার্যক্রম, ছাত্র হাজিরা ও হিফজ মূল্যায়ন", Icons.Default.School, MosqueEmeraldPrimary, OperationCategory.EDUCATION_STAFF, madrasaBadge),
            OperationModuleEntry("events", "মাহফিল ও বিশেষ অনুষ্ঠান", "ধর্মীয় ও শিক্ষামূলক প্রতিযোগিতা ও সেমিনার আয়োজন", Icons.Default.Campaign, MosqueGoldDeep, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির সকল পদ ও দায়িত্বের বিস্তারিত তালিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.EXECUTIVE_MEMBER -> listOf(
            OperationModuleEntry("notices", "নোটিশ বোর্ড ও ঘোষণা", "মসজিদের জরুরি বিজ্ঞপ্তি ও সাধারণ নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.ADMIN),
            OperationModuleEntry("events", "জুমা, ঈদ শৃঙ্খলা ও অনুষ্ঠান", "জামাত শৃঙ্খলা ও মাহফিল ব্যবস্থাপনায় সহায়তা", Icons.Default.Celebration, MosqueGoldDeep, OperationCategory.WELFARE_EVENTS, eventsBadge, counts.upcomingEventsCount > 0),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির সকল পদ ও দায়িত্বের বিস্তারিত তালিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.ADVISORY_BOARD -> listOf(
            OperationModuleEntry("sharia_advisory", "উপদেষ্টা পরিষদ কৌশলগত প্যানেল", "সংকটকালীন সমাধান ও সম্প্রসারণ/বহুতল নির্মাণ পরামর্শ", Icons.Default.AccountBalance, RoleAdvisoryAccent, OperationCategory.ADMIN, advisoryBadge, counts.activeAdvisoryCount > 0),
            OperationModuleEntry("audit", "স্বচ্ছতা ও সিস্টেম অডিট লগ", "সকল আর্থিক অনুমোদন ও তহবিলের স্বচ্ছ অডিট পর্যবেক্ষণ", Icons.Default.FactCheck, MosqueWarningAmber, OperationCategory.FINANCE, auditBadge),
            OperationModuleEntry("meetings", "কমিটি সভা ও এজেন্ডা", "কমিটি সভার আলোচ্যসূচি ও রেজুলেশন পর্যবেক্ষণ", Icons.Default.Event, MosqueGoldDeep, OperationCategory.ADMIN, meetingsBadge, counts.upcomingMeetingsCount > 0),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির সকল পদ ও দায়িত্বের বিস্তারিত তালিকা", Icons.Default.VerifiedUser, MosqueEmeraldPrimary, OperationCategory.INFO)
        )
        UserRole.IMAM -> listOf(
            OperationModuleEntry("notices", "নোটিশ বোর্ড ও ঘোষণা", "মসজিদের জরুরি বিজ্ঞপ্তি ও সাধারণ নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark, OperationCategory.ADMIN),
            OperationModuleEntry("food", "ইমাম ও খাদেমদের খাবার শিডিউল", "দৈনিক দুপুর ও রাতের খাবার বণ্টন ও দায়িত্বপ্রাপ্ত দাতাদের তালিকা", Icons.Default.Restaurant, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, foodBadge),
            OperationModuleEntry("staff", "ইমাম ও কর্মচারী বেতন বিবরণী", "মসজিদ কর্মচারীদের সার্ভিস রেকর্ড ও মাসিক বেতন পে-রোল", Icons.Default.Badge, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, staffBadge),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "৩-স্তরীয় কমিটির সকল পদ ও দায়িত্বের বিস্তারিত তালিকা", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO)
        )
        UserRole.MUAZZIN, UserRole.KHADEM -> listOf(
            OperationModuleEntry("roster", "ওয়াক্ত ও নামাজের দায়িত্ব বণ্টন", "ইমাম ও মুয়াজ্জিনদের ৫ ওয়াক্তের ডিউটি রোস্টার ও পরিচালনা", Icons.Default.EventNote, MosqueGoldDeep, OperationCategory.EDUCATION_STAFF, rosterBadge),
            OperationModuleEntry("food", "ইমাম ও খাদেমদের খাবার শিডিউল", "দৈনিক দুপুর ও রাতের খাবার বণ্টন ও দায়িত্বপ্রাপ্ত দাতাদের তালিকা", Icons.Default.Restaurant, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, foodBadge),
            OperationModuleEntry("madrasa", "মাদরাসা ও মক্তব পরিচালনা", "ছাত্র হাজিরা, হিফজ ট্র্যাকার ও তালিম মূল্যায়ন", Icons.Default.School, MosqueEmeraldPrimary, OperationCategory.EDUCATION_STAFF, madrasaBadge),
            OperationModuleEntry("staff", "ইমাম ও কর্মচারী বেতন বিবরণী", "মসজিদ কর্মচারীদের সার্ভিস রেকর্ড ও মাসিক বেতন পে-রোল", Icons.Default.Badge, MosqueInfoBlue, OperationCategory.EDUCATION_STAFF, staffBadge),
            OperationModuleEntry("prayer", "পূর্ণ নামাজের সময়সূচি ও ওয়াক্ত", "আজান ও জামাতের বিস্তারিত সূচি ও ক্যালেন্ডার", Icons.Default.AccessTime, MosqueEmeraldLight, OperationCategory.INFO)
        )
        UserRole.GENERAL_MEMBER -> listOf(
            OperationModuleEntry("chanda", "আমার পরিশোধিত চাঁদা ও রসিদ", "ব্যক্তিগত মাসিক চাঁদা ও ভেরিফাইড ডিজিটাল রসিদ", Icons.Default.ReceiptLong, MosqueEmeraldPrimary, OperationCategory.FINANCE, chandaBadge),
            OperationModuleEntry("welfare", "সমাজকল্যাণ ও যাকাত আবেদন", "যাকাত ও চিকিৎসা অনুদানের জন্য আবেদনপত্র দাখিল", Icons.Default.Favorite, MosqueEmeraldPrimary, OperationCategory.WELFARE_EVENTS),
            OperationModuleEntry("charter", "আদর্শ কমিটি সনদ ও দায়িত্ব", "মসজিদ পরিচালনা কমিটির সকল দায়িত্বশীল ও তাদের কর্তব্য", Icons.Default.VerifiedUser, MosqueEmeraldDark, OperationCategory.INFO),
            OperationModuleEntry("prayer", "পূর্ণ নামাজের সময়সূচি ও ওয়াক্ত", "আজান ও জামাতের বিস্তারিত সূচি ও ক্যালেন্ডার", Icons.Default.AccessTime, MosqueEmeraldLight, OperationCategory.INFO)
        )
        else -> listOf(
            // President & Vice President: Only features not present in Dashboard/Members/Finance/Notices tabs
            OperationModuleEntry("cashbox", "দান বাক্স (ক্যাশবক্স) ও ব্যাংক জমা", "ক্যাশবক্স গণনা, সাক্ষী নিশ্চিতকরণ ও ব্যাংক জমার অনুমোদন", Icons.Default.Inventory2, MosqueGoldDeep, OperationCategory.FINANCE, cashboxBadge, counts.pendingCashboxCount > 0),
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
