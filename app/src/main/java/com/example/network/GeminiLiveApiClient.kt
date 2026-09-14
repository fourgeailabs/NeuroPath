package com.example.network

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Minimal raw-WebSocket Gemini Live API client.
 * Gemini Live uses a stateful WebSocket rather than generateContent REST calls.
 */
object GeminiLiveApiClient {
    private const val MODEL = "gemini-3.1-flash-live-preview"
    private const val WS_ENDPOINT =
        "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    suspend fun generateTurn(
        apiKey: String,
        userVoiceAudio: ByteArray?,
        userText: String?,
        conversationHistory: List<Pair<String, String>>,
        systemPrompt: String,
        curriculumContext: String,
        schoolDistrict: String,
        stateOrProvince: String,
        country: String,
        standardTitle: String,
        languageCode: String
    ): LiveVoiceTurnResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext LiveVoiceTurnResult("Voice service unavailable.")

        val history = conversationHistory.takeLast(6).joinToString("\n") { (role, text) ->
            "${role.uppercase()}: $text"
        }
        val instructions = """
            $systemPrompt
            You are NeuroPath's real-time Learning Buddy.
            Respond naturally and briefly for spoken conversation.
            Respond in $languageCode.
            Jurisdiction: $schoolDistrict, $stateOrProvince, $country.
            Standards: $standardTitle.
            Curriculum context: ${curriculumContext.ifBlank { "Use the student's current learning context." }}
            Recent conversation:
            ${history.ifBlank { "No prior turns." }}
        """.trimIndent()

        val result = withTimeoutOrNull(45_000L) {
            suspendCoroutine { continuation ->
                var resumed = false
                val audioChunks = mutableListOf<ByteArray>()
                val transcript = StringBuilder()
                var inputTranscript = StringBuilder()
                var socket: WebSocket? = null

                fun finish(result: LiveVoiceTurnResult) {
                    if (!resumed) {
                        resumed = true
                        socket?.close(1000, "turn complete")
                        continuation.resume(result)
                    }
                }

                val request = Request.Builder()
                    .url("$WS_ENDPOINT?key=$apiKey")
                    .build()

                socket = client.newWebSocket(request, object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        val setup = JSONObject()
                            .put("model", "models/$MODEL")
                            .put("generationConfig", JSONObject()
                                .put("responseModalities", org.json.JSONArray().put("AUDIO"))
                                .put("inputAudioTranscription", JSONObject())
                                .put("outputAudioTranscription", JSONObject()))
                            .put("systemInstruction", JSONObject()
                                .put("parts", org.json.JSONArray().put(JSONObject().put("text", instructions))))

                        webSocket.send(JSONObject().put("setup", setup).toString())
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        try {
                            val root = JSONObject(text)
                            if (root.has("error")) {
                                finish(LiveVoiceTurnResult("Live voice service returned an error."))
                                return
                            }
                            if (root.has("setupComplete")) {
                                if (userVoiceAudio != null) {
                                    val pcm = wavToPcm16(userVoiceAudio)
                                    val chunkSize = 3200 // 100 ms at 16 kHz mono PCM16
                                    var offset = 0
                                    while (offset < pcm.size) {
                                        val end = minOf(offset + chunkSize, pcm.size)
                                        val chunk = pcm.copyOfRange(offset, end)
                                        val blob = JSONObject()
                                            .put("data", Base64.encodeToString(chunk, Base64.NO_WRAP))
                                            .put("mimeType", "audio/pcm;rate=16000")
                                        webSocket.send(JSONObject()
                                            .put("realtimeInput", JSONObject().put("audio", blob))
                                            .toString())
                                        offset = end
                                    }
                                    webSocket.send(JSONObject()
                                        .put("realtimeInput", JSONObject().put("audioStreamEnd", true))
                                        .toString())
                                } else {
                                    webSocket.send(JSONObject()
                                        .put("realtimeInput", JSONObject().put("text", userText ?: "Hello buddy!"))
                                        .toString())
                                }
                                return
                            }

                            val server = root.optJSONObject("serverContent") ?: return
                            server.optJSONObject("inputTranscription")?.optString("text")?.let {
                                if (it.isNotBlank()) inputTranscript.append(it)
                            }
                            server.optJSONObject("outputTranscription")?.optString("text")?.let {
                                if (it.isNotBlank()) transcript.append(it)
                            }

                            val turn = server.optJSONObject("modelTurn")
                            val parts = turn?.optJSONArray("parts")
                            if (parts != null) {
                                for (i in 0 until parts.length()) {
                                    val inline = parts.optJSONObject(i)?.optJSONObject("inlineData")
                                    val data = inline?.optString("data")
                                    if (!data.isNullOrBlank()) {
                                        audioChunks.add(Base64.decode(data, Base64.DEFAULT))
                                    }
                                    val partText = parts.optJSONObject(i)?.optString("text")
                                    if (!partText.isNullOrBlank()) transcript.append(partText)
                                }
                            }

                            if (server.optBoolean("turnComplete", false)) {
                                val audio = if (audioChunks.isEmpty()) null else {
                                    val merged = ByteArray(audioChunks.sumOf { it.size })
                                    var pos = 0
                                    audioChunks.forEach { chunk ->
                                        chunk.copyInto(merged, pos)
                                        pos += chunk.size
                                    }
                                    Base64.encodeToString(merged, Base64.NO_WRAP)
                                }
                                val spoken = transcript.toString().trim()
                                val finalText = spoken.ifBlank {
                                    inputTranscript.toString().trim().ifBlank { "Let's explore that together." }
                                }
                                finish(LiveVoiceTurnResult(finalText, audio, ""))
                            }
                        } catch (_: Exception) {
                            finish(LiveVoiceTurnResult("I had trouble completing the live voice turn."))
                        }
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        finish(LiveVoiceTurnResult("Live voice is temporarily unavailable."))
                    }
                })
            }
        }

        result ?: LiveVoiceTurnResult("Live voice timed out. Please try again.")
    }

    private fun wavToPcm16(audio: ByteArray): ByteArray {
        if (audio.size < 44 || audio[0].toInt().toChar() != 'R' || audio[8].toInt().toChar() != 'W') {
            return audio
        }
        return audio.copyOfRange(44, audio.size)
    }
}
