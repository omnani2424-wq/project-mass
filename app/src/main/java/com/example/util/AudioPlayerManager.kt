package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AudioPlaybackState(
    val currentAudioUri: String? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0
)

class AudioPlayerManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    fun playOrPause(uriString: String) {
        val currentState = _playbackState.value

        if (currentState.currentAudioUri == uriString && mediaPlayer != null) {
            if (mediaPlayer?.isPlaying == true) {
                pause()
            } else {
                resume()
            }
            return
        }

        // New audio track
        stop()
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, Uri.parse(uriString))
                prepare()
                start()
            }

            val duration = mediaPlayer?.duration ?: 0
            _playbackState.value = AudioPlaybackState(
                currentAudioUri = uriString,
                isPlaying = true,
                currentPositionMs = 0,
                durationMs = duration
            )

            mediaPlayer?.setOnCompletionListener {
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    currentPositionMs = 0
                )
                stopProgressUpdates()
            }

            mediaPlayer?.setOnErrorListener { _, what, extra ->
                Log.e("AudioPlayerManager", "MediaPlayer error: $what, $extra")
                stop()
                true
            }

            startProgressUpdates()
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Failed to play audio: ${e.message}", e)
            stop()
        }
    }

    private fun resume() {
        mediaPlayer?.let {
            it.start()
            _playbackState.value = _playbackState.value.copy(isPlaying = true)
            startProgressUpdates()
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
            _playbackState.value = _playbackState.value.copy(isPlaying = false)
            stopProgressUpdates()
        }
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.let {
            it.seekTo(positionMs)
            _playbackState.value = _playbackState.value.copy(currentPositionMs = positionMs)
        }
    }

    fun stop() {
        stopProgressUpdates()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        }
        mediaPlayer = null
        _playbackState.value = AudioPlaybackState()
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = scope.launch {
            while (isActive && mediaPlayer?.isPlaying == true) {
                val current = mediaPlayer?.currentPosition ?: 0
                val duration = mediaPlayer?.duration ?: _playbackState.value.durationMs
                _playbackState.value = _playbackState.value.copy(
                    currentPositionMs = current,
                    durationMs = duration
                )
                delay(200)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stop()
    }
}
