package com.mosqueseba.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.data.model.UserRole
import com.mosqueseba.app.ui.theme.*

/**
 * Islamic Mehrab Arch Card Container
 */
@Composable
fun MehrabArchCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = MosqueCardBorderGold,
    elevation: Dp = 3.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp), spotColor = MosqueGoldDeep.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Gold Arch Accent Header Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(IslamicGoldTrimGradient)
            )
            content()
        }
    }
}

/**
 * Islamic Geometric Divider with Star (۞) Motif
 */
@Composable
fun RubElHizbDivider(
    modifier: Modifier = Modifier,
    title: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, MosqueGoldDeep.copy(alpha = 0.5f))
                    )
                )
        )
        Text(
            text = if (title != null) "  ۞  $title  ۞  " else "  ۞  ",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MosqueGoldDeep,
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(MosqueGoldDeep.copy(alpha = 0.5f), Color.Transparent)
                    )
                )
        )
    }
}

/**
 * Hijri Date & Bismillah Sanctuary Banner
 */
@Composable
fun HijriDateBanner(
    mosqueName: String = "বায়তুল জান্নাত জামে মসজিদ",
    hijriDate: String = "১৯ জমাদিউল আউয়াল ১৪৪৬ হিজরি",
    gregorianDate: String = "সোমবার, ৯ নভেম্বর ২০২৬"
) {
    Surface(
        color = Color(0xFF022C22),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MosqueGold.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF011C15), Color(0xFF022C22), Color(0xFF064E3B))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MosqueGoldLight,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = mosqueName,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = MosqueGold.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MosqueGold.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "🌙 $hijriDate",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MosqueGoldLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = gregorianDate,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }
    }
}

/**
 * Role-Specific Official Workspace Badge & Command Header
 */
@Composable
fun RoleWorkspaceHeader(
    currentRole: UserRole,
    modifier: Modifier = Modifier
) {
    val (title, subtitle, accentColor, icon) = when (currentRole) {
        UserRole.PRESIDENT, UserRole.VICE_PRESIDENT -> Quadruple(
            "নির্বাহী পরিষদ ও সুপ্রিম কন্ট্রোল রুম",
            "কমিটির সর্বোচ্চ সিদ্ধান্ত, ভাউচার অনুমোদন ও অডিট নিরীক্ষা",
            RolePresidentAccent,
            Icons.Default.VerifiedUser
        )
        UserRole.TREASURER -> Quadruple(
            "বায়তুল মাল ও খতিয়ান হিসাব দফতর",
            "আয়-ব্যয় লেজার, চাঁদা রেজিস্টার ও পে-রোল বিতরণ",
            RoleTreasurerAccent,
            Icons.Default.AccountBalanceWallet
        )
        UserRole.GENERAL_SECRETARY, UserRole.ORGANIZING_SECRETARY, UserRole.EXECUTIVE_MEMBER -> Quadruple(
            "প্রশাসনিক সচিবালয় ও রেজুলেশন সেল",
            "সদস্য সমন্বয়, সভার কার্যবিবরণী ও নোটিশ জারি",
            RoleSecretaryAccent,
            Icons.Default.FolderShared
        )
        UserRole.IMAM, UserRole.MUAZZIN, UserRole.KHADEM -> Quadruple(
            "মসজিদ খেদমত ও নূরানী তালিম পোর্টাল",
            "৫ ওয়াক্ত নামাজের জামাত, ডিউটি রোস্টার ও মেহমানদারি",
            RoleImamAccent,
            Icons.Default.Mosque
        )
        UserRole.GENERAL_MEMBER -> Quadruple(
            "মুসুল্লি খেদমত ও স্বচ্ছ তহবিল পোর্টাল",
            "জামাত ট্র্যাকিং, ব্যক্তিগত চাঁদা রসিদ ও দ্বীনি সেবা",
            RoleMusalliAccent,
            Icons.Default.People
        )
    }

    Surface(
        color = accentColor.copy(alpha = 0.08f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MosqueEmeraldDark
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MosqueTextSecondary,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
