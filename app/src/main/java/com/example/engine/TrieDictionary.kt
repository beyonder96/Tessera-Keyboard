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
                    .thenByDescending { it.word.equals(cleanLower, ignoreCase = true) }
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
        val words = current.words ?: return@synchronized null
        val exactLiteral = words.firstOrNull { it.word.equals(word, ignoreCase = true) }
        if (exactLiteral != null) return@synchronized exactLiteral
        return@synchronized words.maxByOrNull { it.frequency }
    }

    fun findFuzzySuggestions(word: String, maxCount: Int = 3): List<String> = synchronized(this) {
        if (Thread.currentThread().isInterrupted) return@synchronized emptyList()
        val clean = word.trim()
        if (clean.length < 2) return@synchronized emptyList()
        val norm = normalizeFast(clean)
        if (norm.length < 2) return@synchronized emptyList()

        val maxErrors = when {
            norm.length >= 7 -> 3
            norm.length >= 4 -> 2
            else -> 1
        }

        val scoredCandidates = mutableMapOf<String, Float>()

        // 1. Guided QWERTY proximity search in Trie (handles multiple neighbor typos, e.g. "dkgitsndk" -> "digitando")
        searchTrieProximity(root, norm, 0, 0, maxErrors, scoredCandidates)

        // 2. Transposition of adjacent characters (e.g. "tduo" -> "tudo")
        val chars = norm.toCharArray()
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

        if (scoredCandidates.isEmpty()) return@synchronized emptyList()

        return@synchronized scoredCandidates.entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Float>> { it.value }
                    .thenBy { it.key.length }
            )
            .take(maxCount)
            .map { matchCasing(clean, it.key) }
    }

    private fun searchTrieProximity(
        node: TrieNode,
        word: String,
        idx: Int,
        errors: Int,
        maxErrors: Int,
        results: MutableMap<String, Float>
    ) {
        if (errors > maxErrors || Thread.currentThread().isInterrupted) return
        if (idx == word.length) {
            val list = node.words
            val errorFactor = when (errors) {
                0 -> 1.0f
                1 -> 0.95f
                2 -> 0.85f
                else -> 0.75f
            }
            if (!list.isNullOrEmpty()) {
                for (entry in list) {
                    val score = entry.frequency * errorFactor
                    val currentBest = results[entry.word] ?: 0f
                    if (score > currentBest) {
                        results[entry.word] = score
                    }
                }
            } else if (word.length >= 4) {
                val subCandidates = mutableListOf<WordEntry>()
                collectWords(node, subCandidates, maxCollect = 5)
                for (entry in subCandidates) {
                    val score = entry.frequency * errorFactor * 0.8f
                    val currentBest = results[entry.word] ?: 0f
                    if (score > currentBest) {
                        results[entry.word] = score
                    }
                }
            }
            return
        }

        val c = word[idx]
        if (c in 'a'..'z') {
            val exactChild = node.children[c - 'a']
            if (exactChild != null) {
                searchTrieProximity(exactChild, word, idx + 1, errors, maxErrors, results)
            }

            if (errors < maxErrors) {
                val neighbors = KeyProximityMap.getNeighbors(c)
                for (neighbor in neighbors) {
                    if (neighbor in 'a'..'z') {
                        val neighborChild = node.children[neighbor - 'a']
                        if (neighborChild != null) {
                            searchTrieProximity(neighborChild, word, idx + 1, errors + 1, maxErrors, results)
                        }
                    }
                }
            }
        }
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
