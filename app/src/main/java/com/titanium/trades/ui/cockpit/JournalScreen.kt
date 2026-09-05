package com.titanium.trades.ui.cockpit

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titanium.trades.data.model.TradeRecord
import com.titanium.trades.enginevm.CockpitViewModel
import com.titanium.trades.ui.theme.AmbientBg
import com.titanium.trades.ui.theme.Gold
import com.titanium.trades.ui.theme.GreenUp
import com.titanium.trades.ui.theme.RedDown
import com.titanium.trades.ui.theme.TextDim
import com.titanium.trades.ui.theme.TextHigh
import com.titanium.trades.ui.theme.TextMid
import com.titanium.trades.ui.theme.surface1
import com.titanium.trades.ui.theme.surface2

/** Trade journal + posture tallies. */
@Composable
fun JournalScreen(vm: CockpitViewModel) {
    val state by vm.state.collectAsState()
    val status = state.snapshot?.status
    val stat = status?.statistics
    val trades = stat?.tradeHistory ?: emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AmbientBg),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // posture chips
        item {
            val wl = stat?.winLoss
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = surface1)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    PostureBlock("WINS", wl?.wins ?: 0, GreenUp)
                    PostureBlock("LOSSES", wl?.losses ?: 0, RedDown)
                    PostureBlock("FLAT", wl?.flat ?: 0, Gold)
                    PostureBlock("WIN RATE", "%.1f%%".format(wl?.winRate ?: 0.0), TextHigh)
                }
            }
        }
        if (trades.isEmpty()) {
            item { Text("No trade history yet", color = TextDim,
                modifier = Modifier.padding(vertical = 40.dp, horizontal = 8.dp),
                style = MaterialTheme.typography.bodyMedium) }
        } else {
            item { SectionLabel("TRADE LEDGER", "${trades.size} trades on record") }
            items(trades) { tr -> TradeLedgerRow(tr) }
        }
    }
}

@Composable
private fun PostureBlock(label: String, value: String, fg: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = fg, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, color = TextDim, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun PostureBlock(label: String, v: Int, fg: Color) = PostureBlock(label, v.toString(), fg)

@Composable
private fun TradeLedgerRow(tr: TradeRecord) {
    val open = tr.status == "open"
    val pnlCol = when {
        tr.pnl != null && tr.pnl >= 0 -> GreenUp
        tr.pnl != null -> RedDown
        open -> Gold
        else -> TextDim
    }
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = surface2)) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(regimeColor(tr.regime), CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(tr.symbol ?: "—", fontWeight = FontWeight.ExtraBold, color = TextHigh,
                    style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.width(6.dp))
                Text((tr.side ?: "").uppercase() + (if (open) " · OPEN" else ""),
                    color = TextDim, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.weight(1f))
                Text(
                    when {
                        tr.pnl != null -> ((if (tr.pnl >= 0) "+" else "") + "%.2f%%".format(tr.pnl))
                        open -> "RUNNING"
                        else -> "q${"%.0f".format(tr.quality ?: 0.0)}"
                    }, color = pnlCol,
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("in $" + (tr.entry?.let { f2x(it) } ?: "—"), color = TextMid, style = MaterialTheme.typography.labelSmall)
                Text("SL $" + (tr.stop?.let { f2x(it) } ?: "—"), color = TextMid, style = MaterialTheme.typography.labelSmall)
                Text("TP $" + (tr.take?.let { f2x(it) } ?: "—"), color = TextMid, style = MaterialTheme.typography.labelSmall)
                Text("Q ${"%.0f".format(tr.quality ?: 0.0)}", color = TextMid, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(2.dp))
            Text(tr.openedAt ?: "", color = TextDim, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun f2x(v: Double): String = if (v >= 100) "%,.2f".format(v) else "%.2f".format(v)
