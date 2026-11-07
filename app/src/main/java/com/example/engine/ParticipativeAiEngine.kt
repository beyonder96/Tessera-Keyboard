package com.example.engine

import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GroqChatRequest
import com.example.api.GroqClient
import com.example.api.GroqMessage
import com.example.api.Part
import com.example.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class AiIntentMode {
    GRAMMAR_CORRECTION,
    BIBLE_VERSE_LOOKUP,
    SEMANTIC_REPAIR,
    SMART_ASSISTANT
}

data class ParsedAiTrigger(
    val mode: AiIntentMode,
    val matchedTag: String,
    val userContent: String,
    val targetSpanText: String,
    val deleteBeforeCount: Int,
    val deleteAfterCount: Int = 0
) {
    val fullPrefixLength: Int get() = deleteBeforeCount
}

data class AiSuggestionResult(
    val originalText: String,
    val targetSpanText: String = "",
    val suggestionText: String,
    val displayLabel: String,
    val mode: AiIntentMode,
    val deleteBeforeCount: Int = 0,
    val deleteAfterCount: Int = 0,
    val isError: Boolean = false,
    val modelUsed: String? = null
) {
    val charsToDelete: Int get() = deleteBeforeCount
}

object ParticipativeAiEngine {

    private val CORRECTION_TAGS = listOf("corrigir", "correcao", "correção", "c", "fix")
    private val VERSE_TAGS = listOf("versiculo", "versículo", "v", "biblia", "bíblia")
    private val AI_TAGS = listOf("ia", "ai", "copiloto")

    fun parseTrigger(
        textBefore: String,
        textAfter: String = "",
        selectedText: String? = null,
        isPassiveModeEnabled: Boolean = false
    ): ParsedAiTrigger? {
        // 1. Se o usuário selecionou texto manualmente, prioridade total na seleção
        if (!selectedText.isNullOrBlank()) {
            val content = selectedText.trim()
            if (content.isNotEmpty()) {
                return ParsedAiTrigger(
                    mode = AiIntentMode.GRAMMAR_CORRECTION,
                    matchedTag = "selection",
                    userContent = content,
                    targetSpanText = selectedText,
                    deleteBeforeCount = 0,
                    deleteAfterCount = 0
                )
            }
        }

        if (textBefore.isBlank() && textAfter.isBlank()) return null

        // 2. Detecção de tags no final do texto: ex: "frase digitada @corrigir"
        for ((mode, tags) in listOf(
            AiIntentMode.GRAMMAR_CORRECTION to CORRECTION_TAGS,
            AiIntentMode.BIBLE_VERSE_LOOKUP to VERSE_TAGS,
            AiIntentMode.SMART_ASSISTANT to AI_TAGS
        )) {
            val tagPattern = tags.joinToString("|")
            val endRegex = Regex("""(?:^|[\n.?!;])?\s*(.+?)\s+[@/]($tagPattern)[.?!]?\s*$""", RegexOption.IGNORE_CASE)
            endRegex.find(textBefore)?.let { match ->
                val content = match.groupValues[1].trim()
                val tag = match.groupValues[2]
                if (content.isNotEmpty()) {
                    val fullTarget = match.value.trimStart()
                    val tagStartInBefore = textBefore.lastIndexOf(fullTarget).takeIf { it >= 0 } ?: 0
                    val deleteBefore = textBefore.length - tagStartInBefore
                    return ParsedAiTrigger(
                        mode = mode,
                        matchedTag = tag,
                        userContent = content,
                        targetSpanText = fullTarget,
                        deleteBeforeCount = deleteBefore,
                        deleteAfterCount = 0
                    )
                }
            }
        }

        // 3. Detecção de tags no início do texto: ex: "@corrigir frase digitada"
        for ((mode, tags) in listOf(
            AiIntentMode.GRAMMAR_CORRECTION to CORRECTION_TAGS,
            AiIntentMode.BIBLE_VERSE_LOOKUP to VERSE_TAGS,
            AiIntentMode.SMART_ASSISTANT to AI_TAGS
        )) {
            val tagPattern = tags.joinToString("|")
            val startRegex = Regex("""(?:^|[\n\s])[@/]($tagPattern)\s+(.+)""", RegexOption.IGNORE_CASE)
            startRegex.find(textBefore)?.let { match ->
                val tag = match.groupValues[1]
                val contentBefore = match.groupValues[2]
                // Se houver texto após o cursor na mesma linha/oração, une para dar contexto total
                val contentAfter = textAfter.takeWhile { it != '\n' && it != '.' && it != '!' && it != '?' }
                val fullContent = (contentBefore + contentAfter).trim()
                if (fullContent.isNotEmpty()) {
                    val tagIndexInBefore = textBefore.lastIndexOf("@$tag").takeIf { it >= 0 }
                        ?: textBefore.lastIndexOf("/$tag").takeIf { it >= 0 }
                        ?: match.range.first
                    val deleteBefore = textBefore.length - tagIndexInBefore
                    val deleteAfter = contentAfter.length
                    val fullTarget = textBefore.substring(tagIndexInBefore) + contentAfter
                    return ParsedAiTrigger(
                        mode = mode,
                        matchedTag = tag,
                        userContent = fullContent,
                        targetSpanText = fullTarget,
                        deleteBeforeCount = deleteBefore,
                        deleteAfterCount = deleteAfter
                    )
                }
            }
        }

        // 4. Modo passivo contínuo (Sem tags): captura a oração completa ao redor do cursor
        if (isPassiveModeEnabled) {
            val sentenceStartInBefore = maxOf(
                textBefore.lastIndexOf('\n'),
                textBefore.lastIndexOf(". "),
                textBefore.lastIndexOf("! "),
                textBefore.lastIndexOf("? ")
            ).let { if (it == -1) 0 else if (textBefore[it] == '\n') it + 1 else it + 2 }

            val sentencePartBefore = textBefore.substring(sentenceStartInBefore)
            val sentenceEndInAfter = minOf(
                textAfter.indexOf('\n').takeIf { it != -1 } ?: textAfter.length,
                textAfter.indexOf('.').takeIf { it != -1 } ?: textAfter.length,
                textAfter.indexOf('!').takeIf { it != -1 } ?: textAfter.length,
                textAfter.indexOf('?').takeIf { it != -1 } ?: textAfter.length
            )
            val sentencePartAfter = textAfter.substring(0, sentenceEndInAfter)
            val fullSentence = (sentencePartBefore + sentencePartAfter).trim()

            if (fullSentence.length >= 12 && !fullSentence.startsWith("@") && !fullSentence.startsWith("/")) {
                val words = fullSentence.split(Regex("""\s+""")).filter { it.isNotBlank() }
                if (words.size >= 2) {
                    return ParsedAiTrigger(
                        mode = AiIntentMode.SEMANTIC_REPAIR,
                        matchedTag = "",
                        userContent = fullSentence,
                        targetSpanText = sentencePartBefore + sentencePartAfter,
                        deleteBeforeCount = sentencePartBefore.length,
                        deleteAfterCount = sentencePartAfter.length
                    )
                }
            }
        }

        return null
    }

