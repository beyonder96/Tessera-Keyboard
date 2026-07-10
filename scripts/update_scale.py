with open("app/src/main/java/com/example/StitchKeyboardService.kt", "r") as f:
    content = f.read()

target = "isShifted = false\n        updateShiftUI()"
insertion = """
        isShifted = false
        updateShiftUI()

        val scale = getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE).getFloat("KEYBOARD_SCALE", 1.0f)
        if (::keyboardRoot.isInitialized) {
            val rootLayout = (keyboardRoot.parent as? android.widget.FrameLayout)
            rootLayout?.scaleX = scale
            rootLayout?.scaleY = scale
            
            rootLayout?.post {
                rootLayout.pivotY = rootLayout.height.toFloat()
                rootLayout.pivotX = rootLayout.width.toFloat() / 2
            }
        }
"""
content = content.replace(target, insertion)

with open("app/src/main/java/com/example/StitchKeyboardService.kt", "w") as f:
    f.write(content)
