import re

with open('app/src/main/res/layout/stitch_keyboard_layout.xml', 'r') as f:
    content = f.read()

# I will find the EXACT string block that is messed up and replace it.
# The messed up block is from `<LinearLayout android:id="@+id/voice_ui_root"` to the `</FrameLayout></FrameLayout>` at the end.
# I'll rebuild everything after `<LinearLayout android:id="@+id/keyboard_root"` ends.

root_end = content.find('</LinearLayout>', content.find('id="@+id/keyboard_root"'))
# Actually `keyboard_root` has many nested layouts. It ends at line 128: `</LinearLayout><LinearLayout android:id="@+id/voice_ui_root"`
voice_start = content.find('<LinearLayout android:id="@+id/voice_ui_root"')
ai_start = content.find('<LinearLayout android:id="@+id/ai_ui_root"')
emoji_start = content.find('<LinearLayout android:id="@+id/emoji_ui_root"')
popup_start = content.find('<LinearLayout android:id="@+id/key_preview_popup"')

if voice_start != -1 and ai_start != -1:
    new_voice = """
    <LinearLayout android:id="@+id/voice_ui_root" android:layout_width="match_parent" android:layout_height="match_parent" android:orientation="vertical" android:background="?attr/stitchBgColor" android:gravity="center" android:visibility="gone" android:clickable="true" android:focusable="true" android:padding="24dp">
        <TextView android:id="@+id/voice_text" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="Fale agora..." android:textColor="?attr/stitchTextColor" android:textSize="18sp" android:gravity="center" android:fontFamily="sans-serif-medium" android:layout_marginBottom="16dp" />
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
    </LinearLayout>
    """
    
    # We replace from voice_start to ai_start with new_voice
    content = content[:voice_start] + new_voice + content[ai_start:]
    
    # Now fix the end of file because of </FrameLayout></FrameLayout>
    content = content.replace('</FrameLayout></FrameLayout>', '</FrameLayout>')
    
    with open('app/src/main/res/layout/stitch_keyboard_layout.xml', 'w') as f:
        f.write(content)
