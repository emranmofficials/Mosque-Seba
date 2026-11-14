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
import androidx.compose.ui.window.DialogProperties
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
    onLogout: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val currentRole by viewModel.currentUserRole.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val showHijriBanner by viewModel.showHijriBanner.collectAsState()
    val rememberMeEnabled by viewModel.rememberMeEnabled.collectAsState()
    val darkModeEnabled by viewModel.darkModeEnabled.collectAsState()

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showHelplineGuideDialog by remember { mutableStateOf(false) }
    var showAboutAppDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    val mosqueNameBn = mosque?.nameBn ?: "বায়তুল জান্নাত জামে মসজিদ"
    val mosqueCommitteeId = mosque?.committeeId ?: "MC-DHK-2024"
    val mosqueAddress = mosque?.address ?: "মিরপুর-১০, ঢাকা-১২১৬"

    val helplineContact = remember(allMembers) {
        allMembers.firstOrNull { it.roleCode.equals("general_secretary", ignoreCase = true) && it.mobile.isNotBlank() }
            ?: allMembers.firstOrNull { it.roleCode.equals("president", ignoreCase = true) && it.mobile.isNotBlank() }
    }
    val helplineNumber = helplineContact?.mobile?.takeIf { it.isNotBlank() } ?: "01711-223344"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Emerald-Gold Header
        Surface(
            color = Color.Transparent,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MosqueEmeraldDarkGradient)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("account_hub_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = MosqueGoldLight
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "অ্যাকাউন্ট, প্রোফাইল ও সেটিংস",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "মসজিদ ও ব্যক্তিগত প্রোফাইল, নিরাপত্তা ও সহায়তা কেন্দ্র",
                            style = MaterialTheme.typography.labelSmall,
                            color = MosqueGoldLight.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .background(IslamicGoldTrimGradient)
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
            ) {
            // Section 1: Mosque Profile (Primary — contains User Profile inside details)
            item {
                AccountSectionTitle(title = "মসজিদের প্রোফাইল", icon = Icons.Default.Mosque)
            }

            item {
                // Primary Featured Card: Mosque Profile
                val mosqueLogoBitmap = remember(mosque?.logoUrl) {
                    loadLocalThumbnail(mosque?.logoUrl, 256)
                }
                val mosqueEstBn = toBengaliDigits((mosque?.establishedYear ?: 1998).toString())
                MehrabArchCard(
                    backgroundColor = Color.Transparent,
                    borderColor = MosqueGold.copy(alpha = 0.85f),
                    elevation = 5.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenMosqueProfile)
                        .testTag("account_hub_mosque_profile_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MosqueEmeraldDarkGradient)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .border(2.dp, MosqueGoldGradient, RoundedCornerShape(16.dp))
                                    .padding(if (mosqueLogoBitmap != null) 2.dp else 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (mosqueLogoBitmap != null) {
                                    Image(
                                        bitmap = mosqueLogoBitmap,
                                        contentDescription = mosqueNameBn,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(14.dp))
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.app_logo),
                                        contentDescription = mosqueNameBn,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = MosqueGold,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "মসজিদের প্রোফাইল (প্রধান)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MosqueEmeraldDark,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = mosqueCommitteeId,
                                        fontSize = 10.sp,
                                        color = MosqueGoldLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                Text(
                                    text = mosqueNameBn,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$mosqueAddress • স্থাপিত $mosqueEstBn",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.86f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "মসজিদের প্রোফাইল দেখুন",
                                tint = MosqueGoldLight
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "লোগো, কমিটি কাঠামো ও আমার প্রোফাইল দেখুন",
                                    fontSize = 11.sp,
                                    color = MosqueGoldLight.copy(alpha = 0.92f),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "প্রোফাইল খুলুন ›",
                                    fontSize = 11.sp,
                                    color = MosqueGold,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: App Settings
            item {
                AccountSectionTitle(title = "অ্যাপ সেটিংস", icon = Icons.Default.Tune)
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MosqueCardBorderGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        AccountSettingSwitchRow(
                            icon = Icons.Default.DarkMode,
                            iconColor = MosqueGoldDeep,
                            title = "আই-কমফোর্ট ডার্ক মোড (নাইট থিম)",
                            subtitle = "ফজর ও এশার সময় চোখের আরামের জন্য গাঢ় সবুজ ডার্ক থিম",
                            checked = darkModeEnabled,
                            testTag = "setting_dark_mode_switch",
                            onCheckedChange = {
                                viewModel.setDarkModeEnabled(it)
                                Toast.makeText(
                                    context,
                                    if (it) "ডার্ক মোড চালু করা হয়েছে।" else "লাইট মোড চালু করা হয়েছে।",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), modifier = Modifier.padding(horizontal = 14.dp))
                        AccountSettingSwitchRow(
                            icon = Icons.Default.NotificationsActive,
                            iconColor = MosqueEmeraldPrimary,
                            title = "নোটিফিকেশন ও সতর্কতা ব্যাজ",
                            subtitle = "অপেক্ষমাণ ভাউচার, নোটিশ ও সভার সংখ্যা ব্যাজ প্রদর্শন",
                            checked = notificationsEnabled,
                            testTag = "setting_notifications_switch",
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), modifier = Modifier.padding(horizontal = 14.dp))
                        AccountSettingSwitchRow(
                            icon = Icons.Default.CalendarMonth,
                            iconColor = MosqueGoldDeep,
                            title = "ড্যাশবোর্ডে হিজরি তারিখ প্রদর্শন",
                            subtitle = "হোম স্ক্রিনে হিজরি ও বাংলা তারিখ ব্যানার চালু রাখুন",
                            checked = showHijriBanner,
                            testTag = "setting_hijri_banner_switch",
                            onCheckedChange = { viewModel.setShowHijriBanner(it) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), modifier = Modifier.padding(horizontal = 14.dp))
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

            // Section 3: Security & Password
            item {
                AccountSectionTitle(title = "নিরাপত্তা ও পাসওয়ার্ড", icon = Icons.Default.Security)
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MosqueCardBorderGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AccountActionRow(
                        icon = Icons.Default.LockReset,
                        iconColor = MosqueGoldDeep,
                        title = "পাসওয়ার্ড পরিবর্তন করুন",
                        subtitle = "আপনার অ্যাকাউন্টের বর্তমান লগইন পাসওয়ার্ড আপডেট করুন",
                        testTag = "account_hub_change_password",
                        onClick = { showChangePasswordDialog = true }
                    )
                }
            }

            // Section 4: Helpline & Support
            item {
                AccountSectionTitle(title = "হেল্প লাইন ও সহায়তা", icon = Icons.Default.SupportAgent)
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MosqueCardBorderGold),
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
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), modifier = Modifier.padding(horizontal = 14.dp))
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
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), modifier = Modifier.padding(horizontal = 14.dp))
                        AccountActionRow(
                            icon = Icons.AutoMirrored.Filled.HelpOutline,
                            iconColor = MosqueGoldDeep,
                            title = "ব্যবহার নির্দেশিকা ও সাধারণ প্রশ্নোত্তর",
                            subtitle = "লগইন, চাঁদা রসিদ, বেতন ও ভূমিকা-ভিত্তিক ব্যবহারবিধি",
                            testTag = "account_hub_help_faq",
                            onClick = { showHelplineGuideDialog = true }
                        )
                    }
                }
            }

            // Section 5: About App & Version
            item {
                AccountSectionTitle(title = "অ্যাপ সম্পর্কে ও ভার্সন", icon = Icons.Default.Info)
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MosqueCardBorderGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        AccountActionRow(
                            icon = Icons.Default.Mosque,
                            iconColor = MaterialTheme.colorScheme.primary,
                            title = "মসজিদ সেবা",
                            subtitle = "ভার্শন ${toBengaliDigits(BuildConfig.VERSION_NAME)} • বিল্ড ${toBengaliDigits(BuildConfig.VERSION_CODE.toString())}",
                            badgeText = "সংস্করণ ${toBengaliDigits(BuildConfig.VERSION_NAME)}",
                            testTag = "account_hub_about_app",
                            onClick = { showAboutAppDialog = true }
                        )
                    }
                }
            }

            // Section 6: Logout
            item {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { showLogoutConfirmDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, MosqueUrgentRed.copy(alpha = 0.7f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MosqueUrgentRed.copy(alpha = 0.06f),
                        contentColor = MosqueUrgentRed
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("account_hub_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "অ্যাকাউন্ট থেকে লগআউট করুন",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
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
    onSubmit: (String, String) -> Pair<Boolean, String>,
    onSuccess: (String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

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
                    val (ok, msg) = onSubmit(currentPassword, newPassword)
                    if (ok) {
                        onSuccess(msg)
                    } else {
                        errorText = msg
                    }
                }
            }
        }
    ) {
        Surface(
            color = MosqueGoldSoft,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MosqueCardBorderGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (currentRole == UserRole.PRESIDENT) {
                    "ডিফল্ট পাসওয়ার্ড পরিবর্তন না করে থাকলে বর্তমান পাসওয়ার্ড হিসেবে ১২৩৪৫৬ দিন।"
                } else {
                    "আগে পরিবর্তন না করে থাকলে বর্তমান পাসওয়ার্ড হিসেবে আপনার মোবাইল নম্বরের শেষ ৬টি সংখ্যা দিন।"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MosqueEmeraldDark,
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
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MosqueCardBorderGold),
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
                        fontWeight = FontWeight.ExtraBold,
                        color = MosqueEmeraldDark
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }
                HorizontalDivider(color = MosqueCardBorderSubtle)

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
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text("বুঝেছি", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun GuideItemBlock(question: String, answer: String) {
    Surface(
        color = MosqueBgLight,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.8.dp, MosqueCardBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = question,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MosqueEmeraldDark
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = answer,
                style = MaterialTheme.typography.bodySmall,
                color = MosqueTextSecondary,
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
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MosqueCardBorderGold),
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
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MosqueEmeraldSoft)
                        .border(1.5.dp, MosqueGold, RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "মসজিদ সেবা",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "মসজিদ সেবা",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MosqueEmeraldDark
                )
                Text(
                    text = "ভার্শন ${toBengaliDigits(BuildConfig.VERSION_NAME)} (বিল্ড ${toBengaliDigits(BuildConfig.VERSION_CODE.toString())})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MosqueCardBorderSubtle)
                Spacer(modifier = Modifier.height(8.dp))

                DetailItemRow("সক্রিয় মসজিদ", mosqueName)
                DetailItemRow("কমিটি আইডি", committeeId)
                DetailItemRow("মূল লক্ষ্য", "মসজিদের খেদমতে আধুনিক প্রযুক্তি, পরিচালনায় স্বচ্ছ আস্থা")
                DetailItemRow("ডাটা নিরাপত্তা", "অফলাইন-প্রস্তুত এনক্রিপ্টেড লোকাল স্টোরেজ ও স্বচ্ছ অডিট লগ")
                DetailItemRow("সাপোর্ট ইমেইল", "support@mosqueseba.app")

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text("বন্ধ করুন", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
