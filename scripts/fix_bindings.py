import re

filepath = 'app/src/main/java/com/example/StitchKeyboardService.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Make sure key_shift refers to key_shift_top, suggestion_1 etc to key_disabled_2 children
# The user wants Aa to toggle shift
content = content.replace('R.id.key_plus', 'R.id.key_shift_top')

# Ensure we bind a click listener for the top shift
shift_top_setup = '''
        val topShift = view.findViewById<View>(R.id.key_shift_top)
        topShift?.setOnClickListener {
            handleShiftClick()
        }
        
        val topMic = view.findViewById<View>(R.id.key_mic_top)
        topMic?.setOnClickListener {
            keyboardRoot.visibility = View.GONE
            voiceRoot.visibility = View.VISIBLE
        }
'''

content = content.replace('updateKeyLabels()', f'updateKeyLabels()\n{shift_top_setup}')

with open(filepath, 'w') as f:
    f.write(content)

print("Applied Kotlin binding fixes.")
