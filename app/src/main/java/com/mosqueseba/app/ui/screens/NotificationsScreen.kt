package com.mosqueseba.app.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.window.Dialog
import com.mosqueseba.app.data.local.entity.*
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.MehrabArchCard
import com.mosqueseba.app.ui.components.MosqueToastController
import com.mosqueseba.app.ui.components.toBengaliDigits
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel
import java.util.Locale

enum class NotificationCategory(val labelBn: String) {
    ACTION_REQUIRED("অনুমোদন ও কাজ"),
    NOTICE("নোটিশ"),
    MEETING_EVENT("সভা ও অনুষ্ঠান"),
    PERSONAL("ব্যক্তিগত ও ডিউটি")
}

sealed interface NotificationActionTarget {
    data class PendingTransaction(val transaction: TransactionEntity) : NotificationActionTarget
    data class UnverifiedCashbox(val record: CashboxRecordEntity) : NotificationActionTarget
    data class TomorrowFoodApproval(val foodSchedule: FoodScheduleEntity) : NotificationActionTarget
    data class NoticeDetail(val notice: NoticeEntity) : NotificationActionTarget
    data class OpenModule(val moduleKey: String) : NotificationActionTarget
}

data class RoleNotificationItem(
    val id: String,
    val title: String,
    val summary: String,
    val metaLine: String,
    val badgeText: String,
    val isUrgent: Boolean,
    val category: NotificationCategory,
    val icon: ImageVector,
    val accentColor: Color,
    val target: NotificationActionTarget
)

private enum class NotificationFilter(val labelBn: String) {
    ALL("সব"),
    UNREAD("অপঠিত"),
    ACTIONS("অনুমোদন ও কাজ"),
    NOTICES("নোটিশ"),
    EVENTS("সভা ও অনুষ্ঠান")
}

