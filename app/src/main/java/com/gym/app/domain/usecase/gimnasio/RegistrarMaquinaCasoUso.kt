/**
 * @file RegistrarMaquinaCasoUso.kt
 * @brief Caso de uso de alta o actualización de una máquina del gimnasio.
 */
package com.gym.app.domain.usecase.gimnasio

import com.gym.app.domain.model.Gimnasio
import com.gym.app.domain.model.Maquina
import com.gym.app.domain.repository.RepositorioGimnasio
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @class RegistrarMaquinaCasoUso
 * @brief Añade una [Maquina] al parque de máquinas del gimnasio o, si ya existe
 * una con el mismo identificador, la reemplaza por completo. El gimnasio
 * actualizado se persiste mediante [RepositorioGimnasio.guardarGimnasio].
 *
 * La operación mantiene la inmutabilidad del modelo de dominio: se construye un
 * nuevo [Gimnasio] mediante `copy` con la lista de máquinas ya modificada.
 */
class RegistrarMaquinaCasoUso(
    private val repositorioGimnasio: RepositorioGimnasio,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Registra o actualiza una máquina dentro del gimnasio indicado.
     * @param gimnasio Estado actual del gimnasio que contiene (o contendrá) la máquina.
     * @param maquina Máquina a registrar o reemplazar.
     * @return [Result] con éxito (Unit) o con el error de validación o de persistencia.
     */
    suspend fun ejecutar(gimnasio: Gimnasio, maquina: Maquina): Result<Unit> =
        withContext(dispatcher) {
            if (maquina.nombre.isBlank()) {
                return@withContext Result.failure(
                    IllegalArgumentException("El nombre de la máquina no puede estar vacío.")
                )
            }

            val maquinasActualizadas = if (gimnasio.maquinas.any { it.id == maquina.id }) {
                // La máquina ya existe: se reemplaza la ocurrencia por la nueva versión.
                gimnasio.maquinas.map { existente ->
                    if (existente.id == maquina.id) maquina else existente
                }
            } else {
                // La máquina es nueva: se añade al final de la lista.
                gimnasio.maquinas + maquina
            }

            try {
                repositorioGimnasio.guardarGimnasio(gimnasio.copy(maquinas = maquinasActualizadas))
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}