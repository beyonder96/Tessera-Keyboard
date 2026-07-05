import re
with open("app/src/main/java/com/example/StitchKeyboardService.kt", "r") as f:
    content = f.read()

target = """            val themePref = getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE).getString("KEYBOARD_THEME", "Dark")"""
replacement = """            val themePref = getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE).getString("KEYBOARD_THEME", "Dark") ?: "Dark"
            currentTheme = themePref"""

content = content.replace(target, replacement)
with open("app/src/main/java/com/example/StitchKeyboardService.kt", "w") as f:
    f.write(content)
