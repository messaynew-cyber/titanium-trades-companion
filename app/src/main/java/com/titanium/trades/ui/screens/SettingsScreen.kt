package com.titanium.trades.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.titanium.trades.MainViewModel
import com.titanium.trades.ui.theme.Gold
import com.titanium.trades.ui.theme.GoldHorizontal
import com.titanium.trades.ui.theme.OledBlack
import com.titanium.trades.ui.theme.TextDim
import com.titanium.trades.ui.theme.TextHigh
import com.titanium.trades.ui.theme.TextMid
import com.titanium.trades.ui.theme.outline1
import com.titanium.trades.ui.theme.surface1
import com.titanium.trades.ui.theme.surface2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val cfg = ui.config

    var entryText by remember(cfg.entryPrice) { mutableStateOf(cfg.entryPrice?.toString() ?: "") }
    var slText by remember(cfg.stopLoss) { mutableStateOf(cfg.stopLoss?.toString() ?: "") }
    var tpText by remember(cfg.takeProfit) { mutableStateOf(cfg.takeProfit?.toString() ?: "") }
    var qtyText by remember(cfg.quantity) { mutableStateOf(cfg.quantity?.toString() ?: "") }
    var alerts by remember(cfg.alertsEnabled) { mutableStateOf(cfg.alertsEnabled) }

    Scaffold(
        containerColor = OledBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Position", color = TextHigh, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium)
                        Text("Configure your active trade", color = TextDim,
                            style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextHigh)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OledBlack)
            )
        }
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section header: levels
            SectionLabel("PRICE LEVELS", "Set the entry and the protection levels")
            MoneyField("Entry price (USD)", entryText, { entryText = it })
            MoneyField("Stop-loss (USD)", slText, { slText = it })
            MoneyField("Take-profit (USD)", tpText, { tpText = it })
            MoneyField("Quantity (SOL) · optional", qtyText, { qtyText = it })

            // Alerts toggle card
            Card(shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = surface1),
                border = BorderStroke(1.dp, outline1)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Push Alerts", style = MaterialTheme.typography.titleSmall,
                            color = TextHigh, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(3.dp))
                        Text("Notify on SL/TP even when the app is closed.",
                            color = TextDim, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = alerts,
                        onCheckedChange = { alerts = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OledBlack,
                            checkedTrackColor = Gold,
                            checkedBorderColor = Gold
                        )
                    )
                }
            }

            Spacer(Modifier.height(2.dp))

            // Buttons
            Button(
                onClick = {
                    viewModel.setEntryPrice(entryText.toDoubleOrNull())
                    viewModel.setStopLoss(slText.toDoubleOrNull())
                    viewModel.setTakeProfit(tpText.toDoubleOrNull())
                    viewModel.setQuantity(qtyText.toDoubleOrNull())
                    viewModel.setAlertEnabled(alerts)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = OledBlack)
            ) {
                Text("SAVE POSITION", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            TextButton(onClick = {
                entryText = ""; slText = ""; tpText = ""; qtyText = ""
                viewModel.setEntryPrice(null); viewModel.setStopLoss(null)
                viewModel.setTakeProfit(null); viewModel.setQuantity(null)
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Clear all levels", color = TextDim)
            }

            // Current summary
            Text("CURRENT CONFIG", color = TextDim, style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 4.dp))
            Card(shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = surface2)) {
                Column(Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryRow("Entry", cfg.entryPrice?.let { "$" + it } ?: "Not set")
                    SummaryRow("Stop-loss", cfg.stopLoss?.let { "$" + it } ?: "Not set")
                    SummaryRow("Take-profit", cfg.takeProfit?.let { "$" + it } ?: "Not set")
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String, subtitle: String) {
    Column {
        Text(title, color = Gold, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, color = TextDim, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, color = TextMid, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = TextHigh, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MoneyField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { new -> if (new.isEmpty() || new.matches(Regex("^\\d*\\.?\\d*$"))) onValueChange(new) },
        label = { Text(label, color = TextMid) },
        prefix = { Text("$", color = Gold, style = TextStyle(fontWeight = FontWeight.Bold)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(14.dp),
        textStyle = MaterialTheme.typography.titleMedium.copy(color = TextHigh, fontFamily = FontFamily.Monospace),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Gold,
            unfocusedBorderColor = outline1,
            focusedLabelColor = Gold,
            cursorColor = Gold,
            focusedContainerColor = surface1,
            unfocusedContainerColor = surface1,
            focusedPrefixColor = Gold,
            unfocusedPrefixColor = Gold
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
