import re

with open('app/src/main/res/layout/stitch_keyboard_layout.xml', 'r') as f:
    content = f.read()

old_space = '<TextView android:id="@+id/key_space" android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="4.5" android:layout_marginLeft="4dp" android:layout_marginRight="4dp" android:background="@drawable/bg_command_pill" android:text="Português (BR)" android:textColor="?attr/stitchTextColor" android:textSize="14sp" android:gravity="center" android:clickable="true" android:focusable="true" />'
new_space = '<com.example.ui.ShimmerTextView android:id="@+id/key_space" android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="4.5" android:layout_marginLeft="4dp" android:layout_marginRight="4dp" android:background="@drawable/bg_command_pill" android:text="T E S S E R A" android:fontFamily="sans-serif-light" android:textStyle="bold" android:textColor="?attr/stitchTextColor" android:textSize="14sp" android:gravity="center" android:clickable="true" android:focusable="true" />'

content = content.replace(old_space, new_space)

with open('app/src/main/res/layout/stitch_keyboard_layout.xml', 'w') as f:
    f.write(content)
