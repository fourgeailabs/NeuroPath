package com.fourgeailabs.neuropath.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Fun synthesized bubble-pop sounds for the Pop-It fidget pad.
 *
 * Each pop is generated on-device with math (no audio files, no downloads): a short
 * pitch-dropping sine "bloop" with a click transient and fast decay, like a real
 * silicone bubble. Several pitch variations are pre-rendered to WAV files in the
 * cache dir and played through SoundPool for low-latency polyphonic pops.
 */
class PopSoundPlayer(
    context: Context,
    private val scope: CoroutineScope
) {
    private val appContext = context.applicationContext
    private var soundPool: SoundPool? = null
    private val popSoundIds = mutableListOf<Int>()
    private val loadedIds = mutableSetOf<Int>()
    @Volatile private var ready = false

    init {
        scope.launch(Dispatchers.Default) {
            try {
                soundPool = SoundPool.Builder()
                    .setMaxStreams(6)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .build()
                val pool = soundPool ?: return@launch
                pool.setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0) {
                        synchronized(loadedIds) { loadedIds.add(sampleId) }
                        if (loadedIds.size == popSoundIds.size) ready = true
                    }
                }
                val cacheDir = File(appContext.cacheDir, "pop_sounds").apply { mkdirs() }
                // 5 pitch variations so rapid popping stays playful, not monotonous.
                val baseFreqs = floatArrayOf(520f, 590f, 660f, 740f, 830f)
                baseFreqs.forEachIndexed { index, freq ->
                    val wav = File(cacheDir, "pop_$index.wav")
                    if (!wav.exists()) writePopWav(wav, freq)
                    val id = pool.load(wav.absolutePath, 1)
                    synchronized(popSoundIds) { popSoundIds.add(id) }
                }
            } catch (e: Exception) {
                Log.w("PopSoundPlayer", "Pop sound init failed", e)
            }
        }
    }

    /** Plays one random bubble pop with a slight random pitch bend for variety. */
    fun playPop() {
        try {
            val pool = soundPool ?: return
            val ids = synchronized(popSoundIds) { popSoundIds.toList() }
            val loaded = synchronized(loadedIds) { loadedIds.toSet() }
            val available = ids.filter { it in loaded }
            if (available.isEmpty()) return
            val id = available.random()
            // Slight random playback-rate wobble keeps repeated pops fun.
            val rate = 0.94f + Random.nextFloat() * 0.18f
            pool.play(id, 0.9f, 0.9f, 1, 0, rate)
        } catch (_: Exception) {
        }
    }

    fun release() {
        try {
            soundPool?.release()
        } catch (_: Exception) {
        }
        soundPool = null
    }

    /**
     * Synthesizes one bubble pop: a 25ms click transient, then a sine "bloop"
     * whose pitch drops from [startFreq] to ~40% over 90ms with exponential decay.
     */
    private fun writePopWav(file: File, startFreq: Float) {
        val sampleRate = 22050
        val durationSec = 0.16
        val frames = (sampleRate * durationSec).toInt()
        val pcm = ShortArray(frames)
        var phase = 0.0
        repeat(frames) { i ->
            val t = i.toDouble() / sampleRate
            // Click transient: brief noise burst at the very start.
            val click = if (i < 220) (Random.nextDouble() * 2.0 - 1.0) * exp(-i / 40.0) * 0.5 else 0.0
            // Pitch drops exponentially from startFreq toward startFreq * 0.38.
            val freq = startFreq * (0.38 + 0.62 * exp(-t / 0.045))
            phase += 2.0 * PI * freq / sampleRate
            val body = sin(phase) * exp(-t / 0.05)
            // A touch of octave-up shimmer for a juicy pop.
            val shimmer = sin(phase * 2.0) * exp(-t / 0.03) * 0.25
            val sample = ((click + body + shimmer) * 0.55).coerceIn(-1.0, 1.0)
            pcm[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        writeWavMono16(file, pcm, sampleRate)
    }

    private fun writeWavMono16(file: File, pcm: ShortArray, sampleRate: Int) {
        val dataSize = pcm.size * 2
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray())
        header.putInt(36 + dataSize)
        header.put("WAVE".toByteArray())
        header.put("fmt ".toByteArray())
        header.putInt(16)
        header.putShort(1) // PCM
        header.putShort(1) // mono
        header.putInt(sampleRate)
        header.putInt(sampleRate * 2)
        header.putShort(2) // block align
        header.putShort(16) // bits per sample
        header.put("data".toByteArray())
        header.putInt(dataSize)
        FileOutputStream(file).use { out ->
            out.write(header.array())
            val bytes = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
            pcm.forEach { bytes.putShort(it) }
            out.write(bytes.array())
        }
    }
}
