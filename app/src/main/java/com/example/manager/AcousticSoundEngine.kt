package com.example.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * In-memory acoustic sound engine with zero extra APK asset weight.
 * Synthesizes pristine 44.1kHz 16-bit PCM waveforms for mechanical Thock,
 * Crisp clicks, and Editorial Typewriter acoustic profiles, playing them
 * with ultra-low latency through SoundPool.
 */
class AcousticSoundEngine(private val context: Context) {

    var isEnabled: Boolean = true
    var volume: Float = 0.5f // 0.0f..1.0f
    var profile: String = "thock" // "thock", "crisp", "typewriter", "system"

    private val audioManager: AudioManager? by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<String, Int>()
    private var isInitialized = false

    fun init() {
        if (isInitialized) return
        try {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(attributes)
                .build()

            val cacheDir = File(context.cacheDir, "tessera_audio")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            // Synthesize and load acoustic profiles
            loadOrSynthesize(cacheDir, "thock_std", ::synthesizeThockStd)
            loadOrSynthesize(cacheDir, "thock_spc", ::synthesizeThockSpc)
            loadOrSynthesize(cacheDir, "crisp_std", ::synthesizeCrispStd)
            loadOrSynthesize(cacheDir, "crisp_spc", ::synthesizeCrispSpc)
            loadOrSynthesize(cacheDir, "typewriter_std", ::synthesizeTypewriterStd)
            loadOrSynthesize(cacheDir, "typewriter_spc", ::synthesizeTypewriterSpc)

            isInitialized = true
        } catch (_: Exception) {
            // Audio synthesis fallback to system clicks
        }
    }

    private fun loadOrSynthesize(cacheDir: File, name: String, synthesizer: () -> ByteArray) {
        val file = File(cacheDir, "$name.wav")
        if (!file.exists() || file.length() < 44) {
            val wavBytes = synthesizer()
            FileOutputStream(file).use { it.write(wavBytes) }
        }
        val soundId = soundPool?.load(file.absolutePath, 1) ?: 0
        if (soundId != 0) {
            soundMap[name] = soundId
        }
    }

