/**
 * @file EntidadPlanComida.kt
 * @brief Entidad Room que representa el plan de comidas de un día (cabecera).
 */
package com.gym.app.data.local.entidad

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @class EntidadPlanComida
 * @brief Cabecera del plan de comidas diario según la dieta del nutricionista.
 * Cada plan contiene varias [EntidadToma] (desayuno, comida, cena, etc.).
 */
@Entity(
    tableName = "plan_comida",
    indices = [Index(value = ["userId"])]
)
data class EntidadPlanComida(
    @PrimaryKey
    val id: String,
    val userId: String,
    val nombre: String,
    val fecha: Long, // epoch millis (inicio del día)
    val origenImportacion: Boolean
)