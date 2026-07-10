import re

with open("app/src/main/java/com/example/StitchKeyboardService.kt", "r") as f:
    content = f.read()

target = """    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        
        isShifted = false
        updateShiftUI()"""

replacement = """    private var currentTheme = "Dark"

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        
        val newTheme = getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE).getString("KEYBOARD_THEME", "Dark") ?: "Dark"
        if (newTheme != currentTheme) {
            currentTheme = newTheme
            setInputView(onCreateInputView())
        }

        isShifted = false
        updateShiftUI()"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/StitchKeyboardService.kt", "w") as f:
    f.write(content)
