package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Neutral Slate Palette
val Slate950 = Color(0xFF090D16) // Background principal (nunca preto puro #000)
val Slate900 = Color(0xFF0F172A) // Surface elevada / Secondary background
val Slate850 = Color(0xFF162032) // Card surface
val Slate800 = Color(0xFF1E293B) // Card border / Selected surface
val Slate700 = Color(0xFF334155) // Subtle border / Divider
val Slate600 = Color(0xFF475569) // Inactive icon / Muted outline
val Slate400 = Color(0xFF94A3B8) // Secondary text (WCAG AA)
val Slate300 = Color(0xFFCBD5E1) // Secondary bright text
val Slate100 = Color(0xFFF1F5F9) // Primary text (nunca branco puro #fff)

// Unico Accent de alto contraste para CTAs e elementos ativos criticos
val AccentSky = Color(0xFF38BDF8) // Sky 400 (Alto contraste em fundos escuros)
val AccentSkyDark = Color(0xFF0284C7) // Sky 600 (Press state / border)
val AccentSkyMuted = Color(0x2638BDF8) // 15% opacity Sky para container states

// Feedback states
val StateError = Color(0xFFF87171) // Red 400
val StateSuccess = Color(0xFF34D399) // Emerald 400
val StateWarning = Color(0xFFFBBF24) // Amber 400
