/**
 * @file EntidadBloqueRutina.kt
 * @brief Entidad Room que representa un bloque de series dentro de una rutina.
 */
package com.gym.app.data.local.entidad

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @class EntidadBloqueRutina
 * @brief Bloque de entrenamiento: un ejercicio con sus series, repeticiones,
 * peso y descanso programados dentro de una [EntidadRutina].
 */
@Entity(
    tableName = "bloque_rutina",
    indices = [Index(value = ["rutinaId"])]
)
data class EntidadBloqueRutina(
    @PrimaryKey
    val id: String,
    val rutinaId: String,
    val ejercicioId: String,
    val serie: Int,
    val repeticiones: Int,
    val pesoKg: Double? = null,
    val descansoSegundos: Int
)