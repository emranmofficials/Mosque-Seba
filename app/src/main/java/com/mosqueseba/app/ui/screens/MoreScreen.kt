package com.mosqueseba.app.ui.screens

import java.util.Locale
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mosqueseba.app.data.local.entity.ChandaPaymentEntity
import com.mosqueseba.app.data.local.entity.FoodScheduleEntity
import com.mosqueseba.app.data.local.entity.MeetingEntity
import com.mosqueseba.app.data.local.entity.StaffEntity
import com.mosqueseba.app.data.model.UserRole
import androidx.compose.ui.platform.testTag
import com.mosqueseba.app.ui.components.AddEventDialog
import com.mosqueseba.app.ui.components.AddFoodScheduleFormDialog
import com.mosqueseba.app.ui.components.AddMemberDialog
import com.mosqueseba.app.ui.components.AddRosterFormDialog
import com.mosqueseba.app.ui.components.AddWelfareDialog
import com.mosqueseba.app.ui.components.AutoFoodScheduleDialog
import com.mosqueseba.app.ui.components.MeetingResolutionDialog
import com.mosqueseba.app.ui.components.MosqueConfirmDialog
import com.mosqueseba.app.ui.components.OfficialReceiptDialog
import com.mosqueseba.app.ui.components.PaySalaryFormDialog
import com.mosqueseba.app.ui.components.ScheduleMeetingDialog
import com.mosqueseba.app.ui.components.toBengaliDigits
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun MoreScreen(
    viewModel: MosqueViewModel,
    initialSubModule: String? = null,
    onBack: (() -> Unit)? = null,
    onOpenReceiveChanda: () -> Unit,
    onOpenScheduleMeeting: () -> Unit,
    onOpenPublishNotice: () -> Unit = {},
    onOpenAddMember: () -> Unit = {},
    onOpenMyProfile: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedSubModule by remember(initialSubModule) { mutableStateOf<String?>(initialSubModule) }
    var selectedPaymentForReceipt by remember { mutableStateOf<ChandaPaymentEntity?>(null) }
    var showAddStaffDialog by remember { mutableStateOf(false) }
    var showPaySalaryStaff by remember { mutableStateOf<StaffEntity?>(null) }
    var showAddRosterDialog by remember { mutableStateOf(false) }
    var showAddFoodDialog by remember { mutableStateOf(false) }
    var showAutoFoodDialog by remember { mutableStateOf(false) }
    var showAddWelfareDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var editingMeeting by remember { mutableStateOf<MeetingEntity?>(null) }
    var resolutionTargetMeeting by remember { mutableStateOf<MeetingEntity?>(null) }
    var pendingDeleteMeeting by remember { mutableStateOf<MeetingEntity?>(null) }
    var pendingCancelFood by remember { mutableStateOf<FoodScheduleEntity?>(null) }
    var pendingDeleteFood by remember { mutableStateOf<FoodScheduleEntity?>(null) }

    val currentRole by viewModel.currentUserRole.collectAsState()
    val chandaPayments by viewModel.allChandaPayments.collectAsState()
    val totalChandaCollected by viewModel.totalChandaCollected.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    val activeStaff by viewModel.activeStaff.collectAsState()
    val auditLogs by viewModel.recentAuditLogs.collectAsState()
    val mosque by viewModel.mosque.collectAsState()
    val dutyRosters by viewModel.allDutyRosters.collectAsState()
    val foodSchedules by viewModel.allFoodSchedules.collectAsState()
    val payrolls by viewModel.allPayrolls.collectAsState()
    val allMeetings by viewModel.allMeetings.collectAsState()
    val welfareCases by viewModel.allWelfareCases.collectAsState()
    val events by viewModel.allEvents.collectAsState()
    val cashboxRecords by viewModel.allCashboxRecords.collectAsState()
    val officeDocuments by viewModel.allOfficeDocuments.collectAsState()
    val advisoryNotes by viewModel.allAdvisoryNotes.collectAsState()
    val smartBoardQuotes by viewModel.allSmartBoardQuotes.collectAsState()
    val studentCount by viewModel.studentCount.collectAsState()
    val khademCheckedTasks by viewModel.khademCheckedTasks.collectAsState()
    val activeElection by viewModel.activeElection.collectAsState()

    BackHandler(enabled = selectedSubModule != null && initialSubModule == null) {
        selectedSubModule = null
    }

    val liveCounts = remember(
        cashboxRecords,
        officeDocuments,
        advisoryNotes,
        smartBoardQuotes,
        allMeetings,
        welfareCases,
        activeStaff,
        chandaPayments,
        studentCount,
        dutyRosters,
        foodSchedules,
        events,
        auditLogs,
        activeElection
    ) {
        MoreLiveCounts(
            pendingCashboxCount = cashboxRecords.count { it.depositStatus != "bank_deposited" },
            totalCashboxCount = cashboxRecords.size,
            pendingDocsCount = officeDocuments.count { it.status == "জবাব অপেক্ষমান" },
            totalDocsCount = officeDocuments.size,
            activeAdvisoryCount = advisoryNotes.count { it.status == "পর্যালোচনাধীন" },
            totalQuotesCount = smartBoardQuotes.size,
            upcomingMeetingsCount = allMeetings.count { it.status != "completed" },
            totalWelfareCount = welfareCases.size,
            totalStaffCount = activeStaff.size,
            totalChandaReceipts = chandaPayments.size,
            totalStudentsCount = studentCount,
            totalRosterCount = dutyRosters.size,
            totalFoodScheduleCount = foodSchedules.size,
            upcomingEventsCount = events.count { it.status != "completed" },
            auditLogsCount = auditLogs.size,
            hasActiveElection = activeElection != null
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Cohesive Emerald-Gold Sticky Header for Sub-modules
        if (selectedSubModule != null) {
            Surface(
                color = Color.Transparent,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MosqueEmeraldDarkGradient)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (initialSubModule == null || onBack != null) {
                            IconButton(
                                onClick = {
                                    selectedSubModule = null
                                    onBack?.invoke()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "ফিরে যান",
                                    tint = MosqueGoldLight
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        } else {
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (selectedSubModule) {
                                    "election" -> "কমিটি নির্বাচন ও পুনর্গঠন"
                                    "madrasa" -> "মাদরাসা ও মক্তব পরিচালনা"
                                    "chanda" -> "চাঁদা আদায় ও রসিদ রেজিস্টার"
                                    "staff" -> "ইমাম ও কর্মচারী বেতন ও পে-রোল"
                                    "roster" -> "ওয়াক্ত ও ইমাম-মুয়াজ্জিন ডিউটি রোস্টার"
                                    "food" -> "ইমাম ও খাদেমদের খাবার শিডিউল"
                                    "meetings" -> "কমিটি সভা ও এজেন্ডা রেজুলেশন"
                                    "welfare" -> "সমাজকল্যাণ, যাকাত, মেডিকেল ক্যাম্প ও ত্রাণ"
                                    "events" -> "মাহফিল ও বিশেষ অনুষ্ঠান"
                                    "prayer" -> "পূর্ণ নামাজের সময়সূচি ও ওয়াক্ত"
                                    "audit" -> "স্বচ্ছতা ও সিস্টেম অডিট লগ"
                                    "about" -> "মসজিদের প্রোফাইল ও কমিটি"
                                    "charter" -> "আদর্শ কমিটি সনদ ও দায়িত্ব"
                                    "cashbox" -> "দান বাক্স (ক্যাশবক্স) ও ব্যাংক জমা"
                                    "office_docs" -> "দプター, দলিল-চুক্তি ও চিঠিপত্র"
                                    "sharia_advisory" -> "শরিয়াহ পর্যালোচনা ও উপদেষ্টা পরিষদ"
                                    "publicity" -> "প্রচার, দেয়াল উক্তি ও স্মার্ট বোর্ড"
                                    "notices" -> "নোটিশ বোর্ড ও ঘোষণা"
                                    "members" -> "মসজিদ কমিটি ও সদস্য পরিষদ"
                                    "transparency" -> "মসজিদ স্বচ্ছ তহবিল ও হিসাব"
                                    "finance" -> "আয়-ব্যয় ও আর্থিক ব্যবস্থাপনা"
                                    else -> "অপারেশন মডিউল"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "মসজিদ কমিটি অপারেশনাল মডিউল",
                                style = MaterialTheme.typography.labelSmall,
                                color = MosqueGoldLight.copy(alpha = 0.88f)
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
        }

        // Sub-module routing
        when (selectedSubModule) {
            "election" -> {
                ElectionScreen(
                    viewModel = viewModel,
                    onBack = { selectedSubModule = null }
                )
            }
            "charter" -> {
                CommitteeCharterScreen(viewModel = viewModel)
            }
            "cashbox" -> {
                CashboxScreen(viewModel = viewModel)
            }
            "office_docs" -> {
                OfficeDocumentsScreen(viewModel = viewModel)
            }
            "sharia_advisory" -> {
                ShariaAdvisoryScreen(viewModel = viewModel)
            }
            "publicity" -> {
                PublicitySmartBoardScreen(viewModel = viewModel)
            }
            "notices" -> {
                NoticesScreen(
                    viewModel = viewModel,
                    onPublishNoticeClick = onOpenPublishNotice,
                    onScheduleMeetingClick = if (currentRole.canScheduleMeetings()) {
                        onOpenScheduleMeeting
                    } else null
                )
            }
            "members" -> {
                MembersScreen(
                    viewModel = viewModel,
                    onAddMemberClick = if (currentRole.canManageMembers()) onOpenAddMember else null
                )
            }
            "transparency", "finance" -> {
                FinanceScreen(
                    viewModel = viewModel,
                    onAddIncomeClick = null,
                    onAddExpenseClick = null,
                    isReadOnlyTransparencyMode = selectedSubModule == "transparency" || !currentRole.canManageFinance()
                )
            }
            "madrasa" -> {
                MadrasaScreen(viewModel = viewModel)
            }
            "chanda" -> {
                // Chanda Ledger / My Chanda
                val isDark = LocalIsDarkTheme.current
                val cardBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
                val isGeneralMember = currentRole == UserRole.GENERAL_MEMBER
                val canReceiveChanda = currentRole.canManageFinance()
                val savedMemberId = viewModel.sessionManager.getSavedMemberId()
                val currentMemberProfile = remember(allMembers, savedMemberId, currentRole) {
                    allMembers.firstOrNull { it.id == savedMemberId }
                        ?: allMembers.firstOrNull { it.roleCode.equals(currentRole.code, ignoreCase = true) }
                }
                val displayedPayments = if (isGeneralMember) {
                    chandaPayments.filter { it.memberId == savedMemberId }.ifEmpty { chandaPayments.take(2) }
                } else chandaPayments
                val myPaidTotal = displayedPayments.sumOf { it.amountPaid }

                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPrimary)
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
                                        text = if (isGeneralMember) "আমার ব্যক্তিগত চাঁদা স্থিতি" else "মাসিক চাঁদা আদায় রেজিস্টার",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = if (isGeneralMember) {
                                            "৳${toBengaliDigits(String.format("%,.0f", myPaidTotal))} (পরিশোধিত)"
                                        } else {
                                            "মোট আদায়: ৳${toBengaliDigits(String.format("%,.0f", totalChandaCollected))}"
                                        },
                                        color = MosqueGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    if (isGeneralMember && (currentMemberProfile?.monthlyChanda ?: 0.0) > 0.0) {
                                        Text(
                                            text = "নির্ধারিত মাসিক চাঁদা: ৳${toBengaliDigits(String.format("%,.0f", currentMemberProfile!!.monthlyChanda))}",
                                            color = Color.White.copy(alpha = 0.82f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                if (canReceiveChanda) {
                                    Button(
                                        onClick = onOpenReceiveChanda,
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("chanda_receive_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueGold),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("চাঁদা গ্রহণ করুন", color = MosqueEmeraldDark, fontWeight = FontWeight.Bold)
                                    }
                                } else if (isGeneralMember && displayedPayments.isNotEmpty()) {
                                    Button(
                                        onClick = { selectedPaymentForReceipt = displayedPayments.first() },
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("chanda_latest_receipt_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueGold),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode2,
                                            contentDescription = null,
                                            tint = MosqueEmeraldDark,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("সর্বশেষ রসিদ", color = MosqueEmeraldDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (displayedPayments.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Default.ReceiptLong,
                                message = "এখনো কোনো চাঁদা পরিশোধের রেকর্ড পাওয়া যায়নি।"
                            )
                        }
                    } else {
                        items(displayedPayments, key = { it.id }) { payment ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedPaymentForReceipt = payment }
                                    .testTag("chanda_payment_card_${payment.id}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = payment.memberNameBn,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${toBengaliDigits(payment.month)} • রসিদ: ${toBengaliDigits(payment.receiptCode)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "জমার সময়: ${toBengaliDigits(payment.paidAt)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "৳${toBengaliDigits(String.format("%,.0f", payment.amountPaid))}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueSuccessGreen
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.QrCode,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "রসিদ দেখুন",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "staff" -> {
                // Staff Directory & Payroll / Personal Staff Compensation
                val isDark = LocalIsDarkTheme.current
                val cardBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
                val isStaffRole = currentRole in listOf(UserRole.IMAM, UserRole.MUAZZIN, UserRole.KHADEM)
                val canPaySalary = currentRole.canManageFinance()
                val canAddStaff = currentRole.canManageMembers() || currentRole.canManageFinance()
                val displayedStaff = if (isStaffRole) {
                    activeStaff.filter {
                        when (currentRole) {
                            UserRole.IMAM -> it.roleCode.equals("imam", ignoreCase = true)
                            UserRole.MUAZZIN -> it.roleCode.equals("muazzin", ignoreCase = true)
                            UserRole.KHADEM -> it.roleCode.equals("khadem", ignoreCase = true)
                            else -> true
                        }
                    }.ifEmpty { activeStaff.take(1) }
                } else activeStaff

                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isStaffRole) "আমার দায়িত্ব ও বেতন বিবরণী" else "মসজিদের সম্মানিত স্টাফবৃন্দ (${toBengaliDigits(activeStaff.size.toString())} জন)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (canAddStaff) {
                                Button(
                                    onClick = { showAddStaffDialog = true },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("add_staff_button"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                ) {
                                    Text("+ নতুন স্টাফ", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    items(displayedStaff, key = { it.id }) { staff ->
                        val latestPayroll = payrolls.filter { it.staffId == staff.id }.lastOrNull()
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, cardBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = staff.nameBn,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = staff.designationBn,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MosqueSuccessGreen.copy(alpha = 0.14f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "সক্রিয়",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MosqueSuccessGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = cardBorder)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("যোগদান: ${toBengaliDigits(staff.joinDate)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("মোবাইল: ${toBengaliDigits(staff.mobile)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("মাসিক বেতন ভাতা", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "৳${toBengaliDigits(String.format("%,.0f", staff.salaryAmount))}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                if (canPaySalary) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { showPaySalaryStaff = staff },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 48.dp)
                                            .testTag("pay_salary_${staff.id}"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("বেতন পরিশোধ করুন (পে-রোল)", fontSize = 12.sp)
                                    }
                                } else if (isStaffRole) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    val paidBg = if (latestPayroll != null) MosqueSuccessGreen.copy(alpha = 0.12f) else MosqueInfoBlue.copy(alpha = 0.12f)
                                    val paidColor = if (latestPayroll != null) MosqueSuccessGreen else MosqueInfoBlue
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(paidBg)
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                if (latestPayroll != null) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = paidColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (latestPayroll != null) {
                                                    "সর্বশেষ বেতন (${toBengaliDigits(latestPayroll.month)}) পরিশোধিত • ৳${toBengaliDigits(String.format("%,.0f", latestPayroll.netSalary))}"
                                                } else {
                                                    "মাসিক বেতন ভাতা সক্রিয় (রসিদ পে-রোল অপেক্ষমাণ)"
                                                },
                                                color = paidColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "prayer" -> {
                // Detailed Prayer Schedule
                val isDark = LocalIsDarkTheme.current
                val cardBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
                val (_, nextSummary) = com.mosqueseba.app.ui.components.computeTodayPrayerSchedule()
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPrimary)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "পরবর্তী ওয়াক্ত: ${nextSummary.nameBn}",
                                            color = MosqueGold,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "জামাত: ${nextSummary.iqamahTimeBn} • বাকি ${nextSummary.remainingTextBn}",
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 12.sp
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White.copy(alpha = 0.16f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccessTime,
                                            contentDescription = "ওয়াক্ত ও জামাত",
                                            tint = MosqueGoldLight,
                                            modifier = Modifier.padding(8.dp).size(20.dp)
                                        )
                                    }
                                }
                                if (currentRole == UserRole.MUAZZIN) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "মুয়াজ্জিন নির্দেশিকা: সময়মতো আজান-ইকামত ও মাইক/সাউন্ড সিস্টেম যাচাই নিশ্চিত করুন",
                                        color = MosqueGoldLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, cardBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "দৈনিক ওয়াক্ত ও জামাত সূচি",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                PrayerRowDetail("ফজর", "০৪:৩০", "০৫:০০", "সূর্যোদয়: ০৫:৪২")
                                PrayerRowDetail("যোহর", "১২:৩০", "০১:১৫", "ওয়াক্ত শেষ: ০৪:১৬")
                                PrayerRowDetail("আসর", "০৪:৩০", "০৪:৪৫", "ওয়াক্ত শেষ: ০৬:১৮")
                                PrayerRowDetail("মাগরিব", "০৬:১৮", "০৬:২৩", "ওয়াক্ত শেষ: ০৭:৩৪")
                                PrayerRowDetail("ইশা", "০৭:৪৫", "০৮:১৫", "ওয়াক্ত শেষ: ১১:৪৫")
                                PrayerRowDetail("জুমুআ (শুক্রবার)", "১২:৩০", "০১:১৫", "বয়ান: ১২:৪০")
                            }
                        }
                    }
                }
            }
            "roster" -> {
                // Duty Roster & Khadem Daily Khidmat Checklist
                val isDark = LocalIsDarkTheme.current
                val cardBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (currentRole == UserRole.KHADEM) {
                        val khademTasks = listOf(
                            Triple(
                                "khadem_hall",
                                "মূল ভবন ও নামাজের কাতার পরিষ্কার",
                                "ফজর ও জুমার আগে কার্পেট ও কাতার পরিচ্ছন্ন রাখা"
                            ),
                            Triple(
                                "khadem_wudu",
                                "অজুখানা ও টয়লেট পরিচ্ছন্নতা",
                                "অজুখানার টেপ, মেঝে ও পানি নিষ্কাশন নিয়মিত তদারকি"
                            ),
                            Triple(
                                "khadem_utilities",
                                "আলো, ফ্যান ও এসি ব্যবস্থা যাচাই",
                                "জামাতের পূর্বে বাতি, ফ্যান ও এসি চালু/বন্ধ নিশ্চিত করা"
                            ),
                            Triple(
                                "khadem_water",
                                "পানীয় জল ও মুসল্লি খেদমত",
                                "বিশুদ্ধ পানির জোগান, জুতার র‍্যাক ও পরিবেশ শৃঙ্খলা রক্ষা"
                            )
                        )
                        val doneCount = khademTasks.count { it.first in khademCheckedTasks }
                        item(key = "khadem_daily_checklist") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MosqueCardBorderGold.copy(alpha = 0.7f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CleaningServices,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "আজকের খেদমত ও পরিচ্ছন্নতা চেকলিস্ট",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (doneCount == khademTasks.size) {
                                                MosqueSuccessGreen.copy(alpha = 0.14f)
                                            } else {
                                                MosqueGold.copy(alpha = 0.18f)
                                            }
                                        ) {
                                            Text(
                                                text = "${toBengaliDigits(doneCount.toString())}/${toBengaliDigits(khademTasks.size.toString())} সম্পন্ন",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (doneCount == khademTasks.size) {
                                                    MosqueSuccessGreen
                                                } else if (isDark) {
                                                    MosqueGold
                                                } else {
                                                    MosqueGoldDeep
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    khademTasks.forEachIndexed { idx, (taskId, title, subtitle) ->
                                        val isChecked = taskId in khademCheckedTasks
                                        if (idx > 0) Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            onClick = { viewModel.toggleKhademTask(taskId) },
                                            shape = RoundedCornerShape(10.dp),
                                            color = when {
                                                isChecked && isDark -> MosqueEmeraldDark.copy(alpha = 0.35f)
                                                isChecked -> MosqueEmeraldSoft.copy(alpha = 0.65f)
                                                isDark -> MosqueNightSurfaceVariant
                                                else -> MosqueBgLight
                                            },
                                            border = BorderStroke(
                                                1.dp,
                                                if (isChecked) MosqueEmeraldPrimary.copy(alpha = 0.45f) else cardBorder
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 52.dp)
                                                .testTag("khadem_task_$taskId")
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = isChecked,
                                                    onCheckedChange = null,
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = MosqueEmeraldPrimary,
                                                        checkmarkColor = Color.White
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = title,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = subtitle,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ওয়াক্ত ও নামাজের দায়িত্ব বণ্টন",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            val canAssignRoster = currentRole.canScheduleMeetings() || currentRole == UserRole.IMAM
                            if (canAssignRoster) {
                                Button(
                                    onClick = { showAddRosterDialog = true },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("add_roster_button"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                ) {
                                    Text("+ দায়িত্ব নির্ধারণ", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    if (dutyRosters.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Default.EventNote,
                                message = "এখনো কোনো ওয়াক্ত বা নামাজের দায়িত্ব রোস্টার নির্ধারিত হয়নি।"
                            )
                        }
                    } else {
                        items(dutyRosters, key = { it.id }) { roster ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = roster.prayerName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = toBengaliDigits(roster.dayOfWeek),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isDark) MosqueGold else MosqueGoldDeep,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "দায়িত্বপ্রাপ্ত: ${roster.staffNameBn} (${roster.roleTitle})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (roster.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "বিশেষ দ্রষ্টব্য: ${toBengaliDigits(roster.notes)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "food" -> {
                val isDark = LocalIsDarkTheme.current
                val cardBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
                val canAssignFood = currentRole.canScheduleMeetings() || currentRole == UserRole.IMAM
                val canMarkDelivered = canAssignFood ||
                    currentRole == UserRole.MUAZZIN ||
                    currentRole == UserRole.KHADEM

                val orderedWillingDonors = remember(allMembers, foodSchedules) {
                    viewModel.getOrderedWillingMealDonors(allMembers, foodSchedules)
                }
                val mealRequiringStaff = remember(activeStaff) {
                    activeStaff.filter { it.mealRequired }
                }
                val isoFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH) }
                val todayIso = remember { isoFormat.format(Date()) }
                val tomorrowIso = remember {
                    java.util.Calendar.getInstance().apply {
                        add(java.util.Calendar.DAY_OF_YEAR, 1)
                    }.let { isoFormat.format(it.time) }
                }

                val savedMemberId = remember(currentRole) { viewModel.sessionManager.getSavedMemberId() }
                val savedRole = remember(currentRole) { viewModel.sessionManager.getSavedRole() }
                val currentUserMember = remember(currentRole, allMembers, savedRole, savedMemberId) {
                    allMembers.firstOrNull { it.id == savedMemberId && currentRole == savedRole }
                        ?: allMembers.firstOrNull { it.roleCode.equals(currentRole.code, ignoreCase = true) }
                }
                val myTomorrowPendingMeals = remember(foodSchedules, tomorrowIso, currentUserMember) {
                    val myDigits = currentUserMember?.mobile?.filter { it.isDigit() }.orEmpty()
                    val myName = currentUserMember?.nameBn?.trim().orEmpty()
                    if (myName.isBlank() && myDigits.length < 6) emptyList()
                    else foodSchedules.filter { food ->
                        val isTomorrow = viewModel.normalizeDigitsToAscii(food.dateStr).trim() == tomorrowIso
                        val isPending = food.status == "scheduled"
                        val foodDigits = viewModel.normalizeDigitsToAscii(food.providerMobile).filter { it.isDigit() }
                        val matchesMe = (myDigits.length >= 6 && foodDigits == myDigits) ||
                            (myName.isNotBlank() && food.providerName.trim().equals(myName, ignoreCase = true))
                        isTomorrow && isPending && matchesMe
                    }
                }

                val todayMeals = remember(foodSchedules, todayIso) {
                    foodSchedules.filter {
                        viewModel.normalizeDigitsToAscii(it.dateStr).trim() == todayIso && it.status != "cancelled"
                    }
                }
                val activeCount = remember(foodSchedules) {
                    foodSchedules.count { it.status == "scheduled" || it.status == "approved" || it.status == "rejected" }
                }
                val deliveredCount = remember(foodSchedules) { foodSchedules.count { it.status == "delivered" } }
                val cancelledCount = remember(foodSchedules) { foodSchedules.count { it.status == "cancelled" } }

                var foodFilter by remember { mutableStateOf("active_all") } // active_all, delivered, cancelled, all
                val displayedFoods = remember(foodSchedules, foodFilter) {
                    when (foodFilter) {
                        "active_all" -> foodSchedules
                            .filter { it.status == "scheduled" || it.status == "approved" || it.status == "rejected" }
                            .sortedBy { viewModel.normalizeDigitsToAscii(it.dateStr).trim() }
                        "delivered" -> foodSchedules.filter { it.status == "delivered" }
                        "cancelled" -> foodSchedules.filter { it.status == "cancelled" }
                        else -> foodSchedules.sortedByDescending { viewModel.normalizeDigitsToAscii(it.dateStr).trim() }
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "ইমাম ও খাদেমদের খাবার শিডিউল",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "দৈনিক ১ সদস্য → ১ স্টাফ • ১ দিন আগে সম্মতি নোটিফিকেশন",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (canAssignFood) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { showAutoFoodDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("open_auto_food_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "অটো শিডিউল তৈরি করুন",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("অটো শিডিউল", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { showAddFoodDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("open_single_food_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "একক খাবার বণ্টন",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+ একক বণ্টন", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    // 1-Day-Ahead Personal Approval Prompt Banner (if logged-in member is scheduled for tomorrow)
                    if (myTomorrowPendingMeals.isNotEmpty()) {
                        items(myTomorrowPendingMeals, key = { "tomorrow_confirm_${it.id}" }) { myMeal ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueGoldLight.copy(alpha = 0.22f)
                                ),
                                border = BorderStroke(1.5.dp, MosqueGoldDeep)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = if (isDark) MosqueGold else MosqueGoldDeep,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "আগামীকাল খাবারের দায়িত্ব (${toBengaliDigits(myMeal.dateStr)})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "আগামীকাল আপনাকে ${myMeal.receiverStaffName}-এর সারাদিনের খাবার দিতে হবে। অনুগ্রহ করে সম্মতি বা অপারগতা জানান।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.rejectFoodSchedule(myMeal) { reassignedTo ->
                                                    val msg = if (reassignedTo != null) {
                                                        "অপারগতা জানানো হয়েছে। স্বয়ংক্রিয়ভাবে পরবর্তী দাতা ($reassignedTo)-কে দায়িত্ব দেওয়া হয়েছে।"
                                                    } else {
                                                        "অপারগতা জানানো হয়েছে। বিকল্প দাতা নির্ধারণের জন্য কমিটিকে সতর্কবার্তা পাঠানো হয়েছে।"
                                                    }
                                                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, MosqueUrgentRed),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueUrgentRed),
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 48.dp)
                                                .testTag("food_reject_tomorrow_${myMeal.id}")
                                        ) {
                                            Text("পারব না", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = {
                                                viewModel.approveFoodSchedule(myMeal)
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "আগামীকালের খাবারের দায়িত্বে আপনার সম্মতি নিশ্চিত হয়েছে।",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .heightIn(min = 48.dp)
                                                .testTag("food_approve_tomorrow_${myMeal.id}")
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("সম্মতি দিন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Automation Pool Summary Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("রাজি দাতা", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${toBengaliDigits(orderedWillingDonors.size.toString())} জন",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("প্রাপক স্টাফ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${toBengaliDigits(mealRequiringStaff.size.toString())} জন",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = if (isDark) MosqueGold else MosqueGoldDeep
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("বিতরণকৃত", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${toBengaliDigits(deliveredCount.toString())} টি",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = MosqueSuccessGreen
                                    )
                                }
                            }
                        }
                    }

                    // Today's Highlight Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft
                            ),
                            border = BorderStroke(1.dp, MosqueEmeraldPrimary.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Today,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "আজকের খাবার সূচি (${toBengaliDigits(todayIso)})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    orderedWillingDonors.firstOrNull()?.let { nextDonor ->
                                        Text(
                                            text = "পরবর্তী পালা: ${nextDonor.nameBn}",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                if (todayMeals.isEmpty()) {
                                    Text(
                                        text = "আজকের জন্য এখনো কোনো খাবারের পালা নির্ধারিত নেই।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (canAssignFood && orderedWillingDonors.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                val staffLabels = if (mealRequiringStaff.isEmpty()) {
                                                    listOf("পেশ ইমাম", "মুয়াজ্জিন", "প্রধান খাদেম")
                                                } else {
                                                    mealRequiringStaff.map { "${it.nameBn} (${it.designationBn})" }
                                                }
                                                viewModel.autoGenerateFoodSchedule(
                                                    daysCount = 7,
                                                    startDateIso = todayIso,
                                                    targetStaffLabels = staffLabels
                                                ) { created, _ ->
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "আজ থেকে ৭ দিনের ${toBengaliDigits(created.toString())} টি দৈনিক খাবারের পালা তৈরি হয়েছে।",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .testTag("quick_auto_7days_button")
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("১-ট্যাপে ৭ দিনের অটো শিডিউল তৈরি করুন", fontSize = 12.sp)
                                        }
                                    }
                                } else {
                                    todayMeals.forEachIndexed { idx, meal ->
                                        if (idx > 0) Spacer(modifier = Modifier.height(6.dp))
                                        val badgeLabel = when (meal.status) {
                                            "delivered" -> "বিতরণকৃত"
                                            "approved" -> "সম্মত"
                                            "rejected" -> "বিকল্প প্রয়োজন"
                                            else -> "অপেক্ষমাণ"
                                        }
                                        val badgeColor = when (meal.status) {
                                            "delivered" -> MosqueSuccessGreen
                                            "approved" -> MosqueEmeraldPrimary
                                            "rejected" -> MosqueUrgentRed
                                            else -> MosqueInfoBlue
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isDark) MosqueNightSurface else Color.White.copy(alpha = 0.85f))
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "প্রাপক: ${meal.receiverStaffName}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "দাতা: ${meal.providerName} (${toBengaliDigits(meal.providerMobile)})",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = badgeColor.copy(alpha = 0.14f)
                                            ) {
                                                Text(
                                                    text = badgeLabel,
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = badgeColor,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Filter Chips
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val tabs = listOf(
                                "active_all" to "চলমান ও আসন্ন (${toBengaliDigits(activeCount.toString())})",
                                "delivered" to "বিতরণকৃত (${toBengaliDigits(deliveredCount.toString())})",
                                "cancelled" to "বাতিল (${toBengaliDigits(cancelledCount.toString())})",
                                "all" to "সকল (${toBengaliDigits(foodSchedules.size.toString())})"
                            )
                            items(tabs) { (key, label) ->
                                FilterChip(
                                    selected = foodFilter == key,
                                    onClick = { foodFilter = key },
                                    label = { Text(label, fontSize = 11.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MosqueEmeraldPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    if (displayedFoods.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestaurantMenu,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "এই ক্যাটাগরিতে কোনো খাবার শিডিউল নেই",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    items(displayedFoods, key = { it.id }) { food ->
                        val isDelivered = food.status == "delivered"
                        val isApproved = food.status == "approved"
                        val isRejected = food.status == "rejected"
                        val isCancelled = food.status == "cancelled"
                        val statusColor = when {
                            isDelivered -> MosqueSuccessGreen
                            isApproved -> MosqueEmeraldPrimary
                            isRejected -> MosqueUrgentRed
                            isCancelled -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MosqueInfoBlue
                        }
                        val statusLabel = when {
                            isDelivered -> "বিতরণকৃত"
                            isApproved -> "সম্মত"
                            isRejected -> "বিকল্প প্রয়োজন"
                            isCancelled -> "বাতিল"
                            else -> "অপেক্ষমাণ"
                        }
                        val normDate = viewModel.normalizeDigitsToAscii(food.dateStr).trim()
                        val isTodaySlot = normDate == todayIso
                        val isTomorrowSlot = normDate == tomorrowIso

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = when {
                                isRejected -> BorderStroke(1.2.dp, MosqueUrgentRed.copy(alpha = 0.6f))
                                isTodaySlot && !isCancelled -> BorderStroke(1.dp, MosqueEmeraldPrimary.copy(alpha = 0.4f))
                                else -> BorderStroke(1.dp, cardBorder)
                            }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "তারিখ: ${toBengaliDigits(food.dateStr)}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (isTodaySlot || isTomorrowSlot) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MosqueGoldLight.copy(alpha = if (isDark) 0.18f else 0.3f)
                                            ) {
                                                Text(
                                                    text = if (isTodaySlot) "আজ" else "আগামীকাল",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isDark) MosqueGold else MosqueGoldDeep,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(statusColor.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = statusLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = statusColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "প্রাপক স্টাফ: ${food.receiverStaffName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "খাবার দাতা: ${food.providerName} (${toBengaliDigits(food.providerMobile)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Interactive Automation & Status Action Row
                                if ((!isDelivered && !isCancelled && (canMarkDelivered || canAssignFood)) ||
                                    (isCancelled && canAssignFood)
                                ) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = cardBorder.copy(alpha = 0.6f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (!isDelivered && !isCancelled && !isRejected && canMarkDelivered) {
                                            Button(
                                                onClick = {
                                                    viewModel.markFoodScheduleDelivered(food)
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "${food.providerName}-এর খাবার বিতরণ সম্পন্ন হিসেবে চিহ্নিত হয়েছে।",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MosqueSuccessGreen),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .heightIn(min = 48.dp)
                                                    .testTag("mark_food_delivered_${food.id}")
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("বিতরণ সম্পন্ন", fontSize = 11.5.sp)
                                            }
                                        }

                                        if (!isDelivered && !isCancelled && canAssignFood && orderedWillingDonors.size > 1) {
                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.rotateFoodScheduleDonor(food) { updated ->
                                                        if (updated != null) {
                                                            android.widget.Toast.makeText(
                                                                context,
                                                                "দাতা স্বয়ংক্রিয়ভাবে পরিবর্তিত হয়েছে: ${updated.providerName}",
                                                                android.widget.Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, MosqueGoldDeep),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueGoldDeep),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .heightIn(min = 48.dp)
                                                    .testTag("rotate_food_donor_${food.id}")
                                            ) {
                                                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("পরবর্তী দাতা", fontSize = 11.5.sp)
                                            }
                                        }

                                        if (!isDelivered && !isCancelled && canAssignFood) {
                                            OutlinedButton(
                                                onClick = { pendingCancelFood = food },
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, MosqueUrgentRed.copy(alpha = 0.6f)),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueUrgentRed),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .heightIn(min = 48.dp)
                                                    .testTag("cancel_food_${food.id}")
                                            ) {
                                                Text("বাতিল", fontSize = 11.5.sp)
                                            }
                                        }

                                        if (isCancelled && canAssignFood) {
                                            OutlinedButton(
                                                onClick = { pendingDeleteFood = food },
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, MosqueUrgentRed.copy(alpha = 0.6f)),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueUrgentRed),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .heightIn(min = 48.dp)
                                                    .testTag("delete_food_${food.id}")
                                            ) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("মুছুন", fontSize = 11.5.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                pendingCancelFood?.let { target ->
                    MosqueConfirmDialog(
                        title = "খাবারের পালা বাতিল করবেন?",
                        message = "${target.providerName}-এর ${toBengaliDigits(target.dateStr)} তারিখের খাবারের পালা বাতিল করা হবে।",
                        confirmText = "হ্যাঁ, বাতিল করুন",
                        icon = Icons.Default.EventBusy,
                        isDestructive = true,
                        onDismiss = { pendingCancelFood = null },
                        onConfirm = {
                            viewModel.cancelFoodSchedule(target)
                            pendingCancelFood = null
                            android.widget.Toast.makeText(
                                context,
                                "খাবারের পালা বাতিল করা হয়েছে।",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }

                pendingDeleteFood?.let { target ->
                    MosqueConfirmDialog(
                        title = "বাতিলকৃত পালা মুছবেন?",
                        message = "${target.providerName}-এর ${toBengaliDigits(target.dateStr)} তারিখের পালা তালিকা থেকে স্থায়ীভাবে মুছে ফেলা হবে।",
                        confirmText = "হ্যাঁ, মুছে ফেলুন",
                        icon = Icons.Default.DeleteForever,
                        isDestructive = true,
                        onDismiss = { pendingDeleteFood = null },
                        onConfirm = {
                            viewModel.deleteFoodSchedule(target)
                            pendingDeleteFood = null
                            android.widget.Toast.makeText(
                                context,
                                "খাবারের পালা তালিকা থেকে মুছে ফেলা হয়েছে।",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
            "meetings" -> {
                val canScheduleMeeting = currentRole.canScheduleMeetings()
                val mosqueName = mosque?.nameBn ?: "বায়তুল জান্নাত জামে মসজিদ"
                // Meetings & Agenda
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPrimary)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "নির্ধারিত কমিটি সভা",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        text = "${toBengaliDigits(allMeetings.size.toString())} টি সভা নথিবদ্ধ",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueGold
                                    )
                                }
                                if (canScheduleMeeting) {
                                    Button(
                                        onClick = onOpenScheduleMeeting,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueGold)
                                    ) {
                                        Text(
                                            text = "+ সভা আহ্বান",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueEmeraldDark
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (allMeetings.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "বর্তমানে কোনো নির্ধারিত সভা নেই",
                                    color = MosqueTextSecondary
                                )
                            }
                        }
                    } else {
                        items(allMeetings, key = { it.id }) { meeting ->
                            MeetingCardItem(
                                meeting = meeting,
                                canManage = canScheduleMeeting,
                                canEditResolution = canScheduleMeeting || currentRole.canManageOfficeDocs(),
                                onToggleStatus = {
                                    viewModel.toggleMeetingStatus(meeting)
                                    android.widget.Toast.makeText(
                                        context,
                                        if (meeting.status == "completed") "সভা আসন্ন হিসেবে চিহ্নিত হয়েছে।" else "সভা সম্পন্ন হিসেবে চিহ্নিত হয়েছে।",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onEdit = { editingMeeting = meeting },
                                onEditResolution = { resolutionTargetMeeting = meeting },
                                onShare = { shareMeetingText(context, mosqueName, meeting) },
                                onDeleteRequest = { pendingDeleteMeeting = meeting }
                            )
                        }
                    }
                }

                editingMeeting?.let { target ->
                    ScheduleMeetingDialog(
                        editingMeeting = target,
                        onDismiss = { editingMeeting = null },
                        onConfirm = { title, dateStr, location, agenda ->
                            viewModel.updateMeeting(
                                meeting = target,
                                title = title,
                                dateStr = dateStr,
                                location = location,
                                agenda = agenda
                            )
                            android.widget.Toast.makeText(
                                context,
                                "সভার তথ্য সফলভাবে হালনাগাদ হয়েছে।",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }

                resolutionTargetMeeting?.let { target ->
                    MeetingResolutionDialog(
                        meeting = target,
                        onDismiss = { resolutionTargetMeeting = null },
                        onConfirm = { resolution, markCompleted ->
                            viewModel.saveMeetingResolution(
                                meeting = target,
                                resolution = resolution,
                                markCompleted = markCompleted
                            )
                            android.widget.Toast.makeText(
                                context,
                                "সভার কার্যবিবরণী সংরক্ষিত হয়েছে।",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }

                pendingDeleteMeeting?.let { target ->
                    MosqueConfirmDialog(
                        title = "সভা মুছে ফেলবেন?",
                        message = "\"${target.title}\" সভাটি তালিকা থেকে মুছে ফেলা হবে। আপনি কি নিশ্চিত?",
                        confirmText = "হ্যাঁ, মুছে ফেলুন",
                        icon = Icons.Default.DeleteForever,
                        isDestructive = true,
                        onDismiss = { pendingDeleteMeeting = null },
                        onConfirm = {
                            viewModel.deleteMeeting(target)
                            pendingDeleteMeeting = null
                            android.widget.Toast.makeText(
                                context,
                                "সভাটি তালিকা থেকে মুছে ফেলা হয়েছে।",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
            "welfare" -> {
                val isDark = LocalIsDarkTheme.current
                val cardBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
                val canDisburse = currentRole.canManageWelfareEducation()
                var statusFilter by remember { mutableStateOf("সকল") }
                val pendingWelfareCount = remember(welfareCases) { welfareCases.count { it.status != "disbursed" } }
                val filteredWelfareCases = remember(welfareCases, statusFilter) {
                    when (statusFilter) {
                        "বিতরণ অপেক্ষায়" -> welfareCases.filter { it.status != "disbursed" }
                        "বিতরণকৃত" -> welfareCases.filter { it.status == "disbursed" }
                        else -> welfareCases
                    }
                }
                // Welfare & Zakat Management (PRD §26)
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPrimary)
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
                                        text = "যাকাত ও সমাজকল্যাণ বিতরণ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    val totalDisbursed = welfareCases.sumOf { it.amountApproved }
                                    Text(
                                        text = "৳ ${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", totalDisbursed))}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueGold
                                    )
                                    if (pendingWelfareCount > 0) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MosqueWarningAmber.copy(alpha = 0.22f)
                                        ) {
                                            Text(
                                                text = "বিতরণ বাকি: ${toBengaliDigits(pendingWelfareCount.toString())} টি আবেদন",
                                                color = MosqueGoldLight,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Button(
                                    onClick = { showAddWelfareDialog = true },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("add_welfare_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueGold)
                                ) {
                                    Text(
                                        text = "+ অনুদান আবেদন",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueEmeraldDark
                                    )
                                }
                            }
                        }
                    }

                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf("সকল", "বিতরণ অপেক্ষায়", "বিতরণকৃত")) { filter ->
                                FilterChip(
                                    selected = statusFilter == filter,
                                    onClick = { statusFilter = filter },
                                    label = { Text(filter, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    if (filteredWelfareCases.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolunteerActivism,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(38.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "এই শ্রেণিতে কোনো সমাজকল্যাণ আবেদন নেই।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredWelfareCases, key = { it.id }) { item ->
                            val isDisbursed = item.status == "disbursed"
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.beneficiaryName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (isDisbursed) MosqueSuccessGreen.copy(alpha = 0.14f)
                                                    else MosqueGold.copy(alpha = 0.18f)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = if (isDisbursed) "বিতরণকৃত" else "অনুমোদিত",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isDisbursed) MosqueSuccessGreen else MosqueGoldDeep,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "খাত: ${item.category}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "অনুমোদিত: ৳${toBengaliDigits(item.amountApproved.toInt().toString())}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "মোবাইল: ${toBengaliDigits(item.mobile)}  •  তারিখ: ${toBengaliDigits(item.applicationDate)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (item.remarks.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "মন্তব্য: ${item.remarks}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    if (canDisburse && !isDisbursed) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.markWelfareDisbursed(item)
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "${item.beneficiaryName}-এর সহায়তা বিতরণ সম্পন্ন হিসেবে চিহ্নিত হয়েছে।",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 48.dp)
                                                .testTag("disburse_welfare_${item.id}"),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, MosqueSuccessGreen)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircleOutline,
                                                contentDescription = null,
                                                tint = MosqueSuccessGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "বিতরণ সম্পন্ন নিশ্চিত করুন",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MosqueSuccessGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "events" -> {
                val isDark = LocalIsDarkTheme.current
                val cardBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
                // Mosque Events & Programs (PRD §25)
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPrimary)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "মসজিদ মাহফিল ও অনুষ্ঠান",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        text = "${toBengaliDigits(events.size.toString())} টি ইভেন্ট তালিকাভুক্ত",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueGold
                                    )
                                }
                                val canAddEvent = currentRole.canPublishNotices() || currentRole == UserRole.WELFARE_EDUCATION_SECRETARY
                                if (canAddEvent) {
                                    Button(
                                        onClick = { showAddEventDialog = true },
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("add_event_button"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueGold)
                                    ) {
                                        Text(
                                            text = "+ নতুন ইভেন্ট",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueEmeraldDark
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (events.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Default.Celebration,
                                message = "বর্তমানে কোনো আসন্ন অনুষ্ঠান বা মাহফিল তালিকাভুক্ত নেই।"
                            )
                        }
                    } else {
                        items(events, key = { it.id }) { ev ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = ev.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (ev.status == "completed") MosqueSuccessGreen.copy(alpha = 0.14f)
                                                    else MosqueGold.copy(alpha = 0.18f)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (ev.status == "completed") "সম্পন্ন" else "আসন্ন",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (ev.status == "completed") {
                                                    MosqueSuccessGreen
                                                } else if (isDark) {
                                                    MosqueGold
                                                } else {
                                                    MosqueGoldDeep
                                                },
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "তারিখ ও সময়: ${toBengaliDigits(ev.dateStr)} (${toBengaliDigits(ev.timeStr)})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "বক্তা/অতিথি: ${ev.speakerOrGuest}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "স্থান: ${ev.venue}  •  বাজেট: ৳${toBengaliDigits(ev.budget.toInt().toString())}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "স্বেচ্ছাসেবক প্রধান: ${ev.volunteerLead}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            "audit" -> {
                val isDark = LocalIsDarkTheme.current
                val cardBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
                // Audit Logs
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPrimary)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "স্বচ্ছতা ও সিস্টেম অডিট রেকর্ড",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "${toBengaliDigits(auditLogs.size.toString())} টি কার্যক্রম নথিবদ্ধ",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueGold
                                )
                            }
                        }
                    }

                    if (auditLogs.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FactCheck,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(38.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "এখনো কোনো অডিট লগ রেকর্ড নেই।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(auditLogs, key = { it.id }) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = log.action,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        Text(
                                            text = toBengaliDigits(log.timestamp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = log.details,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "কর্তা: ${log.actor}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            "about" -> {
                MosqueProfileScreen(
                    viewModel = viewModel,
                    showTopBar = false,
                    onOpenMyProfile = onOpenMyProfile
                )
            }
            else -> {
                MoreOperationsHub(
                    currentRole = currentRole,
                    liveCounts = liveCounts,
                    onSelectModule = { moduleKey -> selectedSubModule = moduleKey }
                )
            }
        }
    }

    // Receipt Dialog
    selectedPaymentForReceipt?.let { payment ->
        OfficialReceiptDialog(
            payment = payment,
            mosqueName = mosque?.nameBn ?: "বায়তুল জান্নাত জামে মসজিদ",
            mosqueAddress = mosque?.address ?: "মিরপুর-১০, ঢাকা",
            committeeId = mosque?.committeeId ?: "MC-DHK-2024",
            mosqueLogoUrl = mosque?.logoUrl,
            onDismiss = { selectedPaymentForReceipt = null }
        )
    }

    // Add Staff via Unified Member Form (Permanent MEM ID & Default Rules)
    if (showAddStaffDialog) {
        AddMemberDialog(
            initialRoleCode = "imam",
            defaultMosqueAddress = mosque?.address ?: "",
            onDismiss = { showAddStaffDialog = false },
            onConfirm = { nameBn, nameEn, mobile, email, nid, address, villageArea, roleCode, monthlyChanda, photoUrl, willingToProvideStaffMeals, salaryAmount, staffMealRequired ->
                viewModel.addMember(
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
                showAddStaffDialog = false
                android.widget.Toast.makeText(
                    context,
                    "$nameBn-কে তালিকাভুক্ত করা হয়েছে।",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // Pay Salary Dialog
    showPaySalaryStaff?.let { staff ->
        PaySalaryFormDialog(
            staff = staff,
            onDismiss = { showPaySalaryStaff = null },
            onConfirm = { month, bonus, paymentMethod ->
                viewModel.disburseSalary(
                    staff = staff,
                    month = month,
                    bonus = bonus,
                    paymentMethod = paymentMethod,
                    onDisbursed = { payroll ->
                        android.widget.Toast.makeText(
                            context,
                            "${staff.nameBn}-এর বেতন ৳${toBengaliDigits(payroll.netSalary.toInt().toString())} পরিশোধিত এবং স্বয়ংক্রিয়ভাবে মোট ব্যয়ের সাথে যুক্ত হয়েছে।",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                )
                showPaySalaryStaff = null
            }
        )
    }

    // Add Roster Dialog
    if (showAddRosterDialog && activeStaff.isNotEmpty()) {
        AddRosterFormDialog(
            activeStaff = activeStaff,
            onDismiss = { showAddRosterDialog = false },
            onConfirm = { staff, dayOfWeek, prayerName, roleTitle, notes ->
                viewModel.addDutyRoster(staff, dayOfWeek, prayerName, roleTitle, notes)
                showAddRosterDialog = false
                android.widget.Toast.makeText(
                    context,
                    "${staff.nameBn}-এর দায়িত্ব রোস্টার সংরক্ষিত হয়েছে।",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // Add Food Schedule Dialog
    if (showAddFoodDialog) {
        val orderedDonors = remember(allMembers, foodSchedules) {
            viewModel.getOrderedWillingMealDonors(allMembers, foodSchedules)
        }
        val mealStaff = remember(activeStaff) { activeStaff.filter { it.mealRequired } }
        AddFoodScheduleFormDialog(
            willingDonors = orderedDonors,
            mealStaff = mealStaff,
            onDismiss = { showAddFoodDialog = false },
            onConfirm = { dateStr, providerName, providerMobile, receiverName ->
                viewModel.addFoodSchedule(dateStr, providerName, providerMobile, receiverName)
                showAddFoodDialog = false
                android.widget.Toast.makeText(
                    context,
                    "$providerName-এর খাবার শিডিউল সংরক্ষিত হয়েছে।",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // Auto Food Schedule Generator Dialog
    if (showAutoFoodDialog) {
        val orderedDonors = remember(allMembers, foodSchedules) {
            viewModel.getOrderedWillingMealDonors(allMembers, foodSchedules)
        }
        val mealStaff = remember(activeStaff) { activeStaff.filter { it.mealRequired } }
        AutoFoodScheduleDialog(
            willingDonors = orderedDonors,
            mealStaff = mealStaff,
            onDismiss = { showAutoFoodDialog = false },
            onGenerate = { daysCount, startDateIso, targetStaffLabels ->
                viewModel.autoGenerateFoodSchedule(
                    daysCount = daysCount,
                    startDateIso = startDateIso,
                    targetStaffLabels = targetStaffLabels
                ) { createdCount, skippedCount ->
                    showAutoFoodDialog = false
                    val msg = when {
                        createdCount > 0 && skippedCount > 0 ->
                            "স্বয়ংক্রিয়ভাবে ${toBengaliDigits(createdCount.toString())} টি নতুন পালা তৈরি হয়েছে (${toBengaliDigits(skippedCount.toString())} টি বিদ্যমান পালা বাদ রাখা হয়েছে)।"
                        createdCount > 0 ->
                            "স্বয়ংক্রিয়ভাবে ${toBengaliDigits(createdCount.toString())} টি খাবারের পালা তৈরি হয়েছে।"
                        else ->
                            "নির্বাচিত তারিখগুলোতে ইতোমধ্যেই সব পালা নির্ধারিত রয়েছে।"
                    }
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    // Add Welfare Dialog
    if (showAddWelfareDialog) {
        AddWelfareDialog(
            onDismiss = { showAddWelfareDialog = false },
            onConfirm = { beneficiaryName, category, mobile, amount, remarks ->
                viewModel.addWelfareCase(beneficiaryName, category, mobile, amount, remarks)
                showAddWelfareDialog = false
                android.widget.Toast.makeText(
                    context,
                    "$beneficiaryName-এর কল্যাণ সহায়তা রেকর্ড যুক্ত হয়েছে।",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // Add Event Dialog
    if (showAddEventDialog) {
        AddEventDialog(
            onDismiss = { showAddEventDialog = false },
            onConfirm = { title, dateStr, timeStr, speaker, venue, budget, lead ->
                viewModel.addEvent(title, dateStr, timeStr, speaker, venue, budget, lead)
                showAddEventDialog = false
                android.widget.Toast.makeText(
                    context,
                    "\"$title\" দ্বীনি আয়োজন সংরক্ষিত হয়েছে।",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

@Composable
fun OperationMenuItem(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    badgeText: String? = null,
    badgeIsAlert: Boolean = false,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val primaryColor = MaterialTheme.colorScheme.primary
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!badgeText.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                badgeIsAlert -> MosqueWarningAmber.copy(alpha = 0.14f)
                                isDark -> MosqueNightSurfaceVariant
                                else -> MosqueEmeraldSoft
                            }
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (badgeIsAlert) MosqueWarningAmber else primaryColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PrayerRowDetail(prayerName: String, azan: String, iqamah: String, note: String) {
    val isDark = LocalIsDarkTheme.current
    val dividerColor = if (isDark) MosqueNightBorder else MosqueCardBorder.copy(alpha = 0.5f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = prayerName,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("জামাত: $iqamah", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            Text("আজান: $azan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    HorizontalDivider(color = dividerColor)
}

