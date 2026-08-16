/**
 * @file SincronizarPerfilConBackendCasoUso.kt
 * @brief Caso de uso de sincronización del perfil con el backend remoto.
 */
package com.gym.app.domain.usecase.perfil

import com.gym.app.domain.model.PerfilUsuario
import com.gym.app.domain.repository.RepositorioAutenticacion
import com.gym.app.domain.repository.RepositorioPerfil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @class SincronizarPerfilConBackendCasoUso
 * @brief Descarga el perfil del usuario autenticado desde el backend remoto
 * (Supabase) hacia el almacén local y devuelve el [PerfilUsuario] resultante.
 *
 * Flujo de ejecución:
 * 1. Invoca [RepositorioAutenticacion.sincronizarPerfilLocal] para replicar el
 *    perfil remoto en la base de datos local.
 * 2. Si la sincronización falla, propaga el error.
 * 3. Consulta la sesión activa; si no hay sesión, devuelve `Result.success(null)`.
 * 4. Con el identificador del usuario de la sesión, recupera el perfil local ya
 *    sincronizado mediante [RepositorioPerfil.obtenerPerfil].
 */
class SincronizarPerfilConBackendCasoUso(
    private val repositorioAutenticacion: RepositorioAutenticacion,
    private val repositorioPerfil: RepositorioPerfil,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Sincroniza el perfil desde el backend y lo devuelve.
     * @return [Result] con el [PerfilUsuario] sincronizado, con `null` si no hay
     * sesión activa, o con el error producido en la sincronización.
     */
    suspend fun ejecutar(): Result<PerfilUsuario?> = withContext(dispatcher) {
        val resultadoSincronizacion = repositorioAutenticacion.sincronizarPerfilLocal()
        if (resultadoSincronizacion.isFailure) {
            return@withContext Result.failure(
                resultadoSincronizacion.exceptionOrNull()
                    ?: IllegalStateException("Error desconocido al sincronizar el perfil.")
            )
        }

        val sesion = repositorioAutenticacion.obtenerSesionActual()
        val userId = sesion?.user?.id
        if (userId.isNullOrBlank()) {
            return@withContext Result.success(null)
        }

        try {
            Result.success(repositorioPerfil.obtenerPerfil(userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}