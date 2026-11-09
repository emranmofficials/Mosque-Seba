package com.mosqueseba.app.ui.screens

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.data.local.entity.MeetingEntity
import com.mosqueseba.app.data.local.entity.TransactionEntity
import com.mosqueseba.app.ui.components.*
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel

@Composable
fun DashboardScreen(
    viewModel: MosqueViewModel,
    onNavigateToTab: (Int) -> Unit,
    onOpenDialog: (String) -> Unit
) {
    val mosque by viewModel.mosque.collectAsState()
    val totalMembers by viewModel.memberCount.collectAsState()
    val netBalance by viewModel.netBalance.collectAsState()
    val pendingTransactions by viewModel.pendingTransactions.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allMeetings by viewModel.allMeetings.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MosqueBgLight),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 0. Islamic Hijri Date & Bismillah Sanctuary Banner
        item {
            HijriDateBanner(
                mosqueName = mosque?.nameBn ?: "বায়তুল জান্নাত জামে মসজিদ"
            )
        }

        // 0.1 Official Role Workspace Badge
        item {
            RoleWorkspaceHeader(currentRole = currentRole)
        }

        // 1. Hero Card
        item {
            HeroCard(mosque = mosque)
        }

        // 1.1 President Pending Voucher Approval Hotline
        if ((currentRole == com.mosqueseba.app.data.model.UserRole.PRESIDENT || currentRole == com.mosqueseba.app.data.model.UserRole.VICE_PRESIDENT) && pendingTransactions.isNotEmpty()) {
            item {
                PresidentPendingVouchersCard(
                    pendingTransactions = pendingTransactions,
                    onApprove = { viewModel.updateTransactionStatus(it, true) },
                    onReject = { viewModel.updateTransactionStatus(it, false) }
                )
            }
        }

        // 1.2 Musalli Devotional & Instant Digital Receipt Card
        if (currentRole == com.mosqueseba.app.data.model.UserRole.GENERAL_MEMBER) {
            item {
                MusalliDevotionalCard(
                    onViewReceipt = { onOpenDialog("view_receipt") },
                    onMakeDonation = { onOpenDialog("my_chanda") }
                )
            }
        }

        // 2. Smart Board
        item {
            SmartBoardCard()
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
                dueChanda = 3500.0,
                pendingItems = pendingTransactions.size,
                onKpiClick = { key ->
                    when (key) {
                        "members" -> onNavigateToTab(1)
                        "finance" -> onNavigateToTab(if (currentRole == com.mosqueseba.app.data.model.UserRole.TREASURER) 1 else 2)
                        "treasurer_chanda" -> onNavigateToTab(2)
                        "treasurer_payroll" -> onNavigateToTab(3)
                        "meetings" -> onNavigateToTab(if (currentRole == com.mosqueseba.app.data.model.UserRole.GENERAL_SECRETARY || currentRole == com.mosqueseba.app.data.model.UserRole.ORGANIZING_SECRETARY) 2 else 0)
                        "chanda" -> onOpenDialog(if (currentRole == com.mosqueseba.app.data.model.UserRole.TREASURER || currentRole == com.mosqueseba.app.data.model.UserRole.PRESIDENT) "receive_chanda" else "my_chanda")
                        "pending" -> onNavigateToTab(if (currentRole == com.mosqueseba.app.data.model.UserRole.TREASURER) 1 else 2)
                        "roster" -> onNavigateToTab(1)
                        "food" -> onNavigateToTab(2)
                        "madrasa" -> onNavigateToTab(3)
                        "staff_profile" -> onNavigateToTab(4)
                        "my_chanda" -> onNavigateToTab(1)
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

        // 6. Upcoming Meetings Card (Visible strictly to Secretariat and Executive Committee)
        if (currentRole in listOf(
                com.mosqueseba.app.data.model.UserRole.PRESIDENT,
                com.mosqueseba.app.data.model.UserRole.VICE_PRESIDENT,
                com.mosqueseba.app.data.model.UserRole.GENERAL_SECRETARY,
                com.mosqueseba.app.data.model.UserRole.ORGANIZING_SECRETARY,
                com.mosqueseba.app.data.model.UserRole.EXECUTIVE_MEMBER
            )
        ) {
            item {
                UpcomingMeetingsCard(
                    meetings = allMeetings,
                    onScheduleClick = { onOpenDialog("schedule_meeting") }
                )
            }
        }

        // 7. Recent Financial Activity (Visible strictly to Treasurer and Presidential Supervisors)
        if (currentRole in listOf(
                com.mosqueseba.app.data.model.UserRole.PRESIDENT,
                com.mosqueseba.app.data.model.UserRole.VICE_PRESIDENT,
                com.mosqueseba.app.data.model.UserRole.TREASURER
            )
        ) {
            item {
                RecentActivityCard(
                    transactions = allTransactions.take(4),
                    onViewAllClick = {
                        val financeTab = if (currentRole == com.mosqueseba.app.data.model.UserRole.TREASURER) 1 else 2
                        onNavigateToTab(financeTab)
                    }
                )
            }
        }

        // Bottom space padding
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun UpcomingMeetingsCard(
    meetings: List<MeetingEntity>,
    onScheduleClick: () -> Unit
) {
    MehrabArchCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White,
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
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MosqueEmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "আসন্ন কমিটি সভা ও এজেন্ডা",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MosqueEmeraldDark
                    )
                }

                TextButton(
                    onClick = onScheduleClick,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "+ নতুন সভা",
                        style = MaterialTheme.typography.labelSmall,
                        color = MosqueEmeraldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (meetings.isEmpty()) {
                Text(
                    text = "কোনো নির্ধারিত সভা নেই",
                    style = MaterialTheme.typography.bodySmall,
                    color = MosqueTextMuted
                )
            } else {
                meetings.take(2).forEach { meeting ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MosqueBgLight)
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
                                    color = MosqueTextPrimary,
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
                                    tint = MosqueTextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = meeting.meetingDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MosqueTextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "এজেন্ডা: ${meeting.agenda}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MosqueTextMuted,
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
    MehrabArchCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White,
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
                        tint = MosqueEmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "সাম্প্রতিক আর্থিক লেনদেন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MosqueEmeraldDark
                    )
                }

                TextButton(
                    onClick = onViewAllClick,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "সব দেখুন",
                        style = MaterialTheme.typography.labelSmall,
                        color = MosqueEmeraldPrimary,
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
                                color = MosqueTextPrimary
                            )
                            Text(
                                text = "${txn.txnDate} • ${txn.createdBy}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MosqueTextMuted,
                                maxLines = 1
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${if (txn.type == "INCOME") "+" else "-"}৳${String.format("%,.0f", txn.amount)}",
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
                    HorizontalDivider(color = MosqueCardBorder.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun PresidentPendingVouchersCard(
    pendingTransactions: List<TransactionEntity>,
    onApprove: (TransactionEntity) -> Unit,
    onReject: (TransactionEntity) -> Unit
) {
    MehrabArchCard(
        backgroundColor = Color.White,
        borderColor = MosqueUrgentRed.copy(alpha = 0.5f),
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
                            text = "নির্বাহী অনুমোদন ডেক্স (${pendingTransactions.size}টি ভাউচার)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MosqueEmeraldDark
                        )
                        Text(
                            text = "সভাপতির স্বাক্ষর ও ব্যয় ভাউচার নিষ্পত্তি",
                            style = MaterialTheme.typography.labelSmall,
                            color = MosqueTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            pendingTransactions.take(3).forEach { txn ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MosqueBgLight,
                    border = BorderStroke(1.dp, MosqueCardBorderSubtle),
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
                                color = MosqueTextDark
                            )
                            Text(
                                text = "দাবিকৃত: ৳${String.format("%,.0f", txn.amount)} • ${txn.createdBy}",
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
    MehrabArchCard(
        backgroundColor = Color.White,
        borderColor = MosqueCardBorderGold.copy(alpha = 0.6f),
        elevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(MosqueGoldSoft.copy(alpha = 0.5f), Color.White)
                    )
                )
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📖 আজকের হাদিস ও দ্বীনি অনুপ্রেরণা",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MosqueEmeraldDark
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "«مَنْ بَنَى مَسْجِدًا لِلَّهِ بَنَى اللَّهُ لَهُ فِي الْجَنَّةِ مِثْلَهُ»\n\"যে ব্যক্তি আল্লাহর সন্তুষ্টির উদ্দেশ্যে একটি মসজিদ নির্মাণ করে বা তাতে শরিক হয়, আল্লাহ তার জন্য জান্নাতে একটি প্রাসাদ নির্মাণ করেন।\" — (সহীহ বুখারী: ৪৫০)",
                fontSize = 12.sp,
                color = MosqueTextSecondary,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewReceipt,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MosqueEmeraldPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueEmeraldPrimary)
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ডিজিটাল রসিদ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onMakeDonation,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                ) {
                    Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("অনলাইনে চাঁদা", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

