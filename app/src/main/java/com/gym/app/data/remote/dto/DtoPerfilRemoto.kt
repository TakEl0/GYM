/**
 * @file DtoPerfilRemoto.kt
 * @brief DTO serializable para el perfil de usuario en Supabase (PostgREST).
 */
package com.gym.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @class DtoPerfilRemoto
 * @brief Representa la tabla "perfiles" en Supabase con mapeo snake_case.
 */
@Serializable
data class DtoPerfilRemoto(
    val id: String,
    val email: String,
    val nombre: String,
    @SerialName("peso_objetivo_kg")
    val pesoObjetivoKg: Double? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
