package com.titanium.trades.data

import com.titanium.trades.data.model.PriceSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Fetches live crypto prices. Uses free public APIs (no key required):
 * primary = CoinGecko (24h change included), fallback = Binance (no 24h but reliable).
 * Dispatchers.IO injected so it is testable and never blocks the main thread.
 */
class PriceApi(
    private val client: OkHttpClient = defaultClient()
) {

    companion object {
        // CoinGecko SOL -> USD, include 24h % change
        private const val COINGECKO = "https://api.coingecko.com/api/v3/simple/price?ids=solana&vs_currencies=usd&include_24hr_change=true"
        // Binance ticker fallback
        private const val BINANCE = "https://api.binance.com/api/v3/ticker/24hr?symbol=SOLUSDT"

        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    /** Returns the latest SOL price with 24h change, or null on network failure. */
    suspend fun fetchSolPrice(): PriceSnapshot? = withContext(Dispatchers.IO) {
        // Try CoinGecko first
        try {
            val req = Request.Builder().url(COINGECKO).header("Accept", "application/json").build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val body = resp.body?.string() ?: return@withContext null
                val root = JSONObject(body)
                val solana = root.optJSONObject("solana") ?: return@withContext null
                val usd = solana.optDouble("usd", -1.0)
                if (usd <= 0) return@withContext null
                val change = if (solana.has("usd_24h_change") && !solana.isNull("usd_24h_change"))
                    solana.optDouble("usd_24h_change") else null
                PriceSnapshot("SOL", usd, change, "CoinGecko", System.currentTimeMillis())
            }
        } catch (e: Exception) {
            // Fall back to Binance
            try {
                val req = Request.Builder().url(BINANCE).build()
                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext null
                    val body = resp.body?.string() ?: return@withContext null
                    val root = JSONObject(body)
                    val price = root.optDouble("lastPrice", -1.0)
                    if (price <= 0) return@withContext null
                    PriceSnapshot("SOL", price, null, "Binance", System.currentTimeMillis())
                }
            } catch (e2: Exception) {
                null
            }
        }
    }
}
