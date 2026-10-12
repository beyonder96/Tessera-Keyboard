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
        return try {
            if (!prefs.contains(PREF_SNIPPETS_JSON)) {
                saveSnippets(DEFAULT_SNIPPETS)
                return DEFAULT_SNIPPETS
            }
            val rawJson = prefs.getString(PREF_SNIPPETS_JSON, "{}") ?: "{}"
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
        val trimmedKey = shortcut.trim()
        val formattedShortcut = if (trimmedKey.startsWith("!")) trimmedKey else "!$trimmedKey"
        current[formattedShortcut] = expansion.trim()
        current.remove(trimmedKey.removePrefix("!"))
        saveSnippets(current)
    }

    fun removeSnippet(shortcut: String) {
        val current = getSnippets().toMutableMap()
        val trimmed = shortcut.trim()
        current.remove(trimmed)
        current.remove(if (trimmed.startsWith("!")) trimmed.removePrefix("!") else "!$trimmed")
        saveSnippets(current)
    }

    fun findExpansion(shortcut: String): String? {
        val trimmed = shortcut.trim()
        if (trimmed.isEmpty()) return null
        val snippets = getSnippets()
        
        // Busca exata (case-insensitive) - atalhos com '!' só expandem se digitados com '!'
        for ((k, v) in snippets) {
            if (k.equals(trimmed, ignoreCase = true)) {
                return v
            }
        }
        
        return null
    }

    fun findMatchingSnippets(query: String): List<Pair<String, String>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        val snippets = getSnippets()
        if (snippets.isEmpty()) return emptyList()

        val results = mutableListOf<Pair<String, String>>()
        val queryLower = trimmed.lowercase()
        val isExplicitPrefix = trimmed.startsWith("!")

        for ((k, v) in snippets) {
            val keyLower = k.lowercase()

            // Atalhos iniciados com '!' só devem ser exibidos se o usuário utilizou '!'
            if (keyLower.startsWith("!")) {
                if (!isExplicitPrefix) {
                    continue
                }
                if (keyLower == queryLower) {
                    results.add(0, Pair(k, v))
                } else if (keyLower.startsWith(queryLower)) {
                    results.add(Pair(k, v))
                }
            } else {
                if (isExplicitPrefix) {
                    continue
                }
                if (keyLower == queryLower) {
                    results.add(0, Pair(k, v))
                } else if (queryLower.length >= 2 && keyLower.startsWith(queryLower)) {
                    results.add(Pair(k, v))
                }
            }
        }

        return results.distinctBy { it.first }
    }

    private fun saveSnippets(snippets: Map<String, String>) {
        val jsonObject = JSONObject()
        for ((k, v) in snippets) {
            jsonObject.put(k, v)
        }
        prefs.edit().putString(PREF_SNIPPETS_JSON, jsonObject.toString()).apply()
    }
}
