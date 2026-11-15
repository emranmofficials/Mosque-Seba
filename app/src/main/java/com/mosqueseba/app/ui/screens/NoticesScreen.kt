package com.mosqueseba.app.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.data.local.entity.MeetingEntity
import com.mosqueseba.app.data.local.entity.NoticeEntity
import com.mosqueseba.app.ui.components.MeetingResolutionDialog
import com.mosqueseba.app.ui.components.MosqueConfirmDialog
import com.mosqueseba.app.ui.components.MosqueToastController
import com.mosqueseba.app.ui.components.PublishNoticeDialog
import com.mosqueseba.app.ui.components.ScheduleMeetingDialog
import com.mosqueseba.app.ui.components.toBengaliDigits
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel

private enum class NoticeFilter(val labelBn: String) {
    ALL("সকল"),
    URGENT("জরুরি"),
    IMPORTANT("গুরুত্বপূর্ণ"),
    NORMAL("সাধারণ"),
    PINNED("পিন করা")
}

private enum class MeetingFilter(val labelBn: String) {
    ALL("সকল সভা"),
    UPCOMING("আসন্ন"),
    COMPLETED("সম্পন্ন")
}

@Composable
fun NoticesScreen(
    viewModel: MosqueViewModel,
    onPublishNoticeClick: () -> Unit,
    onScheduleMeetingClick: (() -> Unit)? = null,
    initialTabIndex: Int = 0,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val notices by viewModel.allNotices.collectAsState()
    val meetings by viewModel.allMeetings.collectAsState()
    val mosque by viewModel.mosque.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val aiNoticeState by viewModel.aiNoticeState.collectAsState()

    val canPublish = currentRole.canPublishNotices()
    val canSchedule = currentRole.canScheduleMeetings()
    val mosqueName = mosque?.nameBn ?: "বায়তুল জান্নাত জামে মসজিদ"

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(initialTabIndex.coerceIn(0, 1)) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedNoticeFilter by rememberSaveable { mutableStateOf(NoticeFilter.ALL) }
    var selectedMeetingFilter by rememberSaveable { mutableStateOf(MeetingFilter.ALL) }

    var editingNotice by remember { mutableStateOf<NoticeEntity?>(null) }
    var pendingDeleteNotice by remember { mutableStateOf<NoticeEntity?>(null) }

    var editingMeeting by remember { mutableStateOf<MeetingEntity?>(null) }
    var resolutionTargetMeeting by remember { mutableStateOf<MeetingEntity?>(null) }
    var pendingDeleteMeeting by remember { mutableStateOf<MeetingEntity?>(null) }

    val urgentOrPinnedCount = remember(notices) {
        notices.count { it.priority == "urgent" || it.isPinned }
    }
    val upcomingMeetingsCount = remember(meetings) {
        meetings.count { it.status != "completed" }
    }
    val completedMeetingsCount = remember(meetings) {
        meetings.count { it.status == "completed" }
    }

    val filteredNotices = remember(notices, searchQuery, selectedNoticeFilter) {
        notices.filter { notice ->
            val matchesQuery = searchQuery.isBlank() ||
                notice.title.contains(searchQuery, ignoreCase = true) ||
                notice.content.contains(searchQuery, ignoreCase = true) ||
                notice.createdBy.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedNoticeFilter) {
                NoticeFilter.ALL -> true
                NoticeFilter.URGENT -> notice.priority == "urgent"
                NoticeFilter.IMPORTANT -> notice.priority == "important"
                NoticeFilter.NORMAL -> notice.priority == "normal"
                NoticeFilter.PINNED -> notice.isPinned
            }
            matchesQuery && matchesFilter
        }
    }

    val filteredMeetings = remember(meetings, searchQuery, selectedMeetingFilter) {
        meetings.filter { meeting ->
            val matchesQuery = searchQuery.isBlank() ||
                meeting.title.contains(searchQuery, ignoreCase = true) ||
                meeting.location.contains(searchQuery, ignoreCase = true) ||
                meeting.agenda.contains(searchQuery, ignoreCase = true) ||
                meeting.resolution.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedMeetingFilter) {
                MeetingFilter.ALL -> true
                MeetingFilter.UPCOMING -> meeting.status != "completed"
                MeetingFilter.COMPLETED -> meeting.status == "completed"
            }
            matchesQuery && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Emerald-Gold Hero Header + 2-Tab Switcher + KPI Summary
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (onBack != null) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier.testTag("notices_back_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "পিছনে যান",
                                        tint = MosqueGoldLight
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "নোটিশ বোর্ড ও কমিটি সভা",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "মসজিদ ঘোষণা, জরুরি বার্তা ও সভার কার্যবিবরণী",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MosqueGoldLight.copy(alpha = 0.9f)
                                )
                            }
                        }

                        if (selectedTabIndex == 0 && canPublish) {
                            Button(
                                onClick = onPublishNoticeClick,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MosqueGold,
                                    contentColor = MosqueEmeraldDark
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .heightIn(min = 42.dp)
                                    .testTag("publish_notice_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = "নতুন নোটিশ",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "+ নোটিশ",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        } else if (selectedTabIndex == 1 && canSchedule && onScheduleMeetingClick != null) {
                            Button(
                                onClick = onScheduleMeetingClick,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MosqueGold,
                                    contentColor = MosqueEmeraldDark
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .heightIn(min = 42.dp)
                                    .testTag("schedule_meeting_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventAvailable,
                                    contentDescription = "নতুন সভা আহ্বান",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "+ সভা আহ্বান",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Segmented 2-Tab Switcher
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
                            NoticeTabButton(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tab_notices_board"),
                                selected = selectedTabIndex == 0,
                                title = "নোটিশ বোর্ড",
                                countText = toBengaliDigits(notices.size.toString()),
                                icon = Icons.Default.Campaign,
                                onClick = { selectedTabIndex = 0 }
                            )
                            NoticeTabButton(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tab_committee_meetings"),
                                selected = selectedTabIndex == 1,
                                title = "কমিটি সভা",
                                countText = toBengaliDigits(meetings.size.toString()),
                                icon = Icons.Default.EventNote,
                                onClick = { selectedTabIndex = 1 }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3-Metric Summary Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NoticeHeaderSummaryPill(
                            modifier = Modifier.weight(1f),
                            label = "মোট নোটিশ",
                            value = "${toBengaliDigits(notices.size.toString())} টি",
                            accent = MosqueGoldLight
                        )
                        NoticeHeaderSummaryPill(
                            modifier = Modifier.weight(1f),
                            label = "জরুরি / পিন",
                            value = "${toBengaliDigits(urgentOrPinnedCount.toString())} টি",
                            accent = if (urgentOrPinnedCount > 0) Color(0xFFFCD34D) else Color(0xFF6EE7B7)
                        )
                        NoticeHeaderSummaryPill(
                            modifier = Modifier.weight(1f),
                            label = "আসন্ন সভা",
                            value = "${toBengaliDigits(upcomingMeetingsCount.toString())} টি",
                            accent = Color(0xFF6EE7B7)
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

        // 2. Search & Filter Card (Matching MembersScreen & FinanceScreen)
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
                            text = if (selectedTabIndex == 0) {
                                "নোটিশ খুঁজুন (শিরোনাম, বিবরণ, প্রচারক)"
                            } else {
                                "সভা খুঁজুন (নাম, স্থান, এজেন্ডা, সিদ্ধান্ত)"
                            },
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
                        .testTag("notices_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedTabIndex == 0) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(NoticeFilter.entries) { filter ->
                            val count = when (filter) {
                                NoticeFilter.ALL -> notices.size
                                NoticeFilter.URGENT -> notices.count { it.priority == "urgent" }
                                NoticeFilter.IMPORTANT -> notices.count { it.priority == "important" }
                                NoticeFilter.NORMAL -> notices.count { it.priority == "normal" }
                                NoticeFilter.PINNED -> notices.count { it.isPinned }
                            }
                            FilterChip(
                                selected = selectedNoticeFilter == filter,
                                onClick = { selectedNoticeFilter = filter },
                                label = {
                                    Text(
                                        text = "${filter.labelBn} (${toBengaliDigits(count.toString())})",
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MosqueEmeraldPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("notice_filter_${filter.name.lowercase()}")
                            )
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(MeetingFilter.entries) { filter ->
                            val count = when (filter) {
                                MeetingFilter.ALL -> meetings.size
                                MeetingFilter.UPCOMING -> upcomingMeetingsCount
                                MeetingFilter.COMPLETED -> completedMeetingsCount
                            }
                            FilterChip(
                                selected = selectedMeetingFilter == filter,
                                onClick = { selectedMeetingFilter = filter },
                                label = {
                                    Text(
                                        text = "${filter.labelBn} (${toBengaliDigits(count.toString())})",
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MosqueEmeraldPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("meeting_filter_${filter.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        // 3. Content List
        LazyColumn(
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .widthIn(max = 840.dp)
        ) {
            if (selectedTabIndex == 0) {
                if (filteredNotices.isEmpty()) {
                    item {
                        EmptyNoticesMeetingsState(
                            icon = Icons.Default.Campaign,
                            title = if (notices.isEmpty()) "বর্তমানে কোনো সক্রিয় নোটিশ নেই" else "ফিল্টার অনুযায়ী কোনো নোটিশ পাওয়া যায়নি",
                            subtitle = if (notices.isEmpty()) "নতুন ঘোষণা বা নির্দেশনা এলে এখানে দেখা যাবে" else "অনুসন্ধান বা ফিল্টার পরিবর্তন করে পুনরায় চেষ্টা করুন",
                            hasActiveFilters = searchQuery.isNotBlank() || selectedNoticeFilter != NoticeFilter.ALL,
                            onResetFilters = {
                                searchQuery = ""
                                selectedNoticeFilter = NoticeFilter.ALL
                            }
                        )
                    }
                } else {
                    items(filteredNotices, key = { it.id }) { notice ->
                        NoticeCardItem(
                            notice = notice,
                            canManage = canPublish,
                            onTogglePin = {
                                viewModel.togglePinNotice(notice)
                                MosqueToastController.info(
                                    if (notice.isPinned) "নোটিশ আনপিন করা হয়েছে।" else "নোটিশটি পিন করা হয়েছে।"
                                )
                            },
                            onEdit = { editingNotice = notice },
                            onShare = { shareNoticeText(context, mosqueName, notice) },
                            onDeleteRequest = { pendingDeleteNotice = notice }
                        )
                    }
                }
            } else {
                if (filteredMeetings.isEmpty()) {
                    item {
                        EmptyNoticesMeetingsState(
                            icon = Icons.Default.EventNote,
                            title = if (meetings.isEmpty()) "বর্তমানে কোনো নির্ধারিত সভা নেই" else "ফিল্টার অনুযায়ী কোনো সভা পাওয়া যায়নি",
                            subtitle = if (meetings.isEmpty()) "কমিটি সভা আহ্বান করা হলে তারিখ ও আলোচ্যসূচি এখানে প্রদর্শিত হবে" else "অনুসন্ধান বা ফিল্টার পরিবর্তন করে পুনরায় চেষ্টা করুন",
                            hasActiveFilters = searchQuery.isNotBlank() || selectedMeetingFilter != MeetingFilter.ALL,
                            onResetFilters = {
                                searchQuery = ""
                                selectedMeetingFilter = MeetingFilter.ALL
                            }
                        )
                    }
                } else {
                    items(filteredMeetings, key = { it.id }) { meeting ->
                        MeetingCardItem(
                            meeting = meeting,
                            canManage = canSchedule,
                            onToggleStatus = {
                                viewModel.toggleMeetingStatus(meeting)
                                MosqueToastController.info(
                                    if (meeting.status == "completed") "সভা আসন্ন হিসেবে চিহ্নিত হয়েছে।" else "সভা সম্পন্ন হিসেবে চিহ্নিত হয়েছে।"
                                )
                            },
                            onEdit = { editingMeeting = meeting },
                            onEditResolution = { resolutionTargetMeeting = meeting },
                            onShare = { shareMeetingText(context, mosqueName, meeting) },
                            onDeleteRequest = { pendingDeleteMeeting = meeting }
                        )
                    }
                }
            }
        }
    }

    // Edit Notice Dialog
    editingNotice?.let { target ->
        PublishNoticeDialog(
            editingNotice = target,
            aiNoticeState = aiNoticeState,
            onGenerateAiDraft = { instruction -> viewModel.draftNoticeWithAi(instruction) },
            onClearAiDraft = { viewModel.clearAiNoticeState() },
            onDismiss = { editingNotice = null },
            onConfirm = { title, content, priority, isPinned ->
                viewModel.updateNotice(
                    notice = target,
                    title = title,
                    content = content,
                    priority = priority,
                    isPinned = isPinned
                )
                MosqueToastController.success("নোটিশ সফলভাবে হালনাগাদ হয়েছে।", "নোটিশ হালনাগাদ")
            }
        )
    }

    // Delete Notice Confirmation Dialog
    pendingDeleteNotice?.let { target ->
        MosqueConfirmDialog(
            title = "নোটিশ মুছে ফেলবেন?",
            message = "\"${target.title}\" নোটিশটি স্থায়ীভাবে মুছে ফেলা হবে। আপনি কি নিশ্চিত?",
            confirmText = "হ্যাঁ, মুছে ফেলুন",
            icon = Icons.Default.DeleteForever,
            isDestructive = true,
            confirmTag = "confirm_delete_notice_button",
            onDismiss = { pendingDeleteNotice = null },
            onConfirm = {
                viewModel.deleteNotice(target)
                pendingDeleteNotice = null
                MosqueToastController.warning("নোটিশটি মুছে ফেলা হয়েছে।", "নোটিশ মোছা হয়েছে")
            }
        )
    }

    // Edit Meeting Dialog
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
                MosqueToastController.success("সভার তথ্য সফলভাবে হালনাগাদ হয়েছে।", "সভা হালনাগাদ")
            }
        )
    }

    // Meeting Resolution / Minutes Dialog
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
                MosqueToastController.success("সভার কার্যবিবরণী সংরক্ষিত হয়েছে।", "কার্যবিবরণী সংরক্ষিত")
            }
        )
    }

    // Delete Meeting Confirmation Dialog
    pendingDeleteMeeting?.let { target ->
        MosqueConfirmDialog(
            title = "সভা মুছে ফেলবেন?",
            message = "\"${target.title}\" সভাটি তালিকা থেকে মুছে ফেলা হবে। আপনি কি নিশ্চিত?",
            confirmText = "হ্যাঁ, মুছে ফেলুন",
            icon = Icons.Default.DeleteForever,
            isDestructive = true,
            confirmTag = "confirm_delete_meeting_button",
            onDismiss = { pendingDeleteMeeting = null },
            onConfirm = {
                viewModel.deleteMeeting(target)
                pendingDeleteMeeting = null
                MosqueToastController.warning("সভাটি তালিকা থেকে মুছে ফেলা হয়েছে।", "সভা মোছা হয়েছে")
            }
        )
    }
}

@Composable
private fun NoticeTabButton(
    modifier: Modifier = Modifier,
    selected: Boolean,
    title: String,
    countText: String,
    icon: ImageVector,
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
                    color = MosqueGoldLight,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun NoticeHeaderSummaryPill(
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
fun NoticeCardItem(
    notice: NoticeEntity,
    canManage: Boolean,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    val priorityColor = when (notice.priority) {
        "urgent" -> MosqueUrgentRed
        "important" -> MosqueWarningAmber
        else -> MosqueEmeraldPrimary
    }
    val priorityLabel = when (notice.priority) {
        "urgent" -> "জরুরি"
        "important" -> "গুরুত্বপূর্ণ"
        else -> "সাধারণ"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notice_card_${notice.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            width = 1.dp,
            color = when {
                notice.priority == "urgent" -> MosqueUrgentRed.copy(alpha = 0.4f)
                notice.isPinned -> MosqueCardBorderGold
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left Status Strip
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(priorityColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Priority, Pinned & Actions Row
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
                                .clip(RoundedCornerShape(6.dp))
                                .background(priorityColor.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = priorityLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = priorityColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (notice.isPinned) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MosqueGoldLight)
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = null,
                                    tint = MosqueGoldDeep,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "পিন করা",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MosqueEmeraldDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier.testTag("share_notice_button_${notice.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "নোটিশ শেয়ার করুন",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        if (canManage) {
                            IconButton(
                                onClick = onTogglePin,
                                modifier = Modifier.testTag("pin_notice_button_${notice.id}")
                            ) {
                                Icon(
                                    imageVector = if (notice.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = if (notice.isPinned) "আনপিন করুন" else "পিন করুন",
                                    tint = if (notice.isPinned) MosqueGoldDeep else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            IconButton(
                                onClick = onEdit,
                                modifier = Modifier.testTag("edit_notice_button_${notice.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "নোটিশ সম্পাদনা",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            IconButton(
                                onClick = onDeleteRequest,
                                modifier = Modifier.testTag("delete_notice_button_${notice.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "নোটিশ মুছুন",
                                    tint = MosqueUrgentRed.copy(alpha = 0.8f),
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = notice.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "প্রকাশ: ${toBengaliDigits(notice.publishDate)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = "প্রচারে: ${notice.createdBy}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MeetingCardItem(
    meeting: MeetingEntity,
    canManage: Boolean,
    canEditResolution: Boolean = canManage,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit,
    onEditResolution: () -> Unit,
    onShare: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    val isCompleted = meeting.status == "completed"
    val statusColor = if (isCompleted) MosqueSuccessGreen else MosqueGoldDeep

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meeting_card_${meeting.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isCompleted) MosqueSuccessGreen.copy(alpha = 0.35f) else MosqueCardBorderGold.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isCompleted) MosqueSuccessGreen.copy(alpha = 0.12f)
                                else MosqueGold.copy(alpha = 0.18f)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isCompleted) "সম্পন্ন" else "আসন্ন সভা",
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier.testTag("share_meeting_button_${meeting.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "সভার বিবরণ শেয়ার করুন",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        if (canManage) {
                            IconButton(
                                onClick = onEdit,
                                modifier = Modifier.testTag("edit_meeting_button_${meeting.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "সভা সম্পাদনা",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            IconButton(
                                onClick = onDeleteRequest,
                                modifier = Modifier.testTag("delete_meeting_button_${meeting.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "সভা মুছুন",
                                    tint = MosqueUrgentRed.copy(alpha = 0.8f),
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = meeting.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = toBengaliDigits(meeting.meetingDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MosqueGoldDeep,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "স্থান: ${meeting.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (meeting.agenda.isNotBlank()) {
                    val isDarkMeeting = LocalIsDarkTheme.current
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = if (isDarkMeeting) MosqueNightSurfaceVariant else MosqueBgLight,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "আলোচ্যসূচি (এজেন্ডা):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = meeting.agenda,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                if (meeting.resolution.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MosqueEmeraldPrimary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.8.dp, MosqueEmeraldPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FactCheck,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "গৃহীত সিদ্ধান্ত / কার্যবিবরণী:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = meeting.resolution,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                if (canManage || canEditResolution) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MosqueCardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (canManage) {
                            OutlinedButton(
                                onClick = onToggleStatus,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.6f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("toggle_meeting_status_${meeting.id}")
                            ) {
                                Icon(
                                    imageVector = if (isCompleted) Icons.Default.Replay else Icons.Default.CheckCircleOutline,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isCompleted) "পুনরায় আসন্ন" else "সম্পন্ন করুন",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (canEditResolution) {
                            Button(
                                onClick = onEditResolution,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MosqueEmeraldPrimary,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("meeting_resolution_button_${meeting.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NoteAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (meeting.resolution.isBlank()) "সিদ্ধান্ত লিখুন" else "সিদ্ধান্ত সম্পাদনা",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyNoticesMeetingsState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    hasActiveFilters: Boolean,
    onResetFilters: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val borderColor = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, borderColor)
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
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (hasActiveFilters) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onResetFilters,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("reset_notices_filters_button")
                ) {
                    Text("ফিল্টার মুছুন", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun shareNoticeText(context: Context, mosqueName: String, notice: NoticeEntity) {
    val priorityBn = when (notice.priority) {
        "urgent" -> "জরুরি নোটিশ"
        "important" -> "গুরুত্বপূর্ণ নোটিশ"
        else -> "মসজিদ নোটিশ"
    }
    val text = buildString {
        appendLine("[$priorityBn] $mosqueName")
        appendLine()
        appendLine("*${notice.title}*")
        appendLine()
        appendLine(notice.content)
        appendLine()
        appendLine("প্রকাশ: ${toBengaliDigits(notice.publishDate)}")
        append("প্রচারে: ${notice.createdBy}")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "$mosqueName: ${notice.title}")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, "নোটিশ শেয়ার করুন"))
    }
}

fun shareMeetingText(context: Context, mosqueName: String, meeting: MeetingEntity) {
    val statusBn = if (meeting.status == "completed") "সম্পন্ন সভা" else "আসন্ন কমিটি সভা"
    val text = buildString {
        appendLine("[$statusBn] $mosqueName")
        appendLine()
        appendLine("*${meeting.title}*")
        appendLine("তারিখ ও সময়: ${toBengaliDigits(meeting.meetingDate)}")
        appendLine("স্থান: ${meeting.location}")
        if (meeting.agenda.isNotBlank()) {
            appendLine()
            appendLine("আলোচ্যসূচি:")
            appendLine(meeting.agenda)
        }
        if (meeting.resolution.isNotBlank()) {
            appendLine()
            appendLine("গৃহীত সিদ্ধান্ত / কার্যবিবরণী:")
            appendLine(meeting.resolution)
        }
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "$mosqueName: ${meeting.title}")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, "সভার বিবরণ শেয়ার করুন"))
    }
}
