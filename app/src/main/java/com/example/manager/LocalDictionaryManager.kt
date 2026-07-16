package com.example.manager

import android.content.Context

class LocalDictionaryManager(context: Context) {
    private val prefs = context.getSharedPreferences("LocalDictionary", Context.MODE_PRIVATE)
    private val WORDS_KEY = "learned_words"

    fun getWords(): Set<String> {
        return prefs.getStringSet(WORDS_KEY, emptySet()) ?: emptySet()
    }

    fun learnWord(word: String) {
        val cleanWord = word.trim().lowercase()
        if (cleanWord.length > 1) {
            val currentWords = getWords().toMutableSet()
            if (currentWords.add(cleanWord)) {
                prefs.edit().putStringSet(WORDS_KEY, currentWords).apply()
            }
        }
    }
}
