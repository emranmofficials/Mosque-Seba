package com.mosqueseba.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.theme.LocalIsDarkTheme
import com.mosqueseba.app.ui.theme.MosqueCardBorderSubtle
import com.mosqueseba.app.ui.theme.MosqueEmeraldLight
import com.mosqueseba.app.ui.theme.MosqueEmeraldPrimary
import com.mosqueseba.app.ui.theme.MosqueEmeraldSoft
import com.mosqueseba.app.ui.theme.MosqueNightBorder
import com.mosqueseba.app.ui.theme.MosqueNightSurface
import com.mosqueseba.app.ui.theme.MosqueNightSurfaceVariant
import com.mosqueseba.app.ui.theme.MosqueNightTextPrimary
import com.mosqueseba.app.ui.theme.MosqueNightTextSecondary
import com.mosqueseba.app.ui.theme.MosqueTextPrimary
import com.mosqueseba.app.ui.theme.MosqueTextSecondary

/**
 * দায়িত্ব-ভিত্তিক ড্যাশবোর্ডের কাজের ক্ষেত্র। প্রতিটি পদবি নিজের দায়িত্ব অনুযায়ী
 * এই ক্ষেত্রগুলোর একটি অংশ পায়; ড্যাশবোর্ড শুধু ওই ক্ষেত্রগুলোর কার্ড দেখায়।
 */
enum class DashboardResponsibility(
    val titleBn: String,
    val icon: ImageVector
) {
    LEADERSHIP("নেতৃত্ব ও সিদ্ধান্ত", Icons.Default.WorkspacePremium),
    FINANCE("বায়তুলমাল ও হিসাব", Icons.Default.AccountBalanceWallet),
    SECRETARIAT("প্রশাসন ও দপ্তর", Icons.Default.Description),
    ISLAMIC_SERVICE("ইমামতি ও দ্বীনি সেবা", Icons.Default.Mosque),
    PRAYER_SERVICE("আজান ও ওয়াক্ত সেবা", Icons.Default.Mosque),
    FACILITY("খেদমত ও পরিচ্ছন্নতা", Icons.Default.CleaningServices),
    WELFARE("সমাজকল্যাণ ও শিক্ষা", Icons.Default.VolunteerActivism),
    PUBLICITY("প্রচার ও প্রকাশনা", Icons.Default.Campaign),
    MEMBERSHIP("সদস্য ও স্বচ্ছতা", Icons.Default.People),
    OVERSIGHT("তদারকি ও পরামর্শ", Icons.Default.Gavel)
}

data class DashboardQuickAction(
    val labelBn: String,
    val dialogKey: String,
    val icon: ImageVector
)

/**
 * পদবি → দায়িত্বের ক্ষেত্র ও দ্রুত কাজের কেন্দ্রীয় নিয়ম। UI-তে কার্ড লুকোনো/shows
 * করা শুধু সুবিধা; প্রতিটি অ্যাকশন ব্যাকএন্ডে আবার যাচাই হয়।
 */
object DashboardRolePolicy {

    fun responsibilitiesFor(role: UserRole): List<DashboardResponsibility> = when (role) {
        UserRole.PRESIDENT, UserRole.VICE_PRESIDENT -> listOf(
            DashboardResponsibility.LEADERSHIP,
            DashboardResponsibility.OVERSIGHT,
            DashboardResponsibility.MEMBERSHIP
        )
        UserRole.TREASURER, UserRole.ASSISTANT_TREASURER -> listOf(
            DashboardResponsibility.FINANCE,
            DashboardResponsibility.MEMBERSHIP
        )
        UserRole.GENERAL_SECRETARY, UserRole.ASSISTANT_SECRETARY,
        UserRole.ORGANIZING_SECRETARY, UserRole.OFFICE_SECRETARY -> listOf(
            DashboardResponsibility.SECRETARIAT,
            DashboardResponsibility.MEMBERSHIP
        )
        UserRole.IMAM -> listOf(
            DashboardResponsibility.ISLAMIC_SERVICE,
            DashboardResponsibility.WELFARE,
            DashboardResponsibility.OVERSIGHT
        )
        UserRole.MUAZZIN -> listOf(
            DashboardResponsibility.PRAYER_SERVICE,
            DashboardResponsibility.FACILITY
        )
        UserRole.KHADEM -> listOf(
            DashboardResponsibility.FACILITY,
            DashboardResponsibility.PRAYER_SERVICE
        )
        UserRole.WELFARE_EDUCATION_SECRETARY -> listOf(
            DashboardResponsibility.WELFARE,
            DashboardResponsibility.MEMBERSHIP
        )
        UserRole.PUBLICITY_SECRETARY -> listOf(
            DashboardResponsibility.PUBLICITY,
            DashboardResponsibility.MEMBERSHIP
        )
        UserRole.EXECUTIVE_MEMBER, UserRole.ADVISORY_BOARD -> listOf(
            DashboardResponsibility.OVERSIGHT,
            DashboardResponsibility.MEMBERSHIP
        )
        UserRole.GENERAL_MEMBER -> listOf(
            DashboardResponsibility.MEMBERSHIP
        )
    }

    fun has(role: UserRole, responsibility: DashboardResponsibility): Boolean =
        responsibilitiesFor(role).contains(responsibility)

