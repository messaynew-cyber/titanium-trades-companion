package com.titanium.trades.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.titanium.trades.MainViewModel
import com.titanium.trades.ui.theme.Gold
import com.titanium.trades.ui.theme.OledBlack
import com.titanium.trades.ui.theme.SurfaceElevated
import com.titanium.trades.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cfg = uiState.config

    // Local editable state (strings for user typing)
    var entryText by remember(cfg.entryPrice) { mutableStateOf(cfg.entryPrice?.toString() ?: "") }
    var slText by remember(cfg.stopLoss) { mutableStateOf(cfg.stopLoss?.toString() ?: "") }
    var tpText by remember(cfg.takeProfit) { mutableStateOf(cfg.takeProfit?.toString() ?: "") }
    var qtyText by remember(cfg.quantity) { mutableStateOf(cfg.quantity?.toString() ?: "") }
    var alertsEnabled by remember(cfg.alertsEnabled) { mutableStateOf(cfg.alertsEnabled) }

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                title = { Text("Position Settings", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OledBlack,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Configure your active SOL trade. Alerts arm when both SL + TP are set.",
                color = TextSecondary, style = MaterialTheme.typography.bodySmall)

            MoneyField("Entry price (USD)", entryText, { entryText = it })
            MoneyField("Stop-loss (USD)", slText, { slText = it })
            MoneyField("Take-profit (USD)", tpText, { tpText = it })
            MoneyField("Quantity (SOL)  ·  optional", qtyText, { qtyText = it })

            // Alerts toggle
            Card(shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated)) {
                Row(Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Push Alerts", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(2.dp))
                        Text("Notify when SL/TP is hit even if app is closed.",
                            color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = alertsEnabled,
                        onCheckedChange = { alertsEnabled = it }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Save + defaults
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = {
                    entryText = ""; slText = ""; tpText = ""; qtyText = ""
                }, modifier = Modifier.weight(1f)) {
                    Text("Clear")
                }
                androidx.compose.material3.Button(
                    onClick = {
                        viewModel.setEntryPrice(entryText.toDoubleOrNull())
                        viewModel.setStopLoss(slText.toDoubleOrNull())
                        viewModel.setTakeProfit(tpText.toDoubleOrNull())
                        viewModel.setQuantity(qtyText.toDoubleOrNull())
                        viewModel.setAlertEnabled(alertsEnabled)
                        onBack()
                    },
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Position")
                }
            }

            Spacer(Modifier.height(8.dp))
            Card(shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Current levels", color = Gold,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Entry: ${cfg.entryPrice?.let{"$"+it} ?: "—"}",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium)
                    Text("Stop-loss: ${cfg.stopLoss?.let{"$"+it} ?: "—"}",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium)
                    Text("Take-profit: ${cfg.takeProfit?.let{"$"+it} ?: "—"}",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun MoneyField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { new ->
            // Allow numbers and one decimal point only
            if (new.isEmpty() || new.matches(Regex("^\\d*\\.?\\d*$"))) onValueChange(new)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(12.dp),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
