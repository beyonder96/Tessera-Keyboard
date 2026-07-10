import re

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

# Replace rootLayout?.scaleX = scale
old_scale = """        val scale = getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE).getFloat("KEYBOARD_SCALE", 1.0f)
        if (::keyboardRoot.isInitialized) {
            val rootLayout = (keyboardRoot.parent as? android.widget.FrameLayout)
            rootLayout?.scaleX = scale
            rootLayout?.scaleY = scale
            
            rootLayout?.post {
                rootLayout.pivotY = rootLayout.height.toFloat()
                rootLayout.pivotX = rootLayout.width.toFloat() / 2f
            }
        }"""
        
new_scale = """        val scale = getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE).getFloat("KEYBOARD_SCALE", 1.0f)
        if (::keyboardRoot.isInitialized) {
            val rootLayout = (keyboardRoot.parent as? android.widget.FrameLayout)
            
            // Adjust row heights instead of visual scaling
            val rowsToScale = listOf(
                R.id.key_q, R.id.key_a, R.id.key_z, R.id.key_space
            )
            for (id in rowsToScale) {
                val key = keyboardRoot.findViewById<android.view.View>(id)
                val row = key?.parent as? android.view.View
                if (row != null) {
                    val lp = row.layoutParams
                    // Base heights: Q/A/Z = 52dp, Space = 44dp
                    val baseHeightDp = if (id == R.id.key_space) 44f else 52f
                    val density = resources.displayMetrics.density
                    lp.height = (baseHeightDp * density * scale).toInt()
                    row.layoutParams = lp
                }
            }
        }"""
        
content = content.replace(old_scale, new_scale)

# Also fix the dragging logic for scaling
old_drag = """                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.rawY - initialY
                    // Moving up (negative delta) increases scale, moving down decreases scale.
                    // A delta of -500 pixels could correspond to a scale increase of 0.3.
                    val scaleDelta = -(deltaY / 1000f)
                    var newScale = initialScale + scaleDelta
                    
                    if (newScale < 0.7f) newScale = 0.7f
                    if (newScale > 1.3f) newScale = 1.3f
                    
                    rootLayout.scaleX = newScale
                    rootLayout.scaleY = newScale
                    
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    keyboardRoot.alpha = 1.0f
                    v.isPressed = false
                    
                    val currentScale = rootLayout.scaleY
                    getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE).edit().putFloat("KEYBOARD_SCALE", currentScale).apply()
                    true
                }"""

new_drag = """                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.rawY - initialY
                    val scaleDelta = -(deltaY / 1000f)
                    var newScale = initialScale + scaleDelta
                    
                    if (newScale < 0.7f) newScale = 0.7f
                    if (newScale > 1.3f) newScale = 1.3f
                    
                    val rowsToScale = listOf(
                        R.id.key_q, R.id.key_a, R.id.key_z, R.id.key_space
                    )
                    for (id in rowsToScale) {
                        val key = keyboardRoot.findViewById<android.view.View>(id)
                        val row = key?.parent as? android.view.View
                        if (row != null) {
                            val lp = row.layoutParams
                            val baseHeightDp = if (id == R.id.key_space) 44f else 52f
                            val density = resources.displayMetrics.density
                            lp.height = (baseHeightDp * density * newScale).toInt()
                            row.layoutParams = lp
                        }
                    }
                    
                    // Store intermediate scale so we can save it on ACTION_UP
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
                
content = content.replace(old_drag, new_drag)

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
