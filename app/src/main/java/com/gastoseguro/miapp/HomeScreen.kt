package com.gastoseguro.miapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun HomeScreen(vm: AppViewModel, modifier: Modifier = Modifier, onSeeAccounts: () -> Unit) {
    var showCashDialog by remember { mutableStateOf(false) }

    val available = vm.accounts.filter { it.type != AccountType.CREDIT }.sumOf { it.amount }
    val debt = vm.accounts.filter { it.type == AccountType.CREDIT }.sumOf { it.amount }
    val cash = vm.movements.filter { it.method == PayMethod.CASH }.sumOf { it.amount }
    val card = vm.movements.filter { it.method == PayMethod.CARD }.sumOf { it.amount }
    val byCategory = vm.movements
        .groupBy { it.category }
        .map { (name, list) -> name to list.sumOf { it.amount } }
        .sortedByDescending { it.second }

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { BalanceCard(net = available - debt, available = available, debt = debt) }
        item { CashReminderCard(onClick = { showCashDialog = true }) }
        item { CashVsCardCard(cash, card) }
        item { CategoriesCard(byCategory, cash + card) }
        item {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Cuentas principales",
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onSeeAccounts) { Text("Ver todas") }
                }
                vm.accounts.take(3).forEachIndexed { i, account ->
                    if (i > 0) HorizontalDivider()
                    AccountRow(account)
                }
            }
        }
        item {
            AppCard {
                Text(
                    "Últimos movimientos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                vm.movements.take(4).forEachIndexed { i, m ->
                    if (i > 0) HorizontalDivider()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(m.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text(
                                "${m.category} · ${m.whenText}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            money(-m.amount),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Rojo
                        )
                    }
                }
            }
        }
    }

    if (showCashDialog) {
        CashDialog(
            onDismiss = { showCashDialog = false },
            onConfirm = {
                vm.addCashEstimate(it)
                showCashDialog = false
            }
        )
    }
}

@Composable
private fun BalanceCard(net: Double, available: Double, debt: Double) {
    AppCard(containerColor = Verde) {
        Text("Balance total", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
        Text(
            money(net),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Disponible", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
                Text(money(available), style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Deuda en tarjetas",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Text(money(debt), style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }
    }
}

@Composable
private fun CashReminderCard(onClick: () -> Unit) {
    AppCard(containerColor = VerdeClaro) {
        Text(
            "¿Cuánto gastaste en efectivo hoy?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            "Una cantidad aproximada es suficiente.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp)) { Text("Registrar efectivo") }
    }
}

@Composable
private fun CashVsCardCard(cash: Double, card: Double) {
    val total = cash + card
    val cashPct = if (total > 0) (cash / total * 100).roundToInt() else 0
    AppCard {
        Text("Gastos del mes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(money(total), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        if (total > 0) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
            ) {
                Box(
                    Modifier
                        .weight(cash.toFloat().coerceAtLeast(0.001f))
                        .fillMaxHeight()
                        .background(Verde)
                )
                Box(
                    Modifier
                        .weight(card.toFloat().coerceAtLeast(0.001f))
                        .fillMaxHeight()
                        .background(Azul)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
        LegendRow(Verde, "💵 Efectivo", money(cash), "$cashPct%")
        LegendRow(Azul, "💳 Tarjeta", money(card), "${if (total > 0) 100 - cashPct else 0}%")
    }
}

@Composable
private fun CategoriesCard(byCategory: List<Pair<String, Double>>, total: Double) {
    AppCard {
        Text("Gastos por categoría", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (byCategory.isEmpty()) {
            Text("Todavía no hay gastos este mes.", style = MaterialTheme.typography.bodyMedium)
        } else {
            DonutChart(
                slices = byCategory.map { (name, value) -> value to (categoryColors[name] ?: Color.Gray) },
                centerLabel = "Total",
                centerValue = money(total),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(8.dp))
            byCategory.forEach { (name, value) ->
                LegendRow(
                    color = categoryColors[name] ?: Color.Gray,
                    label = name,
                    value = money(value),
                    extra = "${(value / total * 100).roundToInt()}%"
                )
            }
        }
    }
}

@Composable
private fun CashDialog(onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var text by remember { mutableStateOf("") }
    val value = text.replace(",", ".").toDoubleOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Efectivo de hoy") },
        text = {
            Column {
                Text("No necesitas recordar cada compra, escribe un aproximado.")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= 10) text = it },
                    label = { Text("Monto aproximado") },
                    prefix = { Text("$") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (value != null) onConfirm(value) },
                enabled = value != null && value > 0
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
