/**
 * @file RegistrarSesionEntrenamientoCasoUso.kt
 * @brief Caso de uso de registro de una sesión de entrenamiento realizada.
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.model.SesionEntrenamiento
import com.gym.app.domain.repository.RepositorioSesionEntrenamiento
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @class RegistrarSesionEntrenamientoCasoUso
 * @brief Valida y persiste una nueva [SesionEntrenamiento] a través del
 * [RepositorioSesionEntrenamiento], garantizando que el registro sea coherente
 * antes de alimentar el seguimiento de progreso y la sincronización
 * nutrición-entrenamiento.
 *
 * Validaciones aplicadas:
 * - La fecha de la sesión debe ser mayor que 0 (epoch millis válido).
 * - La duración de la sesión no puede ser negativa.
 */
class RegistrarSesionEntrenamientoCasoUso(
    private val repositorioSesionEntrenamiento: RepositorioSesionEntrenamiento,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Registra una sesión de entrenamiento en el sistema.
     * @param sesion [SesionEntrenamiento] con los datos de la sesión a guardar.
     * @return [Result] con éxito (Unit) o con el error de validación o de persistencia.
     */
    suspend fun ejecutar(sesion: SesionEntrenamiento): Result<Unit> = withContext(dispatcher) {
        if (sesion.fecha <= 0L) {
            return@withContext Result.failure(
                IllegalArgumentException("La fecha de la sesión debe ser mayor que 0.")
            )
        }
        if (sesion.duracionMinutos < 0) {
            return@withContext Result.failure(
                IllegalArgumentException("La duración de la sesión no puede ser negativa.")
            )
        }
        try {
            repositorioSesionEntrenamiento.guardarSesion(sesion)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}