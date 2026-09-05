package com.titanium.trades.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val TitaniumColors = darkColorScheme(
    primary = Gold,
    onPrimary = TextOnGold,
    primaryContainer = Color(0xFF3A2F10),
    onPrimaryContainer = GoldSoft,
    inversePrimary = GoldDeep,
    secondary = GreenUp,
    onSecondary = Color(0xFF03170B),
    secondaryContainer = Color(0xFF0F2A1B),
    onSecondaryContainer = Color(0xFF7AE6AC),
    tertiary = AmberWarn,
    onTertiary = Color(0xFF1E1200),
    background = bgDeep,
    onBackground = TextHigh,
    surface = bgDeep,
    onSurface = TextHigh,
    surfaceVariant = surface1,
    onSurfaceVariant = TextMid,
    surfaceTint = Gold,
    inverseSurface = TextHigh,
    inverseOnSurface = bgDeep,
    outline = outline1,
    outlineVariant = Color(0xFF33333D),
    error = RedDown,
    onError = Color(0xFFFFFBFC),
    errorContainer = Color(0xFF3B1215),
    onErrorContainer = Color(0xFFFFA8A8),
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
fun TitaniumTheme(content: @Composable () -> Unit) {
    // OLED-first premium trading terminal: always dark for contrast & identity.
    MaterialTheme(
        colorScheme = TitaniumColors,
        typography = Typography,
        shapes = TitaniumShapes,
        content = content
    )
}
