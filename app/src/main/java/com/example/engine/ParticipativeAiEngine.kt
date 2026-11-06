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
    val fullPrefixLength: Int
)

data class AiSuggestionResult(
    val originalText: String,
    val suggestionText: String,
    val displayLabel: String,
    val mode: AiIntentMode,
    val charsToDelete: Int
)

object ParticipativeAiEngine {

    // Regex para detecção flexível de tags com ou sem '@' ou '/'
    private val TAG_CORRECTION_REGEX = Regex(
        """^[@/](corrigir|correcao|correção|c|fix)\s+(.+)""",
        RegexOption.IGNORE_CASE
    )
    private val TAG_VERSE_REGEX = Regex(
        """^[@/](versiculo|versículo|v|biblia|bíblia)\s+(.+)""",
        RegexOption.IGNORE_CASE
    )
    private val TAG_GENERIC_AI_REGEX = Regex(
        """^[@/](ia|ai|copiloto)\s+(.+)""",
        RegexOption.IGNORE_CASE
    )

    fun parseTrigger(text: String, isPassiveModeEnabled: Boolean = false): ParsedAiTrigger? {
        val trimmed = text.trimStart()
        if (trimmed.isEmpty()) return null

        TAG_CORRECTION_REGEX.find(trimmed)?.let { match ->
            val tag = match.groupValues[1]
            val content = match.groupValues[2].trim()
            if (content.isNotEmpty()) {
                return ParsedAiTrigger(
                    mode = AiIntentMode.GRAMMAR_CORRECTION,
                    matchedTag = tag,
                    userContent = content,
                    fullPrefixLength = trimmed.length
                )
            }
        }

        TAG_VERSE_REGEX.find(trimmed)?.let { match ->
            val tag = match.groupValues[1]
            val content = match.groupValues[2].trim()
            if (content.isNotEmpty()) {
                return ParsedAiTrigger(
                    mode = AiIntentMode.BIBLE_VERSE_LOOKUP,
                    matchedTag = tag,
                    userContent = content,
                    fullPrefixLength = trimmed.length
                )
            }
        }

        TAG_GENERIC_AI_REGEX.find(trimmed)?.let { match ->
            val tag = match.groupValues[1]
            val content = match.groupValues[2].trim()
            if (content.isNotEmpty()) {
                return ParsedAiTrigger(
                    mode = AiIntentMode.SMART_ASSISTANT,
                    matchedTag = tag,
                    userContent = content,
                    fullPrefixLength = trimmed.length
                )
            }
        }

        // Modo passivo/contínuo ativado: analisa frases sem tag explícita
        if (isPassiveModeEnabled && trimmed.length >= 10 && !trimmed.startsWith("@") && !trimmed.startsWith("/")) {
            return ParsedAiTrigger(
                mode = AiIntentMode.SEMANTIC_REPAIR,
                matchedTag = "",
                userContent = trimmed,
                fullPrefixLength = trimmed.length
            )
        }

        return null
    }

    fun buildSystemPrompt(mode: AiIntentMode): String {
        return when (mode) {
            AiIntentMode.GRAMMAR_CORRECTION ->
                "Você é o revisor gramatical e ortográfico de alta precisão do teclado Tessera em português do Brasil. " +
                "Sua única tarefa é corrigir rigorosamente erros ortográficos, concordância verbal/nominal, pontuação, acentuação e fluidez da frase. " +
                "Mantenha fielmente o significado e intenção original do usuário. " +
                "Retorne ESTRITAMENTE APENAS o texto final corrigido, sem aspas, explicações, saudações ou comentários."

            AiIntentMode.BIBLE_VERSE_LOOKUP ->
                "Você é um especialista em citações bíblicas e sabedoria para o teclado Tessera. " +
                "Ao receber um trecho imperfeito, incompleto ou palavras-chave de uma passagem bíblica em português, " +
                "identifique a passagem correta (versão ARA ou NVI) e retorne ESTRITAMENTE no formato:\n" +
                "[Texto do versículo corrigido]. — [Livro] [Capítulo]:[Versículo]\n" +
                "Exemplo para 'tudo posso naquele': Posso todas as coisas naquele que me fortalece. — Filipenses 4:13\n" +
                "Exemplo para 'o senhor e meu pastor': O Senhor é o meu pastor; nada me faltará. — Salmos 23:1\n" +
                "Se for uma citação célebre não-bíblica, retorne no formato: [Citação]. — [Autor]\n" +
                "Retorne APENAS o texto puro sem aspas adicionais no início/fim e sem explicações."

            AiIntentMode.SEMANTIC_REPAIR ->
                "Você é o assistente de clareza do teclado Tessera em português do Brasil. " +
                "Se a frase digitada tiver erros de digitação, palavras sem sentido ou truncadas, reconstrua-a para o sentido correto e natural mais provável. " +
                "Se já estiver correta, devolva a frase inalterada ou com a pontuação polida. " +
                "Retorne ESTRITAMENTE APENAS o texto a ser digitado, sem aspas ou notas."

            AiIntentMode.SMART_ASSISTANT ->
                "Você é o copiloto de escrita inteligente do teclado Tessera. " +
                "Se o texto for um versículo ou citação, complete com a referência correspondente. " +
                "Se for uma frase com erros, corrija o português. " +
                "Se for uma pergunta direta muito curta, responda de forma ultra-concisa. " +
                "Retorne ESTRITAMENTE APENAS o resultado final a ser inserido no campo de texto."
        }
    }

    suspend fun processText(
        rawInput: String,
        isPassiveModeEnabled: Boolean,
        provider: String,
        apiKey: String
    ): AiSuggestionResult? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext null

        val trigger = parseTrigger(rawInput, isPassiveModeEnabled) ?: return@withContext null
        val systemPrompt = buildSystemPrompt(trigger.mode)
        val userPrompt = trigger.userContent

        try {
            val generatedText = when (provider.lowercase()) {
                "gemini" -> callGemini(apiKey, systemPrompt, userPrompt)
                else -> callGroq(apiKey, systemPrompt, userPrompt)
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
                originalText = rawInput,
                suggestionText = cleaned,
                displayLabel = displayLabel,
                mode = trigger.mode,
                charsToDelete = trigger.fullPrefixLength
            )
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun callGroq(apiKey: String, systemPrompt: String, userPrompt: String): String {
        val request = GroqChatRequest(
            model = "llama-3.1-8b-instant",
            messages = listOf(
                GroqMessage(role = "system", content = systemPrompt),
                GroqMessage(role = "user", content = userPrompt)
            ),
            temperature = 0.2,
            maxTokens = 256
        )

        return try {
            val response = GroqClient.service.chatCompletion("Bearer $apiKey", request)
            response.choices?.firstOrNull()?.message?.content?.trim() ?: ""
        } catch (httpEx: retrofit2.HttpException) {
            if (httpEx.code() == 404) {
                // Fallback para Llama 3.3 se 3.1 mudar de ID
                val fallbackReq = request.copy(model = "llama-3.3-70b-versatile")
                val fallbackResp = GroqClient.service.chatCompletion("Bearer $apiKey", fallbackReq)
                fallbackResp.choices?.firstOrNull()?.message?.content?.trim() ?: ""
            } else {
                throw httpEx
            }
        }
    }

    private suspend fun callGemini(apiKey: String, systemPrompt: String, userPrompt: String): String {
        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = userPrompt)))
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        val response = RetrofitClient.service.generateContent(
            model = "gemini-1.5-flash",
            apiKey = apiKey,
            request = request
        )

        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: ""
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
