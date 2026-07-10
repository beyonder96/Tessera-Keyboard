with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

# fix end
content = content.strip()
if content.endswith("}"):
    pass
else:
    content += "\n}"

# add settingsRoot initialization
idx = content.find('keyboardRoot = view.findViewById(R.id.keyboard_root)')
if idx != -1:
    content = content[:idx] + 'settingsRoot = view.findViewById(R.id.settings_ui_root)\n        ' + content[idx:]
else:
    # fallback
    idx = content.find('emojiRoot = view.findViewById(R.id.emoji_ui_root)')
    content = content[:idx] + 'settingsRoot = view.findViewById(R.id.settings_ui_root)\n        ' + content[idx:]

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
