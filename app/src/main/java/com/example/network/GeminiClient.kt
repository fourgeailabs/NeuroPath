package com.example.network

import com.example.BuildConfig
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

    suspend fun generateChatReply(
        conversationHistory: List<Pair<String, String>>, // (role "user" or "model", text)
        systemPrompt: String,
        useThinkingPro: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Friendly offline fallback response
            return@withContext "I'm right here with you! You're doing a fantastic job learning today. Let's keep exploring step by step!"
        }

        val model = if (useThinkingPro) "gemini-3.1-pro-preview" else "gemini-3.5-flash"

        val contents = conversationHistory.map { (role, text) ->
            GeminiContent(
                role = role,
                parts = listOf(GeminiPart(text = text))
            )
        }

        val request = GeminiGenerateRequest(
            contents = contents,
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            ),
            generationConfig = if (useThinkingPro) {
                GeminiGenerationConfig(
                    temperature = 0.7f,
                    thinkingConfig = GeminiThinkingConfig(thinkingLevel = "HIGH")
                )
            } else {
                GeminiGenerationConfig(
                    temperature = 0.7f
                )
            }
        )

        try {
            val response = service.generateContent(model, apiKey, request)
            val candidateText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            candidateText ?: "You did great! Let's continue discovering new things together."
        } catch (e: Exception) {
            "That's a thoughtful question! Even when offline, remember to take deep breaths and celebrate every small step of progress you make!"
        }
    }

    suspend fun generateAdaptiveHint(
        question: String,
        wrongAnswer: String,
        themeTitle: String,
        gradeLevel: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Take a breath! Let's look at the question again slowly. You're building your brain power with every try!"
        }

        val prompt = """
            You are a gentle, growth-mindset neurodiversity learning coach for a $gradeLevel student.
            Their special interest theme is $themeTitle.
            The student was asked: "$question".
            They selected "$wrongAnswer".
            Provide a warm, supportive 2-sentence hint that uses their theme, without giving away the direct answer, encouraging them to try again.
        """.trimIndent()

        val request = GeminiGenerateRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.7f)
        )

        try {
            val response = service.generateContent("gemini-3.5-flash", apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Mistakes are how our brains make new connections! Take another look at the clues."
        } catch (e: Exception) {
            "Mistakes are how our brains make new connections! Take another look at the clues."
        }
    }
}
