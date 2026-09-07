package com.example.ui.screens

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.MosqueViewModel

@Composable
fun FinanceScreen(
    viewModel: MosqueViewModel,
    onAddIncomeClick: () -> Unit,
    onAddExpenseClick: () -> Unit
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val pendingTransactions by viewModel.pendingTransactions.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val netBalance by viewModel.netBalance.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Pending, 2: Income, 3: Expense

    val displayedTransactions = when (selectedTab) {
        1 -> pendingTransactions
        2 -> allTransactions.filter { it.type == "INCOME" }
        3 -> allTransactions.filter { it.type == "EXPENSE" }
        else -> allTransactions
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MosqueBgLight)
    ) {
        // Sticky Summary Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(14.dp)
        ) {
            // Net Balance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPrimary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "মসজিদ ফান্ড বর্তমান নিট ব্যালেন্স",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "৳${String.format("%,.0f", netBalance)}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MosqueGold,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MosqueSuccessGreen.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("মোট জমা (আয়)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                                Text("৳${String.format("%,.0f", totalIncome)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MosqueUrgentRed.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("মোট খরচ (ব্যয়)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                                Text("৳${String.format("%,.0f", totalExpense)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Add Income & Add Expense
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddIncomeClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueSuccessGreen)
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("আয় যোগ করুন", style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = onAddExpenseClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueUrgentRed)
                ) {
                    Icon(Icons.Default.RemoveCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ব্যয় লিপিবদ্ধ করুন", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("সমস্ত লেনদেন (${allTransactions.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("অনুমোদন বাকি")
                            if (pendingTransactions.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MosqueUrgentRed)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "${pendingTransactions.size}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("আয়সমূহ") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("ব্যয়সমূহ") }
                )
            }
        }

        // Transactions List
        LazyColumn(
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (displayedTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "কোনো লেনদেন পাওয়া যায়নি",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MosqueTextMuted
                        )
                    }
                }
            } else {
                items(displayedTransactions, key = { it.id }) { txn ->
                    TransactionCardItem(
                        transaction = txn,
                        canApprove = currentRole == UserRole.PRESIDENT || currentRole == UserRole.TREASURER,
                        onApprove = { viewModel.updateTransactionStatus(txn, true) },
                        onReject = { viewModel.updateTransactionStatus(txn, false) }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionCardItem(
    transaction: TransactionEntity,
    canApprove: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (transaction.type == "INCOME") MosqueSuccessGreen.copy(alpha = 0.12f)
                                else MosqueUrgentRed.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (transaction.type == "INCOME") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (transaction.type == "INCOME") MosqueSuccessGreen else MosqueUrgentRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = transaction.categoryNameBn,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MosqueTextPrimary
                        )
                        Text(
                            text = "ভাউচার: ${transaction.transactionCode} • ${transaction.txnDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MosqueTextSecondary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (transaction.type == "INCOME") "+" else "-"}৳${String.format("%,.0f", transaction.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.type == "INCOME") MosqueSuccessGreen else MosqueUrgentRed
                    )

                    // Approval badge
                    val badgeColor = when (transaction.approvalStatus) {
                        "approved" -> MosqueSuccessGreen
                        "pending" -> MosqueWarningAmber
                        else -> MosqueUrgentRed
                    }
                    val badgeText = when (transaction.approvalStatus) {
                        "approved" -> "অনুমোদিত"
                        "pending" -> "অনুমোদন বাকি"
                        else -> "প্রত্যাখ্যাত"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            if (transaction.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MosqueTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "দাখিলকারী: ${transaction.createdBy}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MosqueTextMuted,
                    fontSize = 11.sp
                )

                transaction.approvedBy?.let { approver ->
                    Text(
                        text = "অনুমোদক: $approver",
                        style = MaterialTheme.typography.labelSmall,
                        color = MosqueEmeraldDark,
                        fontSize = 11.sp
                    )
                }
            }

            // If pending and user has authority, show Approve / Reject actions (PRD §9)
            if (transaction.approvalStatus == "pending" && canApprove) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "কর্তৃপক্ষের অনুমোদন আবশ্যক:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MosqueTextSecondary,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueUrgentRed),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("প্রত্যাখ্যান")
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = onApprove,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("অনুমোদন করুন")
                    }
                }
            }
        }
    }
}
