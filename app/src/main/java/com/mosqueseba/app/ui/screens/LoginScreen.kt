package com.mosqueseba.app.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mosqueseba.app.R
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.*
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: MosqueViewModel,
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: President Email Login, 1: Member/Staff Mobile Login
    val scrollState = rememberScrollState()

    var committeeId by remember { mutableStateOf("MC-DHK-2024") }
    var presidentEmail by remember { mutableStateOf("rafiqul.islam@example.com") }
    var presidentPassword by remember { mutableStateOf("123456") }
    var memberMobile by remember { mutableStateOf("01715-556677") }
    var memberPassword by remember { mutableStateOf("556677") }
    var rememberMe by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val isDark = LocalIsDarkTheme.current
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MosqueEmeraldPrimary,
        unfocusedBorderColor = if (isDark) MosqueNightBorder else MosqueCardBorder,
        focusedLabelColor = MosqueEmeraldPrimary,
        unfocusedLabelColor = MosqueTextSecondary,
        cursorColor = MosqueEmeraldPrimary,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = if (isDark) MosqueNightSurfaceVariant.copy(alpha = 0.5f) else MosqueBgLight.copy(alpha = 0.45f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sanctuary Header Banner matching RegistrationScreen & GlobalHeader
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
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MosqueGoldLight,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Official Branding Emblem
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(84.dp)
                            .shadow(elevation = 10.dp, shape = RoundedCornerShape(22.dp), spotColor = MosqueGold)
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(listOf(MosqueGold, MosqueGoldLight, MosqueEmeraldLight)),
                                shape = RoundedCornerShape(22.dp)
                            )
                            .clip(RoundedCornerShape(22.dp))
                            .background(MosqueEmeraldDark)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "মসজিদ সেবা লোগো",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "মসজিদ সেবা",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "মসজিদের খেদমতে আধুনিক প্রযুক্তি, পরিচালনায় স্বচ্ছ আস্থা",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MosqueGoldLight.copy(alpha = 0.88f),
                        textAlign = TextAlign.Center
                    )
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

        // Scrollable Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RubElHizbDivider(title = "অ্যাকাউন্টে প্রবেশ")

            // Segmented Pill Tab Selector
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderGold),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LoginRoleTabButton(
                        title = "সভাপতি (ইমেইল)",
                        icon = Icons.Default.AdminPanelSettings,
                        selected = selectedTab == 0,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("login_tab_president"),
                        onClick = {
                            selectedTab = 0
                            errorMessage = null
                        }
                    )
                    LoginRoleTabButton(
                        title = "সদস্য ও স্টাফ",
                        icon = Icons.Default.People,
                        selected = selectedTab == 1,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("login_tab_member"),
                        onClick = {
                            selectedTab = 1
                            errorMessage = null
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Login Mehrab Arch Card (Tab 0: President Email+Password, Tab 1: Other Users Mobile+Last 6 Digits)
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
                                .background(MosqueEmeraldSoft)
                                .border(1.dp, MosqueEmeraldPrimary.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Default.VerifiedUser else Icons.Default.Badge,
                                contentDescription = null,
                                tint = MosqueEmeraldPrimary,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (selectedTab == 0) "সভাপতি অ্যাকাউন্টে প্রবেশ" else "সদস্য ও স্টাফ লগইন",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MosqueEmeraldDark
                            )
                            Text(
                                text = if (selectedTab == 0) {
                                    "শুধুমাত্র সভাপতি ইমেইল ও পাসওয়ার্ড দিয়ে প্রবেশ করবেন"
                                } else {
                                    "নিবন্ধিত মোবাইল নম্বর এবং শেষ ৬টি সংখ্যা পাসওয়ার্ড হিসেবে দিন"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MosqueTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = committeeId,
                        onValueChange = { committeeId = it; errorMessage = null },
                        label = { Text("কমিটি আইডি *") },
                        placeholder = { Text("কমিটি আইডি লিখুন") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = MosqueEmeraldPrimary) },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_committee_id_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (selectedTab == 0) {
                        OutlinedTextField(
                            value = presidentEmail,
                            onValueChange = { presidentEmail = it; errorMessage = null },
                            label = { Text("সভাপতির ইমেইল ঠিকানা *") },
                            placeholder = { Text("সভাপতির ইমেইল ঠিকানা লিখুন") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MosqueEmeraldPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_president_email_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = presidentPassword,
                            onValueChange = { presidentPassword = it; errorMessage = null },
                            label = { Text("গোপন পাসওয়ার্ড *") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MosqueEmeraldPrimary) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "পাসওয়ার্ড দেখুন/লুকান",
                                        tint = MosqueTextSecondary
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_president_password_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        OutlinedTextField(
                            value = memberMobile,
                            onValueChange = { memberMobile = it; errorMessage = null },
                            label = { Text("নিবন্ধিত মোবাইল নম্বর *") },
                            placeholder = { Text("01715-556677") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MosqueEmeraldPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_member_mobile_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = memberPassword,
                            onValueChange = { memberPassword = it; errorMessage = null },
                            label = { Text("পাসওয়ার্ড (মোবাইলের শেষ ৬ সংখ্যা) *") },
                            placeholder = { Text("যেমন: 556677") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MosqueEmeraldPrimary) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "পাসওয়ার্ড দেখুন/লুকান",
                                        tint = MosqueTextSecondary
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_member_password_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { rememberMe = !rememberMe }
                                .padding(end = 8.dp)
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = MosqueEmeraldPrimary)
                            )
                            Text("মনে রাখুন", style = MaterialTheme.typography.bodySmall, color = MosqueTextPrimary)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showHelpDialog = true }
                                .padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = MosqueEmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "সাহায্য প্রয়োজন?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MosqueEmeraldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MosqueUrgentRed.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MosqueUrgentRed.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = "ত্রুটি", tint = MosqueUrgentRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = error, color = MosqueUrgentRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (selectedTab == 0) {
                                if (committeeId.isBlank() || presidentEmail.isBlank() || presidentPassword.isBlank()) {
                                    errorMessage = "কমিটি আইডি, ইমেইল ও পাসওয়ার্ড পূরণ করা বাধ্যতামূলক।"
                                    return@Button
                                }
                            } else {
                                if (committeeId.isBlank() || memberMobile.isBlank() || memberPassword.isBlank()) {
                                    errorMessage = "কমিটি আইডি, মোবাইল নম্বর ও ৬ সংখ্যার পাসওয়ার্ড পূরণ করুন।"
                                    return@Button
                                }
                            }
                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                val (success, err) = if (selectedTab == 0) {
                                    viewModel.loginPresidentWithEmail(
                                        committeeId = committeeId,
                                        email = presidentEmail,
                                        pass = presidentPassword,
                                        rememberMe = rememberMe
                                    )
                                } else {
                                    viewModel.loginMemberWithMobile(
                                        committeeId = committeeId,
                                        mobile = memberMobile,
                                        pass = memberPassword,
                                        rememberMe = rememberMe
                                    )
                                }
                                isLoading = false
                                if (success) {
                                    onLoginSuccess()
                                } else {
                                    errorMessage = err ?: "লগইন তথ্য সঠিক নয়।"
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = null,
                                tint = MosqueGoldLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("প্রবেশ করুন (লগইন)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    RubElHizbDivider(title = "কমিটি পরিষদ (সরাসরি প্রবেশ)")

                    // Official role access chips (Option B)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickRoleChip(
                            label = "সভাপতি",
                            icon = UserRole.PRESIDENT.roleIcon(),
                            onClick = {
                                viewModel.quickLoginAsRole(UserRole.PRESIDENT, rememberMe)
                                onLoginSuccess()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        QuickRoleChip(
                            label = "সাধারণ সম্পাদক",
                            icon = UserRole.GENERAL_SECRETARY.roleIcon(),
                            onClick = {
                                viewModel.quickLoginAsRole(UserRole.GENERAL_SECRETARY, rememberMe)
                                onLoginSuccess()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        QuickRoleChip(
                            label = "কোষাধ্যক্ষ",
                            icon = UserRole.TREASURER.roleIcon(),
                            onClick = {
                                viewModel.quickLoginAsRole(UserRole.TREASURER, rememberMe)
                                onLoginSuccess()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickRoleChip(
                            label = "পেশ ইমাম",
                            icon = UserRole.IMAM.roleIcon(),
                            onClick = {
                                viewModel.quickLoginAsRole(UserRole.IMAM, rememberMe)
                                onLoginSuccess()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        QuickRoleChip(
                            label = "সাধারণ মুসল্লি",
                            icon = UserRole.GENERAL_MEMBER.roleIcon(),
                            onClick = {
                                viewModel.quickLoginAsRole(UserRole.GENERAL_MEMBER, rememberMe)
                                onLoginSuccess()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    var showAllCommitteeRoles by remember { mutableStateOf(false) }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showAllCommitteeRoles = !showAllCommitteeRoles }
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showAllCommitteeRoles) "অন্যান্য কমিটি পদবি লুকান" else "সব কমিটি পদবি (সহ-পদ, সম্পাদকীয়, উপদেষ্টা) দেখুন",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MosqueEmeraldPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (showAllCommitteeRoles) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MosqueEmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    AnimatedVisibility(visible = showAllCommitteeRoles) {
                        val extendedRoles = remember {
                            listOf(
                                UserRole.VICE_PRESIDENT,
                                UserRole.ASSISTANT_SECRETARY,
                                UserRole.ASSISTANT_TREASURER,
                                UserRole.ORGANIZING_SECRETARY,
                                UserRole.PUBLICITY_SECRETARY,
                                UserRole.OFFICE_SECRETARY,
                                UserRole.WELFARE_EDUCATION_SECRETARY,
                                UserRole.EXECUTIVE_MEMBER,
                                UserRole.ADVISORY_BOARD,
                                UserRole.MUAZZIN,
                                UserRole.KHADEM
                            )
                        }
                        Column(
                            modifier = Modifier.padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            extendedRoles.chunked(2).forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    pair.forEach { role ->
                                        QuickRoleChip(
                                            label = role.titleBn.substringBefore(" ("),
                                            icon = role.roleIcon(),
                                            onClick = {
                                                viewModel.quickLoginAsRole(role, rememberMe)
                                                onLoginSuccess()
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (pair.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Register Mosque Call to Action Banner (President Only)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MosqueGoldSoft,
                border = BorderStroke(1.dp, MosqueCardBorderGold),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigateToRegister() }
                    .testTag("login_register_mosque_banner")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MosqueEmeraldPrimary)
                                .border(1.5.dp, MosqueGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddBusiness,
                                contentDescription = null,
                                tint = MosqueGoldLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "আপনার মসজিদ যুক্ত নেই?",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = MosqueEmeraldDark
                            )
                            Text(
                                text = "শুধুমাত্র সভাপতি নতুন মসজিদ নিবন্ধন করতে পারবেন",
                                fontSize = 11.sp,
                                color = MosqueTextSecondary
                            )
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = MosqueEmeraldSoft,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "নিবন্ধনে যান",
                                tint = MosqueEmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Help Dialog matching Mehrab Arch style
    if (showHelpDialog) {
        Dialog(onDismissRequest = { showHelpDialog = false }) {
            MehrabArchCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MosqueEmeraldSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = MosqueEmeraldPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "লগইন সহায়তা ও নিয়মাবলি",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MosqueEmeraldDark
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MosqueCardBorder)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "• সভাপতি লগইন: শুধুমাত্র কমিটি আইডি, ইমেইল ও পাসওয়ার্ড দিয়ে প্রবেশ করবেন (ডিফল্ট: rafiqul.islam@example.com / 123456)।\n• অন্যান্য ইউজার লগইন: কমিটি আইডি, নিবন্ধিত মোবাইল নম্বর এবং মোবাইল নম্বরের শেষ ৬টি সংখ্যা পাসওয়ার্ড হিসেবে ব্যবহার করবেন।\n• উদাহরণ (কোষাধ্যক্ষ): 01715-556677 (পাসওয়ার্ড: 556677)\n• উদাহরণ (সাধারণ সম্পাদক): 01912-445566 (পাসওয়ার্ড: 445566)\n• সভাপতি বা দায়িত্বপ্রাপ্ত ইউজার সদস্য যুক্ত করলেই সেই সদস্য ইউজার হিসেবে যুক্ত হবেন।",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp,
                        color = MosqueTextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showHelpDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("login_help_close_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("বুঝেছি", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginRoleTabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MosqueEmeraldPrimary else Color.Transparent,
        border = if (selected) BorderStroke(1.dp, MosqueGold.copy(alpha = 0.7f)) else null,
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MosqueGoldLight else MosqueTextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 13.sp,
                color = if (selected) Color.White else MosqueTextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun QuickRoleChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
        border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderGold),
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MosqueEmeraldPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) MosqueGoldLight else MosqueEmeraldDark,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
