package com.titanium.trades.data.model

/**
 * Model mirroring the LIVE Habesha/Titanium engine status JSON published at
 * https://129.80.112.9/titanium_status.json (public, read-only).
 * Field names/types match the VPS byte-for-byte so parsing is robust.
 */
data class EngineStatus(
    val updated: String?,
    val service: String?,           // "active"
    val symbol: String?,            // currently-watched symbol e.g. "BTCUSD"
    val paper: Boolean,
    val loopSeconds: Int,
    val equity: String?,            // string from engine
    val cash: String?,
    val lastCycle: String?,
    val regimeBlock: RegimeBlock?,
    val statistics: Statistics?
) {
    val equityUsd: Double get() = equity?.toDoubleOrNull() ?: 0.0
    val cashUsd: Double get() = cash?.toDoubleOrNull() ?: 0.0
}

/** The HMM regime output for the active symbol (from "regime" block). */
data class RegimeBlock(
    val regime: String?,      // BULL / BEAR / CHOP
    val score: Double,
    val chopProb: Double,
    val quality: Double,
    val action: String?,      // BUY / HOLD / SELL
    val atr: Double,
    val lastPrice: Double,
    val rsi: Double,
    val hmmState: Int,
    val hmmProb: Double
)

/** Aggregated statistics block. */
data class Statistics(
    val todayCount: Int,
    val todayPnl: Double,
    val openTrade: OpenTrade?,
    val tradeHistory: List<TradeRecord>,
    val signalHistory: List<RegimeSignal>,
    val winLoss: WinLoss?
)

/** An open or recently closed position. */
data class OpenTrade(
    val symbol: String?, val side: String?, val qty: Double?,
    val entry: Double?, val stop: Double?, val take: Double?,
    val openedAt: String?, val status: String?, val currentPrice: Double?
) {
    /** Mark-to-market P/L % vs entry, when we have both. */
    val pnlPercent: Double?
        get() = if (entry != null && entry != 0.0 && currentPrice != null)
            (currentPrice - entry) / entry * 100.0 else null
}

/** One row of trade_history (open or closed). */
data class TradeRecord(
    val symbol: String?, val side: String?, val qty: Double?,
    val entry: Double?, val stop: Double?, val take: Double?,
    val exitPrice: Double?, val exitTime: String?, val pnl: Double?,
    val quality: Double?, val regime: String?, val openedAt: String?,
    val status: String?   // "open" | "closed"
)

/** One entry of signal_history (per-symbol regime feed). */
data class RegimeSignal(
    val id: Long, val ts: String?, val symbol: String?,
    val regime: String?, val score: Double, val quality: Double,
    val action: String?, val price: Double
)

/** Win/loss tallies. */
data class WinLoss(val wins: Int, val losses: Int, val flat: Int) {
    val total: Int get() = wins + losses + flat
    val winRate: Double get() = if (total == 0) 0.0 else wins.toDouble() / total * 100.0
}

/** One point of the equity-curve history. */
data class EquityPoint(val time: String, val value: Double)

/** Immutable result bucket the UI consumes. */
data class CockpitSnapshot(
    val status: EngineStatus?,
    val equityCurve: List<EquityPoint>,
    val fetchedAt: Long
)
