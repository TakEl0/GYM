/**
 * @file DtoRegistroPesoRemoto.kt
 * @brief DTO serializable para registros de peso en Supabase.
 */
package com.gym.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @class DtoRegistroPesoRemoto
 * @brief Representa la tabla "registros_peso" en Supabase.
 */
@Serializable
data class DtoRegistroPesoRemoto(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("peso_kg")
    val pesoKg: Double,
    @SerialName("grasa_corporal")
    val grasaCorporal: Double? = null,
    val fecha: Long
)
