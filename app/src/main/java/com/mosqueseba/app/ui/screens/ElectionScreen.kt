package com.mosqueseba.app.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mosqueseba.app.data.local.entity.ElectionCandidateEntity
import com.mosqueseba.app.data.local.entity.ElectionScheduleEntity
import com.mosqueseba.app.data.local.entity.MemberEntity
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.*
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel

private enum class ElectionTab(val titleBn: String, val icon: ImageVector) {
    SCHEDULE("তফসিল ও পর্যায়", Icons.Default.EventNote),
    CANDIDATES("প্রার্থী ও মনোনয়ন", Icons.Default.HowToVote),
    VOTERS("ভোটার তালিকা", Icons.Default.People),
    RESULTS("গেজেট ও ফলাফল", Icons.Default.WorkspacePremium)
}

@Composable
fun ElectionScreen(
    viewModel: MosqueViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(ElectionTab.SCHEDULE) }
    val currentRole by viewModel.currentUserRole.collectAsState()
    val mosque by viewModel.mosque.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    val allElections by viewModel.allElections.collectAsState()
    val activeElection by viewModel.activeElection.collectAsState()
    val allCandidates by viewModel.allCandidates.collectAsState()

    // Dialog States
    var showAnnounceScheduleDialog by remember { mutableStateOf(false) }
    var showAddCandidateDialog by remember { mutableStateOf(false) }
    var targetCandidateForVote by remember { mutableStateOf<ElectionCandidateEntity?>(null) }
    var candidateToDelete by remember { mutableStateOf<ElectionCandidateEntity?>(null) }
    var showActivateCommitteeDialog by remember { mutableStateOf(false) }

    val currentElection = activeElection ?: allElections.firstOrNull()

    val canManageElection = currentRole == UserRole.PRESIDENT ||
            currentRole == UserRole.GENERAL_SECRETARY ||
            currentRole == UserRole.ADVISORY_BOARD

    val eligibleVoters = remember(allMembers) {
        allMembers.filter { it.status == "active" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sticky Islamic Header
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
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = MosqueGoldLight
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "কমিটি নির্বাচন ও পুনর্গঠন",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${mosque?.nameBn ?: "মসজিদ সেবা"} • শরিয়াহ ও গঠনতন্ত্র অনুযায়ী",
                            style = MaterialTheme.typography.labelSmall,
                            color = MosqueGoldLight.copy(alpha = 0.85f)
                        )
                    }

                    if (canManageElection && currentElection?.status != "completed") {
                        FilledTonalButton(
                            onClick = { showAddCandidateDialog = true },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MosqueGold,
                                contentColor = MosqueEmeraldDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("মনোনয়ন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Islamic Gold Accent Strip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(IslamicGoldTrimGradient)
                )
            }
        }

        // Segmented Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 12.dp,
            divider = { HorizontalDivider(color = MosqueCardBorderSubtle) }
        ) {
            ElectionTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MosqueTextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.titleBn,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                )
            }
        }

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                ElectionTab.SCHEDULE -> ElectionScheduleTabView(
                    election = currentElection,
                    mosque = mosque,
                    canManage = canManageElection,
                    onAnnounceClick = { showAnnounceScheduleDialog = true },
                    onAdvancePhase = { nextStatus ->
                        currentElection?.let {
                            viewModel.updateElectionStatus(it, nextStatus)
                            MosqueToastController.success("নির্বাচনী পর্যায় সফলভাবে হালনাগাদ হয়েছে।", "পর্যায় পরিবর্তন")
                        }
                    }
                )
                ElectionTab.CANDIDATES -> ElectionCandidatesTabView(
                    candidates = allCandidates,
                    canManage = canManageElection,
                    onAddCandidate = { showAddCandidateDialog = true },
                    onVoteEntry = { targetCandidateForVote = it },
                    onDelete = { candidateToDelete = it }
                )
                ElectionTab.VOTERS -> ElectionVotersTabView(
                    members = eligibleVoters,
                    mosque = mosque
                )
                ElectionTab.RESULTS -> ElectionResultsTabView(
                    election = currentElection,
                    candidates = allCandidates,
                    canManage = canManageElection,
                    onActivateCommittee = { showActivateCommitteeDialog = true }
                )
            }
        }
    }

    // --- Dialogs ---

    // 1. Announce / Edit Schedule Dialog
    if (showAnnounceScheduleDialog) {
        AnnounceElectionDialog(
            existingElection = currentElection,
            onDismiss = { showAnnounceScheduleDialog = false },
            onConfirm = { termTitle, startYear, endYear, commissioner, mobile, schDate, nomDate, voteDate, elecType, notes ->
                viewModel.createElection(
                    termTitle = termTitle,
                    termStartYear = startYear,
                    termEndYear = endYear,
                    chiefElectionCommissioner = commissioner,
                    commissionerMobile = mobile,
                    scheduleDate = schDate,
                    nominationDeadline = nomDate,
                    votingDate = voteDate,
                    electionType = elecType,
                    gazetteNotes = notes
                )
                MosqueToastController.success("নতুন নির্বাচনী তফসিল সফলভাবে ঘোষিত হয়েছে।", "তফসিল ঘোষণা")
                showAnnounceScheduleDialog = false
            }
        )
    }

    // 2. Add Candidate Nomination Dialog
    if (showAddCandidateDialog) {
        AddCandidateNominationDialog(
            allMembers = allMembers,
            onDismiss = { showAddCandidateDialog = false },
            onConfirm = { roleCode, roleTitle, name, mobile, code, proposer, seconder, panel ->
                val electionId = currentElection?.id ?: "elec-${System.currentTimeMillis()}"
                viewModel.addElectionCandidate(
                    electionId = electionId,
                    targetRoleCode = roleCode,
                    targetRoleTitleBn = roleTitle,
                    candidateNameBn = name,
                    candidateMobile = mobile,
                    memberCode = code,
                    proposerNameBn = proposer,
                    seconderNameBn = seconder,
                    symbolOrPanel = panel
                )
                MosqueToastController.success("$name-এর মনোনয়ন দাখিল সম্পন্ন হয়েছে।", "মনোনয়ন গৃহীত")
                showAddCandidateDialog = false
            }
        )
    }

    // 3. Vote Entry / Result Mark Dialog
    targetCandidateForVote?.let { cand ->
        CandidateVoteDialog(
            candidate = cand,
            onDismiss = { targetCandidateForVote = null },
            onConfirm = { votes, isElected ->
                viewModel.markCandidateElected(cand, isElected, votes)
                MosqueToastController.success(
                    "${cand.candidateNameBn}-এর ফলাফল আপডেট হয়েছে (" + if (isElected) "নির্বাচিত" else "ভোট: $votes" + ")।",
                    "ফলাফল হালনাগাদ"
                )
                targetCandidateForVote = null
            }
        )
    }

    // 4. Delete Candidate Confirm Dialog
    candidateToDelete?.let { cand ->
        MosqueConfirmDialog(
            title = "মনোনয়ন প্রত্যাহার/বাতিল?",
            message = "\"${cand.candidateNameBn}\" (${cand.targetRoleTitleBn}) প্রার্থীর মনোনয়ন তালিকা থেকে মুছে ফেলা হবে।",
            confirmText = "হ্যাঁ, বাতিল করুন",
            icon = Icons.Default.DeleteOutline,
            isDestructive = true,
            onDismiss = { candidateToDelete = null },
            onConfirm = {
                viewModel.deleteCandidate(cand)
                MosqueToastController.warning("${cand.candidateNameBn}-এর মনোনয়ন অপসারিত হয়েছে।", "মনোনয়ন বাতিল")
                candidateToDelete = null
            }
        )
    }

    // 5. Activate New Committee Confirmation Dialog
    if (showActivateCommitteeDialog && currentElection != null) {
        MosqueConfirmDialog(
            title = "নতুন নির্বাচিত কমিটি কার্যকর করবেন?",
            message = "নতুন নির্বাচিত পরিষদের মেয়াদকাল (${toBengaliDigits(currentElection.termStartYear.toString())}–${toBengaliDigits(currentElection.termEndYear.toString())}) বর্তমান সক্রিয় কমিটি হিসেবে অ্যাপে নির্ধারিত হবে এবং গেজেট চূড়ান্তভাবে বাস্তবায়িত হবে।",
            confirmText = "হ্যাঁ, কার্যকর করুন",
            icon = Icons.Default.Verified,
            isDestructive = false,
            onDismiss = { showActivateCommitteeDialog = false },
            onConfirm = {
                viewModel.activateNewCommittee(currentElection)
                MosqueToastController.success("আলহামদুলিল্লাহ! নতুন নির্বাচিত কমিটি সক্রিয় ও কার্যকর হয়েছে।", "কমিটি কার্যকর")
                showActivateCommitteeDialog = false
            }
        )
    }
}

