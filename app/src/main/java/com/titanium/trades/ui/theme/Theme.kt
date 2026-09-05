package com.titanium.trades.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Literal (skin-locked) Material schemes. They do NOT reference the computed
// public palette vals, so their colors are fixed regardless of the live IsDark
// flag — DeepScheme is always the OLED-dark tonal palette, DayScheme always the
// paper-light one. Component surfaces that need to flip call the computed vals.

private val DeepScheme = darkColorScheme(
    primary = Color(0xFFE8C766),
    onPrimary = Color(0xFF151006),
    primaryContainer = Color(0xFF3A2F10),
    onPrimaryContainer = Color(0xFFF2DFA4),
    inversePrimary = Color(0xFFB8860B),
    secondary = Color(0xFF26E07F),
    onSecondary = Color(0xFF03170B),
    secondaryContainer = Color(0xFF0F2A1B),
    onSecondaryContainer = Color(0xFF7AE6AC),
    tertiary = Color(0xFFFFB020),
    onTertiary = Color(0xFF1E1200),
    background = Color(0xFF050505),
    onBackground = Color(0xFFF7F7F8),
    surface = Color(0xFF050505),
    onSurface = Color(0xFFF7F7F8),
    surfaceVariant = Color(0xFF111114),
    onSurfaceVariant = Color(0xFFB8B8C0),
    surfaceTint = Color(0xFFE8C766),
    inverseSurface = Color(0xFFF7F7F8),
    inverseOnSurface = Color(0xFF050505),
    outline = Color(0xFF26262E),
    outlineVariant = Color(0xFF33333D),
    error = Color(0xFFFF5C5C),
    onError = Color(0xFFFFFBFC),
    errorContainer = Color(0xFF3B1215),
    onErrorContainer = Color(0xFFFFA8A8),
    scrim = Color(0xFF000000),
)

private val DayScheme = lightColorScheme(
    primary = Color(0xFF8A6D1F),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFBF3DE),
    onPrimaryContainer = Color(0xFF4C3D11),
    inversePrimary = Color(0xFF9A7B23),
    secondary = Color(0xFF0F6B44),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE2F6EB),
    onSecondaryContainer = Color(0xFF0F6B44),
    tertiary = Color(0xFFA86400),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF4F1EA),
    onBackground = Color(0xFF181309),
    surface = Color(0xFFFCF8EF),
    onSurface = Color(0xFF181309),
    surfaceVariant = Color(0xFFEDE6D6),
    onSurfaceVariant = Color(0xFF5A5241),
    surfaceTint = Color(0xFF8A6D1F),
    inverseSurface = Color(0xFF181309),
    inverseOnSurface = Color(0xFFF4F1EA),
    outline = Color(0xFFDCD4C2),
    outlineVariant = Color(0xFFDDD4C0),
    error = Color(0xFFD63A3A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9E3E3),
    onErrorContainer = Color(0xFF9E2626),
    scrim = Color(0xFF000000),
)

private val TitaniumShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(30.dp),
)

@Composable
fun TitaniumTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    // IsDark is the single source of truth for the palette. darkTheme (from the
    // saveable MainActivity state) is always kept mirrored to it by the toggle.
    // Reading it here pins recomposition: any flip re-renders every computed
    // color/brush -> the whole UI re-skins without editing the ~361 call sites.
    MaterialTheme(
        colorScheme = if (IsDark) DeepScheme else DayScheme,
        typography = Typography,
        shapes = TitaniumShapes,
        content = content
    )
}
