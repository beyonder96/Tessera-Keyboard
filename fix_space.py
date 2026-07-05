import re

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

old_space_down = """                MotionEvent.ACTION_DOWN -> {
                    spaceStartX = event.rawX
                    spaceStartY = event.rawY
                    isSpaceSwiping = false
                    lastCursorMoveX = spaceStartX
                    
                    v.isPressed = true
                    true
                }"""
                
new_space_down = """                MotionEvent.ACTION_DOWN -> {
                    spaceStartX = event.rawX
                    spaceStartY = event.rawY
                    isSpaceSwiping = false
                    lastCursorMoveX = spaceStartX
                    
                    triggerVibration()
                    v.isPressed = true
                    true
                }"""

content = content.replace(old_space_down, new_space_down)

old_back_down = """                MotionEvent.ACTION_DOWN -> {
                    backspaceStartX = event.rawX
                    backspaceStartY = event.rawY
                    isBackspaceSwiping = false
                    
                    handleBackspace()
                    triggerVibration()
                    v.isPressed = true
                    true
                }"""
                
# Backspace already has triggerVibration()! Let me double check if it's there.

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
