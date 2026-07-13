package com.example.engine

class PredictionEngine {
    private val dictionary = listOf(
        "the", "and", "you", "that", "was", "for", "are", "with", "his", "they", "this", "have", "from", "one", "had", "word", "but", "not", "what", "all", "were", "when", "your", "can", "said", "there", "use", "each", "which", "she", "how", "their", "will", "other", "about", "many", "then", "them", "these", "some", "her", "would", "make", "like", "him", "into", "time", "has", "look", "two", "more", "write", "go", "see", "number", "no", "way", "could", "people", "my", "than", "first", "water", "been", "call", "who", "oil", "its", "now", "find", "long", "down", "day", "did", "get", "come", "made", "may", "part",
        "eu", "você", "ele", "ela", "nós", "eles", "elas", "meu", "minha", "seu", "sua", "nosso", "nossa", "um", "uma", "o", "a", "os", "as", "que", "de", "do", "da", "em", "no", "na", "para", "com", "por", "como", "mas", "se", "ou", "não", "sim", "bom", "dia", "boa", "noite", "tarde", "hoje", "amanhã", "ontem", "sempre", "nunca", "muito", "pouco", "tudo", "nada", "alguém", "ninguém", "qualquer", "cada", "mesmo", "outro", "onde", "quando", "quem", "qual", "porque", "pois", "então", "assim", "apenas", "só", "já", "ainda", "até", "bem", "mal", "melhor", "pior", "maior", "menor", "novo", "velho", "certo", "errado", "claro", "escuro", "alto", "baixo", "grande", "pequeno", "forte", "fraco", "feliz", "triste", "amor", "vida", "tempo", "ano", "vez", "hora", "coisa", "casa", "lugar", "trabalho", "pessoa", "gente", "homem", "mulher", "criança", "amigo", "amiga", "nome", "mundo", "parte", "fim", "jeito", "caso", "forma", "exemplo", "ideia", "problema", "verdade", "história", "certeza", "motivo", "razão", "caminho", "lado", "olho", "mão", "cabeça", "palavra", "água", "fogo", "terra", "ar", "sol", "lua", "mês", "semana", "agora",
        "estou", "está", "estamos", "estão", "sou", "é", "somos", "são", "fui", "foi", "fomos", "foram", "vou", "vai", "vamos", "vão", "quero", "quer", "queremos", "querem", "tenho", "tem", "temos", "têm", "faço", "faz", "fazemos", "fazem", "sei", "sabe", "sabemos", "sabem", "posso", "pode", "podemos", "podem", "vejo", "vê", "vemos", "vêm", "acho", "acha", "achamos", "acham", "devo", "deve", "devemos", "devem", "falo", "fala", "falamos", "falam", "fico", "fica", "ficamos", "ficam", "deixo", "deixa", "deixamos", "deixam", "encontro", "encontra", "encontramos", "encontram", "levo", "leva", "levamos", "levam", "começo", "começa", "começamos", "começam", "mostro", "mostra", "mostramos", "mostram", "penso", "pensa", "pensamos", "pensam", "escrevo", "escreve", "escrevemos", "escrevem", "jogo", "joga", "jogamos", "jogam", "ouço", "ouve", "ouvimos", "ouvem", "tento", "tenta", "tentamos", "tentam", "peço", "pede", "pedimos", "pedem", "preciso", "precisa", "precisamos", "precisam", "lembro", "lembra", "lembramos", "lembram", "entendo", "entende", "entendemos", "entendem", "conheço", "conhece", "conhecemos", "conhecem", "espero", "espera", "esperamos", "esperam", "chamo", "chama", "chamamos", "chamam", "gosto", "gosta", "gostamos", "gostam", "ajudo", "ajuda", "ajudamos", "ajudam", "olho", "olha", "olhamos", "olham", "uso", "usa", "usamos", "usam", "mudo", "muda", "mudamos", "mudam", "continuo", "continua", "continuamos", "continuam", "acredito", "acredita", "acreditamos", "acreditam"
    )
    
    // Sort dict for binary search or just keep it simple, filtering is fast enough for <500 words.

    fun getPredictions(currentWord: String): List<String> {
        if (currentWord.isBlank()) {
            return listOf("eu", "o", "que")
        }
        val lower = currentWord.lowercase()
        return dictionary.filter { it.startsWith(lower) && it != lower }
            .sortedBy { it.length }
            .take(3)
    }
    fun getSwipePrediction(swipePattern: String): String? {
        if (swipePattern.length < 2) return null
        val lower = swipePattern.lowercase()
        val firstChar = lower.first()
        val lastChar = lower.last()
        
        // Find best match in dictionary
        val matches = dictionary.filter { word ->
            if (word.first() != firstChar || word.last() != lastChar) return@filter false
            
            // Check if all characters in the word appear in the swipe pattern in order
            var wordIdx = 0
            for (char in lower) {
                if (wordIdx < word.length && char == word[wordIdx]) {
                    wordIdx++
                }
            }
            wordIdx == word.length
        }
        
        // Prefer shorter words that match the pattern, or most common words
        return matches.sortedBy { it.length }.firstOrNull()
    }
}
