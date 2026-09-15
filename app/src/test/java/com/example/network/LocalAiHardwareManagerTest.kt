package com.example.network

import org.junit.Assert.assertEquals
import org.junit.Test

class LocalAiHardwareManagerTest {
    // Import the data classes from the main source
    import com.example.network.LocalAiBackend
    import com.example.network.LocalAiBackendProbe
    @Test
    fun npuCandidateIsIdentifiedWithoutClaimingItIsActive() {
        assertEquals("QUALCOMM_QNN", LocalAiHardwareManager.potentialNpuBackend("Qualcomm", "SM8650"))
        assertEquals("GOOGLE_TENSOR", LocalAiHardwareManager.potentialNpuBackend("Google", "Tensor G5"))
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