fun buildRoleNotificationItems(
    role: UserRole,
    savedMemberId: String?,
    currentMember: MemberEntity? = null,
    pendingTransactions: List<TransactionEntity>,
    cashboxRecords: List<CashboxRecordEntity>,
    officeDocuments: List<OfficeDocumentEntity>,
    advisoryNotes: List<AdvisoryNoteEntity>,
    notices: List<NoticeEntity>,
    meetings: List<MeetingEntity>,
    events: List<MosqueEventEntity>,
    dutyRosters: List<DutyRosterEntity>,
    foodSchedules: List<FoodScheduleEntity>,
    chandaPayments: List<ChandaPaymentEntity>,
    welfareCases: List<WelfareCaseEntity> = emptyList()
): List<RoleNotificationItem> {
    val items = mutableListOf<RoleNotificationItem>()

    // 1. Pending Financial Vouchers
    if (role.canApproveFinance() || role.canManageFinance()) {
        pendingTransactions.forEach { txn ->
            items.add(
                RoleNotificationItem(
                    id = "txn_${txn.id}",
                    title = "ভাউচার অনুমোদন বাকি: ${txn.categoryNameBn}",
                    summary = "পরিমাণ: ৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", txn.amount))} • প্রস্তাবক: ${txn.createdBy}",
                    metaLine = "${toBengaliDigits(txn.txnDate)} • কোড: ${txn.transactionCode}",
                    badgeText = if (role.canApproveFinance()) "অনুমোদন প্রয়োজন" else "অপেক্ষমাণ",
                    isUrgent = true,
                    category = NotificationCategory.ACTION_REQUIRED,
                    icon = Icons.Default.Gavel,
                    accentColor = MosqueUrgentRed,
                    target = NotificationActionTarget.PendingTransaction(txn)
                )
            )
        }
    }

    // 2. Cashbox Witness & Bank Deposit Pending
    if (role.canWitnessCashbox() || role.canManageCashbox()) {
        cashboxRecords.filter { !it.witnessVerified }.forEach { record ->
            items.add(
                RoleNotificationItem(
                    id = "cb_witness_${record.id}",
                    title = "দান বাক্স সাক্ষী যাচাই: ${record.boxType}",
                    summary = "গণনাকৃত ৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", record.countedAmount))} • গণনাকারী: ${record.countedBy}",
                    metaLine = toBengaliDigits(record.collectionDate),
                    badgeText = if (role.canWitnessCashbox()) "সাক্ষী নিশ্চিত করুন" else "সাক্ষী বাকি",
                    isUrgent = true,
                    category = NotificationCategory.ACTION_REQUIRED,
                    icon = Icons.Default.Inventory2,
                    accentColor = MosqueWarningAmber,
                    target = NotificationActionTarget.UnverifiedCashbox(record)
                )
            )
        }
        if (role.canManageCashbox()) {
            cashboxRecords.filter { it.witnessVerified && it.depositStatus != "bank_deposited" }.forEach { record ->
                items.add(
                    RoleNotificationItem(
                        id = "cb_bank_${record.id}",
                        title = "ব্যাংক জমা বাকি: ${record.boxType}",
                        summary = "৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", record.countedAmount))} সাক্ষী দ্বারা যাচাইকৃত, ব্যাংকে জমা অপেক্ষমাণ",
                        metaLine = toBengaliDigits(record.collectionDate),
                        badgeText = "ব্যাংক জমা",
                        isUrgent = false,
                        category = NotificationCategory.ACTION_REQUIRED,
                        icon = Icons.Default.AccountBalance,
                        accentColor = MosqueGoldDeep,
                        target = NotificationActionTarget.OpenModule("cashbox")
                    )
                )
            }
        }
    }

    // 3. Pending Office Documents
    if (role.canManageOfficeDocs()) {
        officeDocuments.filter { it.status == "জবাব অপেক্ষমান" }.forEach { doc ->
            items.add(
                RoleNotificationItem(
                    id = "doc_${doc.id}",
                    title = "দাপ্তরিক নথির জবাব বাকি: ${doc.title}",
                    summary = "${doc.category} • প্রেরক/পক্ষ: ${doc.counterparty}",
                    metaLine = "${doc.docDate} • ${doc.docCode}",
                    badgeText = "জবাব অপেক্ষমান",
                    isUrgent = true,
                    category = NotificationCategory.ACTION_REQUIRED,
                    icon = Icons.Default.FolderShared,
                    accentColor = RoleOfficeAccent,
                    target = NotificationActionTarget.OpenModule("office_docs")
                )
            )
        }
    }

    // 4. Active Sharia & Advisory Notes
    if (role in listOf(
            UserRole.PRESIDENT,
            UserRole.VICE_PRESIDENT,
            UserRole.GENERAL_SECRETARY,
            UserRole.ASSISTANT_SECRETARY,
            UserRole.IMAM,
            UserRole.ADVISORY_BOARD
        )
    ) {
        advisoryNotes.filter { it.status == "পর্যালোচনাধীন" }.forEach { note ->
            val isSharia = note.noteType == "SHARIA_REVIEW"
            items.add(
                RoleNotificationItem(
                    id = "adv_${note.id}",
                    title = if (isSharia) "শরিয়াহ পর্যালোচনা: ${note.topic}" else "উপদেষ্টা পরামর্শ: ${note.topic}",
                    summary = note.recommendation,
                    metaLine = "${note.createdDate} • ${note.authorName}",
                    badgeText = "পর্যালোচনাধীন",
                    isUrgent = true,
                    category = NotificationCategory.ACTION_REQUIRED,
                    icon = Icons.Default.Balance,
                    accentColor = RoleAdvisoryAccent,
                    target = NotificationActionTarget.OpenModule("sharia_advisory")
                )
            )
        }
    }

    // 4B. Pending Welfare & Zakat Disbursements
    if (role in listOf(
            UserRole.PRESIDENT,
            UserRole.VICE_PRESIDENT,
            UserRole.GENERAL_SECRETARY,
            UserRole.WELFARE_EDUCATION_SECRETARY
        )
    ) {
        welfareCases.filter { it.status != "disbursed" }.forEach { wCase ->
            items.add(
                RoleNotificationItem(
                    id = "welfare_${wCase.id}",
                    title = "সমাজকল্যাণ বিতরণ বাকি: ${wCase.beneficiaryName}",
                    summary = "${wCase.category} • বরাদ্দ: ৳${toBengaliDigits(wCase.amountApproved.toInt().toString())}",
                    metaLine = "${toBengaliDigits(wCase.applicationDate)} • ${toBengaliDigits(wCase.mobile)}",
                    badgeText = "বিতরণ অপেক্ষায়",
                    isUrgent = role == UserRole.WELFARE_EDUCATION_SECRETARY,
                    category = NotificationCategory.ACTION_REQUIRED,
                    icon = Icons.Default.VolunteerActivism,
                    accentColor = RoleWelfareEduAccent,
                    target = NotificationActionTarget.OpenModule("welfare")
                )
            )
        }
    }

    // 5. Mosque Notices (All Roles)
    notices.take(8).forEach { notice ->
        val isUrgentNotice = notice.priority.equals("urgent", ignoreCase = true) || notice.isPinned
        items.add(
            RoleNotificationItem(
                id = "notice_${notice.id}",
                title = notice.title,
                summary = notice.content,
                metaLine = "${notice.publishDate} • ${notice.createdBy}",
                badgeText = when {
                    notice.priority.equals("urgent", ignoreCase = true) -> "জরুরি নোটিশ"
                    notice.isPinned -> "পিন করা নোটিশ"
                    else -> "নোটিশ বোর্ড"
                },
                isUrgent = isUrgentNotice,
                category = NotificationCategory.NOTICE,
                icon = Icons.Default.Campaign,
                accentColor = if (isUrgentNotice) MosqueUrgentRed else MosqueEmeraldPrimary,
                target = NotificationActionTarget.NoticeDetail(notice)
            )
        )
    }

    // 6. Upcoming Committee Meetings (Committee & Imam roles)
    val seesMeetings = role !in listOf(UserRole.MUAZZIN, UserRole.KHADEM, UserRole.GENERAL_MEMBER)
    if (seesMeetings) {
        meetings.filter { it.status != "completed" }.forEach { meeting ->
            items.add(
                RoleNotificationItem(
                    id = "meeting_${meeting.id}",
                    title = "আসন্ন সভা: ${meeting.title}",
                    summary = "স্থান: ${meeting.location} • এজেন্ডা: ${meeting.agenda}",
                    metaLine = meeting.meetingDate,
                    badgeText = "কমিটি সভা",
                    isUrgent = false,
                    category = NotificationCategory.MEETING_EVENT,
                    icon = Icons.Default.Event,
                    accentColor = MosqueGoldDeep,
                    target = NotificationActionTarget.OpenModule("meetings")
                )
            )
        }
    }

    // 7. Upcoming Events (All Roles)
    events.filter { it.status != "completed" }.forEach { event ->
        items.add(
            RoleNotificationItem(
                id = "event_${event.id}",
                title = "আসন্ন অনুষ্ঠান: ${event.title}",
                summary = "অতিথি/বক্তা: ${event.speakerOrGuest} • স্থান: ${event.venue}",
                metaLine = "${event.dateStr} (${event.timeStr})",
                badgeText = "মাহফিল ও ইভেন্ট",
                isUrgent = false,
                category = NotificationCategory.MEETING_EVENT,
                icon = Icons.Default.Celebration,
                accentColor = MosqueEmeraldPrimary,
                target = NotificationActionTarget.OpenModule("events")
            )
        )
    }

    // 7B. 1-Day-Ahead Food Schedule Confirmation (Any Member Assigned for Tomorrow)
    val isoFmt = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val todayCal = java.util.Calendar.getInstance()
    val todayIso = isoFmt.format(todayCal.time)
    val tomorrowIso = (todayCal.clone() as java.util.Calendar).apply {
        add(java.util.Calendar.DAY_OF_YEAR, 1)
    }.let { isoFmt.format(it.time) }
    val normalizeDigits: (String) -> String = { raw ->
        val bn = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        buildString(raw.length) {
            for (ch in raw) {
                val idx = bn.indexOf(ch)
                append(if (idx >= 0) ('0' + idx) else ch)
            }
        }.trim()
    }

    val myMobileDigits = currentMember?.mobile?.filter { it.isDigit() }.orEmpty()
    val myNameClean = currentMember?.nameBn?.trim().orEmpty()
    if (myNameClean.isNotBlank() || myMobileDigits.length >= 6) {
        foodSchedules.filter { food ->
            val foodDate = normalizeDigits(food.dateStr)
            val foodDigits = normalizeDigits(food.providerMobile).filter { it.isDigit() }
            val matchesMember = (myMobileDigits.length >= 6 && foodDigits == myMobileDigits) ||
                (myNameClean.isNotBlank() && food.providerName.trim().equals(myNameClean, ignoreCase = true))
            foodDate == tomorrowIso && food.status == "scheduled" && matchesMember
        }.forEach { food ->
            items.add(
                RoleNotificationItem(
                    id = "food_tomorrow_${food.id}_${food.providerName}",
                    title = "আগামীকাল খাবারের দায়িত্ব: ${food.receiverStaffName}",
                    summary = "আগামীকাল (${toBengaliDigits(food.dateStr)}) আপনাকে ${food.receiverStaffName}-এর সারাদিনের খাবার দিতে হবে। সম্মতি বা অপারগতা জানান।",
                    metaLine = "১ দিন আগে রিমাইন্ডার • দাতা: ${food.providerName}",
                    badgeText = "সম্মতি প্রয়োজন",
                    isUrgent = true,
                    category = NotificationCategory.ACTION_REQUIRED,
                    icon = Icons.Default.Restaurant,
                    accentColor = MosqueGoldDeep,
                    target = NotificationActionTarget.TomorrowFoodApproval(food)
                )
            )
        }
    }

    // 7C. Unassigned Rejected Food Slots Alert for Managers & Imam
    if (role.canScheduleMeetings() || role == UserRole.IMAM) {
        foodSchedules.filter { it.status == "rejected" }.forEach { food ->
            items.add(
                RoleNotificationItem(
                    id = "food_rejected_${food.id}",
                    title = "খাবারের বিকল্প দাতা প্রয়োজন (${toBengaliDigits(food.dateStr)})",
                    summary = "${food.providerName} অপারগতা জানিয়েছেন (${food.receiverStaffName}) — দ্রুত বিকল্প দাতা নির্ধারণ করুন।",
                    metaLine = toBengaliDigits(food.dateStr),
                    badgeText = "বিকল্প প্রয়োজন",
                    isUrgent = true,
                    category = NotificationCategory.ACTION_REQUIRED,
                    icon = Icons.Default.WarningAmber,
                    accentColor = MosqueUrgentRed,
                    target = NotificationActionTarget.OpenModule("food")
                )
            )
        }
    }

    // 8. Staff Duty & Meal Schedule Alerts (Imam, Muazzin, Khadem)
    if (role in listOf(UserRole.IMAM, UserRole.MUAZZIN, UserRole.KHADEM)) {
        dutyRosters.take(2).forEach { roster ->
            items.add(
                RoleNotificationItem(
                    id = "roster_${roster.id}",
                    title = "ওয়াক্ত ও ডিউটি রোস্টার: ${roster.prayerName}",
                    summary = "${roster.dayOfWeek} • দায়িত্ব: ${roster.roleTitle} (${roster.staffNameBn})",
                    metaLine = roster.notes.ifBlank { "নিয়মিত দায়িত্ব সূচি" },
                    badgeText = "ডিউটি রোস্টার",
                    isUrgent = false,
                    category = NotificationCategory.PERSONAL,
                    icon = Icons.Default.EventNote,
                    accentColor = MosqueGoldDeep,
                    target = NotificationActionTarget.OpenModule("roster")
                )
            )
        }
        val activeFoods = foodSchedules
            .filter { it.status == "scheduled" || it.status == "approved" }
            .filter { food ->
                when (role) {
                    UserRole.IMAM -> food.receiverStaffName.contains("ইমাম", ignoreCase = true)
                    UserRole.MUAZZIN -> food.receiverStaffName.contains("মুয়াজ্জিন", ignoreCase = true) ||
                        food.receiverStaffName.contains("মুয়াজ্জিন", ignoreCase = true)
                    UserRole.KHADEM -> food.receiverStaffName.contains("খাদেম", ignoreCase = true)
                    else -> true
                }
            }
            .sortedBy { normalizeDigits(it.dateStr) }
            .take(3)
        activeFoods.forEach { food ->
            val normDate = normalizeDigits(food.dateStr)
            val isToday = normDate == todayIso
            val statusTxt = if (food.status == "approved") "সম্মত" else "অপেক্ষমাণ"
            items.add(
                RoleNotificationItem(
                    id = "food_${food.id}",
                    title = if (isToday) {
                        "আজকের খাবার: ${food.providerName}"
                    } else {
                        "খাবার শিডিউল (${toBengaliDigits(food.dateStr)}): ${food.providerName}"
                    },
                    summary = "দাতা: ${food.providerName} (${toBengaliDigits(food.providerMobile)}) • স্ট্যাটাস: $statusTxt",
                    metaLine = toBengaliDigits(food.dateStr),
                    badgeText = if (isToday) "আজকের খাবার" else "খাবার ব্যবস্থা",
                    isUrgent = isToday,
                    category = NotificationCategory.PERSONAL,
                    icon = Icons.Default.Restaurant,
                    accentColor = if (isToday) MosqueEmeraldPrimary else MosqueInfoBlue,
                    target = NotificationActionTarget.OpenModule("food")
                )
            )
        }
    }

    // 9. Personal Chanda Receipt Update (Strictly for the member who donated)
    if (!savedMemberId.isNullOrBlank()) {
        val personalReceipts = chandaPayments
            .filter { it.memberId == savedMemberId }
        personalReceipts.take(3).forEach { pay ->
            items.add(
                RoleNotificationItem(
                    id = "chanda_${pay.id}",
                    title = "চাঁদা রসিদ সংগৃহীত (${pay.month})",
                    summary = "পরিশোধিত ৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", pay.amountPaid))} • রসিদ: ${pay.receiptCode}",
                    metaLine = toBengaliDigits(pay.paidAt),
                    badgeText = "ডিজিটাল রসিদ",
                    isUrgent = false,
                    category = NotificationCategory.PERSONAL,
                    icon = Icons.Default.ReceiptLong,
                    accentColor = MosqueSuccessGreen,
                    target = NotificationActionTarget.OpenModule("chanda")
                )
            )
        }
    }

    return items
}

