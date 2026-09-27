package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.ChatMessageEntity
import com.example.ui.components.AnimatedGlowOrb
import com.example.ui.components.CapabilityGrid
import com.example.ui.components.FormattedAssistantMessage
import com.example.ui.components.GeneratedImageCard
import com.example.ui.components.GeneratedMusicCard
import com.example.ui.components.GeneratedVideoCard
import com.example.util.AudioPlayerManager
import com.example.ui.theme.BackgroundWhite
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricPurpleLight
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.RadiantOrange
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MassViewModel
import com.example.util.AttachmentHelper
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MassViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showStudioDialog by remember { mutableStateOf(false) }
    var studioInitialMode by remember { mutableStateOf(StudioMode.MUSIC) }

    val messages by viewModel.messages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val attachedFile by viewModel.attachedFile.collectAsState()
    val currentTitle by viewModel.currentSessionTitle.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val speakingMessageId by viewModel.voiceManager.currentSpeakingMessageId.collectAsState()

    val listState = rememberLazyListState()

    // Scroll to bottom on new message
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Media & File pickers
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = AttachmentHelper.uriToScaledBitmap(context, uri)
            val name = AttachmentHelper.getFileName(context, uri)
            viewModel.setAttachedImage(bitmap, uri, name)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.setAttachedImage(bitmap, null, "Camera Photo")
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val textContent = AttachmentHelper.readTextFromUri(context, uri)
            val name = AttachmentHelper.getFileName(context, uri)
            if (textContent != null) {
                viewModel.setAttachedTextDocument(textContent, name)
                Toast.makeText(context, "Document loaded: $name", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Could not read document text", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Speech-to-Text Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = spokenMatches?.firstOrNull()
            if (!recognizedText.isNullOrBlank()) {
                val current = viewModel.inputText.value
                viewModel.inputText.value = if (current.isBlank()) recognizedText else "$current $recognizedText"
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatHistoryDrawer(
                viewModel = viewModel,
                onCloseDrawer = { scope.launch { drawerState.close() } },
                onOpenSettings = { showSettingsDialog = true },
                onOpenStudio = { mode ->
                    studioInitialMode = mode
                    showStudioDialog = true
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                HomeTopBar(
                    title = currentTitle,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onNewChat = { viewModel.createNewSession() },
                    onOpenSettings = { showSettingsDialog = true },
                    onOpenStudio = {
                        studioInitialMode = StudioMode.MUSIC
                        showStudioDialog = true
                    }
                )
            },
            bottomBar = {
                ChatInputBar(
                    inputText = inputText,
                    onInputTextChanged = { viewModel.inputText.value = it },
                    onSendMessage = { viewModel.sendMessage() },
                    onStopGeneration = { viewModel.stopGeneration() },
                    isGenerating = isGenerating,
                    attachedBitmap = attachedFile?.bitmap,
                    attachedName = attachedFile?.name,
                    onClearAttachment = { viewModel.clearAttachment() },
                    onPickPhoto = {
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    onTakePhoto = { cameraLauncher.launch() },
                    onPickDocument = {
                        documentPickerLauncher.launch(
                            arrayOf("text/*", "application/pdf", "*/*")
                        )
                    },
                    onStartVoice = {
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask PROJECT MASS anything...")
                            }
                            speechLauncher.launch(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Voice input unavailable on this device", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onOpenStudio = { mode ->
                        studioInitialMode = mode
                        showStudioDialog = true
                    }
                )
            },
            containerColor = BackgroundWhite,
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (messages.isEmpty()) {
                    // Main Home Welcome Experience
                    HomeWelcomeView(
                        onSelectCapability = { item ->
                            if (item.title == "Generate Music") {
                                studioInitialMode = StudioMode.MUSIC
                                showStudioDialog = true
                            } else if (item.title == "Generate Video from Text" || item.title == "Generate Video") {
                                studioInitialMode = StudioMode.VIDEO
                                showStudioDialog = true
                            } else {
                                viewModel.createNewSession(
                                    category = item.title,
                                    initialPrompt = item.prompt
                                )
                            }
                        },
                        onOpenStudio = { mode ->
                            studioInitialMode = mode
                            showStudioDialog = true
                        },
                        onPasteQuestion = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = clipboard.primaryClip
                            if (clip != null && clip.itemCount > 0) {
                                val text = clip.getItemAt(0).coerceToText(context)?.toString() ?: ""
                                if (text.isNotBlank()) {
                                    viewModel.inputText.value = text
                                    Toast.makeText(context, "Question pasted into message box", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Clipboard is empty. Copy a question first!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Clipboard is empty. Copy a question first!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                } else {
                    // Chat Messages View
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(6.dp)) }

                        items(messages, key = { it.id }) { message ->
                            if (message.role == "user") {
                                UserMessageBubble(
                                    message = message,
                                    onCopyQuestion = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("PROJECT MASS Question", message.content))
                                        Toast.makeText(context, "Question copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    onEditQuestion = {
                                        viewModel.inputText.value = message.content
                                        Toast.makeText(context, "Question pasted into message box", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                AssistantMessageCard(
                                    message = message,
                                    audioPlayerManager = viewModel.audioPlayerManager,
                                    isSpeaking = isSpeaking && speakingMessageId == message.id,
                                    onSpeakClick = { viewModel.speakMessage(message.id, message.content) },
                                    onCopyClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("PROJECT MASS Answer", message.content))
                                        Toast.makeText(context, "Answer copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    onRegenerate = { viewModel.regenerateLastAnswer() }
                                )
                            }
                        }

                        // Thinking State Indicator
                        if (isGenerating) {
                            item {
                                ThinkingAnimationCard(onStop = { viewModel.stopGeneration() })
                            }
                        }

                        // Error Banner
                        if (errorMessage != null && !isGenerating) {
                            item {
                                ErrorBannerCard(
                                    error = errorMessage ?: "Connection issue",
                                    onRetry = { viewModel.regenerateLastAnswer() }
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showStudioDialog) {
        CreativeStudioDialog(
            initialMode = studioInitialMode,
            attachedBitmap = attachedFile?.bitmap,
            viewModel = viewModel,
            onDismiss = { showStudioDialog = false }
        )
    }
}

@Composable
fun HomeTopBar(
    title: String,
    onOpenDrawer: () -> Unit,
    onNewChat: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStudio: () -> Unit = {}
) {
    Surface(
        color = SurfaceWhite,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier.testTag("drawer_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Chat History",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PROJECT MASS",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = ElectricPurple,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(BrightCyan)
                        )
                    }
                    Text(
                        text = if (title != "PROJECT MASS") title else "A PROJECT BY KARTIK AND PRIYANSH",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonMagenta,
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenStudio,
                    modifier = Modifier.testTag("top_studio_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Creative Studio",
                        tint = ElectricPurple
                    )
                }
                IconButton(
                    onClick = onNewChat,
                    modifier = Modifier.testTag("top_new_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = TextSecondary
                    )
                }
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("top_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun HomeWelcomeView(
    onSelectCapability: (com.example.ui.components.CapabilityItem) -> Unit,
    onOpenStudio: (StudioMode) -> Unit = {},
    onPasteQuestion: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow_anim")
    val gradientPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_phase"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            // Laboratory Hero Emblem
            AnimatedGlowOrb(
                size = 100.dp,
                showRings = true
            )
        }

        item {
            // Main Dominant Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PROJECT",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 5.sp,
                    color = ElectricPurple,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "MASS",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 6.sp,
                    color = NeonMagenta,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Mandatory Creator Credit
        item {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF3E8FF).copy(alpha = 0.7f))
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                ElectricPurple.copy(alpha = 0.7f * (1f - gradientPhase)),
                                BrightCyan.copy(alpha = 0.7f * gradientPhase),
                                NeonMagenta.copy(alpha = 0.7f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "A PROJECT BY KARTIK AND PRIYANSH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = Color(0xFF6B21A8),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Subtitle
        item {
            Text(
                text = "Ask anything. Learn anything. Build anything.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }

        // Quick Paste Question Action Card on Welcome Screen
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(
                            ElectricPurple.copy(alpha = 0.6f),
                            BrightCyan.copy(alpha = 0.6f),
                            NeonMagenta.copy(alpha = 0.6f)
                        )
                    )
                ),
                modifier = Modifier
                    .clickable { onPasteQuestion() }
                    .testTag("welcome_paste_question_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste question from clipboard",
                        tint = ElectricPurple,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Paste question from clipboard",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricPurple
                    )
                }
            }
        }

        // Quick Creative Studio Launchers (Music & Video from Text)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Generate Music Button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFAF5FF),
                    border = BorderStroke(1.2.dp, Color(0xFFD8B4FE)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenStudio(StudioMode.MUSIC) }
                        .testTag("welcome_generate_music_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                                text = "Generate Music",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricPurple
                            )
                            Text(
                                text = "Lyria 3 (Clip & Pro)",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Generate Video from Text Button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF0F9FF),
                    border = BorderStroke(1.2.dp, Color(0xFF7DD3FC)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenStudio(StudioMode.VIDEO) }
                        .testTag("welcome_generate_video_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Generate Video",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                            Text(
                                text = "Veo 3.1 (16:9 & 9:16)",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(BrightCyan)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "EXPLORE RESEARCH CAPABILITIES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = ElectricPurple
                )
            }
        }

        // 13 Capability Cards / Chips Grid
        item {
            CapabilityGrid(
                onSelectCapability = onSelectCapability,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun UserMessageBubble(
    message: ChatMessageEntity,
    onCopyQuestion: () -> Unit,
    onEditQuestion: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_message_bubble"),
        horizontalAlignment = Alignment.End
    ) {
        val bubbleShape = RoundedCornerShape(18.dp, 4.dp, 18.dp, 18.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(bubbleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFEDE9FE), Color(0xFFE0E7FF))
                    )
                )
                .border(1.dp, Color(0xFFC7D2FE), bubbleShape)
                .clickable { onCopyQuestion() }
                .padding(14.dp)
        ) {
            // Attached Image Preview if any
            if (!message.imageUri.isNullOrBlank()) {
                AsyncImage(
                    model = message.imageUri,
                    contentDescription = "Attached Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Text(
                text = message.content,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action row: Copy question & Edit/Ask again
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onCopyQuestion() }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .testTag("copy_question_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy question",
                        tint = ElectricPurple,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Copy Question",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricPurple
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onEditQuestion() }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .testTag("edit_question_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Edit question",
                        tint = DeepViolet,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Paste & Re-ask",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepViolet
                    )
                }
            }
        }
    }
}

@Composable
fun AssistantMessageCard(
    message: ChatMessageEntity,
    isSpeaking: Boolean,
    onSpeakClick: () -> Unit,
    onCopyClick: () -> Unit,
    onRegenerate: () -> Unit
) {
    val cardShape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, cardShape)
            .clip(cardShape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), cardShape)
            .padding(16.dp)
            .testTag("assistant_message_card")
    ) {
        // Assistant Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "PROJECT MASS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricPurple
                    )
                    Text(
                        text = "AI Laboratory Intelligence",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            // Quick Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onSpeakClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("speak_answer_button")
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                        contentDescription = "Speak answer",
                        tint = if (isSpeaking) NeonMagenta else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onCopyClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("copy_answer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy answer",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(17.dp)
                    )
                }
                IconButton(
                    onClick = onRegenerate,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("regenerate_answer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Regenerate",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = Color(0xFFF1F5F9))
        Spacer(modifier = Modifier.height(12.dp))

        // Rich Markdown Formatted Response
        FormattedAssistantMessage(
            text = message.content,
            sourcesJson = message.sourcesJson
        )
    }
}

@Composable
fun ThinkingAnimationCard(onStop: () -> Unit) {
    val cardShape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(Color(0xFFFAF5FF))
            .border(1.2.dp, Color(0xFFE9D5FF), cardShape)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("thinking_card"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(ElectricPurple, NeonMagenta, BrightCyan)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "PROJECT MASS is thinking...",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricPurple
                )
                Text(
                    text = "Neural laboratory computation in progress",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFEE2E2))
                .clickable(onClick = onStop)
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("stop_generation_chip")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Stop",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626)
                )
            }
        }
    }
}

