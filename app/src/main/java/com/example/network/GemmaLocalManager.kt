package com.example.network

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import dev.ffmpegkit.llama.Llama
import dev.ffmpegkit.llama.LlamaConfig
import kotlinx.coroutines.Dispatchers
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

sealed class GemmaDownloadState {
    object NotInstalled : GemmaDownloadState()
    data class Downloading(
        val progress: Float,
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val downloadSpeedKbps: Long = 0
    ) : GemmaDownloadState()
    data class Installed(val fileSizeBytes: Long, val localPath: String) : GemmaDownloadState()
    data class Error(val message: String) : GemmaDownloadState()
}

data class GemmaDeviceCompatibility(
    val isSupportedYear2020Plus: Boolean,
    val androidApiVersion: Int,
    val totalRamGb: Float,
    val hasGpuAcceleration: Boolean,
    val compatibilitySummary: String
)

/** Real GGUF Gemma downloader and llama.cpp on-device inference manager. */
object GemmaLocalManager {
    private const val TAG = "GemmaLocalManager"
    const val HUGGINGFACE_REPO_URL = "https://huggingface.co/google/gemma-2-2b"
    const val PUBLIC_GGUF_REPO_URL = "https://huggingface.co/bartowski/gemma-2-2b-it-GGUF"

    private const val MODEL_FILENAME = "gemma-2-2b-it-Q4_K_M.gguf"
    private const val MODEL_ESTIMATED_SIZE_BYTES = 1_600_000_000L
    private const val MIN_VALID_MODEL_SIZE_BYTES = 500_000_000L
    private const val GGUF_MAGIC = 0x46554747
    private const val DEFAULT_CONTEXT_SIZE = 2048
    private const val DEFAULT_MAX_TOKENS = 384
    private const val PUBLIC_HUGGINGFACE_GGUF_URL =
        "https://huggingface.co/bartowski/gemma-2-2b-it-GGUF/resolve/main/gemma-2-2b-it-Q4_K_M.gguf?download=true"