    fun playSound(isSpecialKey: Boolean = false) {
        if (!isEnabled || volume <= 0.01f) return

        if (profile == "system" || soundPool == null) {
            val fx = if (isSpecialKey) AudioManager.FX_KEYPRESS_SPACEBAR else AudioManager.FX_KEYPRESS_STANDARD
            audioManager?.playSoundEffect(fx, volume.coerceIn(0f, 1f))
            return
        }

        val keyName = when (profile) {
            "crisp" -> if (isSpecialKey) "crisp_spc" else "crisp_std"
            "typewriter" -> if (isSpecialKey) "typewriter_spc" else "typewriter_std"
            else -> if (isSpecialKey) "thock_spc" else "thock_std" // Default: thock
        }

        val soundId = soundMap[keyName]
        if (soundId != null && soundId != 0) {
            val v = volume.coerceIn(0.01f, 1.0f)
            soundPool?.play(soundId, v, v, 1, 0, 1.0f)
        } else {
            audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, volume.coerceIn(0f, 1f))
        }
    }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            soundMap.clear()
            isInitialized = false
        } catch (_: Exception) {}
    }

    companion object {
        private const val SAMPLE_RATE = 44100

        /**
         * Builds a 44-byte standard RIFF WAV header for mono 16-bit PCM.
         */
        fun buildWavHeader(dataSize: Int): ByteArray {
            val buffer = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            buffer.put("RIFF".toByteArray())
            buffer.putInt(36 + dataSize)
            buffer.put("WAVE".toByteArray())
            buffer.put("fmt ".toByteArray())
            buffer.putInt(16) // Subchunk1Size (16 for PCM)
            buffer.putShort(1) // AudioFormat (1 = PCM)
            buffer.putShort(1) // NumChannels (1 = Mono)
            buffer.putInt(SAMPLE_RATE) // SampleRate
            buffer.putInt(SAMPLE_RATE * 2) // ByteRate (SampleRate * NumChannels * BitsPerSample/8)
            buffer.putShort(2) // BlockAlign (NumChannels * BitsPerSample/8)
            buffer.putShort(16) // BitsPerSample
            buffer.put("data".toByteArray())
            buffer.putInt(dataSize)
            return buffer.array()
        }

        /**
         * Mechanical Thock: Standard key (30ms). Pitch slide from 130Hz to 75Hz with steep exponential decay
         * and a 3ms initial impact burst.
         */
        fun synthesizeThockStd(): ByteArray {
            val durationSec = 0.030f
            val numSamples = (SAMPLE_RATE * durationSec).toInt()
            val pcmData = ByteArray(numSamples * 2)
            val buffer = ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val progress = t / durationSec
                val freq = 130.0 - 55.0 * progress
                val sine = sin(2.0 * PI * freq * t)
                val decay = exp(-progress * 5.2)
                val noise = if (i < 120) (Random.nextFloat() * 2.0 - 1.0) * (1.0 - i / 120.0) * 0.4 else 0.0
                val sample = ((sine * 0.8 + noise) * decay * 28000.0).toInt().coerceIn(-32767, 32767)
                buffer.putShort(sample.toShort())
            }

            return buildWavHeader(pcmData.size) + pcmData
        }

        /**
         * Mechanical Thock: Special key (42ms, deeper 90Hz to 55Hz pitch).
         */
        fun synthesizeThockSpc(): ByteArray {
            val durationSec = 0.042f
            val numSamples = (SAMPLE_RATE * durationSec).toInt()
            val pcmData = ByteArray(numSamples * 2)
            val buffer = ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val progress = t / durationSec
                val freq = 95.0 - 40.0 * progress
                val sine = sin(2.0 * PI * freq * t)
                val decay = exp(-progress * 4.6)
                val noise = if (i < 160) (Random.nextFloat() * 2.0 - 1.0) * (1.0 - i / 160.0) * 0.35 else 0.0
                val sample = ((sine * 0.85 + noise) * decay * 29000.0).toInt().coerceIn(-32767, 32767)
                buffer.putShort(sample.toShort())
            }

            return buildWavHeader(pcmData.size) + pcmData
        }

        /**
         * Minimal Crisp: Standard key (15ms, high 2200Hz click).
         */
        fun synthesizeCrispStd(): ByteArray {
            val durationSec = 0.015f
            val numSamples = (SAMPLE_RATE * durationSec).toInt()
            val pcmData = ByteArray(numSamples * 2)
            val buffer = ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val progress = t / durationSec
                val sine = sin(2.0 * PI * 2200.0 * t)
                val decay = exp(-progress * 7.5)
                val sample = (sine * decay * 25000.0).toInt().coerceIn(-32767, 32767)
                buffer.putShort(sample.toShort())
            }

            return buildWavHeader(pcmData.size) + pcmData
        }

        /**
         * Minimal Crisp: Special key (20ms, 1500Hz click).
         */
        fun synthesizeCrispSpc(): ByteArray {
            val durationSec = 0.020f
            val numSamples = (SAMPLE_RATE * durationSec).toInt()
            val pcmData = ByteArray(numSamples * 2)
            val buffer = ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val progress = t / durationSec
                val sine = sin(2.0 * PI * 1500.0 * t)
                val decay = exp(-progress * 6.5)
                val sample = (sine * decay * 26000.0).toInt().coerceIn(-32767, 32767)
                buffer.putShort(sample.toShort())
            }

            return buildWavHeader(pcmData.size) + pcmData
        }

        /**
         * Editorial Typewriter: Standard key (28ms, dual 840Hz + 2200Hz metallic click).
         */
        fun synthesizeTypewriterStd(): ByteArray {
            val durationSec = 0.028f
            val numSamples = (SAMPLE_RATE * durationSec).toInt()
            val pcmData = ByteArray(numSamples * 2)
            val buffer = ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val progress = t / durationSec
                val tone1 = sin(2.0 * PI * 840.0 * t)
                val tone2 = sin(2.0 * PI * 2200.0 * t) * 0.4
                val decay = exp(-progress * 5.8)
                val sample = ((tone1 + tone2) * decay * 24000.0).toInt().coerceIn(-32767, 32767)
                buffer.putShort(sample.toShort())
            }

            return buildWavHeader(pcmData.size) + pcmData
        }

        /**
         * Editorial Typewriter: Special key (38ms, deeper resonance 620Hz + 1600Hz).
         */
        fun synthesizeTypewriterSpc(): ByteArray {
            val durationSec = 0.038f
            val numSamples = (SAMPLE_RATE * durationSec).toInt()
            val pcmData = ByteArray(numSamples * 2)
            val buffer = ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val progress = t / durationSec
                val tone1 = sin(2.0 * PI * 620.0 * t)
                val tone2 = sin(2.0 * PI * 1600.0 * t) * 0.45
                val decay = exp(-progress * 5.0)
                val sample = ((tone1 + tone2) * decay * 26000.0).toInt().coerceIn(-32767, 32767)
                buffer.putShort(sample.toShort())
            }

            return buildWavHeader(pcmData.size) + pcmData
        }
    }
}
