package com.fourgeailabs.neuropath.network

/**
 * Single source of truth for LiteRT-LM model/backend compatibility.
 *
 * A device SoC is not sufficient evidence that an NPU model exists. A profile is only exposed
 * when the exact artifact and backend compatibility have been verified.
 */
object LlamaSocProfiles {
    data class ModelProfile(
        val litertlmFilename: String,
        val verifiedBackends: Set<LocalAiBackend>
    )

    const val GENERIC_GPU_MODEL = "llama3_2_3b_mixed_int4_gpu.litertlm"

    private val GENERIC_PROFILE = ModelProfile(
        GENERIC_GPU_MODEL,
        setOf(LocalAiBackend.GPU, LocalAiBackend.CPU)
    )

    /**
     * Returns a verified SoC-specific profile, or null when none is verified.
     * Do not infer NPU support from the presence of an NPU in the device.
     */
    fun resolve(manufacturer: String, socModel: String): ModelProfile? = null

    fun profileForModel(filename: String): ModelProfile? =
        if (filename == GENERIC_GPU_MODEL) GENERIC_PROFILE else null

    fun litertlmFilename(manufacturer: String, socModel: String): String =
        resolve(manufacturer, socModel)?.litertlmFilename ?: GENERIC_GPU_MODEL

    fun verifiedBackends(filename: String): Set<LocalAiBackend> =
        profileForModel(filename)?.verifiedBackends ?: emptySet()

    fun potentialNpuBackend(manufacturer: String, socModel: String): String? {
        val profile = resolve(manufacturer, socModel) ?: return null
        return if (LocalAiBackend.NPU in profile.verifiedBackends) "VERIFIED_NPU" else null
    }
}
