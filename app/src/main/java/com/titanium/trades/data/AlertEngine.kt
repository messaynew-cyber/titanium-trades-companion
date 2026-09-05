package com.titanium.trades.data

import com.titanium.trades.data.model.AlertEvent
import com.titanium.trades.data.model.PriceSnapshot
import com.titanium.trades.data.model.Severity
import com.titanium.trades.data.model.TradeConfig
import com.titanium.trades.data.model.TradeSignal

/**
 * Pure-alert decision logic (no Android dependencies) — evaluates a live price
 * against a trade's stop-loss / take-profit and decides what (if anything) to alert on.
 */
object AlertEngine {

    /** Classify the state of a trade at a given price. */
    fun signal(price: Double, cfg: TradeConfig): TradeSignal {
        val sl = cfg.stopLoss
        val tp = cfg.takeProfit
        return when {
            sl != null && price <= sl -> TradeSignal.BELOW_SL
            tp != null && price >= tp -> TradeSignal.AT_TP
            sl != null && tp != null -> {
                val range = tp - sl
                if (range > 0) {
                    val pct = (price - sl) / range
                    when {
                        pct >= 0.5 -> TradeSignal.IN_PROFIT
                        pct < 0.5 && cfg.entryPrice != null && price < cfg.entryPrice -> TradeSignal.IN_LOSS
                        else -> TradeSignal.NEUTRAL
                    }
                } else TradeSignal.NEUTRAL
            }
            else -> TradeSignal.NEUTRAL
        }
    }

    /** Build an alert event for a crossing. Returns null if thresholds not configured or not triggered. */
    fun evaluate(price: PriceSnapshot, cfg: TradeConfig): AlertEvent? {
        if (!cfg.alertsEnabled) return null
        val usd = price.usd
        return when (signal(usd, cfg)) {
            TradeSignal.BELOW_SL -> {
                val sl = cfg.stopLoss ?: return null
                AlertEvent(
                    "STOP-LOSS HIT",
                    price.symbol + " at $" + String.format("%.2f", usd) + " crossed SL $" + String.format("%.2f", sl),
                    Severity.CRITICAL
                )
            }
            TradeSignal.AT_TP -> {
                val tp = cfg.takeProfit ?: return null
                AlertEvent(
                    "TAKE-PROFIT REACHED \uD83C\uDFAF",
                    price.symbol + " hit $" + String.format("%.2f", usd) + ". TP $" + String.format("%.2f", tp) + " reached. Consider locking gains.",
                    Severity.SUCCESS
                )
            }
            TradeSignal.IN_PROFIT -> {
                // Only surface a nudge when strongly in profit
                val tp = cfg.takeProfit ?: return null
                val pct = (usd - tp * 0.85) / (tp * 0.15)
                if (pct > 0 && usd >= tp * 0.85) {
                    AlertEvent(
                        "Near Target",
                        price.symbol + " at $" + String.format("%.2f", usd) + " is within 15% of TP $" + String.format("%.2f", tp),
                        Severity.WARNING
                    )
                } else null
            }
            else -> null
        }
    }

    /** Rough USD gain/loss vs an optional entry, for dashboard color coding. */
    fun pnlPercent(current: Double, cfg: TradeConfig): Double? {
        val entry = cfg.entryPrice ?: return null
        if (entry <= 0) return null
        return ((current - entry) / entry) * 100.0
    }
}
