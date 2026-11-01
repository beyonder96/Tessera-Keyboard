package com.example.manager

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * High-performance sensory haptics engine for Tessera Keyboard.
 * Uses modern VibrationEffect.Composition primitives on Android 11+ (API 30+),
 * amplitude-scaled one-shot effects on API 26-29, and falls back to view-level
 * haptic constants if hardware composition is unavailable.
 */
class HapticEngine(private val context: Context) {

    var isEnabled: Boolean = true
    var durationMs: Int = 15 // 1..50 ms slider

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    enum class HapticType {
        KEY_PRESS,
        SPECIAL_KEY,
        MICRO_TICK,
        GLIDE_COMMIT
    }

    fun perform(type: HapticType, fallbackView: View? = null) {
        if (!isEnabled || durationMs <= 0) return
        val vib = vibrator
        if (vib == null || !vib.hasVibrator()) {
            fallbackView?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            return
        }

        val clampedDuration = durationMs.coerceIn(1, 50)
        val scale = clampedDuration.toFloat() / 50f // 0.02f..1.0f
        val amplitude = (scale * 255).toInt().coerceIn(1, 255)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val composition = VibrationEffect.startComposition()
                when (type) {
                    HapticType.KEY_PRESS -> {
                        composition.addPrimitive(
                            VibrationEffect.Composition.PRIMITIVE_CLICK,
                            scale.coerceIn(0.12f, 1.0f)
                        )
                    }
                    HapticType.SPECIAL_KEY -> {
                        composition.addPrimitive(
                            VibrationEffect.Composition.PRIMITIVE_THUD,
                            scale.coerceIn(0.2f, 1.0f)
                        )
                    }
                    HapticType.MICRO_TICK -> {
                        composition.addPrimitive(
                            VibrationEffect.Composition.PRIMITIVE_TICK,
                            (scale * 0.6f).coerceIn(0.08f, 0.7f)
                        )
                    }
                    HapticType.GLIDE_COMMIT -> {
                        composition.addPrimitive(
                            VibrationEffect.Composition.PRIMITIVE_QUICK_RISE,
                            scale.coerceIn(0.15f, 1.0f)
                        )
                    }
                }
                vib.vibrate(composition.compose())
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val duration = when (type) {
                    HapticType.KEY_PRESS -> clampedDuration.toLong().coerceIn(1L, 35L)
                    HapticType.SPECIAL_KEY -> (clampedDuration.toLong() * 1.3f).toLong().coerceIn(2L, 50L)
                    HapticType.MICRO_TICK -> (clampedDuration.toLong() * 0.5f).toLong().coerceIn(1L, 15L)
                    HapticType.GLIDE_COMMIT -> (clampedDuration.toLong() * 1.2f).toLong().coerceIn(2L, 45L)
                }
                vib.vibrate(VibrationEffect.createOneShot(duration, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(clampedDuration.toLong().coerceIn(1L, 50L))
            }
        } catch (_: Exception) {
            fallbackView?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    fun performKeyPress(fallbackView: View? = null) = perform(HapticType.KEY_PRESS, fallbackView)
    fun performSpecialKey(fallbackView: View? = null) = perform(HapticType.SPECIAL_KEY, fallbackView)
    fun performMicroTick(fallbackView: View? = null) = perform(HapticType.MICRO_TICK, fallbackView)
    fun performGlideCommit(fallbackView: View? = null) = perform(HapticType.GLIDE_COMMIT, fallbackView)
}
