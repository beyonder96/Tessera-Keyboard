with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

new_callbacks = """
            swipeOverlay.onKeyDown = { view, char ->
                val uppercaseChar = if (isShifted) char.uppercase() else char.lowercase()
                showKeyPopup(view, uppercaseChar)
                view.isPressed = true
                triggerVibration()
            }
            
            swipeOverlay.onKeyUp = {
                hideKeyPopup()
                for (k in keyViews) { k.isPressed = false }
            }
            
            swipeOverlay.onSwipeComplete = { wordPattern ->
"""

content = content.replace("            swipeOverlay.onSwipeComplete = { wordPattern ->", new_callbacks)

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
