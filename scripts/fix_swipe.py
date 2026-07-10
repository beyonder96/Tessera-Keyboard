import re

with open('app/src/main/java/com/example/ui/SwipeGestureOverlay.kt', 'r') as f:
    content = f.read()

# add startX and startY to the class
if 'private var startX = 0f' not in content:
    content = content.replace("private var isSwiping = false", "private var isSwiping = false\n    private var startX = 0f\n    private var startY = 0f")

old_down = """            MotionEvent.ACTION_DOWN -> {
                val hit = checkKeyHit(x, y)
                if (hit == null) return false // Let it pass to command keys
                
                path.reset()
                path.moveTo(x, y)
                swipeWord.clear()
                lastKey = null
                isSwiping = false
                onKeyDown?.invoke(hit.first, hit.second)
                invalidate()
                return true
            }"""
            
new_down = """            MotionEvent.ACTION_DOWN -> {
                val hit = checkKeyHit(x, y)
                if (hit == null) return false // Let it pass to command keys
                
                startX = x
                startY = y
                path.reset()
                path.moveTo(x, y)
                swipeWord.clear()
                lastKey = null
                isSwiping = false
                onKeyDown?.invoke(hit.first, hit.second)
                invalidate()
                return true
            }"""

content = content.replace(old_down, new_down)

old_move = """            MotionEvent.ACTION_MOVE -> {
                if (event.historySize > 0) {
                    val dx = Math.abs(x - event.getHistoricalX(0))
                    val dy = Math.abs(y - event.getHistoricalY(0))
                    if (dx > 5 || dy > 5) {
                        if (!isSwiping) {
                            isSwiping = true
                            onSwipeStart?.invoke()
                        }
                    }
                }
                path.lineTo(x, y)
                checkKeyHit(x, y)
                invalidate()
                return true
            }"""
            
new_move = """            MotionEvent.ACTION_MOVE -> {
                val dx = Math.abs(x - startX)
                val dy = Math.abs(y - startY)
                if (dx > 10f || dy > 10f) {
                    if (!isSwiping) {
                        isSwiping = true
                        onSwipeStart?.invoke()
                    }
                }
                path.lineTo(x, y)
                checkKeyHit(x, y)
                invalidate()
                return true
            }"""

content = content.replace(old_move, new_move)

with open('app/src/main/java/com/example/ui/SwipeGestureOverlay.kt', 'w') as f:
    f.write(content)
