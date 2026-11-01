package com.example.ui.keyboard

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import androidx.core.content.ContextCompat

object DynamicThemeResolver {

    fun resolve(context: Context, themeName: String): KeyboardPalette {
        return when (themeName.lowercase()) {
            "light" -> resolveLight()
            "amoled" -> resolveAmoled()
            "nord" -> resolveNord()
            "cyberpunk" -> resolveCyberpunk()
            "incognito" -> resolveIncognito()
            "monet" -> resolveMonet(context)
            else -> resolveDark()
        }
    }

    private fun resolveDark(): KeyboardPalette {
        return KeyboardPalette(
            bgColor = Color.parseColor("#101114"),
            keyBgColor = Color.parseColor("#1B1C22"),
            actionKeyBgColor = Color.parseColor("#22232A"),
            specularBorderColor = Color.parseColor("#1FFFFFFF"),
            shadowColor = Color.parseColor("#40000000"),
            textColor = Color.parseColor("#E6E8EE"),
            inactiveTextColor = Color.parseColor("#717684"),
            glowColor = Color.parseColor("#2C2D35"),
            calloutBgColor = Color.parseColor("#E61E293B"),
            calloutTextColor = Color.WHITE,
            calloutAccentColor = Color.parseColor("#38BDF8")
        )
    }

    private fun resolveLight(): KeyboardPalette {
        return KeyboardPalette(
            bgColor = Color.parseColor("#F7F4EB"),
            keyBgColor = Color.parseColor("#FFFFFF"),
            actionKeyBgColor = Color.parseColor("#E8E4D8"),
            specularBorderColor = Color.parseColor("#4DFFFFFF"),
            shadowColor = Color.parseColor("#1A000000"),
            textColor = Color.parseColor("#1C1917"),
            inactiveTextColor = Color.parseColor("#57534E"),
            glowColor = Color.parseColor("#DDD8CB"),
            calloutBgColor = Color.parseColor("#F2FAF7F4"),
            calloutTextColor = Color.parseColor("#1C1917"),
            calloutAccentColor = Color.parseColor("#D97706")
        )
    }

    private fun resolveAmoled(): KeyboardPalette {
        return KeyboardPalette(
            bgColor = Color.parseColor("#000000"),
            keyBgColor = Color.parseColor("#111113"),
            actionKeyBgColor = Color.parseColor("#18181B"),
            specularBorderColor = Color.parseColor("#14FFFFFF"),
            shadowColor = Color.TRANSPARENT,
            textColor = Color.parseColor("#FAFAFA"),
            inactiveTextColor = Color.parseColor("#52525B"),
            glowColor = Color.parseColor("#222226"),
            calloutBgColor = Color.parseColor("#F0111113"),
            calloutTextColor = Color.WHITE,
            calloutAccentColor = Color.parseColor("#38BDF8")
        )
    }

    private fun resolveNord(): KeyboardPalette {
        return KeyboardPalette(
            bgColor = Color.parseColor("#242933"),
            keyBgColor = Color.parseColor("#2E3440"),
            actionKeyBgColor = Color.parseColor("#353C4A"),
            specularBorderColor = Color.parseColor("#24FFFFFF"),
            shadowColor = Color.parseColor("#33000000"),
            textColor = Color.parseColor("#ECEFF4"),
            inactiveTextColor = Color.parseColor("#8892B0"),
            glowColor = Color.parseColor("#3B4252"),
            calloutBgColor = Color.parseColor("#F02E3440"),
            calloutTextColor = Color.parseColor("#ECEFF4"),
            calloutAccentColor = Color.parseColor("#88C0D0")
        )
    }

    private fun resolveCyberpunk(): KeyboardPalette {
        return KeyboardPalette(
            bgColor = Color.parseColor("#0B0812"),
            keyBgColor = Color.parseColor("#141021"),
            actionKeyBgColor = Color.parseColor("#1E162E"),
            specularBorderColor = Color.parseColor("#2600F5FF"),
            shadowColor = Color.parseColor("#4D000000"),
            textColor = Color.parseColor("#F0ECF8"),
            inactiveTextColor = Color.parseColor("#7D7094"),
            glowColor = Color.parseColor("#221630"),
            calloutBgColor = Color.parseColor("#F21E162E"),
            calloutTextColor = Color.parseColor("#00F5FF"),
            calloutAccentColor = Color.parseColor("#FF007F")
        )
    }

    private fun resolveIncognito(): KeyboardPalette {
        return KeyboardPalette(
            bgColor = Color.parseColor("#0D0B10"),
            keyBgColor = Color.parseColor("#16131C"),
            actionKeyBgColor = Color.parseColor("#201A29"),
            specularBorderColor = Color.parseColor("#26C084FC"),
            shadowColor = Color.parseColor("#40000000"),
            textColor = Color.parseColor("#EDE9F2"),
            inactiveTextColor = Color.parseColor("#786E85"),
            glowColor = Color.parseColor("#251E30"),
            calloutBgColor = Color.parseColor("#F216131C"),
            calloutTextColor = Color.WHITE,
            calloutAccentColor = Color.parseColor("#C084FC")
        )
    }

    private fun resolveMonet(context: Context): KeyboardPalette {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                val bgRes = if (isDark) android.R.color.system_neutral1_900 else android.R.color.system_neutral1_50
                val keyRes = if (isDark) android.R.color.system_neutral1_800 else android.R.color.system_neutral1_0
                val actionRes = if (isDark) android.R.color.system_neutral2_700 else android.R.color.system_neutral2_100
                val textRes = if (isDark) android.R.color.system_neutral1_100 else android.R.color.system_neutral1_900
                val inactiveRes = if (isDark) android.R.color.system_neutral2_400 else android.R.color.system_neutral2_600
                val accentRes = if (isDark) android.R.color.system_accent1_200 else android.R.color.system_accent1_600

                val bg = ContextCompat.getColor(context, bgRes)
                val key = ContextCompat.getColor(context, keyRes)
                val action = ContextCompat.getColor(context, actionRes)
                val text = ContextCompat.getColor(context, textRes)
                val inactive = ContextCompat.getColor(context, inactiveRes)
                val accent = ContextCompat.getColor(context, accentRes)

                return KeyboardPalette(
                    bgColor = bg,
                    keyBgColor = key,
                    actionKeyBgColor = action,
                    specularBorderColor = if (isDark) Color.parseColor("#20FFFFFF") else Color.parseColor("#33FFFFFF"),
                    shadowColor = if (isDark) Color.parseColor("#40000000") else Color.parseColor("#1A000000"),
                    textColor = text,
                    inactiveTextColor = inactive,
                    glowColor = action,
                    calloutBgColor = action,
                    calloutTextColor = text,
                    calloutAccentColor = accent
                )
            } catch (_: Throwable) {
                // Fallback to static Material You palette if OEM restricts color resources
            }
        }
        return KeyboardPalette(
            bgColor = Color.parseColor("#191C1E"),
            keyBgColor = Color.parseColor("#222629"),
            actionKeyBgColor = Color.parseColor("#2C3034"),
            specularBorderColor = Color.parseColor("#1FFFFFFF"),
            shadowColor = Color.parseColor("#33000000"),
            textColor = Color.parseColor("#E1E3E5"),
            inactiveTextColor = Color.parseColor("#8A9196"),
            glowColor = Color.parseColor("#2D3133"),
            calloutBgColor = Color.parseColor("#E62C3034"),
            calloutTextColor = Color.WHITE,
            calloutAccentColor = Color.parseColor("#80D4F7")
        )
    }
}