    fun quickActionsFor(role: UserRole): List<DashboardQuickAction> = when (role) {
        UserRole.PRESIDENT, UserRole.VICE_PRESIDENT -> listOf(
            DashboardQuickAction("অনুমোদন", "pending_approval", Icons.Default.Gavel),
            DashboardQuickAction("সভা", "schedule_meeting", Icons.Default.Description),
            DashboardQuickAction("সদস্য", "members", Icons.Default.People),
            DashboardQuickAction("নোটিশ", "publish_notice", Icons.Default.Campaign)
        )
        UserRole.TREASURER, UserRole.ASSISTANT_TREASURER -> listOf(
            DashboardQuickAction("চাঁদা আদায়", "receive_chanda", Icons.Default.AccountBalanceWallet),
            DashboardQuickAction("ক্যাশবক্স", "cashbox", Icons.Default.AccountBalanceWallet),
            DashboardQuickAction("বেতন", "treasurer_payroll", Icons.Default.People),
            DashboardQuickAction("হিসাব", "finance", Icons.Default.Description)
        )
        UserRole.GENERAL_SECRETARY, UserRole.ASSISTANT_SECRETARY,
        UserRole.ORGANIZING_SECRETARY, UserRole.OFFICE_SECRETARY -> listOf(
            DashboardQuickAction("সভা", "schedule_meeting", Icons.Default.Description),
            DashboardQuickAction("নোটিশ", "publish_notice", Icons.Default.Campaign),
            DashboardQuickAction("দপ্তর নথি", "office_docs", Icons.Default.Description),
            DashboardQuickAction("সদস্য", "members", Icons.Default.People)
        )
        UserRole.IMAM -> listOf(
            DashboardQuickAction("শরিয়াহ", "sharia_advisory", Icons.Default.Gavel),
            DashboardQuickAction("মাদরাসা", "madrasa", Icons.Default.School),
            DashboardQuickAction("নামাজ", "view_prayers", Icons.Default.Mosque),
            DashboardQuickAction("স্মার্ট বোর্ড", "publicity", Icons.Default.Campaign)
        )
        UserRole.MUAZZIN -> listOf(
            DashboardQuickAction("রোস্টার", "roster", Icons.Default.Description),
            DashboardQuickAction("খাবার", "food", Icons.Default.People),
            DashboardQuickAction("নামাজ", "view_prayers", Icons.Default.Mosque)
        )
        UserRole.KHADEM -> listOf(
            DashboardQuickAction("ডিউটি", "roster", Icons.Default.CleaningServices),
            DashboardQuickAction("খাবার", "food", Icons.Default.People),
            DashboardQuickAction("নোটিশ", "notices", Icons.Default.Campaign)
        )
        UserRole.WELFARE_EDUCATION_SECRETARY -> listOf(
            DashboardQuickAction("সমাজকল্যাণ", "welfare", Icons.Default.VolunteerActivism),
            DashboardQuickAction("মাদরাসা", "madrasa", Icons.Default.School),
            DashboardQuickAction("ইভেন্ট", "events", Icons.Default.Campaign)
        )
        UserRole.PUBLICITY_SECRETARY -> listOf(
            DashboardQuickAction("স্মার্ট বোর্ড", "publicity", Icons.Default.Campaign),
            DashboardQuickAction("নোটিশ", "publish_notice", Icons.Default.Campaign),
            DashboardQuickAction("ইভেন্ট", "events", Icons.Default.Campaign)
        )
        UserRole.EXECUTIVE_MEMBER, UserRole.ADVISORY_BOARD -> listOf(
            DashboardQuickAction("সভা", "meetings", Icons.Default.Description),
            DashboardQuickAction("শরিয়াহ", "sharia_advisory", Icons.Default.Gavel),
            DashboardQuickAction("স্বচ্ছতা", "transparency", Icons.Default.AccountBalanceWallet)
        )
        UserRole.GENERAL_MEMBER -> listOf(
            DashboardQuickAction("আমার চাঁদা", "my_chanda", Icons.Default.AccountBalanceWallet),
            DashboardQuickAction("রসিদ", "view_receipt", Icons.Default.Description),
            DashboardQuickAction("স্বচ্ছতা", "transparency", Icons.Default.AccountBalanceWallet)
        )
    }
}

/**
 * প্রতিটি পদবির জন্য ব্যক্তিগত দায়িত্ব হেডার: পদবি, দায়িত্বের ক্ষেত্র,
 * প্রধান দায়িত্ব ও ওই পদে সবচেয়ে বেশি ব্যবহৃত দ্রুত কাজ।
 */
@Composable
fun RoleResponsibilityHeader(
    role: UserRole,
    onViewAllDuties: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val surfaceColor = if (isDark) MosqueNightSurface else Color.White
    val borderColor = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
    val primaryText = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary
    val secondaryText = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
    val accentColor = if (isDark) Color(0xFF34D399) else MosqueEmeraldLight
    val responsibilities = DashboardRolePolicy.responsibilitiesFor(role)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "আপনার দায়িত্ব · ${role.titleBn}",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    Text(
                        text = role.tier.titleBn,
                        fontSize = 11.sp,
                        color = secondaryText
                    )
                }
                Surface(
                    onClick = onViewAllDuties,
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft
                ) {
                    Text(
                        text = "সব দায়িত্ব ›",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = role.shortSummaryBn,
                fontSize = 12.sp,
                color = secondaryText,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                responsibilities.forEach { responsibility ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft,
                        border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else Color(0xFFD1FAE5))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = responsibility.icon,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF34D399) else MosqueEmeraldPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = responsibility.titleBn,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) MosqueNightTextPrimary else MosqueEmeraldPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            role.dutiesBn.take(3).forEachIndexed { index, duty ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${index + 1}.",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = duty,
                        fontSize = 11.5.sp,
                        color = secondaryText,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

        }
    }
}
