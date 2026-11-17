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
import com.mosqueseba.app.data.local.entity.MemberEntity
import com.mosqueseba.app.data.local.entity.StaffEntity
import com.mosqueseba.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ExecutiveFormDialogShell(
    title: String,
    subtitle: String,
    icon: ImageVector,
    confirmText: String,
    confirmTag: String = "dialog_confirm_button",
    confirmColor: Color = MosqueEmeraldPrimary,
    confirmIcon: ImageVector = Icons.Default.Check,
    errorBanner: String? = null,
    onDismiss: () -> Unit,
    onConfirmClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MehrabArchCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            backgroundColor = if (isDark) MosqueNightSurface else MosqueBgLight,
            borderColor = if (isDark) MosqueNightBorder else MosqueCardBorderGold,
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
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
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
                        .heightIn(max = 510.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!errorBanner.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MosqueUrgentRed.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, MosqueUrgentRed.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MosqueUrgentRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorBanner,
                                    color = MosqueUrgentRed,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    content()
                }

                HorizontalDivider(color = if (isDark) MosqueNightBorder else MosqueCardBorderGold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDark) MosqueNightSurface else Color.White)
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
                        Text(
                            text = "বাতিল",
                            color = if (isDark) MosqueNightTextPrimary else MosqueTextSecondary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Button(
                        onClick = onConfirmClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = confirmColor),
                        modifier = Modifier
                            .weight(1.3f)
                            .heightIn(min = 48.dp)
                            .testTag(confirmTag)
                    ) {
                        Icon(confirmIcon, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = confirmText,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoreFormSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderGold),
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
                    color = if (isDark) MosqueEmeraldPrimary.copy(alpha = 0.22f) else MosqueEmeraldSoft,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) MosqueGoldLight else MosqueEmeraldDark
                )
            }
            content()
        }
    }
}

