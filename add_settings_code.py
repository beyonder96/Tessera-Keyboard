import re

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

# Add settingsRoot variable
init_ui_idx = content.find('private lateinit var emojiRoot: View')
content = content[:init_ui_idx] + 'private lateinit var settingsRoot: View\n    ' + content[init_ui_idx:]

# Find keyboardRoot initialization
key_root_idx = content.find('keyboardRoot = view.findViewById(R.id.keyboard_root)')
content = content[:key_root_idx] + 'settingsRoot = view.findViewById(R.id.settings_ui_root)\n        ' + content[key_root_idx:]

# Update plus button click
old_plus_click = """        plusBtn?.setOnClickListener {
            currentInputConnection?.commitText("✦ ", 1)
            playClickFeedback()
            triggerVibration()
            Toast.makeText(this, "Recursos Rápidos Stitch", Toast.LENGTH_SHORT).show()
        }"""

new_plus_click = """        plusBtn?.setOnClickListener {
            playClickFeedback()
            triggerVibration()
            keyboardRoot.visibility = View.GONE
            settingsRoot.visibility = View.VISIBLE
        }
        
        view.findViewById<View>(R.id.btn_close_settings)?.setOnClickListener {
            playClickFeedback()
            triggerVibration()
            settingsRoot.visibility = View.GONE
            keyboardRoot.visibility = View.VISIBLE
        }
        
        view.findViewById<View>(R.id.btn_settings_app)?.setOnClickListener {
            val intent = android.content.Intent(this, MainActivity::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            settingsRoot.visibility = View.GONE
            keyboardRoot.visibility = View.VISIBLE
        }
        
        view.findViewById<View>(R.id.btn_settings_theme)?.setOnClickListener {
            val prefs = getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE)
            val current = prefs.getString("KEYBOARD_THEME", "Dark")
            val newTheme = if (current == "Dark") "Light" else "Dark"
            prefs.edit().putString("KEYBOARD_THEME", newTheme).apply()
            setInputView(onCreateInputView())
        }
        
        view.findViewById<View>(R.id.btn_settings_size)?.setOnClickListener {
            val prefs = getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE)
            val current = prefs.getFloat("KEYBOARD_SCALE", 1.0f)
            val newScale = if (current == 1.0f) 0.85f else if (current == 0.85f) 1.15f else 1.0f
            prefs.edit().putFloat("KEYBOARD_SCALE", newScale).apply()
            setInputView(onCreateInputView())
        }
        
        view.findViewById<View>(R.id.btn_settings_emoji)?.setOnClickListener {
            Toast.makeText(this, "Tons de emoji em breve", Toast.LENGTH_SHORT).show()
        }
"""

content = content.replace(old_plus_click, new_plus_click)

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
