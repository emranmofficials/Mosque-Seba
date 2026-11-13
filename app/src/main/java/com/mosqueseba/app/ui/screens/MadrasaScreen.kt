package com.mosqueseba.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.mosqueseba.app.data.local.entity.MadrasaAttendanceEntity
import com.mosqueseba.app.data.local.entity.StudentEntity
import com.mosqueseba.app.data.model.UserRole
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
    val currentRole by viewModel.currentUserRole.collectAsState()
    val defaultTab = when {
        initialTab != 0 -> initialTab
        currentRole == UserRole.TREASURER -> 2
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MosqueBgLight)
    ) {
        // Tab selector
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 8.dp,
            containerColor = Color.White,
            contentColor = MosqueEmeraldPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("শিক্ষার্থী ($studentCount)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
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
                                text = "মাদরাসা ও মক্তব শিক্ষার্থী তালিকা",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldDark
                            )
                            val canEnrollStudent = currentRole in listOf(UserRole.PRESIDENT, UserRole.VICE_PRESIDENT, UserRole.GENERAL_SECRETARY, UserRole.IMAM)
                            if (canEnrollStudent) {
                                Button(
                                    onClick = { showAddStudentDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                ) {
                                    Text("+ নতুন ভর্তি", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    items(students, key = { it.id }) { student ->
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
                                            text = student.nameBn,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueTextPrimary
                                        )
                                        Text(
                                            text = "${student.className} • রোল/আইডি: ${student.studentCode}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MosqueEmeraldPrimary,
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
                                HorizontalDivider(color = MosqueCardBorder)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("অভিভাবক: ${student.guardianName}", style = MaterialTheme.typography.bodySmall, color = MosqueTextSecondary)
                                        Text("মোবাইল: ${student.guardianMobile}", style = MaterialTheme.typography.labelSmall, color = MosqueTextMuted)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("মাসিক ফি", style = MaterialTheme.typography.labelSmall, color = MosqueTextMuted)
                                        Text(
                                            text = "৳${student.monthlyFee.toInt()}",
                                            style = MaterialTheme.typography.titleMedium,
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
            1 -> {
                // Hifz Tracker
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
                                text = "হিফজুল কুরআন দৈনিক মূল্যায়ন",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldDark
                            )
                            val canEvaluateHifz = currentRole in listOf(UserRole.PRESIDENT, UserRole.VICE_PRESIDENT, UserRole.IMAM, UserRole.MUAZZIN)
                            if (canEvaluateHifz) {
                                Button(
                                    onClick = { showAddHifzDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueGoldDeep)
                                ) {
                                    Text("+ সবক এন্ট্রি", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    items(hifzList, key = { it.id }) { hfz ->
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
                                        text = hfz.studentNameBn,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueTextPrimary
                                    )
                                    Text(
                                        text = "পারা: ${hfz.paraNumber}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueEmeraldPrimary
                                    )
                                }
                                Text(
                                    text = "পাঠ: ${hfz.pageOrAyah}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "মান: ${"★".repeat(hfz.qualityScore)}${"☆".repeat(5 - hfz.qualityScore)}",
                                        color = MosqueGoldDeep,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ভুল: ${hfz.mistakeCount} টি",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (hfz.mistakeCount == 0) MosqueSuccessGreen else MosqueWarningAmber
                                    )
                                }
                                if (hfz.teacherFeedback.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "উস্তাদের মন্তব্য: \"${hfz.teacherFeedback}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MosqueTextMuted,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Fee Collection
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
                                    Text("মোট সংগৃহীত ফি", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                                    Text("৳${String.format("%,.0f", totalFee)}", color = MosqueGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                }
                                val canCollectFee = currentRole in listOf(UserRole.TREASURER, UserRole.PRESIDENT, UserRole.VICE_PRESIDENT)
                                if (canCollectFee) {
                                    Button(
                                        onClick = { showCollectFeeDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueGold),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("ফি গ্রহণ করুন", color = MosqueEmeraldDark, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    items(feePayments, key = { it.id }) { fee ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
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
                                        text = fee.studentNameBn,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueTextPrimary
                                    )
                                    Text(
                                        text = "${fee.month} • রসিদ: ${fee.receiptCode}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MosqueTextSecondary
                                    )
                                    Text(
                                        text = "জমার সময়: ${fee.paidAt}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MosqueTextMuted
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "৳${fee.amountPaid.toInt()}",
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
            3 -> {
                // Guardian Portal (PRD §24)
                if (students.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(30.dp), contentAlignment = Alignment.Center) {
                        Text("কোনো শিক্ষার্থী অন্তর্ভুক্ত নেই", color = MosqueTextMuted)
                    }
                } else {
                    var selectedStudentIndex by remember { mutableIntStateOf(0) }
                    val currentStudent = students[selectedStudentIndex.coerceIn(0, students.size - 1)]
                    val studentHifz = hifzList.filter { it.studentId == currentStudent.id }

                    LazyColumn(
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "সন্তানের প্রোফাইল নির্বাচন করুন:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MosqueTextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                students.forEachIndexed { index, stu ->
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                                color = MosqueEmeraldDark
                                            )
                                            Text(
                                                text = "${currentStudent.className} • আইডি: ${currentStudent.studentCode}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MosqueTextSecondary
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
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("অভিভাবকের নাম", style = MaterialTheme.typography.bodySmall, color = MosqueTextSecondary)
                                        Text(currentStudent.guardianName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("যোগাযোগের মোবাইল", style = MaterialTheme.typography.bodySmall, color = MosqueTextSecondary)
                                        Text(currentStudent.guardianMobile, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("মাসিক নির্ধারিত ফি", style = MaterialTheme.typography.bodySmall, color = MosqueTextSecondary)
                                        Text("৳${currentStudent.monthlyFee.toInt()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MosqueEmeraldPrimary)
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "কুরআন ও হিফজ প্রগ্রেস আপডেট",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldDark
                            )
                        }

                        if (studentHifz.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Text("এখনও কোনো হিফজ পাঠ রেকর্ড করা হয়নি।", modifier = Modifier.padding(14.dp), color = MosqueTextMuted, fontSize = 12.sp)
                                }
                            }
                        } else {
                            items(studentHifz) { hfz ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("পারা: ${hfz.paraNumber} • ${hfz.pageOrAyah}", fontWeight = FontWeight.Bold, color = MosqueTextPrimary, fontSize = 13.sp)
                                            Text(hfz.date, fontSize = 11.sp, color = MosqueTextMuted)
                                        }
                                        if (hfz.teacherFeedback.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text("উস্তাদের মন্তব্য: \"${hfz.teacherFeedback}\"", fontSize = 12.sp, color = MosqueEmeraldPrimary)
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

                val canTakeAttendance = currentRole in listOf(
                    UserRole.PRESIDENT,
                    UserRole.VICE_PRESIDENT,
                    UserRole.GENERAL_SECRETARY,
                    UserRole.IMAM,
                    UserRole.MUAZZIN
                )

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
                            label = { Text("হাজিরার তারিখ (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = MosqueEmeraldPrimary
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
                                Text("কোনো শিক্ষার্থী নিবন্ধিত নেই", color = MosqueTextMuted)
                            }
                        }
                    } else {
                        items(students, key = { it.id }) { stu ->
                            val currentStatus = attendanceStatusMap[stu.id] ?: "present"
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                                color = MosqueTextPrimary
                                            )
                                            Text(
                                                text = "${stu.className} • ${stu.studentCode}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MosqueTextMuted
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

        Dialog(onDismissRequest = { showAddStudentDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "নতুন শিক্ষার্থী ভর্তি ফরম",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MosqueEmeraldDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("শিক্ষার্থীর নাম (বাংলায়) *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = className,
                        onValueChange = { className = it },
                        label = { Text("শ্রেণি / বিভাগ *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = guardianName,
                        onValueChange = { guardianName = it },
                        label = { Text("অভিভাবকের নাম *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("অভিভাবকের মোবাইল *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fee,
                        onValueChange = { fee = it },
                        label = { Text("মাসিক বেতন/ফি (৳) *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddStudentDialog = false }) { Text("বাতিল") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank() && guardianName.isNotBlank()) {
                                    viewModel.addStudent(
                                        nameBn = name,
                                        className = className,
                                        guardianName = guardianName,
                                        guardianMobile = mobile,
                                        monthlyFee = fee.toDoubleOrNull() ?: 1000.0
                                    )
                                    showAddStudentDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                        ) {
                            Text("ভর্তি সম্পন্ন করুন")
                        }
                    }
                }
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

        Dialog(onDismissRequest = { showAddHifzDialog = false }) {
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
                        text = "হিফজ পাঠ ও সবক রেকর্ড",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MosqueEmeraldDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "শিক্ষার্থী নির্বাচন করুন:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MosqueTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(students, key = { it.id }) { stu ->
                            FilterChip(
                                selected = selectedStudent.id == stu.id,
                                onClick = { selectedStudent = stu },
                                label = { Text(stu.nameBn, fontSize = 11.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = paraNumber,
                        onValueChange = { paraNumber = it },
                        label = { Text("পারা নম্বর (১–৩০) *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pageOrAyah,
                        onValueChange = { pageOrAyah = it },
                        label = { Text("পৃষ্ঠা বা আয়াত নম্বর *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "পাঠের মান (Quality Score): ${toBengaliDigits(qualityScore.toString())}/৫",
                        style = MaterialTheme.typography.labelMedium,
                        color = MosqueTextSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (score in 1..5) {
                            IconButton(
                                onClick = { qualityScore = score },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (score <= qualityScore) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Rating $score",
                                    tint = if (score <= qualityScore) MosqueGoldDeep else MosqueTextMuted
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = mistakeCount,
                        onValueChange = { mistakeCount = it },
                        label = { Text("ভুলের সংখ্যা") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = feedback,
                        onValueChange = { feedback = it },
                        label = { Text("উস্তাদের মন্তব্য ও নির্দেশনা") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddHifzDialog = false }) { Text("বাতিল") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.addHifzProgress(
                                    student = selectedStudent,
                                    paraNumber = paraNumber.toIntOrNull()?.coerceIn(1, 30) ?: 1,
                                    pageOrAyah = pageOrAyah,
                                    qualityScore = qualityScore,
                                    mistakeCount = mistakeCount.toIntOrNull() ?: 0,
                                    feedback = feedback
                                )
                                showAddHifzDialog = false
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

        Dialog(onDismissRequest = { showCollectFeeDialog = false }) {
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
                        text = "মাদরাসা মাসিক ফি গ্রহণ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MosqueEmeraldDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "শিক্ষার্থী নির্বাচন করুন:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MosqueTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(students, key = { it.id }) { stu ->
                            FilterChip(
                                selected = selectedStudent.id == stu.id,
                                onClick = {
                                    selectedStudent = stu
                                    amount = stu.monthlyFee.toInt().toString()
                                },
                                label = { Text(stu.nameBn, fontSize = 11.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = month,
                        onValueChange = { month = it },
                        label = { Text("মাসের নাম *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("পরিশোধিত টাকার পরিমাণ (৳) *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCollectFeeDialog = false }) { Text("বাতিল") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amt = amount.toDoubleOrNull() ?: selectedStudent.monthlyFee
                                viewModel.recordMadrasaFee(
                                    student = selectedStudent,
                                    month = month,
                                    amount = amt
                                )
                                showCollectFeeDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                        ) {
                            Text("ফি গ্রহণ নিশ্চিত করুন")
                        }
                    }
                }
            }
        }
    }
}
