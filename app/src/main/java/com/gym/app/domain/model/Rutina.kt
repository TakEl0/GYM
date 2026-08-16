/**
 * @file Rutina.kt
 * @brief Modelo de dominio que representa una rutina de entrenamiento programada.
 *
 * La rutina define qué días de la semana se entrena y qué bloques de ejercicios se
 * ejecutan. Es compatible con las estructuras PPL, Torso-Pierna y Fullbody utilizadas
 * en la programación del gimnasio.
 */
package com.gym.app.domain.model

/**
 * @class Rutina
 * @brief Representa una rutina de entrenamiento con su programación semanal.
 *
 * @property id Identificador único de la rutina.
 * @property nombre Nombre descriptivo (p. ej. "PPL - Push A").
 * @property descripcion Descripción opcional de la rutina.
 * @property diasSemana Días de la semana en los que se ejecuta la rutina
 * (1 = Lunes, 2 = Martes ... 7 = Domingo).
 * @property bloques Bloques de ejercicios que componen la rutina.
 */
data class Rutina(
    val id: String,
    val nombre: String,
    val descripcion: String? = null,
    val diasSemana: List<Int> = emptyList(),
    val bloques: List<BloqueRutina> = emptyList()
) {

    companion object {
        /** Día de la semana: lunes. */
        const val LUNES: Int = 1

        /** Día de la semana: martes. */
        const val MARTES: Int = 2

        /** Día de la semana: miércoles. */
        const val MIERCOLES: Int = 3

        /** Día de la semana: jueves. */
        const val JUEVES: Int = 4

        /** Día de la semana: viernes. */
        const val VIERNES: Int = 5

        /** Día de la semana: sábado. */
        const val SABADO: Int = 6

        /** Día de la semana: domingo. */
        const val DOMINGO: Int = 7
    }
}