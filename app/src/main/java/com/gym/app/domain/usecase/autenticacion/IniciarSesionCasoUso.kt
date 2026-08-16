/**
 * @file IniciarSesionCasoUso.kt
 * @brief Caso de uso de inicio de sesión de un usuario existente en la app GYM.
 */
package com.gym.app.domain.usecase.autenticacion

import com.gym.app.domain.repository.RepositorioAutenticacion
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @class IniciarSesionCasoUso
 * @brief Orquesta la autenticación del usuario validando las credenciales de
 * entrada y delegando el acceso real en el [RepositorioAutenticacion].
 */
class IniciarSesionCasoUso(
    private val repositorioAutenticacion: RepositorioAutenticacion,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Patrón regex estándar para validar direcciones de correo electrónico.
     */
    private val patronEmail = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    /**
     * @brief Inicia sesión con correo y contraseña.
     * Valida el formato del correo y la longitud mínima de la contraseña antes de
     * delegar en el repositorio. Si la validación falla, devuelve un [Result.failure]
     * con [IllegalArgumentException].
     * @param email Correo electrónico del usuario.
     * @param password Contraseña de acceso (mínimo 8 caracteres).
     * @return [Result] con éxito (Unit) o con el error producido.
     */
    suspend fun ejecutar(email: String, password: String): Result<Unit> =
        withContext(dispatcher) {
            if (!patronEmail.matches(email)) {
                return@withContext Result.failure(
                    IllegalArgumentException("El correo electrónico no tiene un formato válido.")
                )
            }
            if (password.length < 8) {
                return@withContext Result.failure(
                    IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.")
                )
            }
            repositorioAutenticacion.iniciarSesion(email, password)
        }
}