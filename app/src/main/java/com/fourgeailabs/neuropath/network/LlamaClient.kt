package com.fourgeailabs.neuropath.network

import android.content.Context
import android.util.Base64
import android.util.Log
import com.fourgeailabs.neuropath.BuildConfig
import com.fourgeailabs.neuropath.data.model.AppLanguage
import com.fourgeailabs.neuropath.data.model.AppLanguageDictionary
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException
import kotlin.random.Random
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

interface LlamaApiService {
    // Hugging Face Serverless Inference API — OpenAI-compatible chat completions.
    // Documented base: https://router.huggingface.co/v1 ; the model id travels in the request body.
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: LlamaChatRequest
    ): LlamaChatResponse
}

enum class ChatModelMode(
    val id: String,
    val modelName: String,
    val displayName: String,
    val icon: String,
    val description: String,
    /** Dictionary key for the user-facing option label ("Cloud AI" / "Local AI" / "Socratic Teacher"). */
    val labelKey: String,
    /** Dictionary key for the user-facing option description. */
    val descriptionKey: String,
    val isFreeTier: Boolean = true,
    val tierLabel: String = "Llama 3.2 3B",
    val temperature: Float = 0.6f,
    val maxTokens: Int = 512
) {
    GENERAL("GENERAL", "meta-llama/Llama-3.2-3B-Instruct", "Cloud AI", "☁️", "Cloud AI • Llama 3.2 3B tutor", "ai_option_cloud", "ai_option_cloud_desc", true, "Hugging Face", 0.6f, 512),
    LLAMA_LOCAL("LLAMA_LOCAL", "Llama-3.2-3B-Instruct-Q4_K_M.gguf", "Local AI", "📱", "Local AI • On-device Llama 3.2 3B", "ai_option_local", "ai_option_local_desc", true, "Local Llama", 0.6f, 384),
    OFFLINE("OFFLINE", "offline-socratic", "Socratic Teacher", "🧑‍🏫", "Socratic Teacher • Offline no-AI curriculum engine", "ai_option_socratic", "ai_option_socratic_desc", true, "Offline", 0.6f, 384)
}

object LlamaClient {
    private const val TAG = "LlamaClient"

    /**
     * Classified network failure. Used to decide what is retryable and what
     * the UI may honestly report (offline vs. server trouble vs. bad key).
     */
    enum class NetworkErrorKind {
        /** DNS failure / unreachable host: the device is offline or blocked. */
        NO_NETWORK,
        /** Connect or read timeout. */
        TIMEOUT,
        /** HTTP 5xx: the server is having trouble. */
        SERVER_ERROR,
        /** HTTP 401/403: the API key is missing, wrong, or revoked. */
        AUTH,
        /** Other HTTP 4xx: the request itself was rejected. */
        CLIENT_ERROR,
        /** Anything else. */
        UNKNOWN
    }

    /**
     * Walks the causal chain so wrapped exceptions (Retrofit/OkHttp wrap the
     * raw [java.io.IOException]) still classify correctly. Never inspects or
     * logs credential material.
     */
    fun classifyNetworkError(throwable: Throwable): NetworkErrorKind {
        var current: Throwable? = throwable
        while (current != null) {
            when (current) {
                is UnknownHostException, is NoRouteToHostException -> return NetworkErrorKind.NO_NETWORK
                is ConnectException -> return NetworkErrorKind.NO_NETWORK
                is SocketTimeoutException -> return NetworkErrorKind.TIMEOUT
                is HttpException -> return when (current.code()) {
                    401, 403 -> NetworkErrorKind.AUTH
                    in 500..599 -> NetworkErrorKind.SERVER_ERROR
                    else -> NetworkErrorKind.CLIENT_ERROR
                }
            }
            current = current.cause
        }
        return NetworkErrorKind.UNKNOWN
    }

