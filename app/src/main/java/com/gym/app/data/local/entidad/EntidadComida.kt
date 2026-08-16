/**
 * @file EntidadComida.kt
 * @brief Entidad Room que representa una ingesta nutricional.
 */
package com.gym.app.data.local.entidad

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @class EntidadComida
 * @brief Almacena los registros de comidas y macros del usuario en Room.
 */
@Entity(
    tableName = "comidas",
    indices = [Index(value = ["userId"])]
)
data class EntidadComida(
    @PrimaryKey
    val id: String,
    val userId: String,
    val nombre: String,
    val kcal: Int,
    val proteinasG: Double,
    val carbohidratosG: Double,
    val grasasG: Double,
    val tipoIngesta: String, // DESAYUNO, COMIDA, MERIENDA, CENA
    val fecha: Long, // epoch millis
    val sincronizado: Boolean
)
