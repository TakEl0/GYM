/**
 * @file MainActivity.kt
 * @brief Actividad principal de la aplicación Android GYM.
 * Configura el contenedor raíz con el tema oscuro azulado de Material 3 y
 * lanza la navegación entre las pantallas principales de la aplicación.
 */
package com.gym.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.gym.app.presentation.ui.NavegacionGYM
import com.gym.app.presentation.ui.theme.TemaGYM

/**
 * @class MainActivity
 * @brief Actividad de entrada que inicializa el tema de Material Design 3
 * y carga la navegación principal de la aplicación.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaGYM {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavegacionGYM()
                }
            }
        }
    }
}