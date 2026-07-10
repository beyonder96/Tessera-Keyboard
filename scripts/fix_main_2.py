with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    lines = f.readlines()

out = []
skip = False
for i, line in enumerate(lines):
    if "var keyboardScale by remember { mutableStateOf(context.getSharedPreferences" in line and "Tamanho" not in line:
        skip = True
    
    if skip:
        if "colors = SliderDefaults.colors" in line:
            skip = False
            continue
        if "valueRange" in line or "onValueChange" in line or "keyboardScale = " in line or "context.getSharedPreferences" in line or "modifier = Modifier" in line or "value = keyboardScale" in line or "Slider(" in line or "Text(" in line or "}" in line or ")," in line:
            pass # Keep skipping
        continue
        
    out.append(line)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.writelines(out)
