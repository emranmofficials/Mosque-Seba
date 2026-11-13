package com.mosqueseba.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mosqueseba.app.data.local.entity.StaffEntity
import com.mosqueseba.app.ui.theme.*

@Composable
private fun ExecutiveFormDialogShell(
    title: String,
    subtitle: String,
    icon: ImageVector,
    confirmText: String,
    confirmTag: String = "dialog_confirm_button",
    onDismiss: () -> Unit,
    onConfirmClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MehrabArchCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            backgroundColor = MosqueBgLight,
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MosqueGold.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.7f)),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = MosqueGoldLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueGoldLight,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = Color.White)
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
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = content
                )

                HorizontalDivider(color = MosqueCardBorderGold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(0.9f)
                            .heightIn(min = 48.dp)
                    ) {
                        Text("বাতিল", color = MosqueTextSecondary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onConfirmClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        modifier = Modifier
                            .weight(1.3f)
                            .heightIn(min = 48.dp)
                            .testTag(confirmTag)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(confirmText, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreFormSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, MosqueCardBorderGold),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MosqueEmeraldSoft,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MosqueEmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MosqueEmeraldDark
                )
            }
            content()
        }
    }
}

@Composable
fun PaySalaryFormDialog(
    staff: StaffEntity,
    defaultMonth: String = "সেপ্টেম্বর ২০২৪",
    onDismiss: () -> Unit,
    onConfirm: (month: String, bonus: Double) -> Unit
) {
    var month by remember(defaultMonth) { mutableStateOf(defaultMonth) }
    var bonus by remember { mutableStateOf("0") }
    var monthError by remember { mutableStateOf<String?>(null) }

    val bonusAmt = bonus.toDoubleOrNull() ?: 0.0
    val netTotal = staff.salaryAmount + bonusAmt
    val monthPresets = remember(defaultMonth) {
        listOf(defaultMonth, "সেপ্টেম্বর ২০২৪", "অক্টোবর ২০২৪", "নভেম্বর ২০২৪", "ঈদ বোনাসসহ").distinct()
    }

    ExecutiveFormDialogShell(
        title = "বেতন ও ভাতা পরিশোধ",
        subtitle = "${staff.nameBn} (${staff.designationBn})",
        icon = Icons.Default.Payments,
        confirmText = "পরিশোধ নিশ্চিত করুন",
        confirmTag = "confirm_pay_salary_button",
        onDismiss = onDismiss,
        onConfirmClick = {
            if (month.isBlank()) {
                monthError = "বেতনের মাস উল্লেখ করুন"
            } else {
                onConfirm(month.trim(), bonusAmt)
            }
        }
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MosqueEmeraldSoft.copy(alpha = 0.65f),
            border = BorderStroke(1.dp, MosqueCardBorderGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("মূল নির্ধারিত বেতন", fontSize = 11.sp, color = MosqueTextSecondary)
                    Text(
                        text = "৳${toBengaliDigits(String.format("%,.0f", staff.salaryAmount))}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MosqueEmeraldDark
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("মোট প্রদেয়", fontSize = 11.sp, color = MosqueTextSecondary)
                    Text(
                        text = "৳${toBengaliDigits(String.format("%,.0f", netTotal))}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MosqueEmeraldPrimary
                    )
                }
            }
        }

        MoreFormSection(title = "বেতন বিবরণ", icon = Icons.Default.ReceiptLong) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(monthPresets) { preset ->
                    FilterChip(
                        selected = month == preset,
                        onClick = {
                            month = preset
                            monthError = null
                        },
                        label = { Text(preset, fontSize = 11.sp) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = month,
                onValueChange = {
                    month = it
                    monthError = null
                },
                label = { Text("বেতনের মাস *") },
                isError = monthError != null,
                supportingText = monthError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = bonus,
                onValueChange = { bonus = it.filter { ch -> ch.isDigit() } },
                label = { Text("বোনাস / অতিরিক্ত ভাতা (৳)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AddRosterFormDialog(
    activeStaff: List<StaffEntity>,
    onDismiss: () -> Unit,
    onConfirm: (staff: StaffEntity, dayOfWeek: String, prayerName: String, roleTitle: String, notes: String) -> Unit
) {
    var selectedStaff by remember { mutableStateOf(activeStaff.first()) }
    var dayOfWeek by remember { mutableStateOf("শুক্রবার") }
    var prayerName by remember { mutableStateOf("জুমআ ও আসর") }
    var roleTitle by remember { mutableStateOf("খুতবা ও ইমামতি") }
    var notes by remember { mutableStateOf("") }
    var dayError by remember { mutableStateOf<String?>(null) }
    var prayerError by remember { mutableStateOf<String?>(null) }

    val dayPresets = listOf("প্রতিদিন", "শুক্রবার", "শনি-বৃহস্পতি", "রবিবার")
    val rolePresets = listOf("ইমামতি", "খুতবা ও ইমামতি", "আজান ও ইকামত", "মসজিদ পরিচ্ছন্নতা")

    ExecutiveFormDialogShell(
        title = "নতুন ওয়াক্ত দায়িত্ব বণ্টন",
        subtitle = "ইমাম, মুয়াজ্জিন ও খাদেমের ডিউটি নির্ধারণ",
        icon = Icons.Default.EventNote,
        confirmText = "নির্ধারণ করুন",
        confirmTag = "confirm_add_roster_button",
        onDismiss = onDismiss,
        onConfirmClick = {
            var valid = true
            if (dayOfWeek.isBlank()) {
                dayError = "বার বা দিন উল্লেখ করুন"
                valid = false
            }
            if (prayerName.isBlank()) {
                prayerError = "নামাজের ওয়াক্ত উল্লেখ করুন"
                valid = false
            }
            if (valid) {
                onConfirm(selectedStaff, dayOfWeek.trim(), prayerName.trim(), roleTitle.trim(), notes.trim())
            }
        }
    ) {
        MoreFormSection(title = "দায়িত্বপ্রাপ্ত স্টাফ নির্বাচন", icon = Icons.Default.Person) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(activeStaff, key = { it.id }) { staff ->
                    FilterChip(
                        selected = selectedStaff.id == staff.id,
                        onClick = { selectedStaff = staff },
                        label = { Text("${staff.nameBn} (${staff.designationBn})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MosqueEmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        MoreFormSection(title = "সময় ও দায়িত্বের বিবরণ", icon = Icons.Default.Schedule) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(dayPresets) { preset ->
                    SuggestionChip(
                        onClick = {
                            dayOfWeek = preset
                            dayError = null
                        },
                        label = { Text(preset, fontSize = 11.sp) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = dayOfWeek,
                onValueChange = {
                    dayOfWeek = it
                    dayError = null
                },
                label = { Text("বার / দিন *") },
                isError = dayError != null,
                supportingText = dayError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = prayerName,
                onValueChange = {
                    prayerName = it
                    prayerError = null
                },
                label = { Text("নামাজের ওয়াক্ত *") },
                isError = prayerError != null,
                supportingText = prayerError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(rolePresets) { preset ->
                    FilterChip(
                        selected = roleTitle == preset,
                        onClick = { roleTitle = preset },
                        label = { Text(preset, fontSize = 11.sp) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = roleTitle,
                onValueChange = { roleTitle = it },
                label = { Text("দায়িত্ব (ইমামতি / আজান) *") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("বিশেষ নোট (ঐচ্ছিক)") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AddFoodScheduleFormDialog(
    onDismiss: () -> Unit,
    onConfirm: (dateStr: String, mealType: String, providerName: String, providerMobile: String, receiverName: String) -> Unit
) {
    var dateStr by remember { mutableStateOf("২০২৪-০৯-১৫") }
    var mealType by remember { mutableStateOf("দুপুর") }
    var providerName by remember { mutableStateOf("") }
    var providerMobile by remember { mutableStateOf("") }
    var receiverName by remember { mutableStateOf("ইমাম ও মুয়াজ্জিন সাহেব") }
    var providerError by remember { mutableStateOf<String?>(null) }

    val mealOptions = listOf("দুপুর", "রাত", "সকাল ও দুপুর")

    ExecutiveFormDialogShell(
        title = "খাবার দায়িত্ব বণ্টন ফর্ম",
        subtitle = "ইমাম ও খাদেমদের জায়গির / খাবার সূচি",
        icon = Icons.Default.Restaurant,
        confirmText = "সংরক্ষণ করুন",
        confirmTag = "confirm_add_food_button",
        onDismiss = onDismiss,
        onConfirmClick = {
            if (providerName.isBlank()) {
                providerError = "খাবার প্রদানকারীর নাম লিখুন"
            } else {
                onConfirm(dateStr.trim(), mealType.trim(), providerName.trim(), providerMobile.trim(), receiverName.trim())
            }
        }
    ) {
        MoreFormSection(title = "তারিখ ও ওয়াক্ত", icon = Icons.Default.Schedule) {
            OutlinedTextField(
                value = dateStr,
                onValueChange = { dateStr = it },
                label = { Text("তারিখ *") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("খাবারের ওয়াক্ত নির্বাচন:", fontSize = 11.5.sp, color = MosqueTextSecondary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                mealOptions.forEach { option ->
                    FilterChip(
                        selected = mealType == option,
                        onClick = { mealType = option },
                        label = { Text(option, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MosqueEmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        MoreFormSection(title = "দাতা ও প্রাপকের তথ্য", icon = Icons.Default.People) {
            OutlinedTextField(
                value = providerName,
                onValueChange = {
                    providerName = it
                    providerError = null
                },
                label = { Text("খাবার প্রদানকারী দাতার নাম *") },
                isError = providerError != null,
                supportingText = providerError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = providerMobile,
                onValueChange = { providerMobile = it },
                label = { Text("দাতার মোবাইল নম্বর *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = receiverName,
                onValueChange = { receiverName = it },
                label = { Text("প্রাপক স্টাফ *") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AddWelfareDialog(
    onDismiss: () -> Unit,
    onConfirm: (beneficiaryName: String, category: String, mobile: String, amount: Double, remarks: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("জরুরি চিকিৎসা সহায়তা") }
    var mobile by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }

    val categories = listOf(
        "জরুরি চিকিৎসা সহায়তা",
        "বিধবা ও এতিম ভাতা",
        "দরিদ্র শিক্ষা সহায়তা",
        "খাদ্য ও ত্রাণ সহায়তা",
        "ফ্রি মেডিকেল ক্যাম্প",
        "দুর্যোগকালীন ত্রাণ"
    )

    ExecutiveFormDialogShell(
        title = "সমাজকল্যাণ ও যাকাত আবেদন",
        subtitle = "দুস্থ, চিকিৎসা ও শিক্ষা অনুদান নিবন্ধন",
        icon = Icons.Default.Favorite,
        confirmText = "আবেদন জমা দিন",
        confirmTag = "confirm_add_welfare_button",
        onDismiss = onDismiss,
        onConfirmClick = {
            var valid = true
            if (name.isBlank()) {
                nameError = "সুবিধাভোগীর নাম লিখুন"
                valid = false
            }
            if (mobile.isBlank()) {
                mobileError = "মোবাইল নম্বর লিখুন"
                valid = false
            }
            if (valid) {
                val amt = amount.toDoubleOrNull() ?: 5000.0
                onConfirm(name.trim(), category, mobile.trim(), amt, remarks.trim())
            }
        }
    ) {
        MoreFormSection(title = "সুবিধাভোগী ও সহায়তার খাত", icon = Icons.Default.VolunteerActivism) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = { Text("আবেদনকারী / সুবিধাভোগীর নাম *") },
                isError = nameError != null,
                supportingText = nameError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("সহায়তার খাত নির্বাচন:", style = MaterialTheme.typography.labelMedium, color = MosqueTextSecondary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat, fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MosqueEmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = mobile,
                onValueChange = {
                    mobile = it
                    mobileError = null
                },
                label = { Text("যোগাযোগের মোবাইল নম্বর *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = mobileError != null,
                supportingText = mobileError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { ch -> ch.isDigit() } },
                label = { Text("সহায়তার পরিমাণ (৳) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = remarks,
                onValueChange = { remarks = it },
                label = { Text("বিবরণ বা যাচাইকৃত সুপারিশ") },
                minLines = 2,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, dateStr: String, timeStr: String, speaker: String, venue: String, budget: Double, lead: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf("২০২৪-১১-১৫") }
    var timeStr by remember { mutableStateOf("বাদ মাগরিব") }
    var speaker by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("মসজিদ প্রাঙ্গণ") }
    var budget by remember { mutableStateOf("50000") }
    var lead by remember { mutableStateOf("ইঞ্জি: আব্দুল্লাহ আল মামুন") }
    var titleError by remember { mutableStateOf<String?>(null) }

    val eventPresets = listOf("তাফসিরুল কুরআন মাহফিল", "শবে কদর দোয়া মাহফিল", "সীরাতুন্নবী (সা:) সেমিনার", "ইফতার মাহফিল")

    ExecutiveFormDialogShell(
        title = "নতুন মসজিদ অনুষ্ঠান / মাহফিল",
        subtitle = "ধর্মীয় আয়োজন, বক্তা ও বাজেট সূচি",
        icon = Icons.Default.Celebration,
        confirmText = "তালিকায় যুক্ত করুন",
        confirmTag = "confirm_add_event_button",
        onDismiss = onDismiss,
        onConfirmClick = {
            if (title.isBlank()) {
                titleError = "অনুষ্ঠানের নাম লিখুন"
            } else {
                val b = budget.toDoubleOrNull() ?: 20000.0
                onConfirm(title.trim(), dateStr.trim(), timeStr.trim(), speaker.trim(), venue.trim(), b, lead.trim())
            }
        }
    ) {
        MoreFormSection(title = "অনুষ্ঠানের পরিচিতি ও সময়", icon = Icons.Default.Event) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(eventPresets) { preset ->
                    FilterChip(
                        selected = title == preset,
                        onClick = {
                            title = preset
                            titleError = null
                        },
                        label = { Text(preset, fontSize = 11.sp) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = null
                },
                label = { Text("অনুষ্ঠানের নাম *") },
                isError = titleError != null,
                supportingText = titleError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text("তারিখ *") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = timeStr,
                    onValueChange = { timeStr = it },
                    label = { Text("সময় *") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        MoreFormSection(title = "মেহমান, স্থান ও বাজেট", icon = Icons.Default.LocationOn) {
            OutlinedTextField(
                value = speaker,
                onValueChange = { speaker = it },
                label = { Text("প্রধান বক্তা / মেহমান *") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = venue,
                onValueChange = { venue = it },
                label = { Text("অনুষ্ঠানের স্থান *") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it.filter { ch -> ch.isDigit() } },
                    label = { Text("বাজেট (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = lead,
                    onValueChange = { lead = it },
                    label = { Text("দলনেতা") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
