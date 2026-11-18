package com.mosqueseba.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mosqueseba.app.data.local.entity.HifzProgressEntity
import com.mosqueseba.app.data.local.entity.MadrasaAttendanceEntity
import com.mosqueseba.app.data.local.entity.MadrasaFeePaymentEntity
import com.mosqueseba.app.data.local.entity.MemberEntity
import com.mosqueseba.app.data.local.entity.StudentEntity
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.ExecutiveFormDialogShell
import com.mosqueseba.app.ui.components.MoreFormSection
import com.mosqueseba.app.ui.components.MosqueSearchBar
import com.mosqueseba.app.ui.components.StaggeredItemEntrance
import com.mosqueseba.app.ui.components.toBengaliDigits
import com.mosqueseba.app.ui.components.toEnglishDigits
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
    val allMembers by viewModel.allMembers.collectAsState()
    val defaultTab = when {
        initialTab != 0 -> initialTab
        currentRole == UserRole.TREASURER || currentRole == UserRole.ASSISTANT_TREASURER -> 2
        currentRole == UserRole.GENERAL_MEMBER -> 3
        else -> 0
    }
    var selectedTab by remember(currentRole, initialTab) { mutableIntStateOf(defaultTab) }
    var selectedStudentForDetails by remember { mutableStateOf<StudentEntity?>(null) }
    var preselectedStudentForAction by remember { mutableStateOf<StudentEntity?>(null) }
    var studentToEdit by remember { mutableStateOf<StudentEntity?>(null) }

    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showEditStudentDialog by remember { mutableStateOf(false) }
    var showAddHifzDialog by remember { mutableStateOf(false) }
    var showCollectFeeDialog by remember { mutableStateOf(false) }

    val students by viewModel.allStudents.collectAsState()
    val studentCount by viewModel.studentCount.collectAsState()
    val hifzList by viewModel.allHifzProgress.collectAsState()
    val feePayments by viewModel.allFeePayments.collectAsState()
    val totalFee by viewModel.totalFeeCollected.collectAsState()

    val isDark = LocalIsDarkTheme.current
    val cardBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle

    // Back handler for student details view
    BackHandler(enabled = selectedStudentForDetails != null) {
        selectedStudentForDetails = null
    }

    if (selectedStudentForDetails != null) {
        StudentDetailsScreen(
            student = selectedStudentForDetails!!,
            viewModel = viewModel,
            allMembers = allMembers,
            canManage = currentRole.canManageWelfareEducation(),
            onBack = { selectedStudentForDetails = null },
            onOpenFeeCollect = {
                preselectedStudentForAction = selectedStudentForDetails
                showCollectFeeDialog = true
            },
            onOpenHifzEntry = {
                preselectedStudentForAction = selectedStudentForDetails
                showAddHifzDialog = true
            },
            onEditStudent = {
                studentToEdit = selectedStudentForDetails
                showEditStudentDialog = true
            }
        )
    } else {
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
                                            .heightIn(min = 44.dp)
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
                            itemsIndexed(students, key = { _, it -> it.id }) { index, student ->
                                StaggeredItemEntrance(index = index) {
                                    Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedStudentForDetails = student }
                                        .testTag("student_card_${student.studentCode}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, cardBorder)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft)
                                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = student.nameBn.take(1),
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
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

                                        HorizontalDivider(color = cardBorder)

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "অভিভাবক: ${student.guardianName}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "মোবাইল: ${toBengaliDigits(student.guardianMobile)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "মাসিক ফি",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "৳${toBengaliDigits(student.monthlyFee.toInt().toString())}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        // Click action strip
                                        HorizontalDivider(color = cardBorder)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(if (isDark) MosqueNightSurfaceVariant.copy(alpha = 0.5f) else MosqueEmeraldSoft.copy(alpha = 0.35f))
                                                .padding(horizontal = 14.dp, vertical = 7.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "হিফজ, হাজিরা ও ফি বিবরণী",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "বিস্তারিত প্রোফাইল দেখুন ›",
                                                style = MaterialTheme.typography.labelSmall,
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
                                            .heightIn(min = 44.dp)
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
                                                    tint = if (i <= rating) MosqueGoldDeep else MosqueTextMuted,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            if (hfz.mistakeCount > 0) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "ভুল: ${toBengaliDigits(hfz.mistakeCount.toString())} বার",
                                                    color = MosqueUrgentRed,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                        if (hfz.teacherFeedback.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "মন্তব্য: ${hfz.teacherFeedback}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                        Text(
                                            "৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", totalFee))}",
                                            color = MosqueGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                    }
                                    val canCollectFee = currentRole in listOf(UserRole.TREASURER, UserRole.ASSISTANT_TREASURER, UserRole.PRESIDENT, UserRole.VICE_PRESIDENT)
                                    if (canCollectFee) {
                                        Button(
                                            onClick = { showCollectFeeDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = MosqueGold),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.heightIn(min = 44.dp)
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
                    // Guardian Portal
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
                                    Column(modifier = Modifier.fillMaxWidth()) {
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

                                        HorizontalDivider(color = cardBorder)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedStudentForDetails = currentStudent }
                                                .background(if (isDark) MosqueNightSurfaceVariant.copy(alpha = 0.5f) else MosqueEmeraldSoft.copy(alpha = 0.35f))
                                                .padding(horizontal = 14.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "সম্পূর্ণ প্রোফাইল ও অগ্রগতি বিশ্লেষণ",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "বিস্তারিত দেখুন ›",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
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
                    // Daily Attendance
                    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
                    var attendanceDate by remember { mutableStateOf(todayStr) }
                    val existingAttendance by viewModel.getAttendanceByDate(attendanceDate).collectAsState(initial = emptyList())
                    val attendanceStatusMap = remember { mutableStateMapOf<String, String>() }
                    var attendanceSavedBanner by remember { mutableStateOf(false) }

                    val canTakeAttendance = currentRole.canManageWelfareEducation() || currentRole == UserRole.IMAM || currentRole == UserRole.MUAZZIN

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
                                                text = "মাদরাসা দৈনিক হাজিরা",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "তারিখ: ${toBengaliDigits(attendanceDate)}",
                                                color = MosqueGold,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                        if (canTakeAttendance) {
                                            Button(
                                                onClick = {
                                                    val batch = students.map { stu ->
                                                        MadrasaAttendanceEntity(
                                                            id = "${stu.id}_$attendanceDate",
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
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.heightIn(min = 44.dp)
                                            ) {
                                                Text("হাজিরা সংরক্ষণ", color = MosqueEmeraldDark, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("মোট শিক্ষার্থী", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                            Text("${toBengaliDigits(students.size.toString())} জন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("উপস্থিত", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                            Text("${toBengaliDigits(presentCount.toString())} জন", color = MosqueSuccessGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("অনুপস্থিত", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                            Text("${toBengaliDigits(absentCount.toString())} জন", color = MosqueUrgentRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("বিলম্বিত", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                            Text("${toBengaliDigits(lateCount.toString())} জন", color = MosqueGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (attendanceSavedBanner) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MosqueSuccessGreen.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, MosqueSuccessGreen.copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MosqueSuccessGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${toBengaliDigits(attendanceDate)} তারিখের হাজিরা সফলভাবে সংরক্ষণ করা হয়েছে।",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueSuccessGreen
                                        )
                                    }
                                }
                            }
                        }

                        if (students.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    icon = Icons.Default.EventBusy,
                                    message = "কোনো শিক্ষার্থী নিবন্ধিত নেই।"
                                )
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
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
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
                                                text = "${stu.className} • রোল: ${toBengaliDigits(stu.studentCode)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            FilterChip(
                                                selected = currentStatus == "present",
                                                onClick = { if (canTakeAttendance) attendanceStatusMap[stu.id] = "present" },
                                                label = { Text("উপস্থিত", fontSize = 11.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MosqueSuccessGreen,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                            FilterChip(
                                                selected = currentStatus == "absent",
                                                onClick = { if (canTakeAttendance) attendanceStatusMap[stu.id] = "absent" },
                                                label = { Text("অনুপস্থিত", fontSize = 11.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MosqueUrgentRed,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                            FilterChip(
                                                selected = currentStatus == "late",
                                                onClick = { if (canTakeAttendance) attendanceStatusMap[stu.id] = "late" },
                                                label = { Text("দেরি", fontSize = 11.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MosqueGoldDeep,
                                                    selectedLabelColor = Color.White
                                                )
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
        var showGuardianPicker by remember { mutableStateOf(false) }

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
                // Feature 2: Select guardian from member list
                Surface(
                    onClick = { showGuardianPicker = true },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("select_guardian_from_members_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "সদস্য তালিকা থেকে অভিভাবক নির্বাচন করুন",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                    onValueChange = { fee = toEnglishDigits(it).filter { ch -> ch.isDigit() }; feeError = null },
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

        if (showGuardianPicker) {
            GuardianMemberPickerDialog(
                members = allMembers,
                onSelectMember = { member ->
                    guardianName = member.nameBn
                    mobile = member.mobile
                    guardianError = null
                    mobileError = null
                    showGuardianPicker = false
                    Toast.makeText(
                        context,
                        "${member.nameBn}-কে অভিভাবক হিসেবে নির্বাচন করা হয়েছে।",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onDismiss = { showGuardianPicker = false }
            )
        }
    }

    // Edit Student Dialog
    if (showEditStudentDialog && studentToEdit != null) {
        val currentEditStudent = studentToEdit!!
        var name by remember(currentEditStudent) { mutableStateOf(currentEditStudent.nameBn) }
        var className by remember(currentEditStudent) { mutableStateOf(currentEditStudent.className) }
        var guardianName by remember(currentEditStudent) { mutableStateOf(currentEditStudent.guardianName) }
        var mobile by remember(currentEditStudent) { mutableStateOf(currentEditStudent.guardianMobile) }
        var fee by remember(currentEditStudent) { mutableStateOf(currentEditStudent.monthlyFee.toInt().toString()) }
        var nameError by remember { mutableStateOf<String?>(null) }
        var classError by remember { mutableStateOf<String?>(null) }
        var guardianError by remember { mutableStateOf<String?>(null) }
        var mobileError by remember { mutableStateOf<String?>(null) }
        var feeError by remember { mutableStateOf<String?>(null) }
        var showGuardianPicker by remember { mutableStateOf(false) }

        val classPresets = listOf("হিফজুল কুরআন বিভাগ", "নাযেরা বিভাগ", "কায়েদা বিভাগ", "মক্তব প্রভাতি")

        ExecutiveFormDialogShell(
            title = "শিক্ষার্থীর তথ্য সম্পাদনা",
            subtitle = "আইডি: ${toBengaliDigits(currentEditStudent.studentCode)}",
            icon = Icons.Default.Edit,
            confirmText = "তথ্য হালনাগাদ করুন",
            confirmTag = "confirm_edit_student_button",
            onDismiss = { showEditStudentDialog = false },
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
                    val updated = currentEditStudent.copy(
                        nameBn = name.trim(),
                        className = className.trim(),
                        guardianName = guardianName.trim(),
                        guardianMobile = mobile.trim(),
                        monthlyFee = parsedFee
                    )
                    viewModel.updateStudent(updated)
                    if (selectedStudentForDetails?.id == updated.id) {
                        selectedStudentForDetails = updated
                    }
                    Toast.makeText(context, "${updated.nameBn}-এর তথ্য হালনাগাদ সম্পন্ন হয়েছে।", Toast.LENGTH_SHORT).show()
                    showEditStudentDialog = false
                    studentToEdit = null
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
                Surface(
                    onClick = { showGuardianPicker = true },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_select_guardian_from_members_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "সদস্য তালিকা থেকে অভিভাবক পরিবর্তন করুন",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                    onValueChange = { fee = toEnglishDigits(it).filter { ch -> ch.isDigit() }; feeError = null },
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

        if (showGuardianPicker) {
            GuardianMemberPickerDialog(
                members = allMembers,
                onSelectMember = { member ->
                    guardianName = member.nameBn
                    mobile = member.mobile
                    guardianError = null
                    mobileError = null
                    showGuardianPicker = false
                    Toast.makeText(
                        context,
                        "${member.nameBn}-কে অভিভাবক হিসেবে নির্বাচন করা হয়েছে।",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onDismiss = { showGuardianPicker = false }
            )
        }
    }

    // Add Hifz Dialog
    if (showAddHifzDialog && students.isNotEmpty()) {
        val initialSelected = preselectedStudentForAction ?: students.first()
        var selectedStudent by remember(preselectedStudentForAction) { mutableStateOf(initialSelected) }
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
            onDismiss = {
                showAddHifzDialog = false
                preselectedStudentForAction = null
            },
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
                    preselectedStudentForAction = null
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
                    onValueChange = { paraNumber = toEnglishDigits(it).filter { ch -> ch.isDigit() }; paraError = null },
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
                    color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary,
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
                    onValueChange = { mistakeCount = toEnglishDigits(it).filter { ch -> ch.isDigit() } },
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
        val initialSelected = preselectedStudentForAction ?: students.first()
        var selectedStudent by remember(preselectedStudentForAction) { mutableStateOf(initialSelected) }
        var month by remember { mutableStateOf(currentMonthBn) }
        var amount by remember(selectedStudent) { mutableStateOf(selectedStudent.monthlyFee.toInt().toString()) }
        var monthError by remember { mutableStateOf<String?>(null) }
        var amountError by remember { mutableStateOf<String?>(null) }

        ExecutiveFormDialogShell(
            title = "মাদরাসা মাসিক ফি গ্রহণ",
            subtitle = "শিক্ষার্থীর বেতন ও ডিজিটাল রসিদ এন্ট্রি",
            icon = Icons.Default.Payments,
            confirmText = "ফি গ্রহণ নিশ্চিত করুন",
            confirmTag = "confirm_collect_madrasa_fee_button",
            onDismiss = {
                showCollectFeeDialog = false
                preselectedStudentForAction = null
            },
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
                    preselectedStudentForAction = null
                }
            }
        ) {
            MoreFormSection(title = "শিক্ষার্থী ও পরিশোধ বিবরণ", icon = Icons.Default.Receipt) {
                Text(
                    text = "শিক্ষার্থী নির্বাচন করুন:",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary,
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
                    onValueChange = { amount = toEnglishDigits(it).filter { ch -> ch.isDigit() }; amountError = null },
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

// ==========================================
// 1. Student Details Screen Component
// ==========================================
@Composable
private fun StudentDetailsScreen(
    student: StudentEntity,
    viewModel: MosqueViewModel,
    allMembers: List<MemberEntity>,
    canManage: Boolean,
    onBack: () -> Unit,
    onOpenFeeCollect: () -> Unit,
    onOpenHifzEntry: () -> Unit,
    onEditStudent: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val cardBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle

    val allHifz by viewModel.allHifzProgress.collectAsState()
    val allFees by viewModel.allFeePayments.collectAsState()
    val studentAttendance by viewModel.getAttendanceForStudent(student.id).collectAsState(initial = emptyList())

    val studentHifz = remember(allHifz, student.id) {
        allHifz.filter { it.studentId == student.id }
    }
    val studentFees = remember(allFees, student.id) {
        allFees.filter { it.studentId == student.id }
    }

    val totalPaidFee = remember(studentFees) {
        studentFees.sumOf { it.amountPaid }
    }

    val maxPara = remember(studentHifz) {
        studentHifz.maxOfOrNull { it.paraNumber } ?: 1
    }

    val presentDays = remember(studentAttendance) {
        studentAttendance.count { it.status == "present" }
    }
    val absentDays = remember(studentAttendance) {
        studentAttendance.count { it.status == "absent" }
    }
    val lateDays = remember(studentAttendance) {
        studentAttendance.count { it.status == "late" }
    }
    val totalAttendanceDays = presentDays + absentDays + lateDays
    val attendanceRate = remember(totalAttendanceDays, presentDays) {
        if (totalAttendanceDays == 0) 100 else ((presentDays.toDouble() / totalAttendanceDays) * 100).toInt()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
        ) {
            // Header Bar
            item(key = "student_details_header") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, cardBorder),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("student_details_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "ফিরে যান",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "শিক্ষার্থী বিস্তারিত বিবরণ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${student.className} • রোল/আইডি: ${toBengaliDigits(student.studentCode)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (canManage) {
                            FilledTonalIconButton(
                                onClick = onEditStudent,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("edit_student_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "সম্পাদনা",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Hero Card: Student Identity
            item(key = "student_hero_card") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, cardBorder),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft)
                                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = student.nameBn.take(1),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = student.className,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = MosqueSuccessGreen.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "সক্রিয়",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueSuccessGreen,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = student.nameBn,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "ভর্তি তারিখ: ${toBengaliDigits(student.joinDate)} • আইডি: ${toBengaliDigits(student.studentCode)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = cardBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("মাসিক ফি", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("৳${toBengaliDigits(student.monthlyFee.toInt().toString())}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("মোট পরিশোধ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("৳${toBengaliDigits(totalPaidFee.toInt().toString())}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MosqueSuccessGreen)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("হিফজ পারা", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("পারা ${toBengaliDigits(maxPara.toString())}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MosqueGoldDeep)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("উপস্থিতি", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${toBengaliDigits(attendanceRate.toString())}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (attendanceRate >= 80) MosqueSuccessGreen else MosqueUrgentRed)
                            }
                        }
                    }
                }
            }

            // Guardian Information Card
            item(key = "guardian_info_card") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, cardBorder),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FamilyRestroom,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "অভিভাবকের তথ্য ও সরাসরি যোগাযোগ",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                            border = BorderStroke(0.8.dp, cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = student.guardianName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "মোবাইল: ${toBengaliDigits(student.guardianMobile)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = {
                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.guardianMobile}"))
                                            context.startActivity(dialIntent)
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MosqueEmeraldPrimary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "কল করুন",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${student.guardianMobile}"))
                                            context.startActivity(smsIntent)
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MosqueInfoBlue)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Message,
                                            contentDescription = "এসএমএস পাঠান",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Hifz Progress Card
            item(key = "student_hifz_card") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, cardBorder),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = MosqueGoldDeep,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "হিফজ ও কুরআন পাঠ অগ্রগতি",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (canManage) {
                                TextButton(onClick = onOpenHifzEntry) {
                                    Text("+ সবক এন্ট্রি", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MosqueGoldDeep)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (studentHifz.isEmpty()) {
                            Text(
                                text = "এখনো কোনো হিফজ সবক রেকর্ড করা হয়নি।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            studentHifz.take(5).forEach { hfz ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                                    border = BorderStroke(0.6.dp, cardBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "পারা: ${toBengaliDigits(hfz.paraNumber.toString())} • পাঠ: ${toBengaliDigits(hfz.pageOrAyah)}",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = toBengaliDigits(hfz.date),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "মান: ",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MosqueGoldDeep
                                                )
                                                for (i in 1..5) {
                                                    Icon(
                                                        imageVector = if (i <= hfz.qualityScore) Icons.Default.Star else Icons.Default.StarBorder,
                                                        contentDescription = null,
                                                        tint = if (i <= hfz.qualityScore) MosqueGoldDeep else MosqueTextMuted,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
                                            }
                                            if (hfz.mistakeCount > 0) {
                                                Text(
                                                    text = "ভুল: ${toBengaliDigits(hfz.mistakeCount.toString())} টি",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MosqueUrgentRed
                                                )
                                            }
                                        }
                                        if (hfz.teacherFeedback.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = "উস্তাদের মন্তব্য: \"${hfz.teacherFeedback}\"",
                                                style = MaterialTheme.typography.labelSmall,
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

            // Fee Payments Card
            item(key = "student_fees_card") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, cardBorder),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = MosqueSuccessGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "বেতন ও ফি পরিশোধের ইতিহাস",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (canManage) {
                                TextButton(onClick = onOpenFeeCollect) {
                                    Text("+ ফি আদায়", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MosqueSuccessGreen)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (studentFees.isEmpty()) {
                            Text(
                                text = "এখনো কোনো ফি পরিশোধের রেকর্ড পাওয়া যায়নি।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            studentFees.take(5).forEach { fee ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                                    border = BorderStroke(0.6.dp, cardBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = fee.month,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "রসিদ: ${toBengaliDigits(fee.receiptCode)} • ${toBengaliDigits(fee.paidAt)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "৳${toBengaliDigits(fee.amountPaid.toInt().toString())}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MosqueSuccessGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Attendance Card
            item(key = "student_attendance_card") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, cardBorder),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventAvailable,
                                contentDescription = null,
                                tint = MosqueEmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ডিজিটাল উপস্থিতি রেকর্ড",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MosqueSuccessGreen.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f).padding(end = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("উপস্থিত", fontSize = 10.sp, color = MosqueSuccessGreen, fontWeight = FontWeight.Bold)
                                    Text("${toBengaliDigits(presentDays.toString())} দিন", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MosqueSuccessGreen)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MosqueUrgentRed.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("অনুপস্থিত", fontSize = 10.sp, color = MosqueUrgentRed, fontWeight = FontWeight.Bold)
                                    Text("${toBengaliDigits(absentDays.toString())} দিন", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MosqueUrgentRed)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MosqueGoldDeep.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f).padding(start = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("বিলম্বিত", fontSize = 10.sp, color = MosqueGoldDeep, fontWeight = FontWeight.Bold)
                                    Text("${toBengaliDigits(lateDays.toString())} দিন", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MosqueGoldDeep)
                                }
                            }
                        }

                        if (studentAttendance.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "সাম্প্রতিক উপস্থিতির লগ:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            studentAttendance.take(5).forEach { att ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = toBengaliDigits(att.date),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val (label, col) = when (att.status) {
                                        "present" -> "উপস্থিত" to MosqueSuccessGreen
                                        "late" -> "বিলম্বিত" to MosqueGoldDeep
                                        else -> "অনুপস্থিত" to MosqueUrgentRed
                                    }
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = col
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

// ==========================================
// 2. Guardian Member Picker Dialog
// ==========================================
@Composable
private fun GuardianMemberPickerDialog(
    members: List<MemberEntity>,
    onSelectMember: (MemberEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val isDark = LocalIsDarkTheme.current
    val cardBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle

    val filteredMembers = remember(members, searchQuery) {
        if (searchQuery.isBlank()) {
            members
        } else {
            val q = searchQuery.trim()
            members.filter {
                it.nameBn.contains(q, ignoreCase = true) ||
                    it.mobile.contains(q) ||
                    it.memberCode.contains(q, ignoreCase = true)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, cardBorder),
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "সদস্য তালিকা থেকে অভিভাবক নির্বাচন",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "যেকোনো সদস্যকে শিক্ষার্থীর অভিভাবক হিসেবে যুক্ত করুন",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "বন্ধ করুন",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                MosqueSearchBar(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "নাম, কোড বা মোবাইল নম্বর দিয়ে খুঁজুন...",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (filteredMembers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "কোনো সদস্য পাওয়া যায়নি।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredMembers, key = { it.id }) { member ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                                border = BorderStroke(0.8.dp, cardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectMember(member) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = member.nameBn.take(1),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = member.nameBn,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${toBengaliDigits(member.memberCode)} • ${toBengaliDigits(member.mobile)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (member.address.isNotBlank()) {
                                            Text(
                                                text = member.address,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.CheckCircleOutline,
                                        contentDescription = "নির্বাচন করুন",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
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
