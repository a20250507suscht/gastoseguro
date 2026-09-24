package com.gastoseguro.miapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AccountsScreen(vm: AppViewModel, modifier: Modifier = Modifier) {
    var showAdd by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Account?>(null) }

    val regular = vm.accounts.filter { it.type != AccountType.CREDIT }
    val credit = vm.accounts.filter { it.type == AccountType.CREDIT }

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            // Espacio abajo para que el botón flotante no tape la última fila
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AppCard(containerColor = VerdeClaro) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Disponible", style = MaterialTheme.typography.labelMedium)
                            Text(
                                money(regular.sumOf { it.amount }),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Deuda en tarjetas", style = MaterialTheme.typography.labelMedium)
                            Text(
                                money(credit.sumOf { it.amount }),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Rojo
                            )
                        }
                    }
                }
            }
            item { AccountGroup("Cuentas", regular, "Aún no tienes cuentas. Toca «Agregar cuenta».") { toDelete = it } }
            item {
                AccountGroup("Tarjetas de crédito", credit, "Aún no tienes tarjetas de crédito.") { toDelete = it }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Verde,
            contentColor = Color.White
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Agregar cuenta")
        }
    }

    if (showAdd) {
        AddAccountDialog(
            onDismiss = { showAdd = false },
            onConfirm = { name, type, amount ->
                vm.addAccount(name, type, amount)
                showAdd = false
            }
        )
    }

    toDelete?.let { account ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("¿Eliminar ${account.name}?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.removeAccount(account)
                    toDelete = null
                }) { Text("Eliminar", color = Rojo) }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun AccountGroup(
    title: String,
    accounts: List<Account>,
    emptyMessage: String,
    onDelete: (Account) -> Unit
) {
    AppCard {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        if (accounts.isEmpty()) {
            Text(
                emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            accounts.forEachIndexed { i, account ->
                if (i > 0) HorizontalDivider()
                AccountRow(account, onDelete = { onDelete(account) })
            }
        }
    }
}

@Composable
private fun AddAccountDialog(onDismiss: () -> Unit, onConfirm: (String, AccountType, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AccountType.BANK) }
    var amountText by remember { mutableStateOf("") }

    val amount = if (amountText.isBlank()) 0.0 else amountText.replace(",", ".").toDoubleOrNull()
    val valid = name.isNotBlank() && amount != null && amount >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar cuenta") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 30) name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text("Tipo", style = MaterialTheme.typography.labelLarge)
                AccountType.entries.forEach { t ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .selectable(selected = type == t, onClick = { type = t }, role = Role.RadioButton),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = type == t, onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text("${t.emoji} ${t.label}")
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.length <= 12) amountText = it },
                    label = { Text(if (type == AccountType.CREDIT) "Deuda actual" else "Saldo actual") },
                    prefix = { Text("$") },
                    singleLine = true,
                    isError = amount == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim(), type, amount ?: 0.0) }, enabled = valid) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
