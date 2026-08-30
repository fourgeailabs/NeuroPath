package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class LyriaMusicPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrackTitle = MutableStateFlow<String?>(null)
    val currentTrackTitle: StateFlow<String?> = _currentTrackTitle.asStateFlow()

    private val _isLooping = MutableStateFlow(true)
    val isLooping: StateFlow<Boolean> = _isLooping.asStateFlow()

    suspend fun playAudioFromBase64(base64Audio: String, trackTitle: String, loop: Boolean = true): Boolean = withContext(Dispatchers.IO) {
        stop()
        try {
            val audioBytes = Base64.decode(base64Audio, Base64.DEFAULT)
            val tempFile = File.createTempFile("lyria_soundscape_", ".mp3", context.cacheDir)
            FileOutputStream(tempFile).use { it.write(audioBytes) }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                isLooping = loop
                prepare()
                start()
                setOnCompletionListener {
                    if (!loop) {
                        _isPlaying.value = false
                        _currentTrackTitle.value = null
                    }
                }
            }
            _isPlaying.value = true
            _currentTrackTitle.value = trackTitle
            _isLooping.value = loop
            true
        } catch (_: Exception) {
            _isPlaying.value = false
            _currentTrackTitle.value = null
            false
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
            } else {
                player.start()
                _isPlaying.value = true
            }
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _isPlaying.value = false
        _currentTrackTitle.value = null
    }

    fun setLooping(loop: Boolean) {
        _isLooping.value = loop
        mediaPlayer?.isLooping = loop
    }
}
