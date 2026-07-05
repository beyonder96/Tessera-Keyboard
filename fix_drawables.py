import os
import xml.etree.ElementTree as ET

drawables_dir = 'app/src/main/res/drawable'

# 1. bg_keyboard_main.xml
# <solid android:color="@color/stitch_background" />
content = open(f'{drawables_dir}/bg_keyboard_main.xml').read()
content = content.replace('@color/stitch_background', '?attr/stitchBgColor')
content = content.replace('@color/glass_border', '?attr/stitchKeyBorderColor')
open(f'{drawables_dir}/bg_keyboard_main.xml', 'w').write(content)

# 2. bg_floating_key.xml
# <solid android:color="#0DFFFFFF" />
# <stroke android:color="#1FFFFFFF" />
content = open(f'{drawables_dir}/bg_floating_key.xml').read()
content = content.replace('#0DFFFFFF', '?attr/stitchKeyBgColor')
content = content.replace('#1FFFFFFF', '?attr/stitchKeyBorderColor')
content = content.replace('@color/stitch_glow_pressed', '?attr/stitchGlowColor')
open(f'{drawables_dir}/bg_floating_key.xml', 'w').write(content)

# 3. bg_command_pill.xml
content = open(f'{drawables_dir}/bg_command_pill.xml').read()
content = content.replace('@color/glass_bg_translucent', '?attr/stitchGlassBgTranslucent')
content = content.replace('@color/glass_border', '?attr/stitchKeyBorderColor')
open(f'{drawables_dir}/bg_command_pill.xml', 'w').write(content)

# 4. bg_command_pill_active.xml
content = open(f'{drawables_dir}/bg_command_pill_active.xml').read()
content = content.replace('@color/glass_ai_bg', '?attr/stitchAiBg')
content = content.replace('@color/stitch_glow', '?attr/stitchGlowColor')
open(f'{drawables_dir}/bg_command_pill_active.xml', 'w').write(content)

# 5. bg_preview_popup.xml
content = open(f'{drawables_dir}/bg_preview_popup.xml').read()
content = content.replace('#E6001F1F', '?attr/stitchBgColor')
content = content.replace('#06fbfb', '?attr/stitchGlowColor')
open(f'{drawables_dir}/bg_preview_popup.xml', 'w').write(content)
