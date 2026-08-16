/**
 * @file ObtenerSesionActualCasoUso.kt
 * @brief Caso de uso de consulta de la sesión activa del usuario.
 */
package com.gym.app.domain.usecase.autenticacion

import com.gym.app.domain.repository.RepositorioAutenticacion
import io.github.jan.supabase.gotrue.user.UserSession

/**
 * @class ObtenerSesionActualCasoUso
 * @brief Recupera la sesión activa del usuario autenticado, si existe.
 * La sesión se representa mediante [UserSession] de Supabase GoTrue.
 */
class ObtenerSesionActualCasoUso(
    private val repositorioAutenticacion: RepositorioAutenticacion
) {

    /**
     * @brief Obtiene la sesión actual del usuario.
     * @return [UserSession] activa, o null si no hay ninguna sesión iniciada.
     */
    fun ejecutar(): UserSession? = repositorioAutenticacion.obtenerSesionActual()
}