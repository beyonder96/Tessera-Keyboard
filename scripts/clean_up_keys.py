import re

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

switch_key_code = """        val switchKey = view.findViewById<FrameLayout>(R.id.key_switch)
        switchKey?.setOnClickListener {
            triggerVibration()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
            playClickFeedback()
        }"""

new_symbol_code = """        val symbolKey = view.findViewById<FrameLayout>(R.id.key_symbol)
        symbolKey?.setOnClickListener {
            playClickFeedback()
            triggerVibration()
            Toast.makeText(this, "Teclado Numérico/Símbolos em breve", Toast.LENGTH_SHORT).show()
        }"""

content = content.replace(switch_key_code, new_symbol_code)

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
