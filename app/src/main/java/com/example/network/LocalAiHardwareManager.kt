package com.example.network

import android.app.ActivityManager
import android.content.Context
import android.os.Build

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
        val manufacturer = if (Build.VERSION.SDK_INT >= 31) Build.SOC_MANUFACTURER else Build.MANUFACTURER
        val socModel = if (Build.VERSION.SDK_INT >= 31) Build.SOC_MODEL else ""
        return LocalAiHardwareCapabilities(
            cpuAvailable = Runtime.getRuntime().availableProcessors() > 0,
            armNeonAvailable = Build.SUPPORTED_ABIS.any { it == "arm64-v8a" || it == "armeabi-v7a" },
            potentialNpuBackend = potentialNpuBackend(manufacturer, socModel),
            // Hardware presence is not proof that an LLM runtime can use the GPU.
            gpuRuntimeAvailable = false,
            totalRamGb = memoryInfo.totalMem / (1024f * 1024f * 1024f),
            manufacturer = manufacturer,
            socModel = socModel
        )
    }

    fun potentialNpuBackend(manufacturer: String, socModel: String): String? {
        val model = socModel.lowercase()
        return when {
            manufacturer.equals("Qualcomm", true) && model.contains("sm8550") -> "QUALCOMM_QNN"
            manufacturer.equals("Qualcomm", true) && model.contains("sm8650") -> "QUALCOMM_QNN"
            manufacturer.equals("Qualcomm", true) && model.contains("sm8750") -> "QUALCOMM_QNN"
            manufacturer.equals("Qualcomm", true) && model.contains("sm8850") -> "QUALCOMM_QNN"
            manufacturer.equals("MediaTek", true) && (model.contains("mt6989") || model.contains("mt6991") || model.contains("mt6993")) -> "MEDIATEK"
            manufacturer.equals("Google", true) && (model.contains("tensor") || model.contains("g5") || model.contains("g6")) -> "GOOGLE_TENSOR"
            else -> null
        }
    }

    fun selectBackend(probes: List<LocalAiBackendProbe>, cloudAvailable: Boolean): LocalAiBackend {
        val priority = listOf(LocalAiBackend.NPU, LocalAiBackend.GPU, LocalAiBackend.CPU)
        priority.firstOrNull { backend -> probes.any { it.backend == backend && it.initialized } }?.let { return it }
        return if (cloudAvailable) LocalAiBackend.CLOUD else LocalAiBackend.UNAVAILABLE
    }
}
