package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.model.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _currentSpokenTranscription = MutableStateFlow<String?>(null)
    val currentSpokenTranscription: StateFlow<String?> = _currentSpokenTranscription.asStateFlow()

    private val _speechTextResult = MutableStateFlow<String?>(null)
    val speechTextResult: StateFlow<String?> = _speechTextResult.asStateFlow()

    private val _activeUtteranceId = MutableStateFlow<String?>(null)
    val activeUtteranceId: StateFlow<String?> = _activeUtteranceId.asStateFlow()

    // Real-time character span [startIndex, endIndex] for karaoke highlighting
    private val _highlightRange = MutableStateFlow<Pair<Int, Int>?>(null)
    val highlightRange: StateFlow<Pair<Int, Int>?> = _highlightRange.asStateFlow()

    private val _currentWordIndex = MutableStateFlow<Int>(-1)
    val currentWordIndex: StateFlow<Int> = _currentWordIndex.asStateFlow()

    private var speechRate: Float = 0.88f
    private var speechPitch: Float = 1.05f
    private var currentLanguage: AppLanguage = AppLanguage.ENGLISH_US

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = currentLanguage.locale
                tts?.setSpeechRate(speechRate)
                tts?.setPitch(speechPitch)
                isInitialized = true
                setupUtteranceListener()
            }
        }
    }

    fun setLanguage(languageCode: String) {
        val appLang = AppLanguage.fromCode(languageCode)
        currentLanguage = appLang
        if (isInitialized) {
            try {
                val result = tts?.setLanguage(appLang.locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.US
                }
            } catch (_: Exception) {
                tts?.language = Locale.US
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
                    _currentSpokenTranscription.value = null
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                mainHandler.post {
                    _isSpeaking.value = false
                    _activeUtteranceId.value = null
                    _highlightRange.value = null
                    _currentWordIndex.value = -1
                    _currentSpokenTranscription.value = null
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
        _currentSpokenTranscription.value = text
        _activeUtteranceId.value = utteranceId
        _isSpeaking.value = true
        _highlightRange.value = Pair(0, text.indexOf(' ').takeIf { it != -1 } ?: text.length)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun startListening(onTextRecognized: (String) -> Unit, onError: (String) -> Unit = {}) {
        stop()
        _isListening.value = true
        _speechTextResult.value = null

        mainHandler.post {
            try {
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    speechRecognizer?.destroy()
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage.code)
                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak into your microphone...")
                    }

                    speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {}
                        override fun onBeginningOfSpeech() {}
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {
                            _isListening.value = false
                        }

                        override fun onError(error: Int) {
                            _isListening.value = false
                            val errorMsg = when (error) {
                                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                                SpeechRecognizer.ERROR_NETWORK -> "Network required for speech"
                                else -> "Voice assist unavailable"
                            }
                            onError(errorMsg)
                        }

                        override fun onResults(results: Bundle?) {
                            _isListening.value = false
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val recognized = matches?.firstOrNull()
                            if (!recognized.isNull_or_blank()) {
                                _speechTextResult.value = recognized
                                recognized?.let { onTextRecognized(it) }
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            matches?.firstOrNull()?.let {
                                _speechTextResult.value = it
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })

                    speechRecognizer?.startListening(intent)
                } else {
                    _isListening.value = false
                    onError("Speech recognition not supported on this device")
                }
            } catch (e: Exception) {
                _isListening.value = false
                onError(e.message ?: "Voice assist failed")
            }
        }
    }

    fun stopListening() {
        _isListening.value = false
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        stopListening()
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

private fun String?.isNull_or_blank(): Boolean {
    return this == null || this.trim().isEmpty()
}
