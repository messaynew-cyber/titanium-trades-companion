package com.titanium.trades.ui.cockpit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titanium.trades.data.model.RegimeSignal
import com.titanium.trades.enginevm.CockpitViewModel
import com.titanium.trades.ui.components.PriceSparkline
import com.titanium.trades.ui.theme.AmbientBg
import com.titanium.trades.ui.theme.Gold
import com.titanium.trades.ui.theme.GoldHorizontal
import com.titanium.trades.ui.theme.GreenUp
import com.titanium.trades.ui.theme.OledBlack
import com.titanium.trades.ui.theme.RedDown
import com.titanium.trades.ui.theme.surface1
import com.titanium.trades.ui.theme.surface2
import com.titanium.trades.ui.theme.surface3
import com.titanium.trades.ui.theme.TextDim
import com.titanium.trades.ui.theme.TextHigh
import com.titanium.trades.ui.theme.TextMid

/** Flagship screen: the live engine cockpit. */
@Composable
fun CockpitHomeScreen(
    viewModel: CockpitViewModel,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val snap = state.snapshot
    val status = snap?.status
    val stat = status?.statistics

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AmbientBg),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── status hero header
        item { EngineHeader(status = status, loading = state.loading && !state.loaded, error = state.error, vm = viewModel,
            darkTheme = darkTheme, onToggleTheme = onToggleTheme) }

        if (state.error != null && status == null) {
            item {
                ErrorCard(message = state.error!!)
            }
        }
        if (status != null) {
            // ── engine regime + equity pulse
            item { PulseRow(status = status) }
            // ── per-symbol watchlist (multi-symbol tier)
            val watch = viewModel.watchlist()
            if (watch.isNotEmpty()) {
                item { SectionLabel("MARKET REGIME", "per-symbol engine signal feed") }
                item { WatchlistRow(watch = watch) }
            }
            // ── open position + today stat
            item { PositionPulse(status = status) }
            // ── signals strip
            item { SectionLabel("SIGNAL FEED", "last trades pushed by the engine") }
            item { SignalStrip(stat = stat) }
            // ── equity curve from downsampled history
            val curve = snap?.equityCurve ?: emptyList()
            if (curve.size >= 2) {
                item { SectionLabel("EQUITY", "master paper account") }
                item { EquityCard(curve = curve, status = status) }
            }
        }
        item { Spacer(Modifier.height(2.dp)) }
    }
}

// ─────────────────────────── HELPERS/THEMING ───────────────────────────
internal fun regimeColor(r: String?): Color = when (r?.uppercase()) {
    "BULL" -> GreenUp
    "BEAR" -> RedDown
    "CHOP" -> Gold
    else -> TextMid
}

