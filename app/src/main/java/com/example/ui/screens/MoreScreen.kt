package com.example.ui.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.ChandaPaymentEntity
import com.example.data.local.entity.StaffEntity
import com.example.ui.components.OfficialReceiptDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.MosqueViewModel

@Composable
fun MoreScreen(
    viewModel: MosqueViewModel,
    onOpenReceiveChanda: () -> Unit,
    onOpenScheduleMeeting: () -> Unit
) {
    var selectedSubModule by remember { mutableStateOf<String?>(null) }
    var selectedPaymentForReceipt by remember { mutableStateOf<ChandaPaymentEntity?>(null) }
    var showAddStaffDialog by remember { mutableStateOf(false) }

    val chandaPayments by viewModel.allChandaPayments.collectAsState()
    val activeStaff by viewModel.activeStaff.collectAsState()
    val auditLogs by viewModel.recentAuditLogs.collectAsState()
    val mosque by viewModel.mosque.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MosqueBgLight)
    ) {
        // Sticky Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedSubModule != null) {
                IconButton(onClick = { selectedSubModule = null }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MosqueEmeraldDark)
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            Column {
                Text(
                    text = when (selectedSubModule) {
                        "chanda" -> "চাঁদা আদায় ও রসিদ রেজিস্টার"
                        "staff" -> "ইমাম ও কর্মচারী বেতন ব্যবস্থাপনা"
                        "prayer" -> "পূর্ণ নামাজের সময়সূচি ও ওয়াক্ত"
                        "audit" -> "স্বচ্ছতা ও সিস্টেম অডিট লগ"
                        "about" -> "মসজিদ পরিচিতি ও কমিটি"
                        else -> "কমিটি পরিচালনা ও অন্যান্য ফিচার"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )
                Text(
                    text = "মসজিদ কমিটি অপারেশনাল মডিউলসমূহ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MosqueTextSecondary
                )
            }
        }

        // Sub-module routing
        when (selectedSubModule) {
            "chanda" -> {
                // Chanda Ledger
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = MosqueEmeraldPrimary)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("মাসিক চাঁদা আদায়", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                                    Text("মোট আদায়: ৳৮,০০০", color = MosqueGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                }
                                Button(
                                    onClick = onOpenReceiveChanda,
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueGold),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("চাঁদা গ্রহণ করুন", color = MosqueEmeraldDark, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    items(chandaPayments, key = { it.id }) { payment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedPaymentForReceipt = payment },
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = payment.memberNameBn,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueTextPrimary
                                    )
                                    Text(
                                        text = "${payment.month} • রসিদ: ${payment.receiptCode}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MosqueTextSecondary
                                    )
                                    Text(
                                        text = "জমার সময়: ${payment.paidAt}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MosqueTextMuted
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "৳${payment.amountPaid.toInt()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueSuccessGreen
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode,
                                            contentDescription = null,
                                            tint = MosqueEmeraldPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "রসিদ দেখুন",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MosqueEmeraldPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "staff" -> {
                // Staff Directory & Payroll
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "মসজিদের সম্মানিত স্টাফবৃন্দ (${activeStaff.size} জন)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldDark
                            )

                            Button(
                                onClick = { showAddStaffDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                            ) {
                                Text("+ নতুন স্টাফ", fontSize = 12.sp)
                            }
                        }
                    }

                    items(activeStaff, key = { it.id }) { staff ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = staff.nameBn,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueTextPrimary
                                        )
                                        Text(
                                            text = staff.designationBn,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MosqueEmeraldPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MosqueSuccessGreen.copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "সক্রিয়",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MosqueSuccessGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MosqueCardBorder)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("যোগদান: ${staff.joinDate}", style = MaterialTheme.typography.labelSmall, color = MosqueTextMuted)
                                        Text("মোবাইল: ${staff.mobile}", style = MaterialTheme.typography.labelSmall, color = MosqueTextSecondary)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("মাসিক বেতন ভাতা", style = MaterialTheme.typography.labelSmall, color = MosqueTextMuted)
                                        Text(
                                            text = "৳${String.format("%,.0f", staff.salaryAmount)}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueEmeraldDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "prayer" -> {
                // Detailed Prayer Schedule
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "সেপ্টেম্বর ২০২৪ ওয়াক্ত ও জামাত সূচি",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueEmeraldDark
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                PrayerRowDetail("ফজর", "০৪:৩০", "০৫:০০", "সূর্যোদয়: ০৫:৪২")
                                PrayerRowDetail("যোহর", "১২:৩০", "০১:১৫", "ওয়াক্ত শেষ: ০৪:১৬")
                                PrayerRowDetail("আসর", "০৪:৩০", "০৪:৪৫", "ওয়াক্ত শেষ: ০৬:১৮")
                                PrayerRowDetail("মাগরিব", "০৬:১৮", "০৬:২৩", "ওয়াক্ত শেষ: ০৭:৩৪")
                                PrayerRowDetail("ইশা", "০৭:৪৫", "০৮:১৫", "ওয়াক্ত শেষ: ১১:৪৫")
                                PrayerRowDetail("জুমুআ (শুক্রবার)", "১২:৩০", "০১:১৫", "বয়ান: ১২:৪০")
                            }
                        }
                    }
                }
            }
            "audit" -> {
                // Audit Logs
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(auditLogs, key = { it.id }) { log ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = log.action,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueEmeraldPrimary
                                    )
                                    Text(
                                        text = log.timestamp,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MosqueTextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = log.details,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "কর্তা: ${log.actor}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MosqueTextSecondary
                                )
                            }
                        }
                    }
                }
            }
            "about" -> {
                // Mosque About & Tenure
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = mosque?.nameBn ?: "বায়তুল জান্নাত জামে মসজিদ",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueEmeraldDark
                                )
                                Text(
                                    text = mosque?.nameEn ?: "Baitul Jannat Jame Masjid",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueTextSecondary
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(10.dp))

                                DetailItemRow("কমিটি আইডি", mosque?.committeeId ?: "MC-DHK-2024")
                                DetailItemRow("ঠিকানা", mosque?.address ?: "মিরপুর-১০, ঢাকা")
                                DetailItemRow("প্রতিষ্ঠা সাল", "১৯৯৮ ইং")
                                DetailItemRow("কমিটির মেয়াদকাল", "২০২৪ — ২০২৬ খ্রি.")
                                DetailItemRow("কমিটির সভাপতি", "মো: রফিকুল ইসলাম")
                                DetailItemRow("সাধারণ সম্পাদক", "ইঞ্জি: আব্দুল্লাহ আল মামুন")
                                DetailItemRow("কোষাধ্যক্ষ", "হাজী মো: আবুল কাসেম")
                                DetailItemRow("সিস্টেম স্ট্যাটাস", "সক্রিয় (Active Tenant)")
                            }
                        }
                    }
                }
            }
            else -> {
                // Main Operations Menu
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OperationMenuItem(
                            title = "চাঁদা আদায় ও রসিদ রেজিস্টার",
                            description = "সদস্যদের চাঁদা গ্রহণ ও ডিজিটাল ভেরিফাইড রসিদ প্রদান",
                            icon = Icons.Default.ReceiptLong,
                            iconColor = MosqueEmeraldPrimary,
                            onClick = { selectedSubModule = "chanda" }
                        )
                    }

                    item {
                        OperationMenuItem(
                            title = "ইমাম ও কর্মচারী বেতন ব্যবস্থাপনা",
                            description = "পেশ ইমাম, মুয়াজ্জিন ও খাদেমদের বেতন ও দায়িত্ব",
                            icon = Icons.Default.Badge,
                            iconColor = MosqueInfoBlue,
                            onClick = { selectedSubModule = "staff" }
                        )
                    }

                    item {
                        OperationMenuItem(
                            title = "পূর্ণ নামাজের সময়সূচি ও ওয়াক্ত",
                            description = "আজান ও জামাতের বিস্তারিত সূচি ও ক্যালেন্ডার",
                            icon = Icons.Default.AccessTime,
                            iconColor = MosqueEmeraldLight,
                            onClick = { selectedSubModule = "prayer" }
                        )
                    }

                    item {
                        OperationMenuItem(
                            title = "কমিটি সভা ও এজেন্ডা",
                            description = "আসন্ন মিটিং নির্ধারণ ও পূর্ববর্তী রেজুলেশন",
                            icon = Icons.Default.Event,
                            iconColor = MosqueGoldDeep,
                            onClick = onOpenScheduleMeeting
                        )
                    }

                    item {
                        OperationMenuItem(
                            title = "স্বচ্ছতা ও সিস্টেম অডিট লগ",
                            description = "সকল আর্থিক অনুমোদন ও ডাটা পরিবর্তনের অপরিবর্তনীয় রেকর্ড",
                            icon = Icons.Default.FactCheck,
                            iconColor = MosqueWarningAmber,
                            onClick = { selectedSubModule = "audit" }
                        )
                    }

                    item {
                        OperationMenuItem(
                            title = "মসজিদ পরিচিতি ও কমিটি তথ্য",
                            description = "কমিটি কাঠামো, ইতিহাস, প্রতিষ্ঠা সাল ও ঠিকানা",
                            icon = Icons.Default.Mosque,
                            iconColor = MosqueEmeraldDark,
                            onClick = { selectedSubModule = "about" }
                        )
                    }
                }
            }
        }
    }

    // Receipt Dialog
    selectedPaymentForReceipt?.let { payment ->
        OfficialReceiptDialog(
            payment = payment,
            onDismiss = { selectedPaymentForReceipt = null }
        )
    }

    // Add Staff Dialog
    if (showAddStaffDialog) {
        AddStaffDialog(
            onDismiss = { showAddStaffDialog = false },
            onConfirm = { name, role, designation, salary, mobile ->
                viewModel.addStaff(name, role, designation, salary, mobile)
                showAddStaffDialog = false
            }
        )
    }
}

@Composable
fun OperationMenuItem(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MosqueTextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MosqueTextSecondary
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MosqueTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PrayerRowDetail(prayerName: String, azan: String, iqamah: String, note: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(prayerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            Text(note, fontSize = 11.sp, color = MosqueTextMuted)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("জামাত: $iqamah", fontWeight = FontWeight.Bold, color = MosqueEmeraldPrimary, style = MaterialTheme.typography.labelMedium)
            Text("আজান: $azan", fontSize = 11.sp, color = MosqueTextSecondary)
        }
    }
    HorizontalDivider(color = MosqueCardBorder.copy(alpha = 0.5f))
}

@Composable
fun AddStaffDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, role: String, designation: String, salary: Double, mobile: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("other_staff") }
    var designation by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("15000") }
    var mobile by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "নতুন স্টাফ অন্তর্ভুক্ত করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("নাম (বাংলায়) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("পদবী (যেমন: সহকারী ইমাম / খাদেম) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it },
                    label = { Text("মাসিক বেতন (৳) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("মোবাইল নম্বর *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                            val sal = salary.toDoubleOrNull() ?: 15000.0
                            if (name.isNotBlank() && designation.isNotBlank()) {
                                onConfirm(name, role, designation, sal, mobile)
                            }
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
