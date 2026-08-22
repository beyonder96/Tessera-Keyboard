package com.example.manager

import android.content.Context
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class LocalDictionaryManager(context: Context) {
    private val prefs = context.getSharedPreferences("LocalDictionary", Context.MODE_PRIVATE)
    private val WORDS_KEY = "learned_words"
    private val inMemoryWords = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    init {
        val saved = prefs.getStringSet(WORDS_KEY, emptySet()) ?: emptySet()
        inMemoryWords.addAll(saved)
    }

    fun getWords(): Set<String> {
        return inMemoryWords
    }

    fun learnWord(word: String) {
        val cleanWord = word.trim().lowercase()
        // Ignora palavras inválidas, com números ou símbolos
        if (cleanWord.length in 2..30 && cleanWord.all { it.isLetter() }) {
            if (inMemoryWords.add(cleanWord)) {
                // Persiste de forma assíncrona sem bloquear
                prefs.edit().putStringSet(WORDS_KEY, HashSet(inMemoryWords)).apply()
            }
        }
    }
}
