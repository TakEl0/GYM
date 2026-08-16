/**
 * @file ObservarEstadoSesionCasoUso.kt
 * @brief Caso de uso de observación reactiva del estado de la sesión.
 */
package com.gym.app.domain.usecase.autenticacion

import com.gym.app.domain.model.EstadoSesion
import com.gym.app.domain.repository.RepositorioAutenticacion
import kotlinx.coroutines.flow.Flow

/**
 * @class ObservarEstadoSesionCasoUso
 * @brief Expone un flujo reactivo con el [EstadoSesion] actual del usuario,
 * permitiendo que la capa de presentación reaccione a los cambios de autenticación.
 */
class ObservarEstadoSesionCasoUso(
    private val repositorioAutenticacion: RepositorioAutenticacion
) {

    /**
     * @brief Observa en tiempo real el estado de autenticación del usuario.
     * @return [Flow] que emite el [EstadoSesion] actual (NO_AUTENTICADO,
     * AUTENTICADO, CARGANDO o CONFIGURACION_PENDIENTE).
     */
    fun ejecutar(): Flow<EstadoSesion> = repositorioAutenticacion.observarEstadoSesion()
}