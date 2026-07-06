package com.example

import android.annotation.SuppressLint
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import android.widget.FrameLayout
import android.widget.ImageView
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

class StitchKeyboardService : InputMethodService() {

    private var isShifted = false
    private lateinit var shiftIcon: ImageView
    private val alphabetKeys = mutableMapOf<Int, String>()
    private val keyViews = mutableListOf<TextView>()
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var keyboardRoot: View
    private lateinit var voiceRoot: View
    private lateinit var aiRoot: View
    private lateinit var settingsRoot: View
    private lateinit var emojiRoot: View
    private val predictionEngine = PredictionEngine()
    private val wordSeparatorRegex = Regex("[^a-zA-ZáéíóúãõâêîôûçÁÉÍÓÚÃÕÂÊÎÔÛÇ]+")
    private var suggestion1: android.widget.TextView? = null
    private var suggestion2: android.widget.TextView? = null
    private var suggestion3: android.widget.TextView? = null
    
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
        vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        audioManager = getSystemService(android.content.Context.AUDIO_SERVICE) as? android.media.AudioManager
        inputMethodManager = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
    }

override fun onCreateInputView(): View {
        try {
            val themePref = getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE).getString("KEYBOARD_THEME", "Dark") ?: "Dark"
            currentTheme = themePref
            val themeResId = if (themePref == "Light") R.style.Theme_Tessera_Light else R.style.Theme_Tessera_Dark
            val themedContext = ContextThemeWrapper(this, themeResId)
            val keyboardView = layoutInflater.cloneInContext(themedContext)
                .inflate(R.layout.stitch_keyboard_layout, null)

            keyboardRoot = keyboardView.findViewById(R.id.keyboard_root)
            voiceRoot = keyboardView.findViewById(R.id.voice_ui_root)
            aiRoot = keyboardView.findViewById(R.id.ai_ui_root)
            settingsRoot = keyboardView.findViewById(R.id.settings_ui_root)
            emojiRoot = keyboardView.findViewById(R.id.emoji_ui_root)

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
            keyboardView.findViewById<View>(R.id.btn_close_ai)?.setOnClickListener {
                aiRoot.visibility = View.GONE
                keyboardRoot.visibility = View.VISIBLE
            }
            keyboardView.findViewById<View>(R.id.btn_close_emoji)?.setOnClickListener {
                emojiRoot.visibility = View.GONE
                keyboardRoot.visibility = View.VISIBLE
            }

            keyboardView.findViewById<View>(R.id.btn_ai_prof)?.setOnClickListener { rewriteText("profissional") }
            keyboardView.findViewById<View>(R.id.btn_ai_casual)?.setOnClickListener { rewriteText("casual") }

            setupKeys(keyboardView)
                    setupEmojiKeyboard(keyboardView)
        setupCommandKeys(keyboardView)
            setupSuggestionBar(keyboardView)
            setupDragResizer(keyboardView)
            setupEmojiGrid(keyboardView)

            return keyboardView
        } catch (e: Exception) {
            val errorView = TextView(this)
            errorView.text = "Error loading keyboard:\n${e.message}\n${e.stackTraceToString()}"
            errorView.setTextColor(android.graphics.Color.WHITE)
            errorView.setBackgroundColor(android.graphics.Color.RED)
            return errorView
        }
    }

    private var currentTheme = "Dark"


    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int,
        newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        updatePredictions()
    }

    private fun updatePredictions() {
        val ic = currentInputConnection
        if (ic == null || !::keyboardRoot.isInitialized) return
        
        val textBeforeCursor = ic.getTextBeforeCursor(30, 0)?.toString() ?: ""
        val words = textBeforeCursor.split(wordSeparatorRegex)
        val lastWord = words.lastOrNull() ?: ""
        
        val predictions = predictionEngine.getPredictions(lastWord)
        
        val s1 = predictions.getOrNull(0) ?: ""
        val s2 = predictions.getOrNull(1) ?: ""
        val s3 = predictions.getOrNull(2) ?: ""
        
        suggestion1?.text = s1
        suggestion1?.visibility = if (s1.isEmpty()) android.view.View.INVISIBLE else android.view.View.VISIBLE
        
        suggestion2?.text = s2
        suggestion2?.visibility = if (s2.isEmpty()) android.view.View.INVISIBLE else android.view.View.VISIBLE
        
        suggestion3?.text = s3
        suggestion3?.visibility = if (s3.isEmpty()) android.view.View.INVISIBLE else android.view.View.VISIBLE
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
    
    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        
        getWindow()?.window?.let { win ->
            applyGlassmorphismBlur(win)
        }
        
        val newTheme = getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE).getString("KEYBOARD_THEME", "Dark") ?: "Dark"
        if (newTheme != currentTheme) {
            currentTheme = newTheme
            setInputView(onCreateInputView())
        }

        isShifted = false
        updateShiftUI()
        updatePredictions()

        val scale = getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE).getFloat("KEYBOARD_SCALE", 1.0f)
        if (::keyboardRoot.isInitialized) {
            val rootLayout = (keyboardRoot.parent as? android.widget.FrameLayout)
            
            // Adjust row heights instead of visual scaling
            val rowsToScale = listOf(
                R.id.key_q, R.id.key_a, R.id.key_z, R.id.key_space
            )
            for (id in rowsToScale) {
                val key = keyboardRoot.findViewById<android.view.View>(id)
                val row = key?.parent as? android.view.View
                if (row != null) {
                    val lp = row.layoutParams
                    // Base heights: Q/A/Z = 52dp, Space = 44dp
                    val baseHeightDp = if (id == R.id.key_space) 44f else 52f
                    val density = resources.displayMetrics.density
                    lp.height = (baseHeightDp * density * scale).toInt()
                    row.layoutParams = lp
                }
            }
        }

        if (::keyboardRoot.isInitialized) {
            keyboardRoot.visibility = View.VISIBLE
            voiceRoot.visibility = View.GONE
            aiRoot.visibility = View.GONE
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
        var initialScale = 1.0f
        var currentScale = 1.0f
        
        dragHandle?.setOnTouchListener { v, event ->
            val rootLayout = (keyboardRoot.parent as? android.widget.FrameLayout) ?: return@setOnTouchListener false
            
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = event.rawY
                    initialScale = getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE).getFloat("KEYBOARD_SCALE", 1.0f)
                    keyboardRoot.alpha = 0.6f
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
                    for (id in rowsToScale) {
                        val key = keyboardRoot.findViewById<android.view.View>(id)
                        val row = key?.parent as? android.view.View
                        if (row != null) {
                            val lp = row.layoutParams
                            val baseHeightDp = if (id == R.id.key_space) 44f else 52f
                            val density = resources.displayMetrics.density
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
                    
                    
                    getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE).edit().putFloat("KEYBOARD_SCALE", currentScale).apply()
                    true
                }
                else -> false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
        private fun setupKeys(view: View) {
        val idMap = mapOf(
            R.id.key_q to "q", R.id.key_w to "w", R.id.key_e to "e", R.id.key_r to "r",
            R.id.key_t to "t", R.id.key_y to "y", R.id.key_u to "u", R.id.key_i to "i",
            R.id.key_o to "o", R.id.key_p to "p",
            R.id.key_a to "a", R.id.key_s to "s", R.id.key_d to "d", R.id.key_f to "f",
            R.id.key_g to "g", R.id.key_h to "h", R.id.key_j to "j", R.id.key_k to "k",
            R.id.key_l to "l", R.id.key_cedilla to "ç",
            R.id.key_z to "z", R.id.key_x to "x", R.id.key_c to "c", R.id.key_v to "v",
            R.id.key_b to "b", R.id.key_n to "n", R.id.key_m to "m"
        )
        alphabetKeys.clear()
        keyViews.clear()

        for ((id, char) in idMap) {
            val keyView = view.findViewById<android.widget.TextView>(id) ?: continue
            alphabetKeys[id] = char
            keyViews.add(keyView)

            keyView.setOnTouchListener { v, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        val uppercaseChar = if (isShifted) char.uppercase() else char.lowercase()
                        handleCharacterClick(char)
                        triggerVibration()
                        showKeyPopup(keyView, uppercaseChar)
                        v.isPressed = true
                        true
                    }
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        hideKeyPopup()
                        v.isPressed = false
                        true
                    }
                    else -> false
                }
            }
        }

        val swipeOverlay = view.findViewById<com.example.ui.SwipeGestureOverlay>(R.id.swipe_overlay)
        if (swipeOverlay != null) {
            val keyList = mutableListOf<Pair<android.widget.TextView, String>>()
            for ((id, char) in idMap) {
                val keyView = view.findViewById<android.widget.TextView>(id) ?: continue
                keyList.add(Pair(keyView, char))
            }
            swipeOverlay.setKeys(keyList)
            
            val typedValue = android.util.TypedValue()
            theme.resolveAttribute(R.attr.stitchGlowColor, typedValue, true)
            swipeOverlay.setThemeColor(typedValue.data)


            swipeOverlay.onKeyDown = { view, char ->
                val uppercaseChar = if (isShifted) char.uppercase() else char.lowercase()
                showKeyPopup(view, uppercaseChar)
                view.isPressed = true
                triggerVibration()
            }
            
            swipeOverlay.onKeyUp = {
                hideKeyPopup()
                for (k in keyViews) { k.isPressed = false }
            }
            
            swipeOverlay.onSwipeComplete = { wordPattern ->

                val prediction = predictionEngine.getSwipePrediction(wordPattern)
                if (prediction != null) {
                    val ic = currentInputConnection
                    ic?.commitText(prediction + " ", 1)
                    playClickFeedback()
                } else {
                    android.widget.Toast.makeText(this, "Palavra não encontrada", android.widget.Toast.LENGTH_SHORT).show()
                }
                updatePredictions()
            }
            
            swipeOverlay.onSwipeChar = { char ->
                handleCharacterClick(char)
            }
            
            swipeOverlay.onSwipeStart = {
                triggerVibration()
            }
        }
    }
    
    private fun setupEmojiKeyboard(view: View) {
        val emojiGridLayout = view.findViewById<android.widget.GridLayout>(R.id.emoji_grid_layout) ?: return
        val emojis = listOf(
            "😀", "😂", "🤣", "😊", "🥰", "😍", "😘", "😜",
            "😎", "🤩", "🥳", "😏", "😒", "😞", "😔", "😢",
            "😭", "😤", "😡", "🤬", "🤯", "😳", "🥵", "🥶",
            "😱", "😨", "😰", "😥", "😓", "🤗", "🤔", "🤭",
            "🤫", "🤥", "😶", "😐", "😑", "😬", "🙄", "😯",
            "😦", "😧", "😮", "😲", "🥱", "😴", "🤤", "😪",
            "😵", "🤐", "🥴", "🤢", "🤮", "🤧", "😷", "🤒",
            "🤕", "🤑", "🤠", "😈", "👿", "👹", "👺", "🤡",
            "💩", "👻", "💀", "☠️", "👽", "👾", "🤖", "🎃",
            "😺", "😸", "😹", "😻", "😼", "😽", "🙀", "😿",
            "😾", "🙈", "🙉", "🙊", "💋", "💌", "💘", "💝",
            "💖", "💗", "💓", "💞", "💕", "💟", "❣️", "💔",
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🤎", "🖤",
            "🤍", "💯", "💢", "💥", "💫", "💦", "💨", "🕳️"
        )
        
        emojiGridLayout.removeAllViews()
        val density = resources.displayMetrics.density
        val padding = (8 * density).toInt()
        val size = (42 * density).toInt()
        
        for (emoji in emojis) {
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
            
            tv.setOnClickListener {
                playClickFeedback()
                triggerVibration()
                val ic = currentInputConnection
                ic?.commitText(emoji, 1)
            }
            emojiGridLayout.addView(tv)
        }
        
        view.findViewById<View>(R.id.btn_close_emoji)?.setOnClickListener {
            playClickFeedback()
            emojiRoot.visibility = View.GONE
            keyboardRoot.visibility = View.VISIBLE
        }
    }
