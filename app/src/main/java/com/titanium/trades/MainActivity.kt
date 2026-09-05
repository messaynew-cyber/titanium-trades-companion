package com.titanium.trades

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.titanium.trades.data.PriceApi
import com.titanium.trades.data.TradeRepository
import com.titanium.trades.enginevm.CockpitViewModel
import com.titanium.trades.ui.cockpit.CockpitScaffold
import com.titanium.trades.ui.theme.IsDark
import com.titanium.trades.ui.theme.TitaniumTheme
import com.titanium.trades.ui.theme.bgDeep

/** New-instance factory for the engine cockpit viewModel (defaults otherwise not creatable). */
object CockpitVmFactory : androidx.lifecycle.ViewModelProvider.NewInstanceFactory() {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CockpitViewModel() as T
    }
}

class MainViewModelFactory(private val repo: TradeRepository) :
    androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(repo, PriceApi()) as T
    }
}

class MainActivity : ComponentActivity() {

    private val repo by lazy { TradeRepository(applicationContext) }
    private val vmFactory by lazy { MainViewModelFactory(repo) }
    private val viewModel: MainViewModel by viewModels { vmFactory }
    private val cockpitViewModel: CockpitViewModel by viewModels { CockpitVmFactory }

    // Request notification permission on Android 13+ so background price alerts show.
    private val notifPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* granted or not */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        maybeRequestNotificationPermission()
        setContent {
            // IsDark (ui.theme) is the single source of truth for light/dark.
            // Reading it here exposes the current mode; the Scaffold's sun/moon
            // toggle flips IsDark and recomposition re-skins every screen via the
            // computed palette vals in Color.kt (no per-screen edits required).
            val dark = IsDark
            TitaniumTheme(darkTheme = dark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = bgDeep
                ) {
                    TitaniumEntry(
                        manualVm = viewModel,
                        cockpitVm = cockpitViewModel,
                        darkTheme = dark,
                        onToggleTheme = { IsDark = !IsDark }
                    )
                }
            }
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun TitaniumEntry(
    manualVm: MainViewModel,
    cockpitVm: CockpitViewModel,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    CockpitScaffold(
        manualVm = manualVm,
        cockpitVm = cockpitVm,
        darkTheme = darkTheme,
        onToggleTheme = onToggleTheme
    )
}
