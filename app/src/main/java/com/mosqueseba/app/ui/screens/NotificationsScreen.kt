package com.mosqueseba.app.ui.screens

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
    pendingTransactions: List<TransactionEntity>,
    cashboxRecords: List<CashboxRecordEntity>,
    officeDocuments: List<OfficeDocumentEntity>,
    advisoryNotes: List<AdvisoryNoteEntity>,
    notices: List<NoticeEntity>,
    meetings: List<MeetingEntity>,
    events: List<MosqueEventEntity>,
    dutyRosters: List<DutyRosterEntity>,
    foodSchedules: List<FoodScheduleEntity>,
    chandaPayments: List<ChandaPaymentEntity>
): List<RoleNotificationItem> {
    val items = mutableListOf<RoleNotificationItem>()

    // 1. Pending Financial Vouchers
    if (role.canApproveFinance() || role.canManageFinance()) {
        pendingTransactions.forEach { txn ->
            items.add(
                RoleNotificationItem(
                    id = "txn_${txn.id}",
                    title = "ভাউচার অনুমোদন বাকি: ${txn.categoryNameBn}",
                    summary = "পরিমাণ: ৳${String.format(Locale.getDefault(), "%,.0f", txn.amount)} • প্রস্তাবক: ${txn.createdBy}",
                    metaLine = "${txn.txnDate} • কোড: ${txn.transactionCode}",
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
                    summary = "গণনাকৃত ৳${String.format(Locale.getDefault(), "%,.0f", record.countedAmount)} • গণনাকারী: ${record.countedBy}",
                    metaLine = record.collectionDate,
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
                        summary = "৳${String.format(Locale.getDefault(), "%,.0f", record.countedAmount)} সাক্ষী দ্বারা যাচাইকৃত, ব্যাংকে জমা অপেক্ষমাণ",
                        metaLine = record.collectionDate,
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
        foodSchedules.take(2).forEach { food ->
            items.add(
                RoleNotificationItem(
                    id = "food_${food.id}",
                    title = "খাবার শিডিউল (${food.mealType}): ${food.receiverStaffName}",
                    summary = "দাতা: ${food.providerName} (${food.providerMobile})",
                    metaLine = food.dateStr,
                    badgeText = "খাবার ব্যবস্থা",
                    isUrgent = false,
                    category = NotificationCategory.PERSONAL,
                    icon = Icons.Default.Restaurant,
                    accentColor = MosqueInfoBlue,
                    target = NotificationActionTarget.OpenModule("food")
                )
            )
        }
    }

    // 9. Personal Chanda Receipt Update (General Member)
    if (role == UserRole.GENERAL_MEMBER) {
        val personalReceipts = chandaPayments
            .filter { savedMemberId != null && it.memberId == savedMemberId }
            .ifEmpty { chandaPayments.take(1) }
        personalReceipts.take(2).forEach { pay ->
            items.add(
                RoleNotificationItem(
                    id = "chanda_${pay.id}",
                    title = "চাঁদা রসিদ সংগৃহীত (${pay.month})",
                    summary = "পরিশোধিত ৳${String.format(Locale.getDefault(), "%,.0f", pay.amountPaid)} • রসিদ: ${pay.receiptCode}",
                    metaLine = pay.paidAt,
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
    val savedMemberId = remember(currentRole) { viewModel.sessionManager.getSavedMemberId() }

    val allItems = remember(
        currentRole,
        savedMemberId,
        pendingTransactions,
        cashboxRecords,
        officeDocuments,
        advisoryNotes,
        notices,
        meetings,
        events,
        dutyRosters,
        foodSchedules,
        chandaPayments
    ) {
        buildRoleNotificationItems(
            role = currentRole,
            savedMemberId = savedMemberId,
            pendingTransactions = pendingTransactions,
            cashboxRecords = cashboxRecords,
            officeDocuments = officeDocuments,
            advisoryNotes = advisoryNotes,
            notices = notices,
            meetings = meetings,
            events = events,
            dutyRosters = dutyRosters,
            foodSchedules = foodSchedules,
            chandaPayments = chandaPayments
        )
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
            .background(MosqueBgLight)
    ) {
        // 1. Emerald-Gold Header
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
                        onClick = onBack,
                        modifier = Modifier.testTag("notifications_back_button")
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
                            text = "বিজ্ঞপ্তি ও বার্তা কেন্দ্র",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${currentRole.titleBn} • গুরুত্বপূর্ণ সতর্কতা ও আপডেট",
                            style = MaterialTheme.typography.labelSmall,
                            color = MosqueGoldLight.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (unreadCount > 0) {
                        Surface(
                            onClick = {
                                viewModel.markAllNotificationsAsRead(allItems.map { it.id })
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = MosqueGold.copy(alpha = 0.22f),
                            border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.7f)),
                            modifier = Modifier.testTag("notifications_mark_all_read")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = null,
                                    tint = MosqueGoldLight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "সব পঠিত",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MosqueGoldLight
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
                        accent = Color.White
                    )
                    NotificationStatPill(
                        modifier = Modifier.weight(1f),
                        label = "অপঠিত",
                        value = "${toBengaliDigits(unreadCount.toString())} টি",
                        accent = if (unreadCount > 0) Color(0xFFFCD34D) else Color(0xFF6EE7B7)
                    )
                    NotificationStatPill(
                        modifier = Modifier.weight(1f),
                        label = "জরুরি / করণীয়",
                        value = "${toBengaliDigits(urgentCount.toString())} টি",
                        accent = if (urgentCount > 0) Color(0xFFFCA5A5) else Color(0xFF6EE7B7)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .background(IslamicGoldTrimGradient)
                )
            }
        }

        // 2. Filter Chips Bar
        Surface(
            color = Color.White,
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
                            selectedContainerColor = MosqueEmeraldSoft,
                            selectedLabelColor = MosqueEmeraldDark
                        ),
                        modifier = Modifier.testTag("notifications_filter_${filter.name.lowercase()}")
                    )
                }
            }
        }

        // 3. Notification Feed
        LazyColumn(
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (!notificationsEnabled) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MosqueGoldSoft),
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
                                    tint = MosqueGoldDeep,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "হেডার নোটিফিকেশন ব্যাজ সেটিংস থেকে বন্ধ রাখা আছে।",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MosqueEmeraldDark
                                )
                            }
                            TextButton(
                                onClick = { viewModel.setNotificationsEnabled(true) },
                                modifier = Modifier.testTag("notifications_enable_badge_button")
                            ) {
                                Text("চালু করুন", fontWeight = FontWeight.ExtraBold, color = MosqueEmeraldPrimary)
                            }
                        }
                    }
                }
            }

            if (filteredItems.isEmpty()) {
                item {
                    MehrabArchCard(
                        backgroundColor = Color.White,
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
                                    .background(MosqueEmeraldSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = null,
                                    tint = MosqueEmeraldPrimary,
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
                                color = MosqueEmeraldDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "মসজিদের নতুন নোটিশ, সভা বা আর্থিক অনুমোদনের তথ্য এলে এখানে দেখা যাবে।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MosqueTextMuted
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
                                is NotificationActionTarget.NoticeDetail -> selectedNoticeForDialog = target.notice
                                is NotificationActionTarget.OpenModule -> onNavigateToModule(target.moduleKey)
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
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accent,
                maxLines = 1
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.85f),
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
    onVerifyCashbox: (CashboxRecordEntity) -> Unit
) {
    val containerColor = when {
        !isRead && item.isUrgent -> MosqueGoldSoft.copy(alpha = 0.72f)
        !isRead -> MosqueEmeraldSoft.copy(alpha = 0.45f)
        else -> Color.White
    }
    val borderColor = when {
        !isRead && item.isUrgent -> item.accentColor.copy(alpha = 0.55f)
        !isRead -> MosqueEmeraldPrimary.copy(alpha = 0.4f)
        else -> MosqueCardBorderSubtle
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isRead) 1.dp else 3.dp),
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
                                        .background(if (item.isUrgent) MosqueUrgentRed else MosqueEmeraldPrimary)
                                )
                                Text(
                                    text = "নতুন",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (item.isUrgent) MosqueUrgentRed else MosqueEmeraldPrimary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "বিস্তারিত দেখুন",
                            tint = MosqueTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isRead) FontWeight.Bold else FontWeight.ExtraBold,
                        color = MosqueEmeraldDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = item.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MosqueTextSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = item.metaLine,
                        style = MaterialTheme.typography.labelSmall,
                        color = MosqueTextMuted
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
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        } else {
                            MosqueEmeraldSoft
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (notice.priority.equals("urgent", true)) "জরুরি নোটিশ" else "মসজিদ নোটিশ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (notice.priority.equals("urgent", true)) MosqueUrgentRed else MosqueEmeraldPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MosqueEmeraldDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${notice.publishDate} • প্রকাশক: ${notice.createdBy}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MosqueTextMuted
                )

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MosqueCardBorderSubtle)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = notice.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MosqueTextPrimary,
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
                    ) {
                        Text("সব নোটিশ দেখুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