@Composable
fun NotificationsScreen(
    viewModel: MosqueViewModel,
    onBack: () -> Unit,
    onNavigateToModule: (String) -> Unit
) {
    BackHandler { onBack() }

    val currentRole by viewModel.currentUserRole.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val readIds by viewModel.readNotificationIds.collectAsState()

    val pendingTransactions by viewModel.pendingTransactions.collectAsState()
    val cashboxRecords by viewModel.allCashboxRecords.collectAsState()
    val officeDocuments by viewModel.allOfficeDocuments.collectAsState()
    val advisoryNotes by viewModel.allAdvisoryNotes.collectAsState()
    val notices by viewModel.allNotices.collectAsState()
    val meetings by viewModel.allMeetings.collectAsState()
    val events by viewModel.allEvents.collectAsState()
    val dutyRosters by viewModel.allDutyRosters.collectAsState()
    val foodSchedules by viewModel.allFoodSchedules.collectAsState()
    val chandaPayments by viewModel.allChandaPayments.collectAsState()
    val welfareCases by viewModel.allWelfareCases.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    val savedMemberId = remember(currentRole) { viewModel.sessionManager.getSavedMemberId() }
    val savedRole = remember(currentRole) { viewModel.sessionManager.getSavedRole() }
    val currentMember = remember(currentRole, allMembers, savedRole, savedMemberId) {
        allMembers.firstOrNull { it.id == savedMemberId && currentRole == savedRole }
            ?: allMembers.firstOrNull { it.roleCode.equals(currentRole.code, ignoreCase = true) }
    }
    val context = androidx.compose.ui.platform.LocalContext.current

    val roleNotificationItems = remember(
        currentRole,
        savedMemberId,
        currentMember,
        pendingTransactions,
        cashboxRecords,
        officeDocuments,
        advisoryNotes,
        notices,
        meetings,
        events,
        dutyRosters,
        foodSchedules,
        chandaPayments,
        welfareCases
    ) {
        buildRoleNotificationItems(
            role = currentRole,
            savedMemberId = savedMemberId,
            currentMember = currentMember,
            pendingTransactions = pendingTransactions,
            cashboxRecords = cashboxRecords,
            officeDocuments = officeDocuments,
            advisoryNotes = advisoryNotes,
            notices = notices,
            meetings = meetings,
            events = events,
            dutyRosters = dutyRosters,
            foodSchedules = foodSchedules,
            chandaPayments = chandaPayments,
            welfareCases = welfareCases
        )
    }

    val availableUpdate by viewModel.availableUpdate.collectAsState()
    val allItems = remember(roleNotificationItems, availableUpdate) {
        if (availableUpdate != null && availableUpdate?.isUpdateAvailable == true) {
            val updateItem = RoleNotificationItem(
                id = "app_update_${availableUpdate!!.latestVersionName}",
                title = "নতুন অ্যাপ আপডেট: v${toBengaliDigits(availableUpdate!!.latestVersionName)}",
                summary = "মসজিদ সেবার সর্বশেষ সংস্করণ প্রকাশিত হয়েছে। ট্যাপ করে সরাসরি আপডেট করুন। কোনো ডাটা নষ্ট হবে না এবং আনইনস্টল করতে হবে না।",
                metaLine = "সংস্করণ: v${availableUpdate!!.latestVersionName}",
                badgeText = "জরুরি আপডেট",
                isUrgent = true,
                category = NotificationCategory.ACTION_REQUIRED,
                icon = Icons.Default.SystemUpdate,
                accentColor = MosqueEmeraldPrimary,
                target = NotificationActionTarget.OpenModule("app_update")
            )
            listOf(updateItem) + roleNotificationItems
        } else {
            roleNotificationItems
        }
    }

    var selectedFilter by remember { mutableStateOf(NotificationFilter.ALL) }
    var selectedNoticeForDialog by remember { mutableStateOf<NoticeEntity?>(null) }

    val unreadCount = remember(allItems, readIds) {
        allItems.count { it.id !in readIds }
    }
    val urgentCount = remember(allItems) {
        allItems.count { it.isUrgent }
    }

    val filteredItems = remember(allItems, readIds, selectedFilter) {
        when (selectedFilter) {
            NotificationFilter.ALL -> allItems
            NotificationFilter.UNREAD -> allItems.filter { it.id !in readIds }
            NotificationFilter.ACTIONS -> allItems.filter { it.category == NotificationCategory.ACTION_REQUIRED }
            NotificationFilter.NOTICES -> allItems.filter { it.category == NotificationCategory.NOTICE }
            NotificationFilter.EVENTS -> allItems.filter { it.category == NotificationCategory.MEETING_EVENT }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Modern Clean Surface Header
        val isDark = LocalIsDarkTheme.current
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
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("notifications_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "বিজ্ঞপ্তি ও বার্তা কেন্দ্র",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${currentRole.titleBn} • গুরুত্বপূর্ণ সতর্কতা ও আপডেট",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (unreadCount > 0) {
                        Surface(
                            onClick = {
                                viewModel.markAllNotificationsAsRead(allItems.map { it.id })
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDark) MosqueEmeraldPrimary.copy(alpha = 0.22f) else MosqueEmeraldSoft,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            modifier = Modifier.testTag("notifications_mark_all_read")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "সব পঠিত",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Compact live stats bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NotificationStatPill(
                        modifier = Modifier.weight(1f),
                        label = "মোট বিজ্ঞপ্তি",
                        value = "${toBengaliDigits(allItems.size.toString())} টি",
                        accent = MaterialTheme.colorScheme.primary
                    )
                    NotificationStatPill(
                        modifier = Modifier.weight(1f),
                        label = "অপঠিত",
                        value = "${toBengaliDigits(unreadCount.toString())} টি",
                        accent = if (unreadCount > 0) MosqueWarningAmber else (if (isDark) MosqueGreenAccent else MosqueSuccessGreen)
                    )
                    NotificationStatPill(
                        modifier = Modifier.weight(1f),
                        label = "জরুরি / করণীয়",
                        value = "${toBengaliDigits(urgentCount.toString())} টি",
                        accent = if (urgentCount > 0) MosqueUrgentRed else (if (isDark) MosqueGreenAccent else MosqueSuccessGreen)
                    )
                }

                HorizontalDivider(
                    color = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle,
                    thickness = 0.8.dp
                )
            }
        }

        // 2. Filter Chips Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(NotificationFilter.entries) { filter ->
                    val selected = selectedFilter == filter
                    val countLabel = when (filter) {
                        NotificationFilter.ALL -> allItems.size
                        NotificationFilter.UNREAD -> unreadCount
                        NotificationFilter.ACTIONS -> allItems.count { it.category == NotificationCategory.ACTION_REQUIRED }
                        NotificationFilter.NOTICES -> allItems.count { it.category == NotificationCategory.NOTICE }
                        NotificationFilter.EVENTS -> allItems.count { it.category == NotificationCategory.MEETING_EVENT }
                    }
                    FilterChip(
                        selected = selected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = "${filter.labelBn} (${toBengaliDigits(countLabel.toString())})",
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (LocalIsDarkTheme.current) MosqueEmeraldPrimary else MosqueEmeraldSoft,
                            selectedLabelColor = if (LocalIsDarkTheme.current) Color.White else MosqueEmeraldDark
                        ),
                        modifier = Modifier.testTag("notifications_filter_${filter.name.lowercase()}")
                    )
                }
            }
        }

        // 3. Notification Feed
        LazyColumn(
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
        ) {
            if (!notificationsEnabled) {
                item {
                    val isDarkBanner = LocalIsDarkTheme.current
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkBanner) MosqueNightSurfaceVariant else MosqueGoldSoft
                        ),
                        border = BorderStroke(1.dp, MosqueGoldDeep.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    tint = if (isDarkBanner) MosqueGold else MosqueGoldDeep,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "হেডার নোটিফিকেশন ব্যাজ সেটিংস থেকে বন্ধ রাখা আছে।",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            TextButton(
                                onClick = { viewModel.setNotificationsEnabled(true) },
                                modifier = Modifier.testTag("notifications_enable_badge_button")
                            ) {
                                Text("চালু করুন", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            item(key = "push_notification_tester") {
                val isDarkTester = LocalIsDarkTheme.current
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTester) MosqueNightSurfaceVariant else MosqueBgLight
                    ),
                    border = BorderStroke(1.dp, if (isDarkTester) MosqueNightBorder else MosqueCardBorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MosqueEmeraldPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MosqueEmeraldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "সিস্টেম পুশ নোটিফিকেশন",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "নোটিশ, রসিদ ও নির্বাচনের স্ট্যাটাস বার অ্যালার্ট",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        FilledTonalButton(
                            onClick = {
                                viewModel.sendTestPushNotification()
                                MosqueToastController.success("টেস্ট পুশ নোটিফিকেশন স্ট্যাটাস বারে পাঠানো হয়েছে")
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MosqueEmeraldPrimary.copy(alpha = 0.14f),
                                contentColor = MosqueEmeraldPrimary
                            )
                        ) {
                            Text("পরীক্ষা", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            if (filteredItems.isEmpty()) {
                item {
                    val isDarkEmpty = LocalIsDarkTheme.current
                    MehrabArchCard(
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        borderColor = MosqueCardBorderGold,
                        elevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(if (isDarkEmpty) MosqueNightSurfaceVariant else MosqueEmeraldSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = when (selectedFilter) {
                                    NotificationFilter.UNREAD -> "কোনো অপঠিত বিজ্ঞপ্তি নেই"
                                    NotificationFilter.ACTIONS -> "কোনো অপেক্ষমাণ অনুমোদন বা কাজ নেই"
                                    else -> "বর্তমানে কোনো নতুন বিজ্ঞপ্তি নেই"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "মসজিদের নতুন নোটিশ, সভা বা আর্থিক অনুমোদনের তথ্য এলে এখানে দেখা যাবে।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredItems, key = { it.id }) { item ->
                    val isRead = item.id in readIds
                    NotificationFeedCard(
                        item = item,
                        isRead = isRead,
                        currentRole = currentRole,
                        onCardClick = {
                            viewModel.markNotificationAsRead(item.id)
                            when (val target = item.target) {
                                is NotificationActionTarget.PendingTransaction -> onNavigateToModule("finance")
                                is NotificationActionTarget.UnverifiedCashbox -> onNavigateToModule("cashbox")
                                is NotificationActionTarget.TomorrowFoodApproval -> onNavigateToModule("food")
                                is NotificationActionTarget.NoticeDetail -> selectedNoticeForDialog = target.notice
                                is NotificationActionTarget.OpenModule -> {
                                    if (target.moduleKey == "app_update") {
                                        val url = availableUpdate?.apkDownloadUrl ?: availableUpdate?.releasePageUrl
                                        if (!url.isNullOrBlank()) {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                        }
                                    } else {
                                        onNavigateToModule(target.moduleKey)
                                    }
                                }
                            }
                        },
                        onApproveTransaction = { txn ->
                            viewModel.markNotificationAsRead(item.id)
                            viewModel.updateTransactionStatus(txn, true)
                        },
                        onRejectTransaction = { txn ->
                            viewModel.markNotificationAsRead(item.id)
                            viewModel.updateTransactionStatus(txn, false)
                        },
                        onVerifyCashbox = { record ->
                            viewModel.markNotificationAsRead(item.id)
                            viewModel.verifyCashboxAsWitness(record)
                        },
                        onApproveFood = { food ->
                            viewModel.markNotificationAsRead(item.id)
                            viewModel.approveFoodSchedule(food)
                            android.widget.Toast.makeText(
                                context,
                                "আগামীকালের খাবারের দায়িত্বে আপনার সম্মতি নিশ্চিত হয়েছে।",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        onRejectFood = { food ->
                            viewModel.markNotificationAsRead(item.id)
                            viewModel.rejectFoodSchedule(food) { reassignedTo ->
                                val msg = if (reassignedTo != null) {
                                    "অপারগতা জানানো হয়েছে। স্বয়ংক্রিয়ভাবে পরবর্তী দাতা ($reassignedTo)-কে দায়িত্ব হস্তান্তর করা হয়েছে।"
                                } else {
                                    "অপারগতা জানানো হয়েছে। বিকল্প দাতা নির্ধারণের জন্য কমিটিকে সতর্কবার্তা পাঠানো হয়েছে।"
                                }
                                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    selectedNoticeForDialog?.let { notice ->
        NoticeDetailPopupDialog(
            notice = notice,
            onDismiss = { selectedNoticeForDialog = null },
            onOpenAllNotices = {
                selectedNoticeForDialog = null
                onNavigateToModule("notices")
            }
        )
    }
}

@Composable
private fun NotificationStatPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (isDark) MosqueNightSurfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(0.8.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NotificationFeedCard(
    item: RoleNotificationItem,
    isRead: Boolean,
    currentRole: UserRole,
    onCardClick: () -> Unit,
    onApproveTransaction: (TransactionEntity) -> Unit,
    onRejectTransaction: (TransactionEntity) -> Unit,
    onVerifyCashbox: (CashboxRecordEntity) -> Unit,
    onApproveFood: (FoodScheduleEntity) -> Unit,
    onRejectFood: (FoodScheduleEntity) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val containerColor = when {
        !isRead && item.isUrgent -> if (isDark) MosqueNightSurfaceVariant else MosqueGoldSoft.copy(alpha = 0.72f)
        !isRead -> if (isDark) MosqueEmeraldDark.copy(alpha = 0.35f) else MosqueEmeraldSoft.copy(alpha = 0.45f)
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        !isRead && item.isUrgent -> item.accentColor.copy(alpha = 0.65f)
        !isRead -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
        else -> if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isRead) 1.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("notification_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(item.accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = item.accentColor.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = item.badgeText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = item.accentColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (!isRead) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (item.isUrgent) MosqueUrgentRed else MaterialTheme.colorScheme.primary)
                                )
                                Text(
                                    text = "নতুন",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (item.isUrgent) MosqueUrgentRed else MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "বিস্তারিত দেখুন",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isRead) FontWeight.Bold else FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = item.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = item.metaLine,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )
                }
            }

            // Inline role-authorized actions
            when (val target = item.target) {
                is NotificationActionTarget.PendingTransaction -> {
                    if (currentRole.canApproveFinance()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onRejectTransaction(target.transaction) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueUrgentRed),
                                border = BorderStroke(1.dp, MosqueUrgentRed.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("notif_reject_txn_${target.transaction.id}")
                            ) {
                                Text("বাতিল", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onApproveTransaction(target.transaction) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                                modifier = Modifier
                                    .weight(1.4f)
                                    .heightIn(min = 48.dp)
                                    .testTag("notif_approve_txn_${target.transaction.id}")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("অনুমোদন দিন", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
                is NotificationActionTarget.UnverifiedCashbox -> {
                    if (currentRole.canWitnessCashbox()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onVerifyCashbox(target.record) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("notif_verify_cashbox_${target.record.id}")
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("উপস্থিত সাক্ষী হিসেবে নিশ্চিত করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                is NotificationActionTarget.TomorrowFoodApproval -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onRejectFood(target.foodSchedule) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueUrgentRed),
                            border = BorderStroke(1.dp, MosqueUrgentRed.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("notif_reject_food_${target.foodSchedule.id}")
                        ) {
                            Text("পারব না", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onApproveFood(target.foodSchedule) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                            modifier = Modifier
                                .weight(1.3f)
                                .heightIn(min = 48.dp)
                                .testTag("notif_approve_food_${target.foodSchedule.id}")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("সম্মতি দিন", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun NoticeDetailPopupDialog(
    notice: NoticeEntity,
    onDismiss: () -> Unit,
    onOpenAllNotices: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val dividerColor = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MosqueCardBorderGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (notice.priority.equals("urgent", true)) {
                            MosqueUrgentRed.copy(alpha = 0.14f)
                        } else if (isDark) {
                            MosqueNightSurfaceVariant
                        } else {
                            MosqueEmeraldSoft
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (notice.priority.equals("urgent", true)) "জরুরি নোটিশ" else "মসজিদ নোটিশ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (notice.priority.equals("urgent", true)) MosqueUrgentRed else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("notice_popup_close_icon")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${toBengaliDigits(notice.publishDate)} • প্রকাশক: ${notice.createdBy}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = dividerColor)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = notice.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("notice_popup_dismiss_button")
                    ) {
                        Text("বন্ধ করুন", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onOpenAllNotices,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        modifier = Modifier
                            .weight(1.2f)
                            .heightIn(min = 48.dp)
                            .testTag("notice_popup_all_button")
                    ) {
                        Text("সব নোটিশ দেখুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
