with open("app/src/main/java/com/example/StitchKeyboardService.kt", "r") as f:
    content = f.read()

target = "val themedContext = ContextThemeWrapper(this, R.style.Theme_MyApplication)"
replacement = """val themePref = getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE).getString("KEYBOARD_THEME", "Dark")
            val themeResId = if (themePref == "Light") R.style.Theme_Tessera_Light else R.style.Theme_Tessera_Dark
            val themedContext = ContextThemeWrapper(this, themeResId)"""
            
content = content.replace(target, replacement)

# We also need to fix shiftIcon color filter using theme attribute
# R.color.stitch_glow -> resolved via theme
# To get color from theme:
# val typedValue = TypedValue()
# themedContext.theme.resolveAttribute(R.attr.stitchGlowColor, typedValue, true)
# typedValue.data
shift_color = """        if (::shiftIcon.isInitialized) {
            if (isShifted) {
                val typedValue = android.util.TypedValue()
                theme.resolveAttribute(R.attr.stitchGlowColor, typedValue, true)
                shiftIcon.setColorFilter(typedValue.data)
            } else {
                shiftIcon.clearColorFilter()
            }
        }"""
        
# Find the updateShiftUI
import re
content = re.sub(r'        if \(::shiftIcon\.isInitialized\) \{[\s\S]*?\}', shift_color, content, count=1)

with open("app/src/main/java/com/example/StitchKeyboardService.kt", "w") as f:
    f.write(content)
