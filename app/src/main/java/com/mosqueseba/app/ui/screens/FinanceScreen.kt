package com.mosqueseba.app.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mosqueseba.app.data.local.entity.CashboxRecordEntity
import com.mosqueseba.app.data.local.entity.TransactionEntity
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.ChandaMonthUtils
import com.mosqueseba.app.ui.components.MehrabArchCard
import com.mosqueseba.app.ui.components.MosqueConfirmDialog
import com.mosqueseba.app.ui.components.MosqueToastController
import com.mosqueseba.app.ui.components.toBengaliDigits
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.AiSummaryUiState
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private data class CategoryBreakdownItem(
    val category: String,
    val amount: Double,
    val count: Int,
    val percentage: Float
)

private data class JummahCollectionRecord(
    val id: String,
    val date: String,
    val title: String,
    val amount: Double,
    val isVerified: Boolean,
    val sourceTxn: TransactionEntity? = null
)

private fun isJummahTransaction(txn: TransactionEntity): Boolean {
    if (txn.type != "INCOME" || txn.approvalStatus == "rejected" || txn.isReversal) return false
    return txn.sourceType == "jummah_collection" ||
        txn.categoryNameBn.contains("জুমা") ||
        txn.categoryNameBn.contains("জুম্মা") ||
        txn.description.contains("জুমার") ||
        txn.description.contains("জুম্মা")
}

private fun isJummahCashbox(cb: CashboxRecordEntity): Boolean {
    return cb.boxType.contains("জুমা") ||
        cb.boxType.contains("জুম্মা") ||
        cb.notes.contains("জুমা") ||
        cb.notes.contains("জুম্মা")
}

private fun getFridaysOfMonth(ymKey: String): List<String> {
    return try {
        val ym = YearMonth.parse(ymKey)
        val fridays = mutableListOf<String>()
        var date = ym.atDay(1)
        val end = ym.atEndOfMonth()
        while (!date.isAfter(end)) {
            if (date.dayOfWeek == DayOfWeek.FRIDAY) {
                fridays.add(date.toString())
            }
            date = date.plusDays(1)
        }
        fridays
    } catch (_: Exception) {
        emptyList()
    }
}

private fun ordinalBengali(indexOneBased: Int): String = when (indexOneBased) {
    1 -> "১ম"
    2 -> "২য়"
    3 -> "৩য়"
    4 -> "৪র্থ"
    5 -> "৫ম"
    else -> "${toBengaliDigits(indexOneBased.toString())}তম"
}

private fun formatShortDayMonthBn(isoDate: String): String {
    return try {
        val ld = LocalDate.parse(isoDate)
        val shortMonth = ChandaMonthUtils.bengaliMonths[ld.monthValue - 1].take(6)
        "${toBengaliDigits(ld.dayOfMonth.toString())} $shortMonth"
    } catch (_: Exception) {
        toBengaliDigits(isoDate)
    }
}

private fun resolveTransactionChannel(
    txn: TransactionEntity,
    paymentMethodsById: Map<String, String>
): String {
    val chandaMethod = txn.sourceId?.let { paymentMethodsById[it] }
    if (chandaMethod != null) return chandaMethod
    return when {
        txn.sourceType.endsWith("mobile_banking") -> "mobile_banking"
        txn.sourceType.endsWith("online") ||
            txn.sourceType == "cashbox_deposit" ||
            txn.sourceType == "special_donation" -> "online"
        else -> "cash"
    }
}

private fun channelLabelBn(channel: String): String = when (channel) {
    "mobile_banking" -> "বিকাশ/নগদ"
    "online" -> "ব্যাংক/অনলাইন"
    else -> "নগদ"
}

private fun extractYearMonthKey(txnDate: String): String {
    val match = Regex("(\\d{4}-\\d{2})").find(txnDate.trim())
    return match?.groupValues?.get(1) ?: ""
}

private fun formatYearMonthBn(ymKey: String): String {
    val parts = ymKey.split("-")
    if (parts.size != 2) return toBengaliDigits(ymKey)
    val year = parts[0].toIntOrNull() ?: return toBengaliDigits(ymKey)
    val monthNum = parts[1].toIntOrNull() ?: return toBengaliDigits(ymKey)
    if (monthNum !in 1..12) return toBengaliDigits(ymKey)
    return ChandaMonthUtils.formatMonthYear(monthNum - 1, year)
}

private fun formatCurrencyBn(amount: Double): String {
    val absStr = String.format("%,.0f", kotlin.math.abs(amount))
    val formatted = "৳${toBengaliDigits(absStr)}"
    return if (amount < 0) "-$formatted" else formatted
}

