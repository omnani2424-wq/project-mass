package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceManager(context: Context) {
    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentSpeakingMessageId = MutableStateFlow<String?>(null)
    val currentSpeakingMessageId: StateFlow<String?> = _currentSpeakingMessageId.asStateFlow()

    init {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                isInitialized = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        _currentSpeakingMessageId.value = null
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _currentSpeakingMessageId.value = null
                    }
                })
            } else {
                Log.e("VoiceManager", "TextToSpeech init failed with status: $status")
            }
        }
    }

    fun speak(text: String, messageId: String? = null, speechRate: Float = 1.0f) {
        if (!isInitialized) return
        stop()

        // Clean markdown syntax for spoken speech
        val cleanedText = text
            .replace(Regex("```[\\s\\S]*?```"), "Code block omitted from speech.")
            .replace(Regex("[*#_`>]"), "")
            .trim()

        if (cleanedText.isBlank()) return

        tts?.setSpeechRate(speechRate)
        _currentSpeakingMessageId.value = messageId
        _isSpeaking.value = true
        tts?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, messageId ?: "mass_speech")
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
        }
        _isSpeaking.value = false
        _currentSpeakingMessageId.value = null
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
