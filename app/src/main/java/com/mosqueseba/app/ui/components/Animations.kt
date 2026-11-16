package com.mosqueseba.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

/**
 * অ্যাপের যেকোনো পেজ, স্ক্রিন বা মডিউলের কার্ড এবং উপাদানগুলোকে
 * পর্যায়ক্রমে (staggered) মসৃণ বাউন্সি এন্ট্রান্স অ্যানিমেশন প্রদান করে।
 */
@Composable
fun StaggeredItemEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    baseDelayMs: Long = 35L,
    maxDelayMs: Long = 280L,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val delayTime = (index * baseDelayMs).coerceAtMost(maxDelayMs)
        delay(delayTime)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)) +
            slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                initialOffsetY = { it / 5 }
            )
    ) {
        content()
    }
}
