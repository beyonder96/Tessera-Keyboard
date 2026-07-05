with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# I will replace the space after the buttons with the themes setting screen

theme_section = """
        // Theme Selection
        Spacer(modifier = Modifier.height(16.dp))
        var keyboardTheme by remember { mutableStateOf(context.getSharedPreferences("StitchPrefs", Context.MODE_PRIVATE).getString("KEYBOARD_THEME", "Dark") ?: "Dark") }
        
        Text("Tema do Teclado", style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.align(Alignment.Start))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ThemeOption(
                title = "Dark Glass",
                isSelected = keyboardTheme == "Dark",
                onClick = {
                    keyboardTheme = "Dark"
                    context.getSharedPreferences("StitchPrefs", Context.MODE_PRIVATE).edit().putString("KEYBOARD_THEME", "Dark").apply()
                },
                modifier = Modifier.weight(1f)
            )
            ThemeOption(
                title = "Light Glass",
                isSelected = keyboardTheme == "Light",
                onClick = {
                    keyboardTheme = "Light"
                    context.getSharedPreferences("StitchPrefs", Context.MODE_PRIVATE).edit().putString("KEYBOARD_THEME", "Light").apply()
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
"""

target = "        var keyboardScale by remember"
content = content.replace(target, theme_section + target)

theme_option_composable = """
@Composable
fun ThemeOption(title: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bgColor = if (isSelected) Color(0xFF06FBFB).copy(alpha = 0.15f) else Color(0x0DFFFFFF)
    val borderColor = if (isSelected) Color(0xFF06FBFB) else Color(0x2BFFFFFF)
    val textColor = if (isSelected) Color(0xFF06FBFB) else Color.White
    
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
"""

content += "\n" + theme_option_composable

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
