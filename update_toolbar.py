import re

filepath = 'app/src/main/res/layout/stitch_keyboard_layout.xml'
with open(filepath, 'r') as f:
    content = f.read()

# The top toolbar is a LinearLayout with height 48dp containing key_plus etc
new_toolbar = """
    <LinearLayout android:layout_width="match_parent" android:layout_height="48dp" android:orientation="horizontal" android:gravity="center" android:layout_marginBottom="12dp">
        <TextView android:id="@+id/key_plus" android:layout_width="wrap_content" android:layout_height="38dp" android:text="Aa" android:textColor="#FFFFFF" android:textSize="16sp" android:gravity="center" android:paddingHorizontal="16dp" android:background="@drawable/bg_command_pill" android:layout_marginRight="8dp" />
        <View android:id="@+id/key_sparkle" android:layout_width="24dp" android:layout_height="24dp" android:background="@drawable/bg_key_circle" android:layout_marginRight="8dp" />
        <TextView android:id="@+id/suggestion_2" android:layout_width="wrap_content" android:layout_height="38dp" android:text="≡" android:textColor="#FFFFFF" android:textSize="20sp" android:gravity="center" android:paddingHorizontal="16dp" android:background="@drawable/bg_command_pill" android:layout_marginRight="16dp" />
        
        <!-- Font sizes -->
        <LinearLayout android:layout_width="wrap_content" android:layout_height="match_parent" android:orientation="horizontal" android:gravity="center" android:background="@drawable/bg_command_pill" android:paddingHorizontal="12dp">
            <TextView android:id="@+id/suggestion_1" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="15" android:textColor="#888888" android:textSize="14sp" android:layout_marginRight="12dp" />
            <TextView android:id="@+id/suggestion_3" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="16" android:textColor="#FFFFFF" android:textSize="16sp" android:layout_marginRight="12dp" />
            <TextView android:id="@+id/key_ia" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="17" android:textColor="#888888" android:textSize="18sp" />
        </LinearLayout>
    </LinearLayout>
"""

# Find the toolbar block using regex (from <LinearLayout android:layout_width="match_parent" android:layout_height="48dp" to </LinearLayout> just before keys_container)
content = re.sub(r'<LinearLayout android:layout_width="match_parent" android:layout_height="48dp" android:orientation="horizontal" android:gravity="center_vertical" android:layout_marginBottom="12dp">.*?</LinearLayout>', new_toolbar, content, flags=re.DOTALL)

with open(filepath, 'w') as f:
    f.write(content)
print("Updated Toolbar")