    fun parseTrigger(text: String, isPassiveModeEnabled: Boolean = false): ParsedAiTrigger? {
        return parseTrigger(
            textBefore = text,
            textAfter = "",
            selectedText = null,
            isPassiveModeEnabled = isPassiveModeEnabled
        )
    }

    fun buildSystemPrompt(mode: AiIntentMode): String {
        return when (mode) {
            AiIntentMode.GRAMMAR_CORRECTION ->
                "Você é o revisor gramatical e ortográfico de alta precisão do teclado Tessera em português do Brasil. " +
                "Sua única tarefa é reescrever a oração/frase inteira corrigindo com rigor erros de ortografia, concordância verbal/nominal, pontuação, acentuação e clareza. " +
                "Mantenha fielmente o significado, o contexto e a intenção original do usuário. " +
                "Retorne ESTRITAMENTE APENAS o texto completo e corrigido para substituição, sem aspas, explicações, saudações ou comentários."

            AiIntentMode.BIBLE_VERSE_LOOKUP ->
                "Você é um especialista em citações bíblicas e referências para o teclado Tessera. " +
                "Ao receber um trecho imperfeito, incompleto ou palavras-chave de uma passagem bíblica em português, " +
                "identifique a passagem correta (versão ARA ou NVI) e retorne ESTRITAMENTE no formato:\n" +
                "[Texto do versículo corrigido]. — [Livro] [Capítulo]:[Versículo]\n" +
                "Exemplo para 'o senhor e meu pastor': O Senhor é o meu pastor; nada me faltará. — Salmos 23:1\n" +
                "Se for uma citação célebre não-bíblica, retorne no formato: [Citação]. — [Autor]\n" +
                "Retorne APENAS o texto puro sem aspas adicionais no início/fim e sem explicações."

            AiIntentMode.SEMANTIC_REPAIR ->
                "Você é o assistente de escrita e clareza do teclado Tessera em português do Brasil. " +
                "Analise a oração recebida no contexto completo. Se houver digitação incorreta, palavras trocadas, truncadas ou falta de pontuação, reescreva a oração inteira de forma natural e correta. " +
                "Se já estiver correta, devolva a frase inalterada com a pontuação polida. " +
                "Retorne ESTRITAMENTE APENAS o texto final completo, sem aspas, notas ou introduções."

            AiIntentMode.SMART_ASSISTANT ->
                "Você é o copiloto de escrita inteligente do teclado Tessera em português do Brasil. " +
                "Se for um pedido de texto ou pergunta, responda de forma ultra-concisa e direta para ser inserido na mensagem. " +
                "Se for um texto com erros, devolva-o corrigido por completo. " +
                "Retorne ESTRITAMENTE APENAS o resultado final a ser inserido no campo de texto."
        }
    }

