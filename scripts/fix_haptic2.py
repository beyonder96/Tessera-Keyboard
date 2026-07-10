import re

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

# Let's check if settingsRoot is properly initialized
if "settingsRoot = view.findViewById(R.id.settings_ui_root)" not in content and "settingsRoot = keyboardView.findViewById(R.id.settings_ui_root)" not in content:
    print("WARNING settings root is missing!")
