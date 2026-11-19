package com.mosqueseba.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.BuildConfig
import com.mosqueseba.app.R
import com.mosqueseba.app.ui.components.toBengaliDigits
import com.mosqueseba.app.ui.theme.*
import kotlinx.coroutines.delay

/**
 * প্রিমিয়াম রিডিজাইন করা স্প্ল্যাশ স্ক্রিন:
 * অ্যাপ চালু হওয়ার সাথে সাথে মসৃণ ইসলামিক গ্রেডিয়েন্ট, সোনালী আলোকবৃত্ত এবং
 * থ্রিডি লোগো অ্যানিমেশন প্রদর্শন করে।
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "SplashAlpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.75f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "SplashScale"
    )

    val ringTransition = rememberInfiniteTransition(label = "RingPulseTransition")
    val pulseAnim by ringTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseRing"
    )

    val outerPulseAnim by ringTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OuterPulseRing"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2100)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MosqueEmeraldPrimary,
                        MosqueEmeraldDark,
                        MosqueEmeraldPrimary,
                        MosqueNightBg
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Outer decorative ambient glow ring
        Box(
            modifier = Modifier
                .size(380.dp)
                .scale(outerPulseAnim)
                .alpha(0.04f)
                .clip(CircleShape)
                .background(MosqueGold)
        )

        // Inner decorative gold glow ring
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(pulseAnim)
                .alpha(0.08f)
                .clip(CircleShape)
                .background(MosqueGold)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .scale(scaleAnim)
                .alpha(alphaAnim)
        ) {
            // Bismillah Calligraphic Header with soft gold glow
            Surface(
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MosqueGold.copy(alpha = 0.3f)),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MosqueGold.copy(alpha = 0.95f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                )
            }

            // 3D Mosque Logo with glowing dual golden border
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(132.dp)
                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(32.dp), spotColor = MosqueGold)
                    .border(
                        width = 2.5.dp,
                        brush = Brush.linearGradient(
                            listOf(MosqueGold, MosqueGoldLight, MosqueGoldDeep)
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(MosqueEmeraldDark)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "মসজিদ সেবা লোগো",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Primary App Title in Bengali
            Text(
                text = "মসজিদ সেবা",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )

            // Sub-tagline with golden accent lines
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .width(28.dp)
                        .background(MosqueGold.copy(alpha = 0.6f))
                )
                Text(
                    text = "  ডিজিটাল ব্যবস্থাপনা  ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MosqueGold,
                    letterSpacing = 2.sp
                )
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .width(28.dp)
                        .background(MosqueGold.copy(alpha = 0.6f))
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Official Slogan Banner
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "মসজিদের খেদমতে আধুনিক প্রযুক্তি, পরিচালনায় স্বচ্ছ আস্থা",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.92f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Stylized Spinner
            CircularProgressIndicator(
                modifier = Modifier.size(26.dp),
                color = MosqueGold,
                strokeWidth = 2.5.dp
            )
        }

        // Bottom Footer Badge
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
                .alpha(alphaAnim),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "নিশ্চয়ই মসজিদসমূহ আল্লাহরই জন্য (সূরা আল-জিন: ১৮)",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "সংস্করণ ${toBengaliDigits(BuildConfig.VERSION_NAME)} • অফিসিয়াল সংস্করণ",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MosqueGold.copy(alpha = 0.85f)
            )
        }
    }
}
