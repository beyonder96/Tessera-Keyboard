package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.manager.SnippetManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SnippetManagerTest {

    private lateinit var context: Context
    private lateinit var snippetManager: SnippetManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("StitchPrefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        snippetManager = SnippetManager(context)
    }

    @Test
    fun `default snippets are loaded`() {
        val snippets = snippetManager.getSnippets()
        assertTrue(snippets.containsKey("!pix"))
        assertTrue(snippets.containsKey("!email"))
        assertTrue(snippets.containsKey("!tel"))
        assertEquals("chave.pix.exemplo@tessera.app", snippets["!pix"])
    }

    @Test
    fun `findExpansion requires exclamation mark when shortcut is configured with exclamation mark`() {
        // Exato com '!'
        assertEquals("chave.pix.exemplo@tessera.app", snippetManager.findExpansion("!pix"))
        
        // Sem '!' não deve expandir
        assertNull(snippetManager.findExpansion("pix"))
        
        // Case-insensitive com '!'
        assertEquals("chave.pix.exemplo@tessera.app", snippetManager.findExpansion("!Pix"))
        assertEquals("chave.pix.exemplo@tessera.app", snippetManager.findExpansion("!PIX"))
        
        // Case-insensitive sem '!' não deve expandir
        assertNull(snippetManager.findExpansion("Pix"))
        assertNull(snippetManager.findExpansion("PIX"))

        // Email
        assertEquals("contato@exemplo.com", snippetManager.findExpansion("!email"))
        assertNull(snippetManager.findExpansion("email"))
    }

    @Test
    fun `add and remove custom snippet`() {
        snippetManager.addSnippet("cpf", "123.456.789-00")
        assertNull(snippetManager.findExpansion("cpf"))
        assertEquals("123.456.789-00", snippetManager.findExpansion("!cpf"))

        snippetManager.removeSnippet("cpf")
        assertNull(snippetManager.findExpansion("!cpf"))
    }

    @Test
    fun `findMatchingSnippets returns all snippets on exclamation mark`() {
        val matches = snippetManager.findMatchingSnippets("!")
        assertTrue(matches.size >= 3)
        assertTrue(matches.any { it.first == "!pix" })
        assertTrue(matches.any { it.first == "!email" })
        assertTrue(matches.any { it.first == "!tel" })
    }

    @Test
    fun `findMatchingSnippets returns specific snippet on prefix with exclamation mark`() {
        val matchesP = snippetManager.findMatchingSnippets("!p")
        assertEquals(1, matchesP.size)
        assertEquals("!pix", matchesP[0].first)

        val matchesE = snippetManager.findMatchingSnippets("!e")
        assertEquals(1, matchesE.size)
        assertEquals("!email", matchesE[0].first)
    }

    @Test
    fun `findMatchingSnippets does not return snippets when symbol is not used`() {
        assertTrue(snippetManager.findMatchingSnippets("pix").isEmpty())
        assertTrue(snippetManager.findMatchingSnippets("pi").isEmpty())
        assertTrue(snippetManager.findMatchingSnippets("email").isEmpty())
        assertTrue(snippetManager.findMatchingSnippets("em").isEmpty())
        assertTrue(snippetManager.findMatchingSnippets("tel").isEmpty())
        assertTrue(snippetManager.findMatchingSnippets("te").isEmpty())
    }
}
