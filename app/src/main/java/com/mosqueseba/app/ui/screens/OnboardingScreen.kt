package com.mosqueseba.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.ui.components.toBengaliDigits
import com.mosqueseba.app.ui.theme.*
import kotlinx.coroutines.launch

private data class OnboardingSlideData(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val highlights: List<String>
)

/**
 * আধুনিক অনবোর্ডিং (Onboarding) স্ক্রিন:
 * অ্যাপের মূল বৈশিষ্ট্য ও সুবিধাসমূহ ৪টি আকর্ষণীয় স্লাইডে তুলে ধরে।
 */
@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val scope = rememberCoroutineScope()

    val slides = remember {
        listOf(
            OnboardingSlideData(
                title = "স্বচ্ছ আয়-ব্যয় ও ডিজিটাল চাঁদা",
                subtitle = "মসজিদের নিখুঁত আর্থিক হিসাব",
                description = "সদস্য চাঁদা আদায়, স্বয়ংক্রিয় রসিদ তৈরি, দানবাক্স/ক্যাশ গণনা ও খরচের ভাউচার স্বচ্ছভাবে সংরক্ষণ ও ট্র্যাকিং করুন।",
                icon = Icons.Default.AccountBalance,
                accentColor = MosqueEmeraldPrimary,
                highlights = listOf("ডিজিটাল চাঁদা রশিদ ও খতিয়ান", "স্বয়ংক্রিয় ক্যাশ ও ব্যাংক হিসাব", "ভাউচার অনুমোদন ও জবাবদিহিতা")
            ),
            OnboardingSlideData(
                title = "নামাজের সময়সূচি ও স্মার্ট বোর্ড",
                subtitle = "দৈনিক ওয়াক্ত ও ডিজিটাল নোটিশ",
                description = "৫ ওয়াক্ত আজান ও জামাতের রিয়েলটাইম সময়সূচি, হিজরি ক্যালেন্ডার এবং ডিজিটাল নোটিশ বোর্ডে তাৎক্ষণিক ইসলামিক ঘোষণা।",
                icon = Icons.Default.Schedule,
                accentColor = RoleMusalliAccent,
                highlights = listOf("আজান ও জামাতের সঠিক ওয়াক্ত", "রিয়েলটাইম নোটিশ প্রচার", "কার্যনির্বাহী সভার আপডেট")
            ),
            OnboardingSlideData(
                title = "মাদরাসা, মক্তব ও হিফজ ট্র্যাকার",
                subtitle = "দ্বীনি শিক্ষার ডিজিটাল ব্যবস্থাপনা",
                description = "ছাত্র-ছাত্রীদের বিস্তারিত প্রোফাইল, দৈনিক হাজিরা, মাসিক বেতন ও হিফজ অগ্রগতির নিয়মিত তদারকি এক ক্লিকে।",
                icon = Icons.Default.School,
                accentColor = MosqueGoldDeep,
                highlights = listOf("হিফজ পারা ও পৃষ্ঠা ট্র্যাকিং", "সদস্য তালিকা থেকে অভিভাবক বাছাই", "মাসিক বেতন ও ডিজিটাল হাজিরা")
            ),
            OnboardingSlideData(
                title = "কমিটি নির্বাচন ও স্মার্ট সেবা",
                subtitle = "মসজিদ পরিচালনায় আধুনিক প্রযুক্তি",
                description = "পরিচালনা কমিটির স্বচ্ছ ডিজিটাল নির্বাচন, গোপন ব্যালটে ভোটদান, ফলাফল এবং সমাজকল্যাণ ও খাদ্য ব্যবস্থাপনা।",
                icon = Icons.Default.HowToVote,
                accentColor = MosqueEmeraldDark,
                highlights = listOf("গোপন ও নিরপেক্ষ ডিজিটাল ভোট", "লাইভ ফলাফল ও ভোটার তালিকা", "কল্যাণ তহবিল ও সেবামূলক কার্যক্রম")
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val isLastPage = pagerState.currentPage == slides.size - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(Color(0xFF002214), Color(0xFF032D1B), Color(0xFF02190E))
                    } else {
                        listOf(Color(0xFFF0FDF4), Color(0xFFE8F5E9), Color(0xFFDCEDC8).copy(alpha = 0.5f))
                    }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Bismillah + Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) MosqueGoldLight else MosqueGoldDeep
                )

                if (!isLastPage) {
                    TextButton(
                        onClick = onOnboardingFinished,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "স্কিপ করুন",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White.copy(alpha = 0.8f) else MosqueEmeraldPrimary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // Pager: Slide Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val slide = slides[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Animated Icon Circle
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .shadow(12.dp, shape = CircleShape, spotColor = slide.accentColor)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        slide.accentColor.copy(alpha = 0.85f),
                                        slide.accentColor
                                    )
                                )
                            )
                            .border(3.dp, MosqueGold.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = slide.icon,
                            contentDescription = slide.title,
                            tint = Color.White,
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    // Title
                    Text(
                        text = slide.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color.White else MosqueEmeraldDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Subtitle
                    Text(
                        text = slide.subtitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) MosqueGoldLight else MosqueEmeraldPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Description
                    Text(
                        text = slide.description,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF4B5563),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Key Highlights
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        slide.highlights.forEach { highlight ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isDark) MosqueNightSurfaceVariant.copy(alpha = 0.8f)
                                        else Color.White.copy(alpha = 0.85f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isDark) MosqueNightBorder else MosqueCardBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(slide.accentColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = slide.accentColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = highlight,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF1F2937)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Area: Indicators & Navigation Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    repeat(slides.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val widthAnim by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
                            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                            label = "DotWidth"
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(widthAnim)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isSelected) {
                                        if (isDark) MosqueGold else MosqueEmeraldPrimary
                                    } else {
                                        if (isDark) Color.White.copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.3f)
                                    }
                                )
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pagerState.currentPage > 0) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "পূর্ববর্তী",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("আগে", fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (isLastPage) {
                                onOnboardingFinished()
                            } else {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MosqueEmeraldPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = if (isLastPage) "অ্যাপে প্রবেশ করুন" else "পরবর্তী",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (isLastPage) Icons.Default.Done else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
