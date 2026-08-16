/**
 * @file SesionesViewModelTest.kt
 * @brief Pruebas unitarias del ViewModel de la pantalla de Sesiones.
 */
package com.gym.app.presentation.viewmodel

import com.gym.app.data.repository.RepositorioSesionEntrenamientoFake
import com.gym.app.domain.model.SesionEntrenamiento
import com.gym.app.test.MainDispatcherRule
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * @class SesionesViewModelTest
 * @brief Verifica el listado semanal de sesiones, el contador de sesiones
 * completadas y el registro de una sesión manual con el repositorio Fake.
 */
class SesionesViewModelTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    /**
     * @brief Construye una sesión de entrenamiento con fecha en la semana actual.
     */
    private fun crearSesion(
        completo: Boolean = true,
        duracionMinutos: Int = 60,
        nombreRutina: String = "PPL - Pecho"
    ): SesionEntrenamiento {
        val fechaEpoch = LocalDate.now()
            .atTime(LocalTime.NOON)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return SesionEntrenamiento(
            id = UUID.randomUUID().toString(),
            userId = "usuario-1",
            fecha = fechaEpoch,
            nombreRutina = nombreRutina,
            serieRealizadas = 12,
            duracionMinutos = duracionMinutos,
            completo = completo
        )
    }

    @Test
    fun `estado inicial termina la carga sin sesiones`() {
        val viewModel = SesionesViewModel(RepositorioSesionEntrenamientoFake())

        assertEquals(false, viewModel.estado.value.cargando)
        assertTrue(viewModel.estado.value.sesiones.isEmpty())
        assertEquals(0, viewModel.estado.value.sesionesCompletadas)
        assertNull(viewModel.estado.value.error)
    }

    @Test
    fun `carga las sesiones de la semana actual`() {
        val repositorio = RepositorioSesionEntrenamientoFake()
        val viewModel = SesionesViewModel(repositorio)

        runBlocking { repositorio.guardarSesion(crearSesion(completo = true)) }

        assertEquals(1, viewModel.estado.value.sesiones.size)
        assertEquals("PPL - Pecho", viewModel.estado.value.sesiones[0].nombreRutina)
        assertEquals(false, viewModel.estado.value.cargando)
    }

    @Test
    fun `cuenta unicamente las sesiones completadas de la semana`() {
        val repositorio = RepositorioSesionEntrenamientoFake()
        val viewModel = SesionesViewModel(repositorio)

        runBlocking {
            repositorio.guardarSesion(crearSesion(completo = true))
            repositorio.guardarSesion(crearSesion(completo = false))
            repositorio.guardarSesion(crearSesion(completo = true, nombreRutina = "PPL - Pierna"))
        }

        assertEquals(3, viewModel.estado.value.sesiones.size)
        assertEquals(2, viewModel.estado.value.sesionesCompletadas)
    }

    @Test
    fun `registrarSesion guarda una sesion valida y la refleja en el estado`() {
        // Dispatchers.Unconfined hace síncrono el withContext del caso de uso
        // interno de registro, garantizando que el estado refleje la sesión
        // guardada antes de las aserciones.
        val viewModel = SesionesViewModel(
            RepositorioSesionEntrenamientoFake(),
            dispatcher = Dispatchers.Unconfined
        )

        viewModel.registrarSesion(
            nombreRutina = "PPL - Espalda",
            serieRealizadas = 10,
            duracionMinutos = 75,
            completo = true
        )

        val estado = viewModel.estado.value
        assertEquals(false, estado.registrando)
        assertNull(estado.error)
        assertEquals(1, estado.sesiones.size)
        assertEquals("PPL - Espalda", estado.sesiones[0].nombreRutina)
        assertEquals(10, estado.sesiones[0].serieRealizadas)
        assertEquals(75, estado.sesiones[0].duracionMinutos)
        assertTrue(estado.sesiones[0].completo)
        assertEquals(1, estado.sesionesCompletadas)
    }

    @Test
    fun `registrarSesion con nombre vacio no bloquea el registro`() {
        val viewModel = SesionesViewModel(
            RepositorioSesionEntrenamientoFake(),
            dispatcher = Dispatchers.Unconfined
        )

        viewModel.registrarSesion(
            nombreRutina = "   ",
            serieRealizadas = 4,
            duracionMinutos = 30,
            completo = false
        )

        val estado = viewModel.estado.value
        assertNull(estado.error)
        // El nombre se guarda recortado (vacío), pero la sesión se registra.
        assertEquals(1, estado.sesiones.size)
        assertFalse(estado.sesiones[0].completo)
        assertEquals(0, estado.sesionesCompletadas)
    }
}