package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TextPrimary

data class CapabilityItem(
    val title: String,
    val icon: ImageVector,
    val prompt: String,
    val gradientColors: List<Color>
)

val capabilitiesList = listOf(
    CapabilityItem(
        title = "Generate Music",
        icon = Icons.Default.MusicNote,
        prompt = "Generate music: Uplifting cinematic orchestral soundtrack with soaring strings and piano (Lyria 3)",
        gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFFD946EF))
    ),
    CapabilityItem(
        title = "Generate Video from Text",
        icon = Icons.Default.Videocam,
        prompt = "Generate video from text: Cinematic drone flight through a futuristic neon cybercity at sunset in 16:9 (Veo 3.1)",
        gradientColors = listOf(Color(0xFF06B6D4), Color(0xFF2563EB))
    ),
    CapabilityItem(
        title = "Mathematics",
        icon = Icons.Default.Calculate,
        prompt = "Explain and calculate the step-by-step solution to evaluate the integral ∫ x * e^(2x) dx.",
        gradientColors = listOf(Color(0xFF7C3AED), Color(0xFF9333EA))
    ),
    CapabilityItem(
        title = "Physics",
        icon = Icons.Default.Science,
        prompt = "Derive and explain Einstein's mass-energy equivalence and time dilation in special relativity.",
        gradientColors = listOf(Color(0xFF2563EB), Color(0xFF06B6D4))
    ),
    CapabilityItem(
        title = "Chemistry",
        icon = Icons.Default.Biotech,
        prompt = "Explain orbital hybridization (sp, sp2, sp3) with molecular geometry examples.",
        gradientColors = listOf(Color(0xFF059669), Color(0xFF10B981))
    ),
    CapabilityItem(
        title = "Biology",
        icon = Icons.Default.Timeline,
        prompt = "Explain the detailed cellular mechanism of CRISPR-Cas9 gene editing.",
        gradientColors = listOf(Color(0xFF10B981), Color(0xFF06B6D4))
    ),
    CapabilityItem(
        title = "Coding",
        icon = Icons.Default.Code,
        prompt = "Write an idiomatic Kotlin Coroutines Flow pipeline with retry, debounce, and backpressure handling.",
        gradientColors = listOf(Color(0xFFD946EF), Color(0xFFEC4899))
    ),
    CapabilityItem(
        title = "Computer Science",
        icon = Icons.Default.Computer,
        prompt = "Compare Dijkstra's vs A* pathfinding algorithm with time & space complexity breakdowns.",
        gradientColors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
    ),
    CapabilityItem(
        title = "History",
        icon = Icons.Default.HistoryEdu,
        prompt = "Analyze the primary geopolitical catalysts and technological impacts of the Industrial Revolution.",
        gradientColors = listOf(Color(0xFFD97706), Color(0xFFF59E0B))
    ),
    CapabilityItem(
        title = "Geography",
        icon = Icons.Default.Public,
        prompt = "Explain plate tectonics, subduction zones, and the formation of deep ocean trenches.",
        gradientColors = listOf(Color(0xFF0284C7), Color(0xFF38BDF8))
    ),
    CapabilityItem(
        title = "English",
        icon = Icons.Default.MenuBook,
        prompt = "Analyze Shakespeare's use of soliloquy and dramatic irony in Hamlet.",
        gradientColors = listOf(Color(0xFFE11D48), Color(0xFFFB7185))
    ),
    CapabilityItem(
        title = "Research",
        icon = Icons.Default.Search,
        prompt = "Synthesize recent research breakthroughs in quantum computing and error correction.",
        gradientColors = listOf(Color(0xFF7C3AED), Color(0xFF06B6D4))
    ),
    CapabilityItem(
        title = "Writing",
        icon = Icons.Default.EditNote,
        prompt = "Draft an engaging, compelling scientific essay introducing the concept of Dyson Spheres.",
        gradientColors = listOf(Color(0xFFEA580C), Color(0xFFF97316))
    ),
    CapabilityItem(
        title = "Problem Solving",
        icon = Icons.Default.Lightbulb,
        prompt = "Solve this logic riddle step-by-step: You have 12 identical-looking coins where one is counterfeit and has a different weight...",
        gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
    ),
    CapabilityItem(
        title = "General Knowledge",
        icon = Icons.Default.AutoAwesome,
        prompt = "What are the latest verified scientific discoveries and space exploration milestones?",
        gradientColors = listOf(Color(0xFF06B6D4), Color(0xFF10B981))
    )
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CapabilityGrid(
    onSelectCapability: (CapabilityItem) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        capabilitiesList.forEach { item ->
            CapabilityChip(
                item = item,
                onClick = { onSelectCapability(item) }
            )
        }
    }
}

@Composable
fun CapabilityChip(
    item: CapabilityItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chipShape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .clip(chipShape)
            .background(Color.White)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        item.gradientColors.first().copy(alpha = 0.45f),
                        item.gradientColors.last().copy(alpha = 0.25f)
                    )
                ),
                shape = chipShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("capability_chip_${item.title.lowercase().replace(" ", "_")}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                item.gradientColors.first().copy(alpha = 0.15f),
                                item.gradientColors.last().copy(alpha = 0.25f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    modifier = Modifier.size(16.dp),
                    tint = item.gradientColors.first()
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}
