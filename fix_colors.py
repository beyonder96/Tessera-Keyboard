import re

with open('app/src/main/java/com/example/ui/ShimmerTextView.kt', 'r') as f:
    content = f.read()

content = content.replace("0x66000000", "0x66000000.toInt()")
content = content.replace("-0x1000000", "0xFF000000.toInt()")
content = content.replace("0x00FFFFFF", "0x00FFFFFF.toInt()")

with open('app/src/main/java/com/example/ui/ShimmerTextView.kt', 'w') as f:
    f.write(content)
