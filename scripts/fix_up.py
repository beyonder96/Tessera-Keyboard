import re

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

content = content.replace("var initialScale = 1.0f", "var initialScale = 1.0f\n        var currentScale = 1.0f")

content = content.replace("newScale = newScale.coerceIn(0.6f, 1.4f)", "newScale = newScale.coerceIn(0.6f, 1.4f)\n                    currentScale = newScale")

content = content.replace("val currentScale = rootLayout.scaleY", "")

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
