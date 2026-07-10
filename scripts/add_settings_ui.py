import re

with open('app/src/main/res/layout/stitch_keyboard_layout.xml', 'r') as f:
    content = f.read()

settings_ui = """
    <LinearLayout android:id="@+id/settings_ui_root" android:layout_width="match_parent" android:layout_height="280dp" android:orientation="vertical" android:background="?attr/stitchBgColor" android:visibility="gone" android:padding="16dp">
        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center_vertical" android:layout_marginBottom="16dp">
            <TextView android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:text="Recursos Rápidos" android:textColor="?attr/stitchTextColor" android:textSize="16sp" android:textStyle="bold" />
            <ImageButton android:id="@+id/btn_close_settings" android:layout_width="32dp" android:layout_height="32dp" android:background="@drawable/bg_command_pill" android:src="@drawable/ic_backspace" android:contentDescription="Close" android:tint="?attr/stitchTextColor" android:padding="6dp" android:scaleType="fitCenter" />
        </LinearLayout>
        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:weightSum="4">
            <LinearLayout android:id="@+id/btn_settings_app" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical" android:gravity="center" android:background="?android:attr/selectableItemBackground" android:clickable="true">
                <View android:layout_width="48dp" android:layout_height="48dp" android:background="@drawable/bg_command_pill" />
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Config" android:textColor="?attr/stitchTextColor" android:textSize="12sp" android:layout_marginTop="8dp" />
            </LinearLayout>
            <LinearLayout android:id="@+id/btn_settings_theme" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical" android:gravity="center" android:background="?android:attr/selectableItemBackground" android:clickable="true">
                <View android:layout_width="48dp" android:layout_height="48dp" android:background="@drawable/bg_command_pill" />
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Tema" android:textColor="?attr/stitchTextColor" android:textSize="12sp" android:layout_marginTop="8dp" />
            </LinearLayout>
            <LinearLayout android:id="@+id/btn_settings_emoji" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical" android:gravity="center" android:background="?android:attr/selectableItemBackground" android:clickable="true">
                <View android:layout_width="48dp" android:layout_height="48dp" android:background="@drawable/bg_command_pill" />
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Tons" android:textColor="?attr/stitchTextColor" android:textSize="12sp" android:layout_marginTop="8dp" />
            </LinearLayout>
            <LinearLayout android:id="@+id/btn_settings_size" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:orientation="vertical" android:gravity="center" android:background="?android:attr/selectableItemBackground" android:clickable="true">
                <View android:layout_width="48dp" android:layout_height="48dp" android:background="@drawable/bg_command_pill" />
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Tamanho" android:textColor="?attr/stitchTextColor" android:textSize="12sp" android:layout_marginTop="8dp" />
            </LinearLayout>
        </LinearLayout>
    </LinearLayout>
"""

# Insert before </FrameLayout>
idx = content.rfind('</FrameLayout>')
content = content[:idx] + settings_ui + content[idx:]

with open('app/src/main/res/layout/stitch_keyboard_layout.xml', 'w') as f:
    f.write(content)
