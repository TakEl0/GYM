/**
 * @file EntidadUsuarioPerfil.kt
 * @brief Entidad Room que representa el perfil del usuario sincronizado con Supabase.
 */
package com.gym.app.data.local.entidad

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @class EntidadUsuarioPerfil
 * @brief Almacena localmente los datos del perfil de usuario.
 */
@Entity(tableName = "perfiles")
data class EntidadUsuarioPerfil(
    @PrimaryKey
    val id: String,
    val email: String,
    val nombre: String,
    val pesoObjetivoKg: Double?,
    val createdAt: String?,
    val updatedAt: String?
)
