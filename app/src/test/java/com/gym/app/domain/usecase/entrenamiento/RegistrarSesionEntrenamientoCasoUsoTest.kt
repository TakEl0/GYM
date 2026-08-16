/**
 * @file RegistrarSesionEntrenamientoCasoUsoTest.kt
 * @brief Pruebas unitarias del registro de sesiones de entrenamiento.
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.model.SesionEntrenamiento
import com.gym.app.domain.repository.RepositorioSesionEntrenamiento
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class RegistrarSesionEntrenamientoCasoUsoTest
 * @brief Verifica la validación de la fecha y la duración de la sesión y la
 * delegación en [RepositorioSesionEntrenamiento.guardarSesion].
 */
class RegistrarSesionEntrenamientoCasoUsoTest {

    private val repositorio = mockk<RepositorioSesionEntrenamiento>(relaxed = true)

    private val casoUso = RegistrarSesionEntrenamientoCasoUso(
        repositorioSesionEntrenamiento = repositorio,
        dispatcher = Dispatchers.Unconfined
    )

    /** Sesión válida de referencia (fecha epoch y duración positivas). */
    private fun crearSesion(
        fecha: Long = 1_752_048_000_000L,
        duracionMinutos: Int = 60
    ): SesionEntrenamiento = SesionEntrenamiento(
        id = "sesion-1",
        userId = "usuario-1",
        fecha = fecha,
        nombreRutina = "PPL - Pecho",
        serieRealizadas = 12,
        duracionMinutos = duracionMinutos,
        completo = true
    )

    @Test
    fun `sesion valida tiene exito y delega en el repositorio`() = runTest {
        val sesion = crearSesion()

        val resultado = casoUso.ejecutar(sesion)

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) { repositorio.guardarSesion(sesion) }
    }

    @Test
    fun `sesion con fecha no positiva devuelve error`() = runTest {
        val sesion = crearSesion(fecha = 0L)

        val resultado = casoUso.ejecutar(sesion)

        assertTrue(resultado.isFailure)
        assertEquals(
            "La fecha de la sesión debe ser mayor que 0.",
            resultado.exceptionOrNull()?.message
        )
        coVerify(exactly = 0) { repositorio.guardarSesion(any()) }
    }

    @Test
    fun `sesion con duracion negativa devuelve error`() = runTest {
        val sesion = crearSesion(duracionMinutos = -5)

        val resultado = casoUso.ejecutar(sesion)

        assertTrue(resultado.isFailure)
        assertEquals(
            "La duración de la sesión no puede ser negativa.",
            resultado.exceptionOrNull()?.message
        )
        coVerify(exactly = 0) { repositorio.guardarSesion(any()) }
    }

    @Test
    fun `si el repositorio falla se devuelve el error de persistencia`() = runTest {
        val sesion = crearSesion()
        coEvery { repositorio.guardarSesion(any()) } throws IllegalStateException("Error de red")

        val resultado = casoUso.ejecutar(sesion)

        assertTrue(resultado.isFailure)
        assertEquals("Error de red", resultado.exceptionOrNull()?.message)
    }
}