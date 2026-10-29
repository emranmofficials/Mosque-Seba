package com.mosqueseba.app.ui.components

import androidx.compose.foundation.layout.size
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
    com.mosqueseba.app.data.model.UserRole.VICE_PRESIDENT,
    com.mosqueseba.app.data.model.UserRole.TREASURER -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("সদস্য", Icons.Filled.People, Icons.Outlined.People, "members"),
        NavItem("ফাইন্যান্স", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "finance"),
        NavItem("নোটিশ", Icons.Filled.Campaign, Icons.Outlined.Campaign, "notices"),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    com.mosqueseba.app.data.model.UserRole.GENERAL_SECRETARY,
    com.mosqueseba.app.data.model.UserRole.ORGANIZING_SECRETARY,
    com.mosqueseba.app.data.model.UserRole.EXECUTIVE_MEMBER -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("সদস্য", Icons.Filled.People, Icons.Outlined.People, "members"),
        NavItem("নোটিশ ও সভা", Icons.Filled.Campaign, Icons.Outlined.Campaign, "notices"),
        NavItem("সমাজকল্যাণ", Icons.Filled.VolunteerActivism, Icons.Outlined.VolunteerActivism, "welfare"),
        NavItem("অন্যান্য", Icons.Filled.Menu, Icons.Outlined.Menu, "more")
    )
    com.mosqueseba.app.data.model.UserRole.IMAM,
    com.mosqueseba.app.data.model.UserRole.MUAZZIN,
    com.mosqueseba.app.data.model.UserRole.KHADEM -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("রোস্টার", Icons.Filled.DateRange, Icons.Outlined.DateRange, "roster"),
        NavItem("খাবার শিডিউল", Icons.Filled.Restaurant, Icons.Outlined.Restaurant, "food"),
        NavItem("মাদরাসা", Icons.Filled.School, Icons.Outlined.School, "madrasa"),
        NavItem("প্রোফাইল", Icons.Filled.Badge, Icons.Outlined.Badge, "staff_profile")
    )
    com.mosqueseba.app.data.model.UserRole.GENERAL_MEMBER -> listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard"),
        NavItem("আমার চাঁদা", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "my_chanda"),
        NavItem("স্বচ্ছ হিসাব", Icons.Filled.Assessment, Icons.Outlined.Assessment, "transparency"),
        NavItem("নোটিশ", Icons.Filled.Campaign, Icons.Outlined.Campaign, "notices"),
        NavItem("কমিটি", Icons.Filled.Groups, Icons.Outlined.Groups, "committee")
    )
}

@Composable
fun NavigationBottomBar(
    currentTabIndex: Int,
    currentRole: com.mosqueseba.app.data.model.UserRole,
    onTabSelected: (Int) -> Unit
) {
    val items = getNavItemsForRole(currentRole)

    NavigationBar(
        containerColor = Color.White,
        contentColor = MosqueEmeraldPrimary,
        tonalElevation = 8.dp
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
                    indicatorColor = MosqueEmeraldLight.copy(alpha = 0.15f),
                    unselectedIconColor = MosqueTextSecondary,
                    unselectedTextColor = MosqueTextSecondary
                )
            )
        }
    }
}

