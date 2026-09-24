package com.gastoseguro.miapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {
    // El ViewModel conserva los datos al rotar la pantalla
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Barra de estado verde con iconos claros
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Verde.toArgb()))
        setContent {
            GastoSeguroTheme {
                GastoSeguroApp(vm)
            }
        }
    }
}

enum class Destination(val label: String, val icon: ImageVector) {
    HOME("Inicio", Icons.Filled.Home),
    MOVES("Movimientos", Icons.AutoMirrored.Filled.List),
    ACCOUNTS("Cuentas", Icons.Filled.AccountBox),
    REPORTS("Reportes", Icons.Filled.DateRange)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GastoSeguroApp(vm: AppViewModel) {
    var current by rememberSaveable { mutableStateOf(Destination.HOME) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (current == Destination.HOME) "Gasto Seguro" else current.label,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Verde,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                Destination.entries.forEach { d ->
                    NavigationBarItem(
                        selected = current == d,
                        onClick = { current = d },
                        icon = { Icon(d.icon, contentDescription = null) },
                        label = { Text(d.label) }
                    )
                }
            }
        }
    ) { padding ->
        val content = Modifier.padding(padding)
        when (current) {
            Destination.HOME -> HomeScreen(vm, content, onSeeAccounts = { current = Destination.ACCOUNTS })
            Destination.ACCOUNTS -> AccountsScreen(vm, content)
            else -> Box(content.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("${current.label}: próximamente", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
