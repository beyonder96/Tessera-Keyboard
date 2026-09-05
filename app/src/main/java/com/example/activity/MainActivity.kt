package com.example.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.manager.LocalDictionaryManager
import com.example.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize().imePadding(),
                    containerColor = Slate950
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Slate950, Slate900)
                                )
                            )
                            .padding(innerPadding)
                    ) {
                        TesseraDashboardContainer(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

// Helpers para verificar o status do teclado no sistema operacional
fun isKeyboardEnabled(context: Context): Boolean {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return false
    val list = imm.enabledInputMethodList
    for (info in list) {
        if (info.packageName == context.packageName) {
            return true
        }
    }
    return false
}

fun isKeyboardSelected(context: Context): Boolean {
    val currentInputMethodId = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.DEFAULT_INPUT_METHOD
    )
    return currentInputMethodId != null && currentInputMethodId.startsWith(context.packageName)
}

// ==========================================
// CONTAINER COM LÓGICA E ESTADO
// ==========================================
@Composable
fun TesseraDashboardContainer(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("StitchPrefs", Context.MODE_PRIVATE) }
    val dictManager = remember { LocalDictionaryManager(context) }

    // Estados de ciclo de vida e sistema
    var isLoading by remember { mutableStateOf(true) }
    var isEnabled by remember { mutableStateOf(false) }
    var isSelected by remember { mutableStateOf(false) }

    // Preferências de Digitação
    var autocorrect by remember { mutableStateOf(prefs.getBoolean("PREF_AUTOCORRECT", true)) }
    var smartAbbr by remember { mutableStateOf(prefs.getBoolean("PREF_SMART_ABBR", true)) }
    var doubleSpacePeriod by remember { mutableStateOf(prefs.getBoolean("PREF_DOUBLE_SPACE_PERIOD", true)) }
    var autoCap by remember { mutableStateOf(prefs.getBoolean("PREF_AUTO_CAP", true)) }
    var keyPopup by remember { mutableStateOf(prefs.getBoolean("PREF_KEY_POPUP", true)) }

    // Preferências de Feedback
    var hapticFeedback by remember { mutableStateOf(prefs.getBoolean("PREF_HAPTIC_FEEDBACK", true)) }
    var soundFeedback by remember { mutableStateOf(prefs.getBoolean("PREF_SOUND_FEEDBACK", false)) }

    // Tema e Escala
    var keyboardTheme by remember { mutableStateOf(prefs.getString("KEYBOARD_THEME", "Dark") ?: "Dark") }
    var keyboardScale by remember { mutableStateOf(prefs.getFloat("KEYBOARD_SCALE", 1.0f)) }

    // Dicionário pessoal
    var learnedWords by remember { mutableStateOf(dictManager.getWords().toList().sorted()) }

    // Permissão de microfone
    var hasMicPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasMicPermission = granted
    }

    // Polling contínuo de status com delay suave
    LaunchedEffect(Unit) {
        delay(200) // Simula carregamento suave inicial (Loading State)
        isLoading = false
        while (true) {
            isEnabled = isKeyboardEnabled(context)
            isSelected = isKeyboardSelected(context)
            delay(1000)
        }
    }

    Crossfade(
        targetState = isLoading,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "DashboardCrossfade"
    ) { loading ->
        if (loading) {
            LoadingSkeletonView(modifier = modifier)
        } else {
            TesseraDashboardContent(
                modifier = modifier,
                isEnabled = isEnabled,
                isSelected = isSelected,
                autocorrect = autocorrect,
                onAutocorrectChange = {
                    autocorrect = it
                    prefs.edit().putBoolean("PREF_AUTOCORRECT", it).apply()
                },
                smartAbbr = smartAbbr,
                onSmartAbbrChange = {
                    smartAbbr = it
                    prefs.edit().putBoolean("PREF_SMART_ABBR", it).apply()
                },
                doubleSpacePeriod = doubleSpacePeriod,
                onDoubleSpacePeriodChange = {
                    doubleSpacePeriod = it
                    prefs.edit().putBoolean("PREF_DOUBLE_SPACE_PERIOD", it).apply()
                },
                autoCap = autoCap,
                onAutoCapChange = {
                    autoCap = it
                    prefs.edit().putBoolean("PREF_AUTO_CAP", it).apply()
                },
                keyPopup = keyPopup,
                onKeyPopupChange = {
                    keyPopup = it
                    prefs.edit().putBoolean("PREF_KEY_POPUP", it).apply()
                },
                hapticFeedback = hapticFeedback,
                onHapticFeedbackChange = {
                    hapticFeedback = it
                    prefs.edit().putBoolean("PREF_HAPTIC_FEEDBACK", it).apply()
                },
                soundFeedback = soundFeedback,
                onSoundFeedbackChange = {
                    soundFeedback = it
                    prefs.edit().putBoolean("PREF_SOUND_FEEDBACK", it).apply()
                },
                keyboardTheme = keyboardTheme,
                onThemeChange = {
                    keyboardTheme = it
                    prefs.edit().putString("KEYBOARD_THEME", it).apply()
                },
                keyboardScale = keyboardScale,
                onScaleChange = {
                    keyboardScale = it
                    prefs.edit().putFloat("KEYBOARD_SCALE", it).apply()
                },
                hasMicPermission = hasMicPermission,
                onRequestMicPermission = {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                learnedWords = learnedWords,
                onAddWord = { word ->
                    dictManager.learnWord(word)
                    learnedWords = dictManager.getWords().toList().sorted()
                },
                onRemoveWord = { word ->
                    dictManager.removeWord(word)
                    learnedWords = dictManager.getWords().toList().sorted()
                },
                onClearWords = {
                    dictManager.clearWords()
                    learnedWords = emptyList()
                },
                onEnableKeyboardClick = {
                    try {
                        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        Toast.makeText(context, "Ative o 'Tessera' na lista", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                onSelectKeyboardClick = {
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.showInputMethodPicker()
                }
            )
        }
    }
}

// ==========================================
// COMPONENTES DE UI PUROS (DUMB)
// ==========================================

@Composable
fun TesseraDashboardContent(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    isSelected: Boolean,
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
    hapticFeedback: Boolean,
    onHapticFeedbackChange: (Boolean) -> Unit,
    soundFeedback: Boolean,
    onSoundFeedbackChange: (Boolean) -> Unit,
    keyboardTheme: String,
    onThemeChange: (String) -> Unit,
    keyboardScale: Float,
    onScaleChange: (Float) -> Unit,
    hasMicPermission: Boolean,
    onRequestMicPermission: () -> Unit,
    learnedWords: List<String>,
    onAddWord: (String) -> Unit,
    onRemoveWord: (String) -> Unit,
    onClearWords: () -> Unit,
    onEnableKeyboardClick: () -> Unit,
    onSelectKeyboardClick: () -> Unit
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Cabeçalho de Identidade
        TesseraHeader()

        // Card de Status do Teclado (com Error / Warning / Success states)
        KeyboardStatusCard(
            isEnabled = isEnabled,
            isSelected = isSelected,
            onEnableClick = onEnableKeyboardClick,
            onSelectClick = onSelectKeyboardClick
        )

        // Seção: Preferências de Digitação (Otimizações Principais)
        SectionCard(title = "Digitação e Otimização") {
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
                title = "Pré-visualização de tecla",
                description = "Exibe popup com o caractere pressionado",
                checked = keyPopup,
                onCheckedChange = onKeyPopupChange
            )
        }

        // Seção: Feedback e Resposta Tátil
        SectionCard(title = "Resposta e Feedback") {
            SettingToggle(
                title = "Resposta tátil (vibração)",
                description = "Vibração sutil a cada caractere digitado",
                checked = hapticFeedback,
                onCheckedChange = onHapticFeedbackChange
            )
            DividerLine()
            SettingToggle(
                title = "Som ao tocar",
                description = "Clique de áudio leve e assíncrono (zero atraso na digitação)",
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

        // Seção: Aparência e Escala
        SectionCard(title = "Aparência e Escala") {
            Text(
                text = "Estilo de Vidro",
                style = MaterialTheme.typography.titleSmall,
                color = Slate100
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionButton(
                    title = "Dark Glass",
                    isSelected = keyboardTheme == "Dark",
                    onClick = { onThemeChange("Dark") },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionButton(
                    title = "Light Glass",
                    isSelected = keyboardTheme == "Light",
                    onClick = { onThemeChange("Light") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            DividerLine()
            Spacer(modifier = Modifier.height(12.dp))

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
                value = keyboardScale,
                onValueChange = onScaleChange,
                valueRange = 0.85f..1.25f,
                steps = 7,
                colors = SliderDefaults.colors(
                    thumbColor = AccentSky,
                    activeTrackColor = AccentSky,
                    inactiveTrackColor = Slate800
                )
            )
        }

        // Seção: Dicionário Pessoal & Palavras Aprendidas (com Empty State obrigatório)
        DictionarySection(
            words = learnedWords,
            onAddWord = onAddWord,
            onRemoveWord = onRemoveWord,
            onClearAll = onClearWords
        )

        // Seção: Laboratório de Testes (Demonstra QWERTY e o novo Teclado Numérico Automático)
        PlaygroundSection(isEnabled = isEnabled && isSelected)

        // Rodapé Minimalista
        Text(
            text = "Tessera Keyboard • Minimalismo & Máxima Performance",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate600,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
    }
}

// ==========================================
// SUBCOMPONENTES E ESTADOS OBRIGATÓRIOS
// ==========================================

@Composable
fun TesseraHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Slate900, RoundedCornerShape(12.dp))
                .border(1.dp, Slate800, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Keyboard,
                contentDescription = "Tessera",
                tint = AccentSky,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = "TESSERA",
            style = MaterialTheme.typography.titleLarge,
            color = Slate100
        )
        Text(
            text = "Teclado minimalista com latência ultra-baixa",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate400,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun KeyboardStatusCard(
    isEnabled: Boolean,
    isSelected: Boolean,
    onEnableClick: () -> Unit,
    onSelectClick: () -> Unit
) {
    val (statusTitle, statusDesc, statusColor, isReady) = when {
        !isEnabled -> Quadruple("Teclado Desativado", "O Tessera precisa ser ativado nas configurações do sistema.", StateError, false)
        !isSelected -> Quadruple("Teclado Não Selecionado", "O Tessera está ativo, mas outro teclado está em uso.", StateWarning, false)
        else -> Quadruple("Teclado Pronto para Uso", "O Tessera está selecionado como seu teclado padrão.", StateSuccess, true)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate900, RoundedCornerShape(12.dp))
            .border(1.dp, if (isReady) Slate800 else statusColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(statusColor, RoundedCornerShape(5.dp))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = statusTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = Slate100
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = statusDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate400
                    )
                }
            }

            if (!isReady) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isEnabled) {
                        Button(
                            onClick = onEnableClick,
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentSky,
                                contentColor = Slate950
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("1. Ativar no Sistema", style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        Button(
                            onClick = onSelectClick,
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentSky,
                                contentColor = Slate950
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("2. Selecionar Teclado", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate900, RoundedCornerShape(12.dp))
            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Slate100
        )
        Spacer(modifier = Modifier.height(2.dp))
        content()
    }
}

@Composable
fun SettingToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Slate100
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate400
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Slate950,
                checkedTrackColor = AccentSky,
                uncheckedThumbColor = Slate400,
                uncheckedTrackColor = Slate800,
                uncheckedBorderColor = Slate700
            )
        )
    }
}

@Composable
fun ThemeOptionButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) AccentSkyMuted else Slate850
    val borderColor = if (isSelected) AccentSky else Slate700
    val textColor = if (isSelected) AccentSky else Slate300

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

@Composable
fun DictionarySection(
    words: List<String>,
    onAddWord: (String) -> Unit,
    onRemoveWord: (String) -> Unit,
    onClearAll: () -> Unit
) {
    var newWordInput by remember { mutableStateOf("") }

    SectionCard(title = "Dicionário Pessoal") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newWordInput,
                onValueChange = { newWordInput = it },
                placeholder = {
                    Text(
                        "Adicionar palavra...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Slate600
                    )
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Slate100),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentSky,
                    unfocusedBorderColor = Slate700,
                    focusedContainerColor = Slate850,
                    unfocusedContainerColor = Slate850
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Button(
                onClick = {
                    val clean = newWordInput.trim()
                    if (clean.isNotEmpty()) {
                        onAddWord(clean)
                        newWordInput = ""
                    }
                },
                modifier = Modifier.height(52.dp),
                enabled = newWordInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentSky,
                    contentColor = Slate950,
                    disabledContainerColor = Slate800,
                    disabledContentColor = Slate600
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar", modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // EMPTY STATE (Obrigatório conforme regras de design)
        if (words.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate850, RoundedCornerShape(8.dp))
                    .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Nenhuma palavra aprendida",
                        style = MaterialTheme.typography.titleSmall,
                        color = Slate300
                    )
                    Text(
                        text = "Palavras adicionadas ou aprendidas durante a digitação aparecerão aqui.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate400,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${words.size} palavras aprendidas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate400
                )
                Text(
                    text = "Limpar tudo",
                    style = MaterialTheme.typography.labelSmall,
                    color = StateError,
                    modifier = Modifier
                        .clickable(onClick = onClearAll)
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Lista de tags das palavras aprendidas
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                words.chunked(3).forEach { rowWords ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowWords.forEach { word ->
                            WordChip(
                                word = word,
                                onRemove = { onRemoveWord(word) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Preenche espaço se a linha tiver menos de 3 itens
                        repeat(3 - rowWords.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WordChip(
    word: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Slate850, RoundedCornerShape(6.dp))
            .border(1.dp, Slate700, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = word,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate100,
            maxLines = 1
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remover",
            tint = Slate400,
            modifier = Modifier
                .size(14.dp)
                .clickable(onClick = onRemove)
        )
    }
}

@Composable
fun PlaygroundSection(isEnabled: Boolean) {
    var generalText by remember { mutableStateOf("") }
    var numberText by remember { mutableStateOf("") }

    SectionCard(title = "Laboratório de Digitação") {
        Text(
            text = "Experimente a latência imediata e a troca inteligente de teclado:",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate400
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Campo 1: Texto Geral (QWERTY)
        Text(
            text = "Campo Geral (QWERTY + Abreviações)",
            style = MaterialTheme.typography.labelMedium,
            color = Slate300
        )
        OutlinedTextField(
            value = generalText,
            onValueChange = { generalText = it },
            placeholder = {
                Text(
                    "Digite 'vc', 'tbm', 'pq' ou símbolos como '/'...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Slate600
                )
            },
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (isEnabled) 1.0f else 0.45f), // DISABLED STATE
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Slate100),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentSky,
                unfocusedBorderColor = Slate700,
                focusedContainerColor = Slate850,
                unfocusedContainerColor = Slate850
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo 2: Campo Numérico (Abre automaticamente o teclado numérico estilo Gboard!)
        Text(
            text = "Campo Numérico (Abre modo 1 2 3 dedicado)",
            style = MaterialTheme.typography.labelMedium,
            color = Slate300
        )
        OutlinedTextField(
            value = numberText,
            onValueChange = { numberText = it },
            placeholder = {
                Text(
                    "Toque aqui para abrir o teclado numérico...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Slate600
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (isEnabled) 1.0f else 0.45f), // DISABLED STATE
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Slate100),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentSky,
                unfocusedBorderColor = Slate700,
                focusedContainerColor = Slate850,
                unfocusedContainerColor = Slate850
            ),
            shape = RoundedCornerShape(8.dp)
        )

        if (!isEnabled) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ative e selecione o Tessera para testar neste laboratório.",
                style = MaterialTheme.typography.bodyMedium,
                color = StateWarning
            )
        }
    }
}

// LOADING SKELETON (Obrigatório conforme regras de design)
@Composable
fun LoadingSkeletonView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Slate900, RoundedCornerShape(12.dp))
        )
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(24.dp)
                .background(Slate900, RoundedCornerShape(6.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Slate900, RoundedCornerShape(12.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Slate900, RoundedCornerShape(12.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Slate900, RoundedCornerShape(12.dp))
        )
    }
}

@Composable
fun DividerLine() {
    HorizontalDivider(
        color = Slate800,
        thickness = 1.dp
    )
}

@Composable
fun StitchDashboardScreen(modifier: Modifier = Modifier) {
    TesseraDashboardContainer(modifier)
}

// Data holder utilitário para status
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
