import re

with open("app/src/main/java/com/example/StitchKeyboardService.kt", "r") as f:
    content = f.read()

# Add PredictionEngine instance and suggestion views
target_init = "    private lateinit var emojiRoot: View"
replacement_init = """    private lateinit var emojiRoot: View
    private val predictionEngine = PredictionEngine()
    private var suggestion1: android.widget.TextView? = null
    private var suggestion2: android.widget.TextView? = null
    private var suggestion3: android.widget.TextView? = null"""
content = content.replace(target_init, replacement_init)


# Initialize the views in setupSuggestionBar
target_setup_bar = """    private fun setupSuggestionBar(view: View) {
        val suggestion1 = view.findViewById<TextView>(R.id.suggestion_1)
        val suggestion2 = view.findViewById<TextView>(R.id.suggestion_2)
        val suggestion3 = view.findViewById<TextView>(R.id.suggestion_3)"""

replacement_setup_bar = """    private fun setupSuggestionBar(view: View) {
        suggestion1 = view.findViewById<TextView>(R.id.suggestion_1)
        suggestion2 = view.findViewById<TextView>(R.id.suggestion_2)
        suggestion3 = view.findViewById<TextView>(R.id.suggestion_3)
        val suggestion1 = this.suggestion1
        val suggestion2 = this.suggestion2
        val suggestion3 = this.suggestion3"""

content = content.replace(target_setup_bar, replacement_setup_bar)


# Fix the suggestion click listener to replace the current word
target_click = """        val suggestionClickListener = View.OnClickListener { v ->
            if (v is TextView) {
                currentInputConnection?.commitText(v.text.toString() + " ", 1)
                playClickFeedback()
                triggerVibration()
            }
        }"""

replacement_click = """        val suggestionClickListener = View.OnClickListener { v ->
            if (v is TextView) {
                val ic = currentInputConnection ?: return@OnClickListener
                val textBeforeCursor = ic.getTextBeforeCursor(30, 0)?.toString() ?: ""
                val words = textBeforeCursor.split(Regex("[^a-zA-ZáéíóúãõâêîôûçÁÉÍÓÚÃÕÂÊÎÔÛÇ]+"))
                val lastWord = words.lastOrNull() ?: ""
                
                if (lastWord.isNotEmpty()) {
                    ic.deleteSurroundingText(lastWord.length, 0)
                }
                ic.commitText(v.text.toString() + " ", 1)
                playClickFeedback()
                triggerVibration()
            }
        }"""
content = content.replace(target_click, replacement_click)


# Add onUpdateSelection override
on_update_selection_func = """
    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int,
        newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        
        val ic = currentInputConnection
        if (ic == null || !::keyboardRoot.isInitialized) return
        
        val textBeforeCursor = ic.getTextBeforeCursor(30, 0)?.toString() ?: ""
        val words = textBeforeCursor.split(Regex("[^a-zA-ZáéíóúãõâêîôûçÁÉÍÓÚÃÕÂÊÎÔÛÇ]+"))
        val lastWord = words.lastOrNull() ?: ""
        
        val predictions = predictionEngine.getPredictions(lastWord)
        
        suggestion1?.text = predictions.getOrNull(0) ?: ""
        suggestion2?.text = predictions.getOrNull(1) ?: ""
        suggestion3?.text = predictions.getOrNull(2) ?: ""
    }
"""

content = content.replace("    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {", on_update_selection_func + "\n    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {")

with open("app/src/main/java/com/example/StitchKeyboardService.kt", "w") as f:
    f.write(content)

print("Updated predictions")
