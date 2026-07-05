import re

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'r') as f:
    content = f.read()

old_regex = """val words = textBeforeCursor.split(Regex("[^a-zA-ZáéíóúãõâêîôûçÁÉÍÓÚÃÕÂÊÎÔÛÇ]+"))"""
new_regex = """val words = textBeforeCursor.split(wordSeparatorRegex)"""

content = content.replace(old_regex, new_regex)

init_vars = """    private val predictionEngine = PredictionEngine()"""
new_vars = """    private val predictionEngine = PredictionEngine()
    private val wordSeparatorRegex = Regex("[^a-zA-ZáéíóúãõâêîôûçÁÉÍÓÚÃÕÂÊÎÔÛÇ]+")"""

content = content.replace(init_vars, new_vars)

with open('app/src/main/java/com/example/StitchKeyboardService.kt', 'w') as f:
    f.write(content)
