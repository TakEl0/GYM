/**
 * @file GuardarGimnasioCasoUso.kt
 * @brief Caso de uso de persistencia de la información del gimnasio.
 */
package com.gym.app.domain.usecase.gimnasio

import com.gym.app.domain.model.Gimnasio
import com.gym.app.domain.repository.RepositorioGimnasio
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @class GuardarGimnasioCasoUso
 * @brief Valida y persiste la información completa del gimnasio del usuario
 * (nombre, dirección y parque de máquinas) delegando en [RepositorioGimnasio.guardarGimnasio].
 *
 * La única validación exigida por el dominio es que el nombre del gimnasio no
 * esté vacío, ya que es el campo identificativo que muestra la aplicación.
 */
class GuardarGimnasioCasoUso(
    private val repositorioGimnasio: RepositorioGimnasio,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Guarda o actualiza la información del gimnasio.
     * @param gimnasio [Gimnasio] a persistir.
     * @return [Result] con éxito (Unit) o con el error de validación o de persistencia.
     */
    suspend fun ejecutar(gimnasio: Gimnasio): Result<Unit> = withContext(dispatcher) {
        if (gimnasio.nombre.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("El nombre del gimnasio no puede estar vacío.")
            )
        }
        try {
            repositorioGimnasio.guardarGimnasio(gimnasio)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}