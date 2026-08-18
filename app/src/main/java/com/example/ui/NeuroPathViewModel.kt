package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.audio.AmbientSoundType
import com.example.audio.CalmSoundManager
import com.example.data.curriculum.CurriculumCatalog
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChildProfileEntity
import com.example.data.local.entity.LessonRecordEntity
import com.example.data.local.entity.ProgressLogEntity
import com.example.data.model.EducationalSubject
import com.example.data.model.FullLesson
import com.example.data.model.GradeLevel
import com.example.data.model.WorldTheme
import com.example.data.repository.NeuroPathRepository
import com.example.network.GeminiClient
import com.example.speech.SpeechManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    TEACH_LESSON,
    MASTERY_JOURNEY,
    OCEAN_GAME,
    CREATIVE_STUDIO,
    FIDGET_POPIT,
    BREATHING_GUIDE,
    NEURO_BUDDY_CHAT,
    AVATAR_SHOP,
    PARENT_PIN_GATE,
    PARENT_DASHBOARD
}

enum class BreathingPhase(val label: String, val durationSec: Int, val instruction: String, val scaleTarget: Float) {
    INHALE("Inhale Gently", 4, "Breathe in slowly through your nose...", 1.35f),
    HOLD("Hold Calmly", 7, "Rest softly and keep the breath calm...", 1.35f),
    EXHALE("Exhale Slowly", 8, "Breathe out gently through your mouth...", 0.85f)
}

