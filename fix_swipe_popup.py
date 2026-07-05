with open('app/src/main/java/com/example/ui/SwipeGestureOverlay.kt', 'r') as f:
    content = f.read()

new_callbacks = """
    var onSwipeComplete: ((String) -> Unit)? = null
    var onSwipeChar: ((String) -> Unit)? = null
    var onSwipeStart: (() -> Unit)? = null
    var onKeyDown: ((View, String) -> Unit)? = null
    var onKeyUp: (() -> Unit)? = null
"""

content = content.replace("""    var onSwipeComplete: ((String) -> Unit)? = null
    var onSwipeChar: ((String) -> Unit)? = null
    var onSwipeStart: (() -> Unit)? = null""", new_callbacks)

new_down = """
            MotionEvent.ACTION_DOWN -> {
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
            
content = content.replace("""            MotionEvent.ACTION_DOWN -> {
                path.reset()
                path.moveTo(x, y)
                swipeWord.clear()
                lastKey = null
                isSwiping = false
                checkKeyHit(x, y)
                invalidate()
                return true
            }""", new_down)

new_up = """
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                onKeyUp?.invoke()
                if (isSwiping && swipeWord.length > 1) {
                    onSwipeComplete?.invoke(swipeWord.toString())
                } else if (swipeWord.length > 0) {
                    onSwipeChar?.invoke(swipeWord.toString())
                }
                path.reset()
                invalidate()
                isSwiping = false
                return true
            }"""
            
content = content.replace("""            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isSwiping && swipeWord.length > 1) {
                    onSwipeComplete?.invoke(swipeWord.toString())
                } else if (swipeWord.length > 0) {
                    onSwipeChar?.invoke(swipeWord.toString())
                }
                path.reset()
                invalidate()
                isSwiping = false
                return true
            }""", new_up)

new_check = """
    private fun checkKeyHit(x: Float, y: Float): Pair<TextView, String>? {
        for ((view, char) in keys) {
            val location = IntArray(2)
            view.getLocationInWindow(location)
            
            val myLocation = IntArray(2)
            getLocationInWindow(myLocation)
            
            val viewX = location[0] - myLocation[0]
            val viewY = location[1] - myLocation[1]
            
            if (x >= viewX && x <= viewX + view.width &&
                y >= viewY && y <= viewY + view.height) {
                if (char != lastKey) {
                    swipeWord.append(char)
                    lastKey = char
                }
                return Pair(view, char)
            }
        }
        return null
    }"""
    
old_check = """    private fun checkKeyHit(x: Float, y: Float) {
        for ((view, char) in keys) {
            val location = IntArray(2)
            view.getLocationInWindow(location)
            
            val myLocation = IntArray(2)
            getLocationInWindow(myLocation)
            
            val viewX = location[0] - myLocation[0]
            val viewY = location[1] - myLocation[1]
            
            if (x >= viewX && x <= viewX + view.width &&
                y >= viewY && y <= viewY + view.height) {
                if (char != lastKey) {
                    swipeWord.append(char)
                    lastKey = char
                }
                break
            }
        }
    }"""
content = content.replace(old_check, new_check)

with open('app/src/main/java/com/example/ui/SwipeGestureOverlay.kt', 'w') as f:
    f.write(content)