    private val _downloadState = MutableStateFlow<GemmaDownloadState>(GemmaDownloadState.NotInstalled)
    val downloadState: StateFlow<GemmaDownloadState> = _downloadState.asStateFlow()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.MINUTES)
        .writeTimeout(5, TimeUnit.MINUTES)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    fun getGemmaModelFile(context: Context): File {
        val modelsDir = File(context.filesDir, "models")
        if (!modelsDir.exists() && !modelsDir.mkdirs()) Log.w(TAG, "Could not create ${modelsDir.absolutePath}")
        return File(modelsDir, MODEL_FILENAME)
    }

    fun isGemmaInstalled(context: Context): Boolean {
        val file = getGemmaModelFile(context)
        return file.exists() && file.length() >= MIN_VALID_MODEL_SIZE_BYTES && isValidGguf(file)
    }

    private fun isValidGguf(file: File): Boolean = runCatching {
        if (!file.exists() || file.length() < 8L) return false
        file.inputStream().use { input ->
            val header = ByteArray(4)
            if (input.read(header) != 4) return false
            val magic = (header[0].toInt() and 0xff) or
                ((header[1].toInt() and 0xff) shl 8) or
                ((header[2].toInt() and 0xff) shl 16) or
                ((header[3].toInt() and 0xff) shl 24)
            magic == GGUF_MAGIC
        }
    }.getOrDefault(false)

    fun checkDeviceCompatibility(context: Context): GemmaDeviceCompatibility {
        val apiVersion = Build.VERSION.SDK_INT
        val isSupported2020 = apiVersion >= Build.VERSION_CODES.Q
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val totalRamGb = memInfo.totalMem / (1024f * 1024f * 1024f)
        val enoughRam = totalRamGb >= 3.0f

        val summary = when {
            !isSupported2020 -> "❌ Android $apiVersion is below the recommended Android 10 baseline for local Gemma inference."
            !enoughRam -> "⚠️ Android $apiVersion with ${"%.1f".format(totalRamGb)}GB RAM may run Gemma 2 2B, but memory pressure can terminate inference."
            else -> "✅ Compatible: Android $apiVersion with ${"%.1f".format(totalRamGb)}GB RAM. Gemma 2 2B will run locally with CPU/NEON llama.cpp."
        }

        return GemmaDeviceCompatibility(isSupported2020, apiVersion, totalRamGb, false, summary)
    }

    suspend fun startGemmaDownload(context: Context, hfToken: String = "") = withContext(Dispatchers.IO) {
        val destinationFile = getGemmaModelFile(context)
        if (isGemmaInstalled(context)) {
            _downloadState.value = GemmaDownloadState.Installed(destinationFile.length(), destinationFile.absolutePath)
            return@withContext
        }

        val tempFile = File(destinationFile.parentFile, "$MODEL_FILENAME.tmp")
        try {
            tempFile.delete()
            _downloadState.value = GemmaDownloadState.Downloading(0f, 0L, MODEL_ESTIMATED_SIZE_BYTES)

            // Always download GGUF. The official Gemma safetensors file is not
            // compatible with the llama.cpp GGUF runtime, so a token must not
            // silently switch this request to safetensors.
            val request = Request.Builder()
                .url(PUBLIC_HUGGINGFACE_GGUF_URL)
                .header("User-Agent", "NeuroPath-Android/1.25.00")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    _downloadState.value = GemmaDownloadState.Error("Gemma model download failed: HTTP ${response.code} ${response.message}")
                    return@withContext
                }
                val body = response.body ?: run {
                    _downloadState.value = GemmaDownloadState.Error("Gemma model download returned an empty response.")
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
                                _downloadState.value = GemmaDownloadState.Downloading(
                                    (downloaded.toFloat() / contentLength.toFloat()).coerceIn(0f, 0.99f),
                                    downloaded,
                                    contentLength,
                                    downloaded / 1024L / seconds
                                )
                            }
                        }
                        output.fd.sync()
                    }
                }
            }

            if (tempFile.length() < MIN_VALID_MODEL_SIZE_BYTES || !isValidGguf(tempFile)) {
                tempFile.delete()
                _downloadState.value = GemmaDownloadState.Error("Downloaded file is not a valid Gemma GGUF model. The server may have returned an error page instead of model data.")
                return@withContext
            }

            destinationFile.delete()
            if (!tempFile.renameTo(destinationFile)) {
                tempFile.copyTo(destinationFile, overwrite = true)
                tempFile.delete()
            }
            _downloadState.value = GemmaDownloadState.Installed(destinationFile.length(), destinationFile.absolutePath)
            Log.i(TAG, "Installed Gemma GGUF ${destinationFile.absolutePath} (${destinationFile.length()} bytes)")
        } catch (e: Exception) {
            tempFile.delete()
            Log.e(TAG, "Gemma GGUF download failed", e)
            _downloadState.value = GemmaDownloadState.Error("Gemma model download failed: ${e.message ?: e.javaClass.simpleName}")
        }
    }

    fun deleteGemmaModel(context: Context): Boolean {
        val file = getGemmaModelFile(context)
        val deleted = if (file.exists()) file.delete() else false
        File(file.parentFile, "${file.name}.tmp").delete()
        _downloadState.value = GemmaDownloadState.NotInstalled
        return deleted
    }

    suspend fun generateGemmaResponse(
        context: Context,
        prompt: String,
        systemPrompt: String,
        schoolDistrict: String,
        standardTitle: String
    ): String = withContext(Dispatchers.Default) {
        val modelFile = getGemmaModelFile(context)
        if (!isGemmaInstalled(context)) {
            return@withContext "💎 [Gemma 2-2B Local Engine]: The local GGUF model is not installed. Open Parent Dashboard → Local Gemma Manager and download it first."
        }
        if (!checkDeviceCompatibility(context).isSupportedYear2020Plus) {
            return@withContext "💎 [Gemma 2-2B Local Engine]: This Android version is below the recommended Android 10 baseline for local Gemma inference."
        }

        val userPrompt = prompt.trim()
        if (userPrompt.isBlank()) return@withContext "💎 [Gemma 2-2B Local Engine]: Please ask me a learning question."

        val grounding = buildString {
            append("You are NeuroPath's local educational Learning Buddy. ")
            append("Be patient, encouraging, concise, and age-appropriate. ")
            append("Use a Socratic teaching style: guide the learner instead of simply doing their work for them. ")
            if (schoolDistrict.isNotBlank()) append("The learner's school district is $schoolDistrict. ")
            if (standardTitle.isNotBlank()) append("Their curriculum standard/framework is $standardTitle. ")
            if (systemPrompt.isNotBlank()) append(systemPrompt.trim())
        }

        try {
            val model = Llama.loadModel(
                modelPath = modelFile.absolutePath,
                config = LlamaConfig(
                    contextSize = DEFAULT_CONTEXT_SIZE,
                    threads = Runtime.getRuntime().availableProcessors().coerceIn(2, 6)
                )
            )
            val result = try {
                Llama.complete(model, userPrompt, grounding, DEFAULT_MAX_TOKENS)
            } finally {
                Llama.releaseModel(model)
            }
            val text = result.text.trim()
            if (text.isBlank()) "💎 [Gemma 2-2B Local Engine]: I couldn't generate a response. Please try asking the question another way." else text
        } catch (e: Exception) {
            Log.e(TAG, "Local Gemma inference failed", e)
            "💎 [Gemma 2-2B Local Engine]: Local inference failed safely: ${e.message ?: e.javaClass.simpleName}. The model is installed, but the device could not complete inference."
        }
    }
}
