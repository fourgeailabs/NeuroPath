package com.example.audio

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
import kotlin.math.sin
import kotlin.random.Random

enum class AmbientSoundType(
    val id: String,
    val title: String,
    val emoji: String,
    val description: String,
    val providerSource: String = "Epidemic Sound (https://www.epidemicsound.com/sound-effects/)"
) {
    OFF("OFF", "Soundscape Off", "🔇", "Quiet silent mode", "None"),
    RAIN("RAIN", "Gentle Rain", "🌧️", "Soft rhythmic raindrops for ADHD focus", "Epidemic Sound Library"),
    OCEAN("OCEAN", "Ocean Swells", "🌊", "Slow rhythmic ocean tides for calm regulation", "Epidemic Sound Library"),
    FOREST("FOREST", "Forest Breeze", "🌲", "Soothing wind rustling through pine trees", "Epidemic Sound Library"),
    WHITE_NOISE("WHITE_NOISE", "Soft Brown Noise", "📻", "Deep low-frequency sensory blocker", "Epidemic Sound Library"),
    CHIMES("CHIMES", "Zen Chimes", "🔔", "Gentle resonant harmonic tones", "Epidemic Sound Library")
}

class CalmSoundManager(private val scope: CoroutineScope) {

    companion object {
        const val EPIDEMIC_SOUND_URL = "https://www.epidemicsound.com/sound-effects/"
        const val PROVIDER_LABEL = "Sample Audio by Epidemic Sound"
    }

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

                val buffer = ShortArray(bufferSize / 2)
                var phase = 0.0
                var brownNoiseVal = 0.0

                while (isActive) {
                    for (i in buffer.indices) {
                        val sample = when (type) {
                            AmbientSoundType.RAIN -> {
                                val white = (Random.nextDouble() * 2.0 - 1.0)
                                brownNoiseVal = (brownNoiseVal + (0.04 * white)) / 1.04
                                val raindrop = if (Random.nextInt(800) == 0) Random.nextDouble() * 0.3 else 0.0
                                (brownNoiseVal * 0.25 + raindrop)
                            }
                            AmbientSoundType.OCEAN -> {
                                phase += 2.0 * Math.PI * 0.08 / sampleRate
                                val envelope = (sin(phase) + 1.0) * 0.5
                                val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.18
                                (noise * envelope)
                            }
                            AmbientSoundType.FOREST -> {
                                phase += 2.0 * Math.PI * 0.05 / sampleRate
                                val windMod = (sin(phase) + 1.0) * 0.4
                                val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.12
                                (noise * windMod)
                            }
                            AmbientSoundType.WHITE_NOISE -> {
                                val white = (Random.nextDouble() * 2.0 - 1.0)
                                brownNoiseVal = (brownNoiseVal + (0.02 * white)) / 1.02
                                (brownNoiseVal * 0.3)
                            }
                            AmbientSoundType.CHIMES -> {
                                phase += 2.0 * Math.PI * 432.0 / sampleRate
                                val fundamental = sin(phase) * 0.12
                                val overtone = sin(phase * 1.5) * 0.05
                                (fundamental + overtone)
                            }
                            AmbientSoundType.OFF -> 0.0
                        }
                        buffer[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE * 0.4).toInt().toShort()
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
