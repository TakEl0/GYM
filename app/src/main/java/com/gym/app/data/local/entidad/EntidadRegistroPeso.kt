/**
 * @file EntidadRegistroPeso.kt
 * @brief Entidad Room que representa un registro de peso corporal.
 */
package com.gym.app.data.local.entidad

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @class EntidadRegistroPeso
 * @brief Almacena las mediciones de peso corporal en la base de datos local Room.
 */
@Entity(
    tableName = "registros_peso",
    indices = [Index(value = ["userId"])]
)
data class EntidadRegistroPeso(
    @PrimaryKey
    val id: String,
    val userId: String,
    val pesoKg: Double,
    val grasaCorporal: Double?,
    val fecha: Long, // epoch millis
    val sincronizado: Boolean
)
