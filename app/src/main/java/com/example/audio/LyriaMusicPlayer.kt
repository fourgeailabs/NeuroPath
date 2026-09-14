package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
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
            prepareAndPlay(tempFile, loop, trackTitle)
        } catch (_: Exception) {
            _isPlaying.value = false
            _currentTrackTitle.value = null
            false
        }
    }

    /** Plays Gemini Live's raw PCM16 24 kHz mono output by wrapping it in a WAV container. */
    suspend fun playPcm16FromBase64(base64Pcm: String, trackTitle: String, loop: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        stop()
        try {
            val pcm = Base64.decode(base64Pcm, Base64.DEFAULT)
            val wav = pcm16ToWav(pcm, sampleRate = 24000, channels = 1)
            val tempFile = File.createTempFile("gemini_live_voice_", ".wav", context.cacheDir)
            FileOutputStream(tempFile).use { it.write(wav) }
            prepareAndPlay(tempFile, loop, trackTitle)
        } catch (_: Exception) {
            _isPlaying.value = false
            _currentTrackTitle.value = null
            false
        }
    }

    private fun prepareAndPlay(tempFile: File, loop: Boolean, trackTitle: String): Boolean {
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
                runCatching { tempFile.delete() }
            }
        }
        _isPlaying.value = true
        _currentTrackTitle.value = trackTitle
        _isLooping.value = loop
        return true
    }

    private fun pcm16ToWav(pcm: ByteArray, sampleRate: Int, channels: Int): ByteArray {
        val bitsPerSample = 16
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8
        val dataLength = pcm.size
        val out = ByteArrayOutputStream(44 + dataLength)
        fun writeIntLE(value: Int) {
            out.write(value and 0xff)
            out.write((value shr 8) and 0xff)
            out.write((value shr 16) and 0xff)
            out.write((value shr 24) and 0xff)
        }
        fun writeShortLE(value: Int) {
            out.write(value and 0xff)
            out.write((value shr 8) and 0xff)
        }
        out.write(byteArrayOf('R'.code.toByte(), 'I'.code.toByte(), 'F'.code.toByte(), 'F'.code.toByte()))
        writeIntLE(36 + dataLength)
        out.write(byteArrayOf('W'.code.toByte(), 'A'.code.toByte(), 'V'.code.toByte(), 'E'.code.toByte()))
        out.write(byteArrayOf('f'.code.toByte(), 'm'.code.toByte(), 't'.code.toByte(), ' '.code.toByte()))
        writeIntLE(16)
        writeShortLE(1)
        writeShortLE(channels)
        writeIntLE(sampleRate)
        writeIntLE(byteRate)
        writeShortLE(blockAlign)
        writeShortLE(bitsPerSample)
        out.write(byteArrayOf('d'.code.toByte(), 'a'.code.toByte(), 't'.code.toByte(), 'a'.code.toByte()))
        writeIntLE(dataLength)
        out.write(pcm)
        return out.toByteArray()
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
