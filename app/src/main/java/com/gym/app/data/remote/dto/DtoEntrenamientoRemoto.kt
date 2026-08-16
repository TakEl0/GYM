/**
 * @file DtoEntrenamientoRemoto.kt
 * @brief DTO serializable para entrenamientos en Supabase.
 */
package com.gym.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @class DtoEntrenamientoRemoto
 * @brief Representa la tabla "entrenamientos" en Supabase.
 */
@Serializable
data class DtoEntrenamientoRemoto(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val nombre: String,
    @SerialName("grupo_muscular")
    val grupoMuscular: List<String>,
    @SerialName("series_totales")
    val seriesTotales: Int,
    @SerialName("ejercicios_realizados")
    val ejerciciosRealizados: Int,
    @SerialName("total_ejercicios")
    val totalEjercicios: Int,
    @SerialName("duracion_minutos")
    val duracionMinutos: Int,
    val completo: Boolean,
    val fecha: Long = 0L
)
