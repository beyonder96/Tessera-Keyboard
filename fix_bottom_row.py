import re

with open('app/src/main/res/layout/stitch_keyboard_layout.xml', 'r') as f:
    content = f.read()

start_idx = content.find('<LinearLayout android:layout_width="match_parent" android:layout_height="52dp" android:orientation="horizontal" android:weightSum="10" android:gravity="center_vertical">')
end_idx = content.find('</LinearLayout>', start_idx) + 15

new_bottom_row = """    <LinearLayout android:layout_width="match_parent" android:layout_height="52dp" android:orientation="horizontal" android:weightSum="10" android:gravity="center_vertical">

        <FrameLayout android:id="@+id/key_symbol" android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="1.5" android:background="?android:attr/selectableItemBackgroundBorderless" android:clickable="true" android:focusable="true">
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:layout_gravity="center" android:text="?123" android:textColor="?attr/stitchTextColor" android:textSize="14sp" />
        </FrameLayout>

        <FrameLayout android:id="@+id/key_mic" android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="1.0" android:background="?android:attr/selectableItemBackgroundBorderless" android:clickable="true" android:focusable="true">
            <ImageView android:layout_width="20dp" android:layout_height="20dp" android:layout_gravity="center" android:src="@drawable/ic_microphone" android:contentDescription="Voice Input" />
        </FrameLayout>

        <TextView android:id="@+id/key_space" android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="4.5" android:layout_marginLeft="4dp" android:layout_marginRight="4dp" android:background="@drawable/bg_command_pill" android:text="Português (BR)" android:textColor="?attr/stitchTextColor" android:textSize="14sp" android:gravity="center" android:clickable="true" android:focusable="true" />

        <FrameLayout android:id="@+id/key_emoji" android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="1.5" android:layout_marginRight="4dp" android:background="@drawable/bg_command_pill" android:clickable="true" android:focusable="true">
            <TextView android:id="@+id/text_emoji" android:layout_width="wrap_content" android:layout_height="wrap_content" android:layout_gravity="center" android:text="😊" android:textSize="18sp" android:visibility="gone" />
            <ImageView android:id="@+id/img_emoji" android:layout_width="20dp" android:layout_height="20dp" android:layout_gravity="center" android:src="@drawable/ic_emoji" android:contentDescription="Emojis" />
        </FrameLayout>

        <FrameLayout android:id="@+id/key_enter" android:layout_width="0dp" android:layout_height="44dp" android:layout_weight="1.5" android:background="@drawable/bg_command_pill_active" android:clickable="true" android:focusable="true">
            <ImageView android:layout_width="20dp" android:layout_height="20dp" android:layout_gravity="center" android:src="@drawable/ic_enter" android:contentDescription="Enter" />
        </FrameLayout>

    </LinearLayout>"""

content = content[:start_idx] + new_bottom_row + content[end_idx:]

with open('app/src/main/res/layout/stitch_keyboard_layout.xml', 'w') as f:
    f.write(content)