private fun setupCommandKeys(view: View) {
        val shiftKey = view.findViewById<FrameLayout>(R.id.key_shift)
        shiftIcon = view.findViewById(R.id.img_shift_icon)
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
        var backspaceHandler = android.os.Handler(android.os.Looper.getMainLooper())
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
                    backspaceHandler.postDelayed(backspaceRunnable!!, 400)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - backspaceStartX
                    
                    if (!isBackspaceSwiping && deltaX < -30f) {
                        isBackspaceSwiping = true
                        backspaceRunnable?.let { backspaceHandler.removeCallbacks(it) }
                    }
                    
                    if (isBackspaceSwiping) {
                        val moveDelta = event.rawX - lastDeleteX
                        val threshold = -40f
                        
                        if (moveDelta < threshold) {
                            val ic = currentInputConnection
                            ic?.deleteSurroundingText(1, 0)
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

        val spaceKey = view.findViewById<com.example.ui.ShimmerTextView>(R.id.key_space)
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
                        val threshold = 40f // pixels per cursor move
                        
                        if (moveDelta > threshold) {
                            // Move cursor right
                            currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_RIGHT))
                            currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DPAD_RIGHT))
                            lastCursorMoveX = event.rawX
                            triggerVibration()
                        } else if (moveDelta < -threshold) {
                            // Move cursor left
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
                        currentInputConnection?.commitText(" ", 1)
                        playClickFeedback()
                        triggerVibration()
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

        val emojiKey = view.findViewById<FrameLayout>(R.id.key_emoji)
        emojiKey?.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                playClickFeedback()
                triggerVibration()
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
                android.widget.Toast.makeText(this, "Teclado Numérico/Símbolos em breve", android.widget.Toast.LENGTH_SHORT).show()
                v.isPressed = true
                true
            } else if (event.action == android.view.MotionEvent.ACTION_UP || event.action == android.view.MotionEvent.ACTION_CANCEL) {
                v.isPressed = false
                true
            } else {
                false
            }
        }

        val micKey = view.findViewById<FrameLayout>(R.id.key_mic)
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
        val suggestion1 = this.suggestion1
        val suggestion2 = this.suggestion2
        val suggestion3 = this.suggestion3
        val plusBtn = view.findViewById<View>(R.id.key_plus)
        val sparkleBtn = view.findViewById<View>(R.id.key_sparkle)
        val iaBtn = view.findViewById<TextView>(R.id.key_ia)

        val suggestionClickListener = View.OnClickListener { v ->
            if (v is TextView) {
                val ic = currentInputConnection ?: return@OnClickListener
                val textBeforeCursor = ic.getTextBeforeCursor(30, 0)?.toString() ?: ""
                val words = textBeforeCursor.split(wordSeparatorRegex)
                val lastWord = words.lastOrNull() ?: ""
                
                if (lastWord.isNotEmpty()) {
                    ic.deleteSurroundingText(lastWord.length, 0)
                }
                ic.commitText(v.text.toString() + " ", 1)
                playClickFeedback()
                triggerVibration()
            }
        }

        suggestion1?.setOnClickListener(suggestionClickListener)
        suggestion2?.setOnClickListener(suggestionClickListener)
        suggestion3?.setOnClickListener(suggestionClickListener)

        plusBtn?.setOnClickListener {
            playClickFeedback()
            triggerVibration()
            keyboardRoot.visibility = View.GONE
            settingsRoot.visibility = View.VISIBLE
        }
        
        view.findViewById<View>(R.id.btn_close_settings)?.setOnClickListener {
            playClickFeedback()
            triggerVibration()
            settingsRoot.visibility = View.GONE
            keyboardRoot.visibility = View.VISIBLE
        }
        
        view.findViewById<View>(R.id.btn_settings_app)?.setOnClickListener {
            val intent = android.content.Intent(this, MainActivity::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            settingsRoot.visibility = View.GONE
            keyboardRoot.visibility = View.VISIBLE
        }
        
        view.findViewById<View>(R.id.btn_settings_theme)?.setOnClickListener {
            val prefs = getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE)
            val current = prefs.getString("KEYBOARD_THEME", "Dark")
            val newTheme = if (current == "Dark") "Light" else "Dark"
            prefs.edit().putString("KEYBOARD_THEME", newTheme).apply()
            setInputView(onCreateInputView())
        }
        
        view.findViewById<View>(R.id.btn_settings_size)?.setOnClickListener {
            val prefs = getSharedPreferences("StitchPrefs", android.content.Context.MODE_PRIVATE)
            val current = prefs.getFloat("KEYBOARD_SCALE", 1.0f)
            val newScale = if (current == 1.0f) 0.85f else if (current == 0.85f) 1.15f else 1.0f
            prefs.edit().putFloat("KEYBOARD_SCALE", newScale).apply()
            setInputView(onCreateInputView())
        }
        
        view.findViewById<View>(R.id.btn_settings_emoji)?.setOnClickListener {
            Toast.makeText(this, "Tons de emoji em breve", Toast.LENGTH_SHORT).show()
        }


        sparkleBtn?.setOnClickListener {
            playClickFeedback()
            triggerVibration()
            predictWithGemini(view)
        }

        iaBtn?.setOnClickListener {
            playClickFeedback()
            triggerVibration()
            keyboardRoot.visibility = View.GONE
            aiRoot.visibility = View.VISIBLE
        }
    }

    private fun handleCharacterClick(baseChar: String) {
        val ic = currentInputConnection ?: return
        val charToCommit = if (isShifted) baseChar.uppercase() else baseChar.lowercase()
        ic.commitText(charToCommit, 1)

        if (isShifted) {
            isShifted = false
            updateShiftUI()
        }
        playClickFeedback()
        updatePredictions()
    }

    private fun toggleShift() {
        isShifted = !isShifted
        updateShiftUI()
        playClickFeedback()
    }

    private fun updateShiftUI() {
        for (textView in keyViews) {
            val baseChar = alphabetKeys[textView.id] ?: continue
            textView.text = if (isShifted) baseChar.uppercase() else baseChar.lowercase()
        }
        if (::shiftIcon.isInitialized) {
            if (isShifted) {
                val typedValue = android.util.TypedValue()
                theme.resolveAttribute(R.attr.stitchGlowColor, typedValue, true)
                shiftIcon.setColorFilter(typedValue.data)
            } else {
                shiftIcon.clearColorFilter()
            }
        }
    }
    private fun handleBackspace() {
        val ic = currentInputConnection ?: return
        val selectedText = ic.getSelectedText(0)
        if (selectedText.isNullOrEmpty()) {
            ic.deleteSurroundingText(1, 0)
        } else {
            ic.commitText("", 1)
        }
        playClickFeedback()
    }

    private fun handleEnter() {
        val ic = currentInputConnection ?: return
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
        audioManager?.playSoundEffect(android.media.AudioManager.FX_KEYPRESS_STANDARD)
    }

    private fun showKeyPopup(keyView: View, char: String) {
        if (!::previewPopup.isInitialized || !::previewPopupText.isInitialized) return
        previewPopupText.text = char

        val location = IntArray(2)
        keyView.getLocationInWindow(location)

        val rootLocation = IntArray(2)
        keyboardRoot.getLocationInWindow(rootLocation)

        val x = location[0] - rootLocation[0]
        val y = location[1] - rootLocation[1]

        val density = resources.displayMetrics.density
        val popupWidth = if (previewPopup.width > 0) previewPopup.width else (54 * density).toInt()
        val popupHeight = if (previewPopup.height > 0) previewPopup.height else (60 * density).toInt()

        // Center popup horizontally relative to the key
        previewPopup.translationX = x.toFloat() + (keyView.width - popupWidth) / 2f
        // Float popup above the key
        previewPopup.translationY = y.toFloat() - popupHeight - (10 * density)

        previewPopup.visibility = View.VISIBLE
    }

    private fun hideKeyPopup() {
        if (::previewPopup.isInitialized) {
            previewPopup.visibility = View.GONE
        }
    }

    private fun triggerVibration() {
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
            while (voiceRoot.visibility == View.VISIBLE) {
                for (bar in bars) {
                    if (bar == null) continue
                    val randomScale = (Math.random() * 1.6 + 0.4).toFloat()
                    bar.animate()
                        .scaleY(randomScale)
                        .setDuration(100)
                        .start()
                }
                kotlinx.coroutines.delay(100)
            }
            // Reset scale when stopped
            for (bar in bars) {
                bar?.animate()?.scaleY(1.0f)?.setDuration(100)?.start()
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
                if (parts.size >= 4) {
                    view.findViewById<TextView>(R.id.suggestion_1)?.text = parts[0].trim()
                    view.findViewById<TextView>(R.id.suggestion_2)?.text = parts[1].trim()
                    view.findViewById<TextView>(R.id.suggestion_3)?.text = parts[2].trim()
                    
                    val textEmoji = view.findViewById<TextView>(R.id.text_emoji)
                    val imgEmoji = view.findViewById<ImageView>(R.id.img_emoji)
                    
                    if (textEmoji != null && imgEmoji != null) {
                        textEmoji.text = parts[3].trim()
                        textEmoji.visibility = View.VISIBLE
                        imgEmoji.visibility = View.GONE
                    }
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
            aiRoot.visibility = View.GONE
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
                aiRoot.visibility = View.GONE
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
                    
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
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
                        currentInputConnection?.commitText(text + " ", 1)
                        voiceText?.text = text
                    }
                    isListening = false
                    
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
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
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

        }