    suspend fun processText(
        textBefore: String,
        textAfter: String = "",
        selectedText: String? = null,
        isPassiveModeEnabled: Boolean,
        provider: String,
        apiKey: String
    ): AiSuggestionResult? = withContext(Dispatchers.IO) {
        val cleanApiKey = apiKey.trim().removePrefix("Bearer ").removePrefix("bearer ").trim()
        if (cleanApiKey.isBlank()) return@withContext null

        val trigger = parseTrigger(
            textBefore = textBefore,
            textAfter = textAfter,
            selectedText = selectedText,
            isPassiveModeEnabled = isPassiveModeEnabled
        ) ?: return@withContext null

        val systemPrompt = buildSystemPrompt(trigger.mode)
        val userPrompt = trigger.userContent

        try {
            val (generatedText, modelUsed) = when (provider.lowercase()) {
                "gemini" -> callGemini(cleanApiKey, systemPrompt, userPrompt)
                else -> callGroq(cleanApiKey, systemPrompt, userPrompt)
            }

            val cleaned = cleanOutput(generatedText, trigger.mode)
            if (cleaned.isBlank() || cleaned.equals(trigger.userContent, ignoreCase = true)) {
                return@withContext null
            }

            val displayLabel = when (trigger.mode) {
                AiIntentMode.BIBLE_VERSE_LOOKUP -> "📖 $cleaned"
                AiIntentMode.GRAMMAR_CORRECTION -> "✨ $cleaned"
                AiIntentMode.SEMANTIC_REPAIR -> "✨ $cleaned"
                AiIntentMode.SMART_ASSISTANT -> "⚡ $cleaned"
            }

            AiSuggestionResult(
                originalText = trigger.userContent,
                targetSpanText = trigger.targetSpanText,
                suggestionText = cleaned,
                displayLabel = displayLabel,
                mode = trigger.mode,
                deleteBeforeCount = trigger.deleteBeforeCount,
                deleteAfterCount = trigger.deleteAfterCount,
                isError = false,
                modelUsed = modelUsed
            )
        } catch (httpEx: retrofit2.HttpException) {
            val code = httpEx.code()
            val rawErrorBody = try {
                httpEx.response()?.errorBody()?.string()
            } catch (e: Exception) {
                null
            }
            android.util.Log.e("TesseraAI", "Erro HTTP na chamada de IA: $code, body: $rawErrorBody", httpEx)
            val apiMsg = extractErrorMessage(rawErrorBody)

            val errorLabel = when (code) {
                401, 403 -> "⚠️ Chave de API inválida (${provider.uppercase()})"
                429 -> "⚠️ Limite da Groq atingido. Aguarde."
                404 -> if (!apiMsg.isNullOrBlank()) "⚠️ $apiMsg" else "⚠️ Modelo de IA indisponível (404)"
                else -> if (!apiMsg.isNullOrBlank()) "⚠️ $apiMsg" else "⚠️ Erro na IA (HTTP $code)"
            }
            AiSuggestionResult(
                originalText = trigger.userContent,
                targetSpanText = trigger.targetSpanText,
                suggestionText = errorLabel,
                displayLabel = errorLabel,
                mode = trigger.mode,
                deleteBeforeCount = 0,
                deleteAfterCount = 0,
                isError = true
            )
        } catch (t: Throwable) {
            android.util.Log.e("TesseraAI", "Falha de rede ou exceção na IA: ${t.message}", t)
            AiSuggestionResult(
                originalText = trigger.userContent,
                targetSpanText = trigger.targetSpanText,
                suggestionText = "⚠️ Falha de conexão com a IA",
                displayLabel = "⚠️ Falha de conexão com a IA",
                mode = trigger.mode,
                deleteBeforeCount = 0,
                deleteAfterCount = 0,
                isError = true
            )
        }
    }

    suspend fun processText(
        rawInput: String,
        isPassiveModeEnabled: Boolean,
        provider: String,
        apiKey: String
    ): AiSuggestionResult? = processText(
        textBefore = rawInput,
        textAfter = "",
        selectedText = null,
        isPassiveModeEnabled = isPassiveModeEnabled,
        provider = provider,
        apiKey = apiKey
    )

    private val CANDIDATE_GROQ_MODELS = listOf(
        "qwen/qwen3.8-27b",
        "openai/gpt-oss-20b",
        "openai/gpt-oss-120b",
        "llama-3.1-8b-instant",
        "llama-3.3-70b-versatile"
    )

