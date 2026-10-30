package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val angle: Double,
    val speed: Float,
    val radius: Float,
    val color: Color
)

@Composable
fun CoinBurstOverlay(
    isVisible: Boolean,
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val progress = remember { Animatable(0f) }

    val particles = remember {
        val colors = listOf(
            Color(0xFFFFD700),
            Color(0xFFFFA000),
            Color(0xFFFFECB3),
            Color(0xFFFFCA28),
            Color(0xFFFFFFFF),
            Color(0xFF8C52FF)
        )
        List(40) {
            Particle(
                angle = Random.nextDouble(0.0, Math.PI * 2),
                speed = Random.nextFloat() * 450f + 250f,
                radius = Random.nextFloat() * 6f + 4f,
                color = colors[Random.nextInt(colors.size)]
            )
        }
    }

    LaunchedEffect(isVisible) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
        )
        delay(200)
        onAnimationEnd()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height * 0.42f
            val currentProgress = progress.value
            val alpha = (1f - currentProgress).coerceIn(0f, 1f)

            particles.forEach { particle ->
                val distance = particle.speed * currentProgress
                val gravity = 300f * currentProgress * currentProgress
                val x = centerX + (cos(particle.angle) * distance).toFloat()
                val y = centerY + (sin(particle.angle) * distance).toFloat() + gravity

                drawCircle(
                    color = particle.color.copy(alpha = alpha),
                    radius = particle.radius * (1f - currentProgress * 0.3f),
                    center = Offset(x, y)
                )
            }
        }
    }
}
