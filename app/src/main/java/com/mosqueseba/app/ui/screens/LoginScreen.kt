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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
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

    var committeeId by remember { mutableStateOf("") }
    var presidentEmail by remember { mutableStateOf("") }
    var presidentPassword by remember { mutableStateOf("") }
    var memberMobile by remember { mutableStateOf("") }
    var memberPassword by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val isDark = LocalIsDarkTheme.current

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
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RubElHizbDivider(title = "অ্যাকাউন্টে প্রবেশ")

            // Segmented Pill Tab Selector
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
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
                                .background(if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft)
                                .border(1.dp, (if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary).copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Default.VerifiedUser else Icons.Default.Badge,
                                contentDescription = null,
                                tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (selectedTab == 0) "সভাপতি অ্যাকাউন্টে প্রবেশ" else "সদস্য ও স্টাফ লগইন",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark
                            )
                            Text(
                                text = if (selectedTab == 0) {
                                    "শুধুমাত্র সভাপতি ইমেইল ও পাসওয়ার্ড দিয়ে প্রবেশ করবেন"
                                } else {
                                    "নিবন্ধিত মোবাইল নম্বর এবং শেষ ৬টি সংখ্যা পাসওয়ার্ড হিসেবে দিন"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    MosqueStandardTextField(
                        value = committeeId,
                        onValueChange = { committeeId = it; errorMessage = null },
                        label = "কমিটি আইডি *",
                        placeholder = "কমিটি আইডি লিখুন",
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        modifier = Modifier.testTag("login_committee_id_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (selectedTab == 0) {
                        MosqueStandardTextField(
                            value = presidentEmail,
                            onValueChange = { presidentEmail = it; errorMessage = null },
                            label = "সভাপতির ইমেইল ঠিকানা *",
                            placeholder = "সভাপতির ইমেইল ঠিকানা লিখুন",
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.testTag("login_president_email_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        MosqueStandardTextField(
                            value = presidentPassword,
                            onValueChange = { presidentPassword = it; errorMessage = null },
                            label = "গোপন পাসওয়ার্ড *",
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "পাসওয়ার্ড দেখুন/লুকান"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.testTag("login_president_password_input")
                        )
                    } else {
                        MosqueStandardTextField(
                            value = memberMobile,
                            onValueChange = { memberMobile = it; errorMessage = null },
                            label = "নিবন্ধিত মোবাইল নম্বর *",
                            placeholder = "017XXXXXXXX",
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.testTag("login_member_mobile_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        MosqueStandardTextField(
                            value = memberPassword,
                            onValueChange = { memberPassword = it; errorMessage = null },
                            label = "পাসওয়ার্ড *",
                            placeholder = "পাসওয়ার্ড দিন",
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "পাসওয়ার্ড দেখুন/লুকান"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.testTag("login_member_password_input")
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
                                colors = CheckboxDefaults.colors(checkedColor = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary)
                            )
                            Text("মনে রাখুন", style = MaterialTheme.typography.bodySmall, color = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary)
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
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = null,
                                tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "সাহায্য প্রয়োজন?",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
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
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) MosqueEmeraldLight else MosqueEmeraldPrimary)
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
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Register Mosque Call to Action Banner (President Only)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) MosqueNightSurfaceVariant else MosqueGoldSoft,
                border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderGold),
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
                                color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark
                            )
                            Text(
                                text = "শুধুমাত্র সভাপতি নতুন মসজিদ নিবন্ধন করতে পারবেন",
                                fontSize = 11.sp,
                                color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                            )
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = if (isDark) MosqueNightSurface else MosqueEmeraldSoft,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "নিবন্ধনে যান",
                                tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary,
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
                                .background(if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = null,
                                tint = if (isDark) MosqueGreenAccent else MosqueEmeraldPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "লগইন সহায়তা ও নিয়মাবলি",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = if (isDark) MosqueNightBorder else MosqueCardBorder)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "• নতুন মসজিদ: আপনার মসজিদ এখনও নিবন্ধিত না থাকলে নিচে 'নতুন মসজিদ নিবন্ধন করুন' বাটনে ট্যাপ করে সম্পূর্ণ বিনামূল্যে নতুন মসজিদ ও সভাপতির অ্যাকাউন্ট নিবন্ধন করুন।\n• সভাপতি লগইন: মসজিদ নিবন্ধনের সময় পাওয়া কমিটি আইডি, সভাপতির ইমেইল ও নির্ধারিত পাসওয়ার্ড দিয়ে প্রবেশ করুন।\n• সাধারণ সদস্য ও দায়িত্বশীল লগইন: কমিটি আইডি, সদস্য হিসেবে যুক্ত থাকার সময় ব্যবহৃত মোবাইল নম্বর এবং আপনার পাসওয়ার্ড দিয়ে প্রবেশ করুন।\n• পাসওয়ার্ড ভুলে গেলে কমিটির সভাপতি বা অ্যাডমিনের সহায়তায় পাসওয়ার্ড রিসেট করা যাবে।",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp,
                        color = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showHelpDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("login_help_close_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) MosqueEmeraldLight else MosqueEmeraldPrimary),
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
    val isDark = LocalIsDarkTheme.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) (if (isDark) MosqueEmeraldLight else MosqueEmeraldPrimary) else Color.Transparent,
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
                tint = if (selected) MosqueGoldLight else if (isDark) MosqueNightTextSecondary else MosqueTextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 13.sp,
                color = if (selected) Color.White else if (isDark) MosqueNightTextSecondary else MosqueTextSecondary,
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
        border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
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
                    tint = if (isDark) MosqueGoldLight else MosqueEmeraldPrimary,
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
