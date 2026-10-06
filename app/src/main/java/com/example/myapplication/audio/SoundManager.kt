package com.example.myapplication.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.myapplication.domain.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Gestor de efectos de sonido adaptativos según el tema seleccionado.
 */
class SoundManager {

    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Reproduce el sonido de movimiento adaptado al tema actual.
     */
    fun playHumanMove(theme: AppTheme, soundEnabled: Boolean) {
        if (!soundEnabled) return
        scope.launch {
            when (theme) {
                AppTheme.CLASSIC -> playTone(frequency = 523.25, durationMs = 100) // Tono clásico C5
                AppTheme.COSTA -> playTone(frequency = 783.99, durationMs = 90)   // Tono gota de agua G5
                AppTheme.LLANO -> playTone(frequency = 329.63, durationMs = 110)   // Tono acústico llanero E4
                AppTheme.VAQUERO -> playTone(frequency = 659.25, durationMs = 120) // Tono silbato vaquero E5
            }
        }
    }

    /**
     * Reproduce el sonido de la computadora adaptado al tema.
     */
    fun playComputerMove(theme: AppTheme, soundEnabled: Boolean) {
        if (!soundEnabled) return
        scope.launch {
            when (theme) {
                AppTheme.CLASSIC -> playTone(frequency = 392.00, durationMs = 120) // Tono clásico G4
                AppTheme.COSTA -> playTone(frequency = 659.25, durationMs = 100)   // Tono eco marino E5
                AppTheme.LLANO -> playTone(frequency = 261.63, durationMs = 130)   // Tono repique C4
                AppTheme.VAQUERO -> playTone(frequency = 440.00, durationMs = 130) // Tono guitarra A4
            }
        }
    }

    /**
     * Reproduce el sonido de victoria (aplica local, vs CPU o multijugador online).
     */
    fun playWinSound(soundEnabled: Boolean, theme: AppTheme) {
        if (!soundEnabled) return
        scope.launch {
            when (theme) {
                AppTheme.CLASSIC -> {
                    playTone(frequency = 523.25, durationMs = 150) // Do5
                    playTone(frequency = 659.25, durationMs = 150) // Mi5
                    playTone(frequency = 783.99, durationMs = 350) // Sol5
                }
                AppTheme.COSTA -> {
                    playTone(frequency = 783.99, durationMs = 150) 
                    playTone(frequency = 880.00, durationMs = 150) 
                    playTone(frequency = 1046.50, durationMs = 350)
                }
                AppTheme.LLANO -> {
                    playTone(frequency = 329.63, durationMs = 150) 
                    playTone(frequency = 440.00, durationMs = 150) 
                    playTone(frequency = 659.25, durationMs = 350)
                }
                AppTheme.VAQUERO -> {
                    playTone(frequency = 659.25, durationMs = 150) 
                    playTone(frequency = 783.99, durationMs = 150) 
                    playTone(frequency = 987.77, durationMs = 350) 
                }
            }
        }
    }

    /**
     * Reproduce el sonido de derrota (aplica cuando CPU gana o cuando oponente online gana).
     */
    fun playLoseSound(soundEnabled: Boolean, theme: AppTheme) {
        if (!soundEnabled) return
        scope.launch {
            when (theme) {
                AppTheme.CLASSIC -> {
                    playTone(frequency = 392.00, durationMs = 200) // Sol4
                    playTone(frequency = 329.63, durationMs = 200) // Mi4
                    playTone(frequency = 261.63, durationMs = 400) // Do4
                }
                AppTheme.COSTA -> {
                    playTone(frequency = 659.25, durationMs = 200) 
                    playTone(frequency = 587.33, durationMs = 200) 
                    playTone(frequency = 523.25, durationMs = 400) 
                }
                AppTheme.LLANO -> {
                    playTone(frequency = 261.63, durationMs = 200) 
                    playTone(frequency = 220.00, durationMs = 200) 
                    playTone(frequency = 164.81, durationMs = 400) 
                }
                AppTheme.VAQUERO -> {
                    playTone(frequency = 440.00, durationMs = 200) 
                    playTone(frequency = 392.00, durationMs = 200) 
                    playTone(frequency = 329.63, durationMs = 400) 
                }
            }
        }
    }

    /**
     * Reproduce el sonido de empate.
     */
    fun playTieSound(soundEnabled: Boolean) {
        if (!soundEnabled) return
        scope.launch {
            playTone(frequency = 329.63, durationMs = 150)
            playTone(frequency = 329.63, durationMs = 200)
        }
    }

    private fun playTone(frequency: Double, durationMs: Int) {
        val sampleRate = 44100
        val numSamples = (durationMs * sampleRate) / 1000
        val sample = DoubleArray(numSamples)
        val generatedSnd = ByteArray(2 * numSamples)

        for (i in 0 until numSamples) {
            sample[i] = sin(2.0 * Math.PI * i.toDouble() / (sampleRate / frequency))
        }

        var idx = 0
        for (dVal in sample) {
            val valInt = (dVal * 32767).toInt().toShort()
            generatedSnd[idx++] = (valInt.toInt() and 0x00ff).toByte()
            generatedSnd[idx++] = (valInt.toInt() and 0xff00 ushr 8).toByte()
        }

        val audioTrack = AudioTrack.Builder()
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
            .setBufferSizeInBytes(generatedSnd.size)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(generatedSnd, 0, generatedSnd.size)
        audioTrack.play()
    }
}
