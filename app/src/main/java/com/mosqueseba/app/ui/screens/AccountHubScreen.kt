package com.mosqueseba.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mosqueseba.app.BuildConfig
import com.mosqueseba.app.R
import com.mosqueseba.app.data.local.entity.MosqueEntity
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.*
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel

@Composable
fun AccountHubScreen(
    viewModel: MosqueViewModel,
    mosque: MosqueEntity?,
    onBack: () -> Unit,
    onOpenMosqueProfile: () -> Unit,
    onOpenMyProfile: (() -> Unit)? = null,
    onOpenOnboarding: () -> Unit = {},
    onLogout: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val currentRole by viewModel.currentUserRole.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val showHijriBanner by viewModel.showHijriBanner.collectAsState()
    val rememberMeEnabled by viewModel.rememberMeEnabled.collectAsState()
    val darkModeEnabled by viewModel.darkModeEnabled.collectAsState()
    val darkModeFollowSystem by viewModel.darkModeFollowSystem.collectAsState()

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showHelplineGuideDialog by remember { mutableStateOf(false) }
    var showAboutAppDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showCloudSyncDialog by remember { mutableStateOf(false) }
    var showPurgeDemoConfirmDialog by remember { mutableStateOf(false) }

    val currentMember = remember(currentRole, allMembers) {
        val savedId = viewModel.sessionManager.getSavedMemberId()
        val savedRole = viewModel.sessionManager.getSavedRole()
        allMembers.firstOrNull { it.id == savedId && currentRole == savedRole }
            ?: allMembers.firstOrNull { it.roleCode.equals(currentRole.code, ignoreCase = true) }
    }
    val displayName = currentMember?.nameBn ?: viewModel.sessionManager.getSavedMemberName()?.takeIf { it.isNotBlank() } ?: currentRole.titleBn
    val memberMobile = currentMember?.mobile?.takeIf { it.isNotBlank() } ?: "মোবাইল নম্বর সংযুক্ত নেই"
    val memberCode = currentMember?.memberCode?.takeIf { it.isNotBlank() } ?: currentRole.code.uppercase()
    val userAvatarBitmap = remember(currentMember?.photoUrl) {
        loadLocalThumbnail(currentMember?.photoUrl, 256)
    }

    val mosqueNameBn = mosque?.nameBn ?: "মসজিদ সেবা"
    val mosqueCommitteeId = mosque?.committeeId ?: "কমিটি আইডি সংযুক্ত নেই"
    val mosqueAddress = mosque?.address ?: "ঠিকানা যুক্ত করা হয়নি"
    val mosqueLogoBitmap = remember(mosque?.logoUrl) {
        loadLocalThumbnail(mosque?.logoUrl, 256)
    }
    val mosqueEstBn = mosque?.establishedYear?.let { toBengaliDigits(it.toString()) } ?: "—"

    val helplineContact = remember(allMembers) {
        allMembers.firstOrNull { it.roleCode.equals("general_secretary", ignoreCase = true) && it.mobile.isNotBlank() }
            ?: allMembers.firstOrNull { it.roleCode.equals("president", ignoreCase = true) && it.mobile.isNotBlank() }
    }
    val helplineNumber = helplineContact?.mobile?.takeIf { it.isNotBlank() } ?: "কমিটির সাথে যোগাযোগ করুন"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
        ) {
            // 0. Header Surface Control Bar (Integrated into LazyColumn for full-page scroll)
            item(key = "account_header") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("account_hub_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "ফিরে যান",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "আমার প্রোফাইল ও সেটিংস",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "ব্যক্তিগত তথ্য, নিরাপত্তা ও অ্যাপ নিয়ন্ত্রণ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = currentRole.titleBn,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Section 1: প্রোফাইল পরিচিতি (Profiles)
            item(key = "section_profiles_title") {
                AccountSectionTitle(title = "প্রোফাইল পরিচিতি", icon = Icons.Default.AccountCircle)
            }

            // 1.1: আমার প্রোফাইল (User Identity Card)
            item(key = "user_identity_card") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (onOpenMyProfile != null) {
                                onOpenMyProfile()
                            } else {
                                Toast.makeText(context, "প্রোফাইল লোড হচ্ছে...", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .testTag("account_hub_my_profile_card")
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // User Avatar
                            Box(
                                modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft)
                                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (userAvatarBitmap != null) {
                                    Image(
                                        bitmap = userAvatarBitmap,
                                        contentDescription = displayName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Text(
                                        text = displayName.take(1),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "আমার প্রোফাইল",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = currentRole.titleBn,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "কোড: $memberCode • $memberMobile",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "প্রোফাইল দেখুন",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Bottom strip
                        HorizontalDivider(color = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isDark) MosqueNightSurfaceVariant.copy(alpha = 0.5f) else MosqueEmeraldSoft.copy(alpha = 0.35f))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ব্যক্তিগত বিবরণ, চাঁদা ও হাজিরার হিসাব",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "সম্পূর্ণ প্রোফাইল দেখুন ›",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Section 2: অ্যাপ সেটিংস (App Settings)
            item(key = "section_settings_title") {
                AccountSectionTitle(title = "অ্যাপ সেটিংস", icon = Icons.Default.Tune)
            }

            item(key = "settings_card") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        DarkModePreferenceRow(
                            isDark = isDark,
                            followSystem = darkModeFollowSystem,
                            darkEnabled = darkModeEnabled,
                            onFollowSystem = {
                                viewModel.setDarkModeFollowSystem()
                                Toast.makeText(context, "সিস্টেম থিম অনুসরণ করা হচ্ছে।", Toast.LENGTH_SHORT).show()
                            },
                            onLight = {
                                viewModel.setDarkModeEnabled(false)
                                Toast.makeText(context, "লাইট মোড চালু করা হয়েছে।", Toast.LENGTH_SHORT).show()
                            },
                            onDark = {
                                viewModel.setDarkModeEnabled(true)
                                Toast.makeText(context, "ডার্ক মোড চালু করা হয়েছে।", Toast.LENGTH_SHORT).show()
                            }
                        )
                        HorizontalDivider(
                            color = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                        AccountSettingSwitchRow(
                            icon = Icons.Default.NotificationsActive,
                            iconColor = MosqueEmeraldPrimary,
                            title = "নোটিফিকেশন ও সতর্কতা ব্যাজ",
                            subtitle = "অপেক্ষমাণ ভাউচার, নোটিশ ও সভার সংখ্যা ব্যাজ প্রদর্শন",
                            checked = notificationsEnabled,
                            testTag = "setting_notifications_switch",
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                        )
                        HorizontalDivider(
                            color = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                        AccountSettingSwitchRow(
                            icon = Icons.Default.CalendarMonth,
                            iconColor = MosqueGoldDeep,
                            title = "ড্যাশবোর্ডে হিজরি তারিখ প্রদর্শন",
                            subtitle = "হোম স্ক্রিনে হিজরি ও বাংলা তারিখ ব্যানার চালু রাখুন",
                            checked = showHijriBanner,
                            testTag = "setting_hijri_banner_switch",
                            onCheckedChange = { viewModel.setShowHijriBanner(it) }
                        )
                        HorizontalDivider(
                            color = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                        AccountSettingSwitchRow(
                            icon = Icons.Default.VerifiedUser,
                            iconColor = MosqueInfoBlue,
                            title = "স্বয়ংক্রিয় লগইন সেশন (মনে রাখুন)",
                            subtitle = "অ্যাপ বন্ধ করলেও পরবর্তী প্রবেশে সেশন সংরক্ষণ রাখুন",
                            checked = rememberMeEnabled,
                            testTag = "setting_remember_me_switch",
                            onCheckedChange = { viewModel.setRememberMeEnabled(it) }
                        )
                    }
                }
            }

            // Section 3: নিরাপত্তা ও ডেটা সিঙ্ক (Security & Data Sync)
            item(key = "section_security_title") {
                AccountSectionTitle(title = "নিরাপত্তা ও ডেটা সিঙ্ক", icon = Icons.Default.Security)
            }

            item(key = "security_card") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        AccountActionRow(
                            icon = Icons.Default.LockReset,
                            iconColor = MosqueGoldDeep,
                            title = "পাসওয়ার্ড পরিবর্তন করুন",
                            subtitle = "আপনার অ্যাকাউন্টের বর্তমান লগইন পাসওয়ার্ড আপডেট করুন",
                            testTag = "account_hub_change_password",
                            onClick = { showChangePasswordDialog = true }
                        )
                        HorizontalDivider(
                            color = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                        AccountActionRow(
                            icon = Icons.Default.Sync,
                            iconColor = MosqueEmeraldPrimary,
                            title = "অফলাইন ডেটা ও ক্লাউড সিঙ্ক",
                            subtitle = "ডিভাইসে সম্পূর্ণ ডেটা সংরক্ষিত ও রিয়েলটাইমে ক্লাউডে সিঙ্কড",
                            badgeText = if (viewModel.isSupabaseConfigured()) "সক্রিয়" else "অফলাইন",
                            testTag = "account_hub_data_sync",
                            onClick = {
                                showCloudSyncDialog = true
                            }
                        )
                    }
                }
            }

            // Section 4: হেল্প লাইন ও সহায়তা (Helpline & Support)
            item(key = "section_helpline_title") {
                AccountSectionTitle(title = "হেল্প লাইন ও সহায়তা", icon = Icons.Default.SupportAgent)
            }

            item(key = "helpline_card") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        AccountActionRow(
                            icon = Icons.Default.Call,
                            iconColor = MosqueEmeraldPrimary,
                            title = "হেল্প লাইন কল ($helplineNumber)",
                            subtitle = helplineContact?.let { "কমিটি সহায়তা: ${it.nameBn}" } ?: "মসজিদ কমিটি ও কারিগরি সহায়তা ডেস্ক",
                            testTag = "account_hub_helpline_call",
                            onClick = {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$helplineNumber"))
                                context.startActivity(dialIntent)
                            }
                        )
                        HorizontalDivider(
                            color = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                        AccountActionRow(
                            icon = Icons.Default.Email,
                            iconColor = MosqueInfoBlue,
                            title = "ইমেইল সাপোর্ট ও ফিডব্যাক",
                            subtitle = "support@mosqueseba.app — যেকোনো সমস্যা বা পরামর্শ পাঠান",
                            testTag = "account_hub_support_email",
                            onClick = {
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:support@mosqueseba.app")
                                    putExtra(Intent.EXTRA_SUBJECT, "মসজিদ সেবা সহায়তা ($mosqueCommitteeId)")
                                }
                                try {
                                    context.startActivity(Intent.createChooser(emailIntent, "ইমেইল অ্যাপ নির্বাচন করুন"))
                                } catch (_: Exception) {
                                    Toast.makeText(context, "ইমেইল: support@mosqueseba.app", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        HorizontalDivider(
                            color = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                        AccountActionRow(
                            icon = Icons.AutoMirrored.Filled.HelpOutline,
                            iconColor = MosqueGoldDeep,
                            title = "ব্যবহার নির্দেশিকা ও সাধারণ প্রশ্নোত্তর",
                            subtitle = "লগইন, চাঁদা রসিদ, বেতন ও ভূমিকা-ভিত্তিক ব্যবহারবিধি",
                            testTag = "account_hub_help_faq",
                            onClick = { showHelplineGuideDialog = true }
                        )
                        HorizontalDivider(
                            color = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                        AccountActionRow(
                            icon = Icons.Default.Explore,
                            iconColor = MosqueEmeraldPrimary,
                            title = "অ্যাপ পরিচিতি ও অনবোর্ডিং (Onboarding)",
                            subtitle = "অ্যাপের মূল ৪টি মডিউলের পরিচিতি স্লাইডগুলো পুনরায় দেখুন",
                            testTag = "account_hub_onboarding_guide",
                            onClick = onOpenOnboarding
                        )
                    }
                }
            }

            // Section 5: অ্যাপ সম্পর্কে ও ভার্সন (About App & Version)
            item(key = "section_about_title") {
                AccountSectionTitle(title = "অ্যাপ সম্পর্কে ও ভার্সন", icon = Icons.Default.Info)
            }

            item(key = "about_card") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        AccountActionRow(
                            icon = Icons.Default.Mosque,
                            iconColor = MaterialTheme.colorScheme.primary,
                            title = "মসজিদ সেবা",
                            subtitle = "ভার্শন ${toBengaliDigits(BuildConfig.VERSION_NAME)} • বিল্ড ${toBengaliDigits(BuildConfig.VERSION_CODE.toString())}",
                            badgeText = "অফিসিয়াল",
                            testTag = "account_hub_about_app",
                            onClick = { showAboutAppDialog = true }
                        )
                        HorizontalDivider(
                            color = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                        AccountActionRow(
                            icon = Icons.Default.SystemUpdate,
                            iconColor = MaterialTheme.colorScheme.primary,
                            title = "অ্যাপ আপডেট পরীক্ষা করুন",
                            subtitle = "নতুন কোনো রিলিজ বা সংস্করণ এসেছে কি না যাচাই করুন",
                            badgeText = "যাচাই",
                            testTag = "account_hub_check_update",
                            onClick = {
                                Toast.makeText(context, "আপডেট পরীক্ষা করা হচ্ছে...", Toast.LENGTH_SHORT).show()
                                viewModel.checkForAppUpdates(forceManual = true) { update ->
                                    if (update == null || !update.isUpdateAvailable) {
                                        Toast.makeText(context, "আপনার অ্যাপটি সর্বশেষ আপ-টু-ডেট সংস্করণে রয়েছে।", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Section 6: Logout
            item(key = "logout_action") {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { showPurgeDemoConfirmDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.2.dp, MosqueUrgentRed.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MosqueUrgentRed.copy(alpha = 0.04f),
                        contentColor = MosqueUrgentRed
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("account_hub_purge_demo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "সকল ডেমো ডেটা মুছে ফ্রেশ শুরু করুন",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { showLogoutConfirmDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.2.dp, MosqueUrgentRed.copy(alpha = 0.7f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MosqueUrgentRed.copy(alpha = 0.06f),
                        contentColor = MosqueUrgentRed
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("account_hub_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "অ্যাকাউন্ট থেকে লগআউট করুন",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            currentRole = currentRole,
            onDismiss = { showChangePasswordDialog = false },
            onSubmit = { currentPass, newPass ->
                viewModel.changeCurrentUserPassword(currentPass, newPass)
            },
            onSuccess = { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                showChangePasswordDialog = false
            }
        )
    }

    if (showHelplineGuideDialog) {
        HelpGuideDialog(
            helplineNumber = helplineNumber,
            committeeId = mosqueCommitteeId,
            onDismiss = { showHelplineGuideDialog = false }
        )
    }

    if (showAboutAppDialog) {
        AboutAppDialog(
            mosqueName = mosqueNameBn,
            committeeId = mosqueCommitteeId,
            onDismiss = { showAboutAppDialog = false }
        )
    }

    if (showCloudSyncDialog) {
        CloudSyncDialog(
            viewModel = viewModel,
            onDismiss = { showCloudSyncDialog = false }
        )
    }

    if (showLogoutConfirmDialog) {
        MosqueConfirmDialog(
            title = "লগআউট নিশ্চিত করুন",
            message = "আপনি কি নিশ্চিতভাবে বর্তমান অ্যাকাউন্ট থেকে লগআউট করে লগইন স্ক্রিনে ফিরে যেতে চান?",
            confirmText = "হ্যাঁ, লগআউট করুন",
            isDestructive = true,
            icon = Icons.AutoMirrored.Filled.Logout,
            confirmTag = "confirm_logout_button",
            onConfirm = {
                showLogoutConfirmDialog = false
                Toast.makeText(context, "অ্যাকাউন্ট থেকে লগআউট সম্পন্ন হয়েছে।", Toast.LENGTH_SHORT).show()
                onLogout()
            },
            onDismiss = { showLogoutConfirmDialog = false }
        )
    }

    if (showPurgeDemoConfirmDialog) {
        MosqueConfirmDialog(
            title = "ডেমো ডেটা রিসেট নিশ্চিতকরণ",
            message = "আপনি কি নিশ্চিতভাবে অ্যাপের সমস্ত ডেমো/স্যাম্পল ডেটা মুছে ফেলে সম্পূর্ণ ফ্রেশ শুরু করতে চান? এর ফলে সমস্ত ডেমো সদস্য, হিসাব ও ডেমো মসজিদ মুছে যাবে এবং আপনি লগইন/নিবন্ধন স্ক্রিনে ফিরে যাবেন।",
            confirmText = "হ্যাঁ, সমস্ত ডেমো মুছুন",
            isDestructive = true,
            icon = Icons.Default.DeleteSweep,
            confirmTag = "confirm_purge_demo_button",
            onConfirm = {
                showPurgeDemoConfirmDialog = false
                viewModel.purgeDemoData {
                    Toast.makeText(context, "সকল ডেমো ডেটা সফলভাবে মুছে ফেলা হয়েছে।", Toast.LENGTH_SHORT).show()
                    onLogout()
                }
            },
            onDismiss = { showPurgeDemoConfirmDialog = false }
        )
    }
}

@Composable
private fun AccountSectionTitle(
    title: String,
    icon: ImageVector
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(17.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = primaryColor
        )
    }
}

@Composable
private fun AccountSettingSwitchRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MosqueEmeraldPrimary
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun DarkModePreferenceRow(
    isDark: Boolean,
    followSystem: Boolean,
    darkEnabled: Boolean,
    onFollowSystem: () -> Unit,
    onLight: () -> Unit,
    onDark: () -> Unit
) {
    val options: List<Triple<String, Boolean, () -> Unit>> = listOf(
        Triple("সিস্টেম", followSystem, onFollowSystem),
        Triple("লাইট", !followSystem && !darkEnabled, onLight),
        Triple("ডার্ক", !followSystem && darkEnabled, onDark)
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(MosqueRadius.Md))
                    .background(MosqueGoldDeep.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = null,
                    tint = MosqueGoldDeep,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(MosqueSpace.Md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "অ্যাপ থিম",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "সিস্টেম অনুসরণ বা লাইট/ডার্ক বেছে নিন",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(MosqueRadius.Md))
                .background(if (isDark) MosqueNightSurfaceVariant else MosqueBgLight)
                .padding(MosqueSpace.Xs)
        ) {
            options.forEach { (label, selected, action) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(MosqueRadius.Sm))
                        .background(if (selected) MosqueEmeraldPrimary else Color.Transparent)
                        .clickable { action() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Color.White
                        else if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountActionRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    testTag: String,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
        if (badgeText != null) {
            Surface(
                color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChangePasswordDialog(
    currentRole: UserRole,
    onDismiss: () -> Unit,
    onSubmit: suspend (String, String) -> Pair<Boolean, String>,
    onSuccess: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val isDark = LocalIsDarkTheme.current

    ExecutiveFormDialogShell(
        title = "পাসওয়ার্ড পরিবর্তন",
        subtitle = currentRole.titleBn,
        icon = Icons.Default.LockReset,
        confirmText = "সংরক্ষণ করুন",
        confirmTag = "change_password_save_button",
        errorBanner = errorText,
        onDismiss = onDismiss,
        onConfirmClick = {
            when {
                currentPassword.isBlank() -> errorText = "বর্তমান পাসওয়ার্ড লিখুন।"
                newPassword.trim().length < 6 -> errorText = "নতুন পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।"
                newPassword != confirmPassword -> errorText = "নতুন পাসওয়ার্ড দুটি মিলছে না।"
                else -> {
                    scope.launch {
                        val (ok, msg) = onSubmit(currentPassword, newPassword)
                        if (ok) {
                            onSuccess(msg)
                        } else {
                            errorText = msg
                        }
                    }
                }
            }
        }
    ) {
        Surface(
            color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (currentRole == UserRole.PRESIDENT) {
                    "ডিফল্ট পাসওয়ার্ড পরিবর্তন না করে থাকলে বর্তমান পাসওয়ার্ড হিসেবে ১২৩৪৫৬ দিন।"
                } else {
                    "আগে পরিবর্তন না করে থাকলে বর্তমান পাসওয়ার্ড হিসেবে আপনার মোবাইল নম্বরের শেষ ৬টি সংখ্যা দিন।"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 16.sp,
                modifier = Modifier.padding(10.dp)
            )
        }

        MoreFormSection(title = "নিরাপত্তা ও পাসওয়ার্ড তথ্য", icon = Icons.Default.Key) {
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it; errorText = null },
                label = { Text("বর্তমান পাসওয়ার্ড *") },
                visualTransformation = if (showCurrent) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(
                        onClick = { showCurrent = !showCurrent },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = if (showCurrent) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "পাসওয়ার্ড দেখুন বা লুকান"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("change_password_current_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it; errorText = null },
                label = { Text("নতুন পাসওয়ার্ড (কমপক্ষে ৬ অক্ষর) *") },
                visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(
                        onClick = { showNew = !showNew },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = if (showNew) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "নতুন পাসওয়ার্ড দেখুন বা লুকান"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("change_password_new_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorText = null },
                label = { Text("নতুন পাসওয়ার্ড নিশ্চিত করুন *") },
                visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("change_password_confirm_input")
            )
        }
    }
}

@Composable
private fun HelpGuideDialog(
    helplineNumber: String,
    committeeId: String,
    onDismiss: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "হেল্প লাইন ও ব্যবহার নির্দেশিকা",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "বন্ধ করুন",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider(color = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle)

                GuideItemBlock(
                    question = "১. কীভাবে চাঁদা রসিদ ও হিসাব যাচাই করবেন?",
                    answer = "সদস্যদের প্রতিটি পরিশোধিত চাঁদার বিপরীতে স্বয়ংক্রিয়ভাবে ভেরিফায়েড ডিজিটাল রসিদ তৈরি হয় এবং স্বচ্ছ হিসাব লেজারে যুক্ত হয়।"
                )
                GuideItemBlock(
                    question = "২. পাসওয়ার্ড ভুলে গেলে বা পরিবর্তন করতে চাইলে?",
                    answer = "সাধারণ সদস্য ও কর্মচারীরা মোবাইল নম্বরের শেষ ৬টি সংখ্যা দিয়ে লগইন করতে পারেন এবং অ্যাকাউন্ট পেজ থেকে যেকোনো সময় নতুন পাসওয়ার্ড সেট করতে পারেন।"
                )
                GuideItemBlock(
                    question = "৩. মসজিদের তথ্য কে সম্পাদনা করতে পারবেন?",
                    answer = "সভাপতি, সহ-সভাপতি এবং সাধারণ সম্পাদক মসজিদের প্রোফাইল থেকে লোগো, ঠিকানা ও কমিটির মেয়াদ হালনাগাদ করতে পারবেন।"
                )
                GuideItemBlock(
                    question = "৪. জরুরি সহায়তা ডেস্ক",
                    answer = "কমিটি আইডি: $committeeId • হেল্প লাইন নম্বর: $helplineNumber"
                )

                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 46.dp)
                ) {
                    Text("বুঝেছি", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun GuideItemBlock(question: String, answer: String) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        color = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.8.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = question,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = answer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun AboutAppDialog(
    mosqueName: String,
    committeeId: String,
    onDismiss: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft)
                        .border(1.2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "মসজিদ সেবা",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "মসজিদ সেবা",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ভার্শন ${toBengaliDigits(BuildConfig.VERSION_NAME)} (বিল্ড ${toBengaliDigits(BuildConfig.VERSION_CODE.toString())})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle)
                Spacer(modifier = Modifier.height(8.dp))

                DetailItemRow("সক্রিয় মসজিদ", mosqueName)
                DetailItemRow("কমিটি আইডি", committeeId)
                DetailItemRow("মূল লক্ষ্য", "মসজিদের খেদমতে আধুনিক প্রযুক্তি, পরিচালনায় স্বচ্ছ আস্থা")
                DetailItemRow("ডাটা নিরাপত্তা", "অফলাইন-প্রস্তুত লোকাল স্টোরেজ ও স্বচ্ছ অডিট লগ")
                DetailItemRow("সাপোর্ট ইমেইল", "support@mosqueseba.app")

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 46.dp)
                ) {
                    Text("বন্ধ করুন", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
