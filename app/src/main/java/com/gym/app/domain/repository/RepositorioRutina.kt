/**
 * @file RepositorioRutina.kt
 * @brief Puerto de repositorio de rutinas de entrenamiento en la capa de dominio.
 * Define el contrato para observar, guardar y eliminar las rutinas de entrenamiento
 * del usuario (PPL, Torso-Pierna, Fullbody, etc.).
 */
package com.gym.app.domain.repository

import com.gym.app.domain.model.Rutina
import kotlinx.coroutines.flow.Flow

/**
 * @interface RepositorioRutina
 * @brief Contrato de acceso a las rutinas de entrenamiento del usuario.
 */
interface RepositorioRutina {

    /**
     * @brief Observa de forma reactiva todas las rutinas del usuario.
     * @return Flujo reactivo con la lista de [Rutina] configuradas.
     */
    fun observarRutinas(): Flow<List<Rutina>>

    /**
     * @brief Guarda o actualiza una rutina de entrenamiento completa.
     * @param rutina Rutina a persistir.
     */
    suspend fun guardarRutina(rutina: Rutina)

    /**
     * @brief Elimina una rutina de entrenamiento por su identificador.
     * @param id Identificador único de la rutina.
     */
    suspend fun eliminarRutina(id: String)
}