package com.mosqueseba.app.ui.screens

import androidx.compose.foundation.background
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
        // 1. Hero Card
        item {
            HeroCard(mosque = mosque)
        }

        // 2. Smart Board
        item {
            SmartBoardCard()
        }

        // 3. Prayer Schedule Card
        item {
            PrayerCard(
                onViewFullScheduleClick = { onNavigateToTab(4) }
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
                        "finance" -> onNavigateToTab(2)
                        "chanda" -> onOpenDialog("receive_chanda")
                        "pending" -> onNavigateToTab(2)
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
        item {
            UpcomingMeetingsCard(
                meetings = allMeetings,
                onScheduleClick = { onOpenDialog("schedule_meeting") }
            )
        }

        // 7. Recent Financial Activity
        item {
            RecentActivityCard(
                transactions = allTransactions.take(4),
                onViewAllClick = { onNavigateToTab(2) }
            )
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
