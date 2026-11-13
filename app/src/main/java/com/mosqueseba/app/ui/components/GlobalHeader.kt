package com.mosqueseba.app.ui.components

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.R
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.theme.*
import java.io.File

@Composable
fun GlobalHeader(
    currentRole: UserRole,
    memberName: String? = null,
    mosqueLogoUrl: String? = null,
    userPhotoUrl: String? = null,
    notificationCount: Int = 0,
    onRoleSelected: (UserRole) -> Unit,
    onNotificationClick: () -> Unit,
    onLogout: (() -> Unit)? = null
) {
    var showProfileMenu by remember { mutableStateOf(false) }

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
                val resolvedName = memberName?.takeIf { it.isNotBlank() } ?: when (currentRole) {
                    UserRole.PRESIDENT -> "মো: রফিকুল ইসলাম"
                    UserRole.VICE_PRESIDENT -> "আলহাজ্ব আব্দুল বারী"
                    UserRole.GENERAL_SECRETARY -> "ইঞ্জি: আব্দুল্লাহ আল মামুন"
                    UserRole.ASSISTANT_SECRETARY -> "মো: জাকির হোসেন"
                    UserRole.TREASURER -> "হাজী মো: আবুল কাসেম"
                    UserRole.ASSISTANT_TREASURER -> "মো: নজরুল ইসলাম"
                    UserRole.ORGANIZING_SECRETARY -> "মো: আসাদুজ্জামান"
                    UserRole.PUBLICITY_SECRETARY -> "হাফেজ মাওলানা সাইফুল ইসলাম"
                    UserRole.OFFICE_SECRETARY -> "মো: তানভীর আহমেদ"
                    UserRole.WELFARE_EDUCATION_SECRETARY -> "মাওলানা আব্দুর রাজ্জাক"
                    UserRole.EXECUTIVE_MEMBER -> "ডা: তারিকুল ইসলাম"
                    UserRole.IMAM -> "মাওলানা মুফতি মিজানুর রহমান"
                    UserRole.ADVISORY_BOARD -> "আলহাজ্ব শামসুল হক চৌধুরী"
                    UserRole.MUAZZIN -> "কারী মো: ইসমাইল হোসেন"
                    UserRole.KHADEM -> "মো: মোস্তফা মিয়া"
                    UserRole.GENERAL_MEMBER -> "মো: শাহজাহান আলী"
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val mosqueLogoBitmap = remember(mosqueLogoUrl) { loadLocalThumbnail(mosqueLogoUrl, 192) }
                    // Official Mosque Logo
                    if (mosqueLogoBitmap != null) {
                        Image(
                            bitmap = mosqueLogoBitmap,
                            contentDescription = "মসজিদের অফিসিয়াল লোগো",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MosqueGold.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                .background(Color.White)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Mosque Seba Logo",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = resolvedName,
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

                // Right: Notification Bell + User Avatar
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                                if (notificationCount > 0) {
                                    Badge(
                                        containerColor = MosqueUrgentRed,
                                        contentColor = Color.White
                                    ) {
                                        Text(toBengaliDigits(notificationCount.toString()), fontSize = 10.sp)
                                    }
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

                    Spacer(modifier = Modifier.width(8.dp))

                    // User Avatar & Profile Menu
                    val userPhotoBitmap = remember(userPhotoUrl) { loadLocalThumbnail(userPhotoUrl, 192) }
                    Box {
                        Surface(
                            onClick = { showProfileMenu = true },
                            shape = CircleShape,
                            color = MosqueEmeraldLight,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, MosqueGold),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (userPhotoBitmap != null) {
                                    Image(
                                        bitmap = userPhotoBitmap,
                                        contentDescription = "User Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "User Avatar",
                                        tint = MosqueGoldLight,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = showProfileMenu,
                            onDismissRequest = { showProfileMenu = false },
                            shape = RoundedCornerShape(16.dp),
                            containerColor = Color.White
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(IslamicGoldTrimGradient)
                            )
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = resolvedName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueEmeraldDark
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${currentRole.emoji} ${currentRole.titleBn}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MosqueEmeraldPrimary
                                )
                                Text(
                                    text = currentRole.tier.titleBn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MosqueTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            if (onLogout != null) {
                                HorizontalDivider(color = MosqueCardBorderSubtle)
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Logout,
                                            contentDescription = null,
                                            tint = MosqueUrgentRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = "লগআউট করুন",
                                            fontWeight = FontWeight.Bold,
                                            color = MosqueUrgentRed,
                                            fontSize = 13.sp
                                        )
                                    },
                                    onClick = {
                                        showProfileMenu = false
                                        onLogout()
                                    }
                                )
                            }
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

fun loadLocalThumbnail(path: String?, maxDim: Int = 256): ImageBitmap? {
    if (path.isNullOrBlank()) return null
    return try {
        val file = File(path)
        if (!file.exists()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        var sampleSize = 1
        while (bounds.outWidth / sampleSize > maxDim || bounds.outHeight / sampleSize > maxDim) {
            sampleSize *= 2
        }
        val opts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        BitmapFactory.decodeFile(file.absolutePath, opts)?.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}
