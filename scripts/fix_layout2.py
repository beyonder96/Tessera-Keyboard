import re

with open('app/src/main/res/layout/stitch_keyboard_layout.xml', 'r') as f:
    content = f.read()

start_index = content.find('<LinearLayout android:id="@+id/voice_ui_root"')
if start_index != -1:
    end_tag = '</LinearLayout>'
    # count open and close tags to find the matching close tag
    count = 0
    current_index = start_index
    while current_index < len(content):
        next_open = content.find('<LinearLayout', current_index + 1)
        next_close = content.find('</LinearLayout>', current_index + 1)
        
        if next_close == -1:
            break
            
        if next_open != -1 and next_open < next_close:
            count += 1
            current_index = next_open
        else:
            count -= 1
            current_index = next_close
            if count == 0:
                end_index = current_index + len('</LinearLayout>')
                
                voice_ui_new = """<LinearLayout android:id="@+id/voice_ui_root" android:layout_width="match_parent" android:layout_height="match_parent" android:orientation="vertical" android:background="?attr/stitchBgColor" android:gravity="center" android:visibility="gone" android:clickable="true" android:focusable="true" android:padding="24dp">

    <TextView android:id="@+id/voice_text" android:layout_width="match_parent" android:layout_height="0dp" android:layout_weight="1" android:text="Fale agora..." android:textColor="?attr/stitchTextColor" android:textSize="18sp" android:gravity="center" android:fontFamily="sans-serif-medium" android:layout_marginBottom="16dp" />

    <FrameLayout android:layout_width="80dp" android:layout_height="80dp" android:layout_marginBottom="16dp">
        <View android:id="@+id/voice_pulse_bg" android:layout_width="match_parent" android:layout_height="match_parent" android:background="@drawable/bg_preview_popup" android:alpha="0.2" />
        <ImageButton android:id="@+id/btn_mic_action" android:layout_width="60dp" android:layout_height="60dp" android:layout_gravity="center" android:background="@drawable/bg_command_pill_active" android:src="@drawable/ic_microphone" android:tint="?attr/stitchBgColor" android:scaleType="centerInside" android:contentDescription="Ouvindo" />
    </FrameLayout>
    
    <LinearLayout android:id="@+id/voice_wave_container" android:layout_width="wrap_content" android:layout_height="40dp" android:orientation="horizontal" android:gravity="center" android:layout_marginBottom="16dp">
        <View android:id="@+id/wave_bar_1" android:layout_width="4dp" android:layout_height="12dp" android:layout_marginHorizontal="3dp" android:background="@drawable/bg_command_pill_active" />
        <View android:id="@+id/wave_bar_2" android:layout_width="4dp" android:layout_height="24dp" android:layout_marginHorizontal="3dp" android:background="@drawable/bg_command_pill_active" />
        <View android:id="@+id/wave_bar_3" android:layout_width="4dp" android:layout_height="32dp" android:layout_marginHorizontal="3dp" android:background="@drawable/bg_command_pill_active" />
        <View android:id="@+id/wave_bar_4" android:layout_width="4dp" android:layout_height="24dp" android:layout_marginHorizontal="3dp" android:background="@drawable/bg_command_pill_active" />
        <View android:id="@+id/wave_bar_5" android:layout_width="4dp" android:layout_height="12dp" android:layout_marginHorizontal="3dp" android:background="@drawable/bg_command_pill_active" />
    </LinearLayout>

    <ImageButton android:id="@+id/btn_close_voice" android:layout_width="48dp" android:layout_height="48dp" android:background="@drawable/bg_command_pill" android:src="@drawable/ic_backspace" android:contentDescription="Cancelar" android:tint="?attr/stitchTextColor" android:padding="12dp" android:scaleType="fitCenter" />
</LinearLayout>"""
                
                content = content[:start_index] + voice_ui_new + content[end_index:]
                with open('app/src/main/res/layout/stitch_keyboard_layout.xml', 'w') as f:
                    f.write(content)
                print("Successfully replaced layout!")
                break
