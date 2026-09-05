package com.titanium.trades.data.model

/** Live price snapshot for a crypto asset. */
data class PriceSnapshot(
    val symbol: String,          // "SOL" or "BTC"
    val usd: Double,
    val change24hPct: Double?,   // nullable if not provided by source
    val source: String,
    val timestamp: Long
)

/** A configured trade (position) with stop-loss and take-profit levels. */
data class TradeConfig(
    val assetSymbol: String = "SOL",
    val entryPrice: Double? = null,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null,
    val quantity: Double? = null,
    val alertsEnabled: Boolean = true
)

/** Derived status of a trade given a live price. */
enum class TradeSignal { WAITING, BELOW_SL, AT_TP, IN_PROFIT, IN_LOSS, NEUTRAL }

/** Human-friendly name + threshold for an alert. */
data class AlertEvent(
    val title: String,
    val message: String,
    val severity: Severity
)

enum class Severity { INFO, WARNING, CRITICAL, SUCCESS }
