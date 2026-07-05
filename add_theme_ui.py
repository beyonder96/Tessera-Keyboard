with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

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
        
        // Test Input Field
        Spacer(modifier = Modifier.height(16.dp))
        var testText by remember { mutableStateOf("") }
        androidx.compose.material3.OutlinedTextField(
            value = testText,
            onValueChange = { testText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Teste o teclado aqui...", color = Color(0x80FFFFFF)) },
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF06FBFB),
                unfocusedBorderColor = Color(0x2BFFFFFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF06FBFB)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.weight(1f))"""

content = content.replace("        Spacer(modifier = Modifier.weight(1f))", theme_section)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
