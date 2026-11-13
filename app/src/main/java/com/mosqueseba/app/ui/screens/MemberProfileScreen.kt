package com.mosqueseba.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mosqueseba.app.data.local.entity.ChandaPaymentEntity
import com.mosqueseba.app.data.local.entity.MemberEntity
import com.mosqueseba.app.data.local.entity.MosqueEntity
import com.mosqueseba.app.data.local.entity.StaffEntity
import com.mosqueseba.app.data.local.entity.StaffPayrollEntity
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.*
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel

sealed class MemberProfileTarget {
    data class ChandaMember(val memberId: String) : MemberProfileTarget()
    data class SalariedStaff(val staffId: String?, val memberId: String?) : MemberProfileTarget()
}

@Composable
fun MemberProfileScreen(
    target: MemberProfileTarget,
    viewModel: MosqueViewModel,
    activeCycleMonth: String,
    onBack: () -> Unit,
    onOpenReceiveChanda: (MemberEntity, Double) -> Unit,
    onOpenPaySalary: (StaffEntity) -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val allMembers by viewModel.allMembers.collectAsState()
    val allChandaPayments by viewModel.allChandaPayments.collectAsState()
    val activeStaff by viewModel.activeStaff.collectAsState()
    val allPayrolls by viewModel.allPayrolls.collectAsState()
    val mosque by viewModel.mosque.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val savedMemberId = remember { viewModel.sessionManager.getSavedMemberId() }

    val canManageMembers = currentRole.canManageMembers()
    val canManageFinance = currentRole.canManageFinance()

    val resolvedMember: MemberEntity? = remember(target, allMembers, activeStaff) {
        when (target) {
            is MemberProfileTarget.ChandaMember -> allMembers.find { it.id == target.memberId }
            is MemberProfileTarget.SalariedStaff -> {
                target.memberId?.let { id -> allMembers.find { it.id == id } }
                    ?: target.staffId?.let { sid ->
                        val st = activeStaff.find { it.id == sid }
                        if (st != null) {
                            val normMobile = st.mobile.filter { it.isDigit() }
                            allMembers.find {
                                (normMobile.isNotEmpty() && it.mobile.filter { c -> c.isDigit() } == normMobile) ||
                                    it.nameBn.trim() == st.nameBn.trim()
                            }
                        } else null
                    }
            }
        }
    }

    val resolvedStaff: StaffEntity? = remember(target, activeStaff, resolvedMember) {
        when (target) {
            is MemberProfileTarget.SalariedStaff -> {
                target.staffId?.let { sid -> activeStaff.find { it.id == sid } }
                    ?: resolvedMember?.let { m ->
                        val normMobile = m.mobile.filter { it.isDigit() }
                        activeStaff.find {
                            (normMobile.isNotEmpty() && it.mobile.filter { c -> c.isDigit() } == normMobile) ||
                                it.nameBn.trim() == m.nameBn.trim()
                        }
                    }
            }
            is MemberProfileTarget.ChandaMember -> null
        }
    }

    val isSalaried = target is MemberProfileTarget.SalariedStaff || resolvedStaff != null

    val displayNameBn = resolvedMember?.nameBn ?: resolvedStaff?.nameBn ?: "সদস্য প্রোফাইল"
    val displayNameEn = resolvedMember?.nameEn?.takeIf { it.isNotBlank() } ?: ""
    val displayMobile = resolvedMember?.mobile ?: resolvedStaff?.mobile ?: ""
    val displayRole = UserRole.fromCode(resolvedMember?.roleCode ?: resolvedStaff?.roleCode ?: "general_member")
    val displayDesignation = resolvedStaff?.designationBn ?: displayRole.titleBn
    val displayCode = resolvedMember?.memberCode ?: (resolvedStaff?.id?.uppercase() ?: "STAFF")
    val isActive = (resolvedMember?.status ?: if (resolvedStaff?.isActive == true) "active" else "inactive") == "active"

    // Chanda stats
    val memberPayments = remember(allChandaPayments, resolvedMember?.id) {
        resolvedMember?.let { m -> allChandaPayments.filter { it.memberId == m.id } } ?: emptyList()
    }
    val paidThisCycle = remember(memberPayments, activeCycleMonth) {
        memberPayments.filter { it.month == activeCycleMonth }.sumOf { it.amountPaid }
    }
    val totalDeposited = remember(memberPayments) {
        memberPayments.sumOf { it.amountPaid }
    }
    val monthlyCommitment = resolvedMember?.monthlyChanda ?: 0.0
    val dueThisCycle = (monthlyCommitment - paidThisCycle).coerceAtLeast(0.0)

    // Staff payroll stats
    val staffPayrolls = remember(allPayrolls, resolvedStaff?.id) {
        resolvedStaff?.let { st -> allPayrolls.filter { it.staffId == st.id } } ?: emptyList()
    }

    var selectedReceiptForDialog by remember { mutableStateOf<ChandaPaymentEntity?>(null) }
    var showEditMemberDialog by remember { mutableStateOf(false) }
    var showEditStaffSalaryDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var roleMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MosqueBgLight)
    ) {
        // Top Emerald Header Bar
        Surface(
            color = Color.Transparent,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MosqueEmeraldDarkGradient)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("profile_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "ফিরে যান",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = if (isSalaried) "বেতনভুক্ত সদস্য প্রোফাইল" else "সদস্য বিস্তারিত প্রোফাইল",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "আইডি: ${toBengaliDigits(displayCode)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MosqueGoldLight
                            )
                        }
                    }

                    if (canManageMembers && (resolvedMember != null || resolvedStaff != null)) {
                        IconButton(
                            onClick = {
                                if (resolvedMember != null) showEditMemberDialog = true
                                else if (resolvedStaff != null) showEditStaffSalaryDialog = true
                            },
                            modifier = Modifier.testTag("profile_edit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "তথ্য সম্পাদনা",
                                tint = MosqueGoldLight
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(IslamicGoldTrimGradient)
                )
            }
        }

        if (resolvedMember == null && resolvedStaff == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PersonOff, contentDescription = null, tint = MosqueTextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("সদস্যের তথ্য পাওয়া যায়নি", color = MosqueTextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)) {
                        Text("তালিকায় ফিরুন")
                    }
                }
            }
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Hero Profile Summary Card
            item {
                MehrabArchCard(
                    backgroundColor = Color.White,
                    borderColor = MosqueCardBorderGold
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val photoBitmap = remember(resolvedMember?.photoUrl) {
                            loadLocalThumbnail(resolvedMember?.photoUrl, 320)
                        }

                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(CircleShape)
                                    .background(MosqueEmeraldSoft)
                                    .border(2.5.dp, MosqueGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (photoBitmap != null) {
                                    Image(
                                        bitmap = photoBitmap,
                                        contentDescription = displayNameBn,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = displayNameBn.firstOrNull()?.toString() ?: "স",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MosqueEmeraldDark
                                    )
                                }
                            }
                            Surface(
                                shape = CircleShape,
                                color = if (isActive) MosqueSuccessGreen else MosqueUrgentRed,
                                border = BorderStroke(2.dp, Color.White),
                                modifier = Modifier.size(20.dp)
                            ) {}
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = displayNameBn,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MosqueEmeraldDark
                        )
                        if (displayNameEn.isNotBlank()) {
                            Text(
                                text = displayNameEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MosqueTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MosqueGoldLight.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, MosqueGoldDeep.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "${displayRole.emoji} $displayDesignation",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueEmeraldDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                color = if (isActive) MosqueSuccessGreen.copy(alpha = 0.12f) else MosqueUrgentRed.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isActive) "সক্রিয়" else "নিষ্ক্রিয়",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) MosqueSuccessGreen else MosqueUrgentRed,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Action Row: Call + Collect Chanda / Pay Salary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (displayMobile.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$displayMobile"))
                                        context.startActivity(dialIntent)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("profile_call_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MosqueEmeraldPrimary)
                                ) {
                                    Icon(
                                        Icons.Default.Call,
                                        contentDescription = null,
                                        tint = MosqueEmeraldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("কল করুন", color = MosqueEmeraldPrimary, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (!isSalaried && resolvedMember != null && canManageFinance) {
                                Button(
                                    onClick = {
                                        val suggested = if (dueThisCycle > 0) dueThisCycle else resolvedMember.monthlyChanda
                                        onOpenReceiveChanda(resolvedMember, suggested)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("profile_collect_chanda_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                ) {
                                    Icon(
                                        Icons.Default.Payments,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("চাঁদা গ্রহণ", fontWeight = FontWeight.Bold)
                                }
                            } else if (isSalaried && resolvedStaff != null && canManageFinance) {
                                Button(
                                    onClick = { onOpenPaySalary(resolvedStaff) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("profile_pay_salary_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                ) {
                                    Icon(
                                        Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("বেতন পরিশোধ", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 2. Financial Summary Strip (Chanda or Salary)
            item {
                if (!isSalaried) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileStatBox(
                            modifier = Modifier.weight(1f),
                            label = "মাসিক চাঁদা",
                            value = if (monthlyCommitment > 0) {
                                "৳${toBengaliDigits(String.format("%,.0f", monthlyCommitment))}"
                            } else "ঐচ্ছিক",
                            accent = MosqueEmeraldPrimary
                        )
                        ProfileStatBox(
                            modifier = Modifier.weight(1f),
                            label = "মোট জমা",
                            value = "৳${toBengaliDigits(String.format("%,.0f", totalDeposited))}",
                            accent = MosqueSuccessGreen
                        )
                        ProfileStatBox(
                            modifier = Modifier.weight(1f),
                            label = "চলতি বকেয়া",
                            value = "৳${toBengaliDigits(String.format("%,.0f", dueThisCycle))}",
                            accent = if (dueThisCycle > 0) MosqueUrgentRed else MosqueSuccessGreen
                        )
                    }
                } else {
                    val salaryVal = resolvedStaff?.salaryAmount ?: 0.0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileStatBox(
                            modifier = Modifier.weight(1f),
                            label = "মাসিক বেতন",
                            value = "৳${toBengaliDigits(String.format("%,.0f", salaryVal))}",
                            accent = MosqueEmeraldPrimary
                        )
                        ProfileStatBox(
                            modifier = Modifier.weight(1f),
                            label = "খাবার সুবিধা",
                            value = if (resolvedStaff?.mealRequired != false) "প্রযোজ্য" else "প্রযোজ্য নয়",
                            accent = MosqueGoldDeep
                        )
                        ProfileStatBox(
                            modifier = Modifier.weight(1f),
                            label = "বেতন রসিদ",
                            value = "${toBengaliDigits(staffPayrolls.size.toString())} বার",
                            accent = MosqueSuccessGreen
                        )
                    }
                }
            }

            // 3. Personal & Mosque Information
            item {
                RubElHizbDivider(title = "ব্যক্তিগত ও সাংগঠনিক তথ্য")
                MehrabArchCard(
                    backgroundColor = Color.White,
                    borderColor = MosqueCardBorderGold
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileInfoRow("সদস্য / স্টাফ কোড", toBengaliDigits(displayCode))
                        ProfileInfoRow("কমিটির স্তর", displayRole.tier.titleBn)
                        ProfileInfoRow("মোবাইল নম্বর", toBengaliDigits(displayMobile.ifBlank { "প্রযোজ্য নয়" }))
                        if (resolvedMember != null) {
                            ProfileInfoRow("ইমেইল", resolvedMember.email.ifBlank { "প্রযোজ্য নয়" })
                            ProfileInfoRow("জাতীয় পরিচয়পত্র", toBengaliDigits(resolvedMember.nid.ifBlank { "প্রযোজ্য নয়" }))
                            ProfileInfoRow("বাসার ঠিকানা", resolvedMember.address.ifBlank { "প্রযোজ্য নয়" })
                            ProfileInfoRow("এলাকা / মহল্লা", resolvedMember.villageArea.ifBlank { "প্রযোজ্য নয়" })
                            ProfileInfoRow("যোগদানের তারিখ", toBengaliDigits(resolvedMember.joinDate))
                            if (!isSalaried) {
                                ProfileInfoRow(
                                    label = "স্টাফদের খাবার প্রদান",
                                    value = if (resolvedMember.willingToProvideStaffMeals) "হ্যাঁ, রাজি আছেন" else "না",
                                    valueColor = if (resolvedMember.willingToProvideStaffMeals) MosqueSuccessGreen else MosqueTextSecondary
                                )
                            }
                        } else if (resolvedStaff != null) {
                            ProfileInfoRow("নিয়োগের ধরন", if (resolvedStaff.employmentType == "full_time") "পূর্ণকালীন" else "খণ্ডকালীন")
                            ProfileInfoRow("যোগদানের তারিখ", toBengaliDigits(resolvedStaff.joinDate))
                            ProfileInfoRow(
                                label = "খাবার ব্যবস্থা (জায়গীর)",
                                value = if (resolvedStaff.mealRequired) "মসজিদ/সদস্যের মাধ্যমে নির্ধারিত" else "প্রযোজ্য নয়"
                            )
                        }
                    }
                }
            }

            // 4. Payment / Payroll History
            item {
                RubElHizbDivider(
                    title = if (isSalaried) "বেতন পরিশোধের ইতিহাস" else "চাঁদা জমার রসিদ সমূহ"
                )
            }

            if (!isSalaried) {
                if (memberPayments.isEmpty()) {
                    item {
                        MehrabArchCard(backgroundColor = Color.White, borderColor = MosqueCardBorderSubtle) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = MosqueTextMuted, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("এখনো কোনো চাঁদা জমার রসিদ পাওয়া যায়নি", style = MaterialTheme.typography.bodySmall, color = MosqueTextMuted)
                            }
                        }
                    }
                } else {
                    items(memberPayments, key = { it.id }) { payment ->
                        Card(
                            onClick = { selectedReceiptForDialog = payment },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, MosqueCardBorderGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(MosqueSuccessGreen.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, tint = MosqueSuccessGreen, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "${payment.month} • রসিদ ${toBengaliDigits(payment.receiptCode)}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueEmeraldDark
                                        )
                                        Text(
                                            text = "মাধ্যম: ${if (payment.method == "cash") "নগদ" else "মোবাইল ব্যাংকিং"} • ${toBengaliDigits(payment.paidAt)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MosqueTextSecondary
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "৳${toBengaliDigits(String.format("%,.0f", payment.amountPaid))}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MosqueSuccessGreen
                                    )
                                    Text(
                                        text = "রসিদ দেখুন",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MosqueEmeraldPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                if (staffPayrolls.isEmpty()) {
                    item {
                        MehrabArchCard(backgroundColor = Color.White, borderColor = MosqueCardBorderSubtle) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MosqueTextMuted, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("এখনো পে-রোল এন্ট্রি রেকর্ড করা হয়নি", style = MaterialTheme.typography.bodySmall, color = MosqueTextMuted)
                            }
                        }
                    }
                } else {
                    items(staffPayrolls, key = { it.id }) { payroll ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, MosqueCardBorderGold),
                            modifier = Modifier.fillMaxWidth()
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
                                        text = "${payroll.month} বেতন পরিশোধ",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueEmeraldDark
                                    )
                                    Text(
                                        text = "মূল: ৳${toBengaliDigits(payroll.salaryAmount.toInt().toString())} + বোনাস: ৳${toBengaliDigits(payroll.bonusAmount.toInt().toString())}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MosqueTextSecondary
                                    )
                                    payroll.paidDate?.let { d ->
                                        Text("তারিখ: ${toBengaliDigits(d)}", style = MaterialTheme.typography.labelSmall, color = MosqueTextMuted)
                                    }
                                }
                                Surface(
                                    color = MosqueSuccessGreen.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "৳${toBengaliDigits(String.format("%,.0f", payroll.netSalary))}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MosqueSuccessGreen,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Admin / President Control Panel
            if (canManageMembers && resolvedMember != null) {
                item {
                    RubElHizbDivider(title = "প্রশাসনিক নিয়ন্ত্রণ")
                    MehrabArchCard(
                        backgroundColor = Color.White,
                        borderColor = MosqueCardBorderGold
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Role change dropdown
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { roleMenuExpanded = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, MosqueEmeraldPrimary)
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MosqueEmeraldPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("পদবি পরিবর্তন / দায়িত্ব বণ্টন", color = MosqueEmeraldPrimary, fontWeight = FontWeight.Bold)
                                }
                                DropdownMenu(
                                    expanded = roleMenuExpanded,
                                    onDismissRequest = { roleMenuExpanded = false },
                                    modifier = Modifier.heightIn(max = 320.dp)
                                ) {
                                    UserRole.entries.forEach { targetRole ->
                                        DropdownMenuItem(
                                            text = { Text("${targetRole.emoji} ${targetRole.titleBn}", fontSize = 13.sp) },
                                            onClick = {
                                                viewModel.updateMemberRole(resolvedMember, targetRole)
                                                roleMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showEditMemberDialog = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("সম্পাদনা", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                val isSelfAccount = resolvedMember.id == savedMemberId ||
                                    (currentRole == UserRole.PRESIDENT && resolvedMember.roleCode == "president")
                                if (!isSelfAccount) {
                                    OutlinedButton(
                                        onClick = { viewModel.toggleMemberStatus(resolvedMember) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (isActive) MosqueWarningAmber else MosqueSuccessGreen
                                        )
                                    ) {
                                        Icon(
                                            imageVector = if (isActive) Icons.Default.PauseCircle else Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isActive) "নিষ্ক্রিয় করুন" else "সক্রিয় করুন",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            val isSelfPresident = resolvedMember.id == savedMemberId ||
                                (currentRole == UserRole.PRESIDENT && resolvedMember.roleCode == "president")
                            if (!isSelfPresident) {
                                TextButton(
                                    onClick = { showDeleteConfirmDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp),
                                    colors = ButtonDefaults.textButtonColors(contentColor = MosqueUrgentRed)
                                ) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("সদস্য তালিকা থেকে অপসারণ করুন", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    // Official Receipt Dialog
    selectedReceiptForDialog?.let { payment ->
        OfficialReceiptDialog(
            payment = payment,
            mosqueName = mosque?.nameBn ?: "বায়তুল জান্নাত জামে মসজিদ",
            mosqueAddress = mosque?.address ?: "মিরপুর-১০, ঢাকা",
            committeeId = mosque?.committeeId ?: "MC-DHK-2024",
            onDismiss = { selectedReceiptForDialog = null }
        )
    }

    // Edit Member Dialog
    if (showEditMemberDialog && resolvedMember != null) {
        AddMemberDialog(
            initialMember = resolvedMember,
            initialStaff = resolvedStaff,
            onDismiss = { showEditMemberDialog = false },
            onConfirm = { nameBn, nameEn, mobile, email, nid, address, villageArea, roleCode, monthlyChanda, photoUrl, willingToProvideStaffMeals, salaryAmount, staffMealRequired ->
                viewModel.updateMemberDetails(
                    resolvedMember.copy(
                        nameBn = nameBn,
                        nameEn = nameEn,
                        mobile = mobile,
                        email = email,
                        nid = nid,
                        address = address,
                        villageArea = villageArea,
                        roleCode = roleCode,
                        monthlyChanda = monthlyChanda,
                        photoUrl = photoUrl,
                        willingToProvideStaffMeals = willingToProvideStaffMeals
                    )
                )
                resolvedStaff?.let { st ->
                    viewModel.updateStaffDetails(
                        st.copy(
                            nameBn = nameBn,
                            mobile = mobile,
                            roleCode = roleCode,
                            designationBn = UserRole.fromCode(roleCode).titleBn,
                            salaryAmount = if (salaryAmount > 0) salaryAmount else st.salaryAmount,
                            mealRequired = staffMealRequired
                        )
                    )
                }
            }
        )
    }

    // Edit Staff Salary Dialog (if no linked MemberEntity)
    if (showEditStaffSalaryDialog && resolvedStaff != null) {
        var newSalary by remember(resolvedStaff.id) { mutableStateOf(resolvedStaff.salaryAmount.toInt().toString()) }
        var newMobile by remember(resolvedStaff.id) { mutableStateOf(resolvedStaff.mobile) }
        var mealRequired by remember(resolvedStaff.id) { mutableStateOf(resolvedStaff.mealRequired) }
        Dialog(onDismissRequest = { showEditStaffSalaryDialog = false }) {
            MehrabArchCard(backgroundColor = Color.White, borderColor = MosqueCardBorderGold) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("বেতনভুক্ত স্টাফ তথ্য হালনাগাদ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MosqueEmeraldDark)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newMobile,
                        onValueChange = { newMobile = it },
                        label = { Text("মোবাইল নম্বর") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newSalary,
                        onValueChange = { newSalary = it.filter { c -> c.isDigit() } },
                        label = { Text("মাসিক বেতন (৳)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = mealRequired, onCheckedChange = { mealRequired = it })
                        Text("খাবার সুবিধা প্রযোজ্য", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showEditStaffSalaryDialog = false }) { Text("বাতিল") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.updateStaffDetails(
                                    resolvedStaff.copy(
                                        mobile = newMobile.trim(),
                                        salaryAmount = newSalary.toDoubleOrNull() ?: resolvedStaff.salaryAmount,
                                        mealRequired = mealRequired
                                    )
                                )
                                showEditStaffSalaryDialog = false
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

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && resolvedMember != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("সদস্য অপসারণ নিশ্চিত করুন", fontWeight = FontWeight.Bold) },
            text = { Text("আপনি কি নিশ্চিতভাবে \"${resolvedMember.nameBn}\"-কে সদস্য তালিকা থেকে অপসারণ করতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMember(resolvedMember)
                        showDeleteConfirmDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueUrgentRed)
                ) {
                    Text("হ্যাঁ, অপসারণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun ProfileStatBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, MosqueCardBorderGold)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MosqueTextSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = accent
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    valueColor: Color = MosqueTextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MosqueTextMuted,
            modifier = Modifier.weight(0.9f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            modifier = Modifier.weight(1.3f)
        )
    }
    HorizontalDivider(color = MosqueCardBorderSubtle.copy(alpha = 0.7f))
}
