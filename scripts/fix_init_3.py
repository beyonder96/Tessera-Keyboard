with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if line.strip() == "settingsRoot = view.findViewById(R.id.settings_ui_root)":
        continue
    new_lines.append(line)

while len(new_lines) > 0 and new_lines[-1].strip() == "":
    new_lines.pop()

if new_lines[-1].strip() == "}":
    pass
else:
    new_lines.append("}\n")

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.writelines(new_lines)

# now inject correctly
with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()
    
idx = content.find('emojiRoot = keyboardView.findViewById(R.id.emoji_ui_root)')
if idx != -1:
    content = content[:idx] + 'settingsRoot = keyboardView.findViewById(R.id.settings_ui_root)\n            ' + content[idx:]

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
