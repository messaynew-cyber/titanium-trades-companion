package com.titanium.trades.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Titanium Trades always uses the dark OLED scheme for maximum contrast
// on AMOLED displays and a premium trading-terminal feel.
private val DarkColors = darkColorScheme(
    primary = Gold,
    onPrimary = Color.Black,
    primaryContainer = GoldDark,
    onPrimaryContainer = GoldBright,
    secondary = GoldBright,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF2A2300),
    onSecondaryContainer = GoldSoft,
    tertiary = GreenUp,
    onTertiary = Color.Black,
    background = OledBlack,
    onBackground = TextPrimary,
    surface = OledBlack,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceBlack,
    onSurfaceVariant = TextSecondary,
    outline = StrokeGray,
    error = RedDown,
    onError = Color.Black,
)

@Composable
fun TitaniumTheme(
    darkTheme: Boolean = true, // always dark for trading terminal aesthetic
    content: @Composable () -> Unit
) {
    // We force dark regardless of system to preserve OLED + gold identity
    val colorScheme = if (true) DarkColors else lightColorScheme()
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