    private val CANDIDATE_GEMINI_MODELS = listOf(
        "gemini-1.5-flash",
        "gemini-2.0-flash",
        "gemini-1.5-flash-latest",
        "gemini-1.5-pro"
    )

    @Volatile
    private var resolvedGroqModel: String? = null

    fun resetModelCache() {
        resolvedGroqModel = null
    }

    private suspend fun resolveGroqModel(apiKey: String): String {
        resolvedGroqModel?.let { return it }
        try {
            val response = GroqClient.service.listModels("Bearer $apiKey")
            val available = response.data?.map { it.id.trim() } ?: emptyList()
            if (available.isNotEmpty()) {
                val match = CANDIDATE_GROQ_MODELS.firstOrNull { it in available }
                    ?: available.firstOrNull { id ->
                        id.contains("qwen", ignoreCase = true) ||
                        id.contains("gpt-oss", ignoreCase = true) ||
                        id.contains("llama", ignoreCase = true)
                    }
                if (match != null) {
                    resolvedGroqModel = match
                    return match
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("TesseraAI", "Falha ao consultar /models na Groq: ${e.message}")
        }
        return "qwen/qwen3.8-27b"
    }

    private fun extractErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val regex = Regex(""""message"\s*:\s*"([^"]+)"""")
            val match = regex.find(errorBody)
            match?.groupValues?.get(1)?.take(100)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun callGroq(apiKey: String, systemPrompt: String, userPrompt: String): Pair<String, String> {
        val candidateModels = mutableListOf<String>()
        val primary = resolveGroqModel(apiKey)
        candidateModels.add(primary)
        for (candidate in CANDIDATE_GROQ_MODELS) {
            if (candidate !in candidateModels) {
                candidateModels.add(candidate)
            }
        }

        var lastHttpEx: retrofit2.HttpException? = null

        for (model in candidateModels) {
            try {
                val request = GroqChatRequest(
                    model = model,
                    messages = listOf(
                        GroqMessage(role = "system", content = systemPrompt),
                        GroqMessage(role = "user", content = userPrompt)
                    ),
                    temperature = 0.2,
                    maxTokens = 256
                )
                val response = GroqClient.service.chatCompletion("Bearer $apiKey", request)
                val text = response.choices?.firstOrNull()?.message?.content?.trim() ?: ""
                resolvedGroqModel = model
                return Pair(text, model)
            } catch (httpEx: retrofit2.HttpException) {
                lastHttpEx = httpEx
                if (httpEx.code() == 404) {
                    android.util.Log.w("TesseraAI", "Modelo Groq '$model' retornou 404. Tentando alternativo...")
                    continue
                } else {
                    throw httpEx
                }
            }
        }

        throw (lastHttpEx ?: RuntimeException("Nenhum modelo Groq disponível"))
    }

    private suspend fun callGemini(apiKey: String, systemPrompt: String, userPrompt: String): Pair<String, String> {
        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = userPrompt)))
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        var lastEx: Exception? = null
        for (model in CANDIDATE_GEMINI_MODELS) {
            try {
                val response = RetrofitClient.service.generateContent(
                    model = model,
                    apiKey = apiKey,
                    request = request
                )
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: ""
                return Pair(text, model)
            } catch (httpEx: retrofit2.HttpException) {
                lastEx = httpEx
                if (httpEx.code() == 404) {
                    continue
                } else {
                    throw httpEx
                }
            }
        }

        throw (lastEx ?: RuntimeException("Erro ao chamar Gemini"))
    }

    private fun cleanOutput(raw: String, mode: AiIntentMode): String {
        var text = raw.trim()
        
        // Remove blocos de código markdown se o modelo envolver em ```
        if (text.startsWith("```") && text.endsWith("```")) {
            text = text.removeSurrounding("```").trim()
            if (text.startsWith("markdown\n") || text.startsWith("text\n")) {
                text = text.substringAfter("\n").trim()
            }
        }

        // Para versículos e correções, se o modelo colocou aspas desnecessárias no início e fim, remove
        if (mode == AiIntentMode.BIBLE_VERSE_LOOKUP || mode == AiIntentMode.GRAMMAR_CORRECTION) {
            if ((text.startsWith("\"") && text.endsWith("\"")) ||
                (text.startsWith("“") && text.endsWith("”"))) {
                text = text.substring(1, text.length - 1).trim()
            }
        }

        // Remove prefixos como "Correção:" ou "Versículo:" se o modelo inventar
        val unwantedPrefixes = listOf(
            "Correção:", "Texto corrigido:", "Versículo:", "Sugestão:", "Aqui está:", "Resposta:"
        )
        for (prefix in unwantedPrefixes) {
            if (text.startsWith(prefix, ignoreCase = true)) {
                text = text.substring(prefix.length).trim()
            }
        }

        return text
    }
}
