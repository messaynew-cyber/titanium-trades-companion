package com.titanium.trades.data

import android.content.Context
import com.titanium.trades.data.model.TradeConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single source of truth for trade configuration + cached price.
 * Persists the trade config in SharedPreferences so SL/TP survive app restarts.
 * Exposes reactive [StateFlow]s the UI collects with Lifecycle-aware operators.
 */
class TradeRepository(context: Context) {

    private val prefs = context.getSharedPreferences("titanium_trades", Context.MODE_PRIVATE)

    private val _tradeConfig = MutableStateFlow(loadConfig())
    val tradeConfig: StateFlow<TradeConfig> = _tradeConfig.asStateFlow()

    private val _cachedPrice = MutableStateFlow<CachedPrice?>(null)
    val cachedPrice: StateFlow<CachedPrice?> = _cachedPrice.asStateFlow()

    data class CachedPrice(val usd: Double, val timestamp: Long)

    fun updateConfig(transform: (TradeConfig) -> TradeConfig) {
        val updated = transform(_tradeConfig.value)
        _tradeConfig.value = updated
        save(updated)
    }

    fun setStopLoss(value: Double?) = updateConfig { it.copy(stopLoss = value) }
    fun setTakeProfit(value: Double?) = updateConfig { it.copy(takeProfit = value) }
    fun setEntryPrice(value: Double?) = updateConfig { it.copy(entryPrice = value) }
    fun setQuantity(value: Double?) = updateConfig { it.copy(quantity = value) }
    fun setAlertsEnabled(enabled: Boolean) = updateConfig { it.copy(alertsEnabled = enabled) }

    fun cachePrice(usd: Double) {
        _cachedPrice.value = CachedPrice(usd, System.currentTimeMillis())
    }

    private fun loadConfig(): TradeConfig {
        return TradeConfig(
            assetSymbol = prefs.getString("asset", "SOL") ?: "SOL",
            entryPrice = prefs.getFloat("entry", -1f).takeIf { it > 0 }?.toDouble(),
            stopLoss = prefs.getFloat("sl", -1f).takeIf { it > 0 }?.toDouble(),
            takeProfit = prefs.getFloat("tp", -1f).takeIf { it > 0 }?.toDouble(),
            quantity = prefs.getFloat("qty", -1f).takeIf { it > 0 }?.toDouble(),
            alertsEnabled = prefs.getBoolean("alerts", true)
        )
    }

    private fun save(cfg: TradeConfig) {
        prefs.edit()
            .putString("asset", cfg.assetSymbol)
            .putFloat("entry", cfg.entryPrice?.toFloat() ?: -1f)
            .putFloat("sl", cfg.stopLoss?.toFloat() ?: -1f)
            .putFloat("tp", cfg.takeProfit?.toFloat() ?: -1f)
            .putFloat("qty", cfg.quantity?.toFloat() ?: -1f)
            .putBoolean("alerts", cfg.alertsEnabled)
            .apply()
    }
}
