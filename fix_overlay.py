import re

with open('app/src/main/res/layout/stitch_keyboard_layout.xml', 'r') as f:
    content = f.read()

# Replace the FrameLayout at line 33 with a RelativeLayout
content = content.replace('<FrameLayout android:layout_width="match_parent" android:layout_height="wrap_content">\n    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical">',
'<RelativeLayout android:layout_width="match_parent" android:layout_height="wrap_content">\n    <LinearLayout android:id="@+id/keys_container" android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical">')

# Replace the swipe overlay to use alignTop and alignBottom
old_swipe = '<com.example.ui.SwipeGestureOverlay android:id="@+id/swipe_overlay" android:layout_width="match_parent" android:layout_height="match_parent" />\n</FrameLayout>'
new_swipe = '<com.example.ui.SwipeGestureOverlay android:id="@+id/swipe_overlay" android:layout_width="match_parent" android:layout_height="0dp" android:layout_alignTop="@id/keys_container" android:layout_alignBottom="@id/keys_container" />\n</RelativeLayout>'

content = content.replace(old_swipe, new_swipe)

with open('app/src/main/res/layout/stitch_keyboard_layout.xml', 'w') as f:
    f.write(content)
