package com.mosqueseba.app.ui.dialogs

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mosqueseba.app.ui.components.MehrabArchCard
import com.mosqueseba.app.ui.components.MosqueToastController
import com.mosqueseba.app.ui.components.toBengaliDigits
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.util.MosquePrintHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IslamicDawatnamaDialog(
    defaultMosqueName: String,
    defaultMosqueAddress: String,
    presidentSignatureUrl: String? = null,
    presidentStampUrl: String? = null,
    mosqueSealUrl: String? = null,
    onPublishAsNotice: (title: String, content: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current

    val eventTypes = remember {
        listOf(
            "বাৎসরিক ওয়াজ ও দোয়ার মাহফিল",
            "পবিত্র মাহে রমজান ইফতার মাহফিল",
            "শবে বরাত / শবে কদর বিশেষ দোয়া",
            "মসজিদ সম্প্রসারণ ও ভিত্তিপ্রস্তর স্থাপন",
            "বার্ষিক সাধারণ সভা (AGM) ও সুধী সম্মেলন",
            "হিফজ সমাপনী ও পাগড়ি প্রদান অনুষ্ঠান"
        )
    }

    var selectedEventType by remember { mutableStateOf(eventTypes[0]) }
    var title by remember { mutableStateOf("ঐতিহাসিক তাফসিরুল কুরআন ও বার্ষিক ওয়াজ মাহফিল") }
    var chiefGuest by remember { mutableStateOf("") }
    var specialSpeaker by remember { mutableStateOf("") }
    var honorableGuests by remember { mutableStateOf("") }
    var president by remember { mutableStateOf("সভাপতি, মসজিদ পরিচালনা কমিটি") }

    val todayStr = remember {
        SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("bn-BD")).format(Date())
    }
    var dateStr by remember { mutableStateOf(toBengaliDigits(todayStr)) }
    var timeStr by remember { mutableStateOf("বাদ আসর হইতে রাত ১১:০০ টা পর্যন্ত") }
    var venue by remember { mutableStateOf(defaultMosqueName) }
    var description by remember {
        mutableStateOf("পবিত্র কুরআন ও হাদিসের আলোকে বয়ান পেশ করবেন প্রখ্যাত ওলামায়ে কেরাম। সকল ধর্মপ্রাণ মুসলিম ভাইদের মাহফিলে উপস্থিত হয়ে ইহকালীন কল্যাণ ও পরকালীন নাজাত অর্জনের জন্য বিনীত অনুরোধ করা হচ্ছে।")
    }
    var organizer by remember { mutableStateOf("$defaultMosqueName পরিচালনা কমিটি ও এলাকাবাসী") }
    var contact by remember { mutableStateOf("") }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Form, 1: Live Preview

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MehrabArchCard(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 640.dp)
                .fillMaxHeight(0.92f),
            backgroundColor = MaterialTheme.colorScheme.surface,
            borderColor = MosqueCardBorderGold
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MosqueGoldDeep,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "দাওয়াতনামা ও পোস্টার জেনারেটর",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "ইসলামিক মাহফিল ও সভার আধুনিক আমন্ত্রণ পত্র",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Selector
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("তথ্য ও সম্পাদনা", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("লাইভ পোস্টার ভিউ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Body content
                Box(modifier = Modifier.weight(1f)) {
                    if (selectedTab == 0) {
                        // Edit Form
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Event Type Chips
                            Text(
                                text = "অনুষ্ঠানের ধরণ নির্বাচন করুন:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                eventTypes.forEach { type ->
                                    val isSelected = selectedEventType == type
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (isSelected) MosqueEmeraldPrimary else (if (isDark) MosqueNightSurfaceVariant else MosqueBgLight),
                                        border = BorderStroke(1.dp, if (isSelected) MosqueEmeraldPrimary else MosqueCardBorderSubtle),
                                        modifier = Modifier.clickable {
                                            selectedEventType = type
                                            when (type) {
                                                "বাৎসরিক ওয়াজ ও দোয়ার মাহফিল" -> {
                                                    title = "ঐতিহাসিক তাফসিরুল কুরআন ও বার্ষিক ওয়াজ মাহফিল"
                                                    timeStr = "বাদ আসর হইতে রাত ১১:০০ টা পর্যন্ত"
                                                }
                                                "পবিত্র মাহে রমজান ইফতার মাহফিল" -> {
                                                    title = "পবিত্র মাহে রমজান উপলক্ষে দোয়া ও গণ-ইফতার মাহফিল"
                                                    timeStr = "বিকাল ৪:৩০ টা (ইফতারের পূর্বে)"
                                                }
                                                "শবে বরাত / শবে কদর বিশেষ দোয়া" -> {
                                                    title = "পবিত্র শবে কদর উপলক্ষে বিশেষ আলোচনা ও দোয়া মাহফিল"
                                                    timeStr = "বাদ তারাবিহ হইতে শেষ রাত পর্যন্ত"
                                                }
                                                "মসজিদ সম্প্রসারণ ও ভিত্তিপ্রস্তর স্থাপন" -> {
                                                    title = "মসজিদ সম্প্রসারণ প্রকল্পের ভিত্তিপ্রস্তর স্থাপন ও দোয়া অনুষ্ঠান"
                                                    timeStr = "বাদ জুমা দুপুর ২:০০ ঘটিকা"
                                                }
                                                "বার্ষিক সাধারণ সভা (AGM) ও সুধী সম্মেলন" -> {
                                                    title = "মসজিদের বার্ষিক সাধারণ সভা ও সুধী সম্মেলন"
                                                    timeStr = "সকাল ১০:০০ ঘটিকা"
                                                }
                                                "হিফজ সমাপনী ও পাগড়ি প্রদান অনুষ্ঠান" -> {
                                                    title = "হিফজুল কুরআন সমাপনী ও পাগড়ি প্রদান দস্তরবন্দী মাহফিল"
                                                    timeStr = "বাদ মাগরিব"
                                                }
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = type,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            // AI Polish Button
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MosqueGoldLight.copy(alpha = if (isDark) 0.15f else 0.4f),
                                border = BorderStroke(1.dp, MosqueGold),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        description = "নাহমাদুহু ওয়া নুসাল্লি আলা রাসুলিহিল কারিম। পরম করুণাময় মহান আল্লাহর অশেষ মেহেরবানীতে $defaultMosqueName প্রাঙ্গণে $title অনুষ্ঠিত হতে যাচ্ছে। উক্ত বরকতময় মাহফিলে দেশবরেণ্য হক্কানি ওলামায়ে কেরাম কুরআন ও সুন্নাহর আলোকে হেদায়াতি বক্তব্য পেশ করবেন। আপনার সপরিবার উপস্থিতি একান্তভাবে কাম্য।"
                                        MosqueToastController.success("এআই দিয়ে দাওয়াতনামা চমৎকারভাবে সাজানো হয়েছে।", "এআই ড্রাফট প্রস্তুত")
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MosqueGoldDeep, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("✨ এআই দিয়ে দাওয়াতনামা পরিমার্জন করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MosqueGoldDeep)
                                        Text("কুরআনি শিষ্টাচার ও আন্তরিক ভাষায় স্বয়ংক্রিয় বর্ণনা লিখুন", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("মাহফিল / অনুষ্ঠানের শিরোনাম *") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = dateStr,
                                    onValueChange = { dateStr = it },
                                    label = { Text("তারিখ ও বার") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = timeStr,
                                    onValueChange = { timeStr = it },
                                    label = { Text("সময়") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            OutlinedTextField(
                                value = venue,
                                onValueChange = { venue = it },
                                label = { Text("স্থান / ভেন্যু") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = chiefGuest,
                                onValueChange = { chiefGuest = it },
                                label = { Text("প্রধান অতিথি (যদি থাকে)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = specialSpeaker,
                                onValueChange = { specialSpeaker = it },
                                label = { Text("প্রধান বক্তা / মুফাসসির") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = honorableGuests,
                                onValueChange = { honorableGuests = it },
                                label = { Text("বিশেষ মেহমান / অন্যান্য আলোচক") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = president,
                                onValueChange = { president = it },
                                label = { Text("সভাপতিত্ব করবেন") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("কর্মসূচি ও বিশেষ বিবরণ") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = organizer,
                                    onValueChange = { organizer = it },
                                    label = { Text("আমন্ত্রণে") },
                                    modifier = Modifier.weight(1.2f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = contact,
                                    onValueChange = { contact = it },
                                    label = { Text("যোগাযোগ নম্বর") },
                                    modifier = Modifier.weight(0.8f),
                                    singleLine = true
                                )
                            }
                        }
                    } else {
                        // Live Poster Preview
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight,
                                border = BorderStroke(2.dp, MosqueGold),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueEmeraldPrimary
                                    )
                                    Text(
                                        text = "আসসালামু আলাইকুম ওয়া রাহমাতুল্লাহ",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MosqueEmeraldPrimary,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = defaultMosqueName,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Black,
                                                color = MosqueGold
                                            )
                                            Text(
                                                text = selectedEventType,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MosqueEmeraldDark,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Info pill row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("📅 তারিখ", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(dateStr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MosqueEmeraldPrimary)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("⏰ সময়", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(timeStr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MosqueEmeraldPrimary)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("📍 স্থান", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(venue.take(16), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MosqueEmeraldPrimary)
                                        }
                                    }

                                    if (chiefGuest.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = MosqueCardBorderSubtle)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("★ প্রধান অতিথি:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MosqueGoldDeep)
                                        Text(chiefGuest, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }

                                    if (specialSpeaker.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("★ প্রধান আলোচক:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MosqueGoldDeep)
                                        Text(specialSpeaker, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }

                                    if (president.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("★ সভাপতিত্ব করবেন:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MosqueGoldDeep)
                                        Text(president, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = description,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "আমন্ত্রণে: $organizer",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueEmeraldPrimary
                                    )
                                    if (contact.isNotBlank()) {
                                        Text("যোগাযোগ: $contact", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MosqueCardBorderSubtle)
                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(0.7f)
                            .heightIn(min = 46.dp)
                    ) {
                        Text("বন্ধ", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            MosquePrintHelper.printInvitationPoster(
                                context = context,
                                mosqueName = defaultMosqueName,
                                mosqueAddress = defaultMosqueAddress,
                                title = title,
                                eventType = selectedEventType,
                                chiefGuest = chiefGuest,
                                specialSpeaker = specialSpeaker,
                                honorableGuests = honorableGuests,
                                president = president,
                                dateStr = dateStr,
                                timeStr = timeStr,
                                venue = venue,
                                description = description,
                                organizer = organizer,
                                contact = contact,
                                presidentSignatureUrl = presidentSignatureUrl,
                                presidentStampUrl = presidentStampUrl,
                                mosqueSealUrl = mosqueSealUrl
                            )
                        },
                        modifier = Modifier
                            .weight(1.1f)
                            .heightIn(min = 46.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MosqueEmeraldPrimary),
                        border = BorderStroke(1.dp, MosqueEmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("প্রিন্ট / PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val shareText = buildString {
                                appendLine("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ")
                                appendLine("আসসালামু আলাইকুম ওয়া রাহমাতুল্লাহ")
                                appendLine("----------------------------------------")
                                appendLine("🕌 $defaultMosqueName")
                                appendLine("📢 $selectedEventType")
                                appendLine("★ $title")
                                appendLine("----------------------------------------")
                                appendLine("📅 তারিখ: $dateStr")
                                appendLine("⏰ সময়: $timeStr")
                                appendLine("📍 স্থান: $venue")
                                if (chiefGuest.isNotBlank()) appendLine("★ প্রধান অতিথি: $chiefGuest")
                                if (specialSpeaker.isNotBlank()) appendLine("★ প্রধান আলোচক: $specialSpeaker")
                                if (honorableGuests.isNotBlank()) appendLine("★ বিশেষ মেহমান: $honorableGuests")
                                if (president.isNotBlank()) appendLine("★ সভাপতিত্ব: $president")
                                appendLine()
                                appendLine(description)
                                appendLine()
                                appendLine("আমন্ত্রণে: $organizer")
                                if (contact.isNotBlank()) appendLine("যোগাযোগ: $contact")
                                appendLine("----------------------------------------")
                                appendLine("মসজিদ সেবা (Mosque Seba) অ্যাপ থেকে প্রচারিত")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, title)
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "দাওয়াতনামা শেয়ার করুন"))
                        },
                        modifier = Modifier
                            .weight(0.9f)
                            .heightIn(min = 46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("শেয়ার", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val noticeContent = buildString {
                                appendLine("অনুষ্ঠানের ধরণ: $selectedEventType")
                                appendLine("তারিখ: $dateStr | সময়: $timeStr")
                                appendLine("স্থান: $venue")
                                if (chiefGuest.isNotBlank()) appendLine("প্রধান অতিথি: $chiefGuest")
                                if (specialSpeaker.isNotBlank()) appendLine("প্রধান বক্তা: $specialSpeaker")
                                if (honorableGuests.isNotBlank()) appendLine("বিশেষ আলোচক: $honorableGuests")
                                if (president.isNotBlank()) appendLine("সভাপতি: $president")
                                appendLine()
                                appendLine(description)
                                appendLine()
                                appendLine("আমন্ত্রণে: $organizer")
                                if (contact.isNotBlank()) appendLine("যোগাযোগ: $contact")
                            }
                            onPublishAsNotice(title, noticeContent)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .heightIn(min = 46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueEmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নোটিশে প্রকাশ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
