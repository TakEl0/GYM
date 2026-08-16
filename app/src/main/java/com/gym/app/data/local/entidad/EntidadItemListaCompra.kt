/**
 * @file EntidadItemListaCompra.kt
 * @brief Entidad Room que representa un artículo de la lista de la compra semanal.
 */
package com.gym.app.data.local.entidad

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @class EntidadItemListaCompra
 * @brief Artículo de la lista de la compra con su escala a paquetes comerciales
 * y el supermercado donde comprarlo.
 */
@Entity(
    tableName = "item_lista_compra",
    indices = [Index(value = ["listaId"])]
)
data class EntidadItemListaCompra(
    @PrimaryKey
    val id: String,
    val listaId: String,
    val nombreAlimento: String,
    val cantidadGramos: Double,
    val unidadComercial: String? = null,
    val cantidadPaquetes: Int,
    val supermercado: String? = null,
    val tipoIngestaOrigen: String, // join por coma de las tomas donde aparece
    val comprado: Boolean
)