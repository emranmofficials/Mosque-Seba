package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SmartBoardCard() {
    var pageIndex by remember { mutableIntStateOf(0) }
    var currentTimeStr by remember { mutableStateOf("") }

    // Update time and rotate pages every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTimeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now)
            delay(5000)
            pageIndex = (pageIndex + 1) % 4
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MosqueGoldLight),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MosqueGold))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Smart Board label + Dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MosqueEmeraldPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "স্মার্ট নোটিশ ও দিনপঞ্জি বোর্ড",
                        style = MaterialTheme.typography.labelSmall,
                        color = MosqueEmeraldDark,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Page indicator pills
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 0 until 4) {
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(if (i == pageIndex) 16.dp else 6.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (i == pageIndex) MosqueEmeraldPrimary else MosqueEmeraldPrimary.copy(alpha = 0.25f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Animated content switch
            AnimatedContent(
                targetState = pageIndex,
                transitionSpec = {
                    fadeIn() + slideInVertically { it / 2 } togetherWith fadeOut() + slideOutVertically { -it / 2 }
                },
                label = "SmartBoardRotation"
            ) { targetPage ->
                when (targetPage) {
                    0 -> {
                        // Date & Hijri calendar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = MosqueEmeraldPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "২৩ ভাদ্র ১৪৩১ বঙ্গাব্দ • ০৩ রবিউল আউয়াল ১৪৪৬ হিজরি",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MosqueEmeraldDark,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "শুক্রবার, ০৭ সেপ্টেম্বর ২০২৪ • সময়: $currentTimeStr (সকাল/দুপুর)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueTextSecondary
                                )
                            }
                        }
                    }
                    1 -> {
                        // Urgent notice
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = MosqueUrgentRed,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "পবিত্র জুমার প্রস্তুতি ও বয়ান মাহফিল",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MosqueUrgentRed,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "আজ বেলা ১২:৩০ মিনিটে খুতবার পূর্ব বয়ান শুরু হবে। সকলে দ্রুত আসুন।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueTextPrimary
                                )
                            }
                        }
                    }
                    2 -> {
                        // Next Prayer Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MosqueEmeraldPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "পরবর্তী ওয়াক্ত: জুমুআ / যোহর (আজ দুপুর ০১:১৫ জামাত)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MosqueEmeraldDark,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "জুমার আজান: দুপুর ১২:৩০ • খুতবা ও নামাজ: দুপুর ০১:১৫",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueTextSecondary
                                )
                            }
                        }
                    }
                    else -> {
                        // Daily Hadith reminder
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = MosqueEmeraldLight,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "নিয়মিত জামাতে নামাজ আদায়ের ফজিলত",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MosqueEmeraldDark,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "\"যে ব্যক্তি আল্লাহর উদ্দেশ্যে মসজিদ নির্মাণ করে, আল্লাহ তার জন্য জান্নাতে ঘর বানান।\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MosqueTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
