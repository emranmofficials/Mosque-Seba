package com.mosqueseba.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mosqueseba.app.data.local.entity.ChandaPaymentEntity
import com.mosqueseba.app.data.local.entity.MemberEntity
import com.mosqueseba.app.data.local.entity.MosqueEntity
import com.mosqueseba.app.data.local.entity.StaffEntity
import com.mosqueseba.app.data.local.entity.StaffPayrollEntity
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.*
import com.mosqueseba.app.ui.dialogs.*
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel

sealed class MemberProfileTarget {
    data class ChandaMember(val memberId: String) : MemberProfileTarget()
    data class SalariedStaff(val staffId: String?, val memberId: String?) : MemberProfileTarget()
}

@Composable
fun MemberProfileScreen(
    target: MemberProfileTarget,
    viewModel: MosqueViewModel,
    activeCycleMonth: String,
    isOwnProfile: Boolean = false,
    showBackButton: Boolean = true,
    onOpenMosqueProfile: (() -> Unit)? = null,
    onBack: () -> Unit,
    onOpenReceiveChanda: (MemberEntity, Double) -> Unit,
    onOpenPaySalary: (StaffEntity) -> Unit
) {
    BackHandler(enabled = showBackButton) { onBack() }

    val context = LocalContext.current
    val allMembers by viewModel.allMembers.collectAsState()
    val allChandaPayments by viewModel.allChandaPayments.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val activeStaff by viewModel.activeStaff.collectAsState()
    val allPayrolls by viewModel.allPayrolls.collectAsState()
    val allDutyRosters by viewModel.allDutyRosters.collectAsState()
    val allFoodSchedules by viewModel.allFoodSchedules.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val allAdvisoryNotes by viewModel.allAdvisoryNotes.collectAsState()
    val recentAuditLogs by viewModel.recentAuditLogs.collectAsState()
    val mosque by viewModel.mosque.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val savedMemberId = remember { viewModel.sessionManager.getSavedMemberId() }

    val canManageMembers = currentRole.canManageMembers()
    val canManageFinance = currentRole.canManageFinance()

    val resolvedMember: MemberEntity? = remember(target, allMembers, activeStaff) {
        when (target) {
            is MemberProfileTarget.ChandaMember -> allMembers.find { it.id == target.memberId }
            is MemberProfileTarget.SalariedStaff -> {
                target.memberId?.let { id -> allMembers.find { it.id == id } }
                    ?: target.staffId?.let { sid ->
                        val st = activeStaff.find { it.id == sid }
                        if (st != null) {
                            val normMobile = st.mobile.filter { it.isDigit() }
                            allMembers.find {
                                (normMobile.isNotEmpty() && it.mobile.filter { c -> c.isDigit() } == normMobile) ||
                                    it.nameBn.trim() == st.nameBn.trim()
                            }
                        } else null
                    }
            }
        }
    }

    val resolvedStaff: StaffEntity? = remember(target, activeStaff, resolvedMember) {
        when (target) {
            is MemberProfileTarget.SalariedStaff -> {
                target.staffId?.let { sid -> activeStaff.find { it.id == sid } }
                    ?: resolvedMember?.let { m ->
                        val normMobile = m.mobile.filter { it.isDigit() }
                        activeStaff.find {
                            it.id == "staff-${m.id}" ||
                                (normMobile.isNotEmpty() && it.mobile.filter { c -> c.isDigit() } == normMobile) ||
                                it.nameBn.trim() == m.nameBn.trim()
                        } ?: StaffEntity(
                            id = "staff-${m.id}",
                            nameBn = m.nameBn,
                            roleCode = m.roleCode,
                            designationBn = UserRole.fromCode(m.roleCode).titleBn,
                            joinDate = m.joinDate,
                            salaryAmount = when (m.roleCode.lowercase()) {
                                "imam" -> 18000.0
                                "muazzin" -> 12000.0
                                else -> 9000.0
                            },
                            mealRequired = true,
                            mobile = m.mobile,
                            isActive = m.status == "active"
                        )
                    }
            }
            is MemberProfileTarget.ChandaMember -> null
        }
    }

    val isSalaried = target is MemberProfileTarget.SalariedStaff || resolvedStaff != null

    val displayNameBn = resolvedMember?.nameBn ?: resolvedStaff?.nameBn ?: "সদস্য প্রোফাইল"
    val displayNameEn = resolvedMember?.nameEn?.takeIf { it.isNotBlank() } ?: ""
    val displayMobile = resolvedMember?.mobile ?: resolvedStaff?.mobile ?: ""
    val displayRole = UserRole.fromCode(resolvedMember?.roleCode ?: resolvedStaff?.roleCode ?: "general_member")
    val displayDesignation = resolvedStaff?.designationBn ?: displayRole.titleBn
    val displayCode = resolvedMember?.memberCode ?: (resolvedStaff?.id?.uppercase() ?: "স্টাফ")
    val isActive = (resolvedMember?.status ?: if (resolvedStaff?.isActive == true) "active" else "inactive") == "active"

    // Chanda stats
    val memberPayments = remember(allChandaPayments, resolvedMember?.id) {
        resolvedMember?.let { m -> allChandaPayments.filter { it.memberId == m.id } } ?: emptyList()
    }
    val paidThisCycle = remember(memberPayments, activeCycleMonth) {
        memberPayments.sumOf { ChandaMonthUtils.amountForMonth(it, activeCycleMonth) }
    }
    val totalDeposited = remember(memberPayments) {
        memberPayments.sumOf { it.amountPaid }
    }
    val monthlyCommitment = resolvedMember?.monthlyChanda ?: 0.0
    val dueThisCycle = (monthlyCommitment - paidThisCycle).coerceAtLeast(0.0)

    // General Ledger Transactions related to this member (createdBy / approvedBy / description)
    val memberTransactions = remember(allTransactions, displayNameBn, displayCode) {
        val cleanName = displayNameBn.trim()
        val cleanCode = displayCode.trim()
        allTransactions.filter { txn ->
            (cleanName.isNotBlank() && (
                txn.createdBy.contains(cleanName, ignoreCase = true) ||
                    (txn.approvedBy?.contains(cleanName, ignoreCase = true) == true) ||
                    txn.description.contains(cleanName, ignoreCase = true)
                )) ||
                (cleanCode.length >= 4 && txn.description.contains(cleanCode, ignoreCase = true))
        }
    }

    // Staff payroll stats
    val staffPayrolls = remember(allPayrolls, resolvedStaff?.id) {
        resolvedStaff?.let { st -> allPayrolls.filter { it.staffId == st.id } } ?: emptyList()
    }

    // Individual Member/Staff Duty Roster History
    val memberDutyRosters = remember(allDutyRosters, resolvedStaff?.id, displayNameBn) {
        val cleanName = displayNameBn.trim()
        allDutyRosters.filter { roster ->
            (resolvedStaff != null && roster.staffId == resolvedStaff.id) ||
                (cleanName.isNotBlank() && roster.staffNameBn.trim().equals(cleanName, ignoreCase = true))
        }
    }

    // Individual Food Schedule (Jagir) History (donor for non-salaried, receiver for salaried)
    val memberFoodSchedules = remember(allFoodSchedules, isSalaried, displayNameBn, displayMobile, displayDesignation, displayRole) {
        val cleanName = displayNameBn.trim()
        val normDigits = displayMobile.filter { it.isDigit() }
        allFoodSchedules.filter { food ->
            if (isSalaried) {
                val matchesRoleKeyword = when (displayRole) {
                    UserRole.IMAM -> food.receiverStaffName.contains("ইমাম", ignoreCase = true)
                    UserRole.MUAZZIN -> food.receiverStaffName.contains("মুয়াজ্জিন", ignoreCase = true) ||
                        food.receiverStaffName.contains("মুয়াজ্জিন", ignoreCase = true)
                    UserRole.KHADEM -> food.receiverStaffName.contains("খাদেম", ignoreCase = true)
                    else -> false
                }
                (cleanName.isNotBlank() && food.receiverStaffName.contains(cleanName, ignoreCase = true)) ||
                    (displayDesignation.isNotBlank() && food.receiverStaffName.contains(displayDesignation, ignoreCase = true)) ||
                    matchesRoleKeyword
            } else {
                (cleanName.isNotBlank() && food.providerName.contains(cleanName, ignoreCase = true)) ||
                    (normDigits.length >= 6 && food.providerMobile.filter { it.isDigit() } == normDigits)
            }
        }
    }

    // Individual Events / Advisory / Audit Activity History
    val memberEvents = remember(allEvents, displayNameBn) {
        val cleanName = displayNameBn.trim()
        if (cleanName.isBlank()) emptyList()
        else allEvents.filter { ev ->
            ev.volunteerLead.contains(cleanName, ignoreCase = true) ||
                ev.speakerOrGuest.contains(cleanName, ignoreCase = true)
        }
    }

    val memberAdvisoryNotes = remember(allAdvisoryNotes, displayNameBn) {
        val cleanName = displayNameBn.trim()
        if (cleanName.isBlank()) emptyList()
        else allAdvisoryNotes.filter { note ->
            note.authorName.contains(cleanName, ignoreCase = true)
        }
    }

    val memberAuditLogs = remember(recentAuditLogs, displayNameBn, displayCode) {
        val cleanName = displayNameBn.trim()
        val cleanCode = displayCode.trim()
        recentAuditLogs.filter { log ->
            (cleanName.isNotBlank() && (log.details.contains(cleanName, ignoreCase = true) || log.actor.contains(cleanName, ignoreCase = true))) ||
                (cleanCode.length >= 4 && log.details.contains(cleanCode, ignoreCase = true))
        }
    }

    var selectedHistoryFilter by remember { mutableIntStateOf(0) } // 0=সব, 1=লেনদেন, 2=খাবার, 3=দায়িত্ব ও কার্যক্রম

    var selectedReceiptForDialog by remember { mutableStateOf<ChandaPaymentEntity?>(null) }
    var showEditMemberDialog by remember { mutableStateOf(false) }
    var showOfficialSealsDialog by remember { mutableStateOf(false) }

    val isDark = LocalIsDarkTheme.current
    val cardBorderSubtle = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
    val primaryHeadingColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Modern Clean Control Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 0.5.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (showBackButton) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("profile_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "ফিরে যান",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        } else {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Column {
                            Text(
                                text = when {
                                    isOwnProfile -> "আমার নিজস্ব প্রোফাইল"
                                    isSalaried -> "বেতনভুক্ত সদস্য প্রোফাইল"
                                    else -> "সদস্য বিস্তারিত প্রোফাইল"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "আইডি: ${toBengaliDigits(displayCode)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if ((canManageMembers || isOwnProfile) && (resolvedMember != null || resolvedStaff != null)) {
                        IconButton(
                            onClick = { showEditMemberDialog = true },
                            modifier = Modifier.testTag("profile_edit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "তথ্য সম্পাদনা",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                HorizontalDivider(
                    color = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle,
                    thickness = 0.8.dp
                )
            }
        }

        if (resolvedMember == null && resolvedStaff == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PersonOff, contentDescription = null, tint = onSurfaceVariantColor, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("সদস্যের তথ্য পাওয়া যায়নি", color = onSurfaceVariantColor)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)) {
                        Text("তালিকায় ফিরুন")
                    }
                }
            }
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Hero Profile Summary Card
            item {
                MehrabArchCard(
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    borderColor = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val photoBitmap = remember(resolvedMember?.photoUrl) {
                            loadLocalThumbnail(resolvedMember?.photoUrl, 320)
                        }

                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) MosqueEmeraldPrimary.copy(alpha = 0.22f) else MosqueEmeraldSoft)
                                    .border(2.5.dp, MosqueGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (photoBitmap != null) {
                                    Image(
                                        bitmap = photoBitmap,
                                        contentDescription = displayNameBn,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = displayNameBn.firstOrNull()?.toString() ?: "স",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = primaryHeadingColor
                                    )
                                }
                            }
                            Surface(
                                shape = CircleShape,
                                color = if (isActive) MosqueSuccessGreen else MosqueUrgentRed,
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                                modifier = Modifier.size(20.dp)
                            ) {}
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = displayNameBn,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryHeadingColor
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (isDark) MosqueGold.copy(alpha = 0.2f) else MosqueGoldLight.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, MosqueGoldDeep.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = displayRole.roleIcon(),
                                        contentDescription = null,
                                        tint = primaryHeadingColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = displayDesignation,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryHeadingColor
                                    )
                                }
                            }
                            Surface(
                                color = if (isActive) MosqueSuccessGreen.copy(alpha = 0.12f) else MosqueUrgentRed.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isActive) "সক্রিয়" else "নিষ্ক্রিয়",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) MosqueSuccessGreen else MosqueUrgentRed,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Action Row: Self Profile vs Member Directory View
                        if (isOwnProfile) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (resolvedMember != null || resolvedStaff != null) {
                                    Button(
                                        onClick = { showEditMemberDialog = true },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("my_profile_edit_info_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("নিজ তথ্য সম্পাদনা", fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (onOpenMosqueProfile != null) {
                                    OutlinedButton(
                                        onClick = onOpenMosqueProfile,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, MosqueGoldDeep.copy(alpha = 0.65f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("my_profile_to_mosque_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mosque,
                                            contentDescription = null,
                                            tint = primaryHeadingColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("মসজিদের প্রোফাইল", color = primaryHeadingColor, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (displayMobile.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$displayMobile"))
                                            context.startActivity(dialIntent)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("profile_call_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, MosqueEmeraldPrimary)
                                    ) {
                                        Icon(
                                            Icons.Default.Call,
                                            contentDescription = null,
                                            tint = MosqueEmeraldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("কল করুন", color = MosqueEmeraldPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (!isSalaried && resolvedMember != null && canManageFinance) {
                                    Button(
                                        onClick = {
                                            val suggested = if (dueThisCycle > 0) dueThisCycle else resolvedMember.monthlyChanda
                                            onOpenReceiveChanda(resolvedMember, suggested)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("profile_collect_chanda_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                    ) {
                                        Icon(
                                            Icons.Default.Payments,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("চাঁদা গ্রহণ", fontWeight = FontWeight.Bold)
                                    }
                                } else if (isSalaried && resolvedStaff != null && canManageFinance) {
                                    Button(
                                        onClick = { onOpenPaySalary(resolvedStaff) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("profile_pay_salary_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                    ) {
                                        Icon(
                                            Icons.Default.AccountBalanceWallet,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("বেতন পরিশোধ", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (canManageMembers && (resolvedMember != null || resolvedStaff != null)) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = { showEditMemberDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("profile_open_edit_page_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MosqueGoldDeep.copy(alpha = 0.7f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryHeadingColor)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = MosqueGoldDeep,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "তথ্য সম্পাদনা ও প্রশাসনিক নিয়ন্ত্রণ",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (canManageMembers || isOwnProfile) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { showOfficialSealsDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MosqueEmeraldPrimary.copy(alpha = 0.7f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueEmeraldPrimary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Draw,
                                        contentDescription = null,
                                        tint = MosqueEmeraldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "অফিসিয়াল সিলমোহর ও ডিজিটাল স্বাক্ষর",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Financial Summary Strip (shown when viewing member directory profiles, omitted on own profile to keep zero history)
            if (!isOwnProfile) {
                item {
                    if (!isSalaried) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProfileStatBox(
                                modifier = Modifier.weight(1f),
                                label = "মাসিক চাঁদা",
                                value = if (monthlyCommitment > 0) {
                                    "৳${toBengaliDigits(String.format("%,.0f", monthlyCommitment))}"
                                } else "ঐচ্ছিক",
                                accent = MosqueEmeraldPrimary
                            )
                            ProfileStatBox(
                                modifier = Modifier.weight(1f),
                                label = "মোট জমা",
                                value = "৳${toBengaliDigits(String.format("%,.0f", totalDeposited))}",
                                accent = MosqueSuccessGreen
                            )
                            ProfileStatBox(
                                modifier = Modifier.weight(1f),
                                label = "চলতি বকেয়া",
                                value = "৳${toBengaliDigits(String.format("%,.0f", dueThisCycle))}",
                                accent = if (dueThisCycle > 0) MosqueUrgentRed else MosqueSuccessGreen
                            )
                        }
                    } else {
                        val salaryVal = resolvedStaff?.salaryAmount ?: 0.0
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProfileStatBox(
                                modifier = Modifier.weight(1f),
                                label = "মাসিক বেতন",
                                value = "৳${toBengaliDigits(String.format("%,.0f", salaryVal))}",
                                accent = MosqueEmeraldPrimary
                            )
                            ProfileStatBox(
                                modifier = Modifier.weight(1f),
                                label = "খাবার সুবিধা",
                                value = if (resolvedStaff?.mealRequired != false) "প্রযোজ্য" else "প্রযোজ্য নয়",
                                accent = MosqueGoldDeep
                            )
                            ProfileStatBox(
                                modifier = Modifier.weight(1f),
                                label = "বেতন রসিদ",
                                value = "${toBengaliDigits(staffPayrolls.size.toString())} বার",
                                accent = MosqueSuccessGreen
                            )
                        }
                    }
                }
            }

            // 3. Personal & Mosque Information
            item {
                RubElHizbDivider(title = if (isOwnProfile) "আমার ব্যক্তিগত ও সাংগঠনিক তথ্য" else "ব্যক্তিগত ও সাংগঠনিক তথ্য")
                MehrabArchCard(
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    borderColor = MosqueCardBorderGold
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileInfoRow("সদস্য / স্টাফ কোড", toBengaliDigits(displayCode))
                        ProfileInfoRow("পদবি", displayDesignation)
                        ProfileInfoRow("কমিটির স্তর", displayRole.tier.titleBn)
                        ProfileInfoRow("মোবাইল নম্বর", toBengaliDigits(displayMobile.ifBlank { "প্রযোজ্য নয়" }))
                        if (isOwnProfile) {
                            if (isSalaried) {
                                val salaryVal = resolvedStaff?.salaryAmount ?: 0.0
                                ProfileInfoRow(
                                    label = "মাসিক বেতন ভাতা",
                                    value = "৳${toBengaliDigits(String.format("%,.0f", salaryVal))}",
                                    valueColor = primaryHeadingColor
                                )
                            } else {
                                ProfileInfoRow(
                                    label = "নির্ধারিত মাসিক চাঁদা",
                                    value = if (monthlyCommitment > 0) {
                                        "৳${toBengaliDigits(String.format("%,.0f", monthlyCommitment))}"
                                    } else "ঐচ্ছিক / সাধারণ",
                                    valueColor = primaryHeadingColor
                                )
                            }
                        }
                        if (resolvedMember != null) {
                            ProfileInfoRow("ইমেইল", resolvedMember.email.ifBlank { "প্রযোজ্য নয়" })
                            ProfileInfoRow("জাতীয় পরিচয়পত্র", toBengaliDigits(resolvedMember.nid.ifBlank { "প্রযোজ্য নয়" }))
                            val effectiveAddress = resolvedMember.address.ifBlank {
                                if (!isSalaried) (mosque?.address ?: "") else ""
                            }
                            ProfileInfoRow("বাসার ঠিকানা", effectiveAddress.ifBlank { "প্রযোজ্য নয়" })
                            ProfileInfoRow("এলাকা / মহল্লা", resolvedMember.villageArea.ifBlank { "প্রযোজ্য নয়" })
                            ProfileInfoRow("যোগদানের তারিখ", toBengaliDigits(resolvedMember.joinDate))
                            if (!isSalaried) {
                                ProfileInfoRow(
                                    label = "স্টাফদের খাবার প্রদান",
                                    value = if (resolvedMember.willingToProvideStaffMeals) "হ্যাঁ, রাজি আছেন" else "না",
                                    valueColor = if (resolvedMember.willingToProvideStaffMeals) MosqueSuccessGreen else onSurfaceVariantColor
                                )
                            }
                        } else if (resolvedStaff != null) {
                            ProfileInfoRow("নিয়োগের ধরন", if (resolvedStaff.employmentType == "full_time") "পূর্ণকালীন" else "খণ্ডকালীন")
                            ProfileInfoRow("যোগদানের তারিখ", toBengaliDigits(resolvedStaff.joinDate))
                            ProfileInfoRow(
                                label = "খাবার ব্যবস্থা (জায়গির)",
                                value = if (resolvedStaff.mealRequired) "মসজিদ/সদস্যের মাধ্যমে নির্ধারিত" else "প্রযোজ্য নয়"
                            )
                        }
                    }
                }
            }

            // 3A. Personal Salary Slip History for Salaried Staff (Imam, Muazzin, Khadem)
            if (isOwnProfile && isSalaried) {
                item {
                    RubElHizbDivider(title = "আমার বেতন স্লিপ ও পরিশোধ রেকর্ড")
                }
                if (staffPayrolls.isEmpty()) {
                    item {
                        MehrabArchCard(
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            borderColor = cardBorderSubtle
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = onSurfaceVariantColor,
                                    modifier = Modifier.size(30.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "এখনো কোনো বেতন স্লিপ পে-রোল রেকর্ড হয়নি",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = onSurfaceVariantColor
                                )
                            }
                        }
                    }
                } else {
                    items(staffPayrolls.reversed(), key = { "own_payroll-${it.id}" }) { payroll ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MosqueCardBorderGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${toBengaliDigits(payroll.month)} বেতন স্লিপ",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryHeadingColor
                                    )
                                    Text(
                                        text = "মূল: ৳${toBengaliDigits(payroll.salaryAmount.toInt().toString())} + বোনাস: ৳${toBengaliDigits(payroll.bonusAmount.toInt().toString())}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = onSurfaceVariantColor
                                    )
                                    payroll.paidDate?.let { d ->
                                        Text(
                                            text = "পরিশোধের তারিখ: ${toBengaliDigits(d)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = onSurfaceVariantColor
                                        )
                                    }
                                }
                                Surface(
                                    color = MosqueSuccessGreen.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "৳${toBengaliDigits(String.format("%,.0f", payroll.netSalary))}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MosqueSuccessGreen,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3B. Role Duties & Responsibilities Card (Always visible on Own Profile, also helpful on Member Profile)
            item {
                RubElHizbDivider(title = if (isOwnProfile) "আমার পদবি ও নির্ধারিত দায়িত্বসমূহ" else "পদবি ও দায়িত্বসমূহ")
                MehrabArchCard(
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    borderColor = MosqueCardBorderGold
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = if (isDark) MosqueNightSurfaceVariant else MosqueGoldSoft,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.8.dp, MosqueCardBorderGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = displayRole.roleIcon(),
                                        contentDescription = null,
                                        tint = primaryHeadingColor,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = displayRole.titleBn,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = primaryHeadingColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = displayRole.shortSummaryBn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = onSurfaceVariantColor,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        displayRole.dutiesBn.forEachIndexed { idx, duty ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) MosqueEmeraldPrimary.copy(alpha = 0.22f) else MosqueEmeraldSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = toBengaliDigits((idx + 1).toString()),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = primaryHeadingColor
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = duty,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = onSurfaceColor,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (idx < displayRole.dutiesBn.lastIndex) {
                                HorizontalDivider(color = cardBorderSubtle.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }

            // 4. Individual Member History (Shown ONLY when viewing others in directory, NEVER on own profile)
            if (!isOwnProfile) {
            item {
                RubElHizbDivider(title = "লেনদেন ও খাবারের ইতিহাস")
                val totalTxnCount = memberPayments.size + staffPayrolls.size + memberTransactions.size
                val totalFoodCount = memberFoodSchedules.size
                val totalActivityCount = memberDutyRosters.size + memberEvents.size + memberAdvisoryNotes.size + memberAuditLogs.size
                val totalHistoryCount = totalTxnCount + totalFoodCount + totalActivityCount

                val filterTabs = listOf(
                    "সব (${toBengaliDigits(totalHistoryCount.toString())})",
                    "লেনদেন (${toBengaliDigits(totalTxnCount.toString())})",
                    "খাবার (${toBengaliDigits(totalFoodCount.toString())})",
                    "দায়িত্ব ও কার্যক্রম (${toBengaliDigits(totalActivityCount.toString())})"
                )

                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterTabs.size) { index ->
                        val isSelected = selectedHistoryFilter == index
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedHistoryFilter = index },
                            label = {
                                Text(
                                    text = filterTabs[index],
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MosqueEmeraldPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = onSurfaceColor
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MosqueEmeraldPrimary else MosqueCardBorderGold
                            )
                        )
                    }
                }
            }

            // 4A. Transaction History (Chanda Receipts + Staff Payroll + Ledger Transactions)
            if (selectedHistoryFilter == 0 || selectedHistoryFilter == 1) {
                item {
                    Text(
                        text = "লেনদেন ইতিহাস",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryHeadingColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (memberPayments.isEmpty() && staffPayrolls.isEmpty() && memberTransactions.isEmpty()) {
                    item {
                        MehrabArchCard(backgroundColor = MaterialTheme.colorScheme.surface, borderColor = cardBorderSubtle) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = onSurfaceVariantColor, modifier = Modifier.size(30.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isSalaried) "এখনো কোনো বেতন বা লেনদেন রেকর্ড পাওয়া যায়নি" else "এখনো কোনো চাঁদা বা লেনদেন ইতিহাস পাওয়া যায়নি",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = onSurfaceVariantColor
                                )
                            }
                        }
                    }
                } else {
                    items(memberPayments, key = { "chanda-${it.id}" }) { payment ->
                        Card(
                            onClick = { selectedReceiptForDialog = payment },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MosqueCardBorderGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(MosqueSuccessGreen.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, tint = MosqueSuccessGreen, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "${payment.month} • রসিদ ${toBengaliDigits(payment.receiptCode)}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryHeadingColor
                                        )
                                        Text(
                                            text = "মাধ্যম: ${
                                                when (payment.method) {
                                                    "cash" -> "নগদ"
                                                    "online" -> "ব্যাংক/অনলাইন"
                                                    else -> "মোবাইল ব্যাংকিং"
                                                }
                                            } • ${toBengaliDigits(payment.paidAt)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = onSurfaceVariantColor
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "৳${toBengaliDigits(String.format("%,.0f", payment.amountPaid))}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MosqueSuccessGreen
                                    )
                                    Text(
                                        text = "রসিদ দেখুন",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = primaryHeadingColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    items(staffPayrolls, key = { "payroll-${it.id}" }) { payroll ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MosqueCardBorderGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${payroll.month} বেতন পরিশোধ",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryHeadingColor
                                    )
                                    Text(
                                        text = "মূল: ৳${toBengaliDigits(payroll.salaryAmount.toInt().toString())} + বোনাস: ৳${toBengaliDigits(payroll.bonusAmount.toInt().toString())}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = onSurfaceVariantColor
                                    )
                                    payroll.paidDate?.let { d ->
                                        Text("তারিখ: ${toBengaliDigits(d)}", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariantColor)
                                    }
                                }
                                Surface(
                                    color = MosqueSuccessGreen.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "৳${toBengaliDigits(String.format("%,.0f", payroll.netSalary))}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MosqueSuccessGreen,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    items(memberTransactions, key = { "txn-${it.id}" }) { txn ->
                        val isIncome = txn.type == "INCOME"
                        val accent = if (isIncome) MosqueSuccessGreen else MosqueUrgentRed
                        val statusBadge = when (txn.approvalStatus) {
                            "approved" -> "অনুমোদিত"
                            "rejected" -> "বাতিল"
                            else -> "অপেক্ষমাণ"
                        }
                        ProfileHistoryEntryCard(
                            icon = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            accentColor = accent,
                            title = "${txn.categoryNameBn} • ৳${toBengaliDigits(String.format("%,.0f", txn.amount))}",
                            subtitle = "${txn.description} • ${toBengaliDigits(txn.txnDate)} (${toBengaliDigits(txn.transactionCode)})",
                            badgeText = statusBadge
                        )
                    }
                }
            }

            // 4B. Dedicated Food Schedule (Jagir) History
            if (selectedHistoryFilter == 0 || selectedHistoryFilter == 2) {
                item {
                    Text(
                        text = if (isSalaried) "খাবার (জায়গির) গ্রহণের ইতিহাস" else "খাবার (জায়গির) প্রদানের ইতিহাস",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryHeadingColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (memberFoodSchedules.isEmpty()) {
                    item {
                        MehrabArchCard(backgroundColor = MaterialTheme.colorScheme.surface, borderColor = cardBorderSubtle) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Restaurant, contentDescription = null, tint = onSurfaceVariantColor, modifier = Modifier.size(30.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = when {
                                        isSalaried -> "এখনো কোনো খাবার গ্রহণের ইতিহাস পাওয়া যায়নি"
                                        resolvedMember?.willingToProvideStaffMeals == true ->
                                            "খাবার প্রদানে সম্মত — এখনো কোনো নির্ধারিত তারিখ রেকর্ড করা হয়নি"
                                        else -> "এখনো কোনো খাবার (জায়গির) প্রদানের ইতিহাস নেই"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = onSurfaceVariantColor
                                )
                            }
                        }
                    }
                } else {
                    items(memberFoodSchedules, key = { "food-${it.id}" }) { food ->
                        val statusLabel = when (food.status) {
                            "delivered" -> "সম্পন্ন"
                            "approved" -> "সম্মত"
                            "rejected" -> "প্রত্যাখ্যাত"
                            "cancelled" -> "বাতিল"
                            else -> "অপেক্ষমাণ"
                        }
                        ProfileHistoryEntryCard(
                            icon = Icons.Default.Restaurant,
                            accentColor = MosqueGoldDeep,
                            title = if (isSalaried) {
                                "দৈনিক খাবার গ্রহণ (${food.providerName})"
                            } else {
                                "দৈনিক খাবার প্রদান (${food.receiverStaffName})"
                            },
                            subtitle = "তারিখ: ${toBengaliDigits(food.dateStr)} • মোবাইল: ${toBengaliDigits(food.providerMobile)}",
                            badgeText = statusLabel
                        )
                    }
                }
            }

            // 4C. Duty Roster, Events, Advisory Notes & Activity Audit Logs
            if (selectedHistoryFilter == 0 || selectedHistoryFilter == 3) {
                if (selectedHistoryFilter == 3 && memberDutyRosters.isEmpty() && memberEvents.isEmpty() && memberAdvisoryNotes.isEmpty() && memberAuditLogs.isEmpty()) {
                    item {
                        MehrabArchCard(backgroundColor = MaterialTheme.colorScheme.surface, borderColor = cardBorderSubtle) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, tint = onSurfaceVariantColor, modifier = Modifier.size(30.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("এখনো কোনো দায়িত্ব রোস্টার, ইভেন্ট বা কার্যক্রম লগ নেই", style = MaterialTheme.typography.bodySmall, color = onSurfaceVariantColor)
                            }
                        }
                    }
                }

                items(memberDutyRosters, key = { "roster-${it.id}" }) { roster ->
                    ProfileHistoryEntryCard(
                        icon = Icons.Default.Schedule,
                        accentColor = MosqueEmeraldPrimary,
                        title = "${roster.roleTitle} (${roster.prayerName})",
                        subtitle = "দিন: ${roster.dayOfWeek}" + if (roster.notes.isNotBlank()) " • ${roster.notes}" else "",
                        badgeText = "রোস্টার"
                    )
                }

                items(memberEvents, key = { "event-${it.id}" }) { ev ->
                    ProfileHistoryEntryCard(
                        icon = Icons.Default.Event,
                        accentColor = MosqueEmeraldDark,
                        title = ev.title,
                        subtitle = "${toBengaliDigits(ev.dateStr)} • ${ev.timeStr} • স্থান: ${ev.venue}",
                        badgeText = "ইভেন্ট"
                    )
                }

                items(memberAdvisoryNotes, key = { "adv-${it.id}" }) { note ->
                    ProfileHistoryEntryCard(
                        icon = Icons.Default.Forum,
                        accentColor = MosqueGoldDeep,
                        title = note.topic,
                        subtitle = "${note.recommendation.take(80)} • ${toBengaliDigits(note.createdDate)}",
                        badgeText = note.status
                    )
                }

                items(memberAuditLogs, key = { "audit-${it.id}" }) { log ->
                    ProfileHistoryEntryCard(
                        icon = Icons.Default.History,
                        accentColor = MosqueTextSecondary,
                        title = log.action,
                        subtitle = "${log.details} • ${toBengaliDigits(log.timestamp)}",
                        badgeText = "লগ"
                    )
                }
            }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    // Official Receipt Dialog
    selectedReceiptForDialog?.let { payment ->
        val treasurer = viewModel.getTreasurerMember()
        OfficialReceiptDialog(
            payment = payment,
            mosqueName = mosque?.nameBn ?: "মসজিদ সেবা",
            mosqueAddress = mosque?.address ?: "",
            committeeId = mosque?.committeeId ?: "",
            mosqueLogoUrl = mosque?.logoUrl,
            treasurerSignatureUrl = treasurer?.signatureUrl,
            treasurerStampUrl = treasurer?.stampUrl,
            mosqueSealUrl = mosque?.sealUrl,
            onDismiss = { selectedReceiptForDialog = null }
        )
    }

    // Edit Member / Staff Dialog (Includes Role/Responsibility & Administrative Controls)
    if (showEditMemberDialog && (resolvedMember != null || resolvedStaff != null)) {
        val isSelfAccount = isOwnProfile ||
            (resolvedMember != null && resolvedMember.id == savedMemberId) ||
            (currentRole == UserRole.PRESIDENT && resolvedMember?.roleCode == "president")
        AddMemberDialog(
            initialMember = resolvedMember,
            initialStaff = resolvedStaff,
            defaultMosqueAddress = mosque?.address ?: "",
            isActiveStatus = isActive,
            canManageAdministrativeControls = canManageMembers && !isSelfAccount,
            canEditRoleAndSalary = canManageMembers && !isSelfAccount,
            onToggleStatus = {
                if (resolvedMember != null) {
                    viewModel.toggleMemberStatus(resolvedMember)
                    val nextState = if (isActive) "নিষ্ক্রিয়" else "সক্রিয়"
                    MosqueToastController.info("${resolvedMember.nameBn}-কে $nextState করা হয়েছে।", "স্ট্যাটাস পরিবর্তন")
                } else if (resolvedStaff != null) {
                    viewModel.toggleStaffStatus(resolvedStaff)
                    val nextState = if (resolvedStaff.isActive) "নিষ্ক্রিয়" else "সক্রিয়"
                    MosqueToastController.info("${resolvedStaff.nameBn}-কে $nextState করা হয়েছে।", "স্ট্যাটাস পরিবর্তন")
                }
            },
            onDeleteMember = {
                val targetName = resolvedMember?.nameBn ?: resolvedStaff?.nameBn ?: "সদস্য"
                if (resolvedMember != null) {
                    viewModel.deleteMember(resolvedMember)
                } else if (resolvedStaff != null) {
                    viewModel.removeStaff(resolvedStaff)
                }
                MosqueToastController.warning("$targetName তালিকা থেকে অপসারিত হয়েছে।", "অপসারণ সম্পন্ন")
                onBack()
            },
            onDismiss = { showEditMemberDialog = false },
            onConfirm = { nameBn, nameEn, mobile, email, nid, address, villageArea, roleCode, monthlyChanda, photoUrl, willingToProvideStaffMeals, salaryAmount, staffMealRequired ->
                if (resolvedMember != null) {
                    viewModel.updateMemberDetails(
                        member = resolvedMember.copy(
                            nameBn = nameBn,
                            nameEn = nameEn,
                            mobile = mobile,
                            email = email,
                            nid = nid,
                            address = address,
                            villageArea = villageArea,
                            roleCode = roleCode,
                            monthlyChanda = monthlyChanda,
                            photoUrl = photoUrl,
                            willingToProvideStaffMeals = willingToProvideStaffMeals
                        ),
                        salaryAmount = salaryAmount,
                        staffMealRequired = staffMealRequired
                    )
                    MosqueToastController.success("$nameBn-এর তথ্য সফলভাবে হালনাগাদ হয়েছে।", "তথ্য হালনাগাদ")
                } else if (resolvedStaff != null) {
                    viewModel.promoteStaffAndUpdateDetails(
                        staff = resolvedStaff,
                        nameBn = nameBn,
                        nameEn = nameEn,
                        mobile = mobile,
                        email = email,
                        nid = nid,
                        address = address,
                        villageArea = villageArea,
                        roleCode = roleCode,
                        monthlyChanda = monthlyChanda,
                        photoUrl = photoUrl,
                        willingToProvideStaffMeals = willingToProvideStaffMeals,
                        salaryAmount = salaryAmount,
                        staffMealRequired = staffMealRequired
                    )
                    MosqueToastController.success("$nameBn-এর প্রোফাইল সফলভাবে হালনাগাদ হয়েছে।", "প্রোফাইল হালনাগাদ")
                }
            }
        )
    }

    if (showOfficialSealsDialog) {
        OfficialSealSignatureDialog(
            viewModel = viewModel,
            onDismiss = { showOfficialSealsDialog = false }
        )
    }
}

@Composable
private fun ProfileStatBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MosqueCardBorderGold)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = accent
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val isDark = LocalIsDarkTheme.current
    val dividerColor = if (isDark) MosqueNightBorder.copy(alpha = 0.7f) else MosqueCardBorderSubtle.copy(alpha = 0.7f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.9f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            modifier = Modifier.weight(1.3f)
        )
    }
    HorizontalDivider(color = dividerColor)
}

@Composable
private fun ProfileHistoryEntryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    title: String,
    subtitle: String,
    badgeText: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MosqueCardBorderGold),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = accentColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

