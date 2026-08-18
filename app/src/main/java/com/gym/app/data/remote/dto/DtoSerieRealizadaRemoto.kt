/**
 * @file DtoSerieRealizadaRemoto.kt
 * @brief DTO serializable para series realizadas en Supabase.
 */
package com.gym.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @class DtoSerieRealizadaRemoto
 * @brief Representa la tabla "series_realizadas" en Supabase.
 */
@Serializable
data class DtoSerieRealizadaRemoto(
    val id: String,
    @SerialName("sesion_id")
    val sesionId: String,
    @SerialName("ejercicio_id")
    val ejercicioId: String,
    @SerialName("numero_serie")
    val numeroSerie: Int,
    @SerialName("peso_kg")
    val pesoKg: Double,
    val repeticiones: Int,
    val fecha: Long
)