@Composable
internal fun SectionLabel(title: String, sub: String = "") {
    Column(Modifier.padding(top = 2.dp, start = 2.dp)) {
        Text(title, color = Gold, style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        if (sub.isNotEmpty()) {
            Text(sub, color = TextDim, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// ─────────────────────────── ENGINE HEADER ───────────────────────────
@Composable
private fun EngineHeader(status: com.titanium.trades.data.model.EngineStatus?, loading: Boolean, error: String?, vm: CockpitViewModel, darkTheme: Boolean, onToggleTheme: () -> Unit) {
    val ok = status != null
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surface2.copy(alpha = 0.9f)),
        border = BorderStroke(1.dp, if (ok) Gold.copy(alpha = 0.3f) else surface3)
    ) {
        Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(surface2, OledBlack)))) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // status bullet
                    Box(Modifier.size(10.dp).background(if (ok) GreenUp else RedDown, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text("HABESHA ENGINE", color = if (ok) TextHigh else RedDown,
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp)
                    Spacer(Modifier.weight(1f))
                    // PAPER / LIVE badge
                    if (status?.paper == true) PillChip("PAPER", Gold.copy(alpha = 0.16f), Gold)
                    else if (ok) PillChip("LIVE", GreenUp.copy(alpha = 0.16f), GreenUp)
                    IconButton(onClick = { vm.refresh() }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Autorenew, null, tint = TextMid, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(2.dp))
                    IconButton(onClick = onToggleTheme, modifier = Modifier.size(28.dp)) {
                        Icon(
                            if (darkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = Gold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                // equity big + cash (flex columns so the caption can NEVER wrap)
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(0.62f)) {
                        Text("EQUITY", color = TextDim, style = MaterialTheme.typography.labelSmall)
                        Text(
                            "$" + (status?.equityUsd?.let { f2(it) } ?: "—"),
                            color = TextHigh,
                            fontSize = 38.sp, fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                    Column(
                        Modifier.weight(0.38f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("CASH", color = TextDim, style = MaterialTheme.typography.labelSmall)
                        Text("$" + (status?.cashUsd?.let { f2(it) } ?: "—"),
                            color = TextMid, style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        Spacer(Modifier.height(6.dp))
                        status?.let {
                            Text("${it.symbolPrefix()} · cycle ${it.loopSeconds}s · updated ${shortAgo(it.updated)}",
                                color = TextDim, style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                    }
                }
                if (loading && status == null) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(18.dp), color = Gold, strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Contacting engine…", color = TextDim)
                    }
                }
            }
        }
    }
}

// extras
private fun com.titanium.trades.data.model.EngineStatus.symbolPrefix(): String = symbol?.replace("USD", "") ?: ""

@Composable
private fun PillChip(text: String, bg: Color, fg: Color) {
    Surface(color = bg, shape = CircleShape) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 3.dp), color = fg,
            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

private fun f2(v: Double): String = if (v >= 100) "%,.2f".format(v) else "%.2f".format(v)

private fun shortAgo(iso: String?): String {
    if (iso == null) return ""
    return try {
        val t = java.time.OffsetDateTime.parse(iso)
        val secs = java.time.Duration.between(t, java.time.OffsetDateTime.now()).seconds
        when {
            secs < 5 -> "just now"
            secs < 120 -> "${secs}s"
            else -> "${secs / 60}m"
        }
    } catch (e: Exception) { "" }
}

// ─────────────────────────── ERROR CARD ───────────────────────────
@Composable
private fun ErrorCard(message: String) {
    Surface(color = RedDown.copy(alpha = 0.08f), shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, RedDown.copy(alpha=0.4f))) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.ErrorOutline, null, tint = RedDown, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("Cannot reach the engine feed. Retrying…\n$message", color = TextMid,
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

// ─────────────────────────── PULSE ROW (watched-symbol HMM dial) ───────────────────────────
@Composable
private fun PulseRow(status: com.titanium.trades.data.model.EngineStatus) {
    val reg = status.regimeBlock ?: return
    Card(shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surface1.copy(alpha = 0.55f)),
        border = BorderStroke(1.dp, surface3)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // regime glow disc (pure color readout, no misleading ring)
            val rc = regimeColor(reg.regime)
            Box(
                Modifier.size(96.dp).background(rc.copy(alpha = 0.10f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.size(74.dp).background(rc.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center) {
                    if (reg.lastPrice > 0) Text("${formatK(reg.lastPrice)}", color = TextHigh,
                        style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    else Text("—", color = TextMid)
                }
                Box(Modifier.size(96.dp), contentAlignment = Alignment.TopCenter) {
                    Box(Modifier.size(10.dp).background(rc, CircleShape))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("HMM REGIME · ${status.symbol ?: ""}", color = TextDim,
                    style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(reg.regime ?: "—",
                        color = regimeColor(reg.regime),
                        fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(10.dp))
                    Surface(color = regimeColor(reg.regime).copy(alpha = .14f), shape = CircleShape) {
                        Text(reg.action ?: "—", Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            color = regimeColor(reg.regime),
                            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Metric("SCORE", "%.2f".format(reg.score))
                    Metric("RSI", "%.1f".format(reg.rsi))
                    Metric("HMMₚ", "%.0f%%".format(reg.hmmProb * 100))
                }
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column {
        Text(label, color = TextDim, style = MaterialTheme.typography.labelSmall)
        Text(value, color = TextHigh, style = MaterialTheme.typography.titleSmall,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
    }
}

// ─────────────────────────── WATCHLIST (multi-symbol cards) ───────────────────────────
@Composable
private fun WatchlistRow(watch: List<RegimeSignal>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(watch, key = { it.symbol ?: it.id }) { s -> WatchCard(s) }
    }
}

@Composable
private fun WatchCard(s: RegimeSignal) {
    val col = regimeColor(s.regime)
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surface2),
        border = BorderStroke(1.dp, col.copy(alpha = 0.35f)),
        modifier = Modifier.width(150.dp)
    ) {
        Column(Modifier.fillMaxWidth().background(
            Brush.verticalGradient(listOf(col.copy(alpha = 0.08f), surface2))).padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(s.symbol ?: "", fontWeight = FontWeight.ExtraBold,
                    color = TextHigh, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(8.dp).background(col, CircleShape))
            }
            Spacer(Modifier.height(6.dp))
            Text(regimeText(s.regime), color = col, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(3.dp))
            Text(s.action ?: "—", color = if (s.action == "BUY") GreenUp else TextMid,
                style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row {
                Column { Text("Q", color = TextDim, style = MaterialTheme.typography.labelSmall)
                    Text("%.0f".format(s.quality), style = MaterialTheme.typography.bodyMedium) }
                Spacer(Modifier.width(18.dp))
                Column { Text("PRICE", color = TextDim, style = MaterialTheme.typography.labelSmall)
                    Text("$" + f2(s.price), style = MaterialTheme.typography.bodyMedium) }
            }
            Spacer(Modifier.height(6.dp))
            Text(shortTs(s.ts), color = TextDim, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun regimeText(r: String?): String = when (r?.uppercase()) {
    "BULL" -> "BULL"
    "BEAR" -> "BEAR"
    "CHOP" -> "CHOP"
    else -> r ?: "—"
}

private fun shortTs(ts: String?): String {
    if (ts == null) return ""
    return try {
        val t = java.time.OffsetDateTime.parse(ts)
        t.toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) { "" }
}

// ─────────────────────────── POSITION PULSE (open trade live) ───────────────────────────
@Composable
private fun PositionPulse(status: com.titanium.trades.data.model.EngineStatus) {
    val stat = status.statistics ?: return
    val ot = stat.openTrade
    Card(shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surface1.copy(alpha = 0.55f)),
        border = BorderStroke(1.dp, surface3)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.TrendingUp, null, tint = Gold, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("OPEN POSITION", color = TextDim, style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.weight(1f))
                if (stat.todayCount > 0) {
                    Text("${stat.todayCount} TR TODAY · ${
                        (if (stat.todayPnl >= 0) "+" else "") + "%.2f".format(stat.todayPnl)} (engine)",
                        color = if (stat.todayPnl >= 0) GreenUp else RedDown,
                        style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(10.dp))
            if (ot != null && ot.symbol != null) {
                val pct = ot.pnlPercent
                val pnlColor = if (pct != null && pct >= 0) GreenUp else RedDown
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(ot.symbol, color = TextHigh, style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold)
                            Spacer(Modifier.width(8.dp))
                            PillChip(ot.side?.uppercase() ?: "—",
                                Gold.copy(alpha = 0.12f), Gold)
                            Spacer(Modifier.width(6.dp))
                            PillChip((ot.status ?: "").uppercase(),
                                pnlColor.copy(alpha = 0.14f), pnlColor)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("ENTRY $" + f2(ot.entry ?: 0.0), color = TextMid,
                            style = MaterialTheme.typography.bodySmall)
                        Text("NOW $" + f2(ot.currentPrice ?: 0.0) +
                            " · " + (ot.qty?.let { "%.3f".format(it) + " " + ot.symbol } ?: ""),
                            color = TextHigh,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(pctChart(pct), color = pnlColor, fontSize = 26.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Bold)
                        Text("mark-to-market", color = TextDim, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(Modifier.height(12.dp))
                // SL / TP ticker
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    LevelTip("SL", ot.stop, RedDown)
                    LevelTip("TP", ot.take, GreenUp)
                }
            } else {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(6.dp))
                    Text("No open position", color = TextMid, style = MaterialTheme.typography.bodyMedium)
                    Text("Engine is flat right now — watch the watchlist for its next entry.",
                        color = TextDim, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun LevelTip(label: String, value: Double?, accent: Color) {
    Column {
        Text(label, color = accent, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Text("$" + f2(value ?: 0.0), color = if (value != null) TextHigh else TextDim,
            style = MaterialTheme.typography.titleSmall,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
    }
}

private fun pctChart(pct: Double?): String = if (pct == null) "—" else (if (pct >= 0) "+" else "") + "%.2f%%".format(pct)

// ─────────────────────────── SIGNAL STRIP ───────────────────────────
@Composable
private fun SignalStrip(stat: com.titanium.trades.data.model.Statistics?) {
    val th = stat?.tradeHistory ?: emptyList()
    val recents = th.take(6)
    Card(shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surface1)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 6.dp)) {
            if (recents.isEmpty()) {
                Text("No trades recorded yet", Modifier.padding(12.dp), color = TextDim,
                    style = MaterialTheme.typography.bodySmall)
            } else {
                recents.forEach { tr ->
                    SignalRow(tr)
                }
            }
        }
    }
}

@Composable
private fun SignalRow(tr: com.titanium.trades.data.model.TradeRecord) {
    val open = tr.status == "open"
    val closed = tr.status == "closed"
    val pnlColor = when {
        tr.pnl != null && tr.pnl >= 0 -> GreenUp
        tr.pnl != null -> RedDown
        else -> TextDim
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(regimeColor(tr.regime), CircleShape))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tr.symbol ?: "—", fontWeight = FontWeight.ExtraBold, color = TextHigh,
                    style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.width(8.dp))
                Text((tr.regime ?: "") + (if (open) " · OPEN" else ""),
                    color = TextDim, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(2.dp))
            Text("Q${"%.0f".format(tr.quality ?: 0.0)} · entry $" + f2(tr.entry ?: 0.0),
                color = TextMid, style = MaterialTheme.typography.labelSmall)
        }
        if (tr.pnl != null) {
            Text((if (tr.pnl >= 0) "+" else "") + "%.2f".format(tr.pnl) + "%", color = pnlColor,
                style = MaterialTheme.typography.titleSmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
        } else {
            Text(if (open) "OPEN" else "—", color = TextDim, style = MaterialTheme.typography.labelSmall)
        }
    }
    // divider
}

// ─────────────────────────── EQUITY CARD ───────────────────────────
@Composable
private fun EquityCard(curve: List<com.titanium.trades.data.model.EquityPoint>, status: com.titanium.trades.data.model.EngineStatus) {
    val vals = curve.map { it.value }
    val first = vals.first()
    val last = vals.last()
    val pctMove = if (first > 0) (last - first) / first * 100.0 else 0.0
    val up = last >= first
    val col = if (up) GreenUp else RedDown
    Card(shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surface1.copy(alpha = 0.55f)),
        border = BorderStroke(1.dp, surface3)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("MASTER ACCOUNT", color = TextDim, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.weight(1f))
                Text("now $" + f2(last), color = TextHigh, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                Spacer(Modifier.width(8.dp))
                Text((if (pctMove >= 0) "+" else "") + "%.2f%%".format(pctMove), color = col,
                    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            PriceSparkline(
                prices = vals,
                modifier = Modifier.fillMaxWidth().height(90.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text("from " + (curve.firstOrNull()?.time ?: "") + " · ${vals.size} pts",
                color = TextDim, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun formatK(v: Double): String =
    if (v >= 1000) "%.1fk".format(v/1000.0) else if (v>0) "%.0f".format(v) else ""
