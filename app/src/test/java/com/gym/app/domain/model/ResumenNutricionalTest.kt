/**
 * @file ResumenNutricionalTest.kt
 * @brief Pruebas unitarias del modelo de dominio ResumenNutricional.
 */
package com.gym.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @class ResumenNutricionalTest
 * @brief Verifica el cálculo del progreso porcentual del resumen nutricional.
 */
class ResumenNutricionalTest {

    private fun crearResumen(
        kcalConsumidas: Double = 0.0,
        kcalObjetivo: Double = 2000.0
    ): ResumenNutricional = ResumenNutricional(
        kcalConsumidas = kcalConsumidas,
        kcalObjetivo = kcalObjetivo,
        kcalRestantes = kcalObjetivo - kcalConsumidas,
        proteinasConsumidasG = 0.0,
        proteinasObjetivoG = 100.0,
        carbohidratosConsumidosG = 0.0,
        carbohidratosObjetivoG = 200.0,
        grasasConsumidasG = 0.0,
        grasasObjetivoG = 60.0
    )

    @Test
    fun `progreso con cero consumido es cero`() {
        assertEquals(0, crearResumen(kcalConsumidas = 0.0, kcalObjetivo = 2000.0).progresoPorcentaje)
    }

    @Test
    fun `progreso con la mitad consumida es cincuenta`() {
        assertEquals(50, crearResumen(kcalConsumidas = 1000.0, kcalObjetivo = 2000.0).progresoPorcentaje)
    }

    @Test
    fun `progreso completo es cien`() {
        assertEquals(100, crearResumen(kcalConsumidas = 2000.0, kcalObjetivo = 2000.0).progresoPorcentaje)
    }

    @Test
    fun `progreso no supera cien aunque se exceda el objetivo`() {
        assertEquals(100, crearResumen(kcalConsumidas = 2500.0, kcalObjetivo = 2000.0).progresoPorcentaje)
    }

    @Test
    fun `progreso con objetivo cero es cero para evitar division por cero`() {
        assertEquals(0, crearResumen(kcalConsumidas = 500.0, kcalObjetivo = 0.0).progresoPorcentaje)
    }
}