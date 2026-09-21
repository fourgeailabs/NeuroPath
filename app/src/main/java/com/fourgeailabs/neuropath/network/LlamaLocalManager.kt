package com.fourgeailabs.neuropath.network

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.fourgeailabs.neuropath.BuildConfig
import dev.ffmpegkit.llama.Llama
import dev.ffmpegkit.llama.LlamaConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

sealed class LlamaDownloadState {
    object NotInstalled : LlamaDownloadState()
    data class Downloading(val progress: Float, val bytesDownloaded: Long, val totalBytes: Long, val downloadSpeedKbps: Long = 0) : LlamaDownloadState()
    data class Installed(val fileSizeBytes: Long, val localPath: String) : LlamaDownloadState()
    data class Error(val message: String) : LlamaDownloadState()
}

data class LlamaDeviceCompatibility(
    val isSupportedYear2020Plus: Boolean,
    val androidApiVersion: Int,
    val totalRamGb: Float,
    val compatibilitySummary: String
)

@OptIn(ExperimentalCoroutinesApi::class)
object LlamaLocalManager {
    private const val TAG = "LlamaLocalManager"
    const val HUGGINGFACE_REPO_URL = "https://huggingface.co/meta-llama/Llama-3.2-3B-Instruct"
    const val PUBLIC_GGUF_REPO_URL = "https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF"
    private const val MODEL_FILENAME = "Llama-3.2-3B-Instruct-Q4_K_M.gguf"
    private const val MODEL_ESTIMATED_SIZE_BYTES = 2_020_000_000L
    // Sanity floor for the ~2GB Q4_K_M GGUF: anything far smaller cannot be the real model.
    private const val MIN_VALID_MODEL_SIZE_BYTES = 1_500_000_000L
    private const val GGUF_MAGIC = 0x46554747
    private const val DEFAULT_CONTEXT_SIZE = 2048
    private const val DEFAULT_MAX_TOKENS = 384
    private const val MAX_HISTORY_TURNS = 6
    private const val MAX_HISTORY_CHARS_PER_TURN = 900
    private const val PUBLIC_HUGGINGFACE_GGUF_URL = "https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF/resolve/main/Llama-3.2-3B-Instruct-Q4_K_M.gguf?download=true"

    private val _downloadState = MutableStateFlow<LlamaDownloadState>(LlamaDownloadState.NotInstalled)
    val downloadState: StateFlow<LlamaDownloadState> = _downloadState.asStateFlow()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    // Native inference is heavy and must never run two sessions at once; a dedicated
    // single-thread dispatcher keeps it off the shared Default/IO pools.
    private val inferenceDispatcher = Dispatchers.Default.limitedParallelism(1)

    fun getLlamaModelFile(context: Context): File {
        val modelsDir = File(context.filesDir, "models")
        if (!modelsDir.exists() && !modelsDir.mkdirs()) Log.w(TAG, "Could not create ${modelsDir.absolutePath}")
        return File(modelsDir, MODEL_FILENAME)
    }

    fun isLlamaInstalled(context: Context): Boolean {
        val file = getLlamaModelFile(context)
        return file.exists() && file.length() >= MIN_VALID_MODEL_SIZE_BYTES && isValidGguf(file)
    }

    private fun isValidGguf(file: File): Boolean = runCatching {
        if (!file.exists() || file.length() < 8L) return false
        file.inputStream().use { input ->
            val header = ByteArray(4)
            if (input.read(header) != 4) return false
            val magic = (header[0].toInt() and 0xff) or ((header[1].toInt() and 0xff) shl 8) or ((header[2].toInt() and 0xff) shl 16) or ((header[3].toInt() and 0xff) shl 24)
            magic == GGUF_MAGIC
        }
    }.getOrDefault(false)

    fun checkDeviceCompatibility(context: Context): LlamaDeviceCompatibility {
        val apiVersion = Build.VERSION.SDK_INT
        val isSupported2020 = apiVersion >= Build.VERSION_CODES.Q
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val totalRamGb = memInfo.totalMem / (1024f * 1024f * 1024f)
        val enoughRam = totalRamGb >= 3.0f
        val summary = when {
            !isSupported2020 -> "❌ Android $apiVersion is below the recommended Android 10 baseline for local Llama 3.2 3B inference."
            !enoughRam -> "⚠️ Android $apiVersion with ${"%.1f".format(totalRamGb)}GB RAM may run Llama 3.2 3B, but memory pressure can terminate inference."
            else -> "✅ Compatible: Android $apiVersion with ${"%.1f".format(totalRamGb)}GB RAM. Local Llama 3.2 3B is available; any GPU/NPU label is shown only after hardware acceleration initializes and completes inference."
        }
        return LlamaDeviceCompatibility(isSupported2020, apiVersion, totalRamGb, summary)
    }

