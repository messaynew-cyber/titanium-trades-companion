package com.titanium.trades.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Single source of truth for the active palette.
 *
 * Screens refer to the public vals below as *computed* properties, so reading
 * `IsDark` once at the TitaniumTheme root (to force recomposition) re-skins the
 * entire app — including the ~361 existing call sites — with no per-screen edits.
 */

private object Deep {
    // ─── OLED dark: the Titanium glass-cockpit identity ───
    val OledBlack = Color(0xFF000000)
    val bgDeep = Color(0xFF050505)
    val surface1 = Color(0xFF111114)
    val surface2 = Color(0xFF18181D)
    val surface3 = Color(0xFF202028)
    val outline1 = Color(0xFF26262E)
    val Gold = Color(0xFFE8C766)
    val GoldDeep = Color(0xFFB8860B)
    val GoldSoft = Color(0xFFF2DFA4)
    val GoldDark = Color(0xFF6E5410)
    val GoldGlow = Color(0xFFD4AF37)
    val GreenUp = Color(0xFF26E07F)
    val GreenDeep = Color(0xFF0F6B44)
    val RedDown = Color(0xFFFF5C5C)
    val RedDeep = Color(0xFF8A1F1F)
    val AmberWarn = Color(0xFFFFB020)
    val Neutral = Color(0xFF8A8A93)
    val TextHigh = Color(0xFFF7F7F8)
    val TextMid = Color(0xFFB8B8C0)
    val TextDim = Color(0xFF6E6E78)
    val TextOnGold = Color(0xFF151006)
    val UpPill = Color(0xFF26E07F)
    val DownPill = Color(0xFFFF5C5C)
}

private object Day {
    // ─── Light: warm paper + ink, deep-brass brand, same market semantics ───
    val OledBlack = Color(0xFF0A0A0F)
    val bgDeep = Color(0xFFF4F1EA)
    val surface1 = Color(0xFFFFFFFF)
    val surface2 = Color(0xFFFCF8EF)
    val surface3 = Color(0xFFEDE6D6)
    val outline1 = Color(0xFFDCD4C2)
    val Gold = Color(0xFF8A6D1F)
    val GoldDeep = Color(0xFF9A7B23)
    val GoldSoft = Color(0xFFE6C97B)
    val GoldDark = Color(0xFF4C3D11)
    val GoldGlow = Color(0xFFA9871F)
    val GreenUp = Color(0xFF0E9C5A)
    val GreenDeep = Color(0xFF0F6B44)
    val RedDown = Color(0xFFD63A3A)
    val RedDeep = Color(0xFF9E2626)
    val AmberWarn = Color(0xFFA86400)
    val Neutral = Color(0xFF8C8575)
    val TextHigh = Color(0xFF181309)
    val TextMid = Color(0xFF5A5241)
    val TextDim = Color(0xFF9A917B)
    val TextOnGold = Color(0xFFFFFFFF)
    val UpPill = Color(0xFF0F8A51)
    val DownPill = Color(0xFFCF2F2F)
}

/** Active-skin switch — flip it (and read it) at the theme root to recompose. */
var IsDark by mutableStateOf(true)

// ─────────────────────────── SCALED COLORS ───────────────────────────
// Compat: any function lighten/darken helper screens might use in one place.
private fun blend(a: Color, b: Color, t: Float): Color = Color(
    a.red + (b.red - a.red) * t,
    a.green + (b.green - a.green) * t,
    a.blue + (b.blue - a.blue) * t,
    1f
)

// Surface extras derived from scaling, computed live so they match the skin.
val surface2High get() = blend(surface2, Gold.copy(alpha = 1f), if (IsDark) 0.06f else 0.10f)

// ─────────────────────────── PUBLIC PALETTE ───────────────────────────
// Everything below is a *computed* val: it reflects the active skin every time
// it is read. IsDark is read by TitaniumTheme so any change recomposes the tree.
val OledBlack get() = if (IsDark) Deep.OledBlack else Day.OledBlack
val bgDeep get() = if (IsDark) Deep.bgDeep else Day.bgDeep
val surface1 get() = if (IsDark) Deep.surface1 else Day.surface1
val surface2 get() = if (IsDark) Deep.surface2 else Day.surface2
val surface3 get() = if (IsDark) Deep.surface3 else Day.surface3
val outline1 get() = if (IsDark) Deep.outline1 else Day.outline1
val outlineGold get() = if (IsDark) Deep.GoldDeep.copy(alpha = 0.30f) else Day.Gold.copy(alpha = 0.25f)

val Gold get() = if (IsDark) Deep.Gold else Day.Gold
val GoldDeep get() = if (IsDark) Deep.GoldDeep else Day.GoldDeep
val GoldSoft get() = if (IsDark) Deep.GoldSoft else Day.GoldSoft
val GoldDark get() = if (IsDark) Deep.GoldDark else Day.GoldDark
val GoldGlow get() = if (IsDark) Deep.GoldGlow else Day.GoldGlow

val GreenUp get() = if (IsDark) Deep.GreenUp else Day.GreenUp
val GreenDeep get() = if (IsDark) Deep.GreenDeep else Day.GreenDeep
val RedDown get() = if (IsDark) Deep.RedDown else Day.RedDown
val RedDeep get() = if (IsDark) Deep.RedDeep else Day.RedDeep
val AmberWarn get() = if (IsDark) Deep.AmberWarn else Day.AmberWarn
val Neutral get() = if (IsDark) Deep.Neutral else Day.Neutral

val TextHigh get() = if (IsDark) Deep.TextHigh else Day.TextHigh
val TextMid get() = if (IsDark) Deep.TextMid else Day.TextMid
val TextDim get() = if (IsDark) Deep.TextDim else Day.TextDim
val TextOnGold get() = if (IsDark) Deep.TextOnGold else Day.TextOnGold

val UpPill get() = if (IsDark) Deep.UpPill else Day.UpPill
val DownPill get() = if (IsDark) Deep.DownPill else Day.DownPill

// ─────────────────────────── GRADIENT BRUSHES ───────────────────────────
// Plain vals — built from the CURRENT skin. Because they are handed to
// background/brush params inside composition, a toggle (which recomposes the
// whole tree via IsDark read) rebuilds them against the new palette.
val GoldGradient: Brush get() = Brush.linearGradient(listOf(GoldSoft, Gold, GoldDeep))
val GoldHorizontal: Brush get() = Brush.horizontalGradient(listOf(GoldSoft, Gold, GoldDeep))
val GreenGradient: Brush get() = Brush.verticalGradient(listOf(GreenUp, GreenDeep))
val RedGradient: Brush get() = Brush.verticalGradient(listOf(RedDown.copy(alpha = 0.9f), RedDeep))
val AmbientBg: Brush get() = Brush.verticalGradient(listOf(surface1, bgDeep, OledBlack))
