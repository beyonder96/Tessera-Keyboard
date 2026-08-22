package com.example

import com.example.engine.PredictionEngine
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
}
