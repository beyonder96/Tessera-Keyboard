package com.example

import com.example.engine.EmojiDictionary
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiDictionaryTest {

    @Test
    fun testEmptySearchReturnsEmpty() {
        val results = EmojiDictionary.search("")
        assertTrue(results.isEmpty())
    }

    @Test
    fun testSearchSmileys() {
        val results = EmojiDictionary.search("sorriso")
        assertTrue(results.isNotEmpty())
        assertTrue(results.contains("😀") || results.contains("😃") || results.contains("😄"))
    }

    @Test
    fun testSearchLoveAccents() {
        val resultsAccented = EmojiDictionary.search("coração")
        val resultsUnaccented = EmojiDictionary.search("coracao")
        assertTrue(resultsAccented.isNotEmpty())
        assertTrue(resultsUnaccented.isNotEmpty())
        assertTrue(resultsAccented.contains("❤️") || resultsAccented.contains("😍") || resultsAccented.contains("🥰"))
        assertTrue(resultsUnaccented.contains("❤️") || resultsUnaccented.contains("😍") || resultsUnaccented.contains("🥰"))
    }

    @Test
    fun testSearchFood() {
        val pizza = EmojiDictionary.search("pizza")
        assertTrue(pizza.contains("🍕"))

        val cafe = EmojiDictionary.search("cafe")
        assertTrue(cafe.contains("☕"))
    }
}
