import re

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

# Replace getSystemService with cached vars
init_vars = """
    private var vibrator: android.os.Vibrator? = null
    private var audioManager: android.media.AudioManager? = null
    private var inputMethodManager: android.view.inputmethod.InputMethodManager? = null

    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        audioManager = getSystemService(android.content.Context.AUDIO_SERVICE) as? android.media.AudioManager
        inputMethodManager = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
    }

"""

# We need to insert onCreate. Let's check if it exists.
if 'override fun onCreate()' not in content:
    idx = content.find('override fun onCreateInputView(): View {')
    content = content[:idx] + init_vars + content[idx:]

# update playClickFeedback
old_click = """    private fun playClickFeedback() {
        val am = getSystemService(AUDIO_SERVICE) as? android.media.AudioManager
        am?.playSoundEffect(android.media.AudioManager.FX_KEYPRESS_STANDARD)
    }"""
new_click = """    private fun playClickFeedback() {
        audioManager?.playSoundEffect(android.media.AudioManager.FX_KEYPRESS_STANDARD)
    }"""
content = content.replace(old_click, new_click)

# update triggerVibration
old_vib = """    private fun triggerVibration() {
        try {
            val vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(20, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(20)
                }
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }"""
new_vib = """    private fun triggerVibration() {
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
content = content.replace(old_vib, new_vib)

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
