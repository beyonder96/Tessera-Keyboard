package com.example.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.example.manager.SnippetManager
import com.example.ui.widget.KeyboardLivePreview
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.*
import com.example.ui.lumina.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge(
                statusBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
                navigationBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            )
        } catch (_: Exception) {}

        setContent {
            MyApplicationTheme {
                var fatalError by remember { mutableStateOf<String?>(null) }

                LuminaAtmosphere(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (fatalError != null) {
                        CrashRecoveryScreen(
                            errorMessage = fatalError!!,
                            onRetry = { fatalError = null },
                            onResetPreferences = {
                                try {
                                    getSharedPreferences("StitchPrefs", Context.MODE_PRIVATE).edit().clear().apply()
                                    getSharedPreferences("LocalDictionary", Context.MODE_PRIVATE).edit().clear().apply()
                                } catch (_: Exception) {}
                                fatalError = null
                            }
                        )
                    } else {
                        TesseraDashboardContainer(
                            modifier = Modifier.fillMaxSize(),
                            onError = { fatalError = it }
                        )
                    }
                }
            }
        }
    }
}

// Helpers para verificar o status do teclado no sistema operacional
fun isKeyboardEnabled(context: Context): Boolean {
    return try {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return false
        val list = imm.enabledInputMethodList ?: return false
        for (info in list) {
            if (info != null && info.packageName == context.packageName) {
                return true
            }
        }
        false
    } catch (_: Exception) {
        false
    }
}

fun isKeyboardSelected(context: Context): Boolean {
    return try {
        val currentInputMethodId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        ) ?: ""
        currentInputMethodId.contains(context.packageName)
    } catch (_: Exception) {
        false
    }
}

