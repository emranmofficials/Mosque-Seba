package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class PrayerTimeItem(
    val nameBn: String,
    val azanTime: String,
    val iqamahTime: String,
    val isNext: Boolean = false
)

@Composable
fun PrayerCard(
    onViewFullScheduleClick: () -> Unit = {}
) {
    val prayers = listOf(
        PrayerTimeItem("ফজর", "০৪:৩০", "০৫:০০"),
        PrayerTimeItem("যোহর/জুমা", "১২:৩০", "০১:১৫", isNext = true),
        PrayerTimeItem("আসর", "০৪:৩০", "০৪:৪৫"),
        PrayerTimeItem("মাগরিব", "০৬:১৮", "০৬:২৩"),
        PrayerTimeItem("ইশা", "০৭:৪৫", "০৮:১৫")
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Card Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MosqueEmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "আজকের নামাজের সময়সূচি",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MosqueEmeraldDark
                    )
                }

                TextButton(
                    onClick = onViewFullScheduleClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "পূর্ণ সূচি",
                        style = MaterialTheme.typography.labelSmall,
                        color = MosqueEmeraldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5 Prayer Rows in Horizontal Scroll or Compact Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                prayers.forEach { item ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (item.isNext) MosqueEmeraldPrimary else MosqueBgLight
                            )
                            .border(
                                width = if (item.isNext) 1.dp else 0.dp,
                                color = if (item.isNext) MosqueGold else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(vertical = 8.dp, horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = item.nameBn,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (item.isNext) MosqueGold else MosqueTextPrimary,
                            fontWeight = if (item.isNext) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.iqamahTime,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (item.isNext) Color.White else MosqueEmeraldDark,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "আজান ${item.azanTime}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = if (item.isNext) Color.White.copy(alpha = 0.8f) else MosqueTextMuted
                        )
                    }
                    if (item != prayers.last()) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Next prayer callout banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MosqueGoldLight)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "পরবর্তী জামাত: জুমুআ / যোহর (০১:১৫)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MosqueEmeraldDark,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "বাকি আছে: ৪২ মিনিট",
                    style = MaterialTheme.typography.labelSmall,
                    color = MosqueEmeraldPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
