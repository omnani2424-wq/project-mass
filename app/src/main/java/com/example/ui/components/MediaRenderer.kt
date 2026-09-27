package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.RadiantOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.util.AudioPlayerManager

@Composable
fun GeneratedImageCard(
    imageUri: String,
    prompt: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showFullDialog by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFFF8FAFC))
            .border(1.2.dp, Color(0xFFE2E8F0), shape)
            .padding(10.dp)
            .testTag("generated_image_card")
    ) {
        // Tag Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonMagenta)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Gemini 3.1 Flash Image • 1K",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonMagenta
                )
            }
            IconButton(
                onClick = { showFullDialog = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "Zoom image",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Image View
        AsyncImage(
            model = imageUri,
            contentDescription = prompt,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { showFullDialog = true },
            contentScale = ContentScale.Crop
        )

        if (prompt.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = prompt,
                fontSize = 12.sp,
                color = TextMuted,
                maxLines = 2,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }

    if (showFullDialog) {
        Dialog(onDismissRequest = { showFullDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .padding(8.dp)
            ) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = prompt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun GeneratedMusicCard(
    audioUri: String,
    prompt: String,
    audioPlayerManager: AudioPlayerManager,
    modifier: Modifier = Modifier
) {
    val playbackState by audioPlayerManager.playbackState.collectAsState()
    val isThisPlaying = playbackState.isPlaying && playbackState.currentAudioUri == audioUri

    val shape = RoundedCornerShape(16.dp)

    // Animated soundwave bars
    val infiniteTransition = rememberInfiniteTransition(label = "soundwave")
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w1"
    )
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w2"
    )
    val wave3 by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w3"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFAF5FF), Color(0xFFF3E8FF))
                )
            )
            .border(1.2.dp, Color(0xFFD8B4FE), shape)
            .padding(14.dp)
            .testTag("generated_music_card")
    ) {
        // Music header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElectricPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Lyria 3 Music Synthesis",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricPurple
                    )
                    Text(
                        text = if (prompt.isNotBlank()) prompt else "Studio Audio Track",
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1
                    )
                }
            }

            // Soundwave animation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.height(20.dp)
            ) {
                val heights = listOf(wave1, wave2, wave3, wave1, wave2)
                heights.forEach { h ->
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height((if (isThisPlaying) (18 * h).dp else 4.dp))
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isThisPlaying) NeonMagenta else Color(0xFFC084FC))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Playback control row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(ElectricPurple, NeonMagenta)
                        )
                    )
                    .clickable {
                        audioPlayerManager.playOrPause(audioUri)
                    }
                    .testTag("music_play_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isThisPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val currentMs = if (playbackState.currentAudioUri == audioUri) playbackState.currentPositionMs else 0
                val totalMs = if (playbackState.currentAudioUri == audioUri && playbackState.durationMs > 0) playbackState.durationMs else 30000

                Slider(
                    value = currentMs.toFloat().coerceIn(0f, totalMs.toFloat()),
                    onValueChange = { newPos ->
                        if (playbackState.currentAudioUri == audioUri) {
                            audioPlayerManager.seekTo(newPos.toInt())
                        }
                    },
                    valueRange = 0f..totalMs.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricPurple,
                        activeTrackColor = ElectricPurple
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentMs),
                        fontSize = 10.5.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = formatTime(totalMs),
                        fontSize = 10.5.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun GeneratedVideoCard(
    videoUri: String?,
    prompt: String,
    aspectRatio: String = "16:9",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(16.dp)
    val isPortrait = aspectRatio == "9:16"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFF0F172A))
            .border(1.2.dp, Color(0xFF334155), shape)
            .padding(12.dp)
            .testTag("generated_video_card")
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
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2563EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Veo 3.1 Video Generation",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrightCyan
                    )
                    Text(
                        text = "Aspect Ratio: $aspectRatio",
                        fontSize = 10.5.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Video container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isPortrait) Modifier.height(300.dp) else Modifier.aspectRatio(16f / 9f)
                )
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                )
                .clickable {
                    if (!videoUri.isNullOrBlank()) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(Uri.parse(videoUri), "video/mp4")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Opening video viewer...", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Video synthesized ($aspectRatio)", Toast.LENGTH_SHORT).show()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(BrightCyan, Color(0xFF2563EB))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play video",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap to Play Video",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        if (prompt.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = prompt,
                fontSize = 11.5.sp,
                color = Color(0xFFCBD5E1),
                maxLines = 2,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

private fun formatTime(ms: Int): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}
