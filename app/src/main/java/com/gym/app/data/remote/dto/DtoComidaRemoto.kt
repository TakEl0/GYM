/**
 * @file DtoComidaRemoto.kt
 * @brief DTO serializable para comidas e ingestas en Supabase.
 */
package com.gym.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @class DtoComidaRemoto
 * @brief Representa la tabla "comidas" en Supabase.
 */
@Serializable
data class DtoComidaRemoto(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val nombre: String,
    val kcal: Int,
    @SerialName("proteinas_g")
    val proteinasG: Double,
    @SerialName("carbohidratos_g")
    val carbohidratosG: Double,
    @SerialName("grasas_g")
    val grasasG: Double,
    @SerialName("tipo_ingesta")
    val tipoIngesta: String,
    val fecha: Long
)
