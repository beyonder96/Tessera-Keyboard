import re

with open('app/src/main/res/layout/stitch_keyboard_layout.xml', 'r') as f:
    content = f.read()

# The Q row starts with:
# <LinearLayout android:layout_width="match_parent" android:layout_height="52dp" android:orientation="horizontal" android:weightSum="10" android:layout_marginBottom="6dp">
# The bottom row ends with:
#     </LinearLayout>

start_q = content.find('<LinearLayout android:layout_width="match_parent" android:layout_height="52dp" android:orientation="horizontal" android:weightSum="10" android:layout_marginBottom="6dp">')
bottom_row_start = content.find('<LinearLayout android:layout_width="match_parent" android:layout_height="52dp" android:orientation="horizontal" android:weightSum="10" android:gravity="center_vertical">')
end_bottom_row = content.find('</LinearLayout>', bottom_row_start) + 15

wrapped_content = (
    '<FrameLayout android:layout_width="match_parent" android:layout_height="wrap_content">\n' +
    '    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical">\n' +
    content[start_q:end_bottom_row] + '\n' +
    '    </LinearLayout>\n' +
    '    <com.example.ui.SwipeGestureOverlay android:id="@+id/swipe_overlay" android:layout_width="match_parent" android:layout_height="match_parent" />\n' +
    '</FrameLayout>'
)

content = content[:start_q] + wrapped_content + content[end_bottom_row:]

with open('app/src/main/res/layout/stitch_keyboard_layout.xml', 'w') as f:
    f.write(content)
