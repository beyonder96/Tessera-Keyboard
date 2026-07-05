import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Update name from Stitch to Tessera
content = content.replace('text = "STITCH KEYBOARD",', 'text = "TESSERA KEYBOARD",')
content = content.replace("Stitch Dashboard", "Tessera Dashboard")
content = content.replace("Teclado Stitch Ativo", "Teclado Tessera Ativo")
content = content.replace("'Stitch Keyboard'", "'Tessera Keyboard'")
content = content.replace("SELECIONAR STITCH KEYBOARD", "SELECIONAR TESSERA KEYBOARD")
content = content.replace("Stitch OS System Layer", "Tessera OS System Layer")

# Remove from Button 3 (Mostrar Teclado Emulador) down to the end of the Column
match = re.search(r'        Button\(\n            onClick = \{\n                try \{\n                    val intent = Intent\(Settings\.ACTION_HARD_KEYBOARD_SETTINGS\).*?Stitch OS System Layer Design • Physical Light"[^\n]*\n            style = MaterialTheme\.typography\.labelSmall,\n            color = Color\(0x80FFFFFF\),\n            textAlign = TextAlign\.Center\n        \)\n    \}', content, re.DOTALL)

if match:
    replacement = """        Spacer(modifier = Modifier.weight(1f))

        // Branding bottom note
        Text(
            text = "Tessera Keyboard • Minimalist & Intelligent",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0x80FFFFFF),
            textAlign = TextAlign.Center
        )
    }"""
    content = content[:match.start()] + replacement + content[match.end():]

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
