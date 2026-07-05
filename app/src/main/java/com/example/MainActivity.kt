package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.filled.Mic
import androidx.compose.ui.graphics.Brush

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize().imePadding(),
                    containerColor = Color.Transparent
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0A0F14), // Deep space
                                        Color(0xFF001F22), // Deep teal
                                        Color(0xFF003838)  // Lighter teal at bottom
                                    )
                                )
                            )
                            .padding(innerPadding)
                    ) {
                        // Decorative glowing orb in the background
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 100.dp, y = (-50).dp)
                                .size(250.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF06FBFB).copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .offset(x = (-80).dp, y = 100.dp)
                                .size(300.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF008B8B).copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                        StitchDashboardScreen(
                            modifier = Modifier.fillMaxSize()
                        )

                    }
                }
            }
        }
    }
}

// Helpers to check the state of the custom input method
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

@Composable
fun StitchDashboardScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var testInputText by remember { mutableStateOf("") }
    
    var isEnabled by remember { mutableStateOf(false) }
    var isSelected by remember { mutableStateOf(false) }

    // Periodically poll keyboard status to react instantly when the user enables it
    LaunchedEffect(Unit) {
        while (true) {
            isEnabled = isKeyboardEnabled(context)
            isSelected = isKeyboardSelected(context)
            kotlinx.coroutines.delay(1000)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "AI Sparkles",
                tint = Color(0xFF06FBFB),
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "TESSERA KEYBOARD",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                color = Color.White
            )
        }

        Text(
            text = "Interface Glassmorphic de Alta Performance",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF06FBFB),
            textAlign = TextAlign.Center
        )

        // Dynamic Keyboard Status Card
        val (statusText, statusColor) = when {
            !isEnabled -> "🔴 Teclado Desativado (Ative na Etapa 1)" to Color(0xFFFF5252)
            !isSelected -> "🟡 Ativado, mas não Selecionado (Selecione na Etapa 2)" to Color(0xFFFFD740)
            else -> "🟢 Teclado Tessera Ativo e Pronto para Uso!" to Color(0xFF00E676)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = statusText,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        // Onboarding Instructions
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0x12FFFFFF),
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color(0x2BFFFFFF),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(18.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Como habilitar e usar o teclado:",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                InstructionRow(number = "1", text = "Clique em 'ATIVAR NAS CONFIGURAÇÕES' e ative o interruptor do 'Tessera Keyboard'.")
                InstructionRow(number = "2", text = "Clique em 'SELECIONAR TESSERA KEYBOARD' e escolha ele na lista de teclados ativos.")
                InstructionRow(number = "3", text = "Toque no campo de testes abaixo e use o teclado!")
            }
        }

        // Actions (Pill buttons conforming to the requested design approach)
        Button(
            onClick = {
                try {
                    val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                    Toast.makeText(context, "Ative o 'Tessera Keyboard' na lista", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Erro ao abrir configurações: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF003C3C),
                contentColor = Color(0xFF06FBFB)
            ),
            shape = RoundedCornerShape(100.dp),
            border = BorderStroke(1.2.dp, Color(0xFF06FBFB))
        ) {
            Icon(imageVector = Icons.Default.Build, contentDescription = "Configurações", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "1. ATIVAR NAS CONFIGURAÇÕES",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
            )
        }

        Button(
            onClick = {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                if (imm != null) {
                    imm.showInputMethodPicker()
                } else {
                    Toast.makeText(context, "Não foi possível exibir o seletor de teclado", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF06FBFB),
                contentColor = Color(0xFF001717)
            ),
            shape = RoundedCornerShape(100.dp),
            border = BorderStroke(1.2.dp, Color.White)
        ) {
            Icon(imageVector = Icons.Default.Keyboard, contentDescription = "Mudar teclado", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "2. SELECIONAR TESSERA KEYBOARD",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
            )
        }


        
        // Microphone permission
        var hasMicPermission by remember { 
            mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) 
        }
        val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasMicPermission = granted
        }
        
        if (!hasMicPermission) {
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC0017), contentColor = Color.White),
                shape = RoundedCornerShape(100.dp),
                border = BorderStroke(1.2.dp, Color.White)
            ) {
                Icon(imageVector = Icons.Default.Mic, contentDescription = "Mic", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("CONCEDER PERMISSÃO DE MICROFONE", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Theme Selection
        Spacer(modifier = Modifier.height(16.dp))
        var keyboardTheme by remember { mutableStateOf(context.getSharedPreferences("StitchPrefs", Context.MODE_PRIVATE).getString("KEYBOARD_THEME", "Dark") ?: "Dark") }
        
        Text("Tema do Teclado", style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.align(Alignment.Start))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ThemeOption(
                title = "Dark Glass",
                isSelected = keyboardTheme == "Dark",
                onClick = {
                    keyboardTheme = "Dark"
                    context.getSharedPreferences("StitchPrefs", Context.MODE_PRIVATE).edit().putString("KEYBOARD_THEME", "Dark").apply()
                },
                modifier = Modifier.weight(1f)
            )
            ThemeOption(
                title = "Light Glass",
                isSelected = keyboardTheme == "Light",
                onClick = {
                    keyboardTheme = "Light"
                    context.getSharedPreferences("StitchPrefs", Context.MODE_PRIVATE).edit().putString("KEYBOARD_THEME", "Light").apply()
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Test Input Field
        Spacer(modifier = Modifier.height(16.dp))
        var testText by remember { mutableStateOf("") }
        androidx.compose.material3.OutlinedTextField(
            value = testText,
            onValueChange = { testText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Teste o teclado aqui...", color = Color(0x80FFFFFF)) },
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF06FBFB),
                unfocusedBorderColor = Color(0x2BFFFFFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF06FBFB)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Branding bottom note
        Text(
            text = "Tessera Keyboard • Minimalist & Intelligent",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0x80FFFFFF),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun InstructionRow(number: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color(0xFF06FBFB), RoundedCornerShape(100.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = Color(0xFF001717),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFE2E2E2),
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
fun ThemeOption(title: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bgColor = if (isSelected) Color(0xFF06FBFB).copy(alpha = 0.15f) else Color(0x0DFFFFFF)
    val borderColor = if (isSelected) Color(0xFF06FBFB) else Color(0x2BFFFFFF)
    val textColor = if (isSelected) Color(0xFF06FBFB) else Color.White
    
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
