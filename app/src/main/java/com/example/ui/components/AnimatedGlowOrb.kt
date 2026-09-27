package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedGlowOrb(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    showRings: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val counterRotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_rotation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = (this.size.minDimension / 2.6f) * pulseScale

            // Outer Radial Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6).copy(alpha = 0.45f),
                        Color(0xFFD946EF).copy(alpha = 0.25f),
                        Color(0xFF06B6D4).copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.5f
                ),
                radius = radius * 1.5f,
                center = center
            )

            // Inner Core
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF7C3AED),
                        Color(0xFFD946EF),
                        Color(0xFF06B6D4)
                    ),
                    start = Offset(center.x - radius, center.y - radius),
                    end = Offset(center.x + radius, center.y + radius)
                ),
                radius = radius * 0.72f,
                center = center
            )

            if (showRings) {
                // Orbital Ring 1
                rotate(rotationAngle, pivot = center) {
                    drawOval(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFF00F0FF).copy(alpha = 0.85f),
                                Color(0xFFD946EF).copy(alpha = 0.3f),
                                Color(0xFF7C3AED).copy(alpha = 0.9f)
                            )
                        ),
                        topLeft = Offset(center.x - radius * 1.25f, center.y - radius * 0.5f),
                        size = androidx.compose.ui.geometry.Size(radius * 2.5f, radius),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // Orbital Ring 2 (Tilted)
                rotate(counterRotationAngle + 45f, pivot = center) {
                    drawOval(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFF8A00).copy(alpha = 0.85f),
                                Color(0xFFD946EF).copy(alpha = 0.85f),
                                Color(0xFF06B6D4).copy(alpha = 0.2f)
                            )
                        ),
                        topLeft = Offset(center.x - radius * 1.15f, center.y - radius * 0.65f),
                        size = androidx.compose.ui.geometry.Size(radius * 2.3f, radius * 1.3f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
    }
}
