package com.titanium.trades.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.titanium.trades.ui.theme.Gold
import com.titanium.trades.ui.theme.GoldDeep
import com.titanium.trades.ui.theme.GreenUp
import com.titanium.trades.ui.theme.GreenDeep
import com.titanium.trades.ui.theme.RedDown
import com.titanium.trades.ui.theme.RedDeep
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * A circular "range dial": current price sits somewhere between stop-loss
 * (start of arc) and take-profit (end). The arc fills proportionally and
 * colors by where the price is in that range. When price crosses SL or TP,
 * the whole ring flashes the respective color.
 */
@Composable
fun PositionRing(
    currentPrice: Double?,
    stopLoss: Double?,
    takeProfit: Double?,
    modifier: Modifier = Modifier,
    ringStroke: Dp = 14.dp,
    size: Dp = 168.dp
) {
    val progress = remember {
        Animatable(0f)
    }

    // fraction of the way from SL to TP the price is (0..1), clamped
    val fraction: Float = when {
        currentPrice == null || stopLoss == null || takeProfit == null -> 0f
        takeProfit <= stopLoss -> 0.5f
        currentPrice <= stopLoss -> 0f
        currentPrice >= takeProfit -> 1f
        else -> ((currentPrice - stopLoss) / (takeProfit - stopLoss)).toFloat()
    }

    LaunchedEffect(fraction) {
        progress.animateTo(fraction, animationSpec = tween(800, easing = LinearOutSlowInEasing))
    }

    val isBelow = currentPrice != null && stopLoss != null && currentPrice <= stopLoss
    val isAbove = currentPrice != null && takeProfit != null && currentPrice >= takeProfit

    // driving pulse when near/at a limit
    val infinite = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (isBelow || isAbove) 1f else 0.55f,
        animationSpec = infiniteRepeatable(tween(1100))
    )

    val arcColor: Color = when {
        isBelow -> RedDown
        isAbove -> GreenUp
        fraction >= 0.5f -> GreenUp
        else -> Gold
    }

    Canvas(modifier = modifier.size(size)) {
        val stroke = ringStroke.toPx()
        val pad = stroke / 2f + 2f
        val arcSize = Size(this.size.width - pad * 2, this.size.height - pad * 2)
        val topLeft = Offset(pad, pad)
        val startAngle = 135f          // start bottom-left
        val sweepMax = 270f            // almost full circle gap at bottom
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = (this.size.width - 2f * pad) / 2f

        // Track (background)
        drawArc(
            color = Color.White.copy(alpha = 0.06f),
            startAngle = startAngle,
            sweepAngle = sweepMax,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )

        // Progress arc
        val sweep = progress.value * sweepMax
        val brush = if (isBelow) Brush.sweepGradient(listOf(RedDown, RedDown.copy(alpha=0.6f)), center = center)
            else if (isAbove) Brush.sweepGradient(listOf(Gold, GreenUp), center = center)
            else Brush.sweepGradient(listOf(RedDeep, Gold, GreenUp), center = center)
        drawArc(
            brush = brush,
            startAngle = startAngle,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )

        // small glow at the head marker
        val headAngle = Math.toRadians((startAngle + sweep).toDouble())
        val headX = center.x + radius * cos(headAngle).toFloat()
        val headY = center.y + radius * sin(headAngle).toFloat()
        drawCircle(color = arcColor.copy(alpha = 0.25f * glowAlpha), radius = stroke * 1.6f, center = Offset(headX, headY))
        drawCircle(color = arcColor, radius = stroke * 0.55f, center = Offset(headX, headY))

        // SL / TP tunnel tick s at arc endpoints
        drawLine(color = RedDown.copy(alpha = 0.9f),
            start = Offset(center.x + radius * cos(Math.toRadians(startAngle.toDouble())).toFloat(),
                center.y + radius * sin(Math.toRadians(startAngle.toDouble())).toFloat()),
            end = Offset(center.x + (radius + 10f) * cos(Math.toRadians(startAngle.toDouble())).toFloat(),
                center.y + (radius + 10f) * sin(Math.toRadians(startAngle.toDouble())).toFloat()),
            strokeWidth = 2.dp.value, cap = StrokeCap.Round)
    }
}

