with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

import re
# We want to remove the scale UI.
# It starts from: var keyboardScale by remember { mutableFloatStateOf...
# To the end of Slider(
content = re.sub(r'        var keyboardScale by remember.*?colors = SliderDefaults\.colors\(thumbColor = Color\(0xFF06FBFB\), activeTrackColor = Color\(0xFF06FBFB\)\)\n        \)', '', content, flags=re.DOTALL)
# It might have a Row before it now for Adjust Size.
content = re.sub(r'        Row\(\n            modifier = Modifier\.fillMaxWidth\(\),\n            horizontalArrangement = Arrangement\.SpaceBetween,\n            verticalAlignment = Alignment\.CenterVertically\n        \) \{\n            Text\("Ajuste de Tamanho"[^\n]*\n            Text\("\$\{\(keyboardScale \* 100\)\.toInt\(\)\}%"[^\n]*\n        \}', '', content, flags=re.DOTALL)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
