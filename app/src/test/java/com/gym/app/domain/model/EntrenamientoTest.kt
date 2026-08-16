/**
 * @file EntrenamientoTest.kt
 * @brief Pruebas unitarias del modelo de dominio Entrenamiento.
 */
package com.gym.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @class EntrenamientoTest
 * @brief Verifica el cálculo del progreso porcentual de la sesión de entrenamiento.
 */
class EntrenamientoTest {

    private fun crearEntrenamiento(
        ejerciciosRealizados: Int = 0,
        totalEjercicios: Int = 6
    ): Entrenamiento = Entrenamiento(
        id = "rutina-1",
        nombre = "Push A - Pecho y Tríceps",
        grupoMuscular = listOf("Pecho", "Hombro", "Tríceps"),
        seriesTotales = 18,
        ejerciciosRealizados = ejerciciosRealizados,
        totalEjercicios = totalEjercicios,
        duracionMinutos = 48,
        completo = false
    )

    @Test
    fun `progreso inicial sin ejercicios es cero`() {
        assertEquals(0, crearEntrenamiento(ejerciciosRealizados = 0).progresoPorcentaje)
    }

    @Test
    fun `progreso con la mitad de ejercicios es cincuenta`() {
        assertEquals(50, crearEntrenamiento(ejerciciosRealizados = 3, totalEjercicios = 6).progresoPorcentaje)
    }

    @Test
    fun `progreso completo es cien`() {
        assertEquals(100, crearEntrenamiento(ejerciciosRealizados = 6, totalEjercicios = 6).progresoPorcentaje)
    }

    @Test
    fun `progreso con total de ejercicios cero es cero para evitar division por cero`() {
        assertEquals(0, crearEntrenamiento(ejerciciosRealizados = 4, totalEjercicios = 0).progresoPorcentaje)
    }
}