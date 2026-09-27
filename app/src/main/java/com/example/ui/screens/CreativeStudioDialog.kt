package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.RadiantOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MassViewModel

enum class StudioMode {
    MUSIC, IMAGE, VIDEO
}

@Composable
fun CreativeStudioDialog(
    initialMode: StudioMode = StudioMode.IMAGE,
    attachedBitmap: Bitmap? = null,
    viewModel: MassViewModel,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialMode.ordinal) }
    LaunchedEffect(initialMode) {
        selectedTab = initialMode.ordinal
    }
    var prompt by remember { mutableStateOf("") }

    // Music options
    var isShortClip by remember { mutableStateOf(true) } // lyria-3-clip-preview vs lyria-3-pro-preview

    // Image options
    var imageAspectRatio by remember { mutableStateOf("1:1") } // 1:1, 16:9, 9:16, 4:3

    // Video options
    var videoAspectRatio by remember { mutableStateOf("16:9") } // 16:9 or 9:16 (strictly required)

    val currentMode = StudioMode.values()[selectedTab]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(1.2.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag("creative_studio_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(ElectricPurple, NeonMagenta, BrightCyan)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "PROJECT MASS Studio",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "Lyria 3 • Gemini 3.1 Flash Image • Veo 3.1",
                                fontSize = 11.sp,
                                color = ElectricPurple,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_studio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Studio Mode Tabs
                val tabTitles = listOf("🎵 Music", "🎨 Image", "🎬 Video")
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ElectricPurple
                        )
                    },
                    containerColor = Color(0xFFF8FAFC),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == index) ElectricPurple else TextSecondary
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content per mode
                when (currentMode) {
                    StudioMode.MUSIC -> {
                        Text(
                            text = "LYRIA 3 MUSIC SYNTHESIS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricPurple,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Clip vs Pro toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OptionChip(
                                title = "Short Clip (30s)",
                                subtitle = "lyria-3-clip-preview",
                                isSelected = isShortClip,
                                onClick = { isShortClip = true },
                                modifier = Modifier.weight(1f)
                            )
                            OptionChip(
                                title = "Full Track",
                                subtitle = "lyria-3-pro-preview",
                                isSelected = !isShortClip,
                                onClick = { isShortClip = false },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Music Presets
                        Text(text = "Try a style:", fontSize = 11.5.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                "Cinematic Orchestral",
                                "Synthwave 80s",
                                "Lo-Fi Study Beats"
                            ).forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .clickable { prompt = preset }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = preset, fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }
                    }

                    StudioMode.IMAGE -> {
                        Text(
                            text = "GEMINI 3.1 FLASH IMAGE SYNTHESIS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonMagenta,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (attachedBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFDF4FF))
                                    .border(1.dp, Color(0xFFF0ABFC), RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "📸 Photo Attached: PROJECT MASS will edit or restyle this photo based on your prompt!",
                                    fontSize = 12.sp,
                                    color = Color(0xFF86198F),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Aspect Ratio Options
                        Text(text = "Aspect Ratio:", fontSize = 11.5.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("1:1", "16:9", "9:16", "4:3").forEach { ar ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (imageAspectRatio == ar) Color(0xFFFDF4FF) else Color(0xFFF1F5F9))
                                        .border(
                                            1.dp,
                                            if (imageAspectRatio == ar) NeonMagenta else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { imageAspectRatio = ar }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ar,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (imageAspectRatio == ar) NeonMagenta else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    StudioMode.VIDEO -> {
                        Text(
                            text = "VEO 3.1 FAST VIDEO GENERATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrightCyan,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (attachedBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFECFEFF))
                                    .border(1.dp, Color(0xFF67E8F9), RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "🎬 Photo Animation Mode: Veo 3.1 will animate your uploaded photo into video!",
                                    fontSize = 12.sp,
                                    color = Color(0xFF0E7490),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Mandatory aspect ratio selection (16:9 or 9:16)
                        Text(text = "Aspect Ratio (Required):", fontSize = 11.5.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OptionChip(
                                title = "16:9 Landscape",
                                subtitle = "Cinematic widescreen",
                                isSelected = videoAspectRatio == "16:9",
                                onClick = { videoAspectRatio = "16:9" },
                                modifier = Modifier.weight(1f)
                            )
                            OptionChip(
                                title = "9:16 Portrait",
                                subtitle = "Vertical mobile view",
                                isSelected = videoAspectRatio == "9:16",
                                onClick = { videoAspectRatio = "9:16" },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Video Presets
                        Text(text = "Try a video scene:", fontSize = 11.5.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                "Cyberpunk City (16:9)" to "16:9",
                                "Neon Rain Walk (9:16)" to "9:16",
                                "Hyperlapse Aurora (16:9)" to "16:9"
                            ).forEach { (presetTitle, presetAspect) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .clickable {
                                            prompt = "A breathtaking cinematic video of $presetTitle with volumetric lighting and smooth camera motion"
                                            videoAspectRatio = presetAspect
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(text = presetTitle, fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Prompt Input
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = {
                        Text(
                            text = when (currentMode) {
                                StudioMode.MUSIC -> "Describe the music, instrumentation, mood, tempo..."
                                StudioMode.IMAGE -> if (attachedBitmap != null) "Describe modifications to the attached photo..." else "Describe the image to synthesize..."
                                StudioMode.VIDEO -> if (attachedBitmap != null) "Describe how to animate the photo..." else "Describe the video scene, camera motion..."
                            },
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studio_prompt_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricPurple,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Generate Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                when (currentMode) {
                                    StudioMode.MUSIC -> listOf(ElectricPurple, NeonMagenta)
                                    StudioMode.IMAGE -> listOf(NeonMagenta, RadiantOrange)
                                    StudioMode.VIDEO -> listOf(BrightCyan, Color(0xFF2563EB))
                                }
                            )
                        )
                        .clickable {
                            if (prompt.isNotBlank()) {
                                when (currentMode) {
                                    StudioMode.MUSIC -> {
                                        viewModel.generateMusicMedia(prompt.trim(), isShortClip)
                                    }
                                    StudioMode.IMAGE -> {
                                        viewModel.generateImageMedia(prompt.trim(), imageAspectRatio)
                                    }
                                    StudioMode.VIDEO -> {
                                        viewModel.generateVideoMedia(prompt.trim(), videoAspectRatio)
                                    }
                                }
                                onDismiss()
                            }
                        }
                        .padding(vertical = 13.dp)
                        .testTag("studio_generate_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (currentMode) {
                                StudioMode.MUSIC -> Icons.Default.MusicNote
                                StudioMode.IMAGE -> Icons.Default.Image
                                StudioMode.VIDEO -> Icons.Default.Videocam
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (currentMode) {
                                StudioMode.MUSIC -> if (isShortClip) "GENERATE MUSIC CLIP (30s)" else "GENERATE FULL TRACK"
                                StudioMode.IMAGE -> if (attachedBitmap != null) "EDIT ATTACHED PHOTO" else "GENERATE IMAGE (1K)"
                                StudioMode.VIDEO -> if (attachedBitmap != null) "ANIMATE PHOTO INTO VIDEO" else "GENERATE VEO VIDEO ($videoAspectRatio)"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OptionChip(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(if (isSelected) Color(0xFFF5F3FF) else Color(0xFFF8FAFC))
            .border(
                1.5.dp,
                if (isSelected) ElectricPurple else Color(0xFFE2E8F0),
                shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) ElectricPurple else TextPrimary
        )
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = TextMuted
        )
    }
}
