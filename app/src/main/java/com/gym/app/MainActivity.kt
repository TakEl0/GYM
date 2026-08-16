/**
 * @file MainActivity.kt
 * @brief Actividad principal de la aplicación Android GYM.
 * Configura el contenedor raíz y lanza la pantalla principal basada en Jetpack Compose.
 */
package com.gym.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.gym.app.presentation.ui.PantallaPrincipalGYM

/**
 * @class MainActivity
 * @brief Actividad de entrada que inicializa el tema de Material Design 3 y carga la interfaz de usuario.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PantallaPrincipalGYM()
                }
            }
        }
    }
}
