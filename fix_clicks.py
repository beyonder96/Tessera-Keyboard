import re

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

old_emoji = """        val emojiKey = view.findViewById<FrameLayout>(R.id.key_emoji)
        emojiKey?.setOnClickListener {
            playClickFeedback()
            triggerVibration()
            keyboardRoot.visibility = View.GONE
            emojiRoot.visibility = View.VISIBLE
        }"""
        
new_emoji = """        val emojiKey = view.findViewById<FrameLayout>(R.id.key_emoji)
        emojiKey?.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                playClickFeedback()
                triggerVibration()
                keyboardRoot.visibility = android.view.View.GONE
                emojiRoot.visibility = android.view.View.VISIBLE
                v.isPressed = true
                true
            } else if (event.action == android.view.MotionEvent.ACTION_UP || event.action == android.view.MotionEvent.ACTION_CANCEL) {
                v.isPressed = false
                true
            } else {
                false
            }
        }"""

content = content.replace(old_emoji, new_emoji)

old_symbol = """        val symbolKey = view.findViewById<FrameLayout>(R.id.key_symbol)
        symbolKey?.setOnClickListener {
            playClickFeedback()
            triggerVibration()
            Toast.makeText(this, "Teclado Numérico/Símbolos em breve", Toast.LENGTH_SHORT).show()
        }"""
        
new_symbol = """        val symbolKey = view.findViewById<FrameLayout>(R.id.key_symbol)
        symbolKey?.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                playClickFeedback()
                triggerVibration()
                android.widget.Toast.makeText(this, "Teclado Numérico/Símbolos em breve", android.widget.Toast.LENGTH_SHORT).show()
                v.isPressed = true
                true
            } else if (event.action == android.view.MotionEvent.ACTION_UP || event.action == android.view.MotionEvent.ACTION_CANCEL) {
                v.isPressed = false
                true
            } else {
                false
            }
        }"""

content = content.replace(old_symbol, new_symbol)

old_mic = """        val micKey = view.findViewById<FrameLayout>(R.id.key_mic)
        micKey?.setOnClickListener {
            playClickFeedback()
            triggerVibration()
            
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                Toast.makeText(this, "Conceda a permissão no app", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            
            keyboardRoot.visibility = View.GONE
            voiceRoot.visibility = View.VISIBLE
            startListening()
        }"""
        
new_mic = """        val micKey = view.findViewById<FrameLayout>(R.id.key_mic)
        micKey?.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                playClickFeedback()
                triggerVibration()
                if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    val intent = android.content.Intent(this, MainActivity::class.java)
                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    android.widget.Toast.makeText(this, "Conceda a permissão no app", android.widget.Toast.LENGTH_LONG).show()
                } else {
                    keyboardRoot.visibility = android.view.View.GONE
                    voiceRoot.visibility = android.view.View.VISIBLE
                    startListening()
                }
                v.isPressed = true
                true
            } else if (event.action == android.view.MotionEvent.ACTION_UP || event.action == android.view.MotionEvent.ACTION_CANCEL) {
                v.isPressed = false
                true
            } else {
                false
            }
        }"""
        
content = content.replace(old_mic, new_mic)

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
