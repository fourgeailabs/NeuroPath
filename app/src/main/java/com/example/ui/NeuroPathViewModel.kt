package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.audio.AmbientSoundType
import com.example.audio.AudioRecorderHelper
import com.example.audio.CalmSoundManager
import com.example.audio.LyriaMusicPlayer
import com.example.data.curriculum.CurriculumCatalog
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChildProfileEntity
import com.example.data.local.entity.LessonRecordEntity
import com.example.data.local.entity.ProgressLogEntity
import com.example.data.model.AgeGroupTier
import com.example.data.model.AppLanguage
import com.example.data.model.EducationalLocaleManager
import com.example.data.model.EducationalSubject
import com.example.data.model.FullLesson
import com.example.data.model.GradeLevel
import com.example.data.model.LocaleLegalComplianceManager
import com.example.data.model.LocaleLegalNotice
import com.example.data.model.WorldTheme
import com.example.data.repository.NeuroPathRepository
import com.example.network.ChatModelMode
import com.example.network.GeminiClient
import com.example.speech.SpeechManager
import com.example.util.LocationComplianceHelper
import com.example.util.LocationComplianceResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    LANGUAGE_SELECTION,
    TERMS_AND_CONDITIONS,
    PARENT_PIN_SETUP,
    CHILD_PROFILE_SETUP,
    PROFILE_SELECTION,
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

    val activeApiKey: String
        get() {
            val userKey = _currentProfile.value.customApiKey.trim()
            if (userKey.isNotBlank() && userKey != "MY_GEMINI_API_KEY") return userKey
            val configKey = com.example.BuildConfig.GEMINI_API_KEY
            if (configKey.isNotBlank() && configKey != "MY_GEMINI_API_KEY") return configKey
            val envKey = System.getenv("GEMINI_API_KEY") ?: ""
            if (envKey.isNotBlank() && envKey != "MY_GEMINI_API_KEY") return envKey
            return ""
        }

    val hasValidApiKey: Boolean
        get() {
            val key = activeApiKey
            return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
        }

    private val db: AppDatabase = Room.databaseBuilder(
        application.applicationContext,
        AppDatabase::class.java,
        "neuropath_database.db"
    ).fallbackToDestructiveMigration().build()

    val repository = NeuroPathRepository(db)
    val speechManager = SpeechManager(application.applicationContext)
    val soundManager = CalmSoundManager(viewModelScope)
    val audioRecorder = AudioRecorderHelper(application.applicationContext)
    val lyriaMusicPlayer = LyriaMusicPlayer(application.applicationContext)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    val allProfiles: StateFlow<List<ChildProfileEntity>> = repository.allProfilesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active Profile & Settings
    private val _currentProfile = MutableStateFlow(
        ChildProfileEntity(
            id = 1,
            name = "",
            age = 6,
            gradeLevel = "KINDERGARTEN",
            ageGroupTier = "ELEMENTARY",
            stateStandard = "CA",
            country = "United States",
            stateOrProvince = "California",
            city = "Los Angeles",
            schoolDistrict = "Los Angeles Unified School District (LAUSD)",
            appLanguageCode = "en-US",
            activeThemeId = "dino",
            neurodivergentTypesCsv = "ADHD,AUTISM_ASD",
            strugglesCsv = "Focus & Staying On Task",
            strengthsCsv = "Visual Pattern Recognition",
            hyperFixationsCsv = "Dinosaurs & Prehistoric Safari",
            totalStars = 0,
            totalGems = 0,
            currentStreakDays = 0,
            currentAvatarId = "av_robot",
            unlockedItemIdsCsv = "av_robot",
            readAnswersAloud = false,
            isInitialSetupComplete = false
        )
    )
    val currentProfile: StateFlow<ChildProfileEntity> = _currentProfile.asStateFlow()

    // Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.LANGUAGE_SELECTION)
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

    // Learning Buddy AI Chatbot
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatGenerating = MutableStateFlow(false)
    val isChatGenerating: StateFlow<Boolean> = _isChatGenerating.asStateFlow()

    private val _chatModelMode = MutableStateFlow(ChatModelMode.GENERAL)
    val chatModelMode: StateFlow<ChatModelMode> = _chatModelMode.asStateFlow()

    // Voice Conversations (Live API - gemini-3.1-flash-live-preview)
    private val _isVoiceConversationMode = MutableStateFlow(false)
    val isVoiceConversationMode: StateFlow<Boolean> = _isVoiceConversationMode.asStateFlow()

    private val _liveVoiceStatus = MutableStateFlow("Tap the microphone or speak with your buddy!")
    val liveVoiceStatus: StateFlow<String> = _liveVoiceStatus.asStateFlow()

    private val _liveVoiceTranscript = MutableStateFlow<String?>(null)
    val liveVoiceTranscript: StateFlow<String?> = _liveVoiceTranscript.asStateFlow()

    private val _isLiveVoiceActive = MutableStateFlow(false)
    val isLiveVoiceActive: StateFlow<Boolean> = _isLiveVoiceActive.asStateFlow()

    // Audio Recording & Transcription (gemini-3.5-transcribe)
    private val _isRecordingAudio = MutableStateFlow(false)
    val isRecordingAudio: StateFlow<Boolean> = _isRecordingAudio.asStateFlow()

    private val _isTranscribingAudio = MutableStateFlow(false)
    val isTranscribingAudio: StateFlow<Boolean> = _isTranscribingAudio.asStateFlow()

    // Music Generation for Soundscape (lyria-3-clip-preview & lyria-3-pro-preview)
    private val _isGeneratingLyriaMusic = MutableStateFlow(false)
    val isGeneratingLyriaMusic: StateFlow<Boolean> = _isGeneratingLyriaMusic.asStateFlow()

    private val _lyriaGeneratedStatus = MutableStateFlow<String?>(null)
    val lyriaGeneratedStatus: StateFlow<String?> = _lyriaGeneratedStatus.asStateFlow()
    
    // Offline Curriculum Sync & Downloaded Standards
    private val _isDownloadingCurriculum = MutableStateFlow(false)
    val isDownloadingCurriculum: StateFlow<Boolean> = _isDownloadingCurriculum.asStateFlow()

    val latestCurriculum: StateFlow<com.example.data.local.entity.DownloadedCurriculumEntity?> = repository.latestCurriculumFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Location Compliance State
    private val _isVerifyingLocation = MutableStateFlow(false)
    val isVerifyingLocation: StateFlow<Boolean> = _isVerifyingLocation.asStateFlow()

    private val _locationComplianceResult = MutableStateFlow<LocationComplianceResult?>(null)
    val locationComplianceResult: StateFlow<LocationComplianceResult?> = _locationComplianceResult.asStateFlow()

    private val _dailyQuote = MutableStateFlow("You are capable of amazing things!")
    val dailyQuote: StateFlow<String> = _dailyQuote.asStateFlow()

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
            val profiles = repository.getAllProfilesDirect()
            if (profiles.isNotEmpty()) {
                val completed = profiles.firstOrNull { it.isInitialSetupComplete }
                if (completed != null) {
                    _currentProfile.value = completed
                    GeminiClient.customApiKeyOverride = completed.customApiKey
                    speechManager.setLanguage(completed.appLanguageCode)
                    speechManager.setSpeechParameters(completed.ttsSpeed, completed.ttsVoicePitch)
                    _currentScreen.value = AppScreen.PROFILE_SELECTION
                } else {
                    _currentProfile.value = profiles.first()
                    _currentScreen.value = AppScreen.LANGUAGE_SELECTION
                }
            } else {
                val initial = repository.getOrCreateProfile()
                _currentProfile.value = initial
                _currentScreen.value = AppScreen.LANGUAGE_SELECTION
            }
        }
        initDefaultChatGreeting()
        initiateOfflineCurriculumSync()
        fetchDailyQuote()
    }

    fun selectChildProfile(profile: ChildProfileEntity) {
        _currentProfile.value = profile
        GeminiClient.customApiKeyOverride = profile.customApiKey
        speechManager.setLanguage(profile.appLanguageCode)
        speechManager.setSpeechParameters(profile.ttsSpeed, profile.ttsVoicePitch)
        fetchDailyQuote()
        navigateTo(AppScreen.HOME)
    }

    fun saveNewChildProfile(profile: ChildProfileEntity) {
        viewModelScope.launch {
            val id = repository.insertProfile(profile)
            val inserted = profile.copy(id = if (id > 0) id else profile.id)
            _currentProfile.value = inserted
            GeminiClient.customApiKeyOverride = inserted.customApiKey
            speechManager.setLanguage(inserted.appLanguageCode)
            speechManager.setSpeechParameters(inserted.ttsSpeed, inserted.ttsVoicePitch)
        }
    }

    fun updateChildProfile(profile: ChildProfileEntity) {
        viewModelScope.launch {
            repository.updateProfile(profile)
            if (_currentProfile.value.id == profile.id) {
                _currentProfile.value = profile
                GeminiClient.customApiKeyOverride = profile.customApiKey
                speechManager.setLanguage(profile.appLanguageCode)
                speechManager.setSpeechParameters(profile.ttsSpeed, profile.ttsVoicePitch)
            }
        }
    }

    fun deleteChildProfile(profileId: Long) {
        viewModelScope.launch {
            repository.deleteProfile(profileId)
            val remaining = repository.getAllProfilesDirect()
            if (remaining.isNotEmpty()) {
                selectChildProfile(remaining.first())
            } else {
                val fresh = repository.getOrCreateProfile()
                _currentProfile.value = fresh
                navigateTo(AppScreen.CHILD_PROFILE_SETUP)
            }
        }
    }

    fun detectLocationCompliance(context: Context) {
        viewModelScope.launch {
            _isVerifyingLocation.value = true
            val result = LocationComplianceHelper.detectAndVerifyHomeCountry(context)
            _locationComplianceResult.value = result
            _isVerifyingLocation.value = false

            // Automatically sync profile locale and standards
            val educationalLocale = result.matchedEducationalLocale
            val prof = _currentProfile.value
            val country = educationalLocale?.countryName ?: result.detectedCountry
            val state = if (result.detectedState.isNotBlank()) result.detectedState else educationalLocale?.defaultStateOrProvince ?: "California"
            val city = if (result.detectedCity?.isNotBlank() == true) result.detectedCity else educationalLocale?.defaultCity ?: "Los Angeles"
            val district = if (result.detectedDistrict.isNotBlank()) result.detectedDistrict else educationalLocale?.schoolDistricts?.firstOrNull() ?: "Accredited District"
            val standard = educationalLocale?.stateCurriculumStandards?.firstOrNull() ?: "Standard"
            val langCode = if (prof.appLanguageCode.isBlank()) educationalLocale?.primaryLanguageCode ?: "en" else prof.appLanguageCode

            val updated = prof.copy(
                country = country,
                stateOrProvince = state,
                city = city,
                schoolDistrict = district,
                stateStandard = standard,
                appLanguageCode = langCode
            )
            repository.updateProfile(updated)
            _currentProfile.value = updated
        }
    }

    fun applyLocationCompliance(result: LocationComplianceResult) {
        viewModelScope.launch {
            _locationComplianceResult.value = result
            val educationalLocale = result.matchedEducationalLocale
            val prof = _currentProfile.value
            val country = educationalLocale?.countryName ?: result.detectedCountry
            val state = if (result.detectedState.isNotBlank()) result.detectedState else educationalLocale?.defaultStateOrProvince ?: "California"
            val city = if (result.detectedCity?.isNotBlank() == true) result.detectedCity else educationalLocale?.defaultCity ?: "Los Angeles"
            val district = if (result.detectedDistrict.isNotBlank()) result.detectedDistrict else educationalLocale?.schoolDistricts?.firstOrNull() ?: "Accredited District"
            val standard = educationalLocale?.stateCurriculumStandards?.firstOrNull() ?: "Standard"

            val updated = prof.copy(
                country = country,
                stateOrProvince = state,
                city = city,
                schoolDistrict = district,
                stateStandard = standard
            )
            repository.updateProfile(updated)
            _currentProfile.value = updated
        }
    }

    fun saveAndActivateChildProfile(profile: ChildProfileEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val finalProfile = profile.copy(isInitialSetupComplete = true)
            val id = if (finalProfile.id == 0L) {
                repository.insertProfile(finalProfile)
            } else {
                repository.updateProfile(finalProfile)
                finalProfile.id
            }
            val activated = finalProfile.copy(id = if (id > 0) id else finalProfile.id)
            _currentProfile.value = activated
            GeminiClient.customApiKeyOverride = activated.customApiKey.trim()
            speechManager.setLanguage(activated.appLanguageCode)
            speechManager.setSpeechParameters(activated.ttsSpeed, activated.ttsVoicePitch)

            // Trigger AI curriculum download and sync for the child's locale across all grades
            syncDailyCurriculumForLocale(forceRefresh = true)
            initDefaultChatGreeting()
            fetchDailyQuote()

            navigateTo(AppScreen.HOME)
            onComplete()
        }
    }

    fun resolvePostalOrZipCode(
        context: Context,
        postalCode: String,
        onResolved: ((LocationComplianceResult) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isVerifyingLocation.value = true
            val result = LocationComplianceHelper.resolvePostalOrZipCode(context, postalCode)
            _locationComplianceResult.value = result
            _isVerifyingLocation.value = false

            val educationalLocale = result.matchedEducationalLocale
            val prof = _currentProfile.value
            val country = educationalLocale?.countryName ?: result.detectedCountry
            val state = if (result.detectedState.isNotBlank()) result.detectedState else educationalLocale?.defaultStateOrProvince ?: "California"
            val city = if (result.detectedCity?.isNotBlank() == true) result.detectedCity else educationalLocale?.defaultCity ?: "Los Angeles"
            val district = if (result.detectedDistrict.isNotBlank()) result.detectedDistrict else educationalLocale?.schoolDistricts?.firstOrNull() ?: "Accredited District"
            val standard = educationalLocale?.stateCurriculumStandards?.firstOrNull() ?: "Standard"
            val langCode = if (prof.appLanguageCode.isBlank()) educationalLocale?.primaryLanguageCode ?: "en" else prof.appLanguageCode

            val updated = prof.copy(
                country = country,
                stateOrProvince = state,
                city = city,
                schoolDistrict = district,
                stateStandard = standard,
                appLanguageCode = langCode,
                zipOrPostalCodeOverride = postalCode.trim()
            )
            repository.updateProfile(updated)
            _currentProfile.value = updated
            syncDailyCurriculumForLocale(forceRefresh = true)
            onResolved?.invoke(result)
        }
    }

    fun syncDailyCurriculumForLocale(forceRefresh: Boolean = false) {
        if (_isDownloadingCurriculum.value) return
        viewModelScope.launch {
            _isDownloadingCurriculum.value = true
            try {
                val profile = _currentProfile.value
                val existing = repository.getLatestCurriculum()
                val oneDayMillis = 24 * 60 * 60 * 1000L
                val isExpired = existing == null || (System.currentTimeMillis() - existing.lastSyncTimestamp > oneDayMillis)

                if (forceRefresh || isExpired) {
                    val result = GeminiClient.downloadAllGradeCurriculumForLocale(
                        country = profile.country,
                        stateOrProvince = profile.stateOrProvince,
                        city = profile.city,
                        schoolDistrict = profile.schoolDistrict,
                        postalCode = profile.zipOrPostalCodeOverride,
                        standardTitle = profile.stateStandard,
                        languageCode = profile.appLanguageCode
                    )

                    val currId = "curriculum_${profile.country}_${profile.stateOrProvince}_${profile.schoolDistrict}".replace(" ", "_")
                    val entity = com.example.data.local.entity.DownloadedCurriculumEntity(
                        id = currId,
                        country = profile.country,
                        stateOrProvince = profile.stateOrProvince,
                        schoolDistrict = profile.schoolDistrict,
                        postalCode = profile.zipOrPostalCodeOverride,
                        standardTitle = profile.stateStandard,
                        officialSourceAgency = result.officialSourceAgency,
                        officialSourceUrl = result.officialSourceUrl,
                        gradesCoveredSummary = result.gradesSummary,
                        rawCurriculumJson = result.curriculumSummary,
                        lastSyncTimestamp = System.currentTimeMillis(),
                        lastSyncDateFormatted = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                        syncStatus = if (result.isOnlineSynced) "ONLINE_AI_VERIFIED" else "STANDARDS_COMPLIANT"
                    )
                    repository.saveDownloadedCurriculum(entity)
                }
            } catch (_: Exception) {
            } finally {
                _isDownloadingCurriculum.value = false
            }
        }
    }

    fun getLocaleLegalNotice(): LocaleLegalNotice {
        return LocaleLegalComplianceManager.getComplianceNotice(_currentProfile.value.country)
    }

    fun isForeignCurriculumRestricted(requestedCountry: String): Boolean {
        val homeCountry = _currentProfile.value.country
        return !homeCountry.equals(requestedCountry, ignoreCase = true)
    }

    fun initiateOfflineCurriculumSync() {
        if (_isDownloadingCurriculum.value) return
        _isDownloadingCurriculum.value = true
        viewModelScope.launch {
            delay(15000)
            _isDownloadingCurriculum.value = false
        }
    }

    private fun getSystemPromptForProfile(prof: ChildProfileEntity, roleContext: String = "tutor"): String {
        val tier = AgeGroupTier.values().find { it.id == prof.ageGroupTier } ?: AgeGroupTier.ELEMENTARY
        val grade = GradeLevel.values().find { it.code == prof.gradeLevel } ?: GradeLevel.GRADE_1

        return when (tier) {
            AgeGroupTier.HIGH_SCHOOL -> when (roleContext) {
                "story" -> "You are an inspiring creative writing mentor for high school scholars. Provide mature, engaging narrative hooks and concept starters under 3 sentences."
                "quote" -> "You are an empowering academic coach for high school students. Provide a mature, inspiring 1-sentence quote focused on focus, perseverance, and intellectual growth."
                else -> "You are an articulate, highly knowledgeable academic AI tutor and study partner for a High School student (${grade.displayName}). Provide mature, clear Socratic guidance, advanced concept breakdowns, and college/career-ready academic support while honoring neurodivergent accommodations."
            }
            AgeGroupTier.MIDDLE_SCHOOL -> when (roleContext) {
                "story" -> "You are an engaging creative writing mentor for middle school students. Provide exciting, structured story hooks under 3 sentences."
                "quote" -> "You are an encouraging study mentor for middle school students. Provide a motivating 1-sentence quote focused on curiosity and mastery."
                else -> "You are an engaging, supportive study mentor for a Middle School student (${grade.displayName}). Provide structured, interactive Socratic guidance and relatable real-world problem-solving."
            }
            AgeGroupTier.ELEMENTARY -> when (roleContext) {
                "story" -> "You are a warm, imaginative story helper for elementary learners. Keep suggestions under 3 sentences, vibrant, and fun."
                "quote" -> "You are a warm, supportive tutor for young learners. Give a friendly, encouraging 1-sentence quote."
                else -> "You are a warm, patient, supportive tutor for an elementary student (${grade.displayName}). Explain concepts with clear, bite-sized steps and encouraging metaphors."
            }
        }
    }

    fun fetchDailyQuote() {
        viewModelScope.launch {
            val theme = getActiveTheme()
            val prof = _currentProfile.value
            val prompt = "Give a 1-sentence encouraging, inspiring motivational quote for a student in ${AppLanguage.fromCode(prof.appLanguageCode).displayName}, using a ${theme.title} theme."
            val quote = GeminiClient.generateChatReply(
                conversationHistory = listOf("user" to prompt),
                systemPrompt = getSystemPromptForProfile(prof, roleContext = "quote"),
                languageCode = prof.appLanguageCode,
                schoolDistrict = prof.schoolDistrict,
                modelMode = ChatModelMode.FAST
            )
            if (quote.isNotBlank()) {
                _dailyQuote.value = quote
            }
        }
    }

    fun readDailyQuote() {
        speechManager.speak(_dailyQuote.value)
    }

    fun getActiveTheme(): WorldTheme {
        val themeId = _currentProfile.value.activeThemeId
        return WorldTheme.values().find { it.id == themeId } ?: WorldTheme.DINOSAURS
    }

    private val navigationBackStack = mutableListOf<AppScreen>()

    fun navigateTo(screen: AppScreen, addToBackStack: Boolean = true) {
        speechManager.stop()
        if (addToBackStack && _currentScreen.value != screen) {
            navigationBackStack.add(_currentScreen.value)
        }
        if (screen == AppScreen.BREATHING_GUIDE) {
            startBreathingSession()
        } else {
            stopBreathingSession()
        }
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        speechManager.stop()
        stopBreathingSession()
        if (navigationBackStack.isNotEmpty()) {
            val previous = navigationBackStack.removeAt(navigationBackStack.size - 1)
            _currentScreen.value = previous
            return true
        } else if (_currentScreen.value != AppScreen.HOME && _currentProfile.value.isInitialSetupComplete) {
            _currentScreen.value = AppScreen.HOME
            return true
        }
        return false
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

    fun getAiHelpForCurrentTeachStep() {
        viewModelScope.launch {
            val prof = _currentProfile.value
            val step = _activeLesson.value?.teachSteps?.getOrNull(_currentTeachStep.value) ?: return@launch
            speechManager.speak("Thinking of a good way to explain this...")
            val prompt = "Explain '${step.title}' clearly for a student in ${prof.schoolDistrict} (${prof.city}). Use accessible concepts and the ${getActiveTheme().title} theme. Keep to 2 short sentences."
            val explanation = GeminiClient.generateChatReply(
                conversationHistory = listOf("user" to prompt),
                systemPrompt = getSystemPromptForProfile(prof, roleContext = "tutor"),
                languageCode = prof.appLanguageCode,
                schoolDistrict = prof.schoolDistrict,
                modelMode = ChatModelMode.GENERAL
            )
            speechManager.speak(explanation)
        }
    }

    fun getAiHelpForCurrentQuestion() {
        viewModelScope.launch {
            val prof = _currentProfile.value
            val question = _activeLesson.value?.questions?.getOrNull(_journeyQuestionIndex.value) ?: return@launch
            speechManager.speak("Let's look at this together...")
            val prompt = "A student needs help with: '${question.questionText}'. Give a small hint without spoiling the answer. Use the ${getActiveTheme().title} theme. Keep to 2 short sentences."
            val explanation = GeminiClient.generateChatReply(
                conversationHistory = listOf("user" to prompt),
                systemPrompt = getSystemPromptForProfile(prof, roleContext = "tutor"),
                languageCode = prof.appLanguageCode,
                schoolDistrict = prof.schoolDistrict,
                modelMode = ChatModelMode.GENERAL
            )
            speechManager.speak(explanation)
        }
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
            _showErrorCoach.value = true
            _errorCoachText.value = question.hint
            viewModelScope.launch {
                val theme = getActiveTheme()
                val prof = _currentProfile.value
                val aiHint = GeminiClient.generateAdaptiveHint(
                    question = question.questionText,
                    wrongAnswer = question.options.getOrElse(selected) { "" },
                    themeTitle = theme.title,
                    gradeLevel = prof.gradeLevel,
                    languageCode = prof.appLanguageCode,
                    schoolDistrict = prof.schoolDistrict
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
                profileId = _currentProfile.value.id,
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
            repository.logSensorySession(_currentProfile.value.id, "POP_IT", 30, _totalPoppedCount.value)
        }
    }

    private fun startBreathingSession() {
        breathingJob?.cancel()
        _completedBreathCycles.value = 0
        breathingJob = viewModelScope.launch {
            while (true) {
                _breathingPhase.value = BreathingPhase.INHALE
                for (sec in 4 downTo 1) {
                    _breathingSecondsRemaining.value = sec
                    if (sec == 4 && _currentProfile.value.readAnswersAloud) {
                        speechManager.speak("Breathe in...")
                    }
                    delay(1000)
                }

                _breathingPhase.value = BreathingPhase.HOLD
                for (sec in 7 downTo 1) {
                    _breathingSecondsRemaining.value = sec
                    if (sec == 7 && _currentProfile.value.readAnswersAloud) {
                        speechManager.speak("Hold...")
                    }
                    delay(1000)
                }

                _breathingPhase.value = BreathingPhase.EXHALE
                for (sec in 8 downTo 1) {
                    _breathingSecondsRemaining.value = sec
                    if (sec == 8 && _currentProfile.value.readAnswersAloud) {
                        speechManager.speak("Exhale slowly...")
                    }
                    delay(1000)
                }

                _completedBreathCycles.value += 1
                repository.logSensorySession(_currentProfile.value.id, "BREATHING", 19, _completedBreathCycles.value)
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
            val success = repository.unlockItem(_currentProfile.value.id, itemId, starCost, gemCost)
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
            repository.equipItem(_currentProfile.value.id, category, itemId)
            triggerHapticPop()
        }
    }

    // Voice Assist Speech-To-Text
    fun startVoiceAssistForChat(onTextReceived: (String) -> Unit) {
        speechManager.startListening(
            onTextRecognized = { text ->
                onTextReceived(text)
            },
            onError = { error ->
                speechManager.speak("Voice assist: $error")
            }
        )
    }

    fun setChatModelMode(mode: ChatModelMode) {
        _chatModelMode.value = mode
    }

    fun startAudioRecording(): Boolean {
        val started = audioRecorder.startRecording()
        _isRecordingAudio.value = started
        if (started) {
            triggerHapticPop()
        }
        return started
    }

    fun stopAudioRecordingAndTranscribe(onTranscribed: (String) -> Unit) {
        viewModelScope.launch {
            _isRecordingAudio.value = false
            _isTranscribingAudio.value = true
            val wavBytes = audioRecorder.stopRecording()
            if (wavBytes != null && wavBytes.isNotEmpty()) {
                val profile = _currentProfile.value
                val transcript = GeminiClient.transcribeAudio(
                    audioBytes = wavBytes,
                    mimeType = "audio/wav",
                    languageCode = profile.appLanguageCode
                )
                _isTranscribingAudio.value = false
                if (transcript.isNotBlank()) {
                    triggerHapticSuccess()
                    onTranscribed(transcript)
                } else {
                    speechManager.speak("Could not catch that clearly. Please try again.")
                }
            } else {
                _isTranscribingAudio.value = false
            }
        }
    }

    fun toggleVoiceConversationMode(enabled: Boolean) {
        _isVoiceConversationMode.value = enabled
        if (!enabled) {
            speechManager.stop()
            _isLiveVoiceActive.value = false
            _liveVoiceTranscript.value = null
            _liveVoiceStatus.value = "Tap the microphone or speak with your buddy!"
        } else {
            val theme = getActiveTheme()
            val greeting = "Hi! I'm in voice conversation mode. What would you like to learn or explore together?"
            _liveVoiceStatus.value = "Listening for your voice..."
            _liveVoiceTranscript.value = greeting
            if (_currentProfile.value.readAnswersAloud) {
                speechManager.speak(greeting)
            }
        }
    }

    fun sendLiveVoiceTurn(userVoiceWav: ByteArray? = null, userText: String? = null) {
        viewModelScope.launch {
            _isLiveVoiceActive.value = true
            _liveVoiceStatus.value = "Buddy is thinking and responding..."
            val theme = getActiveTheme()
            val profile = _currentProfile.value
            val currContext = latestCurriculum.value?.curriculumSummary
                ?: "Official state academic standards for ${profile.gradeLevel} covering core learning requirements."

            val history = _chatMessages.value.takeLast(6).map {
                (if (it.sender == "USER") "user" else "model") to it.text
            }

            val basePrompt = getSystemPromptForProfile(profile, roleContext = "tutor")
            val systemPrompt = """
                $basePrompt
                Theme: ${theme.title} (${theme.buddyRole}).
                Accommodations: ${profile.neurodivergentTypesCsv}.
                District Standards Context: ${profile.schoolDistrict}, ${profile.stateOrProvince}, ${profile.country}.
            """.trimIndent()

            val result = GeminiClient.generateLiveVoiceConversationTurn(
                userVoiceAudio = userVoiceWav,
                userText = userText,
                conversationHistory = history,
                systemPrompt = systemPrompt,
                curriculumContext = currContext,
                schoolDistrict = profile.schoolDistrict,
                stateOrProvince = profile.stateOrProvince,
                country = profile.country,
                standardTitle = profile.stateStandard,
                languageCode = profile.appLanguageCode,
                customApiKey = activeApiKey
            )

            _isLiveVoiceActive.value = false
            _liveVoiceTranscript.value = result.transcriptText
            _liveVoiceStatus.value = "Tap microphone to speak again"

            // Add to chat history for continuity
            if (!userText.isNullOrBlank()) {
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    id = System.currentTimeMillis().toString(),
                    sender = "USER",
                    text = userText
                )
            }
            _chatMessages.value = _chatMessages.value + ChatMessage(
                id = (System.currentTimeMillis() + 1).toString(),
                sender = "BUDDY",
                text = result.transcriptText
            )

            // Play voice audio or TTS
            if (!result.audioBase64.isNullOrBlank()) {
                lyriaMusicPlayer.playAudioFromBase64(result.audioBase64, "Live Buddy Voice", loop = false)
            } else {
                speechManager.speak(result.transcriptText)
            }
        }
    }

    // Lyria Music Generation for Soundscapes (lyria-3-clip-preview & lyria-3-pro-preview)
    fun generateLyriaSoundscape(
        soundType: AmbientSoundType,
        isShortClip: Boolean = true,
        customPrompt: String? = null
    ) {
        viewModelScope.launch {
            _isGeneratingLyriaMusic.value = true
            _lyriaGeneratedStatus.value = "Synthesizing with Lyria AI..."
            soundManager.stopSound()

            val effectivePrompt = customPrompt ?: when (soundType) {
                AmbientSoundType.RAIN -> "Gentle rainfall on a cozy window with distant soft lo-fi piano chords."
                AmbientSoundType.OCEAN -> "Calming ocean surf rhythm with 432Hz theta waves for anxiety decompression."
                AmbientSoundType.FOREST -> "Tranquil forest woodland morning with soft rustling leaves and distant gentle stream."
                AmbientSoundType.WHITE_NOISE -> "Deep gentle brown noise with warm acoustic low-frequency drone for ADHD deep focus."
                AmbientSoundType.CHIMES -> "Zen calming chimes and harmonic sound bowls for deep sensory tranquility."
                AmbientSoundType.OFF -> "Calm ambient soothing music."
            }

            val result = GeminiClient.generateSoundscapeMusic(
                prompt = effectivePrompt,
                isShortClip = isShortClip,
                title = if (customPrompt != null) "Custom Sensory Music" else soundType.title
            )

            _isGeneratingLyriaMusic.value = false
            if (result.isSuccess && !result.audioBase64.isNullOrBlank()) {
                _lyriaGeneratedStatus.value = "Playing: ${result.trackTitle} (${result.durationLabel})"
                lyriaMusicPlayer.playAudioFromBase64(result.audioBase64, result.trackTitle, loop = true)
                triggerHapticSuccess()
            } else {
                _lyriaGeneratedStatus.value = "Synthesized Procedural Ambient Soundscape (${soundType.title})"
                soundManager.playSound(soundType)
            }
        }
    }

    fun stopLyriaMusic() {
        lyriaMusicPlayer.stop()
        _lyriaGeneratedStatus.value = null
    }

    fun toggleLyriaPlayPause() {
        lyriaMusicPlayer.togglePlayPause()
    }

    // Learning Buddy AI Tutor Chat
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
            try {
                val theme = getActiveTheme()
                val profile = _currentProfile.value
                val currSummary = latestCurriculum.value?.curriculumSummary
                    ?: "Accredited grade-level curriculum benchmarks for ${profile.gradeLevel} in ${profile.schoolDistrict}."
                
                val replyText = if (hasValidApiKey) {
                    val history = _chatMessages.value.takeLast(6).map {
                        (if (it.sender == "USER") "user" else "model") to it.text
                    }
        
                    val basePrompt = getSystemPromptForProfile(profile, roleContext = "tutor")
                    val isHighSchool = profile.ageGroupTier == AgeGroupTier.HIGH_SCHOOL.id
                    val socraticDirective = if (isHighSchool) {
                        "1. NEVER give direct answers to homework or questions. Guide the student Socratically with probing questions, concept breakdowns, and scaffolded analytical steps."
                    } else {
                        "1. NEVER give direct answers to homework or questions. Guide the student Socratically with clues, questions, and scaffolded steps."
                    }

                    val systemPrompt = """
                        $basePrompt
                        Theme world: ${theme.title} (${theme.buddyRole}).
                        District requirements context: ${profile.schoolDistrict} in ${profile.city}, ${profile.stateOrProvince}, ${profile.country}.
                        Learner profile accommodation considerations: ${profile.neurodivergentTypesCsv}.
                        Pedagogical Guidance Mandate:
                        $socraticDirective
                        2. Ground and align all explanations in the active curriculum standard document: [Curriculum Source: ${profile.stateStandard} Standard Framework / ${profile.schoolDistrict}].
                        3. Always be patient, encouraging, and use positive reinforcement.
                        4. Explain concepts in clear, structured steps with relatable real-world and ${theme.title} metaphors.
                    """.trimIndent()
        
                    GeminiClient.generateChatReply(
                        conversationHistory = history,
                        systemPrompt = systemPrompt,
                        languageCode = profile.appLanguageCode,
                        schoolDistrict = profile.schoolDistrict,
                        stateOrProvince = profile.stateOrProvince,
                        country = profile.country,
                        standardTitle = profile.stateStandard,
                        curriculumContext = currSummary,
                        modelMode = _chatModelMode.value,
                        customApiKey = activeApiKey
                    )
                } else {
                    delay(600)
                    GeminiClient.generateLocalSocraticReply(
                        lastUserMessage = userText,
                        schoolDistrict = profile.schoolDistrict,
                        stateOrProvince = profile.stateOrProvince,
                        country = profile.country,
                        standardTitle = profile.stateStandard,
                        languageCode = profile.appLanguageCode
                    )
                }

                val replyMsg = ChatMessage(
                    id = (System.currentTimeMillis() + 1).toString(),
                    sender = "BUDDY",
                    text = replyText
                )
                _chatMessages.value = _chatMessages.value + replyMsg
                if (_currentProfile.value.readAnswersAloud) {
                    speechManager.speak(replyText)
                }
            } catch (e: Exception) {
                Log.e("NeuroPathViewModel", "Error in sendChatMessage", e)
                val fallbackReply = GeminiClient.generateLocalSocraticReply(
                    lastUserMessage = userText,
                    schoolDistrict = _currentProfile.value.schoolDistrict,
                    stateOrProvince = _currentProfile.value.stateOrProvince,
                    country = _currentProfile.value.country,
                    standardTitle = _currentProfile.value.stateStandard,
                    languageCode = _currentProfile.value.appLanguageCode
                )
                val replyMsg = ChatMessage(
                    id = (System.currentTimeMillis() + 1).toString(),
                    sender = "BUDDY",
                    text = fallbackReply
                )
                _chatMessages.value = _chatMessages.value + replyMsg
            } finally {
                _isChatGenerating.value = false
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
        val validPin = if (actual.isNotBlank()) actual else allProfiles.value.firstOrNull { it.parentPin.isNotBlank() }?.parentPin ?: "1234"
        if (entered == validPin) {
            _pinInput.value = ""
            _pinError.value = false
            navigateTo(AppScreen.PARENT_DASHBOARD)
        } else {
            _pinError.value = true
            _pinInput.value = ""
            speechManager.speak("Incorrect passcode. Please try again.")
        }
    }

    fun acceptTermsAndConditions() {
        val current = _currentProfile.value
        val updated = current.copy(isCoppaConsented = true)
        viewModelScope.launch {
            repository.updateProfile(updated)
            _currentProfile.value = updated
        }
    }

    fun updateParentPin(newPin: String) {
        viewModelScope.launch {
            repository.updateParentPinForAll(newPin)
            val current = _currentProfile.value
            val updated = current.copy(parentPin = newPin)
            _currentProfile.value = updated
        }
    }

    fun updateAiSetup(platform: String, key: String) {
        val current = _currentProfile.value
        val updated = current.copy(customAiPlatform = platform, customApiKey = key)
        viewModelScope.launch {
            repository.updateProfile(updated)
            _currentProfile.value = updated
        }
    }

    fun completeInitialSetup() {
        val current = _currentProfile.value
        val updated = current.copy(isInitialSetupComplete = true)
        viewModelScope.launch {
            repository.updateProfile(updated)
            _currentProfile.value = updated
            navigateTo(AppScreen.PROFILE_SELECTION)
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
        val current = _currentProfile.value
        updateProfileSettingsWithLocale(
            name = name,
            gradeLevel = gradeLevel,
            stateStandard = stateStandard,
            country = current.country,
            stateOrProvince = current.stateOrProvince,
            city = current.city,
            schoolDistrict = current.schoolDistrict,
            appLanguageCode = current.appLanguageCode,
            themeId = themeId,
            neuroTypes = neuroTypes,
            dyslexiaFont = dyslexiaFont,
            contrastMode = contrastMode,
            ttsSpeed = ttsSpeed,
            readAloud = readAloud,
            dailyMinutes = dailyMinutes
        )
    }

    fun updateProfileSettingsWithLocale(
        name: String,
        gradeLevel: String,
        stateStandard: String,
        country: String,
        stateOrProvince: String,
        city: String,
        schoolDistrict: String,
        appLanguageCode: String,
        themeId: String,
        neuroTypes: String,
        dyslexiaFont: Boolean,
        contrastMode: String,
        ttsSpeed: Float,
        readAloud: Boolean,
        dailyMinutes: Int,
        customAiPlatform: String = _currentProfile.value.customAiPlatform,
        customApiKey: String = _currentProfile.value.customApiKey
    ) {
        viewModelScope.launch {
            val updated = _currentProfile.value.copy(
                name = name,
                gradeLevel = gradeLevel,
                stateStandard = stateStandard,
                country = country,
                stateOrProvince = stateOrProvince,
                city = city,
                schoolDistrict = schoolDistrict,
                appLanguageCode = appLanguageCode,
                activeThemeId = themeId,
                neurodivergentTypesCsv = neuroTypes,
                dyslexiaFontEnabled = dyslexiaFont,
                highContrastMode = contrastMode,
                ttsSpeed = ttsSpeed,
                readAnswersAloud = readAloud,
                dailyGoalMinutes = dailyMinutes,
                customAiPlatform = customAiPlatform,
                customApiKey = customApiKey
            )
            GeminiClient.customApiKeyOverride = customApiKey.trim()
            repository.updateProfile(updated)
            _currentProfile.value = updated
            speechManager.setLanguage(appLanguageCode)
            speechManager.setSpeechParameters(ttsSpeed, updated.ttsVoicePitch)
            speechManager.speak("Settings saved successfully!")
            fetchDailyQuote()
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
            repository.logSensorySession(prof.id, activityName, 60, stars)
            triggerHapticSuccess()
            speechManager.speak("Awesome job! You earned $stars stars and $gems gems!")
        }
    }

    suspend fun sparkStoryIdea(themeTitle: String, promptTopic: String): String {
        val prof = _currentProfile.value
        val prompt = "Give a 2-sentence creative story starter idea about $themeTitle and $promptTopic."
        return GeminiClient.generateChatReply(
            conversationHistory = listOf("user" to prompt),
            systemPrompt = getSystemPromptForProfile(prof, roleContext = "story"),
            languageCode = prof.appLanguageCode,
            schoolDistrict = prof.schoolDistrict,
            modelMode = ChatModelMode.FAST
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
