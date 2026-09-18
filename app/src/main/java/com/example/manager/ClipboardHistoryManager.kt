package com.example.manager

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ClipboardEntry(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    var isPinned: Boolean = false
)

class ClipboardHistoryManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val entries = mutableListOf<ClipboardEntry>()

    companion object {
        private const val PREFS_NAME = "TesseraClipboardHistory"
        private const val KEY_HISTORY = "clipboard_entries"
        private const val MAX_UNPINNED = 25
    }

    init {
        load()
    }

    @Synchronized
    fun addEntry(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return false

        // Se já existe um item idêntico, remove a versão antiga para recolocá-lo no topo
        val existingIndex = entries.indexOfFirst { it.text == trimmed }
        val wasPinned = if (existingIndex != -1) entries[existingIndex].isPinned else false

        if (existingIndex != -1) {
            entries.removeAt(existingIndex)
        }

        val newEntry = ClipboardEntry(
            id = UUID.randomUUID().toString(),
            text = trimmed,
            timestamp = System.currentTimeMillis(),
            isPinned = wasPinned
        )
        entries.add(0, newEntry)

        // Limita os itens não fixados
        trimUnpinned()
        save()
        return true
    }

    @Synchronized
    fun deleteEntry(id: String): Boolean {
        val removed = entries.removeAll { it.id == id }
        if (removed) {
            save()
        }
        return removed
    }

    @Synchronized
    fun togglePin(id: String): Boolean {
        val entry = entries.find { it.id == id } ?: return false
        entry.isPinned = !entry.isPinned
        save()
        return entry.isPinned
    }

    @Synchronized
    fun clearUnpinned(): Int {
        val initialSize = entries.size
        entries.removeAll { !it.isPinned }
        val removedCount = initialSize - entries.size
        if (removedCount > 0) {
            save()
        }
        return removedCount
    }

    @Synchronized
    fun getEntries(): List<ClipboardEntry> {
        val pinned = entries.filter { it.isPinned }.sortedByDescending { it.timestamp }
        val unpinned = entries.filter { !it.isPinned }.sortedByDescending { it.timestamp }
        return pinned + unpinned
    }

    @Synchronized
    fun getPinnedEntries(): List<ClipboardEntry> = entries.filter { it.isPinned }.sortedByDescending { it.timestamp }

    @Synchronized
    fun getRecentEntries(): List<ClipboardEntry> = entries.filter { !it.isPinned }.sortedByDescending { it.timestamp }

    private fun trimUnpinned() {
        val unpinned = entries.filter { !it.isPinned }
        if (unpinned.size > MAX_UNPINNED) {
            val toRemove = unpinned.drop(MAX_UNPINNED).toSet()
            entries.removeAll(toRemove)
        }
    }

    private fun save() {
        try {
            val jsonArray = JSONArray()
            for (entry in entries) {
                val obj = JSONObject().apply {
                    put("id", entry.id)
                    put("text", entry.text)
                    put("timestamp", entry.timestamp)
                    put("isPinned", entry.isPinned)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
        } catch (e: Exception) {
            // Log or ignore
        }
    }

    private fun load() {
        entries.clear()
        val raw = prefs.getString(KEY_HISTORY, null) ?: return
        try {
            val jsonArray = JSONArray(raw)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                entries.add(
                    ClipboardEntry(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        text = obj.optString("text", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isPinned = obj.optBoolean("isPinned", false)
                    )
                )
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }
}
