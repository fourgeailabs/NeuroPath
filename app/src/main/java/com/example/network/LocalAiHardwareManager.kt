package com.example.network

import android.app.ActivityManager
import android.content.Context
import android.os.Build

/**
 * Describes hardware/runtime opportunities without claiming that an accelerator is active.
 * An accelerator becomes ACTIVE only after the inference runtime successfully initializes it.
 */
enum class LocalAiBackend {
    NPU,
    GPU,
    CPU,
    CLOUD,
    UNAVAILABLE
}

data class LocalAiHardwareCapabilities(
    val cpuAvailable: Boolean,
    val armNeonAvailable: Boolean,
    val potentialNpuBackend: String?,
    val gpuRuntimeAvailable: Boolean,
    val totalRamGb: Float,
    val manufacturer: String,
    val socModel: String
)

data class LocalAiBackendProbe(
    val backend: LocalAiBackend,
    val initialized: Boolean,
    val reason: String
)

object LocalAiHardwareManager {
    fun inspect(context: Context): LocalAiHardwareCapabilities {
        val memoryInfo = ActivityManager.MemoryInfo()
        (context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.getMemoryInfo(memoryInfo)
        return LocalAiHardwareCapabilities(
            cpuAvailable = Runtime.getRuntime().availableProcessors() > 0,
            armNeonAvailable = Build.SUPPORTED_ABIS.any { it == "arm64-v8a" || it == "armeabi-v7a" },
            potentialNpuBackend = potentialNpuBackend(Build.SOC_MANUFACTURER, Build.SOC_MODEL),
            // Presence of a GPU is not treated as proof that the LLM runtime can use it.
            gpuRuntimeAvailable = false,
            totalRamGb = memoryInfo.totalMem / (1024f * 1024f * 1024f),
            manufacturer = Build.SOC_MANUFACTURER.ifBlank { Build.MANUFACTURER },
            socModel = Build.SOC_MODEL
        )
    }

    /**
     * Returns a known hardware-targeted LiteRT-LM backend family when one can be identified.
     * This is a candidate only; the runtime must still successfully initialize the model.
     */
    fun potentialNpuBackend(manufacturer: String, socModel: String): String? {
        val model = socModel.lowercase()
        return when {
            manufacturer.equals("Qualcomm", true) && model.contains("sm8550") -> "QUALCOMM_QNN"
            manufacturer.equals("Qualcomm", true) && model.contains("sm8650") -> "QUALCOMM_QNN"
            manufacturer.equals("Qualcomm", true) && model.contains("sm8750") -> "QUALCOMM_QNN"
            manufacturer.equals("Qualcomm", true) && model.contains("sm8850") -> "QUALCOMM_QNN"
            manufacturer.equals("MediaTek", true) && (model.contains("mt6989") || model.contains("mt6991") || model.contains("mt6993")) -> "MEDIATEK"
            manufacturer.equals("Google", true) && (model.contains("tensor") || model.contains("g5") || model.contains("g6")) -> "GOOGLE_TENSOR"
            manufacturer.equals("Samsung", true) && model.contains("exynos") -> "SAMSUNG_EXYNOS"
            else -> null
        }
    }

    /**
     * Chooses the first backend that the actual runtime probe says initialized successfully.
     * The method deliberately does not infer accelerator availability from device hardware alone.
     */
    fun selectBackend(probes: List<LocalAiBackendProbe>, cloudAvailable: Boolean): LocalAiBackend {
        val priority = listOf(LocalAiBackend.NPU, LocalAiBackend.GPU, LocalAiBackend.CPU)
        priority.firstOrNull { backend -> probes.any { it.backend == backend && it.initialized } }?.let { return it }
        return if (cloudAvailable) LocalAiBackend.CLOUD else LocalAiBackend.UNAVAILABLE
    }
}
