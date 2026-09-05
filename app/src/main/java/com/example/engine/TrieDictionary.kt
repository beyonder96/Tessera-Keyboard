package com.example.engine

class TrieDictionary {

    data class WordEntry(val word: String, var frequency: Int)

    class TrieNode {
        val children: Array<TrieNode?> = arrayOfNulls(26)
        var words: MutableList<WordEntry>? = null
    }

    val root = TrieNode()

    companion object {
        fun normalizeChar(c: Char): Char = when (c) {
            'á', 'à', 'ã', 'â', 'ä', 'Á', 'À', 'Ã', 'Â', 'Ä' -> 'a'
            'é', 'è', 'ê', 'ë', 'É', 'È', 'Ê', 'Ë' -> 'e'
            'í', 'ì', 'î', 'ï', 'Í', 'Ì', 'Î', 'Ï' -> 'i'
            'ó', 'ò', 'õ', 'ô', 'ö', 'Ó', 'Ò', 'Õ', 'Ô', 'Ö' -> 'o'
            'ú', 'ù', 'û', 'ü', 'Ú', 'Ù', 'Û', 'Ü' -> 'u'
            'ç', 'Ç' -> 'c'
            'ñ', 'Ñ' -> 'n'
            else -> c.lowercaseChar()
        }

        fun normalizeFast(input: String): String {
            val sb = java.lang.StringBuilder(input.length)
            for (i in 0 until input.length) {
                val c = normalizeChar(input[i])
                if (c in 'a'..'z') {
                    sb.append(c)
                }
            }
            return sb.toString()
        }
    }

    fun insert(word: String, frequency: Int = 1) {
        val clean = word.trim()
        if (clean.isEmpty()) return
        var current = root
        for (i in 0 until clean.length) {
            val c = normalizeChar(clean[i])
            if (c !in 'a'..'z') continue
            val index = c - 'a'
            var next = current.children[index]
            if (next == null) {
                next = TrieNode()
                current.children[index] = next
            }
            current = next
        }

        val list = current.words ?: mutableListOf<WordEntry>().also { current.words = it }
        val existing = list.find { it.word.equals(clean, ignoreCase = true) }
        if (existing != null) {
            if (frequency > existing.frequency) {
                existing.frequency = frequency
            }
        } else {
            list.add(WordEntry(clean.lowercase(), frequency))
        }
    }

    fun findTopSuggestions(prefix: String, maxCount: Int = 3, excludeExact: Boolean = true): List<String> {
        val normalizedPrefix = normalizeFast(prefix)
        if (normalizedPrefix.isEmpty()) return emptyList()

        var current = root
        for (i in 0 until normalizedPrefix.length) {
            val c = normalizedPrefix[i]
            if (c !in 'a'..'z') return emptyList()
            val next = current.children[c - 'a'] ?: return emptyList()
            current = next
        }

        val candidates = mutableListOf<WordEntry>()
        collectWords(current, candidates, maxCollect = 30)

        val cleanPrefix = prefix.trim().lowercase()
        return candidates
            .filter { !excludeExact || !it.word.equals(cleanPrefix, ignoreCase = true) }
            .sortedWith(
                compareByDescending<WordEntry> { it.frequency }
                    .thenBy { it.word.length }
            )
            .map { it.word }
            .distinct()
            .take(maxCount)
    }

    private fun collectWords(node: TrieNode, result: MutableList<WordEntry>, maxCollect: Int) {
        if (result.size >= maxCollect) return
        node.words?.let {
            result.addAll(it)
        }
        for (child in node.children) {
            if (child != null) {
                collectWords(child, result, maxCollect)
                if (result.size >= maxCollect) break
            }
        }
    }
}
