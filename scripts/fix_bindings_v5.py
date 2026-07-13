import re

filepath = 'app/src/main/java/com/example/StitchKeyboardService.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Fix unresolved references
content = re.sub(r'iaBtn\?.*?$', '// iaBtn removed', content, flags=re.MULTILINE)
content = re.sub(r'.*iaBtn.*', '// removed', content)

with open(filepath, 'w') as f:
    f.write(content)

print("Applied Kotlin binding fixes for iaBtn.")