// ==========================================
// CONTAINER COM LÓGICA E ESTADO
// ==========================================
@Composable
fun TesseraDashboardContainer(
    modifier: Modifier = Modifier,
    onError: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("StitchPrefs", Context.MODE_PRIVATE) }
    val dictManager = remember { LocalDictionaryManager(context) }

    // Estados de ciclo de vida e sistema
    var isLoading by remember { mutableStateOf(true) }
    var isEnabled by remember { mutableStateOf(false) }
    var isSelected by remember { mutableStateOf(false) }

    // Preferências de Digitação protegidas contra corrupção
    var autocorrect by remember { mutableStateOf(try { prefs.getBoolean("PREF_AUTOCORRECT", true) } catch (_: Exception) { true }) }
    var smartAbbr by remember { mutableStateOf(try { prefs.getBoolean("PREF_SMART_ABBR", true) } catch (_: Exception) { true }) }
    var doubleSpacePeriod by remember { mutableStateOf(try { prefs.getBoolean("PREF_DOUBLE_SPACE_PERIOD", true) } catch (_: Exception) { true }) }
    var autoCap by remember { mutableStateOf(try { prefs.getBoolean("PREF_AUTO_CAP", true) } catch (_: Exception) { true }) }
    var keyPopup by remember { mutableStateOf(try { prefs.getBoolean("PREF_KEY_POPUP", true) } catch (_: Exception) { true }) }
    var numberRow by remember { mutableStateOf(try { prefs.getBoolean("PREF_NUMBER_ROW", false) } catch (_: Exception) { false }) }
    var glideTyping by remember { mutableStateOf(try { prefs.getBoolean("PREF_GLIDE_TYPING", true) } catch (_: Exception) { true }) }

    // Preferências de Feedback
    var hapticFeedback by remember { mutableStateOf(try { prefs.getBoolean("PREF_HAPTIC_FEEDBACK", true) } catch (_: Exception) { true }) }
    var hapticDurationMs by remember {
        val raw = try { prefs.getInt("PREF_HAPTIC_DURATION_MS", 15) } catch (_: Exception) { 15 }
        mutableStateOf(raw.coerceIn(1, 50))
    }
    var soundProfile by remember {
        mutableStateOf(prefs.getString("PREF_SOUND_PROFILE", "thock") ?: "thock")
    }
    var adaptiveHitboxes by remember {
        mutableStateOf(prefs.getBoolean("PREF_ADAPTIVE_HITBOXES", true))
    }
    var soundFeedback by remember { mutableStateOf(try { prefs.getBoolean("PREF_SOUND_FEEDBACK", false) } catch (_: Exception) { false }) }

    // Tema, Formato, Trilha e Escala
    var keyboardTheme by remember { mutableStateOf(try { prefs.getString("KEYBOARD_THEME", "Dark") ?: "Dark" } catch (_: Exception) { "Dark" }) }
    var keyboardScale by remember {
        val raw = try { prefs.getFloat("KEYBOARD_SCALE", 1.0f) } catch (_: Exception) { 1.0f }
        val safe = if (raw.isNaN() || raw.isInfinite() || raw < 0.5f) 1.0f else raw
        mutableStateOf(safe.coerceIn(0.85f, 1.25f))
    }
    var popupStyle by remember { mutableStateOf(try { prefs.getString("PREF_POPUP_STYLE", "dark_glass") ?: "dark_glass" } catch (_: Exception) { "dark_glass" }) }
    var keyShape by remember { mutableStateOf(try { prefs.getString("PREF_KEY_SHAPE", "squircle") ?: "squircle" } catch (_: Exception) { "squircle" }) }
    var keyHints by remember { mutableStateOf(try { prefs.getBoolean("PREF_KEY_HINTS", true) } catch (_: Exception) { true }) }
    var glideTrailStyle by remember { mutableStateOf(try { prefs.getString("PREF_GLIDE_TRAIL_STYLE", "Theme") ?: "Theme" } catch (_: Exception) { "Theme" }) }
    val snippetManager = remember { SnippetManager(context) }
    var snippets by remember { mutableStateOf(snippetManager.getSnippets()) }

    // Dicionário pessoal (apenas palavras adicionadas manualmente)
    var learnedWords by remember { mutableStateOf(try { dictManager.getManualWords().toList().sorted() } catch (_: Exception) { emptyList() }) }

    // Permissão de microfone
    var hasMicPermission by remember {
        mutableStateOf(try { ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED } catch (_: Exception) { false })
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasMicPermission = granted
    }

    // Permissão de fotos/prints recentes (capturas de tela)
    val screenshotPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    var hasScreenshotPermission by remember {
        mutableStateOf(try { ContextCompat.checkSelfPermission(context, screenshotPermission) == PackageManager.PERMISSION_GRANTED } catch (_: Exception) { false })
    }
    val screenshotPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasScreenshotPermission = granted
        if (granted) {
            Toast.makeText(context, "Permissão concedida! Prints aparecerão no teclado.", Toast.LENGTH_SHORT).show()
        }
    }
    var suggestScreenshots by remember { mutableStateOf(try { prefs.getBoolean("PREF_SUGGEST_SCREENSHOTS", true) } catch (_: Exception) { true }) }
    var flickAccents by remember { mutableStateOf(try { prefs.getBoolean("PREF_FLICK_ACCENTS", true) } catch (_: Exception) { true }) }
    var swipeDownHide by remember { mutableStateOf(try { prefs.getBoolean("PREF_SWIPE_DOWN_HIDE", true) } catch (_: Exception) { true }) }
    var undoDelete by remember { mutableStateOf(try { prefs.getBoolean("PREF_UNDO_DELETE", true) } catch (_: Exception) { true }) }
    var aiParticipative by remember { mutableStateOf(try { prefs.getBoolean("PREF_AI_PARTICIPATIVE", true) } catch (_: Exception) { true }) }
    var aiPassiveContinuous by remember { mutableStateOf(try { prefs.getBoolean("PREF_AI_PASSIVE_CONTINUOUS", false) } catch (_: Exception) { false }) }
    var aiProvider by remember { mutableStateOf(try { prefs.getString("PREF_AI_PROVIDER", "groq") ?: "groq" } catch (_: Exception) { "groq" }) }
    var groqApiKey by remember { mutableStateOf(try { prefs.getString("PREF_GROQ_API_KEY", "") ?: "" } catch (_: Exception) { "" }) }
    var geminiApiKey by remember { mutableStateOf(try { prefs.getString("PREF_GEMINI_API_KEY", "") ?: "" } catch (_: Exception) { "" }) }

    // Atualização OTA via GitHub Releases (Tessera Core Update Pill)
    var updateInfo by remember { mutableStateOf<com.example.update.AppUpdateInfo?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val info = com.example.update.TesseraUpdateManager.checkForUpdates(context)
            if (info != null && info.hasUpdate) {
                updateInfo = info
            }
        } catch (_: Exception) {}
    }

    // Polling contínuo de status com delay suave
    LaunchedEffect(Unit) {
        delay(200) // Simula carregamento suave inicial (Loading State)
        isLoading = false
        while (true) {
            try {
                isEnabled = isKeyboardEnabled(context)
                isSelected = isKeyboardSelected(context)
                hasScreenshotPermission = ContextCompat.checkSelfPermission(context, screenshotPermission) == PackageManager.PERMISSION_GRANTED
                hasMicPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            } catch (_: Exception) {
            }
            delay(1000)
        }
    }

    if (showUpdateDialog && updateInfo != null) {
        com.example.update.TesseraKeyboardUpdateDialog(
            updateInfo = updateInfo!!,
            onDismiss = { showUpdateDialog = false },
            onDownload = {
                showUpdateDialog = false
                com.example.update.TesseraUpdateManager.launchDownload(context, updateInfo!!.downloadUrl)
            }
        )
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
                updateInfo = updateInfo,
                onUpdatePillClick = { showUpdateDialog = true },
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
                numberRow = numberRow,
                onNumberRowChange = {
                    numberRow = it
                    prefs.edit().putBoolean("PREF_NUMBER_ROW", it).apply()
                },
                glideTyping = glideTyping,
                onGlideTypingChange = {
                    glideTyping = it
                    prefs.edit().putBoolean("PREF_GLIDE_TYPING", it).apply()
                },
                suggestScreenshots = suggestScreenshots,
                onSuggestScreenshotsChange = {
                    suggestScreenshots = it
                    prefs.edit().putBoolean("PREF_SUGGEST_SCREENSHOTS", it).apply()
                },
                hasScreenshotPermission = hasScreenshotPermission,
                onRequestScreenshotPermission = {
                    screenshotPermissionLauncher.launch(screenshotPermission)
                },
                flickAccents = flickAccents,
                onFlickAccentsChange = {
                    flickAccents = it
                    prefs.edit().putBoolean("PREF_FLICK_ACCENTS", it).apply()
                },
                swipeDownHide = swipeDownHide,
                onSwipeDownHideChange = {
                    swipeDownHide = it
                    prefs.edit().putBoolean("PREF_SWIPE_DOWN_HIDE", it).apply()
                },
                undoDelete = undoDelete,
                onUndoDeleteChange = {
                    undoDelete = it
                    prefs.edit().putBoolean("PREF_UNDO_DELETE", it).apply()
                },
                adaptiveHitboxes = adaptiveHitboxes,
                onAdaptiveHitboxesChange = {
                    adaptiveHitboxes = it
                    prefs.edit().putBoolean("PREF_ADAPTIVE_HITBOXES", it).apply()
                },
                aiParticipative = aiParticipative,
                onAiParticipativeChange = {
                    aiParticipative = it
                    prefs.edit().putBoolean("PREF_AI_PARTICIPATIVE", it).apply()
                },
                aiPassiveContinuous = aiPassiveContinuous,
                onAiPassiveContinuousChange = {
                    aiPassiveContinuous = it
                    prefs.edit().putBoolean("PREF_AI_PASSIVE_CONTINUOUS", it).apply()
                },
                aiProvider = aiProvider,
                onAiProviderChange = {
                    aiProvider = it
                    prefs.edit().putString("PREF_AI_PROVIDER", it).apply()
                },
                groqApiKey = groqApiKey,
                onGroqApiKeyChange = {
                    val sanitized = it.trim().removePrefix("Bearer ").removePrefix("bearer ").trim()
                    groqApiKey = sanitized
                    prefs.edit().putString("PREF_GROQ_API_KEY", sanitized).apply()
                },
                geminiApiKey = geminiApiKey,
                onGeminiApiKeyChange = {
                    val sanitized = it.trim().removePrefix("Bearer ").removePrefix("bearer ").trim()
                    geminiApiKey = sanitized
                    prefs.edit().putString("PREF_GEMINI_API_KEY", sanitized).apply()
                },
                hapticFeedback = hapticFeedback,
                onHapticFeedbackChange = {
                    hapticFeedback = it
                    prefs.edit().putBoolean("PREF_HAPTIC_FEEDBACK", it).apply()
                },
                hapticDurationMs = hapticDurationMs,
                onHapticDurationChange = { ms ->
                    hapticDurationMs = ms
                    prefs.edit().putInt("PREF_HAPTIC_DURATION_MS", ms).apply()
                    try {
                        val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vib?.vibrate(android.os.VibrationEffect.createOneShot(ms.toLong(), android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            vib?.vibrate(ms.toLong())
                        }
                    } catch (_: Exception) {}
                },
                soundFeedback = soundFeedback,
                onSoundFeedbackChange = {
                    soundFeedback = it
                    prefs.edit().putBoolean("PREF_SOUND_FEEDBACK", it).apply()
                },
                soundProfile = soundProfile,
                onSoundProfileChange = {
                    soundProfile = it
                    prefs.edit().putString("PREF_SOUND_PROFILE", it).apply()
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
                popupStyle = popupStyle,
                onPopupStyleChange = {
                    popupStyle = it
                    prefs.edit().putString("PREF_POPUP_STYLE", it).apply()
                },
                keyShape = keyShape,
                onKeyShapeChange = {
                    keyShape = it
                    prefs.edit().putString("PREF_KEY_SHAPE", it).apply()
                },
                keyHints = keyHints,
                onKeyHintsChange = {
                    keyHints = it
                    prefs.edit().putBoolean("PREF_KEY_HINTS", it).apply()
                },
                glideTrailStyle = glideTrailStyle,
                onGlideTrailStyleChange = {
                    glideTrailStyle = it
                    prefs.edit().putString("PREF_GLIDE_TRAIL_STYLE", it).apply()
                },
                snippets = snippets,
                onAddSnippet = { shortcut, text ->
                    snippetManager.addSnippet(shortcut, text)
                    snippets = snippetManager.getSnippets()
                },
                onRemoveSnippet = { shortcut ->
                    snippetManager.removeSnippet(shortcut)
                    snippets = snippetManager.getSnippets()
                },
                hasMicPermission = hasMicPermission,
                onRequestMicPermission = {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                learnedWords = learnedWords,
                onAddWord = { word ->
                    dictManager.addManualWord(word)
                    learnedWords = dictManager.getManualWords().toList().sorted()
                },
                onRemoveWord = { word ->
                    dictManager.removeManualWord(word)
                    learnedWords = dictManager.getManualWords().toList().sorted()
                },
                onClearWords = {
                    dictManager.clearManualWords()
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
    updateInfo: com.example.update.AppUpdateInfo? = null,
    onUpdatePillClick: () -> Unit = {},
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
    suggestScreenshots: Boolean,
    onSuggestScreenshotsChange: (Boolean) -> Unit,
    hasScreenshotPermission: Boolean,
    onRequestScreenshotPermission: () -> Unit,
    flickAccents: Boolean,
    onFlickAccentsChange: (Boolean) -> Unit,
    swipeDownHide: Boolean,
    onSwipeDownHideChange: (Boolean) -> Unit,
    undoDelete: Boolean,
    onUndoDeleteChange: (Boolean) -> Unit,
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
    onGeminiApiKeyChange: (String) -> Unit = {},
    hapticFeedback: Boolean,
    onHapticFeedbackChange: (Boolean) -> Unit,
    hapticDurationMs: Int,
    onHapticDurationChange: (Int) -> Unit,
    soundFeedback: Boolean,
    onSoundFeedbackChange: (Boolean) -> Unit,
    soundProfile: String = "thock",
    onSoundProfileChange: (String) -> Unit = {},
    keyboardTheme: String,
    onThemeChange: (String) -> Unit,
    keyboardScale: Float,
    onScaleChange: (Float) -> Unit,
    popupStyle: String,
    onPopupStyleChange: (String) -> Unit,
    keyShape: String,
    onKeyShapeChange: (String) -> Unit,
    keyHints: Boolean,
    onKeyHintsChange: (Boolean) -> Unit,
    glideTrailStyle: String,
    onGlideTrailStyleChange: (String) -> Unit,
    snippets: Map<String, String>,
    onAddSnippet: (String, String) -> Unit,
    onRemoveSnippet: (String) -> Unit,
    hasMicPermission: Boolean,
    onRequestMicPermission: () -> Unit,
    learnedWords: List<String>,
    onAddWord: (String) -> Unit,
    onRemoveWord: (String) -> Unit,
    onClearWords: () -> Unit,
    onEnableKeyboardClick: () -> Unit,
    onSelectKeyboardClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            val tabs = remember {
                listOf(
                    DockTabItem("Início", Icons.Default.Home, 0),
                    DockTabItem("Digitação", Icons.Default.Edit, 1),
                    DockTabItem("Aparência", Icons.Default.Palette, 2),
                    DockTabItem("Dicionário", Icons.Default.MenuBook, 3)
                )
            }
            KineticCapsuleDock(
                tabs = tabs,
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { _ ->
        Crossfade(
            targetState = selectedTab,
            modifier = Modifier.fillMaxSize(),
            label = "TabCrossfade"
        ) { tab ->
            when (tab) {
                0 -> TabHomeContent(
                    keyboardTheme = keyboardTheme,
                    keyShape = keyShape,
                    keyHints = keyHints,
                    numberRow = numberRow,
                    keyboardScale = keyboardScale,
                    isEnabled = isEnabled,
                    isSelected = isSelected,
                    updateInfo = updateInfo,
                    onUpdatePillClick = onUpdatePillClick,
                    onEnableKeyboardClick = onEnableKeyboardClick,
                    onSelectKeyboardClick = onSelectKeyboardClick
                )
                1 -> TabTypingContent(
                    autocorrect = autocorrect,
                    onAutocorrectChange = onAutocorrectChange,
                    smartAbbr = smartAbbr,
                    onSmartAbbrChange = onSmartAbbrChange,
                    doubleSpacePeriod = doubleSpacePeriod,
                    onDoubleSpacePeriodChange = onDoubleSpacePeriodChange,
                    autoCap = autoCap,
                    onAutoCapChange = onAutoCapChange,
                    keyPopup = keyPopup,
                    onKeyPopupChange = onKeyPopupChange,
                    numberRow = numberRow,
                    onNumberRowChange = onNumberRowChange,
                    glideTyping = glideTyping,
                    onGlideTypingChange = onGlideTypingChange,
                    keyHints = keyHints,
                    onKeyHintsChange = onKeyHintsChange,
                    suggestScreenshots = suggestScreenshots,
                    hasScreenshotPermission = hasScreenshotPermission,
                    onRequestScreenshotPermission = onRequestScreenshotPermission,
                    onSuggestScreenshotsChange = onSuggestScreenshotsChange,
                    flickAccents = flickAccents,
                    onFlickAccentsChange = onFlickAccentsChange,
                    swipeDownHide = swipeDownHide,
                    onSwipeDownHideChange = onSwipeDownHideChange,
                    undoDelete = undoDelete,
                    onUndoDeleteChange = onUndoDeleteChange,
                    hapticFeedback = hapticFeedback,
                    onHapticFeedbackChange = onHapticFeedbackChange,
                    hapticDurationMs = hapticDurationMs,
                    onHapticDurationChange = onHapticDurationChange,
                    soundFeedback = soundFeedback,
                    onSoundFeedbackChange = onSoundFeedbackChange,
                    hasMicPermission = hasMicPermission,
                    onRequestMicPermission = onRequestMicPermission,
                    soundProfile = soundProfile,
                    onSoundProfileChange = onSoundProfileChange,
                    adaptiveHitboxes = adaptiveHitboxes,
                    onAdaptiveHitboxesChange = onAdaptiveHitboxesChange,
                    aiParticipative = aiParticipative,
                    onAiParticipativeChange = onAiParticipativeChange,
                    aiPassiveContinuous = aiPassiveContinuous,
                    onAiPassiveContinuousChange = onAiPassiveContinuousChange,
                    aiProvider = aiProvider,
                    onAiProviderChange = onAiProviderChange,
                    groqApiKey = groqApiKey,
                    onGroqApiKeyChange = onGroqApiKeyChange,
                    geminiApiKey = geminiApiKey,
                    onGeminiApiKeyChange = onGeminiApiKeyChange
                )
                2 -> TabAppearanceContent(
                    keyboardTheme = keyboardTheme,
                    onThemeChange = onThemeChange,
                    keyShape = keyShape,
                    onKeyShapeChange = onKeyShapeChange,
                    glideTrailStyle = glideTrailStyle,
                    onGlideTrailStyleChange = onGlideTrailStyleChange,
                    keyboardScale = keyboardScale,
                    onScaleChange = onScaleChange,
                    popupStyle = popupStyle,
                    onPopupStyleChange = onPopupStyleChange,
                    numberRow = numberRow,
                    keyHints = keyHints
                )
                else -> TabDictionaryContent(
                    snippets = snippets,
                    onAddSnippet = onAddSnippet,
                    onRemoveSnippet = onRemoveSnippet,
                    learnedWords = learnedWords,
                    onAddWord = onAddWord,
                    onRemoveWord = onRemoveWord,
                    onClearWords = onClearWords
                )
            }
        }
    }
}


// ==========================================
// SUBCOMPONENTES E ESTADOS OBRIGATÓRIOS
// ==========================================

@Composable
fun TesseraLogoBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(56.dp)
            .shadow(10.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.5f))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF28282B), Color(0xFF1B1B1D))
                ),
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                Color(0x22FFFFFF),
                RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_tessera_logo),
            contentDescription = "Tessera Logo",
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
        )
    }
}

