package com.fourgeailabs.neuropath.network

/**
 * Single source of truth for the SoC → Llama 3.2 3B LiteRT-LM artifact/backend mapping.
 *
 * [LlamaAccelerator.selectedModelFilename] and [LocalAiHardwareManager.potentialNpuBackend]
 * both delegate here, so a new chip is added in exactly one place. Detection is deliberately
 * explicit (chip identifiers plus Tensor-generation regexes) instead of loose substring
 * matching, so unknown chips fall through to null instead of being mislabelled.
 */
object LlamaSocProfiles {

    data class SocProfile(
        val litertlmFilename: String,
        val npuBackendLabel: String
    )

    const val GENERIC_GPU_MODEL = "llama3.2-3b-it-int4.litertlm"

    // Tensor G5/G6 detection anchors on the "tensor g5"/"tensor g6" generation marker instead of
    // a bare contains("g5"), which could false-positive on unrelated model strings.
    private val TENSOR_G5 = Regex("""tensor[ _-]?g5\b""")
    private val TENSOR_G6 = Regex("""tensor[ _-]?g6\b""")

    fun resolve(manufacturer: String, socModel: String): SocProfile? {
        val model = socModel.lowercase()
        return when {
            manufacturer.equals("Qualcomm", ignoreCase = true) && model.contains("sm8550") ->
                SocProfile("Llama-3.2-3B-Instruct_q4_ekv1280_sm8550.litertlm", "QUALCOMM_QNN")
            manufacturer.equals("Qualcomm", ignoreCase = true) && model.contains("sm8650") ->
                SocProfile("Llama-3.2-3B-Instruct_q4_ekv1280_sm8650.litertlm", "QUALCOMM_QNN")
            manufacturer.equals("Qualcomm", ignoreCase = true) && model.contains("sm8750") ->
                SocProfile("Llama-3.2-3B-Instruct_q4_ekv1280_sm8750.litertlm", "QUALCOMM_QNN")
            manufacturer.equals("Qualcomm", ignoreCase = true) && model.contains("sm8850") ->
                SocProfile("Llama-3.2-3B-Instruct_q4_ekv1280_sm8850.litertlm", "QUALCOMM_QNN")
            manufacturer.equals("MediaTek", ignoreCase = true) && model.contains("mt6989") ->
                SocProfile("Llama-3.2-3B-Instruct_q4_ekv1280_mt6989.litertlm", "MEDIATEK")
            manufacturer.equals("MediaTek", ignoreCase = true) && model.contains("mt6991") ->
                SocProfile("Llama-3.2-3B-Instruct_q4_ekv1280_mt6991.litertlm", "MEDIATEK")
            manufacturer.equals("MediaTek", ignoreCase = true) && model.contains("mt6993") ->
                SocProfile("Llama-3.2-3B-Instruct_q4_ekv1280_mt6993.litertlm", "MEDIATEK")
            manufacturer.equals("Google", ignoreCase = true) && TENSOR_G5.containsMatchIn(model) ->
                SocProfile("Llama-3.2-3B-Instruct_q8_ekv1280_Google_Tensor_G5.litertlm", "GOOGLE_TENSOR")
            manufacturer.equals("Google", ignoreCase = true) && TENSOR_G6.containsMatchIn(model) ->
                SocProfile("Llama-3.2-3B-Instruct_q8_ekv1280_Google_Tensor_G6.litertlm", "GOOGLE_TENSOR")
            else -> null
        }
    }

    fun litertlmFilename(manufacturer: String, socModel: String): String =
        resolve(manufacturer, socModel)?.litertlmFilename ?: GENERIC_GPU_MODEL
}
