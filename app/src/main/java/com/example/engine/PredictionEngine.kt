package com.example.engine

import com.example.manager.LocalDictionaryManager
import java.text.Normalizer
import java.util.regex.Pattern

class PredictionEngine(private val localDict: LocalDictionaryManager? = null) {

    private val staticDictionary = listOf(
        // Pronomes e conectivos frequentes
        "eu", "você", "ele", "ela", "nós", "eles", "elas", "meu", "minha", "seu", "sua", "nosso", "nossa",
        "um", "uma", "uns", "umas", "o", "a", "os", "as", "que", "de", "do", "da", "dos", "das", "em", "no",
        "na", "nos", "nas", "para", "com", "por", "como", "mas", "se", "ou", "não", "sim", "já", "ainda", "até",
        "bem", "mal", "muito", "pouco", "tudo", "nada", "alguém", "ninguém", "qualquer", "cada", "mesmo", "outro",
        "outra", "onde", "quando", "quem", "qual", "quais", "porque", "porquê", "pois", "então", "assim", "apenas", "só",
        // Saudações e tempo
        "bom", "dia", "boa", "tarde", "noite", "olá", "oi", "hoje", "amanhã", "ontem", "sempre", "nunca", "agora",
        "depois", "tempo", "ano", "anos", "mês", "semana", "hora", "horas", "vez", "vezes", "cedo", "tarde",
        // Substantivos comuns
        "coisa", "casa", "lugar", "trabalho", "pessoa", "pessoas", "gente", "homem", "mulher", "criança",
        "amigo", "amiga", "amigos", "família", "nome", "mundo", "parte", "fim", "começo", "jeito", "caso",
        "forma", "exemplo", "ideia", "problema", "verdade", "certeza", "motivo", "razão", "caminho", "lado",
        "olho", "mão", "cabeça", "palavra", "água", "fogo", "terra", "ar", "sol", "lua", "cidade", "carro",
        // Verbos essenciais
        "estou", "está", "estamos", "estão", "estava", "sou", "é", "somos", "são", "era", "fui", "foi", "fomos", "foram",
        "vou", "vai", "vamos", "vão", "ia", "quero", "quer", "queremos", "querem", "queria", "tenho", "tem", "temos", "têm", "tinha",
        "faço", "faz", "fazemos", "fazem", "fazia", "sei", "sabe", "sabemos", "sabem", "sabia", "posso", "pode", "podemos", "podem", "podia",
        "vejo", "vê", "vemos", "vêm", "via", "acho", "acha", "achamos", "acham", "devo", "deve", "devemos", "devem",
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

    private val accentPattern: Pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")

    private fun normalize(str: String): String {
        val nfd = Normalizer.normalize(str.lowercase(), Normalizer.Form.NFD)
        return accentPattern.matcher(nfd).replaceAll("")
    }

    // Cache pré-computado de palavras e suas formas normalizadas para busca ultrarrápida
    private val normalizedStatic = staticDictionary.map { word -> word to normalize(word) }

    fun getPredictions(currentWord: String): List<String> {
        if (currentWord.isBlank()) {
            return listOf("eu", "o", "que")
        }
        val cleanInput = currentWord.trim().lowercase()
        val normalizedInput = normalize(cleanInput)
        
        // 1. Busca primeiro nas palavras aprendidas pelo usuário
        val localWords = localDict?.getWords() ?: emptySet()
        val localMatches = mutableListOf<String>()
        for (word in localWords) {
            val normWord = normalize(word)
            if (normWord.startsWith(normalizedInput) && !word.equals(cleanInput, ignoreCase = true)) {
                localMatches.add(word)
                if (localMatches.size >= 3) break
            }
        }

        // 2. Busca no dicionário estático
        val staticMatches = mutableListOf<String>()
        for ((word, normWord) in normalizedStatic) {
            if (normWord.startsWith(normalizedInput) && !word.equals(cleanInput, ignoreCase = true) && !localMatches.contains(word)) {
                staticMatches.add(word)
                if (localMatches.size + staticMatches.size >= 6) break
            }
        }

        // Ordena por comprimento (mais curtas primeiro) e pega as top 3
        return (localMatches + staticMatches)
            .distinct()
            .sortedBy { it.length }
            .take(3)
    }

    fun getSwipePrediction(swipePattern: String): String? {
        if (swipePattern.length < 2) return null
        val lower = normalize(swipePattern)
        val firstChar = lower.first()
        val lastChar = lower.last()

        val matches = normalizedStatic.filter { (word, norm) ->
            if (norm.first() != firstChar || norm.last() != lastChar) return@filter false
            var wordIdx = 0
            for (char in lower) {
                if (wordIdx < norm.length && char == norm[wordIdx]) {
                    wordIdx++
                }
            }
            wordIdx == norm.length
        }

        return matches.sortedBy { it.first.length }.firstOrNull()?.first
    }
}

