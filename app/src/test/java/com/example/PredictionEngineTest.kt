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
}
