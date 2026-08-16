/**
 * @file ObservarEntrenamientosCalendarioCasoUso.kt
 * @brief Caso de uso de observación reactiva de los entrenamientos del calendario personal.
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.model.Entrenamiento
import com.gym.app.domain.repository.RepositorioEntrenamiento
import kotlinx.coroutines.flow.Flow

/**
 * @class ObservarEntrenamientosCalendarioCasoUso
 * @brief Expone un flujo reactivo con las sesiones de [Entrenamiento] comprendidas
 * en un rango de fechas, para alimentar la vista de calendario personal de entrenos.
 */
class ObservarEntrenamientosCalendarioCasoUso(
    private val repositorioEntrenamiento: RepositorioEntrenamiento
) {

    /**
     * @brief Observa los entrenamientos dentro del rango de fechas indicado.
     * @param inicio Epoch millis del primer día del rango (inclusive).
     * @param fin Epoch millis del último día del rango (inclusive).
     * @return [Flow] que emite la lista de [Entrenamiento] del rango ordenada por fecha.
     */
    operator fun invoke(inicio: Long, fin: Long): Flow<List<Entrenamiento>> =
        repositorioEntrenamiento.observarEntrenamientosEntre(inicio, fin)
}