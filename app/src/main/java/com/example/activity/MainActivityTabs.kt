package com.example.activity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.widget.KeyboardLivePreview
import com.example.ui.theme.*

@Composable
fun TabHeader(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Slate100
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate400
        )
    }
}

@Composable
fun TabHomeContent(
    keyboardTheme: String,
    keyShape: String,
    keyHints: Boolean,
    keyboardScale: Float,
    isEnabled: Boolean,
    isSelected: Boolean,
    onEnableKeyboardClick: () -> Unit,
    onSelectKeyboardClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TesseraHeader()

        KeyboardLivePreview(
            themeName = keyboardTheme,
            keyShape = keyShape,
            keyHints = keyHints,
            scale = keyboardScale.coerceIn(0.85f, 1.25f),
            modifier = Modifier.fillMaxWidth()
        )

        KeyboardStatusCard(
            isEnabled = isEnabled,
            isSelected = isSelected,
            onEnableClick = onEnableKeyboardClick,
            onSelectClick = onSelectKeyboardClick
        )

        PlaygroundSection(isEnabled = isEnabled && isSelected)

        Text(
            text = "Tessera Keyboard • Minimalismo & Máxima Performance",
            style = MaterialTheme.typography.bodySmall,
            color = Slate600,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )
    }
}

