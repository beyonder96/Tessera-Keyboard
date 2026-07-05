import xml.etree.ElementTree as ET

tree = ET.parse('app/src/main/res/layout/stitch_keyboard_layout.xml')
root = tree.getroot()

ET.register_namespace('android', 'http://schemas.android.com/apk/res/android')

keyboard_root = None
for child in root:
    if child.get('{http://schemas.android.com/apk/res/android}id') == '@+id/keyboard_root':
        keyboard_root = child
        break

# Create drag handle container
handle_container = ET.Element('FrameLayout')
handle_container.set('{http://schemas.android.com/apk/res/android}id', '@+id/drag_handle_container')
handle_container.set('{http://schemas.android.com/apk/res/android}layout_width', 'match_parent')
handle_container.set('{http://schemas.android.com/apk/res/android}layout_height', '24dp')
handle_container.set('{http://schemas.android.com/apk/res/android}layout_marginTop', '-8dp')

# Create the pill itself
pill = ET.SubElement(handle_container, 'View')
pill.set('{http://schemas.android.com/apk/res/android}layout_width', '40dp')
pill.set('{http://schemas.android.com/apk/res/android}layout_height', '4dp')
pill.set('{http://schemas.android.com/apk/res/android}layout_gravity', 'center')
pill.set('{http://schemas.android.com/apk/res/android}background', '@drawable/bg_command_pill')
pill.set('{http://schemas.android.com/apk/res/android}alpha', '0.5')

# Insert it at the very beginning of keyboard_root
keyboard_root.insert(0, handle_container)

tree.write('app/src/main/res/layout/stitch_keyboard_layout.xml', xml_declaration=True, encoding='utf-8')
