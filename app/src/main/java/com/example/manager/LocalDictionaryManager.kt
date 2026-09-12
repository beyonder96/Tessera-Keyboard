package com.example.manager

import android.content.Context
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class LocalDictionaryManager(context: Context) {
    private val prefs = context.getSharedPreferences("LocalDictionary", Context.MODE_PRIVATE)
    private val WORDS_KEY = "learned_words"
    private val inMemoryWords = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    private val BIGRAMS_KEY = "learned_bigrams"
    private val inMemoryBigrams = ConcurrentHashMap<String, ConcurrentHashMap<String, Int>>()

    init {
        val saved = prefs.getStringSet(WORDS_KEY, emptySet()) ?: emptySet()
        inMemoryWords.addAll(saved)

        val savedBigrams = prefs.getStringSet(BIGRAMS_KEY, emptySet()) ?: emptySet()
        for (item in savedBigrams) {
            val arrowIdx = item.indexOf("->")
            val colonIdx = item.lastIndexOf(':')
            if (arrowIdx > 0 && colonIdx > arrowIdx) {
                val w1 = item.substring(0, arrowIdx)
                val w2 = item.substring(arrowIdx + 2, colonIdx)
                val count = item.substring(colonIdx + 1).toIntOrNull() ?: 1
                inMemoryBigrams.getOrPut(w1) { ConcurrentHashMap() }[w2] = count
            }
        }
    }

    fun getWords(): Set<String> {
        return inMemoryWords.toSet()
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

    fun learnBigram(w1: String, w2: String) {
        val clean1 = w1.trim().lowercase()
        val clean2 = w2.trim().lowercase()
        if (clean1.length in 1..30 && clean2.length in 1..30 &&
            clean1.all { it.isLetter() } && clean2.all { it.isLetter() } && clean1 != clean2) {
            val map = inMemoryBigrams.getOrPut(clean1) { ConcurrentHashMap() }
            val current = map[clean2] ?: 0
            map[clean2] = (current + 1).coerceAtMost(100)

            // Persistência assíncrona limitada
            persistBigrams()
        }
    }

    fun getPredictedNextWords(w1: String): List<String> {
        val clean1 = w1.trim().lowercase()
        val map = inMemoryBigrams[clean1] ?: return emptyList()
        return map.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }
    }

    private fun persistBigrams() {
        val serialized = HashSet<String>()
        var count = 0
        for ((w1, nextMap) in inMemoryBigrams) {
            for ((w2, freq) in nextMap) {
                serialized.add("$w1->$w2:$freq")
                count++
                if (count >= 500) break
            }
            if (count >= 500) break
        }
        prefs.edit().putStringSet(BIGRAMS_KEY, serialized).apply()
    }

    fun removeWord(word: String) {
        val cleanWord = word.trim().lowercase()
        if (inMemoryWords.remove(cleanWord)) {
            prefs.edit().putStringSet(WORDS_KEY, HashSet(inMemoryWords)).apply()
        }
    }

    fun clearWords() {
        inMemoryWords.clear()
        inMemoryBigrams.clear()
        prefs.edit().remove(WORDS_KEY).remove(BIGRAMS_KEY).apply()
    }
}
