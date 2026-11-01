package com.example.ui.lumina

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Tessera Monolith — Lumina Visual Design Tokens
 * Haute-Horlogerie & Cyber-Editorial Tactile Design System
 */
object LuminaTokens {
    // 🌌 Obsidian Depth Bases
    val ObsidianVoid = Color(0xFF060709)            // Fundo abissal primário
    val ObsidianDeep = Color(0xFF0B0E17)            // Superfície secundária / Fundo de tela
    val ObsidianGlass = Color(0xFF131926)           // Cartões e painéis com elevação
    val ObsidianGlassElevated = Color(0xFF1A2234)   // Elementos focados / Interativos
    val ObsidianGlassSubtle = Color(0xFF0F1522)     // Fundo de inputs e trilhas

    // ⚡ Bioluminescent Chromatics
    val LuminaCyan = Color(0xFF38BDF8)              // Ação primária / Pulso cinético
    val LuminaCyanGlow = Color(0x3338BDF8)          // Penumbra de proximidade (20%)
    val LuminaCyanBright = Color(0xFF7DD3FC)        // Destaque reflexivo
    val LuminaEmerald = Color(0xFF10B981)           // Precisão > 90% / Status OK
    val LuminaEmeraldGlow = Color(0x3310B981)       // Brilho esmeralda
    val LuminaAmber = Color(0xFFF59E0B)             // Alerta / Cadência média
    val LuminaCrimson = Color(0xFFF43F5E)           // Erros / Desconexão
    val LuminaCrimsonGlow = Color(0x33F43F5E)       // Brilho carmesim de erro
    val LuminaViolet = Color(0xFFA855F7)            // Frequências harmônicas / Monet
    val LuminaVioletGlow = Color(0x33A855F7)        // Brilho violeta

    // 🪞 Specular Rim & Shading (Double-Stroke Physics)
    val SpecularHighlightTop = Color(0x59FFFFFF)    // 35% luz branca no bisel superior
    val SpecularHighlightCyan = Color(0x6638BDF8)   // 40% ciano no bisel ativo
    val SubtlyLuminescentBorder = Color(0x2694A3B8) // 15% ardósia neutra
    val OcclusionDeepShadow = Color(0x99000000)     // Sombra de contato profunda
    val ObsidianDeepShadow = Color(0x99000000)      // Sombra de oclusão profunda

    // 🖋️ Tipografia Editorial de Alto Contraste
    val TextPureLight = Color(0xFFF8FAFC)           // Texto de máxima legibilidade
    val TextMutedSecondary = Color(0xFF94A3B8)      // Texto de apoio
    val TextDimTertiary = Color(0xFF64748B)         // Marcadores de unidade / legendas

    // 🖌️ Brushes Especulares Pré-calculados (Zero alocação no compose draw)
    val SpecularRimBrush = Brush.verticalGradient(
        colors = listOf(
            SpecularHighlightTop,
            Color(0x1AFFFFFF),
            Color(0x05000000)
        )
    )

    val ActiveSpecularRimBrush = Brush.verticalGradient(
        colors = listOf(
            SpecularHighlightCyan,
            Color(0x2638BDF8),
            Color(0x05000000)
        )
    )

    val CardSurfaceBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFA141B2A),
            Color(0xF50E1420)
        )
    )

    val DockGlassBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xF0161F32),
            Color(0xEB0D121D)
        )
    )
}
