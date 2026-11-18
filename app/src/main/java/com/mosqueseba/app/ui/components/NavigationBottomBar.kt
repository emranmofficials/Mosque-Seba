package com.mosqueseba.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val isDark = LocalIsDarkTheme.current
    val surfaceColor = if (isDark) MosqueNightSurface else Color.White
    val borderColor = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
    val activeColor = if (isDark) Color(0xFF34D399) else MosqueEmeraldPrimary
    val inactiveColor = if (isDark) MosqueNightTextSecondary else Color(0xFF64748B)

    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 8.dp
    ) {
        Column {
            NavigationBar(
                containerColor = Color.Transparent,
                contentColor = activeColor,
                tonalElevation = 0.dp,
                windowInsets = NavigationBarDefaults.windowInsets
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
                                modifier = Modifier.size(if (isSelected) 24.dp else 22.dp)
                            )
                        },
                        label = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = item.titleBn,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .height(2.5.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(activeColor)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(2.5.dp))
                                }
                            }
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeColor,
                            selectedTextColor = activeColor,
                            indicatorColor = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft,
                            unselectedIconColor = inactiveColor,
                            unselectedTextColor = inactiveColor
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
    val isDark = LocalIsDarkTheme.current
    val surfaceColor = if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.98f) else MaterialTheme.colorScheme.surface
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f)
    val inactiveTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f)
    val indicatorColor = if (isDark) MosqueGreenAccent.copy(alpha = 0.18f) else activeColor.copy(alpha = 0.12f)

    Surface(
        color = surfaceColor,
        tonalElevation = 2.dp,
        shadowElevation = 8.dp
    ) {
        Row {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.5.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MosqueGold.copy(alpha = 0.5f),
                                MosqueGold.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    )
            )
            NavigationRail(
                containerColor = Color.Transparent,
                contentColor = activeColor,
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp)
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
                                modifier = Modifier.size(if (isSelected) 23.dp else 21.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.titleBn,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = activeColor,
                            selectedTextColor = activeColor,
                            indicatorColor = indicatorColor,
                            unselectedIconColor = inactiveIconColor,
                            unselectedTextColor = inactiveTextColor
                        )
                    )
                }
            }
        }
    }
}

