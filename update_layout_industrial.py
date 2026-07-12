import re

filepath = 'app/src/main/res/layout/stitch_keyboard_layout.xml'
with open(filepath, 'r') as f:
    content = f.read()

# Make sure background reflects dark gray industrial metal
# (Wait, actually we can just leave the background drawable names alone and modify the drawables themselves next)
# The user asked for a complete visual re-envisioning, but also specific functional mapping for the top bar.

# 1. Update the top bar (Toolbar)
toolbar_start = '<LinearLayout android:layout_width="match_parent" android:layout_height="48dp" android:orientation="horizontal" android:gravity="center" android:layout_marginBottom="12dp">'
toolbar_end = '</LinearLayout>\n    <RelativeLayout'

new_toolbar = """<LinearLayout android:layout_width="match_parent" android:layout_height="48dp" android:orientation="horizontal" android:gravity="center" android:layout_marginBottom="12dp">
        <!-- 1: Shift Toggle -->
        <FrameLayout android:id="@+id/key_shift_top" android:layout_width="0dp" android:layout_weight="1.2" android:layout_height="38dp" android:background="@drawable/bg_command_pill" android:layout_marginRight="4dp" android:clickable="true" android:focusable="true">
            <TextView android:layout_width="match_parent" android:layout_height="match_parent" android:text="Aa" android:textColor="#FFFFFF" android:textSize="16sp" android:gravity="center" />
        </FrameLayout>
        
        <!-- 2: Microphone (Optical Trackball look) -->
        <FrameLayout android:id="@+id/key_mic_top" android:layout_width="0dp" android:layout_weight="1" android:layout_height="38dp" android:background="@drawable/bg_command_pill" android:layout_marginRight="4dp" android:clickable="true" android:focusable="true">
            <View android:layout_width="16dp" android:layout_height="16dp" android:background="@drawable/bg_key_circle" android:layout_gravity="center" />
            <View android:layout_width="6dp" android:layout_height="6dp" android:background="@drawable/bg_circle_active" android:layout_gravity="center" />
        </FrameLayout>
        
        <!-- 3: Disabled (Multi-position selector switch) -->
        <TextView android:id="@+id/key_disabled_1" android:layout_width="0dp" android:layout_weight="1" android:layout_height="38dp" android:text="≡" android:textColor="#555555" android:textSize="20sp" android:gravity="center" android:background="@drawable/bg_command_pill" android:layout_marginRight="4dp" android:clickable="false" />
        
        <!-- 4: Disabled (OLED ribbon with 15 16 17) -->
        <LinearLayout android:id="@+id/key_disabled_2" android:layout_width="0dp" android:layout_weight="2" android:layout_height="38dp" android:orientation="horizontal" android:gravity="center" android:background="@drawable/bg_command_pill" android:clickable="false">
            <TextView android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content" android:text="15" android:textColor="#444444" android:textSize="14sp" android:gravity="center" />
            <TextView android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content" android:text="16" android:textColor="#888888" android:textSize="16sp" android:gravity="center" />
            <TextView android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content" android:text="17" android:textColor="#444444" android:textSize="16sp" android:gravity="center" />
        </LinearLayout>
    </LinearLayout>\n    <RelativeLayout"""

pattern = re.compile(re.escape(toolbar_start) + r'.*?' + r'</LinearLayout>\s*<RelativeLayout', re.DOTALL)
content = pattern.sub(new_toolbar, content)

with open(filepath, 'w') as f:
    f.write(content)

print("Applied new top bar layout.")
