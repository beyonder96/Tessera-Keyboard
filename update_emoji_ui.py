import re

# Update layout xml
with open('app/src/main/res/layout/stitch_keyboard_layout.xml', 'r') as f:
    content = f.read()

emoji_start = content.find('<LinearLayout android:id="@+id/emoji_ui_root"')
popup_start = content.find('<LinearLayout android:id="@+id/key_preview_popup"')

new_emoji_ui = """    <LinearLayout android:id="@+id/emoji_ui_root" android:layout_width="match_parent" android:layout_height="280dp" android:orientation="vertical" android:background="?attr/stitchBgColor" android:visibility="gone" android:padding="8dp">
        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center_vertical" android:layout_marginBottom="8dp">
            <TextView android:id="@+id/emoji_category_title" android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:text="Smileys &amp; People" android:textColor="?attr/stitchTextColor" android:textSize="14sp" android:textStyle="bold" android:layout_marginLeft="8dp" />
            <ImageButton android:id="@+id/btn_close_emoji" android:layout_width="32dp" android:layout_height="32dp" android:background="@drawable/bg_command_pill" android:src="@drawable/ic_backspace" android:contentDescription="Close" android:tint="?attr/stitchTextColor" android:padding="6dp" android:scaleType="fitCenter" />
        </LinearLayout>
        
        <ScrollView android:id="@+id/emoji_scroll_view" android:layout_width="match_parent" android:layout_height="0dp" android:layout_weight="1" android:scrollbars="none">
            <GridLayout android:id="@+id/emoji_grid_layout" android:layout_width="match_parent" android:layout_height="wrap_content" android:columnCount="8" android:useDefaultMargins="true" />
        </ScrollView>
        
        <HorizontalScrollView android:layout_width="match_parent" android:layout_height="44dp" android:layout_marginTop="8dp" android:scrollbars="none">
            <LinearLayout android:layout_width="wrap_content" android:layout_height="match_parent" android:orientation="horizontal" android:gravity="center" android:background="@drawable/bg_command_pill" android:paddingHorizontal="4dp">
                <TextView android:id="@+id/cat_smileys" android:layout_width="44dp" android:layout_height="match_parent" android:text="😀" android:gravity="center" android:textSize="20sp" android:background="?android:attr/selectableItemBackgroundBorderless" android:clickable="true" />
                <TextView android:id="@+id/cat_animals" android:layout_width="44dp" android:layout_height="match_parent" android:text="🐻" android:gravity="center" android:textSize="20sp" android:background="?android:attr/selectableItemBackgroundBorderless" android:clickable="true" />
                <TextView android:id="@+id/cat_food" android:layout_width="44dp" android:layout_height="match_parent" android:text="🍔" android:gravity="center" android:textSize="20sp" android:background="?android:attr/selectableItemBackgroundBorderless" android:clickable="true" />
                <TextView android:id="@+id/cat_activities" android:layout_width="44dp" android:layout_height="match_parent" android:text="⚽" android:gravity="center" android:textSize="20sp" android:background="?android:attr/selectableItemBackgroundBorderless" android:clickable="true" />
                <TextView android:id="@+id/cat_travel" android:layout_width="44dp" android:layout_height="match_parent" android:text="🚗" android:gravity="center" android:textSize="20sp" android:background="?android:attr/selectableItemBackgroundBorderless" android:clickable="true" />
                <TextView android:id="@+id/cat_objects" android:layout_width="44dp" android:layout_height="match_parent" android:text="💡" android:gravity="center" android:textSize="20sp" android:background="?android:attr/selectableItemBackgroundBorderless" android:clickable="true" />
                <TextView android:id="@+id/cat_symbols" android:layout_width="44dp" android:layout_height="match_parent" android:text="❤️" android:gravity="center" android:textSize="20sp" android:background="?android:attr/selectableItemBackgroundBorderless" android:clickable="true" />
            </LinearLayout>
        </HorizontalScrollView>
    </LinearLayout>
"""

content = content[:emoji_start] + new_emoji_ui + content[popup_start:]
with open('app/src/main/res/layout/stitch_keyboard_layout.xml', 'w') as f:
    f.write(content)
