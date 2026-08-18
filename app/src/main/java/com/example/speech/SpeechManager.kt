package com.example.speech

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _activeUtteranceId = MutableStateFlow<String?>(null)
    val activeUtteranceId: StateFlow<String?> = _activeUtteranceId.asStateFlow()

    // Real-time character span [startIndex, endIndex] for karaoke highlighting
    private val _highlightRange = MutableStateFlow<Pair<Int, Int>?>(null)
    val highlightRange: StateFlow<Pair<Int, Int>?> = _highlightRange.asStateFlow()

    // Word index fallback for simulation or timer-based pacing
    private val _currentWordIndex = MutableStateFlow<Int>(-1)
    val currentWordIndex: StateFlow<Int> = _currentWordIndex.asStateFlow()

    private var speechRate: Float = 0.88f
    private var speechPitch: Float = 1.05f

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(speechRate)
                tts?.setPitch(speechPitch)
                isInitialized = true
                setupUtteranceListener()
            }
        }
    }

    fun setSpeechParameters(rate: Float, pitch: Float) {
        speechRate = rate.coerceIn(0.5f, 1.5f)
        speechPitch = pitch.coerceIn(0.6f, 1.5f)
        tts?.setSpeechRate(speechRate)
        tts?.setPitch(speechPitch)
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                mainHandler.post {
                    _isSpeaking.value = true
                    _activeUtteranceId.value = utteranceId
                    _highlightRange.value = null
                    _currentWordIndex.value = 0
                }
            }

            override fun onDone(utteranceId: String?) {
                mainHandler.post {
                    _isSpeaking.value = false
                    _activeUtteranceId.value = null
                    _highlightRange.value = null
                    _currentWordIndex.value = -1
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                mainHandler.post {
                    _isSpeaking.value = false
                    _activeUtteranceId.value = null
                    _highlightRange.value = null
                    _currentWordIndex.value = -1
                }
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                super.onRangeStart(utteranceId, start, end, frame)
                mainHandler.post {
                    _highlightRange.value = Pair(start, end)
                }
            }
        })
    }

    fun speak(text: String, utteranceId: String = "neuropath_speech") {
        if (!isInitialized) return
        stop()
        _activeUtteranceId.value = utteranceId
        _isSpeaking.value = true
        _highlightRange.value = Pair(0, text.indexOf(' ').takeIf { it != -1 } ?: text.length)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun speakWordByWordWithTimer(words: List<String>, onWordAdvance: (Int) -> Unit) {
        // Fallback for visual rhythm animation
        var index = 0
        val wordDurationMs = (400 / speechRate).toLong()
        val runnable = object : Runnable {
            override fun run() {
                if (index < words.size && _isSpeaking.value) {
                    onWordAdvance(index)
                    _currentWordIndex.value = index
                    index++
                    mainHandler.postDelayed(this, wordDurationMs)
                }
            }
        }
        mainHandler.post(runnable)
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
        _activeUtteranceId.value = null
        _highlightRange.value = null
        _currentWordIndex.value = -1
    }

    fun shutdown() {
        stop()
        try {
            tts?.shutdown()
        } catch (_: Exception) {}
    }
}
