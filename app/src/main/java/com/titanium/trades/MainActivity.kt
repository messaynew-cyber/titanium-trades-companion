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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.titanium.trades.data.PriceApi
import com.titanium.trades.data.TradeRepository
import com.titanium.trades.ui.screens.DashboardScreen
import com.titanium.trades.ui.screens.SettingsScreen
import com.titanium.trades.ui.theme.OledBlack
import com.titanium.trades.ui.theme.TitaniumTheme

/** Convenience factory so the ViewModel only needs its dependencies, not the context. */
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

    // Request notification permission on Android 13+ so background price alerts show.
    private val notifPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* granted or not */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        maybeRequestNotificationPermission()
        setContent {
            TitaniumTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = OledBlack) {
                    TradesApp(viewModel)
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
private fun TradesApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onOpenSettings = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
