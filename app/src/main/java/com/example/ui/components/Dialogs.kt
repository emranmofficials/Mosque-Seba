package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.ChandaPaymentEntity
import com.example.data.local.entity.MemberEntity
import com.example.ui.theme.*

// 1. Add Member Dialog
@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        nameBn: String,
        nameEn: String,
        mobile: String,
        email: String,
        nid: String,
        address: String,
        villageArea: String,
        roleCode: String,
        monthlyChanda: Double
    ) -> Unit
) {
    var nameBn by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var nid by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var roleCode by remember { mutableStateOf("general_member") }
    var monthlyChanda by remember { mutableStateOf("500") }

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
                    text = "নতুন সদস্য নিবন্ধন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )
                Text(
                    text = "মসজিদ কমিটির সম্মানিত সদস্য ডাটাবেজে অন্তর্ভুক্ত করুন",
                    style = MaterialTheme.typography.bodySmall,
                    color = MosqueTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text("নাম (বাংলায়) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("নাম (English)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("মোবাইল নম্বর *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা / লেন") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = monthlyChanda,
                    onValueChange = { monthlyChanda = it },
                    label = { Text("মাসিক চাঁদার পরিমাণ (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = MosqueTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nameBn.isNotBlank() && mobile.isNotBlank()) {
                                onConfirm(
                                    nameBn,
                                    nameEn,
                                    mobile,
                                    email,
                                    nid,
                                    address,
                                    "মিরপুর",
                                    roleCode,
                                    monthlyChanda.toDoubleOrNull() ?: 500.0
                                )
                                onDismiss()
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

// 2. Add Transaction Dialog (Income or Expense)
@Composable
fun AddTransactionDialog(
    initialType: String = "INCOME",
    onDismiss: () -> Unit,
    onConfirm: (category: String, type: String, amount: Double, description: String) -> Unit
) {
    var type by remember { mutableStateOf(initialType) }
    var category by remember { mutableStateOf(if (type == "INCOME") "জুমার দান বাক্স" else "বিদ্যুৎ ও ইউটিলিটি বিল") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val incomeCategories = listOf("জুমার দান বাক্স", "মাসিক সদস্য চাঁদা", "মসজিদ সংস্কার অনুদান", "হিফজখানা অনুদান", "সাধারণ দান")
    val expenseCategories = listOf("ইমাম ও কর্মচারীদের বেতন", "বিদ্যুৎ ও ইউটিলিটি বিল", "মসজিদ রক্ষণাবেক্ষণ", "জেনারেটর জ্বালানি", "আপ্যায়ন ও বিবিধ")

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
                    text = if (type == "INCOME") "নতুন আয় অন্তর্ভুক্ত করুন" else "নতুন ব্যয় লিপিবদ্ধ করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (type == "INCOME") MosqueSuccessGreen else MosqueUrgentRed
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Type Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MosqueBgLight)
                        .padding(4.dp)
                ) {
                    Button(
                        onClick = {
                            type = "INCOME"
                            category = incomeCategories.first()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "INCOME") MosqueSuccessGreen else Color.Transparent,
                            contentColor = if (type == "INCOME") Color.White else MosqueTextSecondary
                        ),
                        elevation = null
                    ) {
                        Text("আয় (জমা)")
                    }
                    Button(
                        onClick = {
                            type = "EXPENSE"
                            category = expenseCategories.first()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "EXPENSE") MosqueUrgentRed else Color.Transparent,
                            contentColor = if (type == "EXPENSE") Color.White else MosqueTextSecondary
                        ),
                        elevation = null
                    ) {
                        Text("ব্যয় (খরচ)")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Categories
                Text(
                    text = "খাত নির্বাচন করুন:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MosqueTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                val currentCats = if (type == "INCOME") incomeCategories else expenseCategories
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    currentCats.take(4).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("টাকার পরিমাণ (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("বিবরণ / উদ্দেশ্য") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = MosqueTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                onConfirm(category, type, amt, description)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "INCOME") MosqueSuccessGreen else MosqueUrgentRed
                        )
                    ) {
                        Text("দাখিল করুন")
                    }
                }
            }
        }
    }
}

