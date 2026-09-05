package com.titanium.trades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titanium.trades.data.PriceApi
import com.titanium.trades.data.TradeRepository
import com.titanium.trades.data.model.PriceSnapshot
import com.titanium.trades.data.model.TradeConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UI state holder. Combines the trade config with the latest price fetch loop
 * into a single [UiState] the screens collect with collectAsStateWithLifecycle.
 */
class MainViewModel(
    private val repo: TradeRepository,
    private val priceApi: PriceApi
) : ViewModel() {

    data class UiState(
        val config: TradeConfig = TradeConfig(),
        val price: PriceSnapshot? = null,
        val loading: Boolean = true,
        val error: String? = null,
        val lastUpdated: Long? = null,
        val history: List<Double> = emptyList()
    )

    private val _manualPrice = MutableStateFlow<PriceSnapshot?>(null)
    private val _history = MutableStateFlow<List<Double>>(emptyList())

    val uiState: StateFlow<UiState> = combine(
        repo.tradeConfig,
        _manualPrice,
        _history
    ) { cfg, manual, history ->
        UiState(
            config = cfg,
            price = manual,
            loading = manual == null,
            lastUpdated = manual?.timestamp,
            history = history
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState(loading = true)
    )

    init {
        refreshLoop()
    }

    /** Continuously refresh price every N seconds. */
    private fun refreshLoop() {
        viewModelScope.launch {
            while (true) {
                val fetched = refreshPrice()
                if (fetched != null) _manualPrice.value = fetched
                delay(REFRESH_INTERVAL_MS)
            }
        }
    }

    /** Fetch a fresh price snapshot, cache it, and append to the history buffer. */
    suspend fun refreshPrice(): PriceSnapshot? {
        return withContext(Dispatchers.IO) { priceApi.fetchSolPrice() }
            ?.also { snapshot ->
                repo.cachePrice(snapshot.usd)
                _history.value = (_history.value + snapshot.usd)
                    .takeLast(MAX_HISTORY_POINTS)   // keep a rolling window
            }
    }

    fun setStopLoss(v: Double?) = repo.setStopLoss(v)
    fun setTakeProfit(v: Double?) = repo.setTakeProfit(v)
    fun setEntryPrice(v: Double?) = repo.setEntryPrice(v)
    fun setQuantity(v: Double?) = repo.setQuantity(v)
    fun setAlertEnabled(b: Boolean) = repo.setAlertsEnabled(b)

    companion object {
        private const val REFRESH_INTERVAL_MS = 30_000L
        private const val MAX_HISTORY_POINTS = 30
    }
}
