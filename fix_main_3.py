import re
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    text = f.read()

# I will find the exact OutlinedTextField declaration and replace the messy part
# The messy part is between `value = testInputText,` and `onValueChange = { testInputText = it },`
text = re.sub(r'value = testInputText,[\s\S]*?onValueChange = { testInputText = it },', 'value = testInputText,\n                onValueChange = { testInputText = it },', text)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(text)
