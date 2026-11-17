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

    Surface(
        color = Color.Transparent,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(MosqueEmeraldDarkGradient)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Clickable Mosque Logo + User Name/Role Badge (clean, professional, no institution name above user name)
                val resolvedName = memberName?.takeIf { it.isNotBlank() } ?: currentRole.titleBn

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Official Mosque Seba App Logo -> Permanent in Header (Never replaced by mosque logo)
                    Surface(
                        onClick = onMosqueProfileClick,
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MosqueGold.copy(alpha = 0.85f)
                        ),
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("header_app_logo_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "মসজিদ সেবা লোগো",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = resolvedName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(onClick = onUserProfileClick)
                                .testTag("header_user_name_button")
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Role Badge -> Opens User's Own Profile
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MosqueGold)
                                .clickable(onClick = onUserProfileClick)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (memberName.isNullOrBlank()) "সক্রিয় দায়িত্বশীল" else currentRole.titleBn,
                                style = MaterialTheme.typography.labelSmall,
                                color = MosqueEmeraldDark,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Right: Notification Bell + User Avatar (Profile & Mosque Menu)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Notification bell
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MosqueEmeraldLight)
                            .size(38.dp)
                            .testTag("header_notifications_button")
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
                                contentDescription = "বিজ্ঞপ্তি ও বার্তা",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // User Avatar -> Opens Account, Profiles & Settings Hub
                    val userPhotoBitmap = remember(userPhotoUrl) { loadLocalThumbnail(userPhotoUrl, 192) }
                    Surface(
                        onClick = onAvatarClick,
                        shape = CircleShape,
                        color = MosqueEmeraldLight,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MosqueGold),
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("header_user_avatar_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (userPhotoBitmap != null) {
                                Image(
                                    bitmap = userPhotoBitmap,
                                    contentDescription = "অ্যাকাউন্ট, প্রোফাইল ও সেটিংস",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "অ্যাকাউন্ট, প্রোফাইল ও সেটিংস",
                                    tint = MosqueGoldLight,
                                    modifier = Modifier.size(22.dp)
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
