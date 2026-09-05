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

        fun matchCasing(source: String, target: String): String {
            if (source.isEmpty() || target.isEmpty()) return target
            if (source.length >= 2 && source.all { it.isUpperCase() }) {
                return target.uppercase()
            }
            if (source.first().isUpperCase()) {
                return target.replaceFirstChar { it.uppercase() }
            }
            return target
        }

        fun hasAccents(w: String): Boolean {
            for (i in 0 until w.length) {
                val c = w[i]
                if (c in "áéíóúãõâêîôûàçÁÉÍÓÚÃÕÂÊÎÔÛÀÇ") return true
            }
            return false
        }
    }

    fun insert(word: String, frequency: Int = 1) = synchronized(this) {
        val clean = word.trim()
        if (clean.isEmpty()) return@synchronized
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

    fun insertBatch(entries: Iterable<Pair<String, Int>>) = synchronized(this) {
        for ((word, freq) in entries) {
            val clean = word.trim()
            if (clean.isEmpty()) continue
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
                if (freq > existing.frequency) {
                    existing.frequency = freq
                }
            } else {
                list.add(WordEntry(clean.lowercase(), freq))
            }
        }
    }

    fun findTopSuggestions(prefix: String, maxCount: Int = 3, excludeExact: Boolean = false): List<String> = synchronized(this) {
        val cleanPrefix = prefix.trim()
        if (cleanPrefix.isEmpty()) return@synchronized emptyList()
        val normalizedPrefix = normalizeFast(cleanPrefix)
        if (normalizedPrefix.isEmpty()) return@synchronized emptyList()

        var current = root
        for (i in 0 until normalizedPrefix.length) {
            val c = normalizedPrefix[i]
            if (c !in 'a'..'z') return@synchronized emptyList()
            val next = current.children[c - 'a'] ?: return@synchronized emptyList()
            current = next
        }

        val candidates = mutableListOf<WordEntry>()
        collectWords(current, candidates, maxCollect = 45)

        val cleanLower = cleanPrefix.lowercase()
        val exactLen = normalizedPrefix.length

        candidates
            .filter { !excludeExact || !it.word.equals(cleanLower, ignoreCase = true) }
            .sortedWith(
                compareByDescending<WordEntry> { it.word.length == exactLen }
                    .thenByDescending { if (it.word.length == exactLen && hasAccents(it.word)) 1 else 0 }
                    .thenByDescending { it.frequency }
                    .thenBy { it.word.length }
            )
            .map { matchCasing(cleanPrefix, it.word) }
            .distinct()
            .take(maxCount)
    }

    fun findExactWord(word: String): WordEntry? = synchronized(this) {
        val normalized = normalizeFast(word)
        if (normalized.isEmpty()) return@synchronized null
        var current = root
        for (i in 0 until normalized.length) {
            val c = normalized[i]
            if (c !in 'a'..'z') return@synchronized null
            current = current.children[c - 'a'] ?: return@synchronized null
        }
        current.words?.sortedWith(
            compareByDescending<WordEntry> { hasAccents(it.word) }
                .thenByDescending { it.frequency }
        )?.firstOrNull()
    }

    fun findFuzzySuggestions(word: String, maxCount: Int = 3): List<String> {
        if (Thread.currentThread().isInterrupted) return emptyList()
        val clean = word.trim()
        if (clean.length < 2) return emptyList()
        val norm = normalizeFast(clean)
        if (norm.length < 2) return emptyList()

        val scoredCandidates = mutableMapOf<String, Float>()

        // 1. Proximity Substitution: replace 1 character with adjacent QWERTY key
        val chars = norm.toCharArray()
        for (i in chars.indices) {
            if (Thread.currentThread().isInterrupted) return emptyList()
            val original = chars[i]
            val neighbors = KeyProximityMap.getNeighbors(original)
            for (neighbor in neighbors) {
                chars[i] = neighbor
                val match = findExactWord(String(chars))
                if (match != null) {
                    val score = match.frequency * 0.95f
                    val currentBest = scoredCandidates[match.word] ?: 0f
                    if (score > currentBest) {
                        scoredCandidates[match.word] = score
                    }
                }
            }
            chars[i] = original
        }

        // 2. Transposition of adjacent characters (e.g. "tduo" -> "tudo")
        for (i in 0 until chars.size - 1) {
            val c1 = chars[i]
            val c2 = chars[i + 1]
            chars[i] = c2
            chars[i + 1] = c1
            val match = findExactWord(String(chars))
            if (match != null) {
                val score = match.frequency * 0.90f
                val currentBest = scoredCandidates[match.word] ?: 0f
                if (score > currentBest) {
                    scoredCandidates[match.word] = score
                }
            }
            chars[i] = c1
            chars[i + 1] = c2
        }

        // 3. Deletion of 1 accidentally inserted character (e.g. "tuudo" -> "tudo")
        if (chars.size >= 4) {
            for (i in chars.indices) {
                val sb = StringBuilder(chars.size - 1)
                for (j in chars.indices) {
                    if (j != i) sb.append(chars[j])
                }
                val match = findExactWord(sb.toString())
                if (match != null) {
                    val score = match.frequency * 0.85f
                    val currentBest = scoredCandidates[match.word] ?: 0f
                    if (score > currentBest) {
                        scoredCandidates[match.word] = score
                    }
                }
            }
        }

        if (scoredCandidates.isEmpty()) return emptyList()

        return scoredCandidates.entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Float>> { it.value }
                    .thenByDescending { hasAccents(it.key) }
            )
            .take(maxCount)
            .map { matchCasing(clean, it.key) }
    }

    private fun collectWords(startNode: TrieNode, result: MutableList<WordEntry>, maxCollect: Int) {
        val queue = java.util.ArrayDeque<TrieNode>()
        queue.add(startNode)
        while (!queue.isEmpty() && result.size < maxCollect) {
            val node = queue.removeFirst()
            node.words?.let {
                result.addAll(it)
            }
            if (result.size >= maxCollect) break
            for (child in node.children) {
                if (child != null) {
                    queue.add(child)
                }
            }
        }
    }
}
