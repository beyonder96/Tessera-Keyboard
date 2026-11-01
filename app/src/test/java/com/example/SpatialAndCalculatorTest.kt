package com.example

import android.graphics.RectF
import com.example.engine.InlineCalculatorEngine
import com.example.engine.SpatialHitboxResolver
import com.example.manager.AcousticSoundEngine
import com.example.ui.keyboard.KeyAction
import com.example.ui.keyboard.KeyCell
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SpatialAndCalculatorTest {

    @Test
    fun testSpatialHitboxDirectCoreHit() {
        val cellA = KeyCell(
            action = KeyAction.CharKey("a"),
            primaryLabel = "a",
            logicalRect = RectF(0f, 0f, 100f, 100f),
            visualRect = RectF(0f, 0f, 100f, 100f)
        )
        val cellB = KeyCell(
            action = KeyAction.CharKey("b"),
            primaryLabel = "b",
            logicalRect = RectF(100f, 0f, 200f, 100f),
            visualRect = RectF(100f, 0f, 200f, 100f)
        )
        val keys = listOf(cellA, cellB)

        // Touch dead center of key A (50, 50)
        val resolved = SpatialHitboxResolver.resolveKey(50f, 50f, keys, validNextCharsMask = 0, isAdaptiveEnabled = true)
        assertEquals(cellA, resolved)
    }

    @Test
    fun testSpatialHitboxVoronoiBoundaryWithLexicalBiasing() {
        // Two adjacent keys A [0..100] and B [100..200]
        val cellA = KeyCell(
            action = KeyAction.CharKey("a"),
            primaryLabel = "a",
            logicalRect = RectF(0f, 0f, 100f, 100f),
            visualRect = RectF(0f, 0f, 100f, 100f)
        )
        val cellB = KeyCell(
            action = KeyAction.CharKey("b"),
            primaryLabel = "b",
            logicalRect = RectF(100f, 0f, 200f, 100f),
            visualRect = RectF(100f, 0f, 200f, 100f)
        )
        val keys = listOf(cellA, cellB)

        // Touch slightly towards B at x=104 (closer to B without biasing)
        val unBiased = SpatialHitboxResolver.resolveKey(104f, 50f, keys, validNextCharsMask = 0, isAdaptiveEnabled = true)
        assertEquals(cellB, unBiased)

        // Now give 'a' lexical priority in validNextMask (1 shl ('a' - 'a') = 1)
        val maskA = 1 shl ('a' - 'a')
        val biased = SpatialHitboxResolver.resolveKey(104f, 50f, keys, validNextCharsMask = maskA, isAdaptiveEnabled = true)
        // With 16% distance reduction, A wins the boundary contention!
        assertEquals(cellA, biased)
    }

    @Test
    fun testInlineCalculatorBasicArithmetic() {
        val mult = InlineCalculatorEngine.tryEvaluate("25*4")
        assertNotNull(mult)
        assertEquals("100", mult?.formatted)

        val div = InlineCalculatorEngine.tryEvaluate("100/4")
        assertNotNull(div)
        assertEquals("25", div?.formatted)

        val add = InlineCalculatorEngine.tryEvaluate("12,5+7,5")
        assertNotNull(add)
        assertEquals("20", add?.formatted)

        val paren = InlineCalculatorEngine.tryEvaluate("(10+5)*2")
        assertNotNull(paren)
        assertEquals("30", paren?.formatted)
    }

    @Test
    fun testInlineCalculatorPercentagesAndDecimals() {
        val pct = InlineCalculatorEngine.tryEvaluate("50*10%")
        assertNotNull(pct)
        assertEquals("5", pct?.formatted)

        val dec = InlineCalculatorEngine.tryEvaluate("1,25*2")
        assertNotNull(dec)
        assertEquals("2,5", dec?.formatted)
    }

    @Test
    fun testInlineCalculatorIgnoresNonMath() {
        assertNull(InlineCalculatorEngine.tryEvaluate("olá tudo bem?"))
        assertNull(InlineCalculatorEngine.tryEvaluate("teste 123"))
        assertNull(InlineCalculatorEngine.tryEvaluate("a+b"))
        assertNull(InlineCalculatorEngine.tryEvaluate("1+"))
    }

    @Test
    fun testAcousticSoundEngineWaveformSynthesis() {
        val header = AcousticSoundEngine.buildWavHeader(1000)
        assertEquals(44, header.size)
        // Verify "RIFF" and "WAVE" magic bytes
        assertEquals('R'.code.toByte(), header[0])
        assertEquals('I'.code.toByte(), header[1])
        assertEquals('F'.code.toByte(), header[2])
        assertEquals('F'.code.toByte(), header[3])
        assertEquals('W'.code.toByte(), header[8])
        assertEquals('A'.code.toByte(), header[9])
        assertEquals('V'.code.toByte(), header[10])
        assertEquals('E'.code.toByte(), header[11])

        val thock = AcousticSoundEngine.synthesizeThockStd()
        assertTrue(thock.size > 44)

        val crisp = AcousticSoundEngine.synthesizeCrispStd()
        assertTrue(crisp.size > 44)

        val typewriter = AcousticSoundEngine.synthesizeTypewriterStd()
        assertTrue(typewriter.size > 44)
    }
}
