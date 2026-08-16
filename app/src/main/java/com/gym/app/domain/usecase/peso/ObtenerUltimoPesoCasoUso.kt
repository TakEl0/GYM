/**
 * @file ObtenerUltimoPesoCasoUso.kt
 * @brief Caso de uso de consulta de la medición de peso más reciente.
 */
package com.gym.app.domain.usecase.peso

import com.gym.app.domain.model.RegistroPeso
import com.gym.app.domain.repository.RepositorioPeso
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @class ObtenerUltimoPesoCasoUso
 * @brief Recupera la última medición de peso corporal registrada por el usuario.
 *
 * El parámetro [userId] se conserva en la firma pública por coherencia con el
 * contrato de dominio; la implementación concreta del repositorio resuelve el
 * usuario activo internamente.
 */
class ObtenerUltimoPesoCasoUso(
    private val repositorioPeso: RepositorioPeso,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Obtiene el último registro de peso disponible.
     * @param userId Identificador del usuario propietario del registro.
     * @return [RegistroPeso] más reciente, o null si no existe ninguno.
     */
    suspend fun ejecutar(userId: String): RegistroPeso? = withContext(dispatcher) {
        repositorioPeso.obtenerUltimoRegistro()
    }
}