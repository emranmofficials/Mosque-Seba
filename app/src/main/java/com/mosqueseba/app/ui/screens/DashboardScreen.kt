package com.mosqueseba.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.data.local.entity.AdvisoryNoteEntity
import com.mosqueseba.app.data.local.entity.CashboxRecordEntity
import com.mosqueseba.app.data.local.entity.MeetingEntity
import com.mosqueseba.app.data.local.entity.OfficeDocumentEntity
import com.mosqueseba.app.data.local.entity.TransactionEntity
import com.mosqueseba.app.data.local.entity.WelfareCaseEntity
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.*
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MosqueViewModel,
    onNavigateToTab: (Int) -> Unit,
    onOpenDialog: (String) -> Unit
) {
    val mosque by viewModel.mosque.collectAsState()
    val totalMembers by viewModel.memberCount.collectAsState()
    val netBalance by viewModel.netBalance.collectAsState()
    val dueChanda by viewModel.dueChanda.collectAsState()
    val pendingTransactions by viewModel.pendingTransactions.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allMeetings by viewModel.allMeetings.collectAsState()
    val studentCount by viewModel.studentCount.collectAsState()
    val activeStaff by viewModel.activeStaff.collectAsState()
    val allNotices by viewModel.allNotices.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val allWelfareCases by viewModel.allWelfareCases.collectAsState()
    val allChandaPayments by viewModel.allChandaPayments.collectAsState()
    val allCashboxRecords by viewModel.allCashboxRecords.collectAsState()
    val allOfficeDocuments by viewModel.allOfficeDocuments.collectAsState()
    val allAdvisoryNotes by viewModel.allAdvisoryNotes.collectAsState()
    val allSmartBoardQuotes by viewModel.allSmartBoardQuotes.collectAsState()
    val allPayrolls by viewModel.allPayrolls.collectAsState()
    val allFoodSchedules by viewModel.allFoodSchedules.collectAsState()
    val khademCheckedTasks by viewModel.khademCheckedTasks.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val showHijriBanner by viewModel.showHijriBanner.collectAsState()
    val context = LocalContext.current

    var showDutiesDialog by remember { mutableStateOf(false) }
    var pendingRejectVoucher by remember { mutableStateOf<TransactionEntity?>(null) }

    val (_, nextPrayerSummary) = computeTodayPrayerSchedule()
    val nextMeeting = allMeetings.firstOrNull { it.status == "upcoming" } ?: allMeetings.firstOrNull()
    val savedMemberId = viewModel.sessionManager.getSavedMemberId()
    val memberChandaPayments = allChandaPayments.filter { it.memberId == savedMemberId }.ifEmpty { allChandaPayments.take(1) }
    val myChandaPaid = memberChandaPayments.firstOrNull()?.amountPaid ?: 0.0
    val myChandaSub = memberChandaPayments.firstOrNull()?.let { "পরিশোধিত • ${it.month}" } ?: "চলতি মাস"
    val unverifiedCashboxes = allCashboxRecords.filter { !it.witnessVerified }
    val pendingOfficeDocs = allOfficeDocuments.filter { it.status == "জবাব অপেক্ষমান" }
    val pendingReplyDocs = pendingOfficeDocs.size
    val activeAdvisories = allAdvisoryNotes.filter { it.status == "পর্যালোচনাধীন" }
    val upcomingEventCount = allEvents.count { it.status != "completed" }
    val pendingWelfareCases = allWelfareCases.filter { it.status != "disbursed" }
    val roleStaff = activeStaff.firstOrNull { it.roleCode.equals(currentRole.code, ignoreCase = true) }
    val latestStaffPayroll = roleStaff?.let { st ->
        allPayrolls.filter { it.staffId == st.id }.lastOrNull()
    }
    val mySalaryValue = when {
        latestStaffPayroll != null -> "পরিশোধিত"
        roleStaff != null -> "৳${toBengaliDigits(roleStaff.salaryAmount.toInt().toString())}"
        else -> "পরিশোধিত"
    }
    val mySalarySub = when {
        latestStaffPayroll != null -> "${toBengaliDigits(latestStaffPayroll.month)} • বেতন স্লিপ"
        roleStaff != null -> "মাসিক বেতন • সার্ভিস রেকর্ড"
        else -> "বেতন স্লিপ ও রেকর্ড"
    }
    val todayIso = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(java.util.Date())
    }
    val todayStaffFoodProvider = remember(allFoodSchedules, currentRole, todayIso) {
        allFoodSchedules.firstOrNull { food ->
            val dateNorm = viewModel.normalizeDigitsToAscii(food.dateStr).trim()
            val matchesRole = when (currentRole) {
                UserRole.IMAM -> food.receiverStaffName.contains("ইমাম", ignoreCase = true)
                UserRole.MUAZZIN -> food.receiverStaffName.contains("মুয়াজ্জিন", ignoreCase = true) ||
                    food.receiverStaffName.contains("মুয়াজ্জিন", ignoreCase = true)
                UserRole.KHADEM -> food.receiverStaffName.contains("খাদেম", ignoreCase = true)
                else -> false
            }
            dateNorm == todayIso && food.status != "cancelled" && food.status != "rejected" && matchesRole
        }?.providerName
    }
    val khademChecklistProgress = remember(currentRole, khademCheckedTasks) {
        if (currentRole == UserRole.KHADEM) {
            "${toBengaliDigits(khademCheckedTasks.size.toString())}/${toBengaliDigits("4")} কাজ সম্পন্ন"
        } else {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        if (currentRole == UserRole.PRESIDENT) {
            // 1. Compact Unified Executive Sanctuary Hero
            item(key = "pres_hero") {
                StaggeredDashboardItem(index = 0, modifier = Modifier.animateItem()) {
                    ExecutiveMosqueHeroCard(
                        mosque = mosque,
                        currentRole = currentRole,
                        showHijriDate = showHijriBanner,
                        onDutiesClick = { showDutiesDialog = true },
                        onMosqueProfileClick = { onOpenDialog("mosque_profile") }
                    )
                }
            }

            // 2. Priority Executive Approvals & Oversight (only shown when action is needed)
            val hasPriorityItems = pendingTransactions.isNotEmpty() ||
                unverifiedCashboxes.isNotEmpty() ||
                activeAdvisories.isNotEmpty()

            if (hasPriorityItems) {
                item(key = "pres_priority_div") {
                    StaggeredDashboardItem(index = 1, modifier = Modifier.animateItem()) {
                        RubElHizbDivider(
                            title = "তদারকি ও অনুমোদন",
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                if (pendingTransactions.isNotEmpty()) {
                    item(key = "pres_pending_vouchers") {
                        StaggeredDashboardItem(index = 2, modifier = Modifier.animateItem()) {
                            PresidentPendingVouchersCard(
                                pendingTransactions = pendingTransactions,
                                onApprove = { txn ->
                                    viewModel.updateTransactionStatus(txn, true)
                                    Toast.makeText(
                                        context,
                                        "${txn.categoryNameBn} ভাউচারটি অনুমোদিত হয়েছে।",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onReject = { txn -> pendingRejectVoucher = txn },
                                onViewAll = { onOpenDialog("pending_approval") }
                            )
                        }
                    }
                }

                if (unverifiedCashboxes.isNotEmpty()) {
                    item(key = "pres_cashbox_witness") {
                        StaggeredDashboardItem(index = 2, modifier = Modifier.animateItem()) {
                            PendingCashboxWitnessCard(
                                records = unverifiedCashboxes,
                                canVerify = currentRole.canWitnessCashbox(),
                                onVerify = { viewModel.verifyCashboxAsWitness(it) },
                                onOpenCashbox = { onOpenDialog("cashbox") }
                            )
                        }
                    }
                }

                if (activeAdvisories.isNotEmpty()) {
                    item(key = "pres_advisory") {
                        StaggeredDashboardItem(index = 2, modifier = Modifier.animateItem()) {
                            AdvisoryHighlightCard(
                                notes = activeAdvisories,
                                onOpenAdvisory = { onOpenDialog("sharia_advisory") }
                            )
                        }
                    }
                }
            }

            // 3. Committee KPI Overview
            item(key = "pres_kpi_div") {
                StaggeredDashboardItem(index = 2, modifier = Modifier.animateItem()) {
                    RubElHizbDivider(
                        title = "কমিটি সারসংক্ষেপ",
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            item(key = "pres_kpi_grid") {
                StaggeredDashboardItem(index = 3, modifier = Modifier.animateItem()) {
                    DashboardKpiGrid(
                        role = currentRole,
                        totalMembers = totalMembers,
                        netBalance = netBalance,
                        dueChanda = dueChanda,
                        pendingItems = pendingTransactions.size,
                        studentCount = studentCount,
                        staffCount = activeStaff.size,
                        noticeCount = allNotices.size,
                        upcomingEventCount = upcomingEventCount,
                        totalEventCount = allEvents.size,
                        welfareCount = allWelfareCases.size,
                        pendingWelfareCount = pendingWelfareCases.size,
                        cashboxCount = allCashboxRecords.size,
                        pendingCashboxWitnessCount = unverifiedCashboxes.size,
                        officeDocCount = allOfficeDocuments.size,
                        pendingReplyDocCount = pendingReplyDocs,
                        advisoryCount = allAdvisoryNotes.size,
                        smartQuoteCount = allSmartBoardQuotes.size,
                        nextMeetingTitleDate = nextMeeting?.meetingDate?.substringBefore(",") ?: "নির্ধারিত নেই",
                        nextMeetingSubtitle = nextMeeting?.title ?: "কোনো আসন্ন সভা নেই",
                        myChandaPaid = myChandaPaid,
                        myChandaSubtitle = myChandaSub,
                        nextPrayerName = nextPrayerSummary.nameBn,
                        nextPrayerSubtitle = "${nextPrayerSummary.iqamahTimeBn} (বাকি ${nextPrayerSummary.remainingTextBn})",
                        onKpiClick = { key ->
                            when (key) {
                                "members" -> onOpenDialog("members")
                                "finance" -> onOpenDialog("finance")
                                "chanda" -> onOpenDialog("receive_chanda")
                                "pending" -> onOpenDialog("pending_approval")
                                else -> onOpenDialog(key)
                            }
                        }
                    )
                }
            }

            // 4. Quick Actions
            item(key = "pres_quick_actions") {
                StaggeredDashboardItem(index = 4, modifier = Modifier.animateItem()) {
                    QuickActionsGrid(
                        role = currentRole,
                        onActionClick = { actionKey -> onOpenDialog(actionKey) }
                    )
                }
            }

            // 5. Prayer Schedule & Wall Notice Quotes (no duplicate date/prayer slides)
            item(key = "pres_prayer_div") {
                StaggeredDashboardItem(index = 5, modifier = Modifier.animateItem()) {
                    RubElHizbDivider(
                        title = "নামাজের সূচি ও দেয়াল বোর্ড",
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            item(key = "pres_prayer_card") {
                StaggeredDashboardItem(index = 5, modifier = Modifier.animateItem()) {
                    PrayerCard(
                        onViewFullScheduleClick = { onOpenDialog("view_prayers") }
                    )
                }
            }

            item(key = "pres_smart_board") {
                StaggeredDashboardItem(index = 6, modifier = Modifier.animateItem()) {
                    SmartBoardCard(
                        quotes = allSmartBoardQuotes,
                        includeDateAndPrayerSlides = false
                    )
                }
            }

            // 6. Meetings & Completed Financial Activity (excludes pending items already shown above)
            item(key = "pres_meetings_div") {
                StaggeredDashboardItem(index = 6, modifier = Modifier.animateItem()) {
                    RubElHizbDivider(
                        title = "সভা ও সাম্প্রতিক লেনদেন",
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            item(key = "pres_meetings_card") {
                StaggeredDashboardItem(index = 7, modifier = Modifier.animateItem()) {
                    UpcomingMeetingsCard(
                        meetings = allMeetings,
                        canSchedule = currentRole.canScheduleMeetings(),
                        onScheduleClick = { onOpenDialog("schedule_meeting") },
                        onViewAllClick = { onOpenDialog("meetings") }
                    )
                }
            }

            val completedTransactions = allTransactions.filter { it.approvalStatus != "pending" }.take(4)
            if (completedTransactions.isNotEmpty()) {
                item(key = "pres_recent_txns") {
                    StaggeredDashboardItem(index = 8, modifier = Modifier.animateItem()) {
                        RecentActivityCard(
                            transactions = completedTransactions,
                            onViewAllClick = { onOpenDialog("finance") }
                        )
                    }
                }
            }
        } else {
            // 0. Unified Compact Executive Sanctuary Hero (same crisp layout for all 16 roles)
            item(key = "role_hero") {
                StaggeredDashboardItem(index = 0, modifier = Modifier.animateItem()) {
                    ExecutiveMosqueHeroCard(
                        mosque = mosque,
                        currentRole = currentRole,
                        showHijriDate = showHijriBanner,
                        onDutiesClick = { showDutiesDialog = true },
                        onMosqueProfileClick = { onOpenDialog("mosque_profile") }
                    )
                }
            }

            // 1.1 Vice President Pending Voucher Approval Hotline
            if (currentRole.canApproveFinance() && pendingTransactions.isNotEmpty()) {
                item {
                    PresidentPendingVouchersCard(
                        pendingTransactions = pendingTransactions,
                        onApprove = { txn ->
                            viewModel.updateTransactionStatus(txn, true)
                            Toast.makeText(
                                context,
                                "${txn.categoryNameBn} ভাউচারটি অনুমোদিত হয়েছে।",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onReject = { txn -> pendingRejectVoucher = txn },
                        onViewAll = { onOpenDialog("pending_approval") }
                    )
                }
            }

            // 1.1b Cashbox Witness Verification Card
            if ((currentRole.canWitnessCashbox() || currentRole.canManageCashbox()) && unverifiedCashboxes.isNotEmpty()) {
                item {
                    PendingCashboxWitnessCard(
                        records = unverifiedCashboxes,
                        canVerify = currentRole.canWitnessCashbox(),
                        onVerify = { viewModel.verifyCashboxAsWitness(it) },
                        onOpenCashbox = { onOpenDialog("cashbox") }
                    )
                }
            }

            // 1.1c Advisory & Sharia Guidance Highlight
            if (currentRole in listOf(
                    UserRole.VICE_PRESIDENT,
                    UserRole.GENERAL_SECRETARY,
                    UserRole.ASSISTANT_SECRETARY,
                    UserRole.IMAM,
                    UserRole.ADVISORY_BOARD
                ) && activeAdvisories.isNotEmpty()
            ) {
                item {
                    AdvisoryHighlightCard(
                        notes = activeAdvisories,
                        onOpenAdvisory = { onOpenDialog("sharia_advisory") }
                    )
                }
            }

            // 1.1d Pending Office Correspondence Highlight (Office Secretary)
            if (currentRole == UserRole.OFFICE_SECRETARY && pendingOfficeDocs.isNotEmpty()) {
                item {
                    PendingOfficeDocsHighlightCard(
                        pendingDocs = pendingOfficeDocs,
                        onOpenOfficeDocs = { onOpenDialog("office_docs") }
                    )
                }
            }

            // 1.1e Pending Welfare Distribution Highlight (Welfare & Education Secretary)
            if (currentRole == UserRole.WELFARE_EDUCATION_SECRETARY && pendingWelfareCases.isNotEmpty()) {
                item {
                    PendingWelfareHighlightCard(
                        pendingCases = pendingWelfareCases,
                        onOpenWelfare = { onOpenDialog("welfare") }
                    )
                }
            }

            // 1.2 Musalli Devotional & Instant Digital Receipt Card
            if (currentRole == UserRole.GENERAL_MEMBER) {
                item {
                    MusalliDevotionalCard(
                        onViewReceipt = { onOpenDialog("view_receipt") },
                        onMakeDonation = { onOpenDialog("my_chanda") }
                    )
                }
            }

            // 2. Smart Board (Dynamic Quotes)
            item {
                SmartBoardCard(
                    quotes = allSmartBoardQuotes,
                    includeDateAndPrayerSlides = false
                )
            }

            // 3. Prayer Schedule Card
            item {
                PrayerCard(
                    onViewFullScheduleClick = { onOpenDialog("view_prayers") }
                )
            }

            // 4. Role-based KPIs
            item {
                DashboardKpiGrid(
                    role = currentRole,
                    totalMembers = totalMembers,
                    netBalance = netBalance,
                    dueChanda = dueChanda,
                    pendingItems = pendingTransactions.size,
                    studentCount = studentCount,
                    staffCount = activeStaff.size,
                    noticeCount = allNotices.size,
                    upcomingEventCount = upcomingEventCount,
                    totalEventCount = allEvents.size,
                    welfareCount = allWelfareCases.size,
                    pendingWelfareCount = pendingWelfareCases.size,
                    cashboxCount = allCashboxRecords.size,
                    pendingCashboxWitnessCount = unverifiedCashboxes.size,
                    officeDocCount = allOfficeDocuments.size,
                    pendingReplyDocCount = pendingReplyDocs,
                    advisoryCount = allAdvisoryNotes.size,
                    pendingAdvisoryCount = activeAdvisories.size,
                    mySalaryStatusValue = mySalaryValue,
                    mySalaryStatusSubtitle = mySalarySub,
                    todayStaffFoodProvider = todayStaffFoodProvider,
                    khademChecklistProgress = khademChecklistProgress,
                    smartQuoteCount = allSmartBoardQuotes.size,
                    nextMeetingTitleDate = nextMeeting?.meetingDate?.substringBefore(",") ?: "নির্ধারিত নেই",
                    nextMeetingSubtitle = nextMeeting?.title ?: "কোনো আসন্ন সভা নেই",
                    myChandaPaid = myChandaPaid,
                    myChandaSubtitle = myChandaSub,
                    nextPrayerName = nextPrayerSummary.nameBn,
                    nextPrayerSubtitle = "${nextPrayerSummary.iqamahTimeBn} (বাকি ${nextPrayerSummary.remainingTextBn})",
                    onKpiClick = { key ->
                        when (key) {
                            "members" -> onOpenDialog("members")
                            "finance" -> onOpenDialog("finance")
                            "treasurer_chanda" -> onOpenDialog("treasurer_chanda")
                            "treasurer_payroll" -> onOpenDialog("treasurer_payroll")
                            "meetings" -> onOpenDialog("meetings")
                            "chanda" -> onOpenDialog(if (currentRole.canManageFinance()) "receive_chanda" else "my_chanda")
                            "pending" -> onOpenDialog("pending_approval")
                            "roster" -> onOpenDialog("roster")
                            "food" -> onOpenDialog("food")
                            "madrasa" -> onOpenDialog("madrasa")
                            "staff_profile" -> onOpenDialog("staff_profile")
                            "my_chanda" -> onOpenDialog("my_chanda")
                            else -> onOpenDialog(key)
                        }
                    }
                )
            }

            // 5. Role-based Quick Actions
            item {
                QuickActionsGrid(
                    role = currentRole,
                    onActionClick = { actionKey ->
                        onOpenDialog(actionKey)
                    }
                )
            }

            // 6. Upcoming Meetings Card
            if (currentRole in listOf(
                    UserRole.VICE_PRESIDENT,
                    UserRole.GENERAL_SECRETARY,
                    UserRole.ASSISTANT_SECRETARY,
                    UserRole.TREASURER,
                    UserRole.ASSISTANT_TREASURER,
                    UserRole.ORGANIZING_SECRETARY,
                    UserRole.PUBLICITY_SECRETARY,
                    UserRole.OFFICE_SECRETARY,
                    UserRole.WELFARE_EDUCATION_SECRETARY,
                    UserRole.EXECUTIVE_MEMBER,
                    UserRole.IMAM,
                    UserRole.ADVISORY_BOARD
                )
            ) {
                item {
                    UpcomingMeetingsCard(
                        meetings = allMeetings,
                        canSchedule = currentRole.canScheduleMeetings(),
                        onScheduleClick = { onOpenDialog("schedule_meeting") },
                        onViewAllClick = { onOpenDialog("meetings") }
                    )
                }
            }

            // 7. Recent Financial Activity
            if (currentRole.canManageFinance()) {
                item {
                    RecentActivityCard(
                        transactions = allTransactions.take(4),
                        onViewAllClick = { onOpenDialog("finance") }
                    )
                }
            }
        }

        // Bottom space padding
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        }
    }

    if (showDutiesDialog) {
        MyRoleDutiesDialog(
            role = currentRole,
            onOpenFullCharter = { onOpenDialog("charter") },
            onDismiss = { showDutiesDialog = false }
        )
    }

    pendingRejectVoucher?.let { txn ->
        MosqueConfirmDialog(
            title = "ভাউচার বাতিলের নিশ্চিতকরণ",
            message = "আপনি কি \"${txn.categoryNameBn}\" (৳${toBengaliDigits(String.format("%,.0f", txn.amount))}) ভাউচারটি বাতিল করতে চান?",
            confirmText = "হ্যাঁ, বাতিল করুন",
            isDestructive = true,
            onConfirm = {
                viewModel.updateTransactionStatus(txn, false)
                Toast.makeText(
                    context,
                    "${txn.categoryNameBn} ভাউচারটি বাতিল করা হয়েছে।",
                    Toast.LENGTH_SHORT
                ).show()
                pendingRejectVoucher = null
            },
            onDismiss = { pendingRejectVoucher = null }
        )
    }
}

@Composable
fun PendingCashboxWitnessCard(
    records: List<CashboxRecordEntity>,
    canVerify: Boolean,
    onVerify: (CashboxRecordEntity) -> Unit,
    onOpenCashbox: () -> Unit
) {
    val firstRec = records.first()
    MehrabArchCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MosqueWarningAmber.copy(alpha = 0.55f),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = MosqueWarningAmber, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "দান বাক্স সাক্ষ্য যাচাই (${toBengaliDigits(records.size.toString())}টি)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                TextButton(onClick = onOpenCashbox) {
                    Text("সব দেখুন", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            Text(
                text = "${firstRec.boxType} • ৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", firstRec.countedAmount))} (${toBengaliDigits(firstRec.collectionDate)})",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "গণনাকারী: ${firstRec.countedBy}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (canVerify) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onVerify(firstRec) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("উপস্থিত সাক্ষী হিসেবে নিশ্চিত করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdvisoryHighlightCard(
    notes: List<AdvisoryNoteEntity>,
    onOpenAdvisory: () -> Unit
) {
    val top = notes.first()
    val isDark = LocalIsDarkTheme.current
    val cardBg = if (isDark) MosqueNightSurfaceVariant else MosqueGoldSoft
    val headingColor = if (isDark) MosqueGoldLight else MosqueEmeraldDark
    val primaryColor = MaterialTheme.colorScheme.primary
    MehrabArchCard(
        backgroundColor = cardBg,
        borderColor = MosqueGoldDeep.copy(alpha = 0.5f),
        elevation = 2.dp
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
                        imageVector = if (top.noteType == "SHARIA_REVIEW") Icons.Default.Mosque else Icons.Default.SupervisorAccount,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (top.noteType == "SHARIA_REVIEW") "শরিয়াহ পর্যালোচনা নোট" else "উপদেষ্টা পরিষদের পরামর্শ",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = headingColor
                    )
                }
                TextButton(onClick = onOpenAdvisory) {
                    Text("বিস্তারিত", fontSize = 11.sp, color = primaryColor)
                }
            }
            Text(
                text = top.topic,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = top.recommendation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
fun PendingOfficeDocsHighlightCard(
    pendingDocs: List<OfficeDocumentEntity>,
    onOpenOfficeDocs: () -> Unit
) {
    val firstDoc = pendingDocs.first()
    MehrabArchCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = RoleOfficeAccent.copy(alpha = 0.55f),
        elevation = 2.dp
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
                        imageVector = Icons.Default.MarkEmailUnread,
                        contentDescription = null,
                        tint = RoleOfficeAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "জবাব অপেক্ষমাণ চিঠি (${toBengaliDigits(pendingDocs.size.toString())}টি)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                TextButton(onClick = onOpenOfficeDocs) {
                    Text("সব নথি", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            Text(
                text = firstDoc.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${firstDoc.category} • প্রেরক: ${firstDoc.counterparty} • ${toBengaliDigits(firstDoc.docDate)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onOpenOfficeDocs,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoleOfficeAccent)
            ) {
                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("জবাব তৈরি ও ফাইল খুলুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PendingWelfareHighlightCard(
    pendingCases: List<WelfareCaseEntity>,
    onOpenWelfare: () -> Unit
) {
    val firstCase = pendingCases.first()
    MehrabArchCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = RoleWelfareEduAccent.copy(alpha = 0.55f),
        elevation = 2.dp
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
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        tint = RoleWelfareEduAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "বিতরণ অপেক্ষমাণ সহায়তা (${toBengaliDigits(pendingCases.size.toString())}টি)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                TextButton(onClick = onOpenWelfare) {
                    Text("সব দেখুন", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            Text(
                text = "${firstCase.beneficiaryName} • ৳${toBengaliDigits(firstCase.amountApproved.toInt().toString())}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${firstCase.category} • ${toBengaliDigits(firstCase.applicationDate)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onOpenWelfare,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoleWelfareEduAccent)
            ) {
                Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("যাকাত ও সহায়তা বিতরণ খুলুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun UpcomingMeetingsCard(
    meetings: List<MeetingEntity>,
    canSchedule: Boolean = true,
    onScheduleClick: () -> Unit,
    onViewAllClick: (() -> Unit)? = null
) {
    val isDark = LocalIsDarkTheme.current
    val itemBg = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight
    val headingColor = MaterialTheme.colorScheme.primary
    MehrabArchCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MosqueCardBorderGold.copy(alpha = 0.5f),
        elevation = 2.dp
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
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = headingColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "আসন্ন কমিটি সভা ও এজেন্ডা",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = headingColor
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onViewAllClick != null) {
                        TextButton(
                            onClick = onViewAllClick,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "সব দেখুন",
                                style = MaterialTheme.typography.labelSmall,
                                color = headingColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (canSchedule) {
                        TextButton(
                            onClick = onScheduleClick,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "+ নতুন সভা",
                                style = MaterialTheme.typography.labelSmall,
                                color = headingColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (meetings.isEmpty()) {
                Text(
                    text = "কোনো নির্ধারিত সভা নেই",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                meetings.take(2).forEach { meeting ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(itemBg)
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = meeting.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MosqueGoldLight)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "আসন্ন",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MosqueEmeraldDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = meeting.meetingDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "এজেন্ডা: ${meeting.agenda}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentActivityCard(
    transactions: List<TransactionEntity>,
    onViewAllClick: () -> Unit
) {
    val headingColor = MaterialTheme.colorScheme.primary
    MehrabArchCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MosqueCardBorderGold.copy(alpha = 0.5f),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = headingColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "সাম্প্রতিক আর্থিক লেনদেন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = headingColor
                    )
                }

                TextButton(
                    onClick = onViewAllClick,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "সব দেখুন",
                        style = MaterialTheme.typography.labelSmall,
                        color = headingColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            transactions.forEach { txn ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (txn.type == "INCOME") MosqueSuccessGreen.copy(alpha = 0.12f)
                                    else MosqueUrgentRed.copy(alpha = 0.12f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (txn.type == "INCOME") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (txn.type == "INCOME") MosqueSuccessGreen else MosqueUrgentRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = txn.categoryNameBn,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${toBengaliDigits(txn.txnDate)} • ${txn.createdBy}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${if (txn.type == "INCOME") "+" else "-"}৳${toBengaliDigits(String.format("%,.0f", txn.amount))}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (txn.type == "INCOME") MosqueSuccessGreen else MosqueUrgentRed
                        )
                        if (txn.approvalStatus == "pending") {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MosqueWarningAmber.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "অনুমোদন বাকি",
                                    fontSize = 9.sp,
                                    color = MosqueWarningAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                if (txn != transactions.last()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun PresidentPendingVouchersCard(
    pendingTransactions: List<TransactionEntity>,
    onApprove: (TransactionEntity) -> Unit,
    onReject: (TransactionEntity) -> Unit,
    onViewAll: (() -> Unit)? = null
) {
    val isDark = LocalIsDarkTheme.current
    val rowBg = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight
    MehrabArchCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MosqueUrgentRed.copy(alpha = 0.5f),
        elevation = 3.dp
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
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MosqueUrgentRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = MosqueUrgentRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "নির্বাহী অনুমোদন ডেস্ক (${toBengaliDigits(pendingTransactions.size.toString())}টি ভাউচার)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "নির্বাহী স্বাক্ষর ও ব্যয় ভাউচার নিষ্পত্তি",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (onViewAll != null) {
                    TextButton(
                        onClick = onViewAll,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "সব দেখুন",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            pendingTransactions.take(3).forEach { txn ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = rowBg,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = txn.categoryNameBn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "দাবিকৃত: ৳${toBengaliDigits(String.format("%,.0f", txn.amount))} • ${txn.createdBy}",
                                fontSize = 11.sp,
                                color = MosqueUrgentRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { onReject(txn) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueUrgentRed)
                            ) {
                                Text("বাতিল", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onApprove(txn) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("অনুমোদন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MusalliDevotionalCard(
    onViewReceipt: () -> Unit,
    onMakeDonation: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val gradientColors = if (isDark) {
        listOf(MosqueNightSurfaceVariant, MosqueNightSurface)
    } else {
        listOf(MosqueGoldSoft.copy(alpha = 0.5f), Color.White)
    }
    MehrabArchCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MosqueCardBorderGold.copy(alpha = 0.6f),
        elevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(gradientColors))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "আজকের হাদিস ও দ্বীনি অনুপ্রেরণা",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "«مَنْ بَنَى مَسْجِدًا لِلَّهِ بَنَى اللَّهُ لَهُ فِي الْجَنَّةِ مِثْلَهُ»\n\"যে ব্যক্তি আল্লাহর সন্তুষ্টির উদ্দেশ্যে একটি মসজিদ নির্মাণ করে বা তাতে শরিক হয়, আল্লাহ তার জন্য জান্নাতে একটি প্রাসাদ নির্মাণ করেন।\" — (সহিহ বুখারি: ৪৫০)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewReceipt,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("musalli_receipt_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ডিজিটাল রসিদ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onMakeDonation,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("musalli_donation_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                ) {
                    Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("চাঁদা খাতা দেখুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StaggeredDashboardItem(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((index * 45L).coerceAtMost(270L))
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(durationMillis = 240)) +
            slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                initialOffsetY = { it / 5 }
            )
    ) {
        content()
    }
}