// 3. Receive Chanda Dialog
@Composable
fun ReceiveChandaDialog(
    members: List<MemberEntity>,
    onDismiss: () -> Unit,
    onConfirm: (member: MemberEntity, month: String, amount: Double, method: String) -> Unit
) {
    var selectedMember by remember { mutableStateOf(members.firstOrNull()) }
    var month by remember { mutableStateOf("সেপ্টেম্বর ২০২৪") }
    var amount by remember { mutableStateOf(selectedMember?.monthlyChanda?.toInt()?.toString() ?: "1000") }
    var method by remember { mutableStateOf("cash") }

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
                    text = "সদস্য চাঁদা গ্রহণ ও রসিদ তৈরি",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "সদস্য নির্বাচন করুন:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MosqueTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                members.take(5).forEach { m ->
                    FilterChip(
                        selected = selectedMember?.id == m.id,
                        onClick = {
                            selectedMember = m
                            amount = m.monthlyChanda.toInt().toString()
                        },
                        label = { Text("${m.nameBn} (${m.memberCode})") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = month,
                    onValueChange = { month = it },
                    label = { Text("মাসের নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("আদায়ের পরিমাণ (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "পদ্ধতি:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MosqueTextSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = method == "cash",
                        onClick = { method = "cash" },
                        label = { Text("নগদ (Cash)") }
                    )
                    FilterChip(
                        selected = method == "mobile_banking",
                        onClick = { method = "mobile_banking" },
                        label = { Text("বিকাশ/নগদ") }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = MosqueTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            val m = selectedMember
                            if (m != null && amt > 0) {
                                onConfirm(m, month, amt, method)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Text("রসিদ প্রস্তুত করুন")
                    }
                }
            }
        }
    }
}

// 4. Official Chanda Receipt Dialog with QR verification (PRD §8)
@Composable
fun OfficialReceiptDialog(
    payment: ChandaPaymentEntity,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MosqueGold))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "বায়তুল জান্নাত জামে মসজিদ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )
                Text(
                    text = "মিরপুর-১০, ঢাকা • কমিটি আইডি: MC-DHK-2024",
                    style = MaterialTheme.typography.bodySmall,
                    color = MosqueTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MosqueGoldLight)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "অফিসিয়াল চাঁদা জমার রসিদ",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MosqueEmeraldPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MosqueGold)
                Spacer(modifier = Modifier.height(14.dp))

                // Receipt Fields
                ReceiptRow("রসিদ নম্বর", payment.receiptCode)
                ReceiptRow("সদস্যের নাম", payment.memberNameBn)
                ReceiptRow("মাসের বিবরণ", payment.month)
                ReceiptRow("টাকার পরিমাণ", "৳${String.format("%,.0f", payment.amountPaid)}")
                ReceiptRow("পরিশোধের মাধ্যম", if (payment.method == "cash") "নগদ (Cash)" else "বিকাশ/নগদ")
                ReceiptRow("জমার তারিখ ও সময়", payment.paidAt)
                ReceiptRow("গ্রহণকারী", payment.receivedBy)

                Spacer(modifier = Modifier.height(14.dp))

                // QR Verification Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MosqueBgLight)
                        .border(1.dp, MosqueCardBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "QR Code",
                            tint = MosqueEmeraldDark,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ডিজিটাল ভেরিফিকেশন কোড:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MosqueTextMuted,
                            fontSize = 10.sp
                        )
                        Text(
                            text = payment.qrVerificationCode,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MosqueEmeraldPrimary,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                ) {
                    Text("বন্ধ করুন")
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MosqueTextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MosqueTextPrimary
        )
    }
}

// 5. Publish Notice Dialog
@Composable
fun PublishNoticeDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, priority: String, isPinned: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("normal") }
    var isPinned by remember { mutableStateOf(false) }

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
                    text = "নতুন নোটিশ প্রকাশ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("নোটিশের শিরোনাম *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("বিস্তারিত বিবরণ *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "গুরুত্ব নির্বাচন করুন:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MosqueTextSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = priority == "normal",
                        onClick = { priority = "normal" },
                        label = { Text("সাধারণ") }
                    )
                    FilterChip(
                        selected = priority == "important",
                        onClick = { priority = "important" },
                        label = { Text("গুরুত্বপূর্ণ") }
                    )
                    FilterChip(
                        selected = priority == "urgent",
                        onClick = { priority = "urgent" },
                        label = { Text("জরুরি") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it }
                    )
                    Text(
                        text = "বোর্ডের শীর্ষে পিন করে রাখুন",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = MosqueTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                onConfirm(title, content, priority, isPinned)
                                onDismiss()
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

// 6. Schedule Meeting Dialog
@Composable
fun ScheduleMeetingDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, dateStr: String, location: String, agenda: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf("১৫ সেপ্টেম্বর ২০২৪, বাদ আসর") }
    var location by remember { mutableStateOf("মসজিদ অফিস কক্ষ") }
    var agenda by remember { mutableStateOf("") }

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
                    text = "কমিটি সভা আহ্বান করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("সভার নাম / উদ্দেশ্য *") },
                    placeholder = { Text("যেমন: সেপ্টেম্বর মাসিক সাধারণ সভা") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text("তারিখ ও সময় *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("স্থান *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = agenda,
                    onValueChange = { agenda = it },
                    label = { Text("আলোচ্যসূচি / এজেন্ডা") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = MosqueTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm(title, dateStr, location, agenda)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Text("সভা নির্ধারণ")
                    }
                }
            }
        }
    }
}

