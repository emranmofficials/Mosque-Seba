package com.mosqueseba.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

/**
 * পদবি → নেভিগেশন আইটেম।
 *
 * নীতিমালা:
 * - সব রোলে ৫টি ট্যাব (Material / bKash নীতিমালা)।
 * - index 2 = ওই পদবির **প্রধান কাজ**; বটম বার ও নেভ রেলে এটিই উত্থাপিত গোল্ড বাটনে
 *   হাইলাইট হয়। তাই `DashboardRolePolicy.quickActionsFor(role)`-এর প্রথম কাজটি
 *   সবসময় ২নম্বর স্লটে রাখা হয়েছে (যেমন: সভাপতি → আয়-ব্যয়, কোষাধ্যক্ষ → চাঁদা,
 *   ইমাম → শরিয়াহ, সাধারণ সদস্য → আমার চাঁদা)।
 * - অন্যান্য দায়িত্ব/অনুমতি (canManageMembers, canScheduleMeetings ইত্যাদি) অনুযায়ী
 *   বাকি স্লটগুলো বেছে নেওয়া হয়; বাকি সব মডিউল "আরও" হাব থেকে পৌঁছানো যায়।
 */
fun getNavItemsForRole(role: com.mosqueseba.app.data.model.UserRole): List<NavItem> = when (role) {
    // নেতৃত্ব: চূড়ান্ত অনুমোদন, সভা, সদস্য — প্রধান কাজ: আর্থিক অনুমোদন
    com.mosqueseba.app.data.model.UserRole.PRESIDENT,
    com.mosqueseba.app.data.model.UserRole.VICE_PRESIDENT -> listOf(
        NavItem("হোম", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("সদস্য", Icons.Filled.People, Icons.Outlined.People, "members"),
        NavItem("আয়-ব্যয়", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "finance"), // PRIMARY
        NavItem("সভা", Icons.Filled.EventAvailable, Icons.Outlined.EventAvailable, "secretary_meetings"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    // কোষাধ্যক্ষ: প্রধান কাজ: চাঁদা আদায় ও রসিদ
    com.mosqueseba.app.data.model.UserRole.TREASURER,
    com.mosqueseba.app.data.model.UserRole.ASSISTANT_TREASURER -> listOf(
        NavItem("হোম", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("আয়-ব্যয়", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "finance"),
        NavItem("চাঁদা", Icons.Filled.Payments, Icons.Outlined.Payments, "treasurer_chanda"), // PRIMARY
        NavItem("ক্যাশবক্স", Icons.Filled.Inventory2, Icons.Outlined.Inventory2, "cashbox"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    // সেক্রেটারিয়েট: প্রধান কাজ: সভা ও রেজুলেশন
    com.mosqueseba.app.data.model.UserRole.GENERAL_SECRETARY,
    com.mosqueseba.app.data.model.UserRole.ASSISTANT_SECRETARY,
    com.mosqueseba.app.data.model.UserRole.OFFICE_SECRETARY -> listOf(
        NavItem("হোম", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("সদস্য", Icons.Filled.People, Icons.Outlined.People, "members"),
        NavItem("সভা", Icons.Filled.EventAvailable, Icons.Outlined.EventAvailable, "secretary_meetings"), // PRIMARY
        NavItem("নথি", Icons.Filled.FolderShared, Icons.Outlined.FolderShared, "office_docs"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    // সাংগঠনিক: প্রধান কাজ: ইভেন্ট আয়োজন
    com.mosqueseba.app.data.model.UserRole.ORGANIZING_SECRETARY -> listOf(
        NavItem("হোম", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("সদস্য", Icons.Filled.People, Icons.Outlined.People, "members"),
        NavItem("ইভেন্ট", Icons.Filled.Celebration, Icons.Outlined.Celebration, "events"), // PRIMARY
        NavItem("সভা", Icons.Filled.EventAvailable, Icons.Outlined.EventAvailable, "secretary_meetings"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    // প্রচারণা: প্রধান কাজ: স্মার্ট বোর্ড / প্রচার
    com.mosqueseba.app.data.model.UserRole.PUBLICITY_SECRETARY -> listOf(
        NavItem("হোম", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("নোটিশ", Icons.Filled.Campaign, Icons.Outlined.Campaign, "notices"),
        NavItem("প্রচারণা", Icons.Filled.ViewCarousel, Icons.Outlined.ViewCarousel, "publicity"), // PRIMARY
        NavItem("ইভেন্ট", Icons.Filled.Celebration, Icons.Outlined.Celebration, "events"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    // কল্যাণ ও শিক্ষা: প্রধান কাজ: সমাজকল্যাণ/যাকাত
    com.mosqueseba.app.data.model.UserRole.WELFARE_EDUCATION_SECRETARY -> listOf(
        NavItem("হোম", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("মাদরাসা", Icons.Filled.School, Icons.Outlined.School, "madrasa"),
        NavItem("কল্যাণ", Icons.Filled.VolunteerActivism, Icons.Outlined.VolunteerActivism, "secretary_welfare"), // PRIMARY
        NavItem("ইভেন্ট", Icons.Filled.Celebration, Icons.Outlined.Celebration, "events"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    // তদারকি: প্রধান কাজ: ক্যাশবক্সে সাক্ষী / সভা
    com.mosqueseba.app.data.model.UserRole.EXECUTIVE_MEMBER -> listOf(
        NavItem("হোম", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("কমিটি", Icons.Filled.People, Icons.Outlined.People, "committee"),
        NavItem("সাক্ষী", Icons.Filled.Verified, Icons.Outlined.Verified, "cashbox"), // PRIMARY
        NavItem("সভা", Icons.Filled.EventAvailable, Icons.Outlined.EventAvailable, "secretary_meetings"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    // উপদেষ্টা পরিষদ: প্রধান কাজ: স্বচ্ছতা/অডিট পর্যবেক্ষণ
    com.mosqueseba.app.data.model.UserRole.ADVISORY_BOARD -> listOf(
        NavItem("হোম", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("শরিয়াহ", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "sharia_advisory"),
        NavItem("হিসাব", Icons.Filled.Assessment, Icons.Outlined.Assessment, "transparency"), // PRIMARY
        NavItem("সনদ", Icons.Filled.VerifiedUser, Icons.Outlined.VerifiedUser, "charter"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    // ইমাম: প্রধান কাজ: শরিয়াহ পর্যালোচনা
    com.mosqueseba.app.data.model.UserRole.IMAM -> listOf(
        NavItem("হোম", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("নামাজ", Icons.Filled.AccessTime, Icons.Outlined.AccessTime, "prayer"),
        NavItem("শরিয়াহ", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "sharia_advisory"), // PRIMARY
        NavItem("মাদরাসা", Icons.Filled.School, Icons.Outlined.School, "madrasa"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    // মুয়াজ্জিন: প্রধান কাজ: ওয়াক্ত রোস্টার
    com.mosqueseba.app.data.model.UserRole.MUAZZIN -> listOf(
        NavItem("হোম", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("নামাজ", Icons.Filled.AccessTime, Icons.Outlined.AccessTime, "prayer"),
        NavItem("রোস্টার", Icons.Filled.DateRange, Icons.Outlined.DateRange, "roster"), // PRIMARY
        NavItem("খাবার", Icons.Filled.Restaurant, Icons.Outlined.Restaurant, "food"),
        NavItem("প্রোফাইল", Icons.Filled.Badge, Icons.Outlined.Badge, "staff_profile")
    )
    // খাদেম: প্রধান কাজ: ডিউটি রোস্টার
    com.mosqueseba.app.data.model.UserRole.KHADEM -> listOf(
        NavItem("হোম", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("খাবার", Icons.Filled.Restaurant, Icons.Outlined.Restaurant, "food"),
        NavItem("ডিউটি", Icons.Filled.CleaningServices, Icons.Outlined.CleaningServices, "roster"), // PRIMARY
        NavItem("নোটিশ", Icons.Filled.Campaign, Icons.Outlined.Campaign, "notices"),
        NavItem("প্রোফাইল", Icons.Filled.Badge, Icons.Outlined.Badge, "staff_profile")
    )
    // সাধারণ সদস্য: প্রধান কাজ: আমার চাঁদা ও রসিদ
    com.mosqueseba.app.data.model.UserRole.GENERAL_MEMBER -> listOf(
        NavItem("হোম", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("হিসাব", Icons.Filled.Assessment, Icons.Outlined.Assessment, "transparency"),
        NavItem("আমার চাঁদা", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "my_chanda"), // PRIMARY
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
    val surfaceColor = if (isDark) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f)
    val inactiveTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f)
    val indicatorColor = activeColor.copy(alpha = if (isDark) 0.24f else 0.14f)
    // Islamic Gold & Emerald Accent Trim
    val topLine = Brush.horizontalGradient(
        colors = listOf(
            Color.Transparent,
            MosqueGold.copy(alpha = if (isDark) 0.8f else 0.65f),
            MosqueGoldLight.copy(alpha = if (isDark) 0.6f else 0.45f),
            MosqueGold.copy(alpha = if (isDark) 0.8f else 0.65f),
            Color.Transparent
        )
    )

    Surface(
        color = surfaceColor,
        tonalElevation = 2.dp,
        shadowElevation = 10.dp
    ) {
        Column {
            // Premium gold accent trim (Islamic identity)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(topLine)
            )
            NavigationBar(
                containerColor = Color.Transparent,
                contentColor = activeColor,
                tonalElevation = 0.dp,
                windowInsets = NavigationBarDefaults.windowInsets
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = currentTabIndex == index
                    val isCenter = index == 2
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onTabSelected(index) },
                        icon = {
                            if (isCenter) {
                                Surface(
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = if (isSelected) MosqueGold else (if (isDark) MaterialTheme.colorScheme.primaryContainer else MosqueEmeraldSoft),
                                    shadowElevation = if (isSelected) 4.dp else 1.dp,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.titleBn,
                                            tint = if (isSelected) Color.White else MosqueEmeraldPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            } else {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.titleBn,
                                    modifier = Modifier.size(if (isSelected) 23.dp else 21.dp)
                                )
                            }
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
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
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
    val indicatorColor = activeColor.copy(alpha = if (isDark) 0.24f else 0.14f)

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
                                MosqueEmeraldPrimary.copy(alpha = 0.7f),
                                MosqueEmeraldPrimary.copy(alpha = 0.7f),
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
                            if (index == 2) {
                                // বটম বারের মতো রেলেও পদবির প্রধান কাজ হাইলাইট
                                Surface(
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = if (isSelected) MosqueGold else (if (isDark) MaterialTheme.colorScheme.primaryContainer else MosqueEmeraldSoft),
                                    shadowElevation = if (isSelected) 4.dp else 1.dp,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.titleBn,
                                            tint = if (isSelected) Color.White else MosqueEmeraldPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            } else {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.titleBn,
                                    modifier = Modifier.size(if (isSelected) 23.dp else 21.dp)
                                )
                            }
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

