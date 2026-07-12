import re

filepath = 'app/src/main/res/layout/stitch_keyboard_layout.xml'
with open(filepath, 'r') as f:
    content = f.read()

# 1. Backgrounds
content = content.replace('@drawable/bg_keyboard_main', '@drawable/bg_floating_keyboard')
content = content.replace('@drawable/bg_floating_key', '@drawable/bg_key_circle')
content = content.replace('bg_key_circleboard', 'bg_floating_keyboard') # just in case

# 2. Make letter keys circular in FrameLayouts
def replace_key(match):
    m = match.group(0)
    # Extract id, weight, text
    id_match = re.search(r'android:id="([^"]+)"', m)
    weight_match = re.search(r'android:layout_weight="([^"]+)"', m)
    text_match = re.search(r'android:text="([^"]+)"', m)
    
    if id_match and weight_match and text_match:
        key_id = id_match.group(1)
        weight = weight_match.group(1)
        text = text_match.group(1)
        
        return f'''<FrameLayout android:layout_width="0dp" android:layout_height="match_parent" android:layout_weight="{weight}">
            <TextView android:id="{key_id}" android:layout_width="42dp" android:layout_height="42dp" android:layout_gravity="center" android:text="{text}" android:textColor="?attr/stitchTextColor" android:textSize="20sp" android:gravity="center" android:background="@drawable/bg_key_circle" android:clickable="true" android:focusable="true" android:fontFamily="sans-serif-light" />
        </FrameLayout>'''
    return m

content = re.sub(r'<TextView android:id="@+id/key_[a-zç]+".*?/>', replace_key, content, flags=re.DOTALL)

# 3. Replace space bar
space_bar = '''<com.example.ui.ShimmerTextView android:id="@+id/key_space" android:layout_width="0dp" android:layout_height="42dp" android:layout_gravity="center" android:layout_weight="4.5" android:layout_marginHorizontal="6dp" android:background="@drawable/bg_command_pill" android:text="space" android:fontFamily="sans-serif-light" android:textColor="#FFFFFF" android:textSize="16sp" android:gravity="center" android:clickable="true" android:focusable="true" />'''
content = re.sub(r'<com\.example\.ui\.ShimmerTextView android:id="@+id/key_space".*?/>', space_bar, content, flags=re.DOTALL)

# 4. Replace other bottom row keys to match style (123, ret)
sym_key = '''<FrameLayout android:id="@+id/key_symbol" android:layout_width="0dp" android:layout_height="match_parent" android:layout_weight="1.5">
            <TextView android:id="@+id/text_key_symbol" android:layout_width="42dp" android:layout_height="42dp" android:layout_gravity="center" android:text="123" android:textColor="#FFFFFF" android:textSize="14sp" android:gravity="center" android:background="@drawable/bg_command_pill" android:clickable="true" android:focusable="true" />
        </FrameLayout>'''
content = re.sub(r'<FrameLayout android:id="@+id/key_symbol".*?</FrameLayout>', sym_key, content, flags=re.DOTALL)

ret_key = '''<FrameLayout android:id="@+id/key_enter" android:layout_width="0dp" android:layout_height="match_parent" android:layout_weight="1.5">
            <TextView android:layout_width="42dp" android:layout_height="42dp" android:layout_gravity="center" android:text="ret" android:textColor="#FFFFFF" android:textSize="14sp" android:gravity="center" android:background="@drawable/bg_command_pill" android:clickable="true" android:focusable="true" />
        </FrameLayout>'''
content = re.sub(r'<FrameLayout android:id="@+id/key_enter".*?</FrameLayout>', ret_key, content, flags=re.DOTALL)

