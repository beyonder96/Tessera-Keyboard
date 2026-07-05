import re

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

old_vib = """    private fun triggerVibration() {
        try {
            if (vibrator != null && vibrator!!.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator!!.vibrate(android.os.VibrationEffect.createOneShot(20, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator!!.vibrate(20)
                }
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }"""

new_vib = """    private fun triggerVibration() {
        try {
            if (::keyboardRoot.isInitialized) {
                keyboardRoot.performHapticFeedback(
                    android.view.HapticFeedbackConstants.KEYBOARD_TAP,
                    android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }"""

content = content.replace(old_vib, new_vib)

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
