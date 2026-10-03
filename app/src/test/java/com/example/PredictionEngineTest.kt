package com.example

import com.example.engine.PredictionEngine
import com.example.engine.TrieDictionary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

    @Test
    fun testSwipePredictions() {
        val testEngine = PredictionEngine()
        val predictions = testEngine.getSwipePredictions("obrigado")
        assertTrue(predictions.contains("obrigado"))

        val bomPredictions = testEngine.getSwipePredictions("bom")
        assertTrue(bomPredictions.contains("bom"))

        val vocePredictions = testEngine.getSwipePredictions("voce")
        assertTrue(vocePredictions.contains("você") || vocePredictions.contains("voce"))

        // Teste de letras duplicadas no gesto (usuário passa apenas 1 vez pela tecla)
        val carroPredictions = testEngine.getSwipePredictions("caro")
        assertTrue(carroPredictions.contains("carro") || carroPredictions.contains("claro"))

        val issoPredictions = testEngine.getSwipePredictions("iso")
        assertTrue(issoPredictions.contains("isso"))
    }

    @Test
    fun testCorrectTextLocally() {
        val testEngine = PredictionEngine()
        val input = "ola como vc ta hj? espero que esteja td bem"
        val corrected = testEngine.correctTextLocally(input)
        
        // Verifica expansão de abreviações e restauração de maiúscula inicial
        assertTrue(corrected.startsWith("Olá"))
        assertTrue(corrected.contains("você"))
        assertTrue(corrected.contains("hoje?"))
        assertTrue(corrected.contains("tudo"))
    }

    @Test
    fun testLearningDisabledWhenIncognito() {
        val testEngine = PredictionEngine()
        testEngine.isLearningEnabled = false
        testEngine.learnWord("palavrasecreta")
        val results = testEngine.getPredictions("palavrasec")
        assertTrue(!results.contains("palavrasecreta"))
    }

    @Test
    fun testLearningEnabledByDefault() {
        val testEngine = PredictionEngine()
        assertTrue(testEngine.isLearningEnabled)
        testEngine.learnWord("palavranova")
        val results = testEngine.getPredictions("palavrano")
        assertTrue(results.contains("palavranova"))
    }

    @Test
    fun testTopAutocorrectOnSpace() {
        val testEngine = PredictionEngine()

        // 1. Abreviações
        assertEquals("você", testEngine.getTopAutocorrect("vc"))
        assertEquals("também", testEngine.getTopAutocorrect("tbm"))
        assertEquals("porque", testEngine.getTopAutocorrect("pq"))

        // 2. Restauração de acentos
        assertEquals("estão", testEngine.getTopAutocorrect("estao"))
        assertEquals("não", testEngine.getTopAutocorrect("nao"))
        assertEquals("você", testEngine.getTopAutocorrect("voce"))

        // 3. Typos QWERTY
        val typoCorrection = testEngine.getTopAutocorrect("obrigaso")
        assertEquals("obrigado", typoCorrection)

        // 4. Palavras corretas existentes não devem ser sequestradas
        assertNull(testEngine.getTopAutocorrect("casa"))
        assertNull(testEngine.getTopAutocorrect("carro"))
        assertNull(testEngine.getTopAutocorrect("teclado"))
    }

    @Test
    fun testFuzzyOmissionCorrection() {
        val trie = TrieDictionary()
        trie.insert("obrigado", 240)
        trie.insert("computador", 220)
        trie.insert("falando", 210)

        // Omissão de 1 letra (usuário comeu uma letra digitando rápido)
        val obrigdoSuggestions = trie.findFuzzySuggestions("obrigdo")
        assertTrue(obrigdoSuggestions.contains("obrigado"))

        val flandoSuggestions = trie.findFuzzySuggestions("flando")
        assertTrue(flandoSuggestions.contains("falando"))
    }

    @Test
    fun testContinuousSpatialGestureRecognition() {
        val testEngine = PredictionEngine()

        // Simula posições geométricas de teclas na tela (320x240 dip) com KeyRect Kotlin puro
        val keyBounds = mutableMapOf<Char, com.example.engine.KeyRect>()
        // Linha 1: q w e r t y u i o p
        val r1 = "qwertyuiop"
        for (i in r1.indices) {
            val left = i * 40
            keyBounds[r1[i]] = com.example.engine.KeyRect(left, 0, left + 40, 50)
        }
        // Linha 2: a s d f g h j k l ç
        val r2 = "asdfghjklç"
        for (i in r2.indices) {
            val left = i * 40
            keyBounds[r2[i]] = com.example.engine.KeyRect(left, 50, left + 40, 100)
        }
        // Linha 3: z x c v b n m
        val r3 = "zxcvbnm"
        for (i in r3.indices) {
            val left = i * 40 + 40
            keyBounds[r3[i]] = com.example.engine.KeyRect(left, 100, left + 40, 150)
        }

        // Trajetória simulada para "bom" ('b' -> sobe para 'o' -> desce para 'm')
        val bCenter = keyBounds['b']!!
        val oCenter = keyBounds['o']!!
        val mCenter = keyBounds['m']!!

        val bX = bCenter.cx
        val bY = bCenter.cy
        val oX = oCenter.cx
        val oY = oCenter.cy
        val mX = mCenter.cx
        val mY = mCenter.cy

        val trajectory = listOf(
            com.example.engine.TrajectoryPoint(bX, bY, 1000L),
            com.example.engine.TrajectoryPoint((bX + oX) / 2f, (bY + oY) / 2f, 1050L),
            com.example.engine.TrajectoryPoint(oX, oY, 1100L), // Quina em 'o'
            com.example.engine.TrajectoryPoint((oX + mX) / 2f, (oY + mY) / 2f, 1150L),
            com.example.engine.TrajectoryPoint(mX, mY, 1200L)
        )

        val candidates = testEngine.getSwipePredictionsWithKeyRects(trajectory, keyBounds)
        assertTrue(candidates.contains("bom") || candidates.contains("bem"))
    }
}

