package com.mosqueseba.app.ui.dialogs

import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mosqueseba.app.data.local.entity.NoticeEntity
import com.mosqueseba.app.ui.components.MehrabArchCard
import com.mosqueseba.app.ui.components.MosqueToastController
import com.mosqueseba.app.ui.components.toBengaliDigits
import com.mosqueseba.app.ui.theme.*
import com.mosqueseba.app.util.MosquePrintHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun JanazaAlertDialog(
    mosqueName: String,
    presidentSignatureUrl: String? = null,
    presidentStampUrl: String? = null,
    mosqueSealUrl: String? = null,
    onBroadcastConfirm: (
        deceasedName: String,
        relativeRelation: String,
        guardianName: String,
        age: String,
        villageArea: String,
        timeOfDeath: String,
        janazaVenue: String,
        janazaTime: String,
        graveyardVenue: String,
        contactPhone: String,
        extraNotes: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current

    var deceasedName by remember { mutableStateOf("") }
    var relativeRelation by remember { mutableStateOf("পিতা") }
    var guardianName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var villageArea by remember { mutableStateOf("মসজিদ সংলগ্ন এলাকা") }

    val defaultDemiseTime = remember {
        val now = SimpleDateFormat("hh:mm a", Locale.forLanguageTag("bn-BD")).format(Date())
        "আজ আনুমানিক $now ঘটিকা"
    }
    var timeOfDeath by remember { mutableStateOf(toBengaliDigits(defaultDemiseTime)) }
    var janazaVenue by remember { mutableStateOf("$mosqueName প্রাঙ্গণ") }
    var janazaTime by remember { mutableStateOf("আজ বাদ জোহর দুপুর ১:৪৫ মি.") }
    var graveyardVenue by remember { mutableStateOf("মসজিদ কেন্দ্রীয় কবরস্থান") }
    var contactPhone by remember { mutableStateOf("") }
    var extraNotes by remember { mutableStateOf("সকল ধর্মপ্রাণ মুসল্লি ভাইদের মরহুমের জানাজায় শরিক হয়ে মাগফিরাত কামনায় দোয়ার উদাত্ত আহ্বান জানানো যাচ্ছে।") }

    val formattedBroadcastMessage = remember(
        deceasedName, relativeRelation, guardianName, age, villageArea,
        timeOfDeath, janazaVenue, janazaTime, graveyardVenue, contactPhone, extraNotes
    ) {
        buildString {
            appendLine("إِنَّا لِلَّهِ وَإِنَّا إِلَيْهِ رَاجِعُونَ")
            appendLine("------------------------------------------")
            appendLine("🚨 জরুরি জানাজা ও শোক সংবাদ")
            appendLine("মসজিদ: $mosqueName")
            appendLine("------------------------------------------")
            appendLine("অত্যন্ত শোকাহত হৃদয়ে জানানো যাচ্ছে যে, আমাদের এলাকার শ্রদ্ধেয় $deceasedName")
            if (guardianName.isNotBlank()) {
                appendLine("($relativeRelation: $guardianName)")
            }
            if (age.isNotBlank()) {
                appendLine("বয়স: ${toBengaliDigits(age)} বছর | ঠিকানা: $villageArea")
            }
            appendLine("তিনি $timeOfDeath ইন্তেকাল করেছেন।")
            appendLine()
            appendLine("🕌 নামাজে জানাজার সময় ও স্থান:")
            appendLine("• স্থান: $janazaVenue")
            appendLine("• সময়: $janazaTime")
            if (graveyardVenue.isNotBlank()) {
                appendLine("• দাফন: $graveyardVenue")
            }
            if (contactPhone.isNotBlank()) {
                appendLine("• যোগাযোগ: $contactPhone")
            }
            appendLine()
            appendLine(extraNotes)
            appendLine("------------------------------------------")
            appendLine("মসজিদ সেবা (Mosque Seba) জরুরি সম্প্রচার")
        }.trim()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        MehrabArchCard(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 620.dp)
                .fillMaxHeight(0.92f),
            backgroundColor = MaterialTheme.colorScheme.surface,
            borderColor = MosqueUrgentRed
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
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = MosqueUrgentRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "জরুরি জানাজা ও মৃত্যু সংবাদ সম্প্রচার",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MosqueUrgentRed
                            )
                        }
                        Text(
                            text = "মহল্লার মুসল্লি ও এলাকাবাসীর নিকট তাৎক্ষণিক নোটিশ ও ব্রডকাস্ট",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Inna Lillahi Header Banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MosqueUrgentRed.copy(alpha = if (isDark) 0.15f else 0.08f),
                    border = BorderStroke(1.dp, MosqueUrgentRed.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "إِنَّا لِلَّهِ وَإِنَّا إِلَيْهِ رَاجِعُونَ",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MosqueUrgentRed
                        )
                        Text(
                            text = "নিশ্চয়ই আমরা আল্লাহর এবং আমরা তাঁরই দিকে প্রত্যাবর্তনকারী",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Form Scroll
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = deceasedName,
                        onValueChange = { deceasedName = it },
                        label = { Text("মরহুম / মরহুমার নাম *") },
                        placeholder = { Text("যেমন: আলহাজ্ব মোঃ রফিকুল ইসলাম") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = relativeRelation,
                            onValueChange = { relativeRelation = it },
                            label = { Text("সম্পর্ক") },
                            modifier = Modifier.weight(0.7f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = guardianName,
                            onValueChange = { guardianName = it },
                            label = { Text("পিতা / স্বামীর নাম") },
                            modifier = Modifier.weight(1.3f),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = age,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) age = it },
                            label = { Text("বয়স (বছর)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.8f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = villageArea,
                            onValueChange = { villageArea = it },
                            label = { Text("গ্রাম / মহল্লা / বাড়ি") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = timeOfDeath,
                        onValueChange = { timeOfDeath = it },
                        label = { Text("ইন্তেকালের সময় *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = janazaVenue,
                            onValueChange = { janazaVenue = it },
                            label = { Text("নামাজে জানাজার স্থান *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = janazaTime,
                            onValueChange = { janazaTime = it },
                            label = { Text("জানাজার সময় *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = graveyardVenue,
                            onValueChange = { graveyardVenue = it },
                            label = { Text("দাফনের স্থান (কবরস্থান)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = { Text("যোগাযোগের মোবাইল নম্বর") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = extraNotes,
                        onValueChange = { extraNotes = it },
                        label = { Text("বিশেষ আহ্বান / বার্তা") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MosqueCardBorderSubtle)
                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons
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
                        Text("বাতিল", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            if (deceasedName.isBlank()) {
                                MosqueToastController.warning("মরহুমের নাম লিখুন।")
                                return@OutlinedButton
                            }
                            val fakeNotice = NoticeEntity(
                                id = "janaza-${UUID.randomUUID().toString().take(6)}",
                                title = "জরুরি জানাজার নোটিশ: মরহুম $deceasedName-এর ইন্তেকাল",
                                content = formattedBroadcastMessage,
                                priority = "urgent",
                                isPinned = true,
                                publishDate = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("bn-BD")).format(Date()),
                                createdBy = mosqueName,
                                createdAt = System.currentTimeMillis(),
                                imageUrl = null
                            )
                            MosquePrintHelper.printNotice(
                                context = context,
                                mosqueName = mosqueName,
                                notice = fakeNotice,
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
                        Text("পোস্টার প্রিন্ট", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            if (deceasedName.isBlank()) {
                                MosqueToastController.warning("মরহুমের নাম লিখুন।")
                                return@OutlinedButton
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "জরুরি জানাজা সংবাদ: মরহুম $deceasedName")
                                putExtra(Intent.EXTRA_TEXT, formattedBroadcastMessage)
                            }
                            context.startActivity(Intent.createChooser(intent, "জানাজার সংবাদ হোয়াটসঅ্যাপ/এসএমএসে শেয়ার করুন"))
                        },
                        modifier = Modifier
                            .weight(1.1f)
                            .heightIn(min = 46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("মেসেজ শেয়ার", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (deceasedName.isBlank()) {
                                MosqueToastController.warning("মরহুমের নাম আবশ্যক।")
                                return@Button
                            }
                            if (janazaTime.isBlank() || janazaVenue.isBlank()) {
                                MosqueToastController.warning("জানাজার সময় ও স্থান উল্লেখ করুন।")
                                return@Button
                            }
                            onBroadcastConfirm(
                                deceasedName, relativeRelation, guardianName, age, villageArea,
                                timeOfDeath, janazaVenue, janazaTime, graveyardVenue, contactPhone, extraNotes
                            )
                            MosqueToastController.success(
                                "জানাজার জরুরি নোটিশ অ্যাপে পিন করা হয়েছে।",
                                "জানাজা সম্প্রচার সফল"
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .heightIn(min = 46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MosqueUrgentRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("জরুরি সম্প্রচার", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
