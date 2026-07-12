filepath = 'app/src/main/res/layout/stitch_keyboard_layout.xml'
with open(filepath, 'r') as f:
    content = f.read()

# Fix double gravity attributes
content = content.replace('android:gravity="center_vertical" android:gravity="center"', 'android:gravity="center"')

with open(filepath, 'w') as f:
    f.write(content)
print("Fixed gravity")
