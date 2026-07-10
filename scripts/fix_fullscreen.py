import re

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

new_methods = """
    override fun onEvaluateFullscreenMode(): Boolean {
        return false
    }

    override fun onEvaluateInputViewShown(): Boolean {
        return super.onEvaluateInputViewShown()
    }
    
"""

if "onEvaluateFullscreenMode" not in content:
    content = content.replace("    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {", new_methods + "    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {")

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
