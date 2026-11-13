package com.mosqueseba.app.ui.components

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mosqueseba.app.data.local.entity.ChandaPaymentEntity
import com.mosqueseba.app.data.local.entity.MemberEntity
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

    var nameBn by remember { mutableStateOf(initialMember?.nameBn ?: initialStaff?.nameBn ?: "") }
    var nameEn by remember { mutableStateOf(initialMember?.nameEn ?: "") }
    var mobile by remember { mutableStateOf(initialMember?.mobile ?: initialStaff?.mobile ?: "") }
    var email by remember { mutableStateOf(initialMember?.email ?: "") }
    var nid by remember { mutableStateOf(initialMember?.nid ?: "") }
    var address by remember { mutableStateOf(initialMember?.address ?: "") }
    var villageArea by remember { mutableStateOf(initialMember?.villageArea ?: "") }
    var selectedRole by remember {
        mutableStateOf(
            initialMember?.let { UserRole.fromCode(it.roleCode) }
                ?: initialStaff?.let { UserRole.fromCode(it.roleCode) }
                ?: UserRole.GENERAL_MEMBER
        )
    }
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
                                        "প্রোফাইল, পদবি ও নির্ধারিত তথ্য পরিবর্তন করুন"
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
                                onClick = { roleDropdownExpanded = true },
                                shape = RoundedCornerShape(12.dp),
                                color = MosqueEmeraldSoft.copy(alpha = 0.65f),
                                border = BorderStroke(1.dp, MosqueEmeraldPrimary.copy(alpha = 0.55f)),
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
                                                text = if (isStaffRole) "বেতনভুক্ত সদস্য তালিকা" else "মাসিক চাঁদা দাতা তালিকা",
                                                fontSize = 10.5.sp,
                                                color = MosqueTextSecondary
                                            )
                                        }
                                    }
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = "পদবি নির্বাচন",
                                        tint = MosqueEmeraldPrimary
                                    )
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
                                    salaryAmount = it.filter { ch -> ch.isDigit() }
                                    salaryError = null
                                },
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
                                        text = "মসজিদ বা সদস্যদের পক্ষ থেকে দৈনিক খাবারের (জায়গীর) ব্যবস্থা নেবেন কি না তা নিশ্চিত করুন।",
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
                                            text = "বেতনভুক্ত স্টাফদের খাবার (জায়গীর) প্রদান:",
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
                                onValueChange = { address = it },
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
                                    val cleanArea = villageArea.trim().ifBlank {
                                        address.trim().substringBefore(",").ifBlank { "মসজিদ এলাকা" }
                                    }
                                    onConfirm(
                                        nameBn.trim(),
                                        nameEn.trim(),
                                        mobile.trim(),
                                        email.trim(),
                                        nid.trim(),
                                        address.trim().ifBlank { cleanArea },
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

// 2. Add Transaction Dialog (Income or Expense)
@Composable
fun AddTransactionDialog(
    initialType: String = "INCOME",
    onDismiss: () -> Unit,
    onConfirm: (category: String, type: String, amount: Double, description: String) -> Unit
) {
    var type by remember { mutableStateOf(initialType) }
    var category by remember { mutableStateOf(if (type == "INCOME") "জুমার দান বাক্স" else "বিদ্যুৎ ও ইউটিলিটি বিল") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val incomeCategories = listOf("জুমার দান বাক্স", "মাসিক সদস্য চাঁদা", "মসজিদ সংস্কার অনুদান", "হিফজখানা অনুদান", "সাধারণ দান")
    val expenseCategories = listOf("ইমাম ও কর্মচারীদের বেতন", "বিদ্যুৎ ও ইউটিলিটি বিল", "মসজিদ রক্ষণাবেক্ষণ", "জেনারেটর জ্বালানি", "আপ্যায়ন ও বিবিধ")

    Dialog(onDismissRequest = onDismiss) {
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
                    text = if (type == "INCOME") "নতুন আয় অন্তর্ভুক্ত করুন" else "নতুন ব্যয় লিপিবদ্ধ করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (type == "INCOME") MosqueSuccessGreen else MosqueUrgentRed
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Type Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MosqueBgLight)
                        .padding(4.dp)
                ) {
                    Button(
                        onClick = {
                            type = "INCOME"
                            category = incomeCategories.first()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "INCOME") MosqueSuccessGreen else Color.Transparent,
                            contentColor = if (type == "INCOME") Color.White else MosqueTextSecondary
                        ),
                        elevation = null
                    ) {
                        Text("আয় (জমা)")
                    }
                    Button(
                        onClick = {
                            type = "EXPENSE"
                            category = expenseCategories.first()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "EXPENSE") MosqueUrgentRed else Color.Transparent,
                            contentColor = if (type == "EXPENSE") Color.White else MosqueTextSecondary
                        ),
                        elevation = null
                    ) {
                        Text("ব্যয় (খরচ)")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Categories
                Text(
                    text = "খাত নির্বাচন করুন:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MosqueTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                val currentCats = if (type == "INCOME") incomeCategories else expenseCategories
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    currentCats.take(4).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("টাকার পরিমাণ (৳) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("বিবরণ / উদ্দেশ্য") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = MosqueTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                onConfirm(category, type, amt, description)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "INCOME") MosqueSuccessGreen else MosqueUrgentRed
                        )
                    ) {
                        Text("দাখিল করুন")
                    }
                }
            }
        }
    }
}

