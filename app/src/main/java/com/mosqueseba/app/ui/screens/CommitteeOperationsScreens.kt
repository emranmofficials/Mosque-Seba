package com.mosqueseba.app.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mosqueseba.app.data.local.entity.*
import com.mosqueseba.app.data.model.CommitteeTier
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.MehrabArchCard
import com.mosqueseba.app.ui.components.toBengaliDigits
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel
import java.util.Locale

// =========================================================================
// 1. ৩-স্তরীয় আদর্শ কমিটি সনদ ও দায়িত্ব নির্দেশিকা (Committee Charter)
// =========================================================================
@Composable
fun CommitteeCharterScreen(
    viewModel: MosqueViewModel
) {
    val currentRole by viewModel.currentUserRole.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    var selectedTier by remember { mutableStateOf<CommitteeTier?>(null) }

    val displayedTiers = if (selectedTier == null) {
        listOf(CommitteeTier.CORE_EXECUTIVE, CommitteeTier.SECRETARIAL, CommitteeTier.GENERAL_ADVISORY)
    } else {
        listOf(selectedTier!!)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MosqueBgLight),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            MehrabArchCard(
                backgroundColor = Color.White,
                borderColor = MosqueCardBorderGold
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "আদর্শ মসজিদ পরিচালনা কমিটি — দায়িত্ব সনদ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MosqueEmeraldDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "৩টি স্তরে বিভক্ত ১৩টি আনুষ্ঠানিক পদবি ও প্রতিটি দায়িত্বশীলের সুনির্দিষ্ট কর্তব্য তালিকা",
                        style = MaterialTheme.typography.bodySmall,
                        color = MosqueTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = selectedTier == null,
                                onClick = { selectedTier = null },
                                label = { Text("সকল স্তর", fontSize = 11.sp) }
                            )
                        }
                        listOf(
                            CommitteeTier.CORE_EXECUTIVE to "১. মূল কার্যনির্বাহী",
                            CommitteeTier.SECRETARIAL to "২. সম্পাদকীয়",
                            CommitteeTier.GENERAL_ADVISORY to "৩. সদস্য ও উপদেষ্টা"
                        ).forEach { (tier, label) ->
                            item {
                                FilterChip(
                                    selected = selectedTier == tier,
                                    onClick = { selectedTier = if (selectedTier == tier) null else tier },
                                    label = { Text(label, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }

        displayedTiers.forEach { tier ->
            item(key = "tier_hdr_${tier.name}") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MosqueEmeraldPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text(
                            text = tier.titleBn,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MosqueGoldLight
                        )
                        Text(
                            text = tier.subtitleBn,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            val tierRoles = UserRole.byTier(tier)
            items(tierRoles, key = { it.code }) { role ->
                val isCurrent = role == currentRole
                val assignedMembers = allMembers.filter { it.roleCode.equals(role.code, ignoreCase = true) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) MosqueGoldSoft else Color.White
                    ),
                    border = BorderStroke(
                        width = if (isCurrent) 1.5.dp else 1.dp,
                        color = if (isCurrent) MosqueGoldDeep else MosqueCardBorderSubtle
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Text(text = role.emoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = role.titleBn,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueEmeraldDark
                                    )
                                    val holderText = if (assignedMembers.isEmpty()) {
                                        "দায়িত্বপ্রাপ্ত: নির্ধারিত নয়"
                                    } else {
                                        "দায়িত্বপ্রাপ্ত: ${assignedMembers.joinToString(", ") { it.nameBn }}"
                                    }
                                    Text(
                                        text = holderText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MosqueEmeraldPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            if (isCurrent) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MosqueGold
                                ) {
                                    Text(
                                        text = "আপনার পদবি",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueEmeraldDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MosqueCardBorder.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(8.dp))

                        role.dutiesBn.forEachIndexed { idx, duty ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${toBengaliDigits((idx + 1).toString())}.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueGoldDeep,
                                    modifier = Modifier.width(20.dp)
                                )
                                Text(
                                    text = duty,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueTextPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 1.1 ড্যাশবোর্ড থেকে দ্রুত "আমার দায়িত্ব তালিকা" ডায়ালগ
// =========================================================================
@Composable
fun MyRoleDutiesDialog(
    role: UserRole,
    onOpenFullCharter: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = role.emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${role.titleBn} — সুনির্দিষ্ট দায়িত্ব",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MosqueEmeraldDark
                        )
                        Text(
                            text = role.tier.titleBn,
                            style = MaterialTheme.typography.labelSmall,
                            color = MosqueGoldDeep,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MosqueCardBorderGold)
                Spacer(modifier = Modifier.height(10.dp))

                role.dutiesBn.forEach { duty ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MosqueEmeraldPrimary,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = duty,
                            style = MaterialTheme.typography.bodySmall,
                            color = MosqueTextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenFullCharter()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MosqueEmeraldPrimary)
                    ) {
                        Text("পূর্ণ সনদ দেখুন", fontSize = 12.sp, color = MosqueEmeraldPrimary)
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Text("বুঝেছি", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// =========================================================================
// 2. দান বাক্স (ক্যাশবক্স) খোলা, সাক্ষী ও ব্যাংক জমা রেজিস্টার
// =========================================================================
@Composable
fun CashboxScreen(
    viewModel: MosqueViewModel
) {
    val records by viewModel.allCashboxRecords.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val canOpenBox = currentRole.canManageCashbox()
    val canWitness = currentRole.canWitnessCashbox()

    var showOpenCashboxDialog by remember { mutableStateOf(false) }
    var depositTargetRecord by remember { mutableStateOf<CashboxRecordEntity?>(null) }

    val totalCounted = records.sumOf { it.countedAmount }
    val depositedTotal = records.filter { it.depositStatus == "bank_deposited" }.sumOf { it.countedAmount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MosqueBgLight),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPrimary)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "দান বাক্স (ক্যাশবক্স) গণনা ও ব্যাংক জমা",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "মোট গণনা: ৳${String.format(Locale.getDefault(), "%,.0f", totalCounted)}",
                                color = MosqueGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "ব্যাংকে জমা সম্পন্ন: ৳${String.format(Locale.getDefault(), "%,.0f", depositedTotal)}",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                        }
                        if (canOpenBox) {
                            Button(
                                onClick = { showOpenCashboxDialog = true },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("open_cashbox_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MosqueGold),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("+ বাক্স গণনা", color = MosqueEmeraldDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        if (records.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Inventory2,
                    message = "এখনো কোনো দান বাক্স খোলার রেকর্ড যুক্ত করা হয়নি।"
                )
            }
        } else {
            items(records, key = { it.id }) { rec ->
                val statusLabel = when (rec.depositStatus) {
                    "bank_deposited" -> "ব্যাংকে জমা সম্পন্ন"
                    "witness_verified" -> "সাক্ষী যাচাইকৃত (জমা বাকি)"
                    else -> "গণনা সম্পন্ন (সাক্ষ্য বাকি)"
                }
                val statusColor = when (rec.depositStatus) {
                    "bank_deposited" -> MosqueSuccessGreen
                    "witness_verified" -> MosqueInfoBlue
                    else -> MosqueWarningAmber
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MosqueCardBorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rec.boxType,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueEmeraldDark
                                )
                                Text(
                                    text = "তারিখ: ${rec.collectionDate} • গণনা: ${rec.countedBy}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MosqueTextSecondary
                                )
                            }
                            Text(
                                text = "৳${String.format(Locale.getDefault(), "%,.0f", rec.countedAmount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MosqueEmeraldPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = statusColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = statusLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "উপস্থিত সাক্ষী: ${rec.witnessNames}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MosqueTextPrimary
                        )
                        if (rec.verifiedBy != null) {
                            Text(
                                text = "ডিজিটাল সাক্ষ্য নিশ্চিতকারী: ${rec.verifiedBy}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MosqueSuccessGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (rec.bankName.isNotBlank()) {
                            Text(
                                text = "ব্যাংক: ${rec.bankName} (স্লিপ: ${rec.depositSlipNo})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MosqueInfoBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (rec.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = rec.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MosqueTextMuted
                            )
                        }

                        if ((!rec.witnessVerified && canWitness) || (rec.depositStatus != "bank_deposited" && canOpenBox)) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!rec.witnessVerified && canWitness) {
                                    OutlinedButton(
                                        onClick = { viewModel.verifyCashboxAsWitness(rec) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("verify_cashbox_witness_${rec.id}"),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, MosqueInfoBlue)
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp), tint = MosqueInfoBlue)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("সাক্ষী নিশ্চিত করুন", fontSize = 11.sp, color = MosqueInfoBlue, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (rec.depositStatus != "bank_deposited" && canOpenBox) {
                                    Button(
                                        onClick = { depositTargetRecord = rec },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("deposit_cashbox_bank_${rec.id}"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                    ) {
                                        Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ব্যাংকে জমা দিন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showOpenCashboxDialog) {
        OpenCashboxDialog(
            onDismiss = { showOpenCashboxDialog = false },
            onConfirm = { boxType, amount, witnesses, bank, slip, notes ->
                viewModel.recordCashboxOpening(boxType, amount, witnesses, bank, slip, notes)
                showOpenCashboxDialog = false
            }
        )
    }

    depositTargetRecord?.let { target ->
        BankDepositDialog(
            record = target,
            onDismiss = { depositTargetRecord = null },
            onConfirm = { bankName, slipNo ->
                viewModel.markCashboxBankDeposited(target, bankName, slipNo)
                depositTargetRecord = null
            }
        )
    }
}

@Composable
private fun OpenCashboxDialog(
    onDismiss: () -> Unit,
    onConfirm: (boxType: String, amount: Double, witnesses: String, bank: String, slip: String, notes: String) -> Unit
) {
    var boxType by remember { mutableStateOf("সাপ্তাহিক জুমার দান বাক্স") }
    var amount by remember { mutableStateOf("") }
    var witnesses by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var slipNo by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "দান বাক্স (ক্যাশবক্স) গণনা এন্ট্রি",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("সাপ্তাহিক জুমার দান বাক্স", "মাসিক সিন্দুক ও দান বাক্স").forEach { opt ->
                        FilterChip(
                            selected = boxType == opt,
                            onClick = { boxType = opt },
                            label = { Text(opt, fontSize = 10.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("গণনাকৃত মোট টাকা (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = witnesses,
                    onValueChange = { witnesses = it },
                    label = { Text("উপস্থিত সাক্ষী সদস্যদের নাম *") },
                    placeholder = { Text("যেমন: ডা: তারিকুল ইসলাম, মো: আসাদুজ্জামান") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("ব্যাংকের নাম (সাথে সাথে জমা হলে)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = slipNo,
                    onValueChange = { slipNo = it },
                    label = { Text("জমা স্লিপ নম্বর (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("মন্তব্য / নোট") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (amt > 0 && witnesses.isNotBlank()) {
                                onConfirm(boxType, amt, witnesses, bankName, slipNo, notes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Text("সংরক্ষণ করুন")
                    }
                }
            }
        }
    }
}

@Composable
private fun BankDepositDialog(
    record: CashboxRecordEntity,
    onDismiss: () -> Unit,
    onConfirm: (bankName: String, slipNo: String) -> Unit
) {
    var bankName by remember { mutableStateOf("ইসলামী ব্যাংক বাংলাদেশ পিএলসি") }
    var slipNo by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "ব্যাংকে জমা নিশ্চিতকরণ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )
                Text(
                    text = "${record.boxType} • ৳${record.countedAmount.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MosqueEmeraldPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("ব্যাংক ও শাখার নাম *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = slipNo,
                    onValueChange = { slipNo = it },
                    label = { Text("জমা স্লিপ / রেফারেন্স নম্বর *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (bankName.isNotBlank() && slipNo.isNotBlank()) {
                                onConfirm(bankName, slipNo)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Text("জমা সম্পন্ন")
                    }
                }
            }
        }
    }
}

// =========================================================================
// 3. দপ্তর, দলিল-চুক্তি ও চিঠিপত্র রেজিস্টার (দপ্তর সম্পাদক)
// =========================================================================
@Composable
fun OfficeDocumentsScreen(
    viewModel: MosqueViewModel
) {
    val documents by viewModel.allOfficeDocuments.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val canManageDocs = currentRole.canManageOfficeDocs()

    var selectedCategory by remember { mutableStateOf("সকল") }
    var showAddDocDialog by remember { mutableStateOf(false) }
    var replyTargetDoc by remember { mutableStateOf<OfficeDocumentEntity?>(null) }

    val categories = listOf("সকল", "দলিল", "চুক্তি", "আগত চিঠি", "প্রেরিত চিঠি", "অফিস ফাইল")
    val filteredDocs = if (selectedCategory == "সকল") documents else documents.filter { it.category == selectedCategory }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MosqueBgLight),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPrimary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "দপ্তর, দলিল-চুক্তি ও চিঠিপত্র ফাইল",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${toBengaliDigits(documents.size.toString())} টি নথি সংরক্ষিত",
                            color = MosqueGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    if (canManageDocs) {
                        Button(
                            onClick = { showAddDocDialog = true },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("add_office_doc_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MosqueGold),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ নতুন নথি", color = MosqueEmeraldDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 11.sp) }
                    )
                }
            }
        }

        if (filteredDocs.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.FolderOpen,
                    message = "এই শ্রেণিতে কোনো দাপ্তরিক নথি বা চিঠি পাওয়া যায়নি।"
                )
            }
        } else {
            items(filteredDocs, key = { it.id }) { doc ->
                val statusColor = when (doc.status) {
                    "জবাব অপেক্ষমান" -> MosqueWarningAmber
                    "জবাব প্রেরিত" -> MosqueInfoBlue
                    else -> MosqueSuccessGreen
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MosqueCardBorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MosqueEmeraldSoft
                            ) {
                                Text(
                                    text = "${doc.category} • ${doc.docCode}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueEmeraldDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = statusColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = doc.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = doc.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MosqueTextPrimary
                        )
                        Text(
                            text = "পক্ষ/প্রেরক: ${doc.counterparty} • তারিখ: ${doc.docDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MosqueTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = doc.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MosqueTextPrimary
                        )
                        if (doc.replyNote.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MosqueBgLight,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "জবাব / নোট: ${doc.replyNote}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueInfoBlue,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        if (canManageDocs) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { replyTargetDoc = doc },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MosqueEmeraldPrimary)
                            ) {
                                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp), tint = MosqueEmeraldPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("জবাব লিখুন / ফাইল হালনাগাদ", fontSize = 12.sp, color = MosqueEmeraldPrimary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDocDialog) {
        AddOfficeDocumentDialog(
            onDismiss = { showAddDocDialog = false },
            onConfirm = { title, cat, party, summary, status ->
                viewModel.addOfficeDocument(title, cat, party, summary, status)
                showAddDocDialog = false
            }
        )
    }

    replyTargetDoc?.let { doc ->
        ReplyOfficeDocumentDialog(
            doc = doc,
            onDismiss = { replyTargetDoc = null },
            onConfirm = { replyNote, status ->
                viewModel.updateOfficeDocumentReply(doc, replyNote, status)
                replyTargetDoc = null
            }
        )
    }
}

@Composable
private fun AddOfficeDocumentDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, counterparty: String, summary: String, status: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("আগত চিঠি") }
    var counterparty by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("জবাব অপেক্ষমান") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "নতুন নথি / চিঠি নিবন্ধন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("দলিল", "চুক্তি", "আগত চিঠি", "প্রেরিত চিঠি", "অফিস ফাইল")) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = {
                                category = cat
                                status = if (cat == "আগত চিঠি") "জবাব অপেক্ষমান" else "সংরক্ষিত"
                            },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("নথি বা চিঠির শিরোনাম *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = counterparty,
                    onValueChange = { counterparty = it },
                    label = { Text("প্রেরক / প্রাপক / সংশ্লিষ্ট পক্ষ *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("সংক্ষিপ্ত বিবরণ *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && counterparty.isNotBlank()) {
                                onConfirm(title, category, counterparty, summary, status)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Text("সংরক্ষণ করুন")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplyOfficeDocumentDialog(
    doc: OfficeDocumentEntity,
    onDismiss: () -> Unit,
    onConfirm: (replyNote: String, status: String) -> Unit
) {
    var replyNote by remember { mutableStateOf(doc.replyNote) }
    var status by remember { mutableStateOf(doc.status) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "নথি জবাব ও স্ট্যাটাস হালনাগাদ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )
                Text(text = doc.title, style = MaterialTheme.typography.bodySmall, color = MosqueTextSecondary)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("সংরক্ষিত", "জবাব অপেক্ষমান", "জবাব প্রেরিত").forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st, fontSize = 10.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = replyNote,
                    onValueChange = { replyNote = it },
                    label = { Text("জবাবের খসড়া / দাপ্তরিক নোট") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(replyNote, status) },
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Text("হালনাগাদ করুন")
                    }
                }
            }
        }
    }
}

// =========================================================================
// 4. শরিয়াহ পর্যালোচনা (ইমাম/খতিব) ও উপদেষ্টা পরামর্শ প্যানেল
// =========================================================================
@Composable
fun ShariaAdvisoryScreen(
    viewModel: MosqueViewModel
) {
    val notes by viewModel.allAdvisoryNotes.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val canSubmitSharia = currentRole.canSubmitShariaReview()
    val canSubmitAdvice = currentRole.canSubmitAdvisoryGuidance()
    val canAcknowledge = currentRole == UserRole.PRESIDENT || currentRole == UserRole.VICE_PRESIDENT || currentRole == UserRole.GENERAL_SECRETARY

    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Sharia, 2: Advisory
    var showAddDialog by remember { mutableStateOf<String?>(null) } // "SHARIA_REVIEW" or "ADVISORY_GUIDANCE"

    val filtered = when (selectedTab) {
        1 -> notes.filter { it.noteType == "SHARIA_REVIEW" }
        2 -> notes.filter { it.noteType == "ADVISORY_GUIDANCE" }
        else -> notes
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MosqueBgLight),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPrimary)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "শরিয়াহ সম্মতি পর্যালোচনা ও উপদেষ্টা পরিষদ",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "পদাধিকারবলে ইমাম/খতিবের শরিয়াহ মতামত এবং উপদেষ্টা পরিষদের সংকট ও সম্প্রসারণ নির্দেশনা",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp
                    )
                    if (canSubmitSharia || canSubmitAdvice) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                showAddDialog = if (canSubmitSharia) "SHARIA_REVIEW" else "ADVISORY_GUIDANCE"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("add_advisory_note_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MosqueGold),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (canSubmitSharia) "+ শরিয়াহ পর্যবেক্ষণ দাখিল করুন" else "+ উপদেষ্টা পরামর্শ প্রদান করুন",
                                color = MosqueEmeraldDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = selectedTab == 0, onClick = { selectedTab = 0 }, label = { Text("সকল", fontSize = 11.sp) })
                FilterChip(selected = selectedTab == 1, onClick = { selectedTab = 1 }, label = { Text("শরিয়াহ পর্যালোচনা", fontSize = 11.sp) })
                FilterChip(selected = selectedTab == 2, onClick = { selectedTab = 2 }, label = { Text("উপদেষ্টা পরামর্শ", fontSize = 11.sp) })
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.MenuBook,
                    message = "কোনো শরিয়াহ পর্যালোচনা বা উপদেষ্টা পরামর্শ পাওয়া যায়নি।"
                )
            }
        } else {
            items(filtered, key = { it.id }) { note ->
                val isSharia = note.noteType == "SHARIA_REVIEW"
                MehrabArchCard(
                    backgroundColor = Color.White,
                    borderColor = if (isSharia) MosqueEmeraldPrimary.copy(alpha = 0.4f) else MosqueGoldDeep.copy(alpha = 0.45f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSharia) MosqueEmeraldSoft else MosqueGoldLight
                            ) {
                                Text(
                                    text = note.shariaCompliance,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueEmeraldDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = note.status,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (note.status == "বাস্তবায়িত" || note.status == "গৃহীত") MosqueSuccessGreen else MosqueWarningAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = note.topic,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MosqueEmeraldDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = note.recommendation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MosqueTextPrimary,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "প্রদানকারী: ${note.authorName} (${note.authorRoleTitle}) • ${note.createdDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MosqueTextMuted
                        )

                        if (canAcknowledge && note.status == "পর্যালোচনাধীন") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.updateAdvisoryNoteStatus(note, "গৃহীত") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("গৃহীত চিহ্নিত করুন", fontSize = 11.sp, color = MosqueEmeraldPrimary)
                                }
                                Button(
                                    onClick = { viewModel.updateAdvisoryNoteStatus(note, "বাস্তবায়িত") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                ) {
                                    Text("বাস্তবায়িত", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    showAddDialog?.let { noteType ->
        AddAdvisoryNoteDialog(
            noteType = noteType,
            onDismiss = { showAddDialog = null },
            onConfirm = { topic, rec, compliance ->
                viewModel.submitAdvisoryNote(noteType, topic, rec, compliance)
                showAddDialog = null
            }
        )
    }
}

@Composable
private fun AddAdvisoryNoteDialog(
    noteType: String,
    onDismiss: () -> Unit,
    onConfirm: (topic: String, recommendation: String, compliance: String) -> Unit
) {
    val isSharia = noteType == "SHARIA_REVIEW"
    var topic by remember { mutableStateOf("") }
    var recommendation by remember { mutableStateOf("") }
    var compliance by remember { mutableStateOf(if (isSharia) "শরিয়াহ সম্মত" else "কৌশলগত পরামর্শ") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (isSharia) "শরিয়াহ সম্মতি পর্যবেক্ষণ ফরম" else "উপদেষ্টা পরিষদের পরামর্শ ফরম",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val options = if (isSharia) listOf("শরিয়াহ সম্মত", "সংশোধনী প্রয়োজন") else listOf("কৌশলগত পরামর্শ", "সংকট নিরসন")
                    options.forEach { opt ->
                        FilterChip(
                            selected = compliance == opt,
                            onClick = { compliance = opt },
                            label = { Text(opt, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("বিষয় / প্রকল্প / সিদ্ধান্তের নাম *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = recommendation,
                    onValueChange = { recommendation = it },
                    label = { Text(if (isSharia) "শরিয়াহ দলিল ও সুপারিশ *" else "দিকনির্দেশনা ও পরামর্শ *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (topic.isNotBlank() && recommendation.isNotBlank()) {
                                onConfirm(topic, recommendation, compliance)
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

// =========================================================================
// 5. প্রচার, দেয়াল উক্তি ও স্মার্ট বোর্ড পরিচালনা (প্রচার ও প্রকাশনা সম্পাদক)
// =========================================================================
@Composable
fun PublicitySmartBoardScreen(
    viewModel: MosqueViewModel
) {
    val quotes by viewModel.allSmartBoardQuotes.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val canManage = currentRole.canManageSmartBoard()

    var showAddQuoteDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MosqueBgLight),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPrimary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "দেয়াল উক্তি ও স্মার্ট নোটিশ বোর্ড প্রচার",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${toBengaliDigits(quotes.size.toString())} টি প্রচার বার্তা",
                            color = MosqueGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    if (canManage) {
                        Button(
                            onClick = { showAddQuoteDialog = true },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("add_smart_quote_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MosqueGold),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ নতুন প্রচার", color = MosqueEmeraldDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (quotes.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.FormatQuote,
                    message = "স্মার্ট বোর্ডের জন্য কোনো দেয়াল উক্তি বা প্রচার বার্তা নেই।"
                )
            }
        } else {
            items(quotes, key = { it.id }) { quote ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (quote.isPinned) MosqueGoldSoft else Color.White
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (quote.isPinned) MosqueGoldDeep.copy(alpha = 0.5f) else MosqueCardBorderSubtle
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MosqueEmeraldSoft
                            ) {
                                Text(
                                    text = quote.category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueEmeraldDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            if (canManage) {
                                Row {
                                    IconButton(onClick = { viewModel.togglePinSmartBoardQuote(quote) }) {
                                        Icon(
                                            imageVector = if (quote.isPinned) Icons.Default.PushPin else Icons.Default.OutlinedFlag,
                                            contentDescription = "Pin to Smart Board",
                                            tint = if (quote.isPinned) MosqueGoldDeep else MosqueTextMuted
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deleteSmartBoardQuote(quote) }) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Quote",
                                            tint = MosqueUrgentRed
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = quote.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MosqueEmeraldDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = quote.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MosqueTextPrimary,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "প্রচারকারী: ${quote.createdBy}" + if (quote.isPinned) " • ড্যাশবোর্ডে প্রদর্শিত" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MosqueTextMuted
                        )
                    }
                }
            }
        }
    }

    if (showAddQuoteDialog) {
        AddSmartQuoteDialog(
            onDismiss = { showAddQuoteDialog = false },
            onConfirm = { cat, title, msg, pinned ->
                viewModel.addSmartBoardQuote(cat, title, msg, pinned)
                showAddQuoteDialog = false
            }
        )
    }
}

@Composable
private fun AddSmartQuoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: String, title: String, message: String, isPinned: Boolean) -> Unit
) {
    var category by remember { mutableStateOf("বিশেষ ধর্মীয় দিবস") }
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "স্মার্ট বোর্ড ও দেয়াল প্রচারণা যোগ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("দেয়াল উক্তি", "জুমার নির্দেশনা", "বিশেষ ধর্মীয় দিবস")) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("শিরোনাম (যেমন: শবে কদর / দেয়াল বাণী) *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("বিস্তারিত বার্তা / উক্তি *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isPinned = !isPinned }
                ) {
                    Checkbox(checked = isPinned, onCheckedChange = { isPinned = it })
                    Text("ড্যাশবোর্ড স্মার্ট বোর্ডে পিন করে দেখান", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && message.isNotBlank()) {
                                onConfirm(category, title, message, isPinned)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Text("প্রকাশ করুন")
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MosqueTextMuted, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, style = MaterialTheme.typography.bodySmall, color = MosqueTextMuted)
        }
    }
}
