with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

start_idx = content.find('private fun setupKeys(view: View) {')
end_idx = content.find('private fun setupCommandKeys(view: View) {') - 4

new_method = """    private fun setupKeys(view: View) {
        val idMap = mapOf(
            R.id.key_q to "q", R.id.key_w to "w", R.id.key_e to "e", R.id.key_r to "r",
            R.id.key_t to "t", R.id.key_y to "y", R.id.key_u to "u", R.id.key_i to "i",
            R.id.key_o to "o", R.id.key_p to "p",
            R.id.key_a to "a", R.id.key_s to "s", R.id.key_d to "d", R.id.key_f to "f",
            R.id.key_g to "g", R.id.key_h to "h", R.id.key_j to "j", R.id.key_k to "k",
            R.id.key_l to "l", R.id.key_cedilla to "ç",
            R.id.key_z to "z", R.id.key_x to "x", R.id.key_c to "c", R.id.key_v to "v",
            R.id.key_b to "b", R.id.key_n to "n", R.id.key_m to "m"
        )
        alphabetKeys.clear()
        keyViews.clear()

        for ((id, char) in idMap) {
            val keyView = view.findViewById<android.widget.TextView>(id) ?: continue
            alphabetKeys[id] = char
            keyViews.add(keyView)

            keyView.setOnTouchListener { v, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        val uppercaseChar = if (isShifted) char.uppercase() else char.lowercase()
                        handleCharacterClick(char)
                        triggerVibration()
                        showKeyPopup(keyView, uppercaseChar)
                        v.isPressed = true
                        true
                    }
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        hideKeyPopup()
                        v.isPressed = false
                        true
                    }
                    else -> false
                }
            }
        }

        val swipeOverlay = view.findViewById<com.example.ui.SwipeGestureOverlay>(R.id.swipe_overlay)
        if (swipeOverlay != null) {
            val keyList = mutableListOf<Pair<android.widget.TextView, String>>()
            for ((id, char) in idMap) {
                val keyView = view.findViewById<android.widget.TextView>(id) ?: continue
                keyList.add(Pair(keyView, char))
            }
            swipeOverlay.setKeys(keyList)
            
            val typedValue = android.util.TypedValue()
            theme.resolveAttribute(R.attr.stitchGlowColor, typedValue, true)
            swipeOverlay.setThemeColor(typedValue.data)

            swipeOverlay.onSwipeComplete = { wordPattern ->
                val prediction = PredictionEngine().getSwipePrediction(wordPattern)
                if (prediction != null) {
                    val ic = currentInputConnection
                    ic?.commitText(prediction + " ", 1)
                    playClickFeedback()
                } else {
                    android.widget.Toast.makeText(this, "Palavra não encontrada", android.widget.Toast.LENGTH_SHORT).show()
                }
                updatePredictions()
            }
            
            swipeOverlay.onSwipeChar = { char ->
                handleCharacterClick(char)
            }
            
            swipeOverlay.onSwipeStart = {
                triggerVibration()
            }
        }
    }
"""

content = content[:start_idx] + new_method + content[end_idx:]

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