// ==========================================
// Tab 1: Election Schedule & Roadmap View
// ==========================================
@Composable
private fun ElectionScheduleTabView(
    election: ElectionScheduleEntity?,
    mosque: com.mosqueseba.app.data.local.entity.MosqueEntity?,
    canManage: Boolean,
    onAnnounceClick: () -> Unit,
    onAdvancePhase: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Active Term & Next Election Banner
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MosqueCardBorderGold),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MosqueEmeraldPrimary.copy(alpha = 0.12f))
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MosqueEmeraldPrimary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "বর্তমান সক্রিয় পরিষদ মেয়াদ",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MosqueTextSecondary
                                )
                                Text(
                                    text = "${toBengaliDigits(mosque?.committeeStartYear?.toString() ?: "২০২৪")}–${toBengaliDigits(mosque?.committeeEndYear?.toString() ?: "২০২৬")} মেয়াদকাল",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueEmeraldPrimary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MosqueSuccessGreen.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, MosqueSuccessGreen.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "বহাল আছে",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MosqueSuccessGreen,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MosqueCardBorderSubtle)
                    Spacer(modifier = Modifier.height(14.dp))

                    if (election != null) {
                        Text(
                            text = election.termTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "প্রধান নির্বাচন কমিশনার: ${election.chiefElectionCommissioner}" +
                                    if (election.commissionerMobile.isNotBlank()) " (${election.commissionerMobile})" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MosqueTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "নির্বাচনের ধরন: ${election.electionType}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MosqueGoldDeep
                        )
                    } else {
                        Text(
                            text = "পরবর্তী মেয়াদের কোনো নির্বাচনী তফসিল এখনো ঘোষণা করা হয়নি।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MosqueTextSecondary
                        )
                        if (canManage) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onAnnounceClick,
                                colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("নতুন তফসিল ঘোষণা করুন", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Election Roadmap & Phases
        if (election != null) {
            item {
                RubElHizbDivider(title = "নির্বাচনী রোডম্যাপ ও পর্যায়")
            }

            item {
                val status = election.status
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    RoadmapPhaseCard(
                        stepNumber = 1,
                        title = "১. নির্বাচনী তফসিল ঘোষণা",
                        dateStr = election.scheduleDate,
                        description = "নির্বাচন কমিশন গঠন, আচরণবিধি ও তফসিল প্রকাশ।",
                        isActive = status == "upcoming" || status == "nomination_open",
                        isPassed = status != "upcoming"
                    )

                    RoadmapPhaseCard(
                        stepNumber = 2,
                        title = "২. প্রার্থী মনোনয়ন দাখিল ও যাচাই",
                        dateStr = "শেষ তারিখ: ${election.nominationDeadline}",
                        description = "১৬টি কার্যনির্বাহী পদের জন্য আগ্রহী প্রার্থীদের ফরম দাখিল।",
                        isActive = status == "nomination_open",
                        isPassed = status == "voting" || status == "completed"
                    )

                    RoadmapPhaseCard(
                        stepNumber = 3,
                        title = "৩. ভোটগ্রহণ বা সর্বসম্মত সিলেকশন",
                        dateStr = "তারিখ: ${election.votingDate}",
                        description = "সাধারণ সদস্যদের প্রত্যক্ষ ভোট বা সর্বসম্মত ঐক্যমত্য গ্রহণ।",
                        isActive = status == "voting",
                        isPassed = status == "completed"
                    )

                    RoadmapPhaseCard(
                        stepNumber = 4,
                        title = "৪. চূড়ান্ত গেজেট ও ফলাফল কার্যকর",
                        dateStr = "${toBengaliDigits(election.termStartYear.toString())}–${toBengaliDigits(election.termEndYear.toString())} মেয়াদ",
                        description = "নতুন কমিটির কাছে আনুষ্ঠানিক দায়িত্ব হস্তান্তর ও গেজেট প্রকাশ।",
                        isActive = status == "completed",
                        isPassed = status == "completed"
                    )
                }
            }

            // Phase Action Controls for Admin
            if (canManage && election.status != "completed") {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MosqueCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "নির্বাচনী পর্যায় পরিবর্তন",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (election.status == "upcoming" || election.status == "nomination_open") {
                                    Button(
                                        onClick = { onAdvancePhase("voting") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                    ) {
                                        Text("ভোটগ্রহণ পর্যায় শুরু", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else if (election.status == "voting") {
                                    Button(
                                        onClick = { onAdvancePhase("completed") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueGoldDeep)
                                    ) {
                                        Text("গেজেট ও ফলাফল সম্পন্ন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoadmapPhaseCard(
    stepNumber: Int,
    title: String,
    dateStr: String,
    description: String,
    isActive: Boolean,
    isPassed: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) MosqueEmeraldSoft.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (isActive) MosqueEmeraldPrimary else MosqueCardBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isPassed -> MosqueSuccessGreen
                            isActive -> MosqueGold
                            else -> MosqueCardBorder
                        }
                    )
            ) {
                if (isPassed) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text(
                        text = toBengaliDigits(stepNumber.toString()),
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) MosqueEmeraldDark else MosqueTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) MosqueEmeraldPrimary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MosqueGoldDeep,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MosqueTextSecondary
                )
            }
        }
    }
}

// ==========================================
// Tab 2: Election Candidates & Nominations
// ==========================================
@Composable
private fun ElectionCandidatesTabView(
    candidates: List<ElectionCandidateEntity>,
    canManage: Boolean,
    onAddCandidate: () -> Unit,
    onVoteEntry: (ElectionCandidateEntity) -> Unit,
    onDelete: (ElectionCandidateEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "দাখিলকৃত প্রার্থী মনোনয়ন (${toBengaliDigits(candidates.size.toString())} জন)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldPrimary
                )

                if (canManage) {
                    FilledTonalButton(
                        onClick = onAddCandidate,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MosqueEmeraldPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নতুন প্রার্থী", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MosqueCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.HowToVote,
                            contentDescription = null,
                            tint = MosqueTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "এখনো কোনো প্রার্থীর মনোনয়ন দাখিল হয়নি",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MosqueTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "উপরে 'নতুন প্রার্থী' বাটনে ট্যাপ করে ১৬টি কার্যনির্বাহী পদের মনোনয়ন যুক্ত করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MosqueTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Group by target role title
            val grouped = candidates.groupBy { it.targetRoleTitleBn }
            grouped.forEach { (roleTitle, candList) ->
                item {
                    Text(
                        text = "পদবি: $roleTitle (${toBengaliDigits(candList.size.toString())} জন প্রার্থী)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MosqueGoldDeep,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                items(candList, key = { it.id }) { cand ->
                    CandidateCard(
                        candidate = cand,
                        canManage = canManage,
                        onVoteEntry = { onVoteEntry(cand) },
                        onDelete = { onDelete(cand) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CandidateCard(
    candidate: ElectionCandidateEntity,
    canManage: Boolean,
    onVoteEntry: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (candidate.isElected) MosqueGold else MosqueCardBorder
        ),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (candidate.isElected) MosqueGold.copy(alpha = 0.2f)
                                else MosqueEmeraldSoft
                            )
                    ) {
                        Text(
                            text = candidate.candidateNameBn.take(1),
                            fontWeight = FontWeight.Bold,
                            color = if (candidate.isElected) MosqueGoldDeep else MosqueEmeraldPrimary,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = candidate.candidateNameBn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "প্যানেল/প্রতীক: ${candidate.symbolOrPanel}" +
                                    if (candidate.memberCode.isNotBlank()) " • কোড: ${candidate.memberCode}" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MosqueTextSecondary
                        )
                    }
                }

                if (candidate.isElected) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MosqueGold.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, MosqueGold)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MosqueGoldDeep, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "নির্বাচিত",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MosqueGoldDeep
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MosqueCardBorderSubtle)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "প্রস্তাবক: ${candidate.proposerNameBn.ifBlank { "স্বতঃস্ফূর্ত" }} • সমর্থক: ${candidate.seconderNameBn.ifBlank { "সাধারণ সদস্যবৃন্দ" }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MosqueTextSecondary
                    )
                    if (candidate.votesReceived > 0) {
                        Text(
                            text = "প্রাপ্ত সমর্থন/ভোট: ${toBengaliDigits(candidate.votesReceived.toString())}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MosqueEmeraldPrimary
                        )
                    }
                }

                if (canManage) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onVoteEntry,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "ভোট/ফলাফল এন্ট্রি", tint = MosqueEmeraldPrimary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "বাতিল", tint = MosqueUrgentRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// Tab 3: Eligible Voters List View
// ==========================================
@Composable
private fun ElectionVotersTabView(
    members: List<MemberEntity>,
    mosque: com.mosqueseba.app.data.local.entity.MosqueEntity?
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val filteredVoters = remember(members, searchQuery) {
        if (searchQuery.isBlank()) members
        else members.filter {
            it.nameBn.contains(searchQuery, ignoreCase = true) ||
                    it.memberCode.contains(searchQuery, ignoreCase = true) ||
                    it.mobile.contains(searchQuery)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // Voter Stat Header
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MosqueEmeraldSoft.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MosqueEmeraldPrimary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "মোট বৈধ ভোটাধিকার সদস্য",
                            style = MaterialTheme.typography.labelMedium,
                            color = MosqueTextSecondary
                        )
                        Text(
                            text = "${toBengaliDigits(members.size.toString())} জন মুসল্লি",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MosqueEmeraldPrimary
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            val shareText = buildString {
                                appendLine("📋 ${mosque?.nameBn ?: "মসজিদ সেবা"} - বৈধ ভোটার তালিকা")
                                appendLine("মোট ভোটার সংখ্যা: ${toBengaliDigits(members.size.toString())} জন")
                                appendLine("-----------------------------")
                                members.forEachIndexed { i, m ->
                                    appendLine("${toBengaliDigits((i + 1).toString())}. ${m.nameBn} (${m.memberCode}) - ${m.mobile}")
                                }
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "ভোটার তালিকা শেয়ার করুন"))
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MosqueEmeraldPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("শেয়ার", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("নাম, কোড বা মোবাইল দিয়ে খুঁজুন...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MosqueTextSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        items(filteredVoters, key = { it.id }) { voter ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MosqueCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MosqueEmeraldPrimary.copy(alpha = 0.12f))
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MosqueEmeraldPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = voter.nameBn,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "কোড: ${voter.memberCode} • মোবাইল: ${voter.mobile}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MosqueTextSecondary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MosqueSuccessGreen.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "ভোটার",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MosqueSuccessGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// Tab 4: Gazette & Results View
// ==========================================
@Composable
private fun ElectionResultsTabView(
    election: ElectionScheduleEntity?,
    candidates: List<ElectionCandidateEntity>,
    canManage: Boolean,
    onActivateCommittee: () -> Unit
) {
    val electedList = remember(candidates) {
        candidates.filter { it.isElected }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Official Gazette Certificate Frame
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(2.dp, MosqueGold),
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MosqueGoldDeep,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "মসজিদ পরিচালনা পরিষদ নির্বাচন গেজেট",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MosqueEmeraldPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = election?.termTitle ?: "২০২৬–২০২৮ মেয়াদের পূর্ণাঙ্গ ফলাফল",
                        style = MaterialTheme.typography.bodySmall,
                        color = MosqueTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MosqueCardBorderGold)
                    Spacer(modifier = Modifier.height(14.dp))

                    if (electedList.isEmpty()) {
                        Text(
                            text = "এখনো কোনো প্রার্থীকে নির্বাচিত হিসেবে ঘোষণা করা হয়নি। 'প্রার্থী ও মনোনয়ন' ট্যাব থেকে ফলাফল এন্ট্রি করুন।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MosqueTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        electedList.forEachIndexed { idx, cand ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${toBengaliDigits((idx + 1).toString())}. ${cand.targetRoleTitleBn}:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueEmeraldPrimary
                                )
                                Text(
                                    text = cand.candidateNameBn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            HorizontalDivider(color = MosqueCardBorderSubtle, thickness = 0.5.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Signature Block
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "স্বাক্ষর / সীলমোহর",
                                fontSize = 11.sp,
                                color = MosqueTextMuted
                            )
                            Text(
                                text = election?.chiefElectionCommissioner ?: "প্রধান নির্বাচন কমিশনার",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "অনুমোদনে",
                                fontSize = 11.sp,
                                color = MosqueTextMuted
                            )
                            Text(
                                text = "উপদেষ্টামণ্ডলী ও সর্বসাধারণ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldPrimary
                            )
                        }
                    }
                }
            }
        }

        // Activate Committee Action Button
        if (canManage && electedList.isNotEmpty()) {
            item {
                Button(
                    onClick = onActivateCommittee,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MosqueGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "নতুন নির্বাচিত কমিটি কার্যকর ও মেয়াদ হালনাগাদ করুন",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ==========================================
// Dialog: Announce Election Schedule
// ==========================================
@Composable
private fun AnnounceElectionDialog(
    existingElection: ElectionScheduleEntity?,
    onDismiss: () -> Unit,
    onConfirm: (
        termTitle: String,
        startYear: Int,
        endYear: Int,
        commissioner: String,
        mobile: String,
        scheduleDate: String,
        nominationDate: String,
        votingDate: String,
        electionType: String,
        notes: String
    ) -> Unit
) {
    var termTitle by remember { mutableStateOf(existingElection?.termTitle ?: "২০২৬–২০২৮ মেয়াদের পরিচালনা পরিষদ নির্বাচন") }
    var startYear by remember { mutableStateOf((existingElection?.termStartYear ?: 2026).toString()) }
    var endYear by remember { mutableStateOf((existingElection?.termEndYear ?: 2028).toString()) }
    var commissioner by remember { mutableStateOf(existingElection?.chiefElectionCommissioner ?: "") }
    var commissionerMobile by remember { mutableStateOf(existingElection?.commissionerMobile ?: "") }
    var scheduleDate by remember { mutableStateOf(existingElection?.scheduleDate ?: "২০২৫-১২-০১") }
    var nominationDeadline by remember { mutableStateOf(existingElection?.nominationDeadline ?: "২০২৫-১২-১৫") }
    var votingDate by remember { mutableStateOf(existingElection?.votingDate ?: "২০২৫-১২-২৮") }
    var electionType by remember { mutableStateOf(existingElection?.electionType ?: "সর্বসম্মত ও ব্যালট নির্বাচন") }
    var notes by remember { mutableStateOf(existingElection?.gazetteNotes ?: "মসজিদের গঠনতন্ত্র ও শরিয়াহ নির্দেশিত পদ্ধতিতে নির্বাচন কার্যক্রম পরিচালিত হচ্ছে।") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "নির্বাচনী তফসিল ঘোষণা ও শিডিউল",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = termTitle,
                    onValueChange = { termTitle = it },
                    label = { Text("নির্বাচনী মেয়াদের শিরোনাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startYear,
                        onValueChange = { startYear = it },
                        label = { Text("শুরু সন") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endYear,
                        onValueChange = { endYear = it },
                        label = { Text("সমাপ্তি সন") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = commissioner,
                    onValueChange = { commissioner = it },
                    label = { Text("প্রধান নির্বাচন কমিশনারের নাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = commissionerMobile,
                    onValueChange = { commissionerMobile = it },
                    label = { Text("কমিশনারের মোবাইল নম্বর") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nominationDeadline,
                    onValueChange = { nominationDeadline = it },
                    label = { Text("মনোনয়ন দাখিলের শেষ তারিখ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = votingDate,
                    onValueChange = { votingDate = it },
                    label = { Text("ভোটগ্রহণ বা ফলাফল ঘোষণার তারিখ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (termTitle.isNotBlank() && commissioner.isNotBlank()) {
                                onConfirm(
                                    termTitle,
                                    startYear.toIntOrNull() ?: 2026,
                                    endYear.toIntOrNull() ?: 2028,
                                    commissioner,
                                    commissionerMobile,
                                    scheduleDate,
                                    nominationDeadline,
                                    votingDate,
                                    electionType,
                                    notes
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Text("তফসিল সংরক্ষণ")
                    }
                }
            }
        }
    }
}

// ==========================================
// Dialog: Add Candidate Nomination
// ==========================================
@Composable
private fun AddCandidateNominationDialog(
    allMembers: List<MemberEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        roleCode: String,
        roleTitle: String,
        candidateName: String,
        mobile: String,
        memberCode: String,
        proposer: String,
        seconder: String,
        panel: String
    ) -> Unit
) {
    val roles = listOf(
        "president" to "সভাপতি",
        "vice_president" to "সহ-সভাপতি",
        "general_secretary" to "সাধারণ সম্পাদক",
        "assistant_secretary" to "সহ-সাধারণ সম্পাদক",
        "treasurer" to "কোষাধ্যক্ষ",
        "assistant_treasurer" to "সহ-কোষাধ্যক্ষ",
        "organizing_secretary" to "সাংগঠনিক সম্পাদক",
        "publicity" to "প্রচার ও প্রকাশনা সম্পাদক",
        "office_secretary" to "দপ্তর সম্পাদক",
        "welfare_edu" to "সমাজকল্যাণ ও শিক্ষা সম্পাদক",
        "executive_member" to "কার্যনির্বাহী সদস্য"
    )

    var selectedRoleIndex by remember { mutableIntStateOf(0) }
    var candidateName by remember { mutableStateOf("") }
    var candidateMobile by remember { mutableStateOf("") }
    var memberCode by remember { mutableStateOf("") }
    var proposer by remember { mutableStateOf("") }
    var seconder by remember { mutableStateOf("") }
    var panel by remember { mutableStateOf("মসজিদ ঐক্য পরিষদ") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "প্রার্থী মনোনয়ন দাখিল ফরম",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text("কাঙ্ক্ষিত পদবি নির্বাচন করুন:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(roles.indices.toList()) { idx ->
                        val isSel = selectedRoleIndex == idx
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedRoleIndex = idx },
                            label = { Text(roles[idx].second, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = candidateName,
                    onValueChange = { candidateName = it },
                    label = { Text("প্রার্থীর নাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = candidateMobile,
                    onValueChange = { candidateMobile = it },
                    label = { Text("মোবাইল নম্বর") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = panel,
                    onValueChange = { panel = it },
                    label = { Text("প্যানেল বা প্রতীক") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = proposer,
                    onValueChange = { proposer = it },
                    label = { Text("প্রস্তাবকের নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = seconder,
                    onValueChange = { seconder = it },
                    label = { Text("সমর্থকের নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (candidateName.isNotBlank()) {
                                val role = roles[selectedRoleIndex]
                                onConfirm(
                                    role.first,
                                    role.second,
                                    candidateName.trim(),
                                    candidateMobile.trim(),
                                    memberCode.trim(),
                                    proposer.trim(),
                                    seconder.trim(),
                                    panel.trim()
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Text("দাখিল করুন")
                    }
                }
            }
        }
    }
}

// ==========================================
// Dialog: Candidate Vote / Result Mark
// ==========================================
@Composable
private fun CandidateVoteDialog(
    candidate: ElectionCandidateEntity,
    onDismiss: () -> Unit,
    onConfirm: (votes: Int, isElected: Boolean) -> Unit
) {
    var votesStr by remember { mutableStateOf(candidate.votesReceived.toString()) }
    var isElected by remember { mutableStateOf(candidate.isElected) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "ফলাফল ও ভোট নির্ধারণ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${candidate.candidateNameBn} (${candidate.targetRoleTitleBn})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MosqueTextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = votesStr,
                    onValueChange = { votesStr = it },
                    label = { Text("প্রাপ্ত ভোট সংখ্যা / সমর্থন") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isElected = !isElected }
                        .padding(4.dp)
                ) {
                    Checkbox(
                        checked = isElected,
                        onCheckedChange = { isElected = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "এই পদে চূড়ান্তভাবে 'নির্বাচিত' হিসেবে ঘোষণা করুন",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val votes = votesStr.toIntOrNull() ?: 0
                            onConfirm(votes, isElected)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Text("সংরক্ষণ")
                    }
                }
            }
        }
    }
}
