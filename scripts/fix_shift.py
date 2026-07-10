with open("app/src/main/java/com/example/StitchKeyboardService.kt", "r") as f:
    lines = f.readlines()

new_lines = lines[:368] + [
"        if (::shiftIcon.isInitialized) {\n",
"            if (isShifted) {\n",
"                val typedValue = android.util.TypedValue()\n",
"                theme.resolveAttribute(R.attr.stitchGlowColor, typedValue, true)\n",
"                shiftIcon.setColorFilter(typedValue.data)\n",
"            } else {\n",
"                shiftIcon.clearColorFilter()\n",
"            }\n",
"        }\n",
"    }\n"
] + lines[382:]

with open("app/src/main/java/com/example/StitchKeyboardService.kt", "w") as f:
    f.writelines(new_lines)
