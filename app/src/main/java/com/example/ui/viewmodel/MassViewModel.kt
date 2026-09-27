package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ChatMessagePayload
import com.example.data.api.GeminiClient
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.MassDatabase
import com.example.data.repository.ChatRepository
import com.example.util.AudioPlayerManager
import com.example.util.VoiceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

data class AttachedFile(
    val bitmap: Bitmap? = null,
    val uriString: String? = null,
    val name: String = "Attachment"
)

class MassViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MassDatabase.getDatabase(application)
    private val repository = ChatRepository(application, database.chatDao())
    private val geminiClient = GeminiClient()
    val voiceManager = VoiceManager(application)
    val audioPlayerManager = AudioPlayerManager(application)

    val sessions: StateFlow<List<ChatSessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    private val _currentSessionTitle = MutableStateFlow("PROJECT MASS")
    val currentSessionTitle: StateFlow<String> = _currentSessionTitle.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val messages: StateFlow<List<ChatMessageEntity>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val inputText = MutableStateFlow("")
    val attachedFile = MutableStateFlow<AttachedFile?>(null)

    // Personalization & Settings
    val responseStyle = MutableStateFlow("Balanced") // "Concise", "Balanced", "Detailed", "Step-by-step"
    val enableWebSearch = MutableStateFlow(true)
    val speechRate = MutableStateFlow(1.0f)
    val autoRead = MutableStateFlow(false)

    private var activeGenerationJob: Job? = null
    private var messageObserverJob: Job? = null

    init {
        // Automatically select or create the first session
        viewModelScope.launch {
            sessions.collectLatest { sessionList ->
                if (_currentSessionId.value == null && sessionList.isNotEmpty()) {
                    selectSession(sessionList.first().id)
                }
            }
        }
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
        observeSessionMessages(sessionId)
        viewModelScope.launch {
            val session = repository.getSessionById(sessionId)
            _currentSessionTitle.value = session?.title ?: "PROJECT MASS"
        }
    }

    private fun observeSessionMessages(sessionId: String) {
        messageObserverJob?.cancel()
        messageObserverJob = viewModelScope.launch {
            repository.getMessagesForSession(sessionId).collectLatest { list ->
                _messages.value = list
            }
        }
    }

    fun createNewSession(category: String? = null, initialPrompt: String? = null) {
        viewModelScope.launch {
            val session = repository.createNewSession(
                title = if (category != null) "Session: $category" else "New Session",
                category = category
            )
            selectSession(session.id)
            if (!initialPrompt.isNullOrBlank()) {
                inputText.value = initialPrompt
            }
        }
    }

    private suspend fun ensureActiveSession(title: String): String {
        val existing = _currentSessionId.value
        if (existing != null) return existing
        val newSession = repository.createNewSession(
            title = if (title.length > 36) title.take(36) + "..." else title
        )
        selectSession(newSession.id)
        return newSession.id
    }

    fun sendMessage(forcedPrompt: String? = null) {
        val promptToSend = (forcedPrompt ?: inputText.value).trim()
        val currentAttachment = attachedFile.value
        if (promptToSend.isBlank() && currentAttachment?.bitmap == null) return

        val lower = promptToSend.lowercase(Locale.ROOT)

        // Check if user specifically requested media generation in natural language
        if (lower.startsWith("generate image") || lower.startsWith("create image") || lower.startsWith("draw ")) {
            val cleanImgPrompt = promptToSend.replace(Regex("(?i)^(generate image( of)?|create image( of)?|draw )"), "").trim()
            generateImageMedia(if (cleanImgPrompt.isNotBlank()) cleanImgPrompt else promptToSend, "1:1")
            return
        }
        if (lower.startsWith("generate music") || lower.startsWith("compose music") || lower.startsWith("create song") || lower.startsWith("create music")) {
            val cleanMusicPrompt = promptToSend.replace(Regex("(?i)^(generate music( for| of)?|compose music( for| of)?|create song( for)?|create music( for)?)"), "").trim()
            generateMusicMedia(if (cleanMusicPrompt.isNotBlank()) cleanMusicPrompt else promptToSend, true)
            return
        }
        if (lower.startsWith("generate video") || lower.startsWith("create video") || lower.startsWith("animate this")) {
            val cleanVidPrompt = promptToSend.replace(Regex("(?i)^(generate video( of)?|create video( of)?|animate this)"), "").trim()
            generateVideoMedia(if (cleanVidPrompt.isNotBlank()) cleanVidPrompt else promptToSend, "16:9")
            return
        }

        inputText.value = ""
        val attachedBitmap = currentAttachment?.bitmap
        var localImageUri = currentAttachment?.uriString
        clearAttachment()
        _errorMessage.value = null

        activeGenerationJob?.cancel()
        activeGenerationJob = viewModelScope.launch {
            try {
                val sessionId = ensureActiveSession(promptToSend)

                // If image was attached, persist locally
                if (attachedBitmap != null && localImageUri == null) {
                    localImageUri = repository.saveBitmapLocally(attachedBitmap)
                }

                // 1. Save user message in Room (only if not a retry of existing prompt)
                val lastMsg = _messages.value.lastOrNull()
                val isRetry = forcedPrompt != null && lastMsg?.role == "user" && lastMsg.content == promptToSend
                if (!isRetry) {
                    repository.saveUserMessage(
                        sessionId = sessionId,
                        content = promptToSend,
                        imageUri = localImageUri
                    )
                }

                _isGenerating.value = true

                // Prepare history payloads (exclude error messages)
                val historyPayloads = _messages.value
                    .filter { !it.isError }
                    .map { msg ->
                        ChatMessagePayload(
                            role = msg.role,
                            text = msg.content
                        )
                    }

                // 2. Call Gemini Engine with Grounding and Style
                val result = geminiClient.generateResponse(
                    history = historyPayloads,
                    currentPrompt = promptToSend,
                    currentImage = attachedBitmap,
                    responseStyle = responseStyle.value,
                    enableWebSearch = enableWebSearch.value
                )

                if (result.isSuccess) {
                    val assistantMsg = repository.saveAssistantMessage(
                        sessionId = sessionId,
                        content = result.text,
                        sources = result.sources,
                        isError = false
                    )
                    _errorMessage.value = null
                    if (autoRead.value) {
                        voiceManager.speak(result.text, assistantMsg.id, speechRate.value)
                    }
                } else {
                    _errorMessage.value = result.text.ifBlank { result.errorMessage ?: "Request failed" }
                }

            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Failed to generate answer"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    // --- GENERATE MUSIC (Lyria 3) ---
    fun generateMusicMedia(prompt: String, isShortClip: Boolean = true) {
        val currentPrompt = prompt.trim()
        if (currentPrompt.isBlank()) return
        inputText.value = ""
        _errorMessage.value = null

        activeGenerationJob?.cancel()
        activeGenerationJob = viewModelScope.launch {
            try {
                val sessionId = ensureActiveSession("🎵 $currentPrompt")
                val modelName = if (isShortClip) "Lyria 3 Clip (30s)" else "Lyria 3 Pro"

                // Save user request
                repository.saveUserMessage(
                    sessionId = sessionId,
                    content = "🎵 [Music Generation: $modelName]\n$currentPrompt"
                )

                _isGenerating.value = true

                val result = geminiClient.generateMusic(currentPrompt, isShortClip)
                if (result.isSuccess && result.bytes != null) {
                    val audioUri = repository.saveBytesLocally(result.bytes, "wav", "mass_lyria")
                    val explanation = "${result.description}\n\n**Style:** $currentPrompt\n**Engine:** $modelName"
                    repository.saveAssistantMessage(
                        sessionId = sessionId,
                        content = explanation,
                        mediaType = "music",
                        mediaUri = audioUri
                    )
                } else {
                    _errorMessage.value = result.errorMessage ?: "Could not synthesize audio track"
                    repository.saveAssistantMessage(
                        sessionId = sessionId,
                        content = "Music synthesis was unable to complete: ${result.errorMessage}. Please verify API permissions or try another prompt.",
                        isError = true
                    )
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Music generation failed"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    // --- CREATE & EDIT IMAGES (gemini-3.1-flash-image-preview) ---
    fun generateImageMedia(prompt: String, aspectRatio: String = "1:1") {
        val currentPrompt = prompt.trim()
        val currentAttachment = attachedFile.value
        val bitmapToUse = currentAttachment?.bitmap
        clearAttachment()
        inputText.value = ""
        _errorMessage.value = null

        activeGenerationJob?.cancel()
        activeGenerationJob = viewModelScope.launch {
            try {
                val isEditing = bitmapToUse != null
                val actionTag = if (isEditing) "🎨 [Edit Image • Gemini 3.1]" else "🎨 [Create Image • Gemini 3.1]"
                val sessionId = ensureActiveSession(currentPrompt)

                var inputImageUri: String? = null
                if (bitmapToUse != null) {
                    inputImageUri = repository.saveBitmapLocally(bitmapToUse)
                }

                repository.saveUserMessage(
                    sessionId = sessionId,
                    content = "$actionTag\n$currentPrompt",
                    imageUri = inputImageUri
                )

                _isGenerating.value = true

                val result = geminiClient.createOrEditImage(
                    prompt = currentPrompt,
                    inputImage = bitmapToUse,
                    aspectRatio = aspectRatio
                )

                if (result.isSuccess && result.bytes != null) {
                    val imageUri = repository.saveBytesLocally(result.bytes, "png", "mass_gemini_image")
                    val desc = "**Gemini 3.1 Flash Image Synthesis ($aspectRatio)**\n\nPrompt: $currentPrompt\n${result.description}"
                    repository.saveAssistantMessage(
                        sessionId = sessionId,
                        content = desc,
                        mediaType = "image",
                        mediaUri = imageUri
                    )
                } else {
                    _errorMessage.value = result.errorMessage ?: "Failed to generate image"
                    repository.saveAssistantMessage(
                        sessionId = sessionId,
                        content = "Image generation was unable to complete: ${result.errorMessage}. Please verify your prompt or API key.",
                        isError = true
                    )
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Image synthesis failed"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    // --- GENERATE VIDEO & ANIMATE PHOTO (veo-3.1-fast-generate-preview) ---
    fun generateVideoMedia(prompt: String, aspectRatio: String = "16:9") {
        val currentPrompt = prompt.trim()
        val currentAttachment = attachedFile.value
        val bitmapToUse = currentAttachment?.bitmap
        clearAttachment()
        inputText.value = ""
        _errorMessage.value = null

        activeGenerationJob?.cancel()
        activeGenerationJob = viewModelScope.launch {
            try {
                val isAnimating = bitmapToUse != null
                val actionTag = if (isAnimating) "🎬 [Animate Photo • Veo 3.1 ($aspectRatio)]" else "🎬 [Generate Video • Veo 3.1 ($aspectRatio)]"
                val sessionId = ensureActiveSession(currentPrompt)

                var inputImageUri: String? = null
                if (bitmapToUse != null) {
                    inputImageUri = repository.saveBitmapLocally(bitmapToUse)
                }

                repository.saveUserMessage(
                    sessionId = sessionId,
                    content = "$actionTag\n$currentPrompt",
                    imageUri = inputImageUri
                )

                _isGenerating.value = true

                val result = geminiClient.generateVideo(
                    prompt = currentPrompt,
                    inputImage = bitmapToUse,
                    aspectRatio = aspectRatio
                )

                if (result.isSuccess) {
                    var videoUri: String? = null
                    if (result.bytes != null) {
                        videoUri = repository.saveBytesLocally(result.bytes, "mp4", "mass_veo_video")
                    }
                    val desc = "**Veo 3.1 Fast Video Generation**\n\n${result.description}"
                    repository.saveAssistantMessage(
                        sessionId = sessionId,
                        content = desc,
                        mediaType = "video",
                        mediaUri = videoUri ?: inputImageUri
                    )
                } else {
                    _errorMessage.value = result.errorMessage ?: "Video generation failed"
                    repository.saveAssistantMessage(
                        sessionId = sessionId,
                        content = "Video synthesis encountered an issue: ${result.errorMessage}.",
                        isError = true
                    )
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Video generation failed"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun regenerateLastAnswer() {
        val currentMsgs = _messages.value
        val lastUserMessage = currentMsgs.findLast { it.role == "user" }
        if (lastUserMessage != null) {
            val content = lastUserMessage.content
            if (content.contains("[Music Generation")) {
                val prompt = content.substringAfter("\n").trim()
                generateMusicMedia(prompt, true)
            } else if (content.contains("[Create Image") || content.contains("[Edit Image")) {
                val prompt = content.substringAfter("\n").trim()
                generateImageMedia(prompt, "1:1")
            } else if (content.contains("[Generate Video") || content.contains("[Animate Photo")) {
                val prompt = content.substringAfter("\n").trim()
                generateVideoMedia(prompt, "16:9")
            } else {
                sendMessage(forcedPrompt = content)
            }
        }
    }

    fun stopGeneration() {
        activeGenerationJob?.cancel()
        activeGenerationJob = null
        _isGenerating.value = false
    }

    fun setAttachedImage(bitmap: Bitmap?, uri: Uri?, name: String?) {
        attachedFile.value = AttachedFile(
            bitmap = bitmap,
            uriString = uri?.toString(),
            name = name ?: "Image"
        )
    }

    fun setAttachedTextDocument(text: String, name: String) {
        val currentText = inputText.value
        inputText.value = if (currentText.isBlank()) {
            "Please analyze the following document ($name):\n\n$text\n\nQuestion: "
        } else {
            "$currentText\n\n--- Document: $name ---\n$text"
        }
    }

    fun clearAttachment() {
        attachedFile.value = null
    }

    fun renameSession(sessionId: String, newTitle: String) {
        viewModelScope.launch {
            repository.renameSession(sessionId, newTitle)
            if (_currentSessionId.value == sessionId) {
                _currentSessionTitle.value = newTitle
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                val remaining = sessions.value.filter { it.id != sessionId }
                if (remaining.isNotEmpty()) {
                    selectSession(remaining.first().id)
                } else {
                    _currentSessionId.value = null
                    _currentSessionTitle.value = "PROJECT MASS"
                    _messages.value = emptyList()
                }
            }
        }
    }

    fun clearAllSessions() {
        viewModelScope.launch {
            repository.clearAllSessions()
            _currentSessionId.value = null
            _currentSessionTitle.value = "PROJECT MASS"
            _messages.value = emptyList()
        }
    }

    fun speakMessage(messageId: String, text: String) {
        if (voiceManager.currentSpeakingMessageId.value == messageId && voiceManager.isSpeaking.value) {
            voiceManager.stop()
        } else {
            voiceManager.speak(text, messageId, speechRate.value)
        }
    }

    fun stopSpeaking() {
        voiceManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.shutdown()
        audioPlayerManager.release()
        activeGenerationJob?.cancel()
        messageObserverJob?.cancel()
    }
}
