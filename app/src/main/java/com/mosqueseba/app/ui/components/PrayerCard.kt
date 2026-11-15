package com.mosqueseba.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.ui.theme.*
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

data class PrayerTimeItem(
    val nameBn: String,
    val azanTime: String,
    val iqamahTime: String,
    val iqamahLocalTime: LocalTime,
    val isNext: Boolean = false
)

data class NextPrayerSummary(
    val nameBn: String,
    val iqamahTimeBn: String,
    val remainingTextBn: String
)

fun computeTodayPrayerSchedule(now: LocalTime = LocalTime.now()): Pair<List<PrayerTimeItem>, NextPrayerSummary> {
    val isFriday = LocalDate.now().dayOfWeek == DayOfWeek.FRIDAY
    val dhuhrLabel = if (isFriday) "জুমুআ" else "যোহর"
    val base = listOf(
        PrayerTimeItem("ফজর", "০৪:৩০", "০৫:০০", LocalTime.of(5, 0)),
        PrayerTimeItem(dhuhrLabel, "১২:৩০", "০১:১৫", LocalTime.of(13, 15)),
        PrayerTimeItem("আসর", "০৪:৩০", "০৪:৪৫", LocalTime.of(16, 45)),
        PrayerTimeItem("মাগরিব", "০৬:১৮", "০৬:২৩", LocalTime.of(18, 23)),
        PrayerTimeItem("ইশা", "০৭:৪৫", "০৮:১৫", LocalTime.of(20, 15))
    )

    val nextIndex = base.indexOfFirst { it.iqamahLocalTime.isAfter(now) }.let {
        if (it == -1) 0 else it
    }
    val nextPrayer = base[nextIndex]
    val minutesUntil = if (nextPrayer.iqamahLocalTime.isAfter(now)) {
        Duration.between(now, nextPrayer.iqamahLocalTime).toMinutes()
    } else {
        (24 * 60) - Duration.between(nextPrayer.iqamahLocalTime, now).toMinutes()
    }.coerceAtLeast(1)

    val hours = minutesUntil / 60
    val mins = minutesUntil % 60
    val remainingBn = if (hours > 0) {
        toBengaliDigits("${hours} ঘণ্টা ${mins} মিনিট")
    } else {
        toBengaliDigits("$mins মিনিট")
    }

    val updatedList = base.mapIndexed { index, item ->
        item.copy(isNext = index == nextIndex)
    }
    return updatedList to NextPrayerSummary(
        nameBn = nextPrayer.nameBn,
        iqamahTimeBn = nextPrayer.iqamahTime,
        remainingTextBn = remainingBn
    )
}

@Composable
fun PrayerCard(
    onViewFullScheduleClick: () -> Unit = {}
) {
    val (prayers, nextSummary) = computeTodayPrayerSchedule()
    val isDark = LocalIsDarkTheme.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val tileInactiveBg = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight
    val tileInactiveBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
    val secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val subtitleHighlight = if (isDark) MosqueGoldLight else MosqueGoldDark

    val infiniteTransition = rememberInfiniteTransition(label = "NextPrayerPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    MehrabArchCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MosqueCardBorderGold.copy(alpha = 0.6f),
        elevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Card Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "আজকের নামাজের সময়সূচি",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(subtitleHighlight.copy(alpha = pulseAlpha))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "পরবর্তী ${nextSummary.nameBn} • বাকি ${nextSummary.remainingTextBn}",
                                style = MaterialTheme.typography.labelSmall,
                                color = subtitleHighlight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                TextButton(
                    onClick = onViewFullScheduleClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "পূর্ণ সূচি",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5 Prayer Rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                prayers.forEach { item ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (item.isNext) {
                                    Modifier
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    MosqueEmeraldPrimary,
                                                    MosqueEmeraldDark
                                                )
                                            )
                                        )
                                        .border(
                                            width = 1.5.dp,
                                            brush = Brush.verticalGradient(
                                                listOf(
                                                    MosqueGoldLight.copy(alpha = pulseAlpha),
                                                    MosqueGoldDeep
                                                )
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                } else {
                                    Modifier
                                        .background(tileInactiveBg)
                                        .border(
                                            width = 1.dp,
                                            color = tileInactiveBorder,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                }
                            )
                            .padding(vertical = 8.dp, horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (item.isNext) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(MosqueGold.copy(alpha = pulseAlpha))
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        Text(
                            text = item.nameBn,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (item.isNext) MosqueGold else secondaryColor,
                            fontWeight = if (item.isNext) FontWeight.ExtraBold else FontWeight.Medium,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.iqamahTime,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (item.isNext) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "আজান ${item.azanTime}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = if (item.isNext) Color.White.copy(alpha = 0.85f) else secondaryColor.copy(alpha = 0.85f)
                        )
                    }
                    if (item != prayers.last()) {
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                }
            }
        }
    }
}
