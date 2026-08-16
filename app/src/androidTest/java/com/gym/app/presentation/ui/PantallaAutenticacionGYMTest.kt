/**
 * @file PantallaAutenticacionGYMTest.kt
 * @brief Pruebas de interfaz (Compose UI Tests) de la pantalla de autenticación.
 * Verifica que los campos de correo y contraseña se renderizan correctamente
 * y que el modo de registro muestra el campo de nombre.
 */
package com.gym.app.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.gym.app.di.ContenedorDependencias
import com.gym.app.presentation.viewmodel.AutenticacionViewModel
import com.gym.app.presentation.viewmodel.ModoAutenticacion
import org.junit.Rule
import org.junit.Test

/**
 * @class PantallaAutenticacionGYMTest
 * @brief Prueba de interfaz básica de la pantalla de autenticación.
 *
 * Nota: se prueban los componentes visuales con estado simulado para no
 * depender de Supabase. Requiere que `autenticacionViewModel` se construya
 * con un contenedor o con casos de uso simulados.
 */
class PantallaAutenticacionGYMTest {

    @get:Rule
    val reglaCompose = createComposeRule()

    @Test
    fun `la pantalla muestra los campos de correo y contrasena`() {
        // El contenedor real usa los Fakes cuando no hay credenciales Supabase,
        // por lo que la pantalla se puede componer sin backend.
        val contexto = androidx.test.platform.app.InstrumentationRegistry
            .getInstrumentation().targetContext
        val contenedor = ContenedorDependencias(contexto)
        val viewModel = AutenticacionViewModel(contenedor)

        reglaCompose.setContent {
            PantallaAutenticacionGYM(
                contenedor = contenedor,
                viewModelInyectado = viewModel
            )
        }

        reglaCompose.onNodeWithTag("campoEmail").assertIsDisplayed()
        reglaCompose.onNodeWithTag("campoPassword").assertIsDisplayed()
    }

    @Test
    fun `el modo registro muestra el campo de nombre`() {
        val contexto = androidx.test.platform.app.InstrumentationRegistry
            .getInstrumentation().targetContext
        val contenedor = ContenedorDependencias(contexto)
        val viewModel = AutenticacionViewModel(contenedor)
        viewModel.cambiarModo(ModoAutenticacion.REGISTRO)

        reglaCompose.setContent {
            PantallaAutenticacionGYM(
                contenedor = contenedor,
                viewModelInyectado = viewModel
            )
        }

        reglaCompose.onNodeWithTag("campoNombre").assertIsDisplayed()
    }

    @Test
    fun `escribir en el campo de correo actualiza el estado`() {
        val contexto = androidx.test.platform.app.InstrumentationRegistry
            .getInstrumentation().targetContext
        val contenedor = ContenedorDependencias(contexto)
        val viewModel = AutenticacionViewModel(contenedor)

        reglaCompose.setContent {
            PantallaAutenticacionGYM(
                contenedor = contenedor,
                viewModelInyectado = viewModel
            )
        }

        reglaCompose.onNodeWithTag("campoEmail").performTextInput("usuario@correo.com")

        assert(viewModel.estado.value.email == "usuario@correo.com")
    }
}