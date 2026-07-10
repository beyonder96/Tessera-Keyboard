import re

with open('app/src/main/res/layout/stitch_keyboard_layout.xml', 'r') as f:
    content = f.read()

# The original voice_ui_root block we want to replace:
old_voice_ui = """<LinearLayout android:id="@+id/voice_ui_root" android:layout_width="match_parent" android:layout_height="match_parent" android:orientation="vertical" android:background="@color/stitch_background" android:gravity="center" android:visibility="gone" android:clickable="true" android:padding="24dp"><TextView android:id="@+id/voice_text" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="Ouvindo..." android:textColor="@color/white" android:textSize="18sp" android:gravity="center" /><LinearLayout android:id="@+id/voice_wave_container" android:layout_width="wrap_content" android:layout_height="60dp" android:orientation="horizontal" android:gravity="center" android:layout_marginTop="16dp"><View android:id="@+id/wave_bar_1" android:layout_width="6dp" android:layout_height="16dp" android:layout_marginLeft="4dp" android:layout_marginRight="4dp" android:background="@drawable/bg_command_pill_active" /><View android:id="@+id/wave_bar_2" android:layout_width="6dp" android:layout_height="32dp" android:layout_marginLeft="4dp" android:layout_marginRight="4dp" android:background="@drawable/bg_command_pill_active" /><View android:id="@+id/wave_bar_3" android:layout_width="6dp" android:layout_height="44dp" android:layout_marginLeft="4dp" android:layout_marginRight="4dp" android:background="@drawable/bg_command_pill_active" /><View android:id="@+id/wave_bar_4" android:layout_width="6dp" android:layout_height="28dp" android:layout_marginLeft="4dp" android:layout_marginRight="4dp" android:background="@drawable/bg_command_pill_active" /><View android:id="@+id/wave_bar_5" android:layout_width="6dp" android:layout_height="12dp" android:layout_marginLeft="4dp" android:layout_marginRight="4dp" android:background="@drawable/bg_command_pill_active" /></LinearLayout><Button android:id="@+id/btn_close_voice" android:layout_width="wrap_content" android:layout_height="wrap_content" android:layout_marginTop="16dp" android:text="Cancelar" /></LinearLayout>"""

new_voice_ui = """<LinearLayout android:id="@+id/voice_ui_root" android:layout_width="match_parent" android:layout_height="match_parent" android:orientation="vertical" android:background="?attr/stitchBgColor" android:gravity="center" android:visibility="gone" android:clickable="true" android:focusable="true" android:padding="24dp">
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

# But because I might have messed up the file with the previous script, I'll download a fresh copy or use a smart regex if it's broken.
# Wait! I messed it up, so the file has my WRONG structure now. Let me find the WRONG structure.
wrong_structure_start = content.find('<LinearLayout android:id="@+id/voice_ui_root"')
wrong_structure_end = content.find('</FrameLayout>', wrong_structure_start)
if wrong_structure_end != -1:
    # This is too hard, I will just fix the layout manually.
    pass

