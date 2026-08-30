package com.example.audio

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

class AudioRecorderHelper(private val context: Context) {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var outputStream: ByteArrayOutputStream? = null
    private var recordingThread: Thread? = null

    fun startRecording(): Boolean {
        if (isRecording) return true
        try {
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
                return false
            }

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                minBufferSize * 2
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                return false
            }

            outputStream = ByteArrayOutputStream()
            isRecording = true
            audioRecord?.startRecording()

            recordingThread = Thread {
                val buffer = ByteArray(minBufferSize)
                while (isRecording) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        outputStream?.write(buffer, 0, read)
                    }
                }
            }.apply { start() }

            return true
        } catch (_: Exception) {
            isRecording = false
            return false
        }
    }

    suspend fun stopRecording(): ByteArray? = withContext(Dispatchers.IO) {
        if (!isRecording) return@withContext null
        isRecording = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null

        try {
            recordingThread?.join(500)
        } catch (_: Exception) {}
        recordingThread = null

        val pcmData = outputStream?.toByteArray() ?: return@withContext null
        outputStream = null

        if (pcmData.isEmpty()) return@withContext null

        // Convert raw PCM 16-bit to standard WAV format
        return@withContext addWavHeader(pcmData, sampleRate, 1, 16)
    }

    fun isCurrentlyRecording(): Boolean = isRecording

    private fun addWavHeader(
        pcmData: ByteArray,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ): ByteArray {
        val totalAudioLen = pcmData.size.toLong()
        val totalDataLen = totalAudioLen + 36
        val byteRate = (sampleRate * channels * bitsPerSample / 8).toLong()

        val header = ByteArray(44)
        // RIFF/WAVE header
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xffL).toByte()
        header[5] = ((totalDataLen shr 8) and 0xffL).toByte()
        header[6] = ((totalDataLen shr 16) and 0xffL).toByte()
        header[7] = ((totalDataLen shr 24) and 0xffL).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        // 'fmt ' chunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1 (PCM)
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate.toLong() and 0xffL).toByte()
        header[25] = ((sampleRate.toLong() shr 8) and 0xffL).toByte()
        header[26] = ((sampleRate.toLong() shr 16) and 0xffL).toByte()
        header[27] = ((sampleRate.toLong() shr 24) and 0xffL).toByte()
        header[28] = (byteRate and 0xffL).toByte()
        header[29] = ((byteRate shr 8) and 0xffL).toByte()
        header[30] = ((byteRate shr 16) and 0xffL).toByte()
        header[31] = ((byteRate shr 24) and 0xffL).toByte()
        header[32] = (channels * bitsPerSample / 8).toByte() // block align
        header[33] = 0
        header[34] = bitsPerSample.toByte() // bits per sample
        header[35] = 0
        // 'data' chunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xffL).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xffL).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xffL).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xffL).toByte()

        val wavStream = ByteArrayOutputStream(header.size + pcmData.size)
        wavStream.write(header)
        wavStream.write(pcmData)
        return wavStream.toByteArray()
    }
}
