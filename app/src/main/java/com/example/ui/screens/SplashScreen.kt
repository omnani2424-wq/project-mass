package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AnimatedGlowOrb
import com.example.ui.theme.BackgroundWhite
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.RadiantOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onDismissSplash: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.92f) }

    val infiniteTransition = rememberInfiniteTransition(label = "glow_transition")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_shift"
    )

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        // Splash auto dismiss after 2.6s, or user can tap "Enter Laboratory"
        delay(2600)
        onDismissSplash()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Futuristic Subtle Background Glow Gradients
        Box(
            modifier = Modifier
                .size(420.dp)
                .scale(1.2f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ElectricPurple.copy(alpha = 0.15f),
                            NeonMagenta.copy(alpha = 0.10f),
                            BrightCyan.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value)
        ) {
            // Laboratory Orb Emblem
            AnimatedGlowOrb(
                size = 140.dp,
                showRings = true
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main Dominant Title: PROJECT MASS
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PROJECT",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 6.sp,
                    color = ElectricPurple,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "MASS",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 8.sp,
                    color = NeonMagenta,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Creator Credit: Elegant Secondary Subtitle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF3E8FF).copy(alpha = 0.6f))
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                ElectricPurple.copy(alpha = 0.6f * (1f - gradientShift)),
                                BrightCyan.copy(alpha = 0.6f * gradientShift),
                                NeonMagenta.copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "A PROJECT BY KARTIK AND PRIYANSH",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = Color(0xFF6B21A8),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Subtitle: Ask anything. Learn anything. Build anything.
            Text(
                text = "Ask anything. Learn anything. Build anything.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Next-Gen Laboratory Badge / Enter Action
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(ElectricPurple, NeonMagenta, RadiantOrange)
                        )
                    )
                    .clickable { onDismissSplash() }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .testTag("enter_laboratory_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENTER LABORATORY",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
