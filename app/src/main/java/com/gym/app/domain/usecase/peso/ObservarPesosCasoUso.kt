/**
 * @file ObservarPesosCasoUso.kt
 * @brief Caso de uso de observación reactiva del historial de peso corporal.
 */
package com.gym.app.domain.usecase.peso

import com.gym.app.domain.model.RegistroPeso
import com.gym.app.domain.repository.RepositorioPeso
import kotlinx.coroutines.flow.Flow

/**
 * @class ObservarPesosCasoUso
 * @brief Expone un flujo reactivo con el historial de [RegistroPeso] del usuario,
 * permitiendo que la capa de presentación se actualice automáticamente ante
 * nuevas mediciones.
 */
class ObservarPesosCasoUso(
    private val repositorioPeso: RepositorioPeso
) {

    /**
     * @brief Observa el historial de registros de peso del usuario.
     * @param userId Identificador del usuario propietario de los registros.
     * @return [Flow] que emite la lista de [RegistroPeso] ordenada de más reciente
     * a más antiguo, actualizándose ante cambios en la fuente de datos.
     */
    fun ejecutar(userId: String): Flow<List<RegistroPeso>> =
        repositorioPeso.observarPesos(userId)
}