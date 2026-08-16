/**
 * @file EntidadToma.kt
 * @brief Entidad Room que representa una toma de comida dentro de un plan diario.
 */
package com.gym.app.data.local.entidad

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @class EntidadToma
 * @brief Toma de comida del plan (desayuno, media mañana, comida, merienda, cena...).
 * Cada toma agrupa varios [EntidadIngredienteToma].
 */
@Entity(
    tableName = "toma",
    indices = [Index(value = ["planComidaId"])]
)
data class EntidadToma(
    @PrimaryKey
    val id: String,
    val planComidaId: String,
    val tipoIngesta: String, // DESAYUNO, MEDIA_MAÑANA, COMIDA, MERIENDA, CENA, POST_ENTRENO
    val orden: Int,
    val horaSugerida: String? = null
)