@Composable
fun TesseraHeader() {
    MonolithKeycapVisualizer()
}

@Composable
fun KeyboardStatusCard(
    isEnabled: Boolean,
    isSelected: Boolean,
    onEnableClick: () -> Unit,
    onSelectClick: () -> Unit
) {
    val (statusTitle, statusDesc, statusColor, isReady) = when {
        !isEnabled -> Quadruple(
            "Teclado Desativado",
            "Habilite o Tessera nas configurações do Android para começar.",
            LuminaTokens.LuminaCrimson,
            false
        )
        !isSelected -> Quadruple(
            "Aguardando Seleção",
            "O Tessera está habilitado, mas outro método de entrada está ativo.",
            LuminaTokens.LuminaAmber,
            false
        )
        else -> Quadruple(
            "Tessera Ativo & Pronto",
            "Motor 120Hz, precisão Voronoi e IA Participativa operacionais.",
            LuminaTokens.LuminaEmerald,
            true
        )
    }

    TesseraGroupCard {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.14f))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isReady) Icons.Default.Check else Icons.Default.Keyboard,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(statusColor, CircleShape)
                        )
                        Text(
                            text = statusTitle,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = LuminaTokens.TextPureLight
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = statusDesc,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                        color = LuminaTokens.TextMutedSecondary
                    )
                }
            }

            if (!isReady) {
                Button(
                    onClick = if (!isEnabled) onEnableClick else onSelectClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LuminaTokens.LuminaCyan,
                        contentColor = Color(0xFF090D16)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (!isEnabled) "1. Ativar nas Configurações" else "2. Selecionar Tessera como Padrão",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(LuminaTokens.TesseraPillTrackBg)
                                .border(1.dp, LuminaTokens.TesseraCardBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "IME PADRÃO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = LuminaTokens.LuminaEmerald
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = onSelectClick,
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LuminaTokens.TextPureLight),
                        border = BorderStroke(1.dp, LuminaTokens.TesseraCardBorder),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Trocar Teclado",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
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
    Column(modifier = Modifier.fillMaxWidth()) {
        TesseraSectionHeader(title = title)
        TesseraGroupCard {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    LuminaSettingRow(
        title = title,
        description = description,
        checked = checked,
        onCheckedChange = onCheckedChange
    )
}

@Composable
fun ThemeOptionButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) LuminaTokens.TesseraPillActiveBg else LuminaTokens.TesseraPillTrackBg
    val borderColor = if (isSelected) LuminaTokens.LuminaCyan else LuminaTokens.TesseraCardBorder
    val textColor = if (isSelected) LuminaTokens.TextPureLight else LuminaTokens.TextMutedSecondary

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(14.dp))
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PopupStyleOption(
    title: String,
    accentColor: Color,
    surfaceColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) accentColor else Slate700
    val bgColor = if (isSelected) accentColor.copy(alpha = 0.20f) else Slate850

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(surfaceColor, CircleShape)
                    .border(1.5.dp, accentColor, CircleShape)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) Color.White else Slate300,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SnippetsSection(
    snippets: Map<String, String>,
    onAddSnippet: (String, String) -> Unit,
    onRemoveSnippet: (String) -> Unit
) {
    var shortcutInput by remember { mutableStateOf("") }
    var textInput by remember { mutableStateOf("") }

    SectionCard(title = "Expansão de Texto & Snippets") {
        Text(
            text = "Atalhos rápidos expansíveis no teclado:",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate400
        )
        Text(
            text = "Digite o atalho (ex: !pix) seguido de espaço ou clique na sugestão para colar o texto completo.",
            style = MaterialTheme.typography.bodySmall,
            color = Slate400
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = shortcutInput,
                onValueChange = { shortcutInput = it },
                placeholder = { Text("!atalho", color = LuminaTokens.TextDimTertiary) },
                label = { Text("Atalho") },
                modifier = Modifier.weight(0.4f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = LuminaTokens.TextPureLight),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0x60FFFFFF),
                    unfocusedBorderColor = LuminaTokens.TesseraCardBorder,
                    focusedContainerColor = LuminaTokens.TesseraPillTrackBg,
                    unfocusedContainerColor = LuminaTokens.TesseraPillTrackBg
                ),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Texto...", color = LuminaTokens.TextDimTertiary) },
                label = { Text("Conteúdo") },
                modifier = Modifier.weight(0.6f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = LuminaTokens.TextPureLight),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0x60FFFFFF),
                    unfocusedBorderColor = LuminaTokens.TesseraCardBorder,
                    focusedContainerColor = LuminaTokens.TesseraPillTrackBg,
                    unfocusedContainerColor = LuminaTokens.TesseraPillTrackBg
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    val s = shortcutInput.trim()
                    val t = textInput.trim()
                    if (s.isNotEmpty() && t.isNotEmpty()) {
                        val formattedShortcut = if (s.startsWith("!")) s else "!$s"
                        onAddSnippet(formattedShortcut, t)
                        shortcutInput = ""
                        textInput = ""
                    }
                },
                modifier = Modifier.height(56.dp),
                enabled = shortcutInput.isNotBlank() && textInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF282834),
                    contentColor = LuminaTokens.TextPureLight,
                    disabledContainerColor = Color(0xFF16161C),
                    disabledContentColor = Color(0xFF52525E)
                ),
                border = BorderStroke(1.dp, Color(0xFF3A3A48)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Snippet", modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (snippets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LuminaTokens.TesseraCardBg, RoundedCornerShape(14.dp))
                    .border(1.dp, LuminaTokens.TesseraCardBorder, RoundedCornerShape(14.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum atalho cadastrado. Crie atalhos rápidos como !pix, !email ou !tel acima!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LuminaTokens.TextMutedSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                snippets.forEach { (shortcut, expansion) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LuminaTokens.TesseraCardBg, RoundedCornerShape(14.dp))
                            .border(1.dp, LuminaTokens.TesseraCardBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = shortcut,
                                style = MaterialTheme.typography.labelLarge,
                                color = LuminaTokens.TextPureLight,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = expansion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = LuminaTokens.TextMutedSecondary,
                                maxLines = 2
                            )
                        }
                        IconButton(
                            onClick = { onRemoveSnippet(shortcut) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Excluir Snippet",
                                tint = LuminaTokens.TextMutedSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
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
                        color = LuminaTokens.TextDimTertiary
                    )
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = LuminaTokens.TextPureLight),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0x60FFFFFF),
                    unfocusedBorderColor = LuminaTokens.TesseraCardBorder,
                    focusedContainerColor = LuminaTokens.TesseraPillTrackBg,
                    unfocusedContainerColor = LuminaTokens.TesseraPillTrackBg
                ),
                shape = RoundedCornerShape(12.dp)
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
                    containerColor = Color(0xFF282834),
                    contentColor = LuminaTokens.TextPureLight,
                    disabledContainerColor = Color(0xFF16161C),
                    disabledContentColor = Color(0xFF52525E)
                ),
                border = BorderStroke(1.dp, Color(0xFF3A3A48)),
                shape = RoundedCornerShape(12.dp)
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
                    .background(LuminaTokens.TesseraCardBg, RoundedCornerShape(14.dp))
                    .border(1.dp, LuminaTokens.TesseraCardBorder, RoundedCornerShape(14.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Nenhuma palavra adicionada",
                        style = MaterialTheme.typography.titleSmall,
                        color = LuminaTokens.TextPureLight
                    )
                    Text(
                        text = "Adicione termos, gírias ou nomes próprios usando o campo acima para que o teclado os reconheça e sugira com prioridade.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LuminaTokens.TextMutedSecondary,
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
                    text = "${words.size} palavras no dicionário manual",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LuminaTokens.TextMutedSecondary
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
            .background(LuminaTokens.TesseraPillTrackBg, RoundedCornerShape(10.dp))
            .border(1.dp, LuminaTokens.TesseraCardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = word,
            style = MaterialTheme.typography.bodyMedium,
            color = LuminaTokens.TextPureLight,
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
    val challengePhrases = remember {
        listOf(
            "O teclado Tessera foi concebido para máxima eficiência e precisão.",
            "A tecnologia eleva a produtividade humana com fluidez e velocidade.",
            "O rápido morcego marrom salta sobre o cão preguiçoso no jardim.",
            "Digitação silenciosa e ágil com física de toque de altíssima latência.",
            "Escrever mensagens com clareza transforma nossa comunicação diária."
        )
    }

    var challengeIndex by remember { mutableStateOf(0) }
    val targetSentence = challengePhrases[challengeIndex % challengePhrases.size]

    var typedText by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf(0L) }
    var numberText by remember { mutableStateOf("") }

    val now = System.currentTimeMillis()
    val elapsedSec = if (startTime > 0L) ((now - startTime) / 1000L).coerceAtLeast(1L) else 1L
    val charsCount = typedText.length
    val wpm = if (startTime > 0L && charsCount > 0) {
        (((charsCount / 5f) / (elapsedSec / 60f))).toInt().coerceIn(0, 250)
    } else 0
    val cpm = if (startTime > 0L && charsCount > 0) {
        ((charsCount / (elapsedSec / 60f))).toInt().coerceIn(0, 1200)
    } else 0

    val accuracy = remember(typedText, targetSentence) {
        if (typedText.isEmpty()) 100
        else {
            var correct = 0
            val minLen = minOf(typedText.length, targetSentence.length)
            for (i in 0 until minLen) {
                if (typedText[i] == targetSentence[i]) correct++
            }
            ((correct.toFloat() / typedText.length.coerceAtLeast(1)) * 100f).toInt().coerceIn(0, 100)
        }
    }

    SectionCard(title = "Laboratório de Digitação & Benchmark") {
        Text(
            text = "Teste a física tátil, a precisão Voronoi e meça seu WPM em tempo real:",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate400
        )

        Spacer(modifier = Modifier.height(10.dp))

        WpmTachometerView(
            wpm = wpm,
            accuracy = accuracy,
            cpm = cpm
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LuminaTokens.TesseraCardBg, RoundedCornerShape(14.dp))
                .border(1.dp, LuminaTokens.TesseraCardBorder, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Frase Desafio:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LuminaTokens.TextPureLight
                    )
                    TextButton(
                        onClick = {
                            challengeIndex = (challengeIndex + 1) % challengePhrases.size
                            typedText = ""
                            startTime = 0L
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Próxima ↻", style = MaterialTheme.typography.labelSmall, color = LuminaTokens.TextMutedSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = targetSentence,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = LuminaTokens.TextPureLight
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = typedText,
            onValueChange = {
                if (startTime == 0L && it.isNotEmpty()) {
                    startTime = System.currentTimeMillis()
                }
                typedText = it
            },
            placeholder = {
                Text(
                    "Digite a frase desafio acima para calcular WPM...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LuminaTokens.TextDimTertiary
                )
            },
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (isEnabled) 1.0f else 0.45f),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = LuminaTokens.TextPureLight),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0x60FFFFFF),
                unfocusedBorderColor = LuminaTokens.TesseraCardBorder,
                focusedContainerColor = LuminaTokens.TesseraPillTrackBg,
                unfocusedContainerColor = LuminaTokens.TesseraPillTrackBg
            ),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Campo Numérico (Abre modo 1 2 3 dedicado)",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = LuminaTokens.TextPureLight
        )
        OutlinedTextField(
            value = numberText,
            onValueChange = { numberText = it },
            placeholder = {
                Text(
                    "Toque aqui para abrir o teclado numérico...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = LuminaTokens.TextDimTertiary
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (isEnabled) 1.0f else 0.45f),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = LuminaTokens.TextPureLight),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0x60FFFFFF),
                unfocusedBorderColor = LuminaTokens.TesseraCardBorder,
                focusedContainerColor = LuminaTokens.TesseraPillTrackBg,
                unfocusedContainerColor = LuminaTokens.TesseraPillTrackBg
            ),
            shape = RoundedCornerShape(14.dp)
        )

        if (!isEnabled) {
            Spacer(modifier = Modifier.height(6.dp))
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
        color = Color(0x1AFFFFFF),
        thickness = 1.dp
    )
}

