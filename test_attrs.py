with open('app/src/main/res/values/attrs.xml', 'w') as f:
    f.write('''<?xml version="1.0" encoding="utf-8"?>
<resources>
    <attr name="stitchBgColor" format="color" />
    <attr name="stitchGlowColor" format="color" />
    <attr name="stitchKeyBgColor" format="color" />
    <attr name="stitchKeyBorderColor" format="color" />
    <attr name="stitchTextColor" format="color" />
    <attr name="stitchTextInactiveColor" format="color" />
    <attr name="stitchGlassBgTranslucent" format="color" />
    <attr name="stitchAiBg" format="color" />
    <attr name="stitchAiText" format="color" />
</resources>
''')