@Composable
fun ErrorBannerCard(
    error: String,
    onRetry: () -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(Color(0xFFFEF2F2))
            .border(1.dp, Color(0xFFFECACA), cardShape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = error,
                fontSize = 12.5.sp,
                color = Color(0xFFB91C1C)
            )
        }
        TextButton(
            onClick = onRetry,
            modifier = Modifier.testTag("error_retry_button")
        ) {
            Text(
                text = "Retry",
                color = Color(0xFFDC2626),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ChatInputBar(
    inputText: String,
    onInputTextChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onStopGeneration: () -> Unit,
    isGenerating: Boolean,
    attachedBitmap: Bitmap?,
    attachedName: String?,
    onClearAttachment: () -> Unit,
    onPickPhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickDocument: () -> Unit,
    onStartVoice: () -> Unit
) {
    val context = LocalContext.current
    var attachmentMenuOpen by remember { mutableStateOf(false) }

    Surface(
        color = SurfaceWhite,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Attachment Preview Strip
            if (attachedBitmap != null) {
                Row(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        bitmap = attachedBitmap.asImageBitmap(),
                        contentDescription = "Attachment preview",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = attachedName ?: "Image ready",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onClearAttachment,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove attachment",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Quick Question Toolbar (Paste Question, Copy Question, Clear)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 📋 Paste Question Chip
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF3E8FF),
                        border = BorderStroke(1.dp, Color(0xFFDDD6FE)),
                        modifier = Modifier
                            .clickable {
                                val clip = clipboard.primaryClip
                                if (clip != null && clip.itemCount > 0) {
                                    val text = clip.getItemAt(0).coerceToText(context)?.toString() ?: ""
                                    if (text.isNotBlank()) {
                                        val current = inputText
                                        onInputTextChanged(if (current.isBlank()) text else "$current\n$text")
                                        Toast.makeText(context, "Question pasted from clipboard", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .testTag("paste_question_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste Question",
                                tint = ElectricPurple,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Paste Question",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ElectricPurple
                            )
                        }
                    }

                    // 📑 Copy Question Chip (only when user has entered question text)
                    if (inputText.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier
                                .clickable {
                                    val clip = ClipData.newPlainText("PROJECT MASS Question", inputText)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Question copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                                .testTag("copy_question_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Question",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Copy Question",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2563EB)
                                )
                            }
                        }

                        // Clear Input Chip
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier
                                .clickable { onInputTextChanged("") }
                                .testTag("clear_question_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Clear",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                // Attachment Button with Dropdown
                Box {
                    IconButton(
                        onClick = { attachmentMenuOpen = true },
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                            .testTag("attachment_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Attach file or photo",
                            tint = ElectricPurple,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = attachmentMenuOpen,
                        onDismissRequest = { attachmentMenuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Choose from Gallery") },
                            leadingIcon = {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = ElectricPurple)
                            },
                            onClick = {
                                attachmentMenuOpen = false
                                onPickPhoto()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Take Camera Photo") },
                            leadingIcon = {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BrightCyan)
                            },
                            onClick = {
                                attachmentMenuOpen = false
                                onTakePhoto()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Paste Question") },
                            leadingIcon = {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = DeepViolet)
                            },
                            onClick = {
                                attachmentMenuOpen = false
                                val clip = clipboard.primaryClip
                                if (clip != null && clip.itemCount > 0) {
                                    val text = clip.getItemAt(0).coerceToText(context)?.toString() ?: ""
                                    if (text.isNotBlank()) {
                                        val current = inputText
                                        val newText = if (current.isBlank()) text else "$current\n$text"
                                        onInputTextChanged(newText)
                                        Toast.makeText(context, "Question pasted", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Upload Document / Text") },
                            leadingIcon = {
                                Icon(Icons.Default.Description, contentDescription = null, tint = RadiantOrange)
                            },
                            onClick = {
                                attachmentMenuOpen = false
                                onPickDocument()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text Field with Paste and Clear support
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputTextChanged,
                    placeholder = {
                        Text(
                            text = "Ask PROJECT MASS anything...",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            if (inputText.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        val clip = ClipData.newPlainText("PROJECT MASS Question", inputText)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Question copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("copy_input_question_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy question",
                                        tint = ElectricPurple,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onInputTextChanged("") },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("clear_input_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear text",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = {
                                        val clip = clipboard.primaryClip
                                        if (clip != null && clip.itemCount > 0) {
                                            val pasted = clip.getItemAt(0).coerceToText(context)?.toString() ?: ""
                                            if (pasted.isNotBlank()) {
                                                onInputTextChanged(pasted)
                                                Toast.makeText(context, "Question pasted from clipboard", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("paste_clipboard_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste from clipboard",
                                        tint = ElectricPurple,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("message_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricPurple,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Default
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Microphone voice button
                IconButton(
                    onClick = onStartVoice,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                        .testTag("microphone_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice input",
                        tint = BrightCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Send / Stop button with glowing gradient
                Box(
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isGenerating) {
                                Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626)))
                            } else {
                                Brush.linearGradient(
                                    listOf(ElectricPurple, NeonMagenta, RadiantOrange)
                                )
                            }
                        )
                        .clickable {
                            if (isGenerating) {
                                onStopGeneration()
                            } else {
                                onSendMessage()
                            }
                        }
                        .testTag("send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isGenerating) Icons.Default.Stop else Icons.Default.ArrowUpward,
                        contentDescription = if (isGenerating) "Stop generation" else "Send prompt",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