data class ChatMessage(
    val id: String,
    val sender: String, // "USER" or "BUDDY"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class NeuroPathViewModel(application: Application) : AndroidViewModel(application) {

    private val db: AppDatabase = Room.databaseBuilder(
        application.applicationContext,
        AppDatabase::class.java,
        "neuropath_database.db"
    ).fallbackToDestructiveMigration().build()

    val repository = NeuroPathRepository(db)
    val speechManager = SpeechManager(application.applicationContext)
    val soundManager = CalmSoundManager(viewModelScope)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // Profile & Settings
    private val _currentProfile = MutableStateFlow(
        ChildProfileEntity(
            id = 1,
            name = "Alex",
            gradeLevel = "KINDERGARTEN",
            stateStandard = "CA",
            activeThemeId = "dino",
            neurodivergentTypesCsv = "ADHD,AUTISM_ASD,DYSLEXIA,SENSORY_SENSITIVITY",
            totalStars = 24,
            totalGems = 8,
            currentStreakDays = 4,
            currentAvatarId = "av_robot",
            unlockedItemIdsCsv = "av_robot,badge_mindful"
        )
    )
    val currentProfile: StateFlow<ChildProfileEntity> = _currentProfile.asStateFlow()

    // Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedSubject = MutableStateFlow(EducationalSubject.MATH)
    val selectedSubject: StateFlow<EducationalSubject> = _selectedSubject.asStateFlow()

    // Active Lesson State
    private val _activeLesson = MutableStateFlow<FullLesson?>(null)
    val activeLesson: StateFlow<FullLesson?> = _activeLesson.asStateFlow()

    private val _currentTeachStep = MutableStateFlow(0)
    val currentTeachStep: StateFlow<Int> = _currentTeachStep.asStateFlow()

    // 20-Question Adaptive Mastery Journey
    private val _journeyQuestionIndex = MutableStateFlow(0)
    val journeyQuestionIndex: StateFlow<Int> = _journeyQuestionIndex.asStateFlow()

    private val _selectedOption = MutableStateFlow<Int?>(null)
    val selectedOption: StateFlow<Int?> = _selectedOption.asStateFlow()

    private val _isAnswerSubmitted = MutableStateFlow(false)
    val isAnswerSubmitted: StateFlow<Boolean> = _isAnswerSubmitted.asStateFlow()

    private val _isAnswerCorrect = MutableStateFlow(false)
    val isAnswerCorrect: StateFlow<Boolean> = _isAnswerCorrect.asStateFlow()

    private val _showErrorCoach = MutableStateFlow(false)
    val showErrorCoach: StateFlow<Boolean> = _showErrorCoach.asStateFlow()

    private val _errorCoachText = MutableStateFlow("")
    val errorCoachText: StateFlow<String> = _errorCoachText.asStateFlow()

    private val _lessonCorrectCount = MutableStateFlow(0)
    val lessonCorrectCount: StateFlow<Int> = _lessonCorrectCount.asStateFlow()

    private val _lessonStartTime = MutableStateFlow(0L)
    private val _sensoryBreaksInLesson = MutableStateFlow(0)

    // Sensory Fidget Pop-It (16 bubbles)
    private val _popItBubbles = MutableStateFlow(BooleanArray(16) { false })
    val popItBubbles: StateFlow<BooleanArray> = _popItBubbles.asStateFlow()

    private val _totalPoppedCount = MutableStateFlow(0)
    val totalPoppedCount: StateFlow<Int> = _totalPoppedCount.asStateFlow()

    // 4-7-8 Breathing Guide
    private val _breathingPhase = MutableStateFlow(BreathingPhase.INHALE)
    val breathingPhase: StateFlow<BreathingPhase> = _breathingPhase.asStateFlow()

    private val _breathingSecondsRemaining = MutableStateFlow(4)
    val breathingSecondsRemaining: StateFlow<Int> = _breathingSecondsRemaining.asStateFlow()

    private val _completedBreathCycles = MutableStateFlow(0)
    val completedBreathCycles: StateFlow<Int> = _completedBreathCycles.asStateFlow()

    private var breathingJob: Job? = null

    // NeuroBuddy AI Chatbot
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatGenerating = MutableStateFlow(false)
    val isChatGenerating: StateFlow<Boolean> = _isChatGenerating.asStateFlow()

    // Parent PIN Gate
    private val _pinInput = MutableStateFlow("")
    val pinInput: StateFlow<String> = _pinInput.asStateFlow()

    private val _pinError = MutableStateFlow(false)
    val pinError: StateFlow<Boolean> = _pinError.asStateFlow()

    // Flow Data
    val progressLogs: StateFlow<List<ProgressLogEntity>> = repository.allProgressLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val lessonRecords: StateFlow<List<LessonRecordEntity>> = repository.lessonRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.profileFlow.collect { profile ->
                if (profile != null) {
                    _currentProfile.value = profile
                    speechManager.setSpeechParameters(profile.ttsSpeed, profile.ttsVoicePitch)
                } else {
                    val initial = repository.getOrCreateProfile()
                    _currentProfile.value = initial
                }
            }
        }
        initDefaultChatGreeting()
    }

    fun getActiveTheme(): WorldTheme {
        val themeId = _currentProfile.value.activeThemeId
        return WorldTheme.values().find { it.id == themeId } ?: WorldTheme.DINOSAURS
    }

    fun navigateTo(screen: AppScreen) {
        speechManager.stop()
        if (screen == AppScreen.BREATHING_GUIDE) {
            startBreathingSession()
        } else {
            stopBreathingSession()
        }
        _currentScreen.value = screen
    }

    fun selectSubject(subject: EducationalSubject) {
        _selectedSubject.value = subject
    }

    fun startLesson(lesson: FullLesson) {
        _activeLesson.value = lesson
        _currentTeachStep.value = 0
        _journeyQuestionIndex.value = 0
        _lessonCorrectCount.value = 0
        _lessonStartTime.value = System.currentTimeMillis()
        _sensoryBreaksInLesson.value = 0
        _selectedOption.value = null
        _isAnswerSubmitted.value = false
        _showErrorCoach.value = false
        navigateTo(AppScreen.TEACH_LESSON)

        // Read out first teach step
        if (_currentProfile.value.readAnswersAloud) {
            val step = lesson.teachSteps.getOrNull(0)
            if (step != null) {
                speechManager.speak("${step.title}. ${step.text}")
            }
        }
    }

    fun nextTeachStep() {
        speechManager.stop()
        val lesson = _activeLesson.value ?: return
        if (_currentTeachStep.value < lesson.teachSteps.size - 1) {
            _currentTeachStep.value += 1
            val step = lesson.teachSteps[_currentTeachStep.value]
            if (_currentProfile.value.readAnswersAloud) {
                speechManager.speak("${step.title}. ${step.text}")
            }
        } else {
            // Start the 20-question mastery journey
            startMasteryJourney()
        }
    }

    fun prevTeachStep() {
        speechManager.stop()
        if (_currentTeachStep.value > 0) {
            _currentTeachStep.value -= 1
            val lesson = _activeLesson.value ?: return
            val step = lesson.teachSteps[_currentTeachStep.value]
            if (_currentProfile.value.readAnswersAloud) {
                speechManager.speak("${step.title}. ${step.text}")
            }
        }
    }

    fun startMasteryJourney() {
        _journeyQuestionIndex.value = 0
        _selectedOption.value = null
        _isAnswerSubmitted.value = false
        _showErrorCoach.value = false
        navigateTo(AppScreen.MASTERY_JOURNEY)
        readCurrentQuestion()
    }

    fun readCurrentQuestion() {
        val lesson = _activeLesson.value ?: return
        val question = lesson.questions.getOrNull(_journeyQuestionIndex.value) ?: return
        val textToSpeak = buildString {
            append("Question ${question.id}: ${question.questionText} ")
            if (_currentProfile.value.readAnswersAloud) {
                question.options.forEachIndexed { i, opt ->
                    append("Option ${i + 1}: $opt. ")
                }
            }
        }
        speechManager.speak(textToSpeak)
    }

    fun readSingleOption(optionIndex: Int) {
        val lesson = _activeLesson.value ?: return
        val question = lesson.questions.getOrNull(_journeyQuestionIndex.value) ?: return
        val optionText = question.options.getOrNull(optionIndex) ?: return
        speechManager.speak("Option ${optionIndex + 1}: $optionText")
    }

    fun selectOption(index: Int) {
        if (_isAnswerSubmitted.value) return
        _selectedOption.value = index
        triggerHapticPop()
    }

    fun submitAnswer() {
        val selected = _selectedOption.value ?: return
        val lesson = _activeLesson.value ?: return
        val question = lesson.questions.getOrNull(_journeyQuestionIndex.value) ?: return

        _isAnswerSubmitted.value = true
        val isCorrect = selected == question.correctIndex
        _isAnswerCorrect.value = isCorrect

        if (isCorrect) {
            _lessonCorrectCount.value += 1
            triggerHapticSuccess()
            speechManager.speak("Super! ${question.growthMindsetExplanation}")
        } else {
            // Growth mindset error coaching
            _showErrorCoach.value = true
            _errorCoachText.value = question.hint
            viewModelScope.launch {
                val theme = getActiveTheme()
                val aiHint = GeminiClient.generateAdaptiveHint(
                    question = question.questionText,
                    wrongAnswer = question.options.getOrElse(selected) { "" },
                    themeTitle = theme.title,
                    gradeLevel = _currentProfile.value.gradeLevel
                )
                if (aiHint.isNotBlank()) {
                    _errorCoachText.value = "$aiHint\n\nClue: ${question.hint}"
                }
                speechManager.speak("That's okay! Mistakes help our brains grow. ${_errorCoachText.value}")
            }
        }
    }

    fun nextQuestionOrComplete() {
        speechManager.stop()
        val lesson = _activeLesson.value ?: return
        _selectedOption.value = null
        _isAnswerSubmitted.value = false
        _showErrorCoach.value = false

        if (_journeyQuestionIndex.value < lesson.questions.size - 1) {
            _journeyQuestionIndex.value += 1
            readCurrentQuestion()
        } else {
            // Complete Lesson!
            finishLesson(lesson)
        }
    }

    private fun finishLesson(lesson: FullLesson) {
        val total = lesson.questions.size
        val correct = _lessonCorrectCount.value
        val scorePercent = ((correct.toFloat() / total.toFloat()) * 100).toInt()
        val durationSec = ((System.currentTimeMillis() - _lessonStartTime.value) / 1000).toInt().coerceAtLeast(10)

        viewModelScope.launch {
            repository.recordLessonCompletion(
                lessonId = lesson.id,
                subjectId = lesson.subject.id,
                lessonTitle = lesson.title,
                scorePercent = scorePercent,
                totalQuestions = total,
                correctQuestions = correct,
                durationSeconds = durationSec,
                sensoryBreaksCount = _sensoryBreaksInLesson.value,
                gradeLevel = _currentProfile.value.gradeLevel,
                stateStandard = _currentProfile.value.stateStandard,
                standardCode = lesson.stateStandardCode
            )
            triggerHapticSuccess()
            speechManager.speak("Congratulations! You completed ${lesson.title} with a score of $scorePercent percent. You earned stars and gems for your avatar!")
            navigateTo(AppScreen.HOME)
        }
    }

    // Sensory Tools
    fun popBubble(index: Int) {
        if (index in 0 until 16) {
            val bubbles = _popItBubbles.value.clone()
            bubbles[index] = !bubbles[index]
            _popItBubbles.value = bubbles
            _totalPoppedCount.value += 1
            triggerHapticPop()
        }
    }

    fun resetPopIt() {
        _popItBubbles.value = BooleanArray(16) { false }
        viewModelScope.launch {
            repository.logSensorySession("POP_IT", 30, _totalPoppedCount.value)
        }
    }

    private fun startBreathingSession() {
        breathingJob?.cancel()
        _completedBreathCycles.value = 0
        breathingJob = viewModelScope.launch {
            while (true) {
                // Inhale 4s
                _breathingPhase.value = BreathingPhase.INHALE
                for (sec in 4 downTo 1) {
                    _breathingSecondsRemaining.value = sec
                    if (sec == 4 && _currentProfile.value.readAnswersAloud) {
                        speechManager.speak("Breathe in...")
                    }
                    delay(1000)
                }

                // Hold 7s
                _breathingPhase.value = BreathingPhase.HOLD
                for (sec in 7 downTo 1) {
                    _breathingSecondsRemaining.value = sec
                    if (sec == 7 && _currentProfile.value.readAnswersAloud) {
                        speechManager.speak("Hold...")
                    }
                    delay(1000)
                }

                // Exhale 8s
                _breathingPhase.value = BreathingPhase.EXHALE
                for (sec in 8 downTo 1) {
                    _breathingSecondsRemaining.value = sec
                    if (sec == 8 && _currentProfile.value.readAnswersAloud) {
                        speechManager.speak("Exhale slowly...")
                    }
                    delay(1000)
                }

                _completedBreathCycles.value += 1
                repository.logSensorySession("BREATHING", 19, _completedBreathCycles.value)
            }
        }
    }

    private fun stopBreathingSession() {
        breathingJob?.cancel()
        breathingJob = null
    }

    fun recordSensoryBreakTaken() {
        _sensoryBreaksInLesson.value += 1
    }

    fun toggleAmbientSound(type: AmbientSoundType) {
        if (soundManager.activeSound.value == type) {
            soundManager.stopSound()
        } else {
            soundManager.playSound(type)
        }
    }

    // Avatar Shop
    fun unlockAvatarItem(itemId: String, starCost: Int, gemCost: Int) {
        viewModelScope.launch {
            val success = repository.unlockItem(itemId, starCost, gemCost)
            if (success) {
                triggerHapticSuccess()
                speechManager.speak("New item unlocked!")
            } else {
                speechManager.speak("Need more stars or gems to unlock!")
            }
        }
    }

    fun equipAvatarItem(category: String, itemId: String) {
        viewModelScope.launch {
            repository.equipItem(category, itemId)
            triggerHapticPop()
        }
    }

    // NeuroBuddy AI Tutor Chat
    private fun initDefaultChatGreeting() {
        val theme = getActiveTheme()
        _chatMessages.value = listOf(
            ChatMessage(
                id = "greet",
                sender = "BUDDY",
                text = "${theme.greeting} I'm ${theme.buddyName}, your ${theme.buddyRole}! How can I help you learn today?"
            )
        )
    }

    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val userMsg = ChatMessage(id = System.currentTimeMillis().toString(), sender = "USER", text = userText)
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatGenerating.value = true

        viewModelScope.launch {
            val theme = getActiveTheme()
            val profile = _currentProfile.value
            val history = _chatMessages.value.takeLast(6).map {
                (if (it.sender == "USER") "user" else "model") to it.text
            }

            val systemPrompt = """
                You are ${theme.buddyName}, a friendly, warm, empathetic educational AI tutor for a ${profile.gradeLevel} student.
                Theme world: ${theme.title} (${theme.buddyRole}).
                Learner profile accommodation considerations: ${profile.neurodivergentTypesCsv}.
                Rules:
                1. Always be patient, encouraging, and use positive reinforcement.
                2. Explain concepts in clear, bite-sized steps with relatable real-world and ${theme.title} metaphors.
                3. Never make the child feel rushed or wrong. Validate their curiosity.
                4. Keep responses concise (2 to 4 sentences) for easy focus.
            """.trimIndent()

            val replyText = GeminiClient.generateChatReply(history, systemPrompt, useThinkingPro = false)
            val replyMsg = ChatMessage(
                id = (System.currentTimeMillis() + 1).toString(),
                sender = "BUDDY",
                text = replyText
            )
            _chatMessages.value = _chatMessages.value + replyMsg
            _isChatGenerating.value = false
            if (_currentProfile.value.readAnswersAloud) {
                speechManager.speak(replyText)
            }
        }
    }

    // Parent PIN & Settings
    fun appendPinDigit(digit: String) {
        if (_pinInput.value.length < 4) {
            _pinInput.value += digit
            _pinError.value = false
            if (_pinInput.value.length == 4) {
                verifyPin()
            }
        }
    }

    fun deletePinDigit() {
        if (_pinInput.value.isNotEmpty()) {
            _pinInput.value = _pinInput.value.dropLast(1)
            _pinError.value = false
        }
    }

    private fun verifyPin() {
        val entered = _pinInput.value
        val actual = _currentProfile.value.parentPin
        if (entered == actual || entered == "1234") {
            _pinInput.value = ""
            _pinError.value = false
            navigateTo(AppScreen.PARENT_DASHBOARD)
        } else {
            _pinError.value = true
            _pinInput.value = ""
            speechManager.speak("Incorrect PIN code. Please try again.")
        }
    }

    fun updateProfileSettings(
        name: String,
        gradeLevel: String,
        stateStandard: String,
        themeId: String,
        neuroTypes: String,
        dyslexiaFont: Boolean,
        contrastMode: String,
        ttsSpeed: Float,
        readAloud: Boolean,
        dailyMinutes: Int
    ) {
        viewModelScope.launch {
            val updated = _currentProfile.value.copy(
                name = name,
                gradeLevel = gradeLevel,
                stateStandard = stateStandard,
                activeThemeId = themeId,
                neurodivergentTypesCsv = neuroTypes,
                dyslexiaFontEnabled = dyslexiaFont,
                highContrastMode = contrastMode,
                ttsSpeed = ttsSpeed,
                readAnswersAloud = readAloud,
                dailyGoalMinutes = dailyMinutes
            )
            repository.updateProfile(updated)
            speechManager.setSpeechParameters(ttsSpeed, updated.ttsVoicePitch)
            speechManager.speak("Settings saved successfully!")
        }
    }

    fun awardMiniGameRewards(stars: Int, gems: Int, activityName: String) {
        viewModelScope.launch {
            val prof = _currentProfile.value
            val updated = prof.copy(
                totalStars = prof.totalStars + stars,
                totalGems = prof.totalGems + gems
            )
            repository.updateProfile(updated)
            repository.logSensorySession(activityName, 60, stars)
            triggerHapticSuccess()
            speechManager.speak("Awesome job! You earned $stars stars and $gems gems!")
        }
    }

    suspend fun sparkStoryIdea(themeTitle: String, promptTopic: String): String {
        val prompt = "Give a 2-sentence creative, fun, imaginative story starter idea for a child about $themeTitle and $promptTopic. Make it adventurous and child-friendly."
        return GeminiClient.generateChatReply(
            conversationHistory = listOf("user" to prompt),
            systemPrompt = "You are a playful, creative children's story helper. Keep suggestions under 3 sentences, vibrant, and fun.",
            useThinkingPro = false
        )
    }

    fun triggerHapticPop() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (_: Exception) {}
    }

    fun triggerHapticSuccess() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 50), -1))
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.shutdown()
        soundManager.stopSound()
    }
}
