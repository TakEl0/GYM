/**
 * @file RepositorioEjercicio.kt
 * @brief Puerto de repositorio del catálogo de ejercicios en la capa de dominio.
 * Define el contrato para observar y mantener el catálogo de ejercicios usado por
 * las rutinas y por el motor de sustituciones de maquinaria.
 */
package com.gym.app.domain.repository

import com.gym.app.domain.model.Ejercicio
import kotlinx.coroutines.flow.Flow

/**
 * @interface RepositorioEjercicio
 * @brief Contrato de acceso al catálogo de ejercicios del gimnasio.
 */
interface RepositorioEjercicio {

    /**
     * @brief Observa de forma reactiva todo el catálogo de ejercicios.
     * @return Flujo reactivo con la lista de [Ejercicio] disponibles.
     */
    fun observarEjercicios(): Flow<List<Ejercicio>>

    /**
     * @brief Guarda o actualiza un ejercicio individual del catálogo.
     * @param ejercicio Ejercicio a persistir.
     */
    suspend fun guardarEjercicio(ejercicio: Ejercicio)

    /**
     * @brief Guarda o actualiza varios ejercicios del catálogo de forma atómica.
     * Se utiliza durante la carga inicial del catálogo o la sincronización masiva.
     * @param ejercicios Lista de ejercicios a persistir.
     */
    suspend fun guardarVarios(ejercicios: List<Ejercicio>)
}