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
    val headerShape = remember { MehrabHeaderShape(archDepthDp = 16.dp) }

    Surface(
        color = Color.Transparent,
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(headerShape)
                .background(MosqueEmeraldDarkGradient)
        ) {
            // Background Arabesque watermark pattern
            IslamicGeometricWatermark(
                patternColor = Color.White.copy(alpha = 0.05f),
                cellSize = 44.dp
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp)
            ) {
                // Top Row: Arched Logo + Brand Title ("মসজিদ সেবা" / "Mosque Seba") + (Bell & Avatar)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Mosque Crest Logo + App Brand Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onMosqueProfileClick)
                            .testTag("header_brand_button")
                    ) {
                        // Arched Mosque Emblem Container
                        Surface(
                            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
                            color = Color.White.copy(alpha = 0.14f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                Brush.verticalGradient(listOf(MosqueGoldLight, MosqueGold))
                            ),
                            modifier = Modifier.size(48.dp)
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

                        Column {
                            // "মসজিদ সেবা" (মসজিদ in White, সেবা in Royal Gold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "মসজিদ ",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.2.sp
                                )
                                Text(
                                    text = "সেবা",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MosqueGold
                                )
                            }
                            Text(
                                text = "Mosque Seba",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.88f),
                                letterSpacing = 0.4.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right: Notification Bell + Profile Avatar
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Notification Bell with Badge
                        IconButton(
                            onClick = onNotificationClick,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f))
                                .size(40.dp)
                                .testTag("header_notifications_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (notificationCount > 0) {
                                        Badge(
                                            containerColor = MosqueUrgentRed,
                                            contentColor = Color.White
                                        ) {
                                            Text(toBengaliDigits(notificationCount.toString()), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "বিজ্ঞপ্তি ও বার্তা",
                                    tint = Color.White,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // User Avatar with clean white circular ring
                        val userPhotoBitmap = remember(userPhotoUrl) { loadLocalThumbnail(userPhotoUrl, 192) }
                        Surface(
                            onClick = onAvatarClick,
                            shape = CircleShape,
                            color = MosqueEmeraldLight,
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                            modifier = Modifier
                                .size(42.dp)
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
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Slogan Banner Row: — সেবায় ঐক্য, উন্নয়নে অগ্রগতি —
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, MosqueGold.copy(alpha = 0.7f))
                                )
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    ArabesqueStarMotif(size = 7.dp, color = MosqueGoldLight, fillAlpha = 0.4f)
                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "সেবায় ঐক্য, উন্নয়নে অগ্রগতি",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MosqueGoldLight,
                        letterSpacing = 0.3.sp
                    )

                    Spacer(modifier = Modifier.width(6.dp))
                    ArabesqueStarMotif(size = 7.dp, color = MosqueGoldLight, fillAlpha = 0.4f)
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(MosqueGold.copy(alpha = 0.7f), Color.Transparent)
                                )
                            )
                    )
                }
            }

            // Bottom Gold Mehrab Outline Stroke
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                MosqueGold.copy(alpha = 0.85f),
                                MosqueGoldLight,
                                MosqueGold.copy(alpha = 0.85f),
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
