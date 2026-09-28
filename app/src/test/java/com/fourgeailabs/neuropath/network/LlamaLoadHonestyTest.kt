package com.fourgeailabs.neuropath.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.ffmpegkit.llama.LlamaConfig
import dev.ffmpegkit.llama.LlamaModel
import dev.ffmpegkit.llama.LlamaResult
import kotlinx.coroutines.awaitCancellation
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

/**
 * Proves the local-model load state machine stays honest: "Ready!" is only
 * ever reported after the model actually generates a token. A warmup that
 * throws, hangs, or a load that fails must surface as a failed load with the
 * loading-screen error kept visible — never a cached-but-broken model.
 *
 * Uses a fake [LlamaRuntime] so no 2GB model or native library is needed.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LlamaLoadHonestyTest {

    private lateinit var context: Context
    private lateinit var modelFile: File

    private class FakeRuntime(
        var loadResult: Result<LlamaModel> =
            Result.success(newFakeModel()),
        var completeBehavior: suspend () -> LlamaResult =
            { LlamaResult("hello", 1, 1f, 1L, 1L) },
    ) : LlamaRuntime {
        val releasedModels = mutableListOf<LlamaModel>()
        override suspend fun loadModel(modelPath: String, config: LlamaConfig): LlamaModel =
            loadResult.getOrThrow()
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

    private lateinit var fake: FakeRuntime

    companion object {
        /**
         * The library declares LlamaModel's constructor internal, so tests
         * reach it via reflection (it's public at the bytecode level).
         */
        fun newFakeModel(handle: Long = 1L): LlamaModel {
            val ctor = LlamaModel::class.java.getDeclaredConstructor(
                java.lang.Long.TYPE,
                LlamaConfig::class.java
            )
            ctor.isAccessible = true
            return ctor.newInstance(handle, LlamaConfig())
        }
    }

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Fake an installed GGUF: sparse 1.6GB file carrying the GGUF magic.
        // setLength() does not allocate the bytes, so this stays fast.
        val modelsDir = File(context.filesDir, "models").apply { mkdirs() }
        modelFile = File(modelsDir, "Llama-3.2-3B-Instruct-Q4_K_M.gguf")
        RandomAccessFile(modelFile, "rw").use { raf ->
            raf.write("GGUF".toByteArray(Charsets.US_ASCII))
            raf.setLength(1_600_000_000L)
        }
        // Shrink timeouts so the hang test runs in ~1s instead of minutes.
        LlamaLocalManager.MODEL_LOAD_TIMEOUT_MS = 5_000L
        LlamaLocalManager.WARMUP_TIMEOUT_MS = 1_000L
        LlamaLocalManager.GENERATION_TIMEOUT_MS = 5_000L
        fake = FakeRuntime()
        LlamaLocalManager.llamaRuntime = fake
    }

    @After
    fun tearDown() {
        runBlocking { LlamaLocalManager.releaseCachedModel() }
        LlamaLocalManager.llamaRuntime = RealLlamaRuntime()
        LlamaLocalManager.MODEL_LOAD_TIMEOUT_MS = 5 * 60 * 1000L
        LlamaLocalManager.WARMUP_TIMEOUT_MS = 3 * 60 * 1000L
        LlamaLocalManager.GENERATION_TIMEOUT_MS = 5 * 60 * 1000L
        modelFile.delete()
    }

    @Test
    fun successfulWarmup_marksLoadReadyAndCachesModel() = runBlocking {
        val ok = LlamaLocalManager.preloadModel(context)

        assertTrue("preload should succeed when the warmup token generates", ok)
        assertTrue("model should be warm after a verified load", LlamaLocalManager.isModelWarm())
        assertEquals("Ready!", LlamaLocalManager.loadStage.value)
        assertNull(LlamaLocalManager.loadError.value)
        assertTrue("verified model must not be released", fake.releasedModels.isEmpty())
    }

    @Test
    fun failedWarmup_marksLoadFailed_releasesModel_setsError() = runBlocking {
        fake.completeBehavior = { throw RuntimeException("native inference crashed") }

        val ok = LlamaLocalManager.preloadModel(context)

        assertFalse("preload must fail when the warmup token cannot generate", ok)
        assertFalse("a model that cannot think must never be cached as warm", LlamaLocalManager.isModelWarm())
        assertEquals("Hmm, that didn't work…", LlamaLocalManager.loadStage.value)
        assertTrue(
            "loading screen must explain the brain loaded but couldn't think",
            LlamaLocalManager.loadError.value.orEmpty().contains("couldn't think")
        )
        assertEquals("broken model handle must be released, not cached", 1, fake.releasedModels.size)
    }

    @Test
    fun hungWarmup_timesOutAsFailure_withoutCaching() = runBlocking {
        fake.completeBehavior = { awaitCancellation() }

        val ok = LlamaLocalManager.preloadModel(context)

        assertFalse("preload must fail when the warmup hangs past its timeout", ok)
        assertFalse("a hung model must never be cached as warm", LlamaLocalManager.isModelWarm())
        assertTrue(
            "loading screen must report the timeout",
            LlamaLocalManager.loadError.value.orEmpty().contains("timed out")
        )
        assertTrue(
            "a timed-out (possibly still running) native call must not be freed underneath itself",
            fake.releasedModels.isEmpty()
        )
    }

    @Test
    fun failedModelLoad_marksLoadFailed() = runBlocking {
        fake.loadResult = Result.failure(RuntimeException("mmap failed"))

        val ok = LlamaLocalManager.preloadModel(context)

        assertFalse("preload must fail when the weights cannot be mapped", ok)
        assertFalse(LlamaLocalManager.isModelWarm())
        assertTrue(
            LlamaLocalManager.loadError.value.orEmpty().contains("Couldn't load the AI brain")
        )
    }

    @Test
    fun generationFailureAfterVerifiedWarmup_returnsSafeError() = runBlocking {
        // Warm up successfully first so the model is cached…
        assertTrue(LlamaLocalManager.preloadModel(context))
        // …then break generation: the chat must get an honest error, not a hang.
        fake.completeBehavior = { throw RuntimeException("decode exploded") }

        val reply = LlamaLocalManager.generateLlamaResponse(
            context = context,
            prompt = "What is 2 + 2?"
        )

        assertTrue(
            "generation failure must surface as a safe error message",
            reply.contains("I couldn't answer that")
        )
        assertFalse(
            "raw native exception text must never reach the chat",
            reply.contains("decode exploded")
        )
    }
}
