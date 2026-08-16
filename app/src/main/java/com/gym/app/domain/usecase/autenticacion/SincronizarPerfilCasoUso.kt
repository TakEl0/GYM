/**
 * @file SincronizarPerfilCasoUso.kt
 * @brief Caso de uso de sincronización del perfil del usuario autenticado.
 */
package com.gym.app.domain.usecase.autenticacion

import com.gym.app.domain.repository.RepositorioAutenticacion
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @class SincronizarPerfilCasoUso
 * @brief Descarga el perfil del usuario desde el backend remoto (Supabase) y lo
 * replica en la base de datos local Room para su uso sin conexión.
 */
class SincronizarPerfilCasoUso(
    private val repositorioAutenticacion: RepositorioAutenticacion,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Sincroniza el perfil del usuario autenticado desde el remoto hacia lo local.
     * @return [Result] con éxito (Unit) o con el error producido en la sincronización.
     */
    suspend fun ejecutar(): Result<Unit> = withContext(dispatcher) {
        repositorioAutenticacion.sincronizarPerfilLocal()
    }
}