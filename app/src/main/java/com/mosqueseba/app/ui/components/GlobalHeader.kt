package com.mosqueseba.app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    mosqueName: String? = null,
    mosqueLogoUrl: String? = null,
    userPhotoUrl: String? = null,
    notificationCount: Int = 0,
    onRoleSelected: (UserRole) -> Unit,
    onNotificationClick: () -> Unit,
    onMosqueProfileClick: () -> Unit = {},
    onUserProfileClick: () -> Unit = {},
    onAvatarClick: () -> Unit = {},
    onLogout: (() -> Unit)? = null
) {
    val isDark = LocalIsDarkTheme.current
    val surfaceColor = if (isDark) MosqueNightSurface else Color.White
    val borderColor = if (isDark) MosqueNightBorder else MosqueCardBorderSubtle
    val primaryText = if (isDark) MosqueNightTextPrimary else MosqueTextPrimary
    val secondaryText = if (isDark) MosqueNightTextSecondary else MosqueTextSecondary
    val logoBitmap = remember(mosqueLogoUrl) { loadLocalThumbnail(mosqueLogoUrl, 192) }
    val userPhotoBitmap = remember(userPhotoUrl) { loadLocalThumbnail(userPhotoUrl, 192) }

    Surface(
        color = surfaceColor,
        shadowElevation = 1.5.dp,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Logo + Greeting + Mosque Name (Clickable for Mosque Profile)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onMosqueProfileClick)
                    .testTag("header_brand_button")
            ) {
                // Sleek Rounded Mosque/App Logo Container
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft,
                    border = BorderStroke(1.dp, if (isDark) MosqueNightBorder else Color(0xFFD1FAE5)),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (logoBitmap != null) {
                            Image(
                                bitmap = logoBitmap,
                                contentDescription = mosqueName ?: "মসজিদ সেবা",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "মসজিদ সেবা",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    // Salam / Greeting Line
                    val greeting = if (!memberName.isNullOrBlank() && memberName != "সদস্য") {
                        "আসসালামু আলাইকুম, ${memberName.split(" ").firstOrNull() ?: memberName}"
                    } else {
                        "আসসালামু আলাইকুম"
                    }
                    Text(
                        text = greeting,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    // Mosque Name / App Title
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = mosqueName ?: "মসজিদ সেবা",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right: Notification Bell + Profile Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Modern Notification Bell with Tonal Container
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isDark) MosqueNightSurfaceVariant else Color(0xFFF1F5F9))
                        .testTag("header_notifications_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (notificationCount > 0) {
                                Badge(
                                    containerColor = MosqueUrgentRed,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = toBengaliDigits(notificationCount.toString()),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "বিজ্ঞপ্তি ও বার্তা",
                            tint = if (isDark) MosqueNightTextPrimary else Color(0xFF334155),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Modern Circular Profile Avatar
                Surface(
                    onClick = onAvatarClick,
                    shape = CircleShape,
                    color = if (isDark) MosqueNightSurfaceVariant else MosqueEmeraldSoft,
                    border = BorderStroke(1.5.dp, if (isDark) MosqueNightBorder else Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("header_user_avatar_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (userPhotoBitmap != null) {
                            Image(
                                bitmap = userPhotoBitmap,
                                contentDescription = "অ্যাকাউন্ট ও সেটিংস",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "অ্যাকাউন্ট ও সেটিংস",
                                tint = MosqueEmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun loadLocalThumbnail(path: String?, maxDim: Int = 256): ImageBitmap? {
    if (path.isNullOrBlank()) return null
    return try {
        val cleanPath = when {
            path.startsWith("file://") -> path.removePrefix("file://")
            else -> path.trim()
        }
        val file = File(cleanPath)
        if (!file.exists() || !file.isFile) return null
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
