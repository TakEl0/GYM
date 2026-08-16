/**
 * @file EntidadGimnasio.kt
 * @brief Entidad Room que representa el gimnasio del usuario (cabecera).
 */
package com.gym.app.data.local.entidad

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @class EntidadGimnasio
 * @brief Cabecera del gimnasio del usuario. Cada gimnasio contiene varias
 * [EntidadMaquina] con su disponibilidad y equipamiento.
 */
@Entity(
    tableName = "gimnasio",
    indices = [Index(value = ["userId"])]
)
data class EntidadGimnasio(
    @PrimaryKey
    val id: String,
    val userId: String,
    val nombre: String,
    val direccion: String? = null
)