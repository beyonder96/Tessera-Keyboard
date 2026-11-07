package com.example.ui.widget

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.lumina.LuminaTokens

@Composable
fun KeyboardLivePreview(
    themeName: String,
    keyShape: String = "squircle",
    keyHints: Boolean = true,
    scale: Float = 1.0f,
    numberRow: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Paleta fiel aos temas XML (themes.xml) e DynamicThemeResolver
    val palette = when (themeName) {
        "Light" -> HexThemeColors(
            label = "Journal Light",
            bg = Color(0xFFF3EFE6),
            glow = Color(0xFF0284C7),
            keyBg = Color(0xFFFFFFFF),
            actionKeyBg = Color(0xFFE5DFD2),
            text = Color(0xFF1C1917),
            inactive = Color(0xFF78716C),
            aiPillBg = Color(0xFFE0F2FE)
        )
        "Amoled" -> HexThemeColors(
            label = "Pure AMOLED",
            bg = Color(0xFF000000),
            glow = Color(0xFF38BDF8),
            keyBg = Color(0xFF121215),
            actionKeyBg = Color(0xFF1A1A20),
            text = Color(0xFFFAFAFA),
            inactive = Color(0xFF63636E),
            aiPillBg = Color(0xFF0C2233)
        )
        "Cyberpunk" -> HexThemeColors(
            label = "Cyberpunk",
            bg = Color(0xFF090614),
            glow = Color(0xFF00F0FF),
            keyBg = Color(0xFF150E2A),
            actionKeyBg = Color(0xFF231542),
            text = Color(0xFFFCEE09),
            inactive = Color(0xFFFF007F),
            aiPillBg = Color(0xFF1D1038)
        )
        "Nord" -> HexThemeColors(
            label = "Nord Frost",
            bg = Color(0xFF242933),
            glow = Color(0xFF88C0D0),
            keyBg = Color(0xFF2E3440),
            actionKeyBg = Color(0xFF3B4252),
            text = Color(0xFFECEFF4),
            inactive = Color(0xFF81A1C1),
            aiPillBg = Color(0xFF2B394A)
        )
        "Monet" -> HexThemeColors(
            label = "Material You",
            bg = Color(0xFF191C1E),
            glow = Color(0xFFA8C7FA),
            keyBg = Color(0xFF23272B),
            actionKeyBg = Color(0xFF2E3338),
            text = Color(0xFFE2E2E6),
            inactive = Color(0xFF8E9199),
            aiPillBg = Color(0xFF1E2B3C)
        )
        "Incognito" -> HexThemeColors(
            label = "Stealth Incognito",
            bg = Color(0xFF0D0B12),
            glow = Color(0xFFA855F7),
            keyBg = Color(0xFF171321),
            actionKeyBg = Color(0xFF221C30),
            text = Color(0xFFEDE9F6),
            inactive = Color(0xFF7E7291),
            aiPillBg = Color(0xFF241638)
        )
        else -> HexThemeColors( // Obsidian Dark (Tessera Core)
            label = "Obsidian Dark",
            bg = Color(0xFF0E0E12),
            glow = Color(0xFF38BDF8),
            keyBg = Color(0xFF1B1B22),
            actionKeyBg = Color(0xFF24242E),
            text = Color(0xFFF2F2F7),
            inactive = Color(0xFF7C7C88),
            aiPillBg = Color(0xFF132638)
        )
    }

    val animatedBg by animateColorAsState(palette.bg, tween(250), label = "bgAnim")
    val animatedText by animateColorAsState(palette.text, tween(250), label = "textAnim")
    val animatedKeyBg by animateColorAsState(palette.keyBg, tween(250), label = "keyBgAnim")
    val animatedActionKeyBg by animateColorAsState(palette.actionKeyBg, tween(250), label = "actionKeyBgAnim")
    val animatedGlow by animateColorAsState(palette.glow, tween(250), label = "glowAnim")

    val safeScale = if (scale.isNaN() || scale.isInfinite() || scale < 0.5f) 1.0f else scale.coerceIn(0.85f, 1.25f)
    val keyCornerRadius = when (keyShape) {
        "pill" -> 14.dp
        "borderless" -> 8.dp
        else -> 7.dp
    }
    val shapeLabel = when (keyShape) {
        "pill" -> "Circular"
        "borderless" -> "Sem Bordas"
        else -> "Esculpido"
    }

    val isBorderless = keyShape == "borderless"
    val effectiveKeyBg = if (isBorderless) Color.Transparent else animatedKeyBg
    val effectiveActionKeyBg = if (isBorderless) animatedActionKeyBg.copy(alpha = 0.45f) else animatedActionKeyBg

    var previewAiBarMode by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(LuminaTokens.TesseraCardBg)
            .border(1.dp, LuminaTokens.TesseraCardBorder, RoundedCornerShape(26.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Barra de status superior do Card de Preview (Estilo Tessera Core)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(animatedGlow)
                )
                Text(
                    text = "${palette.label} • $shapeLabel",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = LuminaTokens.TextMutedSecondary
                )
            }

            // Toggle para testar visualmente a Barra de Ferramentas vs Barra de IA sem cortes
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(LuminaTokens.TesseraPillTrackBg)
                    .border(1.dp, LuminaTokens.TesseraCardBorder, RoundedCornerShape(999.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { previewAiBarMode = !previewAiBarMode }
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (previewAiBarMode) "✨ Prévia IA Ativa" else "Ver Barra IA ✨",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (previewAiBarMode) LuminaTokens.LuminaCyan else LuminaTokens.TextPureLight
                )
            }
        }

        // Superfície Realista do Teclado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = Color.Black.copy(alpha = 0.6f),
                    spotColor = animatedGlow.copy(alpha = 0.25f)
                )
                .clip(RoundedCornerShape(18.dp))
                .background(animatedBg)
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            if (themeName == "Light") Color(0xFFDDD8CB) else Color(0x2EFFFFFF),
                            if (themeName == "Light") Color(0xFFDDD8CB) else Color(0x0DFFFFFF)
                        )
                    ),
                    RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 6.dp, vertical = (8 * safeScale).dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.5.dp)
            ) {
                // Barra Superior Contextual (Alterna entre Smart Tools e Recomendação da IA)
                AnimatedContent(
                    targetState = previewAiBarMode,
                    label = "PreviewTopBarMode"
                ) { isAiMode ->
                    if (isAiMode) {
                        // Demonstração exata da barra de recomendação da IA ocupando toda a largura + botão ✕ compacto
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 32.dp)
                                .padding(horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(palette.aiPillBg)
                                    .border(1.dp, animatedGlow.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✨ \"Você vai à festa hoje à noite?\"",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = animatedText,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(effectiveActionKeyBg)
                                    .clickable { previewAiBarMode = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✕",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = animatedText.copy(alpha = 0.75f)
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val tools = listOf("🎤", "📋", "🎯", "😊", "📱", "⚙️")
                            tools.forEach { icon ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(effectiveActionKeyBg.copy(alpha = if (isBorderless) 0.25f else 0.7f))
                                        .border(
                                            0.5.dp,
                                            if (themeName == "Light") Color(0x18000000) else Color(0x18FFFFFF),
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = icon,
                                        fontSize = 10.5.sp,
                                        color = animatedText.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Linha Numérica (quando ativada, oculta os hints 1..0 da linha Q..P)
                if (numberRow) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((23 * safeScale).dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").forEach { num ->
                            MiniKey(
                                label = num,
                                hint = null,
                                weight = 1f,
                                cornerRadius = keyCornerRadius,
                                bg = effectiveKeyBg,
                                glowColor = animatedGlow,
                                textColor = animatedText,
                                inactiveColor = palette.inactive,
                                isBorderless = isBorderless,
                                isLight = themeName == "Light"
                            )
                        }
                    }
                }

                // Row 1: Q W E R T Y U I O P
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((28 * safeScale).dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    val row1 = listOf("Q" to "1", "W" to "2", "E" to "3", "R" to "4", "T" to "5", "Y" to "6", "U" to "7", "I" to "8", "O" to "9", "P" to "0")
                    row1.forEach { (letter, hint) ->
                        MiniKey(
                            label = letter,
                            hint = if (keyHints && !numberRow) hint else null,
                            weight = 1f,
                            cornerRadius = keyCornerRadius,
                            bg = effectiveKeyBg,
                            glowColor = animatedGlow,
                            textColor = animatedText,
                            inactiveColor = palette.inactive,
                            isBorderless = isBorderless,
                            isLight = themeName == "Light"
                        )
                    }
                }

                // Row 2: A S D F G H J K L
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((28 * safeScale).dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Spacer(modifier = Modifier.weight(0.45f))
                    val row2 = listOf("A" to "@", "S" to "#", "D" to "$", "F" to "%", "G" to "&", "H" to "*", "J" to "-", "K" to "+", "L" to "=")
                    row2.forEach { (letter, hint) ->
                        MiniKey(
                            label = letter,
                            hint = if (keyHints) hint else null,
                            weight = 1f,
                            cornerRadius = keyCornerRadius,
                            bg = effectiveKeyBg,
                            glowColor = animatedGlow,
                            textColor = animatedText,
                            inactiveColor = palette.inactive,
                            isBorderless = isBorderless,
                            isLight = themeName == "Light"
                        )
                    }
                    Spacer(modifier = Modifier.weight(0.45f))
                }

                // Row 3: Shift Z X C V B N M Backspace
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((28 * safeScale).dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    MiniPill(
                        label = "⇧",
                        weight = 1.45f,
                        cornerRadius = keyCornerRadius,
                        bg = effectiveActionKeyBg,
                        textColor = animatedText,
                        isBorderless = isBorderless,
                        isLight = themeName == "Light"
                    )
                    val row3 = listOf("Z" to "(", "X" to ")", "C" to "/", "V" to "\\", "B" to "'", "N" to "\"", "M" to "?")
                    row3.forEach { (letter, hint) ->
                        MiniKey(
                            label = letter,
                            hint = if (keyHints) hint else null,
                            weight = 1f,
                            cornerRadius = keyCornerRadius,
                            bg = effectiveKeyBg,
                            glowColor = animatedGlow,
                            textColor = animatedText,
                            inactiveColor = palette.inactive,
                            isBorderless = isBorderless,
                            isLight = themeName == "Light"
                        )
                    }
                    MiniPill(
                        label = "⌫",
                        weight = 1.45f,
                        cornerRadius = keyCornerRadius,
                        bg = effectiveActionKeyBg,
                        textColor = animatedText,
                        isBorderless = isBorderless,
                        isLight = themeName == "Light"
                    )
                }

                // Row 4: 123 , [ Tessera ] . ↵
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((27 * safeScale).dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MiniPill(
                        label = "123",
                        weight = 1.4f,
                        cornerRadius = keyCornerRadius,
                        bg = effectiveActionKeyBg,
                        textColor = animatedText,
                        isBorderless = isBorderless,
                        isLight = themeName == "Light"
                    )
                    MiniPill(
                        label = ",",
                        weight = 0.95f,
                        cornerRadius = keyCornerRadius,
                        bg = effectiveKeyBg,
                        textColor = animatedText,
                        isBorderless = isBorderless,
                        isLight = themeName == "Light"
                    )
                    MiniPill(
                        label = "Tessera",
                        weight = 4.8f,
                        cornerRadius = keyCornerRadius,
                        bg = if (isBorderless) animatedActionKeyBg.copy(alpha = 0.35f) else effectiveKeyBg,
                        textColor = animatedText,
                        isSpace = true,
                        isBorderless = isBorderless,
                        isLight = themeName == "Light"
                    )
                    MiniPill(
                        label = ".",
                        weight = 0.95f,
                        cornerRadius = keyCornerRadius,
                        bg = effectiveKeyBg,
                        textColor = animatedText,
                        isBorderless = isBorderless,
                        isLight = themeName == "Light"
                    )
                    MiniPill(
                        label = "↵",
                        weight = 1.5f,
                        cornerRadius = keyCornerRadius,
                        bg = animatedGlow.copy(alpha = if (themeName == "Light") 0.85f else 0.28f),
                        textColor = if (themeName == "Light") Color.White else animatedGlow,
                        isBorderless = false,
                        isLight = themeName == "Light"
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.MiniKey(
    label: String,
    hint: String?,
    weight: Float,
    cornerRadius: Dp,
    bg: Color,
    glowColor: Color,
    textColor: Color,
    inactiveColor: Color,
    isBorderless: Boolean,
    isLight: Boolean
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "MiniKeyScale"
    )
    val borderColor = when {
        pressed -> glowColor
        isBorderless -> Color.Transparent
        isLight -> Color(0x1F000000)
        else -> Color(0x1FFFFFFF)
    }

    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(cornerRadius))
            .background(if (pressed) glowColor.copy(alpha = 0.25f) else bg)
            .border(0.6.dp, borderColor, RoundedCornerShape(cornerRadius))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    }
                )
            }
    ) {
        if (hint != null) {
            Text(
                text = hint,
                fontSize = 6.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = inactiveColor,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 1.dp, end = 2.5.dp)
            )
        }
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun RowScope.MiniPill(
    label: String,
    weight: Float,
    cornerRadius: Dp,
    bg: Color,
    textColor: Color,
    isSpace: Boolean = false,
    isBorderless: Boolean = false,
    isLight: Boolean = false
) {
    val borderColor = when {
        isBorderless && !isSpace -> Color.Transparent
        isLight -> Color(0x1F000000)
        else -> Color(0x1FFFFFFF)
    }

    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .clip(RoundedCornerShape(cornerRadius))
            .background(bg)
            .border(0.6.dp, borderColor, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = if (isSpace) 9.5.sp else 10.sp,
            fontWeight = if (isSpace) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSpace) textColor.copy(alpha = 0.75f) else textColor,
            textAlign = TextAlign.Center
        )
    }
}

private data class HexThemeColors(
    val label: String,
    val bg: Color,
    val glow: Color,
    val keyBg: Color,
    val actionKeyBg: Color,
    val text: Color,
    val inactive: Color,
    val aiPillBg: Color
)
