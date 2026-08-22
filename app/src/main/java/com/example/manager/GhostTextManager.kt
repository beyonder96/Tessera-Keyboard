package com.example.manager

import android.graphics.Color
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

class GhostTextManager {

    var currentGhostText: String? = null
        private set

    private var isPasswordContext = false

    fun onStartInput(editorInfo: EditorInfo?) {
        if (editorInfo == null) return
        val variation = editorInfo.inputType and EditorInfo.TYPE_MASK_VARIATION
        isPasswordContext = variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD
        currentGhostText = null
    }

    fun updateGhostText(ic: InputConnection, lastWord: String, topPrediction: String = "") {
        if (isPasswordContext) {
            currentGhostText = null
            return
        }

        if (lastWord.length >= 2 && topPrediction.startsWith(lastWord, ignoreCase = true) && topPrediction.length > lastWord.length) {
            currentGhostText = topPrediction.substring(lastWord.length)
        } else {
            currentGhostText = null
        }
    }

    /**
     * O espaço agora NÃO completa automaticamente a palavra a menos que o usuário clique na sugestão.
     * Retorna false para que o teclado insira um espaço normal (' ') sem interferir no que foi digitado.
     */
    fun onSpaceClicked(ic: InputConnection?): Boolean {
        currentGhostText = null
        return false
    }

    fun clearGhostText(ic: InputConnection?) {
        currentGhostText = null
    }
}

