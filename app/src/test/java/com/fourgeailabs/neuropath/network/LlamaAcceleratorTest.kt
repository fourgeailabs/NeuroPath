package com.fourgeailabs.neuropath.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LlamaAcceleratorTest {
    @Test
    fun publishedGenericModelUsesOnlyVerifiedBackends() {
        assertEquals(
            setOf(LocalAiBackend.GPU, LocalAiBackend.CPU),
            LlamaSocProfiles.verifiedBackends(LlamaSocProfiles.GENERIC_GPU_MODEL)
        )
    }

    @Test
    fun unknownHardwareUsesPublishedGenericGpuCapableModel() {
        assertEquals(
            LlamaSocProfiles.GENERIC_GPU_MODEL,
            LlamaAccelerator.selectedModelFilename("Samsung", "Exynos 2400")
        )
    }

    @Test
    fun unverifiedNpuArtifactsAreNeverSelected() {
        assertEquals(null, LocalAiHardwareManager.potentialNpuBackend("Qualcomm", "SM8650"))
        assertEquals(null, LocalAiHardwareManager.potentialNpuBackend("MediaTek", "MT6993"))
        assertEquals(null, LocalAiHardwareManager.potentialNpuBackend("Google", "Tensor G5"))
    }

    @Test
    fun selectedArtifactHasLiteRtLmExtension() {
        assertTrue(
            LlamaAccelerator.selectedModelFilename("Qualcomm", "SM8650").endsWith(".litertlm")
        )
    }
}
