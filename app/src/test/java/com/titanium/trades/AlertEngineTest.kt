package com.titanium.trades

import com.titanium.trades.data.AlertEngine
import com.titanium.trades.data.model.PriceSnapshot
import com.titanium.trades.data.model.TradeConfig
import com.titanium.trades.data.model.TradeSignal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Unit tests for the pure alert-decision engine (no Android needed). */
class AlertEngineTest {

    private fun snap(usd: Double) = PriceSnapshot("SOL", usd, null, "Test", 0L)

    @Test
    fun stopLossTriggersBelowThreshold() {
        val cfg = TradeConfig(stopLoss = 80.76, takeProfit = 105.34)
        assertEquals(TradeSignal.BELOW_SL, AlertEngine.signal(80.00, cfg))
    }

    @Test
    fun takeProfitTriggersAtThreshold() {
        val cfg = TradeConfig(stopLoss = 80.76, takeProfit = 105.34)
        assertEquals(TradeSignal.AT_TP, AlertEngine.signal(106.00, cfg))
    }

    @Test
    fun neutralBetweenLevels() {
        val cfg = TradeConfig(stopLoss = 80.76, takeProfit = 105.34)
        assertEquals(TradeSignal.NEUTRAL, AlertEngine.signal(93.00, cfg))
    }

    @Test
    fun evaluateReturnsCriticalOnSlHit() {
        val cfg = TradeConfig(entryPrice = 90.0, stopLoss = 80.76, takeProfit = 105.34)
        val event = AlertEngine.evaluate(snap(80.00), cfg)
        assertNotNull(event)
        assertEquals("STOP-LOSS HIT", event?.title)
    }

    @Test
    fun evaluateReturnsSuccessOnTpHit() {
        val cfg = TradeConfig(entryPrice = 90.0, stopLoss = 80.76, takeProfit = 105.34)
        val event = AlertEngine.evaluate(snap(106.00), cfg)
        assertNotNull(event)
        assertEquals("TAKE-PROFIT REACHED 🎯", event?.title)
    }

    @Test
    fun noThresholds_noAlert() {
        val cfg = TradeConfig()
        assertNull(AlertEngine.evaluate(snap(93.0), cfg))
    }

    @Test
    fun alertsDisabled_noAlert() {
        val cfg = TradeConfig(stopLoss = 80.76, alertsEnabled = false)
        assertNull(AlertEngine.evaluate(snap(80.0), cfg))
    }

    @Test
    fun pnlPercentComputed() {
        val cfg = TradeConfig(entryPrice = 90.0)
        assertEquals(10.0, AlertEngine.pnlPercent(99.0, cfg)!!, 0.0001)
    }
}
