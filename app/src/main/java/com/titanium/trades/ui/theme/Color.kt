package com.titanium.trades.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ─────── Core surfaces (true OLED blacks, layered) ───────
val OledBlack = Color(0xFF000000)        // pure black — pixels literally off on AMOLED
val bgDeep = Color(0xFF050505)            // app background
val surface1 = Color(0xFF111114)          // raised surface
val surface2 = Color(0xFF18181D)          // higher surface
val surface3 = Color(0xFF202028)          // highest surface / pressed
val outline1 = Color(0xFF26262E)          // subtle hairline borders
val outlineGold = Color(0xFF3A2F10)

// ─────── Brand: gold identity ───────
val Gold = Color(0xFFE8C766)             // primary gold
val GoldDeep = Color(0xFFB8860B)         // darker gold — for depth/gradients
val GoldSoft = Color(0xFFF2DFA4)         // highlight gold
val GoldDark = Color(0xFF6E5410)         // pressed / on-gold-text
val GoldGlow = Color(0xFFD4AF37)

// ─────── Semantic: market / trend ───────
val GreenUp = Color(0xFF26E07F)          // vivid profitable green
val GreenDeep = Color(0xFF0F6B44)
val RedDown = Color(0xFFFF5C5C)          // vivid losing red
val RedDeep = Color(0xFF8A1F1F)
val AmberWarn = Color(0xFFFFB020)
val Neutral = Color(0xFF8A8A93)

// ─────── Text ───────
val TextHigh = Color(0xFFF7F7F8)
val TextMid = Color(0xFFB8B8C0)
val TextDim = Color(0xFF6E6E78)
val TextOnGold = Color(0xFF151006)

// ─────── Frequently used gradient brushes ───────
val GoldGradient = Brush.linearGradient(
    listOf(GoldSoft, Gold, GoldDeep)
)
val GoldHorizontal = Brush.horizontalGradient(listOf(GoldSoft, Gold, GoldDeep))
val GreenGradient = Brush.verticalGradient(listOf(Color(0xFF2AF08A), GreenDeep))
val RedGradient = Brush.verticalGradient(listOf(Color(0xFFFF7A7A), RedDeep))
val AmbientBg = Brush.verticalGradient(listOf(surface1, bgDeep, OledBlack))