@Composable
fun FinanceScreen(
    viewModel: MosqueViewModel,
    onAddIncomeClick: (() -> Unit)? = null,
    onAddExpenseClick: (() -> Unit)? = null,
    isReadOnlyTransparencyMode: Boolean = false,
    openReportTrigger: Int = 0,
    onReportTriggerConsumed: () -> Unit = {},
    openPendingTabTrigger: Int = 0,
    onPendingTabTriggerConsumed: () -> Unit = {}
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val pendingTransactions by viewModel.pendingTransactions.collectAsState()
    val allChandaPayments by viewModel.allChandaPayments.collectAsState()
    val allCashboxRecords by viewModel.allCashboxRecords.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val netBalance by viewModel.netBalance.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val mosque by viewModel.mosque.collectAsState()
    val aiSummaryState by viewModel.aiSummaryState.collectAsState()

    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0: All, 1: Pending, 2: Income, 3: Expense
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedMonthKey by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCategoryFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var expandedJummahSchedule by rememberSaveable { mutableStateOf(false) }

    var showCategoryBreakdown by rememberSaveable { mutableStateOf(true) }
    var breakdownType by rememberSaveable { mutableStateOf("INCOME") }
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedVoucherTxn by remember { mutableStateOf<TransactionEntity?>(null) }
    var pendingRejectTxn by remember { mutableStateOf<TransactionEntity?>(null) }

    LaunchedEffect(openReportTrigger) {
        if (openReportTrigger > 0) {
            showReportDialog = true
            onReportTriggerConsumed()
        }
    }

    LaunchedEffect(openPendingTabTrigger) {
        if (openPendingTabTrigger > 0) {
            selectedTab = 1
            onPendingTabTriggerConsumed()
        }
    }

    val paymentMethodsById = remember(allChandaPayments) {
        allChandaPayments.associate { it.id to it.method }
    }

    // Channel-wise approved fund split (sums directly to netBalance)
    val channelBalances = remember(allTransactions, paymentMethodsById) {
        var cashNet = 0.0
        var mobileNet = 0.0
        var onlineNet = 0.0
        allTransactions.forEach { txn ->
            if (txn.approvalStatus == "approved" && !txn.isReversal) {
                val signed = if (txn.type == "INCOME") txn.amount else -txn.amount
                when (resolveTransactionChannel(txn, paymentMethodsById)) {
                    "mobile_banking" -> mobileNet += signed
                    "online" -> onlineNet += signed
                    else -> cashNet += signed
                }
            }
        }
        Triple(cashNet, mobileNet, onlineNet)
    }

    // Available months sorted latest-first
    val availableMonths = remember(allTransactions) {
        allTransactions
            .mapNotNull { txn -> extractYearMonthKey(txn.txnDate).takeIf { it.isNotBlank() } }
            .distinct()
            .sortedDescending()
    }

    // Deduplicated Jummah Donation Records (Transactions + Jummah Cashbox Openings)
    val allJummahRecords = remember(allTransactions, allCashboxRecords) {
        val jummahTxns = allTransactions.filter { isJummahTransaction(it) }
        val result = jummahTxns.map { txn ->
            JummahCollectionRecord(
                id = "txn-${txn.id}",
                date = txn.txnDate,
                title = txn.categoryNameBn,
                amount = txn.amount,
                isVerified = txn.approvalStatus == "approved",
                sourceTxn = txn
            )
        }.toMutableList()

        allCashboxRecords.filter { isJummahCashbox(it) }.forEach { cb ->
            val alreadyInTxns = jummahTxns.any { txn ->
                txn.sourceId == cb.id ||
                    (txn.txnDate == cb.collectionDate && kotlin.math.abs(txn.amount - cb.countedAmount) < 1.0)
            }
            if (!alreadyInTxns) {
                result.add(
                    JummahCollectionRecord(
                        id = "cb-${cb.id}",
                        date = cb.collectionDate,
                        title = cb.boxType,
                        amount = cb.countedAmount,
                        isVerified = cb.witnessVerified || cb.depositStatus == "bank_deposited",
                        sourceTxn = null
                    )
                )
            }
        }
        result.sortedByDescending { it.date }
    }

    val activeJummahMonthKey = remember(selectedMonthKey, allJummahRecords, availableMonths) {
        selectedMonthKey
            ?: allJummahRecords.firstOrNull()?.let { extractYearMonthKey(it.date).takeIf { ym -> ym.isNotBlank() } }
            ?: availableMonths.firstOrNull()
            ?: YearMonth.now().toString()
    }
    val fridaysInActiveMonth = remember(activeJummahMonthKey) {
        getFridaysOfMonth(activeJummahMonthKey)
    }
    val scopedJummahRecords = remember(allJummahRecords, selectedMonthKey) {
        if (selectedMonthKey != null) {
            allJummahRecords.filter { extractYearMonthKey(it.date) == selectedMonthKey }
        } else {
            allJummahRecords
        }
    }
    val jummahTotalAmount = remember(scopedJummahRecords) {
        scopedJummahRecords.sumOf { it.amount }
    }
    val jummahDistinctDaysCount = remember(scopedJummahRecords) {
        scopedJummahRecords.map { it.date }.distinct().size
    }
    val jummahAverageIncome = remember(jummahTotalAmount, jummahDistinctDaysCount) {
        if (jummahDistinctDaysCount > 0) jummahTotalAmount / jummahDistinctDaysCount else 0.0
    }
    val jummahMonthCompletedCount = remember(allJummahRecords, activeJummahMonthKey) {
        allJummahRecords
            .filter { extractYearMonthKey(it.date) == activeJummahMonthKey }
            .map { it.date }
            .distinct()
            .size
    }

    // Transactions filtered by search + month (used before tab/category filters)
    val baseFilteredTransactions = remember(
        allTransactions,
        selectedMonthKey,
        searchQuery
    ) {
        val cleanQuery = searchQuery.trim()
        allTransactions.filter { txn ->
            val matchesMonth = selectedMonthKey == null || extractYearMonthKey(txn.txnDate) == selectedMonthKey
            val matchesQuery = cleanQuery.isBlank() ||
                txn.categoryNameBn.contains(cleanQuery, ignoreCase = true) ||
                txn.description.contains(cleanQuery, ignoreCase = true) ||
                txn.transactionCode.contains(cleanQuery, ignoreCase = true) ||
                txn.createdBy.contains(cleanQuery, ignoreCase = true)
            matchesMonth && matchesQuery
        }
    }

    // Category Breakdown data computed over non-rejected transactions in the selected month/channel scope
    val categoryBreakdownList = remember(baseFilteredTransactions, breakdownType) {
        val scoped = baseFilteredTransactions.filter {
            it.type == breakdownType && it.approvalStatus != "rejected" && !it.isReversal
        }
        val grandTotal = scoped.sumOf { it.amount }.coerceAtLeast(1.0)
        scoped.groupBy { it.categoryNameBn }
            .map { (cat, list) ->
                val catTotal = list.sumOf { it.amount }
                CategoryBreakdownItem(
                    category = cat,
                    amount = catTotal,
                    count = list.size,
                    percentage = (catTotal / grandTotal).toFloat().coerceIn(0f, 1f)
                )
            }
            .sortedByDescending { it.amount }
    }

    // Final displayed list respecting Tab + Category filter
    val displayedTransactions = remember(baseFilteredTransactions, selectedTab, selectedCategoryFilter) {
        baseFilteredTransactions.filter { txn ->
            val matchesTab = when (selectedTab) {
                1 -> txn.approvalStatus == "pending"
                2 -> txn.type == "INCOME"
                3 -> txn.type == "EXPENSE"
                else -> true
            }
            val matchesCategory = selectedCategoryFilter == null || txn.categoryNameBn == selectedCategoryFilter
            matchesTab && matchesCategory
        }
    }

    val hasAnyActiveFilter = searchQuery.isNotBlank() ||
        selectedMonthKey != null ||
        selectedCategoryFilter != null ||
        selectedTab != 0

    val canAddIncomeExpense = currentRole.canManageFinance()
    val isTransparencyOnly = isReadOnlyTransparencyMode || !canAddIncomeExpense

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
        ) {
            // 0. Role & Responsibility Authorization Banner
            item(key = "finance_role_badge") {
                val isDark = LocalIsDarkTheme.current
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (canAddIncomeExpense) {
                        if (isDark) MosqueEmeraldDark else MosqueEmeraldSoft
                    } else {
                        if (isDark) MosqueNightSurfaceVariant else MosqueGoldSoft
                    },
                    border = BorderStroke(
                        1.dp,
                        if (canAddIncomeExpense) MosqueEmeraldPrimary.copy(alpha = 0.5f) else MosqueGold.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (canAddIncomeExpense) Icons.Default.VerifiedUser else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = if (canAddIncomeExpense) MosqueEmeraldPrimary else MosqueGoldDeep,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (canAddIncomeExpense) {
                                    "${currentRole.titleBn} • বায়তুলমাল হিসাব ব্যবস্থাপনা ও অডিট নিয়ন্ত্রণ"
                                } else {
                                    "${currentRole.titleBn} • উন্মুক্ত ও ১০০% স্বচ্ছ কেন্দ্রীয় হিসাব নিরীক্ষণ ভিউ"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (canAddIncomeExpense) MosqueEmeraldPrimary else MosqueGoldDeep,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (canAddIncomeExpense) MosqueEmeraldPrimary.copy(alpha = 0.15f) else MosqueGold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (canAddIncomeExpense) "পরিচালনা মোড" else "স্বচ্ছতা মোড",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (canAddIncomeExpense) MosqueEmeraldPrimary else MosqueGoldDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // 1. Compact Hero Net Balance + Jummah Day Analytics Card
            item {
                val activeMonthLabel = formatYearMonthBn(activeJummahMonthKey)
                val totalFridays = fridaysInActiveMonth.size.coerceAtLeast(4)
                val runningOrdinal = (jummahMonthCompletedCount + 1).coerceAtMost(totalFridays)
                val activeMonthJummahRecords = remember(allJummahRecords, activeJummahMonthKey) {
                    allJummahRecords.filter { extractYearMonthKey(it.date) == activeJummahMonthKey }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                    border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MosqueEmeraldDarkGradient)
                    ) {
                        // Subtle Royal Gold Top Trim
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.5.dp)
                                .background(IslamicGoldTrimGradient)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawBehind {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFFF59E0B).copy(alpha = 0.14f),
                                                Color.Transparent
                                            ),
                                            center = Offset(size.width * 0.92f, size.height * 0.12f),
                                            radius = size.width * 0.55f
                                        ),
                                        radius = size.width * 0.55f,
                                        center = Offset(size.width * 0.92f, size.height * 0.12f)
                                    )
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF10B981).copy(alpha = 0.14f),
                                                Color.Transparent
                                            ),
                                            center = Offset(size.width * 0.08f, size.height * 0.88f),
                                            radius = size.width * 0.45f
                                        ),
                                        radius = size.width * 0.45f,
                                        center = Offset(size.width * 0.08f, size.height * 0.88f)
                                    )
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Top Row: Net Balance + Transaction count
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "মসজিদ ফান্ড নিট ব্যালেন্স",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                        Text(
                                            text = formatCurrencyBn(netBalance),
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MosqueGold,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ReceiptLong,
                                                contentDescription = null,
                                                tint = MosqueGoldLight,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${toBengaliDigits(allTransactions.size.toString())} লেনদেন",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                color = MosqueGoldLight,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Compact Inline Income vs Expense Pills
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White.copy(alpha = 0.08f),
                                        border = BorderStroke(1.dp, MosqueGreenAccent.copy(alpha = 0.38f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(CircleShape)
                                                    .background(MosqueSuccessGreen.copy(alpha = 0.45f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDownward,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "আয়: ${formatCurrencyBn(totalIncome)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White.copy(alpha = 0.08f),
                                        border = BorderStroke(1.dp, MosqueUrgentRed.copy(alpha = 0.45f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(CircleShape)
                                                    .background(MosqueUrgentRed.copy(alpha = 0.45f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowUpward,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "ব্যয়: ${formatCurrencyBn(totalExpense)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                // Compact Visual Income vs Expense Ratio Bar
                                val grandFlow = (totalIncome + totalExpense).coerceAtLeast(1.0)
                                val incomeRatio by animateFloatAsState(
                                    targetValue = (totalIncome / grandFlow).toFloat().coerceIn(0f, 1f),
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    label = "IncomeRatioAnim"
                                )
                                val incomePct = ((totalIncome / grandFlow) * 100).toInt().coerceIn(0, 100)
                                val expensePct = 100 - incomePct
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "আয় ${toBengaliDigits(incomePct.toString())}%",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF6EE7B7)
                                        )
                                        Text(
                                            text = "আয়-ব্যয় অনুপাত",
                                            fontSize = 10.sp,
                                            color = Color.White.copy(alpha = 0.72f)
                                        )
                                        Text(
                                            text = "ব্যয় ${toBengaliDigits(expensePct.toString())}%",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFCA5A5)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MosqueUrgentRed.copy(alpha = 0.75f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(incomeRatio)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MosqueGreenAccent)
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color.White.copy(alpha = 0.14f))

                            // Compact Jummah Section inside Hero Card
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.testTag("finance_jummah_hero_section")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { expandedJummahSchedule = !expandedJummahSchedule }
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mosque,
                                            contentDescription = "জুমার দিনের দান",
                                            tint = MosqueGoldLight,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        val jummahHeaderSubtitle = if (jummahMonthCompletedCount < totalFridays) {
                                            "জুমার তথ্য • $activeMonthLabel (${ordinalBengali(runningOrdinal)} চলমান)"
                                        } else {
                                            "জুমার তথ্য • $activeMonthLabel (সব সম্পন্ন)"
                                        }
                                        Text(
                                            text = jummahHeaderSubtitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MosqueGoldLight,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (expandedJummahSchedule) "লুকান" else "সূচি",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Icon(
                                            imageVector = if (expandedJummahSchedule) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = if (expandedJummahSchedule) "জুমাবার সূচি লুকান" else "জুমাবার সূচি দেখুন",
                                            tint = MosqueGoldLight,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    HeroJummahTile(
                                        title = if (selectedMonthKey != null) "মাসের জুমার দান" else "জুমার দিনের দান",
                                        value = formatCurrencyBn(jummahTotalAmount),
                                        subText = "${toBengaliDigits(jummahDistinctDaysCount.toString())} জুমা",
                                        icon = Icons.Default.Mosque,
                                        selected = selectedCategoryFilter == "জুমার দান বাক্স",
                                        onClick = {
                                            if (selectedCategoryFilter == "জুমার দান বাক্স") {
                                                selectedCategoryFilter = null
                                                selectedTab = 0
                                            } else {
                                                selectedTab = 2
                                                selectedCategoryFilter = "জুমার দান বাক্স"
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("hero_jummah_total_tile")
                                    )
                                    HeroJummahTile(
                                        title = "জুমা চলমান",
                                        value = "${toBengaliDigits(jummahMonthCompletedCount.toString())}/${toBengaliDigits(totalFridays.toString())} টি",
                                        subText = if (jummahMonthCompletedCount < totalFridays) {
                                            "${ordinalBengali(runningOrdinal)} চলমান"
                                        } else {
                                            "সব সম্পন্ন"
                                        },
                                        icon = Icons.Default.EventAvailable,
                                        selected = expandedJummahSchedule,
                                        onClick = { expandedJummahSchedule = !expandedJummahSchedule },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("hero_jummah_running_tile")
                                    )
                                    HeroJummahTile(
                                        title = "জুমার গড় আয়",
                                        value = formatCurrencyBn(jummahAverageIncome),
                                        subText = "প্রতি জুমা গড়",
                                        icon = Icons.Default.TrendingUp,
                                        selected = false,
                                        onClick = { expandedJummahSchedule = !expandedJummahSchedule },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("hero_jummah_avg_tile")
                                    )
                                }

                                AnimatedVisibility(visible = expandedJummahSchedule) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        if (fridaysInActiveMonth.isNotEmpty()) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                fridaysInActiveMonth.forEachIndexed { idx, fridayDate ->
                                                    val dayRecords = activeMonthJummahRecords.filter { it.date == fridayDate }
                                                    val dayTotal = dayRecords.sumOf { it.amount }
                                                    val isDone = dayRecords.isNotEmpty()
                                                    val isCurrentRunning = !isDone && idx == jummahMonthCompletedCount

                                                    val pillBg = when {
                                                        isDone -> MosqueSuccessGreen.copy(alpha = 0.25f)
                                                        isCurrentRunning -> MosqueGold.copy(alpha = 0.28f)
                                                        else -> Color.White.copy(alpha = 0.08f)
                                                    }
                                                    val pillBorder = when {
                                                        isDone -> MosqueGoldLight.copy(alpha = 0.65f)
                                                        isCurrentRunning -> MosqueGold
                                                        else -> Color.White.copy(alpha = 0.18f)
                                                    }
                                                    Surface(
                                                        onClick = {
                                                            if (isDone) {
                                                                dayRecords.firstOrNull()?.sourceTxn?.let { selectedVoucherTxn = it }
                                                            }
                                                        },
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = pillBg,
                                                        border = BorderStroke(if (isCurrentRunning) 1.5.dp else 1.dp, pillBorder),
                                                        modifier = Modifier.widthIn(min = 84.dp)
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                            verticalArrangement = Arrangement.spacedBy(1.dp)
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(
                                                                    imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                                                    contentDescription = null,
                                                                    tint = if (isDone || isCurrentRunning) MosqueGoldLight else Color.White.copy(alpha = 0.7f),
                                                                    modifier = Modifier.size(11.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(3.dp))
                                                                Text(
                                                                    text = "${ordinalBengali(idx + 1)} জুমা",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color.White
                                                                )
                                                            }
                                                            Text(
                                                                text = formatShortDayMonthBn(fridayDate),
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontSize = 9.sp,
                                                                color = Color.White.copy(alpha = 0.78f)
                                                            )
                                                            Text(
                                                                text = when {
                                                                    isDone -> formatCurrencyBn(dayTotal)
                                                                    isCurrentRunning -> "চলমান"
                                                                    else -> "আসছে"
                                                                },
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = when {
                                                                    isDone || isCurrentRunning -> MosqueGoldLight
                                                                    else -> Color.White.copy(alpha = 0.65f)
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (allJummahRecords.isNotEmpty()) {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                allJummahRecords.take(4).forEach { rec ->
                                                    Surface(
                                                        onClick = { rec.sourceTxn?.let { selectedVoucherTxn = it } },
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = Color.White.copy(alpha = 0.1f),
                                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                    text = rec.title,
                                                                    style = MaterialTheme.typography.labelMedium,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color.White,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                                Text(
                                                                    text = "${toBengaliDigits(rec.date)} • ${if (rec.isVerified) "যাচাইকৃত" else "অপেক্ষমাণ"}",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontSize = 10.sp,
                                                                    color = Color.White.copy(alpha = 0.75f)
                                                                )
                                                            }
                                                            Text(
                                                                text = formatCurrencyBn(rec.amount),
                                                                style = MaterialTheme.typography.labelLarge,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = MosqueGoldLight
                                                            )
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
                }
            }
            }

            // 2. Action Buttons Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isTransparencyOnly && onAddIncomeClick != null) {
                        Button(
                            onClick = onAddIncomeClick,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("finance_add_income_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MosqueEmeraldPrimary,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("আয় যোগ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }

                    if (!isTransparencyOnly && onAddExpenseClick != null) {
                        Button(
                            onClick = onAddExpenseClick,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("finance_add_expense_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MosqueUrgentRed,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.RemoveCircle, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("ব্যয় যোগ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }

                    Button(
                        onClick = { showReportDialog = true },
                        modifier = (if (isTransparencyOnly) Modifier.fillMaxWidth() else Modifier.weight(1f))
                            .heightIn(min = 48.dp)
                            .testTag("finance_report_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = if (isTransparencyOnly) {
                            ButtonDefaults.buttonColors(
                                containerColor = MosqueEmeraldPrimary,
                                contentColor = Color.White
                            )
                        } else {
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = MosqueGoldSoft,
                                contentColor = MosqueEmeraldDark
                            )
                        },
                        border = if (isTransparencyOnly) null else BorderStroke(1.dp, MosqueGold.copy(alpha = 0.65f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = if (isTransparencyOnly) Icons.Default.Summarize else Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isTransparencyOnly) MosqueGoldLight else MosqueGoldDeep,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isTransparencyOnly) "উন্মুক্ত অডিট ও আর্থিক প্রতিবেদন" else "রিপোর্ট ও এআই",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }


            // 3. Search Bar & Filter Section (Matching MembersScreen Design)
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    shape = RoundedCornerShape(14.dp),
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
                                    text = "ভাউচার, খাত বা বিবরণ খুঁজুন",
                                    fontSize = 13.sp
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "অনুসন্ধান", tint = MosqueEmeraldPrimary)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "মুছুন", tint = MosqueTextMuted)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MosqueEmeraldPrimary,
                                unfocusedBorderColor = MosqueCardBorderGold
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("finance_search_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val pendingCount = baseFilteredTransactions.count { it.approvalStatus == "pending" }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    label = {
                                        Text(
                                            text = "সমস্ত (${toBengaliDigits(baseFilteredTransactions.size.toString())})",
                                            fontSize = 11.sp
                                        )
                                    }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.WarningAmber,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (selectedTab == 1) Color.White else MosqueWarningAmber
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "অনুমোদন বাকি (${toBengaliDigits(pendingCount.toString())})",
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MosqueWarningAmber,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    label = { Text("আয়সমূহ", fontSize = 11.sp) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedTab == 3,
                                    onClick = { selectedTab = 3 },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    label = { Text("ব্যয়সমূহ", fontSize = 11.sp) }
                                )
                            }
                            selectedCategoryFilter?.let { cat ->
                                item {
                                    FilterChip(
                                        selected = true,
                                        onClick = { selectedCategoryFilter = null },
                                        label = { Text("খাত: $cat", fontSize = 11.sp) },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "বাদ দিন",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        if (availableMonths.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedMonthKey == null,
                                        onClick = { selectedMonthKey = null },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.CalendarMonth,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        label = { Text("সব মাস", fontSize = 11.sp) }
                                    )
                                }
                                items(availableMonths, key = { it }) { ym ->
                                    FilterChip(
                                        selected = selectedMonthKey == ym,
                                        onClick = {
                                            selectedMonthKey = if (selectedMonthKey == ym) null else ym
                                        },
                                        label = { Text(formatYearMonthBn(ym), fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Category Breakdown Visualization Card
            item {
                val breakdownScopedTotal = remember(categoryBreakdownList) {
                    categoryBreakdownList.sumOf { it.amount }
                }
                val isDark = LocalIsDarkTheme.current
                val primaryColor = MaterialTheme.colorScheme.primary
                val subtleSurfaceBg = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight
                val activeCatBg = if (isDark) MosqueEmeraldPrimary.copy(alpha = 0.28f) else MosqueEmeraldSoft.copy(alpha = 0.78f)
                val softIconBg = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft
                MehrabArchCard(
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    borderColor = MosqueCardBorderGold,
                    modifier = Modifier.animateContentSize(spring(stiffness = Spring.StiffnessMediumLow))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showCategoryBreakdown = !showCategoryBreakdown }
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(softIconBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BarChart,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "খাতভিত্তিক আয়-ব্যয় বিশ্লেষণ",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                    Text(
                                        text = "যেকোনো খাতে ট্যাপ করে লেনদেন ফিল্টার করুন",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (breakdownType == "INCOME") MosqueSuccessGreen.copy(alpha = 0.1f) else MosqueUrgentRed.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = formatCurrencyBn(breakdownScopedTotal),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (breakdownType == "INCOME") MosqueSuccessGreen else MosqueUrgentRed,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (showCategoryBreakdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (showCategoryBreakdown) "সংকুচিত করুন" else "বিস্তারিত দেখুন",
                                tint = primaryColor
                            )
                        }

                        AnimatedVisibility(visible = showCategoryBreakdown) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(subtleSurfaceBg)
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = { breakdownType = "INCOME" },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 42.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (breakdownType == "INCOME") MosqueEmeraldPrimary else Color.Transparent,
                                            contentColor = if (breakdownType == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        elevation = null,
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Text("আয় খাত", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { breakdownType = "EXPENSE" },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 42.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (breakdownType == "EXPENSE") MosqueUrgentRed else Color.Transparent,
                                            contentColor = if (breakdownType == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        elevation = null,
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Text("ব্যয় খাত", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (categoryBreakdownList.isEmpty()) {
                                    Text(
                                        text = "নির্বাচিত ফিল্টারে কোনো খাতভিত্তিক হিসাব পাওয়া যায়নি",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                } else {
                                    val barColor = if (breakdownType == "INCOME") MosqueSuccessGreen else MosqueUrgentRed
                                    categoryBreakdownList.forEachIndexed { index, item ->
                                        val isSelectedCat = selectedCategoryFilter == item.category
                                        val percentInt = kotlin.math.round(item.percentage * 100).toInt()
                                        val animatedBarFraction by animateFloatAsState(
                                            targetValue = item.percentage.coerceAtLeast(0.04f),
                                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                                            label = "CategoryBarAnim"
                                        )
                                        Surface(
                                            onClick = {
                                                selectedCategoryFilter = if (isSelectedCat) null else item.category
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelectedCat) activeCatBg else subtleSurfaceBg.copy(alpha = 0.72f),
                                            border = BorderStroke(
                                                width = if (isSelectedCat) 1.5.dp else 1.dp,
                                                color = if (isSelectedCat) primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 9.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                                        Box(
                                                            modifier = Modifier
                                                                .size(20.dp)
                                                                .clip(CircleShape)
                                                                .background(barColor.copy(alpha = 0.14f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = toBengaliDigits((index + 1).toString()),
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = barColor
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "${item.category} (${toBengaliDigits(item.count.toString())} টি)",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text(
                                                            text = formatCurrencyBn(item.amount),
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = barColor
                                                        )
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = barColor.copy(alpha = 0.12f)
                                                        ) {
                                                            Text(
                                                                text = "${toBengaliDigits(percentInt.toString())}%",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = barColor,
                                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(7.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(barColor.copy(alpha = 0.12f))
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(animatedBarFraction)
                                                            .fillMaxHeight()
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(
                                                                Brush.horizontalGradient(
                                                                    listOf(barColor.copy(alpha = 0.85f), barColor)
                                                                )
                                                            )
                                                    )
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

            // 5. Ledger Header
            item {
                val isDarkLedger = LocalIsDarkTheme.current
                val primaryColor = MaterialTheme.colorScheme.primary
                val pillBg = if (isDarkLedger) MosqueNightSurfaceVariant else MosqueEmeraldSoft
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(pillBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "লেনদেন ও ভাউচার তালিকা",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = pillBg
                        ) {
                            Text(
                                text = "${toBengaliDigits(displayedTransactions.size.toString())} টি",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (hasAnyActiveFilter) {
                        TextButton(
                            onClick = {
                                searchQuery = ""
                                selectedTab = 0
                                selectedMonthKey = null
                                selectedCategoryFilter = null
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("ফিল্টার মুছুন", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = primaryColor)
                        }
                    }
                }
            }

            // 6. Transactions List
            if (displayedTransactions.isEmpty()) {
                item {
                    MehrabArchCard(backgroundColor = MaterialTheme.colorScheme.surface, borderColor = MaterialTheme.colorScheme.outline) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(26.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(if (LocalIsDarkTheme.current) MosqueNightSurfaceVariant else MosqueEmeraldSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = "নির্বাচিত ফিল্টারে কোনো লেনদেন পাওয়া যায়নি",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "সার্চ শব্দ বা মাস/খাত ফিল্টার পরিবর্তন করে আবার দেখুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = MosqueTextMuted,
                                textAlign = TextAlign.Center
                            )
                            if (hasAnyActiveFilter) {
                                FilledTonalButton(
                                    onClick = {
                                        searchQuery = ""
                                        selectedTab = 0
                                        selectedMonthKey = null
                                        selectedCategoryFilter = null
                                    },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MosqueEmeraldSoft,
                                        contentColor = MosqueEmeraldDark
                                    ),
                                    modifier = Modifier.heightIn(min = 44.dp)
                                ) {
                                    Text("সব ফিল্টার মুছুন", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                items(displayedTransactions, key = { it.id }) { txn ->
                    val channel = resolveTransactionChannel(txn, paymentMethodsById)
                    TransactionCardItem(
                        transaction = txn,
                        channelLabel = channelLabelBn(channel),
                        canApprove = currentRole.canApproveFinance(),
                        onClick = { selectedVoucherTxn = txn },
                        onApprove = {
                            viewModel.updateTransactionStatus(txn, true)
                            MosqueToastController.success(
                                "লেনদেনটি অনুমোদিত হয়েছে (৳${toBengaliDigits(txn.amount.toInt().toString())})।",
                                "ভাউচার অনুমোদিত"
                            )
                        },
                        onReject = { pendingRejectTxn = txn }
                    )
                }
            }
        }
    }

    // Financial Report & AI Summary Dialog
    if (showReportDialog) {
        FinancialReportDialog(
            mosqueName = mosque?.nameBn ?: "বায়তুল জান্নাত জামে মসজিদ",
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = netBalance,
            channelBalances = channelBalances,
            jummahSummary = Triple(jummahTotalAmount, jummahDistinctDaysCount, jummahAverageIncome),
            transactions = allTransactions,
            aiSummaryState = aiSummaryState,
            onGenerateAiSummary = { viewModel.generateFinanceSummaryWithAi() },
            onClearAiSummary = { viewModel.clearAiSummaryState() },
            onDismiss = { showReportDialog = false }
        )
    }

    // Official Transaction Voucher Preview Dialog
    selectedVoucherTxn?.let { txn ->
        val latestTxn = allTransactions.firstOrNull { it.id == txn.id } ?: txn
        val channel = resolveTransactionChannel(latestTxn, paymentMethodsById)
        TransactionVoucherDialog(
            transaction = latestTxn,
            channelLabel = channelLabelBn(channel),
            mosqueName = mosque?.nameBn ?: "বায়তুল জান্নাত জামে মসজিদ",
            mosqueAddress = mosque?.address ?: "মিরপুর-১০, ঢাকা",
            committeeId = mosque?.committeeId ?: "MC-DHK-2024",
            canApprove = currentRole.canApproveFinance(),
            onApprove = {
                viewModel.updateTransactionStatus(latestTxn, true)
                selectedVoucherTxn = null
                MosqueToastController.success(
                    "ভাউচারটি অনুমোদিত হয়েছে (৳${toBengaliDigits(latestTxn.amount.toInt().toString())})।",
                    "ভাউচার অনুমোদিত"
                )
            },
            onReject = {
                selectedVoucherTxn = null
                pendingRejectTxn = latestTxn
            },
            onDismiss = { selectedVoucherTxn = null }
        )
    }

    // Reject Transaction Confirmation Dialog
    pendingRejectTxn?.let { target ->
        MosqueConfirmDialog(
            title = "লেনদেন বাতিল/প্রত্যাখ্যান করবেন?",
            message = "\"${target.categoryNameBn}\" (৳${toBengaliDigits(target.amount.toInt().toString())}) লেনদেনটি প্রত্যাখ্যান করা হবে এবং হিসাবের চূড়ান্ত ব্যালেন্স থেকে বাদ যাবে।",
            confirmText = "হ্যাঁ, প্রত্যাখ্যান করুন",
            icon = Icons.Default.Cancel,
            isDestructive = true,
            onDismiss = { pendingRejectTxn = null },
            onConfirm = {
                viewModel.updateTransactionStatus(target, false)
                pendingRejectTxn = null
                MosqueToastController.warning(
                    "লেনদেনটি প্রত্যাখ্যান করা হয়েছে।",
                    "ভাউচার প্রত্যাখ্যাত"
                )
            }
        )
    }
}

@Composable
private fun HeroJummahTile(
    title: String,
    value: String,
    subText: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MosqueGold.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.12f),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MosqueGold else Color.White.copy(alpha = 0.2f)
        ),
        modifier = modifier.heightIn(min = 54.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 7.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MosqueGoldLight,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MosqueGoldLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subText,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
fun TransactionCardItem(
    transaction: TransactionEntity,
    channelLabel: String = "নগদ",
    canApprove: Boolean,
    onClick: () -> Unit = {},
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val isIncome = transaction.type == "INCOME"
    val accentStripColor = when {
        transaction.approvalStatus == "pending" -> MosqueWarningAmber
        transaction.approvalStatus == "rejected" -> MosqueTextMuted
        isIncome -> MosqueSuccessGreen
        else -> MosqueUrgentRed
    }
    val isDark = LocalIsDarkTheme.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val channelBadgeBg = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("txn_card_${transaction.transactionCode}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (transaction.approvalStatus == "pending") MosqueWarningAmber.copy(alpha = 0.55f) else MosqueCardBorderGold
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(
                        color = accentStripColor,
                        size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height)
                    )
                }
                .padding(start = 16.dp, end = 14.dp, top = 12.dp, bottom = 12.dp)
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isIncome) MosqueSuccessGreen.copy(alpha = 0.14f)
                                else MosqueUrgentRed.copy(alpha = 0.14f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = if (isIncome) "আয়" else "ব্যয়",
                            tint = if (isIncome) MosqueSuccessGreen else MosqueUrgentRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = transaction.categoryNameBn,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "ভাউচার: ${toBengaliDigits(transaction.transactionCode)} • ${toBengaliDigits(transaction.txnDate)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = secondaryTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isIncome) MosqueSuccessGreen.copy(alpha = 0.12f) else MosqueUrgentRed.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${if (isIncome) "+" else "-"}${formatCurrencyBn(transaction.amount)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isIncome) MosqueSuccessGreen else MosqueUrgentRed,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = channelBadgeBg
                        ) {
                            Text(
                                text = channelLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }

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
                                .background(badgeColor.copy(alpha = 0.14f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            if (transaction.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MosqueTextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "দাখিলকারী: ${transaction.createdBy}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MosqueTextMuted,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    transaction.approvedBy?.let { approver ->
                        Text(
                            text = "অনুমোদক: $approver",
                            style = MaterialTheme.typography.labelSmall,
                            color = MosqueEmeraldDark,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MosqueEmeraldSoft.copy(alpha = 0.65f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MosqueEmeraldPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "ভাউচার",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MosqueEmeraldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // If pending and user has authority, show Approve / Reject actions (PRD §9)
            if (transaction.approvalStatus == "pending" && canApprove) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MosqueCardBorderSubtle)
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
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("প্রত্যাখ্যান")
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = onApprove,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.heightIn(min = 48.dp)
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
fun TransactionVoucherDialog(
    transaction: TransactionEntity,
    channelLabel: String,
    mosqueName: String,
    mosqueAddress: String,
    committeeId: String,
    canApprove: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val subtleBg = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight
    val subtleBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
    val isIncome = transaction.type == "INCOME"
    val statusText = when (transaction.approvalStatus) {
        "approved" -> "অনুমোদিত"
        "pending" -> "অনুমোদন বাকি"
        else -> "প্রত্যাখ্যাত"
    }
    val statusColor = when (transaction.approvalStatus) {
        "approved" -> MosqueSuccessGreen
        "pending" -> MosqueWarningAmber
        else -> MosqueUrgentRed
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MehrabArchCard(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 520.dp),
            backgroundColor = MaterialTheme.colorScheme.surface,
            borderColor = MosqueCardBorderGold
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = mosqueName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "$mosqueAddress • কমিটি আইডি: ${toBengaliDigits(committeeId)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(6.dp), color = MosqueGoldLight) {
                        Text(
                            text = if (isIncome) "অফিসিয়াল আয় ভাউচার" else "অফিসিয়াল ব্যয় ভাউচার",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MosqueEmeraldDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(alpha = 0.14f)) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MosqueGold)
                Spacer(modifier = Modifier.height(12.dp))

                VoucherRow("ভাউচার নম্বর", toBengaliDigits(transaction.transactionCode))
                VoucherRow("খাতের নাম", transaction.categoryNameBn)
                VoucherRow("লেনদেনের ধরন", if (isIncome) "আয় (জমা)" else "ব্যয় (খরচ)")
                VoucherRow("টাকার পরিমাণ", formatCurrencyBn(transaction.amount))
                VoucherRow("মাধ্যম", channelLabel)
                VoucherRow("তারিখ", toBengaliDigits(transaction.txnDate))
                VoucherRow("দাখিলকারী", transaction.createdBy)
                transaction.approvedBy?.let {
                    VoucherRow("অনুমোদক", it)
                }

                if (transaction.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = subtleBg,
                        border = BorderStroke(1.dp, subtleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "বিস্তারিত বিবরণ:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = transaction.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (transaction.approvalStatus == "pending" && canApprove) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueUrgentRed)
                        ) {
                            Text("প্রত্যাখ্যান", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onApprove,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("অনুমোদন", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                    ) {
                        Text("বন্ধ করুন", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            val shareText = buildString {
                                appendLine(mosqueName)
                                appendLine(if (isIncome) "অফিসিয়াল আয় ভাউচার" else "অফিসিয়াল ব্যয় ভাউচার")
                                appendLine("-------------------------")
                                appendLine("ভাউচার নম্বর: ${toBengaliDigits(transaction.transactionCode)}")
                                appendLine("খাত: ${transaction.categoryNameBn}")
                                appendLine("পরিমাণ: ${formatCurrencyBn(transaction.amount)}")
                                appendLine("মাধ্যম: $channelLabel")
                                appendLine("তারিখ: ${toBengaliDigits(transaction.txnDate)}")
                                appendLine("অবস্থা: $statusText")
                                appendLine("দাখিলকারী: ${transaction.createdBy}")
                                transaction.approvedBy?.let { appendLine("অনুমোদক: $it") }
                                if (transaction.description.isNotBlank()) {
                                    appendLine("বিবরণ: ${transaction.description}")
                                }
                            }
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "ভাউচার - ${transaction.transactionCode}")
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "ভাউচার শেয়ার করুন"))
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ভাউচার শেয়ার", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun VoucherRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}



@Composable
fun FinancialReportDialog(
    mosqueName: String = "বায়তুল জান্নাত জামে মসজিদ",
    totalIncome: Double,
    totalExpense: Double,
    netBalance: Double,
    channelBalances: Triple<Double, Double, Double> = Triple(netBalance, 0.0, 0.0),
    jummahSummary: Triple<Double, Int, Double> = Triple(0.0, 0, 0.0),
    transactions: List<TransactionEntity>,
    aiSummaryState: AiSummaryUiState = AiSummaryUiState.Idle,
    onGenerateAiSummary: () -> Unit = {},
    onClearAiSummary: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val dividerColor = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
    val aiContainerBg = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft.copy(alpha = 0.65f)
    val channelColor = MaterialTheme.colorScheme.primary

    DisposableEffect(Unit) {
        onDispose { onClearAiSummary() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MehrabArchCard(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 540.dp),
            backgroundColor = MaterialTheme.colorScheme.surface,
            borderColor = MosqueCardBorderGold
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
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$mosqueName • অডিট প্রতিবেদন",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MosqueGold)
                Spacer(modifier = Modifier.height(12.dp))

                val approvedCount = transactions.count { it.approvalStatus == "approved" }
                val pendingCount = transactions.count { it.approvalStatus == "pending" }

                ReportRow("সর্বমোট জমা (আয়)", formatCurrencyBn(totalIncome), MosqueSuccessGreen)
                ReportRow("সর্বমোট খরচ (ব্যয়)", formatCurrencyBn(totalExpense), MosqueUrgentRed)
                ReportRow("বর্তমান নিট ব্যালেন্স", formatCurrencyBn(netBalance), MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = dividerColor)
                Spacer(modifier = Modifier.height(6.dp))
                ReportRow("নগদ ফান্ড ব্যালেন্স", formatCurrencyBn(channelBalances.first), channelColor)
                ReportRow("বিকাশ/নগদ ব্যালেন্স", formatCurrencyBn(channelBalances.second), channelColor)
                ReportRow("ব্যাংক/অনলাইন ব্যালেন্স", formatCurrencyBn(channelBalances.third), channelColor)
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = dividerColor)
                Spacer(modifier = Modifier.height(6.dp))
                ReportRow("জুমার দিনের মোট দান", formatCurrencyBn(jummahSummary.first), MaterialTheme.colorScheme.primary)
                ReportRow("সম্পন্ন জুমার সংখ্যা", "${toBengaliDigits(jummahSummary.second.toString())} টি", MaterialTheme.colorScheme.onSurface)
                ReportRow("জুমার গড় আয়", formatCurrencyBn(jummahSummary.third), MosqueSuccessGreen)
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = dividerColor)
                Spacer(modifier = Modifier.height(6.dp))
                ReportRow("অনুমোদিত লেনদেন সংখ্যা", "${toBengaliDigits(approvedCount.toString())} টি", MaterialTheme.colorScheme.onSurface)
                ReportRow("অপেক্ষমাণ অনুমোদন", "${toBengaliDigits(pendingCount.toString())} টি", MosqueWarningAmber)

                Spacer(modifier = Modifier.height(12.dp))

                // Gemini AI Financial Summary Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = aiContainerBg,
                    border = BorderStroke(1.dp, MosqueCardBorderGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "জেমিনি এআই আর্থিক সারসংক্ষেপ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        when (aiSummaryState) {
                            is AiSummaryUiState.Idle -> {
                                Button(
                                    onClick = onGenerateAiSummary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldDark)
                                ) {
                                    Text("এআই আর্থিক সারসংক্ষেপ তৈরি করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            is AiSummaryUiState.Loading -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "হিসাব বিশ্লেষণ চলছে...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            is AiSummaryUiState.Streaming -> {
                                Text(
                                    text = aiSummaryState.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            is AiSummaryUiState.Success -> {
                                Text(
                                    text = aiSummaryState.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                TextButton(
                                    onClick = onGenerateAiSummary,
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("পুনরায় বিশ্লেষণ করুন", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
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
                                    Text("আবার চেষ্টা করুন", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = dividerColor)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                    ) {
                        Text("বন্ধ করুন", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
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
                                appendLine("মোট আদায় (আয়): ${formatCurrencyBn(totalIncome)}")
                                appendLine("মোট খরচ (ব্যয়): ${formatCurrencyBn(totalExpense)}")
                                appendLine("বর্তমান নিট ব্যালেন্স: ${formatCurrencyBn(netBalance)}")
                                appendLine("নগদ ফান্ড: ${formatCurrencyBn(channelBalances.first)}")
                                appendLine("বিকাশ/নগদ ফান্ড: ${formatCurrencyBn(channelBalances.second)}")
                                appendLine("ব্যাংক/অনলাইন ফান্ড: ${formatCurrencyBn(channelBalances.third)}")
                                appendLine("জুমার মোট দান: ${formatCurrencyBn(jummahSummary.first)} (${toBengaliDigits(jummahSummary.second.toString())} জুমা)")
                                appendLine("জুমার গড় আয়: ${formatCurrencyBn(jummahSummary.third)}")
                                appendLine("অনুমোদিত লেনদেন: ${toBengaliDigits(approvedCount.toString())} টি")
                                appendLine("অপেক্ষমাণ অনুমোদন: ${toBengaliDigits(pendingCount.toString())} টি")
                                append(aiText)
                            }
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "$mosqueName - আর্থিক রিপোর্ট")
                                putExtra(Intent.EXTRA_TEXT, reportText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "আর্থিক রিপোর্ট শেয়ার করুন"))
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("রিপোর্ট শেয়ার", fontWeight = FontWeight.Bold)
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
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
