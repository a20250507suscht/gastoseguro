package com.gastoseguro.miapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Verde = Color(0xFF1B7F4B)
val VerdeClaro = Color(0xFFE3F4EA)
val Rojo = Color(0xFFC62828)
val Azul = Color(0xFF2E86DE)
val FondoApp = Color(0xFFF4F6F4)

private val Esquema = lightColorScheme(
    primary = Verde,
    onPrimary = Color.White,
    primaryContainer = VerdeClaro,
    onPrimaryContainer = Color(0xFF0B3D24),
    background = FondoApp,
    surface = Color.White,
    onSurface = Color(0xFF1C1F1D),
    onSurfaceVariant = Color(0xFF5B615D),
    error = Rojo
)

@Composable
fun GastoSeguroTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Esquema, content = content)
}
