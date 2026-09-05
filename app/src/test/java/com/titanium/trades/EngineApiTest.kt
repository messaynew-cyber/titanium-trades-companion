package com.titanium.trades

import com.titanium.trades.data.EngineApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Decoder must parse the LIVE engine contract accurately. */
class EngineApiTest {

    private val sample = """
    {
      "updated": "2026-09-05T20:17:51.690433Z",
      "service": "active",
      "symbol": "BTCUSD",
      "paper": true,
      "loop_seconds": 60,
      "equity": "914.39",
      "cash": "306.04",
      "last_cycle": "2026-09-05T20:17:51.690433Z",
      "regime": {
        "regime": "BULL",
        "score": 0.8387,
        "chop_prob": 0.4053,
        "quality": 43.1,
        "action": "BUY",
        "atr": 120.4369,
        "last_price": 79744.4,
        "rsi": 28.28,
        "hmm_state": 1,
        "hmm_prob": 0.9722
      },
      "statistics": {
        "today_count": 3,
        "today_pnl": 0.2073,
        "open_trade": {
          "symbol": "ETHUSD",
          "side": "buy",
          "qty": 0.122958832,
          "entry": 2474.765,
          "stop": 2466.7688,
          "take": 2488.0918,
          "opened_at": "2026-09-05T19:55:53.797334",
          "status": "open",
          "current_price": 2449.565
        },
        "trade_history": [
          {
            "symbol": "ETHUSD", "side": "buy", "qty": 0.122,
            "entry": 2474.765, "stop": 2466.76, "take": 2488.09,
            "exit_price": null, "exit_time": null, "pnl": null,
            "quality": 78.61, "regime": "BULL",
            "opened_at": "2026-09-05T19:55:53", "status": "open"
          }
        ],
        "signal_history": [
          {
            "id": 58998, "ts": "2026-09-05T20:17:51.690433", "symbol": "SOLUSD",
            "regime": "CHOP", "score": 0.119, "quality": 27.5,
            "action": "HOLD", "price": 103.4015
          }
        ],
        "win_loss": { "wins": 45, "losses": 53, "flat": 0 }
      }
    }
    """.trimIndent()

    @Test
    fun decodesLiveContract() {
        val api = EngineApi()
        val st = api.decodeStatus(sample)
        assertNotNull(st)
        st!!
        assertEquals("active", st.service)
        assertEquals(true, st.paper)
        assertEquals("914.39", st.equity)
        assertEquals(914.39, st.equityUsd, 0.001)
        assertEquals(306.04, st.cashUsd, 0.001)
        assertEquals("BTCUSD", st.symbol)

        // regime block
        val r = st.regimeBlock
        assertNotNull(r)
        assertEquals("BULL", r!!.regime)
        assertEquals("BUY", r.action)
        assertEquals(0.8387, r.score, 0.0001)
        assertEquals(28.28, r.rsi, 0.001)
        assertEquals(0.9722, r.hmmProb, 0.001)

        // statistics
        val s = st.statistics
        assertNotNull(s)
        assertEquals(3, s!!.todayCount)
        assertEquals(0.2073, s.todayPnl, 0.0001)

        // open trade + mtm pnl
        val ot = s.openTrade
        assertNotNull(ot)
        assertEquals("ETHUSD", ot!!.symbol)
        assertEquals(2474.765, ot.entry!!, 0.0001)
        // price below entry => negative mtm
        assertTrue((ot.pnlPercent ?: 0.0) < 0)

        // history + signals + winloss
        assertEquals(1, s.tradeHistory.size)
        assertEquals(1, s.signalHistory.size)
        assertEquals("SOLUSD", s.signalHistory[0].symbol)
        assertEquals("CHOP", s.signalHistory[0].regime)
        val wl = s.winLoss
        assertEquals(45, wl!!.wins)
        assertEquals(45.9, wl.winRate, 0.5)
    }
}
