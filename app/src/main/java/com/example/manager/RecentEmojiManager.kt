package com.example.manager

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages recently used emojis for the Tessera Keyboard.
 * Stores up to 24 most recently used emojis in SharedPreferences.
 * Respects incognito and sensitive/password fields by omitting persistence.
 */
class RecentEmojiManager(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("tessera_emoji_prefs", Context.MODE_PRIVATE)
    }

    private val defaultEmojis = listOf(
        "😀", "😂", "🥰", "👍", "❤️", "🔥", "🙏", "✨",
        "🎉", "👀", "😎", "🤔", "👏", "🙌", "💯", "🚀",
        "😍", "😭", "😊", "🥳", "💪", "💡", "👌", "⭐"
    )

    fun getRecentEmojis(): List<String> {
        val raw = prefs.getString(KEY_RECENTS, null)
        if (raw.isNullOrBlank()) {
            return defaultEmojis
        }
        val items = raw.split(",").filter { it.isNotBlank() }
        return if (items.isNotEmpty()) items else defaultEmojis
    }

    fun recordEmojiUsage(emoji: String, isIncognitoOrSensitive: Boolean = false) {
        if (isIncognitoOrSensitive || emoji.isBlank()) return

        val currentList = getRecentEmojis().toMutableList()
        currentList.remove(emoji)
        currentList.add(0, emoji)
        val cappedList = currentList.take(MAX_RECENTS)

        prefs.edit().putString(KEY_RECENTS, cappedList.joinToString(",")).apply()
    }

    companion object {
        private const val KEY_RECENTS = "recent_emojis_list"
        private const val MAX_RECENTS = 24
    }
}
