import re

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

setup_emojis_code = """
    private fun setupEmojiKeyboard(view: View) {
        val emojiGridLayout = view.findViewById<android.widget.GridLayout>(R.id.emoji_grid_layout) ?: return
        val emojis = listOf(
            "😀", "😂", "🤣", "😊", "🥰", "😍", "😘", "😜",
            "😎", "🤩", "🥳", "😏", "😒", "😞", "😔", "😢",
            "😭", "😤", "😡", "🤬", "🤯", "😳", "🥵", "🥶",
            "😱", "😨", "😰", "😥", "😓", "🤗", "🤔", "🤭",
            "🤫", "🤥", "😶", "😐", "😑", "😬", "🙄", "😯",
            "😦", "😧", "😮", "😲", "🥱", "😴", "🤤", "😪",
            "😵", "🤐", "🥴", "🤢", "🤮", "🤧", "😷", "🤒",
            "🤕", "🤑", "🤠", "😈", "👿", "👹", "👺", "🤡",
            "💩", "👻", "💀", "☠️", "👽", "👾", "🤖", "🎃",
            "😺", "😸", "😹", "😻", "😼", "😽", "🙀", "😿",
            "😾", "🙈", "🙉", "🙊", "💋", "💌", "💘", "💝",
            "💖", "💗", "💓", "💞", "💕", "💟", "❣️", "💔",
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🤎", "🖤",
            "🤍", "💯", "💢", "💥", "💫", "💦", "💨", "🕳️"
        )
        
        emojiGridLayout.removeAllViews()
        val density = resources.displayMetrics.density
        val padding = (8 * density).toInt()
        val size = (42 * density).toInt()
        
        for (emoji in emojis) {
            val tv = android.widget.TextView(this)
            tv.text = emoji
            tv.textSize = 28f
            tv.gravity = android.view.Gravity.CENTER
            
            val params = android.widget.GridLayout.LayoutParams()
            params.width = size
            params.height = size
            params.setMargins(4, 4, 4, 4)
            tv.layoutParams = params
            
            tv.background = androidx.core.content.ContextCompat.getDrawable(this, android.R.attr.selectableItemBackgroundBorderless)
            tv.isClickable = true
            tv.isFocusable = true
            
            tv.setOnClickListener {
                playClickFeedback()
                triggerVibration()
                val ic = currentInputConnection
                ic?.commitText(emoji, 1)
            }
            emojiGridLayout.addView(tv)
        }
        
        view.findViewById<View>(R.id.btn_close_emoji)?.setOnClickListener {
            playClickFeedback()
            emojiRoot.visibility = View.GONE
            keyboardRoot.visibility = View.VISIBLE
        }
    }
"""

if 'private fun setupEmojiKeyboard' not in content:
    idx = content.find('private fun setupCommandKeys(view: View) {')
    content = content[:idx] + setup_emojis_code + content[idx:]

# Call setupEmojiKeyboard in onCreateInputView
call_code = """        setupEmojiKeyboard(keyboardView)"""
if call_code not in content:
    idx2 = content.find('setupCommandKeys(keyboardView)')
    content = content[:idx2] + call_code + "\n        " + content[idx2:]
    
with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
