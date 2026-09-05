package com.example.service

import com.example.R
import com.example.BuildConfig
import com.example.engine.PredictionEngine
import com.example.manager.GhostTextManager
import com.example.activity.MainActivity
import com.example.api.GenerateContentRequest
import com.example.api.Content
import com.example.api.Part
import com.example.api.RetrofitClient
import android.annotation.SuppressLint
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.ContextThemeWrapper
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.cancel

class StitchKeyboardService : InputMethodService() {

    private var isShifted = false
    private lateinit var shiftText: TextView
    private val alphabetKeys = mutableMapOf<Int, String>()
    private val keyViewMap = mutableMapOf<Int, TextView>()
    private val composingBuffer = java.lang.StringBuilder(32)
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val keyLocation = IntArray(2)
    private val rootLocation = IntArray(2)
    private var localEditCount = 0
    private val keyPositionCache = HashMap<Int, Pair<Float, Float>>()
    private var spaceKeyView: TextView? = null
    private lateinit var numericRoot: View
    private var isExtendedSymbolMode = false
    
    private data class LastAutocorrection(
        val originalWord: String,
        val correctedWord: String
    )
    private var lastAutocorrection: LastAutocorrection? = null
    private var lastUndoneWord: String? = null
    private var justCommittedSpace = false
    private var lastSpaceTime = 0L
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var keyboardRoot: View
    private lateinit var voiceRoot: View
    private lateinit var emojiRoot: View
    private lateinit var localDict: com.example.manager.LocalDictionaryManager
    private lateinit var predictionEngine: com.example.engine.PredictionEngine
    private val ghostTextManager = com.example.manager.GhostTextManager()
    private var suggestionContainer: LinearLayout? = null
    private var suggestion1: android.widget.TextView? = null
    private var suggestion2: android.widget.TextView? = null
    private var suggestion3: android.widget.TextView? = null
    private var clipboardPill: android.widget.TextView? = null
    private var dragPill: android.view.View? = null
    private var enterIcon: android.widget.ImageView? = null
    private var lastClipboardText: String? = null
    private var lastConsumedClip: String? = null
    private var clipboardDismissRunnable: Runnable? = null
    private var predictionJob: Job? = null
    private var lastQueriedWord: String = ""
    private var cachedKeyboardScale: Float = 1.0f
    
    // Wave animation bars
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var voiceText: TextView? = null

    private var waveJob: Job? = null

    // Key preview popups
    private lateinit var previewPopup: View
    private lateinit var previewPopupText: TextView
    
    private var vibrator: android.os.Vibrator? = null
    private var audioManager: android.media.AudioManager? = null
    private var inputMethodManager: android.view.inputmethod.InputMethodManager? = null