@Composable
fun PaySalaryFormDialog(
    staff: StaffEntity,
    defaultMonth: String = ChandaMonthUtils.getCurrentMonthYear(),
    onDismiss: () -> Unit,
    onConfirm: (month: String, bonus: Double, paymentMethod: String) -> Unit
) {
    var month by remember(defaultMonth) { mutableStateOf(defaultMonth) }
    var bonus by remember { mutableStateOf("0") }
    var paymentMethod by remember { mutableStateOf("cash") }
    var monthError by remember { mutableStateOf<String?>(null) }

    val baseSalary = if (staff.salaryAmount > 0.0) staff.salaryAmount else {
        when (staff.roleCode.lowercase()) {
            "imam" -> 18000.0
            "muazzin" -> 12000.0
            else -> 9000.0
        }
    }
    val bonusAmt = bonus.toDoubleOrNull() ?: 0.0
    val netTotal = baseSalary + bonusAmt
    val currentCal = Calendar.getInstance()
    val curM = currentCal.get(Calendar.MONTH)
    val curY = currentCal.get(Calendar.YEAR)
    val prevMonth = ChandaMonthUtils.formatMonthYear((curM - 1 + 12) % 12, if (curM == 0) curY - 1 else curY)
    val nextMonth = ChandaMonthUtils.formatMonthYear((curM + 1) % 12, if (curM == 11) curY + 1 else curY)
    val monthPresets = remember(defaultMonth) {
        listOf(defaultMonth, prevMonth, nextMonth, "ঈদ বোনাসসহ").distinct()
    }
    val methodOptions = listOf(
        "cash" to "নগদ",
        "mobile_banking" to "বিকাশ/নগদ",
        "online" to "ব্যাংক/অনলাইন"
    )

    ExecutiveFormDialogShell(
        title = "বেতন ও ভাতা পরিশোধ",
        subtitle = "${staff.nameBn} (${staff.designationBn})",
        icon = Icons.Default.Payments,
        confirmText = "৳${toBengaliDigits(String.format("%,.0f", netTotal))} বেতন পরিশোধ করুন",
        confirmTag = "confirm_pay_salary_button",
        onDismiss = onDismiss,
        onConfirmClick = {
            if (month.isBlank()) {
                monthError = "বেতনের মাস উল্লেখ করুন"
            } else {
                onConfirm(month.trim(), bonusAmt, paymentMethod)
            }
        }
    ) {
        val isDark = LocalIsDarkTheme.current
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft.copy(alpha = 0.65f),
            border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderGold),
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
                    Text("মূল নির্ধারিত বেতন", fontSize = 11.sp, color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary)
                    Text(
                        text = "৳${toBengaliDigits(String.format("%,.0f", baseSalary))}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) MosqueGreenAccent else MosqueEmeraldDark
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("মোট প্রদেয় (ব্যয় খাত)", fontSize = 11.sp, color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary)
                    Text(
                        text = "৳${toBengaliDigits(String.format("%,.0f", netTotal))}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MosqueUrgentRed
                    )
                }
            }
        }

        MoreFormSection(title = "বেতন বিবরণ ও পরিশোধ মাধ্যম", icon = Icons.Default.ReceiptLong) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(monthPresets) { preset ->
                    FilterChip(
                        selected = month == preset,
                        onClick = {
                            month = preset
                            monthError = null
                        },
                        label = { Text(preset, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                            selectedLabelColor = if (isDark) MosqueEmeraldDark else Color.White,
                            containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                            labelColor = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                        )
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
                shape = RoundedCornerShape(14.dp),
                colors = mosqueStandardFieldColors(isDark),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = bonus,
                onValueChange = { bonus = it.filter { ch -> ch.isDigit() } },
                label = { Text("বোনাস / অতিরিক্ত ভাতা (৳)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                colors = mosqueStandardFieldColors(isDark),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "পরিশোধের মাধ্যম:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) MosqueGoldLight else MosqueEmeraldDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                methodOptions.forEach { (key, label) ->
                    FilterChip(
                        selected = paymentMethod == key,
                        onClick = { paymentMethod = key },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                            selectedLabelColor = if (isDark) MosqueEmeraldDark else Color.White,
                            containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                            labelColor = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                        )
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MosqueUrgentRed.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, MosqueUrgentRed.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MosqueUrgentRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "বেতন প্রদান করলে তা স্বয়ংক্রিয়ভাবে 'ইমাম ও কর্মচারীদের বেতন' খাতে মসজিদের মোট ব্যয়ের সাথে যুক্ত হবে — ম্যানুয়ালি যুক্ত করতে হবে না।",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 15.sp
                )
            }
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
    var roleError by remember { mutableStateOf<String?>(null) }

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
            if (roleTitle.isBlank()) {
                roleError = "দায়িত্বের ধরন উল্লেখ করুন"
                valid = false
            }
            if (valid) {
                onConfirm(selectedStaff, dayOfWeek.trim(), prayerName.trim(), roleTitle.trim(), notes.trim())
            }
        }
    ) {
        val isDark = LocalIsDarkTheme.current
        MoreFormSection(title = "দায়িত্বপ্রাপ্ত স্টাফ নির্বাচন", icon = Icons.Default.Person) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(activeStaff, key = { it.id }) { staff ->
                    FilterChip(
                        selected = selectedStaff.id == staff.id,
                        onClick = { selectedStaff = staff },
                        leadingIcon = {
                            MosqueMemberAvatar(
                                nameBn = staff.nameBn,
                                photoUrl = null,
                                size = 18.dp
                            )
                        },
                        label = { Text("${staff.nameBn} (${staff.designationBn})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                            selectedLabelColor = if (isDark) MosqueEmeraldDark else Color.White,
                            containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                            labelColor = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
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
                        label = { Text(preset, fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                            labelColor = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary
                        ),
                        border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle)
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
                shape = RoundedCornerShape(14.dp),
                colors = mosqueStandardFieldColors(isDark),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
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
                shape = RoundedCornerShape(14.dp),
                colors = mosqueStandardFieldColors(isDark),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(rolePresets) { preset ->
                    FilterChip(
                        selected = roleTitle == preset,
                        onClick = { roleTitle = preset },
                        label = { Text(preset, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                            selectedLabelColor = if (isDark) MosqueEmeraldDark else Color.White,
                            containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                            labelColor = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = roleTitle,
                onValueChange = {
                    roleTitle = it
                    roleError = null
                },
                label = { Text("দায়িত্ব (ইমামতি / আজান) *") },
                isError = roleError != null,
                supportingText = roleError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(14.dp),
                colors = mosqueStandardFieldColors(isDark),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("বিশেষ নোট (ঐচ্ছিক)") },
                shape = RoundedCornerShape(14.dp),
                colors = mosqueStandardFieldColors(isDark),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AddFoodScheduleFormDialog(
    willingDonors: List<MemberEntity> = emptyList(),
    mealStaff: List<StaffEntity> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (dateStr: String, providerName: String, providerMobile: String, receiverName: String) -> Unit
) {
    val isoFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH) }
    val todayIso = remember { isoFormat.format(Date()) }
    val tomorrowIso = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.let { isoFormat.format(it.time) }
    }

    val staffOptions = remember(mealStaff) {
        if (mealStaff.isEmpty()) {
            listOf("পেশ ইমাম", "মুয়াজ্জিন", "প্রধান খাদেম")
        } else {
            mealStaff.map { "${it.nameBn} (${it.designationBn})" }
        }
    }

    val suggestedDonor = remember(willingDonors) { willingDonors.firstOrNull() }
    var dateStr by remember { mutableStateOf(tomorrowIso) }
    var providerName by remember { mutableStateOf(suggestedDonor?.nameBn.orEmpty()) }
    var providerMobile by remember { mutableStateOf(suggestedDonor?.mobile.orEmpty()) }
    var receiverName by remember { mutableStateOf(staffOptions.first()) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var providerError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }
    var receiverError by remember { mutableStateOf<String?>(null) }

    ExecutiveFormDialogShell(
        title = "একক খাবার বণ্টন ফর্ম",
        subtitle = "একদিনে ১ জন সদস্য → ১ জন বেতনভুক্ত স্টাফ",
        icon = Icons.Default.Restaurant,
        confirmText = "সংরক্ষণ করুন",
        confirmTag = "confirm_add_food_button",
        onDismiss = onDismiss,
        onConfirmClick = {
            var valid = true
            if (dateStr.isBlank()) {
                dateError = "তারিখ লিখুন (বছর-মাস-দিন)"
                valid = false
            }
            if (providerName.isBlank()) {
                providerError = "খাবার প্রদানকারীর নাম লিখুন"
                valid = false
            }
            if (providerMobile.isBlank()) {
                mobileError = "দাতার মোবাইল নম্বর লিখুন"
                valid = false
            }
            if (receiverName.isBlank()) {
                receiverError = "প্রাপক স্টাফ নির্বাচন বা লিখুন"
                valid = false
            }
            if (valid) {
                onConfirm(dateStr.trim(), providerName.trim(), providerMobile.trim(), receiverName.trim())
            }
        }
    ) {
        val isDark = LocalIsDarkTheme.current
        MoreFormSection(title = "তারিখ নির্বাচন", icon = Icons.Default.Schedule) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = dateStr == todayIso,
                    onClick = { dateStr = todayIso; dateError = null },
                    label = { Text("আজ (${toBengaliDigits(todayIso)})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                        selectedLabelColor = if (isDark) MosqueEmeraldDark else Color.White,
                        containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                        labelColor = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                    )
                )
                FilterChip(
                    selected = dateStr == tomorrowIso,
                    onClick = { dateStr = tomorrowIso; dateError = null },
                    label = { Text("আগামীকাল", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                        selectedLabelColor = if (isDark) MosqueEmeraldDark else Color.White,
                        containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                        labelColor = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                    )
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = dateStr,
                onValueChange = { dateStr = it; dateError = null },
                label = { Text("তারিখ (বছর-মাস-দিন) *") },
                isError = dateError != null,
                supportingText = dateError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(14.dp),
                colors = mosqueStandardFieldColors(isDark),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            )
        }

        MoreFormSection(title = "দাতা ও প্রাপক স্টাফ", icon = Icons.Default.People) {
            if (willingDonors.isNotEmpty()) {
                Text(
                    text = "খাবার দিতে রাজি সদস্য (${toBengaliDigits(willingDonors.size.toString())} জন) — ট্যাপ করে নির্বাচন করুন:",
                    fontSize = 11.5.sp,
                    color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(willingDonors, key = { it.id }) { donor ->
                        val isNextInTurn = donor.id == suggestedDonor?.id
                        val isSelected = providerName.trim().equals(donor.nameBn.trim(), ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                providerName = donor.nameBn
                                providerMobile = donor.mobile
                                providerError = null
                                mobileError = null
                            },
                            leadingIcon = {
                                MosqueMemberAvatar(
                                    nameBn = donor.nameBn,
                                    photoUrl = donor.photoUrl,
                                    size = 18.dp
                                )
                            },
                            label = {
                                Text(
                                    text = if (isNextInTurn) "${donor.nameBn} (পরবর্তী পালা)" else donor.nameBn,
                                    fontSize = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                                selectedLabelColor = if (isDark) MosqueEmeraldDark else Color.White,
                                containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                                labelColor = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = providerName,
                onValueChange = {
                    providerName = it
                    providerError = null
                },
                label = { Text("খাবার প্রদানকারী দাতার নাম *") },
                isError = providerError != null,
                supportingText = providerError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(14.dp),
                colors = mosqueStandardFieldColors(isDark),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = providerMobile,
                onValueChange = {
                    providerMobile = it
                    mobileError = null
                },
                label = { Text("দাতার মোবাইল নম্বর *") },
                isError = mobileError != null,
                supportingText = mobileError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(14.dp),
                colors = mosqueStandardFieldColors(isDark),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "খাবার প্রাপক বেতনভুক্ত স্টাফ নির্বাচন:",
                fontSize = 11.5.sp,
                color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(staffOptions) { option ->
                    FilterChip(
                        selected = receiverName == option,
                        onClick = {
                            receiverName = option
                            receiverError = null
                        },
                        label = { Text(option, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                            selectedLabelColor = if (isDark) MosqueEmeraldDark else Color.White,
                            containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                            labelColor = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = receiverName,
                onValueChange = {
                    receiverName = it
                    receiverError = null
                },
                label = { Text("প্রাপক স্টাফ *") },
                isError = receiverError != null,
                supportingText = receiverError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(14.dp),
                colors = mosqueStandardFieldColors(isDark),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            )
        }
    }
}

@Composable
fun AutoFoodScheduleDialog(
    willingDonors: List<MemberEntity>,
    mealStaff: List<StaffEntity>,
    onDismiss: () -> Unit,
    onGenerate: (daysCount: Int, startDateIso: String, targetStaffLabels: List<String>) -> Unit
) {
    val isoFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH) }
    val todayIso = remember { isoFormat.format(Date()) }
    val tomorrowIso = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.let { isoFormat.format(it.time) }
    }

    val allStaffLabels = remember(mealStaff) {
        if (mealStaff.isEmpty()) {
            listOf("পেশ ইমাম", "মুয়াজ্জিন", "প্রধান খাদেম")
        } else {
            mealStaff.map { "${it.nameBn} (${it.designationBn})" }
        }
    }

    var startDateIso by remember { mutableStateOf(tomorrowIso) }
    var daysCount by remember { mutableIntStateOf(7) }
    var selectedMode by remember { mutableStateOf("ALL") } // "ALL" or specific staff label
    var startDateError by remember { mutableStateOf<String?>(null) }
    var donorWarningBanner by remember { mutableStateOf<String?>(null) }

    val durationOptions = listOf(7 to "৭ দিন (সাপ্তাহিক)", 15 to "১৫ দিন (পাক্ষিক)", 30 to "৩০ দিন (মাসিক)")
    val targetStaffLabels = remember(selectedMode, allStaffLabels) {
        if (selectedMode == "ALL") allStaffLabels else listOf(selectedMode)
    }

    val plannedSlotCount = daysCount * targetStaffLabels.size
    val avgCycleDays = remember(willingDonors.size, targetStaffLabels.size) {
        if (willingDonors.isEmpty() || targetStaffLabels.isEmpty()) 0.0
        else willingDonors.size.toDouble() / targetStaffLabels.size.toDouble()
    }

    ExecutiveFormDialogShell(
        title = "অটো খাবার শিডিউল জেনারেটর",
        subtitle = "১ দিন আগে নোটিফিকেশনসহ স্টাফ ও সদস্য অনুযায়ী স্বয়ংক্রিয় ভাগ",
        icon = Icons.Default.AutoAwesome,
        confirmText = if (willingDonors.isEmpty()) "দাতা নেই" else "অটো তৈরি করুন",
        confirmTag = "confirm_auto_food_button",
        errorBanner = donorWarningBanner,
        onDismiss = onDismiss,
        onConfirmClick = {
            when {
                willingDonors.isEmpty() -> {
                    donorWarningBanner = "অটো শিডিউল তৈরির আগে অন্তত ১ জন সদস্যের প্রোফাইলে খাবার প্রদানের সম্মতি চালু করুন।"
                }
                startDateIso.isBlank() -> {
                    startDateError = "শুরুর তারিখ লিখুন (বছর-মাস-দিন)"
                }
                targetStaffLabels.isEmpty() -> {
                    donorWarningBanner = "কমপক্ষে ১ জন প্রাপক স্টাফ নির্বাচন করুন।"
                }
                else -> {
                    onGenerate(daysCount, startDateIso.trim(), targetStaffLabels)
                }
            }
        }
    ) {
        val isDark = LocalIsDarkTheme.current
        MoreFormSection(title = "দাতা ও প্রাপক অনুপাত", icon = Icons.Default.Groups) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) MosqueEmeraldPrimary.copy(alpha = 0.22f) else MosqueEmeraldSoft
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("রাজি দাতা সদস্য", fontSize = 11.sp, color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary)
                        Text(
                            text = "${toBengaliDigits(willingDonors.size.toString())} জন",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) MosqueGreenAccent else MosqueEmeraldDark
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) MosqueGold.copy(alpha = 0.2f) else MosqueGoldLight.copy(alpha = 0.25f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("খাবার প্রাপক স্টাফ", fontSize = 11.sp, color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary)
                        Text(
                            text = "${toBengaliDigits(allStaffLabels.size.toString())} জন",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) MosqueGoldLight else MosqueGoldDeep
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (willingDonors.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MosqueUrgentRed.copy(alpha = if (isDark) 0.15f else 0.1f),
                    border = BorderStroke(1.dp, MosqueUrgentRed.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "কোনো সদস্য 'খাবার দিতে রাজি' হিসেবে চিহ্নিত নেই। আগে সদস্য তালিকা থেকে দাতা সদস্যদের প্রোফাইলে 'হ্যাঁ, রাজি আছেন' চালু করুন।",
                        fontSize = 11.5.sp,
                        color = MosqueUrgentRed,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            } else {
                Text(
                    text = "রোটেশন সিরিয়াল: ${willingDonors.take(5).joinToString(" → ") { it.nameBn }}${if (willingDonors.size > 5) " (+${toBengaliDigits((willingDonors.size - 5).toString())} জন)" else ""}",
                    fontSize = 11.5.sp,
                    color = if (isDark) MosqueGoldLight else MosqueEmeraldDark,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        MoreFormSection(title = "তারিখ ও প্রাপক স্টাফ বণ্টন", icon = Icons.Default.DateRange) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = startDateIso == todayIso,
                    onClick = { startDateIso = todayIso; startDateError = null },
                    label = { Text("আজ থেকে শুরু", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                        selectedLabelColor = if (isDark) MosqueEmeraldDark else Color.White,
                        containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                        labelColor = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                    )
                )
                FilterChip(
                    selected = startDateIso == tomorrowIso,
                    onClick = { startDateIso = tomorrowIso; startDateError = null },
                    label = { Text("আগামীকাল থেকে", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                        selectedLabelColor = if (isDark) MosqueEmeraldDark else Color.White,
                        containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                        labelColor = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                    )
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = startDateIso,
                onValueChange = { startDateIso = it; startDateError = null },
                label = { Text("শুরুর তারিখ (বছর-মাস-দিন) *") },
                isError = startDateError != null,
                supportingText = startDateError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(14.dp),
                colors = mosqueStandardFieldColors(isDark),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("কত দিনের শিডিউল তৈরি হবে:", fontSize = 11.5.sp, color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(durationOptions) { (days, label) ->
                    FilterChip(
                        selected = daysCount == days,
                        onClick = { daysCount = days },
                        label = { Text(label, fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                            selectedLabelColor = if (isDark) MosqueEmeraldDark else Color.White,
                            containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                            labelColor = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("কাদের জন্য খাবার শিডিউল ভাগ হবে:", fontSize = 11.5.sp, color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = selectedMode == "ALL",
                        onClick = { selectedMode = "ALL" },
                        label = { Text("সবাই (${toBengaliDigits(allStaffLabels.size.toString())} জন স্টাফ)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                            selectedLabelColor = if (isDark) MosqueEmeraldDark else Color.White,
                            containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                            labelColor = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                        )
                    )
                }
                items(allStaffLabels) { label ->
                    FilterChip(
                        selected = selectedMode == label,
                        onClick = { selectedMode = label },
                        label = { Text(label.substringBefore(" ("), fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                            selectedLabelColor = if (isDark) MosqueEmeraldDark else Color.White,
                            containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                            labelColor = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                        )
                    )
                }
            }
        }

        MoreFormSection(title = "স্বয়ংক্রিয় ভাগ ও নোটিফিকেশন প্রিভিউ", icon = Icons.Default.RestaurantMenu) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MosqueInfoBlue.copy(alpha = if (isDark) 0.15f else 0.08f),
                border = BorderStroke(1.dp, MosqueInfoBlue.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "অটো-বণ্টন সারসংক্ষেপ:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) MosqueInfoBlue else MosqueInfoBlue
                    )
                    Text(
                        text = "• প্রতিদিন ${toBengaliDigits(targetStaffLabels.size.toString())} জন ভিন্ন দাতা সদস্য ${toBengaliDigits(targetStaffLabels.size.toString())} জন স্টাফের খাবার দেবেন (মোট ${toBengaliDigits(plannedSlotCount.toString())} টি পালা)।",
                        fontSize = 11.sp,
                        color = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary
                    )
                    if (avgCycleDays > 0) {
                        Text(
                            text = "• ${toBengaliDigits(willingDonors.size.toString())} জন সদস্যের মধ্যে ভাগ হলে গড়ে প্রতি ${toBengaliDigits(String.format(Locale.ENGLISH, "%.1f", avgCycleDays))} দিন পর পর একজনের পালা আসবে।",
                            fontSize = 11.sp,
                            color = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary
                        )
                    }
                    Text(
                        text = "• প্রতিটি পালার ১ দিন আগে নির্ধারিত সদস্যের নোটিফিকেশনে সম্মতি বা অপারগতা জানানোর অনুরোধ যাবে।",
                        fontSize = 11.sp,
                        color = if (isDark) MosqueGreenAccent else MosqueEmeraldDark,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
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
    var amountError by remember { mutableStateOf<String?>(null) }

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
            val amt = amount.toDoubleOrNull() ?: 0.0
            if (amt <= 0.0) {
                amountError = "সহায়তার সঠিক পরিমাণ (৳) লিখুন"
                valid = false
            }
            if (valid) {
                onConfirm(name.trim(), category, mobile.trim(), amt, remarks.trim())
            }
        }
    ) {
        val isDark = LocalIsDarkTheme.current
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
                shape = RoundedCornerShape(14.dp),
                colors = mosqueStandardFieldColors(isDark),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("সহায়তার খাত নির্বাচন:", style = MaterialTheme.typography.labelMedium, color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat, fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                            selectedLabelColor = if (isDark) MosqueEmeraldDark else Color.White,
                            containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                            labelColor = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
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
                shape = RoundedCornerShape(14.dp),
                colors = mosqueStandardFieldColors(isDark),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it.filter { ch -> ch.isDigit() }
                    amountError = null
                },
                label = { Text("সহায়তার পরিমাণ (৳) *") },
                isError = amountError != null,
                supportingText = amountError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                colors = mosqueStandardFieldColors(isDark),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = remarks,
                onValueChange = { remarks = it },
                label = { Text("বিবরণ বা যাচাইকৃত সুপারিশ") },
                minLines = 2,
                shape = RoundedCornerShape(14.dp),
                colors = mosqueStandardFieldColors(isDark),
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
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var title by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf(todayDate) }
    var timeStr by remember { mutableStateOf("বাদ মাগরিব") }
    var speaker by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("মসজিদ প্রাঙ্গণ") }
    var budget by remember { mutableStateOf("") }
    var lead by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var speakerError by remember { mutableStateOf<String?>(null) }
    var venueError by remember { mutableStateOf<String?>(null) }

    val isDark = LocalIsDarkTheme.current
    val eventPresets = listOf("তাফসিরুল কুরআন মাহফিল", "শবে কদর দোয়া মাহফিল", "সীরাতুন্নবী (সা:) সেমিনার", "ইফতার মাহফিল")

    ExecutiveFormDialogShell(
        title = "নতুন মসজিদ অনুষ্ঠান / মাহফিল",
        subtitle = "ধর্মীয় আয়োজন, বক্তা ও বাজেট সূচি",
        icon = Icons.Default.Celebration,
        confirmText = "তালিকায় যুক্ত করুন",
        confirmTag = "confirm_add_event_button",
        onDismiss = onDismiss,
        onConfirmClick = {
            var valid = true
            if (title.isBlank()) {
                titleError = "অনুষ্ঠানের নাম লিখুন"
                valid = false
            }
            if (dateStr.isBlank()) {
                dateError = "তারিখ উল্লেখ করুন"
                valid = false
            }
            if (speaker.isBlank()) {
                speakerError = "বক্তা বা মেহমানের নাম লিখুন"
                valid = false
            }
            if (venue.isBlank()) {
                venueError = "অনুষ্ঠানের স্থান লিখুন"
                valid = false
            }
            if (valid) {
                val b = budget.toDoubleOrNull() ?: 0.0
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
                        label = { Text(preset, fontSize = 11.sp, fontWeight = if (title == preset) FontWeight.SemiBold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                            selectedLabelColor = if (isDark) MosqueEmeraldDark else Color.White,
                            containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                            labelColor = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = title == preset,
                            borderColor = if (isDark) MosqueNightBorder else MosqueCardBorderGold,
                            selectedBorderColor = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary
                        )
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
                shape = RoundedCornerShape(14.dp),
                colors = mosqueStandardFieldColors(isDark),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it; dateError = null },
                    label = { Text("তারিখ *") },
                    isError = dateError != null,
                    supportingText = dateError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                    shape = RoundedCornerShape(14.dp),
                    colors = mosqueStandardFieldColors(isDark),
                    singleLine = true,
                    modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                )
                OutlinedTextField(
                    value = timeStr,
                    onValueChange = { timeStr = it },
                    label = { Text("সময় *") },
                    shape = RoundedCornerShape(14.dp),
                    colors = mosqueStandardFieldColors(isDark),
                    singleLine = true,
                    modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                )
            }
        }

        MoreFormSection(title = "মেহমান, স্থান ও বাজেট", icon = Icons.Default.LocationOn) {
            OutlinedTextField(
                value = speaker,
                onValueChange = { speaker = it; speakerError = null },
                label = { Text("প্রধান বক্তা / মেহমান *") },
                isError = speakerError != null,
                supportingText = speakerError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(14.dp),
                colors = mosqueStandardFieldColors(isDark),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = venue,
                onValueChange = { venue = it; venueError = null },
                label = { Text("অনুষ্ঠানের স্থান *") },
                isError = venueError != null,
                supportingText = venueError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                shape = RoundedCornerShape(14.dp),
                colors = mosqueStandardFieldColors(isDark),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it.filter { ch -> ch.isDigit() } },
                    label = { Text("বাজেট (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = mosqueStandardFieldColors(isDark),
                    singleLine = true,
                    modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                )
                OutlinedTextField(
                    value = lead,
                    onValueChange = { lead = it },
                    label = { Text("দলনেতা") },
                    shape = RoundedCornerShape(14.dp),
                    colors = mosqueStandardFieldColors(isDark),
                    singleLine = true,
                    modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                )
            }
        }
    }
}
