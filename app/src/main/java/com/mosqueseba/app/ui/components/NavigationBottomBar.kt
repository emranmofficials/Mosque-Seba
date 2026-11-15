package com.mosqueseba.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    com.mosqueseba.app.data.model.UserRole.PRESIDENT -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("সদস্য", Icons.Filled.People, Icons.Outlined.People, "members"),
        NavItem("আয়-ব্যয়", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "finance"),
        NavItem("নোটিশ", Icons.Filled.Campaign, Icons.Outlined.Campaign, "notices"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    com.mosqueseba.app.data.model.UserRole.VICE_PRESIDENT -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("আয়-ব্যয়", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "finance"),
        NavItem("সদস্য", Icons.Filled.People, Icons.Outlined.People, "members"),
        NavItem("সভা", Icons.Filled.EventAvailable, Icons.Outlined.EventAvailable, "secretary_meetings"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    com.mosqueseba.app.data.model.UserRole.TREASURER -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("আয়-ব্যয়", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "finance"),
        NavItem("ক্যাশবক্স", Icons.Filled.Inventory2, Icons.Outlined.Inventory2, "cashbox"),
        NavItem("চাঁদা", Icons.Filled.Payments, Icons.Outlined.Payments, "treasurer_chanda"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    com.mosqueseba.app.data.model.UserRole.ASSISTANT_TREASURER -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("আয়-ব্যয়", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "finance"),
        NavItem("ক্যাশবক্স", Icons.Filled.Inventory2, Icons.Outlined.Inventory2, "cashbox"),
        NavItem("চাঁদা", Icons.Filled.Payments, Icons.Outlined.Payments, "treasurer_chanda"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    com.mosqueseba.app.data.model.UserRole.GENERAL_SECRETARY -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("সদস্য", Icons.Filled.People, Icons.Outlined.People, "members"),
        NavItem("সভা", Icons.Filled.EventAvailable, Icons.Outlined.EventAvailable, "secretary_meetings"),
        NavItem("নথি", Icons.Filled.FolderShared, Icons.Outlined.FolderShared, "office_docs"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    com.mosqueseba.app.data.model.UserRole.ASSISTANT_SECRETARY -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("সদস্য", Icons.Filled.People, Icons.Outlined.People, "members"),
        NavItem("নোটিশ", Icons.Filled.Campaign, Icons.Outlined.Campaign, "notices"),
        NavItem("নথি", Icons.Filled.FolderShared, Icons.Outlined.FolderShared, "office_docs"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    com.mosqueseba.app.data.model.UserRole.ORGANIZING_SECRETARY -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("সদস্য", Icons.Filled.People, Icons.Outlined.People, "members"),
        NavItem("ইভেন্ট", Icons.Filled.Celebration, Icons.Outlined.Celebration, "events"),
        NavItem("সভা", Icons.Filled.EventAvailable, Icons.Outlined.EventAvailable, "secretary_meetings"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    com.mosqueseba.app.data.model.UserRole.PUBLICITY_SECRETARY -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("প্রচারণা", Icons.Filled.ViewCarousel, Icons.Outlined.ViewCarousel, "publicity"),
        NavItem("নোটিশ", Icons.Filled.Campaign, Icons.Outlined.Campaign, "notices"),
        NavItem("ইভেন্ট", Icons.Filled.Celebration, Icons.Outlined.Celebration, "events"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    com.mosqueseba.app.data.model.UserRole.OFFICE_SECRETARY -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("দপ্তর", Icons.Filled.FolderShared, Icons.Outlined.FolderShared, "office_docs"),
        NavItem("সভা", Icons.Filled.EventAvailable, Icons.Outlined.EventAvailable, "secretary_meetings"),
        NavItem("নোটিশ", Icons.Filled.Campaign, Icons.Outlined.Campaign, "notices"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    com.mosqueseba.app.data.model.UserRole.WELFARE_EDUCATION_SECRETARY -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("কল্যাণ", Icons.Filled.VolunteerActivism, Icons.Outlined.VolunteerActivism, "secretary_welfare"),
        NavItem("মাদরাসা", Icons.Filled.School, Icons.Outlined.School, "madrasa"),
        NavItem("নোটিশ", Icons.Filled.Campaign, Icons.Outlined.Campaign, "notices"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    com.mosqueseba.app.data.model.UserRole.EXECUTIVE_MEMBER -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("সাক্ষী", Icons.Filled.Verified, Icons.Outlined.Verified, "cashbox"),
        NavItem("সভা", Icons.Filled.EventAvailable, Icons.Outlined.EventAvailable, "secretary_meetings"),
        NavItem("সদস্য", Icons.Filled.People, Icons.Outlined.People, "committee"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    com.mosqueseba.app.data.model.UserRole.ADVISORY_BOARD -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("উপদেষ্টা", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance, "sharia_advisory"),
        NavItem("হিসাব", Icons.Filled.Assessment, Icons.Outlined.Assessment, "transparency"),
        NavItem("সনদ", Icons.Filled.VerifiedUser, Icons.Outlined.VerifiedUser, "charter"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    com.mosqueseba.app.data.model.UserRole.IMAM -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("শরিয়াহ", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "sharia_advisory"),
        NavItem("রোস্টার", Icons.Filled.DateRange, Icons.Outlined.DateRange, "roster"),
        NavItem("মাদরাসা", Icons.Filled.School, Icons.Outlined.School, "madrasa"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    com.mosqueseba.app.data.model.UserRole.MUAZZIN -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("নামাজ", Icons.Filled.AccessTime, Icons.Outlined.AccessTime, "prayer"),
        NavItem("রোস্টার", Icons.Filled.DateRange, Icons.Outlined.DateRange, "roster"),
        NavItem("খাবার", Icons.Filled.Restaurant, Icons.Outlined.Restaurant, "food"),
        NavItem("প্রোফাইল", Icons.Filled.Badge, Icons.Outlined.Badge, "staff_profile")
    )
    com.mosqueseba.app.data.model.UserRole.KHADEM -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("খাবার", Icons.Filled.Restaurant, Icons.Outlined.Restaurant, "food"),
        NavItem("রোস্টার", Icons.Filled.DateRange, Icons.Outlined.DateRange, "roster"),
        NavItem("নোটিশ", Icons.Filled.Campaign, Icons.Outlined.Campaign, "notices"),
        NavItem("প্রোফাইল", Icons.Filled.Badge, Icons.Outlined.Badge, "staff_profile")
    )
    com.mosqueseba.app.data.model.UserRole.GENERAL_MEMBER -> listOf(
        NavItem("হোম", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("চাঁদা", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "my_chanda"),
        NavItem("হিসাব", Icons.Filled.Assessment, Icons.Outlined.Assessment, "transparency"),
        NavItem("নোটিশ", Icons.Filled.Campaign, Icons.Outlined.Campaign, "notices"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
}

@Composable
fun NavigationBottomBar(
    currentTabIndex: Int,
    currentRole: com.mosqueseba.app.data.model.UserRole,
    onTabSelected: (Int) -> Unit
) {
    val items = getNavItemsForRole(currentRole)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    val isDark = LocalIsDarkTheme.current
    val topBorderColor = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle

    Surface(
        color = surfaceColor,
        shadowElevation = 6.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.8.dp)
                    .background(topBorderColor)
            )
            NavigationBar(
                containerColor = surfaceColor,
                contentColor = activeColor,
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
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeColor,
                            selectedTextColor = activeColor,
                            indicatorColor = activeColor.copy(alpha = 0.16f),
                            unselectedIconColor = inactiveTextColor.copy(alpha = 0.72f),
                            unselectedTextColor = inactiveTextColor
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MosqueNavigationRail(
    currentTabIndex: Int,
    currentRole: com.mosqueseba.app.data.model.UserRole,
    onTabSelected: (Int) -> Unit
) {
    val items = getNavItemsForRole(currentRole)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = surfaceColor,
        shadowElevation = 6.dp
    ) {
        Row {
            NavigationRail(
                containerColor = surfaceColor,
                contentColor = activeColor,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = currentTabIndex == index
                    NavigationRailItem(
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
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = activeColor,
                            selectedTextColor = activeColor,
                            indicatorColor = activeColor.copy(alpha = 0.16f),
                            unselectedIconColor = inactiveTextColor.copy(alpha = 0.72f),
                            unselectedTextColor = inactiveTextColor
                        )
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.5.dp)
                    .background(MosqueGold.copy(alpha = 0.35f))
            )
        }
    }
}

