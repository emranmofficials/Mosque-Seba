package com.mosqueseba.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.ui.theme.*

@Composable
fun DashboardKpiGrid(
    totalMembers: Int,
    netBalance: Double,
    dueChanda: Double = 3500.0,
    pendingItems: Int = 1,
    onKpiClick: (String) -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCardItem(
                modifier = Modifier.weight(1f),
                title = "মোট সদস্য",
                value = "$totalMembers জন",
                subtext = "৯ জন সক্রিয় পরিষদ",
                icon = Icons.Default.Groups,
                iconBg = MosqueEmeraldPrimary.copy(alpha = 0.1f),
                iconColor = MosqueEmeraldPrimary,
                onClick = { onKpiClick("members") }
            )

            KpiCardItem(
                modifier = Modifier.weight(1f),
                title = "তহবিল ব্যালেন্স",
                value = "৳${String.format("%,.0f", netBalance)}",
                subtext = "বর্তমান নেট স্থিতি",
                icon = Icons.Default.AccountBalanceWallet,
                iconBg = MosqueSuccessGreen.copy(alpha = 0.1f),
                iconColor = MosqueSuccessGreen,
                onClick = { onKpiClick("finance") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCardItem(
                modifier = Modifier.weight(1f),
                title = "বকেয়া চাঁদা",
                value = "৳${String.format("%,.0f", dueChanda)}",
                subtext = "চলতি সেপ্টেম্বর মাস",
                icon = Icons.Default.PendingActions,
                iconBg = MosqueWarningAmber.copy(alpha = 0.1f),
                iconColor = MosqueWarningAmber,
                onClick = { onKpiClick("chanda") }
            )

            KpiCardItem(
                modifier = Modifier.weight(1f),
                title = "অমীমাংসিত বিষয়",
                value = "$pendingItems টি অনুমোদন",
                subtext = "অনুমোদনের অপেক্ষায়",
                icon = Icons.Default.AssignmentLate,
                iconBg = MosqueUrgentRed.copy(alpha = 0.1f),
                iconColor = MosqueUrgentRed,
                onClick = { onKpiClick("pending") }
            )
        }
    }
}

@Composable
fun KpiCardItem(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MosqueTextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MosqueTextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = MosqueTextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun QuickActionsGrid(
    onActionClick: (String) -> Unit
) {
    val actions = listOf(
        QuickActionItem("add_member", "নতুন সদস্য", Icons.Default.PersonAdd, MosqueEmeraldPrimary),
        QuickActionItem("add_income", "আয় যোগ", Icons.Default.AddCircle, MosqueSuccessGreen),
        QuickActionItem("add_expense", "ব্যয় যোগ", Icons.Default.RemoveCircle, MosqueUrgentRed),
        QuickActionItem("receive_chanda", "চাঁদা গ্রহণ", Icons.Default.Payments, MosqueGoldDeep),
        QuickActionItem("schedule_meeting", "সভা আহ্বান", Icons.Default.EventAvailable, MosqueInfoBlue),
        QuickActionItem("publish_notice", "নোটিশ প্রকাশ", Icons.Default.Campaign, MosqueEmeraldDark)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "দ্রুত কার্যক্রম (Quick Actions)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MosqueEmeraldDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2 rows x 3 columns
            for (rowIndex in 0..1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (colIndex in 0..2) {
                        val item = actions[rowIndex * 3 + colIndex]
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MosqueBgLight)
                                .clickable { onActionClick(item.key) }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(item.color.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = item.color,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MosqueTextPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }
                if (rowIndex == 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

data class QuickActionItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val color: Color
)
