package com.fourgeailabs.neuropath.ui

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
import com.fourgeailabs.neuropath.audio.AmbientSoundType
import com.fourgeailabs.neuropath.audio.CalmSoundManager
import com.fourgeailabs.neuropath.data.curriculum.CurriculumCatalog
import com.fourgeailabs.neuropath.data.curriculum.oer.OerCommonsCurriculumItem
import com.fourgeailabs.neuropath.data.curriculum.oer.OerSyncResult
import com.fourgeailabs.neuropath.data.curriculum.oer.OerTutorCurriculumContext
import com.fourgeailabs.neuropath.data.local.AppDatabase
import com.fourgeailabs.neuropath.data.local.ChatSessionSummary
import com.fourgeailabs.neuropath.data.local.entity.ChatMessageEntity
import com.fourgeailabs.neuropath.data.local.entity.ChildProfileEntity
import com.fourgeailabs.neuropath.data.local.entity.LessonRecordEntity
import com.fourgeailabs.neuropath.data.local.entity.OerCurriculumEntity
import com.fourgeailabs.neuropath.data.local.entity.ProgressLogEntity
import com.fourgeailabs.neuropath.data.model.AgeGroupTier
import com.fourgeailabs.neuropath.data.model.AppLanguage
import com.fourgeailabs.neuropath.data.model.EducationalExplanationMode
import com.fourgeailabs.neuropath.data.model.EducationalLocaleManager
import com.fourgeailabs.neuropath.data.model.EducationalSubject
import com.fourgeailabs.neuropath.data.model.EducationalSubjectTag
import com.fourgeailabs.neuropath.data.model.FullLesson
import com.fourgeailabs.neuropath.data.model.GradeLevel
import com.fourgeailabs.neuropath.data.model.LocaleLegalComplianceManager
import com.fourgeailabs.neuropath.data.model.LocaleLegalNotice
import com.fourgeailabs.neuropath.data.model.NeuroThemeCatalog
import com.fourgeailabs.neuropath.data.model.NeuroThemeData
import com.fourgeailabs.neuropath.data.model.ThemeRotationSchedule
import com.fourgeailabs.neuropath.data.model.WorldTheme
import com.fourgeailabs.neuropath.data.repository.NeuroPathRepository
import com.fourgeailabs.neuropath.network.ChatModelMode
import com.fourgeailabs.neuropath.network.LlamaClient
import com.fourgeailabs.neuropath.security.SecureStorage
import com.fourgeailabs.neuropath.network.LlamaLocalManager
import com.fourgeailabs.neuropath.speech.SpeechManager
import com.fourgeailabs.neuropath.ui.components.BreathingVisualMode
import com.fourgeailabs.neuropath.util.LocationComplianceHelper
import com.fourgeailabs.neuropath.util.LocationComplianceResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID


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
    val id: String = UUID.randomUUID().toString(),
    val dbId: Long = 0L,
    val sender: String, // "USER" or "BUDDY"
    val text: String,
    val explanationMode: EducationalExplanationMode = EducationalExplanationMode.STEP_BY_STEP,
    val modelMode: ChatModelMode = ChatModelMode.GENERAL,
    val isFreeModel: Boolean = true,
    val subjectTag: EducationalSubjectTag = EducationalSubjectTag.ALL,
    val isBookmarked: Boolean = false,
    val suggestedFollowUps: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)


class NeuroPathViewModel(application: Application) : AndroidViewModel(application) {

    val activeApiKey: String
        get() = LlamaClient.getApiKey(LlamaClient.customApiKeyOverride)

    val hasValidApiKey: Boolean
        get() = LlamaClient.hasValidApiKey(LlamaClient.customApiKeyOverride)

    private val db: AppDatabase = Room.databaseBuilder(
        application.applicationContext,
        AppDatabase::class.java,
        "neuropath_database.db"
    // NOTE: no fallbackToDestructiveMigration() here, deliberately. A missing migration must
    // fail loudly (IllegalStateException) rather than silently wiping a child's learning data.
    // Add an explicit Migration to AppDatabase.MIGRATIONS for any future schema change.
    ).addMigrations(*AppDatabase.MIGRATIONS.toTypedArray())
     .build()

    val repository = NeuroPathRepository(db)
    private val secureStorage = SecureStorage.getInstance(application)
    val speechManager = SpeechManager(application.applicationContext)
    val soundManager = CalmSoundManager(viewModelScope)

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

