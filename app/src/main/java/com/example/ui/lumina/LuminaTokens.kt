package com.example.ui.lumina

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Tessera Core & Lumina Visual Design Tokens
 * Editorial Minimalist, Charcoal Slate & Liquid Glass Design System
 */
object LuminaTokens {
    // 🌌 Tessera Core Charcoal & Obsidian Depth Bases
    val ObsidianVoid = Color(0xFF0B0B0E)            // Fundo primário OLED anti-smear (Tessera Core)
    val ObsidianDeep = Color(0xFF121216)            // Superfície secundária Charcoal
    val ObsidianGlass = Color(0xFF16161C)           // Cartões agrupados Tessera Core
    val ObsidianGlassElevated = Color(0xFF22222B)   // Elementos focados / Ícones circulares
    val ObsidianGlassSubtle = Color(0xFF111116)     // Fundo de inputs e trilhas segmentadas

    // 🏛️ Tessera Core Structural Tokens
    val TesseraCharcoalBg = Color(0xFF0B0B0E)
    val TesseraCardBg = Color(0xFF16161C)
    val TesseraCardBorder = Color(0xFF262630)
    val TesseraIconCircleBg = Color(0xFF22222B)
    val TesseraDivider = Color(0x18FFFFFF)
    val TesseraPillTrackBg = Color(0xFF111116)
    val TesseraPillActiveBg = Color(0xFF282834)
    val TesseraPillActiveBorder = Color(0xFF3A3A48)

    // ⚡ Bioluminescent & Pop Accent Chromatics
    val LuminaCyan = Color(0xFF38BDF8)              // Ação primária / Pulso cinético
    val LuminaCyanGlow = Color(0x2E38BDF8)          // Penumbra de proximidade (18%)
    val LuminaCyanBright = Color(0xFF7DD3FC)        // Destaque reflexivo
    val LuminaEmerald = Color(0xFF34C759)           // Precisão > 90% / Status OK (Tessera Green)
    val LuminaEmeraldGlow = Color(0x3334C759)       // Brilho esmeralda
    val LuminaAmber = Color(0xFFFF9F0A)             // Alerta / Cadência média
    val LuminaCrimson = Color(0xFFFF453A)           // Erros / Desconexão
    val LuminaCrimsonGlow = Color(0x33FF453A)       // Brilho carmesim de erro
    val LuminaViolet = Color(0xFFB388FF)            // Frequências harmônicas / Monet
    val LuminaVioletGlow = Color(0x33B388FF)        // Brilho violeta

    // 🪞 Specular Rim & Shading (Apple Liquid Glass & Tessera Rim)
    val SpecularHighlightTop = Color(0x38FFFFFF)    // Micro-borda de luz superior (rim light)
    val SpecularHighlightCyan = Color(0x6638BDF8)   // 40% ciano no bisel ativo
    val SubtlyLuminescentBorder = Color(0xFF262630) // Contorno de cartão Tessera Core
    val OcclusionDeepShadow = Color(0x99000000)     // Sombra de contato profunda
    val ObsidianDeepShadow = Color(0x99000000)      // Sombra de oclusão profunda

    // 🖋️ Tipografia Editorial de Alto Contraste
    val TextPureLight = Color(0xFFF5F5F7)           // Texto primário Cupertino/Tessera
    val TextMutedSecondary = Color(0xFF8E8E98)      // Texto de apoio secundário
    val TextDimTertiary = Color(0xFF63636E)         // Marcadores de unidade / legendas

    // 🖌️ Brushes Especulares Pré-calculados (Zero alocação no compose draw)
    val SpecularRimBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0x2EFFFFFF),
            Color(0x14FFFFFF),
            Color(0x08FFFFFF)
        )
    )

    val ActiveSpecularRimBrush = Brush.verticalGradient(
        colors = listOf(
            SpecularHighlightCyan,
            Color(0x2638BDF8),
            Color(0x12FFFFFF)
        )
    )

    val CardSurfaceBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF18181F),
            Color(0xFF14141A)
        )
    )

    val DockGlassBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xF21C1C24),
            Color(0xF5131319)
        )
    )
}
