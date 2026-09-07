package com.example.ui.components

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
import com.example.ui.theme.*

data class NavItem(
    val titleBn: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun NavigationBottomBar(
    currentTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val items = listOf(
        NavItem("ড্যাশবোর্ড", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
        NavItem("সদস্য", Icons.Filled.People, Icons.Outlined.People),
        NavItem("ফাইন্যান্স", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
        NavItem("নোটিশ", Icons.Filled.Campaign, Icons.Outlined.Campaign),
        NavItem("আরও", Icons.Filled.Menu, Icons.Outlined.Menu)
    )

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
