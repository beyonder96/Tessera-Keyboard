package com.example.ui.lumina

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.lumina.LuminaTokens.LuminaCyan
import com.example.ui.lumina.LuminaTokens.LuminaCyanGlow
import com.example.ui.lumina.LuminaTokens.LuminaEmerald
import com.example.ui.lumina.LuminaTokens.LuminaViolet
import com.example.ui.lumina.LuminaTokens.ObsidianGlassElevated
import com.example.ui.lumina.LuminaTokens.ObsidianGlassSubtle
import com.example.ui.lumina.LuminaTokens.TextDimTertiary
import com.example.ui.lumina.LuminaTokens.TextMutedSecondary
import com.example.ui.lumina.LuminaTokens.TextPureLight
import kotlin.math.sin

data class SoundProfileOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val frequencyTag: String,
    val accentColor: Color
)

/**
 * Deck de Síntese Acústica com Visualizador de Ondas Harmônicas em Tempo Real.
 */
@Composable
fun AcousticHarmonicDeck(
    currentProfile: String,
    onProfileSelected: (String) -> Unit,
    hapticDurationMs: Int,
    onHapticDurationChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val profiles = remember {
        listOf(
            SoundProfileOption("thock", "Mechanical Thock", "Grave amortecido linear", "210Hz–520Hz", LuminaCyan),
            SoundProfileOption("crisp", "Minimal Crisp", "Estalo tátil ultracurto", "2.4kHz", LuminaEmerald),
            SoundProfileOption("typewriter", "Typewriter", "Ataque mecânico clássico", "1.1kHz", LuminaViolet),
            SoundProfileOption("system", "Sistema Android", "Padrão OEM de sistema", "System FX", TextMutedSecondary)
        )
    }

    val selectedProfile = profiles.find { it.id == currentProfile } ?: profiles[0]

    // Animação de fase da onda senoidal contínua
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformPhase")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Phase"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Osciloscópio Acústico de Ondas Senoidais
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(ObsidianGlassSubtle)
                .border(1.dp, Color(0x2238BDF8), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .drawWithCache {
                    val width = size.width
                    val height = size.height
                    val centerY = height / 2f
                    val wavePath = Path()

                    val (frequency, amplitude) = when (currentProfile) {
                        "thock" -> Pair(2.2f, height * 0.38f)
                        "crisp" -> Pair(6.5f, height * 0.22f)
                        "typewriter" -> Pair(4.2f, height * 0.32f)
                        else -> Pair(1.5f, height * 0.15f)
                    }

                    wavePath.moveTo(0f, centerY)
                    val steps = 80
                    for (i in 0..steps) {
                        val x = (i / steps.toFloat()) * width
                        val angle = (x / width) * frequency * 2f * Math.PI + wavePhase
                        val y = centerY + sin(angle).toFloat() * amplitude
                        wavePath.lineTo(x, y)
                    }

                    val glowBrush = Brush.horizontalGradient(
                        colors = listOf(
                            selectedProfile.accentColor.copy(alpha = 0.2f),
                            selectedProfile.accentColor,
                            selectedProfile.accentColor.copy(alpha = 0.4f)
                        )
                    )

                    onDrawBehind {
                        // Linha de centro de referência
                        drawLine(
                            color = Color(0x1AFFFFFF),
                            start = Offset(0f, centerY),
                            end = Offset(width, centerY),
                            strokeWidth = 1f
                        )
                        // Onda Harmônica Ativa
                        drawPath(
                            path = wavePath,
                            brush = glowBrush,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "RESPOSTA EM FREQUÊNCIA",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 1.2.sp),
                    color = TextDimTertiary
                )
                Text(
                    text = selectedProfile.frequencyTag,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = selectedProfile.accentColor
                    )
                )
            }
        }

        // 2. Seletor de Perfis Acústicos (Pedais de Precisão)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            profiles.forEach { profile ->
                val isSelected = profile.id == currentProfile
                val borderBrush = if (isSelected) {
                    Brush.horizontalGradient(listOf(profile.accentColor, profile.accentColor.copy(alpha = 0.3f)))
                } else {
                    Brush.linearGradient(listOf(Color(0x1AFFFFFF), Color(0x05FFFFFF)))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) ObsidianGlassElevated else ObsidianGlassSubtle)
                        .border(1.dp, borderBrush, RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onProfileSelected(profile.id)
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) profile.accentColor else Color(0xFF334155))
                            )
                            Column {
                                Text(
                                    text = profile.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) TextPureLight else TextMutedSecondary
                                )
                                Text(
                                    text = profile.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextDimTertiary
                                )
                            }
                        }

                        Text(
                            text = profile.frequencyTag,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) profile.accentColor else TextDimTertiary
                            )
                        )
                    }
                }
            }
        }

        // 3. Slider de Duração Háptica com Degraus de Precisão
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DURAÇÃO HÁPTICA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextDimTertiary
                )
                Text(
                    text = "${hapticDurationMs}ms",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaCyan
                    )
                )
            }

            Slider(
                value = hapticDurationMs.toFloat(),
                onValueChange = { onHapticDurationChange(it.toInt()) },
                valueRange = 1f..50f,
                steps = 49,
                colors = SliderDefaults.colors(
                    thumbColor = LuminaCyan,
                    activeTrackColor = LuminaCyan,
                    inactiveTrackColor = Color(0x3338BDF8)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
