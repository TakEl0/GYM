/**
 * @file EntidadRutina.kt
 * @brief Entidad Room que representa una rutina de entrenamiento (cabecera).
 */
package com.gym.app.data.local.entidad

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @class EntidadRutina
 * @brief Cabecera de una rutina de entrenamiento con los días de la semana en
 * los que se ejecuta. Cada rutina contiene varios [EntidadBloqueRutina].
 */
@Entity(
    tableName = "rutina",
    indices = [Index(value = ["userId"])]
)
data class EntidadRutina(
    @PrimaryKey
    val id: String,
    val userId: String,
    val nombre: String,
    val descripcion: String? = null,
    val diasSemana: String // join por coma (1=Lunes..7=Domingo)
)