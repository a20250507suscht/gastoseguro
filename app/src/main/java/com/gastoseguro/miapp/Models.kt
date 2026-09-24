package com.gastoseguro.miapp

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import java.util.Locale
import kotlin.math.abs

enum class AccountType(val label: String, val emoji: String) {
    CASH("Efectivo", "💵"),
    BANK("Cuenta bancaria", "🏦"),
    INVESTMENT("Inversión", "📈"),
    CREDIT("Tarjeta de crédito", "💳")
}

enum class PayMethod { CASH, CARD }

/** En una tarjeta de crédito, [amount] es la deuda; en las demás, el saldo. */
data class Account(val id: Int, val name: String, val type: AccountType, val amount: Double)

/** [amount] siempre es positivo y representa lo gastado. */
data class Movement(
    val id: Int,
    val title: String,
    val category: String,
    val amount: Double,
    val method: PayMethod,
    val whenText: String
)

val categoryColors = mapOf(
    "Alimentación" to Azul,
    "Transporte" to Color(0xFF27AE60),
    "Hogar" to Color(0xFFD4A017),
    "Servicios" to Color(0xFFE67E22),
    "Otros" to Color(0xFF8E44AD)
)

fun money(value: Double): String {
    val number = String.format(Locale.forLanguageTag("es-MX"), "%,.2f", abs(value))
    return (if (value < 0) "-" else "") + "$" + number
}

/** Datos de prueba en memoria. Más adelante se reemplazan por Room. */
class AppViewModel : ViewModel() {
    val accounts = mutableStateListOf(
        Account(1, "Efectivo", AccountType.CASH, 5282.0),
        Account(2, "Banco BBVA", AccountType.BANK, 3450.0),
        Account(3, "Inversiones", AccountType.INVESTMENT, 12517.0),
        Account(4, "BBVA Oro", AccountType.CREDIT, 2129.0),
        Account(5, "Santander Zero", AccountType.CREDIT, 1850.0)
    )

    val movements = mutableStateListOf(
        Movement(1, "Súper Aurrerá", "Alimentación", 850.0, PayMethod.CARD, "Hoy, 10:30"),
        Movement(2, "Uber", "Transporte", 120.0, PayMethod.CARD, "Hoy, 09:15"),
        Movement(3, "Netflix", "Servicios", 199.0, PayMethod.CARD, "Ayer"),
        Movement(4, "CFE", "Servicios", 650.0, PayMethod.CARD, "Ayer"),
        Movement(5, "Comida", "Alimentación", 100.0, PayMethod.CASH, "Ayer"),
        Movement(6, "Transporte", "Transporte", 50.0, PayMethod.CASH, "Ayer")
    )

    private var nextId = 100

    fun addAccount(name: String, type: AccountType, amount: Double) {
        accounts.add(Account(nextId++, name, type, amount))
    }

    fun removeAccount(account: Account) {
        accounts.remove(account)
    }

    /** Registra el gasto aproximado en efectivo del día y lo descuenta de la cuenta de efectivo. */
    fun addCashEstimate(amount: Double) {
        movements.add(0, Movement(nextId++, "Efectivo del día (aprox.)", "Otros", amount, PayMethod.CASH, "Hoy"))
        val i = accounts.indexOfFirst { it.type == AccountType.CASH }
        if (i >= 0) accounts[i] = accounts[i].copy(amount = (accounts[i].amount - amount).coerceAtLeast(0.0))
    }
}
