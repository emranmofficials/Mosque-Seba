package com.mosqueseba.app.ui.screens

import java.util.Locale
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
import com.mosqueseba.app.data.local.entity.ChandaPaymentEntity
import com.mosqueseba.app.data.local.entity.StaffEntity
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.OfficialReceiptDialog
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel

@Composable
fun MoreScreen(
    viewModel: MosqueViewModel,
    initialSubModule: String? = null,
    onBack: (() -> Unit)? = null,
    onOpenReceiveChanda: () -> Unit,
    onOpenScheduleMeeting: () -> Unit
) {
    var selectedSubModule by remember(initialSubModule) { mutableStateOf<String?>(initialSubModule) }
    var selectedPaymentForReceipt by remember { mutableStateOf<ChandaPaymentEntity?>(null) }
    var showAddStaffDialog by remember { mutableStateOf(false) }
    var showPaySalaryStaff by remember { mutableStateOf<StaffEntity?>(null) }
    var showAddRosterDialog by remember { mutableStateOf(false) }
    var showAddFoodDialog by remember { mutableStateOf(false) }
    var showAddWelfareDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    val currentRole by viewModel.currentUserRole.collectAsState()
    val chandaPayments by viewModel.allChandaPayments.collectAsState()
    val activeStaff by viewModel.activeStaff.collectAsState()
    val auditLogs by viewModel.recentAuditLogs.collectAsState()
    val mosque by viewModel.mosque.collectAsState()
    val dutyRosters by viewModel.allDutyRosters.collectAsState()
    val foodSchedules by viewModel.allFoodSchedules.collectAsState()
    val payrolls by viewModel.allPayrolls.collectAsState()
    val allMeetings by viewModel.allMeetings.collectAsState()
    val welfareCases by viewModel.allWelfareCases.collectAsState()
    val events by viewModel.allEvents.collectAsState()

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
                IconButton(onClick = { 
                    selectedSubModule = null 
                    onBack?.invoke()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MosqueEmeraldDark)
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            Column {
                Text(
                    text = when (selectedSubModule) {
                        "madrasa" -> "মাদরাসা ও মক্তব পরিচালনা"
                        "chanda" -> "চাঁদা আদায় ও রসিদ রেজিস্টার"
                        "staff" -> "ইমাম ও কর্মচারী বেতন ও পে-রোল"
                        "roster" -> "ওয়াক্ত ও ইমাম-মুয়াজ্জিন ডিউটি রোস্টার"
                        "food" -> "ইমাম ও খাদেমদের খাবার শিডিউল"
                        "meetings" -> "কমিটি সভা ও এজেন্ডা রেজুলেশন"
                        "welfare" -> "সমাজকল্যাণ ও যাকাত তহবিল"
                        "events" -> "মাহফিল ও বিশেষ অনুষ্ঠান"
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
            "madrasa" -> {
                MadrasaScreen(viewModel = viewModel)
            }
            "chanda" -> {
                // Chanda Ledger / My Chanda
                val isGeneralMember = currentRole == UserRole.GENERAL_MEMBER
                val canReceiveChanda = currentRole in listOf(UserRole.TREASURER, UserRole.PRESIDENT, UserRole.VICE_PRESIDENT)
                val displayedPayments = if (isGeneralMember) {
                    chandaPayments.take(2)
                } else chandaPayments

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
                                    Text(
                                        if (isGeneralMember) "আমার ব্যক্তিগত চাঁদা স্থিতি" else "মাসিক চাঁদা আদায় রেজিস্টার",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        if (isGeneralMember) "৳১,৫০০ (পরিশোধিত)" else "মোট আদায়: ৳৮,০০০",
                                        color = MosqueGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                                if (canReceiveChanda) {
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
                    }

                    items(displayedPayments, key = { it.id }) { payment ->
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
                // Staff Directory & Payroll / Personal Staff Compensation
                val isStaffRole = currentRole in listOf(UserRole.IMAM, UserRole.MUAZZIN, UserRole.KHADEM)
                val canManageStaff = currentRole in listOf(UserRole.TREASURER, UserRole.PRESIDENT, UserRole.VICE_PRESIDENT)
                val displayedStaff = if (isStaffRole) {
                    activeStaff.filter {
                        when (currentRole) {
                            UserRole.IMAM -> it.roleCode.equals("imam", ignoreCase = true)
                            UserRole.MUAZZIN -> it.roleCode.equals("muazzin", ignoreCase = true)
                            UserRole.KHADEM -> it.roleCode.equals("khadem", ignoreCase = true)
                            else -> true
                        }
                    }.ifEmpty { activeStaff.take(1) }
                } else activeStaff

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
                                text = if (isStaffRole) "আমার দায়িত্ব ও বেতন বিবরণী" else "মসজিদের সম্মানিত স্টাফবৃন্দ (${activeStaff.size} জন)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldDark
                            )

                            if (canManageStaff) {
                                Button(
                                    onClick = { showAddStaffDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                ) {
                                    Text("+ নতুন স্টাফ", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    items(displayedStaff, key = { it.id }) { staff ->
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

                                Spacer(modifier = Modifier.height(10.dp))
                                if (canManageStaff) {
                                    Button(
                                        onClick = { showPaySalaryStaff = staff },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("বেতন পরিশোধ করুন (পে-রোল)", fontSize = 12.sp)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MosqueSuccessGreen.copy(alpha = 0.12f))
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MosqueSuccessGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "চলতি মাসের বেতন ও ভাতা পরিশোধিত (রসিদ সংগৃহীত)",
                                                color = MosqueSuccessGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
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
            "roster" -> {
                // Duty Roster
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
                                text = "ওয়াক্ত ও নামাজের দায়িত্ব বণ্টন",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldDark
                            )
                            val canAssignRoster = currentRole in listOf(UserRole.PRESIDENT, UserRole.VICE_PRESIDENT, UserRole.GENERAL_SECRETARY, UserRole.IMAM)
                            if (canAssignRoster) {
                                Button(
                                    onClick = { showAddRosterDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                ) {
                                    Text("+ দায়িত্ব নির্ধারণ", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    items(dutyRosters, key = { it.id }) { roster ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = roster.prayerName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueEmeraldDark
                                    )
                                    Text(
                                        text = roster.dayOfWeek,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MosqueGoldDeep,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "দায়িত্বপ্রাপ্ত: ${roster.staffNameBn} (${roster.roleTitle})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MosqueTextPrimary
                                )
                                if (roster.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "বিশেষ দ্রষ্টব্য: ${roster.notes}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MosqueTextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
            "food" -> {
                // Food Schedules
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
                                text = "ইমাম ও খাদেমদের খাবার শিডিউল",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldDark
                            )
                            val canAssignFood = currentRole in listOf(UserRole.PRESIDENT, UserRole.VICE_PRESIDENT, UserRole.GENERAL_SECRETARY, UserRole.ORGANIZING_SECRETARY, UserRole.IMAM)
                            if (canAssignFood) {
                                Button(
                                    onClick = { showAddFoodDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                ) {
                                    Text("+ খাবার বণ্টন", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    items(foodSchedules, key = { it.id }) { food ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "তারিখ: ${food.dateStr} (${food.mealType})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueEmeraldDark
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MosqueSuccessGreen.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (food.status == "delivered") "বিতরিত" else "নির্ধারিত",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MosqueSuccessGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "খাবার প্রেরক: ${food.providerName} (${food.providerMobile})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MosqueTextPrimary
                                )
                                Text(
                                    text = "প্রাপক: ${food.receiverStaffName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueTextSecondary
                                )
                            }
                        }
                    }
                }
            }
            "meetings" -> {
                // Meetings & Agenda
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
                                    Text(
                                        text = "নির্ধারিত কমিটি সভা",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        text = "${allMeetings.size} টি সভা নথিবদ্ধ",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueGold
                                    )
                                }
                                val canScheduleMeeting = currentRole in listOf(UserRole.PRESIDENT, UserRole.VICE_PRESIDENT, UserRole.GENERAL_SECRETARY, UserRole.ORGANIZING_SECRETARY)
                                if (canScheduleMeeting) {
                                    Button(
                                        onClick = onOpenScheduleMeeting,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueGold)
                                    ) {
                                        Text(
                                            text = "+ সভা আহ্বান",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueEmeraldDark
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (allMeetings.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "বর্তমানে কোনো নির্ধারিত সভা নেই",
                                    color = MosqueTextSecondary
                                )
                            }
                        }
                    } else {
                        items(allMeetings, key = { it.id }) { meeting ->
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
                                        Text(
                                            text = meeting.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueEmeraldDark
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (meeting.status == "completed") MosqueSuccessGreen.copy(alpha = 0.12f)
                                                    else MosqueGold.copy(alpha = 0.18f)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (meeting.status == "completed") "সম্পন্ন" else "আসন্ন সভা",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (meeting.status == "completed") MosqueSuccessGreen else MosqueGoldDeep,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Event,
                                            contentDescription = null,
                                            tint = MosqueEmeraldPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = meeting.meetingDate,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MosqueTextPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                            text = "স্থান: ${meeting.location}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MosqueTextSecondary
                                        )
                                    }

                                    if (meeting.agenda.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider()
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "আলোচ্য এজেন্ডা:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueEmeraldDark
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = meeting.agenda,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MosqueTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "welfare" -> {
                // Welfare & Zakat Management (PRD §26)
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
                                    Text(
                                        text = "যাকাত ও সমাজকল্যাণ বিতরণ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    val totalDisbursed = welfareCases.sumOf { it.amountApproved }
                                    Text(
                                        text = "৳ ${String.format(Locale.getDefault(), "%,.0f", totalDisbursed)}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueGold
                                    )
                                }
                                Button(
                                    onClick = { showAddWelfareDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueGold)
                                ) {
                                    Text(
                                        text = "+ অনুদান আবেদন",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueEmeraldDark
                                    )
                                }
                            }
                        }
                    }

                    if (welfareCases.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "বর্তমানে কোনো সমাজকল্যাণ আবেদন নেই",
                                    color = MosqueTextSecondary
                                )
                            }
                        }
                    } else {
                        items(welfareCases, key = { it.id }) { item ->
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
                                        Text(
                                            text = item.beneficiaryName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueEmeraldDark
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (item.status == "disbursed") MosqueSuccessGreen.copy(alpha = 0.12f)
                                                    else MosqueGold.copy(alpha = 0.18f)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (item.status == "disbursed") "বিতরিত" else "অনুমোদিত",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (item.status == "disbursed") MosqueSuccessGreen else MosqueGoldDeep,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "খাত: ${item.category}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MosqueTextSecondary
                                        )
                                        Text(
                                            text = "অনুমোদিত: ৳${item.amountApproved.toInt()}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueEmeraldPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "মোবাইল: ${item.mobile}  •  তারিখ: ${item.applicationDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MosqueTextMuted
                                    )

                                    if (item.remarks.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "মন্তব্য: ${item.remarks}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MosqueTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "events" -> {
                // Mosque Events & Programs (PRD §25)
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
                                    Text(
                                        text = "মসজিদ মাহফিল ও অনুষ্ঠান",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        text = "${events.size} টি ইভেন্ট তালিকাভুক্ত",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueGold
                                    )
                                }
                                val canAddEvent = currentRole in listOf(UserRole.PRESIDENT, UserRole.VICE_PRESIDENT, UserRole.GENERAL_SECRETARY, UserRole.ORGANIZING_SECRETARY)
                                if (canAddEvent) {
                                    Button(
                                        onClick = { showAddEventDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueGold)
                                    ) {
                                        Text(
                                            text = "+ নতুন ইভেন্ট",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueEmeraldDark
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (events.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "বর্তমানে কোনো আসন্ন অনুষ্ঠান নেই",
                                    color = MosqueTextSecondary
                                )
                            }
                        }
                    } else {
                        items(events, key = { it.id }) { ev ->
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
                                        Text(
                                            text = ev.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueEmeraldDark
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (ev.status == "completed") MosqueSuccessGreen.copy(alpha = 0.12f)
                                                    else MosqueGold.copy(alpha = 0.18f)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (ev.status == "completed") "সম্পন্ন" else "আসন্ন",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (ev.status == "completed") MosqueSuccessGreen else MosqueGoldDeep,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "তারিখ ও সময়: ${ev.dateStr} (${ev.timeStr})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MosqueTextPrimary
                                    )
                                    Text(
                                        text = "বক্তা/অতিথি: ${ev.speakerOrGuest}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MosqueTextSecondary
                                    )
                                    Text(
                                        text = "স্থান: ${ev.venue}  •  বাজেট: ৳${ev.budget.toInt()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MosqueTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "স্বেচ্ছাসেবক প্রধান: ${ev.volunteerLead}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MosqueEmeraldPrimary
                                    )
                                }
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
                                DetailItemRow("অ্যাপ প্ল্যাটফর্ম", "মসজিদ সেবা (Mosque Seba)")
                                DetailItemRow("অফিসিয়াল স্লোগান", "মসজিদের খেদমতে আধুনিক প্রযুক্তি, পরিচালনায় স্বচ্ছ আস্থা")
                                DetailItemRow("প্যাকেজ আইডি", "com.mosqueseba.app")
                            }
                        }
                    }
                }
            }
            else -> {
                // Main Operations Menu strictly filtered by role responsibility
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (currentRole) {
                        UserRole.TREASURER -> {
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
                                    description = "পেশ ইমাম, মুয়াজ্জিন ও খাদেমদের বেতন বিতরণ ও পে-রোল",
                                    icon = Icons.Default.Badge,
                                    iconColor = MosqueInfoBlue,
                                    onClick = { selectedSubModule = "staff" }
                                )
                            }
                            item {
                                OperationMenuItem(
                                    title = "মাদরাসা ও মক্তব ফি আদায়",
                                    description = "হিফজ ও মক্তব শিক্ষার্থীদের মাসিক বেতন ও ফি কালেকশন",
                                    icon = Icons.Default.School,
                                    iconColor = MosqueEmeraldPrimary,
                                    onClick = { selectedSubModule = "madrasa" }
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
                        UserRole.GENERAL_SECRETARY, UserRole.ORGANIZING_SECRETARY, UserRole.EXECUTIVE_MEMBER -> {
                            item {
                                OperationMenuItem(
                                    title = "কমিটি সভা ও এজেন্ডা",
                                    description = "আসন্ন মিটিং নির্ধারণ ও পূর্ববর্তী রেজুলেশন",
                                    icon = Icons.Default.Event,
                                    iconColor = MosqueGoldDeep,
                                    onClick = { selectedSubModule = "meetings" }
                                )
                            }
                            item {
                                OperationMenuItem(
                                    title = "সমাজকল্যাণ ও যাকাত তহবিল",
                                    description = "যাকাত ও ফেতরা বিতরণ, দুস্থ সহায়তা এবং চিকিৎসা অনুদান",
                                    icon = Icons.Default.Favorite,
                                    iconColor = MosqueEmeraldPrimary,
                                    onClick = { selectedSubModule = "welfare" }
                                )
                            }
                            item {
                                OperationMenuItem(
                                    title = "মাহফিল ও বিশেষ অনুষ্ঠান",
                                    description = "বাৎসরিক ওয়াজ, শবে কদর, ইফতার মাহফিল ও বিশেষ আয়োজন",
                                    icon = Icons.Default.Campaign,
                                    iconColor = MosqueGoldDeep,
                                    onClick = { selectedSubModule = "events" }
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
                                    title = "মসজিদ পরিচিতি ও কমিটি তথ্য",
                                    description = "কমিটি কাঠামো, ইতিহাস, প্রতিষ্ঠা সাল ও ঠিকানা",
                                    icon = Icons.Default.Mosque,
                                    iconColor = MosqueEmeraldDark,
                                    onClick = { selectedSubModule = "about" }
                                )
                            }
                        }
                        UserRole.IMAM, UserRole.MUAZZIN, UserRole.KHADEM -> {
                            item {
                                OperationMenuItem(
                                    title = "ওয়াক্ত ও নামাজের দায়িত্ব বণ্টন",
                                    description = "ইমাম ও মুয়াজ্জিনদের ৫ ওয়াক্তের ডিউটি রোস্টার ও পরিচালনা",
                                    icon = Icons.Default.EventNote,
                                    iconColor = MosqueGoldDeep,
                                    onClick = { selectedSubModule = "roster" }
                                )
                            }
                            item {
                                OperationMenuItem(
                                    title = "ইমাম ও খাদেমদের খাবার শিডিউল",
                                    description = "দৈনিক দুপুর ও রাতের খাবার বণ্টন ও দায়িত্বপ্রাপ্ত দাতাদের তালিকা",
                                    icon = Icons.Default.Restaurant,
                                    iconColor = MosqueInfoBlue,
                                    onClick = { selectedSubModule = "food" }
                                )
                            }
                            item {
                                OperationMenuItem(
                                    title = "মাদরাসা ও মক্তব পরিচালনা",
                                    description = "ছাত্র হাজিরা, হিফজ ট্র্যাকার ও তালিম মূল্যায়ন",
                                    icon = Icons.Default.School,
                                    iconColor = MosqueEmeraldPrimary,
                                    onClick = { selectedSubModule = "madrasa" }
                                )
                            }
                            item {
                                OperationMenuItem(
                                    title = "আমার দায়িত্ব ও বেতন বিবরণী",
                                    description = "ব্যক্তিগত সার্ভিস রেকর্ড ও মাসিক বেতন স্ট্যাটাস",
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
                                    title = "মসজিদ পরিচিতি ও কমিটি তথ্য",
                                    description = "কমিটি কাঠামো, ইতিহাস, প্রতিষ্ঠা সাল ও ঠিকানা",
                                    icon = Icons.Default.Mosque,
                                    iconColor = MosqueEmeraldDark,
                                    onClick = { selectedSubModule = "about" }
                                )
                            }
                        }
                        UserRole.GENERAL_MEMBER -> {
                            item {
                                OperationMenuItem(
                                    title = "আমার পরিশোধিত চাঁদা ও রসিদ",
                                    description = "ব্যক্তিগত মাসিক চাঁদা ও ভেরিফাইড ডিজিটাল রসিদ",
                                    icon = Icons.Default.ReceiptLong,
                                    iconColor = MosqueEmeraldPrimary,
                                    onClick = { selectedSubModule = "chanda" }
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
                                    title = "সমাজকল্যাণ ও যাকাত আবেদন",
                                    description = "যাকাত ও চিকিৎসা অনুদানের জন্য আবেদনপত্র দাখিল",
                                    icon = Icons.Default.Favorite,
                                    iconColor = MosqueEmeraldPrimary,
                                    onClick = { selectedSubModule = "welfare" }
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
                        else -> {
                            // President & Vice President: Supervisory access to all operations
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
                                    title = "কমিটি সভা ও এজেন্ডা",
                                    description = "আসন্ন মিটিং নির্ধারণ ও রেজুলেশন অনুমোদন",
                                    icon = Icons.Default.Event,
                                    iconColor = MosqueGoldDeep,
                                    onClick = { selectedSubModule = "meetings" }
                                )
                            }
                            item {
                                OperationMenuItem(
                                    title = "সমাজকল্যাণ ও যাকাত তহবিল",
                                    description = "যাকাত ও ফেতরা বিতরণ, দুস্থ সহায়তা এবং চিকিৎসা অনুদান অনুমোদন",
                                    icon = Icons.Default.Favorite,
                                    iconColor = MosqueEmeraldPrimary,
                                    onClick = { selectedSubModule = "welfare" }
                                )
                            }
                            item {
                                OperationMenuItem(
                                    title = "ইমাম ও কর্মচারী বেতন ব্যবস্থাপনা",
                                    description = "পেশ ইমাম, মুয়াজ্জিন ও খাদেমদের বেতন অনুমোদন ও তদারকি",
                                    icon = Icons.Default.Badge,
                                    iconColor = MosqueInfoBlue,
                                    onClick = { selectedSubModule = "staff" }
                                )
                            }
                            item {
                                OperationMenuItem(
                                    title = "চাঁদা আদায় ও রসিদ রেজিস্টার",
                                    description = "সদস্যদের চাঁদা গ্রহণ ও ডিজিটাল ভেরিফাইড রসিদ তদারকি",
                                    icon = Icons.Default.ReceiptLong,
                                    iconColor = MosqueEmeraldPrimary,
                                    onClick = { selectedSubModule = "chanda" }
                                )
                            }
                            item {
                                OperationMenuItem(
                                    title = "মাদরাসা ও মক্তব পরিচালনা",
                                    description = "ছাত্র ভর্তি, দৈনিক হাজিরা, হিফজ ট্র্যাকার ও মাদরাসা ফি তদারকি",
                                    icon = Icons.Default.School,
                                    iconColor = MosqueEmeraldPrimary,
                                    onClick = { selectedSubModule = "madrasa" }
                                )
                            }
                            item {
                                OperationMenuItem(
                                    title = "ওয়াক্ত ও নামাজের দায়িত্ব বণ্টন",
                                    description = "ইমাম ও মুয়াজ্জিনদের ৫ ওয়াক্তের ডিউটি রোস্টার ও পরিচালনা",
                                    icon = Icons.Default.EventNote,
                                    iconColor = MosqueGoldDeep,
                                    onClick = { selectedSubModule = "roster" }
                                )
                            }
                            item {
                                OperationMenuItem(
                                    title = "ইমাম ও খাদেমদের খাবার শিডিউল",
                                    description = "দৈনিক দুপুর ও রাতের খাবার বণ্টন ও দায়িত্বপ্রাপ্ত দাতাদের তালিকা",
                                    icon = Icons.Default.Restaurant,
                                    iconColor = MosqueInfoBlue,
                                    onClick = { selectedSubModule = "food" }
                                )
                            }
                            item {
                                OperationMenuItem(
                                    title = "মাহফিল ও বিশেষ অনুষ্ঠান",
                                    description = "বাৎসরিক ওয়াজ, শবে কদর, ইফতার মাহফিল ও বিশেষ আয়োজন",
                                    icon = Icons.Default.Campaign,
                                    iconColor = MosqueGoldDeep,
                                    onClick = { selectedSubModule = "events" }
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

    // Pay Salary Dialog
    showPaySalaryStaff?.let { staff ->
        var month by remember { mutableStateOf("সেপ্টেম্বর ২০২৪") }
        var bonus by remember { mutableStateOf("0") }

        Dialog(onDismissRequest = { showPaySalaryStaff = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "বেতন ও ভাতা পরিশোধ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MosqueEmeraldDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "কর্মচারী: ${staff.nameBn} (${staff.designationBn})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MosqueTextPrimary
                    )
                    Text(
                        text = "মূল বেতন: ৳${String.format("%,.0f", staff.salaryAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MosqueEmeraldPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = month,
                        onValueChange = { month = it },
                        label = { Text("বেতনের মাস *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bonus,
                        onValueChange = { bonus = it },
                        label = { Text("বোনাস / অন্যান্য ভাতা (৳)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showPaySalaryStaff = null }) { Text("বাতিল") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val b = bonus.toDoubleOrNull() ?: 0.0
                                viewModel.disburseSalary(staff, month, b)
                                showPaySalaryStaff = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                        ) {
                            Text("পরিশোধ নিশ্চিত করুন")
                        }
                    }
                }
            }
        }
    }

    // Add Roster Dialog
    if (showAddRosterDialog && activeStaff.isNotEmpty()) {
        var selectedStaff by remember { mutableStateOf(activeStaff.first()) }
        var dayOfWeek by remember { mutableStateOf("শুক্রবার") }
        var prayerName by remember { mutableStateOf("জুমআ ও আসর") }
        var roleTitle by remember { mutableStateOf("খুতবা ও ইমামতি") }
        var notes by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddRosterDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "নতুন ওয়াক্ত দায়িত্ব বণ্টন",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MosqueEmeraldDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "দায়িত্বপ্রাপ্ত: ${selectedStaff.nameBn}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MosqueEmeraldPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dayOfWeek,
                        onValueChange = { dayOfWeek = it },
                        label = { Text("বার / দিন *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = prayerName,
                        onValueChange = { prayerName = it },
                        label = { Text("নামাজের ওয়াক্ত *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = roleTitle,
                        onValueChange = { roleTitle = it },
                        label = { Text("দায়িত্ব (ইমামতি / আজান) *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("বিশেষ নোট") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddRosterDialog = false }) { Text("বাতিল") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.addDutyRoster(selectedStaff, dayOfWeek, prayerName, roleTitle, notes)
                                showAddRosterDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                        ) {
                            Text("নির্ধারণ করুন")
                        }
                    }
                }
            }
        }
    }

    // Add Food Schedule Dialog
    if (showAddFoodDialog) {
        var dateStr by remember { mutableStateOf("২০২৪-০৯-১৫") }
        var mealType by remember { mutableStateOf("দুপুর") }
        var providerName by remember { mutableStateOf("") }
        var providerMobile by remember { mutableStateOf("") }
        var receiverName by remember { mutableStateOf("ইমাম ও মুয়াজ্জিন সাহেব") }

        Dialog(onDismissRequest = { showAddFoodDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "খাবার দায়িত্ব বণ্টন ফরম",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MosqueEmeraldDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = { dateStr = it },
                        label = { Text("তারিখ *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mealType,
                        onValueChange = { mealType = it },
                        label = { Text("খাবারের ওয়াক্ত (দুপুর / রাত) *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = providerName,
                        onValueChange = { providerName = it },
                        label = { Text("খাবার প্রদানকারী দাতার নাম *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = providerMobile,
                        onValueChange = { providerMobile = it },
                        label = { Text("দাতার মোবাইল নম্বর *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = receiverName,
                        onValueChange = { receiverName = it },
                        label = { Text("প্রাপক স্টাফ *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddFoodDialog = false }) { Text("বাতিল") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (providerName.isNotBlank()) {
                                    viewModel.addFoodSchedule(dateStr, mealType, providerName, providerMobile, receiverName)
                                    showAddFoodDialog = false
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

    // Add Welfare Dialog
    if (showAddWelfareDialog) {
        AddWelfareDialog(
            onDismiss = { showAddWelfareDialog = false },
            onConfirm = { beneficiaryName, category, mobile, amount, remarks ->
                viewModel.addWelfareCase(beneficiaryName, category, mobile, amount, remarks)
                showAddWelfareDialog = false
            }
        )
    }

    // Add Event Dialog
    if (showAddEventDialog) {
        AddEventDialog(
            onDismiss = { showAddEventDialog = false },
            onConfirm = { title, dateStr, timeStr, speaker, venue, budget, lead ->
                viewModel.addEvent(title, dateStr, timeStr, speaker, venue, budget, lead)
                showAddEventDialog = false
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

    val categories = listOf("জরুরি চিকিৎসা সহায়তা", "বিধবা ও এতিম ভাতা", "দরিদ্র শিক্ষা সহায়তা", "খাদ্য ও ত্রাণ সহায়তা")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "সমাজকল্যাণ ও যাকাত সহায়তা আবেদন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("আবেদনকারী / সুবিধাভোগীর নাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("সহায়তার খাত:", style = MaterialTheme.typography.labelMedium, color = MosqueTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(2).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.drop(2).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("যোগাযোগের মোবাইল নম্বর *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("সহায়তার পরিমাণ (৳) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("বিবরণ বা যাচাইকৃত সুপারিশ") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 5000.0
                            if (name.isNotBlank() && mobile.isNotBlank()) {
                                onConfirm(name, category, mobile, amt, remarks)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Text("আবেদন জমা দিন")
                    }
                }
            }
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
    var timeStr by remember { mutableStateOf("বা’দ মাগরিব") }
    var speaker by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("মসজিদ প্রাঙ্গণ") }
    var budget by remember { mutableStateOf("50000") }
    var lead by remember { mutableStateOf("ইঞ্জি: আব্দুল্লাহ আল মামুন") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "নতুন মসজিদ অনুষ্ঠান / মাহফিল সূচি",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("অনুষ্ঠানের নাম (যেমন: তাফসির মাহফিল) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = { dateStr = it },
                        label = { Text("তারিখ *") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = timeStr,
                        onValueChange = { timeStr = it },
                        label = { Text("সময় *") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = speaker,
                    onValueChange = { speaker = it },
                    label = { Text("প্রধান বক্তা / মেহমান *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = { Text("অনুষ্ঠানের স্থান *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = budget,
                        onValueChange = { budget = it },
                        label = { Text("আনুমানিক বাজেট (৳) *") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lead,
                        onValueChange = { lead = it },
                        label = { Text("দায়িত্বপ্রাপ্ত দলনেতা") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val b = budget.toDoubleOrNull() ?: 20000.0
                            if (title.isNotBlank()) {
                                onConfirm(title, dateStr, timeStr, speaker, venue, b, lead)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Text("তালিকায় যুক্ত করুন")
                    }
                }
            }
        }
    }
}

