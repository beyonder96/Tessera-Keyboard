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

    private val accentRestorationMap = mapOf(
        "voce" to "você", "voces" to "vocês", "nao" to "não", "tambem" to "também",
        "esta" to "está", "estao" to "estão", "ate" to "até", "ja" to "já", "so" to "só",
        "sera" to "será", "serao" to "serão", "ola" to "olá", "agua" to "água",
        "opcao" to "opção", "opcoes" to "opções", "acao" to "ação", "acoes" to "ações",
        "situacao" to "situação", "situacoes" to "situações", "informacao" to "informação",
        "informacoes" to "informações", "atencao" to "atenção", "questao" to "questão",
        "questoes" to "questões", "relacao" to "relação", "relacoes" to "relações",
        "razao" to "razão", "razoes" to "razões", "coracao" to "coração", "coracoes" to "corações",
        "visao" to "visão", "visoes" to "visões", "padrao" to "padrão", "padroes" to "padrões",
        "versao" to "versão", "versoes" to "versões", "solucao" to "solução", "solucoes" to "soluções",
        "opiniao" to "opinião", "opinioes" to "opiniões", "producao" to "produção",
        "criacao" to "criação", "funcao" to "função", "funcoes" to "funções",
        "regiao" to "região", "regioes" to "regiões", "posicao" to "posição", "posicoes" to "posições",
        "condicao" to "condição", "condicoes" to "condições", "educacao" to "educação",
        "protecao" to "proteção", "construcao" to "construção", "comunicacao" to "comunicação",
        "populacao" to "população", "geracao" to "geração", "geracoes" to "gerações",
        "facil" to "fácil", "faceis" to "fáceis", "dificil" to "difícil", "dificeis" to "difíceis",
        "util" to "útil", "uteis" to "úteis", "nivel" to "nível", "niveis" to "níveis",
        "possivel" to "possível", "possiveis" to "possíveis", "impossivel" to "impossível",
        "impossiveis" to "impossíveis", "responsavel" to "responsável", "responsaveis" to "responsáveis",
        "incrivel" to "incrível", "incriveis" to "incríveis", "alem" to "além", "porem" to "porém",
        "alguem" to "alguém", "ninguem" to "ninguém", "ultimo" to "último", "ultima" to "última",
        "ultimos" to "últimos", "ultimas" to "últimas", "proximo" to "próximo", "proxima" to "próxima",
        "proximos" to "próximos", "proximas" to "próximas", "rapido" to "rápido", "rapida" to "rápida",
        "rapidos" to "rápidos", "rapidas" to "rápidas", "publico" to "público", "publica" to "pública",
        "publicos" to "públicos", "publicas" to "públicas", "numero" to "número", "numeros" to "números",
        "duvida" to "dúvida", "duvidas" to "dúvidas", "saude" to "saúde", "musica" to "música",
        "musicas" to "músicas", "politica" to "política", "politicas" to "políticas", "historia" to "história",
        "historias" to "histórias", "familia" to "família", "familias" to "famílias", "ciencia" to "ciência",
        "ciencias" to "ciências", "inicio" to "início", "periodo" to "período", "periodos" to "períodos",
        "analise" to "análise", "analises" to "análises", "pagina" to "página", "paginas" to "páginas",
        "titulo" to "título", "titulos" to "títulos", "codigo" to "código", "codigos" to "códigos",
        "unico" to "único", "unica" to "única", "unicos" to "únicos", "unicas" to "únicas",
        "otimo" to "ótimo", "otima" to "ótima", "otimos" to "ótimos", "otimas" to "ótima",
        "valido" to "válido", "valida" to "válida", "maximo" to "máximo", "maxima" to "máxima",
        "minimo" to "mínimo", "minima" to "mínima", "pratico" to "prático", "pratica" to "prática",
        "basico" to "básico", "basica" to "básica", "fisico" to "físico", "fisica" to "física",
        "logico" to "lógico", "logica" to "lógica", "critico" to "crítico", "critica" to "crítica",
        "medico" to "médico", "medica" to "médica", "necessario" to "necessário", "necessaria" to "necessária",
        "proprio" to "próprio", "propria" to "própria", "gratis" to "grátis"
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
        for (word in accentRestorationMap.values) {
            trie.insert(word, frequency = 245)
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
        "o" to listOf("que", "dia", "tempo", "trabalho", "seu", "mundo"),
        "a" to listOf("gente", "sua", "mesma", "minha", "vida", "casa"),
        "do" to listOf("brasil", "mundo", "dia", "tempo", "que", "ano"),
        "da" to listOf("casa", "manhã", "tarde", "noite", "sua", "vida"),
        "no" to listOf("brasil", "mundo", "trabalho", "dia", "seu", "fim"),
        "na" to listOf("casa", "verdade", "hora", "cidade", "sua", "vida"),
        "para" to listOf("você", "fazer", "ver", "mim", "o", "a"),
        "por" to listOf("favor", "isso", "exemplo", "enquanto", "causa", "que"),
        "muito" to listOf("obrigado", "obrigada", "bom", "bem", "mais", "tempo"),
        "boa" to listOf("tarde", "noite", "viagem", "sorte", "ideia", "semana"),
        "bom" to listOf("dia", "trabalho", "fim", "tempo", "demais", "ver"),
        "tudo" to listOf("bem", "bom", "certo", "isso", "o", "que"),
        "você" to listOf("está", "vai", "quer", "pode", "sabe", "tem"),
        "eu" to listOf("quero", "vou", "acho", "tenho", "posso", "estou"),
        "não" to listOf("sei", "quero", "posso", "tem", "vai", "está"),
        "com" to listOf("você", "certeza", "calma", "tempo", "ele", "ela"),
        "como" to listOf("você", "vai", "está", "fazer", "se", "isso"),
        "mais" to listOf("uma", "um", "ou", "tarde", "tempo", "nada"),
        "de" to listOf("acordo", "novo", "fato", "repente", "volta", "nada"),
        "se" to listOf("você", "puder", "quiser", "der", "não", "for"),
        "mas" to listOf("não", "eu", "se", "também", "o", "a"),
        "além" to listOf("disso", "do", "da", "de"),
        "sem" to listOf("dúvida", "problema", "certeza", "saber"),
        "vamos" to listOf("fazer", "ver", "lá", "conversar", "marcar")
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

    fun learnBigram(w1: String, w2: String) {
        localDict?.learnBigram(w1, w2)
    }

    fun getPredictions(currentWord: String, previousWord: String? = null): List<String> {
        val clean = currentWord.trim()
        val cleanPrev = previousWord?.trim()?.lowercase()
        if (clean.isBlank()) {
            if (!cleanPrev.isNullOrEmpty()) {
                // 1. Tenta bigramas aprendidos do próprio usuário no LocalDictionary
                val userNextWords = localDict?.getPredictedNextWords(cleanPrev) ?: emptyList()
                val staticNextWords = bigramNextWordMap[cleanPrev] ?: emptyList()
                val combined = (userNextWords + staticNextWords).distinct().take(3)
                if (combined.isNotEmpty()) {
                    return combined
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
        val result = mutableListOf<String>()

        // 1. Camada de Restauração Imediata de Acentos (prioridade máxima)
        val accentRestored = accentRestorationMap[lower]
        if (accentRestored != null) {
            result.add(TrieDictionary.matchCasing(clean, accentRestored))
        }

        // 2. Busca por Prefixo no Trie (até 5 candidatos)
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

        for (p in prefixSuggestions) {
            if (result.size >= 3) break
            if (!result.any { it.equals(p, ignoreCase = true) }) {
                result.add(p)
            }
        }

        val hasExactMatch = result.any {
            TrieDictionary.normalizeFast(it).length == norm.length
        }

        if (hasExactMatch && result.isNotEmpty()) {
            return result.take(3)
        }

        if (Thread.currentThread().isInterrupted) return result.take(3)

        // 3. Camada Fuzzy / QWERTY Proximity se não tiver match exato
        val fuzzySuggestions = trie.findFuzzySuggestions(clean, maxCount = 3)
        if (fuzzySuggestions.isNotEmpty()) {
            val fuzzyCandidate = fuzzySuggestions[0]
            if (!result.any { it.equals(fuzzyCandidate, ignoreCase = true) }) {
                result.add(0, fuzzyCandidate)
            }
            if (!result.contains(clean)) {
                result.add(clean)
            }
            for (i in 1 until fuzzySuggestions.size) {
                if (result.size >= 3) break
                val fz = fuzzySuggestions[i]
                if (!result.any { it.equals(fz, ignoreCase = true) }) {
                    result.add(fz)
                }
            }
        }

        if (!result.contains(clean) && clean.length in 2..30) {
            result.add(clean)
        }

        return result.take(3)
    }

    fun getSwipePredictions(swipePattern: String): List<String> {
        if (swipePattern.length < 2) return emptyList()
        val lower = TrieDictionary.normalizeFast(swipePattern)
        if (lower.length < 2) return emptyList()
        val firstChar = lower.first()
        val lastChar = lower.last()

        val results = mutableListOf<String>()

        // 1. Busca nos termos canônicos e estáticos de alta frequência
        val candidates = normalizedStatic.filter { (word, norm) ->
            if (norm.length < 2) return@filter false
            if (norm.first() != firstChar || norm.last() != lastChar) return@filter false
            
            // Verifica subsequência
            var pIdx = 0
            for (i in 0 until norm.length) {
                val c = norm[i]
                while (pIdx < lower.length && lower[pIdx] != c) {
                    pIdx++
                }
                if (pIdx >= lower.length) return@filter false
                pIdx++
            }
            true
        }

        // Ordena pela diferença de tamanho em relação ao padrão do swipe
        val sortedCandidates = candidates.sortedWith(
            compareBy(
                { Math.abs(it.second.length - lower.length) },
                { -it.first.length }
            )
        ).map { it.first }

        results.addAll(sortedCandidates)

        // 2. Se não encontrou no estático, busca na Trie pelo prefixo inicial
        if (results.size < 3) {
            val trieMatches = trie.findTopSuggestions(firstChar.toString(), maxCount = 25, excludeExact = false)
            for (w in trieMatches) {
                val norm = TrieDictionary.normalizeFast(w)
                if (norm.length >= 2 && norm.first() == firstChar && norm.last() == lastChar) {
                    var pIdx = 0
                    var matched = true
                    for (i in 0 until norm.length) {
                        val c = norm[i]
                        while (pIdx < lower.length && lower[pIdx] != c) {
                            pIdx++
                        }
                        if (pIdx >= lower.length) {
                            matched = false
                            break
                        }
                        pIdx++
                    }
                    if (matched && !results.any { it.equals(w, ignoreCase = true) }) {
                        results.add(w)
                        if (results.size >= 3) break
                    }
                }
            }
        }

        return results.take(3)
    }

    fun getSwipePrediction(swipePattern: String): String? {
        return getSwipePredictions(swipePattern).firstOrNull()
    }
}
