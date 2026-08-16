/**
 * @file ObservarEntrenamientosCasoUso.kt
 * @brief Caso de uso de observación reactiva de los entrenamientos del usuario.
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.model.Entrenamiento
import com.gym.app.domain.repository.RepositorioEntrenamiento
import kotlinx.coroutines.flow.Flow

/**
 * @class ObservarEntrenamientosCasoUso
 * @brief Expone un flujo reactivo con las sesiones de [Entrenamiento] del usuario,
 * permitiendo que la capa de presentación reaccione a los cambios de la rutina.
 */
class ObservarEntrenamientosCasoUso(
    private val repositorioEntrenamiento: RepositorioEntrenamiento
) {

    /**
     * @brief Observa los entrenamientos del usuario.
     * @param userId Identificador del usuario (opcional). Si el repositorio no
     * filtra por usuario, se usa la observación genérica de la rutina del día.
     * @return [Flow] que emite la lista de [Entrenamiento] disponible.
     */
    fun ejecutar(userId: String?): Flow<List<Entrenamiento>> =
        repositorioEntrenamiento.observarEntrenamientos(userId)
}