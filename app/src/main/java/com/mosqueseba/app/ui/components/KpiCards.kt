package com.mosqueseba.app.ui.components

import androidx.compose.foundation.BorderStroke
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
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.theme.*

@Composable
fun DashboardKpiGrid(
    role: UserRole = UserRole.PRESIDENT,
    totalMembers: Int,
    netBalance: Double,
    dueChanda: Double = 3500.0,
    pendingItems: Int = 1,
    onKpiClick: (String) -> Unit = {}
) {
    when (role) {
        UserRole.GENERAL_MEMBER -> {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCardItem(
                        modifier = Modifier.weight(1f),
                        title = "আমার মাসিক চাঁদা",
                        value = "৳১,৫০০",
                        subtext = "পরিশোধিত • সেপ্টেম্বর ২০২৪",
                        icon = Icons.Default.Verified,
                        iconBg = MosqueSuccessGreen.copy(alpha = 0.1f),
                        iconColor = MosqueSuccessGreen,
                        onClick = { onKpiClick("my_chanda") }
                    )
                    KpiCardItem(
                        modifier = Modifier.weight(1f),
                        title = "মসজিদ স্বচ্ছ তহবিল",
                        value = "৳${String.format("%,.0f", netBalance)}",
                        subtext = "সার্বজনীন নেট স্থিতি",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconBg = MosqueEmeraldPrimary.copy(alpha = 0.1f),
                        iconColor = MosqueEmeraldPrimary,
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
                        title = "পরবর্তী জামায়াত",
                        value = "যোহর/জুমুআ",
                        subtext = "০১:১৫ মিনিট (বাকি ৪২ মি.)",
                        icon = Icons.Default.AccessTime,
                        iconBg = MosqueGold.copy(alpha = 0.15f),
                        iconColor = MosqueGoldDeep,
                        onClick = { onKpiClick("prayers") }
                    )
                    KpiCardItem(
                        modifier = Modifier.weight(1f),
                        title = "নতুন নোটিশ",
                        value = "২টি বিজ্ঞপ্তি",
                        subtext = "কমিটি কর্তৃক প্রকাশিত",
                        icon = Icons.Default.Campaign,
                        iconBg = MosqueInfoBlue.copy(alpha = 0.1f),
                        iconColor = MosqueInfoBlue,
                        onClick = { onKpiClick("notices") }
                    )
                }
            }
        }
        UserRole.IMAM, UserRole.MUAZZIN, UserRole.KHADEM -> {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCardItem(
                        modifier = Modifier.weight(1f),
                        title = "আজকের দায়িত্ব",
                        value = "ইমামতি ও খুতবা",
                        subtext = "জুমা ও মাগরিব জামায়াত",
                        icon = Icons.Default.Schedule,
                        iconBg = MosqueEmeraldPrimary.copy(alpha = 0.1f),
                        iconColor = MosqueEmeraldPrimary,
                        onClick = { onKpiClick("roster") }
                    )
                    KpiCardItem(
                        modifier = Modifier.weight(1f),
                        title = "খাবার শিডিউল",
                        value = "দুপুর ও রাত",
                        subtext = "হাজী মো: আবুল কাসেম",
                        icon = Icons.Default.Restaurant,
                        iconBg = MosqueGold.copy(alpha = 0.15f),
                        iconColor = MosqueGoldDeep,
                        onClick = { onKpiClick("food") }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCardItem(
                        modifier = Modifier.weight(1f),
                        title = "মাদরাসা শিক্ষার্থী",
                        value = "১৮ জন ছাত্র",
                        subtext = "হিফজ ও নূরানী বিভাগ",
                        icon = Icons.Default.School,
                        iconBg = MosqueInfoBlue.copy(alpha = 0.1f),
                        iconColor = MosqueInfoBlue,
                        onClick = { onKpiClick("madrasa") }
                    )
                    KpiCardItem(
                        modifier = Modifier.weight(1f),
                        title = "আমার বেতন স্ট্যাটাস",
                        value = "পরিশোধিত",
                        subtext = "সেপ্টেম্বর ২০২৪",
                        icon = Icons.Default.CheckCircle,
                        iconBg = MosqueSuccessGreen.copy(alpha = 0.1f),
                        iconColor = MosqueSuccessGreen,
                        onClick = { onKpiClick("staff_profile") }
                    )
                }
            }
        }
        UserRole.TREASURER -> {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCardItem(
                        modifier = Modifier.weight(1f),
                        title = "নগদ ও ব্যাংক তহবিল",
                        value = "৳${String.format("%,.0f", netBalance)}",
                        subtext = "বর্তমান নেট ক্যাশ স্থিতি",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconBg = MosqueSuccessGreen.copy(alpha = 0.1f),
                        iconColor = MosqueSuccessGreen,
                        onClick = { onKpiClick("finance") }
                    )
                    KpiCardItem(
                        modifier = Modifier.weight(1f),
                        title = "বকেয়া চাঁদা রেজিস্টার",
                        value = "৳${String.format("%,.0f", dueChanda)}",
                        subtext = "চলতি মাস আদায়যোগ্য",
                        icon = Icons.Default.PendingActions,
                        iconBg = MosqueWarningAmber.copy(alpha = 0.1f),
                        iconColor = MosqueWarningAmber,
                        onClick = { onKpiClick("treasurer_chanda") }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCardItem(
                        modifier = Modifier.weight(1f),
                        title = "অপেক্ষমান ব্যয় ভাউচার",
                        value = "$pendingItems টি ভাউচার",
                        subtext = "অনুমোদনের অপেক্ষায়",
                        icon = Icons.Default.AssignmentLate,
                        iconBg = MosqueUrgentRed.copy(alpha = 0.1f),
                        iconColor = MosqueUrgentRed,
                        onClick = { onKpiClick("pending") }
                    )
                    KpiCardItem(
                        modifier = Modifier.weight(1f),
                        title = "স্টাফ বেতন ও পে-রোল",
                        value = "৪ জন স্টাফ",
                        subtext = "চলতি মাসের বেতন প্রস্তুত",
                        icon = Icons.Default.Badge,
                        iconBg = MosqueEmeraldPrimary.copy(alpha = 0.1f),
                        iconColor = MosqueEmeraldPrimary,
                        onClick = { onKpiClick("treasurer_payroll") }
                    )
                }
            }
        }
        UserRole.GENERAL_SECRETARY, UserRole.ORGANIZING_SECRETARY, UserRole.EXECUTIVE_MEMBER -> {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCardItem(
                        modifier = Modifier.weight(1f),
                        title = "নিবন্ধিত সদস্য পরিষদ",
                        value = "$totalMembers জন",
                        subtext = "৯ জন কার্যনির্বাহী পরিষদ",
                        icon = Icons.Default.Groups,
                        iconBg = MosqueEmeraldPrimary.copy(alpha = 0.1f),
                        iconColor = MosqueEmeraldPrimary,
                        onClick = { onKpiClick("members") }
                    )
                    KpiCardItem(
                        modifier = Modifier.weight(1f),
                        title = "আসন্ন কমিটি সভা",
                        value = "১৫ সেপ্টেম্বর",
                        subtext = "বাদ আসর • সাধারণ সভা",
                        icon = Icons.Default.EventAvailable,
                        iconBg = MosqueInfoBlue.copy(alpha = 0.1f),
                        iconColor = MosqueInfoBlue,
                        onClick = { onKpiClick("meetings") }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCardItem(
                        modifier = Modifier.weight(1f),
                        title = "সক্রিয় সার্কুলার/নোটিশ",
                        value = "২টি নোটিশ",
                        subtext = "সাধারণ ও জরুরি ঘোষণা",
                        icon = Icons.Default.Campaign,
                        iconBg = MosqueGold.copy(alpha = 0.15f),
                        iconColor = MosqueGoldDeep,
                        onClick = { onKpiClick("notices") }
                    )
                    KpiCardItem(
                        modifier = Modifier.weight(1f),
                        title = "সমাজকল্যাণ তহবিল",
                        value = "৩টি আবেদন",
                        subtext = "চিকিৎসা ও ত্রাণ অনুদান",
                        icon = Icons.Default.VolunteerActivism,
                        iconBg = MosqueSuccessGreen.copy(alpha = 0.1f),
                        iconColor = MosqueSuccessGreen,
                        onClick = { onKpiClick("welfare") }
                    )
                }
            }
        }
        else -> {
            Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCardItem(
                modifier = Modifier.weight(1f),
                title = "মোট সদস্য পরিষদ",
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
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 5.dp),
        border = BorderStroke(1.dp, MosqueCardBorderGold.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
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

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
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
    role: UserRole = UserRole.PRESIDENT,
    onActionClick: (String) -> Unit
) {
    val actions = when (role) {
        UserRole.GENERAL_MEMBER -> listOf(
            QuickActionItem("my_chanda", "আমার চাঁদা", Icons.Default.ReceiptLong, MosqueGoldDeep),
            QuickActionItem("view_receipt", "ডিজিটাল রসিদ", Icons.Default.QrCode2, MosqueEmeraldPrimary),
            QuickActionItem("transparency", "স্বচ্ছ তহবিল", Icons.Default.AccountBalanceWallet, MosqueSuccessGreen),
            QuickActionItem("view_prayers", "নামাজের সূচি", Icons.Default.AccessTime, MosqueInfoBlue),
            QuickActionItem("view_notices", "নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark),
            QuickActionItem("welfare_apply", "সহায়তা আবেদন", Icons.Default.VolunteerActivism, MosqueUrgentRed)
        )
        UserRole.IMAM, UserRole.MUAZZIN, UserRole.KHADEM -> listOf(
            QuickActionItem("roster", "ডিউটি রোস্টার", Icons.Default.Schedule, MosqueEmeraldPrimary),
            QuickActionItem("food", "খাবার শিডিউল", Icons.Default.Restaurant, MosqueGoldDeep),
            QuickActionItem("madrasa", "হিফজ মূল্যায়ন", Icons.Default.School, MosqueInfoBlue),
            QuickActionItem("view_prayers", "নামাজের সময়", Icons.Default.AccessTime, MosqueSuccessGreen),
            QuickActionItem("view_notices", "নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark),
            QuickActionItem("staff_profile", "বেতন স্লিপ", Icons.Default.AccountBalanceWallet, MosqueUrgentRed)
        )
        UserRole.TREASURER -> listOf(
            QuickActionItem("add_income", "আয় এন্ট্রি", Icons.Default.AddCircle, MosqueSuccessGreen),
            QuickActionItem("add_expense", "ব্যয় ভাউচার", Icons.Default.RemoveCircle, MosqueUrgentRed),
            QuickActionItem("receive_chanda", "চাঁদা গ্রহণ", Icons.Default.Payments, MosqueGoldDeep),
            QuickActionItem("treasurer_payroll", "বেতন বিতরণ", Icons.Default.Badge, MosqueEmeraldPrimary),
            QuickActionItem("madrasa_fee", "মাদরাসা ফি", Icons.Default.School, MosqueInfoBlue),
            QuickActionItem("treasurer_report", "হিসাব রিপোর্ট", Icons.Default.Assessment, MosqueEmeraldDark)
        )
        UserRole.GENERAL_SECRETARY, UserRole.ORGANIZING_SECRETARY, UserRole.EXECUTIVE_MEMBER -> listOf(
            QuickActionItem("add_member", "নতুন সদস্য", Icons.Default.PersonAdd, MosqueEmeraldPrimary),
            QuickActionItem("schedule_meeting", "সভা আহ্বান", Icons.Default.EventAvailable, MosqueInfoBlue),
            QuickActionItem("publish_notice", "নোটিশ প্রকাশ", Icons.Default.Campaign, MosqueEmeraldDark),
            QuickActionItem("meetings", "কার্যবিবরণী", Icons.Default.Assignment, MosqueGoldDeep),
            QuickActionItem("welfare", "সমাজকল্যাণ", Icons.Default.VolunteerActivism, MosqueSuccessGreen),
            QuickActionItem("events", "ইভেন্ট মাহফিল", Icons.Default.Celebration, MosqueGold)
        )
        else -> listOf(
            QuickActionItem("pending_approval", "ভাউচার অনুমোদন", Icons.Default.AssignmentTurnedIn, MosqueUrgentRed),
            QuickActionItem("schedule_meeting", "সভা আহ্বান", Icons.Default.EventAvailable, MosqueInfoBlue),
            QuickActionItem("publish_notice", "নোটিশ জারি", Icons.Default.Campaign, MosqueEmeraldDark),
            QuickActionItem("welfare", "অনুদান অনুমোদন", Icons.Default.VolunteerActivism, MosqueSuccessGreen),
            QuickActionItem("audit", "অডিট নিরীক্ষা", Icons.Default.FactCheck, MosqueWarningAmber),
            QuickActionItem("members", "সদস্য পরিষদ", Icons.Default.Groups, MosqueEmeraldPrimary)
        )
    }

    MehrabArchCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White,
        borderColor = MosqueCardBorderGold.copy(alpha = 0.6f),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "দায়িত্ব ও দ্রুত কার্যক্রম (Quick Actions)",
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
                                .clip(RoundedCornerShape(14.dp))
                                .background(MosqueBgLight)
                                .border(1.dp, MosqueCardBorderSubtle, RoundedCornerShape(14.dp))
                                .clickable { onActionClick(item.key) }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
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
