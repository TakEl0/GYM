/**
 * @file Entrenamiento.kt
 * @brief Modelo de dominio que representa una rutina de entrenamiento.
 * Esta entidad pertenece a la capa de dominio y es independiente de
 * cualquier framework de Android o de la base de datos subyacente.
 */
package com.gym.app.domain.model

/**
 * @class Entrenamiento
 * @brief Representa una rutina de entrenamiento programada para un día.
 * @property id Identificador único de la rutina.
 * @property nombre Nombre descriptivo de la rutina (p. ej. "Push A - Pecho y Tríceps").
 * @property grupoMuscular Grupos musculares principales trabajados.
 * @property seriesTotales Número total de series previstas en la sesión.
 * @property ejerciciosRealizados Número de ejercicios completados en la sesión.
 * @property totalEjercicios Número total de ejercicios de la sesión.
 * @property duracionMinutos Duración estimada o real de la sesión en minutos.
 * @property completo Indica si la sesión ha finalizado por completo.
 * @property fecha Fecha programada de la sesión en formato epoch millis (0 = sin fecha asignada).
 */
data class Entrenamiento(
    val id: String,
    val nombre: String,
    val grupoMuscular: List<String>,
    val seriesTotales: Int,
    val ejerciciosRealizados: Int,
    val totalEjercicios: Int,
    val duracionMinutos: Int,
    val completo: Boolean,
    val fecha: Long = 0L
) {

    /**
     * @brief Calcula el porcentaje de progreso de la sesión (0..100).
     * @return Porcentaje de ejercicios completados redondeado a entero.
     */
    val progresoPorcentaje: Int
        get() = if (totalEjercicios == 0) 0 else (ejerciciosRealizados * 100) / totalEjercicios
}