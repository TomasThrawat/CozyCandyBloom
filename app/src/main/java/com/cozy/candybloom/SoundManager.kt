package com.cozy.candybloom

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.sin

/**
 * Generates soft, wind-chime-like tones on the fly (sine wave + a quiet
 * overtone, gentle attack/decay envelope). No external audio files needed,
 * which keeps the whole game self-contained and offline-buildable.
 */
class SoundManager {

    private val sampleRate = 44100
    private val executor = Executors.newSingleThreadExecutor()

    private fun envelopeAt(i: Int, total: Int): Double {
        val attack = (total * 0.06).coerceAtLeast(1.0)
        return if (i < attack) {
            i / attack
        } else {
            val decayProgress = (i - attack) / (total - attack)
            (1.0 - decayProgress).coerceIn(0.0, 1.0)
        }
    }

    private fun playTone(freqHz: Double, durationMs: Int, volume: Float) {
        executor.execute {
            val numSamples = (durationMs / 1000.0 * sampleRate).toInt().coerceAtLeast(1)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i / sampleRate.toDouble()
                val env = envelopeAt(i, numSamples)
                val wave = sin(2 * PI * freqHz * t) * 0.8 + sin(2 * PI * freqHz * 2 * t) * 0.12
                buffer[i] = (wave * env * volume * Short.MAX_VALUE).toInt().toShort()
            }

            val bufBytes = buffer.size * 2
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufBytes)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            Thread.sleep((buffer.size * 1000L / sampleRate) + 60)
            track.release()
        }
    }

    /** Quiet click when the player swaps two candies. */
    fun playSwap() = playTone(440.0, 90, 0.14f)

    /** Warm chime when a match clears; bigger combos ring a slightly higher, richer note. */
    fun playMatch(comboSize: Int) {
        val notes = doubleArrayOf(523.25, 587.33, 659.25, 698.46, 783.99, 880.0)
        val note = notes[(comboSize - 3).coerceIn(0, notes.size - 1)]
        playTone(note, 220, 0.20f)
    }

    /** Soft high bell for cascade (chain) reactions. */
    fun playCascade() = playTone(987.77, 260, 0.16f)

    /** Gentle low tone for an invalid move, so it never feels harsh. */
    fun playInvalid() = playTone(220.0, 120, 0.10f)

    fun release() {
        executor.shutdown()
    }
}
