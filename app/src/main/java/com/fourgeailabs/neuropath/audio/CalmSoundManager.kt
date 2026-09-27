package com.fourgeailabs.neuropath.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedurally synthesized ambient soundscapes. Every sound below is generated on-device
 * with math through AudioTrack — no audio samples, no downloads, no third-party libraries.
 *
 * Each soundscape is a small stateful synthesizer tuned to sound like its name:
 * - Rain: sparse high-frequency droplet "plinks" over soft airy noise.
 * - Ocean: slow ~16s swell envelopes over deep brown noise with a low wash rumble.
 * - Forest: wandering wind gusts with leaf-rustle puffs and rare distant bird chirps.
 * - Brown noise: deep low-frequency sensory blocker.
 * - Chimes: real struck chimes — pentatonic notes with inharmonic partials and long
 *   exponential decays, struck at gentle random intervals.
 */
enum class AmbientSoundType(
    val id: String,
    val title: String,
    val emoji: String,
    val description: String
) {
    OFF("OFF", "Soundscape Off", "🔇", "Quiet silent mode"),
    RAIN("RAIN", "Gentle Rain", "🌧️", "Soft rhythmic raindrops for ADHD focus"),
    OCEAN("OCEAN", "Ocean Swells", "🌊", "Slow rhythmic ocean tides for calm regulation"),
    FOREST("FOREST", "Forest Breeze", "🌲", "Soothing wind rustling through pine trees"),
    WHITE_NOISE("WHITE_NOISE", "Soft Brown Noise", "📻", "Deep low-frequency sensory blocker"),
    CHIMES("CHIMES", "Zen Chimes", "🔔", "Gentle resonant harmonic tones")
}

/** Stateful per-sample generator for one soundscape. */
private interface SoundscapeSynth {
    fun nextSample(): Double
}

private class RainSynth(private val sampleRate: Int) : SoundscapeSynth {
    private var airLp = 0.0
    private var dropT = -1.0
    private var dropFreq = 3000.0
    private var dropPhase = 0.0
    override fun nextSample(): Double {
        // Soft airy bed: gently low-passed white noise.
        val white = Random.nextDouble() * 2.0 - 1.0
        airLp += 0.06 * (white - airLp)
        // Sparse droplet plinks: short decaying high sine bursts (~18/sec).
        if (dropT < 0 && Random.nextInt(1225) == 0) {
            dropT = 0.0
            dropFreq = 1800.0 + Random.nextDouble() * 2800.0
            dropPhase = 0.0
        }
        var drop = 0.0
        if (dropT >= 0) {
            dropT += 1.0 / sampleRate
            dropPhase += 2.0 * Math.PI * dropFreq / sampleRate
            drop = sin(dropPhase) * exp(-dropT / 0.012) * 0.5
            if (dropT > 0.09) dropT = -1.0
        }
        return airLp * 0.35 + drop
    }
}

private class OceanSynth(private val sampleRate: Int) : SoundscapeSynth {
    private var brown = 0.0
    private var t = 0.0
    private var rumblePhase = 0.0
    override fun nextSample(): Double {
        t += 1.0 / sampleRate
        // ~16s primary swell + ~9s secondary swell, like waves arriving in sets.
        val swell = 0.55 + 0.30 * sin(2.0 * Math.PI * 0.062 * t) + 0.15 * sin(2.0 * Math.PI * 0.11 * t + 1.3)
        val white = Random.nextDouble() * 2.0 - 1.0
        brown = (brown + 0.03 * white) / 1.03
        rumblePhase += 2.0 * Math.PI * 52.0 / sampleRate
        val wash = sin(rumblePhase) * 0.05 * swell
        return (brown * 0.5 * swell) + wash
    }
}

private class ForestSynth(private val sampleRate: Int) : SoundscapeSynth {
    private var t = 0.0
    private var gust = 0.4
    private var gustTarget = 0.4
    private var gustTimer = 0
    private var rustleT = -1.0
    private var rustleLp = 0.0
    private var chirpT = -1.0
    private var chirpPhase = 0.0
    private var chirpFrom = 2800.0
    private var nextChirpAt = 6.0
    override fun nextSample(): Double {
        t += 1.0 / sampleRate
        // Slow wandering wind gusts: pick a new target strength every few seconds.
        if (--gustTimer <= 0) {
            gustTarget = 0.15 + Random.nextDouble() * 0.65
            gustTimer = (2.5 + Random.nextDouble() * 4.0).toInt() * sampleRate
        }
        gust += (gustTarget - gust) * 0.0004
        val white = Random.nextDouble() * 2.0 - 1.0
        // Leaf rustle: bandy noise puffs riding the gusts.
        if (rustleT < 0 && Random.nextDouble() < 0.00012 + gust * 0.0006) rustleT = 0.0
        var rustle = 0.0
        if (rustleT >= 0) {
            rustleT += 1.0 / sampleRate
            rustleLp += 0.35 * (white - rustleLp)
            rustle = rustleLp * exp(-rustleT / 0.09) * (0.3 + gust)
            if (rustleT > 0.4) rustleT = -1.0
        }
        // Rare distant bird chirp: gentle descending whistle every 8-20s.
        var chirp = 0.0
        if (chirpT < 0 && t > nextChirpAt) {
            chirpT = 0.0
            chirpPhase = 0.0
            chirpFrom = 2400.0 + Random.nextDouble() * 900.0
            nextChirpAt = t + 8.0 + Random.nextDouble() * 12.0
        }
        if (chirpT >= 0) {
            chirpT += 1.0 / sampleRate
            val freq = chirpFrom * (1.0 - 0.25 * (chirpT / 0.16))
            chirpPhase += 2.0 * Math.PI * freq / sampleRate
            val env = sin(Math.PI * (chirpT / 0.16).coerceIn(0.0, 1.0))
            chirp = sin(chirpPhase) * env * 0.06
            if (chirpT > 0.16) chirpT = -1.0
        }
        val windBed = white * 0.10 * gust
        return windBed + rustle + chirp
    }
}

