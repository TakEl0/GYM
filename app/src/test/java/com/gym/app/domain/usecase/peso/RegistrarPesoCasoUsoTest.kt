/**
 * @file RegistrarPesoCasoUsoTest.kt
 * @brief Pruebas unitarias del caso de uso de registro de peso corporal.
 */
package com.gym.app.domain.usecase.peso

import com.gym.app.domain.model.RegistroPeso
import com.gym.app.domain.repository.RepositorioPeso
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class RegistrarPesoCasoUsoTest
 * @brief Verifica la validación de rangos del peso y la persistencia en el repositorio.
 */
class RegistrarPesoCasoUsoTest {

    private val repositorio = mockk<RepositorioPeso>(relaxed = true)

    private val casoUso = RegistrarPesoCasoUso(
        repositorioPeso = repositorio,
        dispatcher = Dispatchers.Unconfined
    )

    @Test
    fun `registro con peso valido tiene exito y delega en el repositorio`() = runTest {
        val resultado = casoUso.ejecutar(pesoKg = 75.5, grasaCorporal = 18.0, userId = "usuario-1")

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) {
            repositorio.guardarRegistro(match<RegistroPeso> { it.pesoKg == 75.5 && it.grasaCorporalPorcentaje == 18.0 })
        }
    }

    @Test
    fun `registro con peso cero devuelve error de rango`() = runTest {
        val resultado = casoUso.ejecutar(pesoKg = 0.0, grasaCorporal = null, userId = "usuario-1")

        assertTrue(resultado.isFailure)
        assertEquals(
            "El peso debe ser mayor que 0 y menor o igual que 500 kg.",
            resultado.exceptionOrNull()?.message
        )
    }

    @Test
    fun `registro con peso negativo devuelve error de rango`() = runTest {
        val resultado = casoUso.ejecutar(pesoKg = -5.0, grasaCorporal = null, userId = "usuario-1")

        assertTrue(resultado.isFailure)
    }

    @Test
    fun `registro con peso superior a 500 devuelve error de rango`() = runTest {
        val resultado = casoUso.ejecutar(pesoKg = 501.0, grasaCorporal = null, userId = "usuario-1")

        assertTrue(resultado.isFailure)
    }

    @Test
    fun `registro con porcentaje de grasa invalido devuelve error`() = runTest {
        val resultado = casoUso.ejecutar(pesoKg = 75.5, grasaCorporal = 150.0, userId = "usuario-1")

        assertTrue(resultado.isFailure)
        assertEquals(
            "El porcentaje de grasa corporal debe estar entre 0 y 100.",
            resultado.exceptionOrNull()?.message
        )
    }

    @Test
    fun `registro con grasa nula es valido`() = runTest {
        val resultado = casoUso.ejecutar(pesoKg = 75.5, grasaCorporal = null, userId = "usuario-1")

        assertTrue(resultado.isSuccess)
    }
}