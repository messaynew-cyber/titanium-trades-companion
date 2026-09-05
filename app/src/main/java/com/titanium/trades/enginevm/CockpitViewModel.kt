package com.titanium.trades.enginevm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.titanium.trades.data.EngineApi
import com.titanium.trades.data.model.CockpitSnapshot
import com.titanium.trades.data.model.EngineStatus
import com.titanium.trades.data.model.OpenTrade
import com.titanium.trades.data.model.RegimeSignal
import com.titanium.trades.data.model.TradeRecord
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Pulls the live engine cockpit every [pollMs] while observed. Derives the
 * per-symbol watchlist (most recent signal per symbol) and keeps raw status +
 * downsampled equity curve for the UI.
 */
class CockpitViewModel(
    private val api: EngineApi = EngineApi(),
    private val pollMs: Long = 20_000L
) : ViewModel() {

    data class UiState(
        val loaded: Boolean = false,
        val loading: Boolean = true,
        val error: String? = null,
        val snapshot: CockpitSnapshot? = null,
        val fetchAgeMs: Long = 0L
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    /** Most recent signal per symbol, in stable order (BTC/ETH/SOL first). */
    fun watchlist(): List<RegimeSignal> {
        val snap = _state.value.snapshot ?: return emptyList()
        val status = snap.status ?: return emptyList()
        val history = status.statistics?.signalHistory ?: emptyList()
        // history is newest-first; keep the FIRST occurrence per symbol => the newest signal
        val bySymbol = LinkedHashMap<String, RegimeSignal>()
        for (s in history) {
            val sym = s.symbol ?: continue
            if (sym !in bySymbol) bySymbol[sym] = s
        }
        // render flagships first, then any other symbols the engine traded
        val order = listOf("BTCUSD", "ETHUSD", "SOLUSD")
        val sorted = bySymbol.values.sortedWith(Comparator { a, b ->
            val ia = order.indexOf(a.symbol).let { if (it < 0) order.size else it }
            val ib = order.indexOf(b.symbol).let { if (it < 0) order.size else it }
            ia.compareTo(ib)
        })
        return sorted
    }

    fun openTrade(): OpenTrade? = _state.value.snapshot?.status?.statistics?.openTrade

    fun recentTrades(): List<TradeRecord> =
        _state.value.snapshot?.status?.statistics?.tradeHistory ?: emptyList()

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val t0 = System.currentTimeMillis()
            val snap = api.fetchCockpit()
            if (snap != null) {
                _state.value = UiState(
                    loaded = true,
                    loading = false,
                    error = null,
                    snapshot = snap,
                    fetchAgeMs = System.currentTimeMillis() - t0
                )
            } else {
                _state.value = _state.value.copy(loaded = false, loading = false,
                    error = "Engine feed unreachable")
            }
        }
    }

    init { refresh(); loop() }

    private fun loop() {
        viewModelScope.launch {
            while (true) {
                delay(pollMs)
                // only auto-refresh if there is no active manual error spam; simply refresh
                api.fetchCockpit()?.let {
                    _state.value = UiState(loaded = true, loading = false, error = null,
                        snapshot = it, fetchAgeMs = 0)
                }
            }
        }
    }
}
