package com.example.ui.keyboard

import android.graphics.RectF

sealed class KeyAction {
    data class CharKey(val char: String) : KeyAction()
    object Shift : KeyAction()
    object Backspace : KeyAction()
    object ModeSymbol : KeyAction()
    object ModeExtendedSymbol : KeyAction()
    object Space : KeyAction()
    object Enter : KeyAction()
}

enum class KeyboardLayoutMode {
    ALPHA,
    SYMBOLS,
    EXTENDED_SYMBOLS
}

data class KeyCell(
    val action: KeyAction,
    var primaryLabel: String,
    val secondaryHint: String? = null,
    val accents: List<String> = emptyList(),
    val logicalRect: RectF = RectF(),
    val visualRect: RectF = RectF(),
    val isActionKey: Boolean = false,
    var isPressed: Boolean = false,
    var pressProgress: Float = 0f
) {
    val centerX: Float get() = logicalRect.centerX()
    val centerY: Float get() = logicalRect.centerY()
    val width: Float get() = logicalRect.width()
    val height: Float get() = logicalRect.height()
}

data class KeyboardPalette(
    val bgColor: Int,
    val keyBgColor: Int,
    val actionKeyBgColor: Int,
    val specularBorderColor: Int,
    val shadowColor: Int,
    val textColor: Int,
    val inactiveTextColor: Int,
    val glowColor: Int,
    val calloutBgColor: Int,
    val calloutTextColor: Int,
    val calloutAccentColor: Int
)
