package com.example.activity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.lumina.*
import com.example.ui.theme.*
import com.example.ui.widget.KeyboardLivePreview
import com.example.update.AppUpdateInfo
import com.example.update.TesseraKeyboardUpdatePill
import kotlinx.coroutines.launch

/**
 * Barra superior de identidade "Tessera." inspirada no Tessera Core (Tessera Launcher).
 */
@Composable
fun TesseraCoreTopBar(
    subtitleTag: String = "KEYBOARD",
    updateInfo: AppUpdateInfo? = null,
    onUpdatePillClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = LuminaTokens.TextPureLight,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Serif
                        )
                    ) {
                        append("Tessera")
                    }
                    withStyle(
                        SpanStyle(
                            color = LuminaTokens.TextPureLight,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Serif
                        )
                    ) {
                        append(".")
                    }
                },
                fontSize = 28.sp,
                letterSpacing = (-0.6).sp
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(LuminaTokens.TesseraCardBg)
                    .border(1.dp, LuminaTokens.TesseraCardBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = subtitleTag,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp
                    ),
                    color = LuminaTokens.TextMutedSecondary
                )
            }
        }

        TesseraKeyboardUpdatePill(
            updateInfo = updateInfo,
            onClick = onUpdatePillClick
        )
    }
}

@Composable
fun TabHeader(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "TESSERA CORE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = LuminaTokens.TextMutedSecondary
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp
            ),
            color = LuminaTokens.TextPureLight
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = LuminaTokens.TextMutedSecondary
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
    onSelectKeyboardClick: () -> Unit,
    numberRow: Boolean = false,
    updateInfo: AppUpdateInfo? = null,
    onUpdatePillClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
            .padding(top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        TesseraCoreTopBar(
            subtitleTag = "MONOLITH IME",
            updateInfo = updateInfo,
            onUpdatePillClick = onUpdatePillClick
        )

        TesseraHeader()

        Column(modifier = Modifier.fillMaxWidth()) {
            TesseraSectionHeader(title = "STATUS DO SISTEMA")
            KeyboardStatusCard(
                isEnabled = isEnabled,
                isSelected = isSelected,
                onEnableClick = onEnableKeyboardClick,
                onSelectClick = onSelectKeyboardClick
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            TesseraSectionHeader(title = "PRÉVIA INTERATIVA DO TECLADO")
            KeyboardLivePreview(
                themeName = keyboardTheme,
                keyShape = keyShape,
                keyHints = keyHints,
                numberRow = numberRow,
                scale = keyboardScale.coerceIn(0.85f, 1.25f),
                modifier = Modifier.fillMaxWidth()
            )
        }

        PlaygroundSection(isEnabled = isEnabled && isSelected)

        Text(
            text = "Tessera Core • Minimalismo Calmo & Latência Zero",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
            color = LuminaTokens.TextDimTertiary,
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
    onRequestMicPermission: () -> Unit,
    soundProfile: String = "thock",
    onSoundProfileChange: (String) -> Unit = {},
    adaptiveHitboxes: Boolean = true,
    onAdaptiveHitboxesChange: (Boolean) -> Unit = {},
    aiParticipative: Boolean = true,
    onAiParticipativeChange: (Boolean) -> Unit = {},
    aiPassiveContinuous: Boolean = false,
    onAiPassiveContinuousChange: (Boolean) -> Unit = {},
    aiProvider: String = "groq",
    onAiProviderChange: (String) -> Unit = {},
    groqApiKey: String = "",
    onGroqApiKeyChange: (String) -> Unit = {},
    geminiApiKey: String = "",
    onGeminiApiKeyChange: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
            .padding(top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TabHeader(
            title = "Digitação & IA",
            subtitle = "Copiloto inteligente, precisão Voronoi, gestos e resposta sensorial"
        )

        // CATEGORIA 1: IA PARTICIPATIVA (COPILOTO)
        Column(modifier = Modifier.fillMaxWidth()) {
            TesseraSectionHeader(title = "IA PARTICIPATIVA & COPILOTO")
            TesseraGroupCard {
                TesseraSwitchRow(
                    icon = Icons.Outlined.AutoAwesome,
                    title = "Ativar IA Participativa",
                    subtitle = "Lê o contexto e sugere correções ou versículos na barra superior",
                    checked = aiParticipative,
                    onCheckedChange = onAiParticipativeChange
                )

                if (aiParticipative) {
                    TesseraItemDivider(withIconOffset = true)

                    TesseraSwitchRow(
                        icon = Icons.Outlined.Bolt,
                        title = "Participação Contínua (Sem Tags)",
                        subtitle = "Analisa frases automaticamente ao pausar mesmo sem digitar @corrigir",
                        checked = aiPassiveContinuous,
                        onCheckedChange = onAiPassiveContinuousChange
                    )

                    TesseraItemDivider(withIconOffset = false)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Provedor de Processamento",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = LuminaTokens.TextPureLight
                        )

                        TesseraSegmentedControl(
                            options = listOf(
                                "groq" to "Groq (Llama / Qwen)",
                                "gemini" to "Google Gemini"
                            ),
                            selectedValue = aiProvider,
                            onValueSelected = onAiProviderChange
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        var currentKeyInput by remember(if (aiProvider == "groq") groqApiKey else geminiApiKey) {
                            mutableStateOf(if (aiProvider == "groq") groqApiKey else geminiApiKey)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (aiProvider == "groq") "Chave de API da Groq" else "Chave de API do Google Gemini",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = LuminaTokens.TextPureLight
                            )
                            Text(
                                text = if (aiProvider == "groq") "console.groq.com" else "aistudio.google.com",
                                style = MaterialTheme.typography.labelSmall,
                                color = LuminaTokens.LuminaCyan
                            )
                        }

                        OutlinedTextField(
                            value = currentKeyInput,
                            onValueChange = {
                                val cleaned = it.trim().removePrefix("Bearer ").removePrefix("bearer ").trim()
                                currentKeyInput = cleaned
                                com.example.engine.ParticipativeAiEngine.resetModelCache()
                                if (aiProvider == "groq") onGroqApiKeyChange(cleaned) else onGeminiApiKeyChange(cleaned)
                            },
                            placeholder = {
                                Text(
                                    if (aiProvider == "groq") "gsk_..." else "AIzaSy...",
                                    color = LuminaTokens.TextDimTertiary
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuminaTokens.LuminaCyan,
                                unfocusedBorderColor = LuminaTokens.TesseraCardBorder,
                                focusedContainerColor = LuminaTokens.TesseraPillTrackBg,
                                unfocusedContainerColor = LuminaTokens.TesseraPillTrackBg,
                                focusedTextColor = LuminaTokens.TextPureLight,
                                unfocusedTextColor = LuminaTokens.TextPureLight,
                                cursorColor = LuminaTokens.LuminaCyan
                            )
                        )

                        var testStatus by remember { mutableStateOf<String?>(null) }
                        var isTesting by remember { mutableStateOf(false) }
                        val coroutineScope = rememberCoroutineScope()

                        Button(
                            onClick = {
                                val activeKey = currentKeyInput.trim()
                                if (activeKey.isBlank()) {
                                    testStatus = "⚠️ Insira a chave antes de testar"
                                    return@Button
                                }
                                isTesting = true
                                testStatus = "Testando conexão com a IA..."
                                coroutineScope.launch {
                                    try {
                                        val result = com.example.engine.ParticipativeAiEngine.processText(
                                            rawInput = "@versiculo posso todas as coisas",
                                            isPassiveModeEnabled = false,
                                            provider = aiProvider,
                                            apiKey = activeKey
                                        )
                                        isTesting = false
                                        testStatus = if (result != null && !result.isError) {
                                            val modelTag = result.modelUsed?.let { "[$it] " } ?: ""
                                            "✓ Conexão OK! ${modelTag}Resposta: ${result.suggestionText.take(45)}..."
                                        } else {
                                            result?.displayLabel ?: "⚠️ Erro ao conectar com o provedor"
                                        }
                                    } catch (e: Exception) {
                                        isTesting = false
                                        testStatus = "⚠️ Falha: ${e.message}"
                                    }
                                }
                            },
                            enabled = !isTesting && currentKeyInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LuminaTokens.TesseraPillActiveBg,
                                contentColor = LuminaTokens.LuminaCyan,
                                disabledContainerColor = LuminaTokens.TesseraPillTrackBg,
                                disabledContentColor = LuminaTokens.TextDimTertiary
                            ),
                            border = BorderStroke(1.dp, LuminaTokens.TesseraPillActiveBorder),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                        ) {
                            Text(
                                text = if (isTesting) "Testando conexão..." else "Testar Conexão com a IA",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (testStatus != null) {
                            Text(
                                text = testStatus!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (testStatus!!.startsWith("✓")) LuminaTokens.LuminaEmerald else LuminaTokens.LuminaCrimson
                            )
                        }

                        // Chips visuais dos comandos rápidos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "@corrigir" to "Gramática",
                                "@versiculo" to "Bíblia",
                                "@ia" to "Assistente"
                            ).forEach { (cmd, desc) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(LuminaTokens.TesseraPillTrackBg)
                                        .border(1.dp, LuminaTokens.TesseraCardBorder, RoundedCornerShape(10.dp))
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = cmd,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = LuminaTokens.LuminaCyan
                                        )
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = LuminaTokens.TextMutedSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // CATEGORIA 2: ENTRADA & AUTOMAÇÕES
        Column(modifier = Modifier.fillMaxWidth()) {
            TesseraSectionHeader(title = "MOTOR DE ENTRADA & AUTOMAÇÕES")
            TesseraGroupCard {
                TesseraSwitchRow(
                    icon = Icons.Outlined.GridView,
                    title = "Hitboxes Voronoi Inteligentes",
                    subtitle = "Expande dinamicamente a área de toque para as letras mais prováveis",
                    checked = adaptiveHitboxes,
                    onCheckedChange = onAdaptiveHitboxesChange
                )
                TesseraItemDivider(withIconOffset = true)

                TesseraSwitchRow(
                    icon = Icons.Outlined.Spellcheck,
                    title = "Correção Automática",
                    subtitle = "Corrige palavras comuns ao pressionar a barra de espaço",
                    checked = autocorrect,
                    onCheckedChange = onAutocorrectChange
                )
                TesseraItemDivider(withIconOffset = true)

                TesseraSwitchRow(
                    icon = Icons.Outlined.FastForward,
                    title = "Abreviações Rápidas",
                    subtitle = "Expande vc ➔ você, tbm ➔ também, pq ➔ porque instantaneamente",
                    checked = smartAbbr,
                    onCheckedChange = onSmartAbbrChange
                )
                TesseraItemDivider(withIconOffset = true)

                TesseraSwitchRow(
                    icon = Icons.Outlined.SpaceBar,
                    title = "Ponto com Duplo Espaço",
                    subtitle = "Insere ponto final e espaço ao tocar duas vezes na barra de espaço",
                    checked = doubleSpacePeriod,
                    onCheckedChange = onDoubleSpacePeriodChange
                )
                TesseraItemDivider(withIconOffset = true)

                TesseraSwitchRow(
                    icon = Icons.Outlined.Title,
                    title = "Primeira Letra Maiúscula",
                    subtitle = "Capitalização automática no início de frases e períodos",
                    checked = autoCap,
                    onCheckedChange = onAutoCapChange
                )
                TesseraItemDivider(withIconOffset = true)

                TesseraSwitchRow(
                    icon = Icons.Outlined.Pin,
                    title = "Linha Numérica Superior",
                    subtitle = "Exibe linha dedicada de 1 a 0 acima do teclado e oculta números duplicados",
                    checked = numberRow,
                    onCheckedChange = onNumberRowChange
                )
                TesseraItemDivider(withIconOffset = true)

                TesseraSwitchRow(
                    icon = Icons.Outlined.Subtitles,
                    title = "Sub-legendas nas Teclas",
                    subtitle = "Exibe símbolos secundários para acesso rápido por toque longo",
                    checked = keyHints,
                    onCheckedChange = onKeyHintsChange
                )
                TesseraItemDivider(withIconOffset = true)

                TesseraSwitchRow(
                    icon = Icons.Outlined.Visibility,
                    title = "Pré-visualização ao Tocar",
                    subtitle = "Exibe balão flutuante com a tecla pressionada",
                    checked = keyPopup,
                    onCheckedChange = onKeyPopupChange
                )
            }
        }

        // CATEGORIA 3: GESTOS & MÍDIA
        Column(modifier = Modifier.fillMaxWidth()) {
            TesseraSectionHeader(title = "GESTOS & ÁREA DE TRANSFERÊNCIA")
            TesseraGroupCard {
                TesseraSwitchRow(
                    icon = Icons.Outlined.Gesture,
                    title = "Digitação por Gestos (Glide)",
                    subtitle = "Deslize o dedo continuamente pelas letras para formar palavras",
                    checked = glideTyping,
                    onCheckedChange = onGlideTypingChange
                )
                TesseraItemDivider(withIconOffset = true)

                TesseraSwitchRow(
                    icon = Icons.Outlined.Image,
                    title = "Sugerir Prints Recentes",
                    subtitle = "Sugere colar capturas de tela recentes diretamente na barra do teclado",
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
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Permissão de imagens necessária para detectar prints",
                            style = MaterialTheme.typography.bodySmall,
                            color = LuminaTokens.LuminaAmber,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onRequestScreenshotPermission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LuminaTokens.TesseraPillActiveBg,
                                contentColor = LuminaTokens.LuminaCyan
                            ),
                            border = BorderStroke(1.dp, LuminaTokens.TesseraPillActiveBorder),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Permitir", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                TesseraItemDivider(withIconOffset = true)

                TesseraSwitchRow(
                    icon = Icons.Outlined.KeyboardDoubleArrowUp,
                    title = "Acentuação Rápida por Deslize (Flick)",
                    subtitle = "Deslize rápido para cima na tecla para inserir acentos (c ➔ ç, e ➔ é)",
                    checked = flickAccents,
                    onCheckedChange = onFlickAccentsChange
                )
                TesseraItemDivider(withIconOffset = true)

                TesseraSwitchRow(
                    icon = Icons.Outlined.KeyboardHide,
                    title = "Deslizar para Baixo para Fechar",
                    subtitle = "Gesto rápido descendente recolhe o teclado imediatamente",
                    checked = swipeDownHide,
                    onCheckedChange = onSwipeDownHideChange
                )
                TesseraItemDivider(withIconOffset = true)

                TesseraSwitchRow(
                    icon = Icons.Outlined.Undo,
                    title = "Desfazer Exclusões do Backspace",
                    subtitle = "Exibe pílula para restaurar trechos apagados por deslize no Backspace",
                    checked = undoDelete,
                    onCheckedChange = onUndoDeleteChange
                )
            }
        }

        // CATEGORIA 4: FEEDBACK SENSORIAL & ACÚSTICA
        Column(modifier = Modifier.fillMaxWidth()) {
            TesseraSectionHeader(title = "ACÚSTICA & RESPOSTA HÁPTICA")
            TesseraGroupCard {
                TesseraSwitchRow(
                    icon = Icons.Outlined.Vibration,
                    title = "Resposta Tátil (Vibração)",
                    subtitle = "Clique háptico mecânico calibrado com primitivas de baixa latência",
                    checked = hapticFeedback,
                    onCheckedChange = onHapticFeedbackChange
                )
                TesseraItemDivider(withIconOffset = true)

                TesseraSwitchRow(
                    icon = Icons.Outlined.VolumeUp,
                    title = "Som ao Tocar (Síntese PCM)",
                    subtitle = "Motor acústico em tempo real com perfis de switches mecânicos",
                    checked = soundFeedback,
                    onCheckedChange = onSoundFeedbackChange
                )

                if (soundFeedback || hapticFeedback) {
                    TesseraItemDivider(withIconOffset = false)
                    Box(modifier = Modifier.padding(16.dp)) {
                        AcousticHarmonicDeck(
                            currentProfile = soundProfile,
                            onProfileSelected = onSoundProfileChange,
                            hapticDurationMs = hapticDurationMs,
                            onHapticDurationChange = onHapticDurationChange
                        )
                    }
                }

                if (!hasMicPermission) {
                    TesseraItemDivider(withIconOffset = false)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Digitação por Voz",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = LuminaTokens.TextPureLight
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Permissão de microfone necessária para ditado de voz",
                                style = MaterialTheme.typography.bodySmall,
                                color = LuminaTokens.TextMutedSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = onRequestMicPermission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LuminaTokens.TesseraPillActiveBg,
                                contentColor = LuminaTokens.LuminaCyan
                            ),
                            border = BorderStroke(1.dp, LuminaTokens.TesseraPillActiveBorder),
                            shape = RoundedCornerShape(12.dp),
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
}

private data class ThemeSwatchInfo(
    val id: String,
    val title: String,
    val subtitle: String,
    val bgColor: Color,
    val keyColor: Color,
    val accentColor: Color
)

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
    onPopupStyleChange: (String) -> Unit,
    numberRow: Boolean = false,
    keyHints: Boolean = true
) {
    val themeSwatches = remember {
        listOf(
            ThemeSwatchInfo("Dark", "Obsidian Dark", "Charcoal Padrão", Color(0xFF0E0E12), Color(0xFF1B1B22), Color(0xFF38BDF8)),
            ThemeSwatchInfo("Light", "Journal Light", "Papel Aquecido", Color(0xFFF3EFE6), Color(0xFFFFFFFF), Color(0xFF0284C7)),
            ThemeSwatchInfo("Amoled", "Pure AMOLED", "Preto Absoluto", Color(0xFF000000), Color(0xFF121215), Color(0xFF38BDF8)),
            ThemeSwatchInfo("Cyberpunk", "Cyberpunk", "Neon High-Tech", Color(0xFF090614), Color(0xFF150E2A), Color(0xFF00F0FF)),
            ThemeSwatchInfo("Nord", "Nord Frost", "Ardósia Ártica", Color(0xFF242933), Color(0xFF2E3440), Color(0xFF88C0D0)),
            ThemeSwatchInfo("Monet", "Material You", "Tons Dinâmicos", Color(0xFF191C1E), Color(0xFF23272B), Color(0xFFA8C7FA)),
            ThemeSwatchInfo("Incognito", "Stealth 🕵", "Modo Anônimo", Color(0xFF0D0B12), Color(0xFF171321), Color(0xFFA855F7))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
            .padding(top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TabHeader(
            title = "Aparência",
            subtitle = "Paletas editoriais Tessera Core, geometria e escala das teclas"
        )

        KeyboardLivePreview(
            themeName = keyboardTheme,
            keyShape = keyShape,
            keyHints = keyHints,
            numberRow = numberRow,
            scale = keyboardScale.coerceIn(0.85f, 1.25f),
            modifier = Modifier.fillMaxWidth()
        )

        // PALETA E TEMA (Cards com Swatches Visuais)
        Column(modifier = Modifier.fillMaxWidth()) {
            TesseraSectionHeader(title = "PALETA & TEMA VISUAL")
            TesseraGroupCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    themeSwatches.chunked(2).forEach { rowThemes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowThemes.forEach { swatch ->
                                val isSelected = keyboardTheme == swatch.id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isSelected) LuminaTokens.TesseraPillActiveBg else LuminaTokens.TesseraPillTrackBg
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) LuminaTokens.LuminaCyan else LuminaTokens.TesseraCardBorder,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { onThemeChange(swatch.id) }
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Mini amostra da paleta (Fundo + Tecla + Ponto de Destaque)
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(swatch.bgColor)
                                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(RoundedCornerShape(5.dp))
                                                    .background(swatch.keyColor)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(3.dp)
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(swatch.accentColor)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = swatch.title,
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                                ),
                                                color = LuminaTokens.TextPureLight,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = swatch.subtitle,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                                color = if (isSelected) LuminaTokens.LuminaCyan else LuminaTokens.TextMutedSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                            if (rowThemes.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // GEOMETRIA E PROPORÇÕES
        Column(modifier = Modifier.fillMaxWidth()) {
            TesseraSectionHeader(title = "GEOMETRIA, ESCALA & RASTRO")
            TesseraGroupCard {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Formato das Teclas (Segmented Control)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Geometria das Teclas",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = LuminaTokens.TextPureLight
                        )
                        Text(
                            text = "Contorno e esculpido das teclas no teclado:",
                            style = MaterialTheme.typography.bodySmall,
                            color = LuminaTokens.TextMutedSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        TesseraSegmentedControl(
                            options = listOf(
                                "squircle" to "Esculpido",
                                "pill" to "Circular",
                                "borderless" to "Sem Bordas"
                            ),
                            selectedValue = keyShape,
                            onValueSelected = onKeyShapeChange
                        )
                    }

                    TesseraItemDivider(withIconOffset = false)

                    // 2. Altura do Teclado
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Escala Vertical do Teclado",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = LuminaTokens.TextPureLight
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if ((keyboardScale * 100).toInt() != 100) {
                                    Text(
                                        text = "Padrão",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LuminaTokens.TextMutedSecondary,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(LuminaTokens.TesseraPillTrackBg)
                                            .clickable { onScaleChange(1.0f) }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "${(keyboardScale * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = LuminaTokens.LuminaCyan
                                )
                            }
                        }
                        Slider(
                            value = keyboardScale.coerceIn(0.85f, 1.25f),
                            onValueChange = { onScaleChange(it.coerceIn(0.85f, 1.25f)) },
                            valueRange = 0.85f..1.25f,
                            steps = 7,
                            colors = SliderDefaults.colors(
                                thumbColor = LuminaTokens.LuminaCyan,
                                activeTrackColor = LuminaTokens.LuminaCyan,
                                inactiveTrackColor = LuminaTokens.TesseraPillTrackBg
                            )
                        )
                    }

                    TesseraItemDivider(withIconOffset = false)

                    // 3. Rastro de Deslize (Glide Trail) com bolinhas de cor
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Cor do Rastro de Deslize (Glide Trail)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = LuminaTokens.TextPureLight
                        )
                        val trailOptions = listOf(
                            Triple("Theme", "Auto", LuminaTokens.LuminaCyan),
                            Triple("Cyan", "Cyan", Color(0xFF00E5FF)),
                            Triple("Magenta", "Pink", Color(0xFFFF2E93)),
                            Triple("Amber", "Amber", Color(0xFFFFB300)),
                            Triple("White", "Puro", Color(0xFFFFFFFF))
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            trailOptions.forEach { (opt, label, dotColor) ->
                                val isSel = glideTrailStyle == opt
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSel) LuminaTokens.TesseraPillActiveBg else LuminaTokens.TesseraPillTrackBg
                                        )
                                        .border(
                                            1.dp,
                                            if (isSel) LuminaTokens.LuminaCyan else LuminaTokens.TesseraCardBorder,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onGlideTrailStyleChange(opt) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(dotColor)
                                        )
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.5.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (isSel) LuminaTokens.TextPureLight else LuminaTokens.TextMutedSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    TesseraItemDivider(withIconOffset = false)

                    // 4. Estilo dos Popups ao Tocar
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Estilo dos Balões de Tecla (Popup)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = LuminaTokens.TextPureLight
                        )
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
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
            .padding(top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TabHeader(
            title = "Dicionário & Snippets",
            subtitle = "Expansões rápidas de texto (!pix, !email) e vocabulário pessoal"
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
