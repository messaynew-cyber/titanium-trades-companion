package com.titanium.trades.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.titanium.trades.MainViewModel
import com.titanium.trades.data.AlertEngine
import com.titanium.trades.data.model.TradeConfig
import com.titanium.trades.data.model.TradeSignal
import com.titanium.trades.ui.components.PositionRing
import com.titanium.trades.ui.components.PriceSparkline
import com.titanium.trades.ui.theme.AmbientBg
import com.titanium.trades.ui.theme.Gold
import com.titanium.trades.ui.theme.GoldDeep
import com.titanium.trades.ui.theme.GoldGlow
import com.titanium.trades.ui.theme.GoldHorizontal
import com.titanium.trades.ui.theme.GreenUp
import com.titanium.trades.ui.theme.Neutral
import com.titanium.trades.ui.theme.OledBlack
import com.titanium.trades.ui.theme.RedDown
import com.titanium.trades.ui.theme.surface1
import com.titanium.trades.ui.theme.surface2
import com.titanium.trades.ui.theme.surface3
import com.titanium.trades.ui.theme.TextDim
import com.titanium.trades.ui.theme.TextHigh
import com.titanium.trades.ui.theme.TextMid
import com.titanium.trades.ui.theme.outline1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel, onOpenSettings: () -> Unit) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val cfg = ui.config
    val priceUsd = ui.price?.usd
    val signal = if (priceUsd != null) AlertEngine.signal(priceUsd, cfg) else TradeSignal.NEUTRAL
    val pnl = if (priceUsd != null) AlertEngine.pnlPercent(priceUsd, cfg) else null

    Scaffold(
        containerColor = OledBlack,
        topBar = { DashboardTopBar(onOpenSettings = onOpenSettings) }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AmbientBg)
                .padding(pad),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { HeroCard(ui = ui) }
            item { RingSection(cfg = cfg, currentPrice = priceUsd, signal = signal, pnl = pnl) }
            item { LevelsRow(cfg = cfg, currentPrice = priceUsd) }
            if (ui.history.size >= 2) {
                item { SparklineCard(history = ui.history, currentPrice = priceUsd) }
            }
            item { AlertsBanner(armed = cfg.stopLoss != null || cfg.takeProfit != null, onOpenSettings = onOpenSettings) }
        }
    }
}

// ───────────────────────────── TOP BAR ─────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(onOpenSettings: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(30.dp)
                        .background(GoldHorizontal, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.ShowChart,
                        contentDescription = null,
                        tint = OledBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("TITANIUM", color = TextHigh, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Text("TRADES", color = Gold, style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 3.sp)
                }
            }
        },
        actions = {
            // Market "LIVE" status chip + settings
            Box(
                Modifier
                    .padding(end = 4.dp)
                    .background(surface2, CircleShape)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).background(GreenUp, CircleShape))
                    Spacer(Modifier.width(5.dp))
                    Text("LIVE", color = GreenUp, style = MaterialTheme.typography.labelMedium)
                }
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = TextMid)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = OledBlack,
            titleContentColor = TextHigh
        )
    )
}