// 3. Receive Chanda Dialog
@Composable
fun ReceiveChandaDialog(
    members: List<MemberEntity>,
    preselectedMember: MemberEntity? = null,
    defaultMonth: String = "সেপ্টেম্বর ২০২৪",
    suggestedAmount: Double? = null,
    onDismiss: () -> Unit,
    onConfirm: (member: MemberEntity, month: String, amount: Double, method: String) -> Unit
) {
    val nonStaffMembers = remember(members) {
        members.filter {
            !it.roleCode.equals("imam", ignoreCase = true) &&
                !it.roleCode.equals("muazzin", ignoreCase = true) &&
                !it.roleCode.equals("khadem", ignoreCase = true)
        }.ifEmpty { members }
    }
    var selectedMember by remember(preselectedMember, nonStaffMembers) {
        mutableStateOf(preselectedMember ?: nonStaffMembers.firstOrNull())
    }
    var memberPickerExpanded by remember { mutableStateOf(false) }
    var memberSearch by remember { mutableStateOf("") }
    var month by remember { mutableStateOf(defaultMonth) }
    var amount by remember(preselectedMember, suggestedAmount) {
        val initAmt = suggestedAmount ?: preselectedMember?.monthlyChanda ?: selectedMember?.monthlyChanda ?: 500.0
        mutableStateOf(if (initAmt > 0) initAmt.toInt().toString() else "")
    }
    var method by remember { mutableStateOf("cash") }
    var amountError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        MehrabArchCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White,
            borderColor = MosqueCardBorderGold
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "সদস্য চাঁদা গ্রহণ ও রসিদ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "অফিসিয়াল রসিদসহ স্বয়ংক্রিয় হিসাব যুক্ত হবে",
                                style = MaterialTheme.typography.bodySmall,
                                color = MosqueGoldLight,
                                fontSize = 11.sp
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = Color.White)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "সদস্য নির্বাচন:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MosqueEmeraldDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            onClick = { memberPickerExpanded = true },
                            shape = RoundedCornerShape(12.dp),
                            color = MosqueBgLight,
                            border = BorderStroke(1.dp, MosqueCardBorderGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedMember?.nameBn ?: "সদস্য বাছাই করুন",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueEmeraldDark
                                    )
                                    selectedMember?.let { m ->
                                        val r = UserRole.fromCode(m.roleCode)
                                        Text(
                                            text = "${r.titleBn} • ${m.memberCode} • মাসিক ৳${toBengaliDigits(m.monthlyChanda.toInt().toString())}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MosqueTextSecondary
                                        )
                                    }
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "সদস্য পরিবর্তন", tint = MosqueEmeraldPrimary)
                            }
                        }

                        DropdownMenu(
                            expanded = memberPickerExpanded,
                            onDismissRequest = { memberPickerExpanded = false },
                            modifier = Modifier
                                .widthIn(min = 260.dp)
                                .heightIn(max = 320.dp)
                        ) {
                            OutlinedTextField(
                                value = memberSearch,
                                onValueChange = { memberSearch = it },
                                placeholder = { Text("নাম বা কোড লিখুন...", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            nonStaffMembers.filter {
                                memberSearch.isBlank() ||
                                    it.nameBn.contains(memberSearch, ignoreCase = true) ||
                                    it.memberCode.contains(memberSearch, ignoreCase = true) ||
                                    it.mobile.contains(memberSearch)
                            }.forEach { m ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(m.nameBn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(
                                                "${UserRole.fromCode(m.roleCode).titleBn} (${m.memberCode}) • ৳${toBengaliDigits(m.monthlyChanda.toInt().toString())}",
                                                fontSize = 11.sp,
                                                color = MosqueTextSecondary
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedMember = m
                                        amount = if (m.monthlyChanda > 0) m.monthlyChanda.toInt().toString() else ""
                                        memberPickerExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

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
                        onValueChange = { amount = it.filter { ch -> ch.isDigit() }; amountError = null },
                        label = { Text("আদায়ের পরিমাণ (৳) *") },
                        isError = amountError != null,
                        supportingText = amountError?.let { err -> { Text(err, color = MosqueUrgentRed, fontSize = 11.sp) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "পরিশোধের পদ্ধতি:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MosqueTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = method == "cash",
                            onClick = { method = "cash" },
                            label = { Text("নগদ (Cash)", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = method == "mobile_banking",
                            onClick = { method = "mobile_banking" },
                            label = { Text("বিকাশ/নগদ", fontSize = 12.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("বাতিল", color = MosqueTextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amt = amount.toDoubleOrNull() ?: 0.0
                                val m = selectedMember
                                if (m != null && amt > 0) {
                                    onConfirm(m, month.trim().ifBlank { "চলতি মাস" }, amt, method)
                                    onDismiss()
                                } else {
                                    amountError = "সঠিক টাকার পরিমাণ লিখুন"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("রসিদ প্রস্তুত করুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PayStaffSalaryDialog(
    staff: StaffEntity,
    defaultMonth: String = "সেপ্টেম্বর ২০২৪",
    onDismiss: () -> Unit,
    onConfirm: (month: String, bonus: Double) -> Unit
) {
    var month by remember { mutableStateOf(defaultMonth) }
    var bonus by remember { mutableStateOf("0") }

    Dialog(onDismissRequest = onDismiss) {
        MehrabArchCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White,
            borderColor = MosqueCardBorderGold
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MosqueEmeraldDarkGradient)
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Column {
                        Text(
                            text = "বেতন ও ভাতা পরিশোধ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "${staff.nameBn} (${staff.designationBn})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MosqueGoldLight
                        )
                    }
                }
                Column(modifier = Modifier.padding(18.dp)) {
                    Surface(
                        color = MosqueEmeraldSoft,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("মূল মাসিক বেতন", style = MaterialTheme.typography.labelMedium, color = MosqueEmeraldDark)
                            Text(
                                text = "৳${toBengaliDigits(String.format("%,.0f", staff.salaryAmount))}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MosqueEmeraldDark
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = month,
                        onValueChange = { month = it },
                        label = { Text("বেতনের মাস *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bonus,
                        onValueChange = { bonus = it.filter { ch -> ch.isDigit() } },
                        label = { Text("বোনাস / অতিরিক্ত ভাতা (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                            Text("বাতিল", color = MosqueTextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val b = bonus.toDoubleOrNull() ?: 0.0
                                onConfirm(month.trim().ifBlank { defaultMonth }, b)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("পরিশোধ নিশ্চিত করুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
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
                ReceiptRow("রসিদ নম্বর", payment.receiptCode)
                ReceiptRow("সদস্যের নাম", payment.memberNameBn)
                ReceiptRow("মাসের বিবরণ", payment.month)
                ReceiptRow("টাকার পরিমাণ", "৳${String.format("%,.0f", payment.amountPaid)}")
                ReceiptRow("পরিশোধের মাধ্যম", if (payment.method == "cash") "নগদ (Cash)" else "বিকাশ/নগদ")
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
                                appendLine("মাধ্যম: ${if (payment.method == "cash") "নগদ (Cash)" else "বিকাশ/নগদ"}")
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
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MosqueTextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MosqueTextPrimary
        )
    }
}

// 5. Publish Notice Dialog
@Composable
fun PublishNoticeDialog(
    aiNoticeState: AiNoticeUiState = AiNoticeUiState.Idle,
    onGenerateAiDraft: (String) -> Unit = {},
    onClearAiDraft: () -> Unit = {},
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, priority: String, isPinned: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("normal") }
    var isPinned by remember { mutableStateOf(false) }

    var showAiDraftBox by remember { mutableStateOf(false) }
    var aiPrompt by remember { mutableStateOf("") }

    LaunchedEffect(aiNoticeState) {
        if (aiNoticeState is AiNoticeUiState.Success) {
            title = aiNoticeState.title
            content = aiNoticeState.content
            showAiDraftBox = false
            onClearAiDraft()
        }
    }

    DisposableEffect(Unit) {
        onDispose { onClearAiDraft() }
    }

    Dialog(onDismissRequest = onDismiss) {
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
                    text = "নতুন নোটিশ প্রকাশ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Gemini AI Notice Generator Box (PRD §14, §28)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MosqueEmeraldLight.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✨", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "জেমিনি এআই নোটিশ ড্রাফটার (Gemini AI)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueEmeraldDark
                                )
                            }
                            TextButton(onClick = { showAiDraftBox = !showAiDraftBox }) {
                                Text(
                                    if (showAiDraftBox) "লুকান" else "ড্রাফট তৈরি করুন",
                                    fontSize = 11.sp,
                                    color = MosqueEmeraldPrimary
                                )
                            }
                        }

                        if (showAiDraftBox) {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = aiPrompt,
                                onValueChange = {
                                    aiPrompt = it
                                    if (aiNoticeState is AiNoticeUiState.Error) onClearAiDraft()
                                },
                                placeholder = {
                                    Text(
                                        "যেমন: জুমার পর মসজিদ সম্প্রসারণে বিশেষ তহবিল গঠন ও আলোচনা",
                                        fontSize = 12.sp
                                    )
                                },
                                enabled = aiNoticeState !is AiNoticeUiState.Loading,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (aiNoticeState is AiNoticeUiState.Error) {
                                Surface(
                                    color = MosqueUrgentRed.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
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

                            Button(
                                onClick = {
                                    onGenerateAiDraft(aiPrompt)
                                },
                                enabled = aiNoticeState !is AiNoticeUiState.Loading && aiPrompt.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldDark)
                            ) {
                                if (aiNoticeState is AiNoticeUiState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("জেমিনি খসড়া লিখছে...", fontSize = 12.sp)
                                } else {
                                    Text("স্বয়ংক্রিয় ইসলামিক খসড়া নোটিশ তৈরি করুন", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("নোটিশের শিরোনাম *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("বিস্তারিত বিবরণ *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))


                Text(
                    text = "গুরুত্ব নির্বাচন করুন:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MosqueTextSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = priority == "normal",
                        onClick = { priority = "normal" },
                        label = { Text("সাধারণ") }
                    )
                    FilterChip(
                        selected = priority == "important",
                        onClick = { priority = "important" },
                        label = { Text("গুরুত্বপূর্ণ") }
                    )
                    FilterChip(
                        selected = priority == "urgent",
                        onClick = { priority = "urgent" },
                        label = { Text("জরুরি") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it }
                    )
                    Text(
                        text = "বোর্ডের শীর্ষে পিন করে রাখুন",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = MosqueTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                onConfirm(title, content, priority, isPinned)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Text("প্রকাশ করুন")
                    }
                }
            }
        }
    }
}

// 6. Schedule Meeting Dialog
@Composable
fun ScheduleMeetingDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, dateStr: String, location: String, agenda: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf("১৫ সেপ্টেম্বর ২০২৪, বাদ আসর") }
    var location by remember { mutableStateOf("মসজিদ অফিস কক্ষ") }
    var agenda by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
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
                    text = "কমিটি সভা আহ্বান করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("সভার নাম / উদ্দেশ্য *") },
                    placeholder = { Text("যেমন: সেপ্টেম্বর মাসিক সাধারণ সভা") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text("তারিখ ও সময় *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("স্থান *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = agenda,
                    onValueChange = { agenda = it },
                    label = { Text("আলোচ্যসূচি / এজেন্ডা") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = MosqueTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm(title, dateStr, location, agenda)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        Text("সভা নির্ধারণ")
                    }
                }
            }
        }
    }
}

