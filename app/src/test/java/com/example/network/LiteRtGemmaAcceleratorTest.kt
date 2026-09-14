package com.example.network

import org.junit.Assert.assertEquals
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
            "Gemma3-1B-IT_q4_ekv1280_mt6991.litertlm",
            LiteRtGemmaAccelerator.selectedModelFilename("MediaTek", "MT6991")
        )
    }

    @Test
    fun tensorModelUsesTensorArtifact() {
        assertEquals(
            "Gemma3-1B-IT_q8_ekv1280_Google_Tensor_G5.litertlm",
            LiteRtGemmaAccelerator.selectedModelFilename("Google", "Tensor G5")
        )
    }

    @Test
    fun unsupportedHardwareUsesGenericGpuCapableModel() {
        assertEquals(
            "gemma3-1b-it-int4.litertlm",
            LiteRtGemmaAccelerator.selectedModelFilename("Samsung", "Exynos 2400")
        )
    }
}
