package com.titanium.trades.data

import com.titanium.trades.data.model.CockpitSnapshot
import com.titanium.trades.data.model.EngineStatus
import com.titanium.trades.data.model.EquityPoint
import com.titanium.trades.data.model.OpenTrade
import com.titanium.trades.data.model.RegimeBlock
import com.titanium.trades.data.model.RegimeSignal
import com.titanium.trades.data.model.Statistics
import com.titanium.trades.data.model.TradeRecord
import com.titanium.trades.data.model.WinLoss
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Reads the PUBLIC, read-only status of the live Habesha/Titanium engine on
 * the Oracle VPS. The phone is a remote cockpit: it consumes what the engine
 * already publishes and never writes/trades. HMM computation stays server-side.
 */
class EngineApi(
    private val client: OkHttpClient = defaultClient()
) {
    companion object {
        // Public endpoints (engine publishes these; served by the front-end nginx, read-only)
        private val HOST = System.getenv("TT_STATUS_HOST") ?: "http://129.80.112.9"
        private val STATUS_URL = "$HOST/titanium_status.json"
        private val EQUITY_URL = "$HOST/titanium_equity.json"

        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            // Engine cert is self-signed-ish to some providers; be lenient for a read-only feed
            .build()
    }

    /** Fetches both status and equity curve. Returns null if the core status is unreachable. */
    suspend fun fetchCockpit(): CockpitSnapshot? = withContext(Dispatchers.IO) {
        val status = fetchStatus()
        val equity = fetchEquity()
        CockpitSnapshot(status = status, equityCurve = equity, fetchedAt = System.currentTimeMillis())
    }

    suspend fun fetchStatus(): EngineStatus? = withContext(Dispatchers.IO) {
        try {
            val text = get(STATUS_URL) ?: return@withContext null
            decodeStatus(text)
        } catch (e: Exception) {
            null
        }
    }

    /** Returns a downsampled-ish list (caller keeps it small for UI). */
    suspend fun fetchEquity(maxPoints: Int = 240): List<EquityPoint> = withContext(Dispatchers.IO) {
        try {
            val text = get(EQUITY_URL) ?: return@withContext emptyList()
            val arr = JSONArray(text)
            val all = ArrayList<EquityPoint>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val t = o.optString("t", "")
                val e = o.optDouble("e", Double.NaN)
                if (e.isNaN()) continue
                all.add(EquityPoint(t, e))
            }
            downsample(all, maxPoints)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ───── decoding ─────
    internal fun decodeStatus(body: String): EngineStatus? {
        val root = JSONObject(body)
        return EngineStatus(
            updated = root.optString("updated", null)?.takeIf { it.isNotEmpty() },
            service = root.optString("service", null)?.takeIf { it.isNotEmpty() },
            symbol = root.optString("symbol", null)?.takeIf { it.isNotEmpty() },
            paper = root.optBoolean("paper", false),
            loopSeconds = root.optInt("loop_seconds", 0),
            equity = root.optString("equity", null)?.takeIf { it.isNotEmpty() },
            cash = root.optString("cash", null)?.takeIf { it.isNotEmpty() },
            lastCycle = root.optString("last_cycle", null)?.takeIf { it.isNotEmpty() },
            regimeBlock = decodeRegime(root.optJSONObject("regime")),
            statistics = decodeStats(root.optJSONObject("statistics"))
        )
    }

    private fun decodeRegime(o: JSONObject?): RegimeBlock? {
        if (o == null) return null
        return RegimeBlock(
            regime = o.optString("regime", null)?.takeIf { it.isNotEmpty() },
            score = o.optDouble("score", 0.0),
            chopProb = o.optDouble("chop_prob", 0.0),
            quality = o.optDouble("quality", 0.0),
            action = o.optString("action", null)?.takeIf { it.isNotEmpty() },
            atr = o.optDouble("atr", 0.0),
            lastPrice = o.optDouble("last_price", 0.0),
            rsi = o.optDouble("rsi", 0.0),
            hmmState = o.optInt("hmm_state", -1),
            hmmProb = o.optDouble("hmm_prob", 0.0)
        )
    }

    private fun decodeStats(o: JSONObject?): Statistics? {
        if (o == null) return null
        return Statistics(
            todayCount = o.optInt("today_count", 0),
            todayPnl = o.optDouble("today_pnl", 0.0),
            openTrade = decodeOpenTrade(o.optJSONObject("open_trade")),
            tradeHistory = decodeList(o.optJSONArray("trade_history")) { j -> decodeTrade(j) },
            signalHistory = decodeList(o.optJSONArray("signal_history")) { j -> decodeSignal(j) },
            winLoss = decodeWinLoss(o.optJSONObject("win_loss"))
        )
    }

    private fun decodeOpenTrade(o: JSONObject?): OpenTrade? {
        if (o == null) return null
        return OpenTrade(
            symbol = o.optString("symbol", null)?.takeIf { it.isNotEmpty() },
            side = o.optString("side", null)?.takeIf { it.isNotEmpty() },
            qty = o.optNullableDouble("qty"),
            entry = o.optNullableDouble("entry"),
            stop = o.optNullableDouble("stop"),
            take = o.optNullableDouble("take"),
            openedAt = o.optString("opened_at", null)?.takeIf { it.isNotEmpty() },
            status = o.optString("status", null)?.takeIf { it.isNotEmpty() },
            currentPrice = o.optNullableDouble("current_price")
        )
    }

    private fun decodeTrade(j: JSONObject): TradeRecord {
        fun nd(n: String) = j.optNullableDouble(n)
        return TradeRecord(
            symbol = j.optString("symbol", null)?.takeIf { it.isNotEmpty() },
            side = j.optString("side", null)?.takeIf { it.isNotEmpty() },
            qty = nd("qty"), entry = nd("entry"), stop = nd("stop"), take = nd("take"),
            exitPrice = nd("exit_price"),
            exitTime = j.optString("exit_time", null)?.takeIf { it.isNotEmpty() },
            pnl = nd("pnl"), quality = nd("quality"),
            regime = j.optString("regime", null)?.takeIf { it.isNotEmpty() },
            openedAt = j.optString("opened_at", null)?.takeIf { it.isNotEmpty() },
            status = j.optString("status", null)?.takeIf { it.isNotEmpty() }
        )
    }

    private fun decodeSignal(j: JSONObject): RegimeSignal =
        RegimeSignal(
            id = j.optLong("id", 0),
            ts = j.optString("ts", null)?.takeIf { it.isNotEmpty() },
            symbol = j.optString("symbol", null)?.takeIf { it.isNotEmpty() },
            regime = j.optString("regime", null)?.takeIf { it.isNotEmpty() },
            score = j.optDouble("score", 0.0),
            quality = j.optDouble("quality", 0.0),
            action = j.optString("action", null)?.takeIf { it.isNotEmpty() },
            price = j.optDouble("price", 0.0)
        )

    private fun decodeWinLoss(o: JSONObject?): WinLoss? {
        if (o == null) return null
        return WinLoss(
            wins = o.optInt("wins", 0),
            losses = o.optInt("losses", 0),
            flat = o.optInt("flat", 0)
        )
    }

    // ───── helpers ─────
    private fun get(url: String): String? {
        val req = Request.Builder().url(url).header("User-Agent", "TitaniumCompanion/2.0").build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return null
            return resp.body?.string()
        }
    }

    /** Turn a possibly-null/JSON-null numeric into Double?. */
    private fun JSONObject.optNullableDouble(key: String): Double? {
        if (!has(key) || isNull(key)) return null
        val d = optDouble(key, Double.NaN)
        return if (d.isNaN()) null else d
    }

    private fun <T> decodeList(arr: JSONArray?, f: (JSONObject) -> T): List<T> {
        if (arr == null) return emptyList()
        val out = ArrayList<T>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            try { out.add(f(o)) } catch (_: Exception) {}
        }
        return out
    }

    /** Thinning sample so the UI can chart a huge series cheaply. */
    private fun downsample(points: List<EquityPoint>, max: Int): List<EquityPoint> {
        if (points.size <= max) return points
        val stride = points.size.toDouble() / max
        val out = ArrayList<EquityPoint>(max)
        for (k in 0 until max) {
            val idx = (k * stride).toInt().coerceAtMost(points.size - 1)
            out.add(points[idx])
        }
        return out
    }
}
