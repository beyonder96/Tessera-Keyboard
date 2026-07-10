import re

with open("app/src/main/java/com/example/StitchKeyboardService.kt", "r") as f:
    content = f.read()

# Add call to setupDragResizer
content = content.replace("setupSuggestionBar(keyboardView)", "setupSuggestionBar(keyboardView)\n            setupDragResizer(keyboardView)")

# Add the function definition
func = """
    @SuppressLint("ClickableViewAccessibility")
    private fun setupDragResizer(view: View) {
        val dragHandle = view.findViewById<View>(R.id.drag_handle_container)
        var initialY = 0f
        var initialScale = 1.0f
        
        dragHandle?.setOnTouchListener { v, event ->
            val rootLayout = (keyboardRoot.parent as? android.widget.FrameLayout) ?: return@setOnTouchListener false
            
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = event.rawY
                    initialScale = getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE).getFloat("KEYBOARD_SCALE", 1.0f)
                    keyboardRoot.alpha = 0.6f
                    v.isPressed = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.rawY - initialY
                    // Moving up (negative delta) increases scale, moving down decreases scale.
                    // A delta of -500 pixels could correspond to a scale increase of 0.3.
                    val scaleDelta = -(deltaY / 1000f)
                    var newScale = initialScale + scaleDelta
                    newScale = newScale.coerceIn(0.6f, 1.4f)
                    
                    rootLayout.pivotY = rootLayout.height.toFloat()
                    rootLayout.pivotX = rootLayout.width.toFloat() / 2f
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
                }
                else -> false
            }
        }
    }
"""

# Insert before setupKeys
content = content.replace('    @SuppressLint("ClickableViewAccessibility")\n    private fun setupKeys(view: View) {', func + '\n    @SuppressLint("ClickableViewAccessibility")\n    private fun setupKeys(view: View) {')

with open("app/src/main/java/com/example/StitchKeyboardService.kt", "w") as f:
    f.write(content)
