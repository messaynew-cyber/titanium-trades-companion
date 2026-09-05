package com.titanium.trades.ui.cockpit

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import com.titanium.trades.MainViewModel
import com.titanium.trades.enginevm.CockpitViewModel
import com.titanium.trades.ui.screens.SettingsScreen
import com.titanium.trades.ui.theme.Gold
import com.titanium.trades.ui.theme.OledBlack
import com.titanium.trades.ui.theme.TextHigh
import com.titanium.trades.ui.theme.TextMid
import com.titanium.trades.ui.theme.surface2

private enum class Tab(val label: String, val icon: ImageVector) {
    Cockpit("Cockpit", Icons.Filled.DashboardCustomize),
    Journal("Journal", Icons.Filled.SwapVert),
    Position("Position", Icons.Filled.Tune)
}

/** Root scaffold for the v2 app: a bottom-nav cockpit around shared engine + manual view models. */
@Composable
fun CockpitScaffold(
    manualVm: MainViewModel,
    cockpitVm: CockpitViewModel
) {
    var tab by rememberSaveable { mutableStateOf(Tab.Cockpit.name) }
    val selected = Tab.valueOf(tab)
    Scaffold(
        containerColor = OledBlack,
        bottomBar = {
            NavigationBar(containerColor = surface2.copy(alpha = 0.96f)) {
                Tab.entries.forEach { t ->
                    val active = t == selected
                    NavigationBarItem(
                        selected = active,
                        onClick = { tab = t.name },
                        icon = { Icon(t.icon, contentDescription = t.label,
                            tint = if (active) Gold else TextMid) },
                        label = { Text(t.label, color = if (active) TextHigh else TextMid,
                            fontSize = 11.sp, maxLines = 1) }
                    )
                }
            }
        }
    ) { pad ->
        androidx.compose.foundation.layout.Box(Modifier.padding(pad)) {
            when (selected) {
                Tab.Cockpit -> CockpitHomeScreen(viewModel = cockpitVm)
                Tab.Journal -> JournalScreen(vm = cockpitVm)
                Tab.Position -> SettingsScreen(viewModel = manualVm, onBack = {})
            }
        }
    }
}
