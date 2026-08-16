/**
 * @file RegistroPeso.kt
 * @brief Modelo de dominio que representa un registro de peso corporal.
 * Esta entidad pertenece a la capa de dominio y se utiliza para la
 * monitorización de la recomposición corporal del usuario.
 */
package com.gym.app.domain.model

import java.time.LocalDate

/**
 * @class RegistroPeso
 * @brief Representa una medición de peso corporal en una fecha concreta.
 * @property fecha Fecha en la que se tomó la medición.
 * @property pesoKg Peso corporal en kilogramos.
 * @property grasaCorporalPorcentaje Porcentaje de grasa corporal (opcional).
 * @property masaMuscularKg Masa muscular en kilogramos (opcional).
 * @property notaComentario Comentario opcional asociado al registro.
 */
data class RegistroPeso(
    val fecha: LocalDate,
    val pesoKg: Double,
    val grasaCorporalPorcentaje: Double? = null,
    val masaMuscularKg: Double? = null,
    val notaComentario: String? = null
)