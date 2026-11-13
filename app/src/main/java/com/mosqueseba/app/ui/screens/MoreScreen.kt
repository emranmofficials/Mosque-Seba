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
import com.mosqueseba.app.data.local.entity.MeetingEntity
import com.mosqueseba.app.data.local.entity.StaffEntity
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.AddEventDialog
import com.mosqueseba.app.ui.components.AddFoodScheduleFormDialog
import com.mosqueseba.app.ui.components.AddMemberDialog
import com.mosqueseba.app.ui.components.AddRosterFormDialog
import com.mosqueseba.app.ui.components.AddWelfareDialog
import com.mosqueseba.app.ui.components.MeetingResolutionDialog
import com.mosqueseba.app.ui.components.OfficialReceiptDialog
import com.mosqueseba.app.ui.components.PaySalaryFormDialog
import com.mosqueseba.app.ui.components.ScheduleMeetingDialog
import com.mosqueseba.app.ui.components.toBengaliDigits
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel

@Composable
fun MoreScreen(
    viewModel: MosqueViewModel,
    initialSubModule: String? = null,
    onBack: (() -> Unit)? = null,
    onOpenReceiveChanda: () -> Unit,
    onOpenScheduleMeeting: () -> Unit,
    onOpenPublishNotice: () -> Unit = {},
    onOpenMyProfile: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedSubModule by remember(initialSubModule) { mutableStateOf<String?>(initialSubModule) }
    var selectedPaymentForReceipt by remember { mutableStateOf<ChandaPaymentEntity?>(null) }
    var showAddStaffDialog by remember { mutableStateOf(false) }
    var showPaySalaryStaff by remember { mutableStateOf<StaffEntity?>(null) }
    var showAddRosterDialog by remember { mutableStateOf(false) }
    var showAddFoodDialog by remember { mutableStateOf(false) }
    var showAddWelfareDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var editingMeeting by remember { mutableStateOf<MeetingEntity?>(null) }
    var resolutionTargetMeeting by remember { mutableStateOf<MeetingEntity?>(null) }
    var pendingDeleteMeeting by remember { mutableStateOf<MeetingEntity?>(null) }

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
        auditLogs
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
            auditLogsCount = auditLogs.size
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MosqueBgLight)
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (selectedSubModule) {
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
                                    "office_docs" -> "দপ্তর, দলিল-চুক্তি ও চিঠিপত্র"
                                    "sharia_advisory" -> "শরিয়াহ পর্যালোচনা ও উপদেষ্টা পরিষদ"
                                    "publicity" -> "প্রচার, দেয়াল উক্তি ও স্মার্ট বোর্ড"
                                    "notices" -> "নোটিশ বোর্ড ও ঘোষণা"
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
            "madrasa" -> {
                MadrasaScreen(viewModel = viewModel)
            }
            "chanda" -> {
                // Chanda Ledger / My Chanda
                val isGeneralMember = currentRole == UserRole.GENERAL_MEMBER
                val canReceiveChanda = currentRole.canManageFinance()
                val savedMemberId = viewModel.sessionManager.getSavedMemberId()
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
                                Column {
                                    Text(
                                        if (isGeneralMember) "আমার ব্যক্তিগত চাঁদা স্থিতি" else "মাসিক চাঁদা আদায় রেজিস্টার",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        if (isGeneralMember) {
                                            "৳${String.format("%,.0f", myPaidTotal)} (পরিশোধিত)"
                                        } else {
                                            "মোট আদায়: ৳${String.format("%,.0f", totalChandaCollected)}"
                                        },
                                        color = MosqueGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                                if (canReceiveChanda) {
                                    Button(
                                        onClick = onOpenReceiveChanda,
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueGold),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("চাঁদা গ্রহণ করুন", color = MosqueEmeraldDark, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    items(displayedPayments, key = { it.id }) { payment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedPaymentForReceipt = payment },
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = payment.memberNameBn,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueTextPrimary
                                    )
                                    Text(
                                        text = "${payment.month} • রসিদ: ${payment.receiptCode}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MosqueTextSecondary
                                    )
                                    Text(
                                        text = "জমার সময়: ${payment.paidAt}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MosqueTextMuted
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "৳${payment.amountPaid.toInt()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueSuccessGreen
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode,
                                            contentDescription = null,
                                            tint = MosqueEmeraldPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "রসিদ দেখুন",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MosqueEmeraldPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "staff" -> {
                // Staff Directory & Payroll / Personal Staff Compensation
                val isStaffRole = currentRole in listOf(UserRole.IMAM, UserRole.MUAZZIN, UserRole.KHADEM)
                val canManageStaff = currentRole.canManageFinance()
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
                                text = if (isStaffRole) "আমার দায়িত্ব ও বেতন বিবরণী" else "মসজিদের সম্মানিত স্টাফবৃন্দ (${activeStaff.size} জন)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldDark
                            )

                            if (canManageStaff) {
                                Button(
                                    onClick = { showAddStaffDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                ) {
                                    Text("+ নতুন স্টাফ", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    items(displayedStaff, key = { it.id }) { staff ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                            color = MosqueTextPrimary
                                        )
                                        Text(
                                            text = staff.designationBn,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MosqueEmeraldPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MosqueSuccessGreen.copy(alpha = 0.1f))
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
                                HorizontalDivider(color = MosqueCardBorder)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("যোগদান: ${staff.joinDate}", style = MaterialTheme.typography.labelSmall, color = MosqueTextMuted)
                                        Text("মোবাইল: ${staff.mobile}", style = MaterialTheme.typography.labelSmall, color = MosqueTextSecondary)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("মাসিক বেতন ভাতা", style = MaterialTheme.typography.labelSmall, color = MosqueTextMuted)
                                        Text(
                                            text = "৳${String.format("%,.0f", staff.salaryAmount)}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueEmeraldDark
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                if (canManageStaff) {
                                    Button(
                                        onClick = { showPaySalaryStaff = staff },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("বেতন পরিশোধ করুন (পে-রোল)", fontSize = 12.sp)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MosqueSuccessGreen.copy(alpha = 0.12f))
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MosqueSuccessGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "চলতি মাসের বেতন ও ভাতা পরিশোধিত (রসিদ সংগৃহীত)",
                                                color = MosqueSuccessGreen,
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
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "সেপ্টেম্বর ২০২৪ ওয়াক্ত ও জামাত সূচি",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueEmeraldDark
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
                // Duty Roster
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
                                text = "ওয়াক্ত ও নামাজের দায়িত্ব বণ্টন",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldDark
                            )
                            val canAssignRoster = currentRole.canScheduleMeetings() || currentRole == UserRole.IMAM
                            if (canAssignRoster) {
                                Button(
                                    onClick = { showAddRosterDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                ) {
                                    Text("+ দায়িত্ব নির্ধারণ", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    items(dutyRosters, key = { it.id }) { roster ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                        color = MosqueEmeraldDark
                                    )
                                    Text(
                                        text = roster.dayOfWeek,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MosqueGoldDeep,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "দায়িত্বপ্রাপ্ত: ${roster.staffNameBn} (${roster.roleTitle})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MosqueTextPrimary
                                )
                                if (roster.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "বিশেষ দ্রষ্টব্য: ${roster.notes}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MosqueTextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
            "food" -> {
                // Food Schedules
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
                                text = "ইমাম ও খাদেমদের খাবার শিডিউল",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldDark
                            )
                            val canAssignFood = currentRole.canScheduleMeetings() || currentRole == UserRole.IMAM
                            if (canAssignFood) {
                                Button(
                                    onClick = { showAddFoodDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                ) {
                                    Text("+ খাবার বণ্টন", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    items(foodSchedules, key = { it.id }) { food ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "তারিখ: ${food.dateStr} (${food.mealType})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueEmeraldDark
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MosqueSuccessGreen.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (food.status == "delivered") "বিতরণকৃত" else "নির্ধারিত",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MosqueSuccessGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "খাবার প্রেরক: ${food.providerName} (${food.providerMobile})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MosqueTextPrimary
                                )
                                Text(
                                    text = "প্রাপক: ${food.receiverStaffName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueTextSecondary
                                )
                            }
                        }
                    }
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
                                onToggleStatus = { viewModel.toggleMeetingStatus(meeting) },
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
                        }
                    )
                }

                pendingDeleteMeeting?.let { target ->
                    AlertDialog(
                        onDismissRequest = { pendingDeleteMeeting = null },
                        title = {
                            Text(
                                text = "সভা মুছে ফেলবেন?",
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldDark
                            )
                        },
                        text = {
                            Text("\"${target.title}\" সভাটি তালিকা থেকে মুছে ফেলা হবে। আপনি কি নিশ্চিত?")
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.deleteMeeting(target)
                                    pendingDeleteMeeting = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MosqueUrgentRed)
                            ) {
                                Text("হ্যাঁ, মুছে ফেলুন", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { pendingDeleteMeeting = null }) {
                                Text("বাতিল", color = MosqueTextSecondary)
                            }
                        }
                    )
                }
            }
            "welfare" -> {
                // Welfare & Zakat Management (PRD §26)
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
                                        text = "যাকাত ও সমাজকল্যাণ বিতরণ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    val totalDisbursed = welfareCases.sumOf { it.amountApproved }
                                    Text(
                                        text = "৳ ${String.format(Locale.getDefault(), "%,.0f", totalDisbursed)}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueGold
                                    )
                                }
                                Button(
                                    onClick = { showAddWelfareDialog = true },
                                    shape = RoundedCornerShape(8.dp),
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

                    if (welfareCases.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "বর্তমানে কোনো সমাজকল্যাণ আবেদন নেই",
                                    color = MosqueTextSecondary
                                )
                            }
                        }
                    } else {
                        items(welfareCases, key = { it.id }) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                            color = MosqueEmeraldDark
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (item.status == "disbursed") MosqueSuccessGreen.copy(alpha = 0.12f)
                                                    else MosqueGold.copy(alpha = 0.18f)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (item.status == "disbursed") "বিতরণকৃত" else "অনুমোদিত",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (item.status == "disbursed") MosqueSuccessGreen else MosqueGoldDeep,
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
                                            color = MosqueTextSecondary
                                        )
                                        Text(
                                            text = "অনুমোদিত: ৳${item.amountApproved.toInt()}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueEmeraldPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "মোবাইল: ${item.mobile}  •  তারিখ: ${item.applicationDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MosqueTextMuted
                                    )

                                    if (item.remarks.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "মন্তব্য: ${item.remarks}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MosqueTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "events" -> {
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
                                        text = "${events.size} টি ইভেন্ট তালিকাভুক্ত",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueGold
                                    )
                                }
                                val canAddEvent = currentRole.canPublishNotices() || currentRole == UserRole.WELFARE_EDUCATION_SECRETARY
                                if (canAddEvent) {
                                    Button(
                                        onClick = { showAddEventDialog = true },
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
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "বর্তমানে কোনো আসন্ন অনুষ্ঠান নেই",
                                    color = MosqueTextSecondary
                                )
                            }
                        }
                    } else {
                        items(events, key = { it.id }) { ev ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                            color = MosqueEmeraldDark
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (ev.status == "completed") MosqueSuccessGreen.copy(alpha = 0.12f)
                                                    else MosqueGold.copy(alpha = 0.18f)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (ev.status == "completed") "সম্পন্ন" else "আসন্ন",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (ev.status == "completed") MosqueSuccessGreen else MosqueGoldDeep,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "তারিখ ও সময়: ${ev.dateStr} (${ev.timeStr})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MosqueTextPrimary
                                    )
                                    Text(
                                        text = "বক্তা/অতিথি: ${ev.speakerOrGuest}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MosqueTextSecondary
                                    )
                                    Text(
                                        text = "স্থান: ${ev.venue}  •  বাজেট: ৳${ev.budget.toInt()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MosqueTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "স্বেচ্ছাসেবক প্রধান: ${ev.volunteerLead}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MosqueEmeraldPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            "audit" -> {

                // Audit Logs
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(auditLogs, key = { it.id }) { log ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = log.action,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueEmeraldPrimary
                                    )
                                    Text(
                                        text = log.timestamp,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MosqueTextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = log.details,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "কর্তা: ${log.actor}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MosqueTextSecondary
                                )
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
            }
        )
    }

    // Pay Salary Dialog
    showPaySalaryStaff?.let { staff ->
        PaySalaryFormDialog(
            staff = staff,
            onDismiss = { showPaySalaryStaff = null },
            onConfirm = { month, bonus ->
                viewModel.disburseSalary(staff, month, bonus)
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
            }
        )
    }

    // Add Food Schedule Dialog
    if (showAddFoodDialog) {
        AddFoodScheduleFormDialog(
            onDismiss = { showAddFoodDialog = false },
            onConfirm = { dateStr, mealType, providerName, providerMobile, receiverName ->
                viewModel.addFoodSchedule(dateStr, mealType, providerName, providerMobile, receiverName)
                showAddFoodDialog = false
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
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MosqueCardBorderSubtle),
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
                    .background(iconColor.copy(alpha = 0.12f)),
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
                        color = MosqueTextPrimary,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!badgeText.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (badgeIsAlert) MosqueWarningAmber.copy(alpha = 0.14f) else MosqueEmeraldSoft
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (badgeIsAlert) MosqueWarningAmber else MosqueEmeraldPrimary,
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
                    color = MosqueTextSecondary,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MosqueTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PrayerRowDetail(prayerName: String, azan: String, iqamah: String, note: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(prayerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            Text(note, fontSize = 11.sp, color = MosqueTextMuted)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("জামাত: $iqamah", fontWeight = FontWeight.Bold, color = MosqueEmeraldPrimary, style = MaterialTheme.typography.labelMedium)
            Text("আজান: $azan", fontSize = 11.sp, color = MosqueTextSecondary)
        }
    }
    HorizontalDivider(color = MosqueCardBorder.copy(alpha = 0.5f))
}

