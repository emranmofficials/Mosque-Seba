package com.mosqueseba.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mosqueseba.app.R
import com.mosqueseba.app.data.local.entity.MosqueEntity
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.components.*
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.ui.viewmodel.MosqueViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MosqueProfileScreen(
    viewModel: MosqueViewModel,
    showTopBar: Boolean = true,
    onBack: (() -> Unit)? = null,
    onOpenMyProfile: (() -> Unit)? = null
) {
    if (onBack != null && showTopBar) {
        BackHandler { onBack() }
    }

    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current

    val mosque by viewModel.mosque.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    val activeStaff by viewModel.activeStaff.collectAsState()
    val studentCount by viewModel.studentCount.collectAsState()

    val canEditMosque = currentRole.canEditMosqueProfile()
    var showEditDialog by remember { mutableStateOf(false) }
    var copiedCommitteeId by remember { mutableStateOf(false) }

    LaunchedEffect(copiedCommitteeId) {
        if (copiedCommitteeId) {
            delay(1600)
            copiedCommitteeId = false
        }
    }

    val nameBn = mosque?.nameBn ?: "বায়তুল জান্নাত জামে মসজিদ"
    val nameEn = mosque?.nameEn ?: "Baitul Jannat Jame Masjid"
    val committeeId = mosque?.committeeId ?: "MC-DHK-2024"
    val address = mosque?.address ?: "রোড-৭, ব্লক-বি, মিরপুর-১০, ঢাকা-১২১৬"
    val district = mosque?.district?.takeIf { it.isNotBlank() } ?: "ঢাকা"
    val upazila = mosque?.upazila?.takeIf { it.isNotBlank() } ?: "মিরপুর"
    val establishedYear = mosque?.establishedYear ?: 1998
    val startYear = mosque?.committeeStartYear ?: 2024
    val endYear = mosque?.committeeEndYear ?: 2026
    val waqfRegNo = mosque?.waqfRegNo?.takeIf { it.isNotBlank() } ?: "সংযুক্ত নয়"
    val capacityText = mosque?.capacity?.takeIf { it > 0 }?.let { "${toBengaliDigits(it.toString())} জন মুসল্লি" } ?: "নির্ধারিত নয়"
    val bankInfo = mosque?.bankAccountInfo?.takeIf { it.isNotBlank() } ?: "নির্ধারিত নয়"
    val isActive = (mosque?.status ?: "active") == "active"

    val activeMemberCount = remember(allMembers) { allMembers.count { it.status == "active" } }

    val president = remember(allMembers) {
        allMembers.firstOrNull { it.roleCode.equals("president", ignoreCase = true) }
    }
    val vicePresident = remember(allMembers) {
        allMembers.firstOrNull { it.roleCode.equals("vice_president", ignoreCase = true) }
    }
    val generalSecretary = remember(allMembers) {
        allMembers.firstOrNull { it.roleCode.equals("general_secretary", ignoreCase = true) }
    }
    val treasurer = remember(allMembers) {
        allMembers.firstOrNull { it.roleCode.equals("treasurer", ignoreCase = true) }
    }
    val imamMember = remember(allMembers) {
        allMembers.firstOrNull { it.roleCode.equals("imam", ignoreCase = true) }
    }
    val imamStaff = remember(activeStaff) {
        activeStaff.firstOrNull { it.roleCode.equals("imam", ignoreCase = true) }
    }

    val isDark = LocalIsDarkTheme.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (showTopBar) {
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (onBack != null) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier.testTag("mosque_profile_back_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "ফিরে যান",
                                        tint = MosqueGoldLight
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Column {
                                Text(
                                    text = "মসজিদের প্রোফাইল",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "কমিটি আইডি: $committeeId",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MosqueGoldLight.copy(alpha = 0.9f)
                                )
                            }
                        }

                        if (canEditMosque && mosque != null) {
                            IconButton(
                                onClick = { showEditDialog = true },
                                modifier = Modifier.testTag("mosque_profile_edit_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "মসজিদের তথ্য সম্পাদনা",
                                    tint = MosqueGoldLight
                                )
                            }
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
        }

        LazyColumn(
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Sanctuary Hero Card
            item {
                MehrabArchCard(
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    borderColor = if (isDark) MosqueNightBorder else MosqueCardBorderGold,
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val logoBitmap = remember(mosque?.logoUrl) {
                            loadLocalThumbnail(mosque?.logoUrl, 320)
                        }

                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(MosqueEmeraldDark)
                                .border(2.5.dp, MosqueGoldGradient, RoundedCornerShape(22.dp))
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (logoBitmap != null) {
                                Image(
                                    bitmap = logoBitmap,
                                    contentDescription = "মসজিদের অফিসিয়াল লোগো",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(19.dp))
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo),
                                    contentDescription = "মসজিদের অফিসিয়াল লোগো",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(19.dp))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = nameBn,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) MosqueGoldLight else MosqueEmeraldDark
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(committeeId))
                                    copiedCommitteeId = true
                                    MosqueToastController.info(
                                        "কমিটি আইডি ($committeeId) কপি করা হয়েছে",
                                        "কপি সম্পন্ন"
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = MosqueGoldLight.copy(alpha = 0.55f),
                                border = BorderStroke(1.dp, MosqueGoldDeep.copy(alpha = 0.45f)),
                                modifier = Modifier.testTag("mosque_profile_copy_id")
                            ) {
                                AnimatedContent(
                                    targetState = copiedCommitteeId,
                                    transitionSpec = {
                                        (fadeIn(tween(180)) + scaleIn(initialScale = 0.88f)) togetherWith
                                            (fadeOut(tween(140)) + scaleOut(targetScale = 0.88f))
                                    },
                                    label = "CopyMosqueCommitteeId"
                                ) { copied ->
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (copied) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                                            contentDescription = "কমিটি আইডি কপি করুন",
                                            tint = if (copied) MosqueSuccessGreen else if (isDark) MosqueGoldLight else MosqueEmeraldDark,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = if (copied) "আইডি কপি হয়েছে" else "আইডি: $committeeId",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) MosqueGoldLight else MosqueEmeraldDark
                                        )
                                    }
                                }
                            }

                            Surface(
                                color = if (isActive) MosqueSuccessGreen.copy(alpha = 0.12f) else MosqueUrgentRed.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isActive) "সক্রিয় কমিটি" else "নিষ্ক্রিয়",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) MosqueSuccessGreen else MosqueUrgentRed,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val shareText = buildString {
                                        appendLine(nameBn)
                                        appendLine("কমিটি আইডি: $committeeId")
                                        appendLine("ঠিকানা: $address")
                                        appendLine("প্রতিষ্ঠা সাল: ${toBengaliDigits(establishedYear.toString())} ইং")
                                        appendLine("কমিটির মেয়াদ: ${toBengaliDigits(startYear.toString())} — ${toBengaliDigits(endYear.toString())} খ্রি.")
                                    }
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "মসজিদের পরিচিতি শেয়ার করুন"))
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MosqueEmeraldPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("mosque_profile_share_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = MosqueEmeraldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("শেয়ার করুন", color = MosqueEmeraldPrimary, fontWeight = FontWeight.Bold)
                            }

                            if (canEditMosque && mosque != null) {
                                Button(
                                    onClick = { showEditDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("mosque_profile_hero_edit_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("তথ্য সম্পাদনা", fontWeight = FontWeight.Bold)
                                }
                            } else if (onOpenMyProfile != null) {
                                Button(
                                    onClick = onOpenMyProfile,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("mosque_to_my_profile_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("আমার প্রোফাইল", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (canEditMosque && onOpenMyProfile != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = onOpenMyProfile,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MosqueGoldDeep.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("mosque_to_my_profile_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = if (isDark) MosqueGoldLight else MosqueEmeraldDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("আমার নিজস্ব প্রোফাইল দেখুন", color = if (isDark) MosqueGoldLight else MosqueEmeraldDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. Live Overview Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MosqueStatPill(
                        modifier = Modifier.weight(1f),
                        label = "মোট সদস্য",
                        value = "${toBengaliDigits(activeMemberCount.toString())} জন",
                        accent = MosqueEmeraldPrimary
                    )
                    MosqueStatPill(
                        modifier = Modifier.weight(1f),
                        label = "খেদমত স্টাফ",
                        value = "${toBengaliDigits(activeStaff.size.toString())} জন",
                        accent = MosqueInfoBlue
                    )
                    MosqueStatPill(
                        modifier = Modifier.weight(1f),
                        label = "মাদরাসা ছাত্র",
                        value = "${toBengaliDigits(studentCount.toString())} জন",
                        accent = MosqueGoldDeep
                    )
                }
            }

            // 3. Detailed Mosque & Administration Info
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mosque,
                                contentDescription = null,
                                tint = MosqueEmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "মসজিদের মৌলিক ও প্রশাসনিক তথ্য",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) MosqueGoldLight else MosqueEmeraldDark
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MosqueCardBorderSubtle)
                        Spacer(modifier = Modifier.height(6.dp))

                        DetailItemRow("কমিটি আইডি", committeeId)
                        DetailItemRow("পূর্ণ ঠিকানা", address)
                        DetailItemRow("জেলা ও উপজেলা", "$upazila, $district")
                        DetailItemRow("প্রতিষ্ঠা সাল", "${toBengaliDigits(establishedYear.toString())} ইং")
                        DetailItemRow(
                            "কমিটির মেয়াদকাল",
                            "${toBengaliDigits(startYear.toString())} — ${toBengaliDigits(endYear.toString())} খ্রি."
                        )
                        DetailItemRow("ওয়াকফ / নিবন্ধন নম্বর", waqfRegNo)
                        DetailItemRow("মুসল্লি ধারণক্ষমতা", capacityText)
                        DetailItemRow("ব্যাংক / পেমেন্ট তথ্য", bankInfo)
                        DetailItemRow("অফিসিয়াল স্লোগান", "মসজিদের খেদমতে আধুনিক প্রযুক্তি, পরিচালনায় স্বচ্ছ আস্থা")
                    }
                }
            }

            // 4. Key Committee Officeholders Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = MosqueEmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "প্রধান দায়িত্বশীলবৃন্দ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) MosqueGoldLight else MosqueEmeraldDark
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MosqueCardBorderSubtle)
                        Spacer(modifier = Modifier.height(8.dp))

                        OfficeholderRow(
                            roleTitle = UserRole.PRESIDENT.titleBn,
                            name = president?.nameBn ?: "নির্ধারিত নয়",
                            mobile = president?.mobile ?: "",
                            accent = RolePresidentAccent,
                            onCall = { mobile ->
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$mobile")))
                            }
                        )
                        OfficeholderRow(
                            roleTitle = UserRole.VICE_PRESIDENT.titleBn,
                            name = vicePresident?.nameBn ?: "নির্ধারিত নয়",
                            mobile = vicePresident?.mobile ?: "",
                            accent = RolePresidentAccent,
                            onCall = { mobile ->
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$mobile")))
                            }
                        )
                        OfficeholderRow(
                            roleTitle = UserRole.GENERAL_SECRETARY.titleBn,
                            name = generalSecretary?.nameBn ?: "নির্ধারিত নয়",
                            mobile = generalSecretary?.mobile ?: "",
                            accent = RoleSecretaryAccent,
                            onCall = { mobile ->
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$mobile")))
                            }
                        )
                        OfficeholderRow(
                            roleTitle = UserRole.TREASURER.titleBn,
                            name = treasurer?.nameBn ?: "নির্ধারিত নয়",
                            mobile = treasurer?.mobile ?: "",
                            accent = RoleTreasurerAccent,
                            onCall = { mobile ->
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$mobile")))
                            }
                        )
                        OfficeholderRow(
                            roleTitle = UserRole.IMAM.titleBn,
                            name = imamMember?.nameBn ?: imamStaff?.nameBn ?: "নির্ধারিত নয়",
                            mobile = imamMember?.mobile ?: imamStaff?.mobile ?: "",
                            accent = RoleImamAccent,
                            showDivider = false,
                            onCall = { mobile ->
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$mobile")))
                            }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showEditDialog && mosque != null) {
        EditMosqueProfileDialog(
            mosque = mosque!!,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                viewModel.updateMosqueDetails(updated)
                MosqueToastController.success("মসজিদের প্রোফাইল সফলভাবে হালনাগাদ হয়েছে", "হালনাগাদ সম্পন্ন")
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun MosqueStatPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderGold)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MosqueTextSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = accent
            )
        }
    }
}

