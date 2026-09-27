package com.fourgeailabs.neuropath.speech

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
import android.speech.tts.Voice
import android.util.Log
import com.fourgeailabs.neuropath.data.model.AppLanguage
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

    private var speechRate: Float = 0.92f
    private val _speechRateFlow = MutableStateFlow(0.92f)
    val speechRateFlow: StateFlow<Float> = _speechRateFlow.asStateFlow()

    private var speechPitch: Float = 0.95f
    private var currentLanguage: AppLanguage = AppLanguage.ENGLISH_US

    // Voice selection: the available device voices for the current language and
    // the chosen soothing voice. The engine default is often a harsh low-quality
    // voice, so we actively pick the highest-quality one available.
    private val _availableVoices = MutableStateFlow<List<Voice>>(emptyList())
    val availableVoices: StateFlow<List<Voice>> = _availableVoices.asStateFlow()
    private val _currentVoiceName = MutableStateFlow<String?>(null)
    val currentVoiceName: StateFlow<String?> = _currentVoiceName.asStateFlow()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = currentLanguage.locale
                tts?.setSpeechRate(speechRate)
                tts?.setPitch(speechPitch)
                _speechRateFlow.value = speechRate
                isInitialized = true
                selectBestVoice()
                setupUtteranceListener()
            }
        }
    }

    /**
     * Picks the smoothest available voice for the current language: highest
     * reported quality first, preferring on-device voices (no network latency)
     * when quality ties. Falls back to the engine default when none qualify.
     */
    private fun selectBestVoice() {
        val engine = tts ?: return
        try {
            val lang = currentLanguage.locale.language
            val candidates = engine.voices
                ?.filter { it.locale.language == lang && !it.isNetworkConnectionRequired }
                ?.sortedWith(
                    compareByDescending<Voice> { it.quality }
                        .thenByDescending { it.name.contains("female", ignoreCase = true) }
                        .thenBy { it.name }
                )
                ?: emptyList()
            // If no offline voice exists for the language, allow network voices too.
            val pool = candidates.ifEmpty {
                engine.voices
                    ?.filter { it.locale.language == lang }
                    ?.sortedByDescending { it.quality }
                    ?: emptyList()
            }
            _availableVoices.value = pool
            val best = pool.firstOrNull() ?: return
            if (engine.setVoice(best) == TextToSpeech.SUCCESS) {
                _currentVoiceName.value = best.name
                Log.i("SpeechManager", "Selected TTS voice: ${best.name} (quality=${best.quality})")
            }
        } catch (e: Exception) {
            Log.w("SpeechManager", "Voice selection failed; using engine default", e)
        }
    }

    /** Lets the user pick a specific installed voice (used by the AI settings section). */
    fun setVoiceByName(voiceName: String): Boolean {
        val engine = tts ?: return false
        if (!isInitialized) return false
        return try {
            val voice = _availableVoices.value.find { it.name == voiceName } ?: return false
            val ok = engine.setVoice(voice) == TextToSpeech.SUCCESS
            if (ok) _currentVoiceName.value = voice.name
            ok
        } catch (_: Exception) {
            false
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
                // Re-pick the smoothest voice for the new language.
                selectBestVoice()
            } catch (_: Exception) {
                tts?.language = Locale.US
            }
        }
    }

    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 2.0f)
        _speechRateFlow.value = speechRate
        tts?.setSpeechRate(speechRate)
    }

    fun setSpeechParameters(rate: Float, pitch: Float) {
        speechRate = rate.coerceIn(0.5f, 2.0f)
        speechPitch = pitch.coerceIn(0.6f, 1.5f)
        _speechRateFlow.value = speechRate
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
                            if (!recognized.isNullOrBlank()) {
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

    /**
     * Ends the current recognition session and lets the recognizer deliver its final results
     * through onResults/onError. This must NOT destroy the recognizer: destroying immediately
     * after stopListening() would swallow the final results and the transcript would be lost.
     */
    fun stopListening() {
        _isListening.value = false
        // SpeechRecognizer must be touched on the main thread; this can be called from
        // background coroutines, so post it to avoid racing startListening's creation block.
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {}
        }
    }

    /**
     * Cancels recognition outright (no final results) and releases the recognizer. Use for
     * shutdown, interruption, or starting a brand-new session — never to finalise one.
     */
    fun cancelListening() {
        _isListening.value = false
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (_: Exception) {}
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        cancelListening()
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
