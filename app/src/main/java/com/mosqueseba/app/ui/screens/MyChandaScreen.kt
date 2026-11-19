package com.mosqueseba.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.window.Dialog
import com.mosqueseba.app.data.local.entity.ChandaPaymentEntity
import com.mosqueseba.app.ui.components.ChandaMonthUtils
import com.mosqueseba.app.ui.components.toBengaliDigits
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel

/**
 * সদস্যের নিজের চাঁদা — self-only ভিউ।
 *
 * নীতিমালা:
 * - শুধু session-এ সংরক্ষিত নিজের memberId দিয়ে ডেটা ফিল্টার করা হয়।
 * - memberId না পাওয়া গেলে খালি অবস্থা দেখানো হয়; অন্য সদস্যের ডেটা বা
 *   roleCode-ভিত্তিক fallback কখনো ব্যবহার করা হয় না (গোপনীয়তা রক্ষা)।
 * - বর্তমান মাসের পরিশোধ/বাকি, পরিশোধের নির্দেশনা, সর্বশেষ রসিদ ও
 *   মাসওয়ারা পেমেন্ট ইতিহাস — এই ক্রমে সাজানো।
 */
@Composable
fun MyChandaScreen(
    viewModel: MosqueViewModel,
    onOpenReceipt: (ChandaPaymentEntity) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val allPayments by viewModel.allChandaPayments.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    val isDark = LocalIsDarkTheme.current

    val savedMemberId = viewModel.sessionManager.getSavedMemberId()
    val savedMemberName = viewModel.sessionManager.getSavedMemberName()

    // কড়া self-only ফিল্টার — কোনো take()/roleCode fallback নেই।
    val ownMember = remember(allMembers, savedMemberId) {
        if (savedMemberId.isNullOrBlank()) null
        else allMembers.firstOrNull { it.id == savedMemberId }
    }
    val ownPayments = remember(allPayments, savedMemberId) {
        if (savedMemberId.isNullOrBlank()) emptyList()
        else allPayments
            .filter { it.memberId == savedMemberId }
            .sortedByDescending { it.paidAt }
    }
    val treasurer = remember(allMembers) { viewModel.getTreasurerMember() }

    val currentMonth = ChandaMonthUtils.getCurrentMonthYear()
    val monthlyChanda = ownMember?.monthlyChanda ?: 0.0
    val paidThisMonth = remember(ownPayments, currentMonth) {
        ownPayments.sumOf { ChandaMonthUtils.amountForMonth(it, currentMonth) }
    }
    val dueThisMonth = (monthlyChanda - paidThisMonth).coerceAtLeast(0.0)
    val isPaidThisMonth = monthlyChanda > 0.0 && dueThisMonth <= 0.5
    val totalPaid = ownPayments.sumOf { it.amountPaid }
    val latestPayment = ownPayments.firstOrNull()

    var showGuideDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // হেডার
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Column {
                    Text(
                        text = "আমার চাঁদা",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (savedMemberName.isNotBlank()) {
                        Text(
                            text = savedMemberName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // বর্তমান মাসের স্ট্যাটাস কার্ড
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MosqueEmeraldDark),
                border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MosqueEmeraldGradient)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "বর্তমান মাসের চাঁদা",
                                color = MosqueGoldLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isPaidThisMonth) {
                                    MosqueSuccessGreen.copy(alpha = 0.2f)
                                } else {
                                    MosqueWarningAmber.copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    text = if (isPaidThisMonth) "পরিশোধিত" else "বাকি আছে",
                                    color = if (isPaidThisMonth) Color.White else MosqueWarningAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = currentMonth,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(14.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            ChandaStat(
                                label = "নির্ধারিত",
                                value = "৳${toBengaliDigits(String.format("%,.0f", monthlyChanda))}",
                                modifier = Modifier.weight(1f)
                            )
                            ChandaStat(
                                label = "পরিশোধিত",
                                value = "৳${toBengaliDigits(String.format("%,.0f", paidThisMonth))}",
                                modifier = Modifier.weight(1f)
                            )
                            ChandaStat(
                                label = "বাকি",
                                value = "৳${toBengaliDigits(String.format("%,.0f", dueThisMonth))}",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (monthlyChanda <= 0.0) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "আপনার মাসিক চাঁদার হার এখনো নির্ধারণ করা হয়নি। কমিটির সাথে যোগাযোগ করুন।",
                                color = Color.White.copy(alpha = 0.82f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // প্রধান বাটনসমূহ
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { showGuideDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = MosqueEmeraldDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("চাঁদা পরিশোধ করুন", color = MosqueEmeraldDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { latestPayment?.let(onOpenReceipt) },
                    enabled = latestPayment != null,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = null,
                        tint = if (isDark) MosqueGoldLight else MosqueEmeraldDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "সর্বশেষ রসিদ",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // মোট পরিশোধিত সারাংশ
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "মোট পরিশোধित চাঁদা",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${ownPayments.size} টি রসিদ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "৳${toBengaliDigits(String.format("%,.0f", totalPaid))}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MosqueSuccessGreen
                    )
                }
            }
        }

        // পেমেন্ট ইতিহাস
        item {
            Text(
                text = "পেমেন্ট ইতিহাস",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (ownPayments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        1.dp,
                        if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "এখনো আপনার কোনো চাঁদা রেকর্ড পাওয়া যায়নি।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(ownPayments, key = { it.id }) { payment ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenReceipt(payment) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        1.dp,
                        if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = payment.month,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "রসিদ: ${toBengaliDigits(payment.receiptCode)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "জমার সময়: ${toBengaliDigits(payment.paidAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "৳${toBengaliDigits(String.format("%,.0f", payment.amountPaid))}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MosqueSuccessGreen
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text = "রসিদ দেখুন",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "আপনার চাঁদা কোষাধ্যক্ষ গ্রহণ করেন এবং QR-সহ ডিজিটাল রসিদ প্রদান করেন। কোনো রসিদ দেখতে না পেলে কমিটির সাথে যোগাযোগ করুন।",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }

    if (showGuideDialog) {
        ChandaPaymentGuideDialog(
            dueAmount = dueThisMonth,
            treasurerName = treasurer?.nameBn,
            treasurerMobile = treasurer?.mobile,
            onDismiss = { showGuideDialog = false }
        )
    }
}

@Composable
private fun ChandaStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp
        )
        Text(
            text = value,
            color = MosqueGold,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * সদস্যকে সৎ তথ্য দেওয়া হয় — অ্যাপে সরাসরি টাকা কাটা/জমা হয় না।
 * বাস্তব flow: কোষাধ্যক্ষের কাছে জমা দিন → তিনি হিসাব এন্ট্রি করে রসিদ দেন।
 */
@Composable
private fun ChandaPaymentGuideDialog(
    dueAmount: Double,
    treasurerName: String?,
    treasurerMobile: String?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MosqueGold.copy(alpha = 0.16f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = MosqueGold,
                            modifier = Modifier.padding(10.dp).size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "চাঁদা পরিশোধের নির্দেশনা",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "বর্তমান বাকি: ৳${toBengaliDigits(String.format("%,.0f", dueAmount))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MosqueGold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                GuideStep("১", "নগদ, ব্যাংক ট্রান্সফার বা মোবাইল ব্যাংকিংয়ে কোষাধ্যক্ষের কাছে চাঁদা জমা দিন।")
                GuideStep("২", "কোষাধ্যক্ষ হিসাব এন্ট্রি করে QR-সহ অফিশিয়াল রসিদ প্রদান করেন।")
                GuideStep("৩", "রসিদটি 'আমার চাঁদা → পেমেন্ট ইতিহাস'-এ সংরক্ষিত হয়।")

                if (!treasurerName.isNullOrBlank() || !treasurerMobile.isNullOrBlank()) {
                    Spacer(Modifier.height(14.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "কোষাধ্যক্ষের সাথে যোগাযোগ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!treasurerName.isNullOrBlank()) {
                                Text(
                                    text = treasurerName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!treasurerMobile.isNullOrBlank()) {
                                Text(
                                    text = toBengaliDigits(treasurerMobile),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("বুঝেছি", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun GuideStep(number: String, text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MosqueEmeraldPrimary.copy(alpha = 0.14f)
        ) {
            Text(
                text = number,
                color = MosqueEmeraldPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}
