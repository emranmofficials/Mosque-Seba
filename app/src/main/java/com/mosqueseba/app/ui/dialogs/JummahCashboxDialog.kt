package com.mosqueseba.app.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mosqueseba.app.ui.components.MehrabArchCard
import com.mosqueseba.app.ui.components.MosqueToastController
import com.mosqueseba.app.ui.components.toBengaliDigits
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.util.MosquePrintHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JummahCashboxDialog(
    mosqueName: String,
    defaultWitness1: String = "পেশ ইমাম",
    defaultWitness2: String = "কোষাধ্যক্ষ",
    onConfirm: (
        totalAmount: Double,
        breakdownText: String,
        witness1: String,
        witness2: String,
        witness3: String,
        notes: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current

    // Currency Denominations: Value to Count
    var count1000 by remember { mutableStateOf("") }
    var count500 by remember { mutableStateOf("") }
    var count200 by remember { mutableStateOf("") }
    var count100 by remember { mutableStateOf("") }
    var count50 by remember { mutableStateOf("") }
    var count20 by remember { mutableStateOf("") }
    var count10 by remember { mutableStateOf("") }
    var count5 by remember { mutableStateOf("") }
    var count2 by remember { mutableStateOf("") }
    var count1 by remember { mutableStateOf("") }
    var coinsAmount by remember { mutableStateOf("") }

    var witness1 by remember { mutableStateOf(defaultWitness1) }
    var witness2 by remember { mutableStateOf(defaultWitness2) }
    var witness3 by remember { mutableStateOf("সাধারণ মুসল্লি") }
    var notes by remember { mutableStateOf("পবিত্র জুমার জামাত শেষে উপস্থিত মুসল্লিদের প্রত্যক্ষ সাক্ষ্যে দানবাক্স গণনা।") }

    val todayDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Grand total calculation
    val c1000 = count1000.toIntOrNull() ?: 0
    val c500 = count500.toIntOrNull() ?: 0
    val c200 = count200.toIntOrNull() ?: 0
    val c100 = count100.toIntOrNull() ?: 0
    val c50 = count50.toIntOrNull() ?: 0
    val c20 = count20.toIntOrNull() ?: 0
    val c10 = count10.toIntOrNull() ?: 0
    val c5 = count5.toIntOrNull() ?: 0
    val c2 = count2.toIntOrNull() ?: 0
    val c1 = count1.toIntOrNull() ?: 0
    val coins = coinsAmount.toDoubleOrNull() ?: 0.0

    val totalAmount = remember(c1000, c500, c200, c100, c50, c20, c10, c5, c2, c1, coins) {
        (c1000 * 1000) + (c500 * 500) + (c200 * 200) + (c100 * 100) +
        (c50 * 50) + (c20 * 20) + (c10 * 10) + (c5 * 5) + (c2 * 2) + (c1 * 1) + coins
    }

    val breakdownList = remember(c1000, c500, c200, c100, c50, c20, c10, c5, c2, c1, coins) {
        listOf(
            "৳১০০০" to c1000,
            "৳৫০০" to c500,
            "৳২০০" to c200,
            "৳১০০" to c100,
            "৳৫০" to c50,
            "৳২০" to c20,
            "৳১০" to c10,
            "৳৫" to c5,
            "৳২" to c2,
            "৳১" to c1,
            "খুচরা কয়েন (টাকা)" to coins.toInt()
        )
    }

    val breakdownText = remember(breakdownList) {
        breakdownList.filter { it.second > 0 }.joinToString(", ") { "${it.first} x ${toBengaliDigits(it.second.toString())}" }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MehrabArchCard(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 620.dp)
                .fillMaxHeight(0.92f),
            backgroundColor = MaterialTheme.colorScheme.surface,
            borderColor = MosqueGold
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = MosqueGoldDeep,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "জুমার দানবাক্স গণনা ও ৩-সাক্ষী রেজিস্টার",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "$mosqueName • সাপ্তাহিক জুমার দান অডিট ভাউচার",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hero Grand Total Display
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft,
                    border = BorderStroke(1.5.dp, MosqueEmeraldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "সর্বমোট সংগৃহীত দানবাক্স স্থিতি",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "৳ ${toBengaliDigits(String.format(Locale.US, "%,.0f", totalAmount))}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = MosqueEmeraldPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MosqueGold.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, MosqueGold)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = MosqueGoldDeep, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("স্বচ্ছ হিসাব", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MosqueGoldDeep)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "টাকার নোটের সংখ্যা লিখুন (স্বয়ংক্রিয় যোগফল হিসাব হবে):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // 2-column Currency Note Inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CashboxNoteField(
                            label = "৳১০০০ নোট",
                            count = count1000,
                            onCountChange = { count1000 = it },
                            unitValue = 1000,
                            modifier = Modifier.weight(1f)
                        )
                        CashboxNoteField(
                            label = "৳৫০০ নোট",
                            count = count500,
                            onCountChange = { count500 = it },
                            unitValue = 500,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CashboxNoteField(
                            label = "৳২০০ নোট",
                            count = count200,
                            onCountChange = { count200 = it },
                            unitValue = 200,
                            modifier = Modifier.weight(1f)
                        )
                        CashboxNoteField(
                            label = "৳১০০ নোট",
                            count = count100,
                            onCountChange = { count100 = it },
                            unitValue = 100,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CashboxNoteField(
                            label = "৳৫০ নোট",
                            count = count50,
                            onCountChange = { count50 = it },
                            unitValue = 50,
                            modifier = Modifier.weight(1f)
                        )
                        CashboxNoteField(
                            label = "৳২০ নোট",
                            count = count20,
                            onCountChange = { count20 = it },
                            unitValue = 20,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CashboxNoteField(
                            label = "৳১০ নোট",
                            count = count10,
                            onCountChange = { count10 = it },
                            unitValue = 10,
                            modifier = Modifier.weight(1f)
                        )
                        CashboxNoteField(
                            label = "৳৫ নোট",
                            count = count5,
                            onCountChange = { count5 = it },
                            unitValue = 5,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CashboxNoteField(
                            label = "৳২ নোট",
                            count = count2,
                            onCountChange = { count2 = it },
                            unitValue = 2,
                            modifier = Modifier.weight(1f)
                        )
                        CashboxNoteField(
                            label = "৳১ নোট / খুচরা কয়েন (টাকা)",
                            count = coinsAmount,
                            onCountChange = { coinsAmount = it },
                            unitValue = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = MosqueCardBorderSubtle)
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "উপস্থিত ৩ জন সাক্ষীর নাম ও পদবি *",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = witness1,
                        onValueChange = { witness1 = it },
                        label = { Text("১ম সাক্ষী (ইমাম / খতিব) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = witness2,
                        onValueChange = { witness2 = it },
                        label = { Text("২য় সাক্ষী (কোষাধ্যক্ষ / সভাপতি) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = witness3,
                        onValueChange = { witness3 = it },
                        label = { Text("৩য় সাক্ষী (সাধারণ মুসল্লি / নিরপেক্ষ সাক্ষী) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("গণনার স্থান / বিশেষ মন্তব্য") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MosqueCardBorderSubtle)
                Spacer(modifier = Modifier.height(10.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(0.8f)
                            .heightIn(min = 46.dp)
                    ) {
                        Text("বাতিল", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            if (totalAmount <= 0) {
                                MosqueToastController.warning("প্রথমে নোটের সংখ্যা বা টাকার পরিমাণ লিখুন।")
                                return@OutlinedButton
                            }
                            MosquePrintHelper.printCashboxVoucher(
                                context = context,
                                mosqueName = mosqueName,
                                dateStr = todayDate,
                                totalAmount = totalAmount,
                                breakdownList = breakdownList,
                                witness1 = witness1,
                                witness2 = witness2,
                                witness3 = witness3,
                                notes = notes
                            )
                        },
                        modifier = Modifier
                            .weight(1.1f)
                            .heightIn(min = 46.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueEmeraldPrimary),
                        border = BorderStroke(1.dp, MosqueEmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ভাউচার প্রিন্ট", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (totalAmount <= 0) {
                                MosqueToastController.warning("টাকার সংখ্যা বা নোটের পরিমাণ শূন্য হতে পারবে না।")
                                return@Button
                            }
                            if (witness1.isBlank() || witness2.isBlank()) {
                                MosqueToastController.warning("কমপক্ষে ২ জন সাক্ষীর নাম উল্লেখ করতে হবে।")
                                return@Button
                            }
                            onConfirm(totalAmount, breakdownText, witness1, witness2, witness3, notes)
                            MosqueToastController.success(
                                "জুমার দানবাক্স গণনা সফলভাবে সম্পন্ন হয়েছে। বায়তুলমাল আয় সংরক্ষিত।",
                                "গণনা ও জমা সফল"
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .heightIn(min = 46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নিশ্চিত ও জমা", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CashboxNoteField(
    label: String,
    count: String,
    onCountChange: (String) -> Unit,
    unitValue: Int,
    modifier: Modifier = Modifier
) {
    val countInt = count.toIntOrNull() ?: 0
    val subtotal = countInt * unitValue

    OutlinedTextField(
        value = count,
        onValueChange = { input ->
            if (input.all { it.isDigit() }) {
                onCountChange(input)
            }
        },
        label = { Text(label, fontSize = 11.sp) },
        placeholder = { Text("০ টি", fontSize = 11.sp) },
        supportingText = {
            if (subtotal > 0) {
                Text(
                    text = "= ৳ ${toBengaliDigits(String.format(Locale.US, "%,d", subtotal))}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldPrimary
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier
    )
}
