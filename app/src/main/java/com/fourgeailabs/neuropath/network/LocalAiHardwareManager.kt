package com.fourgeailabs.neuropath.network

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
            totalRamGb = memoryInfo.totalMem / (1024f * 1024f * 1024f),
            manufacturer = manufacturer,
            socModel = socModel
        )
    }

    /**
     * Candidate NPU backend label for this chip, or null when unknown. The mapping lives in
     * [LlamaSocProfiles] (shared with [LlamaAccelerator.selectedModelFilename]); a non-null
     * result is a *candidate* only — it never claims the NPU is actually in use.
     */
    fun potentialNpuBackend(manufacturer: String, socModel: String): String? =
        LlamaSocProfiles.potentialNpuBackend(manufacturer, socModel)

    fun selectBackend(probes: List<LocalAiBackendProbe>, cloudAvailable: Boolean): LocalAiBackend {
        val priority = listOf(LocalAiBackend.NPU, LocalAiBackend.GPU, LocalAiBackend.CPU)
        priority.firstOrNull { backend -> probes.any { it.backend == backend && it.initialized } }?.let { return it }
        return if (cloudAvailable) LocalAiBackend.CLOUD else LocalAiBackend.UNAVAILABLE
    }
}
