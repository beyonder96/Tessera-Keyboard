import xml.etree.ElementTree as ET

new_xml = """<?xml version='1.0' encoding='utf-8'?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android" 
    android:layout_width="match_parent" 
    android:layout_height="wrap_content">
    
    <LinearLayout android:id="@+id/keyboard_root" 
        android:layout_width="match_parent" 
        android:layout_height="wrap_content" 
        android:orientation="vertical" 
        android:background="@drawable/bg_floating_keyboard" 
        android:paddingLeft="6dp" 
        android:paddingRight="6dp" 
        android:paddingTop="12dp" 
        android:paddingBottom="12dp" 
        android:clickable="true" 
        android:focusable="true">

        <!-- Toolbar -->
        <LinearLayout android:layout_width="match_parent" 
            android:layout_height="48dp" 
            android:orientation="horizontal" 
            android:gravity="center" 
            android:layout_marginBottom="12dp">
            
            <TextView android:id="@+id/key_plus" 
                android:layout_width="0dp" 
                android:layout_weight="1.2"
                android:layout_height="40dp" 
                android:text="Aa" 
                android:textColor="#FFFFFF" 
                android:textSize="16sp" 
                android:fontFamily="sans-serif-medium"
                android:gravity="center" 
                android:background="@drawable/bg_command_pill" 
                android:layout_marginRight="4dp" />
                
            <FrameLayout android:id="@+id/key_sparkle" 
                android:layout_width="0dp" 
                android:layout_weight="1"
                android:layout_height="40dp" 
                android:background="@drawable/bg_command_pill" 
                android:layout_marginRight="4dp">
                <View android:layout_width="16dp" android:layout_height="16dp" android:background="@drawable/bg_key_circle" android:layout_gravity="center" />
                <View android:layout_width="6dp" android:layout_height="6dp" android:background="@drawable/bg_circle_active" android:layout_gravity="center" />
            </FrameLayout>
            
            <TextView android:id="@+id/suggestion_2" 
                android:layout_width="0dp"
                android:layout_weight="1" 
                android:layout_height="40dp" 
                android:text="≡" 
                android:textColor="#FFFFFF" 
                android:textSize="20sp" 
                android:gravity="center" 
                android:background="@drawable/bg_command_pill" 
                android:layout_marginRight="4dp" />
            
            <!-- Font sizes / Suggestions -->
            <LinearLayout android:layout_width="0dp" 
                android:layout_weight="2"
                android:layout_height="40dp" 
                android:orientation="horizontal" 
                android:gravity="center" 
                android:background="@drawable/bg_command_pill">
                <TextView android:id="@+id/suggestion_1" android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content" android:text="15" android:textColor="#888888" android:textSize="14sp" android:gravity="center" />
                <TextView android:id="@+id/suggestion_3" android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content" android:text="16" android:textColor="#FFFFFF" android:textSize="16sp" android:gravity="center" />
                <TextView android:id="@+id/key_ia" android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content" android:text="17" android:textColor="#888888" android:textSize="16sp" android:gravity="center" />
            </LinearLayout>
        </LinearLayout>

        <RelativeLayout android:layout_width="match_parent" android:layout_height="wrap_content">
            <LinearLayout android:id="@+id/keys_container" android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical">
                
                <!-- Row 1 -->
                <LinearLayout android:layout_width="match_parent" android:layout_height="54dp" android:orientation="horizontal" android:weightSum="10" android:gravity="center">
"""
# Helper to generate a key
def generate_key(id_name, text, weight="1"):
    return f"""
                    <FrameLayout android:layout_width="0dp" android:layout_height="match_parent" android:layout_weight="{weight}">
                        <TextView android:id="@+id/{id_name}" android:layout_width="42dp" android:layout_height="42dp" android:layout_gravity="center" android:text="{text}" android:textColor="#FFFFFF" android:textSize="20sp" android:gravity="center" android:background="@drawable/bg_key_circle" android:clickable="true" android:focusable="true" android:fontFamily="sans-serif-light" />
                    </FrameLayout>"""

row1 = ['q','w','e','r','t','y','u','i','o','p']
for k in row1:
    new_xml += generate_key(f'key_{k}', k.upper())

new_xml += """
                </LinearLayout>
                
                <!-- Row 2 -->
                <LinearLayout android:layout_width="match_parent" android:layout_height="54dp" android:orientation="horizontal" android:weightSum="10" android:gravity="center">
                    <View android:layout_width="0dp" android:layout_height="match_parent" android:layout_weight="0.5" />"""

row2 = ['a','s','d','f','g','h','j','k','l']
for k in row2:
    new_xml += generate_key(f'key_{k}', k.upper())

