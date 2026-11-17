package com.mosqueseba.app.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mosqueseba.app.data.remote.supabase.SyncResult
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel
import kotlinx.coroutines.launch

@Composable
fun CloudSyncDialog(
    viewModel: MosqueViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = LocalIsDarkTheme.current

    val initialCreds = remember { viewModel.getSupabaseCredentials() }
    var urlText by remember { mutableStateOf(initialCreds.first) }
    var keyText by remember { mutableStateOf(initialCreds.second) }
    var isKeyVisible by remember { mutableStateOf(false) }

    var isTestingConnection by remember { mutableStateOf(false) }
    var testResultMsg by remember { mutableStateOf<String?>(null) }
    var isTestSuccess by remember { mutableStateOf<Boolean?>(null) }

    var isSyncing by remember { mutableStateOf(false) }
    var syncResult by remember { mutableStateOf<SyncResult?>(null) }

    var isConfigured by remember { mutableStateOf(viewModel.isSupabaseConfigured()) }
    var lastSyncTime by remember { mutableStateOf(viewModel.getLastCloudSyncTime()) }
    var showSetupGuide by remember { mutableStateOf(!isConfigured) }

    Dialog(
        onDismissRequest = { if (!isSyncing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = if (isDark) MosqueNightSurface else MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isConfigured) MosqueEmeraldPrimary.copy(alpha = 0.15f)
                                    else MosqueWarningAmber.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isConfigured) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = if (isConfigured) MosqueEmeraldPrimary else MosqueWarningAmber,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ক্লাউড ডেটাবেজ ও সিঙ্ক",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary
                            )
                            Text(
                                text = if (isConfigured) "Supabase ক্লাউড সক্রিয়" else "অফলাইন লোকাল মোড সক্রিয়",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isConfigured) MosqueSuccessGreen else MosqueWarningAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSyncing,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "বন্ধ করুন",
                            tint = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Last Sync Timestamp Banner
                if (!lastSyncTime.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (isDark) MosqueGoldLight else MosqueEmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "সর্বশেষ সফল সিঙ্ক: $lastSyncTime",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) MosqueGoldLight else MosqueEmeraldPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Expandable Setup Guide Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                    border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSetupGuide = !showSetupGuide }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = MosqueInfoBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "৩ মিনিটে ফ্রি Supabase ব্যাকএন্ড সেটআপ গাইড",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary
                                )
                            }
                            Icon(
                                imageVector = if (showSetupGuide) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        AnimatedVisibility(visible = showSetupGuide) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                Text(
                                    text = "১. supabase.com এ গিয়ে বিনামূল্যে New Project তৈরি করুন।\n" +
                                            "২. ড্যাশবোর্ডের 'SQL Editor' এ গিয়ে প্রজেক্টের 'supabase/full_schema_setup.sql' ফাইলের কোড পেস্ট করে Run করুন।\n" +
                                            "৩. Project Settings -> API থেকে Project URL এবং anon API Key কপি করে নিচে পেস্ট করুন।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Credentials Input Fields
                Text(
                    text = "Supabase Project URL *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = urlText,
                    onValueChange = {
                        urlText = it
                        testResultMsg = null
                    },
                    placeholder = { Text("https://xyzcompany.supabase.co", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MosqueEmeraldPrimary,
                        unfocusedBorderColor = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            clipboardManager.getText()?.text?.let { urlText = it.trim() }
                        }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "পেস্ট", modifier = Modifier.size(18.dp))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Supabase anon Public API Key *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = keyText,
                    onValueChange = {
                        keyText = it
                        testResultMsg = null
                    },
                    placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MosqueEmeraldPrimary,
                        unfocusedBorderColor = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
                    ),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "দেখুন",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(onClick = {
                                clipboardManager.getText()?.text?.let { keyText = it.trim() }
                            }) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "পেস্ট", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action Row: Save & Test Connection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (urlText.isBlank() || keyText.isBlank()) {
                                Toast.makeText(context, "URL এবং API Key উভয়ই প্রদান করুন", Toast.LENGTH_SHORT).show()
                                return@OutlinedButton
                            }
                            viewModel.saveSupabaseCredentials(urlText, keyText)
                            isConfigured = viewModel.isSupabaseConfigured()
                            coroutineScope.launch {
                                isTestingConnection = true
                                testResultMsg = null
                                val test = viewModel.testCloudConnection()
                                isTestingConnection = false
                                if (test.isSuccess) {
                                    isTestSuccess = true
                                    testResultMsg = "কানেকশন সফল! ডেটাবেজের সাথে যোগাযোগ স্থাপন হয়েছে।"
                                } else {
                                    isTestSuccess = false
                                    testResultMsg = test.exceptionOrNull()?.message ?: "কানেকশন ব্যর্থ"
                                }
                            }
                        },
                        enabled = !isTestingConnection && !isSyncing,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("টেস্ট কানেকশন", fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.saveSupabaseCredentials(urlText, keyText)
                            isConfigured = viewModel.isSupabaseConfigured()
                            Toast.makeText(context, "ক্রেডেনশিয়ালস সংরক্ষিত হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("সংরক্ষণ", fontSize = 12.sp)
                    }
                }

                // Test Result Feedback
                if (!testResultMsg.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isTestSuccess == true) MosqueSuccessGreen.copy(alpha = 0.12f) else MosqueUrgentRed.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = testResultMsg ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isTestSuccess == true) MosqueSuccessGreen else MosqueUrgentRed,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle)
                Spacer(modifier = Modifier.height(14.dp))

                // Primary Sync Action Button
                Button(
                    onClick = {
                        if (urlText.isNotBlank() && keyText.isNotBlank()) {
                            viewModel.saveSupabaseCredentials(urlText, keyText)
                            isConfigured = true
                        }
                        isSyncing = true
                        syncResult = null
                        coroutineScope.launch {
                            viewModel.performCloudSync { result ->
                                isSyncing = false
                                syncResult = result
                                if (result.isSuccess) {
                                    lastSyncTime = result.syncedAt
                                }
                            }
                        }
                    },
                    enabled = !isSyncing,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MosqueEmeraldPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("ক্লাউডে সিঙ্ক হচ্ছে...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("এখনই ক্লাউডে সিঙ্ক করুন", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                // Sync Result Breakdown
                if (syncResult != null) {
                    val res = syncResult!!
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (res.isSuccess) {
                            if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft
                        } else {
                            MosqueUrgentRed.copy(alpha = 0.12f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (res.isSuccess) MosqueSuccessGreen.copy(alpha = 0.3f) else MosqueUrgentRed.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (res.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (res.isSuccess) MosqueSuccessGreen else MosqueUrgentRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = res.messageBn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (res.isSuccess) {
                                        if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark
                                    } else MosqueUrgentRed
                                )
                            }

                            if (res.isSuccess) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "সিঙ্ককৃত ডেটার হিসাব:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) MosqueGoldLight else MosqueEmeraldPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    SyncPillBadge("সদস্য: ${toBengaliDigits(res.membersCount.toString())}")
                                    SyncPillBadge("ভাউচার: ${toBengaliDigits(res.transactionsCount.toString())}")
                                    SyncPillBadge("নোটিশ: ${toBengaliDigits(res.noticesCount.toString())}")
                                    SyncPillBadge("চাঁদা: ${toBengaliDigits(res.chandaCount.toString())}")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Offline Guarantee Note
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (isDark) MosqueGoldLight else MosqueEmeraldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "১০০% অফলাইন প্রস্তুত: ইন্টারনেট সংযোগ বিচ্ছিন্ন থাকলেও আপনার সমস্ত ডেটা ফোনে সর্বদা সুরক্ষিত থাকে।",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncPillBadge(text: String) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isDark) MosqueNightSurface else MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldDark,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
