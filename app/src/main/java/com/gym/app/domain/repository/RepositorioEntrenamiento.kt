/**
 * @file RepositorioEntrenamiento.kt
 * @brief Puerto de repositorio de entrenamientos en la capa de dominio.
 * Define el contrato que la capa de datos debe implementar para obtener
 * la información de las rutinas de entrenamiento del usuario.
 */
package com.gym.app.domain.repository

import com.gym.app.domain.model.Entrenamiento

/**
 * @interface RepositorioEntrenamiento
 * @brief Contrato de acceso a los datos de entrenamiento.
 * La capa de dominio depende de esta abstracción, permitiendo que la
 * implementación concreta (Room, API o datos simulados) sea intercambiable.
 */
interface RepositorioEntrenamiento {

    /**
     * @brief Obtiene la rutina programada para hoy.
     * @return Entrenamiento del día, o null si no hay sesión programada.
     */
    suspend fun obtenerEntrenamientoDeHoy(): Entrenamiento?

    /**
     * @brief Obtiene las sesiones completadas en la semana actual.
     * @return Número de sesiones completadas.
     */
    suspend fun obtenerSesionesCompletadasSemana(): Int

    /**
     * @brief Obtiene el número total de sesiones previstas para la semana.
     * @return Total de sesiones planificadas.
     */
    suspend fun obtenerTotalSesionesSemana(): Int
}