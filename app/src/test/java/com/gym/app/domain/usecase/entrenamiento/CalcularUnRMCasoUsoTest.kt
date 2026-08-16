/**
 * @file CalcularUnRMCasoUsoTest.kt
 * @brief Pruebas unitarias del cálculo del máximo de una repetición (1RM).
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.model.CalculoUnRM
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class CalcularUnRMCasoUsoTest
 * @brief Verifica la validación de los datos de entrada y las estimaciones de
 * 1RM mediante las fórmulas de Epley y Brzycki (promediadas por el modelo).
 */
class CalcularUnRMCasoUsoTest {

    private val casoUso = CalcularUnRMCasoUso()

    @Test
    fun `estima el 1RM con la formula de Epley`() {
        // 1RM = 100 * (1 + 10/30) = 133,333...
        val estimacion = CalculoUnRM.epley(pesoKg = 100.0, repeticiones = 10)

        assertEquals(133.333, estimacion, 0.001)
    }

    @Test
    fun `estima el 1RM con la formula de Brzycki`() {
        // 1RM = 100 * 36 / (37 - 10) = 133,333...
        val estimacion = CalculoUnRM.brzycki(pesoKg = 100.0, repeticiones = 10)

        assertEquals(133.333, estimacion, 0.001)
    }

    @Test
    fun `el 1RM consolidado es el promedio de Epley y Brzycki`() = runTest {
        // Epley = 100 * (1 + 10/30) = 133,333 y Brzycki = 100*36/27 = 133,333.
        val calculo = casoUso.ejecutar(pesoKg = 100.0, repeticiones = 10).getOrThrow()

        assertEquals(133.333, CalculoUnRM.calcular(calculo.pesoKg, calculo.repeticiones), 0.001)
    }

    @Test
    fun `calcula un valor conocido de referencia`() = runTest {
        // 80 kg x 5 repeticiones: Epley = 93,333 y Brzycki = 90 -> promedio 91,667.
        val calculo = casoUso.ejecutar(pesoKg = 80.0, repeticiones = 5).getOrThrow()

        assertEquals(91.667, CalculoUnRM.calcular(calculo.pesoKg, calculo.repeticiones), 0.001)
    }

    @Test
    fun `brzycki delega en epley para repeticiones superiores a 36`() {
        // Brzycki no es fiable a partir de 36 repeticiones (denominador 37 - r <= 1).
        val estimacionBrzycki = CalculoUnRM.brzycki(pesoKg = 100.0, repeticiones = 40)
        val estimacionEpley = CalculoUnRM.epley(pesoKg = 100.0, repeticiones = 40)

        assertEquals(estimacionEpley, estimacionBrzycki, 0.001)
    }

    @Test
    fun `rechaza un peso no positivo`() = runTest {
        val resultado = casoUso.ejecutar(pesoKg = 0.0, repeticiones = 10)

        assertTrue(resultado.isFailure)
        assertEquals(
            "El peso debe ser mayor que 0 kg.",
            resultado.exceptionOrNull()?.message
        )
    }

    @Test
    fun `rechaza repeticiones fuera del rango de 1 a 35`() = runTest {
        val resultadoBajo = casoUso.ejecutar(pesoKg = 100.0, repeticiones = 0)
        val resultadoAlto = casoUso.ejecutar(pesoKg = 100.0, repeticiones = 36)

        assertTrue(resultadoBajo.isFailure)
        assertTrue(resultadoAlto.isFailure)
        assertEquals(
            "Las repeticiones deben estar entre 1 y 35.",
            resultadoAlto.exceptionOrNull()?.message
        )
    }
}