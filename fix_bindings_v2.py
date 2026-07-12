import re

filepath = 'app/src/main/java/com/example/StitchKeyboardService.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Make sure key_shift refers to key_shift_top, suggestion_1 etc to key_disabled_2 children
content = content.replace('R.id.key_plus', 'R.id.key_shift_top')
content = content.replace('R.id.key_sparkle', 'R.id.key_mic_top')

with open(filepath, 'w') as f:
    f.write(content)

print("Applied Kotlin binding fixes safely.")
