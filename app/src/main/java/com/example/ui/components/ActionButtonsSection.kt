package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletLight

@Composable
fun ActionButtonsSection(
    isPendingRewarded: Boolean,
    isPendingInterstitial: Boolean,
    onWatchRewarded: () -> Unit,
    onShowInterstitial: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Continuous idle animations
    val infiniteTransition = rememberInfiniteTransition(label = "buttons_idle")

    // Gentle pulsing glow
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    // Moving shimmer offset
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    // Gift icon wiggle (rotates between -14 and 14 deg every few seconds)
    val giftWiggle by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gift_wiggle"
    )

    // TV icon slight vertical bounce
    val tvBounce by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tv_bounce"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Rewarded Ad Big Button
        InteractiveAdButton(
            testTag = "watch_rewarded_ad_button",
            isLoading = isPendingRewarded,
            loadingText = "Loading ad...",
            defaultTitle = "Watch Rewarded Ad (+10 Coins)",
            icon = {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = "Gift Icon",
                    tint = AccentGold,
                    modifier = Modifier
                        .size(26.dp)
                        .graphicsLayer(rotationZ = giftWiggle)
                )
            },
            gradientColors = listOf(
                Color(0xFF8C52FF),
                Color(0xFF6722ED),
                Color(0xFF4A10B8)
            ),
            borderColor = Color(0xFFFFD700).copy(alpha = glowAlpha),
            shimmerOffset = shimmerOffset,
            onClick = onWatchRewarded
        )

        // Interstitial Ad Big Button
        InteractiveAdButton(
            testTag = "show_interstitial_ad_button",
            isLoading = isPendingInterstitial,
            loadingText = "Loading ad...",
            defaultTitle = "Show Interstitial Ad",
            icon = {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = "TV Icon",
                    tint = Color(0xFF64FFDA),
                    modifier = Modifier
                        .size(26.dp)
                        .graphicsLayer(translationY = tvBounce)
                )
            },
            gradientColors = listOf(
                Color(0xFF1E3A8A),
                Color(0xFF1E1B4B),
                Color(0xFF311068)
            ),
            borderColor = Color(0xFF64FFDA).copy(alpha = glowAlpha * 0.9f),
            shimmerOffset = shimmerOffset,
            onClick = onShowInterstitial
        )
    }
}

@Composable
private fun InteractiveAdButton(
    testTag: String,
    isLoading: Boolean,
    loadingText: String,
    defaultTitle: String,
    icon: @Composable () -> Unit,
    gradientColors: List<Color>,
    borderColor: Color,
    shimmerOffset: Float,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f),
        label = "button_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .testTag(testTag)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = gradientColors
                )
            )
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .pointerInput(isLoading) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        if (!isLoading) {
                            onClick()
                        }
                    }
                )
            }
            .padding(vertical = 18.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Moving shimmer highlight overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        start = Offset(shimmerOffset, 0f),
                        end = Offset(shimmerOffset + 200f, 200f)
                    )
                )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = loadingText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )
            } else {
                icon()
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = defaultTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}
