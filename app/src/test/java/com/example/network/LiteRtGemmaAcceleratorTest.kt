package com.example.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LiteRtGemmaAcceleratorTest {
    @Test
    fun qualcommModelUsesNpuTargetedArtifact() {
        assertEquals(
            "Gemma3-1B-IT_q4_ekv1280_sm8650.litertlm",
            LiteRtGemmaAccelerator.selectedModelFilename("Qualcomm", "SM8650")
        )
    }

    @Test
    fun mediatekModelUsesNpuTargetedArtifact() {
        assertEquals(
            "Gemma3-1B-IT_q4_ekv1280_mt6993.litertlm",
            LiteRtGemmaAccelerator.selectedModelFilename("MediaTek", "MT6993")
        )
    }

    @Test
    fun tensorModelsUseTensorArtifacts() {
        assertEquals(
            "Gemma3-1B-IT_q8_ekv1280_Google_Tensor_G5.litertlm",
            LiteRtGemmaAccelerator.selectedModelFilename("Google", "Tensor G5")
        )
        assertEquals(
            "Gemma3-1B-IT_q8_ekv1280_Google_Tensor_G6.litertlm",
            LiteRtGemmaAccelerator.selectedModelFilename("Google", "Tensor G6")
        )
    }

    @Test
    fun unsupportedHardwareUsesGenericGpuCapableModel() {
        assertEquals(
            "gemma3-1b-it-int4.litertlm",
            LiteRtGemmaAccelerator.selectedModelFilename("Samsung", "Exynos 2400")
        )
    }

    @Test
    fun selectedArtifactsHaveLiteRtLmExtension() {
        val names = listOf(
            LiteRtGemmaAccelerator.selectedModelFilename("Qualcomm", "SM8550"),
            LiteRtGemmaAccelerator.selectedModelFilename("MediaTek", "MT6989"),
            LiteRtGemmaAccelerator.selectedModelFilename("Google", "Tensor G5")
        )
        assertTrue(names.all { it.endsWith(".litertlm") })
    }
}
