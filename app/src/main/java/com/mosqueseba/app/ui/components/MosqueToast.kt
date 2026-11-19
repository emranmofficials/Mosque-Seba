package com.mosqueseba.app.ui.components

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosqueseba.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * টোস্ট নোটিফিকেশনের ধরন (MosqueToastType)
 */
enum class MosqueToastType(
    val defaultTitle: String,
    val icon: ImageVector,
    val iconTint: Color,
    val bgBrush: Brush,
    val borderColor: Color
) {
    SUCCESS(
        defaultTitle = "সফল হয়েছে",
        icon = Icons.Default.CheckCircle,
        iconTint = MosqueGold,
        bgBrush = Brush.linearGradient(
            listOf(
                Color(0xFFAD054F),
                Color(0xFFBE185D),
                Color(0xFF9D174D)
            )
        ),
        borderColor = MosqueGold.copy(alpha = 0.65f)
    ),
    ERROR(
        defaultTitle = "ত্রুটি ঘটেছে",
        icon = Icons.Default.Error,
        iconTint = Color(0xFFFF8A80),
        bgBrush = Brush.linearGradient(
            listOf(
                Color(0xFF5B1010),
                Color(0xFF751818),
                Color(0xFF430B0B)
            )
        ),
        borderColor = Color(0xFFFF8A80).copy(alpha = 0.6f)
    ),
    WARNING(
        defaultTitle = "সতর্কতা",
        icon = Icons.Default.Warning,
        iconTint = MosqueGoldLight,
        bgBrush = Brush.linearGradient(
            listOf(
                Color(0xFF6E4304),
                Color(0xFF8C5506),
                Color(0xFF523102)
            )
        ),
        borderColor = MosqueGold.copy(alpha = 0.75f)
    ),
    INFO(
        defaultTitle = "তথ্য",
        icon = Icons.Default.Info,
        iconTint = Color(0xFFBAE6FD),
        bgBrush = Brush.linearGradient(
            listOf(
                Color(0xFF0C4A6E),
                Color(0xFF075985),
                Color(0xFF082F49)
            )
        ),
        borderColor = Color(0xFF38BDF8).copy(alpha = 0.6f)
    )
}

/**
 * কাস্টম টোস্ট ডাটা মডেল
 */
data class MosqueToastData(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val type: MosqueToastType = MosqueToastType.SUCCESS,
    val title: String? = null,
    val durationMillis: Long = 3200L
)

/**
 * সেন্ট্রাল টোস্ট কন্ট্রোলার (Singleton)
 * অ্যাপের যেকোনো কম্পোজেবল বা নন-কম্পোজেবল লজিক থেকে সহজে টোস্ট দেখানোর সুবিধা
 */
object MosqueToastController {
    private val _currentToast = MutableStateFlow<MosqueToastData?>(null)
    val currentToast: StateFlow<MosqueToastData?> = _currentToast.asStateFlow()

    fun show(
        message: String,
        type: MosqueToastType = MosqueToastType.SUCCESS,
        title: String? = null,
        durationMillis: Long = 3200L
    ) {
        _currentToast.value = MosqueToastData(
            id = System.currentTimeMillis(),
            message = message,
            type = type,
            title = title,
            durationMillis = durationMillis
        )
    }

    fun success(message: String, title: String? = null) {
        show(message = message, type = MosqueToastType.SUCCESS, title = title)
    }

    fun error(message: String, title: String? = null) {
        show(message = message, type = MosqueToastType.ERROR, title = title)
    }

    fun warning(message: String, title: String? = null) {
        show(message = message, type = MosqueToastType.WARNING, title = title)
    }

    fun info(message: String, title: String? = null) {
        show(message = message, type = MosqueToastType.INFO, title = title)
    }

    fun dismiss() {
        _currentToast.value = null
    }
}

/**
 * Context এক্সটেনশন ফাংশন
 */
fun Context.showMosqueToast(
    message: String,
    type: MosqueToastType = MosqueToastType.SUCCESS,
    title: String? = null
) {
    MosqueToastController.show(message, type, title)
}

/**
 * স্ক্রিনে ভাসমান প্রিমিয়াম ইসলামিক টোস্ট কম্পোনেন্ট (MosqueToastHost)
 * এটি MainActivity এর রুটে অবস্থান করবে এবং সব ডায়ালগ ও পেজের উপরে প্রিমিয়ামভাবে ভেসে উঠবে।
 */
@Composable
fun MosqueToastHost(
    modifier: Modifier = Modifier
) {
    val toastData by MosqueToastController.currentToast.collectAsState()

    // স্বয়ংক্রিয়ভাবে নির্দিষ্ট সময় পর টোস্ট হাইড করার ইফেক্ট
    LaunchedEffect(toastData?.id) {
        toastData?.let { toast ->
            delay(toast.durationMillis)
            if (MosqueToastController.currentToast.value?.id == toast.id) {
                MosqueToastController.dismiss()
            }
        }
    }

    AnimatedVisibility(
        visible = toastData != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeOut(),
        modifier = modifier
    ) {
        toastData?.let { toast ->
            MosqueToastBanner(
                toast = toast,
                onDismiss = { MosqueToastController.dismiss() }
            )
        }
    }
}

/**
 * একক টোস্ট ব্যানারের ভিজুয়াল ডিজাইন
 */
@Composable
fun MosqueToastBanner(
    toast: MosqueToastData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.2.dp, toast.type.borderColor),
        shadowElevation = 10.dp,
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(toast.id) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (kotlin.math.abs(offsetX) > 100f) {
                            onDismiss()
                        }
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX += dragAmount
                    }
                )
            }
            .offset(x = offsetX.dp)
            .testTag("mosque_custom_toast")
    ) {
        Box(
            modifier = Modifier
                .background(toast.type.bgBrush)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // বামে ক্যাটাগরি আইকন ও টেক্সট
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = toast.type.icon,
                            contentDescription = toast.type.name,
                            tint = toast.type.iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        val displayTitle = toast.title ?: toast.type.defaultTitle
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = toast.type.iconTint
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = toast.message,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // ডানে ক্রস (ক্লোজ) বাটন
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "বন্ধ করুন",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
