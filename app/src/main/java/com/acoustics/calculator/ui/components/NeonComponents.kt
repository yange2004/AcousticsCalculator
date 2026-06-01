package com.acoustics.calculator.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acoustics.calculator.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ================================================================
// 🎆 V2.0 FLASHY / NEON UI COMPONENTS
// ================================================================

/**
 * Neon glowing card with animated gradient border
 */
@Composable
fun NeonCard(
    modifier: Modifier = Modifier,
    glowColor: Color = NeonCyan,
    borderWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neonGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glowAlpha"
    )

    Card(
        modifier = modifier
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        glowColor.copy(alpha = glowAlpha),
                        glowColor.copy(alpha = glowAlpha * 0.6f),
                        glowColor.copy(alpha = glowAlpha)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

/**
 * Glassmorphism card with frosted glass effect
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 8.dp,
    alpha: Float = 0.15f,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = alpha)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .blur(blurRadius)
                .then(Modifier.padding(16.dp)),
            content = content
        )
    }
}

/**
 * Animated sound wave background
 */
@Composable
fun SoundWaveBackground(
    modifier: Modifier = Modifier,
    waveColor: Color = NeonCyan,
    numWaves: Int = 3,
    amplitude: Float = 30f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "soundWave")
    val phaseOffsets = (0 until numWaves).map { i ->
        infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween((3000 + i * 500), easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "phase$i"
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        phaseOffsets.forEachIndexed { index, phase ->
            val path = Path()
            val alpha = 1.0f - (index.toFloat() / numWaves)

            path.moveTo(0f, h / 2)
            for (x in 0..w.toInt()) {
                val y = h / 2 + sin((x / w * 360f + phase.value) * PI.toFloat() / 180f) * amplitude * (1.0f - index * 0.2f)
                if (x == 0) path.moveTo(x.toFloat(), y)
                else path.lineTo(x.toFloat(), y)
            }

            drawPath(
                path,
                color = waveColor.copy(alpha = alpha * 0.3f),
                style = Stroke(
                    width = (2f - index * 0.5f),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

/**
 * Pulsing gradient button
 */
@Composable
fun NeonButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    icon: @Composable (() -> Unit)? = null,
    gradientColors: List<Color> = listOf(NeonCyan, NeonBlue, NeonPurple),
    enabled: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neonButton")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "gradientRotation"
    )

    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(
                            100f * cos(rotation * PI.toFloat() / 180f),
                            100f * sin(rotation * PI.toFloat() / 180f)
                        )
                    ),
                    RoundedCornerShape(14.dp)
                )
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon?.invoke()
            if (icon != null) Spacer(Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

/**
 * Animated gradient text
 */
@Composable
fun GradientText(
    text: String,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(NeonCyan, NeonPink, NeonPurple),
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineMedium
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradientText")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "gradientOffset"
    )

    Text(
        text = text,
        modifier = modifier,
        style = style.copy(
            brush = Brush.linearGradient(
                colors = gradientColors,
                start = Offset(offset, 0f),
                end = Offset(offset + 200f, 0f)
            )
        )
    )
}

/**
 * Animated spectrum analyzer / sound level meter bars
 */
@Composable
fun SpectrumBars(
    modifier: Modifier = Modifier,
    barCount: Int = 16,
    color: Color = NeonCyan,
    heights: List<Float>? = null
) {
    if (heights != null) {
        Row(
            modifier = modifier.fillMaxWidth().height(60.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            heights.forEach { h ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(h.coerceIn(0.05f, 1f))
                            .background(
                                color.copy(alpha = 0.5f + h * 0.5f),
                                RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                            )
                    )
                }
            }
        }
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "spectrum")
        Row(
            modifier = modifier.fillMaxWidth().height(60.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            (0 until barCount).forEach { i ->
                val h by infiniteTransition.animateFloat(
                    initialValue = 0.1f, targetValue = 0.9f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400 + i * 50, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ), label = "bar$i"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(h.coerceIn(0.05f, 1f))
                            .background(
                                color.copy(alpha = 0.5f + h * 0.5f),
                                RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                            )
                    )
                }
            }
        }
    }
}

/**
 * Floating particle effect background
 */
@Composable
fun ParticleBackground(modifier: Modifier = Modifier) {
    val particles = remember {
        (0 until 30).map {
            Particle(
                x = Math.random().toFloat(),
                y = Math.random().toFloat(),
                size = (2f + Math.random().toFloat() * 4f),
                speed = 0.002f + Math.random().toFloat() * 0.005f,
                alpha = 0.3f + Math.random().toFloat() * 0.5f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "particleTime"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEachIndexed { i, p ->
            val x = (p.x + time * p.speed * 0.1f) % 1.0f
            val y = (p.y + time * p.speed * 0.05f) % 1.0f
            drawCircle(
                color = Color.White.copy(alpha = p.alpha),
                radius = p.size * size.minDimension / 500f,
                center = Offset(x * size.width, y * size.height)
            )
        }
    }
}

/**
 * Animated glowing dot (for status indicators)
 */
@Composable
fun GlowingDot(
    color: Color = NeonGreen,
    size: Dp = 8.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glowingDot")
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = size.value, targetValue = size.value * 3,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glowSize"
    )

    Box(contentAlignment = Alignment.Center) {
        // Glow
        Box(
            modifier = Modifier
                .size(glowRadius.dp)
                .blur(4.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.3f))
        )
        // Core
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
        )
    }
}

private data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float
)
