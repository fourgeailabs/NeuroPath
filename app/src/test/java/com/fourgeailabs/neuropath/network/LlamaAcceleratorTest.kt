package com.fourgeailabs.neuropath.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LlamaAcceleratorTest {
    @Test
    fun qualcommModelUsesNpuTargetedArtifact() {
        assertEquals(
            "Llama-3.2-3B-Instruct_q4_ekv1280_sm8650.litertlm",
            LlamaAccelerator.selectedModelFilename("Qualcomm", "SM8650")
        )
    }

    @Test
    fun mediatekModelUsesNpuTargetedArtifact() {
        assertEquals(
            "Llama-3.2-3B-Instruct_q4_ekv1280_mt6993.litertlm",
            LlamaAccelerator.selectedModelFilename("MediaTek", "MT6993")
        )
    }

    @Test
    fun tensorModelsUseTensorArtifacts() {
        assertEquals(
            "Llama-3.2-3B-Instruct_q8_ekv1280_Google_Tensor_G5.litertlm",
            LlamaAccelerator.selectedModelFilename("Google", "Tensor G5")
        )
        assertEquals(
            "Llama-3.2-3B-Instruct_q8_ekv1280_Google_Tensor_G6.litertlm",
            LlamaAccelerator.selectedModelFilename("Google", "Tensor G6")
        )
    }

    @Test
    fun unsupportedHardwareUsesGenericGpuCapableModel() {
        assertEquals(
            "llama3.2-3b-it-int4.litertlm",
            LlamaAccelerator.selectedModelFilename("Samsung", "Exynos 2400")
        )
    }

    @Test
    fun selectedArtifactsHaveLiteRtLmExtension() {
        val names = listOf(
            LlamaAccelerator.selectedModelFilename("Qualcomm", "SM8550"),
            LlamaAccelerator.selectedModelFilename("MediaTek", "MT6989"),
            LlamaAccelerator.selectedModelFilename("Google", "Tensor G5")
        )
        assertTrue(names.all { it.endsWith(".litertlm") })
    }
}
