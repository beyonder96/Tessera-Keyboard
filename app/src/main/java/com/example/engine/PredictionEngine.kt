package com.example.engine

import android.content.Context
import com.example.manager.LocalDictionaryManager
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class PredictionEngine(
    private val localDict: LocalDictionaryManager? = null,
    context: Context? = null
) {

    private val trie = TrieDictionary()

    private val staticDictionary = listOf(
        // Pronomes e conectivos frequentes (alta prioridade)
        "eu", "você", "ele", "ela", "nós", "eles", "elas", "meu", "minha", "seu", "sua", "nosso", "nossa",
        "um", "uma", "uns", "umas", "o", "a", "os", "as", "que", "de", "do", "da", "dos", "das", "em", "no",
        "na", "nos", "nas", "para", "com", "por", "como", "mas", "se", "ou", "não", "sim", "já", "ainda", "até", "também", "aqui", "ali",
        "bem", "mal", "muito", "pouco", "tudo", "nada", "alguém", "ninguém", "qualquer", "cada", "mesmo", "outro",
        "outra", "onde", "quando", "quem", "qual", "quais", "porque", "porquê", "pois", "então", "assim", "apenas", "só",
        "este", "esta", "estes", "estas", "isto", "esse", "essa", "esses", "essas", "isso", "aquele", "aquela",
        // Saudações e tempo
        "bom", "dia", "boa", "tarde", "noite", "olá", "oi", "hoje", "amanhã", "ontem", "sempre", "nunca", "agora",
        "depois", "tempo", "ano", "anos", "mês", "semana", "hora", "horas", "vez", "vezes", "cedo",
        // Substantivos e termos comuns
        "coisa", "casa", "lugar", "trabalho", "pessoa", "pessoas", "gente", "homem", "mulher", "criança",
        "amigo", "amiga", "amigos", "família", "nome", "mundo", "parte", "fim", "começo", "jeito", "caso",
        "forma", "exemplo", "ideia", "problema", "verdade", "certeza", "motivo", "razão", "caminho", "lado",
        "olho", "mão", "cabeça", "palavra", "água", "fogo", "terra", "ar", "sol", "lua", "cidade", "carro",
        "teclado", "atraso", "camada", "correção", "ortográfica", "português", "brasil",
        // Verbos essenciais
        "estar", "estou", "está", "estamos", "estão", "estava", "ser", "sou", "é", "somos", "são", "era", "fui", "foi", "fomos", "foram",
        "ir", "vou", "vai", "vamos", "vão", "ia", "querer", "quero", "quer", "queremos", "querem", "queria", "ter", "tenho", "tem", "temos", "têm", "tinha",
        "fazer", "faço", "faz", "fazemos", "fazem", "fazia", "saber", "sei", "sabe", "sabemos", "sabem", "sabia", "poder", "posso", "pode", "podemos", "podem", "podia",
        "ver", "vejo", "vê", "vemos", "vêm", "via", "dar", "dizer", "passar", "demonstrar", "demonstra", "digitar", "digitando",
        "acho", "acha", "achamos", "acham", "devo", "deve", "devemos", "devem",
        "falo", "fala", "falamos", "falam", "fico", "fica", "ficamos", "ficam", "deixo", "deixa", "deixamos", "deixam",
        "encontro", "encontra", "encontramos", "encontram", "levo", "leva", "levamos", "levam", "começo", "começa", "começamos", "começam",
        "penso", "pensa", "pensamos", "pensam", "escrevo", "escreve", "escrevemos", "escrevem", "jogo", "joga", "jogamos", "jogam",
        "ouço", "ouve", "ouvimos", "ouvem", "tento", "tenta", "tentamos", "tentam", "peço", "pede", "pedimos", "pedem",
        "preciso", "precisa", "precisamos", "precisam", "lembro", "lembra", "lembramos", "lembram", "entendo", "entende", "entendemos", "entendem",
        "conheço", "conhece", "conhecemos", "conhecem", "espero", "espera", "esperamos", "esperam", "chamo", "chama", "chamamos", "chamam",
        "gosto", "gosta", "gostamos", "gostam", "ajudo", "ajuda", "ajudamos", "ajudam", "olho", "olha", "olhamos", "olham",
        "uso", "usa", "usamos", "usam", "mudo", "muda", "mudamos", "mudam", "continuo", "continua", "continuamos", "continuam",
        "acredito", "acredita", "acreditamos", "acreditam", "obrigado", "obrigada", "favor", "por favor", "beleza", "tudo bem"
    )

    private val abbreviationsMap = mapOf(
        "vc" to "você",
        "vcs" to "vocês",
        "tb" to "também",
        "tbm" to "também",
        "pq" to "porque",
        "oq" to "o que",
        "blz" to "beleza",
        "vlw" to "valeu",
        "obg" to "obrigado",
        "obgd" to "obrigado",
        "obgda" to "obrigada",
        "mto" to "muito",
        "mt" to "muito",
        "cmg" to "comigo",
        "ctz" to "certeza",
        "msg" to "mensagem",
        "td" to "tudo",
        "agr" to "agora",
        "hj" to "hoje",
        "dps" to "depois",
        "qdo" to "quando",
        "qto" to "quanto",
        "pfv" to "por favor",
        "pf" to "por favor",
        "abs" to "abraços",
        "fds" to "fim de semana",
        "pdc" to "pode crer",
        "flw" to "falou",
        "sqn" to "só que não",
        "cm" to "com",
        "kd" to "cadê"
    )

    private val normalizedStatic: List<Pair<String, String>> = staticDictionary.map {
        it to TrieDictionary.normalizeFast(it)
    }

    private val predictionCache = object : LinkedHashMap<String, List<String>>(512, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<String>>?): Boolean {
            return size > 512
        }
    }

    init {
        for (word in staticDictionary) {
            trie.insert(word, frequency = 240)
        }
        for ((abbr, full) in abbreviationsMap) {
            trie.insert(full, frequency = 250)
            trie.insert(abbr, frequency = 220)
        }
        localDict?.getWords()?.forEach { word ->
            trie.insert(word, frequency = 200)
        }
        prewarmPredictions()
        if (context == null) {
            loadDictionary(null)
            prewarmPredictions()
        } else {
            loadDictionaryAsync(context)
        }
    }

    private fun prewarmPredictions() {
        for (c in 'a'..'z') {
            val s = c.toString()
            val preds = computePredictions(s)
            synchronized(predictionCache) {
                predictionCache[s] = preds
            }
        }
    }

    private fun loadDictionaryAsync(context: Context?) {
        kotlin.concurrent.thread(name = "TesseraDictLoader", isDaemon = true) {
            loadDictionary(context)
            prewarmPredictions()
        }
    }

    private fun loadDictionary(context: Context?) {
        var inputStream: InputStream? = null
        try {
            inputStream = context?.assets?.open("dictionary_pt_br.txt")
                ?: javaClass.classLoader?.getResourceAsStream("assets/dictionary_pt_br.txt")
                ?: javaClass.classLoader?.getResourceAsStream("dictionary_pt_br.txt")

            if (inputStream != null) {
                BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8), 65536).use { reader ->
                    val batch = ArrayList<Pair<String, Int>>(5000)
                    var line: String? = reader.readLine()
                    while (line != null) {
                        val trimmed = line.trim()
                        if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                            val spaceIdx = trimmed.indexOf(' ')
                            if (spaceIdx > 0) {
                                val word = trimmed.substring(0, spaceIdx)
                                val freq = trimmed.substring(spaceIdx + 1).toIntOrNull() ?: 50
                                batch.add(Pair(word, freq))
                            } else {
                                batch.add(Pair(trimmed, 50))
                            }
                            if (batch.size >= 5000) {
                                trie.insertBatch(batch)
                                batch.clear()
                            }
                        }
                        line = reader.readLine()
                    }
                    if (batch.isNotEmpty()) {
                        trie.insertBatch(batch)
                        batch.clear()
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback safe: staticDictionary always guarantees coverage
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
        }
    }

    fun learnWord(word: String) {
        val clean = word.trim().lowercase()
        if (clean.length in 2..30 && clean.all { it.isLetter() }) {
            localDict?.learnWord(clean)
            trie.insert(clean, frequency = 200)
            synchronized(predictionCache) {
                predictionCache.clear()
            }
            prewarmPredictions()
        }
    }

    private val bigramNextWordMap = mapOf(
        "o" to listOf("que", "dia", "tempo", "trabalho"),
        "do" to listOf("brasil", "mundo", "dia", "tempo"),
        "da" to listOf("casa", "manhã", "tarde", "noite"),
        "no" to listOf("brasil", "mundo", "trabalho", "dia"),
        "na" to listOf("casa", "verdade", "hora", "cidade"),
        "para" to listOf("você", "fazer", "ver", "mim"),
        "por" to listOf("favor", "isso", "exemplo", "enquanto"),
        "muito" to listOf("obrigado", "obrigada", "bom", "bem"),
        "boa" to listOf("tarde", "noite", "viagem", "sorte"),
        "bom" to listOf("dia", "trabalho", "fim"),
        "tudo" to listOf("bem", "bom", "certo"),
        "você" to listOf("está", "vai", "quer", "pode", "sabe", "tem"),
        "eu" to listOf("quero", "vou", "acho", "tenho", "posso", "estou"),
        "não" to listOf("sei", "quero", "posso", "tem", "vai", "está"),
        "com" to listOf("você", "certeza", "calma", "tempo")
    )

    private val contextAmbiguityBoost = mapOf(
        "para" to "ver",
        "você" to "ver",
        "vai" to "ver",
        "quer" to "ver",
        "queria" to "ver",
        "vamos" to "ver",
        "posso" to "ver",
        "ele" to "vê",
        "ela" to "vê",
        "quem" to "vê",
        "copo" to "de",
        "gosto" to "de",
        "antes" to "de",
        "depois" to "de"
    )

    fun getPredictions(currentWord: String, previousWord: String? = null): List<String> {
        val clean = currentWord.trim()
        val cleanPrev = previousWord?.trim()?.lowercase()
        if (clean.isBlank()) {
            if (!cleanPrev.isNullOrEmpty()) {
                val nextWords = bigramNextWordMap[cleanPrev]
                if (!nextWords.isNullOrEmpty()) {
                    return nextWords.take(3)
                }
            }
            return listOf("eu", "o", "que")
        }

        val cacheKey = if (!cleanPrev.isNullOrEmpty()) "$cleanPrev|$clean" else clean

        synchronized(predictionCache) {
            predictionCache[cacheKey]
        }?.let { return it }

        val result = computePredictions(clean, cleanPrev)
        synchronized(predictionCache) {
            predictionCache[cacheKey] = result
        }
        return result
    }

    private fun computePredictions(clean: String, previousWord: String? = null): List<String> {
        val lower = clean.lowercase()
        val expansion = abbreviationsMap[lower]
        if (expansion != null) {
            val expandedWord = TrieDictionary.matchCasing(clean, expansion)
            return listOf(expandedWord, clean)
        }

        if (Thread.currentThread().isInterrupted) return emptyList()

        val norm = TrieDictionary.normalizeFast(clean)

        var prefixSuggestions = trie.findTopSuggestions(clean, maxCount = 5, excludeExact = false)

        if (!previousWord.isNullOrEmpty() && prefixSuggestions.size > 1) {
            val boostTarget = contextAmbiguityBoost[previousWord]
            if (boostTarget != null) {
                val idx = prefixSuggestions.indexOfFirst { it.equals(boostTarget, ignoreCase = true) }
                if (idx > 0) {
                    val reordered = prefixSuggestions.toMutableList()
                    val target = reordered.removeAt(idx)
                    reordered.add(0, target)
                    prefixSuggestions = reordered
                }
            }
        }
        prefixSuggestions = prefixSuggestions.take(3)

        val hasExactMatch = prefixSuggestions.any {
            TrieDictionary.normalizeFast(it).length == norm.length
        }

        if (hasExactMatch) {
            return prefixSuggestions
        }

        if (Thread.currentThread().isInterrupted) return prefixSuggestions

        val fuzzySuggestions = trie.findFuzzySuggestions(clean, maxCount = 3)
        if (fuzzySuggestions.isNotEmpty()) {
            val result = mutableListOf<String>()
            result.add(fuzzySuggestions[0])
            if (!result.contains(clean)) {
                result.add(clean)
            }
            for (i in 1 until fuzzySuggestions.size) {
                if (result.size >= 3) break
                if (!result.contains(fuzzySuggestions[i])) {
                    result.add(fuzzySuggestions[i])
                }
            }
            for (p in prefixSuggestions) {
                if (result.size >= 3) break
                if (!result.contains(p)) {
                    result.add(p)
                }
            }
            return result.take(3)
        }

        return prefixSuggestions
    }

    fun getSwipePrediction(swipePattern: String): String? {
        if (swipePattern.length < 2) return null
        val lower = TrieDictionary.normalizeFast(swipePattern)
        if (lower.isEmpty()) return null
        val firstChar = lower.first()
        val lastChar = lower.last()

        val matches = normalizedStatic.filter { (word, norm) ->
            if (norm.isEmpty() || norm.first() != firstChar || norm.last() != lastChar) return@filter false
            var wordIdx = 0
            for (i in 0 until lower.length) {
                val char = lower[i]
                if (wordIdx < norm.length && char == norm[wordIdx]) {
                    wordIdx++
                }
            }
            wordIdx == norm.length
        }

        return matches.sortedBy { it.first.length }.firstOrNull()?.first
    }
}
