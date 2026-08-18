/**
 * @file SerieRealizada.kt
 * @brief Modelo de dominio que representa una serie realizada en un ejercicio durante una sesión.
 *
 * El registro granular de cargas (peso y repeticiones por serie) es fundamental para la evolución
 * de la fuerza, cálculo de 1RM (Epley/Brzycki) y la progresión en el entrenamiento en vivo.
 */
package com.gym.app.domain.model

/**
 * @class SerieRealizada
 * @brief Representa una serie ejecutada de un ejercicio.
 *
 * @property id Identificador único de la serie realizada.
 * @property sesionId Identificador de la sesión de entrenamiento a la que pertenece.
 * @property ejercicioId Identificador del ejercicio ejecutado.
 * @property numeroSerie Número secuencial de la serie (1ª serie, 2ª serie, etc.).
 * @property pesoKg Carga utilizada en kilogramos.
 * @property repeticiones Repeticiones completadas en la serie.
 * @property fecha Marca de tiempo en epoch millis en la que se ejecutó la serie.
 */
data class SerieRealizada(
    val id: String,
    val sesionId: String,
    val ejercicioId: String,
    val numeroSerie: Int,
    val pesoKg: Double,
    val repeticiones: Int,
    val fecha: Long
)
