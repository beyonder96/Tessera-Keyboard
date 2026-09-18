package com.example.manager

import android.content.Context
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class LocalDictionaryManager(context: Context) {
    private val prefs = context.getSharedPreferences("LocalDictionary", Context.MODE_PRIVATE)

    // Palavras adicionadas manualmente pelo usuário na interface de configurações
    private val MANUAL_WORDS_KEY = "user_manual_words"
    private val inMemoryManualWords = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    // Bigramas para aprendizado de transições contextuais na digitação (ex: bom -> dia)
    private val BIGRAMS_KEY = "learned_bigrams"
    private val inMemoryBigrams = ConcurrentHashMap<String, ConcurrentHashMap<String, Int>>()

    init {
        val savedManual = prefs.getStringSet(MANUAL_WORDS_KEY, emptySet()) ?: emptySet()
        inMemoryManualWords.addAll(savedManual)

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

    /**
     * Retorna exclusivamente as palavras adicionadas manualmente pelo usuário.
     */
    fun getManualWords(): Set<String> {
        return inMemoryManualWords.toSet()
    }

    /**
     * Adiciona manualmente uma palavra ao dicionário pessoal do usuário.
     */
    fun addManualWord(word: String): Boolean {
        val cleanWord = word.trim().lowercase()
        if (cleanWord.length in 2..30 && cleanWord.all { it.isLetter() }) {
            if (inMemoryManualWords.add(cleanWord)) {
                prefs.edit().putStringSet(MANUAL_WORDS_KEY, HashSet(inMemoryManualWords)).apply()
                return true
            }
        }
        return false
    }

    /**
     * Remove manualmente uma palavra do dicionário pessoal do usuário.
     */
    fun removeManualWord(word: String): Boolean {
        val cleanWord = word.trim().lowercase()
        if (inMemoryManualWords.remove(cleanWord)) {
            prefs.edit().putStringSet(MANUAL_WORDS_KEY, HashSet(inMemoryManualWords)).apply()
            return true
        }
        return false
    }

    /**
     * Limpa todas as palavras do dicionário manual do usuário.
     */
    fun clearManualWords() {
        inMemoryManualWords.clear()
        prefs.edit().remove(MANUAL_WORDS_KEY).apply()
    }

    /**
     * Compatibilidade: Retorna as palavras manuais para inserção na árvore de predição.
     */
    fun getWords(): Set<String> {
        return inMemoryManualWords.toSet()
    }

    /**
     * Palavras digitadas não poluem mais a lista persistente do usuário.
     */
    fun learnWord(word: String) {
        // Intencionalmente não adiciona a inMemoryManualWords para evitar poluir o dicionário pessoal do usuário
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
        removeManualWord(word)
    }

    fun clearWords() {
        clearManualWords()
        inMemoryBigrams.clear()
        prefs.edit().remove(BIGRAMS_KEY).remove("learned_words").apply()
    }
}
