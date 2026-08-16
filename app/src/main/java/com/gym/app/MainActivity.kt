/**
 * @file MainActivity.kt
 * @brief Actividad principal de la aplicación Android GYM.
 * Configura el contenedor de dependencias de la aplicación, el tema oscuro
 * azulado de Material 3 y lanza la navegación condicional por sesión entre
 * las pantallas de autenticación y las pantallas principales de la aplicación.
 */
package com.gym.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.gym.app.di.ContenedorDependencias
import com.gym.app.presentation.ui.NavegacionGYM
import com.gym.app.presentation.ui.theme.TemaGYM
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

/**
 * @class MainActivity
 * @brief Actividad de entrada que inicializa el contenedor de dependencias,
 * aplica el tema de Material Design 3 y carga la navegación principal.
 */
class MainActivity : ComponentActivity() {

    /**
     * Contenedor de dependencias de la aplicación, creado de forma perezosa con
     * el contexto de aplicación para resolver repositorios y casos de uso.
     */
    private val contenedorDependencias: ContenedorDependencias by lazy {
        ContenedorDependencias(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PDFBoxResourceLoader.init(applicationContext)
        setContent {
            TemaGYM {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavegacionGYM(contenedor = contenedorDependencias)
                }
            }
        }
    }
}