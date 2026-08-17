/**
 * @file EntidadPerfilUsuario.kt
 * @brief Entidad Room que representa el perfil completo del usuario con objetivos nutricionales.
 */
package com.gym.app.data.local.entidad

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @class EntidadPerfilUsuario
 * @brief Almacena los datos del perfil y los objetivos nutricionales del usuario en Room.
 * Incluye los campos antropométricos y de objetivos necesarios para calcular el
 * metabolismo basal (Mifflin-St Jeor) y el reparto de macronutrientes.
 */
@Entity(tableName = "perfil_usuario")
data class EntidadPerfilUsuario(
    @PrimaryKey
    val id: String,
    val email: String,
    val nombre: String,
    val alias: String? = null,
    val pesoObjetivoKg: Double? = null,
    val alturaCm: Double? = null,
    val edad: Int? = null,
    val sexo: String? = null,          // MASCULINO / FEMENINO
    val factorActividad: String? = null, // SEDENTARIO / LIGERO / MODERADO / FUERTE
    val objetivo: String? = null,       // VOLUMEN / DEFINICION / MANTENIMIENTO
    val fechaNacimiento: Long? = null
)