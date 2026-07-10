import re

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

old_drag = """                MotionEvent.ACTION_MOVE -> {
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
                }"""

new_drag = """                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.rawY - initialY
                    val scaleDelta = -(deltaY / 1000f)
                    var newScale = initialScale + scaleDelta
                    newScale = newScale.coerceIn(0.6f, 1.4f)
                    
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
                    keyboardRoot.requestLayout()
                    
                    true
                }"""

if old_drag in content:
    content = content.replace(old_drag, new_drag)
else:
    print("Not found old_drag")

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
