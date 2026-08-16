/**
 * @file RepositorioAutenticacion.kt
 * @brief Puerto de repositorio de autenticación y perfil de usuario en la capa de dominio.
 */
package com.gym.app.domain.repository

import com.gym.app.domain.model.EstadoSesion
import kotlinx.coroutines.flow.Flow
import io.github.jan.supabase.gotrue.user.UserSession

/**
 * @interface RepositorioAutenticacion
 * @brief Contrato para gestionar el registro, inicio de sesión, cierre de sesión y perfil del usuario.
 */
interface RepositorioAutenticacion {

    /**
     * @brief Registra un nuevo usuario con correo, contraseña y nombre.
     * @param email Correo electrónico del usuario.
     * @param password Contraseña de acceso.
     * @param nombre Nombre del usuario.
     * @return Result con éxito o error en el registro.
     */
    suspend fun registrar(email: String, password: String, nombre: String): Result<Unit>

    /**
     * @brief Inicia sesión con correo y contraseña.
     * @param email Correo electrónico.
     * @param password Contraseña.
     * @return Result con éxito o error en el inicio de sesión.
     */
    suspend fun iniciarSesion(email: String, password: String): Result<Unit>

    /**
     * @brief Cierra la sesión actual y limpia las credenciales almacenadas.
     */
    suspend fun cerrarSesion()

    /**
     * @brief Observa el estado de autenticación en tiempo real.
     * @return Flow con el [EstadoSesion] actual.
     */
    fun observarEstadoSesion(): Flow<EstadoSesion>

    /**
     * @brief Obtiene la sesión actual del usuario, si existe.
     * @return [UserSession] activa o null.
     */
    fun obtenerSesionActual(): UserSession?

    /**
     * @brief Sincroniza el perfil del usuario autenticado desde Supabase hacia la base de datos local Room.
     * @return Result con éxito o error en la sincronización.
     */
    suspend fun sincronizarPerfilLocal(): Result<Unit>
}