    suspend fun startLlamaDownload(context: Context, hfToken: String = "") = withContext(Dispatchers.IO) {
        val destinationFile = getLlamaModelFile(context)
        if (isLlamaInstalled(context)) {
            _downloadState.value = LlamaDownloadState.Installed(destinationFile.length(), destinationFile.absolutePath)
            if (hfToken.isNotBlank() && !LlamaAccelerator.isInstalled(context)) {
                LlamaAccelerator.download(context, hfToken).onFailure { Log.w(TAG, "Optional accelerated Llama model was not installed: ${it.message}") }
            }
            return@withContext
        }
        val tempFile = File(destinationFile.parentFile, "$MODEL_FILENAME.tmp")
        try {
            tempFile.delete()
            _downloadState.value = LlamaDownloadState.Downloading(0f, 0L, MODEL_ESTIMATED_SIZE_BYTES)
            val requestBuilder = Request.Builder()
                .url(PUBLIC_HUGGINGFACE_GGUF_URL)
                .header("User-Agent", "NeuroPath-Android/${BuildConfig.VERSION_NAME}")
            if (hfToken.isNotBlank()) {
                requestBuilder.header("Authorization", "Bearer ${hfToken.trim()}")
            }
            val request = requestBuilder.build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    _downloadState.value = LlamaDownloadState.Error("Llama 3.2 3B model download failed: HTTP ${response.code} ${response.message}")
                    return@withContext
                }
                val body = response.body ?: run {
                    _downloadState.value = LlamaDownloadState.Error("Llama 3.2 3B model download returned an empty response.")
                    return@withContext
                }
                val contentLength = body.contentLength().takeIf { it > 0 } ?: MODEL_ESTIMATED_SIZE_BYTES
                val startTime = System.currentTimeMillis()
                var downloaded = 0L
                var lastUpdate = startTime
                body.byteStream().use { input: InputStream ->
                    FileOutputStream(tempFile).use { output ->
                        val buffer = ByteArray(256 * 1024)
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            output.write(buffer, 0, read)
                            downloaded += read
                            val now = System.currentTimeMillis()
                            if (now - lastUpdate >= 250) {
                                lastUpdate = now
                                val seconds = ((now - startTime) / 1000L).coerceAtLeast(1L)
                                _downloadState.value = LlamaDownloadState.Downloading((downloaded.toFloat() / contentLength.toFloat()).coerceIn(0f, 0.99f), downloaded, contentLength, downloaded / 1024L / seconds)
                            }
                        }
                        output.fd.sync()
                    }
                }
            }
            if (tempFile.length() < MIN_VALID_MODEL_SIZE_BYTES || !isValidGguf(tempFile)) {
                tempFile.delete()
                _downloadState.value = LlamaDownloadState.Error("Downloaded file is not a valid Llama 3.2 GGUF model. The server may have returned an error page instead of model data.")
                return@withContext
            }
            destinationFile.delete()
            if (!tempFile.renameTo(destinationFile)) {
                tempFile.copyTo(destinationFile, overwrite = true)
                tempFile.delete()
            }
            _downloadState.value = LlamaDownloadState.Installed(destinationFile.length(), destinationFile.absolutePath)
            Log.i(TAG, "Installed Llama 3.2 3B GGUF ${destinationFile.absolutePath} (${destinationFile.length()} bytes)")
            if (hfToken.isNotBlank()) {
                LlamaAccelerator.download(context, hfToken).onFailure { Log.w(TAG, "Optional accelerated Llama model was not installed: ${it.message}") }
            }
        } catch (e: Exception) {
            tempFile.delete()
            Log.e(TAG, "Llama 3.2 3B GGUF download failed", e)
            _downloadState.value = LlamaDownloadState.Error("Llama 3.2 3B model download failed: ${e.message ?: e.javaClass.simpleName}")
        }
    }

    fun deleteLlamaModel(context: Context): Boolean {
        val file = getLlamaModelFile(context)
        val deleted = if (file.exists()) file.delete() else false
        File(file.parentFile, "${file.name}.tmp").delete()
        // Also remove the optional hardware-accelerated LiteRT-LM artifact, otherwise a
        // stale accelerated model survives "delete" and is silently picked up again.
        val litertFile = LlamaAccelerator.modelFile(context)
        val litertDeleted = if (litertFile.exists()) litertFile.delete() else false
        File(litertFile.parentFile, "${litertFile.name}.tmp").delete()
        _downloadState.value = LlamaDownloadState.NotInstalled
        return deleted || litertDeleted
    }

    suspend fun generateLlamaResponse(
        context: Context,
        prompt: String,
        systemPrompt: String = "",
        schoolDistrict: String = "",
        stateOrProvince: String = "",
        country: String = "",
        standardTitle: String = "",
        languageCode: String = "en-US",
        conversationHistory: List<Pair<String, String>> = emptyList(),
        curriculumContext: String = "",
        hasValidApiKey: Boolean = false,
        activeApiKey: String = ""
    ): String = withContext(inferenceDispatcher) {
        val modelFile = getLlamaModelFile(context)
        if (!isLlamaInstalled(context)) return@withContext "🦙 [Llama 3.2 3B Local Engine]: The local GGUF model is not installed. Open Parent Dashboard → Local Llama 3.2 Manager and download it first."
        if (!checkDeviceCompatibility(context).isSupportedYear2020Plus) return@withContext "🦙 [Llama 3.2 3B Local Engine]: This Android version is below the recommended Android 10 baseline for local Llama 3.2 inference."
        val userPrompt = prompt.trim()
        if (userPrompt.isBlank()) return@withContext "🦙 [Llama 3.2 3B Local Engine]: Please ask me a learning question."

        val recentHistory = conversationHistory
            .filter { it.first.equals("user", ignoreCase = true) || it.first.equals("model", ignoreCase = true) || it.first.equals("assistant", ignoreCase = true) }
            .map { (role, text) -> role.lowercase() to text.trim() }
            .filter { it.second.isNotBlank() }
            .takeLast(MAX_HISTORY_TURNS)
            .map { (role, text) ->
                val bounded = text.take(MAX_HISTORY_CHARS_PER_TURN)
                val label = if (role == "user") "Student" else "Learning Buddy"
                "$label: $bounded"
            }

        val historyBlock = if (recentHistory.isNotEmpty()) {
            "\nRecent conversation (use this for continuity; do not repeat it verbatim):\n${recentHistory.joinToString("\n")}\n"
        } else {
            ""
        }

        val grounding = buildString {
            append("You are NeuroPath's local educational Learning Buddy powered by Llama 3.2 3B. Be patient, encouraging, concise, and age-appropriate. Use a Socratic teaching style: guide the learner instead of simply doing their work for them. ")
            if (schoolDistrict.isNotBlank()) append("The learner's school district is $schoolDistrict. ")
            if (stateOrProvince.isNotBlank()) append("Region: $stateOrProvince, $country. ")
            if (standardTitle.isNotBlank()) append("Curriculum standard: $standardTitle. ")
            if (curriculumContext.isNotBlank()) append("Curriculum context: $curriculumContext. ")
            if (systemPrompt.isNotBlank()) append("\n$systemPrompt\n")
        }
        val llamaPrompt = "<|begin_of_text|><|start_header_id|>system<|end_header_id|>\n$grounding\n<|eot_id|><|start_header_id|>user<|end_header_id|>\n$historyBlock$userPrompt<|eot_id|><|start_header_id|>assistant<|end_header_id|>\n"

        // The accelerated path gets the same Llama-3 chat template as the GGUF path so the
        // Instruct model sees identical formatting whichever runtime serves it.
        runCatching { LlamaAccelerator.generate(context, llamaPrompt) }
            .onFailure { Log.w(TAG, "LiteRT-LM accelerated inference unavailable", it) }
            .getOrNull()
            ?.let { result ->
                Log.i(TAG, "Using verified local accelerator ${result.backend} (${result.modelFile})")
                return@withContext result.text
            }

        try {
            val model = Llama.loadModel(
                modelPath = modelFile.absolutePath,
                config = LlamaConfig(contextSize = DEFAULT_CONTEXT_SIZE, threads = Runtime.getRuntime().availableProcessors().coerceIn(2, 6))
            )
            val result = try {
                Llama.complete(model, prompt = llamaPrompt, systemPrompt = "", maxTokens = DEFAULT_MAX_TOKENS)
            } finally {
                Llama.releaseModel(model)
            }
            val text = result.text.trim()
            if (text.isBlank()) "🦙 [Llama 3.2 3B Local Engine]: I couldn't generate a response. Please try asking the question another way." else text
        } catch (e: Exception) {
            Log.e(TAG, "Local Llama 3.2 3B inference failed", e)
            "🦙 [Llama 3.2 3B Local Engine]: Local inference failed safely: ${e.message ?: e.javaClass.simpleName}. The model is installed, but the device could not complete inference."
        }
    }
}
