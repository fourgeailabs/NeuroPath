package com.example.network

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

data class LiteRtGemmaResult(
    val text: String,
    val backend: LocalAiBackend,
    val modelFile: String,
    val loadTimeMs: Long,
    val generationTimeMs: Long
)

object LiteRtGemmaAccelerator {
    private const val TAG = "LiteRtGemmaAccelerator"
    private const val MODEL_REPO = "litert-community/Gemma3-1B-IT"
    private const val GENERIC_GPU_MODEL = "gemma3-1b-it-int4.litertlm"
    private const val MIN_MODEL_BYTES = 450_000_000L
    private const val MODEL_MAGIC = "LITERTLM"
    private const val MAX_OUTPUT_TOKENS = 384

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.MINUTES)
        .build()

    fun modelFile(context: Context): File =
        File(File(context.filesDir, "models"), selectedModelFilename())

    fun selectedModelFilename(
        manufacturer: String = Build.SOC_MANUFACTURER,
        socModel: String = Build.SOC_MODEL
    ): String {
        val model = socModel.lowercase()
        return when {
            manufacturer.equals("Qualcomm", true) && model.contains("sm8550") -> "gemma3-1b-it_q4_ekv1280_sm8550.litertlm"
            manufacturer.equals("Qualcomm", true) && model.contains("sm8650") -> "gemma3-1b-it_q4_ekv1280_sm8650.litertlm"
            manufacturer.equals("Qualcomm", true) && model.contains("sm8750") -> "gemma3-1b-it_q4_ekv1280_sm8750.litertlm"
            manufacturer.equals("Qualcomm", true) && model.contains("sm8850") -> "gemma3-1b-it_q4_ekv1280_sm8850.litertlm"
            manufacturer.equals("MediaTek", true) && model.contains("mt6989") -> "gemma3-1b-it_q4_ekv1280_mt6989.litertlm"
            manufacturer.equals("MediaTek", true) && model.contains("mt6991") -> "gemma3-1b-it_q4_ekv1280_mt6991.litertlm"
            manufacturer.equals("MediaTek", true) && model.contains("mt6993") -> "gemma3-1b-it_q4_ekv1280_mt6993.litertlm"
            else -> GENERIC_GPU_MODEL
        }
    }

    fun isInstalled(context: Context): Boolean {
        val file = modelFile(context)
        return file.exists() && file.length() >= MIN_MODEL_BYTES && isValidLiteRtLm(file)
    }

    private fun isValidLiteRtLm(file: File): Boolean = runCatching {
        if (!file.exists() || file.length() < 16L) return false
        file.inputStream().use { input ->
            val header = ByteArray(MODEL_MAGIC.length)
            if (input.read(header) != header.size) return false
            String(header, Charsets.US_ASCII) == MODEL_MAGIC
        }
    }.getOrDefault(false)

    suspend fun download(context: Context, hfToken: String): Result<File> = withContext(Dispatchers.IO) {
        if (hfToken.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("A Hugging Face token is required for the gated Gemma 3 LiteRT-LM model.")
            )
        }
        val destination = modelFile(context)
        if (isInstalled(context)) return@withContext Result.success(destination)
        destination.parentFile?.mkdirs()
        val temp = File(destination.parentFile, "${destination.name}.tmp")
        runCatching {
            temp.delete()
            val url = "https://huggingface.co/$MODEL_REPO/resolve/main/${destination.name}?download=true"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer ${hfToken.trim()}")
                .header("User-Agent", "NeuroPath-Android/2.00.00")
                .build()
            client.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "LiteRT-LM model download failed: HTTP ${response.code}" }
                val body = response.body ?: error("LiteRT-LM model response was empty")
                body.byteStream().use { input: InputStream ->
                    FileOutputStream(temp).use { output ->
                        val buffer = ByteArray(256 * 1024)
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            output.write(buffer, 0, read)
                        }
                        output.fd.sync()
                    }
                }
            }
            check(temp.length() >= MIN_MODEL_BYTES && isValidLiteRtLm(temp)) {
                "Downloaded LiteRT-LM artifact failed validation"
            }
            destination.delete()
            check(temp.renameTo(destination)) { "Could not install LiteRT-LM model" }
            Log.i(TAG, "Installed ${destination.name} (${destination.length()} bytes)")
            destination
        }.onFailure {
            temp.delete()
            Log.e(TAG, "LiteRT-LM model download failed", it)
        }
    }

    suspend fun generate(
        context: Context,
        prompt: String,
        systemPrompt: String = ""
    ): LiteRtGemmaResult? = withContext(Dispatchers.IO) {
        if (!isInstalled(context) || prompt.isBlank()) return@withContext null
        val file = modelFile(context)
        val candidateBackends = backendCandidates(context, file.name)
        var lastFailure: Throwable? = null

        for (backend in candidateBackends) {
            val loadStart = System.nanoTime()
            try {
                val config = EngineConfig(modelPath = file.absolutePath, backend = backend)
                Engine(config).use { engine ->
                    engine.initialize()
                    val loadTimeMs = (System.nanoTime() - loadStart) / 1_000_000L
                    engine.createConversation().use { conversation ->
                        val generationStart = System.nanoTime()
                        val fullPrompt = buildString {
                            if (systemPrompt.isNotBlank()) append(systemPrompt.trim()).append("\n\n")
                            append(prompt.trim())
                        }
                        val response = conversation.sendMessage(fullPrompt, maxOutputToken = MAX_OUTPUT_TOKENS)
                        val text = response.text.trim()
                        check(text.isNotBlank()) { "LiteRT-LM returned an empty response" }
                        val generationTimeMs = (System.nanoTime() - generationStart) / 1_000_000L
                        val active = when (backend) {
                            is Backend.NPU -> LocalAiBackend.NPU
                            is Backend.GPU -> LocalAiBackend.GPU
                            else -> LocalAiBackend.CPU
                        }
                        Log.i(TAG, "Verified active backend=$active model=${file.name} loadMs=$loadTimeMs generationMs=$generationTimeMs")
                        return@withContext LiteRtGemmaResult(
                            text = text,
                            backend = active,
                            modelFile = file.name,
                            loadTimeMs = loadTimeMs,
                            generationTimeMs = generationTimeMs
                        )
                    }
                }
            } catch (t: Throwable) {
                lastFailure = t
                Log.w(TAG, "LiteRT-LM backend $backend failed; trying next candidate", t)
            }
        }
        Log.w(TAG, "No LiteRT-LM backend completed inference", lastFailure)
        null
    }

    private fun backendCandidates(context: Context, modelName: String): List<Backend> {
        val vendorTargetedNpu = modelName != GENERIC_GPU_MODEL
        return buildList {
            if (vendorTargetedNpu) {
                add(Backend.NPU(nativeLibraryDir = context.applicationInfo.nativeLibraryDir))
            }
            add(Backend.GPU())
            add(Backend.CPU())
        }
    }
}
