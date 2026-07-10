import re

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

content = content.replace("val spaceKey = view.findViewById<TextView>(R.id.key_space)", "val spaceKey = view.findViewById<com.example.ui.ShimmerTextView>(R.id.key_space)")

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
