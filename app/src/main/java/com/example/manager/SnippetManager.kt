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
        
        // 1. Busca exata (case-insensitive)
        for ((k, v) in snippets) {
            if (k.equals(trimmed, ignoreCase = true)) {
                return v
            }
        }
        
        // 2. Busca ignorando o prefixo "!" (ex: usuário digitou "pix" e o atalho é "!pix", ou vice-versa)
        val trimmedNoExcl = trimmed.removePrefix("!")
        for ((k, v) in snippets) {
            val keyNoExcl = k.removePrefix("!")
            if (keyNoExcl.equals(trimmedNoExcl, ignoreCase = true)) {
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
        val isExplicitPrefix = trimmed.startsWith("!")
        val queryLower = trimmed.lowercase()
        val queryNoExcl = queryLower.removePrefix("!")

        for ((k, v) in snippets) {
            val keyLower = k.lowercase()
            val keyNoExcl = keyLower.removePrefix("!")

            // 1. Correspondência exata (máxima prioridade)
            if (keyLower == queryLower || keyNoExcl == queryNoExcl) {
                results.add(0, Pair(k, v))
            } else if (isExplicitPrefix) {
                // Se o usuário digitou '!', busca qualquer snippet que comece com o que ele digitou
                if (keyLower.startsWith(queryLower) || keyNoExcl.startsWith(queryNoExcl)) {
                    results.add(Pair(k, v))
                }
            } else if (queryLower.length >= 2 && keyNoExcl.startsWith(queryNoExcl)) {
                // Se digitou sem '!' com pelo menos 2 caracteres (ex: 'pi' para '!pix')
                results.add(Pair(k, v))
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
