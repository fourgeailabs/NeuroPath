package com.fourgeailabs.neuropath.network

import org.junit.Assert.assertEquals
import org.junit.Test

class LocalAiHardwareManagerTest {
    @Test
    fun unverifiedNpuHardwareDoesNotClaimNpuSupport() {
        assertEquals(null, LocalAiHardwareManager.potentialNpuBackend("Qualcomm", "SM8650"))
        assertEquals(null, LocalAiHardwareManager.potentialNpuBackend("Google", "Tensor G5"))
        assertEquals(null, LocalAiHardwareManager.potentialNpuBackend("MediaTek", "MT6993"))
    }

    @Test
    fun activeBackendMustHaveSuccessfulRuntimeProbe() {
        val selected = LocalAiHardwareManager.selectBackend(
            probes = listOf(
                LocalAiBackendProbe(LocalAiBackend.NPU, false, "hardware present but runtime unavailable"),
                LocalAiBackendProbe(LocalAiBackend.GPU, false, "initialization failed"),
                LocalAiBackendProbe(LocalAiBackend.CPU, true, "initialized")
            ),
            cloudAvailable = false
        )
        assertEquals(LocalAiBackend.CPU, selected)
    }

    @Test
    fun unavailableWhenNoLocalBackendAndCloudDisabled() {
        val selected = LocalAiHardwareManager.selectBackend(
            probes = listOf(
                LocalAiBackendProbe(LocalAiBackend.NPU, false, "unsupported"),
                LocalAiBackendProbe(LocalAiBackend.GPU, false, "unsupported"),
                LocalAiBackendProbe(LocalAiBackend.CPU, false, "model invalid")
            ),
            cloudAvailable = false
        )
        assertEquals(LocalAiBackend.UNAVAILABLE, selected)
    }

    @Test
    fun unknownHardwareDoesNotClaimNpuSupport() {
        assertEquals(null, LocalAiHardwareManager.potentialNpuBackend("Unknown", "MysterySoC"))
    }

    @Test
    fun backendSelectionUsesActualRuntimeProbePriority() {
        val selected = LocalAiHardwareManager.selectBackend(
            probes = listOf(
                LocalAiBackendProbe(LocalAiBackend.NPU, false, "runtime unavailable"),
                LocalAiBackendProbe(LocalAiBackend.GPU, true, "initialized"),
                LocalAiBackendProbe(LocalAiBackend.CPU, true, "initialized")
            ),
            cloudAvailable = true
        )
        assertEquals(LocalAiBackend.GPU, selected)
    }

    @Test
    fun cpuWinsWhenAcceleratorsFail() {
        val selected = LocalAiHardwareManager.selectBackend(
            probes = listOf(
                LocalAiBackendProbe(LocalAiBackend.NPU, false, "unsupported model"),
                LocalAiBackendProbe(LocalAiBackend.GPU, false, "delegate failed"),
                LocalAiBackendProbe(LocalAiBackend.CPU, true, "initialized")
            ),
            cloudAvailable = true
        )
        assertEquals(LocalAiBackend.CPU, selected)
    }

    @Test
    fun cloudIsOnlySelectedWhenNoLocalBackendInitialized() {
        val selected = LocalAiHardwareManager.selectBackend(
            probes = listOf(
                LocalAiBackendProbe(LocalAiBackend.NPU, false, "unsupported"),
                LocalAiBackendProbe(LocalAiBackend.GPU, false, "unsupported"),
                LocalAiBackendProbe(LocalAiBackend.CPU, false, "insufficient memory")
            ),
            cloudAvailable = true
        )
        assertEquals(LocalAiBackend.CLOUD, selected)
    }
}
