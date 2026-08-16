/**
 * @file EntidadIngestaRegistrada.kt
 * @brief Entidad Room que representa una comida realmente consumida por el usuario.
 */
package com.gym.app.data.local.entidad

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @class EntidadIngestaRegistrada
 * @brief Registro de una comida consumida (manual, importada o por foto) con sus macros.
 * Se usa para el rebalanceo intra-día y el resumen nutricional real.
 */
@Entity(
    tableName = "ingesta_registrada",
    indices = [Index(value = ["userId"])]
)
data class EntidadIngestaRegistrada(
    @PrimaryKey
    val id: String,
    val userId: String,
    val nombre: String,
    val kcal: Double,
    val proteinasG: Double,
    val carbohidratosG: Double,
    val grasasG: Double,
    val tipoIngesta: String, // DESAYUNO, COMIDA, MERIENDA, CENA
    val fecha: Long,         // epoch millis
    val momentoDia: String,  // DESAYUNO, MEDIA_MAÑANA, COMIDA, MERIENDA, CENA
    val origen: String,      // MANUAL / IMPORTADA / FOTO
    val sincronizado: Boolean
)