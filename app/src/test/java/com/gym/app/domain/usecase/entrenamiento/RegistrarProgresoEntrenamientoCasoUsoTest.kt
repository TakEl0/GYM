/**
 * @file RegistrarProgresoEntrenamientoCasoUsoTest.kt
 * @brief Pruebas unitarias del caso de uso de actualización del progreso.
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.repository.RepositorioEntrenamiento
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class RegistrarProgresoEntrenamientoCasoUsoTest
 * @brief Verifica la validación del número de ejercicios realizados.
 */
class RegistrarProgresoEntrenamientoCasoUsoTest {

    private val repositorio = mockk<RepositorioEntrenamiento>(relaxed = true)

    private val casoUso = RegistrarProgresoEntrenamientoCasoUso(
        repositorioEntrenamiento = repositorio,
        dispatcher = Dispatchers.Unconfined
    )

    @Test
    fun `actualizar progreso valido tiene exito y delega en el repositorio`() = runTest {
        val resultado = casoUso.ejecutar("rutina-1", ejerciciosRealizados = 4)

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) { repositorio.actualizarProgreso("rutina-1", 4) }
    }

    @Test
    fun `actualizar progreso con ejercicios negativos devuelve error`() = runTest {
        val resultado = casoUso.ejecutar("rutina-1", ejerciciosRealizados = -1)

        assertTrue(resultado.isFailure)
        assertEquals(
            "El número de ejercicios realizados no puede ser negativo.",
            resultado.exceptionOrNull()?.message
        )
        coVerify(exactly = 0) { repositorio.actualizarProgreso(any(), any()) }
    }

    @Test
    fun `actualizar progreso con cero ejercicios es valido`() = runTest {
        val resultado = casoUso.ejecutar("rutina-1", ejerciciosRealizados = 0)

        assertTrue(resultado.isSuccess)
    }
}