package com.fourgeailabs.neuropath.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LlamaChatRequest(
    val model: String = "meta-llama/Llama-3.2-3B-Instruct",
    val messages: List<LlamaChatMessage>,
    val temperature: Float? = 0.6f,
    @Json(name = "max_tokens") val maxTokens: Int? = 512,
    @Json(name = "top_p") val topP: Float? = null,
    val stream: Boolean = false
)

@JsonClass(generateAdapter = true)
data class LlamaChatMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class LlamaChatResponse(
    val id: String? = null,
    val choices: List<LlamaChatChoice>? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class LlamaChatChoice(
    val index: Int? = null,
    val message: LlamaChatMessage? = null,
    @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class LlamaGenerateRequest(
    val inputs: String,
    val parameters: LlamaParameters? = null
)

@JsonClass(generateAdapter = true)
data class LlamaParameters(
    @Json(name = "max_new_tokens") val maxNewTokens: Int = 384,
    val temperature: Float = 0.6f,
    @Json(name = "top_p") val topP: Float = 0.9f
)

@JsonClass(generateAdapter = true)
data class LlamaInferenceItem(
    @Json(name = "generated_text") val generatedText: String? = null
)

data class DownloadedCurriculumResult(
    val officialSourceAgency: String,
    val officialSourceUrl: String,
    val gradesSummary: String,
    val curriculumSummary: String,
    val isOnlineSynced: Boolean = false
)

data class LiveVoiceTurnResult(
    val transcriptText: String,
    val audioBase64: String? = null,
    val curriculumCitation: String = ""
)