@Composable
private fun OfficeholderRow(
    roleTitle: String,
    name: String,
    mobile: String,
    accent: Color,
    showDivider: Boolean = true,
    onCall: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = accent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = roleTitle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (mobile.isNotBlank()) {
                    Text(
                        text = mobile,
                        style = MaterialTheme.typography.labelSmall,
                        color = MosqueTextSecondary
                    )
                }
            }

            if (mobile.isNotBlank()) {
                IconButton(
                    onClick = { onCall(mobile) },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "$name-কে কল করুন",
                        tint = MosqueEmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        if (showDivider) {
            HorizontalDivider(color = MosqueCardBorderSubtle.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun EditMosqueProfileDialog(
    mosque: MosqueEntity,
    onDismiss: () -> Unit,
    onSave: (MosqueEntity) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nameBn by remember { mutableStateOf(mosque.nameBn) }
    var nameEn by remember { mutableStateOf(mosque.nameEn) }
    var address by remember { mutableStateOf(mosque.address) }
    var district by remember { mutableStateOf(mosque.district) }
    var upazila by remember { mutableStateOf(mosque.upazila) }
    var establishedYear by remember { mutableStateOf(mosque.establishedYear.toString()) }
    var startYear by remember { mutableStateOf(mosque.committeeStartYear.toString()) }
    var endYear by remember { mutableStateOf(mosque.committeeEndYear.toString()) }
    var waqfRegNo by remember { mutableStateOf(mosque.waqfRegNo) }
    var capacity by remember { mutableStateOf(mosque.capacity?.toString() ?: "") }
    var bankAccountInfo by remember { mutableStateOf(mosque.bankAccountInfo) }
    var logoPath by remember { mutableStateOf(mosque.logoUrl) }
    var logoPreview by remember(mosque.logoUrl) {
        mutableStateOf<ImageBitmap?>(loadLocalThumbnail(mosque.logoUrl, 256))
    }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val (savedPath, bitmap) = savePickedImageToAppStorage(context, uri, "mosque_logo")
                if (savedPath != null) {
                    logoPath = savedPath
                    logoPreview = bitmap
                }
            }
        }
    }

    ExecutiveFormDialogShell(
        title = "মসজিদের প্রোফাইল সম্পাদনা",
        subtitle = "কমিটি আইডি: ${mosque.committeeId}",
        icon = Icons.Default.Mosque,
        confirmText = "সংরক্ষণ করুন",
        confirmTag = "mosque_profile_save_button",
        errorBanner = errorMsg,
        onDismiss = onDismiss,
        onConfirmClick = {
            if (nameBn.isBlank() || address.isBlank()) {
                errorMsg = "মসজিদের বাংলা নাম এবং ঠিকানা আবশ্যক"
            } else {
                onSave(
                    mosque.copy(
                        nameBn = nameBn.trim(),
                        nameEn = nameEn.trim(),
                        address = address.trim(),
                        district = district.trim(),
                        upazila = upazila.trim(),
                        establishedYear = establishedYear.toIntOrNull() ?: mosque.establishedYear,
                        committeeStartYear = startYear.toIntOrNull() ?: mosque.committeeStartYear,
                        committeeEndYear = endYear.toIntOrNull() ?: mosque.committeeEndYear,
                        waqfRegNo = waqfRegNo.trim(),
                        capacity = capacity.toIntOrNull(),
                        bankAccountInfo = bankAccountInfo.trim(),
                        logoUrl = logoPath
                    )
                )
            }
        }
    ) {
        MoreFormSection(title = "অফিসিয়াল লোগো", icon = Icons.Default.AddPhotoAlternate) {
            Surface(
                onClick = {
                    logoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                color = if (LocalIsDarkTheme.current) MosqueNightSurfaceVariant else MosqueBgLight,
                border = BorderStroke(1.dp, if (LocalIsDarkTheme.current) MosqueNightBorder else MosqueCardBorderGold),
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
                            .clip(RoundedCornerShape(14.dp))
                            .background(MosqueEmeraldSoft)
                            .border(1.5.dp, MosqueGold, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val preview = logoPreview
                        if (preview != null) {
                            Image(
                                bitmap = preview,
                                contentDescription = "মসজিদের লোগো",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Mosque,
                                contentDescription = "লোগো নির্বাচন",
                                tint = MosqueEmeraldPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (logoPreview != null) "অফিসিয়াল লোগো সংযুক্ত" else "মসজিদের লোগো আপলোড (ঐচ্ছিক)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MosqueEmeraldDark
                        )
                        Text(
                            text = "পরিবর্তন করতে ট্যাপ করুন",
                            style = MaterialTheme.typography.labelSmall,
                            color = MosqueTextSecondary
                        )
                    }
                    if (logoPath != null) {
                        IconButton(
                            onClick = {
                                logoPath = null
                                logoPreview = null
                            },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "লোগো মুছুন", tint = MosqueUrgentRed)
                        }
                    }
                }
            }
        }

        MoreFormSection(title = "পরিচিতি ও পূর্ণ ঠিকানা", icon = Icons.Default.LocationOn) {
            OutlinedTextField(
                value = nameBn,
                onValueChange = { nameBn = it; errorMsg = null },
                label = { Text("মসজিদের নাম *") },
                isError = errorMsg != null && nameBn.isBlank(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it; errorMsg = null },
                label = { Text("মসজিদের পূর্ণ ঠিকানা *") },
                isError = errorMsg != null && address.isBlank(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text("জেলা") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = upazila,
                    onValueChange = { upazila = it },
                    label = { Text("উপজেলা/থানা") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        MoreFormSection(title = "নিবন্ধন, মেয়াদ ও ব্যাংক তথ্য", icon = Icons.Default.AccountBalance) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = establishedYear,
                    onValueChange = { establishedYear = it.filter { ch -> ch.isDigit() } },
                    label = { Text("প্রতিষ্ঠা সাল") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it.filter { ch -> ch.isDigit() } },
                    label = { Text("ধারণক্ষমতা") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = startYear,
                    onValueChange = { startYear = it.filter { ch -> ch.isDigit() } },
                    label = { Text("কমিটি শুরু সাল") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = endYear,
                    onValueChange = { endYear = it.filter { ch -> ch.isDigit() } },
                    label = { Text("কমিটি শেষ সাল") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = waqfRegNo,
                onValueChange = { waqfRegNo = it },
                label = { Text("ওয়াকফ / নিবন্ধন নম্বর") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = bankAccountInfo,
                onValueChange = { bankAccountInfo = it },
                label = { Text("ব্যাংক / মোবাইল ব্যাংকিং তথ্য") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
