/**
 * @file RepositorioAutenticacionFake.kt
 * @brief Implementación simulada del repositorio de autenticación.
 * Permite validar el flujo de registro, inicio y cierre de sesión en memoria
 * cuando Supabase no está configurado (modo desarrollo/previsualización).
 */
package com.gym.app.data.repository

import com.gym.app.domain.model.EstadoSesion
import com.gym.app.domain.repository.RepositorioAutenticacion
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * @class RepositorioAutenticacionFake
 * @brief Implementación en memoria del repositorio de autenticación.
 * Mantiene un estado de sesión interno simulando el comportamiento de Supabase
 * GoTrue para poder desarrollar la interfaz sin dependencias externas.
 */
class RepositorioAutenticacionFake : RepositorioAutenticacion {

    private val _estadoSesion = MutableStateFlow(EstadoSesion.NO_AUTENTICADO)
    private var sesionActiva: UserSession? = null

    /**
     * @brief Construye una sesión simulada a partir del correo y el nombre.
     * @param email Correo electrónico del usuario simulado.
     * @param nombre Nombre del usuario simulado.
     * @param sufijoId Sufijo para diferenciar el identificador del registro frente al login.
     * @return [UserSession] simulada con un [UserInfo] y tokens ficticios.
     */
    private fun construirSesion(email: String, nombre: String, sufijoId: String): UserSession {
        val metadatos: JsonObject = buildJsonObject {
            if (nombre.isNotBlank()) put("nombre", nombre)
        }
        val usuario = UserInfo(
            aud = "authenticated",
            email = email,
            id = "usuario_simulado_$sufijoId",
            userMetadata = metadatos
        )
        return UserSession(
            accessToken = "token_simulado_$sufijoId",
            refreshToken = "refresh_simulado_$sufijoId",
            expiresIn = 3600L,
            tokenType = "bearer",
            user = usuario
        )
    }

    override suspend fun registrar(email: String, password: String, nombre: String): Result<Unit> {
        return try {
            sesionActiva = construirSesion(email, nombre, "registro")
            _estadoSesion.value = EstadoSesion.AUTENTICADO
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun iniciarSesion(email: String, password: String): Result<Unit> {
        return try {
            sesionActiva = construirSesion(email, "", "inicio")
            _estadoSesion.value = EstadoSesion.AUTENTICADO
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cerrarSesion() {
        sesionActiva = null
        _estadoSesion.value = EstadoSesion.NO_AUTENTICADO
    }

    override fun observarEstadoSesion(): Flow<EstadoSesion> = _estadoSesion.asStateFlow()

    override fun obtenerSesionActual(): UserSession? = sesionActiva

    override suspend fun sincronizarPerfilLocal(): Result<Unit> = Result.success(Unit)

    companion object {
        // Evita que el compilador elimine la referencia a Json (se mantiene por coherencia).
        @Suppress("unused")
        private val json: Json = Json
    }
}