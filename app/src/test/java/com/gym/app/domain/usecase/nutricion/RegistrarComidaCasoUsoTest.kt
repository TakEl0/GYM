/**
 * @file RegistrarComidaCasoUsoTest.kt
 * @brief Pruebas unitarias del caso de uso de registro de comidas.
 */
package com.gym.app.domain.usecase.nutricion

import com.gym.app.domain.model.Comida
import com.gym.app.domain.repository.RepositorioComida
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * @class RegistrarComidaCasoUsoTest
 * @brief Verifica la validación de kilocalorías y macronutrientes antes de persistir.
 */
class RegistrarComidaCasoUsoTest {

    private val repositorio = mockk<RepositorioComida>(relaxed = true)

    private val casoUso = RegistrarComidaCasoUso(
        repositorioComida = repositorio,
        dispatcher = Dispatchers.Unconfined
    )

    private fun crearComida(
        kcal: Int = 500,
        proteinasG: Double = 30.0,
        carbohidratosG: Double = 50.0,
        grasasG: Double = 15.0
    ): Comida = Comida(
        id = "comida-test",
        nombre = "Comida de prueba",
        kcal = kcal,
        proteinasG = proteinasG,
        carbohidratosG = carbohidratosG,
        grasasG = grasasG,
        tipoIngesta = "COMIDA",
        fecha = LocalDate.now()
    )

    @Test
    fun `registro de comida valida tiene exito y delega en el repositorio`() = runTest {
        val comida = crearComida()

        val resultado = casoUso.ejecutar(comida)

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) { repositorio.guardarComida(comida) }
    }

    @Test
    fun `registro con kilocalorias negativas devuelve error`() = runTest {
        val comida = crearComida(kcal = -1)

        val resultado = casoUso.ejecutar(comida)

        assertTrue(resultado.isFailure)
        assertEquals(
            "Las kilocalorías no pueden ser negativas.",
            resultado.exceptionOrNull()?.message
        )
        coVerify(exactly = 0) { repositorio.guardarComida(any()) }
    }

    @Test
    fun `registro con proteinas negativas devuelve error`() = runTest {
        val comida = crearComida(proteinasG = -5.0)

        val resultado = casoUso.ejecutar(comida)

        assertTrue(resultado.isFailure)
        assertEquals(
            "Los macronutrientes no pueden ser negativos.",
            resultado.exceptionOrNull()?.message
        )
    }

    @Test
    fun `registro con carbohidratos negativos devuelve error`() = runTest {
        val comida = crearComida(carbohidratosG = -1.0)

        assertTrue(casoUso.ejecutar(comida).isFailure)
    }

    @Test
    fun `registro con grasas negativas devuelve error`() = runTest {
        val comida = crearComida(grasasG = -1.0)

        assertTrue(casoUso.ejecutar(comida).isFailure)
    }
}