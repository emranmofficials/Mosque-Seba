package com.mosqueseba.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.data.local.entity.MadrasaAttendanceEntity
import com.mosqueseba.app.data.local.entity.StudentEntity
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.ExecutiveFormDialogShell
import com.mosqueseba.app.ui.components.MoreFormSection
import com.mosqueseba.app.ui.components.toBengaliDigits
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun MadrasaScreen(viewModel: MosqueViewModel, initialTab: Int = 0) {
    val context = LocalContext.current
    val currentRole by viewModel.currentUserRole.collectAsState()
    val defaultTab = when {
        initialTab != 0 -> initialTab
        currentRole == UserRole.TREASURER || currentRole == UserRole.ASSISTANT_TREASURER -> 2
        currentRole == UserRole.GENERAL_MEMBER -> 3
        else -> 0
    }
    var selectedTab by remember(currentRole, initialTab) { mutableIntStateOf(defaultTab) }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showAddHifzDialog by remember { mutableStateOf(false) }
    var showCollectFeeDialog by remember { mutableStateOf(false) }

    val students by viewModel.allStudents.collectAsState()
    val studentCount by viewModel.studentCount.collectAsState()
    val hifzList by viewModel.allHifzProgress.collectAsState()
    val feePayments by viewModel.allFeePayments.collectAsState()
    val totalFee by viewModel.totalFeeCollected.collectAsState()

    val isDark = LocalIsDarkTheme.current
    val cardBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab selector
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 8.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("শিক্ষার্থী (${toBengaliDigits(studentCount.toString())})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("হিফজ ট্র্যাকার", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("মাদরাসা ফি", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("অভিভাবক ভিউ", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
                text = { Text("দৈনিক হাজিরা", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }


        when (selectedTab) {
            0 -> {
                // Students List
                LazyColumn(
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "মাদরাসা ও মক্তব শিক্ষার্থী তালিকা",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val canEnrollStudent = currentRole.canManageWelfareEducation()
                            if (canEnrollStudent) {
                                Button(
                                    onClick = { showAddStudentDialog = true },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("add_student_button"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                ) {
                                    Text("+ নতুন ভর্তি", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    if (students.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Default.School,
                                message = "বর্তমানে কোনো শিক্ষার্থী ভর্তি নেই। নতুন শিক্ষার্থী ভর্তি করতে উপরের বাটনে চাপ দিন।"
                            )
                        }
                    } else {
                        items(students, key = { it.id }) { student ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = student.nameBn,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${student.className} • রোল/আইডি: ${toBengaliDigits(student.studentCode)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MosqueSuccessGreen.copy(alpha = 0.12f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "নিয়মিত",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MosqueSuccessGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = cardBorder)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("অভিভাবক: ${student.guardianName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("মোবাইল: ${toBengaliDigits(student.guardianMobile)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("মাসিক ফি", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = "৳${toBengaliDigits(student.monthlyFee.toInt().toString())}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Hifz Tracker
                LazyColumn(
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "হিফজুল কুরআন দৈনিক মূল্যায়ন",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val canEvaluateHifz = currentRole.canManageWelfareEducation() || currentRole == UserRole.MUAZZIN
                            if (canEvaluateHifz) {
                                Button(
                                    onClick = { showAddHifzDialog = true },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("add_hifz_button"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueGoldDeep)
                                ) {
                                    Text("+ সবক এন্ট্রি", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    if (hifzList.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Default.AutoStories,
                                message = "হিফজুল কুরআনের কোনো দৈনিক সবক রেকর্ড পাওয়া যায়নি।"
                            )
                        }
                    } else {
                        items(hifzList, key = { it.id }) { hfz ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, cardBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = hfz.studentNameBn,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "পারা: ${toBengaliDigits(hfz.paraNumber.toString())}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "পাঠ: ${toBengaliDigits(hfz.pageOrAyah)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "মান:",
                                        color = MosqueGoldDeep,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val rating = hfz.qualityScore.coerceIn(0, 5)
                                    for (i in 1..5) {
                                        Icon(
                                            imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = null,
                                            tint = if (i <= rating) MosqueGoldDeep else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ভুল: ${toBengaliDigits(hfz.mistakeCount.toString())} টি",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (hfz.mistakeCount == 0) MosqueSuccessGreen else MosqueWarningAmber
                                    )
                                }
                                if (hfz.teacherFeedback.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "উস্তাদের মন্তব্য: \"${hfz.teacherFeedback}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
            2 -> {
                // Fee Collection
                LazyColumn(
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 100.dp),
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
                                    Text("মোট সংগৃহীত ফি", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                                    Text("৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", totalFee))}", color = MosqueGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                }
                                val canCollectFee = currentRole in listOf(UserRole.TREASURER, UserRole.ASSISTANT_TREASURER, UserRole.PRESIDENT, UserRole.VICE_PRESIDENT)
                                if (canCollectFee) {
                                    Button(
                                        onClick = { showCollectFeeDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueGold),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Text("ফি গ্রহণ করুন", color = MosqueEmeraldDark, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    if (feePayments.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Default.Payments,
                                message = "এখনো কোনো মাদরাসা ফি আদায়ের রেকর্ড পাওয়া যায়নি।"
                            )
                        }
                    } else {
                        items(feePayments, key = { it.id }) { fee ->
                            Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, cardBorder)
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
                                        text = fee.studentNameBn,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${fee.month} • রসিদ: ${toBengaliDigits(fee.receiptCode)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "জমার সময়: ${toBengaliDigits(fee.paidAt)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "৳${toBengaliDigits(fee.amountPaid.toInt().toString())}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueSuccessGreen
                                    )
                                    Text(
                                        text = "পরিশোধিত",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MosqueSuccessGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    }
                }
            }
            3 -> {
                // Guardian Portal (PRD §24)
                if (students.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(30.dp), contentAlignment = Alignment.Center) {
                        Text("কোনো শিক্ষার্থী অন্তর্ভুক্ত নেই", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    var selectedStudentIndex by remember { mutableIntStateOf(0) }
                    val currentStudent = students[selectedStudentIndex.coerceIn(0, students.size - 1)]
                    val studentHifz = hifzList.filter { it.studentId == currentStudent.id }

                    LazyColumn(
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "সন্তানের প্রোফাইল নির্বাচন করুন:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(students.size) { index ->
                                    val stu = students[index]
                                    FilterChip(
                                        selected = selectedStudentIndex == index,
                                        onClick = { selectedStudentIndex = index },
                                        label = { Text(stu.nameBn) }
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = currentStudent.nameBn,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "${currentStudent.className} • আইডি: ${toBengaliDigits(currentStudent.studentCode)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MosqueEmeraldPrimary)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "নিয়মিত শিক্ষার্থী",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = cardBorder)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("অভিভাবকের নাম", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(currentStudent.guardianName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("যোগাযোগের মোবাইল", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(toBengaliDigits(currentStudent.guardianMobile), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("মাসিক নির্ধারিত ফি", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("৳${toBengaliDigits(currentStudent.monthlyFee.toInt().toString())}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "কুরআন ও হিফজ প্রগ্রেস আপডেট",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (studentHifz.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, cardBorder)
                                ) {
                                    Text("এখনও কোনো হিফজ পাঠ রেকর্ড করা হয়নি।", modifier = Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                }
                            }
                        } else {
                            items(studentHifz) { hfz ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, cardBorder)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("পারা: ${toBengaliDigits(hfz.paraNumber.toString())} • ${toBengaliDigits(hfz.pageOrAyah)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                            Text(toBengaliDigits(hfz.date), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (hfz.teacherFeedback.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text("উস্তাদের মন্তব্য: \"${hfz.teacherFeedback}\"", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            4 -> {
                // Daily Attendance Tab
                val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
                var attendanceDate by remember { mutableStateOf(todayStr) }
                val existingAttendance by viewModel.getAttendanceByDate(attendanceDate).collectAsState(initial = emptyList())
                val attendanceStatusMap = remember { mutableStateMapOf<String, String>() }
                var attendanceSavedBanner by remember { mutableStateOf(false) }

                val canTakeAttendance = currentRole.canManageWelfareEducation() || currentRole == UserRole.MUAZZIN

                LaunchedEffect(students, existingAttendance, attendanceDate) {
                    attendanceSavedBanner = false
                    val existingByStudent = existingAttendance.associateBy { it.studentId }
                    attendanceStatusMap.clear()
                    students.forEach { stu ->
                        attendanceStatusMap[stu.id] = existingByStudent[stu.id]?.status ?: "present"
                    }
                }

                val presentCount = students.count { attendanceStatusMap[it.id] == "present" }
                val absentCount = students.count { attendanceStatusMap[it.id] == "absent" }
                val lateCount = students.count { attendanceStatusMap[it.id] == "late" }

                LazyColumn(
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 100.dp),
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
                                    Column {
                                        Text(
                                            text = "দৈনিক শ্রেণি হাজিরা খাতা",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "তারিখ: ${toBengaliDigits(attendanceDate)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MosqueGoldLight
                                        )
                                    }
                                    if (canTakeAttendance && students.isNotEmpty()) {
                                        Button(
                                            onClick = {
                                                val existingByStudent = existingAttendance.associateBy { it.studentId }
                                                val batch = students.map { stu ->
                                                    val existing = existingByStudent[stu.id]
                                                    MadrasaAttendanceEntity(
                                                        id = existing?.id ?: UUID.randomUUID().toString(),
                                                        studentId = stu.id,
                                                        studentNameBn = stu.nameBn,
                                                        date = attendanceDate,
                                                        status = attendanceStatusMap[stu.id] ?: "present"
                                                    )
                                                }
                                                viewModel.recordAttendanceBatch(batch)
                                                attendanceSavedBanner = true
                                                Toast.makeText(
                                                    context,
                                                    "${toBengaliDigits(attendanceDate)} তারিখের হাজিরা সংরক্ষণ করা হয়েছে।",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MosqueGold),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("হাজিরা সংরক্ষণ", color = MosqueEmeraldDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = Color.White.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "উপস্থিত: ${toBengaliDigits(presentCount.toString())}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                    Surface(
                                        color = Color.White.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "অনুপস্থিত: ${toBengaliDigits(absentCount.toString())}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                    Surface(
                                        color = Color.White.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "বিলম্ব: ${toBengaliDigits(lateCount.toString())}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = attendanceDate,
                            onValueChange = { attendanceDate = it },
                            label = { Text("হাজিরার তারিখ (বছর-মাস-দিন)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    if (attendanceSavedBanner) {
                        item {
                            Surface(
                                color = MosqueSuccessGreen.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, MosqueSuccessGreen.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MosqueSuccessGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${toBengaliDigits(attendanceDate)} তারিখের হাজিরা সফলভাবে সংরক্ষণ করা হয়েছে।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MosqueSuccessGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (students.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("কোনো শিক্ষার্থী নিবন্ধিত নেই", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(students, key = { it.id }) { stu ->
                            val currentStatus = attendanceStatusMap[stu.id] ?: "present"
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stu.nameBn,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${stu.className} • ${toBengaliDigits(stu.studentCode)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = currentStatus == "present",
                                            onClick = { if (canTakeAttendance) attendanceStatusMap[stu.id] = "present" },
                                            enabled = canTakeAttendance || currentStatus == "present",
                                            label = { Text("উপস্থিত", fontSize = 11.sp) }
                                        )
                                        FilterChip(
                                            selected = currentStatus == "absent",
                                            onClick = { if (canTakeAttendance) attendanceStatusMap[stu.id] = "absent" },
                                            enabled = canTakeAttendance || currentStatus == "absent",
                                            label = { Text("অনুপস্থিত", fontSize = 11.sp) }
                                        )
                                        FilterChip(
                                            selected = currentStatus == "late",
                                            onClick = { if (canTakeAttendance) attendanceStatusMap[stu.id] = "late" },
                                            enabled = canTakeAttendance || currentStatus == "late",
                                            label = { Text("বিলম্ব", fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    // Add Student Dialog
    if (showAddStudentDialog) {
        var name by remember { mutableStateOf("") }
        var className by remember { mutableStateOf("হিফজুল কুরআন বিভাগ") }
        var guardianName by remember { mutableStateOf("") }
        var mobile by remember { mutableStateOf("") }
        var fee by remember { mutableStateOf("1000") }
        var nameError by remember { mutableStateOf<String?>(null) }
        var classError by remember { mutableStateOf<String?>(null) }
        var guardianError by remember { mutableStateOf<String?>(null) }
        var mobileError by remember { mutableStateOf<String?>(null) }
        var feeError by remember { mutableStateOf<String?>(null) }

        val classPresets = listOf("হিফজুল কুরআন বিভাগ", "নাযেরা বিভাগ", "কায়েদা বিভাগ", "মক্তব প্রভাতি")

        ExecutiveFormDialogShell(
            title = "নতুন শিক্ষার্থী ভর্তি ফরম",
            subtitle = "মসজিদ ভিত্তিক মক্তব ও হিফজখানায় ভর্তি",
            icon = Icons.Default.School,
            confirmText = "ভর্তি সম্পন্ন করুন",
            confirmTag = "confirm_add_student_button",
            onDismiss = { showAddStudentDialog = false },
            onConfirmClick = {
                var valid = true
                if (name.isBlank()) {
                    nameError = "শিক্ষার্থীর বাংলা নাম লিখুন"
                    valid = false
                }
                if (className.isBlank()) {
                    classError = "শ্রেণি বা বিভাগ উল্লেখ করুন"
                    valid = false
                }
                if (guardianName.isBlank()) {
                    guardianError = "অভিভাবকের নাম লিখুন"
                    valid = false
                }
                if (mobile.filter { it.isDigit() }.length < 6) {
                    mobileError = "অভিভাবকের সঠিক মোবাইল নম্বর দিন"
                    valid = false
                }
                val parsedFee = fee.toDoubleOrNull() ?: 0.0
                if (parsedFee <= 0.0) {
                    feeError = "মাসিক নির্ধারিত ফি (৳) লিখুন"
                    valid = false
                }
                if (valid) {
                    viewModel.addStudent(
                        nameBn = name.trim(),
                        className = className.trim(),
                        guardianName = guardianName.trim(),
                        guardianMobile = mobile.trim(),
                        monthlyFee = parsedFee
                    )
                    Toast.makeText(context, "${name.trim()}-এর ভর্তি সম্পন্ন হয়েছে।", Toast.LENGTH_SHORT).show()
                    showAddStudentDialog = false
                }
            }
        ) {
            MoreFormSection(title = "শিক্ষার্থী ও বিভাগ পরিচিতি", icon = Icons.Default.Person) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("শিক্ষার্থীর নাম (বাংলায়) *") },
                    isError = nameError != null,
                    supportingText = nameError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(classPresets) { preset ->
                        FilterChip(
                            selected = className == preset,
                            onClick = { className = preset; classError = null },
                            label = { Text(preset, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it; classError = null },
                    label = { Text("শ্রেণি / বিভাগ *") },
                    isError = classError != null,
                    supportingText = classError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            MoreFormSection(title = "অভিভাবক ও মাসিক ফি", icon = Icons.Default.FamilyRestroom) {
                OutlinedTextField(
                    value = guardianName,
                    onValueChange = { guardianName = it; guardianError = null },
                    label = { Text("অভিভাবকের নাম *") },
                    isError = guardianError != null,
                    supportingText = guardianError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it; mobileError = null },
                    label = { Text("অভিভাবকের মোবাইল *") },
                    isError = mobileError != null,
                    supportingText = mobileError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fee,
                    onValueChange = { fee = it.filter { ch -> ch.isDigit() }; feeError = null },
                    label = { Text("মাসিক বেতন/ফি (৳) *") },
                    isError = feeError != null,
                    supportingText = feeError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Add Hifz Dialog
    if (showAddHifzDialog && students.isNotEmpty()) {
        var selectedStudent by remember { mutableStateOf(students.first()) }
        var paraNumber by remember { mutableStateOf("1") }
        var pageOrAyah by remember { mutableStateOf("") }
        var qualityScore by remember { mutableIntStateOf(5) }
        var mistakeCount by remember { mutableStateOf("0") }
        var feedback by remember { mutableStateOf("") }
        var paraError by remember { mutableStateOf<String?>(null) }
        var pageError by remember { mutableStateOf<String?>(null) }

        ExecutiveFormDialogShell(
            title = "হিফজ পাঠ ও সবক রেকর্ড",
            subtitle = "দৈনিক সবক, আমপারা ও উস্তাদের মূল্যায়ন",
            icon = Icons.Default.MenuBook,
            confirmText = "সংরক্ষণ করুন",
            confirmTag = "confirm_add_hifz_button",
            onDismiss = { showAddHifzDialog = false },
            onConfirmClick = {
                var valid = true
                val para = paraNumber.toIntOrNull()
                if (para == null || para !in 1..30) {
                    paraError = "পারা নম্বর ১ থেকে ৩০-এর মধ্যে হতে হবে"
                    valid = false
                }
                if (pageOrAyah.isBlank()) {
                    pageError = "পৃষ্ঠা বা আয়াত নম্বর লিখুন"
                    valid = false
                }
                if (valid && para != null) {
                    viewModel.addHifzProgress(
                        student = selectedStudent,
                        paraNumber = para,
                        pageOrAyah = pageOrAyah.trim(),
                        qualityScore = qualityScore,
                        mistakeCount = mistakeCount.toIntOrNull() ?: 0,
                        feedback = feedback.trim()
                    )
                    Toast.makeText(context, "${selectedStudent.nameBn}-এর হিফজ পাঠ সংরক্ষিত হয়েছে।", Toast.LENGTH_SHORT).show()
                    showAddHifzDialog = false
                }
            }
        ) {
            MoreFormSection(title = "শিক্ষার্থী নির্বাচন", icon = Icons.Default.Person) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(students, key = { it.id }) { stu ->
                        FilterChip(
                            selected = selectedStudent.id == stu.id,
                            onClick = { selectedStudent = stu },
                            label = { Text(stu.nameBn, fontSize = 11.sp) }
                        )
                    }
                }
            }

            MoreFormSection(title = "সবক ও পাঠের মান", icon = Icons.Default.BookmarkAdded) {
                OutlinedTextField(
                    value = paraNumber,
                    onValueChange = { paraNumber = it.filter { ch -> ch.isDigit() }; paraError = null },
                    label = { Text("পারা নম্বর (১–৩০) *") },
                    isError = paraError != null,
                    supportingText = paraError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = pageOrAyah,
                    onValueChange = { pageOrAyah = it; pageError = null },
                    label = { Text("পৃষ্ঠা বা আয়াত নম্বর *") },
                    isError = pageError != null,
                    supportingText = pageError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "পাঠের মান: ${toBengaliDigits(qualityScore.toString())}/৫",
                    style = MaterialTheme.typography.labelMedium,
                    color = MosqueTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (score in 1..5) {
                        IconButton(
                            onClick = { qualityScore = score },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = if (score <= qualityScore) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "মান $score",
                                tint = if (score <= qualityScore) MosqueGoldDeep else MosqueTextMuted
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = mistakeCount,
                    onValueChange = { mistakeCount = it.filter { ch -> ch.isDigit() } },
                    label = { Text("ভুলের সংখ্যা") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("উস্তাদের মন্তব্য ও নির্দেশনা") },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Collect Fee Dialog
    if (showCollectFeeDialog && students.isNotEmpty()) {
        val currentMonthBn = remember {
            val today = LocalDate.now()
            val monthsBn = listOf(
                "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
                "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
            )
            toBengaliDigits("${monthsBn.getOrElse(today.monthValue - 1) { "সেপ্টেম্বর" }} ${today.year}")
        }
        var selectedStudent by remember { mutableStateOf(students.first()) }
        var month by remember { mutableStateOf(currentMonthBn) }
        var amount by remember { mutableStateOf(selectedStudent.monthlyFee.toInt().toString()) }
        var monthError by remember { mutableStateOf<String?>(null) }
        var amountError by remember { mutableStateOf<String?>(null) }

        ExecutiveFormDialogShell(
            title = "মাদরাসা মাসিক ফি গ্রহণ",
            subtitle = "শিক্ষার্থীর বেতন ও ডিজিটাল রসিদ এন্ট্রি",
            icon = Icons.Default.Payments,
            confirmText = "ফি গ্রহণ নিশ্চিত করুন",
            confirmTag = "confirm_collect_madrasa_fee_button",
            onDismiss = { showCollectFeeDialog = false },
            onConfirmClick = {
                var valid = true
                if (month.isBlank()) {
                    monthError = "মাসের নাম লিখুন"
                    valid = false
                }
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (amt <= 0.0) {
                    amountError = "সঠিক টাকার পরিমাণ লিখুন"
                    valid = false
                }
                if (valid) {
                    viewModel.recordMadrasaFee(
                        student = selectedStudent,
                        month = month.trim(),
                        amount = amt
                    )
                    Toast.makeText(
                        context,
                        "${selectedStudent.nameBn}-এর ৳${toBengaliDigits(amt.toInt().toString())} ফি গৃহীত হয়েছে।",
                        Toast.LENGTH_SHORT
                    ).show()
                    showCollectFeeDialog = false
                }
            }
        ) {
            MoreFormSection(title = "শিক্ষার্থী ও পরিশোধ বিবরণ", icon = Icons.Default.Receipt) {
                Text(
                    text = "শিক্ষার্থী নির্বাচন করুন:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MosqueTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(students, key = { it.id }) { stu ->
                        FilterChip(
                            selected = selectedStudent.id == stu.id,
                            onClick = {
                                selectedStudent = stu
                                amount = stu.monthlyFee.toInt().toString()
                                amountError = null
                            },
                            label = { Text(stu.nameBn, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = month,
                    onValueChange = { month = it; monthError = null },
                    label = { Text("মাসের নাম *") },
                    isError = monthError != null,
                    supportingText = monthError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { ch -> ch.isDigit() }; amountError = null },
                    label = { Text("পরিশোধিত টাকার পরিমাণ (৳) *") },
                    isError = amountError != null,
                    supportingText = amountError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
