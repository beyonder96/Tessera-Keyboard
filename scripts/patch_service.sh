#!/bin/bash
sed -i '/private lateinit var settingsRoot/d' app/src/main/java/com/example/StitchKeyboardService.kt
sed -i '/private lateinit var aiRoot/d' app/src/main/java/com/example/StitchKeyboardService.kt
sed -i '/private var swipeEnabled/d' app/src/main/java/com/example/StitchKeyboardService.kt
sed -i '/aiRoot = keyboardView.findViewById(R.id.ai_ui_root)/d' app/src/main/java/com/example/StitchKeyboardService.kt
sed -i '/settingsRoot = keyboardView.findViewById(R.id.settings_ui_root)/d' app/src/main/java/com/example/StitchKeyboardService.kt
sed -i '/setupSwipe(keyboardView)/d' app/src/main/java/com/example/StitchKeyboardService.kt
sed -i '/keyboardView.findViewById<View>(R.id.btn_close_ai)?.setOnClickListener/,/btn_ai_casual).*rewriteText/d' app/src/main/java/com/example/StitchKeyboardService.kt
sed -i '/swipeEnabled = prefs.getBoolean("SWIPE_ENABLED", true)/d' app/src/main/java/com/example/StitchKeyboardService.kt
sed -i '/iaBtn?.setOnClickListener/,/aiRoot.visibility = View.VISIBLE\n        }/d' app/src/main/java/com/example/StitchKeyboardService.kt
sed -i '/private fun setupSwipe/,/^    }/d' app/src/main/java/com/example/StitchKeyboardService.kt
sed -i '/private fun commitSwipeText/,/^    }/d' app/src/main/java/com/example/StitchKeyboardService.kt
