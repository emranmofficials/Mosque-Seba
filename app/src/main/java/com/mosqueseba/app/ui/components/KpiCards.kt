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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    upcomingEventCount: Int = 0,
    totalEventCount: Int = 0,
    welfareCount: Int = 0,
    pendingWelfareCount: Int = 0,
    cashboxCount: Int = 0,
    pendingCashboxWitnessCount: Int = 0,
    officeDocCount: Int = 0,
    pendingReplyDocCount: Int = 0,
    advisoryCount: Int = 0,
    pendingAdvisoryCount: Int = 0,
    mySalaryStatusValue: String = "পরিশোধিত",
    mySalaryStatusSubtitle: String = "বেতন স্লিপ ও রেকর্ড",
    todayStaffFoodProvider: String? = null,
    khademChecklistProgress: String? = null,
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
                c1 = KpiSpec("আমার মাসিক চাঁদা", "৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", myChandaPaid))}", myChandaSubtitle, Icons.Default.Verified, MosqueSuccessGreen, "my_chanda"),
                c2 = KpiSpec("মসজিদ স্বচ্ছ তহবিল", "৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", netBalance))}", "সার্বজনীন নেট স্থিতি", Icons.Default.AccountBalanceWallet, MosqueEmeraldPrimary, "transparency"),
                c3 = KpiSpec("পরবর্তী জামায়াত", nextPrayerName, nextPrayerSubtitle, Icons.Default.AccessTime, MosqueGoldDeep, "prayers"),
                c4 = KpiSpec("নতুন নোটিশ", "${toBengaliDigits(noticeCount.toString())}টি বিজ্ঞপ্তি", "কমিটি কর্তৃক প্রকাশিত", Icons.Default.Campaign, MosqueInfoBlue, "notices"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.IMAM -> {
            val shariaSub = if (pendingAdvisoryCount > 0) {
                "পর্যালোচনাধীন ${toBengaliDigits(pendingAdvisoryCount.toString())} টি নোট"
            } else {
                "পদাধিকারবলে দায়িত্ব"
            }
            KpiTwoByTwo(
                c1 = KpiSpec("শরিয়াহ পর্যালোচনা", "${toBengaliDigits(advisoryCount.toString())} টি মতামত", shariaSub, Icons.Default.MenuBook, MosqueEmeraldPrimary, "sharia_advisory"),
                c2 = KpiSpec("আজকের দায়িত্ব", "ইমামতি ও খুতবা", "৫ ওয়াক্ত ও জুমার রোস্টার", Icons.Default.Schedule, MosqueGoldDeep, "roster"),
                c3 = KpiSpec("মাদরাসা শিক্ষার্থী", "${toBengaliDigits(studentCount.toString())} জন ছাত্র", "হিফজ ও নূরানী বিভাগ", Icons.Default.School, MosqueInfoBlue, "madrasa"),
                c4 = KpiSpec("আমার বেতন স্ট্যাটাস", mySalaryStatusValue, mySalaryStatusSubtitle, Icons.Default.CheckCircle, MosqueSuccessGreen, "staff_profile"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.MUAZZIN -> {
            val foodValue = todayStaffFoodProvider?.let { "আজ: $it" } ?: "দুপুর ও রাত"
            val foodSub = if (todayStaffFoodProvider != null) "আজকের দায়িত্বপ্রাপ্ত দাতা" else "দৈনিক মেহমানদারি তালিকা"
            KpiTwoByTwo(
                c1 = KpiSpec("৫ ওয়াক্ত আজান ও ইকামত", nextPrayerName, nextPrayerSubtitle.ifBlank { "আজান ও সাউন্ড সিস্টেম" }, Icons.Default.AccessTime, MosqueEmeraldPrimary, "prayers"),
                c2 = KpiSpec("ডিউটি রোস্টার", "মুয়াজ্জিন সূচি", "ইমাম অনুপস্থিতিতে নায়েবে ইমাম", Icons.Default.Schedule, MosqueGoldDeep, "roster"),
                c3 = KpiSpec("খাবার শিডিউল", foodValue, foodSub, Icons.Default.Restaurant, MosqueInfoBlue, "food"),
                c4 = KpiSpec("আমার বেতন স্ট্যাটাস", mySalaryStatusValue, mySalaryStatusSubtitle, Icons.Default.CheckCircle, MosqueSuccessGreen, "staff_profile"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.KHADEM -> {
            val foodValue = todayStaffFoodProvider?.let { "আজ: $it" } ?: "দুপুর ও রাত"
            val foodSub = if (todayStaffFoodProvider != null) "আজকের দায়িত্বপ্রাপ্ত দাতা" else "দৈনিক মেহমানদারি তালিকা"
            KpiTwoByTwo(
                c1 = KpiSpec(
                    "খেদমত ও পরিচ্ছন্নতা",
                    khademChecklistProgress ?: "দৈনিক তদারকি",
                    "অজুখানা, ফ্যান-লাইট ও কাতার",
                    Icons.Default.CleaningServices,
                    MosqueEmeraldPrimary,
                    "roster"
                ),
                c2 = KpiSpec("খাবার শিডিউল", foodValue, foodSub, Icons.Default.Restaurant, MosqueGoldDeep, "food"),
                c3 = KpiSpec("মসজিদ নোটিশ", "${toBengaliDigits(noticeCount.toString())} টি বিজ্ঞপ্তি", "কমিটি নির্দেশনা", Icons.Default.Campaign, MosqueInfoBlue, "notices"),
                c4 = KpiSpec("আমার বেতন স্ট্যাটাস", mySalaryStatusValue, mySalaryStatusSubtitle, Icons.Default.CheckCircle, MosqueSuccessGreen, "staff_profile"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.TREASURER -> {
            val cashSub = if (pendingItems > 0) {
                "অনুমোদনের অপেক্ষায় ${toBengaliDigits(pendingItems.toString())}টি ভাউচার"
            } else {
                "বর্তমান নেট ক্যাশ স্থিতি"
            }
            KpiTwoByTwo(
                c1 = KpiSpec("নগদ ও ব্যাংক তহবিল", "৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", netBalance))}", cashSub, Icons.Default.AccountBalanceWallet, MosqueSuccessGreen, "finance"),
                c2 = KpiSpec("দান বাক্স (ক্যাশবক্স)", "${toBengaliDigits(cashboxCount.toString())} টি গণনা", "${toBengaliDigits(pendingCashboxWitnessCount.toString())} টি সাক্ষ্য বাকি", Icons.Default.Inventory2, MosqueGoldDeep, "cashbox"),
                c3 = KpiSpec("বকেয়া চাঁদা", "৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", dueChanda))}", "চলতি মাস আদায়যোগ্য", Icons.Default.PendingActions, MosqueWarningAmber, "treasurer_chanda"),
                c4 = KpiSpec("স্টাফ বেতন ও পে-রোল", "${toBengaliDigits(staffCount.toString())} জন স্টাফ", "মাসিক বেতন ও ভাতা পরিশোধ", Icons.Default.Badge, MosqueEmeraldPrimary, "treasurer_payroll"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.ASSISTANT_TREASURER -> {
            val cashSub = if (pendingItems > 0) {
                "অপেক্ষমাণ ভাউচার ${toBengaliDigits(pendingItems.toString())}টি"
            } else {
                "আয়-ব্যয় ও ভাউচার খাতা"
            }
            KpiTwoByTwo(
                c1 = KpiSpec("নগদ ও ব্যাংক তহবিল", "৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", netBalance))}", cashSub, Icons.Default.AccountBalanceWallet, MosqueSuccessGreen, "finance"),
                c2 = KpiSpec("দান বাক্স (ক্যাশবক্স)", "${toBengaliDigits(cashboxCount.toString())} টি গণনা", "${toBengaliDigits(pendingCashboxWitnessCount.toString())} টি সাক্ষ্য বাকি", Icons.Default.Inventory2, MosqueGoldDeep, "cashbox"),
                c3 = KpiSpec("বকেয়া চাঁদা", "৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", dueChanda))}", "সদস্য চাঁদা আদায় ও রসিদ", Icons.Default.Payments, MosqueEmeraldPrimary, "treasurer_chanda"),
                c4 = KpiSpec("বেতনভুক্ত স্টাফ পে-রোল", "${toBengaliDigits(staffCount.toString())} জন স্টাফ", "মাসিক বেতন ও ভাতা পরিশোধ", Icons.Default.Badge, MosqueInfoBlue, "treasurer_payroll"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.PUBLICITY_SECRETARY -> {
            val eventVal = if (upcomingEventCount > 0) {
                "${toBengaliDigits(upcomingEventCount.toString())} টি আসন্ন"
            } else {
                "${toBengaliDigits(totalEventCount.toString())} টি অনুষ্ঠান"
            }
            KpiTwoByTwo(
                c1 = KpiSpec("স্মার্ট বোর্ড ও উক্তি", "${toBengaliDigits(smartQuoteCount.toString())} টি বার্তা", "দেয়াল উক্তি ও নির্দেশনা", Icons.Default.FormatQuote, RolePublicityAccent, "publicity"),
                c2 = KpiSpec("প্রকাশিত নোটিশ", "${toBengaliDigits(noticeCount.toString())} টি বিজ্ঞপ্তি", "জরুরি ও সাধারণ প্রচার", Icons.Default.Campaign, MosqueEmeraldPrimary, "notices"),
                c3 = KpiSpec("মাহফিল ও অনুষ্ঠান", eventVal, "ঈদে মিলাদুন্নবী, শবে কদর ও মাহফিল", Icons.Default.Celebration, MosqueGoldDeep, "events"),
                c4 = KpiSpec("আসন্ন কমিটি সভা", nextMeetingTitleDate, nextMeetingSubtitle, Icons.Default.EventAvailable, MosqueInfoBlue, "meetings"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.OFFICE_SECRETARY -> {
            val replySub = if (pendingReplyDocCount > 0) {
                "দাপ্তরিক জবাব তৈরি বাকি"
            } else {
                "সকল চিঠির জবাব সম্পন্ন"
            }
            KpiTwoByTwo(
                c1 = KpiSpec("দপ্তর ও দলিল ফাইল", "${toBengaliDigits(officeDocCount.toString())} টি নথি", "ওয়াকফ দলিল ও চুক্তিপত্র", Icons.Default.FolderShared, RoleOfficeAccent, "office_docs"),
                c2 = KpiSpec("জবাব অপেক্ষমাণ চিঠি", "${toBengaliDigits(pendingReplyDocCount.toString())} টি পত্র", replySub, Icons.Default.MarkEmailUnread, MosqueWarningAmber, "office_docs"),
                c3 = KpiSpec("আসন্ন কমিটি সভা", nextMeetingTitleDate, nextMeetingSubtitle, Icons.Default.EventAvailable, MosqueEmeraldPrimary, "meetings"),
                c4 = KpiSpec("নিবন্ধিত সদস্য", "${toBengaliDigits(totalMembers.toString())} জন", "কমিটি ও সাধারণ তালিকা", Icons.Default.Groups, MosqueInfoBlue, "members"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.WELFARE_EDUCATION_SECRETARY -> {
            val welfareSub = if (pendingWelfareCount > 0) {
                "বিতরণ বাকি ${toBengaliDigits(pendingWelfareCount.toString())} টি"
            } else {
                "চিকিৎসা, ক্যাম্প ও ত্রাণ"
            }
            val eventVal = if (upcomingEventCount > 0) {
                "${toBengaliDigits(upcomingEventCount.toString())} টি আসন্ন"
            } else {
                "${toBengaliDigits(totalEventCount.toString())} টি আয়োজন"
            }
            KpiTwoByTwo(
                c1 = KpiSpec("মক্তব ও হিফজ শিক্ষার্থী", "${toBengaliDigits(studentCount.toString())} জন ছাত্র", "হাজিরা ও তালিম তদারকি", Icons.Default.School, RoleWelfareEduAccent, "madrasa"),
                c2 = KpiSpec("যাকাত ও সমাজকল্যাণ", "${toBengaliDigits(welfareCount.toString())} টি আবেদন", welfareSub, Icons.Default.VolunteerActivism, MosqueSuccessGreen, "welfare"),
                c3 = KpiSpec("ধর্মীয় ও কল্যাণ আয়োজন", eventVal, "মেডিকেল ক্যাম্প ও মাহফিল", Icons.Default.Celebration, MosqueGoldDeep, "events"),
                c4 = KpiSpec("প্রকাশিত নোটিশ", "${toBengaliDigits(noticeCount.toString())} টি বিজ্ঞপ্তি", "কমিটি ও মক্তব সার্কুলার", Icons.Default.Campaign, MosqueInfoBlue, "notices"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.EXECUTIVE_MEMBER -> {
            val witnessSub = if (pendingCashboxWitnessCount > 0) {
                "সাক্ষ্য নিশ্চিত করুন"
            } else {
                "সকল দান বাক্স যাচাইকৃত"
            }
            KpiTwoByTwo(
                c1 = KpiSpec("ক্যাশবক্স সাক্ষ্য অপেক্ষায়", "${toBengaliDigits(pendingCashboxWitnessCount.toString())} টি বাক্স", witnessSub, Icons.Default.Verified, MosqueWarningAmber, "cashbox"),
                c2 = KpiSpec("আসন্ন কমিটি সভা", nextMeetingTitleDate, nextMeetingSubtitle, Icons.Default.EventAvailable, MosqueInfoBlue, "meetings"),
                c3 = KpiSpec("সদস্য পরিষদ", "${toBengaliDigits(totalMembers.toString())} জন", "জুমা ও ঈদ শৃঙ্খলা সমন্বয়", Icons.Default.Groups, MosqueEmeraldPrimary, "members"),
                c4 = KpiSpec("মসজিদ স্বচ্ছ তহবিল", "৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", netBalance))}", "উন্মুক্ত নেট স্থিতি", Icons.Default.AccountBalanceWallet, MosqueSuccessGreen, "transparency"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.ADVISORY_BOARD -> {
            val advisorySub = if (pendingAdvisoryCount > 0) {
                "পর্যালোচনাধীন ${toBengaliDigits(pendingAdvisoryCount.toString())} টি"
            } else {
                "সংকট ও নির্মাণ নির্দেশনা"
            }
            KpiTwoByTwo(
                c1 = KpiSpec("উপদেষ্টা ও শরিয়াহ লগ", "${toBengaliDigits(advisoryCount.toString())} টি পরামর্শ", advisorySub, Icons.Default.AccountBalance, RoleAdvisoryAccent, "sharia_advisory"),
                c2 = KpiSpec("মসজিদ ফান্ড স্থিতি", "৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", netBalance))}", "স্বচ্ছ আর্থিক অবস্থান", Icons.Default.AccountBalanceWallet, MosqueSuccessGreen, "transparency"),
                c3 = KpiSpec("নিবন্ধিত সদস্য", "${toBengaliDigits(totalMembers.toString())} জন", "কমিটি ও মুসল্লি পরিষদ", Icons.Default.Groups, MosqueEmeraldPrimary, "members"),
                c4 = KpiSpec("আসন্ন কমিটি সভা", nextMeetingTitleDate, nextMeetingSubtitle, Icons.Default.EventAvailable, MosqueInfoBlue, "meetings"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.GENERAL_SECRETARY -> {
            KpiTwoByTwo(
                c1 = KpiSpec("নিবন্ধিত সদস্য পরিষদ", "${toBengaliDigits(totalMembers.toString())} জন", "কার্যনির্বাহী ও সাধারণ সদস্য", Icons.Default.Groups, MosqueEmeraldPrimary, "members"),
                c2 = KpiSpec("আসন্ন কমিটি সভা", nextMeetingTitleDate, nextMeetingSubtitle, Icons.Default.EventAvailable, MosqueInfoBlue, "meetings"),
                c3 = KpiSpec("দপ্তর ও নথি ফাইল", "${toBengaliDigits(officeDocCount.toString())} টি নথি", "জবাব বাকি ${toBengaliDigits(pendingReplyDocCount.toString())} টি", Icons.Default.FolderShared, MosqueGoldDeep, "office_docs"),
                c4 = KpiSpec("ইমাম-মুয়াজ্জিন তদারকি", "${toBengaliDigits(staffCount.toString())} জন স্টাফ", "খেদমত ও রোস্টার সমন্বয়", Icons.Default.Badge, MosqueSuccessGreen, "roster"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.ASSISTANT_SECRETARY -> {
            val docSub = if (pendingReplyDocCount > 0) {
                "জবাব বাকি ${toBengaliDigits(pendingReplyDocCount.toString())} টি"
            } else {
                "সাধারণ সম্পাদকের সহায়তা"
            }
            val noticeSub = if (nextMeetingTitleDate != "নির্ধারিত নেই") {
                "আসন্ন সভা: $nextMeetingTitleDate"
            } else {
                "কমিটি ঘোষণা ও সভা"
            }
            KpiTwoByTwo(
                c1 = KpiSpec("দৈনিক দপ্তর ও নথি", "${toBengaliDigits(officeDocCount.toString())} টি ফাইল", docSub, Icons.Default.FolderShared, MosqueEmeraldPrimary, "office_docs"),
                c2 = KpiSpec("প্রকাশিত নোটিশ ও সভা", "${toBengaliDigits(noticeCount.toString())} টি বিজ্ঞপ্তি", noticeSub, Icons.Default.Campaign, MosqueInfoBlue, "notices"),
                c3 = KpiSpec("স্টাফ ও রোস্টার তদারকি", "${toBengaliDigits(staffCount.toString())} জন স্টাফ", "৫ ওয়াক্ত ও জুমা রোস্টার", Icons.Default.Schedule, MosqueGoldDeep, "roster"),
                c4 = KpiSpec("নিবন্ধিত সদস্য", "${toBengaliDigits(totalMembers.toString())} জন", "সদস্য ও মুসল্লি তালিকা", Icons.Default.Groups, MosqueSuccessGreen, "members"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.ORGANIZING_SECRETARY -> {
            val eventVal = if (upcomingEventCount > 0) {
                "${toBengaliDigits(upcomingEventCount.toString())} টি আসন্ন"
            } else {
                "${toBengaliDigits(totalEventCount.toString())} টি অনুষ্ঠান"
            }
            KpiTwoByTwo(
                c1 = KpiSpec("সদস্য ও স্বেচ্ছাসেবক", "${toBengaliDigits(totalMembers.toString())} জন", "নতুন মুসল্লি ও যুবক সম্পৃক্তকরণ", Icons.Default.Groups, MosqueEmeraldPrimary, "members"),
                c2 = KpiSpec("মাহফিল ও ইভেন্ট", eventVal, "জুমা, ঈদ ও ওয়াজ মাহফিল", Icons.Default.Celebration, MosqueGoldDeep, "events"),
                c3 = KpiSpec("আসন্ন কমিটি সভা", nextMeetingTitleDate, nextMeetingSubtitle, Icons.Default.EventAvailable, MosqueInfoBlue, "meetings"),
                c4 = KpiSpec("সমাজকল্যাণ কার্যক্রম", "${toBengaliDigits(welfareCount.toString())} টি উদ্যোগ", "সামাজিক ও সেবামূলক কাজ", Icons.Default.VolunteerActivism, MosqueSuccessGreen, "welfare"),
                onKpiClick = onKpiClick
            )
        }
        UserRole.VICE_PRESIDENT -> {
            KpiTwoByTwo(
                c1 = KpiSpec("উন্নয়ন ও বিশেষ প্রকল্প", "তদারকি প্যানেল", "উপ-কমিটি ও নির্মাণ তদারকি", Icons.Default.Engineering, MosqueEmeraldPrimary, "events"),
                c2 = KpiSpec("ভাউচার অনুমোদন", "${toBengaliDigits(pendingItems.toString())} টি অপেক্ষমাণ", "নির্বাহী অনুমোদন ও তদারকি", Icons.Default.AssignmentTurnedIn, MosqueUrgentRed, "pending"),
                c3 = KpiSpec("তহবিল ব্যালেন্স", "৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", netBalance))}", "বর্তমান নেট স্থিতি", Icons.Default.AccountBalanceWallet, MosqueSuccessGreen, "finance"),
                c4 = KpiSpec("সদস্য ও উপ-কমিটি", "${toBengaliDigits(totalMembers.toString())} জন", "আসন্ন সভা: $nextMeetingTitleDate", Icons.Default.Groups, MosqueInfoBlue, "members"),
                onKpiClick = onKpiClick
            )
        }
        else -> {
            // PRESIDENT
            KpiTwoByTwo(
                c1 = KpiSpec("মোট সদস্য পরিষদ", "${toBengaliDigits(totalMembers.toString())} জন", "নিবন্ধিত সক্রিয় সদস্য", Icons.Default.Groups, MosqueEmeraldPrimary, "members"),
                c2 = KpiSpec("তহবিল ব্যালেন্স", "৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", netBalance))}", "বর্তমান নেট স্থিতি", Icons.Default.AccountBalanceWallet, MosqueSuccessGreen, "finance"),
                c3 = KpiSpec("বকেয়া চাঁদা", "৳${toBengaliDigits(String.format(Locale.getDefault(), "%,.0f", dueChanda))}", "চলতি মাস", Icons.Default.PendingActions, MosqueWarningAmber, "chanda"),
                c4 = KpiSpec("অমীমাংসিত অনুমোদন", "${toBengaliDigits(pendingItems.toString())} টি ভাউচার", "চূড়ান্ত অনুমোদন ও অডিট", Icons.Default.AssignmentLate, MosqueUrgentRed, "pending"),
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
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_${c1.key}"),
                title = c1.title,
                value = c1.value,
                subtext = c1.subtext,
                icon = c1.icon,
                iconBg = c1.color.copy(alpha = 0.1f),
                iconColor = c1.color,
                onClick = { onKpiClick(c1.key) }
            )
            KpiCardItem(
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_${c2.key}"),
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
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_${c3.key}"),
                title = c3.title,
                value = c3.value,
                subtext = c3.subtext,
                icon = c3.icon,
                iconBg = c3.color.copy(alpha = 0.1f),
                iconColor = c3.color,
                onClick = { onKpiClick(c3.key) }
            )
            KpiCardItem(
                modifier = Modifier
                    .weight(1f)
                    .testTag("kpi_${c4.key}"),
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
    val isDark = LocalIsDarkTheme.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 3.dp),
        border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else MosqueCardBorderSubtle)
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = if (isDark) 0.16f else 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
            QuickActionItem("meetings", "সভা ও সিদ্ধান্ত", Icons.Default.EventAvailable, MosqueGoldDeep),
            QuickActionItem("roster", "ডিউটি রোস্টার", Icons.Default.Schedule, MosqueEmeraldDark),
            QuickActionItem("madrasa", "হিফজ মূল্যায়ন", Icons.Default.School, MosqueInfoBlue),
            QuickActionItem("food", "খাবার শিডিউল", Icons.Default.Restaurant, MosqueSuccessGreen),
            QuickActionItem("view_notices", "নোটিশ দেখুন", Icons.Default.Campaign, RoleOfficeAccent)
        )
        UserRole.MUAZZIN -> listOf(
            QuickActionItem("view_prayers", "নামাজ ও আজান", Icons.Default.AccessTime, MosqueEmeraldPrimary),
            QuickActionItem("roster", "ডিউটি রোস্টার", Icons.Default.Schedule, MosqueGoldDeep),
            QuickActionItem("food", "খাবার শিডিউল", Icons.Default.Restaurant, MosqueInfoBlue),
            QuickActionItem("madrasa", "মক্তব ও তালিম", Icons.Default.School, MosqueSuccessGreen),
            QuickActionItem("view_notices", "নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark),
            QuickActionItem("staff_profile", "বেতন স্লিপ", Icons.Default.AccountBalanceWallet, MosqueUrgentRed)
        )
        UserRole.KHADEM -> listOf(
            QuickActionItem("food", "খাবার শিডিউল", Icons.Default.Restaurant, MosqueGoldDeep),
            QuickActionItem("roster", "খেদমত রোস্টার", Icons.Default.CleaningServices, MosqueEmeraldPrimary),
            QuickActionItem("view_notices", "নোটিশ বোর্ড", Icons.Default.Campaign, MosqueInfoBlue),
            QuickActionItem("view_prayers", "নামাজের সময়", Icons.Default.AccessTime, MosqueSuccessGreen),
            QuickActionItem("staff_profile", "বেতন স্লিপ", Icons.Default.AccountBalanceWallet, MosqueUrgentRed),
            QuickActionItem("charter", "কমিটি সনদ", Icons.Default.VerifiedUser, MosqueEmeraldDark)
        )
        UserRole.TREASURER -> listOf(
            QuickActionItem("receive_chanda", "চাঁদা গ্রহণ", Icons.Default.Payments, MosqueEmeraldPrimary),
            QuickActionItem("cashbox", "ক্যাশবক্স জমা", Icons.Default.Inventory2, MosqueGoldDeep),
            QuickActionItem("treasurer_payroll", "বেতন বিতরণ", Icons.Default.Badge, MosqueInfoBlue),
            QuickActionItem("add_income", "আয় এন্ট্রি", Icons.Default.AddCircle, MosqueSuccessGreen),
            QuickActionItem("add_expense", "ব্যয় ভাউচার", Icons.Default.RemoveCircle, MosqueUrgentRed),
            QuickActionItem("treasurer_report", "মাসিক রিপোর্ট", Icons.Default.Assessment, MosqueEmeraldDark)
        )
        UserRole.ASSISTANT_TREASURER -> listOf(
            QuickActionItem("receive_chanda", "চাঁদা গ্রহণ", Icons.Default.Payments, MosqueEmeraldPrimary),
            QuickActionItem("cashbox", "ক্যাশবক্স গণনা", Icons.Default.Inventory2, MosqueGoldDeep),
            QuickActionItem("view_receipt", "রসিদ বহি", Icons.Default.ReceiptLong, MosqueInfoBlue),
            QuickActionItem("add_income", "আয় এন্ট্রি", Icons.Default.AddCircle, MosqueSuccessGreen),
            QuickActionItem("add_expense", "ব্যয় ভাউচার", Icons.Default.RemoveCircle, MosqueUrgentRed),
            QuickActionItem("treasurer_report", "হিসাব রিপোর্ট", Icons.Default.Assessment, MosqueEmeraldDark)
        )
        UserRole.PUBLICITY_SECRETARY -> listOf(
            QuickActionItem("publicity", "স্মার্ট বোর্ড", Icons.Default.ViewCarousel, RolePublicityAccent),
            QuickActionItem("publish_notice", "নোটিশ প্রকাশ", Icons.Default.Campaign, MosqueEmeraldPrimary),
            QuickActionItem("events", "ধর্মীয় অনুষ্ঠান", Icons.Default.Celebration, MosqueGoldDeep),
            QuickActionItem("view_prayers", "নামাজের সূচি", Icons.Default.AccessTime, MosqueInfoBlue),
            QuickActionItem("meetings", "কমিটি সভা", Icons.Default.EventAvailable, MosqueEmeraldDark),
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
            QuickActionItem("events", "ক্যাম্প ও মাহফিল", Icons.Default.MedicalServices, MosqueUrgentRed),
            QuickActionItem("sharia_advisory", "শরিয়াহ নীতি", Icons.Default.MenuBook, MosqueInfoBlue),
            QuickActionItem("view_notices", "নোটিশ দেখুন", Icons.Default.Campaign, MosqueGoldDeep),
            QuickActionItem("meetings", "কমিটি সভা", Icons.Default.EventAvailable, MosqueEmeraldDark)
        )
        UserRole.EXECUTIVE_MEMBER -> listOf(
            QuickActionItem("cashbox", "বাক্সে সাক্ষী", Icons.Default.Verified, MosqueWarningAmber),
            QuickActionItem("meetings", "সভা ও সিদ্ধান্ত", Icons.Default.EventAvailable, MosqueInfoBlue),
            QuickActionItem("members", "সদস্য পরিষদ", Icons.Default.Groups, MosqueEmeraldPrimary),
            QuickActionItem("events", "জুমা-ঈদ শৃঙ্খলা", Icons.Default.Celebration, MosqueGoldDeep),
            QuickActionItem("transparency", "স্বচ্ছ হিসাব", Icons.Default.Assessment, MosqueSuccessGreen),
            QuickActionItem("view_notices", "নোটিশ দেখুন", Icons.Default.Campaign, MosqueEmeraldDark)
        )
        UserRole.ADVISORY_BOARD -> listOf(
            QuickActionItem("sharia_advisory", "পরামর্শ দাখিল", Icons.Default.AccountBalance, RoleAdvisoryAccent),
            QuickActionItem("transparency", "আর্থিক স্বচ্ছতা", Icons.Default.Assessment, MosqueSuccessGreen),
            QuickActionItem("meetings", "কমিটি এজেন্ডা", Icons.Default.Event, MosqueInfoBlue),
            QuickActionItem("audit", "অডিট পর্যবেক্ষণ", Icons.Default.FactCheck, MosqueWarningAmber),
            QuickActionItem("members", "সদস্য পরিষদ", Icons.Default.Groups, MosqueEmeraldPrimary),
            QuickActionItem("events", "উন্নয়ন ও আয়োজন", Icons.Default.Celebration, MosqueGoldDeep)
        )
        UserRole.GENERAL_SECRETARY -> listOf(
            QuickActionItem("add_member", "নতুন সদস্য", Icons.Default.PersonAdd, MosqueEmeraldPrimary),
            QuickActionItem("schedule_meeting", "সভা আহ্বান", Icons.Default.EventAvailable, MosqueInfoBlue),
            QuickActionItem("publish_notice", "নোটিশ প্রকাশ", Icons.Default.Campaign, MosqueEmeraldDark),
            QuickActionItem("office_docs", "দপ্তর নথি", Icons.Default.FolderShared, MosqueGoldDeep),
            QuickActionItem("roster", "স্টাফ রোস্টার", Icons.Default.Schedule, MosqueSuccessGreen),
            QuickActionItem("food", "খাবার শিডিউল", Icons.Default.Restaurant, RolePublicityAccent)
        )
        UserRole.ASSISTANT_SECRETARY -> listOf(
            QuickActionItem("publish_notice", "নোটিশ প্রকাশ", Icons.Default.Campaign, MosqueEmeraldPrimary),
            QuickActionItem("schedule_meeting", "সভা সমন্বয়", Icons.Default.EventAvailable, MosqueInfoBlue),
            QuickActionItem("office_docs", "দপ্তর সহায়তা", Icons.Default.FolderShared, MosqueGoldDeep),
            QuickActionItem("roster", "রোস্টার তদারকি", Icons.Default.Schedule, MosqueSuccessGreen),
            QuickActionItem("add_member", "সদস্য নিবন্ধন", Icons.Default.PersonAdd, MosqueEmeraldDark),
            QuickActionItem("food", "খাবার শিডিউল", Icons.Default.Restaurant, RolePublicityAccent)
        )
        UserRole.ORGANIZING_SECRETARY -> listOf(
            QuickActionItem("add_member", "সদস্য অন্তর্ভুক্তি", Icons.Default.PersonAdd, MosqueEmeraldPrimary),
            QuickActionItem("events", "মাহফিল ও ইভেন্ট", Icons.Default.Celebration, MosqueGoldDeep),
            QuickActionItem("schedule_meeting", "সাংগঠনিক সভা", Icons.Default.EventAvailable, MosqueInfoBlue),
            QuickActionItem("publish_notice", "বিজ্ঞপ্তি প্রকাশ", Icons.Default.Campaign, MosqueEmeraldDark),
            QuickActionItem("welfare", "যুব ও সমাজকল্যাণ", Icons.Default.VolunteerActivism, MosqueSuccessGreen),
            QuickActionItem("food", "জায়গির ও খাবার", Icons.Default.Restaurant, RolePublicityAccent)
        )
        UserRole.VICE_PRESIDENT -> listOf(
            QuickActionItem("pending_approval", "ভাউচার অনুমোদন", Icons.Default.AssignmentTurnedIn, MosqueUrgentRed),
            QuickActionItem("events", "উন্নয়ন ও প্রকল্প", Icons.Default.Engineering, MosqueEmeraldPrimary),
            QuickActionItem("schedule_meeting", "উপ-কমিটি সভা", Icons.Default.EventAvailable, MosqueInfoBlue),
            QuickActionItem("cashbox", "ক্যাশবক্স সাক্ষী", Icons.Default.Inventory2, MosqueGoldDeep),
            QuickActionItem("welfare", "সমাজকল্যাণ তদারকি", Icons.Default.VolunteerActivism, MosqueSuccessGreen),
            QuickActionItem("audit", "অডিট পর্যবেক্ষণ", Icons.Default.FactCheck, RoleOfficeAccent)
        )
        else -> listOf(
            // PRESIDENT
            QuickActionItem("pending_approval", "ভাউচার অনুমোদন", Icons.Default.AssignmentTurnedIn, MosqueUrgentRed),
            QuickActionItem("receive_chanda", "চাঁদা গ্রহণ", Icons.Default.Payments, MosqueEmeraldPrimary),
            QuickActionItem("treasurer_payroll", "বেতন পরিশোধ", Icons.Default.Badge, MosqueInfoBlue),
            QuickActionItem("schedule_meeting", "সভা আহ্বান", Icons.Default.EventAvailable, MosqueGoldDeep),
            QuickActionItem("sharia_advisory", "শরিয়াহ ও উপদেষ্টা", Icons.Default.MenuBook, RoleAdvisoryAccent),
            QuickActionItem("office_docs", "দপ্তর ও দলিল", Icons.Default.FolderShared, RoleOfficeAccent)
        )
    }

    val isDark = LocalIsDarkTheme.current
    val tileBg = if (isDark) MosqueNightSurfaceVariant else MosqueBgLight
    val tileBorder = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
    MehrabArchCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MosqueCardBorderGold.copy(alpha = 0.6f),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "দায়িত্ব ও দ্রুত কার্যক্রম",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
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
                                .testTag("quick_action_${item.key}")
                                .graphicsLayer {
                                    scaleX = tileScale
                                    scaleY = tileScale
                                }
                                .clip(RoundedCornerShape(14.dp))
                                .background(tileBg)
                                .border(1.dp, tileBorder, RoundedCornerShape(14.dp))
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
                                    .background(item.color.copy(alpha = 0.14f)),
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
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
