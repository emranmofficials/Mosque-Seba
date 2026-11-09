package com.mosqueseba.app.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel
import kotlinx.coroutines.launch

@Composable
fun RegistrationScreen(
    viewModel: MosqueViewModel,
    onNavigateBackToLogin: () -> Unit,
    onRegistrationSuccess: () -> Unit
) {
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
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Step 2: Mosque Info
    var mosqueNameBn by remember { mutableStateOf("") }
    var mosqueNameEn by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var establishedYear by remember { mutableStateOf("2000") }
    var startYear by remember { mutableStateOf("2024") }
    var endYear by remember { mutableStateOf("2026") }

    // Step 3: Terms Agreement
    var agreeToTerms by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MosqueBgLight)
    ) {
        // App Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(MosqueEmeraldDark, Color(0xFF023622))
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (step > 1) step -= 1 else onNavigateBackToLogin()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MosqueGold
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "নতুন মসজিদ ও সভাপতি নিবন্ধন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "মসজিদ পরিচালনা কমিটির ডিজিটাল অ্যাকাউন্ট তৈরি",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Stepper Progress Indicator
        RegistrationStepper(currentStep = step)

        // Scrollable Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MosqueUrgentRed.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MosqueUrgentRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = error, color = MosqueUrgentRed, style = MaterialTheme.typography.bodyMedium)
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
                        // Step 1: President & Account Credentials
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MosqueEmeraldPrimary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = MosqueEmeraldPrimary)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "সভাপতির ব্যক্তিগত ও অ্যাকাউন্ট তথ্য",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MosqueEmeraldDark
                                        )
                                        Text(
                                            text = "কমিটির প্রধান হিসেবে সকল তথ্যের সঠিকতা নিশ্চিত করুন",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MosqueTextMuted
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                OutlinedTextField(
                                    value = presidentNameBn,
                                    onValueChange = { presidentNameBn = it; errorMessage = null },
                                    label = { Text("সভাপতির পূর্ণ নাম (বাংলায়) *") },
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = MosqueEmeraldPrimary) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = presidentNameEn,
                                    onValueChange = { presidentNameEn = it },
                                    label = { Text("সভাপতির নাম (ইংরেজিতে)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = presidentMobile,
                                    onValueChange = { presidentMobile = it; errorMessage = null },
                                    label = { Text("মোবাইল নম্বর (লগইন ইউজার আইডি) *") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MosqueEmeraldPrimary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = presidentEmail,
                                    onValueChange = { presidentEmail = it },
                                    label = { Text("ইমেইল ঠিকানা (ঐচ্ছিক)") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MosqueEmeraldPrimary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = presidentNid,
                                    onValueChange = { presidentNid = it },
                                    label = { Text("জাতীয় পরিচয়পত্র (NID) নম্বর") },
                                    leadingIcon = { Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MosqueEmeraldPrimary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it; errorMessage = null },
                                    label = { Text("গোপন পাসওয়ার্ড তৈরি করুন (কমপক্ষে ৬ ডিজিট) *") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MosqueEmeraldPrimary) },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle Password"
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it; errorMessage = null },
                                    label = { Text("পাসওয়ার্ড নিশ্চিত করুন *") },
                                    leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = MosqueEmeraldPrimary) },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = {
                                        if (presidentNameBn.isBlank()) {
                                            errorMessage = "সভাপতির পূর্ণ নাম প্রদান করুন।"
                                        } else if (presidentMobile.isBlank() || presidentMobile.length < 11) {
                                            errorMessage = "১১ ডিজিটের সঠিক মোবাইল নম্বর প্রদান করুন।"
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
                                        .height(50.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                ) {
                                    Text("পরবর্তী ধাপ (মসজিদের বিবরণ)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                                }
                            }
                        }
                    }

                    2 -> {
                        // Step 2: Mosque & Committee Details
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MosqueEmeraldPrimary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Mosque, contentDescription = null, tint = MosqueEmeraldPrimary)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "মসজিদ ও কমিটির বিবরণ",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MosqueEmeraldDark
                                        )
                                        Text(
                                            text = "মসজিদ পরিচিতি ও বর্তমান কমিটির সময়কাল",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MosqueTextMuted
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                OutlinedTextField(
                                    value = mosqueNameBn,
                                    onValueChange = { mosqueNameBn = it; errorMessage = null },
                                    label = { Text("মসজিদের নাম (বাংলায়) *") },
                                    placeholder = { Text("যেমন: বায়তুল জান্নাত জামে মসজিদ") },
                                    leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MosqueEmeraldPrimary) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = mosqueNameEn,
                                    onValueChange = { mosqueNameEn = it },
                                    label = { Text("মসজিদের নাম (ইংরেজিতে)") },
                                    placeholder = { Text("Baitul Jannat Jame Masjid") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = address,
                                    onValueChange = { address = it; errorMessage = null },
                                    label = { Text("মসজিদের পূর্ণ ঠিকানা, এলাকা ও জেলা *") },
                                    placeholder = { Text("রোড-৭, মিরপুর-১০, ঢাকা-১২১৬") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MosqueEmeraldPrimary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = establishedYear,
                                    onValueChange = { establishedYear = it },
                                    label = { Text("প্রতিষ্ঠা সাল") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
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
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    OutlinedTextField(
                                        value = endYear,
                                        onValueChange = { endYear = it },
                                        label = { Text("কমিটি শেষ সাল") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { step = 1 },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("পূর্ববর্তী ধাপ")
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
                                            .height(50.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                                    ) {
                                        Text("যাচাই ও পর্যালোচনা", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // Step 3: Review & Final Confirmation
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "নিবন্ধনের সারসংক্ষেপ",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MosqueEmeraldDark
                                )
                                Text(
                                    text = "অনুগ্রহ করে প্রদত্ত তথ্যসমূহ সঠিকভাবে যাচাই করে নিশ্চিত করুন",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueTextMuted
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Mosque Summary Box
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MosqueBgLight,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MosqueCardBorderSubtle),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "🕌 মসজিদ তথ্য",
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueEmeraldDark,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        SummaryRow(label = "মসজিদের নাম:", value = mosqueNameBn)
                                        SummaryRow(label = "ঠিকানা:", value = address)
                                        SummaryRow(label = "প্রতিষ্ঠা ও মেয়াদ:", value = "$establishedYear ($startYear - $endYear)")
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // President Summary Box
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MosqueBgLight,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MosqueCardBorderSubtle),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "👑 সভাপতির তথ্য",
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueEmeraldDark,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        SummaryRow(label = "সভাপতি নাম:", value = presidentNameBn)
                                        SummaryRow(label = "মোবাইল (লগইন):", value = presidentMobile)
                                        if (presidentEmail.isNotBlank()) {
                                            SummaryRow(label = "ইমেইল:", value = presidentEmail)
                                        }
                                        SummaryRow(label = "অ্যাসাইনড রোল:", value = "সভাপতি (Chief Admin)")
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { agreeToTerms = !agreeToTerms }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = agreeToTerms,
                                        onCheckedChange = { agreeToTerms = it },
                                        colors = CheckboxDefaults.colors(checkedColor = MosqueEmeraldPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "আমি প্রত্যয়ন করছি যে প্রদত্ত তথ্যসমূহ সঠিক এবং মসজিদের সেবায় আমানতদারি রক্ষা করব।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MosqueTextDark
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { step = 2 },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("সংশোধন")
                                    }

                                    Button(
                                        onClick = {
                                            if (!agreeToTerms) {
                                                errorMessage = "শর্তাবলীতে সম্মত হওয়া আবশ্যক।"
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
                                                    mosqueNameBn = mosqueNameBn,
                                                    mosqueNameEn = mosqueNameEn,
                                                    address = address,
                                                    establishedYear = establishedYear.toIntOrNull() ?: 2000,
                                                    startYear = startYear.toIntOrNull() ?: 2024,
                                                    endYear = endYear.toIntOrNull() ?: 2026
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
                                            .height(50.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
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

            Spacer(modifier = Modifier.height(20.dp))

            // Back to Login Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateBackToLogin() }
                    .padding(8.dp)
            ) {
                Text(
                    text = "ইতিমধ্যে অ্যাকাউন্ট আছে? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MosqueTextMuted
                )
                Text(
                    text = "লগইন করুন",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MosqueTextMuted)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MosqueTextDark)
    }
}

@Composable
fun RegistrationStepper(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
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
}

@Composable
fun StepItem(number: Int, title: String, isActive: Boolean, isCurrent: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(
                    if (isCurrent) MosqueEmeraldPrimary
                    else if (isActive) MosqueSuccessGreen
                    else Color(0xFFE2E8F0)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isActive && !isCurrent) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
                Text(
                    text = number.toString(),
                    color = if (isActive || isCurrent) Color.White else MosqueTextMuted,
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
            color = if (isCurrent) MosqueEmeraldDark else MosqueTextMuted
        )
    }
}

@Composable
fun StepDivider(isDone: Boolean) {
    Box(
        modifier = Modifier
            .width(50.dp)
            .height(2.dp)
            .background(if (isDone) MosqueSuccessGreen else Color(0xFFE2E8F0))
    )
}
