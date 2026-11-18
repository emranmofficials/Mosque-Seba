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
import androidx.compose.ui.text.style.TextOverflow
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
import com.mosqueseba.app.data.model.IslamicCalendarEngine
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
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
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
    val hijriDayOffset by viewModel.hijriDayOffset.collectAsState()
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current

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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        val onKpiClickHandler: (String) -> Unit = { key ->
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
                "transparency" -> onOpenDialog("transparency")
                "cashbox" -> onOpenDialog("cashbox")
                "office_docs" -> onOpenDialog("office_docs")
                "sharia_advisory" -> onOpenDialog("sharia_advisory")
                "publicity" -> onOpenDialog("publicity")
                "notices" -> onOpenDialog("notices")
                "events" -> onOpenDialog("events")
                "welfare" -> onOpenDialog("welfare")
                else -> onOpenDialog(key)
            }
        }

        // 1. Mosque Overview Card (Photo, Location, Established Date & Active Status)
        item(key = "role_hero") {
            ExecutiveMosqueHeroCard(
                mosque = mosque,
                currentRole = currentRole,
                showHijriDate = showHijriBanner,
                onDutiesClick = { showDutiesDialog = true },
                onMosqueProfileClick = { onOpenDialog("mosque_profile") }
            )
        }

        // 2. Actionable Priority Alerts (Conditionally shown ONLY when user action is required)
        if ((currentRole == UserRole.PRESIDENT || currentRole == UserRole.VICE_PRESIDENT || currentRole == UserRole.TREASURER) && pendingTransactions.isNotEmpty()) {
            item(key = "priority_pending_vouchers") {
                PresidentPendingVouchersCard(
                    pendingTransactions = pendingTransactions,
                    onApprove = { txn ->
                        viewModel.updateTransactionStatus(txn, true)
                        MosqueToastController.success(
                            "${txn.categoryNameBn} ভাউচারটি অনুমোদিত হয়েছে।",
                            "ভাউচার অনুমোদিত"
                        )
                    },
                    onReject = { txn -> pendingRejectVoucher = txn },
                    onViewAll = { onOpenDialog("pending_approval") }
                )
            }
        }

        if (unverifiedCashboxes.isNotEmpty() && currentRole.canWitnessCashbox()) {
            item(key = "priority_cashbox_witness") {
                PendingCashboxWitnessCard(
                    records = unverifiedCashboxes,
                    canVerify = currentRole.canWitnessCashbox(),
                    onVerify = { viewModel.verifyCashboxAsWitness(it) },
                    onOpenCashbox = { onOpenDialog("cashbox") }
                )
            }
        }

        if (currentRole == UserRole.KHADEM && khademChecklistProgress != null) {
            item(key = "priority_khadem_checklist") {
                Surface(
                    onClick = { showDutiesDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) MosqueNightSurfaceVariant else Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else Color(0xFFBBF7D0)),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MosqueSuccessGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "আজকের মসজিদ সেবা ও পরিচ্ছন্নতা ডিউটি",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary
                            )
                        }
                        Text(
                            text = khademChecklistProgress,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MosqueSuccessGreen
                        )
                    }
                }
            }
        }

        // 3. Daily Prayer Schedule & Jamat Times (Prime Devotional Utility)
        item(key = "global_top_prayer_card") {
            PrayerCard(onViewFullScheduleClick = { onOpenDialog("view_prayers") })
        }

        // 4. Quick Actions Grid (6 Core Modules - Single Unified Grid)
        item(key = "royal_home_quick_actions") {
            RoyalQuickActionsGrid(
                notificationCount = allNotices.size.coerceAtLeast(3),
                onActionClick = { key ->
                    when (key) {
                        "members" -> onOpenDialog("members")
                        "chanda" -> onOpenDialog(if (currentRole.canManageFinance()) "receive_chanda" else "my_chanda")
                        "meetings" -> onOpenDialog("meetings")
                        "finance" -> onOpenDialog("finance")
                        "office_docs" -> onOpenDialog("office_docs")
                        "notices" -> onOpenDialog("notices")
                        else -> onOpenDialog(key)
                    }
                }
            )
        }

        // 5. Mosque Summary KPI Card ("মসজিদের সংক্ষিপ্ত তথ্য" - মুসল্লী, পরিবার, চলতি তহবিল)
        item(key = "royal_home_summary_kpi") {
            RoyalMosqueSummaryKpiCard(
                totalMembers = totalMembers,
                netBalance = netBalance,
                totalIncome = totalIncome,
                onViewDetailsClick = { onOpenDialog("transparency") }
            )
        }

        // 6. Dynamic Smart Digital Board (Hijri Calendar, Ramadan Hub, Notices & Wall Quotes)
        item(key = "global_top_smart_board") {
            SmartBoardCard(
                quotes = allSmartBoardQuotes,
                notices = allNotices,
                hijriDayOffset = hijriDayOffset,
                includeDateAndPrayerSlides = true,
                onOpenIslamicCalendar = { onOpenDialog("islamic_calendar") },
                onOpenRamadanHub = { onOpenDialog("ramadan_hub") },
                onOpenPrayerSchedule = { onOpenDialog("view_prayers") },
                onOpenNotices = { onOpenDialog("notices") }
            )
        }

        // 7. Recent Activities Feed ("সাম্প্রতিক কার্যক্রম")
        item(key = "royal_home_recent_activities") {
            RoyalRecentActivitiesCard(
                transactions = allTransactions.filter { it.approvalStatus != "pending" }.take(3),
                meetings = allMeetings.take(2),
                onViewAllClick = { onOpenDialog("finance") }
            )
        }

        // 8. Inspirational Islamic Quote Banner
        item(key = "royal_home_quote_banner") {
            InspirationalIslamicQuoteBanner(
                quoteText = allSmartBoardQuotes.firstOrNull()?.message ?: "মসজিদ হলো সমাজের শান্তি ও ঐক্যের কেন্দ্রবিন্দু"
            )
        }

        // Bottom space padding
        item {
            Spacer(modifier = Modifier.height(24.dp))
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
                MosqueToastController.warning(
                    "${txn.categoryNameBn} ভাউচারটি বাতিল করা হয়েছে।",
                    "ভাউচার বাতিল"
                )
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
    if (pendingDocs.isEmpty()) return
    val isDark = LocalIsDarkTheme.current
    val rowBg = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight

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
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(RoleOfficeAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MarkEmailUnread,
                            contentDescription = null,
                            tint = RoleOfficeAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "জরুরি দাপ্তরিক ডাক ও চিঠিপত্র",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = RoleOfficeAccent.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "${toBengaliDigits(pendingDocs.size.toString())}টি জবাব অপেক্ষমাণ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoleOfficeAccent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "আগত চিঠির প্রাতিষ্ঠানিক জবাব তৈরি ও রেজিস্টার",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(
                    onClick = onOpenOfficeDocs,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "সব নথি",
                        fontSize = 11.sp,
                        color = RoleOfficeAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            pendingDocs.take(2).forEach { doc ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = rowBg,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = RoleOfficeAccent.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = doc.category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RoleOfficeAccent,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = toBengaliDigits(doc.docDate),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = doc.title,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "প্রেরক: ${doc.counterparty} • স্মারক: ${doc.docCode}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onOpenOfficeDocs,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoleOfficeAccent)
            ) {
                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("জবাব তৈরি ও নথি রেজিস্টার খুলুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PendingWelfareHighlightCard(
    pendingCases: List<WelfareCaseEntity>,
    onOpenWelfare: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
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
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(RoleWelfareEduAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolunteerActivism,
                            contentDescription = null,
                            tint = RoleWelfareEduAccent,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "বিতরণ অপেক্ষমাণ সহায়তা (${toBengaliDigits(pendingCases.size.toString())}টি)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                TextButton(
                    onClick = onOpenWelfare,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("সব দেখুন", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, RoleWelfareEduAccent.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = firstCase.beneficiaryName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val approvedAmount = if (firstCase.amountApproved > 0) firstCase.amountApproved else firstCase.amountRequested
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RoleWelfareEduAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "৳${toBengaliDigits(approvedAmount.toInt().toString())}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = RoleWelfareEduAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${firstCase.category} • ${toBengaliDigits(firstCase.applicationDate)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (pendingCases.size > 1) {
                            Text(
                                text = "+${toBengaliDigits((pendingCases.size - 1).toString())}টি আরও অপেক্ষমাণ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MosqueInfoBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onOpenWelfare,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoleWelfareEduAccent),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("যাকাত ও কল্যাণ বিতরণ পরিচালনা", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

    if (pendingTransactions.isEmpty()) {
        MehrabArchCard(
            backgroundColor = MaterialTheme.colorScheme.surface,
            borderColor = MosqueCardBorderGold.copy(alpha = 0.6f),
            elevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MosqueEmeraldPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = MosqueEmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "সকল আর্থিক ভাউচার অনুমোদিত ও নিষ্পন্ন",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "বর্তমানে কোনো ব্যয় ভাউচার বা বিল স্বাক্ষরের অপেক্ষায় নেই।",
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
                            text = "খতিয়ান",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        return
    }

    val totalPendingAmount = pendingTransactions.sumOf { it.amount }

    MehrabArchCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MosqueUrgentRed.copy(alpha = 0.6f),
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MosqueUrgentRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = MosqueUrgentRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "নির্বাহী অনুমোদন ডেস্ক",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MosqueUrgentRed.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "${toBengaliDigits(pendingTransactions.size.toString())}টি ভাউচার",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueUrgentRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "মোট দাবিকৃত: ৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", totalPendingAmount))} • নির্বাহী স্বাক্ষর অপেক্ষমাণ",
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
                    shape = RoundedCornerShape(12.dp),
                    color = rowBg,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
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
                                text = "দাবিকৃত: ৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", txn.amount))} • ${txn.createdBy}",
                                fontSize = 11.sp,
                                color = MosqueUrgentRed,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (txn.description.isNotBlank()) {
                                Text(
                                    text = txn.description,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onReject(txn) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(34.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueUrgentRed),
                                border = BorderStroke(1.dp, MosqueUrgentRed.copy(alpha = 0.6f))
                            ) {
                                Text("বাতিল", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = { onApprove(txn) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier.height(34.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
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
                        .height(44.dp)
                        .testTag("musalli_receipt_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ডিজিটাল রসিদ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onMakeDonation,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("musalli_donation_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("চাঁদা খাতা দেখুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TreasurerTreasuryLedgerCard(
    netBalance: Double,
    totalIncome: Double = 0.0,
    totalExpense: Double = 0.0,
    dueChanda: Double,
    unverifiedCashboxCount: Int,
    pendingVoucherCount: Int,
    onReceiveChanda: () -> Unit,
    onOpenCashbox: () -> Unit,
    onOpenPayroll: () -> Unit,
    onViewFinance: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    MehrabArchCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = RoleTreasurerAccent.copy(alpha = 0.6f),
        elevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(RoleTreasurerAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = RoleTreasurerAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "বায়তুলমাল ও প্রধান তহবিল ডেস্ক",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "নগদ স্থিতি ও অর্থ ব্যবস্থাপনা",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(
                    onClick = onViewFinance,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "পূর্ণ খতিয়ান",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Balance Surface
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) MosqueNightSurfaceVariant else MosqueGoldSoft.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "বর্তমান নেট বায়তুলমাল স্থিতি",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", netBalance))}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MosqueSuccessGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "বকেয়া আদায়যোগ্য",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", dueChanda))}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MosqueWarningAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        color = MosqueGold.copy(alpha = 0.2f),
                        thickness = 0.5.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = MosqueSuccessGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "মোট আয়: ৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", totalIncome))}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MosqueSuccessGreen
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = MosqueUrgentRed,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "মোট ব্যয়: ৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", totalExpense))}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MosqueUrgentRed
                            )
                        }
                    }
                }
            }

            // Alert Badges (if any unverified cashboxes or pending vouchers)
            if (unverifiedCashboxCount > 0 || pendingVoucherCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (unverifiedCashboxCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MosqueWarningAmber.copy(alpha = 0.12f),
                            border = BorderStroke(0.8.dp, MosqueWarningAmber.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable(onClick = onOpenCashbox)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Inventory2, contentDescription = null, tint = MosqueWarningAmber, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ক্যাশবক্স গণনা: ${toBengaliDigits(unverifiedCashboxCount.toString())}টি বাকি",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueWarningAmber
                                )
                            }
                        }
                    }
                    if (pendingVoucherCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MosqueUrgentRed.copy(alpha = 0.12f),
                            border = BorderStroke(0.8.dp, MosqueUrgentRed.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable(onClick = onViewFinance)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PendingActions, contentDescription = null, tint = MosqueUrgentRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ভাউচার অনুমোদন: ${toBengaliDigits(pendingVoucherCount.toString())}টি",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueUrgentRed
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Uniform Grid Action Buttons (2x2)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onReceiveChanda,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("চাঁদা গ্রহণ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onOpenCashbox,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoleTreasurerAccent)
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("দানবাক্স জমা", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenPayroll,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MosqueInfoBlue.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueInfoBlue)
                    ) {
                        Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("বেতন খাতা", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onViewFinance,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("হিসাব খতিয়ান", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SecretariatExecutiveHubCard(
    nextMeeting: MeetingEntity?,
    pendingDocsCount: Int,
    onScheduleMeeting: () -> Unit,
    onDraftNotice: () -> Unit,
    onOpenOfficeDocs: () -> Unit,
    onViewMeetings: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    MehrabArchCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = RoleSecretaryAccent.copy(alpha = 0.6f),
        elevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(RoleSecretaryAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderShared,
                            contentDescription = null,
                            tint = RoleSecretaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "প্রশাসনিক সচিবালয় ও কার্যবিবরণী ডেস্ক",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "কমিটি সভা, রেজোলিউশন ও অফিস নথি",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(
                    onClick = onViewMeetings,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "সকল সভা",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Meeting Surface
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MosqueEmeraldPrimary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (nextMeeting != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "আসন্ন কমিটি সভা",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MosqueEmeraldPrimary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "নির্ধারিত সভা",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueEmeraldPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = nextMeeting.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MosqueEmeraldPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "তারিখ: ${nextMeeting.meetingDate}",
                                fontSize = 11.sp,
                                color = MosqueEmeraldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (nextMeeting.location.isNotBlank()) {
                                Spacer(modifier = Modifier.width(10.dp))
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = RoleSecretaryAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = nextMeeting.location,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (nextMeeting.agenda.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "এজেন্ডা: ${nextMeeting.agenda}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventBusy,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "বর্তমানে কোনো আসন্ন সভা নির্ধারিত নেই",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "নতুন সভার নোটিশ জারি করতে নিচের বাটন ব্যবহার করুন",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Alert Badges (if any pending office documents)
            if (pendingDocsCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RoleOfficeAccent.copy(alpha = 0.12f),
                    border = BorderStroke(0.8.dp, RoleOfficeAccent.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenOfficeDocs)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailUnread,
                                contentDescription = null,
                                tint = RoleOfficeAccent,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "দাপ্তরিক ডাক: ${toBengaliDigits(pendingDocsCount.toString())}টি চিঠির জবাব দেওয়া বাকি",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoleOfficeAccent
                            )
                        }
                        Text(
                            text = "নথি দেখুন →",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoleOfficeAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Uniform Grid Action Buttons (2x2)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onScheduleMeeting,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Icon(Icons.Default.EventAvailable, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("নতুন সভা আহ্বান", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onDraftNotice,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoleSecretaryAccent)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("AI নোটিশ ড্রাফট", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenOfficeDocs,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, RoleOfficeAccent.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoleOfficeAccent)
                    ) {
                        Icon(Icons.Default.FolderShared, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("চিঠি ও অফিস নথি", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onViewMeetings,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("কার্যবিবরণী খাতা", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ImamMehrabDutyCard(
    nextPrayerName: String,
    nextPrayerIqamah: String,
    remainingText: String,
    onViewFullSchedule: () -> Unit,
    onOpenAdvisory: () -> Unit,
    onOpenSmartBoard: () -> Unit,
    onOpenMadrasa: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    MehrabArchCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = RoleImamAccent.copy(alpha = 0.6f),
        elevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(RoleImamAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mosque,
                            contentDescription = null,
                            tint = RoleImamAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "মিহরাব ও ইমামতি ডেস্ক",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "পাঁচ ওয়াক্ত সালাত, খুতবা ও দ্বীনি পরামর্শ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MosqueGold.copy(alpha = 0.15f),
                    border = BorderStroke(0.8.dp, MosqueGold.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "সম্মানিত ইমাম",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MosqueGoldDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prayer Surface
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MosqueEmeraldPrimary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "পরবর্তী জামায়াত ও ওয়াক্ত",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$nextPrayerName • ইকামত: $nextPrayerIqamah",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MosqueEmeraldPrimary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MosqueGold.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "বাকি $remainingText",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MosqueGoldDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Uniform Grid Action Buttons (2x2)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onViewFullSchedule,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("নামাজের সূচি", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onOpenAdvisory,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoleImamAccent)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("শরিয়াহ ফতোয়া", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenSmartBoard,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, RoleImamAccent.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoleImamAccent)
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("দেয়াল বার্তা", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onOpenMadrasa,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MosqueInfoBlue.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueInfoBlue)
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("মক্তব ও হিফজ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ImamMadrasaHifzDeskCard(
    studentCount: Int,
    onOpenMadrasa: () -> Unit
) {
    MehrabArchCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MosqueInfoBlue.copy(alpha = 0.5f),
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
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MosqueInfoBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "মক্তব ও হিফজুল কুরআন বিভাগ (${toBengaliDigits(studentCount.toString())} জন ছাত্র)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                TextButton(onClick = onOpenMadrasa) {
                    Text("তালিকা", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            Text(
                text = "ছাত্রদের দৈনিক সবক, আমপারা ও হিফজ মূল্যায়ন খাতা হালনাগাদ রাখুন।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onOpenMadrasa,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MosqueInfoBlue)
            ) {
                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("আজকের সবক মূল্যায়ন ও হাজিরা", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StaffFoodScheduleCard(
    todayFoodProvider: String?,
    onOpenFood: () -> Unit
) {
    MehrabArchCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MosqueGold.copy(alpha = 0.5f),
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
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = MosqueGoldDeep,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "খানাপিনা ও মেহমানদারি সূচি",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                TextButton(onClick = onOpenFood) {
                    Text("সব দিন", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = todayFoodProvider?.let { "আজকের মেহমানদার পরিবার: $it" } ?: "আজকের খানা ব্যবস্থা: সাধারণ / মেহমানদার পরিবার নির্ধারিত নেই",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = if (todayFoodProvider != null) MosqueSuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "দুপুর ও রাতের খাবার সময়মতো নিশ্চিতকরণ ও মেহমানদারি সমন্বয়।",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
fun MuazzinAzanDutyCard(
    nextPrayerName: String,
    nextPrayerIqamah: String,
    remainingText: String,
    onViewFullSchedule: () -> Unit,
    onOpenRoster: () -> Unit,
    onOpenFood: () -> Unit,
    onOpenSmartBoard: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    MehrabArchCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MosqueEmeraldPrimary.copy(alpha = 0.6f),
        elevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MosqueEmeraldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MosqueEmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "আজান, ইকামত ও সাউন্ড কন্ট্রোল ডেস্ক",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "৫ ওয়াক্ত আজান আহ্বান ও সময়নিষ্ঠা",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MosqueEmeraldPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "সম্মানিত মুয়াজ্জিন",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MosqueEmeraldPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Azan / Jamat Surface
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MosqueEmeraldPrimary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "আসন্ন আজান ও জামায়াত",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$nextPrayerName • ইকামত: $nextPrayerIqamah",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MosqueEmeraldPrimary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MosqueGold.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "বাকি $remainingText",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MosqueGoldDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Uniform Grid Action Buttons (2x2)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onViewFullSchedule,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("আজান সময়সূচি", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onOpenRoster,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldDark)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("ডিউটি রোস্টার", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenFood,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueGoldDark)
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("আজকের খানা সূচি", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onOpenSmartBoard,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("দেয়াল বার্তা", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun KhademFacilityChecklistCard(
    checkedTasks: Set<String>,
    onToggleTask: (String) -> Unit,
    onOpenRoster: () -> Unit,
    onOpenFood: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val tasks = listOf(
        Triple("khadem_clean", "প্রধান নামাজ কক্ষ ও কার্পেট পরিচ্ছন্নতা", "কাতার সোজা ও ধূলিমুক্ত রাখা"),
        Triple("khadem_wudu", "অজুখানা ও ওয়াশরুম জীবাণুমুক্তকরণ", "সাবান, লোটা ও পানির কল পরীক্ষা"),
        Triple("khadem_lights", "বাতি, ফ্যান ও সাউন্ড সিস্টেম পরীক্ষা", "জামাতের পূর্বে সুইচিং ও ব্যাটারি চেক"),
        Triple("khadem_water", "বিশুদ্ধ পানি ও রিজার্ভার পাম্প চেক", "পানির ট্যাংক পূর্ণতা ও অজু সুবিধা")
    )
    val doneCount = tasks.count { it.first in checkedTasks }

    MehrabArchCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MosqueEmeraldLight.copy(alpha = 0.6f),
        elevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MosqueEmeraldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            tint = MosqueEmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "মসজিদ পবিত্রতা ও খেদমত চেকলিস্ট",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "দৈনিক ৪-দফা পরিচ্ছন্নতা ও রক্ষণাবেক্ষণ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (doneCount == 4) MosqueSuccessGreen.copy(alpha = 0.15f) else MosqueGold.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${toBengaliDigits(doneCount.toString())}/${toBengaliDigits("4")} সম্পন্ন",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (doneCount == 4) MosqueSuccessGreen else MosqueGoldDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Animated Visual Progress Bar
            LinearProgressIndicator(
                progress = { doneCount / 4f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (doneCount == 4) MosqueSuccessGreen else MosqueEmeraldPrimary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            tasks.forEachIndexed { idx, (taskId, title, subtitle) ->
                val isChecked = taskId in checkedTasks
                if (idx > 0) Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    onClick = { onToggleTask(taskId) },
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        isChecked && isDark -> MosqueEmeraldDark.copy(alpha = 0.35f)
                        isChecked -> MosqueEmeraldSoft.copy(alpha = 0.65f)
                        isDark -> MosqueNightSurfaceVariant
                        else -> MosqueBgLight
                    },
                    border = BorderStroke(
                        0.8.dp,
                        if (isChecked) MosqueEmeraldPrimary.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isChecked) MosqueEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2 Uniform Bottom Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenFood,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.7f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueGoldDark)
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("আজকের খানা সূচি", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onOpenRoster,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ডিউটি রোস্টার ও লগ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MusalliTransparencySummaryCard(
    netBalance: Double,
    myChandaPaid: Double,
    myChandaSub: String,
    onViewReceipt: () -> Unit,
    onMakeDonation: () -> Unit,
    onOpenTransparency: () -> Unit,
    onViewPrayerSchedule: () -> Unit = {}
) {
    val isDark = LocalIsDarkTheme.current
    MehrabArchCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MosqueGold.copy(alpha = 0.6f),
        elevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(RoleMusalliAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = RoleMusalliAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "স্বচ্ছ বায়তুলমাল ও আমার আমানত",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "মসজিদের উন্মুক্ত আয়-ব্যয় ও নিজস্ব চাঁদা রেকর্ড",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(
                    onClick = onOpenTransparency,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "স্বচ্ছ হিসাব",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) MosqueNightSurfaceVariant else MosqueGoldSoft.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "মসজিদের কেন্দ্রীয় তহবিল স্থিতি",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", netBalance))}",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MosqueSuccessGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "আমার প্রদত্ত চাঁদা ($myChandaSub)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", myChandaPaid))}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MosqueGoldDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MosqueSuccessGreen.copy(alpha = if (isDark) 0.2f else 0.1f),
                        border = BorderStroke(1.dp, MosqueSuccessGreen.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = MosqueSuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "সকল হিসাব সাধারণ মুসল্লি ও কমিটির জন্য উন্মুক্ত",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MosqueSuccessGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewReceipt,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("QR রসিদ", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Button(
                        onClick = onMakeDonation,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("চাঁদা ও খাতা", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenTransparency,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueSuccessGreen),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("স্বচ্ছ তহবিল", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Button(
                        onClick = onViewPrayerSchedule,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueInfoBlue),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নামাজের সূচি", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun WelfareEduDeskCard(
    studentCount: Int,
    pendingWelfareCount: Int,
    onOpenWelfare: () -> Unit,
    onOpenMadrasa: () -> Unit,
    onOpenNewApplication: () -> Unit = onOpenWelfare,
    onOpenEvents: () -> Unit = {}
) {
    val isDark = LocalIsDarkTheme.current
    MehrabArchCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = RoleWelfareEduAccent.copy(alpha = 0.6f),
        elevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(RoleWelfareEduAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolunteerActivism,
                            contentDescription = null,
                            tint = RoleWelfareEduAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "সমাজকল্যাণ ও শিক্ষা বিষয়ক ডেস্ক",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "যাকাত তহবিল, অসচ্ছল পরিবার সহায়তা ও মক্তব",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MosqueEmeraldPrimary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "বিতরণ অপেক্ষমাণ আবেদন",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${toBengaliDigits(pendingWelfareCount.toString())} টি কেস",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (pendingWelfareCount > 0) MosqueUrgentRed else MosqueSuccessGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "মাদরাসা ও মক্তব ছাত্র",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${toBengaliDigits(studentCount.toString())} জন",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MosqueInfoBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (pendingWelfareCount > 0) {
                            MosqueUrgentRed.copy(alpha = if (isDark) 0.2f else 0.1f)
                        } else {
                            MosqueSuccessGreen.copy(alpha = if (isDark) 0.2f else 0.1f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (pendingWelfareCount > 0) MosqueUrgentRed.copy(alpha = 0.35f)
                            else MosqueSuccessGreen.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (pendingWelfareCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (pendingWelfareCount > 0) MosqueUrgentRed else MosqueSuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (pendingWelfareCount > 0) "জরুরি বিতরণ ও যাচাই কার্যক্রম পর্যালোচনার জন্য অপেক্ষমাণ"
                                else "সকল কল্যাণ আবেদন ও মক্তব কার্যক্রম সুষ্ঠুভাবে পরিচালিত হচ্ছে",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (pendingWelfareCount > 0) MosqueUrgentRed else MosqueSuccessGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenWelfare,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoleWelfareEduAccent),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("যাকাত ও কল্যাণ", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Button(
                        onClick = onOpenMadrasa,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueInfoBlue),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("মক্তব ও হিফজ", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenNewApplication,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("সহায়তা আবেদন", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Button(
                        onClick = onOpenEvents,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueGoldDark),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Celebration, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ক্যাম্প ও মাহফিল", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun PublicityNoticeDeskCard(
    quoteCount: Int,
    noticeCount: Int,
    onOpenPublicity: () -> Unit,
    onOpenNotices: () -> Unit,
    onOpenEvents: () -> Unit = {},
    onOpenPrayerSchedule: () -> Unit = {}
) {
    val isDark = LocalIsDarkTheme.current
    MehrabArchCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = RolePublicityAccent.copy(alpha = 0.6f),
        elevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(RolePublicityAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewCarousel,
                            contentDescription = null,
                            tint = RolePublicityAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "প্রচার, প্রকাশনা ও প্রযুক্তি ডেস্ক",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "স্মার্ট ডিজিটাল বোর্ড, মসজিদ নোটিশ ও দেয়াল বার্তা",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) MosqueNightSurfaceVariant else MosqueGoldSoft.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "সক্রিয় স্মার্ট বার্তা",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${toBengaliDigits(quoteCount.toString())} টি স্লাইড",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RolePublicityAccent
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "প্রকাশিত নোটিশ",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${toBengaliDigits(noticeCount.toString())} টি বিজ্ঞপ্তি",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MosqueEmeraldSoft.copy(alpha = if (isDark) 0.25f else 0.55f),
                        border = BorderStroke(1.dp, MosqueEmeraldPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MosqueEmeraldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ডিজিটাল ডিসপ্লে ও নোটিশ বোর্ড সার্বক্ষণিক সক্রিয়",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenPublicity,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RolePublicityAccent),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.ViewCarousel, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("স্মার্ট বোর্ড স্লাইড", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Button(
                        onClick = onOpenNotices,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নতুন নোটিশ প্রকাশ", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenEvents,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueGoldDark),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Celebration, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ধর্মীয় অনুষ্ঠান", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Button(
                        onClick = onOpenPrayerSchedule,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueInfoBlue),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নামাজের সময়সূচি", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
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


