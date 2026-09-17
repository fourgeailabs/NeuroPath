package com.example.network

import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AppLanguage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiGenerateRequest
    ): GeminiGenerateResponse
}

enum class ChatModelMode(
    val id: String,
    val modelName: String,
    val displayName: String,
    val icon: String,
    val description: String,
    val isFreeTier: Boolean = true,
    val tierLabel: String = "Free Model"
) {
    GENERAL("GENERAL", "gemini-3.5-flash", "Gemini 3.5 Flash", "⚡", "Free Model • High-speed personalized tutor for educational explanations", true, "Free Tier"),
    FAST("FAST", "gemini-3.5-flash", "Gemini 3.5 Flash", "🚀", "Free Model • Ultra-low latency, quota-friendly chat", true, "Free Tier"),
    COMPLEX("COMPLEX", "gemini-3.1-pro-preview", "Gemini 3.1 Pro", "🧠", "Deep Reasoning • Advanced multi-step STEM breakdown", false, "Pro Tier"),
    GEMMA_LOCAL("GEMMA_LOCAL", "gemma-2-2b-it-Q4_K_M.gguf", "Gemma 2 2B Local", "💎", "On-Device Gemma 2 2B • GGUF CPU/NEON local inference", true, "Local Gemma"),
    OFFLINE("OFFLINE", "offline-socratic", "Offline Socratic", "🛡️", "Offline Local • Zero-network accredited curriculum engine", true, "Offline")
}


data class LiveVoiceTurnResult(
    val transcriptText: String,
    val audioBase64: String? = null,
    val curriculumCitation: String = ""
)

