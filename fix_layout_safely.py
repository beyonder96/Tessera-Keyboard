import re

filepath = 'app/src/main/res/layout/stitch_keyboard_layout.xml'
with open(filepath, 'r') as f:
    content = f.read()

# 1. Background replacements
content = content.replace('@drawable/bg_floating_key', '@drawable/bg_key_circle')
content = content.replace('@drawable/bg_keyboard_main', '@drawable/bg_floating_keyboard')

# 2. Fix Space bar
def replace_space(match):
    m = match.group(0)
    m = re.sub(r'android:background="[^"]+"', 'android:background="@drawable/bg_command_pill"', m)
    return m
content = re.sub(r'<com\.example\.ui\.ShimmerTextView android:id="@+id/key_space"[^>]+>', replace_space, content)

# 3. Fix keys size to 45dp
def replace_key_size(match):
    m = match.group(0)
    m = re.sub(r'android:layout_width="[^"]+"', 'android:layout_width="45dp"', m)
    m = re.sub(r'android:layout_height="[^"]+"', 'android:layout_height="45dp"', m)
    m = re.sub(r'android:layout_weight="[^"]+"', '', m)
    return m
content = re.sub(r'<TextView android:id="@+id/key_[a-zç]"[^>]+>', replace_key_size, content)

# 4. Add gravity="center" to the horizontal layouts holding the keys, but only if they don't have it
def add_gravity(match):
    m = match.group(0)
    if 'android:gravity=' not in m:
        m = m.replace('>', ' android:gravity="center">')
    return m
content = re.sub(r'<LinearLayout android:layout_width="match_parent" android:layout_height="52dp" android:orientation="horizontal"[^>]*>', add_gravity, content)

# 5. Toolbar Replacement
new_toolbar = """<LinearLayout android:layout_width="match_parent" android:layout_height="48dp" android:orientation="horizontal" android:gravity="center" android:layout_marginBottom="12dp">
        <TextView android:id="@+id/key_plus" android:layout_width="wrap_content" android:layout_height="38dp" android:text="Aa" android:textColor="#FFFFFF" android:textSize="16sp" android:gravity="center" android:paddingHorizontal="16dp" android:background="@drawable/bg_command_pill" android:layout_marginRight="8dp" />
        <View android:id="@+id/key_sparkle" android:layout_width="24dp" android:layout_height="24dp" android:background="@drawable/bg_key_circle" android:layout_marginRight="8dp" />
        <TextView android:id="@+id/suggestion_2" android:layout_width="wrap_content" android:layout_height="38dp" android:text="≡" android:textColor="#FFFFFF" android:textSize="20sp" android:gravity="center" android:paddingHorizontal="16dp" android:background="@drawable/bg_command_pill" android:layout_marginRight="16dp" />
        
        <LinearLayout android:layout_width="wrap_content" android:layout_height="match_parent" android:orientation="horizontal" android:gravity="center" android:background="@drawable/bg_command_pill" android:paddingHorizontal="12dp">
            <TextView android:id="@+id/suggestion_1" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="15" android:textColor="#888888" android:textSize="14sp" android:layout_marginRight="12dp" />
            <TextView android:id="@+id/suggestion_3" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="16" android:textColor="#FFFFFF" android:textSize="16sp" android:layout_marginRight="12dp" />
            <TextView android:id="@+id/key_ia" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="17" android:textColor="#888888" android:textSize="18sp" />
        </LinearLayout>
    </LinearLayout>"""

# Using regex to find everything from the start of the toolbar to the start of keys_container
pattern = r'<LinearLayout android:layout_width="match_parent" android:layout_height="48dp" android:orientation="horizontal"[^>]*>.*?</LinearLayout>\s*(?=<RelativeLayout android:layout_width="match_parent" android:layout_height="wrap_content">)'
content = re.sub(pattern, new_toolbar + "\n    ", content, flags=re.DOTALL)

with open(filepath, 'w') as f:
    f.write(content)
print("Updated XML Safely")
