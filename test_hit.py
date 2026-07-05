import re

with open('app/src/main/java/com/example/ui/SwipeGestureOverlay.kt', 'r') as f:
    content = f.read()

old_down = """            MotionEvent.ACTION_DOWN -> {
                path.reset()
                path.moveTo(x, y)
                swipeWord.clear()
                lastKey = null
                isSwiping = false
                val hit = checkKeyHit(x, y)
                if (hit != null) onKeyDown?.invoke(hit.first, hit.second)
                invalidate()
                return true
            }"""

new_down = """            MotionEvent.ACTION_DOWN -> {
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

content = content.replace(old_down, new_down)

with open('app/src/main/java/com/example/ui/SwipeGestureOverlay.kt', 'w') as f:
    f.write(content)
