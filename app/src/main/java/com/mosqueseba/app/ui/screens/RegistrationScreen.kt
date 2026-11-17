package com.mosqueseba.app.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.ui.components.MehrabArchCard
import com.mosqueseba.app.ui.components.RubElHizbDivider
import com.mosqueseba.app.ui.components.mosqueStandardFieldColors
import com.mosqueseba.app.ui.components.toBengaliDigits
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun RegistrationScreen(
    viewModel: MosqueViewModel,
    onNavigateBackToLogin: () -> Unit,
    onRegistrationSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var step by remember { mutableIntStateOf(1) } // 1: President, 2: Mosque, 3: Review
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    // Step 1: President Info
    var presidentNameBn by remember { mutableStateOf("") }
    var presidentNameEn by remember { mutableStateOf("") }
    var presidentMobile by remember { mutableStateOf("") }
    var presidentEmail by remember { mutableStateOf("") }
    var presidentNid by remember { mutableStateOf("") }
    var presidentMonthlyChanda by remember { mutableStateOf("") }
    var willingToProvideStaffMeals by remember { mutableStateOf<Boolean?>(null) }
    var presidentPhotoPath by remember { mutableStateOf<String?>(null) }
    var presidentPhotoBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Step 2: Mosque Info
    var mosqueNameBn by remember { mutableStateOf("") }
    var mosqueNameEn by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var upazila by remember { mutableStateOf("") }
    var waqfRegNo by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var bankAccountInfo by remember { mutableStateOf("") }
    var establishedYear by remember { mutableStateOf("2000") }
    var startYear by remember { mutableStateOf("2024") }
    var endYear by remember { mutableStateOf("2026") }
    var mosqueLogoPath by remember { mutableStateOf<String?>(null) }
    var mosqueLogoBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    // Step 3: Terms Agreement
    var agreeToTerms by remember { mutableStateOf(true) }

    // Android Photo Picker launchers (zero permissions required)
    val presidentPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val (savedPath, bitmap) = saveAndDecodePickedImage(context, uri, "president_photo")
                if (savedPath != null && bitmap != null) {
                    presidentPhotoPath = savedPath
                    presidentPhotoBitmap = bitmap
                    errorMessage = null
                } else {
                    errorMessage = "ছবি লোড করা যায়নি। অনুগ্রহ করে অন্য একটি ছবি চেষ্টা করুন।"
                }
            }
        }
    }

    val mosqueLogoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val (savedPath, bitmap) = saveAndDecodePickedImage(context, uri, "mosque_logo")
                if (savedPath != null && bitmap != null) {
                    mosqueLogoPath = savedPath
                    mosqueLogoBitmap = bitmap
                    errorMessage = null
                } else {
                    errorMessage = "লোগো লোড করা যায়নি। অনুগ্রহ করে অন্য একটি ছবি চেষ্টা করুন।"
                }
            }
        }
    }

    val isDark = LocalIsDarkTheme.current
    val fieldColors = mosqueStandardFieldColors(isDark)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sanctuary Header Banner matching GlobalHeader & LoginScreen
        Surface(
            color = Color.Transparent,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MosqueEmeraldDarkGradient)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (step > 1) {
                                    errorMessage = null
                                    step -= 1
                                } else {
                                    onNavigateBackToLogin()
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .testTag("registration_header_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "পেছনে যান",
                                tint = MosqueGoldLight
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MosqueGoldLight
                            )
                            Text(
                                text = "নতুন মসজিদ ও সভাপতি নিবন্ধন",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "মসজিদ পরিচালনা কমিটির অফিসিয়াল অ্যাকাউন্ট তৈরি",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MosqueGold.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, MosqueGold.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = "ধাপ ${toBengaliDigits(step.toString())}/৩",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MosqueGoldLight,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Gold Islamic Accent Trim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(IslamicGoldTrimGradient)
                )
            }
        }

        // Islamic Stepper Progress Bar
        RegistrationStepper(currentStep = step)

        // Scrollable Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            errorMessage?.let { error ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    color = MosqueUrgentRed.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MosqueUrgentRed.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = "ত্রুটি",
                            tint = MosqueUrgentRed
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MosqueUrgentRed,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                label = "RegistrationStepTransition"
            ) { targetStep ->
                when (targetStep) {
                    1 -> {
                        // Step 1: President Profile Photo, Personal Info, Chanda, Meal Confirmation & Credentials
                        MehrabArchCard {
                            Column(modifier = Modifier.padding(18.dp)) {
                                // Step Header Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft)
                                            .border(1.dp, (if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary).copy(alpha = 0.25f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.AdminPanelSettings,
                                            contentDescription = null,
                                            tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "সভাপতির ব্যক্তিগত ও অ্যাকাউন্ট তথ্য",
                                            fontWeight = FontWeight.ExtraBold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark
                                        )
                                        Text(
                                            text = "কমিটির প্রধান হিসেবে আপনার প্রোফাইল ও লগইন তথ্য দিন",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Hero Circular Profile Photo Uploader for President
                                PresidentPhotoUploadBanner(
                                    bitmap = presidentPhotoBitmap,
                                    onUploadClick = {
                                        presidentPhotoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onClearClick = {
                                        presidentPhotoPath = null
                                        presidentPhotoBitmap = null
                                    }
                                )

                                RubElHizbDivider(title = "মৌলিক পরিচিতি")

                                OutlinedTextField(
                                    value = presidentNameBn,
                                    onValueChange = { presidentNameBn = it; errorMessage = null },
                                    label = { Text("সভাপতির পূর্ণ নাম *") },
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary) },
                                    singleLine = true,
                                    colors = fieldColors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp)
                                        .testTag("president_name_bn_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = presidentMobile,
                                    onValueChange = { presidentMobile = it; errorMessage = null },
                                    label = { Text("সভাপতির মোবাইল নম্বর *") },
                                    placeholder = { Text("০১৭১১-২২৩৩৪৪") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    colors = fieldColors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp)
                                        .testTag("president_mobile_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = presidentEmail,
                                    onValueChange = { presidentEmail = it; errorMessage = null },
                                    label = { Text("ইমেইল ঠিকানা (লগইন ইউজার আইডি) *") },
                                    placeholder = { Text("আপনার ইমেইল ঠিকানা লিখুন") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    singleLine = true,
                                    colors = fieldColors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp)
                                        .testTag("president_email_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = presidentNid,
                                    onValueChange = { presidentNid = it },
                                    label = { Text("জাতীয় পরিচয়পত্র (এনআইডি) নম্বর (ঐচ্ছিক)") },
                                    leadingIcon = { Icon(Icons.Default.Fingerprint, contentDescription = null, tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = fieldColors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp)
                                        .testTag("president_nid_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                RubElHizbDivider(title = "চাঁদা ও দায়িত্বশীলতা")

                                OutlinedTextField(
                                    value = presidentMonthlyChanda,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() || it == '.' }) {
                                            presidentMonthlyChanda = input
                                        }
                                    },
                                    label = { Text("মাসিক চাঁদার পরিমাণ (৳) — ফিক্সড নয় (ঐচ্ছিক)") },
                                    placeholder = { Text("ইচ্ছাধীন পরিমাণ লিখুন অথবা খালি রাখুন") },
                                    leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = MosqueGoldDeep) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = fieldColors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp)
                                        .testTag("president_chanda_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Mandatory Staff Meal Willingness Confirmation Card
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isDark) MosqueNightSurfaceVariant else MosqueGoldSoft,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isDark) MosqueNightBorder else (if (willingToProvideStaffMeals == null) MosqueGold.copy(alpha = 0.7f) else MosqueEmeraldPrimary.copy(alpha = 0.5f))
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isDark) MosqueNightSurface else MosqueGold.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Restaurant,
                                                    contentDescription = null,
                                                    tint = if (isDark) MosqueGoldLight else MosqueGoldDark,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "বেতনভুক্ত সদস্যদের (ইমাম, মুয়াজ্জিন, খাদেম) খাবার দিতে রাজি আছেন কি? *",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "দায়িত্বশীল হিসেবে জায়গির/খাবার ব্যবস্থাপনায় আপনার সম্মতি নিশ্চিত করুন",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            MealChoiceButton(
                                                label = "হ্যাঁ, রাজি আছি",
                                                selected = willingToProvideStaffMeals == true,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("meal_willing_yes_button"),
                                                onClick = {
                                                    willingToProvideStaffMeals = true
                                                    errorMessage = null
                                                }
                                            )
                                            MealChoiceButton(
                                                label = "না, আপাতত নয়",
                                                selected = willingToProvideStaffMeals == false,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("meal_willing_no_button"),
                                                onClick = {
                                                    willingToProvideStaffMeals = false
                                                    errorMessage = null
                                                }
                                            )
                                        }
                                    }
                                }

                                RubElHizbDivider(title = "অ্যাকাউন্ট নিরাপত্তা")

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it; errorMessage = null },
                                    label = { Text("গোপন পাসওয়ার্ড তৈরি করুন (কমপক্ষে ৬ সংখ্যা) *") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary) },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "পাসওয়ার্ড দেখুন/লুকান",
                                                tint = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    colors = fieldColors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp)
                                        .testTag("president_password_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it; errorMessage = null },
                                    label = { Text("পাসওয়ার্ড নিশ্চিত করুন *") },
                                    leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary) },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    colors = fieldColors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp)
                                        .testTag("president_confirm_password_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = {
                                        if (presidentNameBn.isBlank()) {
                                            errorMessage = "সভাপতির পূর্ণ নাম প্রদান করুন।"
                                        } else if (presidentMobile.isBlank() || presidentMobile.filter { it.isDigit() }.length < 11) {
                                            errorMessage = "১১ ডিজিটের সঠিক মোবাইল নম্বর প্রদান করুন।"
                                        } else if (presidentEmail.isBlank() || !presidentEmail.contains("@")) {
                                            errorMessage = "লগইনের জন্য সভাপতির সঠিক ইমেইল ঠিকানা প্রদান করুন।"
                                        } else if (willingToProvideStaffMeals == null) {
                                            errorMessage = "বেতনভুক্ত সদস্যদের খাবার প্রদানের বিষয়ে হ্যাঁ বা না নির্বাচন করে নিশ্চিত করুন।"
                                        } else if (password.length < 6) {
                                            errorMessage = "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।"
                                        } else if (password != confirmPassword) {
                                            errorMessage = "উভয় পাসওয়ার্ডের মিল পাওয়া যায়নি।"
                                        } else {
                                            errorMessage = null
                                            step = 2
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("registration_next_step1_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) MosqueEmeraldLight else MosqueEmeraldPrimary)
                                ) {
                                    Text("পরবর্তী ধাপ: মসজিদের তথ্য", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                                }
                            }
                        }
                    }

                    2 -> {
                        // Step 2: Official Mosque Logo Upload, Mosque & Optional Fields
                        MehrabArchCard {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft)
                                            .border(1.dp, (if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary).copy(alpha = 0.25f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Mosque,
                                            contentDescription = null,
                                            tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "মসজিদ ও কমিটির বিবরণ",
                                            fontWeight = FontWeight.ExtraBold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark
                                        )
                                        Text(
                                            text = "মসজিদের অফিসিয়াল লোগো, পরিচিতি ও মেয়াদকাল দিন",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Hero Official Mosque Logo Uploader matching LoginScreen Emblem Style
                                MosqueOfficialLogoUploadBanner(
                                    bitmap = mosqueLogoBitmap,
                                    onUploadClick = {
                                        mosqueLogoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onClearClick = {
                                        mosqueLogoPath = null
                                        mosqueLogoBitmap = null
                                    }
                                )

                                RubElHizbDivider(title = "মসজিদের মৌলিক তথ্য")

                                OutlinedTextField(
                                    value = mosqueNameBn,
                                    onValueChange = { mosqueNameBn = it; errorMessage = null },
                                    label = { Text("মসজিদের নাম (বাংলায়) *") },
                                    placeholder = { Text("যেমন: বায়তুল জান্নাত জামে মসজিদ") },
                                    leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary) },
                                    singleLine = true,
                                    colors = fieldColors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp)
                                        .testTag("mosque_name_bn_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                OutlinedTextField(
                                    value = address,
                                    onValueChange = { address = it; errorMessage = null },
                                    label = { Text("মসজিদের পূর্ণ ঠিকানা, এলাকা ও জেলা *") },
                                    placeholder = { Text("রোড-৭, মিরপুর-১০, ঢাকা-১২১৬") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary) },
                                    colors = fieldColors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp)
                                        .testTag("mosque_address_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = district,
                                        onValueChange = { district = it },
                                        label = { Text("জেলা (ঐচ্ছিক)") },
                                        placeholder = { Text("ঢাকা") },
                                        singleLine = true,
                                        colors = fieldColors,
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 52.dp)
                                            .testTag("mosque_district_input"),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    OutlinedTextField(
                                        value = upazila,
                                        onValueChange = { upazila = it },
                                        label = { Text("উপজেলা/থানা (ঐচ্ছিক)") },
                                        placeholder = { Text("মিরপুর") },
                                        singleLine = true,
                                        colors = fieldColors,
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 52.dp)
                                            .testTag("mosque_upazila_input"),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                }

                                RubElHizbDivider(title = "প্রতিষ্ঠা ও কমিটির মেয়াদ")

                                OutlinedTextField(
                                    value = establishedYear,
                                    onValueChange = { establishedYear = it },
                                    label = { Text("প্রতিষ্ঠা সাল") },
                                    leadingIcon = { Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = fieldColors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp)
                                        .testTag("mosque_established_year_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = startYear,
                                        onValueChange = { startYear = it },
                                        label = { Text("কমিটি শুরু সাল") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = fieldColors,
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 52.dp)
                                            .testTag("mosque_start_year_input"),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    OutlinedTextField(
                                        value = endYear,
                                        onValueChange = { endYear = it },
                                        label = { Text("কমিটি শেষ সাল") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = fieldColors,
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 52.dp)
                                            .testTag("mosque_end_year_input"),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                }

                                RubElHizbDivider(title = "অতিরিক্ত তথ্য (ঐচ্ছিক)")

                                OutlinedTextField(
                                    value = waqfRegNo,
                                    onValueChange = { waqfRegNo = it },
                                    label = { Text("ওয়াকফ / সরকারি নিবন্ধন নম্বর (ঐচ্ছিক)") },
                                    placeholder = { Text("যেমন: ওয়াকফ-ঢাকা-১২৩৪৫") },
                                    leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MosqueGoldDeep) },
                                    singleLine = true,
                                    colors = fieldColors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp)
                                        .testTag("mosque_waqf_reg_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = capacity,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() }) {
                                            capacity = input
                                        }
                                    },
                                    label = { Text("মসজিদের ধারণক্ষমতা / মুসল্লি সংখ্যা (ঐচ্ছিক)") },
                                    placeholder = { Text("যেমন: ৫০০") },
                                    leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null, tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = fieldColors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp)
                                        .testTag("mosque_capacity_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = bankAccountInfo,
                                    onValueChange = { bankAccountInfo = it },
                                    label = { Text("ব্যাংক অ্যাকাউন্ট / মোবাইল ব্যাংকিং তথ্য (ঐচ্ছিক)") },
                                    placeholder = { Text("ব্যাংকের নাম, হিসাব নম্বর বা বিকাশ/নগদ") },
                                    leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary) },
                                    colors = fieldColors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp)
                                        .testTag("mosque_bank_info_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            errorMessage = null
                                            step = 1
                                        },
                                        border = BorderStroke(1.dp, (if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary).copy(alpha = 0.45f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .testTag("registration_back_step2_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            "পূর্ববর্তী ধাপ",
                                            color = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (mosqueNameBn.isBlank()) {
                                                errorMessage = "মসজিদের নাম প্রদান করুন।"
                                            } else if (address.isBlank()) {
                                                errorMessage = "মসজিদের পূর্ণ ঠিকানা প্রদান করুন।"
                                            } else {
                                                errorMessage = null
                                                step = 3
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1.4f)
                                            .height(52.dp)
                                            .testTag("registration_next_step2_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) MosqueEmeraldLight else MosqueEmeraldPrimary)
                                    ) {
                                        Text("যাচাই ও পর্যালোচনা", fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // Step 3: Review & Final Confirmation
                        MehrabArchCard {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) MosqueNightSurfaceVariant else MosqueGoldSoft)
                                            .border(1.dp, (if (isDark) MosqueGoldLight else MosqueGold).copy(alpha = 0.5f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.FactCheck,
                                            contentDescription = null,
                                            tint = if (isDark) MosqueGoldLight else MosqueGoldDark,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "নিবন্ধনের সারসংক্ষেপ",
                                            fontWeight = FontWeight.ExtraBold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark
                                        )
                                        Text(
                                            text = "প্রদত্ত তথ্যসমূহ ও ছবি যাচাই করে নিবন্ধন সম্পন্ন করুন",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Mosque Summary Box
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderGold),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (mosqueLogoBitmap != null) {
                                                Image(
                                                    bitmap = mosqueLogoBitmap!!,
                                                    contentDescription = "মসজিদের অফিসিয়াল লোগো",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .border(1.5.dp, MosqueGoldGradient, RoundedCornerShape(12.dp))
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.Mosque,
                                                        contentDescription = null,
                                                        tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "মসজিদের তথ্য",
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = if (mosqueLogoBitmap != null) "অফিসিয়াল লোগো সংযুক্ত" else "লোগো সংযুক্ত করা হয়নি",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (mosqueLogoBitmap != null) MosqueSuccessGreen else MosqueTextMuted
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        HorizontalDivider(color = if (isDark) MosqueNightBorder else MosqueCardBorder)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        SummaryRow(label = "মসজিদের নাম:", value = mosqueNameBn)
                                        SummaryRow(label = "ঠিকানা:", value = address)
                                        if (district.isNotBlank() || upazila.isNotBlank()) {
                                            SummaryRow(
                                                label = "উপজেলা ও জেলা:",
                                                value = listOf(upazila, district).filter { it.isNotBlank() }.joinToString(", ")
                                            )
                                        }
                                        SummaryRow(label = "প্রতিষ্ঠা ও মেয়াদ:", value = "$establishedYear ($startYear - $endYear)")
                                        if (waqfRegNo.isNotBlank()) {
                                            SummaryRow(label = "ওয়াকফ / নিবন্ধন নং:", value = waqfRegNo)
                                        }
                                        if (capacity.isNotBlank()) {
                                            SummaryRow(label = "ধারণক্ষমতা:", value = "$capacity জন")
                                        }
                                        if (bankAccountInfo.isNotBlank()) {
                                            SummaryRow(label = "ব্যাংক / পেমেন্ট:", value = bankAccountInfo)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // President Summary Box
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderGold),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (presidentPhotoBitmap != null) {
                                                Image(
                                                    bitmap = presidentPhotoBitmap!!,
                                                    contentDescription = "সভাপতির প্রোফাইল ছবি",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                        .border(1.5.dp, MosqueGoldGradient, CircleShape)
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "সভাপতির তথ্য",
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = if (presidentPhotoBitmap != null) "প্রোফাইল ছবি সংযুক্ত" else "প্রোফাইল ছবি সংযুক্ত করা হয়নি",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (presidentPhotoBitmap != null) MosqueSuccessGreen else MosqueTextMuted
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        HorizontalDivider(color = if (isDark) MosqueNightBorder else MosqueCardBorder)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        SummaryRow(label = "সভাপতি নাম:", value = presidentNameBn)
                                        SummaryRow(label = "ইমেইল (লগইন আইডি):", value = presidentEmail)
                                        SummaryRow(label = "মোবাইল নম্বর:", value = presidentMobile)
                                        val parsedChanda = presidentMonthlyChanda.toDoubleOrNull() ?: 0.0
                                        SummaryRow(
                                            label = "মাসিক চাঁদা:",
                                            value = if (parsedChanda > 0) "৳${parsedChanda.toInt()}" else "নির্ধারিত নয় (ইচ্ছাধীন)"
                                        )
                                        SummaryRow(
                                            label = "স্টাফদের খাবার প্রদান:",
                                            value = if (willingToProvideStaffMeals == true) "হ্যাঁ, রাজি আছেন" else "না"
                                        )
                                        SummaryRow(label = "নির্ধারিত দায়িত্ব:", value = "সভাপতি (প্রধান প্রশাসক)")
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft.copy(alpha = 0.55f),
                                    border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueEmeraldPrimary.copy(alpha = 0.25f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { agreeToTerms = !agreeToTerms }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Checkbox(
                                            checked = agreeToTerms,
                                            onCheckedChange = { agreeToTerms = it },
                                            colors = CheckboxDefaults.colors(checkedColor = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "আমি প্রত্যয়ন করছি যে প্রদত্ত তথ্যসমূহ সঠিক এবং মসজিদের সেবায় আমানতদারি রক্ষা করব।",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            errorMessage = null
                                            step = 2
                                        },
                                        border = BorderStroke(1.dp, (if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary).copy(alpha = 0.45f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .testTag("registration_edit_step3_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            "সংশোধন",
                                            color = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (!agreeToTerms) {
                                                errorMessage = "শর্তাবলিতে সম্মত হওয়া আবশ্যক।"
                                                return@Button
                                            }
                                            isSubmitting = true
                                            errorMessage = null
                                            coroutineScope.launch {
                                                val res = viewModel.registerNewMosqueAndPresident(
                                                    presidentNameBn = presidentNameBn,
                                                    presidentNameEn = presidentNameEn,
                                                    presidentMobile = presidentMobile,
                                                    presidentEmail = presidentEmail,
                                                    presidentNid = presidentNid,
                                                    password = password,
                                                    presidentMonthlyChanda = presidentMonthlyChanda.toDoubleOrNull() ?: 0.0,
                                                    willingToProvideStaffMeals = willingToProvideStaffMeals == true,
                                                    presidentPhotoUri = presidentPhotoPath,
                                                    mosqueNameBn = mosqueNameBn,
                                                    mosqueNameEn = mosqueNameEn,
                                                    address = address,
                                                    establishedYear = establishedYear.toIntOrNull() ?: 2000,
                                                    startYear = startYear.toIntOrNull() ?: 2024,
                                                    endYear = endYear.toIntOrNull() ?: 2026,
                                                    district = district,
                                                    upazila = upazila,
                                                    waqfRegNo = waqfRegNo,
                                                    capacity = capacity.toIntOrNull(),
                                                    bankAccountInfo = bankAccountInfo,
                                                    mosqueLogoUri = mosqueLogoPath
                                                )
                                                isSubmitting = false
                                                if (res.isSuccess) {
                                                    onRegistrationSuccess()
                                                } else {
                                                    errorMessage = res.exceptionOrNull()?.message ?: "নিবন্ধন ব্যর্থ হয়েছে।"
                                                }
                                            }
                                        },
                                        enabled = !isSubmitting,
                                        modifier = Modifier
                                            .weight(1.8f)
                                            .height(52.dp)
                                            .testTag("registration_submit_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) MosqueEmeraldLight else MosqueEmeraldPrimary)
                                    ) {
                                        if (isSubmitting) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Text("নিবন্ধন সম্পন্ন করুন", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back to Login Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onNavigateBackToLogin() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "ইতিমধ্যে অ্যাকাউন্ট আছে? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                )
                Text(
                    text = "লগইন করুন",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PresidentPhotoUploadBanner(
    bitmap: ImageBitmap?,
    onUploadClick: () -> Unit,
    onClearClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
        border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderGold),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .size(96.dp)
                    .clickable { onUploadClick() }
                    .testTag("president_photo_upload_area")
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .shadow(6.dp, CircleShape, spotColor = MosqueGoldDeep.copy(alpha = 0.28f))
                        .border(2.dp, MosqueGoldGradient, CircleShape)
                        .clip(CircleShape)
                        .background(if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "সভাপতির প্রোফাইল ছবি",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "সভাপতির প্রোফাইল ছবি প্লেসহোল্ডার",
                            tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isDark) MosqueEmeraldLight else MosqueEmeraldPrimary)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "ছবি বাছাই করুন",
                        tint = MosqueGoldLight,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "সভাপতির প্রোফাইল ছবি (ঐচ্ছিক)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark
            )
            Text(
                text = if (bitmap != null) "প্রোফাইল ছবি সফলভাবে সংযুক্ত হয়েছে" else "প্রোফাইল ও কমিটি তালিকায় প্রদর্শনের জন্য ছবি আপলোড করুন",
                style = MaterialTheme.typography.labelSmall,
                color = if (bitmap != null) MosqueSuccessGreen else if (isDark) MosqueNightTextSecondary else MosqueTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onUploadClick,
                    border = BorderStroke(1.dp, (if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("president_photo_upload_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (bitmap != null) "ছবি পরিবর্তন করুন" else "প্রোফাইল ছবি আপলোড",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary
                    )
                }

                if (bitmap != null) {
                    OutlinedButton(
                        onClick = onClearClick,
                        border = BorderStroke(1.dp, MosqueUrgentRed.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("president_photo_clear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "ছবি মুছুন",
                            tint = MosqueUrgentRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MosqueOfficialLogoUploadBanner(
    bitmap: ImageBitmap?,
    onUploadClick: () -> Unit,
    onClearClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
        border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderGold),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .size(100.dp)
                    .clickable { onUploadClick() }
                    .testTag("mosque_logo_upload_area")
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .shadow(8.dp, RoundedCornerShape(22.dp), spotColor = MosqueGold)
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(listOf(MosqueGold, if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary)),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isDark) MosqueNightSurfaceVariant else Color.White)
                        .padding(if (bitmap != null) 3.dp else 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "মসজিদের অফিসিয়াল লোগো",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(18.dp))
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Mosque,
                            contentDescription = "মসজিদের অফিসিয়াল লোগো প্লেসহোল্ডার",
                            tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isDark) MosqueEmeraldLight else MosqueEmeraldPrimary)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "লোগো বাছাই করুন",
                        tint = MosqueGoldLight,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "মসজিদের অফিসিয়াল লোগো (ঐচ্ছিক)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark
            )
            Text(
                text = if (bitmap != null) "অফিসিয়াল লোগো সফলভাবে সংযুক্ত হয়েছে" else "রসিদ, নোটিশ বোর্ড ও ড্যাশবোর্ডের জন্য মসজিদের লোগো আপলোড করুন",
                style = MaterialTheme.typography.labelSmall,
                color = if (bitmap != null) MosqueSuccessGreen else if (isDark) MosqueNightTextSecondary else MosqueTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onUploadClick,
                    border = BorderStroke(1.dp, (if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("mosque_logo_upload_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (bitmap != null) "লোগো পরিবর্তন করুন" else "অফিসিয়াল লোগো আপলোড",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary
                    )
                }

                if (bitmap != null) {
                    OutlinedButton(
                        onClick = onClearClick,
                        border = BorderStroke(1.dp, MosqueUrgentRed.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("mosque_logo_clear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "লোগো মুছুন",
                            tint = MosqueUrgentRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    val isDark = LocalIsDarkTheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun RegistrationStepper(currentStep: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepItem(number = 1, title = "সভাপতি", isActive = currentStep >= 1, isCurrent = currentStep == 1)
                StepDivider(isDone = currentStep > 1)
                StepItem(number = 2, title = "মসজিদ", isActive = currentStep >= 2, isCurrent = currentStep == 2)
                StepDivider(isDone = currentStep > 2)
                StepItem(number = 3, title = "যাচাই", isActive = currentStep >= 3, isCurrent = currentStep == 3)
            }
            HorizontalDivider(color = if (isDark) MosqueNightBorder else MosqueCardBorderGold.copy(alpha = 0.65f))
        }
    }
}

@Composable
fun StepItem(number: Int, title: String, isActive: Boolean, isCurrent: Boolean) {
    val isDark = LocalIsDarkTheme.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCurrent -> if (isDark) MosqueEmeraldLight else MosqueEmeraldPrimary
                        isActive -> MosqueSuccessGreen
                        else -> if (isDark) MosqueNightSurfaceVariant else Color(0xFFE2E8F0)
                    }
                )
                .then(
                    if (isCurrent) {
                        Modifier.border(2.dp, MosqueGold, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isActive && !isCurrent) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
            } else {
                Text(
                    text = toBengaliDigits(number.toString()),
                    color = if (isActive || isCurrent) Color.White else if (isDark) MosqueNightTextSecondary else MosqueTextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
            color = if (isCurrent) (if (isDark) MosqueGoldLight else MosqueEmeraldDark) else if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
        )
    }
}

@Composable
fun StepDivider(isDone: Boolean) {
    val isDark = LocalIsDarkTheme.current
    Box(
        modifier = Modifier
            .width(54.dp)
            .height(2.5.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(if (isDone) MosqueSuccessGreen else if (isDark) MosqueNightBorder else MosqueCardBorder)
    )
}

@Composable
private fun MealChoiceButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) (if (isDark) MosqueEmeraldLight else MosqueEmeraldPrimary) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) (if (isDark) MosqueEmeraldLight else MosqueEmeraldPrimary) else if (isDark) MosqueNightBorder else MosqueCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (selected) MosqueGoldLight else MosqueTextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private suspend fun saveAndDecodePickedImage(
    context: Context,
    uri: Uri,
    prefix: String
): Pair<String?, ImageBitmap?> = withContext(Dispatchers.IO) {
    try {
        val uploadDir = File(context.filesDir, "uploads").apply { mkdirs() }
        val targetFile = File(uploadDir, "${prefix}_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        } ?: return@withContext null to null

        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(targetFile.absolutePath, boundsOptions)
        var sampleSize = 1
        while (boundsOptions.outWidth / sampleSize > 512 || boundsOptions.outHeight / sampleSize > 512) {
            sampleSize *= 2
        }
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap: Bitmap? = BitmapFactory.decodeFile(targetFile.absolutePath, decodeOptions)
        if (bitmap != null) {
            targetFile.absolutePath to bitmap.asImageBitmap()
        } else {
            null to null
        }
    } catch (_: Exception) {
        null to null
    }
}
