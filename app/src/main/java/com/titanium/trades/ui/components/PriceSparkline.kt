package com.titanium.trades.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.titanium.trades.ui.theme.Gold
import com.titanium.trades.ui.theme.GoldDeep
import com.titanium.trades.ui.theme.GoldGlow
import com.titanium.trades.ui.theme.GreenUp
import com.titanium.trades.ui.theme.RedDown
import kotlin.math.max
import kotlin.math.min

/**
 * A lightweight price sparkline drawn on Canvas (no heavy chart dependency).
 * Normalizes the rolling price window into a smooth path with a soft gradient
 * fill. Colors by trend (up = green tint, down = red tint, flat = gold).
 */
@Composable
fun PriceSparkline(
    prices: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = Gold,
    fillUp: Color = GreenUp.copy(alpha = 0.30f),
    fillDown: Color = RedDown.copy(alpha = 0.30f),
    strokeWidth: Float = 2.5.dp.value
) {
    Canvas(modifier = modifier, onDraw = {
        if (prices.size < 2) return@Canvas
        val width = size.width
        val height = size.height
        val minP = prices.minOrNull() ?: return@Canvas
        val maxP = prices.maxOrNull() ?: return@Canvas
        val range = (maxP - minP).takeIf { it > 0 } ?: 1.0

        fun norm(idx: Int): Pair<Float, Float> {
            val x = if (width <= 0f) 0f else idx / (prices.size - 1f) * width
            val raw = ((prices[idx] - minP) / range).toFloat()
            // flip + add vertical breathing room
            val y = height - (raw * (height - 12f)) - 6f
            return x to y
        }

        // Determine color by comparing first vs last
        val rising = prices.last() > prices.first()

        // Fill under the line
        val fillPath = Path().apply {
            val (x0, y0) = norm(0)
            moveTo(x0, y0)
            for (i in 1 until prices.size) {
                val (x, y) = norm(i)
                lineTo(x, y)
            }
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        val fillBrush = if (rising) fillUp else fillDown
        drawPath(fillPath, brush = Brush.verticalGradient(
            listOf(fillBrush, fillBrush.copy(alpha = 0.05f))
        ))

        // Smooth line (Catmull-Rom -> quadratic simplification)
        val linePath = Path()
        val (xStart, yStart) = norm(0)
        linePath.moveTo(xStart, yStart)
        for (i in 1 until prices.size) {
            val (x, y) = norm(i)
            // simple quadratic smoothing between points
            val (px, py) = norm(i - 1)
            val mx = (px + x) / 2f
            linePath.quadraticBezierTo(px, py, mx, py)
            linePath.lineTo(x, y)
        }
        drawPath(
            linePath,
            color = if (rising) GreenUp else if (prices.size >= 2 && prices.last() < prices.first()) RedDown else Gold,
            style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // endpoint dot
        val (ex, ey) = norm(prices.size - 1)
        val dotColor = if (rising) GreenUp else RedDown
        drawCircle(color = dotColor.copy(alpha = 0.35f), radius = 7f, center = Offset(ex, ey))
        drawCircle(color = dotColor, radius = 3.5f, center = Offset(ex, ey))
    })
}