// ───────────────────────────── HERO CARD ─────────────────────────────
@Composable
private fun HeroCard(ui: MainViewModel.UiState) {
    val price = ui.price
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = surface1.copy(alpha = 0.6f))
    ) {
        Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(surface2, surface1.copy(alpha=0.2f)))) ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("SOL / USD", color = TextMid, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.weight(1f))
                    Text(price?.source?.uppercase() ?: "…", color = Gold,
                        style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(8.dp))
                if (price == null) {
                    if (ui.loading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(22.dp), color = Gold, strokeWidth = 2.dp)
                            Spacer(Modifier.width(12.dp))
                            Text("Fetching live price…", color = TextDim, style = MaterialTheme.typography.bodyMedium)
                        }
                    } else Text("Offline — no live feed", color = RedDown, style = MaterialTheme.typography.bodyMedium)
                } else {
                    // Animated price ticker
                    AnimatedContent(
                        targetState = price.usd,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "priceTicker",
                        modifier = Modifier.fillMaxWidth()
                    ) { value ->
                        Text(
                            "$" + formatPriceFull(value),
                            color = TextHigh,
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val chg = price.change24hPct
                        if (chg != null) {
                            val up = chg >= 0
                            val tone = if (up) GreenUp else RedDown
                            Surface(color = tone.copy(alpha = 0.12f), shape = CircleShape) {
                                Row(Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (up) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                        null,
                                        tint = tone,
                                        modifier = Modifier.size(18.dp).padding(bottom = 1.dp)
                                    )
                                    Text((if (up) "+" else "") + "%.2f".format(chg) + "%", color = tone,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("24h", color = TextDim, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

private fun formatPriceFull(v: Double): String {
    val s = if (v >= 100) "%,.2f".format(v) else "%.2f".format(v)
    return s
}

// ───────────────────────────── RING SECTION ─────────────────────────────
@Composable
private fun RingSection(
    cfg: TradeConfig,
    currentPrice: Double?,
    signal: TradeSignal,
    pnl: Double?
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = surface1.copy(alpha = 0.45f))
    ) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("POSITION", color = Gold, style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(statusTextFor(signal, pnl, cfg), color = statusColorFor(signal, pnl),
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                if (pnl != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("P/L", color = TextDim, style = MaterialTheme.typography.labelSmall)
                        Text((if (pnl >= 0) "+" else "") + "%.2f".format(pnl) + "%",
                            color = if (pnl >= 0) GreenUp else RedDown,
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // The Ring — hero visual
            Box(contentAlignment = Alignment.Center) {
                PositionRing(
                    currentPrice = currentPrice,
                    stopLoss = cfg.stopLoss,
                    takeProfit = cfg.takeProfit,
                    size = 190.dp,
                    ringStroke = 18.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (cfg.stopLoss != null && cfg.takeProfit != null && currentPrice != null) {
                        val pctToTp = ((currentPrice - cfg.stopLoss) / (cfg.takeProfit - cfg.stopLoss) * 100)
                            .coerceIn(0.0, 100.0)
                        Text("%.1f%%".format(pctToTp), color = TextHigh,
                            style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                        Text("TO TARGET", color = TextDim, style = MaterialTheme.typography.labelSmall)
                    } else {
                        Text("ARM", color = Gold, style = MaterialTheme.typography.titleMedium)
                        Text("levels to track", color = TextDim, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // SL and TP inline mini-labels around the ring area
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                RingLevelTag(
                    "SL",
                    cfg.stopLoss,
                    active = currentPrice != null && cfg.stopLoss != null && currentPrice <= cfg.stopLoss,
                    isDown = true
                )
                RingLevelTag(
                    "TP",
                    cfg.takeProfit,
                    active = currentPrice != null && cfg.takeProfit != null && currentPrice >= cfg.takeProfit,
                    isDown = false
                )
            }
        }
    }
}

@Composable
private fun RingLevelTag(label: String, value: Double?, active: Boolean, isDown: Boolean) {
    val col = when {
        active && isDown -> RedDown
        active && !isDown -> GreenUp
        !isDown -> GreenUp
        else -> RedDown
    }
    val dim = when {
        value != null && !active -> col.copy(alpha = 0.85f)
        else -> col
    }
    Column(horizontalAlignment = if (isDown) Alignment.Start else Alignment.End) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isDown) {
                Surface(color = backdropOf(dim), shape = CircleShape) {
                    Text(label, Modifier.padding(horizontal = 9.dp, vertical = 2.dp), color = dim,
                        style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(color = backdropOf(dim), shape = CircleShape) {
                    Text(label, Modifier.padding(horizontal = 9.dp, vertical = 2.dp), color = dim,
                        style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(if (value != null) "$" + formatPriceFull(value) else "—",
            color = if (value != null) TextHigh else TextDim,
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        if (active) Text("● TRIGGERED", color = col, style = MaterialTheme.typography.labelSmall)
    }
}

private fun backdropOf(c: Color): Color = c.copy(alpha = 0.13f)

private fun statusColorFor(signal: TradeSignal, pnl: Double?): Color = when (signal) {
    TradeSignal.BELOW_SL -> RedDown
    TradeSignal.AT_TP -> GreenUp
    TradeSignal.IN_PROFIT -> GreenUp
    TradeSignal.IN_LOSS -> RedDown
    TradeSignal.NEUTRAL -> if (pnl != null && pnl >= 0) GreenUp else Gold
    TradeSignal.WAITING -> Gold
}

private fun statusTextFor(signal: TradeSignal, pnl: Double?, cfg: TradeConfig): String = when (signal) {
    TradeSignal.BELOW_SL -> "STOP-LOSS HIT"
    TradeSignal.AT_TP -> "TAKE-PROFIT HIT"
    TradeSignal.IN_PROFIT -> if (pnl != null && pnl >= 5) "IN PROFIT" else "IN PROFIT"
    TradeSignal.IN_LOSS -> "IN LOSS"
    TradeSignal.NEUTRAL -> if (pnl != null && pnl >= 0) "IN PROFIT" else "WATCHING"
    TradeSignal.WAITING -> "WAITING"
}

// ───────────────────────────── LEVEL STACK ─────────────────────────────
@Composable
private fun LevelsRow(cfg: TradeConfig, currentPrice: Double?) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LevelCard(
            modifier = Modifier.weight(1f),
            title = "STOP-LOSS",
            value = cfg.stopLoss,
            triggered = currentPrice != null && cfg.stopLoss != null && currentPrice <= cfg.stopLoss,
            isBad = true
        )
        LevelCard(
            modifier = Modifier.weight(1f),
            title = "TAKE-PROFIT",
            value = cfg.takeProfit,
            triggered = currentPrice != null && cfg.takeProfit != null && currentPrice >= cfg.takeProfit,
            isBad = false
        )
    }
    // entry line (optional)
    if (cfg.entryPrice != null) {
        Row(Modifier.padding(horizontal = 4.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("ENTRY", color = TextDim, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(8.dp))
            Text("$" + formatPriceFull(cfg.entryPrice), color = TextMid,
                style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun LevelCard(
    modifier: Modifier,
    title: String,
    value: Double?,
    triggered: Boolean,
    isBad: Boolean
) {
    val accent = if (isBad) RedDown else GreenUp
    val borderColor = when {
        triggered -> accent
        value == null -> outline1
        else -> Gold.copy(alpha = 0.28f)
    }
    Card(
        modifier = modifier.animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surface1),
        border = if (triggered) BorderStroke(1.5.dp, accent) else BorderStroke(1.dp, borderColor)
    ) {
        Column(Modifier.fillMaxWidth().background(
            if (triggered) Brush.verticalGradient(listOf(accent.copy(alpha=0.14f), surface1.copy(alpha=0f)))
            else Brush.verticalGradient(listOf(Gold.copy(alpha=0.06f), surface1.copy(alpha=0f)))
        ).padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = TextMid, style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.weight(1f))
                Surface(color = if (triggered) accent.copy(alpha=0.2f) else surface3, shape = CircleShape) {
                    Text(if (triggered) "HIT" else "SET", Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = if (triggered) accent else TextDim,
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(if (value != null) "$" + formatPriceFull(value) else "Not set",
                color = if (value != null) TextHigh else TextDim,
                style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, maxLines = 1)
            if (triggered) {
                Spacer(Modifier.height(6.dp))
                Text(if (isBad) "Protect capital!" else "Consider locking in",
                    color = accent, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// ───────────────────────────── SPARKLINE ─────────────────────────────
@Composable
private fun SparklineCard(history: List<Double>, currentPrice: Double?) {
    val first = history.first()
    val last = history.lastOrNull()
    val rising = last != null && last > first
    val tick = if (rising) GreenUp else if (last != null && last < first) RedDown else Gold

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = surface1.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, outline1)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("RECENT PRICE", color = TextDim, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.weight(1f))
                currentPrice?.let { c ->
                    Text(if (rising) "▲" else "▼", color = tick,
                        style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.width(5.dp))
                    Text("$" + formatPriceFull(c), color = TextHigh,
                        style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(10.dp))
            PriceSparkline(
                prices = history,
                modifier = Modifier.fillMaxWidth().height(84.dp)
            )
            Spacer(Modifier.height(2.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("low " + (history.minOrNull()?.let { "$" + formatPriceFull(it) } ?: "…"),
                    color = TextDim, style = MaterialTheme.typography.labelSmall)
                Text("high " + (history.maxOrNull()?.let { "$" + formatPriceFull(it) } ?: "…"),
                    color = TextDim, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

// ───────────────────────────── ALERTS BANNER ─────────────────────────────
@Composable
private fun AlertsBanner(armed: Boolean, onOpenSettings: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = surface2)
    ) {
        Row(
            Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(surface2, Gold.copy(alpha = 0.05f), surface2))
            ).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.NotificationsNone, contentDescription = null, tint = if (armed) Gold else TextDim)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(if (armed) "Alerts armed" else "Alerts not configured",
                    color = TextHigh, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Text(
                    if (armed) "Push fired when price crosses your stop-loss or take-profit."
                    else "Set SL + TP to receive push alerts even when the app is closed.",
                    color = TextDim, style = MaterialTheme.typography.bodySmall
                )
            }
            OutlinedButton(onClick = onOpenSettings, border = BorderStroke(1.dp, Gold.copy(alpha=0.4f)),
                contentPadding = PaddingValues(horizontal = 12.dp)) {
                Text("Set", color = Gold)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Rounded.Edit, contentDescription = null, tint = Gold, modifier = Modifier.size(14.dp))
            }
        }
    }
}

