package com.example

import com.example.engine.AiIntentMode
import com.example.engine.ParticipativeAiEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ParticipativeAiEngineTest {

    @Test
    fun parseTrigger_detectsGrammarCorrectionTags() {
        val testCases = listOf(
            "@corrigir nois vai no mercado" to "nois vai no mercado",
            "@correcao nois vai no mercado" to "nois vai no mercado",
            "@correção nois vai no mercado" to "nois vai no mercado",
            "@c nois vai no mercado" to "nois vai no mercado",
            "@fix nois vai no mercado" to "nois vai no mercado",
            "/corrigir nois vai no mercado" to "nois vai no mercado"
        )

        for ((input, expectedContent) in testCases) {
            val parsed = ParticipativeAiEngine.parseTrigger(input, isPassiveModeEnabled = false)
            assertNotNull("Failed to parse: $input", parsed)
            assertEquals(AiIntentMode.GRAMMAR_CORRECTION, parsed?.mode)
            assertEquals(expectedContent, parsed?.userContent)
            assertEquals(input.length, parsed?.fullPrefixLength)
        }
    }

    @Test
    fun parseTrigger_detectsBibleVerseTags() {
        val testCases = listOf(
            "@versiculo tudo posso naquele que me fortalece" to "tudo posso naquele que me fortalece",
            "@versículo tudo posso naquele que me fortalece" to "tudo posso naquele que me fortalece",
            "@v o senhor e meu pastor" to "o senhor e meu pastor",
            "@biblia porque Deus amou o mundo" to "porque Deus amou o mundo",
            "/versiculo tudo posso naquele" to "tudo posso naquele"
        )

        for ((input, expectedContent) in testCases) {
            val parsed = ParticipativeAiEngine.parseTrigger(input, isPassiveModeEnabled = false)
            assertNotNull("Failed to parse: $input", parsed)
            assertEquals(AiIntentMode.BIBLE_VERSE_LOOKUP, parsed?.mode)
            assertEquals(expectedContent, parsed?.userContent)
            assertEquals(input.length, parsed?.fullPrefixLength)
        }
    }

    @Test
    fun parseTrigger_detectsGenericAiTag() {
        val parsed = ParticipativeAiEngine.parseTrigger("@ia qual a capital da França", isPassiveModeEnabled = false)
        assertNotNull(parsed)
        assertEquals(AiIntentMode.SMART_ASSISTANT, parsed?.mode)
        assertEquals("qual a capital da França", parsed?.userContent)
    }

    @Test
    fun parseTrigger_detectsMidSentenceTags() {
        val input = "olá meu amigo @versiculo posso todas as coisas"
        val parsed = ParticipativeAiEngine.parseTrigger(input, isPassiveModeEnabled = false)
        assertNotNull(parsed)
        assertEquals(AiIntentMode.BIBLE_VERSE_LOOKUP, parsed?.mode)
        assertEquals("posso todas as coisas", parsed?.userContent)
        assertEquals("@versiculo posso todas as coisas".length, parsed?.fullPrefixLength)
    }

    @Test
    fun parseTrigger_respectsPassiveMode() {
        val normalText = "ontem nos fomos na praia de manha"
        
        // Com modo passivo desligado: não deve disparar
        val withoutPassive = ParticipativeAiEngine.parseTrigger(normalText, isPassiveModeEnabled = false)
        assertNull(withoutPassive)

        // Com modo passivo ligado: deve identificar como SEMANTIC_REPAIR
        val withPassive = ParticipativeAiEngine.parseTrigger(normalText, isPassiveModeEnabled = true)
        assertNotNull(withPassive)
        assertEquals(AiIntentMode.SEMANTIC_REPAIR, withPassive?.mode)
        assertEquals(normalText, withPassive?.userContent)
    }

    @Test
    fun systemPrompts_enforceExactVerseFormatAndNoQuotes() {
        val versePrompt = ParticipativeAiEngine.buildSystemPrompt(AiIntentMode.BIBLE_VERSE_LOOKUP)
        assertTrue(versePrompt.contains("[Texto do versículo corrigido]. — [Livro] [Capítulo]:[Versículo]"))
        assertTrue(versePrompt.contains("sem aspas"))

        val grammarPrompt = ParticipativeAiEngine.buildSystemPrompt(AiIntentMode.GRAMMAR_CORRECTION)
        assertTrue(grammarPrompt.contains("português do Brasil"))
        assertTrue(grammarPrompt.contains("ESTRITAMENTE APENAS o texto final corrigido"))
    }

    @Test
    fun groqApi_serializationAndDeserializationWorks() {
        val moshi = com.squareup.moshi.Moshi.Builder().build()
        val reqAdapter = moshi.adapter(com.example.api.GroqChatRequest::class.java)
        val req = com.example.api.GroqChatRequest(
            messages = listOf(com.example.api.GroqMessage(role = "user", content = "teste"))
        )
        val json = reqAdapter.toJson(req)
        assertTrue(json.contains("llama-3.1-8b-instant"))
        assertTrue(json.contains("teste"))

        val respAdapter = moshi.adapter(com.example.api.GroqChatResponse::class.java)
        val sampleJson = """
            {
              "id": "chatcmpl-123",
              "choices": [
                {
                  "index": 0,
                  "message": {
                    "role": "assistant",
                    "content": "Posso todas as coisas naquele que me fortalece. — Filipenses 4:13"
                  },
                  "finish_reason": "stop"
                }
              ]
            }
        """.trimIndent()
        val parsedResp = respAdapter.fromJson(sampleJson)
        assertNotNull(parsedResp)
        assertEquals("Posso todas as coisas naquele que me fortalece. — Filipenses 4:13", parsedResp?.choices?.firstOrNull()?.message?.content)
    }
}

