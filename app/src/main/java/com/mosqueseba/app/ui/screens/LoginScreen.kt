package com.mosqueseba.app.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Committee Login, 1: Musalli Access
    val scrollState = rememberScrollState()

    var committeeId by remember { mutableStateOf("MC-DHK-2024") }
    var mobile by remember { mutableStateOf("01711-223344") }
    var password by remember { mutableStateOf("123456") }
    var rememberMe by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showHelpDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MosqueBgLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MosqueGoldDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Official Branding Header
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(92.dp)
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp), spotColor = MosqueGold)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(listOf(MosqueGold, MosqueEmeraldPrimary)),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(6.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Mosque Seba Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "মসজিদ সেবা (Mosque Seba)",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MosqueEmeraldDark
            )

            Text(
                text = "মসজিদের খেদমতে আধুনিক প্রযুক্তি, পরিচালনায় স্বচ্ছ আস্থা",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MosqueEmeraldPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )

            RubElHizbDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Segmented Tab Selector
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, MosqueCardBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MosqueEmeraldPrimary,
                    indicator = { tabPositions ->
                        // Standard indicator
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0; errorMessage = null },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("কমিটি ও স্টাফ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; errorMessage = null },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("মুসুল্লি প্রবেশ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // Committee & Staff Login Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "কমিটি অ্যাকাউন্টে প্রবেশ করুন",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MosqueEmeraldDark
                        )
                        Text(
                            text = "মসজিদের দায়িত্বপ্রাপ্ত পদধারীদের জন্য অনুমোদিত পোর্টাল",
                            style = MaterialTheme.typography.bodySmall,
                            color = MosqueTextMuted
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = committeeId,
                            onValueChange = { committeeId = it; errorMessage = null },
                            label = { Text("কমিটি আইডি *") },
                            placeholder = { Text("MC-DHK-2024") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = MosqueEmeraldPrimary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it; errorMessage = null },
                            label = { Text("নিবন্ধিত মোবাইল নম্বর *") },
                            placeholder = { Text("01711223344") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MosqueEmeraldPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            label = { Text("গোপন পাসওয়ার্ড *") },
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

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { rememberMe = !rememberMe }
                            ) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(checkedColor = MosqueEmeraldPrimary)
                                )
                                Text("মনে রাখুন", style = MaterialTheme.typography.bodySmall, color = MosqueTextDark)
                            }

                            Text(
                                text = "সাহায্য প্রয়োজন?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MosqueEmeraldPrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable { showHelpDialog = true }
                                    .padding(4.dp)
                            )
                        }

                        errorMessage?.let { error ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MosqueUrgentRed.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = MosqueUrgentRed, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = error, color = MosqueUrgentRed, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (committeeId.isBlank() || mobile.isBlank() || password.isBlank()) {
                                    errorMessage = "সকল ঘর পূরণ করা বাধ্যতামূলক।"
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                coroutineScope.launch {
                                    val (success, err) = viewModel.loginWithCredentials(
                                        committeeId = committeeId,
                                        mobile = mobile,
                                        pass = password,
                                        rememberMe = rememberMe
                                    )
                                    isLoading = false
                                    if (success) {
                                        onLoginSuccess()
                                    } else {
                                        errorMessage = err ?: "মোবাইল নম্বর বা পাসওয়ার্ড সঠিক নয়।"
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("প্রবেশ করুন (লগইন)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        HorizontalDivider(color = MosqueCardBorderSubtle)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "মসজিদ কমিটি পরিষদ নির্বাচন (সরাসরি প্রবেশ):",
                            style = MaterialTheme.typography.labelSmall,
                            color = MosqueEmeraldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Official role access chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            QuickRoleChip(
                                label = "👑 সভাপতি",
                                onClick = {
                                    viewModel.quickLoginAsRole(UserRole.PRESIDENT, rememberMe)
                                    onLoginSuccess()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            QuickRoleChip(
                                label = "📝 সম্পাদক",
                                onClick = {
                                    viewModel.quickLoginAsRole(UserRole.GENERAL_SECRETARY, rememberMe)
                                    onLoginSuccess()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            QuickRoleChip(
                                label = "💰 কোষাধ্যক্ষ",
                                onClick = {
                                    viewModel.quickLoginAsRole(UserRole.TREASURER, rememberMe)
                                    onLoginSuccess()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            QuickRoleChip(
                                label = "🕌 পেশ ইমাম",
                                onClick = {
                                    viewModel.quickLoginAsRole(UserRole.IMAM, rememberMe)
                                    onLoginSuccess()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            QuickRoleChip(
                                label = "👥 সাধারণ মুসুল্লি",
                                onClick = {
                                    viewModel.quickLoginAsRole(UserRole.GENERAL_MEMBER, rememberMe)
                                    onLoginSuccess()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            } else {
                // Musalli Portal Direct Access Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MosqueEmeraldPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Mosque, contentDescription = null, tint = MosqueEmeraldPrimary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "উন্মুক্ত মুসুল্লি পোর্টাল",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MosqueEmeraldDark
                                )
                                Text(
                                    text = "পাসওয়ার্ড ছাড়াই ওয়াক্ত ও স্বচ্ছ হিসাব দেখুন",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueTextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MosqueBgLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "মুসুল্লি পোর্টালে যা যা সেবা পাবেন:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MosqueEmeraldDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("• ৫ ওয়াক্ত নামাজের সময়সূচি ও জামাত কাউন্টডাউন", fontSize = 12.sp, color = MosqueTextDark)
                                Text("• মসজিদের উন্মুক্ত তহবিল ও আয়-ব্যয়ের স্বচ্ছ সারসংক্ষেপ", fontSize = 12.sp, color = MosqueTextDark)
                                Text("• অফিসিয়াল নোটিশ বোর্ড ও ওয়াজ মাহফিলের খবর", fontSize = 12.sp, color = MosqueTextDark)
                                Text("• ব্যক্তিগত চাঁদা ইতিহাস ও ডিজিটাল কিউআর রসিদ", fontSize = 12.sp, color = MosqueTextDark)
                                Text("• আপৎকালীন সমাজকল্যাণ ও যাকাত সহায়তার আবেদন", fontSize = 12.sp, color = MosqueTextDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                viewModel.quickLoginAsRole(UserRole.GENERAL_MEMBER, rememberMe = true)
                                onLoginSuccess()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("সরাসরি মুসুল্লি হিসেবে প্রবেশ করুন", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Register Mosque Call to Action Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, MosqueCardBorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToRegister() }
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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MosqueGoldDeep.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AddBusiness, contentDescription = null, tint = MosqueGoldDeep)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "আপনার মসজিদ যুক্ত নেই?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MosqueEmeraldDark
                            )
                            Text(
                                text = "নতুন মসজিদ ও সভাপতি হিসেবে নিবন্ধন করুন",
                                fontSize = 11.sp,
                                color = MosqueTextMuted
                            )
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MosqueEmeraldPrimary)
                }
            }

            Spacer(modifier = Modifier.height(26.dp))
        }
    }

    // Help Dialog
    if (showHelpDialog) {
        Dialog(onDismissRequest = { showHelpDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "লগইন সহায়তা ও পুনরুদ্ধার",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MosqueEmeraldDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• পরিচালনা পরিষদ সদস্যগণ তাদের নিবন্ধিত মোবাইল নম্বর ও পাসওয়ার্ড দিয়ে সরাসরি লগইন করতে পারেন।\n• সভাপতি অ্যাকাউন্ট: 01711-223344 (পাসওয়ার্ড: 123456)\n• কোষাধ্যক্ষ অ্যাকাউন্ট: 01715-556677 (পাসওয়ার্ড: 123456)\n• সাধারণ সম্পাদক অ্যাকাউন্ট: 01912-445566 (পাসওয়ার্ড: 123456)\n• অথবা নিচের দ্রুত পদবি নির্বাচন বোতাম চেপে যেকোনো দায়িত্বে সরাসরি প্রবেশ করতে পারবেন।",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp,
                        color = MosqueTextDark
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showHelpDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("বুঝেছি")
                    }
                }
            }
        }
    }
}

@Composable
fun QuickRoleChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MosqueBgLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, MosqueCardBorderSubtle),
        modifier = modifier
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MosqueEmeraldDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        )
    }
}
