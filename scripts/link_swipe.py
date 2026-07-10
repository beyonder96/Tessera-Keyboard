with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

setup_keys_idx = content.find('private fun setupKeys(view: View) {')
end_setup_keys = content.find('}', content.find('keyViews.add(keyView)', setup_keys_idx))

# Find the end of setupKeys loop
while content[end_setup_keys] != '}':
    end_setup_keys += 1
end_setup_keys = content.find('}', end_setup_keys + 1) + 1 # End of the loop
end_setup_keys = content.find('}', end_setup_keys + 1) + 1 # End of method setupKeys

swipe_setup = """
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
"""

content = content[:end_setup_keys - 1] + swipe_setup + content[end_setup_keys - 1:]

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
