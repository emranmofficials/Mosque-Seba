package com.mosqueseba.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.R
import com.mosqueseba.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "SplashAlpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.78f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "SplashScale"
    )

    val pulseAnim by rememberInfiniteTransition(label = "RingPulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF003822),
                        MosqueEmeraldDark,
                        Color(0xFF022B1B),
                        Color(0xFF011A10)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background gold glow circle
        Box(
            modifier = Modifier
                .size(340.dp)
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
                .padding(horizontal = 28.dp)
                .scale(scaleAnim)
                .alpha(alphaAnim)
        ) {
            // Bismillah Calligraphic Header
            Text(
                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MosqueGold.copy(alpha = 0.92f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(26.dp))

            // 3D Mosque Logo with glowing gold halo ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(136.dp)
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(32.dp), spotColor = MosqueGold)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            listOf(MosqueGold, Color(0xFFFFF2B2), MosqueGoldDeep)
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Mosque Seba Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Primary App Title in Bengali & English
            Text(
                text = "মসজিদ সেবা",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .width(28.dp)
                        .background(MosqueGold.copy(alpha = 0.6f))
                )
                Text(
                    text = "  MOSQUE SEBA  ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MosqueGold,
                    letterSpacing = 2.5.sp
                )
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .width(28.dp)
                        .background(MosqueGold.copy(alpha = 0.6f))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Official Slogan Banner
            Surface(
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Text(
                    text = "মসজিদের খেদমতে আধুনিক প্রযুক্তি, পরিচালনায় স্বচ্ছ আস্থা",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(44.dp))

            // Elegant Spinner
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
                .padding(bottom = 28.dp)
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
                text = "সংস্করণ ১.০.৬ • অফিসিয়াল প্রডাকশন এডিশন",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MosqueGold.copy(alpha = 0.8f)
            )
        }
    }
}
