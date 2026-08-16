/**
 * @file CerrarSesionCasoUso.kt
 * @brief Caso de uso de cierre de sesión en la aplicación GYM.
 */
package com.gym.app.domain.usecase.autenticacion

import com.gym.app.domain.repository.RepositorioAutenticacion
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @class CerrarSesionCasoUso
 * @brief Orquesta el cierre de la sesión actual, limpiando las credenciales
 * almacenadas a través del [RepositorioAutenticacion].
 */
class CerrarSesionCasoUso(
    private val repositorioAutenticacion: RepositorioAutenticacion,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Cierra la sesión activa del usuario en el sistema.
     * Delega la operación en el repositorio de autenticación, que se encarga de
     * invalidar la sesión remota y limpiar las credenciales locales cifradas.
     */
    suspend fun ejecutar() {
        withContext(dispatcher) {
            repositorioAutenticacion.cerrarSesion()
        }
    }
}