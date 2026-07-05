import re

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

old_bg = """            tv.background = androidx.core.content.ContextCompat.getDrawable(this, android.R.attr.selectableItemBackgroundBorderless)"""
new_bg = """            val typedValue = android.util.TypedValue()
            theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typedValue, true)
            tv.setBackgroundResource(typedValue.resourceId)"""

content = content.replace(old_bg, new_bg)

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
