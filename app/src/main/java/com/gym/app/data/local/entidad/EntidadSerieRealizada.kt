/**
 * @file EntidadSerieRealizada.kt
 * @brief Entidad Room que representa una serie realizada en una sesión de entrenamiento.
 */
package com.gym.app.data.local.entidad

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @class EntidadSerieRealizada
 * @brief Serie realizada registrada localmente para persistencia offline-first.
 */
@Entity(
    tableName = "serie_realizada",
    indices = [Index(value = ["sesionId"])]
)
data class EntidadSerieRealizada(
    @PrimaryKey
    val id: String,
    val sesionId: String,
    val ejercicioId: String,
    val numeroSerie: Int,
    val pesoKg: Double,
    val repeticiones: Int,
    val fecha: Long,
    val sincronizado: Boolean
)
