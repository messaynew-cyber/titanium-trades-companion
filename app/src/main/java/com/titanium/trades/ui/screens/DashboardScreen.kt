package com.titanium.trades.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.titanium.trades.MainViewModel
import com.titanium.trades.data.AlertEngine
import com.titanium.trades.data.model.PriceSnapshot
import com.titanium.trades.data.model.TradeConfig
import com.titanium.trades.data.model.TradeSignal
import com.titanium.trades.ui.theme.CardBlack
import com.titanium.trades.ui.theme.Gold
import com.titanium.trades.ui.theme.GreenUp
import com.titanium.trades.ui.theme.OledBlack
import com.titanium.trades.ui.theme.RedDown
import com.titanium.trades.ui.theme.SurfaceElevated
import com.titanium.trades.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel, onOpenSettings: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val config = uiState.config

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ShowChart, contentDescription = null, tint = Gold)
                        Spacer(Modifier.width(8.dp))
                        Text("Titanium Trades", color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OledBlack,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { PriceHeroCard(price = uiState.price, loading = uiState.loading) }
            item { PositionCard(cfg = config, currentPrice = uiState.price?.usd) }
            item { AlertsNote(currentPrice = uiState.price?.usd, config = config) }
        }
    }
}

@Composable
private fun PriceHeroCard(price: PriceSnapshot?, loading: Boolean) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("SOL / USD", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            when {
                price == null && loading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(22.dp), color = Gold, strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("Fetching live price…", color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
                price != null -> {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$" + formatPrice(price.usd),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.headlineLarge)
                    }
                    Spacer(Modifier.height(4.dp))
                    price.change24hPct?.let { change ->
                        val up = change >= 0
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (up) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = if (up) GreenUp else RedDown, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text((if (up) "+" else "") + "%.2f".format(change) + "%  24h",
                                color = if (up) GreenUp else RedDown,
                                style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Source: ${price.source}   •   updates every 30s",
                        color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                }
                else -> {
                    Text("Offline — could not reach price feed", color = RedDown,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun PositionCard(cfg: TradeConfig, currentPrice: Double?) {
    val price = currentPrice
    val signal = if (price != null) AlertEngine.signal(price, cfg) else TradeSignal.NEUTRAL
    val pnl = if (price != null) AlertEngine.pnlPercent(price, cfg) else null
    val statusColor = statusColorFor(signal, pnl)

    Card(shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated)) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("POSITION", color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(statusColor, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(statusTextFor(signal, pnl, cfg), color = statusColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LevelBox("STOP-LOSS", cfg.stopLoss,
                    price != null && cfg.stopLoss != null && price <= cfg.stopLoss)
                LevelBox("TAKE-PROFIT", cfg.takeProfit,
                    price != null && cfg.takeProfit != null && price >= cfg.takeProfit)
            }
            if (price == null && (cfg.stopLoss == null || cfg.takeProfit == null)) {
                Spacer(Modifier.height(14.dp))
                Text("Set a stop-loss & take-profit in ⚙ Settings to arm live alerts.",
                    color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            if (pnl != null) {
                Spacer(Modifier.height(16.dp))
                Text("P/L vs entry ${if (pnl >= 0) "+" else ""}${"%.2f".format(pnl)}%",
                    color = if (pnl >= 0) GreenUp else RedDown,
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BoxPill(color: Color, text: String) {
    Row(
        modifier = Modifier.background(color.copy(alpha = 0.18f), CircleShape).padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(6.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(text, color = color, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun LevelBox(label: String, value: Double?, triggered: Boolean) {
    Column(
        modifier = Modifier
            .weight(1f)
            .background(CardBlack, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(6.dp))
        val levelColor = when {
            triggered && label == "STOP-LOSS" -> RedDown
            triggered && label == "TAKE-PROFIT" -> GreenUp
            value == null -> TextSecondary
            else -> MaterialTheme.colorScheme.onSurface
        }
        Text(if (value != null) "$" + formatPrice(value) else "Not set",
            color = levelColor, style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold, maxLines = 1)
        if (triggered) {
            Spacer(Modifier.height(4.dp))
            Text("● TRIGGERED", color = if (label == "STOP-LOSS") RedDown else GreenUp,
                style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun AlertsNote(currentPrice: Double?, config: TradeConfig) {
    val armed = config.stopLoss != null || config.takeProfit != null
    Card(shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = OledBlack),
        border = BorderStroke(1.dp, CardBlack)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("ALERTS", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(6.dp))
            Text(
                if (armed) {
                    "Background worker checks price every 15 min (even when app is closed) and pushes a notification when price crosses your stop-loss or take-profit."
                } else {
                    "No stop-loss / take-profit set. Open ⚙ Settings, set both levels, and Titanium Trades will push alerts when they trigger."
                },
                color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun statusColorFor(signal: TradeSignal, pnl: Double?): Color = when (signal) {
    TradeSignal.BELOW_SL -> RedDown
    TradeSignal.AT_TP -> GreenUp
    TradeSignal.IN_PROFIT -> GreenUp
    TradeSignal.IN_LOSS -> RedDown
    TradeSignal.NEUTRAL, TradeSignal.WAITING -> Gold
}

private fun statusTextFor(signal: TradeSignal, pnl: Double?, cfg: TradeConfig): String = when (signal) {
    TradeSignal.BELOW_SL -> "STOP-LOSS HIT"
    TradeSignal.AT_TP -> "TAKE-PROFIT REACHED"
    TradeSignal.IN_PROFIT -> "IN PROFIT"
    TradeSignal.IN_LOSS -> "IN LOSS"
    TradeSignal.NEUTRAL -> if (pnl != null && pnl >= 0) "IN PROFIT" else "WATCHING"
    TradeSignal.WAITING -> "WAITING"
}

private fun formatPrice(v: Double): String {
    // Keep this dependency-light; no NumberFormat needed for SOL's current range
    return if (v >= 100) "%.2f".format(v) else "%.4f".format(v)
}
