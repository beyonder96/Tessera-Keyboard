package com.example

import com.example.engine.PredictionEngine
import com.example.engine.TrieDictionary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PredictionEngineTest {

    private val engine = PredictionEngine()

    @Test
    fun testEmptyReturnsDefaults() {
        val predictions = engine.getPredictions("")
        assertEquals(listOf("eu", "o", "que"), predictions)
    }

    @Test
    fun testAccentInsensitivePrediction() {
        val predictions = engine.getPredictions("obrig")
        assertTrue(predictions.contains("obrigado") || predictions.contains("obrigada"))
    }

    @Test
    fun testPrefixMatching() {
        val predictions = engine.getPredictions("precis")
        assertTrue(predictions.contains("preciso") || predictions.contains("precisa"))
    }

    @Test
    fun testMaxThreeSuggestions() {
        val predictions = engine.getPredictions("co")
        assertTrue(predictions.size <= 3)
    }

    @Test
    fun testTrieDirectSuggestions() {
        val trie = TrieDictionary()
        trie.insert("computador", frequency = 50)
        trie.insert("companhia", frequency = 100)
        trie.insert("comprar", frequency = 80)

        val results = trie.findTopSuggestions("comp", maxCount = 3)
        assertEquals("companhia", results[0]) // highest frequency
        assertEquals("comprar", results[1])
        assertEquals("computador", results[2])
    }

    @Test
    fun testTrieAccentHandling() {
        val trie = TrieDictionary()
        trie.insert("você", frequency = 100)
        trie.insert("água", frequency = 80)

        val voceResults = trie.findTopSuggestions("voce", maxCount = 1)
        assertEquals(listOf("você"), voceResults)

        val aguaResults = trie.findTopSuggestions("agu", maxCount = 1)
        assertEquals(listOf("água"), aguaResults)
    }

    @Test
    fun testLearnWordPrioritization() {
        val testEngine = PredictionEngine()
        testEngine.learnWord("compartilhamento")
        val predictions = testEngine.getPredictions("compart")
        assertTrue(predictions.contains("compartilhamento"))
    }

    @Test
    fun testAutoAccentuation() {
        val testEngine = PredictionEngine()
        val naoPreds = testEngine.getPredictions("nao")
        assertEquals("não", naoPreds.firstOrNull())

        val vocePreds = testEngine.getPredictions("voce")
        assertEquals("você", vocePreds.firstOrNull())

        val atePreds = testEngine.getPredictions("ate")
        assertEquals("até", atePreds.firstOrNull())

        val tambemPreds = testEngine.getPredictions("tambem")
        assertEquals("também", tambemPreds.firstOrNull())
    }

    @Test
    fun testCapitalizationPreservation() {
        val testEngine = PredictionEngine()
        val capNao = testEngine.getPredictions("Nao")
        assertEquals("Não", capNao.firstOrNull())

        val upperVoce = testEngine.getPredictions("VOCE")
        assertEquals("VOCÊ", upperVoce.firstOrNull())
    }

    @Test
    fun testFuzzyTypoCorrectionRudoToTudo() {
        val testEngine = PredictionEngine()
        val preds = testEngine.getPredictions("rudo")
        // "r" is adjacent to "t" in QWERTY, so "rudo" must correct to "tudo"
        assertEquals("tudo", preds.firstOrNull())
        assertTrue(preds.contains("rudo")) // literal typed word is kept as alternative
    }

    @Test
    fun testFuzzyTypoTransposition() {
        val testEngine = PredictionEngine()
        val preds = testEngine.getPredictions("tduo")
        assertEquals("tudo", preds.firstOrNull())
    }

    @Test
    fun testPrewarmedSingleLetters() {
        val testEngine = PredictionEngine()
        for (c in 'a'..'z') {
            val preds = testEngine.getPredictions(c.toString())
            assertTrue(preds.isNotEmpty())
        }
    }

    @Test
    fun testCacheConsistencyAfterLearnWord() {
        val testEngine = PredictionEngine()
        testEngine.getPredictions("xyz")
        testEngine.learnWord("xyzw")
        val preds = testEngine.getPredictions("xyz")
        assertTrue(preds.contains("xyzw"))
    }

    @Test
    fun testBFSBranchingShortWordsFirst() {
        val trie = TrieDictionary()
        // Deep word on branch 'a'
        trie.insert("coadunado", frequency = 50)
        trie.insert("coadunamento", frequency = 50)
        // Short natural words on branches 'm' and 'i'
        trie.insert("como", frequency = 50)
        trie.insert("coisa", frequency = 50)

        val suggestions = trie.findTopSuggestions("co", maxCount = 3)
        assertTrue(suggestions.contains("como"))
        assertTrue(suggestions.contains("coisa"))
    }

    @Test
    fun testAccentedWordPriorityOnExactLengthMatch() {
        val testEngine = PredictionEngine()
        val estao = testEngine.getPredictions("estao")
        assertEquals("estão", estao.firstOrNull())

        val ja = testEngine.getPredictions("ja")
        assertEquals("já", ja.firstOrNull())
    }

    @Test
    fun testCommonWordsDoNotReceiveBogusAccents() {
        val testEngine = PredictionEngine()
        assertEquals("que", testEngine.getPredictions("que").firstOrNull())
        assertEquals("de", testEngine.getPredictions("de").firstOrNull())
        assertEquals("do", testEngine.getPredictions("do").firstOrNull())
        assertEquals("no", testEngine.getPredictions("no").firstOrNull())
        assertEquals("para", testEngine.getPredictions("para").firstOrNull())
        assertEquals("passe", testEngine.getPredictions("passe").firstOrNull())
        assertEquals("demonstra", testEngine.getPredictions("demonstra").firstOrNull())
        assertEquals("na", testEngine.getPredictions("na").firstOrNull())
    }

    @Test
    fun testMultiErrorProximityCorrectionDigitando() {
        val testEngine = PredictionEngine()
        val preds = testEngine.getPredictions("dkgitsndk")
        assertEquals("digitando", preds.firstOrNull())
    }

    @Test
    fun testContextualBigramPrediction() {
        val testEngine = PredictionEngine()
        val nextAfterMuito = testEngine.getPredictions("", previousWord = "muito")
        assertTrue(nextAfterMuito.contains("obrigado") || nextAfterMuito.contains("bem"))

        val verAfterVoce = testEngine.getPredictions("ve", previousWord = "você")
        assertEquals("ver", verAfterVoce.firstOrNull())

        val veAfterEle = testEngine.getPredictions("ve", previousWord = "ele")
        assertEquals("vê", veAfterEle.firstOrNull())

        val nextAfterBom = testEngine.getPredictions("", previousWord = "bom")
        assertTrue(nextAfterBom.contains("dia"))

        val nextAfterPor = testEngine.getPredictions("", previousWord = "por")
        assertTrue(nextAfterPor.contains("favor"))
    }

    @Test
    fun testModernAccentRestorations() {
        val testEngine = PredictionEngine()
        assertEquals("água", testEngine.getPredictions("agua").firstOrNull())
        assertEquals("opção", testEngine.getPredictions("opcao").firstOrNull())
        assertEquals("fácil", testEngine.getPredictions("facil").firstOrNull())
        assertEquals("dúvida", testEngine.getPredictions("duvida").firstOrNull())
    }
}
