package com.example.ui.lumina

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.lumina.LuminaTokens.LuminaCyan
import com.example.ui.lumina.LuminaTokens.LuminaViolet
import com.example.ui.lumina.LuminaTokens.ObsidianDeep
import com.example.ui.lumina.LuminaTokens.ObsidianVoid

/**
 * Fundo Atmosférico Generativo com Orbes Bioluminescentes e Efeito de Profundidade 120Hz.
 * Executa inteiramente com zero alocações na fase de desenho graças ao drawWithCache.
 */
@Composable
fun LuminaAtmosphere(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AtmospherePulse")

    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulsePhase"
    )

    val verticalDrift by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Drift"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .drawWithCache {
                val width = size.width
                val height = size.height

                // Orbe 1: Luz ambiente superior sutil em prata monocromático
                val orb1Center = Offset(
                    x = width * 0.85f,
                    y = height * 0.12f + verticalDrift
                )
                val orb1Radius = width * 0.65f
                val orb1Brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF).copy(alpha = 0.035f + pulsePhase * 0.015f),
                        Color(0xFFFFFFFF).copy(alpha = 0.008f),
                        Color.Transparent
                    ),
                    center = orb1Center,
                    radius = orb1Radius
                )

                // Orbe 2: Luz suave na parte intermediária
                val orb2Center = Offset(
                    x = width * 0.10f,
                    y = height * 0.45f - verticalDrift * 0.8f
                )
                val orb2Radius = width * 0.55f
                val orb2Brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF).copy(alpha = 0.025f + (1f - pulsePhase) * 0.010f),
                        Color(0xFFFFFFFF).copy(alpha = 0.005f),
                        Color.Transparent
                    ),
                    center = orb2Center,
                    radius = orb2Radius
                )

                // Gradiente vertical de base profundo (Obsidian Depth)
                val baseGradient = Brush.verticalGradient(
                    colors = listOf(
                        ObsidianDeep.copy(alpha = 0.75f),
                        ObsidianVoid.copy(alpha = 0.95f),
                        ObsidianVoid
                    )
                )

                onDrawBehind {
                    drawRect(baseGradient)
                    drawCircle(brush = orb1Brush, center = orb1Center, radius = orb1Radius)
                    drawCircle(brush = orb2Brush, center = orb2Center, radius = orb2Radius)
                }
            }
    ) {
        content()
    }
}
