/**
 * @file BloqueRutina.kt
 * @brief Modelo de dominio que representa un bloque de series de un ejercicio dentro de una rutina.
 *
 * Un bloque define cuántas series, repeticiones, carga y descanso se deben aplicar a un
 * ejercicio concreto. El volumen semanal recomendado del método Naturvitia (10-12 series
 * para principiantes, 12-20 para intermedios) se calcula a partir de estos bloques.
 */
package com.gym.app.domain.model

/**
 * @class BloqueRutina
 * @brief Representa la prescripción de series de un ejercicio dentro de una rutina.
 *
 * @property id Identificador único del bloque dentro de la rutina.
 * @property ejercicioId Identificador del [Ejercicio] que se ejecuta.
 * @property serie Número de series prescritas para el ejercicio en la sesión.
 * @property repeticiones Número de repeticiones por serie.
 * @property pesoKg Carga de trabajo en kilogramos (opcional; nulo si es peso corporal).
 * @property descansoSegundos Tiempo de descanso entre series en segundos.
 */
data class BloqueRutina(
    val id: String,
    val ejercicioId: String,
    val serie: Int,
    val repeticiones: Int,
    val pesoKg: Double? = null,
    val descansoSegundos: Int = 90
)