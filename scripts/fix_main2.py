with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for line in lines:
    if "Button(" in line and "onClick = {" in line and "intent = Intent(Settings.ACTION_HARD_KEYBOARD_SETTINGS)" in "".join(lines):
        # We need to find the start of Button 3
        pass

# Let's just find the index of "Button(" that has "3. MOSTRAR TECLADO (EMULADOR)" inside it.
import re
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# We want to remove from the 3rd Button all the way to the end of the file, then add our own ending.
# The 3rd button starts at:
#         Button(
#             onClick = {
#                 try {
#                     val intent = Intent(Settings.ACTION_HARD_KEYBOARD_SETTINGS)
# And ends at the end of the Dashboard
start_idx = content.find("        Button(\n            onClick = {\n                try {\n                    val intent = Intent(Settings.ACTION_HARD_KEYBOARD_SETTINGS)")

if start_idx != -1:
    end_part = """        Spacer(modifier = Modifier.weight(1f))

        // Branding bottom note
        Text(
            text = "Tessera Keyboard • Minimalist & Intelligent",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0x80FFFFFF),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun InstructionRow(number: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color(0xFF06FBFB), RoundedCornerShape(100.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = Color(0xFF001717),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFE2E2E2),
            modifier = Modifier.weight(1f)
        )
    }
}
"""
    content = content[:start_idx] + end_part
    with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
        f.write(content)
