/**
 * @file RegistrarProgresoEntrenamientoCasoUso.kt
 * @brief Caso de uso de actualización del progreso de una sesión de entrenamiento.
 */
package com.gym.app.domain.usecase.entrenamiento

import com.gym.app.domain.repository.RepositorioEntrenamiento
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @class RegistrarProgresoEntrenamientoCasoUso
 * @brief Actualiza el número de ejercicios realizados de una sesión de
 * entrenamiento concreta, delegando la persistencia en el [RepositorioEntrenamiento].
 */
class RegistrarProgresoEntrenamientoCasoUso(
    private val repositorioEntrenamiento: RepositorioEntrenamiento,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Registra el progreso de ejercicios realizados en un entrenamiento.
     * Valida que el número de ejercicios realizados no sea negativo. Ante datos
     * inválidos devuelve un [Result.failure] con [IllegalArgumentException].
     * @param entrenamientoId Identificador único del entrenamiento a actualizar.
     * @param ejerciciosRealizados Número de ejercicios completados en la sesión.
     * @return [Result] con éxito (Unit) o con el error producido.
     */
    suspend fun ejecutar(entrenamientoId: String, ejerciciosRealizados: Int): Result<Unit> =
        withContext(dispatcher) {
            if (ejerciciosRealizados < 0) {
                return@withContext Result.failure(
                    IllegalArgumentException("El número de ejercicios realizados no puede ser negativo.")
                )
            }
            try {
                repositorioEntrenamiento.actualizarProgreso(entrenamientoId, ejerciciosRealizados)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}