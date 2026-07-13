import xml.etree.ElementTree as ET
import re
import os

filepath = 'app/src/main/res/layout/stitch_keyboard_layout.xml'

with open(filepath, 'r') as f:
    content = f.read()

# Replace keyboard background
content = content.replace('@drawable/bg_keyboard_main', '@drawable/bg_floating_keyboard')

# Replace keys background
content = content.replace('@drawable/bg_floating_key', '@drawable/bg_key_circle')

# Force letter keys width/height to 45dp (optional if layout_weight is used, but user requested it)
# We can just change layout_weight="1" to fixed size but it will break responsiveness.
# User said "Garante que no seu layout as teclas comuns tenham largura e altura idênticas (ex: 45dp x 45dp) para o círculo não virar uma elipse."
# We will use regex to find letter keys (android:id="@+id/key_[a-z]") and change width/height.
# Since they are in a horizontal LinearLayout with weightSum="10", changing width to 45dp might be too wide or narrow.
# But I will follow instructions.
def replace_key_size(match):
    m = match.group(0)
    m = re.sub(r'android:layout_width="[^"]+"', 'android:layout_width="45dp"', m)
    m = re.sub(r'android:layout_height="[^"]+"', 'android:layout_height="45dp"', m)
    m = re.sub(r'android:layout_weight="[^"]+"', '', m) # remove weight
    return m

content = re.sub(r'<TextView android:id="@+id/key_[a-zç]"[^>]+>', replace_key_size, content)

# But wait, removing weight will make the keys align left!
# To keep them centered, the parent LinearLayout should have android:gravity="center"
# We can change LinearLayouts holding keys to have gravity="center".
content = re.sub(r'(<LinearLayout android:layout_width="match_parent" android:layout_height="[^"]+" android:orientation="horizontal" android:weightSum="10"[^>]*)>', r'\1 android:gravity="center">', content)

# Space bar formatting (elongated pill)
# The space bar ID is key_space
def replace_space(match):
    m = match.group(0)
    m = re.sub(r'android:background="[^"]+"', 'android:background="@drawable/bg_command_pill"', m)
    # The space bar might be a ShimmerTextView, keep the tag
    return m

content = re.sub(r'<com\.example\.ui\.ShimmerTextView android:id="@+id/key_space"[^>]+>', replace_space, content)

with open(filepath, 'w') as f:
    f.write(content)
print("Updated XML")
