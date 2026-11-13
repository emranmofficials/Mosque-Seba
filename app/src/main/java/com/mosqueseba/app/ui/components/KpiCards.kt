package com.mosqueseba.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.theme.*
import java.util.Locale

@Composable
fun DashboardKpiGrid(
    role: UserRole = UserRole.PRESIDENT,
    totalMembers: Int,
    netBalance: Double,
    dueChanda: Double = 0.0,
    pendingItems: Int = 0,
    studentCount: Int = 0,
    staffCount: Int = 0,
    noticeCount: Int = 0,
    welfareCount: Int = 0,
    cashboxCount: Int = 0,
    pendingCashboxWitnessCount: Int = 0,
    officeDocCount: Int = 0,
    pendingReplyDocCount: Int = 0,
    advisoryCount: Int = 0,
    smartQuoteCount: Int = 0,
    nextMeetingTitleDate: String = "নির্ধারিত নেই",
    nextMeetingSubtitle: String = "কোনো আসন্ন সভা নেই",
    myChandaPaid: Double = 0.0,
    myChandaSubtitle: String = "চলতি মাস",
    nextPrayerName: String = "যোহর",
    nextPrayerSubtitle: String = "",
    onKpiClick: (String) -> Unit = {}
) {
    when (role) {
        UserRole.GENERAL_MEMBER -> {
            KpiTwoByTwo(
                c1 = KpiSpec("আমার মাসিক চাঁদা", "৳${String.format(Locale.getDefault(), "%,.0f", myChandaPaid)}", myChandaSubtitle, Icons.Default.Verified, MosqueSuccessGreen, "my_chanda"),
                c2 = KpiSpec("মসজিদ স্বচ্ছ তহবিল", "৳${String.format(Locale.getDefault(), "%,.0f", netBalance)}", "সার্বজনীন নেট স্থিতি", Icons.Default.AccountBalanceWallet, MosqueEmeraldPrimary, "finance"),
                c3 = KpiSpec("পরবর্তী জামায়াত", nextPrayerName, nextPrayerSubtitle, Icons.Default.AccessTime, MosqueGoldDeep, "prayers"),
                c4 = KpiSpec("নতুন নোটিশ", "${toBengaliDigits(noticeCount.toString())}টি বিজ্ঞপ্তি", "কমিটি কর্তৃক প্রকাশিত", Icons.Default.Campaign, MosqueInfoBlue, "notices"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.IMAM -> {
            KpiTwoByTwo(
                c1 = KpiSpec("শরিয়াহ পর্যালোচনা", "${toBengaliDigits(advisoryCount.toString())} টি মতামত", "পদাধিকারবলে দায়িত্ব", Icons.Default.MenuBook, MosqueEmeraldPrimary, "sharia_advisory"),
                c2 = KpiSpec("আজকের দায়িত্ব", "ইমামতি ও খুতবা", "৫ ওয়াক্ত ও জুমার রোস্টার", Icons.Default.Schedule, MosqueGoldDeep, "roster"),
                c3 = KpiSpec("মাদরাসা শিক্ষার্থী", "${toBengaliDigits(studentCount.toString())} জন ছাত্র", "হিফজ ও নূরানী বিভাগ", Icons.Default.School, MosqueInfoBlue, "madrasa"),
                c4 = KpiSpec("আমার বেতন স্ট্যাটাস", "পরিশোধিত", "বেতন স্লিপ ও রেকর্ড", Icons.Default.CheckCircle, MosqueSuccessGreen, "staff_profile"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.MUAZZIN, UserRole.KHADEM -> {
            KpiTwoByTwo(
                c1 = KpiSpec("আজকের দায়িত্ব", "আজান ও খেদমত", "৫ ওয়াক্ত ডিউটি রোস্টার", Icons.Default.Schedule, MosqueEmeraldPrimary, "roster"),
                c2 = KpiSpec("খাবার শিডিউল", "দুপুর ও রাত", "দৈনিক মেহমানদারি তালিকা", Icons.Default.Restaurant, MosqueGoldDeep, "food"),
                c3 = KpiSpec("মাদরাসা শিক্ষার্থী", "${toBengaliDigits(studentCount.toString())} জন ছাত্র", "হিফজ ও নূরানী বিভাগ", Icons.Default.School, MosqueInfoBlue, "madrasa"),
                c4 = KpiSpec("আমার বেতন স্ট্যাটাস", "পরিশোধিত", "বেতন স্লিপ ও রেকর্ড", Icons.Default.CheckCircle, MosqueSuccessGreen, "staff_profile"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.TREASURER, UserRole.ASSISTANT_TREASURER -> {
            KpiTwoByTwo(
                c1 = KpiSpec("নগদ ও ব্যাংক তহবিল", "৳${String.format(Locale.getDefault(), "%,.0f", netBalance)}", "বর্তমান নেট ক্যাশ স্থিতি", Icons.Default.AccountBalanceWallet, MosqueSuccessGreen, "finance"),
                c2 = KpiSpec("দান বাক্স (ক্যাশবক্স)", "${toBengaliDigits(cashboxCount.toString())} টি গণনা", "${toBengaliDigits(pendingCashboxWitnessCount.toString())} টি সাক্ষ্য বাকি", Icons.Default.Inventory2, MosqueGoldDeep, "cashbox"),
                c3 = KpiSpec("বকেয়া চাঁদা রেজিস্টার", "৳${String.format(Locale.getDefault(), "%,.0f", dueChanda)}", "চলতি মাস আদায়যোগ্য", Icons.Default.PendingActions, MosqueWarningAmber, "treasurer_chanda"),
                c4 = KpiSpec("স্টাফ বেতন ও পে-রোল", "${toBengaliDigits(staffCount.toString())} জন স্টাফ", "অপেক্ষমান ভাউচার ${toBengaliDigits(pendingItems.toString())}টি", Icons.Default.Badge, MosqueEmeraldPrimary, "treasurer_payroll"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.PUBLICITY_SECRETARY -> {
            KpiTwoByTwo(
                c1 = KpiSpec("স্মার্ট বোর্ড ও উক্তি", "${toBengaliDigits(smartQuoteCount.toString())} টি বার্তা", "দেয়াল উক্তি ও নির্দেশনা", Icons.Default.FormatQuote, RolePublicityAccent, "publicity"),
                c2 = KpiSpec("প্রকাশিত নোটিশ", "${toBengaliDigits(noticeCount.toString())} টি বিজ্ঞপ্তি", "জরুরি ও সাধারণ প্রচার", Icons.Default.Campaign, MosqueEmeraldPrimary, "notices"),
                c3 = KpiSpec("মাহফিল ও অনুষ্ঠান", "আয়োজন ও প্রচার", "ঈদে মিলাদুন্নবী ও শবে কদর", Icons.Default.Celebration, MosqueGoldDeep, "events"),
                c4 = KpiSpec("আসন্ন কমিটি সভা", nextMeetingTitleDate, nextMeetingSubtitle, Icons.Default.EventAvailable, MosqueInfoBlue, "meetings"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.OFFICE_SECRETARY -> {
            KpiTwoByTwo(
                c1 = KpiSpec("দপ্তর ও দলিল ফাইল", "${toBengaliDigits(officeDocCount.toString())} টি নথি", "ওয়াকফ দলিল ও চুক্তিপত্র", Icons.Default.FolderShared, RoleOfficeAccent, "office_docs"),
                c2 = KpiSpec("জবাব অপেক্ষমান চিঠি", "${toBengaliDigits(pendingReplyDocCount.toString())} টি পত্র", "দাপ্তরিক জবাব প্রস্তুত", Icons.Default.MarkEmailUnread, MosqueWarningAmber, "office_docs"),
                c3 = KpiSpec("আসন্ন কমিটি সভা", nextMeetingTitleDate, nextMeetingSubtitle, Icons.Default.EventAvailable, MosqueEmeraldPrimary, "meetings"),
                c4 = KpiSpec("নিবন্ধিত সদস্য", "${toBengaliDigits(totalMembers.toString())} জন", "কমিটি ও সাধারণ তালিকা", Icons.Default.Groups, MosqueInfoBlue, "members"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.WELFARE_EDUCATION_SECRETARY -> {
            KpiTwoByTwo(
                c1 = KpiSpec("মক্তব ও হিফজ শিক্ষার্থী", "${toBengaliDigits(studentCount.toString())} জন ছাত্র", "হাজিরা ও তালিম তদারকি", Icons.Default.School, RoleWelfareEduAccent, "madrasa"),
                c2 = KpiSpec("যাকাত ও সমাজকল্যাণ", "${toBengaliDigits(welfareCount.toString())} টি আবেদন", "চিকিৎসা, ক্যাম্প ও ত্রাণ", Icons.Default.VolunteerActivism, MosqueSuccessGreen, "welfare"),
                c3 = KpiSpec("প্রকাশিত নোটিশ", "${toBengaliDigits(noticeCount.toString())} টি বিজ্ঞপ্তি", "কমিটি সার্কুলার", Icons.Default.Campaign, MosqueGoldDeep, "notices"),
                c4 = KpiSpec("কমিটি সনদ ও স্তর", "১৬ টি পদবি", "৩-স্তরীয় দায়িত্ব কাঠামো", Icons.Default.VerifiedUser, MosqueInfoBlue, "charter"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.EXECUTIVE_MEMBER -> {
            KpiTwoByTwo(
                c1 = KpiSpec("ক্যাশবক্স সাক্ষ্য অপেক্ষায়", "${toBengaliDigits(pendingCashboxWitnessCount.toString())} টি বাক্স", "দান বাক্স গণনা ও ক্রয়ে সাক্ষী", Icons.Default.Verified, MosqueWarningAmber, "cashbox"),
                c2 = KpiSpec("আসন্ন কমিটি সভা", nextMeetingTitleDate, nextMeetingSubtitle, Icons.Default.EventAvailable, MosqueInfoBlue, "meetings"),
                c3 = KpiSpec("সদস্য পরিষদ", "${toBengaliDigits(totalMembers.toString())} জন", "জুমা ও ঈদ শৃঙ্খলা সমন্বয়", Icons.Default.Groups, MosqueEmeraldPrimary, "members"),
                c4 = KpiSpec("মসজিদ স্বচ্ছ তহবিল", "৳${String.format(Locale.getDefault(), "%,.0f", netBalance)}", "উন্মুক্ত নেট স্থিতি", Icons.Default.AccountBalanceWallet, MosqueSuccessGreen, "transparency"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.ADVISORY_BOARD -> {
            KpiTwoByTwo(
                c1 = KpiSpec("উপদেষ্টা ও শরিয়াহ লগ", "${toBengaliDigits(advisoryCount.toString())} টি পরামর্শ", "সংকট ও নির্মাণ নির্দেশনা", Icons.Default.AccountBalance, RoleAdvisoryAccent, "sharia_advisory"),
                c2 = KpiSpec("মসজিদ ফান্ড স্থিতি", "৳${String.format(Locale.getDefault(), "%,.0f", netBalance)}", "স্বচ্ছ আর্থিক অবস্থান", Icons.Default.AccountBalanceWallet, MosqueSuccessGreen, "transparency"),
                c3 = KpiSpec("নিবন্ধিত সদস্য", "${toBengaliDigits(totalMembers.toString())} জন", "কমিটি ও মুসুল্লি পরিষদ", Icons.Default.Groups, MosqueEmeraldPrimary, "members"),
                c4 = KpiSpec("আদর্শ কমিটি সনদ", "৩-স্তরীয় পর্ষদ", "দায়িত্ব ও গঠনতন্ত্র", Icons.Default.VerifiedUser, MosqueInfoBlue, "charter"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.GENERAL_SECRETARY, UserRole.ASSISTANT_SECRETARY, UserRole.ORGANIZING_SECRETARY -> {
            KpiTwoByTwo(
                c1 = KpiSpec("নিবন্ধিত সদস্য পরিষদ", "${toBengaliDigits(totalMembers.toString())} জন", "কার্যনির্বাহী ও সাধারণ সদস্য", Icons.Default.Groups, MosqueEmeraldPrimary, "members"),
                c2 = KpiSpec("আসন্ন কমিটি সভা", nextMeetingTitleDate, nextMeetingSubtitle, Icons.Default.EventAvailable, MosqueInfoBlue, "meetings"),
                c3 = KpiSpec("দপ্তর ও নথি ফাইল", "${toBengaliDigits(officeDocCount.toString())} টি নথি", "জবাব বাকি ${toBengaliDigits(pendingReplyDocCount.toString())} টি", Icons.Default.FolderShared, MosqueGoldDeep, "office_docs"),
                c4 = KpiSpec("সমাজকল্যাণ ও নোটিশ", "${toBengaliDigits(welfareCount.toString())} / ${toBengaliDigits(noticeCount.toString())}", "অনুদান আবেদন ও বিজ্ঞপ্তি", Icons.Default.VolunteerActivism, MosqueSuccessGreen, "welfare"),
                onKpiClick = onKpiClick
            )
        }
        else -> {
            // PRESIDENT & VICE_PRESIDENT
            KpiTwoByTwo(
                c1 = KpiSpec("মোট সদস্য পরিষদ", "${toBengaliDigits(totalMembers.toString())} জন", "নিবন্ধিত সক্রিয় সদস্য", Icons.Default.Groups, MosqueEmeraldPrimary, "members"),
                c2 = KpiSpec("তহবিল ব্যালেন্স", "৳${String.format(Locale.getDefault(), "%,.0f", netBalance)}", "বর্তমান নেট স্থিতি", Icons.Default.AccountBalanceWallet, MosqueSuccessGreen, "finance"),
                c3 = KpiSpec("বকেয়া চাঁদা", "৳${String.format(Locale.getDefault(), "%,.0f", dueChanda)}", "চলতি মাস", Icons.Default.PendingActions, MosqueWarningAmber, "chanda"),
                c4 = KpiSpec("অমীমাংসিত অনুমোদন", "${toBengaliDigits(pendingItems.toString())} টি ভাউচার", "পরামর্শ ও অডিট পর্যবেক্ষণ", Icons.Default.AssignmentLate, MosqueUrgentRed, "pending"),
                onKpiClick = onKpiClick
            )
        }
    }
}

private data class KpiSpec(
    val title: String,
    val value: String,
    val subtext: String,
    val icon: ImageVector,
    val color: Color,
    val key: String
)

@Composable
private fun KpiTwoByTwo(
    c1: KpiSpec,
    c2: KpiSpec,
    c3: KpiSpec,
    c4: KpiSpec,
    onKpiClick: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCardItem(
                modifier = Modifier.weight(1f),
                title = c1.title,
                value = c1.value,
                subtext = c1.subtext,
                icon = c1.icon,
                iconBg = c1.color.copy(alpha = 0.1f),
                iconColor = c1.color,
                onClick = { onKpiClick(c1.key) }
            )
            KpiCardItem(
                modifier = Modifier.weight(1f),
                title = c2.title,
                value = c2.value,
                subtext = c2.subtext,
                icon = c2.icon,
                iconBg = c2.color.copy(alpha = 0.1f),
                iconColor = c2.color,
                onClick = { onKpiClick(c2.key) }
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCardItem(
                modifier = Modifier.weight(1f),
                title = c3.title,
                value = c3.value,
                subtext = c3.subtext,
                icon = c3.icon,
                iconBg = c3.color.copy(alpha = 0.1f),
                iconColor = c3.color,
                onClick = { onKpiClick(c3.key) }
            )
            KpiCardItem(
                modifier = Modifier.weight(1f),
                title = c4.title,
                value = c4.value,
                subtext = c4.subtext,
                icon = c4.icon,
                iconBg = c4.color.copy(alpha = 0.1f),
                iconColor = c4.color,
                onClick = { onKpiClick(c4.key) }
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "KpiCardPressScale"
    )

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
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
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
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
                color = MosqueTextPrimary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = MosqueTextMuted,
                fontSize = 11.sp,
                maxLines = 1
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
            QuickActionItem("charter", "কমিটি সনদ", Icons.Default.VerifiedUser, MosqueGoldDeep)
        )
        UserRole.IMAM -> listOf(
            QuickActionItem("sharia_advisory", "শরিয়াহ মতামত", Icons.Default.MenuBook, MosqueEmeraldPrimary),
            QuickActionItem("roster", "ডিউটি রোস্টার", Icons.Default.Schedule, MosqueGoldDeep),
            QuickActionItem("madrasa", "হিফজ মূল্যায়ন", Icons.Default.School, MosqueInfoBlue),
            QuickActionItem("food", "খাবার শিডিউল", Icons.Default.Restaurant, MosqueSuccessGreen),
            QuickActionItem("view_notices", "নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark),
            QuickActionItem("charter", "কমিটি সনদ", Icons.Default.VerifiedUser, MosqueGoldDeep)
        )
        UserRole.MUAZZIN, UserRole.KHADEM -> listOf(
            QuickActionItem("roster", "ডিউটি রোস্টার", Icons.Default.Schedule, MosqueEmeraldPrimary),
            QuickActionItem("food", "খাবার শিডিউল", Icons.Default.Restaurant, MosqueGoldDeep),
            QuickActionItem("madrasa", "হিফজ মূল্যায়ন", Icons.Default.School, MosqueInfoBlue),
            QuickActionItem("view_prayers", "নামাজের সময়", Icons.Default.AccessTime, MosqueSuccessGreen),
            QuickActionItem("view_notices", "নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark),
            QuickActionItem("staff_profile", "বেতন স্লিপ", Icons.Default.AccountBalanceWallet, MosqueUrgentRed)
        )
        UserRole.TREASURER, UserRole.ASSISTANT_TREASURER -> listOf(
            QuickActionItem("cashbox", "ক্যাশবক্স জমা", Icons.Default.Inventory2, MosqueGoldDeep),
            QuickActionItem("add_income", "আয় এন্ট্রি", Icons.Default.AddCircle, MosqueSuccessGreen),
            QuickActionItem("add_expense", "ব্যয় ভাউচার", Icons.Default.RemoveCircle, MosqueUrgentRed),
            QuickActionItem("receive_chanda", "চাঁদা গ্রহণ", Icons.Default.Payments, MosqueEmeraldPrimary),
            QuickActionItem("treasurer_payroll", "বেতন বিতরণ", Icons.Default.Badge, MosqueInfoBlue),
            QuickActionItem("treasurer_report", "হিসাব রিপোর্ট", Icons.Default.Assessment, MosqueEmeraldDark)
        )
        UserRole.PUBLICITY_SECRETARY -> listOf(
            QuickActionItem("publicity", "স্মার্ট বোর্ড", Icons.Default.ViewCarousel, RolePublicityAccent),
            QuickActionItem("publish_notice", "নোটিশ প্রকাশ", Icons.Default.Campaign, MosqueEmeraldPrimary),
            QuickActionItem("events", "ধর্মীয় অনুষ্ঠান", Icons.Default.Celebration, MosqueGoldDeep),
            QuickActionItem("view_prayers", "নামাজের সূচি", Icons.Default.AccessTime, MosqueInfoBlue),
            QuickActionItem("charter", "কমিটি সনদ", Icons.Default.VerifiedUser, MosqueEmeraldDark),
            QuickActionItem("members", "সদস্য তালিকা", Icons.Default.Groups, MosqueSuccessGreen)
        )
        UserRole.OFFICE_SECRETARY -> listOf(
            QuickActionItem("office_docs", "দলিল ও চিঠি", Icons.Default.FolderShared, RoleOfficeAccent),
            QuickActionItem("meetings", "সভার নথি", Icons.Default.Assignment, MosqueEmeraldPrimary),
            QuickActionItem("view_notices", "নোটিশ বোর্ড", Icons.Default.Campaign, MosqueGoldDeep),
            QuickActionItem("members", "সদস্য ফাইল", Icons.Default.Groups, MosqueInfoBlue),
            QuickActionItem("audit", "অডিট রেকর্ড", Icons.Default.FactCheck, MosqueWarningAmber),
            QuickActionItem("charter", "কমিটি সনদ", Icons.Default.VerifiedUser, MosqueEmeraldDark)
        )
        UserRole.WELFARE_EDUCATION_SECRETARY -> listOf(
            QuickActionItem("madrasa", "মক্তব ও হিফজ", Icons.Default.School, RoleWelfareEduAccent),
            QuickActionItem("welfare", "যাকাত ও ত্রাণ", Icons.Default.VolunteerActivism, MosqueSuccessGreen),
            QuickActionItem("events", "মেডিকেল ক্যাম্প", Icons.Default.MedicalServices, MosqueUrgentRed),
            QuickActionItem("sharia_advisory", "শরিয়াহ নীতি", Icons.Default.MenuBook, MosqueInfoBlue),
            QuickActionItem("view_notices", "নোটিশ দেখুন", Icons.Default.Campaign, MosqueGoldDeep),
            QuickActionItem("charter", "কমিটি সনদ", Icons.Default.VerifiedUser, MosqueEmeraldDark)
        )
        UserRole.EXECUTIVE_MEMBER -> listOf(
            QuickActionItem("cashbox", "বাক্সে সাক্ষী", Icons.Default.Verified, MosqueWarningAmber),
            QuickActionItem("meetings", "সভা ও সিদ্ধান্ত", Icons.Default.EventAvailable, MosqueInfoBlue),
            QuickActionItem("members", "সদস্য পরিষদ", Icons.Default.Groups, MosqueEmeraldPrimary),
            QuickActionItem("events", "জুমা-ঈদ শৃঙ্খলা", Icons.Default.Celebration, MosqueGoldDeep),
            QuickActionItem("transparency", "স্বচ্ছ হিসাব", Icons.Default.Assessment, MosqueSuccessGreen),
            QuickActionItem("charter", "কমিটি সনদ", Icons.Default.VerifiedUser, MosqueEmeraldDark)
        )
        UserRole.ADVISORY_BOARD -> listOf(
            QuickActionItem("sharia_advisory", "পরামর্শ দাখিল", Icons.Default.AccountBalance, RoleAdvisoryAccent),
            QuickActionItem("transparency", "আর্থিক স্বচ্ছতা", Icons.Default.Assessment, MosqueSuccessGreen),
            QuickActionItem("meetings", "কমিটি এজেন্ডা", Icons.Default.Event, MosqueInfoBlue),
            QuickActionItem("audit", "অডিট পর্যবেক্ষণ", Icons.Default.FactCheck, MosqueWarningAmber),
            QuickActionItem("members", "সদস্য পরিষদ", Icons.Default.Groups, MosqueEmeraldPrimary),
            QuickActionItem("charter", "কমিটি সনদ", Icons.Default.VerifiedUser, MosqueEmeraldDark)
        )
        UserRole.GENERAL_SECRETARY, UserRole.ASSISTANT_SECRETARY, UserRole.ORGANIZING_SECRETARY -> listOf(
            QuickActionItem("add_member", "নতুন সদস্য", Icons.Default.PersonAdd, MosqueEmeraldPrimary),
            QuickActionItem("schedule_meeting", "সভা আহ্বান", Icons.Default.EventAvailable, MosqueInfoBlue),
            QuickActionItem("publish_notice", "নোটিশ প্রকাশ", Icons.Default.Campaign, MosqueEmeraldDark),
            QuickActionItem("office_docs", "দপ্তর নথি", Icons.Default.FolderShared, MosqueGoldDeep),
            QuickActionItem("welfare", "সমাজকল্যাণ", Icons.Default.VolunteerActivism, MosqueSuccessGreen),
            QuickActionItem("charter", "কমিটি সনদ", Icons.Default.VerifiedUser, RolePublicityAccent)
        )
        else -> listOf(
            QuickActionItem("pending_approval", "ভাউচার অনুমোদন", Icons.Default.AssignmentTurnedIn, MosqueUrgentRed),
            QuickActionItem("schedule_meeting", "সভা আহ্বান", Icons.Default.EventAvailable, MosqueInfoBlue),
            QuickActionItem("sharia_advisory", "শরিয়াহ ও উপদেষ্টা", Icons.Default.MenuBook, RoleAdvisoryAccent),
            QuickActionItem("cashbox", "ক্যাশবক্স তদারকি", Icons.Default.Inventory2, MosqueGoldDeep),
            QuickActionItem("office_docs", "দপ্তর দলিল", Icons.Default.FolderShared, RoleOfficeAccent),
            QuickActionItem("charter", "কমিটি সনদ", Icons.Default.VerifiedUser, MosqueEmeraldPrimary)
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
                text = "দায়িত্ব ও দ্রুত কার্যক্রম (Quick Actions)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MosqueEmeraldDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            for (rowIndex in 0..1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (colIndex in 0..2) {
                        val item = actions[rowIndex * 3 + colIndex]
                        val tileInteraction = remember { MutableInteractionSource() }
                        val tilePressed by tileInteraction.collectIsPressedAsState()
                        val tileScale by animateFloatAsState(
                            targetValue = if (tilePressed) 0.94f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                            label = "QuickActionTileScale"
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .graphicsLayer {
                                    scaleX = tileScale
                                    scaleY = tileScale
                                }
                                .clip(RoundedCornerShape(14.dp))
                                .background(MosqueBgLight)
                                .border(1.dp, MosqueCardBorderSubtle, RoundedCornerShape(14.dp))
                                .clickable(
                                    interactionSource = tileInteraction,
                                    indication = ripple(),
                                    onClick = { onActionClick(item.key) }
                                )
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
