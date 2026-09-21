package com.fourgeailabs.neuropath.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Conversational turn producer for the Learning Buddy live-voice flow.
 *
 * What this actually does: it generates a single text reply turn. Audio input is captured
 * and transcribed by [com.fourgeailabs.neuropath.speech.SpeechManager] (Android SpeechRecognizer) and the
 * reply is spoken back through Android TTS — there is no realtime voice WebSocket in this
 * build, so this client carries no WebSocket scaffolding and no unused parameters.
 * The text turn uses cloud Llama 3.2 3B when an API key is available, otherwise the
 * offline Socratic engine.
 */
object LlamaLiveApiClient {
    private const val TAG = "LlamaLiveApiClient"

    suspend fun generateTurn(
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
        val promptText = userText?.takeIf { it.isNotBlank() } ?: "Hello Learning Buddy!"
        val history = conversationHistory.takeLast(6)
        val voiceSystemPrompt = buildString {
            append(systemPrompt.trim())
            append("\nRespond naturally and briefly for spoken conversation.")
            append("\nJurisdiction: $schoolDistrict, $stateOrProvince, $country.")
            append("\nStandards: $standardTitle.")
            append("\nCurriculum context: ${curriculumContext.ifBlank { "Use the student's current learning context." }}")
        }
        val reply = LlamaClient.generateChatReply(
            conversationHistory = history + ("user" to promptText),
            systemPrompt = voiceSystemPrompt,
            languageCode = languageCode,
            schoolDistrict = schoolDistrict,
            stateOrProvince = stateOrProvince,
            country = country,
            standardTitle = standardTitle,
            curriculumContext = curriculumContext,
            modelMode = ChatModelMode.GENERAL,
            customApiKey = customApiKey
        )
        Log.i(
            TAG,
            "Live voice turn produced via " +
                if (LlamaClient.hasValidApiKey(customApiKey)) "cloud Llama 3.2 3B" else "offline Socratic engine"
        )
        LiveVoiceTurnResult(
            transcriptText = reply,
            audioBase64 = null,
            curriculumCitation = if (standardTitle.isNotBlank()) "Aligned with $standardTitle" else ""
        )
    }
}