    // Active Profile & Settings — starts blank; the real profile is loaded from the
    // database on launch and the setup flow fills in location/diagnoses/interests.
    private val _currentProfile = MutableStateFlow(
        ChildProfileEntity()
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

    private val _isBreathingPaused = MutableStateFlow(false)
    val isBreathingPaused: StateFlow<Boolean> = _isBreathingPaused.asStateFlow()

    private val _breathingVisualMode = MutableStateFlow(BreathingVisualMode.BLOSSOM)
    val breathingVisualMode: StateFlow<BreathingVisualMode> = _breathingVisualMode.asStateFlow()

    private var breathingJob: Job? = null

    // Learning Buddy AI Chatbot & Educational Explanations
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatGenerating = MutableStateFlow(false)
    val isChatGenerating: StateFlow<Boolean> = _isChatGenerating.asStateFlow()

    private val _chatModelMode = MutableStateFlow(ChatModelMode.GENERAL)
    val chatModelMode: StateFlow<ChatModelMode> = _chatModelMode.asStateFlow()

    private val _explanationMode = MutableStateFlow(EducationalExplanationMode.STEP_BY_STEP)
    val explanationMode: StateFlow<EducationalExplanationMode> = _explanationMode.asStateFlow()

    private val _selectedSubjectTag = MutableStateFlow(EducationalSubjectTag.ALL)
    val selectedSubjectTag: StateFlow<EducationalSubjectTag> = _selectedSubjectTag.asStateFlow()

    private val _currentSessionId = MutableStateFlow("session_${System.currentTimeMillis()}")
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    private val _currentSessionTitle = MutableStateFlow("Personalized Study Session")
    val currentSessionTitle: StateFlow<String> = _currentSessionTitle.asStateFlow()

    private val _searchQueryChat = MutableStateFlow("")
    val searchQueryChat: StateFlow<String> = _searchQueryChat.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val chatSessionSummaries: StateFlow<List<ChatSessionSummary>> = _currentProfile.flatMapLatest { profile ->
        repository.getChatSessionSummariesFlow(profile.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val bookmarkedChatMessages: StateFlow<List<ChatMessageEntity>> = _currentProfile.flatMapLatest { profile ->
        repository.getBookmarkedChatMessagesFlow(profile.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResultsChatMessages: StateFlow<List<ChatMessageEntity>> = _searchQueryChat.flatMapLatest { query ->
        if (query.isBlank()) {
            flowOf(emptyList())
        } else {
            repository.searchChatMessagesFlow(_currentProfile.value.id, query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // Voice Conversations (Live Llama 3.2 3B Conversational Engine)
    private val _isVoiceConversationMode = MutableStateFlow(false)
    val isVoiceConversationMode: StateFlow<Boolean> = _isVoiceConversationMode.asStateFlow()

    private val _liveVoiceStatus = MutableStateFlow("Tap the microphone or speak with your buddy!")
    val liveVoiceStatus: StateFlow<String> = _liveVoiceStatus.asStateFlow()

    private val _liveVoiceTranscript = MutableStateFlow<String?>(null)
    val liveVoiceTranscript: StateFlow<String?> = _liveVoiceTranscript.asStateFlow()

    private val _isLiveVoiceActive = MutableStateFlow(false)
    val isLiveVoiceActive: StateFlow<Boolean> = _isLiveVoiceActive.asStateFlow()

    // Audio Recording & Transcription (Llama 3.2 Speech Audio)
    private val _isRecordingAudio = MutableStateFlow(false)
    val isRecordingAudio: StateFlow<Boolean> = _isRecordingAudio.asStateFlow()

    private val _isTranscribingAudio = MutableStateFlow(false)
    val isTranscribingAudio: StateFlow<Boolean> = _isTranscribingAudio.asStateFlow()

    // Offline Curriculum Sync & Downloaded Standards
    private val _isDownloadingCurriculum = MutableStateFlow(false)
    val isDownloadingCurriculum: StateFlow<Boolean> = _isDownloadingCurriculum.asStateFlow()

    val latestCurriculum: StateFlow<com.fourgeailabs.neuropath.data.local.entity.DownloadedCurriculumEntity?> = repository.latestCurriculumFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // OER Commons Curated Collections Service & Pre-Installed Catalog
    val oerCurriculumUnits: StateFlow<List<OerCurriculumEntity>> = repository.oerCurriculumUnitsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isOerSyncing = MutableStateFlow(false)
    val isOerSyncing: StateFlow<Boolean> = _isOerSyncing.asStateFlow()

    private val _oerSyncResult = MutableStateFlow<OerSyncResult?>(null)
    val oerSyncResult: StateFlow<OerSyncResult?> = _oerSyncResult.asStateFlow()

    private val _oerSearchResults = MutableStateFlow<List<OerCommonsCurriculumItem>>(emptyList())
    val oerSearchResults: StateFlow<List<OerCommonsCurriculumItem>> = _oerSearchResults.asStateFlow()

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
            runCatching {
                val profiles = repository.getAllProfilesDirect()
                if (profiles.isNotEmpty()) {
                    val completed = profiles.firstOrNull { it.isInitialSetupComplete }
                    if (completed != null) {
                        val rotated = checkAndApplyThemeRotation(completed)
                        _currentProfile.value = rotated
                        initializeSecureCredentials(profiles)
                        runCatching { speechManager.setLanguage(rotated.appLanguageCode) }
                        runCatching { speechManager.setSpeechParameters(rotated.ttsSpeed, rotated.ttsVoicePitch) }
                        _currentScreen.value = AppScreen.PROFILE_SELECTION
                    } else {
                        val first = checkAndApplyThemeRotation(profiles.first())
                        _currentProfile.value = first
                        _currentScreen.value = AppScreen.LANGUAGE_SELECTION
                    }
                } else {
                    val initial = repository.getOrCreateProfile()
                    _currentProfile.value = initial
                    _currentScreen.value = AppScreen.LANGUAGE_SELECTION
                }
            }.onFailure { e ->
                Log.e("NeuroPathViewModel", "Error loading initial profiles", e)
            }
        }
        runCatching { initDefaultChatGreeting() }
        runCatching { initiateOfflineCurriculumSync() }
        runCatching { fetchDailyQuote() }
        viewModelScope.launch {
            runCatching {
                repository.initializeOerCurriculum()
            }.onFailure { e ->
                Log.e("NeuroPathViewModel", "Error initializing OER curriculum", e)
            }
        }
    }

    /**
     * Loads the app-level HF token from encrypted storage into the Llama client, and performs
     * a one-time migration of any legacy per-profile API keys out of the Room database.
     * Must be called from a coroutine (does DB writes on migration).
     */
    private suspend fun initializeSecureCredentials(profiles: List<ChildProfileEntity>) {
        var token = secureStorage.getHfToken()
        if (token.isBlank()) {
            val legacy = profiles.firstOrNull { it.customApiKey.isNotBlank() }?.customApiKey.orEmpty()
            if (legacy.isNotBlank()) {
                // Only wipe the legacy copies once the token is safely in encrypted storage;
                // if encryption is unavailable the token stays where it was (functional, as before).
                if (secureStorage.setHfToken(legacy)) {
                    runCatching { repository.clearAllCustomApiKeys() }
                    Log.d("NeuroPathViewModel", "Migrated legacy per-profile API key into encrypted storage.")
                } else {
                    Log.w("NeuroPathViewModel", "Encrypted storage unavailable; legacy API key left in database")
                }
                token = legacy
            }
        }
        LlamaClient.customApiKeyOverride = token
    }

    private fun checkAndUpdateStreak(profile: ChildProfileEntity): ChildProfileEntity {
        val today = java.time.LocalDate.now().toString()
        if (profile.lastActiveDate == today) return profile
        val yesterday = java.time.LocalDate.now().minusDays(1).toString()
        // Consecutive day: extend. First launch or a 2+ day gap: the streak restarts at 1.
        // (A gap must never increment — that was awarding streaks for not showing up.)
        val newStreak = when {
            profile.lastActiveDate == yesterday -> profile.currentStreakDays + 1
            else -> 1
        }
        val updated = profile.copy(currentStreakDays = if (newStreak < 1) 1 else newStreak, lastActiveDate = today)
        viewModelScope.launch {
            repository.updateStreak(profile.id, updated.currentStreakDays, today)
        }
        return updated
    }

    fun updateParentAiConfig(disabled: Boolean, versionMode: String, localInstalled: Boolean) {
        viewModelScope.launch {
            val prof = _currentProfile.value
            val updated = prof.copy(
                learningBuddyDisabled = disabled,
                aiVersionMode = versionMode,
                localLlamaInstalled = localInstalled
            )
            repository.updateProfile(updated)
            _currentProfile.value = updated
            speechManager.speak("AI and Learning Buddy configuration updated.")
        }
    }

    fun selectChildProfile(profile: ChildProfileEntity) {
        val updatedStreak = checkAndUpdateStreak(profile)
        val rotated = checkAndApplyThemeRotation(updatedStreak)
        // Chat state belongs to the previous profile: stop its collector and reset the thread
        // so the new profile never sees another child's messages.
        chatSessionCollectJob?.cancel()
        chatSessionCollectJob = null
        _currentProfile.value = rotated
        initDefaultChatGreeting()
        speechManager.setLanguage(rotated.appLanguageCode)
        speechManager.setSpeechParameters(rotated.ttsSpeed, rotated.ttsVoicePitch)
        fetchDailyQuote()
        navigateTo(AppScreen.HOME)
    }

    fun saveNewChildProfile(profile: ChildProfileEntity) {
        viewModelScope.launch {
            val id = repository.insertProfile(profile)
            val inserted = profile.copy(id = if (id > 0) id else profile.id)
            _currentProfile.value = inserted
            speechManager.setLanguage(inserted.appLanguageCode)
            speechManager.setSpeechParameters(inserted.ttsSpeed, inserted.ttsVoicePitch)
        }
    }

    fun updateChildProfile(profile: ChildProfileEntity) {
        viewModelScope.launch {
            repository.updateProfile(profile)
            if (_currentProfile.value.id == profile.id) {
                _currentProfile.value = profile
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
                    val result = LlamaClient.downloadAllGradeCurriculumForLocale(
                        country = profile.country,
                        stateOrProvince = profile.stateOrProvince,
                        city = profile.city,
                        schoolDistrict = profile.schoolDistrict,
                        postalCode = profile.zipOrPostalCodeOverride,
                        standardTitle = profile.stateStandard,
                        languageCode = profile.appLanguageCode
                    )

                    val currId = "curriculum_${profile.country}_${profile.stateOrProvince}_${profile.schoolDistrict}".replace(" ", "_")
                    val entity = com.fourgeailabs.neuropath.data.local.entity.DownloadedCurriculumEntity(
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
            } catch (e: Throwable) {
                Log.e("NeuroPathViewModel", "Failed to sync curriculum for locale", e)
                runCatching { speechManager.speak("Curriculum sync failed. Please check your connection and try again.") }
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
        syncDailyCurriculumForLocale(forceRefresh = false)
    }

    fun verifyOerCurriculumLibrary() {
        if (_isOerSyncing.value) return
        viewModelScope.launch {
            _isOerSyncing.value = true
            try {
                val result = repository.verifyOerCurriculumLibrary()
                _oerSyncResult.value = result
                runCatching { speechManager.speak("Curriculum library verified.") }
            } catch (e: Throwable) {
                Log.e("NeuroPathViewModel", "Failed to verify curriculum library", e)
                _oerSyncResult.value = OerSyncResult(
                    isSuccess = false,
                    sourceTitle = "Built-in Curriculum Library",
                    totalUnitsCount = oerCurriculumUnits.value.size,
                    message = "Verification failed: ${e.message}. Using the pre-installed collection."
                )
                runCatching { speechManager.speak("Library verification failed. Using pre-installed curriculum.") }
            } finally {
                _isOerSyncing.value = false
            }
        }
    }

    fun searchOerCommonsUnits(
        query: String,
        subject: EducationalSubject? = null,
        gradeLevel: GradeLevel? = null
    ) {
        viewModelScope.launch {
            val results = repository.searchOerCurriculum(query, subject, gradeLevel)
            _oerSearchResults.value = results
        }
    }

    private fun getSystemPromptForProfile(prof: ChildProfileEntity, roleContext: String = "tutor"): String {
        val tier = AgeGroupTier.entries.find { it.id == prof.ageGroupTier } ?: AgeGroupTier.ELEMENTARY
        val grade = GradeLevel.entries.find { it.code == prof.gradeLevel } ?: GradeLevel.GRADE_1

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
            try {
                val theme = getActiveTheme()
                val prof = _currentProfile.value
                val prompt = "Give a 1-sentence encouraging, inspiring motivational quote with author citation (e.g. \"Quote...\" - Author) for a student in ${AppLanguage.fromCode(prof.appLanguageCode).displayName}, using a ${theme.title} theme."
                val quote = LlamaClient.generateChatReply(
                    conversationHistory = listOf("user" to prompt),
                    systemPrompt = getSystemPromptForProfile(prof, roleContext = "quote"),
                    languageCode = prof.appLanguageCode,
                    schoolDistrict = prof.schoolDistrict,
                    modelMode = ChatModelMode.FAST
                )
                if (quote.isNotBlank()) {
                    _dailyQuote.value = quote
                }
            } catch (e: Throwable) {
                Log.e("NeuroPathViewModel", "Failed to fetch daily quote", e)
            }
        }
    }

    fun refreshDailyQuote() {
        fetchDailyQuote()
    }

    fun readDailyQuote() {
        speechManager.speak(_dailyQuote.value)
    }

    fun getActiveNeuroTheme(): NeuroThemeData {
        val themeId = _currentProfile.value.activeThemeId
        return NeuroThemeCatalog.findThemeById(themeId)
    }

    fun getActiveTheme(): WorldTheme {
        val themeId = _currentProfile.value.activeThemeId
        return WorldTheme.entries.find { it.id == themeId } ?: WorldTheme.DINOSAURS
    }

    fun checkAndApplyThemeRotation(profile: ChildProfileEntity): ChildProfileEntity {
        val schedule = ThemeRotationSchedule.fromId(profile.themeRotationSchedule)
        if (schedule == ThemeRotationSchedule.MANUAL || schedule.intervalDays <= 0) {
            return profile
        }

        val now = System.currentTimeMillis()
        val intervalMillis = schedule.intervalDays * 24L * 60L * 60L * 1000L
        if (now - profile.lastThemeRotationTimestamp >= intervalMillis) {
            val nextTheme = NeuroThemeCatalog.getNextRotatedTheme(profile)
            val updated = profile.copy(
                activeThemeId = nextTheme.id,
                lastThemeRotationTimestamp = now
            )
            updateChildProfile(updated)
            return updated
        }
        return profile
    }

    fun setThemeRotationSchedule(schedule: ThemeRotationSchedule) {
        val prof = _currentProfile.value
        val updated = prof.copy(
            themeRotationSchedule = schedule.id,
            lastThemeRotationTimestamp = System.currentTimeMillis()
        )
        updateChildProfile(updated)
    }

    fun setActiveNeuroTheme(themeId: String) {
        val prof = _currentProfile.value
        val updated = prof.copy(
            activeThemeId = themeId,
            lastThemeRotationTimestamp = System.currentTimeMillis()
        )
        updateChildProfile(updated)
    }

    private val navigationBackStack = mutableListOf<AppScreen>()

    private val NAV_BACK_STACK_KEY = "nav_back_stack"

    private fun saveBackStack() {
        val app = getApplication<Application>()
        val prefs = app.getSharedPreferences("neuropath_nav", Context.MODE_PRIVATE)
        prefs.edit().putString(NAV_BACK_STACK_KEY, navigationBackStack.joinToString(",")).apply()
    }

    private fun loadBackStack() {
        val app = getApplication<Application>()
        val prefs = app.getSharedPreferences("neuropath_nav", Context.MODE_PRIVATE)
        val saved = prefs.getString(NAV_BACK_STACK_KEY, "")
        if (!saved.isNullOrBlank()) {
            navigationBackStack.clear()
            navigationBackStack.addAll(
                saved.split(",")
                    .mapNotNull { name -> runCatching { AppScreen.valueOf(name.trim()) }.getOrNull() }
            )
        }
    }

    init {
        loadBackStack()
    }

    fun navigateTo(screen: AppScreen, addToBackStack: Boolean = true) {
        speechManager.stop()
        if (addToBackStack && _currentScreen.value != screen) {
            navigationBackStack.add(_currentScreen.value)
            saveBackStack()
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
            saveBackStack()
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
            val explanation = LlamaClient.generateChatReply(
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
            val explanation = LlamaClient.generateChatReply(
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
        com.fourgeailabs.neuropath.learning.LearnerPersonalizationEngine.recordAnswer(getApplication(), _currentProfile.value.id, _selectedSubject.value.name, isCorrect, question.questionText.take(100))

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
                val aiHint = LlamaClient.generateAdaptiveHint(
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
        // Guard: a question-less lesson has no meaningful percentage.
        val scorePercent = if (total > 0) ((correct.toFloat() / total.toFloat()) * 100).toInt() else 0
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
            val completionMessage = if (total > 0) {
                "Congratulations! You completed ${lesson.title} with a score of $scorePercent percent. You earned stars and gems for your avatar!"
            } else {
                "Congratulations! You completed ${lesson.title}! You earned stars and gems for your avatar!"
            }
            speechManager.speak(completionMessage)
            navigateTo(AppScreen.HOME)
        }
    }

    // Sensory Tools
    fun popBubble(index: Int) {
        if (index in 0 until 16) {
            val bubbles = _popItBubbles.value.clone()
            val wasPopped = bubbles[index]
            bubbles[index] = !wasPopped
            _popItBubbles.value = bubbles
            // Count only actual pops (un-popped -> popped), not un-pops.
            if (!wasPopped) {
                _totalPoppedCount.value += 1
            }
            triggerHapticPop()
        }
    }

    fun resetPopIt() {
        _popItBubbles.value = BooleanArray(16) { false }
        viewModelScope.launch {
            repository.logSensorySession(_currentProfile.value.id, "POP_IT", 30, _totalPoppedCount.value)
        }
    }

    fun setBreathingVisualMode(mode: BreathingVisualMode) {
        _breathingVisualMode.value = mode
    }

    fun toggleBreathingPlayPause() {
        _isBreathingPaused.value = !_isBreathingPaused.value
    }

    fun resetBreathingSession() {
        _completedBreathCycles.value = 0
        startBreathingSession()
    }

    fun speakBreathingCue(text: String) {
        speechManager.speak(text)
    }

    private fun startBreathingSession() {
        breathingJob?.cancel()
        _isBreathingPaused.value = false
        breathingJob = viewModelScope.launch {
            while (true) {
                // Phase 1: INHALE (4 seconds)
                _breathingPhase.value = BreathingPhase.INHALE
                if (_currentProfile.value.readAnswersAloud) {
                    speechManager.speak("Breathe in slowly...")
                }
                for (sec in 4 downTo 1) {
                    _breathingSecondsRemaining.value = sec
                    while (_isBreathingPaused.value) {
                        delay(200)
                    }
                    delay(1000)
                }

                // Phase 2: HOLD (7 seconds)
                _breathingPhase.value = BreathingPhase.HOLD
                if (_currentProfile.value.readAnswersAloud) {
                    speechManager.speak("Hold calmly...")
                }
                for (sec in 7 downTo 1) {
                    _breathingSecondsRemaining.value = sec
                    while (_isBreathingPaused.value) {
                        delay(200)
                    }
                    delay(1000)
                }

                // Phase 3: EXHALE (8 seconds)
                _breathingPhase.value = BreathingPhase.EXHALE
                if (_currentProfile.value.readAnswersAloud) {
                    speechManager.speak("Exhale slowly...")
                }
                for (sec in 8 downTo 1) {
                    _breathingSecondsRemaining.value = sec
                    while (_isBreathingPaused.value) {
                        delay(200)
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
        _isBreathingPaused.value = false
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

    // Avatar Shop & Rewards
    fun addPoints(points: Int) {
        viewModelScope.launch {
            repository.awardStarsAndGems(_currentProfile.value.id, stars = points, gems = (points / 5).coerceAtLeast(1))
            val updated = repository.getProfileDirect(_currentProfile.value.id)
            if (updated != null) {
                _currentProfile.value = updated
            }
            triggerHapticSuccess()
        }
    }

    fun awardStarsAndGems(stars: Int, gems: Int) {
        viewModelScope.launch {
            repository.awardStarsAndGems(_currentProfile.value.id, stars, gems)
            val updated = repository.getProfileDirect(_currentProfile.value.id)
            if (updated != null) {
                _currentProfile.value = updated
            }
            triggerHapticSuccess()
        }
    }

    fun unlockAvatarItem(itemId: String, starCost: Int, gemCost: Int) {
        viewModelScope.launch {
            val owned = _currentProfile.value.unlockedItemIdsCsv
                .split(",").any { it.trim() == itemId }
            if (owned) {
                speechManager.speak("You already own that item!")
                return@launch
            }
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

    // Holds the callback waiting for the SpeechManager recognition result for the
    // current voice input. Transcription always comes from Android's SpeechRecognizer
    // (there is no offline WAV-to-text engine in this build).
    private var pendingVoiceTranscription: ((String) -> Unit)? = null

    fun startAudioRecording(): Boolean {
        _isRecordingAudio.value = true
        triggerHapticPop()
        speechManager.startListening(
            onTextRecognized = { text ->
                val callback = pendingVoiceTranscription
                pendingVoiceTranscription = null
                _isTranscribingAudio.value = false
                if (text.isNotBlank()) {
                    triggerHapticSuccess()
                    callback?.invoke(text)
                } else {
                    speechManager.speak("Could not catch that clearly. Please try again.")
                }
            },
            onError = {
                pendingVoiceTranscription = null
                _isTranscribingAudio.value = false
                speechManager.speak("Could not catch that clearly. Please try again.")
            }
        )
        return true
    }

    fun stopAudioRecordingAndTranscribe(onTranscribed: (String) -> Unit) {
        pendingVoiceTranscription = onTranscribed
        _isRecordingAudio.value = false
        _isTranscribingAudio.value = true
        // Finalize the SpeechRecognizer session; its onResults/onError callback delivers
        // the transcript (or the failure) to the code above.
        speechManager.stopListening()
        // Safety net: if the recognizer never calls back, don't leave the spinner running.
        viewModelScope.launch {
            delay(2000)
            if (pendingVoiceTranscription != null) {
                pendingVoiceTranscription = null
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

    fun sendLiveVoiceTurn(rawText: String? = null) {
        viewModelScope.launch {
            _isLiveVoiceActive.value = true
            _liveVoiceStatus.value = "Buddy is thinking and responding..."
            val theme = getActiveTheme()
            val profile = _currentProfile.value
            // Sanitize recognized speech the same way as typed input.
            val userText = rawText?.let { sanitizeUserPrompt(it) }
            val studentGrade = GradeLevel.entries.find { it.code == profile.gradeLevel } ?: GradeLevel.GRADE_1
            val oerTutorContext = repository.retrieveOerTutorContext(
                query = userText ?: "Live voice learning inquiry",
                studentGrade = studentGrade,
                studentSubject = _selectedSubject.value,
                schoolDistrict = profile.schoolDistrict,
                country = profile.country,
                stateOrProvince = profile.stateOrProvince
            )
            val currContext = oerTutorContext.formattedContextPrompt.ifBlank {
                latestCurriculum.value?.curriculumSummary
                    ?: "Official state academic standards for ${profile.gradeLevel} covering core learning requirements."
            }

            val history = _chatMessages.value.takeLast(6).map {
                (if (it.sender == "USER") "user" else "model") to it.text
            }

            val personalizationProfile = com.fourgeailabs.neuropath.learning.LearnerPersonalizationEngine.buildPrompt(getApplication(), profile, _selectedSubjectTag.value.id, false)
            val basePrompt = getSystemPromptForProfile(profile, roleContext = "tutor")
            val systemPrompt = """
                $basePrompt
                Theme: ${theme.title} (${theme.buddyRole}).
                $personalizationProfile
                District Standards Context: ${profile.schoolDistrict}, ${profile.stateOrProvince}, ${profile.country}.
            """.trimIndent()

            val result = LlamaClient.generateLiveVoiceConversationTurn(
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
                    id = UUID.randomUUID().toString(),
                    sender = "USER",
                    text = userText
                )
            }
            _chatMessages.value = _chatMessages.value + ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = "BUDDY",
                text = result.transcriptText
            )

            // Speak the buddy's reply aloud (live voice is text + on-device TTS).
            speechManager.speak(result.transcriptText)
        }
    }

    // Learning Buddy AI Tutor Chat & Educational Explanations
    private fun initDefaultChatGreeting() {
        val theme = getActiveTheme()
        _chatMessages.value = listOf(
            ChatMessage(
                id = "greet",
                sender = "BUDDY",
                text = "${theme.greeting} I'm ${theme.buddyName}, your ${theme.buddyRole}! I can explain anything in your lessons with step-by-step guidance, fun analogies, or direct answers. What are we exploring today?",
                explanationMode = _explanationMode.value,
                modelMode = _chatModelMode.value,
                isFreeModel = _chatModelMode.value.isFreeTier,
                subjectTag = _selectedSubjectTag.value,
                suggestedFollowUps = listOf(
                    "Explain today's lesson",
                    "Give me a fun math puzzle",
                    "Tell me a fascinating science fact"
                )
            )
        )
    }

    fun setExplanationMode(mode: EducationalExplanationMode) {
        _explanationMode.value = mode
    }

    fun setSelectedSubjectTag(tag: EducationalSubjectTag) {
        _selectedSubjectTag.value = tag
    }

    fun startNewChatSession(title: String) {
        val newSessionId = "session_${System.currentTimeMillis()}"
        _currentSessionId.value = newSessionId
        _currentSessionTitle.value = title.ifBlank { "Personalized Study Session" }
        initDefaultChatGreeting()
    }

    private var chatSessionCollectJob: Job? = null

    fun loadChatSession(sessionId: String, sessionTitle: String) {
        _currentSessionId.value = sessionId
        _currentSessionTitle.value = sessionTitle
        // Cancel any previous collector: without this, every session switch leaks a Flow
        // collector that keeps overwriting _chatMessages with a stale session's data.
        chatSessionCollectJob?.cancel()
        chatSessionCollectJob = viewModelScope.launch {
            repository.getChatMessagesForSessionFlow(_currentProfile.value.id, sessionId).collect { entities ->
                if (entities.isNotEmpty()) {
                    _chatMessages.value = entities.map { entity ->
                        ChatMessage(
                            id = entity.id.toString(),
                            dbId = entity.id,
                            sender = entity.sender,
                            text = entity.text,
                            explanationMode = EducationalExplanationMode.fromId(entity.explanationMode),
                            modelMode = ChatModelMode.entries.find { it.modelName == entity.modelUsed } ?: ChatModelMode.GENERAL,
                            isFreeModel = entity.isFreeModel,
                            subjectTag = EducationalSubjectTag.fromId(entity.subjectTag),
                            isBookmarked = entity.isBookmarked,
                            timestamp = entity.timestamp
                        )
                    }
                }
            }
        }
    }

    fun deleteChatSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteChatSession(_currentProfile.value.id, sessionId)
            if (_currentSessionId.value == sessionId) {
                startNewChatSession("New Study Topic")
            }
        }
    }

    fun clearAllChatHistory() {
        viewModelScope.launch {
            repository.clearAllChatHistory(_currentProfile.value.id)
            startNewChatSession("Personalized Study Session")
        }
    }

    fun toggleMessageBookmark(message: ChatMessage) {
        viewModelScope.launch {
            val newStatus = !message.isBookmarked
            _chatMessages.value = _chatMessages.value.map {
                if (it.id == message.id) it.copy(isBookmarked = newStatus) else it
            }
            if (message.dbId > 0) {
                repository.toggleChatBookmark(message.dbId, newStatus)
            }
        }
    }

    fun searchChatMessages(query: String) {
        _searchQueryChat.value = query
    }

    fun requestSimplerExplanation(baseText: String) {
        _explanationMode.value = EducationalExplanationMode.SIMPLER_ANALOGY
        val snippet = baseText.take(120).replace("\n", " ")
        sendChatMessage("Can you explain that simpler with a fun, intuitive analogy? \"$snippet...\"")
    }

    fun requestStepByStepExplanation(baseText: String) {
        _explanationMode.value = EducationalExplanationMode.STEP_BY_STEP
        val snippet = baseText.take(120).replace("\n", " ")
        sendChatMessage("Can you break that down into clear numbered step-by-step instructions? \"$snippet...\"")
    }

    /**
     * Sanitizes free-text user input before it enters prompts, conversation history, or the
     * database: trims, strips control characters (which can smuggle prompt-injection tricks and
     * break rendering), collapses runaway whitespace, and caps length so a giant paste cannot
     * blow out the model's context window.
     */
    private fun sanitizeUserPrompt(raw: String): String {
        var s = raw.trim().replace(CONTROL_CHARS_REGEX, "")
        s = s.replace(RUNAWAY_SPACES_REGEX, "  ").replace(RUNAWAY_NEWLINES_REGEX, "\n\n\n")
        if (s.length > MAX_USER_PROMPT_CHARS) {
            s = s.take(MAX_USER_PROMPT_CHARS).trimEnd() + "…"
        }
        return s
    }

    fun sendChatMessage(rawText: String) {
        // Sanitize once: everything downstream (prompts, history, DB, follow-ups) uses this.
        val userText = sanitizeUserPrompt(rawText)
        if (userText.isBlank() || _isChatGenerating.value) return
        val currentMode = _explanationMode.value
        val currentModel = _chatModelMode.value
        val currentSubject = _selectedSubjectTag.value
        val profile = _currentProfile.value
        val personalizationProfile = com.fourgeailabs.neuropath.learning.LearnerPersonalizationEngine.buildPrompt(getApplication(), profile, currentSubject.id, currentModel == ChatModelMode.LLAMA_LOCAL)
        com.fourgeailabs.neuropath.learning.LearnerPersonalizationEngine.recordPreferredStyle(getApplication(), profile.id, currentMode.id)
        val activeSession = _currentSessionId.value
        val sessionTitle = _currentSessionTitle.value

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = "USER",
            text = userText,
            explanationMode = currentMode,
            modelMode = currentModel,
            isFreeModel = currentModel.isFreeTier,
            subjectTag = currentSubject
        )
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatGenerating.value = true

        viewModelScope.launch {
            // Save user message to Room DB
            try {
                repository.saveChatMessage(
                    ChatMessageEntity(
                        profileId = profile.id,
                        sessionId = activeSession,
                        sessionTitle = sessionTitle,
                        sender = "USER",
                        text = userText,
                        explanationMode = currentMode.id,
                        modelUsed = currentModel.modelName,
                        isFreeModel = currentModel.isFreeTier,
                        subjectTag = currentSubject.id,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                Log.w("NeuroPathViewModel", "Error saving user message to DB: ${e.message}")
            }

            try {
                val theme = getActiveTheme()
                val studentGrade = GradeLevel.entries.find { it.code == profile.gradeLevel } ?: GradeLevel.GRADE_1
                val oerTutorContext = repository.retrieveOerTutorContext(
                    query = userText,
                    studentGrade = studentGrade,
                    studentSubject = _selectedSubject.value,
                    schoolDistrict = profile.schoolDistrict,
                    country = profile.country,
                    stateOrProvince = profile.stateOrProvince
                )
                val currSummary = oerTutorContext.formattedContextPrompt.ifBlank {
                    latestCurriculum.value?.curriculumSummary
                        ?: "Accredited grade-level curriculum benchmarks for ${profile.gradeLevel} in ${profile.schoolDistrict}."
                }

                val history = _chatMessages.value.map {
                    (if (it.sender == "USER") "user" else "model") to it.text
                }.takeLast(10)

                val replyText = if (currentModel == ChatModelMode.LLAMA_LOCAL) {
                    val basePrompt = getSystemPromptForProfile(profile, roleContext = "tutor")
                    val systemPrompt = """
                        $basePrompt
                        $personalizationProfile
                        Theme world: ${theme.title} (${theme.buddyRole}).
                        Active Subject Focus: ${currentSubject.title} (${currentSubject.id}).
                        Personalized Explanation Style: ${currentMode.title}
                        Style Directive: ${currentMode.promptDirective}
                        District context: ${profile.schoolDistrict} in ${profile.city}, ${profile.stateOrProvince}, ${profile.country}.
                        Curriculum Framework: ${profile.stateStandard} (${profile.schoolDistrict}).

                        Local Tutoring Rules:
                        1. Adapt to the learner profile and observed mastery above; never treat a diagnosis as an identity label.
                        2. Prefer the learner's recorded explanation style, but change strategy when it is not working.
                        3. Use strengths and interests to make difficult ideas concrete.
                        4. Keep accessibility and accommodation signals in mind without diagnosing or inferring conditions.
                        5. Ground teaching in the supplied curriculum context when relevant.
                        6. Preserve learner agency and use a Socratic approach rather than doing assessed work for the learner.
                        Theme world: ${theme.title} (${theme.buddyRole}).
                        Active Subject Focus: ${currentSubject.title} (${currentSubject.id}).
                        District context: ${profile.schoolDistrict} in ${profile.city}, ${profile.stateOrProvince}, ${profile.country}.
                    """.trimIndent()
                    LlamaLocalManager.generateLlamaResponse(
                        context = getApplication(),
                        prompt = userText,
                        systemPrompt = systemPrompt,
                        schoolDistrict = profile.schoolDistrict,
                        stateOrProvince = profile.stateOrProvince,
                        country = profile.country,
                        standardTitle = profile.stateStandard,
                        languageCode = profile.appLanguageCode,
                        conversationHistory = history,
                        curriculumContext = currSummary,
                        hasValidApiKey = hasValidApiKey,
                        activeApiKey = activeApiKey
                    )
                } else if (currentModel != ChatModelMode.OFFLINE) {

                    val basePrompt = getSystemPromptForProfile(profile, roleContext = "tutor")
                    val systemPrompt = """
                        $basePrompt
                        $personalizationProfile
                        Theme world: ${theme.title} (${theme.buddyRole}).
                        Active Subject Focus: ${currentSubject.title} (${currentSubject.id}).
                        Personalized Explanation Style: ${currentMode.title}
                        Style Directive: ${currentMode.promptDirective}
                        District requirements context: ${profile.schoolDistrict} in ${profile.city}, ${profile.stateOrProvince}, ${profile.country}.
                        Learner accommodation and accessibility signals: use the privacy-safe personalization profile above; do not request or infer diagnosis labels.
                        Curriculum Framework: ${profile.stateStandard} (${profile.schoolDistrict}).
                        
                        Core Tutoring Rules:
                        1. Prioritize ${currentMode.title}: ${currentMode.promptDirective}
                        2. Connect ideas to the learner's interest world (${theme.title}) and real-world examples.
                        3. Ground explanations in accredited benchmarks (${profile.stateStandard}), but do NOT append citation tags or footnotes.
                        4. Provide encouraging, positive reinforcement.
                    """.trimIndent()

                    LlamaClient.generateChatReply(
                        conversationHistory = history,
                        systemPrompt = systemPrompt,
                        languageCode = profile.appLanguageCode,
                        schoolDistrict = profile.schoolDistrict,
                        stateOrProvince = profile.stateOrProvince,
                        country = profile.country,
                        standardTitle = profile.stateStandard,
                        curriculumContext = currSummary,
                        modelMode = currentModel,
                        customApiKey = activeApiKey
                    )
                } else {
                    delay(500)
                    LlamaClient.generateLocalSocraticReply(
                        lastUserMessage = userText,
                        schoolDistrict = profile.schoolDistrict,
                        stateOrProvince = profile.stateOrProvince,
                        country = profile.country,
                        standardTitle = profile.stateStandard,
                        languageCode = profile.appLanguageCode,
                        conversationHistory = _chatMessages.value.map { it.sender to it.text },
                        curriculumContext = currSummary
                    )
                }

                // Generate smart follow-up question chips based on topic
                val followUps = generateSuggestedFollowUps(userText, currentSubject)

                val replyMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = "BUDDY",
                    text = replyText,
                    explanationMode = currentMode,
                    modelMode = currentModel,
                    isFreeModel = currentModel.isFreeTier,
                    subjectTag = currentSubject,
                    suggestedFollowUps = followUps
                )
                _chatMessages.value = _chatMessages.value + replyMsg

                // Persist Buddy reply to Room DB
                val savedId = repository.saveChatMessage(
                    ChatMessageEntity(
                        profileId = profile.id,
                        sessionId = activeSession,
                        sessionTitle = sessionTitle,
                        sender = "BUDDY",
                        text = replyText,
                        explanationMode = currentMode.id,
                        modelUsed = currentModel.modelName,
                        isFreeModel = currentModel.isFreeTier,
                        subjectTag = currentSubject.id,
                        timestamp = System.currentTimeMillis()
                    )
                )
                _chatMessages.value = _chatMessages.value.map {
                    if (it.id == replyMsg.id) it.copy(dbId = savedId) else it
                }

                if (_currentProfile.value.readAnswersAloud) {
                    speechManager.speak(replyText)
                }
            } catch (e: Exception) {
                Log.e("NeuroPathViewModel", "Error in sendChatMessage", e)
                val fallbackReply = LlamaClient.generateLocalSocraticReply(
                    lastUserMessage = userText,
                    schoolDistrict = _currentProfile.value.schoolDistrict,
                    stateOrProvince = _currentProfile.value.stateOrProvince,
                    country = _currentProfile.value.country,
                    standardTitle = _currentProfile.value.stateStandard,
                    languageCode = _currentProfile.value.appLanguageCode,
                    conversationHistory = _chatMessages.value.map { it.sender to it.text }
                )
                val replyMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = "BUDDY",
                    text = fallbackReply,
                    explanationMode = currentMode,
                    modelMode = currentModel,
                    isFreeModel = currentModel.isFreeTier,
                    subjectTag = currentSubject,
                    suggestedFollowUps = listOf("Can you show another example?", "Why does this work?", "Quiz me!")
                )
                _chatMessages.value = _chatMessages.value + replyMsg
            } finally {
                _isChatGenerating.value = false
            }
        }
    }

    private fun generateSuggestedFollowUps(query: String, subject: EducationalSubjectTag): List<String> {
        val qLower = query.lowercase()
        return when {
            qLower.contains("fraction") || qLower.contains("math") || subject == EducationalSubjectTag.MATH -> listOf(
                "Show another practice problem",
                "Why do we need a common denominator?",
                "Give me a real-world math example"
            )
            qLower.contains("plant") || qLower.contains("science") || subject == EducationalSubjectTag.SCIENCE -> listOf(
                "What happens at night?",
                "Can you give me a fun quiz question?",
                "How does this connect to animals?"
            )
            qLower.contains("read") || qLower.contains("word") || subject == EducationalSubjectTag.READING -> listOf(
                "Give me a practice sentence",
                "What is an antonym for this?",
                "How do I use this in an essay?"
            )
            qLower.contains("code") || qLower.contains("program") || subject == EducationalSubjectTag.CODING -> listOf(
                "Show me a simple code snippet",
                "What kind of bug could happen here?",
                "How does this work in video games?"
            )
            else -> listOf(
                "Explain this with another example",
                "Why is this important?",
                "Ask me a check-in question!"
            )
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
        _pinInput.value = ""
        viewModelScope.launch {
            if (!secureStorage.hasParentPin()) {
                // One-time migration: a legacy plaintext 4-digit PIN stored on a profile is
                // hashed into secure storage, then wiped from the database. No silent fallback
                // afterwards. If encrypted storage is unavailable the gate stays shut.
                val legacy = allProfiles.value.firstOrNull { it.parentPin.isNotBlank() }?.parentPin
                    ?: _currentProfile.value.parentPin.takeIf { it.isNotBlank() }
                if (legacy?.matches(Regex("^\\d{4}$")) != true) {
                    _pinError.value = true
                    speechManager.speak("No PIN configured. Please set up a parent PIN in settings first.")
                    return@launch
                }
                val migrated = runCatching { secureStorage.setParentPin(legacy) }.getOrDefault(false)
                if (!migrated) {
                    _pinError.value = true
                    speechManager.speak("Secure storage is unavailable on this device.")
                    return@launch
                }
                runCatching { repository.updateParentPinForAll("") }
            }
            if (secureStorage.verifyParentPin(entered)) {
                _pinError.value = false
                navigateTo(AppScreen.PARENT_DASHBOARD)
            } else {
                _pinError.value = true
                speechManager.speak("Incorrect passcode. Please try again.")
            }
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
            // The parent PIN is app-level (one PIN, not per child profile) and lives only in
            // secure storage as a PBKDF2 hash. Legacy plaintext copies are wiped.
            val stored = runCatching { secureStorage.setParentPin(newPin) }.getOrDefault(false)
            if (!stored) {
                Log.e("NeuroPathViewModel", "Failed to store parent PIN")
                _pinError.value = true
                return@launch
            }
            runCatching { repository.updateParentPinForAll("") }
            val current = _currentProfile.value
            _currentProfile.value = current.copy(parentPin = "")
        }
    }

    fun updateAiSetup(platform: String, key: String) {
        viewModelScope.launch {
            // The API key is a secret: encrypted storage only, never the Room profile.
            if (!secureStorage.setHfToken(key)) {
                Log.w("NeuroPathViewModel", "Encrypted storage unavailable; API key kept in memory only")
            }
            LlamaClient.customApiKeyOverride = key.trim()
            val current = _currentProfile.value
            val updated = current.copy(customAiPlatform = platform, customApiKey = "")
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
        customApiKey: String? = null // null = leave the stored token untouched
    ) {
        viewModelScope.launch {
            // The API key is a secret: it lives in encrypted storage, never in the Room profile.
            // Only overwrite it when the caller explicitly passed a value (so saving other
            // settings with an untouched token field cannot wipe it).
            if (customApiKey != null) {
                if (!secureStorage.setHfToken(customApiKey)) {
                    Log.w("NeuroPathViewModel", "Encrypted storage unavailable; API key kept in memory only")
                }
                LlamaClient.customApiKeyOverride = customApiKey.trim()
            }
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
                customApiKey = ""
            )
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
        return LlamaClient.generateChatReply(
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
        chatSessionCollectJob?.cancel()
        speechManager.shutdown()
        soundManager.stopSound()
    }

    companion object {
        /** Maximum user message length accepted into prompts/history/DB. */
        private const val MAX_USER_PROMPT_CHARS = 2000
        private val CONTROL_CHARS_REGEX = Regex("[\\p{Cntrl}&&[^\r\n\t]]")
        private val RUNAWAY_SPACES_REGEX = Regex("[ \t]{3,}")
        private val RUNAWAY_NEWLINES_REGEX = Regex("\n{4,}")
    }
}