    override fun onCreate() {
        super.onCreate()
        localDict = com.example.manager.LocalDictionaryManager(this)
        predictionEngine = com.example.engine.PredictionEngine(localDict, this)
        vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        audioManager = getSystemService(android.content.Context.AUDIO_SERVICE) as? android.media.AudioManager
        inputMethodManager = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
        cachedKeyboardScale = getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE).getFloat("KEYBOARD_SCALE", 1.0f)
    }

    override fun onCreateInputView(): View {
        try {
            val themePref = getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE).getString("KEYBOARD_THEME", "Dark") ?: "Dark"
            currentTheme = themePref
            val themeResId = if (themePref == "Light") R.style.Theme_Tessera_Light else R.style.Theme_Tessera_Dark
            val themedContext = ContextThemeWrapper(this, themeResId)
            val keyboardView = layoutInflater.cloneInContext(themedContext)
                .inflate(R.layout.stitch_keyboard_layout, null)
            keyPositionCache.clear()

            keyboardRoot = keyboardView.findViewById(R.id.keyboard_root)
            voiceRoot = keyboardView.findViewById(R.id.voice_ui_root)
            emojiRoot = keyboardView.findViewById(R.id.emoji_ui_root)
            numericRoot = keyboardView.findViewById(R.id.numeric_keypad_root)
            dragPill = keyboardView.findViewById(R.id.drag_pill)

            // Key preview popup elements
            previewPopup = keyboardView.findViewById(R.id.key_preview_popup)
            voiceText = keyboardView.findViewById(R.id.voice_text)
            previewPopupText = keyboardView.findViewById(R.id.key_preview_text)

            keyboardView.findViewById<View>(R.id.btn_close_voice)?.setOnClickListener {
                stopListening()
                waveJob?.cancel()
                voiceRoot.visibility = View.GONE
                keyboardRoot.visibility = View.VISIBLE
            }
            keyboardView.findViewById<View>(R.id.btn_mic_action)?.setOnClickListener {
                if (isListening) stopListening() else startListening()
            }
            keyboardView.findViewById<View>(R.id.btn_close_emoji)?.setOnClickListener {
                emojiRoot.visibility = View.GONE
                keyboardRoot.visibility = View.VISIBLE
            }

            setupKeys(keyboardView)
            setupEmojiKeyboard(keyboardView)
            setupCommandKeys(keyboardView)
            setupSuggestionBar(keyboardView)
            setupDragResizer(keyboardView)
            setupEmojiGrid(keyboardView)
            setupNumericKeyboard(keyboardView)

            keyboardView.post {
                prewarmKeyPositions()
            }

            return keyboardView
        } catch (e: Exception) {
            val errorView = TextView(this)
            errorView.text = "Error loading keyboard:\n${e.message}\n${e.stackTraceToString()}"
            errorView.setTextColor(android.graphics.Color.WHITE)
            errorView.setBackgroundColor(android.graphics.Color.RED)
            return errorView
        }
    }

    private fun prewarmKeyPositions() {
        if (!::keyboardRoot.isInitialized) return
        keyboardRoot.getLocationInWindow(rootLocation)
        for ((id, keyView) in keyViewMap) {
            keyView.getLocationInWindow(keyLocation)
            keyPositionCache[id] = Pair(
                (keyLocation[0] - rootLocation[0]).toFloat(),
                (keyLocation[1] - rootLocation[1]).toFloat()
            )
        }
    }

    private var currentTheme = "Dark"
    private var isSymbolMode = false

    private val idMapLetters = mapOf(
        R.id.key_q to "q", R.id.key_w to "w", R.id.key_e to "e", R.id.key_r to "r",
        R.id.key_t to "t", R.id.key_y to "y", R.id.key_u to "u", R.id.key_i to "i",
        R.id.key_o to "o", R.id.key_p to "p",
        R.id.key_a to "a", R.id.key_s to "s", R.id.key_d to "d", R.id.key_f to "f",
        R.id.key_g to "g", R.id.key_h to "h", R.id.key_j to "j", R.id.key_k to "k",
        R.id.key_l to "l",
        R.id.key_z to "z", R.id.key_x to "x", R.id.key_c to "c", R.id.key_v to "v",
        R.id.key_b to "b", R.id.key_n to "n", R.id.key_m to "m", R.id.key_comma to ",", R.id.key_period to "."
    )

    private val idMapSymbols = mapOf(
        R.id.key_q to "1", R.id.key_w to "2", R.id.key_e to "3", R.id.key_r to "4",
        R.id.key_t to "5", R.id.key_y to "6", R.id.key_u to "7", R.id.key_i to "8",
        R.id.key_o to "9", R.id.key_p to "0",
        R.id.key_a to "@", R.id.key_s to "#", R.id.key_d to "$", R.id.key_f to "%",
        R.id.key_g to "&", R.id.key_h to "-", R.id.key_j to "+", R.id.key_k to "(",
        R.id.key_l to ")",
        R.id.key_z to "/", R.id.key_x to "*", R.id.key_c to "\"", R.id.key_v to "'",
        R.id.key_b to ":", R.id.key_n to ";", R.id.key_m to "!", R.id.key_comma to "?", R.id.key_period to "."
    )

    private val idMapExtendedSymbols = mapOf(
        R.id.key_q to "~", R.id.key_w to "\\", R.id.key_e to "|", R.id.key_r to "^",
        R.id.key_t to "=", R.id.key_y to "{", R.id.key_u to "}", R.id.key_i to "[",
        R.id.key_o to "]", R.id.key_p to "°",
        R.id.key_a to "<", R.id.key_s to ">", R.id.key_d to "_", R.id.key_f to "€",
        R.id.key_g to "£", R.id.key_h to "¥", R.id.key_j to "•", R.id.key_k to "©",
        R.id.key_l to "®",
        R.id.key_z to "«", R.id.key_x to "»", R.id.key_c to "§", R.id.key_v to "`",
        R.id.key_b to "~", R.id.key_n to "™", R.id.key_m to "¿", R.id.key_comma to ",", R.id.key_period to "."
    )

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int,
        newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        if (localEditCount > 0) {
            localEditCount--
            return
        }
        if (newSelStart != oldSelStart || newSelEnd != oldSelEnd) {
            composingBuffer.setLength(0)
            clearPredictionsUi()
        }
    }

    private fun isWordChar(c: Char): Boolean {
        return c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9' ||
                c in "áéíóúãõâêîôûçÁÉÍÓÚÃÕÂÊÎÔÛÇ"
    }

    private fun syncComposingBufferFromIme() {
        val ic = currentInputConnection ?: return
        val textBefore = ic.getTextBeforeCursor(30, 0)?.toString() ?: ""
        composingBuffer.setLength(0)
        for (i in textBefore.length - 1 downTo 0) {
            val c = textBefore[i]
            if (isWordChar(c)) {
                composingBuffer.insert(0, c)
            } else {
                break
            }
        }
    }

    private fun isPrivateOrPassword(info: EditorInfo?): Boolean {
        if (info == null) return false
        val inputType = info.inputType
        val variation = inputType and EditorInfo.TYPE_MASK_VARIATION
        val isPassword = variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                (inputType and EditorInfo.TYPE_MASK_CLASS) == EditorInfo.TYPE_CLASS_NUMBER
        val noLearning = (info.imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0
        return isPassword || noLearning
    }

    private fun isNumericField(info: EditorInfo?): Boolean {
        if (info == null) return false
        val inputClass = info.inputType and EditorInfo.TYPE_MASK_CLASS
        return inputClass == EditorInfo.TYPE_CLASS_NUMBER ||
               inputClass == EditorInfo.TYPE_CLASS_PHONE ||
               inputClass == EditorInfo.TYPE_CLASS_DATETIME
    }

    private fun isEmailField(info: EditorInfo?): Boolean {
        if (info == null) return false
        val variation = info.inputType and EditorInfo.TYPE_MASK_VARIATION
        return variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
                variation == EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
    }

    private fun isUriField(info: EditorInfo?): Boolean {
        if (info == null) return false
        val variation = info.inputType and EditorInfo.TYPE_MASK_VARIATION
        return variation == EditorInfo.TYPE_TEXT_VARIATION_URI
    }

    private fun getCharForId(id: Int): String? {
        if (isSymbolMode) {
            return if (isExtendedSymbolMode) idMapExtendedSymbols[id] else idMapSymbols[id]
        }
        if (id == R.id.key_comma) {
            val editorInfo = currentInputEditorInfo
            if (isEmailField(editorInfo)) return "@"
            if (isUriField(editorInfo)) return "/"
            return ","
        }
        return idMapLetters[id]
    }

    private fun shouldAutocorrect(info: EditorInfo?): Boolean {
        val autoCorrectPref = getSharedPreferences("StitchPrefs", Context.MODE_PRIVATE)
            .getBoolean("PREF_AUTOCORRECT", true)
        if (!autoCorrectPref) return false
        if (info == null) return true
        if (isPrivateOrPassword(info)) return false
        val inputType = info.inputType
        val variation = inputType and EditorInfo.TYPE_MASK_VARIATION
        val isEmailOrUrl = variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
                variation == EditorInfo.TYPE_TEXT_VARIATION_URI ||
                variation == EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
        return !isEmailOrUrl
    }

    private fun shouldAutoCapitalize(info: EditorInfo?): Boolean {
        val autoCapPref = getSharedPreferences("StitchPrefs", Context.MODE_PRIVATE)
            .getBoolean("PREF_AUTO_CAP", true)
        if (!autoCapPref) return false
        if (info == null) return false
        val inputType = info.inputType
        if ((inputType and EditorInfo.TYPE_MASK_CLASS) != EditorInfo.TYPE_CLASS_TEXT) return false
        if (isPrivateOrPassword(info)) return false
        val variation = inputType and EditorInfo.TYPE_MASK_VARIATION
        if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
            variation == EditorInfo.TYPE_TEXT_VARIATION_URI ||
            variation == EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS) {
            return false
        }
        return true
    }

    private fun isPunctuation(c: String): Boolean {
        return c in listOf(".", ",", "!", "?", ":", ";")
    }

    private fun updateEnterKeyAction(info: EditorInfo?) {
        val iconView = enterIcon ?: (if (::keyboardRoot.isInitialized) keyboardRoot.findViewById<ImageView>(R.id.key_enter_icon) else null)
        val numIconView = if (::numericRoot.isInitialized) numericRoot.findViewById<ImageView>(R.id.num_enter_icon) else null
        val iconRes = if (info == null) {
            R.drawable.ic_enter_line
        } else {
            val imeOptions = info.imeOptions
            val action = imeOptions and EditorInfo.IME_MASK_ACTION
            when (action) {
                EditorInfo.IME_ACTION_SEARCH -> R.drawable.ic_search_line
                EditorInfo.IME_ACTION_SEND -> R.drawable.ic_send_line
                EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_NEXT -> R.drawable.ic_next_line
                EditorInfo.IME_ACTION_DONE -> R.drawable.ic_done_line
                else -> R.drawable.ic_enter_line
            }
        }
        iconView?.setImageResource(iconRes)
        numIconView?.setImageResource(iconRes)
    }

    private fun getClipboardText(): String? {
        return try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                val desc = clipboard.primaryClipDescription
                val clipTimestamp = desc?.timestamp ?: 0L
                val now = System.currentTimeMillis()
                // Ignora textos copiados há mais de 90 segundos (evita sugestão defasada)
                if (clipTimestamp > 0L && (now - clipTimestamp) > 90_000L) {
                    return null
                }
                val clip = clipboard.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    val text = clip.getItemAt(0)?.coerceToText(this)?.toString()?.trim()
                    if (!text.isNullOrBlank() && text.length <= 500 && text != lastConsumedClip) {
                        text
                    } else null
                } else null
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun showClipboardPill(clipText: String) {
        if (clipText == lastConsumedClip) {
            hideClipboardPill()
            return
        }
        val pill = clipboardPill ?: return
        val info = currentInputEditorInfo
        val display = if (isPrivateOrPassword(info)) {
            "📋 Colar"
        } else {
            val singleLine = clipText.replace("\n", " ").replace("\r", "")
            val trimmed = if (singleLine.length > 22) singleLine.take(20) + "…" else singleLine
            "📋 $trimmed"
        }
        pill.text = display
        pill.alpha = 1f
        pill.visibility = View.VISIBLE
        suggestionContainer?.visibility = View.INVISIBLE
        dragPill?.alpha = 0f

        // Auto-dismiss em 6 segundos se o usuário não tocar no chip
        clipboardDismissRunnable?.let { mainHandler.removeCallbacks(it) }
        clipboardDismissRunnable = Runnable {
            hideClipboardPill()
            lastConsumedClip = clipText
            lastClipboardText = null
        }
        mainHandler.postDelayed(clipboardDismissRunnable!!, 6000L)
    }

    private fun hideClipboardPill() {
        clipboardDismissRunnable?.let {
            mainHandler.removeCallbacks(it)
            clipboardDismissRunnable = null
        }
        if (clipboardPill?.visibility != View.GONE) {
            clipboardPill?.visibility = View.GONE
        }
        if (suggestionContainer?.visibility != View.VISIBLE) {
            suggestionContainer?.visibility = View.VISIBLE
        }
        val hasSuggestions = (suggestion1?.text?.isNotEmpty() == true) ||
                (suggestion2?.text?.isNotEmpty() == true) ||
                (suggestion3?.text?.isNotEmpty() == true)
        dragPill?.alpha = if (hasSuggestions) 0f else 0.5f
    }

    private fun scheduleAsyncPrediction(word: String) {
        if (!::keyboardRoot.isInitialized) return
        val editorInfo = currentInputEditorInfo
        if (editorInfo != null && isPrivateOrPassword(editorInfo)) {
            clearPredictionsUi()
            return
        }

        if (word.isEmpty()) {
            predictionJob?.cancel()
            lastQueriedWord = ""
            clearPredictionsUi()
            return
        }

        hideClipboardPill()

        if (word == lastQueriedWord && word.isNotEmpty()) {
            return
        }
        lastQueriedWord = word

        predictionJob?.cancel()
        predictionJob = scope.launch(Dispatchers.Default) {
            val predictions = predictionEngine.getPredictions(word)
            val (left, center, right) = when (predictions.size) {
                0 -> Triple("", "", "")
                1 -> Triple("", predictions[0], "")
                2 -> Triple(predictions[1], predictions[0], "")
                else -> Triple(predictions[1], predictions[0], predictions[2])
            }

            withContext(Dispatchers.Main) {
                updateSuggestionView(suggestion1, left)
                updateSuggestionView(suggestion2, center)
                updateSuggestionView(suggestion3, right)
                val hasSuggestions = center.isNotEmpty() || left.isNotEmpty() || right.isNotEmpty()
                dragPill?.alpha = if (hasSuggestions) 0f else 0.5f
            }
        }
    }

    private fun updateSuggestionView(tv: TextView?, text: String) {
        if (tv == null) return
        if (text.isEmpty()) {
            if (tv.visibility != View.INVISIBLE) tv.visibility = View.INVISIBLE
            if (tv.text.isNotEmpty()) tv.text = ""
        } else {
            if (tv.text != text) tv.text = text
            if (tv.visibility != View.VISIBLE) tv.visibility = View.VISIBLE
        }
    }

    private fun clearPredictionsUi() {
        predictionJob?.cancel()
        lastQueriedWord = ""
        updateSuggestionView(suggestion1, "")
        updateSuggestionView(suggestion2, "")
        updateSuggestionView(suggestion3, "")
        if (!lastClipboardText.isNullOrBlank() && lastClipboardText != lastConsumedClip && composingBuffer.isEmpty()) {
            showClipboardPill(lastClipboardText!!)
        } else {
            hideClipboardPill()
            dragPill?.alpha = 0.5f
        }
    }

    override fun onConfigureWindow(win: android.view.Window, isFullScreen: Boolean, isCandidatesOnly: Boolean) {
        super.onConfigureWindow(win, isFullScreen, isCandidatesOnly)
        applyGlassmorphismBlur(win)
    }

    private fun applyGlassmorphismBlur(win: android.view.Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            win.setBackgroundBlurRadius(60)
        }
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        return false
    }

    override fun onEvaluateInputViewShown(): Boolean {
        return super.onEvaluateInputViewShown()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        localEditCount = 0
        lastAutocorrection = null
        lastUndoneWord = null
        lastClipboardText = null
        composingBuffer.setLength(0)
        clearPredictionsUi()
        if (::numericRoot.isInitialized) {
            numericRoot.visibility = View.GONE
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        keyPositionCache.clear()
        if (::keyboardRoot.isInitialized) {
            keyboardRoot.post { prewarmKeyPositions() }
        }
    }
    
    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        
        localEditCount = 0
        lastAutocorrection = null
        lastUndoneWord = null
        ghostTextManager.onStartInput(info)
        lastQueriedWord = ""
        composingBuffer.setLength(0)

        if (keyPositionCache.isEmpty() && ::keyboardRoot.isInitialized) {
            keyboardRoot.post { prewarmKeyPositions() }
        }
        
        getWindow()?.window?.let { win ->
            applyGlassmorphismBlur(win)
        }
        
        val newTheme = getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE).getString("KEYBOARD_THEME", "Dark") ?: "Dark"
        if (newTheme != currentTheme) {
            currentTheme = newTheme
            setInputView(onCreateInputView())
        }

        val autoCap = shouldAutoCapitalize(info)
        isShifted = autoCap
        justCommittedSpace = false
        lastSpaceTime = 0L
        updateKeyLabels()
        updateEnterKeyAction(info)
        lastClipboardText = getClipboardText()
        if (!lastClipboardText.isNullOrBlank()) {
            showClipboardPill(lastClipboardText!!)
        } else {
            hideClipboardPill()
        }
        scheduleAsyncPrediction(composingBuffer.toString())

        val scale = cachedKeyboardScale
        if (::keyboardRoot.isInitialized) {
            val displayMetrics = resources.displayMetrics
            val density = displayMetrics.density
            val screenWidth = displayMetrics.widthPixels
            val keyWidth = screenWidth / 10f

            val rowsToScale = listOf(
                R.id.key_q, R.id.key_a, R.id.key_z, R.id.key_space
            )
            for (id in rowsToScale) {
                val key = keyboardRoot.findViewById<android.view.View>(id)
                val row = key?.parent as? android.view.View
                if (row != null) {
                    val lp = row.layoutParams
                    val baseHeightDp = if (id == R.id.key_space) 44f else (keyWidth / density)
                    lp.height = (baseHeightDp * density * scale).toInt()
                    row.layoutParams = lp
                }
            }
            keyboardRoot.requestLayout()
        }

        if (::numericRoot.isInitialized && ::keyboardRoot.isInitialized) {
            if (isNumericField(info)) {
                numericRoot.visibility = View.VISIBLE
                keyboardRoot.visibility = View.GONE
                voiceRoot.visibility = View.GONE
                emojiRoot.visibility = View.GONE
                updateEnterKeyAction(info)
                return
            } else {
                numericRoot.visibility = View.GONE
                keyboardRoot.visibility = View.VISIBLE
                voiceRoot.visibility = View.GONE
                emojiRoot.visibility = View.GONE
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupNumericKeyboard(view: View) {
        val numKeys = mapOf(
            R.id.num_key_1 to "1", R.id.num_key_2 to "2", R.id.num_key_3 to "3",
            R.id.num_key_4 to "4", R.id.num_key_5 to "5", R.id.num_key_6 to "6",
            R.id.num_key_7 to "7", R.id.num_key_8 to "8", R.id.num_key_9 to "9",
            R.id.num_key_0 to "0", R.id.num_key_plus to "+", R.id.num_key_comma to ",",
            R.id.num_key_dot to ".",
            R.id.num_sym_minus to "-", R.id.num_sym_slash to "/", R.id.num_sym_colon to ":",
            R.id.num_sym_paren_open to "(", R.id.num_sym_paren_close to ")",
            R.id.num_sym_dollar to "$", R.id.num_sym_percent to "%"
        )

        for ((id, char) in numKeys) {
            val key = view.findViewById<TextView>(id) ?: continue
            key.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        triggerVibration()
                        playClickFeedback()
                        v.isPressed = true
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        v.isPressed = false
                        localEditCount++
                        currentInputConnection?.commitText(char, 1)
                        true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        v.isPressed = false
                        true
                    }
                    else -> false
                }
            }
        }

        val backspace = view.findViewById<View>(R.id.num_key_backspace)
        val numBackspaceHandler = android.os.Handler(android.os.Looper.getMainLooper())
        var numBackspaceRunnable: Runnable? = null
        backspace?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    triggerVibration()
                    playClickFeedback()
                    handleBackspace()
                    v.isPressed = true
                    numBackspaceRunnable = object : Runnable {
                        override fun run() {
                            handleBackspace()
                            triggerVibration()
                            numBackspaceHandler.postDelayed(this, 50)
                        }
                    }
                    numBackspaceHandler.postDelayed(numBackspaceRunnable!!, 350)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    numBackspaceRunnable?.let { numBackspaceHandler.removeCallbacks(it) }
                    v.isPressed = false
                    true
                }
                else -> false
            }
        }

        val enter = view.findViewById<View>(R.id.num_key_enter)
        enter?.setOnClickListener {
            handleEnter()
            triggerVibration()
        }

        val abcBtn = view.findViewById<View>(R.id.num_key_abc)
        abcBtn?.setOnClickListener {
            triggerVibration()
            playClickFeedback()
            numericRoot.visibility = View.GONE
            keyboardRoot.visibility = View.VISIBLE
        }
    }



    @SuppressLint("ClickableViewAccessibility")
    private fun setupEmojiGrid(view: View) {
        val emojiRootLayout = view.findViewById<android.view.ViewGroup>(R.id.emoji_ui_root) ?: return
        
        fun findAndBindEmojis(parent: android.view.ViewGroup) {
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                if (child is TextView && child.tag == "emoji") {
                    child.setOnTouchListener { v, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                val emojiText = child.text.toString()
                                currentInputConnection?.commitText(emojiText, 1)
                                triggerVibration()
                                playClickFeedback()
                                v.isPressed = true
                                true
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                v.isPressed = false
                                true
                            }
                            else -> false
                        }
                    }
                } else if (child is android.view.ViewGroup) {
                    findAndBindEmojis(child)
                }
            }
        }
        findAndBindEmojis(emojiRootLayout)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDragResizer(view: View) {
        val dragHandle = view.findViewById<View>(R.id.drag_handle_container)
        var initialY = 0f
        var initialScale = cachedKeyboardScale
        var currentScale = cachedKeyboardScale
        
        dragHandle?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = event.rawY
                    initialScale = cachedKeyboardScale
                    keyboardRoot.alpha = 0.7f
                    v.isPressed = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.rawY - initialY
                    val scaleDelta = -(deltaY / 1000f)
                    var newScale = initialScale + scaleDelta
                    newScale = newScale.coerceIn(0.6f, 1.4f)
                    currentScale = newScale
                    
                    val rowsToScale = listOf(
                        R.id.key_q, R.id.key_a, R.id.key_z, R.id.key_space
                    )
                    val displayMetrics = resources.displayMetrics
                    val density = displayMetrics.density
                    val screenWidth = displayMetrics.widthPixels
                    val keyWidth = screenWidth / 10f

                    for (id in rowsToScale) {
                        val key = keyboardRoot.findViewById<android.view.View>(id)
                        val row = key?.parent as? android.view.View
                        if (row != null) {
                            val lp = row.layoutParams
                            val baseHeightDp = if (id == R.id.key_space) 44f else (keyWidth / density)
                            lp.height = (baseHeightDp * density * newScale).toInt()
                            row.layoutParams = lp
                        }
                    }
                    keyboardRoot.requestLayout()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    keyboardRoot.alpha = 1.0f
                    v.isPressed = false
                    keyPositionCache.clear()
                    cachedKeyboardScale = currentScale
                    getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE).edit().putFloat("KEYBOARD_SCALE", currentScale).apply()
                    true
                }
                else -> false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupKeys(view: View) {
        alphabetKeys.clear()
        keyViewMap.clear()

        for ((id, _) in idMapLetters) {
            val keyView = view.findViewById<android.widget.TextView>(id) ?: continue
            keyViewMap[id] = keyView
            
            var isLongPress = false
            var longPressRunnable: Runnable? = null

            keyView.setOnTouchListener { v, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        isLongPress = false
                        val currentChar = getCharForId(id) ?: return@setOnTouchListener false
                        val uppercaseChar = if (isShifted && !isSymbolMode) currentChar.uppercase() else currentChar
                        
                        triggerVibration()
                        playClickFeedback()
                        showKeyPopup(keyView, uppercaseChar)
                        v.isPressed = true

                        longPressRunnable = Runnable {
                            isLongPress = true
                            hideKeyPopup()
                            if (!isSymbolMode) {
                                showAccentsPopup(keyView, currentChar)
                                triggerVibration()
                            }
                        }
                        mainHandler.postDelayed(longPressRunnable!!, 350)
                        true
                    }
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        longPressRunnable?.let { mainHandler.removeCallbacks(it) }
                        hideKeyPopup()
                        v.isPressed = false
                        
                        if (event.action == android.view.MotionEvent.ACTION_UP && !isLongPress) {
                            val currentChar = getCharForId(id)
                            if (currentChar != null) {
                                handleCharacterClick(currentChar)
                            }
                        }
                        true
                    }
                    else -> false
                }
            }
        }

        updateKeyLabels()
    }
    
    private fun setupEmojiKeyboard(view: View) {
        val emojiCategories = mapOf(
            "Smileys & Emotion" to listOf(
                "😀","😃","😄","😁","😆","😅","😂","🤣","🥲","🥹","☺️","😊","😇","🙂","🙃","😉","😌","😍","🥰","😘","😗","😙","😚","😋","😛","😝","😜","🤪","🤨","🧐","🤓","😎","🥸","🤩","🥳","😏","😒","😞","😔","😟","😕","🙁","☹️","😣","😖","😫","😩","🥺","😢","😭","😮‍💨","😤","😠","😡","🤬","🤯","😳","🥵","🥶","😱","😨","😰","😥","😓","🫣","🤗","🫡","🤔","🫢","🤭","🤫","🤥","😶","😶‍🌫️","😐","😑","😬","🫨","🫠","🙄","😯","😦","😧","😮","😲","🥱","😴","🤤","😪","😵","😵‍💫","🫥","🤐","🥴","🤢","🤮","🤧","😷","🤒","🤕","🤑","🤠","😈","👿","👹","👺","🤡","💩","👻","💀","☠️","👽","👾","🤖","🎃","😺","😸","😹","😻","😼","😽","🙀","😿","😾",
            ),
            "People & Body" to listOf(
                "👋","🤚","🖐️","✋","🖖","🫱","🫲","🫳","🫴","🫷","🫸","👌","🤌","🤏","✌️","🤞","🫰","🤟","🤘","🤙","👈","👉","👆","🖕","👇","☝️","👍","👎","✊","👊","🤛","🤜","👏","🙌","🫶","👐","🤲","🤝","🙏","✍️","💅","🤳","💪","🦾","🦿","🦵","🦶","👂","🦻","👃","🧠","🫀","🫁","🦷","🦴","👀","👁️","👅","👄","🫦","👶","👧","🧒","👦","👩","🧑","👨"
            ),
            "Animals & Nature" to listOf(
                "🐵","🐒","🦍","🦧","🐶","🐕","🦮","🐕‍🦺","🐩","🐺","🦊","🦝","🐱","🐈","🐈‍⬛","🦁","🐯","🐅","🐆","🐴","🫎","🫏","🐎","🦄","🦓","🦌","🦬","🐮","🐂","🐃","🐄","🐷","🐖","🐗","🐽","🐏","🐑","🐐","🐪","🐫","🦙","🦒","🐘","🦣","🦏","🦛","🐭","🐁","🐀","🐹","🐰","🐇","🐿️","🦫","🦔","🦇","🐻","🐻‍❄️","🐨","🐼","🦥","🦦","🦨","🦘","🦡","🐾"
            ),
            "Food & Drink" to listOf(
                "🍇","🍈","🍉","🍊","🍋","🍌","🍍","🥭","🍎","🍏","🍐","🍑","🍒","🍓","🫐","🥝","🍅","🫒","🥥","🥑","🍆","🥔","🥕","🌽","🌶️","🫑","🥒","🥬","🥦","🧄","🧅","🍄","🥜","🫘","🌰","🍞","🥐","🥖","🫓","🥨","🥯","🥞","🧇","🧀","🍖","🍗","🥩","🥓","🍔","🍟","🍕","🌭","🥪","🌮","🌯","🫔","🥙","🧆","🥚","🍳","🥘","🍲","🫕","🥣","🥗","🍿","🧈","🧂","🥫"
            ),
            "Objects & Symbols" to listOf(
                "❤️","🧡","💛","💚","💙","🩵","💜","🤎","🖤","🩶","🤍","🩷","💘","💝","💖","💗","💓","💞","💕","💌","💟","💔","❤️‍🔥","❤️‍🩹","💋","💯","💢","💥","💫","💦","💨","🕳️","💣","💬","👁️‍🗨️","🗨️","🗯️","💭","💤","🌍","🌎","🌏","🌐","🗺️","🗾","🧭","🏔️","⛰️","🌋","🗻","🏕️","🏖️","🏜️","🏝️","🏞️","🏟️","🏛️","🏗️","🧱","🪨","🪵"
            ),
            "Flags" to listOf(
                "🏁","🚩","🎌","🏴","🏳️","🏳️‍🌈","🏳️‍⚧️","🏴‍☠️","🇧🇷","🇵🇹","🇺🇸","🇪🇸","🇫🇷","🇩🇪","🇮🇹","🇬🇧","🇯🇵","🇨🇳","🇦🇷","🇨🇦","🇲🇽"
            )
        )

        val emojiListsContainer = view.findViewById<android.widget.LinearLayout>(R.id.emoji_lists_container)
        emojiListsContainer?.removeAllViews()
        val density = resources.displayMetrics.density
        val size = (42 * density).toInt()
        
        val categoryViews = mutableMapOf<String, android.view.View>()

        for ((category, categoryEmojis) in emojiCategories) {
            val titleView = android.widget.TextView(this)
            titleView.text = category
            titleView.textSize = 14f
            titleView.setTextColor(android.graphics.Color.GRAY)
            titleView.setPadding(16, 24, 16, 8)
            emojiListsContainer?.addView(titleView)
            categoryViews[category] = titleView

            val grid = android.widget.GridLayout(this)
            grid.columnCount = 8
            grid.useDefaultMargins = true
            emojiListsContainer?.addView(grid)

            for (emoji in categoryEmojis) {
                val tv = android.widget.TextView(this)
                tv.text = emoji
                tv.textSize = 28f
                tv.gravity = android.view.Gravity.CENTER

                val params = android.widget.GridLayout.LayoutParams()
                params.width = size
                params.height = size
                params.setMargins(4, 4, 4, 4)
                tv.layoutParams = params

                val typedValue = android.util.TypedValue()
                theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, typedValue, true)
                tv.setBackgroundResource(typedValue.resourceId)
                tv.isClickable = true
                tv.isFocusable = true
                
                tv.setOnTouchListener { v, event ->
                    when (event.action) {
                        android.view.MotionEvent.ACTION_DOWN -> {
                            playClickFeedback()
                            triggerVibration()
                            localEditCount++
                            composingBuffer.setLength(0)
                            val ic = currentInputConnection
                            ic?.commitText(emoji, 1)
                            v.isPressed = true
                            true
                        }
                        android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                            v.isPressed = false
                            true
                        }
                        else -> false
                    }
                }
                grid.addView(tv)
            }
        }
        
        view.findViewById<View>(R.id.btn_close_emoji)?.setOnClickListener {
            playClickFeedback()
            emojiRoot.visibility = View.GONE
            keyboardRoot.visibility = View.VISIBLE
        }

        val scrollView = view.findViewById<android.widget.ScrollView>(R.id.emoji_scroll_view)
        fun scrollToCategory(catName: String) {
            val targetView = categoryViews.entries.find { it.key.contains(catName, ignoreCase = true) }?.value
            if (targetView != null && scrollView != null) {
                scrollView.post {
                    scrollView.smoothScrollTo(0, targetView.top)
                }
            }
        }

        view.findViewById<View>(R.id.cat_smileys)?.setOnClickListener { scrollToCategory("Smileys") }
        view.findViewById<View>(R.id.cat_people)?.setOnClickListener { scrollToCategory("People") }
        view.findViewById<View>(R.id.cat_animals)?.setOnClickListener { scrollToCategory("Animals") }
        view.findViewById<View>(R.id.cat_food)?.setOnClickListener { scrollToCategory("Food") }
        view.findViewById<View>(R.id.cat_objects)?.setOnClickListener { scrollToCategory("Objects") }
        view.findViewById<View>(R.id.cat_symbols)?.setOnClickListener { scrollToCategory("Flags") }

        view.findViewById<View>(R.id.btn_emoji_backspace)?.setOnClickListener {
            handleBackspace()
        }
    }

    private fun setupCommandKeys(view: View) {
        val shiftKey = view.findViewById<FrameLayout>(R.id.key_shift_top)
        shiftText = view.findViewById(R.id.text_shift_top)
        shiftKey?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    toggleShift()
                    triggerVibration()
                    v.isPressed = true
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.isPressed = false
                    true
                }
                else -> false
            }
        }

        val backspaceKey = view.findViewById<FrameLayout>(R.id.key_backspace)
        var backspaceStartX = 0f
        var isBackspaceSwiping = false
        var lastDeleteX = 0f
        val backspaceHandler = android.os.Handler(android.os.Looper.getMainLooper())
        var backspaceRunnable: Runnable? = null

        backspaceKey?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    backspaceStartX = event.rawX
                    isBackspaceSwiping = false
                    lastDeleteX = backspaceStartX
                    
                    handleBackspace()
                    triggerVibration()
                    v.isPressed = true
                    
                    backspaceRunnable = object : Runnable {
                        override fun run() {
                            handleBackspace()
                            triggerVibration()
                            backspaceHandler.postDelayed(this, 50)
                        }
                    }
                    backspaceHandler.postDelayed(backspaceRunnable!!, 350)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - backspaceStartX
                    
                    if (!isBackspaceSwiping && deltaX < -35f) {
                        isBackspaceSwiping = true
                        backspaceRunnable?.let { backspaceHandler.removeCallbacks(it) }
                    }
                    
                    if (isBackspaceSwiping) {
                        val moveDelta = event.rawX - lastDeleteX
                        val threshold = -45f
                        
                        if (moveDelta < threshold) {
                            deleteWordBeforeCursor()
                            triggerVibration()
                            lastDeleteX = event.rawX
                        } else if (moveDelta > -threshold && event.rawX < backspaceStartX) {
                            lastDeleteX = event.rawX
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    backspaceRunnable?.let { backspaceHandler.removeCallbacks(it) }
                    v.isPressed = false
                    true
                }
                else -> false
            }
        }

        val spaceKey = view.findViewById<TextView>(R.id.key_space)
        spaceKeyView = spaceKey
        var spaceStartX = 0f
        var spaceStartY = 0f
        var isSpaceSwiping = false
        var lastCursorMoveX = 0f

        spaceKey?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    spaceStartX = event.rawX
                    spaceStartY = event.rawY
                    isSpaceSwiping = false
                    lastCursorMoveX = spaceStartX
                    
                    triggerVibration()
                    v.isPressed = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - spaceStartX
                    
                    if (!isSpaceSwiping && (kotlin.math.abs(deltaX) > 30f)) {
                        isSpaceSwiping = true
                    }
                    
                    if (isSpaceSwiping) {
                        val moveDelta = event.rawX - lastCursorMoveX
                        val threshold = 40f
                        
                        if (moveDelta > threshold) {
                            currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_RIGHT))
                            currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DPAD_RIGHT))
                            lastCursorMoveX = event.rawX
                            triggerVibration()
                        } else if (moveDelta < -threshold) {
                            currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_LEFT))
                            currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DPAD_LEFT))
                            lastCursorMoveX = event.rawX
                            triggerVibration()
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isSpaceSwiping) {
                        val ic = currentInputConnection
                        val editorInfo = currentInputEditorInfo
                        val lastWord = composingBuffer.toString()
                        val centerCandidate = suggestion2?.text?.toString()?.trim() ?: ""

                        val now = android.os.SystemClock.uptimeMillis()
                        val doubleSpacePref = getSharedPreferences("StitchPrefs", Context.MODE_PRIVATE)
                            .getBoolean("PREF_DOUBLE_SPACE_PERIOD", true)
                        if (doubleSpacePref && now - lastSpaceTime < 350L && lastWord.isEmpty() && justCommittedSpace) {
                            ic?.beginBatchEdit()
                            localEditCount++
                            ic?.deleteSurroundingText(1, 0)
                            ic?.commitText(". ", 1)
                            ic?.endBatchEdit()
                            setShiftState(true)
                            lastSpaceTime = 0L
                            justCommittedSpace = true
                            lastAutocorrection = null
                            playClickFeedback()
                            triggerVibration()
                            v.isPressed = false
                            return@setOnTouchListener true
                        }
                        lastSpaceTime = now
                        justCommittedSpace = true

                        val doAutocorrect = shouldAutocorrect(editorInfo) &&
                                lastWord.length in 2..30 &&
                                centerCandidate.isNotEmpty() &&
                                !centerCandidate.equals(lastWord, ignoreCase = false) &&
                                lastWord != lastUndoneWord

                        if (doAutocorrect) {
                            ic?.beginBatchEdit()
                            localEditCount++
                            ic?.deleteSurroundingText(lastWord.length, 0)
                            ic?.commitText(centerCandidate + " ", 1)
                            ic?.endBatchEdit()
                            lastAutocorrection = LastAutocorrection(lastWord, centerCandidate)

                            if (editorInfo == null || !isPrivateOrPassword(editorInfo)) {
                                predictionEngine.learnWord(centerCandidate)
                            }
                        } else {
                            if (editorInfo == null || !isPrivateOrPassword(editorInfo)) {
                                if (lastWord.isNotBlank() && lastWord.length in 2..30) {
                                    predictionEngine.learnWord(lastWord)
                                }
                            }
                            localEditCount++
                            ic?.commitText(" ", 1)
                            lastAutocorrection = null
                        }

                        composingBuffer.setLength(0)
                        playClickFeedback()
                        triggerVibration()
                        scheduleAsyncPrediction("")
                    }
                    v.isPressed = false
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    v.isPressed = false
                    true
                }
                else -> false
            }
        }

        val enterKey = view.findViewById<FrameLayout>(R.id.key_enter)
        enterIcon = view.findViewById<ImageView>(R.id.key_enter_icon)
        enterKey?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    handleEnter()
                    triggerVibration()
                    v.isPressed = true
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.isPressed = false
                    true
                }
                else -> false
            }
        }

        val emojiKey = view.findViewById<FrameLayout>(R.id.key_emoji_top)
        emojiKey?.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                playClickFeedback()
                triggerVibration()
                emojiRoot.layoutParams.height = keyboardRoot.height
                keyboardRoot.visibility = android.view.View.GONE
                emojiRoot.visibility = android.view.View.VISIBLE
                v.isPressed = true
                true
            } else if (event.action == android.view.MotionEvent.ACTION_UP || event.action == android.view.MotionEvent.ACTION_CANCEL) {
                v.isPressed = false
                true
            } else {
                false
            }
        }

        val symbolKey = view.findViewById<FrameLayout>(R.id.key_symbol)
        symbolKey?.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                playClickFeedback()
                triggerVibration()
                isSymbolMode = !isSymbolMode
                if (!isSymbolMode) {
                    isExtendedSymbolMode = false
                }
                updateKeyLabels()
                v.isPressed = true
                true
            } else if (event.action == android.view.MotionEvent.ACTION_UP || event.action == android.view.MotionEvent.ACTION_CANCEL) {
                v.isPressed = false
                true
            } else {
                false
            }
        }

        val micKey = view.findViewById<FrameLayout>(R.id.key_mic_top)
        micKey?.setOnClickListener {
            playClickFeedback()
            triggerVibration()
            
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                Toast.makeText(this, "Conceda a permissão no app", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            
            keyboardRoot.visibility = View.GONE
            voiceRoot.visibility = View.VISIBLE
            voiceText?.text = "Fale agora..."
            startListening()
            startWaveAnimation(view.rootView)
        }
    }

    private fun setupSuggestionBar(view: View) {
        suggestion1 = view.findViewById<TextView>(R.id.suggestion_1)
        suggestion2 = view.findViewById<TextView>(R.id.suggestion_2)
        suggestion3 = view.findViewById<TextView>(R.id.suggestion_3)
        suggestionContainer = view.findViewById<LinearLayout>(R.id.suggestion_container)
        clipboardPill = view.findViewById<TextView>(R.id.clipboard_pill)
        val plusBtn = view.findViewById<View>(R.id.key_settings_top)

        clipboardPill?.setOnClickListener {
            val clip = lastClipboardText
            if (!clip.isNullOrBlank()) {
                val ic = currentInputConnection ?: return@setOnClickListener
                ic.beginBatchEdit()
                localEditCount++
                ic.commitText(clip, 1)
                ic.endBatchEdit()
                triggerVibration()
                playClickFeedback()
                lastConsumedClip = clip
                lastClipboardText = null
                clipboardDismissRunnable?.let {
                    mainHandler.removeCallbacks(it)
                    clipboardDismissRunnable = null
                }
                clipboardPill?.animate()?.alpha(0f)?.setDuration(150)?.withEndAction {
                    hideClipboardPill()
                }?.start()
            }
        }

        val suggestionClickListener = View.OnClickListener { v ->
            if (v is TextView) {
                val selectedSuggestion = v.text.toString()
                if (selectedSuggestion.isEmpty()) return@OnClickListener
                
                val ic = currentInputConnection ?: return@OnClickListener
                val prefixLen = composingBuffer.length
                
                ic.beginBatchEdit()
                if (prefixLen > 0) {
                    ic.deleteSurroundingText(prefixLen, 0)
                } else {
                    val textBefore = ic.getTextBeforeCursor(30, 0)?.toString() ?: ""
                    var count = 0
                    for (i in textBefore.length - 1 downTo 0) {
                        if (isWordChar(textBefore[i])) count++ else break
                    }
                    if (count > 0) {
                        ic.deleteSurroundingText(count, 0)
                    }
                }
                localEditCount++
                ic.commitText(selectedSuggestion + " ", 1)
                ic.endBatchEdit()
                
                lastAutocorrection = null
                lastUndoneWord = null
                lastClipboardText = null
                justCommittedSpace = true
                composingBuffer.setLength(0)
                val editorInfo = currentInputEditorInfo
                if (editorInfo == null || !isPrivateOrPassword(editorInfo)) {
                    predictionEngine.learnWord(selectedSuggestion)
                }
                playClickFeedback()
                triggerVibration()
                scheduleAsyncPrediction("")
            }
        }

        suggestion1?.setOnClickListener(suggestionClickListener)
        suggestion2?.setOnClickListener(suggestionClickListener)
        suggestion3?.setOnClickListener(suggestionClickListener)

        plusBtn?.setOnClickListener {
            playClickFeedback()
            triggerVibration()
            val intent = android.content.Intent(this, MainActivity::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun setShiftState(shifted: Boolean, immediate: Boolean = false) {
        if (isShifted != shifted) {
            isShifted = shifted
            if (::shiftText.isInitialized) {
                shiftText.text = if (isShifted) "AA" else "Aa"
            }
            if (immediate) {
                updateKeyLabels()
            } else {
                mainHandler.post {
                    if (::keyboardRoot.isInitialized) {
                        updateKeyLabels()
                    }
                }
            }
        }
    }

    private fun handleCharacterClick(baseChar: String) {
        val ic = currentInputConnection ?: return
        lastAutocorrection = null
        lastUndoneWord = null
        lastClipboardText = null
        hideClipboardPill()

        // Smart punctuation check: attach punctuation to preceding word and follow with space
        if (isPunctuation(baseChar) && justCommittedSpace) {
            ic.beginBatchEdit()
            localEditCount++
            ic.deleteSurroundingText(1, 0)
            ic.commitText(baseChar + " ", 1)
            ic.endBatchEdit()
            composingBuffer.setLength(0)
            justCommittedSpace = true

            if (baseChar == "." || baseChar == "!" || baseChar == "?") {
                setShiftState(true)
            }
            playClickFeedback()
            scheduleAsyncPrediction("")
            return
        }

        justCommittedSpace = false
        val charToCommit = if (isShifted) baseChar.uppercase() else baseChar.lowercase()
        
        localEditCount++
        // 1. Ação imediata na UI
        ic.commitText(charToCommit, 1)

        if (isShifted) {
            setShiftState(false, immediate = false)
        }

        // 2. Previsão desacoplada e cancelável via buffer local
        var isAllWord = true
        for (i in 0 until charToCommit.length) {
            if (!isWordChar(charToCommit[i])) {
                isAllWord = false
                break
            }
        }
        if (isAllWord) {
            composingBuffer.append(charToCommit)
        } else {
            composingBuffer.setLength(0)
            if (baseChar == "." || baseChar == "!" || baseChar == "?") {
                setShiftState(true)
            }
        }
        scheduleAsyncPrediction(composingBuffer.toString())
    }

    private fun toggleShift() {
        if (isSymbolMode) {
            isExtendedSymbolMode = !isExtendedSymbolMode
            updateKeyLabels()
            playClickFeedback()
            triggerVibration()
        } else {
            setShiftState(!isShifted, immediate = true)
            playClickFeedback()
        }
    }

    private fun updateKeyLabels() {
        val currentMap = if (isSymbolMode) {
            if (isExtendedSymbolMode) idMapExtendedSymbols else idMapSymbols
        } else {
            idMapLetters
        }
        alphabetKeys.clear()
        
        for ((id, defaultChar) in currentMap) {
            val keyView = keyViewMap[id] ?: continue
            val char = getCharForId(id) ?: defaultChar
            val displayChar = if (isShifted && !isSymbolMode) char.uppercase() else char
            if (keyView.text != displayChar) {
                keyView.text = displayChar
            }
            alphabetKeys[id] = char
        }
        
        val symbolKeyText = keyboardRoot.findViewById<TextView>(R.id.text_key_symbol)
        val symbolLabel = if (isSymbolMode) "ABC" else "123"
        if (symbolKeyText?.text != symbolLabel) {
            symbolKeyText?.text = symbolLabel
        }

        if (::shiftText.isInitialized) {
            if (isSymbolMode) {
                val label = if (isExtendedSymbolMode) "?123" else "=\\<"
                if (shiftText.text != label) shiftText.text = label
                shiftText.setTextColor(android.graphics.Color.WHITE)
            } else {
                val label = if (isShifted) "AA" else "Aa"
                if (shiftText.text != label) shiftText.text = label
                if (isShifted) {
                    val typedValue = android.util.TypedValue()
                    theme.resolveAttribute(R.attr.stitchGlowColor, typedValue, true)
                    shiftText.setTextColor(typedValue.data)
                } else {
                    shiftText.setTextColor(android.graphics.Color.WHITE)
                }
            }
        }
    }

    private fun handleBackspace() {
        val ic = currentInputConnection ?: return
        ghostTextManager.clearGhostText(ic)
        justCommittedSpace = false
        lastSpaceTime = 0L

        val autoUndo = lastAutocorrection
        if (autoUndo != null) {
            lastAutocorrection = null
            lastUndoneWord = autoUndo.originalWord

            ic.beginBatchEdit()
            localEditCount++
            val deleteLen = autoUndo.correctedWord.length + 1
            ic.deleteSurroundingText(deleteLen, 0)
            ic.commitText(autoUndo.originalWord, 1)
            ic.endBatchEdit()

            composingBuffer.setLength(0)
            composingBuffer.append(autoUndo.originalWord)
            playClickFeedback()
            scheduleAsyncPrediction(composingBuffer.toString())
            return
        }

        lastAutocorrection = null
        localEditCount++
        val selectedText = ic.getSelectedText(0)
        if (selectedText.isNullOrEmpty()) {
            ic.deleteSurroundingText(1, 0)
            if (composingBuffer.isNotEmpty()) {
                composingBuffer.deleteCharAt(composingBuffer.length - 1)
            } else {
                syncComposingBufferFromIme()
            }
        } else {
            ic.commitText("", 1)
            composingBuffer.setLength(0)
        }
        playClickFeedback()
        scheduleAsyncPrediction(composingBuffer.toString())
    }

    private fun deleteWordBeforeCursor() {
        val ic = currentInputConnection ?: return
        localEditCount++
        if (composingBuffer.isNotEmpty()) {
            val len = composingBuffer.length
            composingBuffer.setLength(0)
            ic.deleteSurroundingText(len, 0)
            scheduleAsyncPrediction("")
            return
        }
        val text = ic.getTextBeforeCursor(50, 0)?.toString() ?: ""
        if (text.isEmpty()) {
            ic.deleteSurroundingText(1, 0)
            return
        }
        var i = text.length - 1
        while (i >= 0 && text[i].isWhitespace()) {
            i--
        }
        while (i >= 0 && !text[i].isWhitespace()) {
            i--
        }
        val deleteCount = text.length - 1 - i
        if (deleteCount > 0) {
            ic.deleteSurroundingText(deleteCount, 0)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
        scheduleAsyncPrediction("")
    }

    private fun handleEnter() {
        lastAutocorrection = null
        lastUndoneWord = null
        composingBuffer.setLength(0)
        clearPredictionsUi()
        val ic = currentInputConnection ?: return
        localEditCount++
        val editorInfo = currentInputEditorInfo
        if (editorInfo != null) {
            val actionId = editorInfo.actionId
            val imeOptions = editorInfo.imeOptions
            val actionMasked = imeOptions and EditorInfo.IME_MASK_ACTION

            if (actionId != 0) {
                ic.performEditorAction(actionId)
            } else if (actionMasked != EditorInfo.IME_ACTION_NONE) {
                ic.performEditorAction(actionMasked)
            } else {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
        } else {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
        playClickFeedback()
    }

    private fun playClickFeedback() {
        val soundPref = getSharedPreferences("StitchPrefs", Context.MODE_PRIVATE)
            .getBoolean("PREF_SOUND_FEEDBACK", false)
        if (!soundPref) return
        mainHandler.post {
            try {
                audioManager?.playSoundEffect(android.media.AudioManager.FX_KEYPRESS_STANDARD)
            } catch (_: Exception) {}
        }
    }

    private fun showKeyPopup(keyView: View, char: String) {
        val popupPref = getSharedPreferences("StitchPrefs", Context.MODE_PRIVATE)
            .getBoolean("PREF_KEY_POPUP", true)
        if (!popupPref) return
        if (!::previewPopup.isInitialized || !::previewPopupText.isInitialized) return
        previewPopupText.text = char

        val (x, y) = keyPositionCache.getOrPut(keyView.id) {
            keyView.getLocationInWindow(keyLocation)
            keyboardRoot.getLocationInWindow(rootLocation)
            Pair((keyLocation[0] - rootLocation[0]).toFloat(), (keyLocation[1] - rootLocation[1]).toFloat())
        }

        val density = resources.displayMetrics.density
        val popupWidth = if (previewPopup.width > 0) previewPopup.width else (54 * density).toInt()
        val popupHeight = if (previewPopup.height > 0) previewPopup.height else (60 * density).toInt()

        previewPopup.translationX = x + (keyView.width - popupWidth) / 2f
        previewPopup.translationY = y - popupHeight - (6 * density)

        previewPopup.animate().cancel()
        previewPopup.alpha = 1f
        previewPopup.visibility = View.VISIBLE
    }

    private fun hideKeyPopup() {
        if (::previewPopup.isInitialized) {
            previewPopup.animate().cancel()
            previewPopup.visibility = View.GONE
        }
    }

    private fun triggerVibration() {
        val hapticPref = getSharedPreferences("StitchPrefs", Context.MODE_PRIVATE)
            .getBoolean("PREF_HAPTIC_FEEDBACK", true)
        if (!hapticPref) return
        try {
            if (::keyboardRoot.isInitialized) {
                keyboardRoot.performHapticFeedback(
                    android.view.HapticFeedbackConstants.KEYBOARD_TAP,
                    android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    private fun startWaveAnimation(view: View) {
        waveJob?.cancel()
        waveJob = scope.launch(Dispatchers.Main) {
            val bars = listOf(
                view.findViewById<View>(R.id.wave_bar_1),
                view.findViewById<View>(R.id.wave_bar_2),
                view.findViewById<View>(R.id.wave_bar_3),
                view.findViewById<View>(R.id.wave_bar_4),
                view.findViewById<View>(R.id.wave_bar_5)
            )
            var time = 0f
            while (voiceRoot.visibility == View.VISIBLE) {
                time += 0.2f
                bars.forEachIndexed { index, bar ->
                    if (bar != null) {
                        val scale = 1f + 0.8f * kotlin.math.sin((time + index).toDouble()).toFloat()
                        bar.scaleY = scale.coerceIn(0.4f, 1.8f)
                    }
                }
                kotlinx.coroutines.delay(32)
            }
            for (bar in bars) {
                bar?.animate()?.scaleY(1.0f)?.setDuration(150)?.start()
            }
        }
    }

    private fun predictWithGemini(view: View) {
        val ic = currentInputConnection ?: return
        val textBefore = ic.getTextBeforeCursor(200, 0)?.toString() ?: ""
        if (textBefore.isBlank()) {
            Toast.makeText(this, "Digite algo primeiro...", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Gerando previsões...", Toast.LENGTH_SHORT).show()

        scope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank()) {
                    Toast.makeText(this@StitchKeyboardService, "Chave API não configurada", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val prompt = "Baseado no texto: '$textBefore'. Sugira 3 proximas palavras ou frases curtas de autocompletar e 1 emoji. Formato exato: 'sugestao1|sugestao2|sugestao3|emoji'."
                val request = GenerateContentRequest(
                    contents = listOf(Content(listOf(Part(prompt))))
                )

                val response = RetrofitClient.service.generateContent("gemini-3.1-flash-lite", apiKey, request)
                val result = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                
                val parts = result.trim().split("|")
                if (parts.size >= 3) {
                    view.findViewById<TextView>(R.id.suggestion_1)?.text = parts[0].trim()
                    view.findViewById<TextView>(R.id.suggestion_2)?.text = parts[1].trim()
                    view.findViewById<TextView>(R.id.suggestion_3)?.text = parts[2].trim()
                }
            } catch (e: Exception) {
                Toast.makeText(this@StitchKeyboardService, "Erro AI: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun rewriteText(tone: String) {
        val ic = currentInputConnection ?: return
        
        val extractedText = ic.getTextBeforeCursor(500, 0)?.toString() ?: ""
        if (extractedText.isBlank()) {
            Toast.makeText(this, "Nada para reescrever", Toast.LENGTH_SHORT).show()
            keyboardRoot.visibility = View.VISIBLE
            return
        }

        Toast.makeText(this, "Reescrevendo...", Toast.LENGTH_SHORT).show()

        scope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank()) {
                    return@launch
                }

                val prompt = "Reescreva o seguinte texto em um tom $tone. Retorne apenas o texto reescrito: '$extractedText'"
                val request = GenerateContentRequest(
                    contents = listOf(Content(listOf(Part(prompt))))
                )

                val response = RetrofitClient.service.generateContent("gemini-3.1-pro-preview", apiKey, request)
                val result = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                
                if (result.isNotBlank()) {
                    ic.deleteSurroundingText(extractedText.length, 0)
                    ic.commitText(result.trim(), 1)
                }
            } catch (e: Exception) {
                Toast.makeText(this@StitchKeyboardService, "Erro AI", Toast.LENGTH_SHORT).show()
            } finally {
                keyboardRoot.visibility = View.VISIBLE
            }
        }
    }

    private fun startListening() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: android.os.Bundle?) {
                    voiceText?.text = "Ouvindo..."
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {
                    val scale = 1.0f + (rmsdB / 10f).coerceIn(0f, 1f)
                    voiceRoot.findViewById<android.view.View>(R.id.voice_pulse_bg)?.animate()?.scaleX(scale)?.scaleY(scale)?.setDuration(50)?.start()
                }
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    voiceText?.text = "Processando..."
                }
                override fun onError(error: Int) {
                    val errorMsg = when(error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "Não entendi"
                        SpeechRecognizer.ERROR_NETWORK -> "Erro de rede"
                        SpeechRecognizer.ERROR_AUDIO -> "Erro de áudio"
                        else -> "Erro: $error"
                    }
                    voiceText?.text = errorMsg
                    isListening = false
                    
                    scope.launch(Dispatchers.Main) {
                        kotlinx.coroutines.delay(1500)
                        if (!isListening && voiceRoot.visibility == android.view.View.VISIBLE) {
                            stopListening()
                            voiceRoot.visibility = android.view.View.GONE
                            keyboardRoot.visibility = android.view.View.VISIBLE
                        }
                    }
                }
                override fun onResults(results: android.os.Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        localEditCount++
                        composingBuffer.setLength(0)
                        currentInputConnection?.commitText(text + " ", 1)
                        voiceText?.text = text
                    }
                    isListening = false
                    
                    scope.launch(Dispatchers.Main) {
                        kotlinx.coroutines.delay(1000)
                        if (!isListening && voiceRoot.visibility == android.view.View.VISIBLE) {
                            stopListening()
                            voiceRoot.visibility = android.view.View.GONE
                            keyboardRoot.visibility = android.view.View.VISIBLE
                        }
                    }
                }
                override fun onPartialResults(partialResults: android.os.Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        voiceText?.text = matches[0]
                    }
                }
                override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        
        try {
            speechRecognizer?.startListening(intent)
            isListening = true
            voiceText?.text = "Preparando..."
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "Reconhecimento de voz não disponível", android.widget.Toast.LENGTH_SHORT).show()
            voiceRoot.visibility = android.view.View.GONE
            keyboardRoot.visibility = android.view.View.VISIBLE
        }
    }

    private fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
    }
    
    override fun onDestroy() {
        super.onDestroy()
        clipboardDismissRunnable?.let { mainHandler.removeCallbacks(it) }
        predictionJob?.cancel()
        waveJob?.cancel()
        mainHandler.removeCallbacksAndMessages(null)
        scope.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun showDomainPopup(keyView: View) {
        val domains = listOf("/", ".com", ".br", ".org", ".net", ".io")
        val context = keyView.context
        val container = android.widget.LinearLayout(context)
        container.orientation = android.widget.LinearLayout.HORIZONTAL
        container.background = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.bg_preview_popup)
        container.setPadding(8, 8, 8, 8)

        val popupWindow = android.widget.PopupWindow(container, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, false)
        popupWindow.isTouchable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        for (domain in domains) {
            val tv = android.widget.TextView(context)
            tv.text = domain
            tv.textSize = 15f
            tv.setTextColor(android.graphics.Color.WHITE)
            tv.setPadding(16, 12, 16, 12)
            tv.setOnClickListener {
                handleCharacterClick(domain)
                popupWindow.dismiss()
            }
            container.addView(tv)
        }

        container.measure(android.view.View.MeasureSpec.UNSPECIFIED, android.view.View.MeasureSpec.UNSPECIFIED)
        val location = IntArray(2)
        keyView.getLocationInWindow(location)
        val density = resources.displayMetrics.density
        popupWindow.showAtLocation(keyView, android.view.Gravity.NO_GRAVITY, location[0] + (keyView.width / 2) - (container.measuredWidth / 2), location[1] - container.measuredHeight - (10 * density).toInt())
    }

    private fun showAccentsPopup(keyView: View, char: String) {
        if (char == ".") {
            showDomainPopup(keyView)
            return
        }
        val accentsMap = mapOf(
            "a" to listOf("a", "á", "à", "ã", "â", "ä"),
            "e" to listOf("e", "é", "è", "ê", "ë"),
            "i" to listOf("i", "í", "ì", "î", "ï"),
            "o" to listOf("o", "ó", "ò", "õ", "ô", "ö"),
            "u" to listOf("u", "ú", "ù", "û", "ü"),
            "c" to listOf("c", "ç"),
            "n" to listOf("n", "ñ")
        )
        val accents = accentsMap[char.lowercase()] ?: return
        
        val context = keyView.context
        val container = android.widget.LinearLayout(context)
        container.orientation = android.widget.LinearLayout.HORIZONTAL
        container.background = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.bg_preview_popup)
        container.setPadding(8, 8, 8, 8)

        val popupWindow = android.widget.PopupWindow(container, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, false)
        popupWindow.isTouchable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        
        for (accent in accents) {
            val tv = android.widget.TextView(context)
            tv.text = if (isShifted && !isSymbolMode) accent.uppercase() else accent
            tv.textSize = 24f
            tv.setTextColor(android.graphics.Color.WHITE)
            tv.setPadding(24, 12, 24, 12)
            tv.setOnClickListener {
                handleCharacterClick(tv.text.toString())
                popupWindow.dismiss()
            }
            container.addView(tv)
        }
        
        container.measure(android.view.View.MeasureSpec.UNSPECIFIED, android.view.View.MeasureSpec.UNSPECIFIED)
        val location = IntArray(2)
        keyView.getLocationInWindow(location)
        val density = resources.displayMetrics.density
        popupWindow.showAtLocation(keyView, android.view.Gravity.NO_GRAVITY, location[0] + (keyView.width / 2) - (container.measuredWidth / 2), location[1] - container.measuredHeight - (10 * density).toInt())
    }
}