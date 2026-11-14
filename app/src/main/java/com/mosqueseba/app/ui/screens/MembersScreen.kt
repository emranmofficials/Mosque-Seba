package com.mosqueseba.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mosqueseba.app.data.local.entity.ChandaPaymentEntity
import com.mosqueseba.app.data.local.entity.MemberEntity
import com.mosqueseba.app.data.local.entity.StaffEntity
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.*
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel

private val SALARIED_ROLE_CODES = setOf("imam", "muazzin", "khadem")

private data class SalariedMemberUiModel(
    val key: String,
    val staff: StaffEntity,
    val member: MemberEntity?,
    val nameBn: String,
    val roleCode: String,
    val designationBn: String,
    val mobile: String,
    val joinDate: String,
    val salaryAmount: Double,
    val mealRequired: Boolean,
    val photoUrl: String?,
    val isPaidCurrentCycle: Boolean,
    val paidCount: Int,
    val totalPaidSalary: Double
)

@Composable
fun MembersScreen(
    viewModel: MosqueViewModel,
    onAddMemberClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val allMembers by viewModel.allMembers.collectAsState()
    val allChandaPayments by viewModel.allChandaPayments.collectAsState()
    val activeStaff by viewModel.activeStaff.collectAsState()
    val allPayrolls by viewModel.allPayrolls.collectAsState()
    val mosque by viewModel.mosque.collectAsState()
    val searchQuery by viewModel.memberSearchQuery.collectAsState()
    val selectedRoleFilter by viewModel.selectedRoleFilter.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) } // 0 = Chanda Members, 1 = Salaried Staff
    var onlyDuesFilter by rememberSaveable { mutableStateOf(false) }

    var activeProfileTarget by remember { mutableStateOf<MemberProfileTarget?>(null) }
    var chandaTargetMember by remember { mutableStateOf<Pair<MemberEntity, Double>?>(null) }
    var dueBreakdownMember by remember { mutableStateOf<MemberEntity?>(null) }
    var depositHistoryMember by remember { mutableStateOf<MemberEntity?>(null) }
    var receiptPreviewPayment by remember { mutableStateOf<ChandaPaymentEntity?>(null) }
    var paySalaryTargetStaff by remember { mutableStateOf<StaffEntity?>(null) }

    val activeCycleMonth = remember(allChandaPayments) {
        ChandaMonthUtils.resolveLatestSingleMonth(
            allChandaPayments.map { it.month },
            "সেপ্টেম্বর ২০২৪"
        )
    }

    // Partition Chanda-paying members (non-staff)
    val chandaMembersAll = remember(allMembers) {
        allMembers.filter { it.roleCode.lowercase() !in SALARIED_ROLE_CODES }
    }

    // Build unified Salaried Staff list combining StaffEntity + any staff-role MemberEntity
    val salariedMembersAll = remember(activeStaff, allMembers, allPayrolls, activeCycleMonth) {
        val result = mutableListOf<SalariedMemberUiModel>()
        val matchedMemberIds = mutableSetOf<String>()

        activeStaff.forEach { st ->
            val normStaffMobile = st.mobile.filter { it.isDigit() }
            val linkedMember = allMembers.find { m ->
                st.id == "staff-${m.id}" ||
                    (normStaffMobile.isNotEmpty() && m.mobile.filter { it.isDigit() } == normStaffMobile) ||
                    m.nameBn.trim() == st.nameBn.trim()
            }
            if (linkedMember != null) matchedMemberIds.add(linkedMember.id)
            val staffPayrolls = allPayrolls.filter { it.staffId == st.id }
            val isPaid = staffPayrolls.any { it.month == activeCycleMonth && it.paymentStatus == "paid" }
            val totalPaid = staffPayrolls.filter { it.paymentStatus == "paid" }.sumOf { it.netSalary }
            result.add(
                SalariedMemberUiModel(
                    key = "staff-${st.id}",
                    staff = st,
                    member = linkedMember,
                    nameBn = st.nameBn,
                    roleCode = st.roleCode,
                    designationBn = st.designationBn,
                    mobile = st.mobile,
                    joinDate = st.joinDate,
                    salaryAmount = st.salaryAmount,
                    mealRequired = st.mealRequired,
                    photoUrl = linkedMember?.photoUrl,
                    isPaidCurrentCycle = isPaid,
                    paidCount = staffPayrolls.size,
                    totalPaidSalary = totalPaid
                )
            )
        }

        allMembers
            .filter { it.roleCode.lowercase() in SALARIED_ROLE_CODES && it.id !in matchedMemberIds }
            .forEach { m ->
                val role = UserRole.fromCode(m.roleCode)
                val fallbackSalary = when (m.roleCode.lowercase()) {
                    "imam" -> 18000.0
                    "muazzin" -> 12000.0
                    else -> 9000.0
                }
                val fallbackStaff = StaffEntity(
                    id = "staff-${m.id}",
                    nameBn = m.nameBn,
                    roleCode = m.roleCode,
                    designationBn = role.titleBn,
                    joinDate = m.joinDate,
                    salaryAmount = fallbackSalary,
                    mealRequired = true,
                    mobile = m.mobile,
                    isActive = m.status == "active"
                )
                val staffPayrolls = allPayrolls.filter { it.staffId == fallbackStaff.id }
                val isPaid = staffPayrolls.any { it.month == activeCycleMonth && it.paymentStatus == "paid" }
                val totalPaid = staffPayrolls.filter { it.paymentStatus == "paid" }.sumOf { it.netSalary }
                result.add(
                    SalariedMemberUiModel(
                        key = "mem-staff-${m.id}",
                        staff = fallbackStaff,
                        member = m,
                        nameBn = m.nameBn,
                        roleCode = m.roleCode,
                        designationBn = role.titleBn,
                        mobile = m.mobile,
                        joinDate = m.joinDate,
                        salaryAmount = fallbackSalary,
                        mealRequired = true,
                        photoUrl = m.photoUrl,
                        isPaidCurrentCycle = isPaid,
                        paidCount = staffPayrolls.size,
                        totalPaidSalary = totalPaid
                    )
                )
            }
        result
    }

    // Precompute Chanda paid/due per member
    val memberChandaStats = remember(chandaMembersAll, allChandaPayments, activeCycleMonth) {
        chandaMembersAll.associate { member ->
            val payments = allChandaPayments.filter { it.memberId == member.id }
            val paidCycle = payments.sumOf { ChandaMonthUtils.amountForMonth(it, activeCycleMonth) }
            val totalDeposited = payments.sumOf { it.amountPaid }
            val due = (member.monthlyChanda - paidCycle).coerceAtLeast(0.0)
            member.id to Triple(paidCycle, totalDeposited, due)
        }
    }

    val totalChandaDeposited = remember(memberChandaStats) {
        memberChandaStats.values.sumOf { it.second }
    }
    val totalChandaDue = remember(memberChandaStats) {
        memberChandaStats.values.sumOf { it.third }
    }
    val totalMonthlyPayroll = remember(salariedMembersAll) {
        salariedMembersAll.sumOf { it.salaryAmount }
    }
    val mealWillingCount = remember(allMembers) {
        allMembers.count { it.willingToProvideStaffMeals }
    }

    // Filtered Chanda Members
    val filteredChandaMembers = remember(
        chandaMembersAll,
        searchQuery,
        selectedRoleFilter,
        onlyDuesFilter,
        memberChandaStats
    ) {
        chandaMembersAll.filter { member ->
            val matchesQuery = searchQuery.isBlank() ||
                member.nameBn.contains(searchQuery, ignoreCase = true) ||
                member.nameEn.contains(searchQuery, ignoreCase = true) ||
                member.memberCode.contains(searchQuery, ignoreCase = true) ||
                member.mobile.contains(searchQuery)
            val matchesRole = selectedRoleFilter == null ||
                selectedRoleFilter in SALARIED_ROLE_CODES ||
                member.roleCode.equals(selectedRoleFilter, ignoreCase = true)
            val due = memberChandaStats[member.id]?.third ?: 0.0
            val matchesDue = !onlyDuesFilter || due > 0.0
            matchesQuery && matchesRole && matchesDue
        }
    }

    // Filtered Salaried Members
    val filteredSalariedMembers = remember(salariedMembersAll, searchQuery, selectedRoleFilter) {
        salariedMembersAll.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                item.nameBn.contains(searchQuery, ignoreCase = true) ||
                item.designationBn.contains(searchQuery, ignoreCase = true) ||
                item.mobile.contains(searchQuery)
            val matchesRole = selectedRoleFilter == null ||
                selectedRoleFilter !in SALARIED_ROLE_CODES ||
                item.roleCode.equals(selectedRoleFilter, ignoreCase = true)
            matchesQuery && matchesRole
        }
    }

    // If a member profile page is active, show the Full-Screen Profile Page
    val currentProfile = activeProfileTarget
    if (currentProfile != null) {
        MemberProfileScreen(
            target = currentProfile,
            viewModel = viewModel,
            activeCycleMonth = activeCycleMonth,
            onBack = { activeProfileTarget = null },
            onOpenReceiveChanda = { member, suggestedAmt ->
                chandaTargetMember = member to suggestedAmt
            },
            onOpenPaySalary = { staff ->
                paySalaryTargetStaff = staff
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Islamic Emerald Header + 2-Tab Switcher + Dynamic KPI Summary
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
                                    text = "সদস্য পরিষদ ও খেদমত তালিকা",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (currentRole.canManageFinance() || currentRole.canManageMembers()) {
                                        "${currentRole.titleBn} প্যানেল • সদস্য চাঁদা সংগ্রহ, রসিদ ইস্যু ও স্টাফ বেতন ছাড়পত্র"
                                    } else {
                                        "${currentRole.titleBn} ভিউ • মাসিক চাঁদা দাতা ও খেদমতকারী স্টাফ তালিকা"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MosqueGoldLight.copy(alpha = 0.9f)
                                )
                            }

                            if (onAddMemberClick != null) {
                                Button(
                                    onClick = onAddMemberClick,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MosqueGold,
                                        contentColor = MosqueEmeraldDark
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .heightIn(min = 42.dp)
                                        .testTag("add_member_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = "নতুন সদস্য",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "নতুন সদস্য",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Segmented 2-Tab Switcher (Gold-bordered pill)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.Black.copy(alpha = 0.22f),
                            border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.55f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                MemberCategoryTabButton(
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("tab_chanda_members"),
                                    selected = selectedTabIndex == 0,
                                    title = "মাসিক চাঁদা দাতা",
                                    countText = toBengaliDigits(chandaMembersAll.size.toString()),
                                    icon = Icons.Default.Groups,
                                    onClick = {
                                        selectedTabIndex = 0
                                        if (selectedRoleFilter in SALARIED_ROLE_CODES) {
                                            viewModel.setSelectedRoleFilter(null)
                                        }
                                    }
                                )
                                MemberCategoryTabButton(
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("tab_salaried_members"),
                                    selected = selectedTabIndex == 1,
                                    title = "বেতনভুক্ত সদস্য",
                                    countText = toBengaliDigits(salariedMembersAll.size.toString()),
                                    icon = Icons.Default.Badge,
                                    onClick = {
                                        selectedTabIndex = 1
                                        onlyDuesFilter = false
                                        if (selectedRoleFilter != null && selectedRoleFilter !in SALARIED_ROLE_CODES) {
                                            viewModel.setSelectedRoleFilter(null)
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dynamic 3-Metric Summary Strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (selectedTabIndex == 0) {
                                HeaderSummaryPill(
                                    modifier = Modifier.weight(1f),
                                    label = "মোট দাতা",
                                    value = "${toBengaliDigits(chandaMembersAll.size.toString())} জন",
                                    accent = MosqueGoldLight
                                )
                                HeaderSummaryPill(
                                    modifier = Modifier.weight(1f),
                                    label = "মোট জমা",
                                    value = "৳${toBengaliDigits(String.format("%,.0f", totalChandaDeposited))}",
                                    accent = Color(0xFF6EE7B7)
                                )
                                HeaderSummaryPill(
                                    modifier = Modifier.weight(1f),
                                    label = "মোট বকেয়া",
                                    value = "৳${toBengaliDigits(String.format("%,.0f", totalChandaDue))}",
                                    accent = if (totalChandaDue > 0) Color(0xFFFCD34D) else Color(0xFF6EE7B7)
                                )
                            } else {
                                HeaderSummaryPill(
                                    modifier = Modifier.weight(1f),
                                    label = "মোট স্টাফ",
                                    value = "${toBengaliDigits(salariedMembersAll.size.toString())} জন",
                                    accent = MosqueGoldLight
                                )
                                HeaderSummaryPill(
                                    modifier = Modifier.weight(1f),
                                    label = "মাসিক বেতন",
                                    value = "৳${toBengaliDigits(String.format("%,.0f", totalMonthlyPayroll))}",
                                    accent = Color(0xFF6EE7B7)
                                )
                                HeaderSummaryPill(
                                    modifier = Modifier.weight(1f),
                                    label = "খাবার দাতা",
                                    value = "${toBengaliDigits(mealWillingCount.toString())} জন",
                                    accent = MosqueGoldLight
                                )
                            }
                        }
                        if (selectedTabIndex == 0) {
                            val collectedThisCycle = remember(memberChandaStats) {
                                memberChandaStats.values.sumOf { it.first }
                            }
                            val targetThisCycle = (collectedThisCycle + totalChandaDue).coerceAtLeast(1.0)
                            val chandaRatio = (collectedThisCycle / targetThisCycle).toFloat().coerceIn(0f, 1f)
                            val chandaPct = (chandaRatio * 100).toInt().coerceIn(0, 100)

                            Spacer(modifier = Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "চলতি মাসের চাঁদা আদায় অগ্রগতি",
                                        fontSize = 10.5.sp,
                                        color = MosqueGoldLight.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${toBengaliDigits(chandaPct.toString())}% আদায়",
                                        fontSize = 10.5.sp,
                                        color = MosqueGold,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { chandaRatio },
                                    color = MosqueGold,
                                    trackColor = Color.White.copy(alpha = 0.18f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                            }
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

            // 2. Search & Role Filter Section
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
                        onValueChange = { viewModel.setMemberSearchQuery(it) },
                        placeholder = {
                            Text(
                                text = if (selectedTabIndex == 0) {
                                    "চাঁদা দাতা খুঁজুন (নাম, মোবাইল, আইডি)"
                                } else {
                                    "বেতনভুক্ত সদস্য খুঁজুন (নাম, পদবি, মোবাইল)"
                                },
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "অনুসন্ধান", tint = MosqueEmeraldPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setMemberSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "মুছুন", tint = MosqueTextMuted)
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
                            .testTag("member_search_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (selectedTabIndex == 0) {
                            item {
                                FilterChip(
                                    selected = selectedRoleFilter == null && !onlyDuesFilter,
                                    onClick = {
                                        viewModel.setSelectedRoleFilter(null)
                                        onlyDuesFilter = false
                                    },
                                    label = { Text("সকল (${toBengaliDigits(chandaMembersAll.size.toString())})", fontSize = 11.sp) }
                                )
                            }
                            item {
                                val dueCount = chandaMembersAll.count { (memberChandaStats[it.id]?.third ?: 0.0) > 0.0 }
                                FilterChip(
                                    selected = onlyDuesFilter,
                                    onClick = { onlyDuesFilter = !onlyDuesFilter },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.WarningAmber,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (onlyDuesFilter) Color.White else MosqueWarningAmber
                                        )
                                    },
                                    label = { Text("বকেয়া আছে (${toBengaliDigits(dueCount.toString())})", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MosqueWarningAmber,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            val nonStaffRoles = UserRole.entries.filter { it.code !in SALARIED_ROLE_CODES }
                            items(nonStaffRoles, key = { it.code }) { role ->
                                FilterChip(
                                    selected = selectedRoleFilter == role.code,
                                    onClick = {
                                        viewModel.setSelectedRoleFilter(
                                            if (selectedRoleFilter == role.code) null else role.code
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = role.roleIcon(),
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    },
                                    label = { Text(role.titleBn, fontSize = 11.sp) }
                                )
                            }
                        } else {
                            item {
                                FilterChip(
                                    selected = selectedRoleFilter == null,
                                    onClick = { viewModel.setSelectedRoleFilter(null) },
                                    label = { Text("সকল স্টাফ (${toBengaliDigits(salariedMembersAll.size.toString())})", fontSize = 11.sp) }
                                )
                            }
                            val staffRoles = listOf(UserRole.IMAM, UserRole.MUAZZIN, UserRole.KHADEM)
                            items(staffRoles, key = { it.code }) { role ->
                                FilterChip(
                                    selected = selectedRoleFilter == role.code,
                                    onClick = {
                                        viewModel.setSelectedRoleFilter(
                                            if (selectedRoleFilter == role.code) null else role.code
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = role.roleIcon(),
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    },
                                    label = { Text(role.titleBn, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // 3. Animated List Content
            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 840.dp),
                label = "MemberTabContent"
            ) { tab ->
                if (tab == 0) {
                    LazyColumn(
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (filteredChandaMembers.isEmpty()) {
                            item {
                                EmptyMemberState(message = "কোনো চাঁদা দাতা সদস্য পাওয়া যায়নি")
                            }
                        } else {
                            items(filteredChandaMembers, key = { it.id }) { member ->
                                val stats = memberChandaStats[member.id] ?: Triple(0.0, 0.0, member.monthlyChanda)
                                ChandaMemberCard(
                                    member = member,
                                    defaultMosqueAddress = mosque?.address ?: "",
                                    paidThisCycle = stats.first,
                                    totalDeposited = stats.second,
                                    dueAmount = stats.third,
                                    canManageFinance = currentRole.canManageFinance(),
                                    onCardClick = { activeProfileTarget = MemberProfileTarget.ChandaMember(member.id) },
                                    onDueClick = { dueBreakdownMember = member },
                                    onPaidClick = { depositHistoryMember = member },
                                    onCollectClick = {
                                        val suggested = if (stats.third > 0.0) stats.third else member.monthlyChanda
                                        chandaTargetMember = member to suggested
                                    },
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                        placementSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                    )
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (filteredSalariedMembers.isEmpty()) {
                            item {
                                EmptyMemberState(message = "কোনো বেতনভুক্ত সদস্য পাওয়া যায়নি")
                            }
                        } else {
                            items(filteredSalariedMembers, key = { it.key }) { staffItem ->
                                SalariedMemberCard(
                                    item = staffItem,
                                    canManageFinance = currentRole.canManageFinance(),
                                    onCardClick = {
                                        activeProfileTarget = MemberProfileTarget.SalariedStaff(
                                            staffId = staffItem.staff.id,
                                            memberId = staffItem.member?.id
                                        )
                                    },
                                    onPaySalaryClick = {
                                        paySalaryTargetStaff = staffItem.staff
                                    },
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                        placementSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Dialogs shared across MembersScreen & MemberProfileScreen ---

    // 1. Collect Chanda Dialog for Specific Member
    chandaTargetMember?.let { (member, suggestedAmt) ->
        ReceiveChandaDialog(
            members = allMembers,
            allPayments = allChandaPayments,
            preselectedMember = member,
            defaultMonth = activeCycleMonth,
            suggestedAmount = suggestedAmt,
            onDismiss = { chandaTargetMember = null },
            onConfirm = { targetMember, month, amount, method, monthCount ->
                viewModel.recordChandaPayment(
                    member = targetMember,
                    month = month,
                    amount = amount,
                    method = method,
                    selectedMonthCount = monthCount,
                    onRecorded = { createdPayment ->
                        receiptPreviewPayment = createdPayment
                        Toast.makeText(
                            context,
                            "৳${toBengaliDigits(amount.toInt().toString())} চাঁদা গৃহীত হয়েছে এবং স্বয়ংক্রিয়ভাবে মোট আয়ের সাথে যুক্ত হয়েছে।",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
                chandaTargetMember = null
            }
        )
    }

    // 2. Due Breakdown Dialog (on tapping "বকেয়া" button)
    dueBreakdownMember?.let { member ->
        val stats = memberChandaStats[member.id] ?: Triple(0.0, 0.0, member.monthlyChanda)
        MemberDueBreakdownDialog(
            member = member,
            activeCycleMonth = activeCycleMonth,
            paidThisCycle = stats.first,
            dueAmount = stats.third,
            canCollect = currentRole.canManageFinance(),
            onCollectNow = {
                dueBreakdownMember = null
                chandaTargetMember = member to (if (stats.third > 0) stats.third else member.monthlyChanda)
            },
            onDismiss = { dueBreakdownMember = null }
        )
    }

    // 3. Deposit History Dialog (on tapping "জমা" button)
    depositHistoryMember?.let { member ->
        val payments = allChandaPayments.filter { it.memberId == member.id }
        MemberDepositHistoryDialog(
            member = member,
            payments = payments,
            onReceiptClick = { payment ->
                depositHistoryMember = null
                receiptPreviewPayment = payment
            },
            onDismiss = { depositHistoryMember = null }
        )
    }

    // 4. Official Receipt Dialog
    receiptPreviewPayment?.let { payment ->
        OfficialReceiptDialog(
            payment = payment,
            mosqueName = mosque?.nameBn ?: "বায়তুল জান্নাত জামে মসজিদ",
            mosqueAddress = mosque?.address ?: "মিরপুর-১০, ঢাকা",
            committeeId = mosque?.committeeId ?: "MC-DHK-2024",
            mosqueLogoUrl = mosque?.logoUrl,
            onDismiss = { receiptPreviewPayment = null }
        )
    }

    // 5. Pay Staff Salary Dialog
    paySalaryTargetStaff?.let { staff ->
        PaySalaryFormDialog(
            staff = staff,
            defaultMonth = activeCycleMonth,
            onDismiss = { paySalaryTargetStaff = null },
            onConfirm = { month, bonus, paymentMethod ->
                viewModel.disburseSalary(
                    staff = staff,
                    month = month,
                    bonus = bonus,
                    paymentMethod = paymentMethod,
                    onDisbursed = { payroll ->
                        Toast.makeText(
                            context,
                            "${staff.nameBn}-এর বেতন ৳${toBengaliDigits(payroll.netSalary.toInt().toString())} পরিশোধিত এবং স্বয়ংক্রিয়ভাবে মোট ব্যয়ের সাথে যুক্ত হয়েছে।",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
                paySalaryTargetStaff = null
            }
        )
    }
}

@Composable
private fun MemberCategoryTabButton(
    modifier: Modifier = Modifier,
    selected: Boolean,
    title: String,
    countText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 44.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MosqueGold else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MosqueEmeraldDark else Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (selected) MosqueEmeraldDark else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                shape = CircleShape,
                color = if (selected) MosqueEmeraldDark else MosqueGold.copy(alpha = 0.25f)
            ) {
                Text(
                    text = countText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) MosqueGoldLight else MosqueGoldLight,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun HeaderSummaryPill(
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
                maxLines = 1
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
private fun ChandaMemberCard(
    member: MemberEntity,
    defaultMosqueAddress: String,
    paidThisCycle: Double,
    totalDeposited: Double,
    dueAmount: Double,
    canManageFinance: Boolean,
    onCardClick: () -> Unit,
    onDueClick: () -> Unit,
    onPaidClick: () -> Unit,
    onCollectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ChandaMemberCardScale"
    )
    val role = UserRole.fromCode(member.roleCode)
    val photoBitmap = remember(member.photoUrl) { loadLocalThumbnail(member.photoUrl, 160) }
    val isActive = member.status == "active"

    val isDark = LocalIsDarkTheme.current
    val softBadgeBg = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft
    val goldBadgeBg = if (isDark) MosqueNightSurfaceVariant else MosqueGoldSoft
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    Card(
        onClick = onCardClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .testTag("chanda_member_card_${member.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 5.dp),
        border = BorderStroke(1.dp, MosqueCardBorderGold)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Subtle gold top line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(IslamicGoldTrimGradient)
            )

            Column(modifier = Modifier.padding(12.dp)) {
                // Top Info Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Avatar
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MosqueEmeraldSoft)
                                .border(1.5.dp, MosqueGold.copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (photoBitmap != null) {
                                Image(
                                    bitmap = photoBitmap,
                                    contentDescription = member.nameBn,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = member.nameBn.firstOrNull()?.toString() ?: "স",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MosqueEmeraldDark
                                )
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = if (isActive) MosqueSuccessGreen else MosqueUrgentRed,
                            border = BorderStroke(1.5.dp, Color.White),
                            modifier = Modifier.size(12.dp)
                        ) {}
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = member.nameBn,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (role.level <= 2 && !isDark) MosqueGoldLight else softBadgeBg
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = role.roleIcon(),
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = role.titleBn,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = "আইডি: ${toBengaliDigits(member.memberCode)} • ${toBengaliDigits(member.mobile)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryColor,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val cardAddress = member.address.ifBlank { defaultMosqueAddress }.ifBlank { "প্রযোজ্য নয়" }
                            Text(
                                text = "ঠিকানা: $cardAddress",
                                style = MaterialTheme.typography.labelSmall,
                                color = secondaryColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (member.willingToProvideStaffMeals) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = goldBadgeBg
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Restaurant,
                                            contentDescription = null,
                                            tint = MosqueGoldDeep,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "খাবার দাতা",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueGoldDeep
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "বিস্তারিত প্রোফাইল",
                        tint = MosqueGoldDeep,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Action Strip: বকেয়া বাটন | জমা বাটন | চাঁদা গ্রহণ বাটন
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val hasDue = dueAmount > 0.0
                    // 1. বকেয়া button
                    Surface(
                        onClick = onDueClick,
                        shape = RoundedCornerShape(10.dp),
                        color = if (hasDue) MosqueUrgentRed.copy(alpha = 0.1f) else MosqueSuccessGreen.copy(alpha = 0.1f),
                        border = BorderStroke(
                            1.dp,
                            if (hasDue) MosqueUrgentRed.copy(alpha = 0.35f) else MosqueSuccessGreen.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 42.dp)
                            .testTag("member_due_button_${member.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "বকেয়া",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasDue) MosqueUrgentRed else MosqueSuccessGreen
                            )
                            Text(
                                text = "৳${toBengaliDigits(String.format("%,.0f", dueAmount))}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (hasDue) MosqueUrgentRed else MosqueSuccessGreen
                            )
                        }
                    }

                    // 2. জমা button
                    Surface(
                        onClick = onPaidClick,
                        shape = RoundedCornerShape(10.dp),
                        color = softBadgeBg,
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 42.dp)
                            .testTag("member_paid_button_${member.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "জমা",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = secondaryColor
                            )
                            Text(
                                text = "৳${toBengaliDigits(String.format("%,.0f", totalDeposited))}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = primaryColor
                            )
                        }
                    }

                    // 3. চাঁদা গ্রহণ button
                    if (canManageFinance) {
                        Button(
                            onClick = onCollectClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MosqueEmeraldPrimary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .heightIn(min = 42.dp)
                                .testTag("member_collect_button_${member.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "চাঁদা গ্রহণ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SalariedMemberCard(
    item: SalariedMemberUiModel,
    canManageFinance: Boolean,
    onCardClick: () -> Unit,
    onPaySalaryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val role = UserRole.fromCode(item.roleCode)
    val photoBitmap = remember(item.photoUrl) { loadLocalThumbnail(item.photoUrl, 160) }

    val isDark = LocalIsDarkTheme.current
    val softBadgeBg = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft
    val goldBadgeBg = if (isDark) MosqueNightSurfaceVariant else MosqueGoldSoft
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        onClick = onCardClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("salaried_member_card_${item.key}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 5.dp),
        border = BorderStroke(1.dp, MosqueCardBorderGold)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(IslamicGoldTrimGradient)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(softBadgeBg)
                            .border(1.5.dp, MosqueGold.copy(alpha = 0.8f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoBitmap != null) {
                            Image(
                                bitmap = photoBitmap,
                                contentDescription = item.nameBn,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = role.roleIcon(),
                                contentDescription = item.nameBn,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.nameBn,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDark) MosqueNightSurfaceVariant else MosqueGoldLight
                            ) {
                                Text(
                                    text = item.designationBn,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "মোবাইল: ${toBengaliDigits(item.mobile)} • যোগদান: ${toBengaliDigits(item.joinDate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryColor,
                            fontSize = 11.5.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "বিস্তারিত প্রোফাইল",
                        tint = MosqueGoldDeep,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = softBadgeBg,
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("মাসিক বেতন", fontSize = 10.sp, color = secondaryColor)
                            Text(
                                text = "৳${toBengaliDigits(String.format("%,.0f", item.salaryAmount))}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = primaryColor
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (item.isPaidCurrentCycle) MosqueSuccessGreen.copy(alpha = 0.12f) else goldBadgeBg,
                        border = BorderStroke(
                            1.dp,
                            if (item.isPaidCurrentCycle) MosqueSuccessGreen.copy(alpha = 0.35f) else MosqueCardBorderGold
                        ),
                        modifier = Modifier.weight(1.1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (item.totalPaidSalary > 0.0) {
                                    "পরিশোধিত (${toBengaliDigits(item.paidCount.toString())} বার)"
                                } else {
                                    "চলতি মাসের অবস্থা"
                                },
                                fontSize = 10.sp,
                                color = secondaryColor,
                                maxLines = 1
                            )
                            Text(
                                text = if (item.totalPaidSalary > 0.0) {
                                    "৳${toBengaliDigits(String.format("%,.0f", item.totalPaidSalary))}"
                                } else if (item.isPaidCurrentCycle) {
                                    "পরিশোধিত"
                                } else {
                                    "প্রদেয়"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (item.isPaidCurrentCycle || item.totalPaidSalary > 0.0) MosqueSuccessGreen else MosqueGoldDeep,
                                maxLines = 1
                            )
                        }
                    }

                    if (canManageFinance) {
                        Button(
                            onClick = onPaySalaryClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .heightIn(min = 48.dp)
                                .testTag("pay_salary_btn_${item.key}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("বেতন দিন", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMemberState(message: String) {
    MehrabArchCard(
        backgroundColor = Color.White,
        borderColor = MosqueCardBorderSubtle
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = MosqueTextMuted,
                modifier = Modifier.size(46.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MosqueTextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MemberDueBreakdownDialog(
    member: MemberEntity,
    activeCycleMonth: String,
    paidThisCycle: Double,
    dueAmount: Double,
    canCollect: Boolean,
    onCollectNow: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        MehrabArchCard(
            backgroundColor = Color.White,
            borderColor = MosqueCardBorderGold
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "বকেয়া চাঁদার বিবরণ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MosqueEmeraldDark
                )
                Text(
                    text = "${member.nameBn} (${toBengaliDigits(member.memberCode)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MosqueTextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MosqueCardBorderSubtle)
                Spacer(modifier = Modifier.height(10.dp))

                DueStatRow("হিসাবকৃত মাস", activeCycleMonth)
                DueStatRow("মাসিক নির্ধারিত চাঁদা", "৳${toBengaliDigits(String.format("%,.0f", member.monthlyChanda))}")
                DueStatRow("চলতি মাসে জমা", "৳${toBengaliDigits(String.format("%,.0f", paidThisCycle))}", MosqueSuccessGreen)
                DueStatRow(
                    label = "বর্তমান বকেয়া",
                    value = "৳${toBengaliDigits(String.format("%,.0f", dueAmount))}",
                    valueColor = if (dueAmount > 0) MosqueUrgentRed else MosqueSuccessGreen
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("বন্ধ করুন")
                    }
                    if (canCollect) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onCollectNow,
                            colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("চাঁদা গ্রহণ করুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberDepositHistoryDialog(
    member: MemberEntity,
    payments: List<ChandaPaymentEntity>,
    onReceiptClick: (ChandaPaymentEntity) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        MehrabArchCard(
            backgroundColor = Color.White,
            borderColor = MosqueCardBorderGold
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "চাঁদা জমার রসিদ তালিকা",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MosqueEmeraldDark
                )
                Text(
                    text = "${member.nameBn} • মোট ${toBengaliDigits(payments.size.toString())} টি রসিদ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MosqueTextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MosqueCardBorderSubtle)
                Spacer(modifier = Modifier.height(10.dp))

                if (payments.isEmpty()) {
                    Text(
                        text = "এখনো কোনো চাঁদা জমা হয়নি",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MosqueTextMuted,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        payments.take(6).forEach { p ->
                            Surface(
                                onClick = { onReceiptClick(p) },
                                shape = RoundedCornerShape(10.dp),
                                color = MosqueBgLight,
                                border = BorderStroke(1.dp, MosqueCardBorderGold),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${p.month} (${toBengaliDigits(p.receiptCode)})",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueEmeraldDark
                                        )
                                        Text(
                                            text = toBengaliDigits(p.paidAt),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MosqueTextMuted
                                        )
                                    }
                                    Text(
                                        text = "৳${toBengaliDigits(String.format("%,.0f", p.amountPaid))}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MosqueSuccessGreen
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                ) {
                    Text("বন্ধ করুন")
                }
            }
        }
    }
}

@Composable
private fun DueStatRow(
    label: String,
    value: String,
    valueColor: Color = MosqueTextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MosqueTextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MosqueTextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MosqueTextPrimary
        )
    }
}
