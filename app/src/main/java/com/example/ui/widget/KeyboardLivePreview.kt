package com.example.ui.widget

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KeyboardLivePreview(
    themeName: String,
    keyShape: String,
    keyHints: Boolean,
    scale: Float,
    numberRow: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Cores calculadas dinamicamente com base no tema escolhido
    val (bgColors, glowColor, keyBg, keyBorder, textColor, inactiveColor) = when (themeName) {
        "Light" -> HexThemeColors(
            bg = listOf(Color(0xFFE2E8F0), Color(0xFFF8FAFC)),
            glow = Color(0xFF007777),
            keyBg = Color(0x33000000),
            keyBorder = Color(0x1A000000),
            text = Color(0xFF0F172A),
            inactive = Color(0x800F172A)
        )
        "Amoled" -> HexThemeColors(
            bg = listOf(Color(0xFF000000), Color(0xFF09090B)),
            glow = Color(0xFF38BDF8),
            keyBg = Color(0xFF18181B),
            keyBorder = Color(0xFF27272A),
            text = Color(0xFFFFFFFF),
            inactive = Color(0xFF71717A)
        )
        "Cyberpunk" -> HexThemeColors(
            bg = listOf(Color(0xFF0D031A), Color(0xFF16042A)),
            glow = Color(0xFFFF007F),
            keyBg = Color(0x1A00F5FF),
            keyBorder = Color(0x4DFF007F),
            text = Color(0xFF00F5FF),
            inactive = Color(0x8000F5FF)
        )
        "Nord" -> HexThemeColors(
            bg = listOf(Color(0xFF242933), Color(0xFF2E3440)),
            glow = Color(0xFF88C0D0),
            keyBg = Color(0x26D8DEE9),
            keyBorder = Color(0x3388C0D0),
            text = Color(0xFFECEFF4),
            inactive = Color(0x80D8DEE9)
        )
        "Monet" -> HexThemeColors(
            bg = listOf(Color(0xFF1C1B1F), Color(0xFF25232A)),
            glow = Color(0xFFD0BCFF),
            keyBg = Color(0x26E6E1E5),
            keyBorder = Color(0x1AE6E1E5),
            text = Color(0xFFE6E1E5),
            inactive = Color(0x80CAC4D0)
        )
        else -> HexThemeColors( // Dark Glass Padrão
            bg = listOf(Color(0xFF001717), Color(0xFF002424)),
            glow = Color(0xFF06FBFB),
            keyBg = Color(0x1AFFFFFF),
            keyBorder = Color(0x1FFFFFFF),
            text = Color(0xFFFFFFFF),
            inactive = Color(0x80FFFFFF)
        )
    }

    val animatedGlow by animateColorAsState(glowColor, tween(300), label = "glowAnim")
    val animatedText by animateColorAsState(textColor, tween(300), label = "textAnim")
    val animatedKeyBg by animateColorAsState(keyBg, tween(300), label = "keyBgAnim")
    val animatedKeyBorder by animateColorAsState(keyBorder, tween(300), label = "keyBorderAnim")

    val safeScale = if (scale.isNaN() || scale.isInfinite() || scale < 0.5f) 1.0f else scale.coerceIn(0.7f, 1.4f)
    val keyCornerRadius = if (keyShape == "squircle") 6.dp else 12.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(bgColors))
            .border(1.5.dp, animatedGlow.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = (10 * safeScale).dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Live Preview Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(animatedGlow, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE PREVIEW • $themeName (${if (keyShape == "squircle") "Squircle" else "Pílula"})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = animatedGlow
                    )
                }
                Text(
                    text = "${(safeScale * 100).toInt()}%",
                    fontSize = 10.sp,
                    color = animatedText.copy(alpha = 0.6f)
                )
            }

            // Top mini toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiniPill(label = "✨", weight = 1.2f, keyCornerRadius, animatedKeyBg, animatedKeyBorder, animatedGlow)
                MiniPill(label = "•", weight = 1f, keyCornerRadius, animatedKeyBg, animatedKeyBorder, animatedText)
                MiniPill(label = "📋", weight = 1f, keyCornerRadius, animatedKeyBg, animatedKeyBorder, animatedText)
                MiniPill(label = "😊", weight = 1f, keyCornerRadius, animatedKeyBg, animatedKeyBorder, animatedText)
                MiniPill(label = "✋", weight = 1f, keyCornerRadius, animatedKeyBg, animatedKeyBorder, animatedText)
                MiniPill(label = "⚙️", weight = 1f, keyCornerRadius, animatedKeyBg, animatedKeyBorder, animatedText)
            }

            // Number row (optional)
            if (numberRow) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").forEach { num ->
                        MiniKey(label = num, hint = null, weight = 1f, keyCornerRadius, animatedKeyBg, animatedKeyBorder, animatedText, inactiveColor)
                    }
                }
            }

            // Row 1: Q W E R T Y U I O P
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((24 * safeScale).dp),
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
                        border = animatedKeyBorder,
                        textColor = animatedText,
                        inactiveColor = inactiveColor
                    )
                }
            }

            // Row 2: A S D F G H J K L
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((24 * safeScale).dp),
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
                        border = animatedKeyBorder,
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
                    .height((24 * safeScale).dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                MiniPill(label = "⇧", weight = 1.4f, keyCornerRadius, animatedKeyBg, animatedKeyBorder, animatedText)
                val row3 = listOf("Z" to "(", "X" to ")", "C" to "/", "V" to "\\", "B" to "'", "N" to "\"", "M" to "?")
                row3.forEach { (letter, hint) ->
                    MiniKey(
                        label = letter,
                        hint = if (keyHints) hint else null,
                        weight = 1f,
                        cornerRadius = keyCornerRadius,
                        bg = animatedKeyBg,
                        border = animatedKeyBorder,
                        textColor = animatedText,
                        inactiveColor = inactiveColor
                    )
                }
                MiniPill(label = "⌫", weight = 1.4f, keyCornerRadius, animatedKeyBg, animatedKeyBorder, animatedText)
            }

            // Row 4: 123 , [ Tessera ] . ↵
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((22 * safeScale).dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiniPill(label = "123", weight = 1.4f, keyCornerRadius, animatedKeyBg, animatedKeyBorder, animatedText)
                MiniPill(label = ",", weight = 1f, keyCornerRadius, animatedKeyBg, animatedKeyBorder, animatedText)
                MiniPill(label = "Tessera", weight = 4.8f, keyCornerRadius, animatedKeyBg, animatedKeyBorder, animatedGlow, isSpace = true)
                MiniPill(label = ".", weight = 1f, keyCornerRadius, animatedKeyBg, animatedKeyBorder, animatedText)
                MiniPill(label = "↵", weight = 1.4f, keyCornerRadius, animatedGlow.copy(alpha = 0.25f), animatedGlow, animatedGlow)
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
    border: Color,
    textColor: Color,
    inactiveColor: Color
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .clip(RoundedCornerShape(cornerRadius))
            .background(bg)
            .border(0.6.dp, border, RoundedCornerShape(cornerRadius)),
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
                    fontWeight = FontWeight.Bold,
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
    border: Color,
    textColor: Color,
    isSpace: Boolean = false
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .clip(RoundedCornerShape(cornerRadius))
            .background(bg)
            .border(0.6.dp, border, RoundedCornerShape(cornerRadius)),
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
    val bg: List<Color>,
    val glow: Color,
    val keyBg: Color,
    val keyBorder: Color,
    val text: Color,
    val inactive: Color
)
