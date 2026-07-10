import re

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

old_drag = """                    // Store intermediate scale so we can save it on ACTION_UP
                    v.setTag(R.id.key_q, newScale)
                    
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    keyboardRoot.alpha = 1.0f
                    v.isPressed = false
                    
                    val currentScale = v.getTag(R.id.key_q) as? Float ?: initialScale
                    getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE).edit().putFloat("KEYBOARD_SCALE", currentScale).apply()
                    true
                }"""

new_drag = """                    initialScale = newScale // keep updated
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    keyboardRoot.alpha = 1.0f
                    v.isPressed = false
                    
                    val currentScale = initialScale
                    getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE).edit().putFloat("KEYBOARD_SCALE", currentScale).apply()
                    true
                }"""
                
content = content.replace(old_drag, new_drag)

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
