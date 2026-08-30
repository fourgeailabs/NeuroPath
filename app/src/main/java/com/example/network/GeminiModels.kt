package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiGenerateRequest(
    val contents: List<GeminiContent>,
    @Json(name = "generation_config") val generationConfig: GeminiGenerationConfig? = null,
    @Json(name = "system_instruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null,
    @Json(name = "inline_data") val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    @Json(name = "mime_type") val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float? = null,
    @Json(name = "top_p") val topP: Float? = null,
    @Json(name = "top_k") val topK: Int? = null,
    @Json(name = "thinking_config") val thinkingConfig: GeminiThinkingConfig? = null,
    @Json(name = "response_modalities") val responseModalities: List<String>? = null,
    @Json(name = "speech_config") val speechConfig: GeminiSpeechConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiSpeechConfig(
    @Json(name = "voice_config") val voiceConfig: GeminiVoiceConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiVoiceConfig(
    @Json(name = "prebuilt_voice_config") val prebuiltVoiceConfig: GeminiPrebuiltVoiceConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPrebuiltVoiceConfig(
    @Json(name = "voice_name") val voiceName: String
)

@JsonClass(generateAdapter = true)
data class GeminiThinkingConfig(
    @Json(name = "thinking_level") val thinkingLevel: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateResponse(
    val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null,
    @Json(name = "finish_reason") val finishReason: String? = null
)

data class DownloadedCurriculumResult(
    val officialSourceAgency: String,
    val officialSourceUrl: String,
    val gradesSummary: String,
    val curriculumSummary: String,
    val isOnlineSynced: Boolean = true
)
