/**
 * @file RepositorioEntrenamiento.kt
 * @brief Puerto de repositorio de entrenamientos en la capa de dominio.
 * Define el contrato que la capa de datos debe implementar para obtener
 * la información de las rutinas de entrenamiento del usuario.
 */
package com.gym.app.domain.repository

import com.gym.app.domain.model.Entrenamiento
import kotlinx.coroutines.flow.Flow

/**
 * @interface RepositorioEntrenamiento
 * @brief Contrato de acceso a los datos de entrenamiento.
 * La capa de dominio depende de esta abstracción, permitiendo que la
 * implementación concreta (Room, API o datos simulados) sea intercambiable.
 */
interface RepositorioEntrenamiento {

    /**
     * @brief Observa de forma reactiva los entrenamientos del usuario.
     * @param userId Identificador del usuario (opcional si se filtra por sesión activa).
     * @return Flujo reactivo con la lista de [Entrenamiento] del usuario.
     */
    fun observarEntrenamientos(userId: String?): Flow<List<Entrenamiento>>

    /**
     * @brief Obtiene la rutina programada para hoy.
     * @return Entrenamiento del día, o null si no hay sesión programada.
     */
    suspend fun obtenerEntrenamientoDeHoy(): Entrenamiento?

    /**
     * @brief Observa de forma reactiva los entrenamientos dentro de un rango de fechas.
     * @param inicio Epoch millis del inicio del rango (inclusive).
     * @param fin Epoch millis del fin del rango (inclusive).
     * @return Flujo reactivo con las sesiones del rango ordenadas por fecha.
     */
    fun observarEntrenamientosEntre(inicio: Long, fin: Long): Flow<List<Entrenamiento>>

    /**
     * @brief Obtiene el entrenamiento programado en una fecha concreta.
     * @param fecha Epoch millis del inicio del día.
     * @return Entrenamiento de esa fecha, o null si no hay sesión.
     */
    suspend fun obtenerEntrenamientoEnFecha(fecha: Long): Entrenamiento?

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

    /**
     * @brief Actualiza el progreso de ejercicios completados en un entrenamiento.
     * @param entrenamientoId Identificador del entrenamiento.
     * @param ejerciciosRealizados Número de ejercicios ya realizados.
     */
    suspend fun actualizarProgreso(entrenamientoId: String, ejerciciosRealizados: Int)

    /**
     * @brief Guarda o inserta una nueva rutina de entrenamiento.
     * @param entrenamiento Rutina de entrenamiento a persistir.
     */
    suspend fun guardarEntrenamiento(entrenamiento: Entrenamiento)
}