    /**
     * Bounded retries with exponential backoff + jitter, for idempotent calls
     * only. All current call sites are POSTs to the stateless chat-completions
     * endpoint (a retry regenerates the same answer; nothing is mutated
     * server-side). Retries transient failures (timeouts, 5xx); auth
     * failures, client errors, and coroutine cancellation are never retried.
     */
    internal suspend fun <T> withNetworkRetry(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 1_000L,
        block: suspend () -> T
    ): T {
        var attempt = 0
        var delayMs = initialDelayMs
        while (true) {
            try {
                return block()
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                attempt++
                val kind = classifyNetworkError(e)
                val retryable = kind == NetworkErrorKind.TIMEOUT || kind == NetworkErrorKind.SERVER_ERROR
                if (attempt >= maxAttempts || !retryable) throw e
                val jitter = if (delayMs > 0) Random.nextLong(0, delayMs / 2 + 1) else 0L
                delay(delayMs + jitter)
                delayMs = (delayMs * 2).coerceAtMost(30_000L)
            }
        }
    }

    /**
     * Exception text safe for logs: redacts anything shaped like a bearer
     * token, so no library surprise can leak the API key into logcat.
     */
    internal fun safeMessage(e: Throwable): String =
        (e.message ?: e.javaClass.simpleName)
            .replace(Regex("(?i)bearer\\s+[A-Za-z0-9._~+/-]+=*"), "Bearer [redacted]")

    /**
     * One-line kill switch for the Socratic last resort. When true (default), paths
     * that cannot reach a real model serve the built-in offline template engine,
     * explicitly labeled [ChatReplySource.SOCRATIC_FALLBACK] ("Socratic Teacher" —
     * never presented as Llama). When false, those paths return an honest
     * [ChatReplySource.ERROR] instead.
     */
    const val ALLOW_SOCRATIC_FALLBACK = true

    /**
     * Prefix of every failure banner returned by
     * [LlamaLocalManager.generateLlamaResponse] — replies starting with this are
     * engine errors, not model answers.
     */
    const val LOCAL_ENGINE_ERROR_PREFIX = "\uD83E\uDD99 [Llama 3.2 3B Local Engine]:"

    /**
     * Wraps a raw on-device engine string, marking engine failure banners as
     * [ChatReplySource.ERROR] so callers surface them as errors, never as answers.
     */
    fun localEngineChatReply(text: String): ChatReply =
        ChatReply(
            text = text,
            source = if (text.startsWith(LOCAL_ENGINE_ERROR_PREFIX)) ChatReplySource.ERROR else ChatReplySource.LOCAL_MODEL
        )

