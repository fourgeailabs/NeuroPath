package com.fourgeailabs.neuropath.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.ffmpegkit.llama.LlamaConfig
import dev.ffmpegkit.llama.LlamaModel
import dev.ffmpegkit.llama.LlamaResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicInteger

/**
 * Local-model lifecycle hardening for [LlamaLocalManager]:
 * - the load-state machine (IDLE → LOADING → READY → FAILED) only reaches
 *   READY after a real warm-up token, FAILED on any load/warm-up failure;
 * - concurrent loads are serialized: a second caller waits for the in-flight
 *   load instead of mapping the ~2GB file twice;
 * - [LlamaLocalManager.onAppBackgrounded] releases the model and frees
 *   native memory after the idle timeout; [LlamaLocalManager.onAppForegrounded]
 *   cancels a pending release;
 * - download resume-state sidecars round-trip, Content-Range totals parse,
 *   and [LlamaLocalManager.verifyModelIntegrity] detects a corrupted model file.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LlamaLocalManagerLifecycleTest {

    private lateinit var context: Context
    private lateinit var modelFile: File

    private class CountingRuntime(
        var loadDelayMs: Long = 300L,
        var completeBehavior: suspend () -> LlamaResult =
            { LlamaResult("hello", 1, 1f, 1L, 1L) },
    ) : LlamaRuntime {
        val loadCalls = AtomicInteger(0)
        val releasedModels = mutableListOf<LlamaModel>()
        override suspend fun loadModel(modelPath: String, config: LlamaConfig): LlamaModel {
            loadCalls.incrementAndGet()
            delay(loadDelayMs)
            return LlamaLoadHonestyTest.newFakeModel(loadCalls.get().toLong())
        }
        override suspend fun complete(
            model: LlamaModel,
            prompt: String,
            systemPrompt: String,
            maxTokens: Int
        ): LlamaResult = completeBehavior()
        override fun releaseModel(model: LlamaModel) {
            releasedModels += model
        }
    }

    private lateinit var fake: CountingRuntime

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val modelsDir = File(context.filesDir, "models").apply { mkdirs() }
        modelFile = File(modelsDir, "Llama-3.2-3B-Instruct-Q4_K_M.gguf")
        RandomAccessFile(modelFile, "rw").use { raf ->
            raf.write("GGUF".toByteArray(Charsets.US_ASCII))
            raf.setLength(1_600_000_000L)
        }
        // Clean any sidecars left by other tests in this JVM.
        LlamaLocalManager.modelSha256File(context).delete()
        LlamaLocalManager.downloadStateFile(context).delete()
        LlamaLocalManager.downloadTempFile(context).delete()

        LlamaLocalManager.MODEL_LOAD_TIMEOUT_MS = 10_000L
        LlamaLocalManager.WARMUP_TIMEOUT_MS = 10_000L
        LlamaLocalManager.GENERATION_TIMEOUT_MS = 10_000L
        LlamaLocalManager.BACKGROUND_RELEASE_DELAY_MS = 2 * 60 * 1000L
        fake = CountingRuntime()
        LlamaLocalManager.llamaRuntime = fake
    }

    @After
    fun tearDown() {
        runBlocking { LlamaLocalManager.releaseCachedModel() }
        LlamaLocalManager.onAppForegrounded()
        LlamaLocalManager.llamaRuntime = RealLlamaRuntime()
        LlamaLocalManager.MODEL_LOAD_TIMEOUT_MS = 5 * 60 * 1000L
        LlamaLocalManager.WARMUP_TIMEOUT_MS = 3 * 60 * 1000L
        LlamaLocalManager.GENERATION_TIMEOUT_MS = 5 * 60 * 1000L
        LlamaLocalManager.BACKGROUND_RELEASE_DELAY_MS = 2 * 60 * 1000L
        LlamaLocalManager.modelSha256File(context).delete()
        LlamaLocalManager.downloadStateFile(context).delete()
        LlamaLocalManager.downloadTempFile(context).delete()
        modelFile.delete()
    }

    @Test
    fun successfulPreload_reachesReadyState() = runBlocking {
        assertTrue(LlamaLocalManager.preloadModel(context))
        assertEquals(
            LlamaLocalManager.ModelLoadState.READY,
            LlamaLocalManager.modelLoadState.value
        )
        assertTrue(LlamaLocalManager.isModelWarm())
    }

    @Test
    fun failedWarmup_reachesFailedState_neverReady() = runBlocking {
        fake.completeBehavior = { throw RuntimeException("native inference crashed") }

        assertFalse(LlamaLocalManager.preloadModel(context))
        assertEquals(
            LlamaLocalManager.ModelLoadState.FAILED,
            LlamaLocalManager.modelLoadState.value
        )
        assertFalse(LlamaLocalManager.isModelWarm())
    }

    @Test
    fun concurrentPreloads_mapModelExactlyOnce() = runBlocking {
        val results = (1..5).map {
            async { LlamaLocalManager.preloadModel(context) }
        }.awaitAll()

        assertTrue("every caller gets the loaded model", results.all { it })
        assertEquals("one load for five concurrent callers", 1, fake.loadCalls.get())
        assertEquals(
            LlamaLocalManager.ModelLoadState.READY,
            LlamaLocalManager.modelLoadState.value
        )
    }

    @Test
    fun appBackgrounded_releasesModelAfterIdleTimeout() = runBlocking {
        LlamaLocalManager.BACKGROUND_RELEASE_DELAY_MS = 100L
        assertTrue(LlamaLocalManager.preloadModel(context))
        assertTrue(LlamaLocalManager.isModelWarm())

        LlamaLocalManager.onAppBackgrounded()
        delay(800)

        assertTrue("native model must be freed on background idle timeout", fake.releasedModels.isNotEmpty())
        assertFalse(LlamaLocalManager.isModelWarm())
        assertEquals(
            LlamaLocalManager.ModelLoadState.IDLE,
            LlamaLocalManager.modelLoadState.value
        )
    }

    @Test
    fun appForegrounded_cancelsPendingBackgroundRelease() = runBlocking {
        LlamaLocalManager.BACKGROUND_RELEASE_DELAY_MS = 200L
        assertTrue(LlamaLocalManager.preloadModel(context))

        LlamaLocalManager.onAppBackgrounded()
        delay(50)
        LlamaLocalManager.onAppForegrounded()
        delay(500)

        assertTrue("cancelled release must keep the warm model", fake.releasedModels.isEmpty())
        assertTrue(LlamaLocalManager.isModelWarm())
        assertEquals(
            LlamaLocalManager.ModelLoadState.READY,
            LlamaLocalManager.modelLoadState.value
        )
    }

    @Test
    fun computeSha256Hex_matchesKnownVector() {
        val file = File(context.cacheDir, "sha-test.txt")
        file.writeText("abc")
        try {
            assertEquals(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                LlamaLocalManager.computeSha256Hex(file)
            )
        } finally {
            file.delete()
        }
    }

    @Test
    fun downloadState_sidecarRoundTrip() {
        LlamaLocalManager.writeDownloadState(context, "https://example.com/model.gguf", 12345L)
        val state = LlamaLocalManager.readDownloadState(context)
        assertEquals("https://example.com/model.gguf", state?.url)
        assertEquals(12345L, state?.expectedTotalBytes)

        LlamaLocalManager.clearDownloadState(context)
        assertNull(LlamaLocalManager.readDownloadState(context))
    }

    @Test
    fun parseContentRangeTotal_parsesTotals() {
        assertEquals(2000L, LlamaLocalManager.parseContentRangeTotal("bytes 100-199/2000"))
        assertEquals(1234L, LlamaLocalManager.parseContentRangeTotal("bytes */1234"))
        assertNull(LlamaLocalManager.parseContentRangeTotal("bytes 0-99/*"))
        assertNull(LlamaLocalManager.parseContentRangeTotal("garbage"))
    }

    @Test
    fun verifyModelIntegrity_detectsCorruption() {
        assertEquals(
            "no sidecar yet: unverified, never silently verified",
            LlamaLocalManager.ModelIntegrity.UNVERIFIED_NO_RECORD,
            LlamaLocalManager.verifyModelIntegrity(context)
        )

        LlamaLocalManager.writeModelSha256Sidecar(context, modelFile)
        assertEquals(
            LlamaLocalManager.ModelIntegrity.VERIFIED,
            LlamaLocalManager.verifyModelIntegrity(context)
        )

        // Corrupt the file: one appended byte changes size, mtime, and hash.
        RandomAccessFile(modelFile, "rw").use { it.seek(it.length()); it.write(1) }
        try {
            assertEquals(
                LlamaLocalManager.ModelIntegrity.CORRUPT,
                LlamaLocalManager.verifyModelIntegrity(context)
            )
        } finally {
            // Restore the pristine sparse file for the other tests.
            RandomAccessFile(modelFile, "rw").use { raf ->
                raf.setLength(1_600_000_000L)
                raf.seek(0)
                raf.write("GGUF".toByteArray(Charsets.US_ASCII))
            }
            LlamaLocalManager.modelSha256File(context).delete()
        }
    }
}
