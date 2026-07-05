import re

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

old_click = """        if (isShifted) {
            isShifted = false
            updateShiftUI()
        }
        playClickFeedback()
    }"""
    
new_click = """        if (isShifted) {
            isShifted = false
            updateShiftUI()
        }
        playClickFeedback()
        updatePredictions()
    }"""

content = content.replace(old_click, new_click)

old_back = """        ic.deleteSurroundingText(1, 0)
    }"""
    
new_back = """        ic.deleteSurroundingText(1, 0)
        updatePredictions()
    }"""

content = content.replace(old_back, new_back)

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
