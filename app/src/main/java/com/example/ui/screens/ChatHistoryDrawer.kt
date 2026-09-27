package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.data.local.ChatSessionEntity
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MassViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatHistoryDrawer(
    viewModel: MassViewModel,
    onCloseDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStudio: (StudioMode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sessions by viewModel.sessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()

    var sessionToRename by remember { mutableStateOf<ChatSessionEntity?>(null) }
    var newRenameTitle by remember { mutableStateOf("") }
    var sessionToDelete by remember { mutableStateOf<ChatSessionEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(Color(0xFFFAFBFF))
            .padding(16.dp)
            .testTag("chat_history_drawer")
    ) {
        // Drawer Header with Creator Credit
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(ElectricPurple, NeonMagenta, BrightCyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "M",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "PROJECT MASS",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = ElectricPurple,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "A PROJECT BY KARTIK AND PRIYANSH",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonMagenta,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // "New Chat" Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(ElectricPurple, NeonMagenta)
                    )
                )
                .clickable {
                    viewModel.createNewSession()
                    onCloseDrawer()
                }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("drawer_new_chat_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "New Chat",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Creative Studio Quick Launchers (Music & Video)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5F3FF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDD6FE)),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onCloseDrawer()
                        onOpenStudio(StudioMode.MUSIC)
                    }
                    .testTag("drawer_music_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = ElectricPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Music (Lyria)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricPurple
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF0F9FF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD)),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onCloseDrawer()
                        onOpenStudio(StudioMode.VIDEO)
                    }
                    .testTag("drawer_video_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Video (Veo)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Divider(color = Color(0xFFE2E8F0))
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "PREVIOUS SESSIONS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        // Session List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (sessions.isEmpty()) {
                item {
                    Text(
                        text = "No previous sessions yet.\nStart asking questions to begin!",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(sessions, key = { it.id }) { session ->
                    val isSelected = session.id == currentSessionId
                    var menuExpanded by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFFEDE9FE) else Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) ElectricPurple else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                viewModel.selectSession(session.id)
                                onCloseDrawer()
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("session_item_${session.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = if (isSelected) ElectricPurple else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = session.title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) ElectricPurple else TextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = formatDate(session.updatedAt),
                                fontSize = 10.5.sp,
                                color = TextMuted
                            )
                        }

                        Box {
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Rename") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        sessionToRename = session
                                        newRenameTitle = session.title
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = Color.Red) },
                                    leadingIcon = {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        sessionToDelete = session
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Divider(color = Color(0xFFE2E8F0))
        Spacer(modifier = Modifier.height(10.dp))

        // Drawer Footer: Settings button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    onCloseDrawer()
                    onOpenSettings()
                }
                .padding(vertical = 8.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = Color(0xFF475569),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Settings & Personalization",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
    }

    // Rename Dialog
    sessionToRename?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToRename = null },
            title = { Text("Rename Session") },
            text = {
                OutlinedTextField(
                    value = newRenameTitle,
                    onValueChange = { newRenameTitle = it },
                    label = { Text("Session Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newRenameTitle.isNotBlank()) {
                            viewModel.renameSession(session.id, newRenameTitle.trim())
                        }
                        sessionToRename = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Dialog
    sessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Delete Session?") },
            text = { Text("Are you sure you want to delete \"${session.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSession(session.id)
                        sessionToDelete = null
                    }
                ) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
