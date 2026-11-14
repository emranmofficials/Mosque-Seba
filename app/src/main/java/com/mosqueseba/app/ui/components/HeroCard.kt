package com.mosqueseba.app.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.R
import com.mosqueseba.app.data.local.entity.MosqueEntity
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.theme.*

@Composable
fun HeroCard(
    mosque: MosqueEntity?,
    onMosqueProfileClick: (() -> Unit)? = null
) {
    val nameBn = mosque?.nameBn ?: "বায়তুল জান্নাত জামে মসজিদ"
    val address = mosque?.address ?: "মিরপুর-১০, ঢাকা-১২১৬"
    val committeeId = mosque?.committeeId ?: "MC-DHK-2024"
    val establishedBn = toBengaliDigits((mosque?.establishedYear ?: 1998).toString())
    val startYearBn = toBengaliDigits((mosque?.committeeStartYear ?: 2024).toString())
    val endYearBn = toBengaliDigits((mosque?.committeeEndYear ?: 2026).toString())
    val mosqueLogoBitmap = remember(mosque?.logoUrl) { loadLocalThumbnail(mosque?.logoUrl, 256) }

    MehrabArchCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onMosqueProfileClick != null) {
                    Modifier.clickable(onClick = onMosqueProfileClick)
                } else Modifier
            ),
        backgroundColor = Color.Transparent,
        borderColor = MosqueCardBorderGold.copy(alpha = 0.5f),
        elevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .background(MosqueEmeraldDarkGradient)
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Committee ID Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = MosqueGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "কমিটি আইডি: $committeeId",
                            style = MaterialTheme.typography.labelSmall,
                            color = MosqueGold,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Established badge
                    Text(
                        text = "স্থাপিত: $establishedBn ইং",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mosque Logo & Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.5.dp, MosqueGoldGradient, RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(if (mosqueLogoBitmap != null) 2.dp else 5.dp)
                            .testTag("hero_mosque_logo"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (mosqueLogoBitmap != null) {
                            Image(
                                bitmap = mosqueLogoBitmap,
                                contentDescription = "মসজিদের অফিসিয়াল লোগো",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "মসজিদের অফিসিয়াল লোগো",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Text(
                        text = nameBn,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Location
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MosqueGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Committee tenure indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "মেয়াদ: $startYearBn — $endYearBn খ্রি.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = if (onMosqueProfileClick != null) "মসজিদ প্রোফাইল ›" else "কার্যনির্বাহী পরিষদ • সক্রিয়",
                        style = MaterialTheme.typography.labelSmall,
                        color = MosqueGoldLight,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Compact Unified Executive Hero Card for President Dashboard — slim height, zero duplicate info, animated feedback.
 */
@Composable
fun ExecutiveMosqueHeroCard(
    mosque: MosqueEntity?,
    currentRole: UserRole,
    showHijriDate: Boolean = true,
    onDutiesClick: () -> Unit,
    onMosqueProfileClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current

    val nameBn = mosque?.nameBn ?: "বায়তুল জান্নাত জামে মসজিদ"
    val baseAddress = mosque?.address ?: "মিরপুর-১০, ঢাকা-১২১৬"
    val fullAddress = remember(baseAddress, mosque?.upazila, mosque?.district) {
        val extra = listOf(mosque?.upazila.orEmpty(), mosque?.district.orEmpty())
            .filter { it.isNotBlank() && !baseAddress.contains(it, ignoreCase = true) }
        if (extra.isNotEmpty()) "$baseAddress (${extra.joinToString(", ")})" else baseAddress
    }
    val committeeId = mosque?.committeeId ?: "MC-DHK-2024"
    val establishedBn = toBengaliDigits((mosque?.establishedYear ?: 1998).toString())
    val startYearBn = toBengaliDigits((mosque?.committeeStartYear ?: 2024).toString())
    val endYearBn = toBengaliDigits((mosque?.committeeEndYear ?: 2026).toString())
    val hijriDate = remember { formatCurrentHijriDateBn() }
    val mosqueLogoBitmap = remember(mosque?.logoUrl) { loadLocalThumbnail(mosque?.logoUrl, 256) }

    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            kotlinx.coroutines.delay(1600)
            copied = false
        }
    }

    MehrabArchCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.Transparent,
        borderColor = MosqueGold.copy(alpha = 0.6f),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MosqueEmeraldDarkGradient)
                .padding(horizontal = 14.dp, vertical = 11.dp)
        ) {
            // Row 1: Compact Hijri Date & Bismillah on one slim strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (showHijriDate) Arrangement.SpaceBetween else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showHijriDate) {
                    Surface(
                        color = MosqueGold.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.7.dp, MosqueGold.copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NightsStay,
                                contentDescription = null,
                                tint = MosqueGoldLight,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = hijriDate,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MosqueGoldLight
                            )
                        }
                    }
                }

                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MosqueGoldLight
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Official Logo + Mosque Name + Address + Duties Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .then(
                            if (onMosqueProfileClick != null) {
                                Modifier.clickable(onClick = onMosqueProfileClick)
                            } else Modifier
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.5.dp, MosqueGoldGradient, RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(if (mosqueLogoBitmap != null) 2.dp else 5.dp)
                            .testTag("hero_mosque_logo"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (mosqueLogoBitmap != null) {
                            Image(
                                bitmap = mosqueLogoBitmap,
                                contentDescription = "মসজিদের অফিসিয়াল লোগো",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "মসজিদের অফিসিয়াল লোগো",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = nameBn,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MosqueGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = fullAddress,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.86f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.14f),
                            border = BorderStroke(0.7.dp, MosqueGold.copy(alpha = 0.45f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = currentRole.roleIcon(),
                                    contentDescription = null,
                                    tint = MosqueGoldLight,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentRole.titleBn,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MosqueGoldLight,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Compact Role Duties Button
                Surface(
                    onClick = onDutiesClick,
                    shape = RoundedCornerShape(8.dp),
                    color = MosqueGold,
                    modifier = Modifier.testTag("president_duties_strip_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "দায়িত্ব তালিকা",
                            tint = MosqueEmeraldDark,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "দায়িত্ব",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MosqueEmeraldDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Slim Footer Strip — Copyable Committee ID + Established & Tenure
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            clipboardManager.setText(AnnotatedString(committeeId))
                            copied = true
                            Toast.makeText(context, "কমিটি আইডি ($committeeId) কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .testTag("hero_copy_committee_id_button")
                ) {
                    AnimatedContent(
                        targetState = copied,
                        transitionSpec = {
                            (fadeIn(tween(180)) + scaleIn(initialScale = 0.85f)) togetherWith
                                (fadeOut(tween(140)) + scaleOut(targetScale = 0.85f))
                        },
                        label = "CopyCommitteeIdAnim"
                    ) { isCopied ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCopied) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                                contentDescription = "কমিটি আইডি কপি করুন",
                                tint = if (isCopied) MosqueGreenAccent else MosqueGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isCopied) "কপি হয়েছে" else "আইডি: $committeeId",
                                fontSize = 11.sp,
                                color = if (isCopied) MosqueGreenAccent else MosqueGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = if (onMosqueProfileClick != null) "প্রোফাইল › ($establishedBn • $startYearBn–$endYearBn)" else "স্থাপিত $establishedBn • মেয়াদ $startYearBn–$endYearBn",
                    fontSize = 10.sp,
                    color = MosqueGoldLight.copy(alpha = 0.92f),
                    fontWeight = FontWeight.SemiBold,
                    modifier = if (onMosqueProfileClick != null) {
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onMosqueProfileClick)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    } else Modifier
                )
            }
        }
    }
}
