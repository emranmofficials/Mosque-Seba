package com.mosqueseba.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel

@Composable
fun LoginScreen(viewModel: MosqueViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Registration
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MosqueBgLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Branding Header
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MosqueEmeraldPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mosque,
                    contentDescription = null,
                    tint = MosqueGold,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "মসজিদ সেবা (Mosque Seba)",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MosqueEmeraldDark
            )
            Text(
                text = "মসজিদের খেদমতে আধুনিক প্রযুক্তি, পরিচালনায় স্বচ্ছ আস্থা",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MosqueEmeraldPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = MosqueEmeraldPrimary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("সদস্য লগইন", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("সভাপতি নিবন্ধন", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // Member Login Form
                MemberLoginForm(
                    onLogin = { committeeId, mobile, pass ->
                        viewModel.login(committeeId, mobile, pass)
                    },
                    onDemoLogin = { role ->
                        viewModel.setUserRole(role)
                        viewModel.login("MC-DHK-2024", "01711223344", "123456")
                    }
                )
            } else {
                // President & Mosque Registration Form
                PresidentRegistrationForm(
                    onSubmitted = {
                        selectedTab = 0
                    }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun MemberLoginForm(
    onLogin: (committeeId: String, mobile: String, pass: String) -> Unit,
    onDemoLogin: (UserRole) -> Unit
) {
    var committeeId by remember { mutableStateOf("MC-DHK-2024") }
    var mobile by remember { mutableStateOf("01711-223344") }
    var password by remember { mutableStateOf("123456") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = committeeId,
                onValueChange = { committeeId = it },
                label = { Text("কমিটি আইডি (যেমন: MC-DHK-2024) *") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = MosqueEmeraldPrimary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = mobile,
                onValueChange = { mobile = it },
                label = { Text("বাংলাদেশি মোবাইল নম্বর *") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MosqueEmeraldPrimary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("পাসওয়ার্ড *") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MosqueEmeraldPrimary) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (committeeId.isBlank() || mobile.isBlank() || password.isBlank()) {
                        errorMessage = "সকল ঘর পূরণ করা বাধ্যতামূলক।"
                    } else {
                        onLogin(committeeId, mobile, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary)
            ) {
                Text("প্রবেশ করুন (লগইন)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MosqueCardBorder)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "দ্রুত ডেমো রোল হিসেবে প্রবেশ করুন:",
                style = MaterialTheme.typography.labelSmall,
                color = MosqueTextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onDemoLogin(UserRole.PRESIDENT) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("সভাপতি", fontSize = 12.sp, color = MosqueEmeraldPrimary)
                }
                OutlinedButton(
                    onClick = { onDemoLogin(UserRole.TREASURER) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("কোষাধ্যক্ষ", fontSize = 12.sp, color = MosqueEmeraldPrimary)
                }
                OutlinedButton(
                    onClick = { onDemoLogin(UserRole.GENERAL_MEMBER) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("সদস্য", fontSize = 12.sp, color = MosqueEmeraldPrimary)
                }
            }
        }
    }
}

@Composable
fun PresidentRegistrationForm(onSubmitted: () -> Unit) {
    var step by remember { mutableIntStateOf(1) } // 1: President Info, 2: Mosque Info

    // President info
    var presidentNameBn by remember { mutableStateOf("") }
    var presidentEmail by remember { mutableStateOf("") }
    var presidentMobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Mosque info
    var mosqueNameBn by remember { mutableStateOf("") }
    var mosqueAddress by remember { mutableStateOf("") }
    var establishedYear by remember { mutableStateOf("1995") }
    var startYear by remember { mutableStateOf("2024") }
    var endYear by remember { mutableStateOf("2026") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (step == 1) "ধাপ ১: সভাপতির তথ্য" else "ধাপ ২: মসজিদের বিবরণ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )
                Text(
                    text = "ধাপ $step / ২",
                    style = MaterialTheme.typography.labelSmall,
                    color = MosqueGoldDeep,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (step == 1) {
                OutlinedTextField(
                    value = presidentNameBn,
                    onValueChange = { presidentNameBn = it },
                    label = { Text("সভাপতির পুরো নাম (বাংলায়) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = presidentMobile,
                    onValueChange = { presidentMobile = it },
                    label = { Text("মোবাইল নম্বর *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = presidentEmail,
                    onValueChange = { presidentEmail = it },
                    label = { Text("ইমেইল অ্যাড্রেস *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("গোপন পাসওয়ার্ড তৈরি করুন *") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (presidentNameBn.isNotBlank() && presidentMobile.isNotBlank()) {
                            step = 2
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("পরবর্তী ধাপ (মসজিদের তথ্য)", fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedTextField(
                    value = mosqueNameBn,
                    onValueChange = { mosqueNameBn = it },
                    label = { Text("মসজিদের নাম (বাংলায়) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = mosqueAddress,
                    onValueChange = { mosqueAddress = it },
                    label = { Text("মসজিদের পূর্ণ ঠিকানা ও এলাকা *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = establishedYear,
                    onValueChange = { establishedYear = it },
                    label = { Text("প্রতিষ্ঠা সাল") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startYear,
                        onValueChange = { startYear = it },
                        label = { Text("কমিটি শুরু") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endYear,
                        onValueChange = { endYear = it },
                        label = { Text("কমিটি মেয়াদ শেষ") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { step = 1 },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("পেছনে")
                    }

                    Button(
                        onClick = {
                            if (mosqueNameBn.isNotBlank() && mosqueAddress.isNotBlank()) {
                                onSubmitted()
                            }
                        },
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("নিবন্ধন সম্পন্ন করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
