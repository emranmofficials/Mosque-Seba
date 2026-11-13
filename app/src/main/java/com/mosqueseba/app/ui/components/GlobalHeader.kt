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
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Clickable Mosque Logo + User Name/Role + Mosque Profile Chip
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
                    // Official Mosque Logo -> Opens Mosque Profile
                    Surface(
                        onClick = onMosqueProfileClick,
                        shape = RoundedCornerShape(12.dp),
                        color = if (mosqueLogoBitmap != null) Color.White else Color.White.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MosqueGold.copy(alpha = 0.75f)
                        ),
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("header_mosque_profile_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (mosqueLogoBitmap != null) {
                                Image(
                                    bitmap = mosqueLogoBitmap,
                                    contentDescription = mosqueName?.takeIf { it.isNotBlank() } ?: "মসজিদের প্রোফাইল দেখুন",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo),
                                    contentDescription = mosqueName?.takeIf { it.isNotBlank() } ?: "মসজিদের প্রোফাইল দেখুন",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                )
                            }
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

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            // Role Badge -> Opens User's Own Profile
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MosqueGold)
                                    .clickable(onClick = onUserProfileClick)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = currentRole.titleBn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MosqueEmeraldDark,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Mosque Profile quick pill
                            Surface(
                                onClick = onMosqueProfileClick,
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.14f),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.7.dp,
                                    MosqueGoldLight.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("header_mosque_chip")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mosque,
                                        contentDescription = "মসজিদের প্রোফাইল",
                                        tint = MosqueGoldLight,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "মসজিদ",
                                        fontSize = 10.sp,
                                        color = MosqueGoldLight,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
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
