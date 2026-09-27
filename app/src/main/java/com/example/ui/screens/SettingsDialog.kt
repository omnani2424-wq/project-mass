package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.api.GeminiClient
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.RadiantOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MassViewModel

@Composable
fun SettingsDialog(
    viewModel: MassViewModel,
    onDismiss: () -> Unit
) {
    val currentStyle by viewModel.responseStyle.collectAsState()
    val enableWebSearch by viewModel.enableWebSearch.collectAsState()
    val speechRate by viewModel.speechRate.collectAsState()
    val autoRead by viewModel.autoRead.collectAsState()
    var showClearConfirm by remember { mutableStateOf(false) }

    val isKeyConfigured = GeminiClient.isApiKeyConfigured()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(1.2.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag("settings_dialog")
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
                                        listOf(ElectricPurple, NeonMagenta)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Personalization",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "PROJECT MASS Laboratory",
                                fontSize = 12.sp,
                                color = ElectricPurple,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(14.dp))

                // Section 1: AI Response Style
                Text(
                    text = "AI RESPONSE STYLE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = ElectricPurple
                )
                Spacer(modifier = Modifier.height(8.dp))

                val styles = listOf(
                    "Balanced" to "Default balance of depth, clarity, and speed",
                    "Concise" to "Quick, dense, direct bullet points and answers",
                    "Detailed" to "Comprehensive deep-dive analysis and context",
                    "Step-by-step" to "Pedagogical sequential proofs, workings, and steps"
                )

                styles.forEach { (style, description) ->
                    val isSelected = currentStyle == style
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFFF5F3FF) else Color(0xFFF8FAFC))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) ElectricPurple else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.responseStyle.value = style }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("style_option_$style"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = style,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) ElectricPurple else TextPrimary
                            )
                            Text(
                                text = description,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(ElectricPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Divider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: Web Search Grounding
                Text(
                    text = "REAL-TIME INTELLIGENCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = BrightCyan
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF0FDF4))
                        .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Google Search Grounding",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Retrieve live web citations for current topics",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                    Switch(
                        checked = enableWebSearch,
                        onCheckedChange = { viewModel.enableWebSearch.value = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF16A34A)
                        ),
                        modifier = Modifier.testTag("web_search_switch")
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                Divider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(14.dp))

                // Section 3: Voice & Speech Settings
                Text(
                    text = "VOICE & SPEECH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = NeonMagenta
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-read Answers",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "PROJECT MASS speaks responses automatically",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Switch(
                        checked = autoRead,
                        onCheckedChange = { viewModel.autoRead.value = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ElectricPurple
                        ),
                        modifier = Modifier.testTag("auto_read_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Speech Rate: ${String.format("%.1fx", speechRate)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Slider(
                    value = speechRate,
                    onValueChange = { viewModel.speechRate.value = it },
                    valueRange = 0.6f..1.6f,
                    steps = 4,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricPurple,
                        activeTrackColor = ElectricPurple
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(14.dp))

                // Section 4: Chat History Management
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEF2F2))
                        .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
                        .clickable { showClearConfirm = true }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("clear_history_row"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Clear All Chat History",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                        Text(
                            text = "Permanently remove all saved conversations",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(14.dp))

                // About & Credits Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))
                            )
                        )
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PROJECT MASS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = ElectricPurple,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "A PROJECT BY KARTIK AND PRIYANSH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonMagenta,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Version 1.0.0 • Next-Generation AI Laboratory",
                        fontSize = 10.5.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isKeyConfigured) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isKeyConfigured) Color(0xFF16A34A) else Color(0xFFD97706))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isKeyConfigured) "AI Intelligence Engine: ACTIVE" else "API Key: Required in Secrets Panel",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isKeyConfigured) Color(0xFF166534) else Color(0xFF92400E)
                        )
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Conversations?") },
            text = { Text("This will permanently delete all conversation history in PROJECT MASS. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllSessions()
                        showClearConfirm = false
                        onDismiss()
                    }
                ) {
                    Text("Clear All", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
