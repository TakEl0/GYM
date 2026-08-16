/**
 * @file RepositorioPerfil.kt
 * @brief Puerto de repositorio del perfil de usuario en la capa de dominio.
 * Define el contrato para observar, guardar y actualizar los datos antropométricos
 * y de objetivos del usuario necesarios para los cálculos del método Naturvitia.
 */
package com.gym.app.domain.repository

import com.gym.app.domain.model.PerfilUsuario
import kotlinx.coroutines.flow.Flow

/**
 * @interface RepositorioPerfil
 * @brief Contrato de acceso al perfil del usuario.
 * La capa de dominio depende de esta abstracción; la implementación concreta puede
 * usar Room, EncryptedSharedPreferences o Supabase de forma intercambiable.
 */
interface RepositorioPerfil {

    /**
     * @brief Observa de forma reactiva el perfil del usuario por su identificador.
     * @param id Identificador único del perfil/usuario.
     * @return Flujo reactivo con el [PerfilUsuario], o `null` si aún no existe.
     */
    fun observarPerfil(id: String): Flow<PerfilUsuario?>

    /**
     * @brief Obtiene el perfil del usuario de forma puntual (una sola lectura).
     * @param id Identificador único del perfil/usuario.
     * @return El [PerfilUsuario] encontrado, o `null` si no existe.
     */
    suspend fun obtenerPerfil(id: String): PerfilUsuario?

    /**
     * @brief Guarda o actualiza por completo el perfil del usuario.
     * @param perfil Perfil a persistir.
     */
    suspend fun guardarPerfil(perfil: PerfilUsuario)

    /**
     * @brief Actualiza únicamente los objetivos y datos antropométricos del perfil.
     * Permite modificar los campos usados en los cálculos de metabolismo sin
     * sobrescribir datos no relacionados (nombre, email, etc.).
     * @param id Identificador único del perfil.
     * @param pesoObjetivoKg Nuevo peso de referencia en kilogramos.
     * @param alturaCm Nueva estatura en centímetros.
     * @param edad Nueva edad en años.
     * @param sexo Sexo de referencia (HOMBRE o MUJER).
     * @param factorActividad Nivel de actividad (SEDENTARIO, LIGERO, MODERADO, FUERTE).
     * @param objetivo Objetivo nutricional (VOLUMEN, DEFINICION, MANTENIMIENTO).
     */
    suspend fun actualizarObjetivos(
        id: String,
        pesoObjetivoKg: Double,
        alturaCm: Double,
        edad: Int,
        sexo: String,
        factorActividad: String,
        objetivo: String
    )
}