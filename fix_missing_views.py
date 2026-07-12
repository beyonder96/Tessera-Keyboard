import re
import xml.etree.ElementTree as ET

filepath = 'app/src/main/res/layout/stitch_keyboard_layout.xml'
with open(filepath, 'r') as f:
    content = f.read()

# Make sure all missing views the Kotlin code complains about exist as gone views at the end.
missing_views = """
    <!-- Missing Views for Kotlin bindings -->
    <TextView android:id="@+id/suggestion_1" android:layout_width="0dp" android:layout_height="0dp" android:visibility="gone" />
    <TextView android:id="@+id/suggestion_2" android:layout_width="0dp" android:layout_height="0dp" android:visibility="gone" />
    <TextView android:id="@+id/suggestion_3" android:layout_width="0dp" android:layout_height="0dp" android:visibility="gone" />
    <FrameLayout android:id="@+id/key_ia" android:layout_width="0dp" android:layout_height="0dp" android:visibility="gone" />
"""

content = content.replace('</FrameLayout>', missing_views + '\n</FrameLayout>')

with open(filepath, 'w') as f:
    f.write(content)

print("Added dummy views back to XML.")
