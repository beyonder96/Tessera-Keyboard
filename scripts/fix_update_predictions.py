import re
with open("app/src/main/java/com/example/StitchKeyboardService.kt", "r") as f:
    content = f.read()

# Replace onUpdateSelection body
target_on_update_selection = """    override fun onUpdateSelection(
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
    }"""

replacement_on_update_selection = """    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int,
        newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        updatePredictions()
    }

    private fun updatePredictions() {
        val ic = currentInputConnection
        if (ic == null || !::keyboardRoot.isInitialized) return
        
        val textBeforeCursor = ic.getTextBeforeCursor(30, 0)?.toString() ?: ""
        val words = textBeforeCursor.split(Regex("[^a-zA-ZáéíóúãõâêîôûçÁÉÍÓÚÃÕÂÊÎÔÛÇ]+"))
        val lastWord = words.lastOrNull() ?: ""
        
        val predictions = predictionEngine.getPredictions(lastWord)
        
        suggestion1?.text = predictions.getOrNull(0) ?: ""
        suggestion2?.text = predictions.getOrNull(1) ?: ""
        suggestion3?.text = predictions.getOrNull(2) ?: ""
    }"""

content = content.replace(target_on_update_selection, replacement_on_update_selection)

# Add to onStartInputView
target_on_start_input_view = """        isShifted = false
        updateShiftUI()"""

replacement_on_start_input_view = """        isShifted = false
        updateShiftUI()
        updatePredictions()"""

content = content.replace(target_on_start_input_view, replacement_on_start_input_view)

with open("app/src/main/java/com/example/StitchKeyboardService.kt", "w") as f:
    f.write(content)