@Composable
fun StitchDashboardScreen(modifier: Modifier = Modifier) {
    TesseraDashboardContainer(modifier)
}

// Data holder utilitário para status
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun CrashRecoveryScreen(
    errorMessage: String,
    onRetry: () -> Unit,
    onResetPreferences: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color(0xFF7F1D1D).copy(alpha = 0.4f), CircleShape)
                .border(1.dp, Color(0xFFEF4444), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Erro",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = "Recuperação do Tessera",
            style = MaterialTheme.typography.titleLarge,
            color = Slate100,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Ocorreu uma falha inesperada ao inicializar o painel de configurações. Seus dados e o teclado permanecem seguros.",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate400,
            textAlign = TextAlign.Center
        )

        Surface(
            color = Slate900,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Slate800),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Detalhes do Diagnóstico:",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentSky,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate300,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = AccentSky, contentColor = Slate950),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Tentar Novamente", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = {
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                cm?.setPrimaryClip(android.content.ClipData.newPlainText("Tessera Error", errorMessage))
                Toast.makeText(context, "Detalhes copiados!", Toast.LENGTH_SHORT).show()
            },
            border = BorderStroke(1.dp, Slate700),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Copiar Detalhes do Erro", color = Slate300)
        }

        TextButton(
            onClick = {
                onResetPreferences()
                Toast.makeText(context, "Configurações restauradas ao padrão.", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Redefinir Preferências para o Padrão", color = Color(0xFFF87171))
        }
    }
}
