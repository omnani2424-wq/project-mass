package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Brand Colors - Futuristic AI Laboratory
val ElectricPurple = Color(0xFF7C3AED)
val ElectricPurpleLight = Color(0xFF8B5CF6)
val DeepViolet = Color(0xFF5B21B6)
val NeonMagenta = Color(0xFFD946EF)
val VividMagenta = Color(0xFFC026D3)
val BrightCyan = Color(0xFF06B6D4)
val ElectricCyan = Color(0xFF00F0FF)
val DeepCyan = Color(0xFF0E7490)
val NeonBlue = Color(0xFF2563EB)
val SkyBlue = Color(0xFF38BDF8)
val NeonPink = Color(0xFFEC4899)
val HotPink = Color(0xFFF43F5E)
val RadiantOrange = Color(0xFFF97316)
val AmberOrange = Color(0xFFFB923C)

// Sophisticated Light Backgrounds & Glassmorphism
val BackgroundWhite = Color(0xFFFAFBFF)
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceLuminous = Color(0xFFF1F5F9)
val SurfaceCard = Color(0xFFFFFFFF)
val SurfaceCardBorder = Color(0xFFE2E8F0)
val GlassSurface = Color(0xEEFFFFFF)
val GlassBorder = Color(0x337C3AED)

// Typography Colors
val TextPrimary = Color(0xFF0F172A)
val TextSecondary = Color(0xFF475569)
val TextMuted = Color(0xFF64748B)
val TextTertiary = Color(0xFF94A3B8)

// Code & Math Block Colors
val CodeBlockBackground = Color(0xFF0F172A)
val CodeBlockText = Color(0xFFF8FAFC)
val CodeBlockBorder = Color(0xFF334155)
val MathBlockBackground = Color(0xFFF8FAFC)
val MathBlockBorder = Color(0xFFCBD5E1)

// Gradient Palettes
val GradientProjectMass = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF7C3AED),
        Color(0xFFD946EF),
        Color(0xFF06B6D4),
        Color(0xFFF97316)
    )
)

val GradientTitle = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF6D28D9),
        Color(0xFFD946EF),
        Color(0xFF2563EB)
    )
)

val GradientCreators = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF7C3AED),
        Color(0xFF06B6D4),
        Color(0xFFEC4899)
    )
)

val GradientSendButton = Brush.linearGradient(
    colors = listOf(
        Color(0xFF7C3AED),
        Color(0xFFD946EF),
        Color(0xFF06B6D4)
    )
)

val GradientCardGlow = Brush.linearGradient(
    colors = listOf(
        Color(0x1F7C3AED),
        Color(0x1FD946EF),
        Color(0x1F06B6D4)
    )
)

val GradientHeroOrb = Brush.radialGradient(
    colors = listOf(
        Color(0xFF7C3AED).copy(alpha = 0.35f),
        Color(0xFFD946EF).copy(alpha = 0.20f),
        Color(0xFF06B6D4).copy(alpha = 0.10f),
        Color.Transparent
    )
)
