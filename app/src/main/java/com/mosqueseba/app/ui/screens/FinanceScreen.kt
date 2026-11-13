package com.mosqueseba.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.data.local.entity.TransactionEntity
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.AiSummaryUiState
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel

@Composable
fun FinanceScreen(
    viewModel: MosqueViewModel,
    onAddIncomeClick: (() -> Unit)? = null,
    onAddExpenseClick: (() -> Unit)? = null,
    isReadOnlyTransparencyMode: Boolean = false
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val pendingTransactions by viewModel.pendingTransactions.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val netBalance by viewModel.netBalance.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val mosque by viewModel.mosque.collectAsState()
    val aiSummaryState by viewModel.aiSummaryState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Pending, 2: Income, 3: Expense
    var showReportDialog by remember { mutableStateOf(false) }

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
                    .clip(RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .background(MosqueEmeraldGradient)
                        .padding(18.dp)
                ) {
                    Column {
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
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(MosqueSuccessGreen.copy(alpha = 0.35f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("মোট আদায় (আয়)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
                                    Text("৳${String.format("%,.0f", totalIncome)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(MosqueUrgentRed.copy(alpha = 0.35f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("মোট খরচ (ব্যয়)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
                                    Text("৳${String.format("%,.0f", totalExpense)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Add Income, Add Expense & Financial Report
            val canAddIncomeExpense = currentRole in listOf(UserRole.TREASURER, UserRole.PRESIDENT, UserRole.VICE_PRESIDENT)
            val isTransparencyOnly = isReadOnlyTransparencyMode || !canAddIncomeExpense
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!isTransparencyOnly && onAddIncomeClick != null) {
                    Button(
                        onClick = onAddIncomeClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueSuccessGreen)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("আয় যোগ", style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (!isTransparencyOnly && onAddExpenseClick != null) {
                    Button(
                        onClick = onAddExpenseClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueUrgentRed)
                    ) {
                        Icon(Icons.Default.RemoveCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("ব্যয় যোগ", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Button(
                    onClick = { showReportDialog = true },
                    modifier = if (isTransparencyOnly) Modifier.fillMaxWidth() else Modifier.wrapContentWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = if (isTransparencyOnly) {
                        ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    } else {
                        ButtonDefaults.outlinedButtonColors(contentColor = MosqueEmeraldPrimary)
                    }
                ) {
                    Icon(Icons.Default.Summarize, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (isTransparencyOnly) "উন্মুক্ত অডিট ও আর্থিক প্রতিবেদন দেখুন" else "রিপোর্ট",
                        style = MaterialTheme.typography.labelSmall
                    )
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
                        canApprove = currentRole.canApproveFinance(),
                        onApprove = { viewModel.updateTransactionStatus(txn, true) },
                        onReject = { viewModel.updateTransactionStatus(txn, false) }
                    )
                }
            }
        }
    }

    if (showReportDialog) {
        FinancialReportDialog(
            mosqueName = mosque?.nameBn ?: "বায়তুল জান্নাত জামে মসজিদ",
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = netBalance,
            transactions = allTransactions,
            aiSummaryState = aiSummaryState,
            onGenerateAiSummary = { viewModel.generateFinanceSummaryWithAi() },
            onClearAiSummary = { viewModel.clearAiSummaryState() },
            onDismiss = { showReportDialog = false }
        )
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MosqueCardBorderSubtle)
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

@Composable
fun FinancialReportDialog(
    mosqueName: String = "বায়তুল জান্নাত জামে মসজিদ",
    totalIncome: Double,
    totalExpense: Double,
    netBalance: Double,
    transactions: List<TransactionEntity>,
    aiSummaryState: AiSummaryUiState = AiSummaryUiState.Idle,
    onGenerateAiSummary: () -> Unit = {},
    onClearAiSummary: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose { onClearAiSummary() }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "আয়-ব্যয় সারসংক্ষেপ রিপোর্ট",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )
                Text(
                    text = "$mosqueName • অডিট প্রতিবেদন",
                    style = MaterialTheme.typography.bodySmall,
                    color = MosqueTextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MosqueGold)
                Spacer(modifier = Modifier.height(12.dp))

                val approvedCount = transactions.count { it.approvalStatus == "approved" }
                val pendingCount = transactions.count { it.approvalStatus == "pending" }

                ReportRow("সর্বমোট জমা (আয়)", "৳${String.format("%,.0f", totalIncome)}", MosqueSuccessGreen)
                ReportRow("সর্বমোট খরচ (ব্যয়)", "৳${String.format("%,.0f", totalExpense)}", MosqueUrgentRed)
                ReportRow("ফান্ড উদ্বৃত্ত / বর্তমান ব্যালেন্স", "৳${String.format("%,.0f", netBalance)}", MosqueEmeraldPrimary)
                ReportRow("অনুমোদিত লেনদেন সংখ্যা", "$approvedCount টি", MosqueTextPrimary)
                ReportRow("অপেক্ষমান অনুমোদন", "$pendingCount টি", MosqueWarningAmber)

                Spacer(modifier = Modifier.height(12.dp))

                // Gemini AI Financial Summary Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MosqueEmeraldLight.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✨ জেমিনি এআই আর্থিক সারসংক্ষেপ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldDark
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        when (aiSummaryState) {
                            is AiSummaryUiState.Idle -> {
                                Button(
                                    onClick = onGenerateAiSummary,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldDark)
                                ) {
                                    Text("এআই আর্থিক সারসংক্ষেপ তৈরি করুন", fontSize = 12.sp)
                                }
                            }
                            is AiSummaryUiState.Loading -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MosqueEmeraldPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "জেমিনি হিসাব বিশ্লেষণ করছে...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MosqueEmeraldDark
                                    )
                                }
                            }
                            is AiSummaryUiState.Streaming -> {
                                Text(
                                    text = aiSummaryState.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueTextPrimary
                                )
                            }
                            is AiSummaryUiState.Success -> {
                                Text(
                                    text = aiSummaryState.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueTextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                TextButton(
                                    onClick = onGenerateAiSummary,
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("পুনরায় বিশ্লেষণ করুন", fontSize = 11.sp, color = MosqueEmeraldPrimary)
                                }
                            }
                            is AiSummaryUiState.Error -> {
                                Text(
                                    text = aiSummaryState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueUrgentRed
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                TextButton(
                                    onClick = onGenerateAiSummary,
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("আবার চেষ্টা করুন", fontSize = 11.sp, color = MosqueEmeraldPrimary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MosqueCardBorder)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বন্ধ করুন")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val aiText = when (aiSummaryState) {
                                is AiSummaryUiState.Success -> "\n\nএআই সারসংক্ষেপ:\n${aiSummaryState.text}"
                                is AiSummaryUiState.Streaming -> "\n\nএআই সারসংক্ষেপ:\n${aiSummaryState.text}"
                                else -> ""
                            }
                            val reportText = buildString {
                                appendLine("$mosqueName — আয়-ব্যয় সারসংক্ষেপ রিপোর্ট")
                                appendLine("------------------------------------------")
                                appendLine("মোট আদায় (আয়): ৳${String.format("%,.0f", totalIncome)}")
                                appendLine("মোট খরচ (ব্যয়): ৳${String.format("%,.0f", totalExpense)}")
                                appendLine("বর্তমান নিট ব্যালেন্স: ৳${String.format("%,.0f", netBalance)}")
                                appendLine("অনুমোদিত লেনদেন: $approvedCount টি")
                                appendLine("অপেক্ষমান অনুমোদন: $pendingCount টি")
                                append(aiText)
                            }
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "$mosqueName - আর্থিক রিপোর্ট")
                                putExtra(Intent.EXTRA_TEXT, reportText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "আর্থিক রিপোর্ট শেয়ার করুন"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("রিপোর্ট শেয়ার")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MosqueTextSecondary)
        Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

