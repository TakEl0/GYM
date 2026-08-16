/**
 * @file EntidadListaCompra.kt
 * @brief Entidad Room que representa la lista de la compra semanal (cabecera).
 */
package com.gym.app.data.local.entidad

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @class EntidadListaCompra
 * @brief Cabecera de la lista de la compra generada a partir de los planes semanales.
 * Cada lista contiene varios [EntidadItemListaCompra].
 */
@Entity(
    tableName = "lista_compra",
    indices = [Index(value = ["userId"])]
)
data class EntidadListaCompra(
    @PrimaryKey
    val id: String,
    val userId: String,
    val semanaInicio: Long // epoch millis (lunes de la semana)
)