@Composable
fun TabTypingContent(
    autocorrect: Boolean,
    onAutocorrectChange: (Boolean) -> Unit,
    smartAbbr: Boolean,
    onSmartAbbrChange: (Boolean) -> Unit,
    doubleSpacePeriod: Boolean,
    onDoubleSpacePeriodChange: (Boolean) -> Unit,
    autoCap: Boolean,
    onAutoCapChange: (Boolean) -> Unit,
    keyPopup: Boolean,
    onKeyPopupChange: (Boolean) -> Unit,
    numberRow: Boolean,
    onNumberRowChange: (Boolean) -> Unit,
    glideTyping: Boolean,
    onGlideTypingChange: (Boolean) -> Unit,
    keyHints: Boolean,
    onKeyHintsChange: (Boolean) -> Unit,
    suggestScreenshots: Boolean,
    hasScreenshotPermission: Boolean,
    onRequestScreenshotPermission: () -> Unit,
    onSuggestScreenshotsChange: (Boolean) -> Unit,
    flickAccents: Boolean,
    onFlickAccentsChange: (Boolean) -> Unit,
    swipeDownHide: Boolean,
    onSwipeDownHideChange: (Boolean) -> Unit,
    undoDelete: Boolean,
    onUndoDeleteChange: (Boolean) -> Unit,
    hapticFeedback: Boolean,
    onHapticFeedbackChange: (Boolean) -> Unit,
    hapticDurationMs: Int,
    onHapticDurationChange: (Int) -> Unit,
    soundFeedback: Boolean,
    onSoundFeedbackChange: (Boolean) -> Unit,
    hasMicPermission: Boolean,
    onRequestMicPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TabHeader(
            title = "Digitação",
            subtitle = "Entrada de texto, gestos, automações e feedback tátil"
        )

        SectionCard(title = "Entrada e Automações") {
            SettingToggle(
                title = "Correção Automática",
                description = "Corrige palavras comuns ao pressionar a barra de espaço",
                checked = autocorrect,
                onCheckedChange = onAutocorrectChange
            )
            DividerLine()
            SettingToggle(
                title = "Abreviações Rápidas",
                description = "Expande vc -> você, tbm -> também, pq -> porque instantaneamente",
                checked = smartAbbr,
                onCheckedChange = onSmartAbbrChange
            )
            DividerLine()
            SettingToggle(
                title = "Ponto com duplo espaço",
                description = "Insere ponto final e espaço ao tocar rapidamente duas vezes na barra de espaço",
                checked = doubleSpacePeriod,
                onCheckedChange = onDoubleSpacePeriodChange
            )
            DividerLine()
            SettingToggle(
                title = "Primeira letra maiúscula",
                description = "Capitalização automática no início de frases e períodos",
                checked = autoCap,
                onCheckedChange = onAutoCapChange
            )
            DividerLine()
            SettingToggle(
                title = "Linha Numérica Superior",
                description = "Linha fixa com números de 0 a 9 acima do teclado alfabético",
                checked = numberRow,
                onCheckedChange = onNumberRowChange
            )
            DividerLine()
            SettingToggle(
                title = "Sub-legendas nas Teclas (Key Hints)",
                description = "Exibe símbolos e números com digitação rápida por toque longo",
                checked = keyHints,
                onCheckedChange = onKeyHintsChange
            )
            DividerLine()
            SettingToggle(
                title = "Pré-visualização de tecla",
                description = "Exibe popup com o caractere pressionado",
                checked = keyPopup,
                onCheckedChange = onKeyPopupChange
            )
        }

        SectionCard(title = "Gestos e Mídia") {
            SettingToggle(
                title = "Digitação por Gestos (Glide Typing)",
                description = "Deslize o dedo pelas letras para digitar palavras continuamente",
                checked = glideTyping,
                onCheckedChange = onGlideTypingChange
            )
            DividerLine()
            SettingToggle(
                title = "Colar prints recentes",
                description = "Sugere colar imagem capturada recentemente direto no teclado",
                checked = suggestScreenshots && hasScreenshotPermission,
                onCheckedChange = { checked ->
                    if (checked && !hasScreenshotPermission) {
                        onRequestScreenshotPermission()
                    }
                    onSuggestScreenshotsChange(checked)
                }
            )
            if (suggestScreenshots && !hasScreenshotPermission) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Permissão de fotos necessária para detectar prints",
                        style = MaterialTheme.typography.bodySmall,
                        color = StateWarning,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onRequestScreenshotPermission,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentSkyMuted,
                            contentColor = AccentSky
                        ),
                        border = BorderStroke(1.dp, AccentSky.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Permitir", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            DividerLine()
            SettingToggle(
                title = "Acentuação Rápida por Deslize (Flick)",
                description = "Deslize rápido para cima na tecla para inserir acentos instantaneamente (c ➔ ç, e ➔ é, a ➔ á)",
                checked = flickAccents,
                onCheckedChange = onFlickAccentsChange
            )
            DividerLine()
            SettingToggle(
                title = "Deslizar para baixo para fechar teclado",
                description = "Deslize rápido descendente para recolher o teclado de forma imediata",
                checked = swipeDownHide,
                onCheckedChange = onSwipeDownHideChange
            )
            DividerLine()
            SettingToggle(
                title = "Desfazer exclusões do Backspace",
                description = "Permite cancelar deslize para a esquerda e exibe botão 'Desfazer' para restaurar texto",
                checked = undoDelete,
                onCheckedChange = onUndoDeleteChange
            )
        }

        SectionCard(title = "Resposta e Feedback") {
            SettingToggle(
                title = "Resposta tátil (vibração)",
                description = "Clique háptico mecânico a cada tecla tocada",
                checked = hapticFeedback,
                onCheckedChange = onHapticFeedbackChange
            )
            if (hapticFeedback) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Intensidade da Vibração",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate300
                    )
                    Text(
                        text = "${hapticDurationMs}ms",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentSky
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = hapticDurationMs.toFloat().coerceIn(1f, 50f),
                    onValueChange = { newValue ->
                        onHapticDurationChange(newValue.toInt().coerceIn(1, 50))
                    },
                    valueRange = 1f..50f,
                    steps = 49,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentSky,
                        activeTrackColor = AccentSky,
                        inactiveTrackColor = Slate800
                    )
                )
            }
            DividerLine()
            SettingToggle(
                title = "Som ao tocar",
                description = "Clique de áudio leve e assíncrono (sem atraso na digitação)",
                checked = soundFeedback,
                onCheckedChange = onSoundFeedbackChange
            )
            if (!hasMicPermission) {
                DividerLine()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Digitação por Voz",
                            style = MaterialTheme.typography.titleSmall,
                            color = Slate100
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Permissão de áudio necessária para usar o microfone",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate400
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onRequestMicPermission,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentSkyMuted,
                            contentColor = AccentSky
                        ),
                        border = BorderStroke(1.dp, AccentSky.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Permitir", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun TabAppearanceContent(
    keyboardTheme: String,
    onThemeChange: (String) -> Unit,
    keyShape: String,
    onKeyShapeChange: (String) -> Unit,
    glideTrailStyle: String,
    onGlideTrailStyleChange: (String) -> Unit,
    keyboardScale: Float,
    onScaleChange: (Float) -> Unit,
    popupStyle: String,
    onPopupStyleChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TabHeader(
            title = "Aparência",
            subtitle = "Temas neutros, geometria e escala visual das teclas"
        )

        SectionCard(title = "Paleta e Tema") {
            Text(
                text = "Tema Visual",
                style = MaterialTheme.typography.titleSmall,
                color = Slate100
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Paleta editorial refinada com contraste suave e fundo translúcido:",
                style = MaterialTheme.typography.bodySmall,
                color = Slate400
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionButton(
                    title = "Obsidian Dark",
                    isSelected = keyboardTheme == "Dark",
                    onClick = { onThemeChange("Dark") },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionButton(
                    title = "Paper Light",
                    isSelected = keyboardTheme == "Light",
                    onClick = { onThemeChange("Light") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionButton(
                    title = "Pure AMOLED",
                    isSelected = keyboardTheme == "Amoled",
                    onClick = { onThemeChange("Amoled") },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionButton(
                    title = "Cyberpunk",
                    isSelected = keyboardTheme == "Cyberpunk",
                    onClick = { onThemeChange("Cyberpunk") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionButton(
                    title = "Nord Frost",
                    isSelected = keyboardTheme == "Nord",
                    onClick = { onThemeChange("Nord") },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionButton(
                    title = "Material You",
                    isSelected = keyboardTheme == "Monet",
                    onClick = { onThemeChange("Monet") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionButton(
                    title = "Stealth Incognito 🕵",
                    isSelected = keyboardTheme == "Incognito",
                    onClick = { onThemeChange("Incognito") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        SectionCard(title = "Geometria e Proporções") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Altura do Teclado",
                    style = MaterialTheme.typography.titleSmall,
                    color = Slate100
                )
                Text(
                    text = "${(keyboardScale * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentSky
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Slider(
                value = keyboardScale.coerceIn(0.85f, 1.25f),
                onValueChange = { onScaleChange(it.coerceIn(0.85f, 1.25f)) },
                valueRange = 0.85f..1.25f,
                steps = 7,
                colors = SliderDefaults.colors(
                    thumbColor = AccentSky,
                    activeTrackColor = AccentSky,
                    inactiveTrackColor = Slate800
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            DividerLine()
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Geometria das Teclas (Key Shape)",
                style = MaterialTheme.typography.titleSmall,
                color = Slate100
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Design dos cantos das teclas:",
                style = MaterialTheme.typography.bodySmall,
                color = Slate400
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionButton(
                    title = "Editorial Flat (7dp)",
                    isSelected = keyShape == "squircle",
                    onClick = { onKeyShapeChange("squircle") },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionButton(
                    title = "Pílula Suave",
                    isSelected = keyShape == "pill",
                    onClick = { onKeyShapeChange("pill") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            DividerLine()
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Estilo do Rastro de Deslize (Glide Trail)",
                style = MaterialTheme.typography.titleSmall,
                color = Slate100
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Cor da linha dinâmica durante a digitação por gestos:",
                style = MaterialTheme.typography.bodySmall,
                color = Slate400
            )
            Spacer(modifier = Modifier.height(8.dp))
            val trailOptions = listOf("Theme", "Cyan", "Magenta", "Amber", "White")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                trailOptions.forEach { opt ->
                    val isSel = glideTrailStyle == opt
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isSel) AccentSkyMuted else Slate850, RoundedCornerShape(8.dp))
                            .border(1.dp, if (isSel) AccentSky else Slate700, RoundedCornerShape(8.dp))
                            .clickable { onGlideTrailStyleChange(opt) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (opt == "Theme") "Auto" else opt,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSel) AccentSky else Slate300
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            DividerLine()
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Cor dos Popups ao Tocar",
                style = MaterialTheme.typography.titleSmall,
                color = Slate100
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Estilo dos balões temporários e menus de acentos:",
                style = MaterialTheme.typography.bodySmall,
                color = Slate400
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PopupStyleOption(
                    title = "Dark Glass",
                    accentColor = Color(0xFF38BDF8),
                    surfaceColor = Color(0xFF1E293B),
                    isSelected = popupStyle == "dark_glass",
                    onClick = { onPopupStyleChange("dark_glass") },
                    modifier = Modifier.weight(1f)
                )
                PopupStyleOption(
                    title = "Obsidian",
                    accentColor = Color(0xFF94A3B8),
                    surfaceColor = Color(0xFF0F172A),
                    isSelected = popupStyle == "obsidian",
                    onClick = { onPopupStyleChange("obsidian") },
                    modifier = Modifier.weight(1f)
                )
                PopupStyleOption(
                    title = "Neon Cyan",
                    accentColor = Color(0xFF00E5FF),
                    surfaceColor = Color(0xFF0B1E2E),
                    isSelected = popupStyle == "neon_cyan",
                    onClick = { onPopupStyleChange("neon_cyan") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PopupStyleOption(
                    title = "Emerald",
                    accentColor = Color(0xFF10B981),
                    surfaceColor = Color(0xFF09241B),
                    isSelected = popupStyle == "emerald",
                    onClick = { onPopupStyleChange("emerald") },
                    modifier = Modifier.weight(1f)
                )
                PopupStyleOption(
                    title = "Purple",
                    accentColor = Color(0xFFA855F7),
                    surfaceColor = Color(0xFF1E1035),
                    isSelected = popupStyle == "purple",
                    onClick = { onPopupStyleChange("purple") },
                    modifier = Modifier.weight(1f)
                )
                PopupStyleOption(
                    title = "Amber",
                    accentColor = Color(0xFFF59E0B),
                    surfaceColor = Color(0xFF29160B),
                    isSelected = popupStyle == "amber",
                    onClick = { onPopupStyleChange("amber") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TabDictionaryContent(
    snippets: Map<String, String>,
    onAddSnippet: (String, String) -> Unit,
    onRemoveSnippet: (String) -> Unit,
    learnedWords: List<String>,
    onAddWord: (String) -> Unit,
    onRemoveWord: (String) -> Unit,
    onClearWords: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TabHeader(
            title = "Dicionário & Snippets",
            subtitle = "Gerenciamento de expansões de texto e palavras personalizadas"
        )

        SnippetsSection(
            snippets = snippets,
            onAddSnippet = onAddSnippet,
            onRemoveSnippet = onRemoveSnippet
        )

        DictionarySection(
            words = learnedWords,
            onAddWord = onAddWord,
            onRemoveWord = onRemoveWord,
            onClearAll = onClearWords
        )
    }
}
