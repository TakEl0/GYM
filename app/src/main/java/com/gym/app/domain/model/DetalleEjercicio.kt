/**
 * @file DetalleEjercicio.kt
 * @brief Modelo de dominio que representa los detalles específicos de prescripción de un ejercicio en una rutina.
 */
package com.gym.app.domain.model

/**
 * @class DetalleEjercicio
 * @brief Contiene las métricas de un ejercicio dentro de un entrenamiento (series, repeticiones, velocidad/TUT y descanso).
 * 
 * @property nombre Nombre descriptivo del ejercicio (p. ej. "Femoral tumbado").
 * @property series Número de series prescritas (S).
 * @property repeticiones Número de repeticiones por serie (R).
 * @property velocidad Velocidad o ritmo de ejecución (TUT, p. ej. "1 y 1").
 * @property descansoSegundos Tiempo de descanso entre series en segundos (T).
 */
data class DetalleEjercicio(
    val nombre: String,
    val series: Int,
    val repeticiones: Int,
    val velocidad: String,
    val descansoSegundos: Int
)