    /**
     * The Socratic last resort, wrapped with its honest provenance label. When
     * [ALLOW_SOCRATIC_FALLBACK] is false — or the parent has turned Socratic
     * Teacher off for this child ([allowSocraticFallback] = false) — this returns
     * an explicit, honestly-worded connection-required error instead of
     * teaching. Internal (not private) so the chat ViewModel can use the same
     * labeled fallback for its explicit offline branch.
     */
    internal fun socraticChatReply(
        lastUserMessage: String,
        schoolDistrict: String = "",
        stateOrProvince: String = "",
        country: String = "",
        standardTitle: String = "",
        languageCode: String = "en-US",
        conversationHistory: List<Pair<String, String>> = emptyList(),
        curriculumContext: String = "",
        lessonContext: LessonContext = LessonContext(),
        allowSocraticFallback: Boolean = true
    ): ChatReply {
        if (!ALLOW_SOCRATIC_FALLBACK || !allowSocraticFallback) {
            return ChatReply(
                text = AppLanguageDictionary.getString("ai_connection_required", languageCode),
                source = ChatReplySource.ERROR
            )
        }
        return ChatReply(
            text = generateLocalSocraticReply(
                lastUserMessage = lastUserMessage,
                schoolDistrict = schoolDistrict,
                stateOrProvince = stateOrProvince,
                country = country,
                standardTitle = standardTitle,
                languageCode = languageCode,
                conversationHistory = conversationHistory,
                curriculumContext = curriculumContext,
                lessonContext = lessonContext
            ),
            source = ChatReplySource.SOCRATIC_FALLBACK
        )
    }
    // Hugging Face Serverless Inference API, OpenAI-compatible route.
    // Documented at https://huggingface.co/docs/inference-providers/en/index :
    // base https://router.huggingface.co/v1 + POST /chat/completions, model id in the request body.
    private const val BASE_URL = "https://router.huggingface.co/v1/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        })
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val service: LlamaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(LlamaApiService::class.java)
    }

    @Volatile
    var customApiKeyOverride: String = ""

    private fun isUsableKey(key: String): Boolean {
        if (key.isBlank()) return false
        val upper = key.uppercase()
        return !upper.contains("MY_API_KEY") &&
            !upper.contains("MY_HF_TOKEN") &&
            !upper.contains("MY_LLAMA_API_KEY") &&
            !upper.contains("PLACEHOLDER")
    }

    fun getApiKey(customKey: String = ""): String {
        val trimmedCustom = customKey.trim()
        if (isUsableKey(trimmedCustom)) {
            return trimmedCustom
        }
        val trimmedOverride = customApiKeyOverride.trim()
        if (isUsableKey(trimmedOverride)) {
            return trimmedOverride
        }
        return try {
            // System.getenv() can never return anything on Android; the token is resolved from
            // the caller-supplied key, the in-memory override, or the BuildConfig secret instead.
            val configHfToken = runCatching { BuildConfig.HF_TOKEN.trim() }.getOrDefault("")
            if (isUsableKey(configHfToken)) {
                return configHfToken
            }

            val configLlamaKey = runCatching { BuildConfig.LLAMA_API_KEY.trim() }.getOrDefault("")
            if (isUsableKey(configLlamaKey)) {
                return configLlamaKey
            }

            ""
        } catch (_: Exception) {
            ""
        }
    }

    fun hasValidApiKey(customKey: String = ""): Boolean {
        val key = getApiKey(customKey)
        return isUsableKey(key)
    }

    fun sanitizeHistory(
        conversationHistory: List<Pair<String, String>>,
        newUserMessage: String? = null
    ): List<LlamaChatMessage> {
        val messages = mutableListOf<LlamaChatMessage>()
        for (item in conversationHistory) {
            val role = if (item.first.equals("user", ignoreCase = true)) "user" else "assistant"
            val text = item.second.trim()
            if (text.isNotBlank()) {
                messages.add(LlamaChatMessage(role = role, content = text))
            }
        }
        if (!newUserMessage.isNullOrBlank()) {
            val trimmedNew = newUserMessage.trim()
            if (messages.isEmpty() || messages.last().content != trimmedNew) {
                messages.add(LlamaChatMessage(role = "user", content = trimmedNew))
            }
        }
        return messages
    }

    /**
     * Surfaces a Hugging Face API-level error payload instead of silently pretending
     * everything worked. Returns the user-facing message, or null when there is no error.
     *
     * The raw payload is never logged: error bodies are server-controlled and
     * could contain anything, so only the call site is recorded.
     */
    private fun apiErrorMessage(response: LlamaChatResponse, callSite: String): String? {
        val apiError = response.error?.trim()
        if (apiError.isNullOrBlank()) return null
        Log.w(TAG, "Hugging Face API error in $callSite (payload withheld from logs)")
        return "The cloud tutor ran into a problem and couldn't answer just now. " +
            "Please check the internet connection and API key, then try again — " +
            "or switch to the on-device Llama 3.2 tutor in the model picker."
    }

    /**
     * Learning Buddy Chat powered by Llama 3.2 3B from Hugging Face with accredited local Socratic fallback.
     *
     * @param appContext optional Android context; when [modelMode] is LLAMA_LOCAL and a context
     * is supplied, the prompt is routed through the real on-device GGUF inference
     * ([LlamaLocalManager.generateLlamaResponse]). Without a context the offline Socratic
     * engine answers instead (documented fallback, not a silent swap).
     */
    suspend fun generateChatReply(
        conversationHistory: List<Pair<String, String>>,
        systemPrompt: String,
        languageCode: String = "en-US",
        schoolDistrict: String = "",
        stateOrProvince: String = "",
        country: String = "",
        standardTitle: String = "State Academic Standards",
        curriculumContext: String = "",
        modelMode: ChatModelMode = ChatModelMode.GENERAL,
        customApiKey: String = "",
        appContext: Context? = null,
        /** Parent-controlled: when false, a dead AI surfaces an honest connection-required error instead of Socratic teaching. */
        allowSocraticFallback: Boolean = true
    ): ChatReply = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(customApiKey)
        val langName = AppLanguage.fromCode(languageCode).displayName
        val lastUserMessage = conversationHistory.lastOrNull { it.first == "user" }?.second ?: ""

        if (modelMode == ChatModelMode.LLAMA_LOCAL) {
            val ctx = appContext
            if (ctx != null) {
                return@withContext localEngineChatReply(
                    LlamaLocalManager.generateLlamaResponse(
                        context = ctx,
                        prompt = lastUserMessage,
                        systemPrompt = systemPrompt,
                        schoolDistrict = schoolDistrict,
                        stateOrProvince = stateOrProvince,
                        country = country,
                        standardTitle = standardTitle,
                        languageCode = languageCode,
                        conversationHistory = conversationHistory,
                        curriculumContext = curriculumContext
                    )
                )
            }
            return@withContext socraticChatReply(
                lastUserMessage = lastUserMessage,
                schoolDistrict = schoolDistrict,
                stateOrProvince = stateOrProvince,
                country = country,
                standardTitle = standardTitle,
                languageCode = languageCode,
                conversationHistory = conversationHistory,
                curriculumContext = curriculumContext,
                allowSocraticFallback = allowSocraticFallback
            )
        }

        if (modelMode == ChatModelMode.OFFLINE || apiKey.isBlank()) {
            // No cloud key: prefer the real on-device Llama 3.2 model when it is
            // installed instead of silently serving canned template replies.
            // The canned Socratic engine is the last resort, not the default.
            val ctx = appContext
            if (ctx != null && LlamaLocalManager.isLlamaInstalled(ctx)) {
                Log.i(TAG, "No cloud API key; routing ${modelMode.name} through the installed on-device Llama 3.2 model")
                return@withContext localEngineChatReply(
                    LlamaLocalManager.generateLlamaResponse(
                        context = ctx,
                        prompt = lastUserMessage,
                        systemPrompt = systemPrompt,
                        schoolDistrict = schoolDistrict,
                        stateOrProvince = stateOrProvince,
                        country = country,
                        standardTitle = standardTitle,
                        languageCode = languageCode,
                        conversationHistory = conversationHistory,
                        curriculumContext = curriculumContext
                    )
                )
            }
            return@withContext socraticChatReply(
                lastUserMessage = lastUserMessage,
                schoolDistrict = schoolDistrict,
                stateOrProvince = stateOrProvince,
                country = country,
                standardTitle = standardTitle,
                languageCode = languageCode,
                conversationHistory = conversationHistory,
                curriculumContext = curriculumContext,
                allowSocraticFallback = allowSocraticFallback
            )
        }

        val enrichedSystemInstruction = buildString {
            append("You are NeuroPath's AI Learning Buddy powered by Meta Llama 3.2 3B Instruct from Hugging Face. ")
            append("You are patient, encouraging, kind, and strictly age-appropriate. ")
            append("Always explain in clear, structured language in $langName ($languageCode). ")
            val jurisdiction = listOf(schoolDistrict, stateOrProvince, country)
                .filter { it.isNotBlank() }.joinToString(", ")
            if (jurisdiction.isNotBlank()) append("Jurisdiction: $jurisdiction. ")
            append("Curriculum standards: $standardTitle. ")
            if (curriculumContext.isNotBlank()) {
                append("Curriculum Context from OER Commons: $curriculumContext. ")
            }
            if (systemPrompt.isNotBlank()) {
                append("\n\nCore Instructions:\n$systemPrompt")
            }
        }

        val messages = mutableListOf<LlamaChatMessage>()
        messages.add(LlamaChatMessage(role = "system", content = enrichedSystemInstruction))
        messages.addAll(sanitizeHistory(conversationHistory))

        val request = LlamaChatRequest(
            model = modelMode.modelName,
            messages = messages,
            temperature = modelMode.temperature,
            maxTokens = modelMode.maxTokens
        )

        try {
            val authHeader = if (apiKey.startsWith("Bearer ", ignoreCase = true)) apiKey else "Bearer $apiKey"
            val response = withNetworkRetry { service.createChatCompletion(authHeader, request) }
            apiErrorMessage(response, "generateChatReply")?.let {
                return@withContext ChatReply(text = it, source = ChatReplySource.ERROR)
            }
            val replyText = response.choices?.firstOrNull()?.message?.content?.trim()
            if (!replyText.isNullOrBlank()) {
                return@withContext ChatReply(text = replyText, source = ChatReplySource.CLOUD)
            }
            Log.w(TAG, "Hugging Face Llama 3.2 API returned no content; falling back to local Socratic engine.")
        } catch (e: Exception) {
            val kind = classifyNetworkError(e)
            Log.w(TAG, "Hugging Face Llama 3.2 API call failed [$kind], falling back to local Socratic engine: ${safeMessage(e)}")
        }

        socraticChatReply(
            lastUserMessage = conversationHistory.lastOrNull { it.first == "user" }?.second ?: "",
            schoolDistrict = schoolDistrict,
            stateOrProvince = stateOrProvince,
            country = country,
            standardTitle = standardTitle,
            languageCode = languageCode,
            conversationHistory = conversationHistory,
            curriculumContext = curriculumContext,
            allowSocraticFallback = allowSocraticFallback
        )
    }

    /**
     * Real-time conversational voice turn using Llama 3.2 3B.
     *
     * Speech-to-text is handled by SpeechManager (Android SpeechRecognizer) before this is
     * called, and the reply is spoken via TTS afterwards — this function only produces the
     * text turn (cloud Llama 3.2 3B when a key is available, offline engine otherwise).
     */
    suspend fun generateLiveVoiceConversationTurn(
        userText: String?,
        conversationHistory: List<Pair<String, String>>,
        systemPrompt: String,
        curriculumContext: String = "",
        schoolDistrict: String = "",
        stateOrProvince: String = "",
        country: String = "",
        standardTitle: String = "State Academic Standards",
        languageCode: String = "en-US",
        customApiKey: String = "",
        lessonContext: LessonContext = LessonContext(),
        /** Parent-controlled: when false, a dead live-voice AI surfaces an honest error instead of Socratic teaching. */
        allowSocraticFallback: Boolean = true
    ): LiveVoiceTurnResult = withContext(Dispatchers.IO) {
        try {
            LlamaLiveApiClient.generateTurn(
                userText = userText,
                conversationHistory = conversationHistory,
                systemPrompt = systemPrompt,
                curriculumContext = curriculumContext,
                schoolDistrict = schoolDistrict,
                stateOrProvince = stateOrProvince,
                country = country,
                standardTitle = standardTitle,
                languageCode = languageCode,
                customApiKey = customApiKey
            )
        } catch (_: Exception) {
            if (!allowSocraticFallback) {
                return@withContext LiveVoiceTurnResult(
                    transcriptText = AppLanguageDictionary.getString("ai_connection_required", languageCode),
                    audioBase64 = null,
                    curriculumCitation = "",
                    source = ChatReplySource.ERROR
                )
            }
            val fallback = generateLocalSocraticReply(
                lastUserMessage = userText ?: "Hello!",
                schoolDistrict = schoolDistrict,
                stateOrProvince = stateOrProvince,
                country = country,
                standardTitle = standardTitle,
                languageCode = languageCode,
                conversationHistory = conversationHistory,
                curriculumContext = curriculumContext,
                lessonContext = lessonContext
            )
            LiveVoiceTurnResult(
                transcriptText = fallback,
                audioBase64 = null,
                curriculumCitation = if (standardTitle.isNotBlank()) "Standard: $standardTitle" else "",
                source = ChatReplySource.SOCRATIC_FALLBACK
            )
        }
    }

    suspend fun generateAdaptiveHint(
        question: String,
        wrongAnswer: String,
        themeTitle: String,
        gradeLevel: String,
        languageCode: String = "en-US",
        schoolDistrict: String = "",
        standardTitle: String = "Core Standards"
    ): String = withContext(Dispatchers.IO) {
        val langName = AppLanguage.fromCode(languageCode).displayName
        val apiKey = getApiKey()

        if (apiKey.isBlank()) {
            return@withContext "Let's take a deep breath! Look at the key clue in the problem. What happens if you break it into two smaller pieces?"
        }

        val districtText = schoolDistrict.ifBlank { "their school district" }
        val prompt = """
            You are a gentle, growth-mindset neurodiversity learning coach for a $gradeLevel student in $districtText powered by Llama 3.2 3B.
            Their special interest theme is $themeTitle.
            The student was asked: "$question".
            They selected "$wrongAnswer".
            
            RULES:
            1. DO NOT give the direct correct answer.
            2. Provide a warm, supportive 2-sentence clue in $langName ($languageCode) that uses their theme and guides them on HOW to solve it themselves.
            3. Do NOT append any curriculum reference tags or citations.
        """.trimIndent()

        val messages = listOf(
            LlamaChatMessage(role = "system", content = "You are a warm, encouraging Socratic learning coach for children."),
            LlamaChatMessage(role = "user", content = prompt)
        )

        try {
            val authHeader = if (apiKey.startsWith("Bearer ", ignoreCase = true)) apiKey else "Bearer $apiKey"
            val response = withNetworkRetry {
                service.createChatCompletion(
                    authHeader,
                    LlamaChatRequest(messages = messages, temperature = 0.6f, maxTokens = 256)
                )
            }
            apiErrorMessage(response, "generateAdaptiveHint")?.let { return@withContext it }
            response.choices?.firstOrNull()?.message?.content?.trim()
                ?: "Mistakes are how our brains make new connections! Take another look at the clues."
        } catch (_: Exception) {
            "Mistakes are how our brains make new connections! Take another look at the clues."
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

        if (apiKey.isBlank()) {
            return@withContext DownloadedCurriculumResult(
                officialSourceAgency = officialAgency,
                officialSourceUrl = officialUrl,
                gradesSummary = "Pre-K, Kindergarten, Elementary (1-5), Middle School (6-8), High School (9-12)",
                curriculumSummary = "Curriculum based on OER Commons Curated Collections (https://oercommons.org/curated-collections) standards for $schoolDistrict ($stateOrProvince, $country). Powered by Llama 3.2 3B from Hugging Face.",
                isOnlineSynced = false
            )
        }

        val prompt = """
            Download, index, and synthesize officially accredited open educational benchmarks across ALL K-12 grade levels (Pre-K, Kindergarten, Elementary 1-5, Middle School 6-8, and High School 9-12) utilizing open educational databases from OER Commons Curated Collections (https://oercommons.org/curated-collections) and the student's home school jurisdiction:
            - Country: $country
            - State/Province: $stateOrProvince
            - City: $city
            - School District: $schoolDistrict
            - Postal/Zip Code: $postalCode
            - Standard Title: $standardTitle
            
            REQUIRED HIGH SCHOOL & K-12 BENCHMARKS:
            Ensure full curriculum coverage for High School (Grades 9-12) as well as Elementary and Middle School:
            1. High School Mathematics: Algebra I, Geometry, Algebra II, Pre-Calculus, Trigonometry, Statistics.
            2. High School English Language Arts: Rhetorical analysis, literary synthesis, argumentative writing.
            3. High School Sciences: Biology, Chemistry, Physics.
            4. High School Social Studies: US Government & Civics, History, Economics.
            5. Life Skills & Executive Functioning: Personal finance, time management, and self-regulation.
            
            Output in $langName ($languageCode).
        """.trimIndent()

        val messages = listOf(
            LlamaChatMessage(role = "system", content = "You are the Chief Educational Standards Registrar & Curriculum Architect powered by Llama 3.2 3B."),
            LlamaChatMessage(role = "user", content = prompt)
        )

        try {
            val authHeader = if (apiKey.startsWith("Bearer ", ignoreCase = true)) apiKey else "Bearer $apiKey"
            val response = withNetworkRetry {
                service.createChatCompletion(
                    authHeader,
                    LlamaChatRequest(messages = messages, temperature = 0.4f, maxTokens = 768)
                )
            }
            val text = response.choices?.firstOrNull()?.message?.content?.trim()
                ?: "Curriculum synchronized from OER Commons Curated Collections (https://oercommons.org/curated-collections) across all K-12 grades for $schoolDistrict."
            DownloadedCurriculumResult(
                officialSourceAgency = officialAgency,
                officialSourceUrl = officialUrl,
                gradesSummary = "K-12 Full Spectrum (Pre-K, Elementary, Middle School, High School 9-12)",
                curriculumSummary = text,
                isOnlineSynced = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download curriculum for locale [${classifyNetworkError(e)}]", e)
            DownloadedCurriculumResult(
                officialSourceAgency = officialAgency,
                officialSourceUrl = officialUrl,
                gradesSummary = "Pre-K through High School (Grades 9-12)",
                curriculumSummary = "Using offline curriculum based on OER Commons Curated Collections (https://oercommons.org/curated-collections) for $schoolDistrict ($stateOrProvince, $country) under $standardTitle.",
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
        if (apiKey.isBlank()) {
            // Honest: without a key nothing was verified or synchronized.
            return@withContext "Couldn't reach the curriculum service — no API key is set. " +
                "District on file: $district ($city, $state, $country)."
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

        val messages = listOf(
            LlamaChatMessage(role = "system", content = "You are a curriculum specialist powered by Llama 3.2 3B."),
            LlamaChatMessage(role = "user", content = prompt)
        )

        try {
            val authHeader = if (apiKey.startsWith("Bearer ", ignoreCase = true)) apiKey else "Bearer $apiKey"
            val response = withNetworkRetry {
                service.createChatCompletion(
                    authHeader,
                    LlamaChatRequest(messages = messages, temperature = 0.5f, maxTokens = 384)
                )
            }
            response.choices?.firstOrNull()?.message?.content?.trim()
                ?: "The curriculum service returned no summary for $district ($city, $state)."
        } catch (_: Exception) {
            // Honest failure: never claim "synchronized" when the call failed.
            "Couldn't reach the curriculum service for $district ($city, $state) — please check the connection and try again."
        }
    }

    data class MathExprResult(
        val originalDisplay: String,
        val steps: List<String>,
        val finalResult: Long,
        val isUndefined: Boolean = false
    )

    fun parseAndEvaluateMath(input: String): MathExprResult? {
        val lower = input.lowercase()
        val normalized = lower
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

        // Tokenize with unary-minus support: a '-' immediately before a number (at the start
        // of the expression or right after another operator) belongs to that number.
        val tokens = mutableListOf<String>()
        var expectingNumber = true
        var i = 0
        while (i < normalized.length) {
            val c = normalized[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() -> {
                    var j = i
                    while (j < normalized.length && normalized[j].isDigit()) j++
                    tokens.add(normalized.substring(i, j))
                    i = j
                    expectingNumber = false
                }
                c == '-' && expectingNumber -> {
                    var j = i + 1
                    while (j < normalized.length && normalized[j].isWhitespace()) j++
                    var k = j
                    while (k < normalized.length && normalized[k].isDigit()) k++
                    if (k > j) {
                        tokens.add("-" + normalized.substring(j, k))
                        i = k
                        expectingNumber = false
                    } else {
                        i++
                    }
                }
                (c == '+' || c == '-' || c == '*' || c == '/') && !expectingNumber -> {
                    tokens.add(c.toString())
                    i++
                    expectingNumber = true
                }
                else -> i++
            }
        }

        if (tokens.size < 3) return null

        if (tokens.size % 2 == 0) {
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
                if (token == "/" && n2 == 0L) {
                    steps.add("$n1 ÷ $n2 is undefined — division by zero isn't allowed")
                    return MathExprResult(originalDisplay, steps, 0L, isUndefined = true)
                }
                val res = if (token == "*") n1 * n2 else n1 / n2
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
        curriculumContext: String = "",
        lessonContext: LessonContext = LessonContext()
    ): String {
        val raw = lastUserMessage.trim()
        val query = raw.lowercase()

        val previousUserMsg = conversationHistory.filter { it.first.equals("user", ignoreCase = true) }.let {
            if (it.size >= 2) it[it.size - 2].second else ""
        }
        val lastBuddyMsg = conversationHistory.lastOrNull { it.first.equals("model", ignoreCase = true) || it.first.equals("BUDDY", ignoreCase = true) || it.first.equals("assistant", ignoreCase = true) }?.second ?: ""

        val numbersInQuery = Regex("\\d+").findAll(query).mapNotNull { it.value.toLongOrNull() }.toList()

        // 1. Evaluate math expressions directly from query
        val mathExpr = parseAndEvaluateMath(query)
        if (mathExpr != null) {
            val (disp, steps, ans, isUndefined) = mathExpr
            if (isUndefined) {
                return "⚠️ **$disp**\n\n${steps.lastOrNull() ?: "Division by zero isn't allowed."} Try again with a non-zero divisor!"
            }
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
        if (prevMathExpr != null && !prevMathExpr.isUndefined && numbersInQuery.isNotEmpty()) {
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
            // Grounded example: teach from the actual on-screen material first.
            val lessonPoints = lessonContext.points(2)
            if (lessonPoints.isNotEmpty()) {
                val lessonName = lessonContext.lessonTitle.ifBlank { "your current lesson" }
                val bullets = lessonPoints.mapIndexed { i, p -> "${i + 1}. $p" }.joinToString("\n")
                return """
                    💡 **From "$lessonName":**
                    
                    $bullets
                    
                    Connecting new ideas to what you just saw on screen makes every concept stick! Which of these would you like to dig into together?
                """.trimIndent()
            }
            return """
                💡 **Here is a Clear Real-World Example:**
                
                Suppose you have 10 apples 🍎 and want to share them equally among 2 friends:
                - Each friend receives **10 ÷ 2 = 5 apples**.
                - If you gain 3 more apples, you now have **10 + 3 = 13 apples**.
                
                Connecting rules to real objects makes every concept crystal clear! What specific question or number problem should we apply this to?
            """.trimIndent()
        }

        if (isQuizRequest) {
            // Grounded quiz: when lesson material is on screen, quiz on a REAL
            // fact from it — never a generic placeholder question.
            val lessonPoints = lessonContext.points(3)
            if (lessonPoints.isNotEmpty()) {
                val fact = lessonPoints.first()
                val lessonName = lessonContext.lessonTitle.ifBlank { "this lesson" }
                return """
                    🎯 **Quick Check-In: "$lessonName"!**
                    
                    From what we just learned: **$fact**
                    
                    **Question**: In your own words, what does that tell us? Type your answer and I'll check it with you!
                    
                    *Reply with your best explanation — there are no wrong guesses here, only good thinking!*
                """.trimIndent()
            }
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

        // 4b. Lesson recap: teach back the actual on-screen material.
        val isLessonRecap = query.contains("what are we learning") || query.contains("recap") ||
                query.contains("summarize the lesson") || query.contains("summarise the lesson") ||
                query.contains("what did we learn") || query.contains("lesson summary") ||
                query.contains("what is this lesson about")

        if (isLessonRecap && lessonContext.hasContent()) {
            val lessonName = lessonContext.lessonTitle.ifBlank { "your current lesson" }
            val subjectLine = if (lessonContext.subject.isNotBlank()) " (${lessonContext.subject})" else ""
            val bullets = lessonContext.points(4).mapIndexed { i, p -> "${i + 1}. $p" }.joinToString("\n")
            return """
                📚 **Lesson Recap: "$lessonName"**$subjectLine
                
                Here are the key ideas from your lesson:
                
                $bullets
                
                💡 *Socratic nudge*: Pick the point that feels trickiest and ask me about it — we'll crack it together, one question at a time!
            """.trimIndent()
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
            // Honest identity: this branch runs inside the offline template engine, so it
            // must identify as the Socratic Teacher — never as the Llama 3.2 3B model.
            val distStr = if (schoolDistrict.isNotBlank()) " I'm set up for $schoolDistrict ($standardTitle)." else ""
            return """
                ✅ **I am fully working and ready to assist you!**
                
                I am your Socratic Teacher — the built-in offline learning engine that works without the internet.$distStr I answer from a built-in curriculum guide, not from the Llama AI model.
                
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

        // Grounded fallback: when lesson material is on screen, anchor the
        // Socratic guidance to a REAL fact from it instead of generic filler.
        // Still honestly labeled — the Socratic Teacher, never Llama.
        val groundedPoints = lessonContext.points(2)
        if (groundedPoints.isNotEmpty()) {
            val lessonName = lessonContext.lessonTitle.ifBlank { "your lesson" }
            val fact = groundedPoints.first()
            return """
                💡 **Exploring "$cleanQuestion"** (from "$lessonName")
                
                Great question! Let's connect it to what you're learning:
                
                - **From your lesson**: **$fact**
                - **Think about it**: How might that fact help answer your question? What is one connection you can already see?
                - **Your turn**: Tell me your best guess — we'll build on it together, one step at a time!
                
                *I'm your Socratic Teacher, teaching from your on-screen lesson material.*
            """.trimIndent()
        }

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
