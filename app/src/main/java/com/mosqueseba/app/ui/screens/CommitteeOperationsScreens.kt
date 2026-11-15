package com.mosqueseba.app.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mosqueseba.app.data.local.entity.*
import com.mosqueseba.app.data.model.CommitteeTier
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.ExecutiveFormDialogShell
import com.mosqueseba.app.ui.components.MehrabArchCard
import com.mosqueseba.app.ui.components.MoreFormSection
import com.mosqueseba.app.ui.components.MosqueConfirmDialog
import com.mosqueseba.app.ui.components.roleIcon
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
    val isDark = LocalIsDarkTheme.current
    val cardBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle

    val displayedTiers = if (selectedTier == null) {
        listOf(CommitteeTier.CORE_EXECUTIVE, CommitteeTier.SECRETARIAL, CommitteeTier.GENERAL_ADVISORY)
    } else {
        listOf(selectedTier!!)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            MehrabArchCard(
                backgroundColor = MaterialTheme.colorScheme.surface,
                borderColor = MosqueCardBorderGold
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "আদর্শ মসজিদ পরিচালনা কমিটি — দায়িত্ব সনদ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "৩টি স্তরে বিভক্ত ১৩টি আনুষ্ঠানিক পদবি ও প্রতিটি দায়িত্বশীলের সুনির্দিষ্ট কর্তব্য তালিকা",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = selectedTier == null,
                                onClick = { selectedTier = null },
                                label = { Text("সকল স্তর (${toBengaliDigits("13")})", fontSize = 11.sp) },
                                modifier = Modifier.testTag("charter_filter_all"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MosqueEmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
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
                                    label = { Text(label, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("charter_filter_${tier.name}"),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MosqueEmeraldPrimary,
                                        selectedLabelColor = Color.White
                                    )
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
                val roleCardBg = if (isCurrent) {
                    if (isDark) MosqueNightSurfaceVariant else MosqueGoldSoft
                } else {
                    MaterialTheme.colorScheme.surface
                }
                val iconCircleBg = if (isDark) MosqueEmeraldPrimary.copy(alpha = 0.2f) else MosqueEmeraldSoft
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = roleCardBg),
                    border = BorderStroke(
                        width = if (isCurrent) 1.5.dp else 1.dp,
                        color = if (isCurrent) MosqueGoldDeep else cardBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = CircleShape,
                                    color = iconCircleBg,
                                    border = BorderStroke(1.dp, MosqueCardBorderGold),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = role.roleIcon(),
                                            contentDescription = role.titleBn,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = role.titleBn,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val holderText = if (assignedMembers.isEmpty()) {
                                        "দায়িত্বপ্রাপ্ত: নির্ধারিত নয়"
                                    } else {
                                        "দায়িত্বপ্রাপ্ত: ${assignedMembers.joinToString(", ") { it.nameBn }}"
                                    }
                                    Text(
                                        text = holderText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            if (isCurrent) {
                                Spacer(modifier = Modifier.width(6.dp))
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
                        HorizontalDivider(color = cardBorder)
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
                                    color = if (isDark) MosqueGold else MosqueGoldDeep,
                                    modifier = Modifier.width(20.dp)
                                )
                                Text(
                                    text = duty,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
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
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MehrabArchCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            backgroundColor = MaterialTheme.colorScheme.surface,
            borderColor = MosqueCardBorderGold,
            elevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MosqueEmeraldDarkGradient)
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MosqueGold.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.7f)),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = role.roleIcon(),
                                    contentDescription = null,
                                    tint = MosqueGoldLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${role.titleBn} — সুনির্দিষ্ট দায়িত্ব",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = role.tier.titleBn,
                                style = MaterialTheme.typography.labelSmall,
                                color = MosqueGoldLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .background(IslamicGoldTrimGradient)
                )
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .heightIn(max = 460.dp)
                        .verticalScroll(rememberScrollState())
                ) {

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
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = duty,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
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
                            .heightIn(min = 48.dp)
                            .testTag("duties_dialog_charter_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text("পূর্ণ সনদ দেখুন", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("duties_dialog_close_button"),
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
}

// =========================================================================
// 2. দান বাক্স (ক্যাশবক্স) খোলা, সাক্ষী ও ব্যাংক জমা রেজিস্টার
// =========================================================================
@Composable
fun CashboxScreen(
    viewModel: MosqueViewModel
) {
    val context = LocalContext.current
    val records by viewModel.allCashboxRecords.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val canOpenBox = currentRole.canManageCashbox()
    val canWitness = currentRole.canWitnessCashbox()
    val isDark = LocalIsDarkTheme.current
    val cardBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle

    var statusFilter by remember { mutableStateOf("সকল") }
    var showOpenCashboxDialog by remember { mutableStateOf(false) }
    var depositTargetRecord by remember { mutableStateOf<CashboxRecordEntity?>(null) }

    val totalCounted = records.sumOf { it.countedAmount }
    val depositedTotal = records.filter { it.depositStatus == "bank_deposited" }.sumOf { it.countedAmount }
    val pendingWitnessCount = records.count { !it.witnessVerified }
    val filteredRecords = remember(records, statusFilter) {
        when (statusFilter) {
            "সাক্ষ্য বাকি" -> records.filter { !it.witnessVerified }
            "ব্যাংক জমা বাকি" -> records.filter { it.witnessVerified && it.depositStatus != "bank_deposited" }
            "সম্পন্ন" -> records.filter { it.depositStatus == "bank_deposited" }
            else -> records
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
                                text = "মোট গণনা: ৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", totalCounted))}",
                                color = MosqueGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "ব্যাংকে জমা সম্পন্ন: ৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", depositedTotal))}",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                            if (pendingWitnessCount > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MosqueWarningAmber.copy(alpha = 0.22f)
                                ) {
                                    Text(
                                        text = "সাক্ষ্য যাচাই বাকি: ${toBengaliDigits(pendingWitnessCount.toString())} টি বাক্স",
                                        color = MosqueGoldLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
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

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(listOf("সকল", "সাক্ষ্য বাকি", "ব্যাংক জমা বাকি", "সম্পন্ন")) { filter ->
                    FilterChip(
                        selected = statusFilter == filter,
                        onClick = { statusFilter = filter },
                        label = { Text(filter, fontSize = 11.sp) }
                    )
                }
            }
        }

        if (filteredRecords.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Inventory2,
                    message = "এই শ্রেণিতে কোনো দান বাক্স খোলার রেকর্ড পাওয়া যায়নি।"
                )
            }
        } else {
            items(filteredRecords, key = { it.id }) { rec ->
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, cardBorder)
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
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "তারিখ: ${toBengaliDigits(rec.collectionDate)} • গণনা: ${rec.countedBy}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", rec.countedAmount))}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = statusColor.copy(alpha = 0.14f)
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
                            color = MaterialTheme.colorScheme.onSurface
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
                                text = "ব্যাংক: ${rec.bankName} (স্লিপ: ${toBengaliDigits(rec.depositSlipNo)})",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) MosqueGoldLight else MosqueInfoBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (rec.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = rec.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                        onClick = {
                                            viewModel.verifyCashboxAsWitness(rec)
                                            MosqueToastController.success("সাক্ষী হিসেবে যাচাই নিশ্চিত হয়েছে।", "যাচাই সম্পন্ন")
                                        },
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
                MosqueToastController.success(
                    "৳${toBengaliDigits(amount.toInt().toString())} দান বাক্স গণনা সংরক্ষিত হয়েছে।",
                    "ক্যাশবক্স গণনা সম্পন্ন"
                )
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
                MosqueToastController.success("$bankName-এ জমা সম্পন্ন হিসেবে রেকর্ড হয়েছে।", "ব্যাংক জমা সফল")
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
    var amountError by remember { mutableStateOf<String?>(null) }
    var witnessError by remember { mutableStateOf<String?>(null) }

    ExecutiveFormDialogShell(
        title = "দান বাক্স (ক্যাশবক্স) গণনা এন্ট্রি",
        subtitle = "স্বচ্ছ গণনা, সাক্ষী তালিকা ও ব্যাংক জমা তথ্য",
        icon = Icons.Default.Inventory2,
        confirmText = "সংরক্ষণ করুন",
        confirmTag = "confirm_open_cashbox_button",
        onDismiss = onDismiss,
        onConfirmClick = {
            var valid = true
            val amt = amount.toDoubleOrNull() ?: 0.0
            if (amt <= 0.0) {
                amountError = "গণনাকৃত সঠিক টাকার পরিমাণ লিখুন"
                valid = false
            }
            if (witnesses.isBlank()) {
                witnessError = "উপস্থিত সাক্ষীদের নাম লিখুন"
                valid = false
            }
            if (valid) {
                onConfirm(boxType, amt, witnesses.trim(), bankName.trim(), slipNo.trim(), notes.trim())
            }
        }
    ) {
        MoreFormSection(title = "বাক্সের ধরন ও গণনাকৃত অর্থ", icon = Icons.Default.Payments) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(listOf("সাপ্তাহিক জুমার দান বাক্স", "মাসিক সিন্দুক ও দান বাক্স")) { opt ->
                    FilterChip(
                        selected = boxType == opt,
                        onClick = { boxType = opt },
                        label = { Text(opt, fontSize = 11.sp) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { ch -> ch.isDigit() }; amountError = null },
                label = { Text("গণনাকৃত মোট টাকা (৳) *") },
                isError = amountError != null,
                supportingText = amountError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = witnesses,
                onValueChange = { witnesses = it; witnessError = null },
                label = { Text("উপস্থিত সাক্ষী সদস্যদের নাম *") },
                placeholder = { Text("যেমন: ডা: তারিকুল ইসলাম, মো: আসাদুজ্জামান") },
                isError = witnessError != null,
                supportingText = witnessError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        MoreFormSection(title = "ব্যাংক ও নোট (ঐচ্ছিক)", icon = Icons.Default.AccountBalance) {
            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("ব্যাংকের নাম (সাথে সাথে জমা হলে)") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = slipNo,
                onValueChange = { slipNo = it },
                label = { Text("জমা স্লিপ নম্বর (ঐচ্ছিক)") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("মন্তব্য / নোট") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
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
    var bankError by remember { mutableStateOf<String?>(null) }
    var slipError by remember { mutableStateOf<String?>(null) }

    ExecutiveFormDialogShell(
        title = "ব্যাংকে জমা নিশ্চিতকরণ",
        subtitle = "${record.boxType} • ৳${toBengaliDigits(record.countedAmount.toInt().toString())}",
        icon = Icons.Default.AccountBalance,
        confirmText = "জমা সম্পন্ন",
        confirmTag = "confirm_bank_deposit_button",
        onDismiss = onDismiss,
        onConfirmClick = {
            var valid = true
            if (bankName.isBlank()) {
                bankError = "ব্যাংক ও শাখার নাম লিখুন"
                valid = false
            }
            if (slipNo.isBlank()) {
                slipError = "জমা স্লিপ / রেফারেন্স নম্বর লিখুন"
                valid = false
            }
            if (valid) {
                onConfirm(bankName.trim(), slipNo.trim())
            }
        }
    ) {
        MoreFormSection(title = "ব্যাংক জমার তথ্য", icon = Icons.Default.ReceiptLong) {
            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it; bankError = null },
                label = { Text("ব্যাংক ও শাখার নাম *") },
                isError = bankError != null,
                supportingText = bankError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = slipNo,
                onValueChange = { slipNo = it; slipError = null },
                label = { Text("জমা স্লিপ / রেফারেন্স নম্বর *") },
                isError = slipError != null,
                supportingText = slipError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
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
    val context = LocalContext.current
    val documents by viewModel.allOfficeDocuments.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val canManageDocs = currentRole.canManageOfficeDocs()
    val isDark = LocalIsDarkTheme.current
    val cardBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
    val replyBg = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight
    val badgeBg = if (isDark) MosqueEmeraldPrimary.copy(alpha = 0.22f) else MosqueEmeraldSoft
    val badgeTextColor = if (isDark) MosqueGoldLight else MosqueEmeraldDark

    var selectedCategory by remember { mutableStateOf("সকল") }
    var onlyPendingReply by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDocDialog by remember { mutableStateOf(false) }
    var replyTargetDoc by remember { mutableStateOf<OfficeDocumentEntity?>(null) }

    val pendingCount = remember(documents) { documents.count { it.status == "জবাব অপেক্ষমান" } }
    val categories = listOf("সকল", "দলিল", "চুক্তি", "আগত চিঠি", "প্রেরিত চিঠি", "অফিস ফাইল")
    val filteredDocs = remember(documents, selectedCategory, onlyPendingReply, searchQuery) {
        val q = searchQuery.trim().lowercase()
        documents.filter { doc ->
            val matchesCat = selectedCategory == "সকল" || doc.category == selectedCategory
            val matchesPending = !onlyPendingReply || doc.status == "জবাব অপেক্ষমান"
            val matchesQuery = q.isEmpty() ||
                doc.title.lowercase().contains(q) ||
                doc.counterparty.lowercase().contains(q) ||
                doc.docCode.lowercase().contains(q) ||
                doc.summary.lowercase().contains(q)
            matchesCat && matchesPending && matchesQuery
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
                        if (pendingCount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MosqueWarningAmber.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = "জবাব অপেক্ষমান: ${toBengaliDigits(pendingCount.toString())} টি",
                                    color = MosqueGoldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("শিরোনাম, নথি কোড বা প্রেরক খুঁজুন...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "খুঁজুন")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "অনুসন্ধান মুছুন")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("office_doc_search_field")
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = onlyPendingReply,
                        onClick = { onlyPendingReply = !onlyPendingReply },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.MarkEmailUnread,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                        },
                        label = { Text("জবাব বাকি (${toBengaliDigits(pendingCount.toString())})", fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_pending_office_docs_chip")
                    )
                }
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = badgeBg
                            ) {
                                Text(
                                    text = "${doc.category} • ${toBengaliDigits(doc.docCode)}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeTextColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = statusColor.copy(alpha = 0.14f)
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "পক্ষ/প্রেরক: ${doc.counterparty} • তারিখ: ${toBengaliDigits(doc.docDate)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = doc.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (doc.replyNote.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = replyBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "জবাব / নোট: ${doc.replyNote}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) MosqueGoldLight else MosqueInfoBlue,
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
                                    .heightIn(min = 48.dp)
                                    .testTag("reply_office_doc_${doc.id}"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("জবাব লিখুন / ফাইল হালনাগাদ", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
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
                MosqueToastController.success("নতুন দাপ্তরিক নথি সংরক্ষিত হয়েছে।", "নথি সংরক্ষিত")
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
                MosqueToastController.success("নথির স্ট্যাটাস ও জবাব হালনাগাদ হয়েছে।", "জবাব সংরক্ষিত")
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
    var titleError by remember { mutableStateOf<String?>(null) }
    var partyError by remember { mutableStateOf<String?>(null) }
    var summaryError by remember { mutableStateOf<String?>(null) }

    ExecutiveFormDialogShell(
        title = "নতুন নথি / চিঠি নিবন্ধন",
        subtitle = "দলিল, চুক্তি ও চিঠিপত্র দপ্তর রেজিস্টার",
        icon = Icons.Default.FolderShared,
        confirmText = "সংরক্ষণ করুন",
        confirmTag = "confirm_add_office_doc_button",
        onDismiss = onDismiss,
        onConfirmClick = {
            var valid = true
            if (title.isBlank()) {
                titleError = "নথি বা চিঠির শিরোনাম লিখুন"
                valid = false
            }
            if (counterparty.isBlank()) {
                partyError = "সংশ্লিষ্ট পক্ষ বা প্রেরকের নাম লিখুন"
                valid = false
            }
            if (summary.isBlank()) {
                summaryError = "সংক্ষিপ্ত বিবরণ লিখুন"
                valid = false
            }
            if (valid) {
                onConfirm(title.trim(), category, counterparty.trim(), summary.trim(), status)
            }
        }
    ) {
        MoreFormSection(title = "নথির শ্রেণি ও পরিচিতি", icon = Icons.Default.Description) {
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
                onValueChange = { title = it; titleError = null },
                label = { Text("নথি বা চিঠির শিরোনাম *") },
                isError = titleError != null,
                supportingText = titleError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = counterparty,
                onValueChange = { counterparty = it; partyError = null },
                label = { Text("প্রেরক / প্রাপক / সংশ্লিষ্ট পক্ষ *") },
                isError = partyError != null,
                supportingText = partyError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = summary,
                onValueChange = { summary = it; summaryError = null },
                label = { Text("সংক্ষিপ্ত বিবরণ *") },
                isError = summaryError != null,
                supportingText = summaryError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
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
    var replyError by remember { mutableStateOf<String?>(null) }

    ExecutiveFormDialogShell(
        title = "নথি জবাব ও স্ট্যাটাস হালনাগাদ",
        subtitle = doc.title,
        icon = Icons.Default.EditNote,
        confirmText = "হালনাগাদ করুন",
        confirmTag = "confirm_reply_office_doc_button",
        onDismiss = onDismiss,
        onConfirmClick = {
            if (status == "জবাব প্রেরিত" && replyNote.isBlank()) {
                replyError = "জবাব প্রেরিত হিসেবে চিহ্নিত করতে দাপ্তরিক নোট লিখুন"
            } else {
                onConfirm(replyNote.trim(), status)
            }
        }
    ) {
        MoreFormSection(title = "স্ট্যাটাস ও জবাব", icon = Icons.Default.AssignmentTurnedIn) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(listOf("সংরক্ষিত", "জবাব অপেক্ষমান", "জবাব প্রেরিত")) { st ->
                    FilterChip(
                        selected = status == st,
                        onClick = { status = st; replyError = null },
                        label = { Text(st, fontSize = 11.sp) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = replyNote,
                onValueChange = { replyNote = it; replyError = null },
                label = { Text("জবাবের খসড়া / দাপ্তরিক নোট") },
                isError = replyError != null,
                supportingText = replyError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
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
    val context = LocalContext.current
    val notes by viewModel.allAdvisoryNotes.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val canSubmitSharia = currentRole.canSubmitShariaReview()
    val canSubmitAdvice = currentRole.canSubmitAdvisoryGuidance()
    val canAcknowledge = currentRole == UserRole.PRESIDENT || currentRole == UserRole.VICE_PRESIDENT || currentRole == UserRole.GENERAL_SECRETARY

    val isDark = LocalIsDarkTheme.current
    val cardBorderSubtle = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
    val shariaCount = notes.count { it.noteType == "SHARIA_REVIEW" }
    val advisoryCount = notes.count { it.noteType == "ADVISORY_GUIDANCE" }
    val pendingCount = notes.count { it.status == "পর্যালোচনাধীন" }

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
            .background(MaterialTheme.colorScheme.background),
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
                                text = "শরিয়াহ সম্মতি পর্যালোচনা ও উপদেষ্টা পরিষদ",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "পদাধিকারবলে ইমাম/খতিবের শরিয়াহ মতামত এবং উপদেষ্টা পরিষদের নির্দেশনা",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                        if (pendingCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MosqueGold.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = "পর্যালোচনাধীন ${toBengaliDigits(pendingCount.toString())} টি",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueGoldLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
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
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val tabs = listOf(
                    Triple(0, "সকল (${toBengaliDigits(notes.size.toString())})", "filter_advisory_all"),
                    Triple(1, "শরিয়াহ পর্যালোচনা (${toBengaliDigits(shariaCount.toString())})", "filter_advisory_sharia"),
                    Triple(2, "উপদেষ্টা পরামর্শ (${toBengaliDigits(advisoryCount.toString())})", "filter_advisory_guidance")
                )
                items(tabs) { (idx, label, tag) ->
                    FilterChip(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        label = { Text(label, fontSize = 11.sp) },
                        modifier = Modifier.testTag(tag),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MosqueEmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
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
                val badgeBg = if (isDark) {
                    if (isSharia) MosqueEmeraldPrimary.copy(alpha = 0.22f) else MosqueGold.copy(alpha = 0.2f)
                } else {
                    if (isSharia) MosqueEmeraldSoft else MosqueGoldLight
                }
                val badgeTextColor = if (isDark) {
                    if (isSharia) MosqueGoldLight else MosqueGold
                } else {
                    MosqueEmeraldDark
                }
                MehrabArchCard(
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    borderColor = if (isSharia) {
                        MosqueEmeraldPrimary.copy(alpha = 0.45f)
                    } else {
                        cardBorderSubtle
                    }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = badgeBg
                            ) {
                                Text(
                                    text = note.shariaCompliance,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeTextColor,
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
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = note.recommendation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "প্রদানকারী: ${note.authorName} (${note.authorRoleTitle}) • ${toBengaliDigits(note.createdDate)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (canAcknowledge && note.status == "পর্যালোচনাধীন") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.updateAdvisoryNoteStatus(note, "গৃহীত")
                                        MosqueToastController.info("পরামর্শটি 'গৃহীত' হিসেবে চিহ্নিত হয়েছে।", "গৃহীত চিহ্নিত")
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("ack_advisory_${note.id}"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("গৃহীত চিহ্নিত করুন", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Button(
                                    onClick = {
                                        viewModel.updateAdvisoryNoteStatus(note, "বাস্তবায়িত")
                                        MosqueToastController.success("পরামর্শটি 'বাস্তবায়িত' হিসেবে চিহ্নিত হয়েছে।", "বাস্তবায়িত চিহ্নিত")
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("implement_advisory_${note.id}"),
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
                MosqueToastController.success("মতামত ও সুপারিশ সফলভাবে দাখিল হয়েছে।", "পরামর্শ দাখিল")
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
    var topicError by remember { mutableStateOf<String?>(null) }
    var recError by remember { mutableStateOf<String?>(null) }

    ExecutiveFormDialogShell(
        title = if (isSharia) "শরিয়াহ সম্মতি পর্যবেক্ষণ ফরম" else "উপদেষ্টা পরিষদের পরামর্শ ফরম",
        subtitle = if (isSharia) "ইমাম ও খতিবের শরিয়াহ পর্যালোচনা" else "প্রবীণ ও উপদেষ্টা পরিষদের দিকনির্দেশনা",
        icon = if (isSharia) Icons.Default.MenuBook else Icons.Default.SupervisorAccount,
        confirmText = "দাখিল করুন",
        confirmTag = "confirm_add_advisory_button",
        onDismiss = onDismiss,
        onConfirmClick = {
            var valid = true
            if (topic.isBlank()) {
                topicError = "বিষয় বা প্রকল্পের নাম লিখুন"
                valid = false
            }
            if (recommendation.isBlank()) {
                recError = if (isSharia) "শরিয়াহ দলিল ও সুপারিশ লিখুন" else "দিকনির্দেশনা ও পরামর্শ লিখুন"
                valid = false
            }
            if (valid) {
                onConfirm(topic.trim(), recommendation.trim(), compliance)
            }
        }
    ) {
        MoreFormSection(title = "পর্যালোচনা ও সুপারিশ বিবরণ", icon = Icons.Default.FactCheck) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val options = if (isSharia) listOf("শরিয়াহ সম্মত", "সংশোধনী প্রয়োজন") else listOf("কৌশলগত পরামর্শ", "সংকট নিরসন")
                items(options) { opt ->
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
                onValueChange = { topic = it; topicError = null },
                label = { Text("বিষয় / প্রকল্প / সিদ্ধান্তের নাম *") },
                isError = topicError != null,
                supportingText = topicError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = recommendation,
                onValueChange = { recommendation = it; recError = null },
                label = { Text(if (isSharia) "শরিয়াহ দলিল ও সুপারিশ *" else "দিকনির্দেশনা ও পরামর্শ *") },
                isError = recError != null,
                supportingText = recError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
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
    val context = LocalContext.current
    val quotes by viewModel.allSmartBoardQuotes.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val canManage = currentRole.canManageSmartBoard()

    val isDark = LocalIsDarkTheme.current
    var showAddQuoteDialog by remember { mutableStateOf(false) }
    var pendingDeleteQuote by remember { mutableStateOf<SmartBoardQuoteEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
                        containerColor = if (quote.isPinned) {
                            if (isDark) MosqueNightSurfaceVariant else MosqueGoldSoft
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (quote.isPinned) MosqueGoldDeep.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
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
                                    IconButton(
                                        onClick = {
                                            viewModel.togglePinSmartBoardQuote(quote)
                                            MosqueToastController.info(
                                                if (quote.isPinned) "বোর্ড থেকে পিন সরানো হয়েছে।" else "স্মার্ট বোর্ডে পিন করা হয়েছে।"
                                            )
                                        },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = if (quote.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                            contentDescription = "স্মার্ট বোর্ডে পিন করুন",
                                            tint = if (quote.isPinned) MosqueGoldDeep else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { pendingDeleteQuote = quote },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "প্রচার বার্তা মুছুন",
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
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = quote.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "প্রচারকারী: ${quote.createdBy}" + if (quote.isPinned) " • ড্যাশবোর্ডে প্রদর্শিত" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                MosqueToastController.success("স্মার্ট বোর্ডে নতুন প্রচার বার্তা প্রকাশিত হয়েছে।", "বার্তা প্রকাশিত")
                showAddQuoteDialog = false
            }
        )
    }

    pendingDeleteQuote?.let { quote ->
        MosqueConfirmDialog(
            title = "প্রচার বার্তা মুছে ফেলবেন?",
            message = "\"${quote.title}\" বার্তাটি স্মার্ট বোর্ড থেকে স্থায়ীভাবে মুছে ফেলা হবে। আপনি কি নিশ্চিত?",
            confirmText = "হ্যাঁ, মুছে ফেলুন",
            isDestructive = true,
            icon = Icons.Default.DeleteOutline,
            confirmTag = "confirm_delete_quote_button",
            onConfirm = {
                viewModel.deleteSmartBoardQuote(quote)
                MosqueToastController.warning("প্রচার বার্তা মুছে ফেলা হয়েছে।", "বার্তা অপসারিত")
                pendingDeleteQuote = null
            },
            onDismiss = { pendingDeleteQuote = null }
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
    var titleError by remember { mutableStateOf<String?>(null) }
    var msgError by remember { mutableStateOf<String?>(null) }

    ExecutiveFormDialogShell(
        title = "স্মার্ট বোর্ড ও দেয়াল প্রচারণা যোগ",
        subtitle = "ডিজিটাল ডিসপ্লে ও মসজিদের দেয়াল বাণী",
        icon = Icons.Default.Campaign,
        confirmText = "প্রকাশ করুন",
        confirmTag = "confirm_add_smart_quote_button",
        onDismiss = onDismiss,
        onConfirmClick = {
            var valid = true
            if (title.isBlank()) {
                titleError = "শিরোনাম লিখুন"
                valid = false
            }
            if (message.isBlank()) {
                msgError = "বিস্তারিত বার্তা বা উক্তি লিখুন"
                valid = false
            }
            if (valid) {
                onConfirm(category, title.trim(), message.trim(), isPinned)
            }
        }
    ) {
        MoreFormSection(title = "প্রচারণার ধরন ও বার্তা", icon = Icons.Default.FormatQuote) {
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
                onValueChange = { title = it; titleError = null },
                label = { Text("শিরোনাম (যেমন: শবে কদর / দেয়াল বাণী) *") },
                isError = titleError != null,
                supportingText = titleError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = message,
                onValueChange = { message = it; msgError = null },
                label = { Text("বিস্তারিত বার্তা / উক্তি *") },
                isError = msgError != null,
                supportingText = msgError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = { isPinned = !isPinned },
                shape = RoundedCornerShape(10.dp),
                color = if (isPinned) MosqueGoldSoft else MosqueBgLight,
                border = BorderStroke(1.dp, if (isPinned) MosqueGold else MosqueCardBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Checkbox(checked = isPinned, onCheckedChange = { isPinned = it })
                    Text(
                        text = "ড্যাশবোর্ড স্মার্ট বোর্ডে পিন করে দেখান",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MosqueEmeraldDark
                    )
                }
            }
        }
    }
}

@Composable
internal fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
    val isDark = LocalIsDarkTheme.current
    val cardBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