new_xml += """
                    <View android:layout_width="0dp" android:layout_height="match_parent" android:layout_weight="0.5" />
                </LinearLayout>
                
                <!-- Row 3 -->
                <LinearLayout android:layout_width="match_parent" android:layout_height="54dp" android:orientation="horizontal" android:weightSum="10" android:gravity="center">
                    <FrameLayout android:id="@+id/key_shift" android:layout_width="0dp" android:layout_height="match_parent" android:layout_weight="1.5">
                        <FrameLayout android:layout_width="42dp" android:layout_height="42dp" android:layout_gravity="center" android:background="@drawable/bg_command_pill" android:clickable="true" android:focusable="true">
                            <ImageView android:id="@+id/img_shift_icon" android:layout_width="18dp" android:layout_height="18dp" android:layout_gravity="center" android:src="@drawable/ic_shift" android:tint="#FFFFFF" />
                        </FrameLayout>
                    </FrameLayout>"""

row3 = ['z','x','c','v','b','n','m']
for k in row3:
    new_xml += generate_key(f'key_{k}', k.upper())

new_xml += """
                    <FrameLayout android:id="@+id/key_backspace" android:layout_width="0dp" android:layout_height="match_parent" android:layout_weight="1.5">
                        <FrameLayout android:layout_width="42dp" android:layout_height="42dp" android:layout_gravity="center" android:background="@drawable/bg_command_pill" android:clickable="true" android:focusable="true">
                            <ImageView android:layout_width="18dp" android:layout_height="18dp" android:layout_gravity="center" android:src="@drawable/ic_backspace" android:tint="#FFFFFF" />
                        </FrameLayout>
                    </FrameLayout>
                </LinearLayout>
                
                <!-- Row 4 -->
                <LinearLayout android:layout_width="match_parent" android:layout_height="54dp" android:orientation="horizontal" android:weightSum="10" android:gravity="center">
                    
                    <FrameLayout android:id="@+id/key_symbol" android:layout_width="0dp" android:layout_height="match_parent" android:layout_weight="2">
                        <TextView android:id="@+id/text_key_symbol" android:layout_width="42dp" android:layout_height="42dp" android:layout_gravity="center" android:text="123" android:textColor="#FFFFFF" android:textSize="16sp" android:gravity="center" android:background="@drawable/bg_command_pill" android:clickable="true" android:focusable="true" android:fontFamily="sans-serif-light" />
                    </FrameLayout>
                    
                    <FrameLayout android:layout_width="0dp" android:layout_height="match_parent" android:layout_weight="6">
                        <com.example.ui.ShimmerTextView android:id="@+id/key_space" android:layout_width="match_parent" android:layout_height="42dp" android:layout_gravity="center" android:layout_marginHorizontal="8dp" android:background="@drawable/bg_command_pill" android:text="space" android:fontFamily="sans-serif-light" android:textColor="#FFFFFF" android:textSize="16sp" android:gravity="center" android:clickable="true" android:focusable="true" />
                    </FrameLayout>

                    <FrameLayout android:id="@+id/key_enter" android:layout_width="0dp" android:layout_height="match_parent" android:layout_weight="2">
                        <TextView android:layout_width="42dp" android:layout_height="42dp" android:layout_gravity="center" android:text="ret" android:textColor="#FFFFFF" android:textSize="16sp" android:gravity="center" android:background="@drawable/bg_command_pill" android:clickable="true" android:focusable="true" android:fontFamily="sans-serif-light" />
                    </FrameLayout>

                </LinearLayout>
            </LinearLayout>
            
            <com.example.ui.SwipeGestureOverlay android:id="@+id/swipe_overlay" android:layout_width="match_parent" android:layout_height="0dp" android:layout_alignTop="@id/keys_container" android:layout_alignBottom="@id/keys_container" />
        </RelativeLayout>
    </LinearLayout>
    
    <!-- Hidden Layouts for logic compatibility -->
    <FrameLayout android:id="@+id/key_mic" android:layout_width="0dp" android:layout_height="0dp" />
    <FrameLayout android:id="@+id/key_emoji" android:layout_width="0dp" android:layout_height="0dp" />
    <LinearLayout android:id="@+id/voice_ui_root" android:layout_width="0dp" android:layout_height="0dp" android:visibility="gone"/>
    <LinearLayout android:id="@+id/ai_ui_root" android:layout_width="0dp" android:layout_height="0dp" android:visibility="gone"/>
    <LinearLayout android:id="@+id/emoji_ui_root" android:layout_width="0dp" android:layout_height="0dp" android:visibility="gone"/>
    <LinearLayout android:id="@+id/settings_ui_root" android:layout_width="0dp" android:layout_height="0dp" android:visibility="gone"/>
    <LinearLayout android:id="@+id/key_preview_popup" android:layout_width="0dp" android:layout_height="0dp" android:visibility="gone"/>

</FrameLayout>
"""

with open("app/src/main/res/layout/stitch_keyboard_layout.xml", "w") as f:
    f.write(new_xml)
print("Generated new exact layout.")
