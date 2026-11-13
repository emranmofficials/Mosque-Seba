package com.mosqueseba.app.ui.components

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mosqueseba.app.data.local.entity.ChandaPaymentEntity
import com.mosqueseba.app.data.local.entity.MeetingEntity
import com.mosqueseba.app.data.local.entity.MemberEntity
import com.mosqueseba.app.data.local.entity.NoticeEntity
import com.mosqueseba.app.data.local.entity.StaffEntity
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.AiNoticeUiState
import kotlinx.coroutines.launch

// 1. Add / Edit Member Dialog (Executive Islamic Design, Zero Emojis)
@Composable
fun AddMemberDialog(
    initialMember: MemberEntity? = null,
    initialStaff: StaffEntity? = null,
    initialRoleCode: String? = null,
    defaultMosqueAddress: String = "",
    isActiveStatus: Boolean = true,
    canManageAdministrativeControls: Boolean = false,
    canEditRoleAndSalary: Boolean = true,
    onToggleStatus: (() -> Unit)? = null,
    onDeleteMember: (() -> Unit)? = null,
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
        monthlyChanda: Double,
        photoUrl: String?,
        willingToProvideStaffMeals: Boolean,
        salaryAmount: Double,
        staffMealRequired: Boolean
    ) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isEditMode = initialMember != null || initialStaff != null
    val initialRole = initialMember?.let { UserRole.fromCode(it.roleCode) }
        ?: initialStaff?.let { UserRole.fromCode(it.roleCode) }
        ?: initialRoleCode?.let { UserRole.fromCode(it) }
        ?: UserRole.GENERAL_MEMBER
    val initialIsStaff = initialRole == UserRole.IMAM ||
        initialRole == UserRole.MUAZZIN ||
        initialRole == UserRole.KHADEM

    var nameBn by remember { mutableStateOf(initialMember?.nameBn ?: initialStaff?.nameBn ?: "") }
    var nameEn by remember { mutableStateOf(initialMember?.nameEn ?: "") }
    var mobile by remember { mutableStateOf(initialMember?.mobile ?: initialStaff?.mobile ?: "") }
    var email by remember { mutableStateOf(initialMember?.email ?: "") }
    var nid by remember { mutableStateOf(initialMember?.nid ?: "") }
    var address by remember {
        mutableStateOf(
            initialMember?.address?.takeIf { it.isNotBlank() }
                ?: if (!initialIsStaff) defaultMosqueAddress else ""
        )
    }
    var addressEditedByUser by remember {
        mutableStateOf(isEditMode && !initialMember?.address.isNullOrBlank())
    }
    var villageArea by remember { mutableStateOf(initialMember?.villageArea ?: "") }
    var selectedRole by remember { mutableStateOf(initialRole) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }
    var monthlyChanda by remember {
        mutableStateOf(
            if (initialMember != null && initialMember.monthlyChanda > 0) {
                initialMember.monthlyChanda.toInt().toString()
            } else ""
        )
    }
    var salaryAmount by remember {
        mutableStateOf(
            if (initialStaff != null && initialStaff.salaryAmount > 0) {
                initialStaff.salaryAmount.toInt().toString()
            } else "12000"
        )
    }
    var willingToProvideStaffMeals by remember {
        mutableStateOf(initialMember?.willingToProvideStaffMeals ?: false)
    }
    var staffMealRequiredChoice by remember {
        mutableStateOf<Boolean?>(initialStaff?.mealRequired)
    }
    var photoPath by remember { mutableStateOf(initialMember?.photoUrl) }
    var photoPreview by remember(initialMember?.photoUrl) {
        mutableStateOf<ImageBitmap?>(loadLocalThumbnail(initialMember?.photoUrl, 256))
    }

    var nameError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }
    var salaryError by remember { mutableStateOf<String?>(null) }
    var staffMealError by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val (savedPath, bitmap) = savePickedImageToAppStorage(context, uri, "member")
                if (savedPath != null) {
                    photoPath = savedPath
                    photoPreview = bitmap
                }
            }
        }
    }

    val isStaffRole = selectedRole == UserRole.IMAM ||
        selectedRole == UserRole.MUAZZIN ||
        selectedRole == UserRole.KHADEM

    LaunchedEffect(defaultMosqueAddress, isStaffRole) {
        if (!addressEditedByUser && !isStaffRole && address.isBlank() && defaultMosqueAddress.isNotBlank()) {
            address = defaultMosqueAddress
        }
    }

    val roleIcon = when {
        isStaffRole -> Icons.Default.Mosque
        selectedRole.level <= 2 -> Icons.Default.VerifiedUser
        else -> Icons.Default.Person
    }

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
                // Executive Emerald Header
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
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        tint = MosqueGoldLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isEditMode) "সদস্য তথ্য হালনাগাদ" else "নতুন সদস্য যুক্ত ফর্ম",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isEditMode) {
                                        val codeText = (initialMember?.memberCode ?: initialStaff?.id?.uppercase())?.let { "আইডি: ${toBengaliDigits(it)} • " } ?: ""
                                        "${codeText}পদবি পরিবর্তন করলেও আইডি অপরিবর্তিত থাকবে"
                                    } else {
                                        "মসজিদ কমিটি ও খেদমত তালিকায় যুক্ত করুন"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueGoldLight,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = Color.White)
                        }
                    }
                }

                // Scrollable Form Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Section 1: Photo & Basic Info
                    MemberFormSectionCard(
                        title = "সদস্যের ছবি ও মৌলিক তথ্য",
                        icon = Icons.Default.Badge
                    ) {
                        // Photo Picker Surface
                        Surface(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = MosqueBgLight,
                            border = BorderStroke(1.dp, MosqueCardBorderGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(MosqueEmeraldSoft)
                                        .border(1.5.dp, MosqueGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val preview = photoPreview
                                    if (preview != null) {
                                        Image(
                                            bitmap = preview,
                                            contentDescription = "সদস্যের ছবি",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.AddAPhoto,
                                            contentDescription = "ছবি আপলোড",
                                            tint = MosqueEmeraldPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (photoPreview != null) "প্রোফাইল ছবি সংযুক্ত হয়েছে" else "প্রোফাইল ছবি আপলোড (ঐচ্ছিক)",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueEmeraldDark
                                    )
                                    Text(
                                        text = if (photoPreview != null) "পরিবর্তন করতে ট্যাপ করুন" else "গ্যালারি থেকে ছবি নির্বাচন করুন",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MosqueTextSecondary
                                    )
                                }
                                if (photoPath != null) {
                                    IconButton(
                                        onClick = {
                                            photoPath = null
                                            photoPreview = null
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "ছবি মুছুন",
                                            tint = MosqueUrgentRed
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = nameBn,
                            onValueChange = { nameBn = it; nameError = null },
                            label = { Text("পূর্ণ নাম (বাংলায়) *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MosqueEmeraldPrimary) },
                            isError = nameError != null,
                            supportingText = nameError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = nameEn,
                            onValueChange = { nameEn = it },
                            label = { Text("পূর্ণ নাম (English - ঐচ্ছিক)") },
                            leadingIcon = { Icon(Icons.Default.Translate, contentDescription = null, tint = MosqueTextMuted) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it; mobileError = null },
                            label = { Text("মোবাইল নম্বর (লগইন আইডি) *") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MosqueEmeraldPrimary) },
                            isError = mobileError != null,
                            supportingText = {
                                Text(
                                    text = mobileError ?: "লগইন পাসওয়ার্ড স্বয়ংক্রিয়ভাবে নম্বরের শেষ ৬টি সংখ্যা হবে",
                                    fontSize = 11.sp,
                                    color = if (mobileError != null) MosqueUrgentRed else MosqueTextMuted
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Section 2: Role, Finance & Meal Confirmation
                    MemberFormSectionCard(
                        title = "পদবি, আর্থিক ও খাবার সংক্রান্ত তথ্য",
                        icon = Icons.Default.AccountBalanceWallet
                    ) {
                        Text(
                            text = "কমিটি বা খেদমত পদবি নির্বাচন *",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MosqueEmeraldDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                onClick = { if (canEditRoleAndSalary) roleDropdownExpanded = true },
                                enabled = canEditRoleAndSalary,
                                shape = RoundedCornerShape(12.dp),
                                color = if (canEditRoleAndSalary) MosqueEmeraldSoft.copy(alpha = 0.65f) else MosqueBgLight,
                                border = BorderStroke(1.dp, MosqueEmeraldPrimary.copy(alpha = if (canEditRoleAndSalary) 0.55f else 0.25f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 54.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MosqueEmeraldPrimary,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = roleIcon,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = selectedRole.titleBn,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MosqueEmeraldDark
                                            )
                                            Text(
                                                text = if (!canEditRoleAndSalary) {
                                                    "পদবি পরিবর্তনের ক্ষমতা কেবল সভাপতি/সম্পাদকের"
                                                } else if (isStaffRole) {
                                                    "বেতনভুক্ত সদস্য তালিকা"
                                                } else {
                                                    "মাসিক চাঁদা দাতা তালিকা"
                                                },
                                                fontSize = 10.5.sp,
                                                color = MosqueTextSecondary
                                            )
                                        }
                                    }
                                    if (canEditRoleAndSalary) {
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = "পদবি নির্বাচন",
                                            tint = MosqueEmeraldPrimary
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = "সংরক্ষিত পদবি",
                                            tint = MosqueTextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            DropdownMenu(
                                expanded = roleDropdownExpanded,
                                onDismissRequest = { roleDropdownExpanded = false },
                                modifier = Modifier
                                    .widthIn(min = 270.dp)
                                    .heightIn(max = 320.dp)
                            ) {
                                UserRole.entries.forEach { role ->
                                    val itemIsStaff = role == UserRole.IMAM ||
                                        role == UserRole.MUAZZIN ||
                                        role == UserRole.KHADEM
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (itemIsStaff) Icons.Default.Mosque else if (role.level <= 2) Icons.Default.VerifiedUser else Icons.Default.Person,
                                                contentDescription = null,
                                                tint = if (itemIsStaff) MosqueGoldDeep else MosqueEmeraldPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        text = {
                                            Column {
                                                Text(
                                                    text = role.titleBn,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (selectedRole == role) FontWeight.Bold else FontWeight.Medium,
                                                    color = MosqueEmeraldDark
                                                )
                                                Text(
                                                    text = if (itemIsStaff) "বেতনভুক্ত সদস্য" else "মাসিক চাঁদা দাতা",
                                                    fontSize = 10.sp,
                                                    color = MosqueTextMuted
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedRole = role
                                            if (!isEditMode && !addressEditedByUser) {
                                                address = if (itemIsStaff) "" else defaultMosqueAddress
                                            }
                                            staffMealError = null
                                            salaryError = null
                                            roleDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isStaffRole) {
                            OutlinedTextField(
                                value = salaryAmount,
                                onValueChange = {
                                    if (canEditRoleAndSalary) {
                                        salaryAmount = it.filter { ch -> ch.isDigit() }
                                        salaryError = null
                                    }
                                },
                                enabled = canEditRoleAndSalary,
                                label = { Text("মাসিক নির্ধারিত বেতন (৳) *") },
                                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = MosqueEmeraldPrimary) },
                                isError = salaryError != null,
                                supportingText = salaryError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Mandatory Staff Meal Confirmation Card
                            Surface(
                                color = if (staffMealError != null) MosqueUrgentRed.copy(alpha = 0.06f) else MosqueGoldSoft.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (staffMealError != null) MosqueUrgentRed else MosqueCardBorderGold
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Restaurant,
                                            contentDescription = null,
                                            tint = MosqueEmeraldDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "বেতনভুক্ত সদস্য হিসেবে খাবার নিতে চান কি না? *",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueEmeraldDark
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "মসজিদ বা সদস্যদের পক্ষ থেকে দৈনিক খাবারের (জায়গির) ব্যবস্থা নেবেন কি না তা নিশ্চিত করুন।",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MosqueTextSecondary,
                                        lineHeight = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        MealOptionButton(
                                            modifier = Modifier.weight(1f),
                                            selected = staffMealRequiredChoice == true,
                                            label = "হ্যাঁ, খাবার নেবেন",
                                            icon = Icons.Default.CheckCircle,
                                            selectedColor = MosqueEmeraldPrimary,
                                            onClick = {
                                                staffMealRequiredChoice = true
                                                staffMealError = null
                                            }
                                        )
                                        MealOptionButton(
                                            modifier = Modifier.weight(1f),
                                            selected = staffMealRequiredChoice == false,
                                            label = "না, নেবেন না",
                                            icon = Icons.Default.Cancel,
                                            selectedColor = MosqueTextSecondary,
                                            onClick = {
                                                staffMealRequiredChoice = false
                                                staffMealError = null
                                            }
                                        )
                                    }
                                    staffMealError?.let { err ->
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = err,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MosqueUrgentRed,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = monthlyChanda,
                                onValueChange = { monthlyChanda = it.filter { ch -> ch.isDigit() } },
                                label = { Text("মাসিক চাঁদার পরিমাণ (৳) — ঐচ্ছিক") },
                                placeholder = { Text("যেমন: ৫০০ (ফাঁকা রাখলে ০৳)") },
                                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = MosqueEmeraldPrimary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Surface(
                                color = MosqueGoldSoft.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MosqueCardBorderGold),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Restaurant,
                                            contentDescription = null,
                                            tint = MosqueEmeraldDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "বেতনভুক্ত স্টাফদের খাবার (জায়গির) প্রদান:",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueEmeraldDark
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "ইমাম, মুয়াজ্জিন ও খাদেমদের পালাক্রমে খাবার দিতে রাজি আছেন কি?",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MosqueTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        MealOptionButton(
                                            modifier = Modifier.weight(1f),
                                            selected = willingToProvideStaffMeals,
                                            label = "হ্যাঁ, রাজি আছেন",
                                            icon = Icons.Default.CheckCircle,
                                            selectedColor = MosqueEmeraldPrimary,
                                            onClick = { willingToProvideStaffMeals = true }
                                        )
                                        MealOptionButton(
                                            modifier = Modifier.weight(1f),
                                            selected = !willingToProvideStaffMeals,
                                            label = "না",
                                            icon = Icons.Default.Cancel,
                                            selectedColor = MosqueTextSecondary,
                                            onClick = { willingToProvideStaffMeals = false }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section 3: Contact & Address (Optional)
                    MemberFormSectionCard(
                        title = "যোগাযোগ ও ঠিকানা (ঐচ্ছিক)",
                        icon = Icons.Default.LocationOn
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("ইমেইল ঠিকানা") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MosqueTextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = nid,
                            onValueChange = { nid = it },
                            label = { Text("জাতীয় পরিচয়পত্র নম্বর (NID)") },
                            leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = MosqueTextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = address,
                                onValueChange = {
                                    address = it
                                    addressEditedByUser = true
                                },
                                label = { Text("বাসার ঠিকানা / লেন") },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier.weight(1.3f)
                            )
                            OutlinedTextField(
                                value = villageArea,
                                onValueChange = { villageArea = it },
                                label = { Text("এলাকা / মহল্লা") },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (!isStaffRole && defaultMosqueAddress.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "মসজিদের ঠিকানা ডিফল্ট হিসেবে ব্যবহৃত হবে (চাইলে পরিবর্তন করতে পারবেন)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MosqueEmeraldPrimary,
                                fontSize = 10.5.sp
                            )
                        } else if (isStaffRole) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "বেতনভুক্ত সদস্যের নিজস্ব/স্থায়ী ঠিকানা উল্লেখ করুন",
                                style = MaterialTheme.typography.labelSmall,
                                color = MosqueTextMuted,
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    // Section 4: Administrative Controls (Edit Mode Only, Zero Emojis)
                    if (isEditMode && canManageAdministrativeControls && (onToggleStatus != null || onDeleteMember != null)) {
                        MemberFormSectionCard(
                            title = "প্রশাসনিক নিয়ন্ত্রণ",
                            icon = Icons.Default.AdminPanelSettings
                        ) {
                            Text(
                                text = "সদস্যের স্থায়ী আইডি (${toBengaliDigits(initialMember?.memberCode ?: initialStaff?.id?.uppercase() ?: "MEM-NEW")}) অপরিবর্তিত রেখে পদবি, সক্রিয় অবস্থা বা তালিকাভুক্তি নিয়ন্ত্রণ করুন।",
                                style = MaterialTheme.typography.labelSmall,
                                color = MosqueTextSecondary,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            if (onToggleStatus != null) {
                                OutlinedButton(
                                    onClick = {
                                        onToggleStatus()
                                        onDismiss()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isActiveStatus) MosqueWarningAmber else MosqueSuccessGreen
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (isActiveStatus) MosqueWarningAmber else MosqueSuccessGreen
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isActiveStatus) Icons.Default.PauseCircle else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isActiveStatus) "সদস্য নিষ্ক্রিয় করুন" else "সদস্য পুনরায় সক্রিয় করুন",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (onDeleteMember != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { showDeleteConfirm = true },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MosqueUrgentRed.copy(alpha = 0.7f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueUrgentRed),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "সদস্য তালিকা থেকে অপসারণ করুন",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Sticky Footer Action Bar
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
                        onClick = {
                            val digitCount = mobile.filter { it.isDigit() }.length
                            val parsedSalary = salaryAmount.toDoubleOrNull() ?: 0.0
                            when {
                                nameBn.isBlank() -> {
                                    nameError = "অনুগ্রহ করে সদস্যের বাংলা নাম লিখুন"
                                }
                                digitCount < 6 -> {
                                    mobileError = "লগইন পাসওয়ার্ডের জন্য কমপক্ষে ৬ ডিজিটের মোবাইল নম্বর দিন"
                                }
                                isStaffRole && parsedSalary <= 0.0 -> {
                                    salaryError = "বেতনভুক্ত সদস্যের মাসিক বেতন লিখুন"
                                }
                                isStaffRole && staffMealRequiredChoice == null -> {
                                    staffMealError = "বেতনভুক্ত সদস্য খাবার নিতে চান কি না তা নিশ্চিত করুন"
                                }
                                else -> {
                                    val resolvedAddress = address.trim().ifBlank {
                                        if (!isStaffRole) defaultMosqueAddress.trim() else ""
                                    }
                                    val cleanArea = villageArea.trim().ifBlank {
                                        resolvedAddress.substringBefore(",").ifBlank { "মসজিদ এলাকা" }
                                    }
                                    onConfirm(
                                        nameBn.trim(),
                                        nameEn.trim(),
                                        mobile.trim(),
                                        email.trim(),
                                        nid.trim(),
                                        resolvedAddress.ifBlank { cleanArea },
                                        cleanArea,
                                        selectedRole.code,
                                        if (isStaffRole) 0.0 else (monthlyChanda.toDoubleOrNull() ?: 0.0),
                                        photoPath,
                                        if (isStaffRole) false else willingToProvideStaffMeals,
                                        if (isStaffRole) parsedSalary else 0.0,
                                        if (isStaffRole) (staffMealRequiredChoice ?: false) else true
                                    )
                                    onDismiss()
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        modifier = Modifier
                            .weight(1.4f)
                            .heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEditMode) "হালনাগাদ করুন" else "সংরক্ষণ করুন",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && onDeleteMember != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("সদস্য অপসারণ নিশ্চিত করুন", fontWeight = FontWeight.Bold) },
            text = { Text("আপনি কি নিশ্চিতভাবে \"${nameBn.ifBlank { initialMember?.nameBn ?: initialStaff?.nameBn ?: "এই সদস্য" }}\"-কে সদস্য তালিকা থেকে অপসারণ করতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteMember()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueUrgentRed)
                ) {
                    Text("হ্যাঁ, অপসারণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun MemberFormSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
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
private fun MealOptionButton(
    modifier: Modifier = Modifier,
    selected: Boolean,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) selectedColor else Color.White,
        border = BorderStroke(1.dp, if (selected) selectedColor else MosqueCardBorderGold),
        modifier = modifier.heightIn(min = 44.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else MosqueTextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else MosqueTextPrimary,
                maxLines = 1
            )
        }
    }
}

// 2. Add Transaction Dialog (Income or Expense — Gold-Emerald Executive Design)
@Composable
fun AddTransactionDialog(
    initialType: String = "INCOME",
    initialCategory: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (category: String, type: String, amount: Double, description: String, method: String) -> Unit
) {
    var type by remember { mutableStateOf(initialType) }
    val incomeCategories = remember {
        listOf(
            "জুমার দান বাক্স",
            "মাসিক সদস্য চাঁদা",
            "মসজিদ সংস্কার অনুদান",
            "হিফজখানা অনুদান",
            "সাধারণ দান"
        )
    }
    val expenseCategories = remember {
        listOf(
            "ইমাম ও কর্মচারীদের বেতন",
            "বিদ্যুৎ ও ইউটিলিটি বিল",
            "মসজিদ রক্ষণাবেক্ষণ",
            "জেনারেটর জ্বালানি",
            "আপ্যায়ন ও বিবিধ"
        )
    }

    var selectedCategory by remember(initialType, initialCategory) {
        mutableStateOf(
            initialCategory?.takeIf { it.isNotBlank() }
                ?: if (initialType == "INCOME") incomeCategories.first() else expenseCategories.first()
        )
    }
    var useCustomCategory by remember { mutableStateOf(false) }
    var customCategory by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("cash") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }

    val isIncome = type == "INCOME"
    val accentColor = if (isIncome) MosqueEmeraldPrimary else MosqueUrgentRed
    val enteredAmount = amountText.toDoubleOrNull() ?: 0.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MehrabArchCard(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 560.dp)
                .heightIn(max = 740.dp),
            backgroundColor = Color.White,
            borderColor = MosqueCardBorderGold
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Sticky Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MosqueEmeraldDarkGradient)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isIncome) "নতুন আয় অন্তর্ভুক্ত করুন" else "নতুন ব্যয় লিপিবদ্ধ করুন",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = if (isIncome) "ফান্ড জমা ও ভাউচার স্বয়ংক্রিয়ভাবে আপডেট হবে" else "খরচের ভাউচার ও অনুমোদন ট্র্যাকিং যুক্ত হবে",
                                style = MaterialTheme.typography.bodySmall,
                                color = MosqueGoldLight,
                                fontSize = 11.sp
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("txn_dialog_close")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = Color.White)
                        }
                    }
                }

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Type Switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MosqueBgLight)
                            .border(1.dp, MosqueCardBorderSubtle, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                type = "INCOME"
                                selectedCategory = incomeCategories.first()
                                useCustomCategory = false
                                categoryError = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("txn_type_income"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isIncome) MosqueSuccessGreen else Color.Transparent,
                                contentColor = if (isIncome) Color.White else MosqueTextSecondary
                            ),
                            elevation = null
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("আয় (জমা)", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                type = "EXPENSE"
                                selectedCategory = expenseCategories.first()
                                useCustomCategory = false
                                categoryError = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("txn_type_expense"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isIncome) MosqueUrgentRed else Color.Transparent,
                                contentColor = if (!isIncome) Color.White else MosqueTextSecondary
                            ),
                            elevation = null
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ব্যয় (খরচ)", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Categories (All 5 + Custom)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "খাত নির্বাচন করুন",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MosqueEmeraldDark
                        )
                        val currentCats = (if (isIncome) incomeCategories else expenseCategories) + "অন্যান্য (কাস্টম)"
                        currentCats.chunked(2).forEach { rowCats ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowCats.forEach { cat ->
                                    val isCustomChip = cat == "অন্যান্য (কাস্টম)"
                                    val isChosen = if (isCustomChip) useCustomCategory else (!useCustomCategory && selectedCategory == cat)
                                    Surface(
                                        onClick = {
                                            if (isCustomChip) {
                                                useCustomCategory = true
                                            } else {
                                                useCustomCategory = false
                                                selectedCategory = cat
                                            }
                                            categoryError = null
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isChosen) {
                                            if (isIncome) MosqueEmeraldPrimary else MosqueUrgentRed
                                        } else {
                                            MosqueBgLight
                                        },
                                        border = BorderStroke(
                                            width = if (isChosen) 1.5.dp else 1.dp,
                                            color = if (isChosen) {
                                                if (isIncome) MosqueEmeraldDark else MosqueUrgentRed
                                            } else {
                                                MosqueCardBorderGold
                                            }
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(
                                                text = cat,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isChosen) Color.White else MosqueEmeraldDark,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                                if (rowCats.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        if (useCustomCategory) {
                            OutlinedTextField(
                                value = customCategory,
                                onValueChange = {
                                    customCategory = it
                                    categoryError = null
                                },
                                label = { Text("নিজস্ব খাতের নাম লিখুন *") },
                                placeholder = { Text("যেমন: ইফতার মাহফিল বা মসজিদ রঙ করা") },
                                isError = categoryError != null,
                                supportingText = categoryError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Amount Field
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it.filter { ch -> ch.isDigit() }
                            amountError = null
                        },
                        label = { Text("টাকার পরিমাণ (৳) *") },
                        prefix = { Text("৳ ", fontWeight = FontWeight.Bold, color = MosqueEmeraldDark) },
                        isError = amountError != null,
                        supportingText = amountError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("txn_amount_input")
                    )

                    // Payment Channel Cards
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isIncome) "জমার মাধ্যম" else "পরিশোধের মাধ্যম",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MosqueEmeraldDark
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ChandaMethodOptionCard(
                                label = "নগদ",
                                subLabel = "Cash",
                                icon = Icons.Default.Payments,
                                selected = method == "cash",
                                onClick = { method = "cash" },
                                modifier = Modifier.weight(1f)
                            )
                            ChandaMethodOptionCard(
                                label = "বিকাশ/নগদ",
                                subLabel = "Mobile",
                                icon = Icons.Default.PhoneAndroid,
                                selected = method == "mobile_banking",
                                onClick = { method = "mobile_banking" },
                                modifier = Modifier.weight(1f)
                            )
                            ChandaMethodOptionCard(
                                label = "ব্যাংক/অনলাইন",
                                subLabel = "Bank",
                                icon = Icons.Default.AccountBalance,
                                selected = method == "online",
                                onClick = { method = "online" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("বিবরণ / ভাউচার নোট") },
                        placeholder = { Text("উদ্দেশ্য বা দাতা/গ্রহীতার বিবরণ লিখুন") },
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("txn_description_input")
                    )
                }

                // Sticky Footer
                HorizontalDivider(color = MosqueCardBorderSubtle)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MosqueBgLight.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(0.9f)
                            .heightIn(min = 48.dp)
                    ) {
                        Text("বাতিল", color = MosqueTextSecondary, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            val finalCategory = if (useCustomCategory) customCategory.trim() else selectedCategory
                            when {
                                useCustomCategory && finalCategory.isBlank() -> {
                                    categoryError = "খাতের নাম লিখুন"
                                }
                                amt <= 0.0 -> {
                                    amountError = "সঠিক টাকার পরিমাণ লিখুন"
                                }
                                else -> {
                                    onConfirm(finalCategory, type, amt, description.trim(), method)
                                    onDismiss()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        modifier = Modifier
                            .weight(1.6f)
                            .heightIn(min = 48.dp)
                            .testTag("txn_confirm_button")
                    ) {
                        Icon(
                            imageVector = if (isIncome) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val btnText = if (enteredAmount > 0.0) {
                            "৳${toBengaliDigits(enteredAmount.toInt().toString())} ${if (isIncome) "আয় দাখিল" else "ব্যয় দাখিল"}"
                        } else {
                            if (isIncome) "আয় দাখিল করুন" else "ব্যয় দাখিল করুন"
                        }
                        Text(btnText, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

// 3. Receive Chanda Dialog (Multi-Month Due & Advance Collection)
@Composable
fun ReceiveChandaDialog(
    members: List<MemberEntity>,
    allPayments: List<ChandaPaymentEntity> = emptyList(),
    preselectedMember: MemberEntity? = null,
    defaultMonth: String = "সেপ্টেম্বর ২০২৪",
    suggestedAmount: Double? = null,
    onDismiss: () -> Unit,
    onConfirm: (member: MemberEntity, month: String, amount: Double, method: String, selectedMonthCount: Int) -> Unit
) {
    val nonStaffMembers = remember(members) {
        members.filter {
            !it.roleCode.equals("imam", ignoreCase = true) &&
                !it.roleCode.equals("muazzin", ignoreCase = true) &&
                !it.roleCode.equals("khadem", ignoreCase = true)
        }.ifEmpty { members }
    }

    val anchorMonth = remember(defaultMonth) {
        ChandaMonthUtils.resolveLatestSingleMonth(listOf(defaultMonth), "সেপ্টেম্বর ২০২৪")
    }
    val billingMonths = remember(anchorMonth) {
        ChandaMonthUtils.getBillingMonthsWindow(
            anchorLabel = anchorMonth,
            pastMonthsCount = 11,
            advanceMonthsCount = 3
        )
    }
    val pastAndCurrentMonths = remember(billingMonths) {
        billingMonths.filter { !it.isAdvance }
    }
    val advanceMonths = remember(billingMonths) {
        billingMonths.filter { it.isAdvance }
    }

    var selectedMember by remember(preselectedMember, nonStaffMembers) {
        mutableStateOf(preselectedMember ?: nonStaffMembers.firstOrNull())
    }
    var memberSearch by remember { mutableStateOf("") }

    val memberPayments = remember(selectedMember?.id, allPayments) {
        val mId = selectedMember?.id ?: return@remember emptyList()
        allPayments.filter { it.memberId == mId }
    }
    val paidMonthLabels = remember(memberPayments, billingMonths) {
        memberPayments.flatMap { payment ->
            ChandaMonthUtils.extractCoveredMonths(payment.month, billingMonths)
        }.toSet()
    }
    val memberTotalDeposited = remember(memberPayments) {
        memberPayments.sumOf { it.amountPaid }
    }
    val unpaidPastAndCurrent = remember(pastAndCurrentMonths, paidMonthLabels) {
        pastAndCurrentMonths.filter { it.label !in paidMonthLabels }
    }
    val unpaidAdvance = remember(advanceMonths, paidMonthLabels) {
        advanceMonths.filter { it.label !in paidMonthLabels }
    }

    fun initialSelectionFor(member: MemberEntity?): Set<String> {
        if (member == null) return setOf(anchorMonth)
        val mPaid = allPayments
            .filter { it.memberId == member.id }
            .flatMap { ChandaMonthUtils.extractCoveredMonths(it.month, billingMonths) }
            .toSet()
        val mUnpaidRegular = pastAndCurrentMonths.filter { it.label !in mPaid }
        val mUnpaidAdvance = advanceMonths.filter { it.label !in mPaid }
        return when {
            anchorMonth !in mPaid -> setOf(anchorMonth)
            mUnpaidRegular.isNotEmpty() -> setOf(mUnpaidRegular.last().label)
            mUnpaidAdvance.isNotEmpty() -> setOf(mUnpaidAdvance.first().label)
            else -> emptySet()
        }
    }

    var selectedMonthLabels by remember(selectedMember?.id, anchorMonth) {
        mutableStateOf(initialSelectionFor(selectedMember))
    }
    var useCustomMonthText by remember { mutableStateOf(false) }
    var customMonthText by remember(anchorMonth) { mutableStateOf(anchorMonth) }
    var manualAmountOverride by remember { mutableStateOf(false) }

    val monthlyRate = selectedMember?.monthlyChanda ?: 0.0
    val selectedMonthCount = if (useCustomMonthText) 1 else selectedMonthLabels.size
    val calculatedTotal = remember(monthlyRate, selectedMonthLabels, useCustomMonthText) {
        if (useCustomMonthText) monthlyRate else monthlyRate * selectedMonthLabels.size
    }

    var amountText by remember(selectedMember?.id, preselectedMember?.id, suggestedAmount) {
        val initAmt = when {
            preselectedMember != null &&
                selectedMember?.id == preselectedMember.id &&
                suggestedAmount != null &&
                suggestedAmount > 0.0 -> suggestedAmount
            else -> calculatedTotal
        }
        mutableStateOf(if (initAmt > 0.0) initAmt.toInt().toString() else "")
    }
    var method by remember { mutableStateOf("cash") }
    var formError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    fun applyMonthSelection(newSelection: Set<String>) {
        useCustomMonthText = false
        selectedMonthLabels = newSelection
        formError = null
        if (monthlyRate > 0.0) {
            manualAmountOverride = false
            val newTotal = (monthlyRate * newSelection.size).toInt()
            amountText = if (newTotal > 0) newTotal.toString() else ""
            amountError = null
        }
    }

    val formattedMonthsSummary = remember(selectedMonthLabels, billingMonths, useCustomMonthText, customMonthText) {
        if (useCustomMonthText) {
            customMonthText.trim()
        } else {
            ChandaMonthUtils.formatMonthsForStorage(selectedMonthLabels, billingMonths)
        }
    }

    val enteredAmount = amountText.toDoubleOrNull() ?: 0.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MehrabArchCard(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 560.dp)
                .heightIn(max = 760.dp),
            backgroundColor = Color.White,
            borderColor = MosqueCardBorderGold
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Sticky Emerald Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MosqueEmeraldDarkGradient)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "সদস্য চাঁদা গ্রহণ ও রসিদ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "এক সাথে ১, ৩, ৬ বা ৯ মাসের বকেয়া ও অগ্রিম আদায়",
                                style = MaterialTheme.typography.bodySmall,
                                color = MosqueGoldLight,
                                fontSize = 11.sp
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("chanda_dialog_close")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = Color.White)
                        }
                    }
                }

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Member Info (+ Inline Searchable Chips only when opened without a preselected member)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (preselectedMember != null) "সদস্যের তথ্য" else "সদস্য বাছাই করুন",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MosqueEmeraldDark
                        )

                        if (preselectedMember == null) {
                            val filteredPickerMembers = remember(nonStaffMembers, memberSearch) {
                                nonStaffMembers.filter {
                                    memberSearch.isBlank() ||
                                        it.nameBn.contains(memberSearch, ignoreCase = true) ||
                                        it.memberCode.contains(memberSearch, ignoreCase = true) ||
                                        it.mobile.contains(memberSearch)
                                }
                            }
                            OutlinedTextField(
                                value = memberSearch,
                                onValueChange = { memberSearch = it },
                                placeholder = { Text("নাম, কোড বা মোবাইল দিয়ে খুঁজুন...", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (memberSearch.isNotBlank()) {
                                        IconButton(onClick = { memberSearch = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "মুছুন", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("chanda_member_search")
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filteredPickerMembers.forEach { m ->
                                    val isChosen = selectedMember?.id == m.id
                                    Surface(
                                        onClick = {
                                            selectedMember = m
                                            val nextSelection = initialSelectionFor(m)
                                            selectedMonthLabels = nextSelection
                                            useCustomMonthText = false
                                            manualAmountOverride = false
                                            val nextAmt = (m.monthlyChanda * nextSelection.size).toInt()
                                            amountText = if (nextAmt > 0) nextAmt.toString() else ""
                                            formError = null
                                            amountError = null
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isChosen) MosqueEmeraldPrimary else MosqueBgLight,
                                        border = BorderStroke(
                                            width = if (isChosen) 1.5.dp else 1.dp,
                                            color = if (isChosen) MosqueEmeraldDark else MosqueCardBorderGold
                                        ),
                                        modifier = Modifier
                                            .widthIn(min = 148.dp, max = 210.dp)
                                            .heightIn(min = 52.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = m.nameBn,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isChosen) Color.White else MosqueEmeraldDark,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${toBengaliDigits(m.memberCode)} • ৳${toBengaliDigits(m.monthlyChanda.toInt().toString())}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                color = if (isChosen) MosqueGoldLight else MosqueTextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MosqueBgLight,
                                border = BorderStroke(1.dp, MosqueCardBorderGold),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("chanda_member_summary")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(MosqueEmeraldSoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MosqueEmeraldPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = selectedMember?.nameBn ?: "",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueEmeraldDark,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        selectedMember?.let { m ->
                                            val r = UserRole.fromCode(m.roleCode)
                                            Text(
                                                text = "${r.titleBn} • ${toBengaliDigits(m.memberCode)} • ${toBengaliDigits(m.mobile)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MosqueTextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3-Stat Member Financial Snapshot
                        selectedMember?.let { m ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ChandaMiniStat(
                                    label = "মাসিক চাঁদা",
                                    value = if (m.monthlyChanda > 0) "৳${toBengaliDigits(String.format("%,.0f", m.monthlyChanda))}" else "নির্ধারিত নয়",
                                    containerColor = MosqueEmeraldSoft,
                                    valueColor = MosqueEmeraldDark,
                                    modifier = Modifier.weight(1f)
                                )
                                ChandaMiniStat(
                                    label = "মোট জমা",
                                    value = "৳${toBengaliDigits(String.format("%,.0f", memberTotalDeposited))}",
                                    containerColor = MosqueGoldLight.copy(alpha = 0.55f),
                                    valueColor = MosqueEmeraldDark,
                                    modifier = Modifier.weight(1f)
                                )
                                val unpaidCount = unpaidPastAndCurrent.size
                                ChandaMiniStat(
                                    label = "বকেয়া মাস (১২ মাসে)",
                                    value = if (unpaidCount > 0) "${toBengaliDigits(unpaidCount.toString())} মাস" else "বকেয়া নেই",
                                    containerColor = if (unpaidCount > 0) MosqueUrgentRed.copy(alpha = 0.08f) else MosqueSuccessGreen.copy(alpha = 0.1f),
                                    valueColor = if (unpaidCount > 0) MosqueUrgentRed else MosqueSuccessGreen,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MosqueCardBorderSubtle)

                    // 2. Quick Month Selection Presets (1, 3, 6, 9, All Due, +Advance, Clear)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "দ্রুত বকেয়া ও অগ্রিম নির্বাচন",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldDark
                            )
                            Text(
                                text = "${toBengaliDigits(selectedMonthCount.toString())} মাস নির্বাচিত",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MosqueEmeraldPrimary
                            )
                        }

                        val currentPreset = remember(anchorMonth, paidMonthLabels, unpaidPastAndCurrent) {
                            if (anchorMonth !in paidMonthLabels) setOf(anchorMonth)
                            else unpaidPastAndCurrent.takeLast(1).map { it.label }.toSet()
                        }
                        val threeDuePreset = remember(unpaidPastAndCurrent) {
                            unpaidPastAndCurrent.takeLast(3).map { it.label }.toSet()
                        }
                        val sixDuePreset = remember(unpaidPastAndCurrent) {
                            unpaidPastAndCurrent.takeLast(6).map { it.label }.toSet()
                        }
                        val nineDuePreset = remember(unpaidPastAndCurrent) {
                            unpaidPastAndCurrent.takeLast(9).map { it.label }.toSet()
                        }
                        val allDuePreset = remember(unpaidPastAndCurrent) {
                            unpaidPastAndCurrent.map { it.label }.toSet()
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (currentPreset.isNotEmpty()) {
                                QuickMonthPresetChip(
                                    label = "চলতি মাস",
                                    selected = !useCustomMonthText && selectedMonthLabels == currentPreset,
                                    onClick = { applyMonthSelection(currentPreset) }
                                )
                            }
                            if (unpaidPastAndCurrent.size >= 3) {
                                QuickMonthPresetChip(
                                    label = "৩ মাস বকেয়া",
                                    selected = !useCustomMonthText && selectedMonthLabels == threeDuePreset,
                                    onClick = { applyMonthSelection(threeDuePreset) }
                                )
                            }
                            if (unpaidPastAndCurrent.size >= 6) {
                                QuickMonthPresetChip(
                                    label = "৬ মাস বকেয়া",
                                    selected = !useCustomMonthText && selectedMonthLabels == sixDuePreset,
                                    onClick = { applyMonthSelection(sixDuePreset) }
                                )
                            }
                            if (unpaidPastAndCurrent.size >= 9) {
                                QuickMonthPresetChip(
                                    label = "৯ মাস বকেয়া",
                                    selected = !useCustomMonthText && selectedMonthLabels == nineDuePreset,
                                    onClick = { applyMonthSelection(nineDuePreset) }
                                )
                            }
                            if (allDuePreset.isNotEmpty()) {
                                QuickMonthPresetChip(
                                    label = "সব বকেয়া (${toBengaliDigits(allDuePreset.size.toString())})",
                                    selected = !useCustomMonthText && selectedMonthLabels == allDuePreset,
                                    accentGold = true,
                                    onClick = { applyMonthSelection(allDuePreset) }
                                )
                            }
                            val nextAdvance = advanceMonths.firstOrNull {
                                it.label !in paidMonthLabels && it.label !in selectedMonthLabels
                            }
                            if (nextAdvance != null) {
                                QuickMonthPresetChip(
                                    label = "+অগ্রিম (${nextAdvance.shortLabel})",
                                    selected = false,
                                    accentGold = true,
                                    onClick = { applyMonthSelection(selectedMonthLabels + nextAdvance.label) }
                                )
                            }
                            if (selectedMonthLabels.isNotEmpty() || useCustomMonthText) {
                                QuickMonthPresetChip(
                                    label = "মুছুন",
                                    selected = false,
                                    onClick = {
                                        useCustomMonthText = false
                                        applyMonthSelection(emptySet())
                                    }
                                )
                            }
                        }
                    }

                    // 3. Interactive 3-Column Month Grid (Paid Locked, Unpaid Red-Tint, Selected Emerald, Advance Gold)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "মাসভিত্তিক তালিকা (ট্যাপ করে বাছাই করুন)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MosqueTextSecondary
                            )
                            TextButton(
                                onClick = {
                                    useCustomMonthText = !useCustomMonthText
                                    if (useCustomMonthText && customMonthText.isBlank()) {
                                        customMonthText = formattedMonthsSummary.ifBlank { anchorMonth }
                                    }
                                    formError = null
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.heightIn(min = 36.dp)
                            ) {
                                Icon(
                                    imageVector = if (useCustomMonthText) Icons.Default.CalendarMonth else Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MosqueEmeraldPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (useCustomMonthText) "গ্রিড দেখুন" else "কাস্টম লিখুন",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MosqueEmeraldPrimary
                                )
                            }
                        }

                        if (!useCustomMonthText) {
                            val rows = remember(billingMonths) { billingMonths.chunked(3) }
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                rows.forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        rowItems.forEach { item ->
                                            val isPaid = item.label in paidMonthLabels
                                            val isSelected = item.label in selectedMonthLabels
                                            val isCurrentAnchor = item.label == anchorMonth
                                            val bgColor = when {
                                                isPaid -> MosqueSuccessGreen.copy(alpha = 0.12f)
                                                isSelected -> MosqueEmeraldPrimary
                                                item.isAdvance -> MosqueGoldLight.copy(alpha = 0.35f)
                                                else -> MosqueUrgentRed.copy(alpha = 0.05f)
                                            }
                                            val borderColor = when {
                                                isPaid -> MosqueSuccessGreen.copy(alpha = 0.45f)
                                                isSelected -> MosqueEmeraldDark
                                                item.isAdvance -> MosqueGold
                                                isCurrentAnchor -> MosqueEmeraldPrimary.copy(alpha = 0.55f)
                                                else -> MosqueUrgentRed.copy(alpha = 0.25f)
                                            }
                                            val titleColor = when {
                                                isPaid -> MosqueSuccessGreen
                                                isSelected -> Color.White
                                                else -> MosqueEmeraldDark
                                            }
                                            val subLabel = when {
                                                isPaid -> "পরিশোধিত"
                                                isSelected -> "নির্বাচিত"
                                                item.isAdvance -> "অগ্রিম"
                                                isCurrentAnchor -> "চলতি বকেয়া"
                                                else -> "বকেয়া"
                                            }

                                            Surface(
                                                onClick = {
                                                    if (!isPaid) {
                                                        val updated = if (isSelected) {
                                                            selectedMonthLabels - item.label
                                                        } else {
                                                            selectedMonthLabels + item.label
                                                        }
                                                        applyMonthSelection(updated)
                                                    }
                                                },
                                                enabled = !isPaid,
                                                shape = RoundedCornerShape(10.dp),
                                                color = bgColor,
                                                border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 52.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 6.dp, vertical = 6.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        if (isPaid || isSelected) {
                                                            Icon(
                                                                imageVector = if (isPaid) Icons.Default.CheckCircle else Icons.Default.Check,
                                                                contentDescription = null,
                                                                tint = titleColor,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                        }
                                                        Text(
                                                            text = "${item.shortLabel} '${item.yearLabel.takeLast(2)}",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = titleColor,
                                                            maxLines = 1
                                                        )
                                                    }
                                                    Text(
                                                        text = subLabel,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontSize = 9.sp,
                                                        color = if (isSelected) MosqueGoldLight else titleColor.copy(alpha = 0.85f),
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                        repeat(3 - rowItems.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = customMonthText,
                                onValueChange = {
                                    customMonthText = it
                                    formError = null
                                },
                                label = { Text("মাসের নাম বা বিবরণ *") },
                                placeholder = { Text("যেমন: জানুয়ারি – সেপ্টেম্বর ২০২৪ (৯ মাস)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // 4. Live Multi-Month Calculation Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MosqueEmeraldSoft.copy(alpha = 0.75f),
                        border = BorderStroke(1.dp, MosqueCardBorderGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (formattedMonthsSummary.isNotBlank()) {
                                        "নির্বাচিত: $formattedMonthsSummary"
                                    } else {
                                        "কোনো মাস নির্বাচিত হয়নি"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueEmeraldDark,
                                    modifier = Modifier.weight(1f)
                                )
                                if (monthlyRate > 0.0 && !useCustomMonthText && selectedMonthLabels.isNotEmpty()) {
                                    Text(
                                        text = "${toBengaliDigits(selectedMonthCount.toString())} × ৳${toBengaliDigits(monthlyRate.toInt().toString())} = ৳${toBengaliDigits(String.format("%,.0f", calculatedTotal))}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MosqueEmeraldPrimary
                                    )
                                }
                            }
                            if (formError != null) {
                                Text(
                                    text = formError!!,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MosqueUrgentRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // 5. Editable Amount Field with Sync Chip & Payment Status Hint
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = {
                                amountText = it.filter { ch -> ch.isDigit() }
                                manualAmountOverride = true
                                amountError = null
                            },
                            label = { Text("আদায়ের মোট পরিমাণ (৳) *") },
                            prefix = { Text("৳ ", fontWeight = FontWeight.Bold, color = MosqueEmeraldDark) },
                            trailingIcon = {
                                if (!useCustomMonthText && calculatedTotal > 0.0 && enteredAmount != calculatedTotal) {
                                    TextButton(
                                        onClick = {
                                            manualAmountOverride = false
                                            amountText = calculatedTotal.toInt().toString()
                                            amountError = null
                                        }
                                    ) {
                                        Text("হিসাব বসান", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            },
                            isError = amountError != null,
                            supportingText = {
                                val caption = when {
                                    amountError != null -> amountError!!
                                    !useCustomMonthText && calculatedTotal > 0.0 && enteredAmount > 0.0 && enteredAmount < calculatedTotal -> {
                                        val diff = (calculatedTotal - enteredAmount).toInt()
                                        "আংশিক পরিশোধ (নির্ধারিত হিসাব থেকে ৳${toBengaliDigits(diff.toString())} কম)"
                                    }
                                    !useCustomMonthText && calculatedTotal > 0.0 && enteredAmount > calculatedTotal -> {
                                        val extra = (enteredAmount - calculatedTotal).toInt()
                                        "পূর্ণ পরিশোধ + ৳${toBengaliDigits(extra.toString())} অতিরিক্ত/দান"
                                    }
                                    !useCustomMonthText && calculatedTotal > 0.0 && enteredAmount == calculatedTotal -> {
                                        "নির্ধারিত ${toBengaliDigits(selectedMonthCount.toString())} মাসের পূর্ণ পরিশোধ"
                                    }
                                    else -> "চাঁদার মোট টাকার পরিমাণ লিখুন"
                                }
                                Text(
                                    text = caption,
                                    color = if (amountError != null) MosqueUrgentRed else MosqueTextSecondary,
                                    fontSize = 11.sp
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("chanda_amount_input")
                        )
                    }

                    // 6. Payment Method Selection (3 Cards)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "পরিশোধের পদ্ধতি",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MosqueEmeraldDark
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ChandaMethodOptionCard(
                                label = "নগদ",
                                subLabel = "Cash",
                                icon = Icons.Default.Payments,
                                selected = method == "cash",
                                onClick = { method = "cash" },
                                modifier = Modifier.weight(1f)
                            )
                            ChandaMethodOptionCard(
                                label = "বিকাশ/নগদ",
                                subLabel = "Mobile",
                                icon = Icons.Default.PhoneAndroid,
                                selected = method == "mobile_banking",
                                onClick = { method = "mobile_banking" },
                                modifier = Modifier.weight(1f)
                            )
                            ChandaMethodOptionCard(
                                label = "ব্যাংক/অনলাইন",
                                subLabel = "Bank",
                                icon = Icons.Default.AccountBalance,
                                selected = method == "online",
                                onClick = { method = "online" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Sticky Footer Actions
                HorizontalDivider(color = MosqueCardBorderSubtle)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MosqueBgLight.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(0.9f)
                            .heightIn(min = 48.dp)
                    ) {
                        Text("বাতিল", color = MosqueTextSecondary, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            val m = selectedMember
                            val finalMonth = formattedMonthsSummary.trim()
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            when {
                                m == null -> formError = "সদস্য নির্বাচন করুন"
                                finalMonth.isBlank() -> formError = "কমপক্ষে ১টি মাস বাছাই করুন বা লিখুন"
                                amt <= 0.0 -> amountError = "সঠিক টাকার পরিমাণ লিখুন"
                                else -> {
                                    onConfirm(m, finalMonth, amt, method, selectedMonthCount.coerceAtLeast(1))
                                    onDismiss()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        modifier = Modifier
                            .weight(1.6f)
                            .heightIn(min = 48.dp)
                            .testTag("chanda_confirm_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val btnAmountLabel = if (enteredAmount > 0.0) {
                            "৳${toBengaliDigits(enteredAmount.toInt().toString())} আদায় ও রসিদ"
                        } else {
                            "আদায় ও রসিদ"
                        }
                        Text(
                            text = btnAmountLabel,
                            fontWeight = FontWeight.Bold,
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
private fun ChandaMiniStat(
    label: String,
    value: String,
    containerColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MosqueTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickMonthPresetChip(
    label: String,
    selected: Boolean,
    accentGold: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = when {
        selected -> MosqueEmeraldPrimary
        accentGold -> MosqueGoldLight.copy(alpha = 0.55f)
        else -> MosqueBgLight
    }
    val textColor = when {
        selected -> Color.White
        accentGold -> MosqueEmeraldDark
        else -> MosqueEmeraldDark
    }
    val borderColor = when {
        selected -> MosqueEmeraldDark
        accentGold -> MosqueGold
        else -> MosqueCardBorder
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.heightIn(min = 48.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ChandaMethodOptionCard(
    label: String,
    subLabel: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MosqueEmeraldSoft else MosqueBgLight,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MosqueEmeraldPrimary else MosqueCardBorderSubtle
        ),
        modifier = modifier.heightIn(min = 56.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) MosqueEmeraldPrimary else MosqueTextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MosqueEmeraldDark else MosqueTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = MosqueTextMuted,
                maxLines = 1
            )
        }
    }
}

// 4. Official Chanda Receipt Dialog with QR verification (PRD §8)
@Composable
fun OfficialReceiptDialog(
    payment: ChandaPaymentEntity,
    mosqueName: String = "বায়তুল জান্নাত জামে মসজিদ",
    mosqueAddress: String = "মিরপুর-১০, ঢাকা",
    committeeId: String = "MC-DHK-2024",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

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
                    text = mosqueName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )
                Text(
                    text = "$mosqueAddress • কমিটি আইডি: $committeeId",
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
                val methodLabel = when (payment.method) {
                    "cash" -> "নগদ (Cash)"
                    "online" -> "ব্যাংক/অনলাইন"
                    else -> "বিকাশ/নগদ"
                }
                ReceiptRow("রসিদ নম্বর", payment.receiptCode)
                ReceiptRow("সদস্যের নাম", payment.memberNameBn)
                ReceiptRow("মাসের বিবরণ", payment.month)
                ReceiptRow("টাকার পরিমাণ", "৳${String.format("%,.0f", payment.amountPaid)}")
                ReceiptRow("পরিশোধের মাধ্যম", methodLabel)
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueEmeraldPrimary)
                    ) {
                        Text("বন্ধ করুন")
                    }

                    Button(
                        onClick = {
                            val shareText = buildString {
                                appendLine(mosqueName)
                                appendLine("অফিসিয়াল চাঁদা জমার রসিদ")
                                appendLine("-------------------------")
                                appendLine("রসিদ নম্বর: ${payment.receiptCode}")
                                appendLine("সদস্যের নাম: ${payment.memberNameBn}")
                                appendLine("মাসের বিবরণ: ${payment.month}")
                                appendLine("টাকার পরিমাণ: ৳${String.format("%,.0f", payment.amountPaid)}")
                                appendLine("মাধ্যম: $methodLabel")
                                appendLine("তারিখ ও সময়: ${payment.paidAt}")
                                appendLine("গ্রহণকারী: ${payment.receivedBy}")
                                appendLine("ভেরিফিকেশন কোড: ${payment.qrVerificationCode}")
                            }
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "চাঁদা রসিদ - ${payment.receiptCode}")
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "রসিদ শেয়ার করুন"))
                        },
                        modifier = Modifier.weight(1.4f),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("রসিদ শেয়ার", fontSize = 13.sp)
                    }
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MosqueTextSecondary,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MosqueTextPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

// 5. Publish / Edit Notice Dialog (Executive Islamic Form Design)
@Composable
fun PublishNoticeDialog(
    editingNotice: NoticeEntity? = null,
    aiNoticeState: AiNoticeUiState = AiNoticeUiState.Idle,
    onGenerateAiDraft: (String) -> Unit = {},
    onClearAiDraft: () -> Unit = {},
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, priority: String, isPinned: Boolean) -> Unit
) {
    val isEditMode = editingNotice != null
    var title by remember(editingNotice?.id) { mutableStateOf(editingNotice?.title ?: "") }
    var content by remember(editingNotice?.id) { mutableStateOf(editingNotice?.content ?: "") }
    var priority by remember(editingNotice?.id) { mutableStateOf(editingNotice?.priority ?: "normal") }
    var isPinned by remember(editingNotice?.id) { mutableStateOf(editingNotice?.isPinned ?: false) }

    var showAiDraftBox by remember { mutableStateOf(false) }
    var aiPrompt by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf<String?>(null) }
    var contentError by remember { mutableStateOf<String?>(null) }

    val aiTopicPresets = listOf(
        "জুমা ও বিশেষ বয়ান" to "আগামী জুমার খুতবা ও বিশেষ দোয়া মাহফিল সম্পর্কে মুসল্লিদের অবহিতকরণ নোটিশ",
        "মাসিক চাঁদা" to "চলতি মাসের মসজিদ পরিচালনা ও মাসিক চাঁদা সময়মতো পরিশোধের বিনীত অনুরোধ",
        "পরিচ্ছন্নতা অভিযান" to "আগামী শুক্রবার সকাল ৮টায় মসজিদ প্রাঙ্গণ ও অজুখানা পরিচ্ছন্নতা অভিযানে অংশগ্রহণের আহ্বান",
        "জরুরি তহবিল" to "মসজিদের উন্নয়ন ও সম্প্রসারণ কাজের জন্য বিশেষ তহবিল সংগ্রহের ঘোষণা"
    )

    LaunchedEffect(aiNoticeState) {
        if (aiNoticeState is AiNoticeUiState.Success) {
            title = aiNoticeState.title
            content = aiNoticeState.content
            titleError = null
            contentError = null
            showAiDraftBox = false
            onClearAiDraft()
        }
    }

    DisposableEffect(Unit) {
        onDispose { onClearAiDraft() }
    }

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
                // Executive Emerald Header
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
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isEditMode) Icons.Default.EditNote else Icons.Default.Campaign,
                                        contentDescription = null,
                                        tint = MosqueGoldLight,
                                        modifier = Modifier.size(21.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isEditMode) "নোটিশ সম্পাদনা ফর্ম" else "নতুন নোটিশ প্রকাশ ফর্ম",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "মুসল্লি ও কমিটির জন্য আনুষ্ঠানিক ঘোষণা ও বার্তা",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueGoldLight,
                                    fontSize = 11.sp
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

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Section 1: Gemini AI Notice Drafter Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.75f)),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MosqueEmeraldSoft.copy(alpha = 0.45f))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showAiDraftBox = !showAiDraftBox },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MosqueGold.copy(alpha = 0.25f),
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = MosqueEmeraldDark,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "জেমিনি এআই নোটিশ ড্রাফটার",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MosqueEmeraldDark
                                        )
                                        Text(
                                            text = "এক ক্লিকে মার্জিত ইসলামিক নোটিশ খসড়া লিখুন",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MosqueTextSecondary,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MosqueEmeraldPrimary,
                                    modifier = Modifier.padding(start = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (showAiDraftBox) "লুকান" else "তৈরি করুন",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = if (showAiDraftBox) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }

                            AnimatedVisibility(visible = showAiDraftBox) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    Text(
                                        text = "দ্রুত বিষয় বাছাই করুন:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MosqueEmeraldDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(aiTopicPresets) { (label, promptText) ->
                                            SuggestionChip(
                                                onClick = {
                                                    aiPrompt = promptText
                                                    if (aiNoticeState is AiNoticeUiState.Error) onClearAiDraft()
                                                },
                                                label = {
                                                    Text(
                                                        text = label,
                                                        fontSize = 11.sp,
                                                        color = MosqueEmeraldDark
                                                    )
                                                },
                                                colors = SuggestionChipDefaults.suggestionChipColors(
                                                    containerColor = Color.White
                                                ),
                                                border = BorderStroke(1.dp, MosqueCardBorderGold)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = aiPrompt,
                                        onValueChange = {
                                            aiPrompt = it
                                            if (aiNoticeState is AiNoticeUiState.Error) onClearAiDraft()
                                        },
                                        label = { Text("কোন বিষয়ে নোটিশ লিখতে চান?") },
                                        placeholder = {
                                            Text(
                                                "যেমন: জুমার পর মসজিদ সম্প্রসারণে বিশেষ তহবিল গঠন ও আলোচনা",
                                                fontSize = 12.sp
                                            )
                                        },
                                        enabled = aiNoticeState !is AiNoticeUiState.Loading,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedBorderColor = MosqueEmeraldPrimary,
                                            unfocusedBorderColor = MosqueCardBorderGold
                                        ),
                                        minLines = 2,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (aiNoticeState is AiNoticeUiState.Error) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = MosqueUrgentRed.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(0.8.dp, MosqueUrgentRed.copy(alpha = 0.35f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.ErrorOutline,
                                                    contentDescription = null,
                                                    tint = MosqueUrgentRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = aiNoticeState.message,
                                                    color = MosqueUrgentRed,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { onGenerateAiDraft(aiPrompt) },
                                        enabled = aiNoticeState !is AiNoticeUiState.Loading && aiPrompt.isNotBlank(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 44.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldDark)
                                    ) {
                                        if (aiNoticeState is AiNoticeUiState.Loading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("জেমিনি খসড়া লিখছে...", fontSize = 12.sp)
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = MosqueGoldLight,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("স্বয়ংক্রিয় খসড়া তৈরি করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section 2: Notice Content
                    MemberFormSectionCard(
                        title = "নোটিশের বিষয় ও বিবরণ",
                        icon = Icons.Default.Description
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                titleError = null
                            },
                            label = { Text("নোটিশের শিরোনাম *") },
                            placeholder = { Text("যেমন: পবিত্র শবে বরাত উপলক্ষে বিশেষ দোয়া ও বয়ান") },
                            leadingIcon = {
                                Icon(Icons.Default.Title, contentDescription = null, tint = MosqueEmeraldPrimary)
                            },
                            isError = titleError != null,
                            supportingText = titleError?.let { err ->
                                { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) }
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("notice_title_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = content,
                            onValueChange = {
                                content = it
                                contentError = null
                            },
                            label = { Text("বিস্তারিত বিবরণ *") },
                            placeholder = { Text("মুসল্লি ও কমিটির জন্য পূর্ণাঙ্গ বার্তা বা নির্দেশনা লিখুন...") },
                            isError = contentError != null,
                            supportingText = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = contentError ?: "স্পষ্ট ও সংক্ষিপ্ত ভাষায় বার্তা লিখুন",
                                        color = if (contentError != null) MosqueUrgentRed else MosqueTextMuted,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "${toBengaliDigits(content.length.toString())} অক্ষর",
                                        color = MosqueTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            },
                            minLines = 4,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("notice_content_input")
                        )
                    }

                    // Section 3: Priority & Pinning
                    MemberFormSectionCard(
                        title = "গুরুত্ব ও প্রদর্শন বিন্যাস",
                        icon = Icons.Default.Tune
                    ) {
                        Text(
                            text = "নোটিশের ধরন নির্বাচন করুন:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MosqueTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NoticePriorityOptionCard(
                                modifier = Modifier.weight(1f),
                                selected = priority == "normal",
                                label = "সাধারণ",
                                accentColor = MosqueEmeraldPrimary,
                                icon = Icons.Default.NotificationsNone,
                                onClick = { priority = "normal" }
                            )
                            NoticePriorityOptionCard(
                                modifier = Modifier.weight(1f),
                                selected = priority == "important",
                                label = "গুরুত্বপূর্ণ",
                                accentColor = MosqueWarningAmber,
                                icon = Icons.Default.PriorityHigh,
                                onClick = { priority = "important" }
                            )
                            NoticePriorityOptionCard(
                                modifier = Modifier.weight(1f),
                                selected = priority == "urgent",
                                label = "জরুরি",
                                accentColor = MosqueUrgentRed,
                                icon = Icons.Default.WarningAmber,
                                onClick = { priority = "urgent" }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            onClick = { isPinned = !isPinned },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPinned) MosqueGoldSoft.copy(alpha = 0.65f) else MosqueBgLight,
                            border = BorderStroke(
                                1.dp,
                                if (isPinned) MosqueGold else MosqueCardBorderSubtle
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = null,
                                        tint = if (isPinned) MosqueGoldDeep else MosqueTextMuted,
                                        modifier = Modifier.size(19.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "বোর্ডের শীর্ষে পিন করে রাখুন",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueEmeraldDark
                                        )
                                        Text(
                                            text = "সক্রিয় থাকলে নোটিশটি সবার ওপরে দেখাবে",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MosqueTextSecondary,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }
                                Switch(
                                    checked = isPinned,
                                    onCheckedChange = { isPinned = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MosqueEmeraldPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // Sticky Footer Action Bar
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
                        onClick = {
                            var valid = true
                            if (title.isBlank()) {
                                titleError = "অনুগ্রহ করে নোটিশের শিরোনাম লিখুন"
                                valid = false
                            }
                            if (content.isBlank()) {
                                contentError = "অনুগ্রহ করে বিস্তারিত বিবরণ লিখুন"
                                valid = false
                            }
                            if (valid) {
                                onConfirm(title.trim(), content.trim(), priority, isPinned)
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        modifier = Modifier
                            .weight(1.3f)
                            .heightIn(min = 48.dp)
                            .testTag("submit_notice_button")
                    ) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEditMode) "সংরক্ষণ করুন" else "প্রকাশ করুন",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoticePriorityOptionCard(
    modifier: Modifier = Modifier,
    selected: Boolean,
    label: String,
    accentColor: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) accentColor.copy(alpha = 0.12f) else MosqueBgLight,
        border = BorderStroke(
            width = if (selected) 1.6.dp else 1.dp,
            color = if (selected) accentColor else MosqueCardBorderSubtle
        ),
        modifier = modifier.heightIn(min = 52.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) accentColor else MosqueTextMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (selected) accentColor else MosqueTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// 6. Schedule / Edit Meeting Dialog (Executive Islamic Form Design)
@Composable
fun ScheduleMeetingDialog(
    editingMeeting: MeetingEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, dateStr: String, location: String, agenda: String) -> Unit
) {
    val isEditMode = editingMeeting != null
    var title by remember(editingMeeting?.id) { mutableStateOf(editingMeeting?.title ?: "") }
    var dateStr by remember(editingMeeting?.id) {
        mutableStateOf(editingMeeting?.meetingDate ?: "১৫ সেপ্টেম্বর ২০২৪, বাদ আসর")
    }
    var location by remember(editingMeeting?.id) {
        mutableStateOf(editingMeeting?.location ?: "মসজিদ অফিস কক্ষ")
    }
    var agenda by remember(editingMeeting?.id) { mutableStateOf(editingMeeting?.agenda ?: "") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }

    val meetingTitlePresets = listOf(
        "মাসিক সাধারণ সভা",
        "জরুরি কমিটি সভা",
        "আয়-ব্যয় হিসাব পর্যালোচনা",
        "মসজিদ উন্নয়ন ও নির্মাণ সভা"
    )
    val prayerTimePresets = listOf(
        "বাদ ফজর",
        "বাদ যোহর",
        "বাদ আসর",
        "বাদ মাগরিব",
        "বাদ এশা",
        "জুমার পর"
    )
    val locationPresets = listOf(
        "মসজিদ অফিস কক্ষ",
        "মসজিদ মূল হলরুম",
        "ইমাম সাহেবের কক্ষ",
        "মক্তব কক্ষ"
    )

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
                // Executive Emerald Header
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
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isEditMode) Icons.Default.EditCalendar else Icons.Default.EventAvailable,
                                        contentDescription = null,
                                        tint = MosqueGoldLight,
                                        modifier = Modifier.size(21.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isEditMode) "সভার তথ্য সম্পাদনা" else "নতুন কমিটি সভা আহ্বান",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "সভার তারিখ, ওয়াক্তভিত্তিক সময়, স্থান ও আলোচ্যসূচি নির্ধারণ করুন",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueGoldLight,
                                    fontSize = 11.sp
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

                // Scrollable Form Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Section 1: Meeting Title & Quick Presets
                    MemberFormSectionCard(
                        title = "সভার পরিচিতি ও সময়সূচি",
                        icon = Icons.Default.EventNote
                    ) {
                        if (!isEditMode) {
                            Text(
                                text = "সভার ধরন দ্রুত বাছাই করুন:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MosqueTextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(meetingTitlePresets) { preset ->
                                    FilterChip(
                                        selected = title == preset,
                                        onClick = {
                                            title = preset
                                            titleError = null
                                        },
                                        label = { Text(preset, fontSize = 11.5.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MosqueEmeraldPrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                titleError = null
                            },
                            label = { Text("সভার নাম / উদ্দেশ্য *") },
                            placeholder = { Text("যেমন: সেপ্টেম্বর মাসিক সাধারণ সভা") },
                            leadingIcon = {
                                Icon(Icons.Default.Groups, contentDescription = null, tint = MosqueEmeraldPrimary)
                            },
                            isError = titleError != null,
                            supportingText = titleError?.let { err ->
                                { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) }
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("meeting_title_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = dateStr,
                            onValueChange = {
                                dateStr = it
                                dateError = null
                            },
                            label = { Text("তারিখ ও সময় *") },
                            leadingIcon = {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = MosqueEmeraldPrimary)
                            },
                            isError = dateError != null,
                            supportingText = dateError?.let { err ->
                                { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) }
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("meeting_date_input")
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(prayerTimePresets) { prayerTime ->
                                val isSelected = dateStr.contains(prayerTime)
                                SuggestionChip(
                                    onClick = {
                                        val baseDate = dateStr.substringBefore(",").trim().ifBlank { "আগামী শুক্রবার" }
                                        dateStr = "$baseDate, $prayerTime"
                                        dateError = null
                                    },
                                    label = {
                                        Text(
                                            text = prayerTime,
                                            fontSize = 11.sp,
                                            color = if (isSelected) Color.White else MosqueEmeraldDark,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = if (isSelected) MosqueEmeraldPrimary else MosqueEmeraldSoft.copy(alpha = 0.6f)
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) MosqueEmeraldPrimary else MosqueCardBorderGold)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = location,
                            onValueChange = {
                                location = it
                                locationError = null
                            },
                            label = { Text("সভার স্থান *") },
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MosqueGoldDeep)
                            },
                            isError = locationError != null,
                            supportingText = locationError?.let { err ->
                                { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) }
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("meeting_location_input")
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(locationPresets) { loc ->
                                val isSelected = location == loc
                                SuggestionChip(
                                    onClick = {
                                        location = loc
                                        locationError = null
                                    },
                                    label = {
                                        Text(
                                            text = loc,
                                            fontSize = 11.sp,
                                            color = if (isSelected) Color.White else MosqueEmeraldDark
                                        )
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = if (isSelected) MosqueEmeraldPrimary else Color.White
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) MosqueEmeraldPrimary else MosqueCardBorderGold)
                                )
                            }
                        }
                    }

                    // Section 2: Agenda
                    MemberFormSectionCard(
                        title = "আলোচ্যসূচি / এজেন্ডা",
                        icon = Icons.Default.Checklist
                    ) {
                        OutlinedTextField(
                            value = agenda,
                            onValueChange = { agenda = it },
                            label = { Text("আলোচ্য বিষয়সমূহ (ঐচ্ছিক)") },
                            placeholder = { Text("১. বিগত সভার সিদ্ধান্ত পর্যালোচনা\n২. মাসিক আয়-ব্যয় অনুমোদন\n৩. ...") },
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("meeting_agenda_input")
                        )
                    }
                }

                // Sticky Footer Action Bar
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
                        onClick = {
                            var valid = true
                            if (title.isBlank()) {
                                titleError = "অনুগ্রহ করে সভার নাম/উদ্দেশ্য লিখুন"
                                valid = false
                            }
                            if (dateStr.isBlank()) {
                                dateError = "তারিখ ও সময় উল্লেখ করুন"
                                valid = false
                            }
                            if (location.isBlank()) {
                                locationError = "সভার স্থান উল্লেখ করুন"
                                valid = false
                            }
                            if (valid) {
                                onConfirm(title.trim(), dateStr.trim(), location.trim(), agenda.trim())
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        modifier = Modifier
                            .weight(1.3f)
                            .heightIn(min = 48.dp)
                            .testTag("submit_meeting_button")
                    ) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.EventAvailable,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEditMode) "সংরক্ষণ করুন" else "সভা নির্ধারণ",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

// 7. Meeting Minutes / Resolution Dialog (Executive Islamic Form Design)
@Composable
fun MeetingResolutionDialog(
    meeting: MeetingEntity,
    onDismiss: () -> Unit,
    onConfirm: (resolution: String, markCompleted: Boolean) -> Unit
) {
    var resolution by remember(meeting.id) { mutableStateOf(meeting.resolution) }
    var markCompleted by remember(meeting.id) { mutableStateOf(true) }
    var resolutionError by remember { mutableStateOf<String?>(null) }

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
                // Executive Emerald Header
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
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.FactCheck,
                                        contentDescription = null,
                                        tint = MosqueGoldLight,
                                        modifier = Modifier.size(21.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "সভার সিদ্ধান্ত ও কার্যবিবরণী",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = meeting.title,
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

                // Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 460.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Meeting Metadata Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MosqueEmeraldSoft.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MosqueCardBorderGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Event, contentDescription = null, tint = MosqueEmeraldPrimary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = toBengaliDigits(meeting.meetingDate),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueEmeraldDark
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MosqueGoldDeep, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = meeting.location,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MosqueTextSecondary
                                )
                            }
                        }
                    }

                    MemberFormSectionCard(
                        title = "গৃহীত সিদ্ধান্ত / রেজুলেশন",
                        icon = Icons.Default.NoteAlt
                    ) {
                        OutlinedTextField(
                            value = resolution,
                            onValueChange = {
                                resolution = it
                                resolutionError = null
                            },
                            label = { Text("সভার চূড়ান্ত সিদ্ধান্তসমূহ *") },
                            placeholder = { Text("যেমন: ১. আগস্ট মাসের হিসাব সর্বসম্মতিক্রমে অনুমোদিত হয়েছে।\n২. ...") },
                            isError = resolutionError != null,
                            supportingText = resolutionError?.let { err ->
                                { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) }
                            },
                            minLines = 4,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("meeting_resolution_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            onClick = { markCompleted = !markCompleted },
                            shape = RoundedCornerShape(12.dp),
                            color = if (markCompleted) MosqueSuccessGreen.copy(alpha = 0.1f) else MosqueBgLight,
                            border = BorderStroke(
                                1.dp,
                                if (markCompleted) MosqueSuccessGreen.copy(alpha = 0.5f) else MosqueCardBorderSubtle
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "সভাটি 'সম্পন্ন' হিসেবে চিহ্নিত করুন",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueEmeraldDark,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = markCompleted,
                                    onCheckedChange = { markCompleted = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MosqueSuccessGreen
                                    )
                                )
                            }
                        }
                    }
                }

                // Sticky Footer Action Bar
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
                        onClick = {
                            if (resolution.isBlank()) {
                                resolutionError = "অনুগ্রহ করে গৃহীত সিদ্ধান্ত লিখুন"
                            } else {
                                onConfirm(resolution.trim(), markCompleted)
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        modifier = Modifier
                            .weight(1.3f)
                            .heightIn(min = 48.dp)
                            .testTag("save_meeting_resolution_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("সিদ্ধান্ত সংরক্ষণ", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

