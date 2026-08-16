/**
 * @file SesionEntrenamiento.kt
 * @brief Modelo de dominio que representa una sesión de entrenamiento realizada.
 *
 * La sesión registra el resultado real de un entrenamiento: qué ejercicios se completaron,
 * cuántas series se ejecutaron, la duración y si la sesión finalizó por completo. Se usa
 * para el seguimiento de progreso, la sincronización con el plan nutricional y la
 * planificación de los días de mayor o menor ingesta calórica.
 */
package com.gym.app.domain.model

/**
 * @class SesionEntrenamiento
 * @brief Representa una sesión de entrenamiento completada o en curso.
 *
 * @property id Identificador único de la sesión.
 * @property userId Identificador del usuario que realizó la sesión.
 * @property fecha Fecha y hora de la sesión en formato epoch millis.
 * @property nombreRutina Nombre de la rutina ejecutada en la sesión.
 * @property ejerciciosCompletados Lista de identificadores de ejercicios completados.
 * @property serieRealizadas Número total de series realizadas en la sesión.
 * @property duracionMinutos Duración de la sesión en minutos.
 * @property completo Indica si la sesión se finalizó por completo (true) o quedó a medias (false).
 */
data class SesionEntrenamiento(
    val id: String,
    val userId: String,
    val fecha: Long,
    val nombreRutina: String,
    val ejerciciosCompletados: List<String> = emptyList(),
    val serieRealizadas: Int = 0,
    val duracionMinutos: Int = 0,
    val completo: Boolean = false
)