/**
 * @file RegistrarUsuarioCasoUso.kt
 * @brief Caso de uso de registro de un nuevo usuario en la aplicación GYM.
 */
package com.gym.app.domain.usecase.autenticacion

import com.gym.app.domain.repository.RepositorioAutenticacion
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @class RegistrarUsuarioCasoUso
 * @brief Orquesta el alta de un nuevo usuario validando los datos de entrada
 * antes de delegar en el [RepositorioAutenticacion].
 *
 * Sigue el principio de responsabilidad única: únicamente valida las credenciales
 * y delega la persistencia remota, sin conocer detalles de Supabase ni de Room.
 */
class RegistrarUsuarioCasoUso(
    private val repositorioAutenticacion: RepositorioAutenticacion,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * @brief Patrón regex estándar para validar direcciones de correo electrónico.
     */
    private val patronEmail = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    /**
     * @brief Registra un nuevo usuario con correo, contraseña y nombre.
     * Valida el formato del correo, la longitud mínima de la contraseña y que el
     * nombre no esté vacío. Si la validación falla, devuelve un [Result.failure]
     * con [IllegalArgumentException]; en caso contrario delega en el repositorio.
     * @param email Correo electrónico del usuario.
     * @param password Contraseña de acceso (mínimo 8 caracteres).
     * @param nombre Nombre completo del usuario.
     * @return [Result] con éxito (Unit) o con el error producido.
     */
    suspend fun ejecutar(email: String, password: String, nombre: String): Result<Unit> =
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
            if (nombre.isBlank()) {
                return@withContext Result.failure(
                    IllegalArgumentException("El nombre no puede estar vacío.")
                )
            }
            repositorioAutenticacion.registrar(email, password, nombre)
        }
}