private class BrownNoiseSynth : SoundscapeSynth {
    private var brown = 0.0
    override fun nextSample(): Double {
        val white = Random.nextDouble() * 2.0 - 1.0
        brown = (brown + 0.02 * white) / 1.02
        return brown * 0.35
    }
}

private class ChimesSynth(private val sampleRate: Int) : SoundscapeSynth {
    // Pentatonic: C5 D5 E5 G5 A5 C6 — always consonant, never clashes.
    private val notes = doubleArrayOf(523.25, 587.33, 659.25, 783.99, 880.0, 1046.5)
    private data class Strike(val freq: Double, val startT: Double, val velocity: Double)
    private val strikes = ArrayDeque<Strike>()
    private var t = 0.0
    private var nextStrikeAt = 0.4
    override fun nextSample(): Double {
        t += 1.0 / sampleRate
        if (t >= nextStrikeAt) {
            strikes.addLast(Strike(notes.random(), t, 0.5 + Random.nextDouble() * 0.5))
            if (strikes.size > 8) strikes.removeFirst()
            nextStrikeAt = t + 1.8 + Random.nextDouble() * 2.7
        }
        var out = 0.0
        val iter = strikes.iterator()
        while (iter.hasNext()) {
            val s = iter.next()
            val age = t - s.startT
            if (age > 6.0) {
                iter.remove()
                continue
            }
            // Inharmonic chime partials with a long dreamy decay.
            val decay = exp(-age / 2.2)
            out += s.velocity * decay * (
                sin(2.0 * Math.PI * s.freq * age) * 1.0 +
                    sin(2.0 * Math.PI * s.freq * 2.76 * age) * 0.35 * exp(-age / 1.2) +
                    sin(2.0 * Math.PI * s.freq * 5.40 * age) * 0.12 * exp(-age / 0.7)
                )
        }
        return out * 0.30
    }
}

class CalmSoundManager(private val scope: CoroutineScope) {

    private val _activeSound = MutableStateFlow(AmbientSoundType.OFF)
    val activeSound: StateFlow<AmbientSoundType> = _activeSound.asStateFlow()

    private var audioTrack: AudioTrack? = null
    private var soundJob: Job? = null
    private val sampleRate = 22050

    fun playSound(type: AmbientSoundType) {
        stopSound()
        if (type == AmbientSoundType.OFF) {
            _activeSound.value = AmbientSoundType.OFF
            return
        }

        _activeSound.value = type
        soundJob = scope.launch(Dispatchers.Default) {
            try {
                val bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(sampleRate)

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.play()

                val synth: SoundscapeSynth = when (type) {
                    AmbientSoundType.RAIN -> RainSynth(sampleRate)
                    AmbientSoundType.OCEAN -> OceanSynth(sampleRate)
                    AmbientSoundType.FOREST -> ForestSynth(sampleRate)
                    AmbientSoundType.WHITE_NOISE -> BrownNoiseSynth()
                    AmbientSoundType.CHIMES -> ChimesSynth(sampleRate)
                    AmbientSoundType.OFF -> BrownNoiseSynth()
                }

                val buffer = ShortArray(bufferSize / 2)
                // Gentle 0.8s fade-in so starting a soundscape never clicks.
                var fadeSamples = (sampleRate * 0.8).toInt()
                var sampleIndex = 0L

                while (isActive) {
                    for (i in buffer.indices) {
                        val raw = synth.nextSample()
                        val fade = if (fadeSamples > 0) {
                            fadeSamples--
                            1.0 - fadeSamples.toDouble() / (sampleRate * 0.8)
                        } else 1.0
                        sampleIndex++
                        buffer[i] = (raw.coerceIn(-1.0, 1.0) * fade * Short.MAX_VALUE * 0.4).toInt().toShort()
                    }
                    audioTrack?.write(buffer, 0, buffer.size)
                }
            } catch (_: Exception) {
            } finally {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (_: Exception) {}
                audioTrack = null
            }
        }
    }

    fun stopSound() {
        soundJob?.cancel()
        soundJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        _activeSound.value = AmbientSoundType.OFF
    }
}
