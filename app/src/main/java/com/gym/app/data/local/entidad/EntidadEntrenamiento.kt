/**
 * @file EntidadEntrenamiento.kt
 * @brief Entidad Room que mapea el modelo de dominio Entrenamiento.
 */
package com.gym.app.data.local.entidad

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * @class EntidadEntrenamiento
 * @brief Almacena las rutinas y sesiones de entrenamiento en la base de datos local Room.
 */
@Entity(
    tableName = "entrenamientos",
    indices = [Index(value = ["userId"])]
)
data class EntidadEntrenamiento(
    @PrimaryKey
    val id: String,
    val userId: String,
    val nombre: String,
    val grupoMuscular: List<String>,
    val seriesTotales: Int,
    val ejerciciosRealizados: Int,
    val totalEjercicios: Int,
    val duracionMinutos: Int,
    val completo: Boolean,
    val fecha: Long = 0L,
    val sincronizado: Boolean
)
