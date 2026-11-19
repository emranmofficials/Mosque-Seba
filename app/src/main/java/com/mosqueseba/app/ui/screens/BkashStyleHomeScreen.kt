package com.mosqueseba.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.toBengaliDigits
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * bKash-অনুপ্রাণিত হোম স্ক্রিন।
 *
 * লেআউট/ভিজুয়াল bKash-এর মতো (pink ব্যালেন্স কার্ড, কুইক-অ্যাকশন গ্রিড,
 * সাম্প্রতিক লেনদেন, নোটিশ) — কিন্তু প্রতিটি অ্যাকশন মসজিদ সেবার আসল
 * ডায়ালগ/মডিউলে যায়। কোনো বিজনেস লজিক এখানে লেখা হয়নি; সব ডেটা
 * আসল MosqueViewModel থেকে আসে।
 */
@Composable
fun BkashStyleHomeScreen(
    viewModel: MosqueViewModel,
    onNavigateToTab: (Int) -> Unit,
    onOpenDialog: (String) -> Unit
) {
    val mosque by viewModel.mosque.collectAsState()
    val netBalance by viewModel.netBalance.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val memberCount by viewModel.memberCount.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allNotices by viewModel.allNotices.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val isDark = com.mosqueseba.app.ui.theme.LocalIsDarkTheme.current
    var isBalanceRevealed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val memberName = viewModel.sessionManager.getSavedMemberName()
        .ifBlank { currentRole.titleBn }

    val recentTransactions = allTransactions
        .sortedByDescending { it.createdAt }
        .take(4)
    val latestNotices = allNotices
        .filter { it.status == "published" }
        .sortedByDescending { it.createdAt }
        .take(2)
    val upcomingEvents = allEvents.filter { it.status != "completed" }.take(2)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "আসসালামু আলাইকুম, $memberName",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = mosque?.nameBn ?: "মসজিদ সেবা",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // bKash-স্টাইল ইন্টারঅ্যাক্টিভ ব্যালেন্স কার্ড → বায়তুল মাল নিট স্থিতি
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MosqueEmeraldDark,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.35f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MosqueEmeraldGradient)
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "বায়তুল মাল তহবিল স্থিতি",
                                color = MosqueGoldLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = mosque?.nameBn ?: "মসজিদ সেবা",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // bKash-স্টাইল ট্যাপ-টু-রিভিল ব্যালেন্স বাটন (Interactive Tap-to-Reveal Pill)
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.55f)),
                            modifier = Modifier
                                .clickable {
                                    if (!isBalanceRevealed) {
                                        coroutineScope.launch {
                                            isBalanceRevealed = true
                                            delay(5000)
                                            isBalanceRevealed = false
                                        }
                                    } else {
                                        isBalanceRevealed = false
                                    }
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                                    .animateContentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MosqueGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                if (isBalanceRevealed) {
                                    Text(
                                        text = "৳ ${toBengaliDigits(netBalance.toLong().toString())}",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "ব্যালেন্স জানতে ট্যাপ করুন",
                                        color = Color.White.copy(alpha = 0.95f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BalanceChip(
                                label = "মোট আয়",
                                value = "৳ ${toBengaliDigits(totalIncome.toLong().toString())}",
                                modifier = Modifier.weight(1f)
                            )
                            BalanceChip(
                                label = "মোট ব্যয়",
                                value = "৳ ${toBengaliDigits(totalExpense.toLong().toString())}",
                                modifier = Modifier.weight(1f)
                            )
                            BalanceChip(
                                label = "সদস্য",
                                value = toBengaliDigits(memberCount.toString()),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // bKash-এর QR / সার্ভিস কার্ড → মসজিদের QR রসিদ ও স্বচ্ছ হিসাব
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureCard(
                    title = "চাঁদা QR",
                    subtitle = "ডিজিটাল রসিদ",
                    icon = Icons.Filled.QrCode2,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenDialog("my_chanda") }
                )
                FeatureCard(
                    title = "স্বচ্ছ হিসাব",
                    subtitle = "আয়-ব্যয়",
                    icon = Icons.Filled.Assessment,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenDialog("transparency") }
                )
            }
        }

        // কুইক অ্যাকশন — bKash-এর সেন্ড মানি/ক্যাশ আউট গ্রিড, ভেতরে মসজিদের ফিচার
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Column(Modifier.padding(vertical = 14.dp, horizontal = 6.dp)) {
                    Text(
                        text = "দ্রুত কাজ",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 10.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(10.dp))
                    val chandaKey = if (currentRole.canManageFinance()) "receive_chanda" else "my_chanda"
                    val actions = listOf(
                        QuickAction("চাঁদা", Icons.Filled.Payments, chandaKey),
                        QuickAction("আয়", Icons.Filled.AccountBalanceWallet, "add_income"),
                        QuickAction("ব্যয়", Icons.Filled.ReceiptLong, "add_expense"),
                        QuickAction("সদস্য", Icons.Filled.People, "members"),
                        QuickAction("ক্যাশবক্স", Icons.Filled.Inventory2, "cashbox"),
                        QuickAction("মাদরাসা", Icons.Filled.School, "madrasa"),
                        QuickAction("সভা", Icons.Filled.EventAvailable, "meetings"),
                        QuickAction("ইভেন্ট", Icons.Filled.Celebration, "events")
                    )
                    actions.chunked(4).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { action ->
                                QuickActionItem(action = action, onClick = { onOpenDialog(action.key) })
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        // সাম্প্রতিক লেনদেন
        item {
            SectionHeader("সাম্প্রতিক লেনদেন")
        }
        items(recentTransactions, key = { it.id }) { txn ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (txn.type == "INCOME") MosqueEmeraldSoft else MosqueGoldLight,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (txn.type == "INCOME") Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(txn.categoryNameBn, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = txn.description.ifBlank { txn.txnDate },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "${if (txn.type == "INCOME") "+" else "-"}৳ ${toBengaliDigits(txn.amount.toLong().toString())}",
                        color = if (txn.type == "INCOME") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // নোটিশ ও ইভেন্ট
        if (latestNotices.isNotEmpty() || upcomingEvents.isNotEmpty()) {
            item { SectionHeader("খবর ও ইভেন্ট") }
            items(latestNotices, key = { "n_${it.id}" }) { notice ->
                InfoRow(Icons.Filled.Campaign, notice.title, notice.publishDate) {
                    onOpenDialog("notices")
                }
            }
            items(upcomingEvents, key = { "e_${it.id}" }) { event ->
                InfoRow(Icons.Filled.Celebration, event.title, "${event.dateStr} • ${event.venue}") {
                    onOpenDialog("events")
                }
            }
        }

        item {
            Text(
                text = "সব ফিচার আগের মতোই আছে — শুধু দেখার ধরণ bKash-এর মতো।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun BalanceChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(label, color = Color.White.copy(alpha = 0.78f), fontSize = 10.sp)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

private data class QuickAction(val label: String, val icon: ImageVector, val key: String)

@Composable
private fun QuickActionItem(action: QuickAction, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(78.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (com.mosqueseba.app.ui.theme.LocalIsDarkTheme.current)
                MaterialTheme.colorScheme.primaryContainer
            else MosqueEmeraldSoft,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = action.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MosqueEmeraldSoft,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 2.dp)
    )
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
