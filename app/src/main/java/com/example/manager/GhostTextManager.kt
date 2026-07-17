package com.example.manager

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

class GhostTextManager {

    private val dictionary = mapOf(
        "Estou" to " a caminho",
        "Muito" to " obrigado",
        "Bom" to " dia",
        "Boa" to " tarde"
    )

    var currentGhostText: String? = null
        private set

    private var isPasswordContext = false
    private val ghostTextColor = Color.parseColor("#80888888")

    fun onStartInput(editorInfo: EditorInfo?) {
        if (editorInfo == null) return
        val variation = editorInfo.inputType and EditorInfo.TYPE_MASK_VARIATION
        isPasswordContext = variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD
        currentGhostText = null
    }

    fun updateGhostText(ic: InputConnection, lastWord: String, topPrediction: String = "") {
        if (isPasswordContext) return

        if (lastWord.length >= 2) {
            var suggestion = topPrediction
            
            // Se a predição principal não começa com a palavra, tenta o dicionário local
            if (suggestion.isBlank() || !suggestion.startsWith(lastWord, ignoreCase = true)) {
                val match = dictionary.keys.firstOrNull { lastWord.equals(it, ignoreCase = true) }
                suggestion = if (match != null) lastWord + dictionary[match] else ""
            }

            if (suggestion.startsWith(lastWord, ignoreCase = true) && suggestion.length > lastWord.length) {
                val ghostPart = suggestion.substring(lastWord.length)
                
                if (currentGhostText == ghostPart) {
                    return
                }
                
                currentGhostText = ghostPart

                val spannable = SpannableString(ghostPart)
                spannable.setSpan(
                    ForegroundColorSpan(ghostTextColor),
                    0, ghostPart.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                // Cursor position 0 coloca o cursor no início do texto fantasma
                ic.setComposingText(spannable, 0)
                return
            }
        }

        clearGhostText(ic)
    }

    fun onSpaceClicked(ic: InputConnection?): Boolean {
        if (ic == null) return false
        if (currentGhostText != null) {
            val textToCommit = currentGhostText!!
            currentGhostText = null
            ic.commitText(textToCommit + " ", 1)
            return true
        }
        return false
    }

    fun clearGhostText(ic: InputConnection?) {
        if (currentGhostText != null) {
            currentGhostText = null
            ic?.commitText("", 1)
        }
    }
}
