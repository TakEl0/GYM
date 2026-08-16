/**
 * @file PantallaCargandoGYM.kt
 * @brief Pantalla de carga (splash) de la aplicación GYM en Jetpack Compose.
 * Se muestra mientras el estado de sesión es `EstadoSesion.CARGANDO` (por
 * ejemplo, durante la restauración de una sesión persistida), ofreciendo un
 * indicador de progreso circular centrado y coherente con el tema azulado.
 */
package com.gym.app.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gym.app.presentation.ui.theme.AzulPrimario

/**
 * @brief Pantalla de carga simple de la aplicación.
 * Muestra un indicador de progreso circular centrado junto con el texto
 * "Cargando…" para informar al usuario de que la sesión se está restaurando.
 */
@Composable
fun PantallaCargandoGYM() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(56.dp),
                color = AzulPrimario,
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cargando…",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}