/**
 * @file EntidadSesionEntrenamiento.kt
 * @brief Entidad Room que representa una sesión de entrenamiento realizada.
 */
package com.gym.app.data.local.entidad

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @class EntidadSesionEntrenamiento
 * @brief Sesión de entrenamiento registrada por el usuario con su resumen
 * (ejercicios completados, series, duración y estado de finalización).
 */
@Entity(
    tableName = "sesion_entrenamiento",
    indices = [Index(value = ["userId"])]
)
data class EntidadSesionEntrenamiento(
    @PrimaryKey
    val id: String,
    val userId: String,
    val fecha: Long, // epoch millis
    val nombreRutina: String,
    val ejerciciosCompletados: String, // join por coma de ids de ejercicios
    val serieRealizadas: Int,
    val duracionMinutos: Int,
    val completo: Boolean,
    val sincronizado: Boolean
)