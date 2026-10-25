package com.example.ui.widget

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KeyboardLivePreview(
    themeName: String,
    keyShape: String = "flat",
    keyHints: Boolean = true,
    scale: Float = 1.0f,
    numberRow: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Cores calculadas dinamicamente com base no tema editorial
    val (bgColor, glowColor, keyBg, actionKeyBg, textColor, inactiveColor) = when (themeName) {
        "Light" -> HexThemeColors(
            bg = Color(0xFFF7F4EB),
            glow = Color(0xFFDDD8CB),
            keyBg = Color(0xFFFFFFFF),
            actionKeyBg = Color(0xFFE8E4D8),
            text = Color(0xFF1C1917),
            inactive = Color(0xFF57534E)
        )
        "Amoled" -> HexThemeColors(
            bg = Color(0xFF000000),
            glow = Color(0xFF222226),
            keyBg = Color(0xFF111113),
            actionKeyBg = Color(0xFF18181B),
            text = Color(0xFFFAFAFA),
            inactive = Color(0xFF52525B)
        )
        "Nord" -> HexThemeColors(
            bg = Color(0xFF242933),
            glow = Color(0xFF3B4252),
            keyBg = Color(0xFF2E3440),
            actionKeyBg = Color(0xFF353C4A),
            text = Color(0xFFECEFF4),
            inactive = Color(0xFF8892B0)
        )
        "Monet" -> HexThemeColors(
            bg = Color(0xFF191C1E),
            glow = Color(0xFF2D3133),
            keyBg = Color(0xFF222629),
            actionKeyBg = Color(0xFF2C3034),
            text = Color(0xFFE1E3E5),
            inactive = Color(0xFF8A9196)
        )
        "Incognito" -> HexThemeColors(
            bg = Color(0xFF0D0B10),
            glow = Color(0xFF251E30),
            keyBg = Color(0xFF16131C),
            actionKeyBg = Color(0xFF201A29),
            text = Color(0xFFEDE9F2),
            inactive = Color(0xFF786E85)
        )
        else -> HexThemeColors( // Obsidian Editorial Dark Padrão
            bg = Color(0xFF101114),
            glow = Color(0xFF2C2D35),
            keyBg = Color(0xFF1B1C22),
            actionKeyBg = Color(0xFF22232A),
            text = Color(0xFFE6E8EE),
            inactive = Color(0xFF717684)
        )
    }

    val animatedBg by animateColorAsState(bgColor, tween(250), label = "bgAnim")
    val animatedText by animateColorAsState(textColor, tween(250), label = "textAnim")
    val animatedKeyBg by animateColorAsState(keyBg, tween(250), label = "keyBgAnim")
    val animatedActionKeyBg by animateColorAsState(actionKeyBg, tween(250), label = "actionKeyBgAnim")

    val safeScale = if (scale.isNaN() || scale.isInfinite() || scale < 0.5f) 1.0f else scale.coerceIn(0.7f, 1.4f)
    val keyCornerRadius = 7.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(animatedBg)
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = (10 * safeScale).dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Contextual Suggestion Preview Bar (Modo Ultralimpio)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "você",
                    fontSize = 11.sp,
                    color = animatedText.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "também",
                    fontSize = 12.sp,
                    color = animatedText,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "porque",
                    fontSize = 11.sp,
                    color = animatedText.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Normal
                )
            }

            // Number row (opcional)
            if (numberRow) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").forEach { num ->
                        MiniKey(label = num, hint = null, weight = 1f, keyCornerRadius, animatedKeyBg, animatedText, inactiveColor)
                    }
                }
            }

            // Row 1: Q W E R T Y U I O P
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((26 * safeScale).dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val row1 = listOf("Q" to "1", "W" to "2", "E" to "3", "R" to "4", "T" to "5", "Y" to "6", "U" to "7", "I" to "8", "O" to "9", "P" to "0")
                row1.forEach { (letter, hint) ->
                    MiniKey(
                        label = letter,
                        hint = if (keyHints) hint else null,
                        weight = 1f,
                        cornerRadius = keyCornerRadius,
                        bg = animatedKeyBg,
                        textColor = animatedText,
                        inactiveColor = inactiveColor
                    )
                }
            }

            // Row 2: A S D F G H J K L
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((26 * safeScale).dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Spacer(modifier = Modifier.weight(0.5f))
                val row2 = listOf("A" to "@", "S" to "#", "D" to "$", "F" to "%", "G" to "&", "H" to "*", "J" to "-", "K" to "+", "L" to "=")
                row2.forEach { (letter, hint) ->
                    MiniKey(
                        label = letter,
                        hint = if (keyHints) hint else null,
                        weight = 1f,
                        cornerRadius = keyCornerRadius,
                        bg = animatedKeyBg,
                        textColor = animatedText,
                        inactiveColor = inactiveColor
                    )
                }
                Spacer(modifier = Modifier.weight(0.5f))
            }

            // Row 3: Shift Z X C V B N M Backspace
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((26 * safeScale).dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                MiniPill(label = "⇧", weight = 1.5f, keyCornerRadius, animatedActionKeyBg, animatedText)
                val row3 = listOf("Z" to "(", "X" to ")", "C" to "/", "V" to "\\", "B" to "'", "N" to "\"", "M" to "?")
                row3.forEach { (letter, hint) ->
                    MiniKey(
                        label = letter,
                        hint = if (keyHints) hint else null,
                        weight = 1f,
                        cornerRadius = keyCornerRadius,
                        bg = animatedKeyBg,
                        textColor = animatedText,
                        inactiveColor = inactiveColor
                    )
                }
                MiniPill(label = "⌫", weight = 1.5f, keyCornerRadius, animatedActionKeyBg, animatedText)
            }

            // Row 4: 123 , [ Tessera ] . ↵
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((24 * safeScale).dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiniPill(label = "123", weight = 1.5f, keyCornerRadius, animatedActionKeyBg, animatedText)
                MiniPill(label = ",", weight = 1f, keyCornerRadius, animatedKeyBg, animatedText)
                MiniPill(label = "Tessera", weight = 5.0f, keyCornerRadius, animatedKeyBg, animatedText, isSpace = true)
                MiniPill(label = ".", weight = 1f, keyCornerRadius, animatedKeyBg, animatedText)
                MiniPill(label = "↵", weight = 1.5f, keyCornerRadius, animatedActionKeyBg, animatedText)
            }
        }
    }
}

@Composable
private fun RowScope.MiniKey(
    label: String,
    hint: String?,
    weight: Float,
    cornerRadius: androidx.compose.ui.unit.Dp,
    bg: Color,
    textColor: Color,
    inactiveColor: Color
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .clip(RoundedCornerShape(cornerRadius))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center
            )
            if (hint != null) {
                Text(
                    text = hint,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Medium,
                    color = inactiveColor,
                    modifier = Modifier.padding(bottom = 4.dp, start = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun RowScope.MiniPill(
    label: String,
    weight: Float,
    cornerRadius: androidx.compose.ui.unit.Dp,
    bg: Color,
    textColor: Color,
    isSpace: Boolean = false
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .clip(RoundedCornerShape(cornerRadius))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = if (isSpace) 9.sp else 10.sp,
            fontWeight = if (isSpace) FontWeight.Medium else FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

private data class HexThemeColors(
    val bg: Color,
    val glow: Color,
    val keyBg: Color,
    val actionKeyBg: Color,
    val text: Color,
    val inactive: Color
)
