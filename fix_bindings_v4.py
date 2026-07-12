import re

filepath = 'app/src/main/java/com/example/StitchKeyboardService.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Fix unresolved references
content = re.sub(r'suggestion_1\?.*?$', '// suggestion_1 removed', content, flags=re.MULTILINE)
content = re.sub(r'suggestion_2\?.*?$', '// suggestion_2 removed', content, flags=re.MULTILINE)
content = re.sub(r'suggestion_3\?.*?$', '// suggestion_3 removed', content, flags=re.MULTILINE)
content = re.sub(r'suggestion_1 = view.*?$', '// suggestion_1 removed', content, flags=re.MULTILINE)
content = re.sub(r'suggestion_2 = view.*?$', '// suggestion_2 removed', content, flags=re.MULTILINE)
content = re.sub(r'suggestion_3 = view.*?$', '// suggestion_3 removed', content, flags=re.MULTILINE)
content = re.sub(r'val keyIa.*?$', '// keyIa removed', content, flags=re.MULTILINE)

# Also fix the specific lines mentioned in the error
content = re.sub(r'.*suggestion_1.*', '// removed', content)
content = re.sub(r'.*suggestion_2.*', '// removed', content)
content = re.sub(r'.*suggestion_3.*', '// removed', content)
content = re.sub(r'.*key_ia.*', '// removed', content)


with open(filepath, 'w') as f:
    f.write(content)

print("Applied Kotlin binding fixes for removed views aggressively.")
