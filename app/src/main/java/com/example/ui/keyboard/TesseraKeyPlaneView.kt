package com.example.ui.keyboard

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.example.engine.SpatialHitboxResolver
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

class TesseraKeyPlaneView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface KeyPlaneListener {
        fun onChar(char: String)
        fun onBackspace()
        fun onSpace()
        fun onEnter()
        fun onShiftClick(isDoubleClick: Boolean)
        fun onModeToggle()
        fun onExtendedModeToggle()
        fun onFlickUp(char: String)
        fun onSpaceTrackpad(deltaSteps: Int, isSelecting: Boolean)
        fun onGlideStart(x: Float, y: Float)
        fun onGlideMove(x: Float, y: Float, time: Long)
        fun onGlideEnd()
        fun onKeyTouchDown()
        fun onAccentSelectedTick()
    }

    var listener: KeyPlaneListener? = null

    // Layout configuration
    var layoutMode = KeyboardLayoutMode.ALPHA
        set(value) {
            field = value
            rebuildLayout()
            invalidate()
        }

    var isShifted = false
        set(value) {
            field = value
            updateLabels()
            invalidate()
        }

    var isCapsLock = false
        set(value) {
            field = value
            invalidate()
        }

    var showNumberRow = false
        set(value) {
            if (field != value) {
                field = value
                rebuildLayout()
                requestLayout()
            }
        }

    var showKeyHints = true
        set(value) {
            field = value
            invalidate()
        }

    var keyShape = "squircle" // "squircle", "pill", "borderless"
        set(value) {
            field = value
            invalidate()
        }

    var keyboardScale = 1.0f
        set(value) {
            val safe = value.coerceIn(0.6f, 1.4f)
            if (field != safe) {
                field = safe
                rebuildLayout()
                requestLayout()
            }
        }

    var isGlideEnabled = true
    var isFlickAccentsEnabled = true
    var isAdaptiveHitboxesEnabled = true
    var validNextCharsMask = 0

    var enterActionIcon = "↵"
        set(value) {
            field = value
            enterKeyCell?.primaryLabel = value
            invalidate()
        }

    private var palette: KeyboardPalette = DynamicThemeResolver.resolve(context, "Dark")

    fun setPalette(newPalette: KeyboardPalette) {
        this.palette = newPalette
        invalidate()
    }

    fun getKeyBoundsMap(): Map<Char, android.graphics.Rect> {
        val map = mutableMapOf<Char, android.graphics.Rect>()
        for (k in allKeys) {
            val charStr = (k.action as? KeyAction.CharKey)?.char ?: continue
            if (charStr.isNotEmpty()) {
                val c = charStr[0].lowercaseChar()
                map[c] = android.graphics.Rect(
                    k.visualRect.left.toInt(),
                    k.visualRect.top.toInt(),
                    k.visualRect.right.toInt(),
                    k.visualRect.bottom.toInt()
                )
            }
        }
        return map
    }
    private val allKeys = mutableListOf<KeyCell>()
    private val rowKeys = mutableListOf<MutableList<KeyCell>>()
    private var enterKeyCell: KeyCell? = null
    private var spaceKeyCell: KeyCell? = null

    // Drawing paints (pre-allocated)
    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val actionKeyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val specularPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    private val actionTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }
    private val calloutBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val calloutHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val calloutTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }
    private val capsIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val tempRect = RectF()
    private val tempPath = Path()
    private val fontMetrics = Paint.FontMetrics()
    private val density = resources.displayMetrics.density
    private val mainHandler = Handler(Looper.getMainLooper())

    // Accent Callout State
    private var activeCalloutKey: KeyCell? = null
    private val calloutAccents = mutableListOf<String>()
    private var activeAccentIndex = -1
    private val calloutRect = RectF()
    private val calloutItemRects = mutableListOf<RectF>()

    // Pointer Session for Multi-Touch
    private class PointerSession(
        val pointerId: Int,
        var downX: Float,
        var downY: Float,
        var currentX: Float,
        var currentY: Float,
        var downTime: Long,
        var currentKey: KeyCell?,
        var isGlide: Boolean = false,
        var isSpaceSlide: Boolean = false,
        var lastSpaceX: Float = 0f,
        var isLongPressActive: Boolean = false,
        var longPressRunnable: Runnable? = null
    )

    private val activeSessions = SparseArray<PointerSession>()
    private var lastShiftPressTime = 0L

    init {
        setWillNotDraw(false)
        isClickable = true
        isFocusable = true
        setTheme("Dark")
    }

    fun setTheme(themeName: String) {
        palette = DynamicThemeResolver.resolve(context, themeName)
        updatePaintColors()
        invalidate()
    }

    private fun updatePaintColors() {
        keyBgPaint.color = palette.keyBgColor
        actionKeyBgPaint.color = palette.actionKeyBgColor
        specularPaint.color = palette.specularBorderColor
        shadowPaint.color = palette.shadowColor
        textPaint.color = palette.textColor
        actionTextPaint.color = palette.textColor
        hintPaint.color = palette.inactiveTextColor
        calloutBgPaint.color = palette.calloutBgColor
        calloutHighlightPaint.color = palette.calloutAccentColor
        calloutTextPaint.color = palette.calloutTextColor
        capsIndicatorPaint.color = palette.calloutAccentColor
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val rowCount = if (showNumberRow) 5 else 4
        val rowHeight = (49f * density * keyboardScale).toInt()
        val totalHeight = rowHeight * rowCount + (8 * density).toInt()
        setMeasuredDimension(width, resolveSize(totalHeight, heightMeasureSpec))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rebuildLayout()
    }

    private fun rebuildLayout() {
        if (width <= 0 || height <= 0) return
        allKeys.clear()
        rowKeys.clear()
        enterKeyCell = null
        spaceKeyCell = null

        val rowCount = if (showNumberRow) 5 else 4
        val availableHeight = height.toFloat()
        val rowHeight = availableHeight / rowCount
        val totalWidth = width.toFloat()
        val keyMarginX = 2.5f * density
        val keyMarginY = 3.0f * density

        var currentRowIdx = 0

        // 1. Optional Number Row
        if (showNumberRow) {
            val numRow = mutableListOf<KeyCell>()
            val numCols = 10
            val keyWidth = totalWidth / numCols
            val top = currentRowIdx * rowHeight
            val bottom = top + rowHeight

            val numKeys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
            for (i in 0 until numCols) {
                val left = i * keyWidth
                val right = left + keyWidth
                val logical = RectF(left, top, right, bottom)
                val visual = RectF(left + keyMarginX, top + keyMarginY, right - keyMarginX, bottom - keyMarginY)
                val cell = KeyCell(
                    action = KeyAction.CharKey(numKeys[i]),
                    primaryLabel = numKeys[i],
                    logicalRect = logical,
                    visualRect = visual,
                    isActionKey = false
                )
                numRow.add(cell)
                allKeys.add(cell)
            }
            rowKeys.add(numRow)
            currentRowIdx++
        }

        // 2. Row 1
        val r1 = mutableListOf<KeyCell>()
        val (row1Chars, row1Hints) = when (layoutMode) {
            KeyboardLayoutMode.ALPHA -> Pair(
                listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
            )
            KeyboardLayoutMode.SYMBOLS -> Pair(
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
                null
            )
            KeyboardLayoutMode.EXTENDED_SYMBOLS -> Pair(
                listOf("~", "\\", "|", "^", "=", "{", "}", "[", "]", "°"),
                null
            )
        }

        val r1Width = totalWidth / row1Chars.size
        var rTop = currentRowIdx * rowHeight
        var rBottom = rTop + rowHeight

        for (i in row1Chars.indices) {
            val left = i * r1Width
            val right = left + r1Width
            val logical = RectF(left, rTop, right, rBottom)
            val visual = RectF(left + keyMarginX, rTop + keyMarginY, right - keyMarginX, rBottom - keyMarginY)
            val char = row1Chars[i]
            val cell = KeyCell(
                action = KeyAction.CharKey(char),
                primaryLabel = if (isShifted && layoutMode == KeyboardLayoutMode.ALPHA) char.uppercase() else char,
                secondaryHint = if (layoutMode == KeyboardLayoutMode.ALPHA && !showNumberRow) row1Hints?.getOrNull(i) else null,
                accents = getAccentsForChar(char),
                logicalRect = logical,
                visualRect = visual,
                isActionKey = false
            )
            r1.add(cell)
            allKeys.add(cell)
        }
        rowKeys.add(r1)
        currentRowIdx++

        // 3. Row 2
        val r2 = mutableListOf<KeyCell>()
        val (row2Chars, row2Hints) = when (layoutMode) {
            KeyboardLayoutMode.ALPHA -> Pair(
                listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ç"),
                listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "=")
            )
            KeyboardLayoutMode.SYMBOLS -> Pair(
                listOf("@", "#", "$", "%", "&", "-", "+", "(", ")"),
                null
            )
            KeyboardLayoutMode.EXTENDED_SYMBOLS -> Pair(
                listOf("<", ">", "_", "€", "£", "¥", "•", "©", "®"),
                null
            )
        }

        rTop = currentRowIdx * rowHeight
        rBottom = rTop + rowHeight
        val r2Count = row2Chars.size
        val r2KeyWidth = if (r2Count == 10) totalWidth / 10f else totalWidth / 10f
        val r2StartOffset = if (r2Count == 9) (totalWidth - (r2KeyWidth * 9f)) / 2f else 0f

        for (i in row2Chars.indices) {
            val left = r2StartOffset + i * r2KeyWidth
            val right = left + r2KeyWidth
            // Expand first and last logical bounds to screen edges to eliminate lateral dead zones!
            val logicalLeft = if (i == 0) 0f else left
            val logicalRight = if (i == row2Chars.size - 1) totalWidth else right
            val logical = RectF(logicalLeft, rTop, logicalRight, rBottom)
            val visual = RectF(left + keyMarginX, rTop + keyMarginY, right - keyMarginX, rBottom - keyMarginY)
            val char = row2Chars[i]
            val cell = KeyCell(
                action = KeyAction.CharKey(char),
                primaryLabel = if (isShifted && layoutMode == KeyboardLayoutMode.ALPHA) char.uppercase() else char,
                secondaryHint = if (layoutMode == KeyboardLayoutMode.ALPHA) row2Hints?.getOrNull(i) else null,
                accents = getAccentsForChar(char),
                logicalRect = logical,
                visualRect = visual,
                isActionKey = false
            )
            r2.add(cell)
            allKeys.add(cell)
        }
        rowKeys.add(r2)
        currentRowIdx++

        // 4. Row 3: Shift, Chars, Backspace
        val r3 = mutableListOf<KeyCell>()
        val (row3Chars, row3Hints) = when (layoutMode) {
            KeyboardLayoutMode.ALPHA -> Pair(
                listOf("z", "x", "c", "v", "b", "n", "m"),
                listOf("!", "\"", "'", ":", ";", "/", "?")
            )
            KeyboardLayoutMode.SYMBOLS -> Pair(
                listOf("/", "*", "\"", "'", ":", ";", "!"),
                null
            )
            KeyboardLayoutMode.EXTENDED_SYMBOLS -> Pair(
                listOf("«", "»", "§", "`", "~", "™", "¿"),
                null
            )
        }

        rTop = currentRowIdx * rowHeight
        rBottom = rTop + rowHeight

        val sideWidth = totalWidth * 0.145f
        val midCols = row3Chars.size
        val midWidth = (totalWidth - sideWidth * 2f) / midCols

        // Shift / Mode toggle key on left
        val shiftLogical = RectF(0f, rTop, sideWidth, rBottom)
        val shiftVisual = RectF(keyMarginX, rTop + keyMarginY, sideWidth - keyMarginX, rBottom - keyMarginY)
        val shiftAction = when (layoutMode) {
            KeyboardLayoutMode.ALPHA -> KeyAction.Shift
            KeyboardLayoutMode.SYMBOLS -> KeyAction.ModeExtendedSymbol
            KeyboardLayoutMode.EXTENDED_SYMBOLS -> KeyAction.ModeSymbol
        }
        val shiftLabel = when (layoutMode) {
            KeyboardLayoutMode.ALPHA -> "⇧"
            KeyboardLayoutMode.SYMBOLS -> "=/<"
            KeyboardLayoutMode.EXTENDED_SYMBOLS -> "123"
        }
        val shiftCell = KeyCell(
            action = shiftAction,
            primaryLabel = shiftLabel,
            logicalRect = shiftLogical,
            visualRect = shiftVisual,
            isActionKey = true
        )
        r3.add(shiftCell)
        allKeys.add(shiftCell)

        // Middle characters
        for (i in row3Chars.indices) {
            val left = sideWidth + i * midWidth
            val right = left + midWidth
            val logical = RectF(left, rTop, right, rBottom)
            val visual = RectF(left + keyMarginX, rTop + keyMarginY, right - keyMarginX, rBottom - keyMarginY)
            val char = row3Chars[i]
            val cell = KeyCell(
                action = KeyAction.CharKey(char),
                primaryLabel = if (isShifted && layoutMode == KeyboardLayoutMode.ALPHA) char.uppercase() else char,
                secondaryHint = if (layoutMode == KeyboardLayoutMode.ALPHA) row3Hints?.getOrNull(i) else null,
                accents = getAccentsForChar(char),
                logicalRect = logical,
                visualRect = visual,
                isActionKey = false
            )
            r3.add(cell)
            allKeys.add(cell)
        }

        // Backspace key on right
        val bkLogical = RectF(totalWidth - sideWidth, rTop, totalWidth, rBottom)
        val bkVisual = RectF(totalWidth - sideWidth + keyMarginX, rTop + keyMarginY, totalWidth - keyMarginX, rBottom - keyMarginY)
        val bkCell = KeyCell(
            action = KeyAction.Backspace,
            primaryLabel = "⌫",
            logicalRect = bkLogical,
            visualRect = bkVisual,
            isActionKey = true
        )
        r3.add(bkCell)
        allKeys.add(bkCell)
        rowKeys.add(r3)
        currentRowIdx++

        // 5. Row 4: Mode(123/ABC), Comma, Space, Period, Enter
        val r4 = mutableListOf<KeyCell>()
        rTop = currentRowIdx * rowHeight
        rBottom = availableHeight

        val modeWidth = totalWidth * 0.145f
        val commaWidth = totalWidth * 0.10f
        val enterWidth = totalWidth * 0.155f
        val periodWidth = totalWidth * 0.10f
        val spaceWidth = totalWidth - (modeWidth + commaWidth + enterWidth + periodWidth)

        // Mode (123 / ABC)
        var xAcc = 0f
        val modeLogical = RectF(xAcc, rTop, xAcc + modeWidth, rBottom)
        val modeVisual = RectF(xAcc + keyMarginX, rTop + keyMarginY, xAcc + modeWidth - keyMarginX, rBottom - keyMarginY)
        val modeCell = KeyCell(
            action = KeyAction.ModeSymbol,
            primaryLabel = if (layoutMode == KeyboardLayoutMode.ALPHA) "123" else "ABC",
            logicalRect = modeLogical,
            visualRect = modeVisual,
            isActionKey = true
        )
        r4.add(modeCell)
        allKeys.add(modeCell)
        xAcc += modeWidth

        // Comma
        val commaLogical = RectF(xAcc, rTop, xAcc + commaWidth, rBottom)
        val commaVisual = RectF(xAcc + keyMarginX, rTop + keyMarginY, xAcc + commaWidth - keyMarginX, rBottom - keyMarginY)
        val commaChar = if (layoutMode == KeyboardLayoutMode.ALPHA) "," else "?"
        val commaCell = KeyCell(
            action = KeyAction.CharKey(commaChar),
            primaryLabel = commaChar,
            accents = if (commaChar == ",") listOf(";", ":") else emptyList(),
            logicalRect = commaLogical,
            visualRect = commaVisual,
            isActionKey = false
        )
        r4.add(commaCell)
        allKeys.add(commaCell)
        xAcc += commaWidth

        // Space
        val spaceLogical = RectF(xAcc, rTop, xAcc + spaceWidth, rBottom)
        val spaceVisual = RectF(xAcc + keyMarginX, rTop + keyMarginY, xAcc + spaceWidth - keyMarginX, rBottom - keyMarginY)
        val spaceCell = KeyCell(
            action = KeyAction.Space,
            primaryLabel = "Tessera",
            logicalRect = spaceLogical,
            visualRect = spaceVisual,
            isActionKey = false
        )
        r4.add(spaceCell)
        allKeys.add(spaceCell)
        spaceKeyCell = spaceCell
        xAcc += spaceWidth

        // Period
        val periodLogical = RectF(xAcc, rTop, xAcc + periodWidth, rBottom)
        val periodVisual = RectF(xAcc + keyMarginX, rTop + keyMarginY, xAcc + periodWidth - keyMarginX, rBottom - keyMarginY)
        val periodChar = "."
        val periodCell = KeyCell(
            action = KeyAction.CharKey(periodChar),
            primaryLabel = periodChar,
            accents = listOf(".com", ".br", ".org", ".net", ".io", "/"),
            logicalRect = periodLogical,
            visualRect = periodVisual,
            isActionKey = false
        )
        r4.add(periodCell)
        allKeys.add(periodCell)
        xAcc += periodWidth

        // Enter
        val enterLogical = RectF(xAcc, rTop, totalWidth, rBottom)
        val enterVisual = RectF(xAcc + keyMarginX, rTop + keyMarginY, totalWidth - keyMarginX, rBottom - keyMarginY)
        val enterCell = KeyCell(
            action = KeyAction.Enter,
            primaryLabel = enterActionIcon,
            logicalRect = enterLogical,
            visualRect = enterVisual,
            isActionKey = true
        )
        r4.add(enterCell)
        allKeys.add(enterCell)
        enterKeyCell = enterCell
        rowKeys.add(r4)
    }

    private fun updateLabels() {
        if (layoutMode != KeyboardLayoutMode.ALPHA) return
        for (key in allKeys) {
            if (key.action is KeyAction.CharKey && key.action.char.length == 1 && key.action.char.first().isLetter()) {
                key.primaryLabel = if (isShifted) key.action.char.uppercase() else key.action.char.lowercase()
            }
        }
    }

    private fun getAccentsForChar(char: String): List<String> {
        val lower = char.lowercase()
        val list = when (lower) {
            "a" -> listOf("a", "á", "à", "ã", "â", "ä")
            "e" -> listOf("e", "é", "è", "ê", "ë")
            "i" -> listOf("i", "í", "ì", "î", "ï")
            "o" -> listOf("o", "ó", "ò", "õ", "ô", "ö")
            "u" -> listOf("u", "ú", "ù", "û", "ü")
            "c" -> listOf("c", "ç")
            "n" -> listOf("n", "ñ")
            "." -> listOf(".com", ".br", ".org", ".net", ".io", "/")
            else -> emptyList()
        }
        return if (isShifted && lower != ".") list.map { it.uppercase() } else list
    }

    // Touch Handling with Multi-Touch Pointer Tracker
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val actionMasked = event.actionMasked
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)

        when (actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val x = event.getX(pointerIndex)
                val y = event.getY(pointerIndex)
                handlePointerDown(pointerId, x, y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val pId = event.getPointerId(i)
                    val px = event.getX(i)
                    val py = event.getY(i)
                    handlePointerMove(pId, px, py, event.eventTime)
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val x = event.getX(pointerIndex)
                val y = event.getY(pointerIndex)
                handlePointerUp(pointerId, x, y)
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                handlePointerCancel()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handlePointerDown(pointerId: Int, x: Float, y: Float) {
        // Chronological Thumb Rollover:
        // If another pointer was holding down a regular character/space key, commit it immediately!
        if (activeSessions.size() > 0) {
            for (i in 0 until activeSessions.size()) {
                val prev = activeSessions.valueAt(i)
                if (prev != null && !prev.isLongPressActive && !prev.isGlide && prev.currentKey != null) {
                    val k = prev.currentKey!!
                    if (k.action is KeyAction.CharKey || k.action is KeyAction.Space) {
                        prev.longPressRunnable?.let { mainHandler.removeCallbacks(it) }
                        prev.longPressRunnable = null
                        k.isPressed = false
                        commitKeyAction(k)
                        prev.currentKey = null
                    }
                }
            }
        }

        val key = SpatialHitboxResolver.resolveKey(
            x, y, allKeys, validNextCharsMask, isAdaptiveHitboxesEnabled
        ) ?: return

        key.isPressed = true
        animateKeyFeedback(key)
        listener?.onKeyTouchDown()

        val session = PointerSession(
            pointerId = pointerId,
            downX = x,
            downY = y,
            currentX = x,
            currentY = y,
            downTime = SystemClock.uptimeMillis(),
            currentKey = key,
            isSpaceSlide = (key.action is KeyAction.Space),
            lastSpaceX = x
        )
        activeSessions.put(pointerId, session)

        // Setup Long Press runnable for Accent Callout
        if (key.accents.isNotEmpty()) {
            val longPressRunnable = Runnable {
                if (session.currentKey == key && !session.isGlide && !session.isSpaceSlide) {
                    session.isLongPressActive = true
                    openAccentCallout(key)
                }
            }
            session.longPressRunnable = longPressRunnable
            mainHandler.postDelayed(longPressRunnable, 280L)
        }

        invalidate()
    }

    private fun handlePointerMove(pointerId: Int, x: Float, y: Float, eventTime: Long) {
        val session = activeSessions.get(pointerId) ?: return
        session.currentX = x
        session.currentY = y

        val deltaX = x - session.downX
        val deltaY = y - session.downY
        val dist = hypot(deltaX, deltaY)

        // 1. Accent Callout selection tracking
        if (session.isLongPressActive && activeCalloutKey != null) {
            updateAccentCalloutSelection(x)
            return
        }

        // 2. Spacebar cursor slide / selection
        if (session.isSpaceSlide) {
            session.longPressRunnable?.let { mainHandler.removeCallbacks(it) }
            val spaceDelta = x - session.lastSpaceX
            val stepDist = 12f * density
            if (abs(spaceDelta) >= stepDist) {
                val steps = (spaceDelta / stepDist).toInt()
                session.lastSpaceX += steps * stepDist
                listener?.onSpaceTrackpad(steps, isShifted)
            }
            return
        }

        // 3. Glide Typing detection
        if (isGlideEnabled && layoutMode == KeyboardLayoutMode.ALPHA && !session.isLongPressActive) {
            if (!session.isGlide && dist > 18f * density && session.currentKey?.isActionKey == false) {
                session.isGlide = true
                session.longPressRunnable?.let { mainHandler.removeCallbacks(it) }
                session.currentKey?.isPressed = false
                listener?.onGlideStart(session.downX, session.downY)
            }
            if (session.isGlide) {
                listener?.onGlideMove(x, y, eventTime)
                return
            }
        }
    }

    private fun handlePointerUp(pointerId: Int, x: Float, y: Float) {
        val session = activeSessions.get(pointerId) ?: return
        activeSessions.remove(pointerId)
        session.longPressRunnable?.let { mainHandler.removeCallbacks(it) }

        val key = session.currentKey

        // 1. Finalize Accent Callout
        if (session.isLongPressActive && activeCalloutKey != null) {
            closeAccentCallout()
            if (activeAccentIndex >= 0 && activeAccentIndex < calloutAccents.size) {
                val selectedAccent = calloutAccents[activeAccentIndex]
                listener?.onChar(selectedAccent)
            }
            key?.isPressed = false
            invalidate()
            return
        }

        // 2. Finalize Glide
        if (session.isGlide) {
            listener?.onGlideEnd()
            key?.isPressed = false
            invalidate()
            return
        }

        // 3. Finalize Space trackpad
        if (session.isSpaceSlide && hypot(x - session.downX, y - session.downY) > 14f * density) {
            key?.isPressed = false
            invalidate()
            return
        }

        // 4. Fast Flick Up
        val pressDuration = SystemClock.uptimeMillis() - session.downTime
        val upwardDelta = session.downY - y
        val horizontalDelta = abs(x - session.downX)
        if (isFlickAccentsEnabled && pressDuration < 340L && upwardDelta > 18f * density && horizontalDelta < 18f * density) {
            key?.isPressed = false
            if (key != null) {
                val flickChar = key.secondaryHint ?: key.accents.getOrNull(1)
                if (flickChar != null) {
                    listener?.onFlickUp(flickChar)
                    invalidate()
                    return
                }
            }
        }

        // 5. Normal Tap Commit
        if (key != null) {
            key.isPressed = false
            commitKeyAction(key)
        }

        invalidate()
    }

    private fun handlePointerCancel() {
        for (i in 0 until activeSessions.size()) {
            val s = activeSessions.valueAt(i)
            s?.longPressRunnable?.let { mainHandler.removeCallbacks(it) }
            s?.currentKey?.isPressed = false
        }
        activeSessions.clear()
        closeAccentCallout()
        invalidate()
    }

    private fun commitKeyAction(key: KeyCell) {
        when (val act = key.action) {
            is KeyAction.CharKey -> {
                val toEmit = if (isShifted && layoutMode == KeyboardLayoutMode.ALPHA) {
                    act.char.uppercase()
                } else {
                    act.char
                }
                listener?.onChar(toEmit)
            }
            is KeyAction.Space -> listener?.onSpace()
            is KeyAction.Backspace -> listener?.onBackspace()
            is KeyAction.Enter -> listener?.onEnter()
            is KeyAction.Shift -> {
                val now = SystemClock.uptimeMillis()
                val isDouble = (now - lastShiftPressTime < 350L)
                lastShiftPressTime = now
                listener?.onShiftClick(isDouble)
            }
            is KeyAction.ModeSymbol -> listener?.onModeToggle()
            is KeyAction.ModeExtendedSymbol -> listener?.onExtendedModeToggle()
        }
    }

    private fun openAccentCallout(key: KeyCell) {
        activeCalloutKey = key
        calloutAccents.clear()
        calloutAccents.addAll(key.accents)
        if (calloutAccents.isEmpty()) {
            calloutAccents.add(key.primaryLabel)
        }
        activeAccentIndex = 0

        // Calculate callout bounding box directly above key
        val itemWidth = 44f * density
        val itemHeight = 48f * density
        val totalCalloutWidth = itemWidth * calloutAccents.size
        val halfW = totalCalloutWidth / 2f
        var left = key.centerX - halfW
        left = left.coerceIn(4f * density, width - totalCalloutWidth - 4f * density)
        val right = left + totalCalloutWidth
        val bottom = key.visualRect.top - 8f * density
        val top = bottom - itemHeight

        calloutRect.set(left, top, right, bottom)
        calloutItemRects.clear()
        for (i in calloutAccents.indices) {
            val iLeft = left + i * itemWidth
            calloutItemRects.add(RectF(iLeft, top, iLeft + itemWidth, bottom))
        }

        listener?.onAccentSelectedTick()
        invalidate()
    }

    private fun updateAccentCalloutSelection(touchX: Float) {
        var newIdx = -1
        for (i in calloutItemRects.indices) {
            val r = calloutItemRects[i]
            if (touchX >= r.left && touchX <= r.right) {
                newIdx = i
                break
            }
        }
        if (newIdx == -1) {
            newIdx = if (touchX < calloutRect.left) 0 else calloutAccents.size - 1
        }
        if (newIdx != activeAccentIndex && newIdx in calloutAccents.indices) {
            activeAccentIndex = newIdx
            listener?.onAccentSelectedTick()
            invalidate()
        }
    }

    private fun closeAccentCallout() {
        activeCalloutKey = null
        calloutAccents.clear()
        activeAccentIndex = -1
        invalidate()
    }

    private fun animateKeyFeedback(key: KeyCell) {
        val anim = ValueAnimator.ofFloat(0.4f, 1.0f).apply {
            duration = 140L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                key.pressProgress = it.animatedValue as Float
                invalidate()
            }
        }
        anim.start()
    }

    // 120Hz Zero-Allocation Canvas Drawing
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(palette.bgColor)

        val cornerRadius = when (keyShape) {
            "pill" -> 100f * density
            "borderless" -> 8f * density
            else -> 8f * density // "squircle"
        }
        val isBorderless = (keyShape == "borderless")

        // Draw Keys
        for (key in allKeys) {
            val vRect = key.visualRect

            // 1. Soft Shadow (only for squircle/pill)
            if (!isBorderless && palette.shadowColor != 0) {
                tempRect.set(vRect.left, vRect.top + 1.5f * density, vRect.right, vRect.bottom + 1.5f * density)
                canvas.drawRoundRect(tempRect, cornerRadius, cornerRadius, shadowPaint)
            }

            // 2. Key Surface
            val bgPaint = if (key.isActionKey) actionKeyBgPaint else keyBgPaint
            if (!isBorderless) {
                canvas.drawRoundRect(vRect, cornerRadius, cornerRadius, bgPaint)
            } else if (key.isPressed) {
                // Translucent glow on press for borderless
                canvas.drawRoundRect(vRect, cornerRadius, cornerRadius, actionKeyBgPaint)
            }

            // 3. Specular 1px Top Highlight
            if (!isBorderless && palette.specularBorderColor != 0) {
                tempPath.reset()
                tempPath.moveTo(vRect.left + cornerRadius * 0.5f, vRect.top + 0.5f)
                tempPath.lineTo(vRect.right - cornerRadius * 0.5f, vRect.top + 0.5f)
                canvas.drawPath(tempPath, specularPaint)
            }

            // 4. Primary Label (Optically Centered)
            val pPaint = if (key.isActionKey) actionTextPaint else textPaint
            pPaint.textSize = when {
                key.action is KeyAction.Space -> 13f * density
                key.action is KeyAction.ModeSymbol || key.action is KeyAction.ModeExtendedSymbol -> 14f * density
                key.primaryLabel.length > 1 -> 18f * density
                else -> 20.5f * density
            }
            pPaint.getFontMetrics(fontMetrics)
            val baseline = vRect.centerY() - (fontMetrics.descent + fontMetrics.ascent) / 2f
            canvas.drawText(key.primaryLabel, vRect.centerX(), baseline, pPaint)

            // Shift Caps Lock bar indicator
            if (key.action is KeyAction.Shift) {
                if (isCapsLock) {
                    val barWidth = 14f * density
                    val barHeight = 2.5f * density
                    val bLeft = vRect.centerX() - barWidth / 2f
                    val bTop = vRect.bottom - 7f * density
                    canvas.drawRoundRect(bLeft, bTop, bLeft + barWidth, bTop + barHeight, 2f * density, 2f * density, capsIndicatorPaint)
                }
            }

            // 5. Secondary Key Hint (Top-Right Corner without shifting letter)
            if (showKeyHints && key.secondaryHint != null && layoutMode == KeyboardLayoutMode.ALPHA) {
                hintPaint.textSize = 9f * density
                val hintX = vRect.right - 5f * density
                val hintY = vRect.top + 11.5f * density
                canvas.drawText(key.secondaryHint, hintX, hintY, hintPaint)
            }
        }

        // Draw Accent Overlay Callout if active
        if (activeCalloutKey != null && calloutAccents.isNotEmpty()) {
            val cRadius = 14f * density
            // Background card
            canvas.drawRoundRect(calloutRect, cRadius, cRadius, calloutBgPaint)

            // Accent items
            for (i in calloutAccents.indices) {
                val itemRect = calloutItemRects[i]
                if (i == activeAccentIndex) {
                    canvas.drawRoundRect(itemRect, cRadius, cRadius, calloutHighlightPaint)
                }
                calloutTextPaint.textSize = 21f * density
                calloutTextPaint.getFontMetrics(fontMetrics)
                val itemBase = itemRect.centerY() - (fontMetrics.descent + fontMetrics.ascent) / 2f
                canvas.drawText(calloutAccents[i], itemRect.centerX(), itemBase, calloutTextPaint)
            }
        }
    }
}
