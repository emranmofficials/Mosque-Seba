package com.mosqueseba.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.ui.theme.*

data class NavItem(
    val titleBn: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val destinationKey: String
)

fun getNavItemsForRole(role: com.mosqueseba.app.data.model.UserRole): List<NavItem> = when (role) {
    com.mosqueseba.app.data.model.UserRole.PRESIDENT,
    com.mosqueseba.app.data.model.UserRole.VICE_PRESIDENT -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("সদস্য পরিষদ", Icons.Filled.People, Icons.Outlined.People, "members"),
        NavItem("ফাইন্যান্স", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "finance"),
        NavItem("নোটিশ ও সভা", Icons.Filled.Campaign, Icons.Outlined.Campaign, "notices"),
        NavItem("সকল অপারেশন", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    com.mosqueseba.app.data.model.UserRole.TREASURER,
    com.mosqueseba.app.data.model.UserRole.ASSISTANT_TREASURER -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("আয়-ব্যয়", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "finance"),
        NavItem("ক্যাশবক্স", Icons.Filled.Inventory2, Icons.Outlined.Inventory2, "cashbox"),
        NavItem("চাঁদা ও বেতন", Icons.Filled.Payments, Icons.Outlined.Payments, "treasurer_chanda"),
        NavItem("সকল অপারেশন", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    com.mosqueseba.app.data.model.UserRole.GENERAL_SECRETARY,
    com.mosqueseba.app.data.model.UserRole.ASSISTANT_SECRETARY -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("সদস্য পরিষদ", Icons.Filled.People, Icons.Outlined.People, "members"),
        NavItem("সভা ও এজেন্ডা", Icons.Filled.EventAvailable, Icons.Outlined.EventAvailable, "secretary_meetings"),
        NavItem("দপ্তর নথি", Icons.Filled.FolderShared, Icons.Outlined.FolderShared, "office_docs"),
        NavItem("সকল অপারেশন", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    com.mosqueseba.app.data.model.UserRole.ORGANIZING_SECRETARY -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("সদস্য ও মুসল্লি", Icons.Filled.People, Icons.Outlined.People, "members"),
        NavItem("সভা ও এজেন্ডা", Icons.Filled.EventAvailable, Icons.Outlined.EventAvailable, "secretary_meetings"),
        NavItem("নোটিশ বোর্ড", Icons.Filled.Campaign, Icons.Outlined.Campaign, "notices"),
        NavItem("সমাজকল্যাণ", Icons.Filled.VolunteerActivism, Icons.Outlined.VolunteerActivism, "secretary_welfare")
    )
    com.mosqueseba.app.data.model.UserRole.PUBLICITY_SECRETARY -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("স্মার্ট বোর্ড", Icons.Filled.ViewCarousel, Icons.Outlined.ViewCarousel, "publicity"),
        NavItem("নোটিশ বোর্ড", Icons.Filled.Campaign, Icons.Outlined.Campaign, "notices"),
        NavItem("মাহফিল ও ইভেন্ট", Icons.Filled.Celebration, Icons.Outlined.Celebration, "events"),
        NavItem("কমিটি সনদ", Icons.Filled.VerifiedUser, Icons.Outlined.VerifiedUser, "charter")
    )
    com.mosqueseba.app.data.model.UserRole.OFFICE_SECRETARY -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("দপ্তর ও ফাইল", Icons.Filled.FolderShared, Icons.Outlined.FolderShared, "office_docs"),
        NavItem("সভা ও এজেন্ডা", Icons.Filled.EventAvailable, Icons.Outlined.EventAvailable, "secretary_meetings"),
        NavItem("নোটিশ বোর্ড", Icons.Filled.Campaign, Icons.Outlined.Campaign, "notices"),
        NavItem("কমিটি সনদ", Icons.Filled.VerifiedUser, Icons.Outlined.VerifiedUser, "charter")
    )
    com.mosqueseba.app.data.model.UserRole.WELFARE_EDUCATION_SECRETARY -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("সমাজকল্যাণ", Icons.Filled.VolunteerActivism, Icons.Outlined.VolunteerActivism, "secretary_welfare"),
        NavItem("মাদরাসা ও মক্তব", Icons.Filled.School, Icons.Outlined.School, "madrasa"),
        NavItem("নোটিশ", Icons.Filled.Campaign, Icons.Outlined.Campaign, "notices"),
        NavItem("কমিটি সনদ", Icons.Filled.VerifiedUser, Icons.Outlined.VerifiedUser, "charter")
    )
    com.mosqueseba.app.data.model.UserRole.EXECUTIVE_MEMBER -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("ক্যাশবক্স সাক্ষী", Icons.Filled.Verified, Icons.Outlined.Verified, "cashbox"),
        NavItem("সভা ও এজেন্ডা", Icons.Filled.EventAvailable, Icons.Outlined.EventAvailable, "secretary_meetings"),
        NavItem("সদস্য পরিষদ", Icons.Filled.People, Icons.Outlined.People, "members"),
        NavItem("সকল অপারেশন", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    com.mosqueseba.app.data.model.UserRole.ADVISORY_BOARD -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("উপদেষ্টা প্যানেল", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance, "sharia_advisory"),
        NavItem("স্বচ্ছ হিসাব", Icons.Filled.Assessment, Icons.Outlined.Assessment, "transparency"),
        NavItem("কমিটি সনদ", Icons.Filled.VerifiedUser, Icons.Outlined.VerifiedUser, "charter"),
        NavItem("নোটিশ", Icons.Filled.Campaign, Icons.Outlined.Campaign, "notices")
    )
    com.mosqueseba.app.data.model.UserRole.IMAM -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("শরিয়াহ প্যানেল", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "sharia_advisory"),
        NavItem("রোস্টার", Icons.Filled.DateRange, Icons.Outlined.DateRange, "roster"),
        NavItem("মাদরাসা", Icons.Filled.School, Icons.Outlined.School, "madrasa"),
        NavItem("সকল অপারেশন", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    com.mosqueseba.app.data.model.UserRole.MUAZZIN,
    com.mosqueseba.app.data.model.UserRole.KHADEM -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("রোস্টার", Icons.Filled.DateRange, Icons.Outlined.DateRange, "roster"),
        NavItem("খাবার শিডিউল", Icons.Filled.Restaurant, Icons.Outlined.Restaurant, "food"),
        NavItem("মাদরাসা", Icons.Filled.School, Icons.Outlined.School, "madrasa"),
        NavItem("প্রোফাইল", Icons.Filled.Badge, Icons.Outlined.Badge, "staff_profile")
    )
    com.mosqueseba.app.data.model.UserRole.GENERAL_MEMBER -> listOf(
        NavItem("হোম", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("আমার চাঁদা", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "my_chanda"),
        NavItem("স্বচ্ছ হিসাব", Icons.Filled.Assessment, Icons.Outlined.Assessment, "transparency"),
        NavItem("নোটিশ", Icons.Filled.Campaign, Icons.Outlined.Campaign, "notices"),
        NavItem("কমিটি সনদ", Icons.Filled.Groups, Icons.Outlined.Groups, "charter")
    )
}

@Composable
fun NavigationBottomBar(
    currentTabIndex: Int,
    currentRole: com.mosqueseba.app.data.model.UserRole,
    onTabSelected: (Int) -> Unit
) {
    val items = getNavItemsForRole(currentRole)

    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(IslamicGoldTrimGradient)
            )
            NavigationBar(
                containerColor = Color.White,
                contentColor = MosqueEmeraldPrimary,
                tonalElevation = 0.dp
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = currentTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onTabSelected(index) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.titleBn,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.titleBn,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MosqueEmeraldPrimary,
                            selectedTextColor = MosqueEmeraldPrimary,
                            indicatorColor = MosqueEmeraldLight.copy(alpha = 0.16f),
                            unselectedIconColor = MosqueTextMuted,
                            unselectedTextColor = MosqueTextSecondary
                        )
                    )
                }
            }
        }
    }
}

