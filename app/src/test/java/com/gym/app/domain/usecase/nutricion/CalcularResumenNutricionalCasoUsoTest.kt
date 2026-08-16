/**
 * @file CalcularResumenNutricionalCasoUsoTest.kt
 * @brief Pruebas unitarias del cálculo del resumen nutricional diario.
 */
package com.gym.app.domain.usecase.nutricion

import com.gym.app.domain.model.Comida
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * @class CalcularResumenNutricionalCasoUsoTest
 * @brief Verifica la agregación de comidas y el cálculo de restantes y progreso.
 */
class CalcularResumenNutricionalCasoUsoTest {

    private val casoUso = CalcularResumenNutricionalCasoUso()

    private val desayuno = Comida(
        id = "1",
        nombre = "Desayuno",
        kcal = 400,
        proteinasG = 30.0,
        carbohidratosG = 45.0,
        grasasG = 10.0,
        tipoIngesta = "DESAYUNO",
        fecha = LocalDate.now()
    )

    private val comida = Comida(
        id = "2",
        nombre = "Comida",
        kcal = 600,
        proteinasG = 50.0,
        carbohidratosG = 70.0,
        grasasG = 15.0,
        tipoIngesta = "COMIDA",
        fecha = LocalDate.now()
    )

    @Test
    fun `calcula correctamente los totales consumidos`() {
        val resumen = casoUso.ejecutar(
            comidas = listOf(desayuno, comida),
            kcalObjetivo = 2500.0,
            proteinasObjetivoG = 150.0,
            carbohidratosObjetivoG = 250.0,
            grasasObjetivoG = 60.0
        )

        assertEquals(1000.0, resumen.kcalConsumidas, 0.001)
        assertEquals(80.0, resumen.proteinasConsumidasG, 0.001)
        assertEquals(115.0, resumen.carbohidratosConsumidosG, 0.001)
        assertEquals(25.0, resumen.grasasConsumidasG, 0.001)
    }

    @Test
    fun `calcula correctamente los restantes`() {
        val resumen = casoUso.ejecutar(
            comidas = listOf(desayuno, comida),
            kcalObjetivo = 2500.0,
            proteinasObjetivoG = 150.0,
            carbohidratosObjetivoG = 250.0,
            grasasObjetivoG = 60.0
        )

        assertEquals(1500.0, resumen.kcalRestantes, 0.001)
        assertEquals(70.0, resumen.proteinasObjetivoG - resumen.proteinasConsumidasG, 0.001)
    }

    @Test
    fun `con lista vacia de comidas el resumen es cero`() {
        val resumen = casoUso.ejecutar(
            comidas = emptyList(),
            kcalObjetivo = 2000.0,
            proteinasObjetivoG = 100.0,
            carbohidratosObjetivoG = 200.0,
            grasasObjetivoG = 50.0
        )

        assertEquals(0.0, resumen.kcalConsumidas, 0.001)
        assertEquals(2000.0, resumen.kcalRestantes, 0.001)
        assertEquals(0, resumen.progresoPorcentaje)
    }

    @Test
    fun `progreso se limita al cien por cien cuando se supera el objetivo`() {
        val resumen = casoUso.ejecutar(
            comidas = listOf(desayuno, comida),
            kcalObjetivo = 500.0,
            proteinasObjetivoG = 100.0,
            carbohidratosObjetivoG = 100.0,
            grasasObjetivoG = 100.0
        )

        assertEquals(100, resumen.progresoPorcentaje)
    }
}