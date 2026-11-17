package com.mosqueseba.app.ui.dialogs

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mosqueseba.app.data.local.entity.MemberEntity
import com.mosqueseba.app.data.local.entity.StaffEntity
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.*
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel
import kotlinx.coroutines.launch

private data class OfficialPersonUiModel(
    val id: String,
    val isStaff: Boolean,
    val nameBn: String,
    val roleTitleBn: String,
    val roleCode: String,
    val mobile: String,
    val photoUrl: String?,
    val signatureUrl: String?,
    val stampUrl: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficialSealSignatureDialog(
    viewModel: MosqueViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = LocalIsDarkTheme.current

    val mosque by viewModel.currentMosque.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    val activeStaff by viewModel.activeStaff.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Officials, 1: Mosque Seal, 2: Signature Pad

    // Picker tracking state
    var pendingPickMemberId by remember { mutableStateOf<String?>(null) }
    var pendingPickStaffId by remember { mutableStateOf<String?>(null) }
    var pendingPickType by remember { mutableStateOf("sig") } // "sig", "stamp", "mosque_seal"

    // Signature pad preselected target
    var padTargetPersonId by remember { mutableStateOf<String?>(null) }
    var padTargetIsStaff by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val memberId = pendingPickMemberId
                val staffId = pendingPickStaffId
                val pickType = pendingPickType

                when {
                    pickType == "mosque_seal" -> {
                        val (savedPath, _) = savePickedImageToAppStorage(context, uri, "mosque_seal", 800)
                        if (savedPath != null) {
                            viewModel.updateMosqueSeal(savedPath)
                            MosqueToastController.success("মসজিদের কেন্দ্রীয় সিলমোহর হালনাগাদ হয়েছে", "সিলমোহর সংরক্ষিত")
                        }
                    }
                    memberId != null -> {
                        val (savedPath, _) = savePickedImageToAppStorage(context, uri, "${pickType}_$memberId", 600)
                        if (savedPath != null) {
                            val existing = allMembers.firstOrNull { it.id == memberId }
                            if (pickType == "sig") {
                                viewModel.updateMemberSignatureAndStamp(memberId, savedPath, existing?.stampUrl)
                                MosqueToastController.success("${existing?.nameBn ?: "সদস্য"}-এর ডিজিটাল স্বাক্ষর সংযুক্ত হয়েছে", "স্বাক্ষর সংরক্ষিত")
                            } else {
                                viewModel.updateMemberSignatureAndStamp(memberId, existing?.signatureUrl, savedPath)
                                MosqueToastController.success("${existing?.nameBn ?: "সদস্য"}-এর অফিসিয়াল সিলমোহর সংযুক্ত হয়েছে", "সিলমোহর সংরক্ষিত")
                            }
                        }
                    }
                    staffId != null -> {
                        val (savedPath, _) = savePickedImageToAppStorage(context, uri, "${pickType}_$staffId", 600)
                        if (savedPath != null) {
                            val existing = activeStaff.firstOrNull { it.id == staffId }
                            if (pickType == "sig") {
                                viewModel.updateStaffSignatureAndStamp(staffId, savedPath, existing?.stampUrl)
                                MosqueToastController.success("${existing?.nameBn ?: "ইমাম"}-এর স্বাক্ষর সংরক্ষিত হয়েছে", "স্বাক্ষর সংরক্ষিত")
                            } else {
                                viewModel.updateStaffSignatureAndStamp(staffId, existing?.signatureUrl, savedPath)
                                MosqueToastController.success("${existing?.nameBn ?: "ইমাম"}-এর সিলমোহর সংরক্ষিত হয়েছে", "সিলমোহর সংরক্ষিত")
                            }
                        }
                    }
                }
                pendingPickMemberId = null
                pendingPickStaffId = null
            }
        }
    }

    // Build designated officials list
    val officials = remember(allMembers, activeStaff) {
        val list = mutableListOf<OfficialPersonUiModel>()

        // 1. Key Committee Members (President, Secretary, Treasurer, Vice President, etc.)
        val priorityRoles = listOf(
            UserRole.PRESIDENT.code,
            UserRole.GENERAL_SECRETARY.code,
            UserRole.TREASURER.code,
            UserRole.VICE_PRESIDENT.code,
            UserRole.ASSISTANT_SECRETARY.code,
            UserRole.ASSISTANT_TREASURER.code,
            UserRole.OFFICE_SECRETARY.code
        )

        allMembers.filter { it.roleCode in priorityRoles }.forEach { m ->
            list.add(
                OfficialPersonUiModel(
                    id = m.id,
                    isStaff = false,
                    nameBn = m.nameBn,
                    roleTitleBn = m.designationBn ?: UserRole.fromCode(m.roleCode).titleBn,
                    roleCode = m.roleCode,
                    mobile = m.mobile,
                    photoUrl = m.photoUrl,
                    signatureUrl = m.signatureUrl,
                    stampUrl = m.stampUrl
                )
            )
        }

        // 2. Staff (Imam, Muazzin, Khadem)
        activeStaff.forEach { st ->
            if (list.none { it.mobile.filter { c -> c.isDigit() } == st.mobile.filter { c -> c.isDigit() } }) {
                list.add(
                    OfficialPersonUiModel(
                        id = st.id,
                        isStaff = true,
                        nameBn = st.nameBn,
                        roleTitleBn = st.designationBn.ifBlank { UserRole.fromCode(st.roleCode).titleBn },
                        roleCode = st.roleCode,
                        mobile = st.mobile,
                        photoUrl = null,
                        signatureUrl = st.signatureUrl,
                        stampUrl = st.stampUrl
                    )
                )
            }
        }

        // 3. Fallback remaining members
        allMembers.filter { it.roleCode !in priorityRoles && it.roleCode != "general_member" }.forEach { m ->
            list.add(
                OfficialPersonUiModel(
                    id = m.id,
                    isStaff = false,
                    nameBn = m.nameBn,
                    roleTitleBn = m.designationBn ?: UserRole.fromCode(m.roleCode).titleBn,
                    roleCode = m.roleCode,
                    mobile = m.mobile,
                    photoUrl = m.photoUrl,
                    signatureUrl = m.signatureUrl,
                    stampUrl = m.stampUrl
                )
            )
        }
        list
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MehrabArchCard(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp),
            backgroundColor = if (isDark) MosqueNightSurface else Color.White,
            borderColor = if (isDark) MosqueNightBorder else MosqueCardBorderGold,
            elevation = 10.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Executive Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MosqueEmeraldDarkGradient)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MosqueGoldDeep.copy(alpha = 0.25f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Draw,
                                        contentDescription = null,
                                        tint = MosqueGoldLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "কর্মকর্তাদের সিলমোহর ও ডিজিটাল স্বাক্ষর",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "রসিদ, নোটিশ ও অডিট বিবরণীতে স্বয়ংক্রিয়ভাবে সংযুক্ত হবে",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = Color.White)
                        }
                    }
                }

                // Tab Selector
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                    contentColor = MosqueEmeraldPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("পদধারী কর্মকর্তাবৃন্দ", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("মসজিদের সিলমোহর", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("স্ক্রিনে সই আঁকুন", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Draw, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                // Content View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(12.dp)
                ) {
                    when (selectedTab) {
                        0 -> OfficialsListTab(
                            officials = officials,
                            isDark = isDark,
                            onPickSignature = { person ->
                                pendingPickType = "sig"
                                if (person.isStaff) {
                                    pendingPickStaffId = person.id
                                    pendingPickMemberId = null
                                } else {
                                    pendingPickMemberId = person.id
                                    pendingPickStaffId = null
                                }
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onDrawSignature = { person ->
                                padTargetPersonId = person.id
                                padTargetIsStaff = person.isStaff
                                selectedTab = 2
                            },
                            onPickStamp = { person ->
                                pendingPickType = "stamp"
                                if (person.isStaff) {
                                    pendingPickStaffId = person.id
                                    pendingPickMemberId = null
                                } else {
                                    pendingPickMemberId = person.id
                                    pendingPickStaffId = null
                                }
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onClearSignature = { person ->
                                if (person.isStaff) {
                                    viewModel.updateStaffSignatureAndStamp(person.id, null, person.stampUrl)
                                } else {
                                    viewModel.updateMemberSignatureAndStamp(person.id, null, person.stampUrl)
                                }
                                MosqueToastController.info("${person.nameBn}-এর স্বাক্ষর অপসারিত হয়েছে")
                            },
                            onClearStamp = { person ->
                                if (person.isStaff) {
                                    viewModel.updateStaffSignatureAndStamp(person.id, person.signatureUrl, null)
                                } else {
                                    viewModel.updateMemberSignatureAndStamp(person.id, person.signatureUrl, null)
                                }
                                MosqueToastController.info("${person.nameBn}-এর সিলমোহর অপসারিত হয়েছে")
                            }
                        )

                        1 -> MosqueSealTab(
                            sealUrl = mosque?.sealUrl,
                            mosqueName = mosque?.nameBn ?: "মসজিদ পরিচালনা কমিটি",
                            isDark = isDark,
                            onPickSeal = {
                                pendingPickType = "mosque_seal"
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onClearSeal = {
                                viewModel.updateMosqueSeal(null)
                                MosqueToastController.info("মসজিদের সিলমোহর অপসারিত হয়েছে")
                            }
                        )

                        2 -> SignatureDrawingPadTab(
                            officials = officials,
                            preselectedId = padTargetPersonId,
                            preselectedIsStaff = padTargetIsStaff,
                            isDark = isDark,
                            onSaveSignature = { targetPerson, bitmap ->
                                scope.launch {
                                    val savedPath = saveBitmapToAppStorage(context, bitmap, "pad_sig_${targetPerson.id}")
                                    if (savedPath != null) {
                                        if (targetPerson.isStaff) {
                                            viewModel.updateStaffSignatureAndStamp(targetPerson.id, savedPath, targetPerson.stampUrl)
                                        } else {
                                            viewModel.updateMemberSignatureAndStamp(targetPerson.id, savedPath, targetPerson.stampUrl)
                                        }
                                        MosqueToastController.success("${targetPerson.nameBn}-এর ডিজিটাল স্বাক্ষর সফলভাবে সংরক্ষিত হয়েছে!", "স্বাক্ষর সম্পন্ন")
                                        selectedTab = 0
                                    } else {
                                        MosqueToastController.error("স্বাক্ষর সংরক্ষণে সমস্যা হয়েছে")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OfficialsListTab(
    officials: List<OfficialPersonUiModel>,
    isDark: Boolean,
    onPickSignature: (OfficialPersonUiModel) -> Unit,
    onDrawSignature: (OfficialPersonUiModel) -> Unit,
    onPickStamp: (OfficialPersonUiModel) -> Unit,
    onClearSignature: (OfficialPersonUiModel) -> Unit,
    onClearStamp: (OfficialPersonUiModel) -> Unit
) {
    if (officials.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("কোনো পদধারী সদস্য তালিকাভুক্ত নেই", color = MosqueTextMuted)
        }
        return
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Surface(
                color = MosqueEmeraldSoft.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MosqueEmeraldPrimary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MosqueEmeraldPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "এখানে সংরক্ষিত স্বাক্ষর ও সিলমোহর সরাসরি চাঁদার রসিদ, মাসিক অডিট রিপোর্ট, জুমার ক্যাশবক্স ভাউচার ও নোটিশে প্রিন্ট হবে।",
                        fontSize = 11.5.sp,
                        color = MosqueEmeraldDark,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        items(officials, key = { it.id }) { person ->
            val sigBitmap = remember(person.signatureUrl) { loadLocalThumbnail(person.signatureUrl, 200) }
            val stampBitmap = remember(person.stampUrl) { loadLocalThumbnail(person.stampUrl, 200) }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight
                ),
                border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Person Info Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = CircleShape,
                                color = MosqueEmeraldPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (person.isStaff) Icons.Default.Mosque else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MosqueEmeraldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = person.nameBn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary
                                )
                                Text(
                                    text = "${person.roleTitleBn} • ${toBengaliDigits(person.mobile)}",
                                    fontSize = 11.sp,
                                    color = MosqueEmeraldPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MosqueGold.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = person.roleTitleBn,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MosqueGoldDeep,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = if (isDark) MosqueNightBorder else Color(0xFFEEEEEE))
                    Spacer(modifier = Modifier.height(10.dp))

                    // 2 Columns: Signature & Rubber Stamp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Signature Card
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDark) MosqueNightSurface else Color.White,
                            border = BorderStroke(1.dp, if (person.signatureUrl != null) MosqueSuccessGreen else Color.LightGray)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ডিজিটাল স্বাক্ষর",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (person.signatureUrl != null) MosqueSuccessGreen else MosqueTextMuted
                                    )
                                    if (person.signatureUrl != null) {
                                        IconButton(
                                            onClick = { onClearSignature(person) },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = MosqueUrgentRed, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                if (sigBitmap != null) {
                                    Image(
                                        bitmap = sigBitmap,
                                        contentDescription = "স্বাক্ষর",
                                        modifier = Modifier
                                            .height(44.dp)
                                            .fillMaxWidth(),
                                        contentScale = ContentScale.Fit
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        TextButton(
                                            onClick = { onPickSignature(person) },
                                            contentPadding = PaddingValues(2.dp)
                                        ) {
                                            Text("ছবি বদল", fontSize = 10.sp)
                                        }
                                        TextButton(
                                            onClick = { onDrawSignature(person) },
                                            contentPadding = PaddingValues(2.dp)
                                        ) {
                                            Text("পুনরায় সই", fontSize = 10.sp)
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .height(44.dp)
                                            .fillMaxWidth()
                                            .background(Color(0xFFF9F9F9), RoundedCornerShape(4.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("স্বাক্ষর নেই", fontSize = 10.5.sp, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onPickSignature(person) },
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            contentPadding = PaddingValues(0.dp),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("গ্যালারি", fontSize = 10.sp)
                                        }
                                        Button(
                                            onClick = { onDrawSignature(person) },
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            contentPadding = PaddingValues(0.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                        ) {
                                            Text("স্ক্রিনে সই", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Rubber Stamp Card
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDark) MosqueNightSurface else Color.White,
                            border = BorderStroke(1.dp, if (person.stampUrl != null) MosqueGoldDeep else Color.LightGray)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "অফিসিয়াল স্ট্যাম্প",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (person.stampUrl != null) MosqueGoldDeep else MosqueTextMuted
                                    )
                                    if (person.stampUrl != null) {
                                        IconButton(
                                            onClick = { onClearStamp(person) },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = MosqueUrgentRed, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                if (stampBitmap != null) {
                                    Image(
                                        bitmap = stampBitmap,
                                        contentDescription = "স্ট্যাম্প",
                                        modifier = Modifier
                                            .height(44.dp)
                                            .fillMaxWidth(),
                                        contentScale = ContentScale.Fit
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    TextButton(
                                        onClick = { onPickStamp(person) },
                                        contentPadding = PaddingValues(2.dp)
                                    ) {
                                        Text("সিল পরিবর্তন", fontSize = 10.sp)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .height(44.dp)
                                            .fillMaxWidth()
                                            .background(Color(0xFFF9F9F9), RoundedCornerShape(4.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("সিল নেই", fontSize = 10.5.sp, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedButton(
                                        onClick = { onPickStamp(person) },
                                        modifier = Modifier.fillMaxWidth().height(32.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, MosqueGoldDeep)
                                    ) {
                                        Text("স্ট্যাম্প আপলোড", fontSize = 10.sp, color = MosqueGoldDeep)
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

@Composable
private fun MosqueSealTab(
    sealUrl: String?,
    mosqueName: String,
    isDark: Boolean,
    onPickSeal: () -> Unit,
    onClearSeal: () -> Unit
) {
    val sealBitmap = remember(sealUrl) { loadLocalThumbnail(sealUrl, 400) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = if (isDark) MosqueNightSurfaceVariant else MosqueGoldSoft.copy(alpha = 0.35f),
            border = BorderStroke(2.dp, MosqueGoldDeep),
            modifier = Modifier.size(150.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (sealBitmap != null) {
                    Image(
                        bitmap = sealBitmap,
                        contentDescription = "মসজিদের সিলমোহর",
                        modifier = Modifier
                            .fillMaxSize(0.9f)
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = MosqueGoldDeep,
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "গোল সিল যুক্ত নেই",
                            fontSize = 11.sp,
                            color = MosqueTextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = mosqueName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "মসজিদের প্রধান অফিসিয়াল গোল সিলমোহর (Official Round Seal)",
            style = MaterialTheme.typography.bodySmall,
            color = MosqueTextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            color = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "📄 এটি কোথায় প্রদর্শিত হবে?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = MosqueEmeraldDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• সকল চাঁদা আদায় ও অনুদানের অফিসিয়াল রসিদের শীর্ষে\n• অফিসিয়াল নোটিশ সার্কুলার এবং জরুরি বিজ্ঞপ্তির ব্যানারে\n• বাৎসরিক ও মাসিক আর্থিক নিরীক্ষা অডিট বিবরণীতে\n• জুমার দানবাক্স গণনা প্রত্যয়ন ভাউচারের ওপর",
                    fontSize = 11.5.sp,
                    color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (sealUrl != null) {
                OutlinedButton(
                    onClick = onClearSeal,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MosqueUrgentRed),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueUrgentRed)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("সিল অপসারণ")
                }
            }

            Button(
                onClick = onPickSeal,
                modifier = Modifier.weight(1.5f).height(46.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (sealUrl != null) "নতুন সিল আপলোড" else "সিলমোহরের ছবি আপলোড")
            }
        }
    }
}

@Composable
private fun SignatureDrawingPadTab(
    officials: List<OfficialPersonUiModel>,
    preselectedId: String?,
    preselectedIsStaff: Boolean,
    isDark: Boolean,
    onSaveSignature: (OfficialPersonUiModel, Bitmap) -> Unit
) {
    var selectedPerson by remember(officials, preselectedId) {
        mutableStateOf(
            officials.firstOrNull { it.id == preselectedId && it.isStaff == preselectedIsStaff }
                ?: officials.firstOrNull()
        )
    }

    var lines by remember { mutableStateOf(listOf<List<Offset>>()) }
    var currentLine by remember { mutableStateOf(listOf<Offset>()) }

    var selectedColor by remember { mutableStateOf(Color(0xFF111111)) }
    var selectedStrokeWidth by remember { mutableFloatStateOf(6f) }

    val inkColors = listOf(
        Color(0xFF111111) to "কালো",
        Color(0xFF0F2E5C) to "গাঢ় নীল",
        Color(0xFF0D5C3A) to "সবুজ"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Person Selection Row
        Text(
            text = "কার ডিজিটাল স্বাক্ষর আঁকছেন?",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MosqueEmeraldDark
        )
        Spacer(modifier = Modifier.height(6.dp))

        var dropdownExpanded by remember { mutableStateOf(false) }
        Surface(
            onClick = { dropdownExpanded = true },
            shape = RoundedCornerShape(10.dp),
            color = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
            border = BorderStroke(1.dp, MosqueEmeraldPrimary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${selectedPerson?.nameBn ?: "নির্বাচন করুন"} (${selectedPerson?.roleTitleBn ?: ""})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MosqueEmeraldPrimary)
            }

            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                officials.forEach { person ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(person.nameBn, fontWeight = FontWeight.Bold)
                                Text(person.roleTitleBn, fontSize = 11.sp, color = MosqueEmeraldPrimary)
                            }
                        },
                        onClick = {
                            selectedPerson = person
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tools Row (Color + Thickness + Clear)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("কালি:", fontSize = 11.sp, color = MosqueTextMuted)
                inkColors.forEach { (color, _) ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selectedColor == color) 2.5.dp else 0.dp,
                                color = if (selectedColor == color) MosqueGold else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    lines = emptyList()
                    currentLine = emptyList()
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, MosqueUrgentRed.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Clear, contentDescription = null, tint = MosqueUrgentRed, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("মুছুন", fontSize = 11.sp, color = MosqueUrgentRed)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Drawing Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.5.dp, if (lines.isNotEmpty() || currentLine.isNotEmpty()) MosqueEmeraldPrimary else Color.LightGray, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(selectedColor, selectedStrokeWidth) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentLine = listOf(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentLine = currentLine + change.position
                            },
                            onDragEnd = {
                                if (currentLine.isNotEmpty()) {
                                    lines = lines + listOf(currentLine)
                                    currentLine = emptyList()
                                }
                            }
                        )
                    }
            ) {
                // Background watermarked guide line
                val canvasH = size.height
                val canvasW = size.width
                drawLine(
                    color = Color(0xFFDDDDDD),
                    start = Offset(30f, canvasH * 0.72f),
                    end = Offset(canvasW - 30f, canvasH * 0.72f),
                    strokeWidth = 2f
                )

                // Draw existing lines
                for (line in lines) {
                    if (line.size > 1) {
                        val path = Path().apply {
                            moveTo(line.first().x, line.first().y)
                            for (i in 1 until line.size) {
                                lineTo(line[i].x, line[i].y)
                            }
                        }
                        drawPath(path, color = selectedColor, style = Stroke(width = selectedStrokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    } else if (line.size == 1) {
                        drawCircle(color = selectedColor, radius = selectedStrokeWidth / 2, center = line.first())
                    }
                }

                // Draw currently dragging line
                if (currentLine.size > 1) {
                    val path = Path().apply {
                        moveTo(currentLine.first().x, currentLine.first().y)
                        for (i in 1 until currentLine.size) {
                            lineTo(currentLine[i].x, currentLine[i].y)
                        }
                    }
                    drawPath(path, color = selectedColor, style = Stroke(width = selectedStrokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
                } else if (currentLine.size == 1) {
                    drawCircle(color = selectedColor, radius = selectedStrokeWidth / 2, center = currentLine.first())
                }
            }

            if (lines.isEmpty() && currentLine.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✍️ এখানে আঙুল বা টাচপেন দিয়ে সই আঁকুন",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Save & Assign Button
        Button(
            onClick = {
                val target = selectedPerson
                if (target == null) {
                    MosqueToastController.error("অনুগ্রহ করে পদধারী সদস্য নির্বাচন করুন")
                    return@Button
                }
                if (lines.isEmpty()) {
                    MosqueToastController.error("অনুগ্রহ করে ক্যানভাসে সই আঁকুন")
                    return@Button
                }

                // Render lines into Android Bitmap
                val width = 600
                val height = 300
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = AndroidCanvas(bitmap)
                // Clear background to transparent
                canvas.drawColor(android.graphics.Color.TRANSPARENT)

                val paint = AndroidPaint().apply {
                    color = selectedColor.toArgb()
                    strokeWidth = 8f
                    isAntiAlias = true
                    style = AndroidPaint.Style.STROKE
                    strokeCap = AndroidPaint.Cap.ROUND
                    strokeJoin = AndroidPaint.Join.ROUND
                }

                for (line in lines) {
                    if (line.size > 1) {
                        val path = android.graphics.Path().apply {
                            moveTo(line.first().x, line.first().y)
                            for (i in 1 until line.size) {
                                lineTo(line[i].x, line[i].y)
                            }
                        }
                        canvas.drawPath(path, paint)
                    }
                }

                onSaveSignature(target, bitmap)
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
            enabled = selectedPerson != null && lines.isNotEmpty()
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${selectedPerson?.nameBn ?: "সদস্য"}-এর স্বাক্ষর হিসেবে সংরক্ষণ করুন",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
