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
    pass # ok
elif new_lines[-1].strip() == "":
    pass
else:
    if "}" not in new_lines[-1]:
        new_lines.append("}")

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.writelines(new_lines)
