/**
 * @file EntidadIngredienteToma.kt
 * @brief Entidad Room que representa un ingrediente dentro de una toma del plan.
 */
package com.gym.app.data.local.entidad

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @class EntidadIngredienteToma
 * @brief Ingrediente con su gramaje dentro de una [EntidadToma] del plan de comidas.
 * El pesaje indica si el alimento se pesa cocinado o en crudo según el método Naturvitia.
 */
@Entity(
    tableName = "ingrediente_toma",
    indices = [Index(value = ["tomaId"])]
)
data class EntidadIngredienteToma(
    @PrimaryKey
    val id: String,
    val tomaId: String,
    val alimentoId: String? = null,
    val nombre: String,
    val cantidadGramos: Double,
    val pesaje: String, // COCINADO / CRUDO
    val origenPlan: Boolean
)