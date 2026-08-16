/**
 * @file AutenticacionViewModelTest.kt
 * @brief Pruebas unitarias del ViewModel de autenticación.
 */
package com.gym.app.presentation.viewmodel

import com.gym.app.domain.model.EstadoSesion
import com.gym.app.domain.usecase.autenticacion.CerrarSesionCasoUso
import com.gym.app.domain.usecase.autenticacion.IniciarSesionCasoUso
import com.gym.app.domain.usecase.autenticacion.ObservarEstadoSesionCasoUso
import com.gym.app.domain.usecase.autenticacion.RegistrarUsuarioCasoUso
import com.gym.app.test.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * @class AutenticacionViewModelTest
 * @brief Verifica los flujos de inicio de sesión, registro y limpieza de credenciales.
 */
class AutenticacionViewModelTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    private val iniciarSesionCasoUso = mockk<IniciarSesionCasoUso>()
    private val registrarUsuarioCasoUso = mockk<RegistrarUsuarioCasoUso>()
    private val cerrarSesionCasoUso = mockk<CerrarSesionCasoUso>()
    private val observarEstadoSesionCasoUso = mockk<ObservarEstadoSesionCasoUso>()

    private fun crearViewModel(estadoInicial: EstadoSesion = EstadoSesion.NO_AUTENTICADO): AutenticacionViewModel {
        every { observarEstadoSesionCasoUso.ejecutar() } returns MutableStateFlow(estadoInicial)
        return AutenticacionViewModel(
            iniciarSesionCasoUso = iniciarSesionCasoUso,
            registrarUsuarioCasoUso = registrarUsuarioCasoUso,
            cerrarSesionCasoUso = cerrarSesionCasoUso,
            observarEstadoSesionCasoUso = observarEstadoSesionCasoUso
        )
    }

    @Test
    fun `el estado inicial no esta autenticado y sin errores`() {
        val viewModel = crearViewModel()

        assertEquals(EstadoSesion.NO_AUTENTICADO, viewModel.estado.value.estadoSesion)
        assertNull(viewModel.estado.value.error)
        assertFalse(viewModel.estado.value.cargando)
    }

    @Test
    fun `iniciar sesion con exito marca exito y limpia la contrasena`() = runTest {
        coEvery { iniciarSesionCasoUso.ejecutar("usuario@correo.com", "contrasena123") } returns Result.success(Unit)

        val viewModel = crearViewModel()
        viewModel.actualizarEmail("usuario@correo.com")
        viewModel.actualizarPassword("contrasena123")
        viewModel.iniciarSesion()

        assertTrue(viewModel.estado.value.exitoLogin)
        assertEquals("", viewModel.estado.value.password)
        assertNull(viewModel.estado.value.error)
    }

    @Test
    fun `iniciar sesion con error muestra el mensaje de error`() = runTest {
        coEvery { iniciarSesionCasoUso.ejecutar(any(), any()) } returns Result.failure(
            IllegalStateException("Credenciales incorrectas")
        )

        val viewModel = crearViewModel()
        viewModel.actualizarEmail("usuario@correo.com")
        viewModel.actualizarPassword("contrasena123")
        viewModel.iniciarSesion()

        assertFalse(viewModel.estado.value.exitoLogin)
        assertEquals("Credenciales incorrectas", viewModel.estado.value.error)
        assertFalse(viewModel.estado.value.cargando)
    }

    @Test
    fun `registro con exito limpia la contrasena y marca exito`() = runTest {
        coEvery {
            registrarUsuarioCasoUso.ejecutar("usuario@correo.com", "contrasena123", "Ana García")
        } returns Result.success(Unit)

        val viewModel = crearViewModel()
        viewModel.actualizarNombre("Ana García")
        viewModel.actualizarEmail("usuario@correo.com")
        viewModel.actualizarPassword("contrasena123")
        viewModel.registrar()

        assertTrue(viewModel.estado.value.exitoLogin)
        assertEquals("", viewModel.estado.value.password)
    }

    @Test
    fun `cambiar de modo no modifica los campos del formulario`() {
        val viewModel = crearViewModel()
        viewModel.actualizarEmail("usuario@correo.com")

        viewModel.cambiarModo(ModoAutenticacion.REGISTRO)

        assertEquals(ModoAutenticacion.REGISTRO, viewModel.estado.value.modo)
        assertEquals("usuario@correo.com", viewModel.estado.value.email)
    }

    @Test
    fun `cerrar sesion delega en el caso de uso`() = runTest {
        coEvery { cerrarSesionCasoUso.ejecutar() } returns Unit

        val viewModel = crearViewModel()
        viewModel.cerrarSesion()

        coVerify(exactly = 1) { cerrarSesionCasoUso.ejecutar() }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `al pasar a no autenticado la contrasena se limpia del formulario`() = runTest {
        val flujoEstado = MutableStateFlow(EstadoSesion.AUTENTICADO)
        every { observarEstadoSesionCasoUso.ejecutar() } returns flujoEstado

        val viewModel = AutenticacionViewModel(
            iniciarSesionCasoUso = iniciarSesionCasoUso,
            registrarUsuarioCasoUso = registrarUsuarioCasoUso,
            cerrarSesionCasoUso = cerrarSesionCasoUso,
            observarEstadoSesionCasoUso = observarEstadoSesionCasoUso
        )
        viewModel.actualizarPassword("contrasena123")

        // Procesa el lanzamiento inicial del flujo observado.
        advanceUntilIdle()

        // Simula el cierre de sesión emitido por el flujo observado.
        flujoEstado.value = EstadoSesion.NO_AUTENTICADO
        advanceUntilIdle()

        assertEquals("", viewModel.estado.value.password)
    }
}