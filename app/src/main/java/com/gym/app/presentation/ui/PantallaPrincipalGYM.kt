/**
 * @file PantallaPrincipalGYM.kt
 * @brief Pantalla principal de la aplicación GYM desarrollada en Jetpack Compose.
 * Gestiona la visualización del panel de control de entrenamientos, nutrición y progreso.
 */
package com.gym.app.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * @brief Composable que representa la pantalla principal del panel de control del gimnasio.
 * Muestra el acceso a rutinas basadas en planes de nutricionistas, selección de maquinaria y control de comidas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipalGYM() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GYM - Control de Entrenamiento y Nutrición") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bienvenido a GYM Pro",
                style = MaterialTheme.colorScheme.headlineMedium
            )
            Text(
                text = "Controla tus entrenamientos, rutinas de nutricionista y progreso con máxima precisión.",
                style = MaterialTheme.colorScheme.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