# Replace shift and backspace
shift_key = '''<FrameLayout android:id="@+id/key_shift" android:layout_width="0dp" android:layout_height="match_parent" android:layout_weight="1.4">
            <FrameLayout android:layout_width="42dp" android:layout_height="42dp" android:layout_gravity="center" android:background="@drawable/bg_command_pill" android:clickable="true" android:focusable="true">
                <ImageView android:id="@+id/img_shift_icon" android:layout_width="20dp" android:layout_height="20dp" android:layout_gravity="center" android:src="@drawable/ic_shift" android:tint="#FFFFFF" />
            </FrameLayout>
        </FrameLayout>'''
content = re.sub(r'<FrameLayout android:id="@+id/key_shift".*?</FrameLayout>', shift_key, content, flags=re.DOTALL)

backspace_key = '''<FrameLayout android:id="@+id/key_backspace" android:layout_width="0dp" android:layout_height="match_parent" android:layout_weight="1.6">
            <FrameLayout android:layout_width="42dp" android:layout_height="42dp" android:layout_gravity="center" android:background="@drawable/bg_command_pill" android:clickable="true" android:focusable="true">
                <ImageView android:layout_width="20dp" android:layout_height="20dp" android:layout_gravity="center" android:src="@drawable/ic_backspace" android:tint="#FFFFFF" />
            </FrameLayout>
        </FrameLayout>'''
content = re.sub(r'<FrameLayout android:id="@+id/key_backspace".*?</FrameLayout>', backspace_key, content, flags=re.DOTALL)

# 5. Toolbar replacement
toolbar_start = '<LinearLayout android:layout_width="match_parent" android:layout_height="48dp" android:orientation="horizontal" android:gravity="center_vertical" android:layout_marginBottom="12dp">'
toolbar_end = '</LinearLayout>\n    <RelativeLayout'

new_toolbar = """<LinearLayout android:layout_width="match_parent" android:layout_height="48dp" android:orientation="horizontal" android:gravity="center" android:layout_marginBottom="12dp">
        <TextView android:id="@+id/key_plus" android:layout_width="0dp" android:layout_weight="1.2" android:layout_height="38dp" android:text="Aa" android:textColor="#FFFFFF" android:textSize="16sp" android:gravity="center" android:background="@drawable/bg_command_pill" android:layout_marginRight="4dp" />
        <FrameLayout android:id="@+id/key_sparkle" android:layout_width="0dp" android:layout_weight="1" android:layout_height="38dp" android:background="@drawable/bg_command_pill" android:layout_marginRight="4dp">
            <View android:layout_width="16dp" android:layout_height="16dp" android:background="@drawable/bg_key_circle" android:layout_gravity="center" />
            <View android:layout_width="6dp" android:layout_height="6dp" android:background="@drawable/bg_circle_active" android:layout_gravity="center" />
        </FrameLayout>
        <TextView android:id="@+id/suggestion_2" android:layout_width="0dp" android:layout_weight="1" android:layout_height="38dp" android:text="≡" android:textColor="#FFFFFF" android:textSize="20sp" android:gravity="center" android:background="@drawable/bg_command_pill" android:layout_marginRight="4dp" />
        <LinearLayout android:layout_width="0dp" android:layout_weight="2" android:layout_height="38dp" android:orientation="horizontal" android:gravity="center" android:background="@drawable/bg_command_pill">
            <TextView android:id="@+id/suggestion_1" android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content" android:text="15" android:textColor="#888888" android:textSize="14sp" android:gravity="center" />
            <TextView android:id="@+id/suggestion_3" android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content" android:text="16" android:textColor="#FFFFFF" android:textSize="16sp" android:gravity="center" />
            <TextView android:id="@+id/key_ia" android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content" android:text="17" android:textColor="#888888" android:textSize="16sp" android:gravity="center" />
        </LinearLayout>
    </LinearLayout>\n    <RelativeLayout"""

# We find the exact block and replace
pattern = re.compile(re.escape(toolbar_start) + r'.*?' + r'</LinearLayout>\s*<RelativeLayout', re.DOTALL)
content = pattern.sub(new_toolbar, content)

with open(filepath, 'w') as f:
    f.write(content)

print("Applied layout modifications safely.")
