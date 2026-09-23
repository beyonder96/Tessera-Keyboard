package com.example.manager

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

class SnippetManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("StitchPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val PREF_SNIPPETS_JSON = "PREF_SNIPPETS_JSON"
        val DEFAULT_SNIPPETS = mapOf(
            "!pix" to "chave.pix.exemplo@tessera.app",
            "!email" to "contato@exemplo.com",
            "!tel" to "(11) 98765-4321"
        )
    }

    fun getSnippets(): Map<String, String> {
        val rawJson = prefs.getString(PREF_SNIPPETS_JSON, null) ?: return DEFAULT_SNIPPETS
        return try {
            val jsonObject = JSONObject(rawJson)
            val map = mutableMapOf<String, String>()
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = jsonObject.getString(key)
            }
            if (map.isEmpty()) DEFAULT_SNIPPETS else map
        } catch (_: Exception) {
            DEFAULT_SNIPPETS
        }
    }

    fun addSnippet(shortcut: String, expansion: String) {
        val current = getSnippets().toMutableMap()
        val formattedShortcut = if (shortcut.startsWith("!")) shortcut.trim() else "!${shortcut.trim()}"
        current[formattedShortcut] = expansion.trim()
        saveSnippets(current)
    }

    fun removeSnippet(shortcut: String) {
        val current = getSnippets().toMutableMap()
        current.remove(shortcut.trim())
        saveSnippets(current)
    }

    fun findExpansion(shortcut: String): String? {
        val trimmed = shortcut.trim()
        val snippets = getSnippets()
        return snippets[trimmed] ?: snippets[if (trimmed.startsWith("!")) trimmed else "!$trimmed"]
    }

    private fun saveSnippets(snippets: Map<String, String>) {
        val jsonObject = JSONObject()
        for ((k, v) in snippets) {
            jsonObject.put(k, v)
        }
        prefs.edit().putString(PREF_SNIPPETS_JSON, jsonObject.toString()).apply()
    }
}
