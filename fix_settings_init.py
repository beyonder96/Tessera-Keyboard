with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if line.strip() == "settingsRoot = view.findViewById(R.id.settings_ui_root)":
        continue
    if line.strip() == "}":
        # we have an extra } at the end
        pass
    new_lines.append(line)

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.writelines(new_lines)
