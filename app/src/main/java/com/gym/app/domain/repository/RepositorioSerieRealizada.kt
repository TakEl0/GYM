/**
 * @file RepositorioSerieRealizada.kt
 * @brief Puerto de repositorio de series realizadas en la capa de dominio.
 */
package com.gym.app.domain.repository

import com.gym.app.domain.model.SerieRealizada
import kotlinx.coroutines.flow.Flow

/**
 * @interface RepositorioSerieRealizada
 * @brief Contrato de acceso a las series realizadas durante los entrenamientos.
 */
interface RepositorioSerieRealizada {
    /** Observa las series realizadas para una sesión dada, ordenadas por número de serie. */
    fun observarPorSesion(sesionId: String): Flow<List<SerieRealizada>>

    /** Guarda o actualiza una serie realizada. */
    suspend fun guardarSerie(serie: SerieRealizada)

    /** Elimina una serie realizada por su identificador. */
    suspend fun eliminarSerie(id: String)

    /** Obtiene el último peso registrado para un ejercicio específico. */
    suspend fun ultimoPesoPorEjercicio(ejercicioId: String): Double?

    /** Obtiene una serie realizada por su identificador, o `null` si no existe. */
    suspend fun obtenerPorId(id: String): SerieRealizada?

    /** Obtiene las series de un ejercicio concreto dentro de una sesión, ordenadas por número de serie. */
    suspend fun observarSeriesDeEjercicio(
        sesionId: String,
        ejercicioId: String
    ): List<SerieRealizada>
}
