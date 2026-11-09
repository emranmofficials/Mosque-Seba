package com.mosqueseba.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.R
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.theme.*

@Composable
fun GlobalHeader(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    onNotificationClick: () -> Unit,
    onLogout: (() -> Unit)? = null
) {
    var showRoleMenu by remember { mutableStateOf(false) }

    Surface(
        color = Color.Transparent,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MosqueEmeraldDarkGradient)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: App Logo & User Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Official Mosque Logo
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Mosque Seba Logo",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                    )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = when (currentRole) {
                            UserRole.PRESIDENT -> "মো: রফিকুল ইসলাম"
                            UserRole.VICE_PRESIDENT -> "আলহাজ্ব আব্দুল বারী"
                            UserRole.GENERAL_SECRETARY -> "ইঞ্জি: আব্দুল্লাহ আল মামুন"
                            UserRole.TREASURER -> "হাজী মো: আবুল কাসেম"
                            UserRole.ORGANIZING_SECRETARY -> "মো: আসাদুজ্জামান"
                            UserRole.EXECUTIVE_MEMBER -> "মো: শফিকুল ইসলাম"
                            UserRole.IMAM -> "মাওলানা মুফতি মিজানুর রহমান"
                            UserRole.MUAZZIN -> "হাফেজ ক্বারী ফয়সাল আহমেদ"
                            UserRole.KHADEM -> "মো: বেলাল হোসেন"
                            UserRole.GENERAL_MEMBER -> "মো: তারেক মাহমুদ"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    // Role Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MosqueGold)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = currentRole.titleBn,
                            style = MaterialTheme.typography.labelSmall,
                            color = MosqueEmeraldDark,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Right: Role Switcher & Notification
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Role Switcher button
                Box {
                    IconButton(
                        onClick = { showRoleMenu = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MosqueEmeraldLight)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch Role",
                            tint = MosqueGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false }
                    ) {
                        Text(
                            text = "ভূমিকা পরিবর্তন করুন (পরীক্ষা)",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MosqueTextSecondary
                        )
                        HorizontalDivider()
                        UserRole.entries.forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = role.titleBn,
                                        fontWeight = if (role == currentRole) FontWeight.Bold else FontWeight.Normal,
                                        color = if (role == currentRole) MosqueEmeraldPrimary else MosqueTextPrimary
                                    )
                                },
                                onClick = {
                                    onRoleSelected(role)
                                    showRoleMenu = false
                                }
                            )
                        }
                        if (onLogout != null) {
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "🚪 লগআউট করুন",
                                        fontWeight = FontWeight.Bold,
                                        color = MosqueUrgentRed
                                    )
                                },
                                onClick = {
                                    showRoleMenu = false
                                    onLogout()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Notification bell
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MosqueEmeraldLight)
                        .size(36.dp)
                ) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = MosqueUrgentRed,
                                contentColor = Color.White
                            ) {
                                Text("২", fontSize = 10.sp)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Subtle Gold Islamic Accent Trim
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                MosqueGold.copy(alpha = 0.7f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}
