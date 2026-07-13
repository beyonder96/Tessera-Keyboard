import re

filepath = 'app/src/main/java/com/example/StitchKeyboardService.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Instead of removing views, we will just bind them to the fallback views we kept hidden in the XML
content = content.replace('R.id.key_plus', 'R.id.key_shift_top')
content = content.replace('R.id.key_sparkle', 'R.id.key_mic_top')

with open(filepath, 'w') as f:
    f.write(content)

print("Applied safe bindings")
