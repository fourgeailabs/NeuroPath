package com.fourgeailabs.neuropath.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LlamaChatRequest(
    val model: String = "meta-llama/Llama-3.2-3B-Instruct",
    val messages: List<LlamaChatMessage>,
    val temperature: Float? = 0.6f,
    @param:Json(name = "max_tokens") val maxTokens: Int? = 512,
    @param:Json(name = "top_p") val topP: Float? = null,
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
    @param:Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class LlamaGenerateRequest(
    val inputs: String,
    val parameters: LlamaParameters? = null
)

@JsonClass(generateAdapter = true)
data class LlamaParameters(
    @param:Json(name = "max_new_tokens") val maxNewTokens: Int = 384,
    val temperature: Float = 0.6f,
    @param:Json(name = "top_p") val topP: Float = 0.9f
)

@JsonClass(generateAdapter = true)
data class LlamaInferenceItem(
    @param:Json(name = "generated_text") val generatedText: String? = null
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
    val curriculumCitation: String = "",
    val source: ChatReplySource
)

/**
 * The educational material currently on screen, handed to the offline Socratic
 * engine so it can genuinely teach from the lesson instead of offering only
 * generic guidance. Key points are short, verbatim facts pulled from the
 * visible lesson content (step titles, key sentences, fun facts) — never
 * invented. The engine still identifies as the Offline Buddy and never claims
 * to be the Llama model.
 */
data class LessonContext(
    val lessonTitle: String = "",
    val subject: String = "",
    val keyPoints: List<String> = emptyList()
) {
    /** True when there is real lesson material to teach from. */
    fun hasContent(): Boolean =
        lessonTitle.isNotBlank() || keyPoints.any { it.isNotBlank() }

    /** The non-blank key points, capped so templates stay readable. */
    fun points(limit: Int = 4): List<String> =
        keyPoints.filter { it.isNotBlank() }.take(limit)
}

/**
 * Where a chat reply actually came from. This is the honesty label behind the
 * standing no-fake-data rule: the UI and chat history must never present a
 * [SOCRATIC_FALLBACK] or [ERROR] reply as a Llama model answer.
 */
enum class ChatReplySource {
    /** Answered by the Hugging Face cloud Llama 3.2 3B API. */
    CLOUD,
    /** Answered by real on-device GGUF inference. */
    LOCAL_MODEL,
    /** Answered by the built-in offline template engine ("Offline Buddy") — never Llama. */
    SOCRATIC_FALLBACK,
    /** The request failed; [ChatReply.text] is a user-facing error, not an answer. */
    ERROR
}

/**
 * A chat reply with its true provenance attached.
 *
 * Callers must surface [source] honestly: never save or display a
 * [ChatReplySource.SOCRATIC_FALLBACK] or [ChatReplySource.ERROR] reply as if a
 * Llama model produced it. To drop the Socratic last resort entirely, flip
 * [com.fourgeailabs.neuropath.network.LlamaClient.ALLOW_SOCRATIC_FALLBACK] to false.
 */
data class ChatReply(
    val text: String,
    val source: ChatReplySource
)

/**
 * A model answer is usable as generated content (daily quote, story idea, ...)
 * only when it really came from a model. Socratic template echoes and error
 * text must never be served as if a model generated them.
 */
fun ChatReply.isUsableModelAnswer(): Boolean =
    text.isNotBlank() &&
        (source == ChatReplySource.CLOUD || source == ChatReplySource.LOCAL_MODEL)

/**
 * The [ChatModelMode] that must be recorded for a reply of this source. A
 * Socratic fallback is always recorded as OFFLINE ("offline-socratic") — it can
 * never be stored carrying the originally requested Llama model label.
 */
fun ChatReplySource.recordedModel(requested: ChatModelMode): ChatModelMode =
    when (this) {
        ChatReplySource.LOCAL_MODEL -> ChatModelMode.LLAMA_LOCAL
        ChatReplySource.SOCRATIC_FALLBACK -> ChatModelMode.OFFLINE
        else -> requested
    }