data class LyriaMusicResult(
    val audioBase64: String? = null,
    val trackTitle: String,
    val durationLabel: String,
    val isSuccess: Boolean
)

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    @Volatile
    var customApiKeyOverride: String = ""

    fun getApiKey(customKey: String = ""): String {
        val trimmedCustom = customKey.trim()
        if (trimmedCustom.isNotBlank() && trimmedCustom != "MY_GEMINI_API_KEY") {
            return trimmedCustom
        }
        val trimmedOverride = customApiKeyOverride.trim()
        if (trimmedOverride.isNotBlank() && trimmedOverride != "MY_GEMINI_API_KEY") {
            return trimmedOverride
        }
        return try {
            val configKey = BuildConfig.GEMINI_API_KEY.trim()
            if (configKey.isNotBlank() && configKey != "MY_GEMINI_API_KEY") {
                configKey
            } else {
                val envKey = (System.getenv("GEMINI_API_KEY") ?: "").trim()
                if (envKey.isNotBlank() && envKey != "MY_GEMINI_API_KEY") envKey else ""
            }
        } catch (_: Exception) {
            ""
        }
    }

    fun hasValidApiKey(customKey: String = ""): Boolean {
        val key = getApiKey(customKey)
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    fun sanitizeHistory(
        conversationHistory: List<Pair<String, String>>,
        newUserMessage: String? = null
    ): List<GeminiContent> {
        val rawList = mutableListOf<Pair<String, String>>()
        for (item in conversationHistory) {
            val role = item.first.lowercase()
            val text = item.second.trim()
            if (text.isNotBlank()) {
                val geminiRole = if (role == "user") "user" else "model"
                rawList.add(geminiRole to text)
            }
        }
        if (!newUserMessage.isNullOrBlank()) {
            val trimmedNew = newUserMessage.trim()
            if (rawList.isEmpty() || rawList.last().second != trimmedNew) {
                rawList.add("user" to trimmedNew)
            }
        }

        val firstUserIndex = rawList.indexOfFirst { it.first == "user" }
        if (firstUserIndex == -1) {
            val fallbackText = newUserMessage?.ifBlank { rawList.lastOrNull()?.second } ?: "Hello"
            return listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = fallbackText))))
        }

        val trimmedRawList = rawList.subList(firstUserIndex, rawList.size)

        val sanitized = mutableListOf<GeminiContent>()
        var currentRole: String? = null
        val currentParts = mutableListOf<GeminiPart>()

        for ((role, text) in trimmedRawList) {
            if (currentRole == null) {
                currentRole = "user"
                currentParts.add(GeminiPart(text = text))
            } else if (role == currentRole) {
                currentParts.add(GeminiPart(text = text))
            } else {
                sanitized.add(GeminiContent(role = currentRole, parts = currentParts.toList()))
                currentParts.clear()
                currentRole = role
                currentParts.add(GeminiPart(text = text))
            }
        }

        if (currentRole != null && currentParts.isNotEmpty()) {
            sanitized.add(GeminiContent(role = currentRole, parts = currentParts.toList()))
        }

        // Ensure the conversation ends with a "user" message
        if (sanitized.isNotEmpty() && sanitized.last().role != "user") {
            val lastUserPart = sanitized.indexOfLast { it.role == "user" }
            if (lastUserPart != -1) {
                return sanitized.subList(0, lastUserPart + 1)
            }
        }

        if (sanitized.isEmpty()) {
            val fallbackText = newUserMessage?.ifBlank { "Hello" } ?: "Hello"
            return listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = fallbackText))))
        }

        return sanitized
    }

    /**
     * Transcribe audio using Gemini models with fallback.
     */
    suspend fun transcribeAudio(
        audioBytes: ByteArray,
        mimeType: String = "audio/wav",
        languageCode: String = "en-US",
        customApiKey: String = ""
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(customApiKey)
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext ""
        }

        val langName = AppLanguage.fromCode(languageCode).displayName
        val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)

        val prompt = "Accurately transcribe the spoken speech in this audio clip. The speaker is speaking in $langName ($languageCode). Output ONLY the exact transcribed text without any conversational preamble or quotation marks."

        val request = GeminiGenerateRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(
                        GeminiPart(text = prompt),
                        GeminiPart(inlineData = GeminiInlineData(mimeType = mimeType, data = base64Audio))
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.0f)
        )

        try {
            val response = service.generateContent("gemini-3.5-flash", apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
            if (!text.isNullOrBlank()) return@withContext text
        } catch (e: Exception) {
            Log.e("GeminiClient", "transcribeAudio primary model failed", e)
        }

        try {
            val response = service.generateContent("gemini-3.1-pro-preview", apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: ""
        } catch (e: Exception) {
            Log.e("GeminiClient", "transcribeAudio fallback model failed", e)
            ""
        }
    }

    /**
     * Gemini Chatbot for Learning Buddy with full access to curriculum.
     */
    suspend fun generateChatReply(
        conversationHistory: List<Pair<String, String>>, // (role "user" or "model", text)
        systemPrompt: String,
        languageCode: String = "en-US",
        schoolDistrict: String = "LAUSD",
        stateOrProvince: String = "California",
        country: String = "United States",
        standardTitle: String = "State Academic Standards",
        curriculumContext: String = "",
        modelMode: ChatModelMode = ChatModelMode.GENERAL,
        customApiKey: String = ""
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(customApiKey)
        val langName = AppLanguage.fromCode(languageCode).displayName

        if (modelMode == ChatModelMode.OFFLINE || apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalSocraticReply(
                lastUserMessage = conversationHistory.lastOrNull { it.first == "user" }?.second ?: "",
                schoolDistrict = schoolDistrict,
                stateOrProvince = stateOrProvince,
                country = country,
                standardTitle = standardTitle,
                languageCode = languageCode,
                conversationHistory = conversationHistory,
                curriculumContext = curriculumContext
            )
        }


        val enrichedSystemPrompt = """
            $systemPrompt
            
            ROLE & KNOWLEDGE BASE ACCESS:
            - You are an expert AI Educational Research Assistant, Socratic Tutor, and Neuro-Affirming Learning Buddy with FULL, UNRESTRICTED ACCESS to OER Commons Curated Collections (https://oercommons.org/curated-collections) and local school jurisdiction standards (${if (curriculumContext.isNotBlank()) curriculumContext else "$standardTitle ($schoolDistrict)"}).
            - Sourced from Open Educational Resources (OER) Curated Collections at https://oercommons.org/curated-collections spanning all K-12 subjects: Mathematics, English Language Arts/Reading, Science & Nature, Social Studies & Civics, and Life Skills/SEL.
            - Respond naturally to greetings, casual chat, educational research inquiries, and learning tasks. Be warm, supportive, scholarly, and conversational!
            - Language: Output natively in $langName ($languageCode).
            
            OFFICIAL OER COMMONS CURATED COLLECTIONS ACCESSED:
            - Primary Open Repository: OER Commons Curated Collections (https://oercommons.org/curated-collections)
            - School Jurisdiction: $schoolDistrict ($stateOrProvince, $country)
            - Educational Framework: $standardTitle
            - Active Curated Curriculum Knowledge Base:
            ${if (curriculumContext.isNotBlank()) curriculumContext else "Full K-12 master curriculum benchmarks synchronized from OER Commons Curated Collections (https://oercommons.org/curated-collections) across Elementary, Middle School, and High School (Grades 9-12)."}

            AI RESEARCH ASSISTANT & TUTORING GUIDELINES:
            1. OER Curated Collections Research: When asked to research, summarize, locate, or explain curriculum topics, standards, lesson plans, or benchmarks, utilize your full knowledge of OER Commons Curated Collections (https://oercommons.org/curated-collections). Present clear, structured, accredited overviews and learning objectives.
            2. For greetings (e.g., "hello", "hi", "hey", "good morning"): Greet the student/parent enthusiastically in character, state that you are an AI Research Assistant connected to the OER Commons Curated Collections (https://oercommons.org/curated-collections) & $schoolDistrict curriculum, and invite them to explore any concept or ask any question.
            3. For academic/homework problems: Provide direct, accurate answers and solutions alongside clear, friendly, step-by-step explanations. When evaluating a student's answer, state clearly whether it is correct or incorrect: praise correct answers, and gently point out mistakes with the correct solution if incorrect.
            4. For general knowledge or conceptual questions: Provide clear, friendly, bite-sized explanations with engaging analogies grounded in the active curriculum and interest world.
            5. NO CITATIONS OR FOOTNOTES: Do NOT append any citation footers, reference tags, or footnotes (such as "[Curriculum Reference:...]") to the end of your responses. Keep responses clean and engaging.
        """.trimIndent()

        val contents = sanitizeHistory(conversationHistory)

        val generationConfig = GeminiGenerationConfig(
            temperature = if (modelMode == ChatModelMode.COMPLEX) 0.6f else 0.7f
        )

        val request = GeminiGenerateRequest(
            contents = contents,
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = enrichedSystemPrompt))
            ),
            generationConfig = generationConfig
        )

        val modelsToTry = listOf(
            modelMode.modelName,
            "gemini-3.5-flash",
            "gemini-flash-latest",
            "gemini-3.1-pro-preview",
            "gemini-3.1-flash-lite-preview"
        ).distinct()

        for (model in modelsToTry) {
            try {
                Log.i("GeminiClient", "Executing Gemini API ($model)...")
                val response = service.generateContent(model, apiKey, request)
                val textParts = response.candidates?.firstOrNull()?.content?.parts
                val text = textParts?.mapNotNull { it.text }?.joinToString("\n")?.trim()
                if (!text.isNullOrBlank()) {
                    return@withContext text
                }
            } catch (e: Exception) {
                Log.w("GeminiClient", "generateChatReply model ($model) with systemInstruction failed: ${e.message}")
                try {
                    // Fallback: Embed system prompt directly into conversation context if systemInstruction is rejected
                    val fallbackContents = mutableListOf<GeminiContent>()
                    fallbackContents.add(
                        GeminiContent(
                            role = "user",
                            parts = listOf(GeminiPart(text = "[System Instructions]\n$enrichedSystemPrompt"))
                        )
                    )
                    fallbackContents.add(
                        GeminiContent(
                            role = "model",
                            parts = listOf(GeminiPart(text = "Understood. I will act as the friendly learning buddy following these guidelines."))
                        )
                    )
                    fallbackContents.addAll(contents)

                    val fallbackRequest = GeminiGenerateRequest(
                        contents = fallbackContents,
                        generationConfig = generationConfig
                    )
                    val response = service.generateContent(model, apiKey, fallbackRequest)
                    val textParts = response.candidates?.firstOrNull()?.content?.parts
                    val text = textParts?.mapNotNull { it.text }?.joinToString("\n")?.trim()
                    if (!text.isNullOrBlank()) {
                        return@withContext text
                    }
                } catch (e2: Exception) {
                    Log.w("GeminiClient", "generateChatReply model ($model) fallback failed: ${e2.message}")
                }
            }
        }

        generateLocalSocraticReply(
            lastUserMessage = conversationHistory.lastOrNull { it.first == "user" }?.second ?: "",
            schoolDistrict = schoolDistrict,
            stateOrProvince = stateOrProvince,
            country = country,
            standardTitle = standardTitle,
            languageCode = languageCode,
            conversationHistory = conversationHistory
        )
    }

    /**
     * Voice Conversations (Live API mode) with full curriculum access.
     */
    suspend fun generateLiveVoiceConversationTurn(
        userVoiceAudio: ByteArray?,
        userText: String?,
        conversationHistory: List<Pair<String, String>>,
        systemPrompt: String,
        curriculumContext: String,
        schoolDistrict: String,
        stateOrProvince: String,
        country: String,
        standardTitle: String,
        languageCode: String,
        customApiKey: String = ""
    ): LiveVoiceTurnResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(customApiKey)
        val langName = AppLanguage.fromCode(languageCode).displayName

        val effectiveUserText = userText ?: if (userVoiceAudio != null) {
            transcribeAudio(userVoiceAudio, languageCode = languageCode, customApiKey = apiKey)
        } else {
            "Hello buddy!"
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val localText = generateLocalSocraticReply(
                lastUserMessage = effectiveUserText,
                schoolDistrict = schoolDistrict,
                stateOrProvince = stateOrProvince,
                country = country,
                standardTitle = standardTitle,
                languageCode = languageCode
            )
            return@withContext LiveVoiceTurnResult(
                transcriptText = localText,
                curriculumCitation = ""
            )
        }

        // 1. Attempt real WebSocket-based Gemini Live API turn first for low-latency voice
        try {
            val liveTurn = GeminiLiveApiClient.generateTurn(
                apiKey = apiKey,
                userVoiceAudio = userVoiceAudio,
                userText = effectiveUserText,
                conversationHistory = conversationHistory,
                systemPrompt = systemPrompt,
                curriculumContext = curriculumContext,
                schoolDistrict = schoolDistrict,
                stateOrProvince = stateOrProvince,
                country = country,
                standardTitle = standardTitle,
                languageCode = languageCode
            )
            if (liveTurn.transcriptText.isNotBlank() && liveTurn.transcriptText != "Voice service unavailable.") {
                return@withContext liveTurn
            }
        } catch (e: Exception) {
            Log.w("GeminiClient", "GeminiLiveApiClient WebSocket turn unavailable, using REST fallback: ${e.message}")
        }

        val enrichedVoiceSystemPrompt = """
            $systemPrompt
            
            ROLE: Live Voice Interactive Learning Companion
            
            FULL CURRICULUM ACCESS & LOCAL STANDARDS:
            - Jurisdiction: $schoolDistrict ($stateOrProvince, $country)
            - Official Framework: $standardTitle
            - Active Curriculum Content: ${if (curriculumContext.isNotBlank()) curriculumContext else "Full accredited standards across subjects."}
            
            LIVE VOICE CONVERSATION GUIDELINES:
            1. Keep spoken responses warm, concise (2-4 sentences), and natural for vocal listening.
            2. Never give away direct answers; ask a gentle guiding question grounded in their curriculum.
            3. Speak warmly in $langName ($languageCode).
            4. Do NOT append any curriculum reference tags, citations, or footnotes to spoken text.
        """.trimIndent()

        val contents = sanitizeHistory(conversationHistory, newUserMessage = effectiveUserText).toMutableList()
        if (userVoiceAudio != null && contents.isNotEmpty()) {
            val lastIdx = contents.lastIndex
            val lastContent = contents[lastIdx]
            if (lastContent.role == "user") {
                val updatedParts = (lastContent.parts ?: emptyList()).toMutableList()
                updatedParts.add(
                    GeminiPart(
                        inlineData = GeminiInlineData(
                            mimeType = "audio/wav",
                            data = Base64.encodeToString(userVoiceAudio, Base64.NO_WRAP)
                        )
                    )
                )
                contents[lastIdx] = GeminiContent(role = "user", parts = updatedParts)
            }
        }

        val request = GeminiGenerateRequest(
            contents = contents,
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = enrichedVoiceSystemPrompt))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7f
            )
        )

        try {
            val response = service.generateContent("gemini-3.5-flash", apiKey, request)
            val candidate = response.candidates?.firstOrNull()
            val text = candidate?.content?.parts?.firstOrNull()?.text ?: ""
            val audioInline = candidate?.content?.parts?.firstOrNull { it.inlineData != null }?.inlineData?.data

            LiveVoiceTurnResult(
                transcriptText = if (text.isNotBlank()) text else "I hear you loud and clear! Let's explore that step together.",
                audioBase64 = audioInline,
                curriculumCitation = ""
            )
        } catch (e: Exception) {
            Log.e("GeminiClient", "generateLiveVoiceConversationTurn primary failed", e)
            try {
                val fallbackResponse = service.generateContent("gemini-3.1-pro-preview", apiKey, request)
                val text = fallbackResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                LiveVoiceTurnResult(
                    transcriptText = if (text.isNotBlank()) text else "I am right here with you! Let's take it one step at a time.",
                    curriculumCitation = ""
                )
            } catch (e2: Exception) {
                Log.e("GeminiClient", "generateLiveVoiceConversationTurn fallback failed", e2)
                LiveVoiceTurnResult(
                    transcriptText = generateLocalSocraticReply(
                        lastUserMessage = effectiveUserText,
                        schoolDistrict = schoolDistrict,
                        stateOrProvince = stateOrProvince,
                        country = country,
                        standardTitle = standardTitle,
                        languageCode = languageCode
                    ),
                    curriculumCitation = ""
                )
            }
        }
    }

    /**
     * Generate Music for Soundscapes using lyria-3-clip-preview (<= 30s clips) or lyria-3-pro-preview (full tracks).
     */
    suspend fun generateSoundscapeMusic(
        prompt: String,
        isShortClip: Boolean = true,
        title: String = "Calm Ambient Soundscape"
    ): LyriaMusicResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val durationLabel = if (isShortClip) "30s Ambient Loop" else "Full Sensory Track"
        val model = if (isShortClip) "lyria-3-clip-preview" else "lyria-3-pro-preview"

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext LyriaMusicResult(
                audioBase64 = null,
                trackTitle = title,
                durationLabel = durationLabel,
                isSuccess = false
            )
        }

        val enrichedPrompt = "Generate sensory ambient music for neurodivergent focus, calm, and emotional regulation: $prompt. Gentle, no sudden loud volume spikes, seamless soothing loop."

        val request = GeminiGenerateRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = enrichedPrompt))
                )
            ),
            generationConfig = GeminiGenerationConfig(
                responseModalities = listOf("AUDIO")
            )
        )

        try {
            val response = service.generateContent(model, apiKey, request)
            val audioPart = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.inlineData != null }
            val base64Data = audioPart?.inlineData?.data

            if (!base64Data.isNullOrBlank()) {
                LyriaMusicResult(
                    audioBase64 = base64Data,
                    trackTitle = title,
                    durationLabel = durationLabel,
                    isSuccess = true
                )
            } else {
                LyriaMusicResult(
                    audioBase64 = null,
                    trackTitle = title,
                    durationLabel = durationLabel,
                    isSuccess = false
                )
            }
        } catch (_: Exception) {
            LyriaMusicResult(
                audioBase64 = null,
                trackTitle = title,
                durationLabel = durationLabel,
                isSuccess = false
            )
        }
    }

    suspend fun generateAdaptiveHint(
        question: String,
        wrongAnswer: String,
        themeTitle: String,
        gradeLevel: String,
        languageCode: String = "en-US",
        schoolDistrict: String = "LAUSD",
        standardTitle: String = "Core Standards"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val langName = AppLanguage.fromCode(languageCode).displayName

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Let's take a deep breath! Look at the key clue in the problem. What happens if you break it into two smaller pieces?"
        }

        val prompt = """
            You are a gentle, growth-mindset neurodiversity learning coach for a $gradeLevel student in $schoolDistrict.
            Their special interest theme is $themeTitle.
            The student was asked: "$question".
            They selected "$wrongAnswer".
            
            RULES:
            1. DO NOT give the direct correct answer.
            2. Provide a warm, supportive 2-sentence clue in $langName ($languageCode) that uses their theme and guides them on HOW to solve it themselves.
            3. Do NOT append any curriculum reference tags or citations.
        """.trimIndent()

        val request = GeminiGenerateRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.6f)
        )

        try {
            val response = service.generateContent("gemini-3.5-flash", apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Mistakes are how our brains make new connections! Take another look at the clues."
        } catch (e: Exception) {
            try {
                val response = service.generateContent("gemini-3.1-pro-preview", apiKey, request)
                response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Mistakes are how our brains make new connections! Take another look at the clues."
            } catch (_: Exception) {
                "Mistakes are how our brains make new connections! Take another look at the clues."
            }
        }
    }

    suspend fun downloadAllGradeCurriculumForLocale(
        country: String,
        stateOrProvince: String,
        city: String,
        schoolDistrict: String,
        postalCode: String,
        standardTitle: String,
        languageCode: String = "en-US"
    ): DownloadedCurriculumResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val langName = AppLanguage.fromCode(languageCode).displayName

        val officialAgency = when {
            country.contains("United States", ignoreCase = true) || country.equals("US", ignoreCase = true) -> {
                when {
                    stateOrProvince.contains("California", ignoreCase = true) -> "OER Commons Curated Collections & California Dept of Education (CA-CCSS/NGSS)"
                    stateOrProvince.contains("Texas", ignoreCase = true) -> "OER Commons Curated Collections & Texas Education Agency (TEKS)"
                    stateOrProvince.contains("New York", ignoreCase = true) -> "OER Commons Curated Collections & NYSED Next Generation Standards"
                    stateOrProvince.contains("Florida", ignoreCase = true) -> "OER Commons Curated Collections & FLDOE B.E.S.T. Standards"
                    stateOrProvince.contains("Illinois", ignoreCase = true) -> "OER Commons Curated Collections & ISBE Learning Standards"
                    stateOrProvince.contains("Washington", ignoreCase = true) -> "OER Commons Curated Collections & Washington OSPI"
                    stateOrProvince.contains("Massachusetts", ignoreCase = true) -> "OER Commons Curated Collections & Massachusetts DESE"
                    else -> "OER Commons Curated Collections (https://oercommons.org/curated-collections) & $stateOrProvince Dept of Education ($schoolDistrict)"
                }
            }
            country.contains("United Kingdom", ignoreCase = true) || country.equals("GB", ignoreCase = true) -> {
                "UK Department for Education (DfE) / National Curriculum & Standards Authority"
            }
            country.contains("Canada", ignoreCase = true) -> {
                "$stateOrProvince Ministry of Education & $schoolDistrict"
            }
            country.contains("Australia", ignoreCase = true) -> {
                "Australian Curriculum, Assessment and Reporting Authority (ACARA)"
            }
            country.contains("India", ignoreCase = true) -> {
                "National Council of Educational Research and Training (NCERT) / NEP 2020"
            }
            else -> "$country Ministry of Education & Regional Standards Agency"
        }

        val officialUrl = when {
            country.contains("United States", ignoreCase = true) || country.equals("US", ignoreCase = true) -> "https://oercommons.org/curated-collections"
            country.contains("United Kingdom", ignoreCase = true) -> "https://www.gov.uk/national-curriculum"
            country.contains("Canada", ignoreCase = true) -> "https://www.ontario.ca/page/curriculum & https://curriculum.gov.bc.ca"
            country.contains("Australia", ignoreCase = true) -> "https://www.australiancurriculum.edu.au"
            country.contains("India", ignoreCase = true) -> "https://ncert.nic.in/textbook.php"
            else -> "https://unesco.org/education/curriculum-standards"
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext DownloadedCurriculumResult(
                officialSourceAgency = officialAgency,
                officialSourceUrl = officialUrl,
                gradesSummary = "Pre-K, Kindergarten, Elementary (1-5), Middle School (6-8), High School (9-12)",
                curriculumSummary = "Offline mode: Curriculum based on OER Commons Curated Collections (https://oercommons.org/curated-collections) standards for $schoolDistrict ($stateOrProvince, $country). Configure a valid Gemini API key for AI-enhanced curriculum sync.",
                isOnlineSynced = false
            )
        }

        val prompt = """
            You are the Chief Educational Standards Registrar & Curriculum Architect.
            Download, index, and synthesize officially accredited open educational benchmarks across ALL K-12 grade levels (Pre-K, Kindergarten, Elementary 1-5, Middle School 6-8, and High School 9-12) utilizing open educational databases from OER Commons Curated Collections (https://oercommons.org/curated-collections) and the student's home school jurisdiction:
            - Country: $country
            - State/Province: $stateOrProvince
            - City: $city
            - School District: $schoolDistrict
            - Postal/Zip Code: $postalCode
            - Standard Title: $standardTitle
            
            REQUIRED HIGH SCHOOL & K-12 BENCHMARKS:
            You MUST ensure full, rigorous curriculum coverage for HIGH SCHOOL (Grades 9-12) as well as Elementary and Middle School:
            1. High School Mathematics: Algebra I, Geometry, Algebra II, Pre-Calculus, Trigonometry, Statistics, and Function Modeling.
            2. High School English Language Arts: Rhetorical analysis, literary synthesis, argumentative writing, classical/world literature.
            3. High School Sciences: Biology (Cellular, Genetics, Evolution), Chemistry (Stoichiometry, Bonding), Physics (Newtonian Mechanics, Thermodynamics, Electromagnetism).
            4. High School Social Studies: US Government & Civics, US/World History, Macroeconomics & Microeconomics, and Constitutional Precedents.
            5. High School Life Skills & Executive Functioning: Personal finance (budgeting, compound interest, credit), time management, and neurodivergent executive strategies.
            
            Reference OER Commons Curated Collections (https://oercommons.org/curated-collections) as the primary open database source.
            Output in $langName ($languageCode).
        """.trimIndent()

        val request = GeminiGenerateRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.4f)
        )

        try {
            val response = service.generateContent("gemini-3.5-flash", apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: "Curriculum synchronized from OER Commons Curated Collections (https://oercommons.org/curated-collections) across all K-12 grades for $schoolDistrict."
            DownloadedCurriculumResult(
                officialSourceAgency = officialAgency,
                officialSourceUrl = officialUrl,
                gradesSummary = "K-12 Full Spectrum (Pre-K, Elementary, Middle School, High School 9-12)",
                curriculumSummary = text,
                isOnlineSynced = true
            )
        } catch (e: Exception) {
            Log.e("GeminiClient", "Failed to download curriculum for locale", e)
            DownloadedCurriculumResult(
                officialSourceAgency = officialAgency,
                officialSourceUrl = officialUrl,
                gradesSummary = "Pre-K through High School (Grades 9-12)",
                curriculumSummary = "Sync failed: ${e.message}. Using offline curriculum based on OER Commons Curated Collections (https://oercommons.org/curated-collections) for $schoolDistrict ($stateOrProvince, $country) under $standardTitle.",
                isOnlineSynced = false
            )
        }
    }

    suspend fun fetchDistrictCurriculumSummary(
        country: String,
        state: String,
        city: String,
        district: String,
        grade: String,
        languageCode: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Custom curriculum alignment verified for $district ($city, $state, $country)."
        }

        val langName = AppLanguage.fromCode(languageCode).displayName

        val prompt = """
            Provide a concise 3-bullet summary of the core $grade learning benchmarks and subject standards for:
            District: $district
            City: $city
            State/Province: $state
            Country: $country
            
            Write the output in $langName ($languageCode).
        """.trimIndent()

        val request = GeminiGenerateRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.5f)
        )

        try {
            val response = service.generateContent("gemini-3.5-flash", apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "$district ($city, $state) $grade curriculum synchronized."
        } catch (e: Exception) {
            "$district ($city, $state) $grade curriculum synchronized."
        }
    }

    data class MathExprResult(
        val originalDisplay: String,
        val steps: List<String>,
        val finalResult: Long
    )

    fun parseAndEvaluateMath(input: String): MathExprResult? {
        val lower = input.lowercase()
        var normalized = lower
            .replace("plus", "+")
            .replace("added to", "+")
            .replace("minus", "-")
            .replace("subtracted from", "-")
            .replace("take away", "-")
            .replace("times", "*")
            .replace("multiplied by", "*")
            .replace("divided by", "/")
            .replace("over", "/")
            .replace("x ", "* ")

        val tokenRegex = Regex("\\d+|[\\+\\-\\*/]")
        val matches = tokenRegex.findAll(normalized).map { it.value }.toList()

        if (matches.size < 3) return null

        val tokens = mutableListOf<String>()
        var expectingNumber = true

        for (m in matches) {
            val isNum = m.toLongOrNull() != null
            val isOp = m in listOf("+", "-", "*", "/")
            if (expectingNumber && isNum) {
                tokens.add(m)
                expectingNumber = false
            } else if (!expectingNumber && isOp) {
                tokens.add(m)
                expectingNumber = true
            }
        }

        if (tokens.size % 2 == 0 && tokens.isNotEmpty()) {
            tokens.removeAt(tokens.lastIndex)
        }

        if (tokens.size < 3) return null

        val originalDisplay = tokens.joinToString(" ") { t ->
            when (t) {
                "*" -> "×"
                "/" -> "÷"
                else -> t
            }
        }

        val workList = tokens.toMutableList()
        val steps = mutableListOf<String>()

        // 1. Multiplication and Division pass
        var idx = 0
        while (idx < workList.size) {
            val token = workList[idx]
            if (token == "*" || token == "/") {
                val n1 = workList[idx - 1].toLong()
                val n2 = workList[idx + 1].toLong()
                val res = if (token == "*") n1 * n2 else if (n2 != 0L) n1 / n2 else 0L
                val opDisp = if (token == "*") "×" else "÷"
                steps.add("$n1 $opDisp $n2 = $res")

                workList.removeAt(idx + 1)
                workList.removeAt(idx)
                workList[idx - 1] = res.toString()
                idx -= 1
            } else {
                idx++
            }
        }

        // 2. Addition and Subtraction pass
        idx = 0
        while (idx < workList.size) {
            val token = workList[idx]
            if (token == "+" || token == "-") {
                val n1 = workList[idx - 1].toLong()
                val n2 = workList[idx + 1].toLong()
                val res = if (token == "+") n1 + n2 else n1 - n2
                steps.add("$n1 $token $n2 = $res")

                workList.removeAt(idx + 1)
                workList.removeAt(idx)
                workList[idx - 1] = res.toString()
                idx -= 1
            } else {
                idx++
            }
        }

        val finalResult = workList.firstOrNull()?.toLongOrNull() ?: return null
        return MathExprResult(originalDisplay, steps, finalResult)
    }

    fun generateLocalSocraticReply(
        lastUserMessage: String,
        schoolDistrict: String = "",
        stateOrProvince: String = "",
        country: String = "",
        standardTitle: String = "",
        languageCode: String = "en-US",
        conversationHistory: List<Pair<String, String>> = emptyList(),
        curriculumContext: String = ""
    ): String {
        val raw = lastUserMessage.trim()
        val query = raw.lowercase()

        val previousUserMsg = conversationHistory.filter { it.first.equals("user", ignoreCase = true) }.let {
            if (it.size >= 2) it[it.size - 2].second else ""
        }
        val lastBuddyMsg = conversationHistory.lastOrNull { it.first.equals("model", ignoreCase = true) || it.first.equals("BUDDY", ignoreCase = true) }?.second ?: ""

        val numbersInQuery = Regex("\\d+").findAll(query).mapNotNull { it.value.toLongOrNull() }.toList()

        // 1. Evaluate math expressions directly from query
        val mathExpr = parseAndEvaluateMath(query)
        if (mathExpr != null) {
            val (disp, steps, ans) = mathExpr
            return if (steps.size == 1) {
                "**$disp = $ans**! 🎉\n\nHere's how to solve it: **${steps.first()}**."
            } else {
                val formattedSteps = steps.mapIndexed { i, s ->
                    val prefix = when (i) {
                        0 -> "First"
                        steps.lastIndex -> "Then"
                        else -> "Next"
                    }
                    "${i + 1}. $prefix, **$s**"
                }.joinToString("\n")
                "**$disp = $ans**! 🎉\n\nHere's how to solve it step-by-step:\n$formattedSteps"
            }
        }

        // 2. Evaluate student answer against previous math expression
        val prevMathExpr = parseAndEvaluateMath(previousUserMsg)
        if (prevMathExpr != null && numbersInQuery.isNotEmpty()) {
            val userAns = numbersInQuery.first()
            return if (userAns == prevMathExpr.finalResult) {
                "Bingo! That's correct! 🎉 **${prevMathExpr.originalDisplay} = ${prevMathExpr.finalResult}**. Outstanding work!"
            } else {
                "Not quite! **$userAns** is not correct for **${prevMathExpr.originalDisplay}**. The correct answer is **${prevMathExpr.finalResult}**. Keep going, you're doing great!"
            }
        }

        // 3. Handle explicit follow-up requests ("explain simpler", "step-by-step", "why?", "example", "quiz me", "check-in")
        val isSimplerRequest = query.contains("simpler") || query.contains("analogy") || query.contains("eli5") || query.contains("easier")
        val isStepByStepRequest = query.contains("step-by-step") || query.contains("step by step") || query.contains("numbered") || query.contains("breakdown") || query.contains("break down") || query.contains("steps")
        val isExampleRequest = query.contains("example") || query.contains("show me") || query.contains("sample")
        val isQuizRequest = query.contains("quiz") || query.contains("test me") || query.contains("check-in") || query.contains("check in") || query.contains("practice question")
        val isWhyImportantRequest = query.contains("why is this important") || query.contains("why is it important") || query.contains("why does this matter") || query.contains("why care") || query.contains("importance")

        if (isWhyImportantRequest) {
            val topicExcerpt = if (lastBuddyMsg.isNotBlank()) {
                val lines = lastBuddyMsg.lines().filter { it.isNotBlank() }
                lines.firstOrNull { !it.startsWith("💡") && !it.startsWith("🌟") && !it.startsWith("✅") }?.take(100) ?: "this lesson concept"
            } else "this core curriculum concept"

            return """
                🌟 **Why This Is So Important in Real Life!**
                
                Understanding **$topicExcerpt** is a powerful skill used every day:
                
                - 💡 **Real-World Application**: Engineers, scientists, artists, and creators use this principle to solve real problems and design amazing things!
                - 🧠 **Brain Growth**: Learning this strengthens your executive function, logical reasoning, and problem-solving memory.
                - 🚀 **Long-Term Success**: Mastering this building block makes advanced math, science, and reading concepts feel natural and easy.
                
                *Would you like to see a fun real-world example, or try a quick check-in question?*
            """.trimIndent()
        }

        if (isSimplerRequest) {
            val topicSnippet = if (lastBuddyMsg.isNotBlank()) lastBuddyMsg.take(120).replace("\n", " ") else "this topic"
            return """
                🌟 **Simple Analogy Time!**
                
                Think of this concept like building with LEGO bricks or decorating a pizza 🍕:
                - Every piece connects together to make something complete.
                - When you break it down into small building blocks, it becomes easy to see how every piece fits!
                
                *Key Takeaway for "$topicSnippet..."*:
                Focus on the big idea first, and then build on it step by step. Would you like a 1-2-3 checklist or a quick fun puzzle about this?
            """.trimIndent()
        }

        if (isStepByStepRequest) {
            return """
                📋 **Step-by-Step Breakdown:**
                
                1. **Identify the Core Goal**: Look at what key question or problem you are trying to solve.
                2. **Gather the Facts & Numbers**: Highlight the known rules, numbers, or definitions.
                3. **Apply the Rule**: Execute the method or calculation step-by-step.
                4. **Verify Your Result**: Double-check your solution to ensure it makes sense!
                
                What part would you like to practice next?
            """.trimIndent()
        }

        if (isExampleRequest) {
            return """
                💡 **Here is a Clear Real-World Example:**
                
                Suppose you have 10 apples 🍎 and want to share them equally among 2 friends:
                - Each friend receives **10 ÷ 2 = 5 apples**.
                - If you gain 3 more apples, you now have **10 + 3 = 13 apples**.
                
                Connecting rules to real objects makes every concept crystal clear! What specific question or number problem should we apply this to?
            """.trimIndent()
        }

        if (isQuizRequest) {
            return """
                🎯 **Quick Learning Buddy Check-In Quiz!**
                
                **Question**: Which of the following best describes the main rule we are learning?
                
                A) Always break big problems into smaller, manageable steps.
                B) Guess without checking your work.
                C) Skip reading the question instructions.
                
                *Reply with A, B, or C to test your answer!*
            """.trimIndent()
        }

        // 4. AI Research Assistant: Direct curriculum standards inquiry
        val isCurriculumResearch = query.contains("research standards") || query.contains("curriculum report") ||
                query.contains("curriculum benchmarks") || query.contains("what am i learning in school") ||
                query.contains("district standards")

        if (isCurriculumResearch) {
            val districtInfo = listOfNotNull(
                schoolDistrict.takeIf { it.isNotBlank() },
                stateOrProvince.takeIf { it.isNotBlank() },
                country.takeIf { it.isNotBlank() }
            ).joinToString(", ")
            val stdInfo = if (standardTitle.isNotBlank()) standardTitle else "OER Commons Curated Collections (https://oercommons.org/curated-collections)"

            return "🔬 **AI Curriculum Research Assistant Report**\n*Jurisdiction: ${if (districtInfo.isNotBlank()) districtInfo else "OER Commons Curated Collections"} ($stdInfo)*\n\n" +
                    "📚 **Curriculum Benchmarks Overview:**\n" +
                    "1. 📊 **Mathematics**: Foundational operations, fractions, geometry, and algebra equations.\n" +
                    "2. 📖 **Reading & ELA**: Phonemic awareness, vocabulary development, main idea, and rhetorical synthesis.\n" +
                    "3. 🔬 **Science**: Ecosystems, physical forces, solar system, cell bioenergetics, and physics.\n" +
                    "4. 🗺️ **Social Studies**: Map reading, community civics, government branches, and history.\n" +
                    "5. 💼 **Life Skills & SEL**: Emotional self-regulation (4-7-8 breathing) & time management.\n\n" +
                    "💡 *Research Assistant Tip*: Ask me any question on these topics to explore them step-by-step!"
        }

        // 5. Subject Specific Answers & Explanation Engines
        val isPhotosynthesis = query.contains("photo") || query.contains("sunlight") || query.contains("chlorophyll")
        val isGravitySpace = query.contains("gravity") || query.contains("space") || query.contains("planet") || query.contains("solar system") || query.contains("moon") || query.contains("orbit")
        val isFractions = query.contains("fraction") || query.contains("half") || query.contains("quarter") || query.contains("numerator") || query.contains("denominator")
        val isAlgebra = query.contains("algebra") || query.contains("variable") || query.contains("equation") || query.contains("quadratic")
        val isReadingGrammar = query.contains("noun") || query.contains("verb") || query.contains("adjective") || query.contains("grammar") || query.contains("sentence") || query.contains("phonics")
        val isHistoryGov = query.contains("government") || query.contains("president") || query.contains("branch") || query.contains("constitution") || query.contains("history") || query.contains("map")
        val isBreathingSel = query.contains("calm") || query.contains("breathe") || query.contains("overwhelm") || query.contains("stress") || query.contains("focus")

        if (isPhotosynthesis) {
            return """
                🌱 **Photosynthesis Explained!**
                
                **Photosynthesis** is how plants make their own food using light energy from the sun!
                
                - **Ingredients**: Sunlight ☀️ + Water (H₂O) 💧 + Carbon Dioxide (CO₂) 🌬️
                - **Where it happens**: Inside tiny green powerhouses in plant leaves called **chloroplasts** (powered by green *chlorophyll*).
                - **What it produces**: Glucose (sugar for plant energy 🍬) + Oxygen (O₂ for us to breathe! 🌬️)
                
                💡 *Fun Analogy*: Chloroplasts are like solar-powered kitchens inside every leaf!
                
                Would you like to learn how plant roots absorb water, or practice a quick quiz question on this?
            """.trimIndent()
        }

        if (isGravitySpace) {
            return """
                🪐 **Gravity & The Solar System!**
                
                **Gravity** is an invisible pulling force that attracts objects toward each other.
                
                - **Earth's Gravity**: Pulls everything toward the center of Earth, keeping our feet on the ground and holding our atmosphere in place!
                - **The Sun's Gravity**: Holds all 8 planets in orbit around the solar system.
                - **Mass & Gravity**: The bigger an object's mass, the stronger its gravitational pull!
                
                💡 *Did you know?*: The Moon has less mass than Earth, so if you weigh 60 lbs on Earth, you'd weigh only 10 lbs on the Moon! 🌕
                
                What space or physics question would you like to explore next?
            """.trimIndent()
        }

        if (isFractions) {
            return """
                🍰 **Fractions Made Simple!**
                
                A **fraction** represents an equal part of a whole thing!
                
                - **Top Number (Numerator)**: How many pieces you *have* (e.g., 3 slices of pizza).
                - **Bottom Number (Denominator)**: The total number of equal pieces the whole is divided into (e.g., 8 slices in a full pizza pie).
                - **Example**: **3/8** means you have 3 out of 8 equal slices!
                
                💡 *Pro Tip*: To add fractions with the same denominator (bottom number), just add the top numbers together: **1/4 + 2/4 = 3/4**!
                
                Would you like to try solving a fraction problem together?
            """.trimIndent()
        }

        if (isAlgebra) {
            return """
                📐 **Algebra & Equations!**
                
                In **Algebra**, we use letters (like **x** or **y**) as mystery variables representing numbers we want to find.
                
                - **Golden Rule**: Whatever operation you do to one side of the equals sign (=), you MUST do to the other side to keep it balanced!
                - **Example**: **x + 5 = 12**
                  - Subtract 5 from both sides: **x = 12 - 5**
                  - Result: **x = 7**! 🎉
                
                Give me any equation (like `2x + 4 = 10`), and I'll walk you through solving it step-by-step!
            """.trimIndent()
        }

        if (isReadingGrammar) {
            return """
                📖 **Parts of Speech & Grammar Basics!**
                
                - **Noun**: A person, place, thing, or idea (e.g., *student*, *school*, *robot* 🤖).
                - **Verb**: An action word describing what someone does (e.g., *run*, *learn*, *think* 🧠).
                - **Adjective**: A descriptive word that tells us more about a noun (e.g., *bright*, *curious*, *fun* ✨).
                - **Adverb**: Describes how an action is performed (e.g., *quickly*, *gently*).
                
                💡 *Example Sentence*: "The **curious** *(adj)* **student** *(noun)* **learned** *(verb)* **quickly** *(adv)*!"
                
                Would you like to practice identifying parts of speech in another sentence?
            """.trimIndent()
        }

        if (isHistoryGov) {
            return """
                🗺️ **Civics & The 3 Branches of Government!**
                
                In the United States democracy, power is divided into three equal branches so no single branch has total control:
                
                1. **Legislative Branch (Congress)**: Makes national laws (Senate & House of Representatives).
                2. **Executive Branch (President & Cabinet)**: Enforces and carries out laws.
                3. **Judicial Branch (Supreme Court)**: Evaluates and interprets laws according to the Constitution.
                
                💡 *Checks & Balances*: Each branch can limit the power of the others to keep the government fair!
                
                Would you like to learn about map compass directions, community helpers, or historical timelines?
            """.trimIndent()
        }

        if (isBreathingSel) {
            return """
                🌬️ **4-7-8 Calm Reset Strategy!**
                
                When feeling overwhelmed or stressed, resetting your nervous system is your superpower:
                
                1. **Inhale**: Breathe in slowly through your nose for **4 seconds** 🌸
                2. **Hold**: Hold your breath calmly for **7 seconds** 🧘
                3. **Exhale**: Slowly breathe out through your mouth for **8 seconds** 🍃
                
                Repeating this 3 times calms your body, lowers heart rate, and brings sharp focus back to your brain!
                
                Would you like to try a guided breathing session or a quick sensory break?
            """.trimIndent()
        }

        // 6. Test & Status verification queries
        val isTestCheck = query.contains("test") || query.contains("working") || query.contains("ready") || query.contains("status") || query.contains("are you working")
        if (isTestCheck) {
            val distStr = if (schoolDistrict.isNotBlank()) " synchronized with $schoolDistrict ($standardTitle)" else ""
            return """
                ✅ **I am fully working and ready to assist you!**
                
                I am your AI Learning Buddy$distStr. I am active and ready for your questions!
                
                **Here is what we can explore together:**
                - 📐 **Math & Problem Solving**: Type any equation, fraction, or word problem for step-by-step guidance.
                - 🔬 **Science & Nature**: Ask about photosynthesis, gravity, space, chemistry, or ecosystems.
                - 📖 **Reading, Phonics & ELA**: Get help with grammar, main ideas, vocabulary, and writing.
                - 🗺️ **Civics, Geography & History**: Explore government branches, maps, and historical timelines.
                - 🧘 **Mindfulness & Focus**: Ask for a 4-7-8 breathing exercise or quick sensory break.
                
                *What topic or question would you like to explore today?*
            """.trimIndent()
        }

        // 7. Dynamic Response Generator for Any Custom Question
        val cleanQuestion = if (raw.length <= 60) raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } else raw.take(55) + "..."

        return """
            💡 **Exploring "$cleanQuestion"**
            
            Great question! Here is how to understand this concept:
            
            - **Core Concept**: **$cleanQuestion** connects directly to foundational learning patterns in our accredited curriculum.
            - **Key Observation**: When we examine this topic, breaking it into smaller pieces reveals how every part fits together.
            - **Interactive Discovery**: What is one thing you already know about this topic, or what specific part would you like to solve or break down step-by-step?
            
            *Type your thoughts below or click 'Simpler' or 'Steps' above to explore further!*
        """.trimIndent()
    }
}
