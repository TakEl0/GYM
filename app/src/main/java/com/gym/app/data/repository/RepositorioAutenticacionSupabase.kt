/**
 * @file RepositorioAutenticacionSupabase.kt
 * @brief Implementación real del repositorio de autenticación usando Supabase GoTrue y Room.
 */
package com.gym.app.data.repository

import android.content.Context
import com.gym.app.data.local.BaseDeDatosGYM
import com.gym.app.data.local.entidad.EntidadUsuarioPerfil
import com.gym.app.data.remote.ClienteSupabase
import com.gym.app.data.remote.dto.DtoPerfilRemoto
import com.gym.app.domain.model.EstadoSesion
import com.gym.app.domain.repository.RepositorioAutenticacion
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * @class RepositorioAutenticacionSupabase
 * @brief Gestiona el registro, login, estado de sesión y sincronización del perfil con Supabase.
 */
class RepositorioAutenticacionSupabase(private val context: Context) : RepositorioAutenticacion {

    private val db = BaseDeDatosGYM.obtenerInstancia(context)
    private val supabase = ClienteSupabase.inicializar(context)

    override suspend fun registrar(email: String, password: String, nombre: String): Result<Unit> {
        return try {
            val client = supabase ?: return Result.failure(IllegalStateException("Supabase no configurado"))
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            val userId = client.auth.currentSessionOrNull()?.user?.id ?: ""
            if (userId.isNotBlank()) {
                val perfilDto = DtoPerfilRemoto(id = userId, email = email, nombre = nombre)
                client.postgrest["perfiles"].insert(perfilDto)
                db.daoUsuarioPerfil().insertarPerfil(
                    EntidadUsuarioPerfil(id = userId, email = email, nombre = nombre, pesoObjetivoKg = null, createdAt = null, updatedAt = null)
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun iniciarSesion(email: String, password: String): Result<Unit> {
        return try {
            val client = supabase ?: return Result.failure(IllegalStateException("Supabase no configurado"))
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            sincronizarPerfilLocal()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cerrarSesion() {
        try {
            supabase?.auth?.signOut()
        } catch (_: Exception) {}
    }

    override fun observarEstadoSesion(): Flow<EstadoSesion> {
        val client = supabase ?: return flowOf(EstadoSesion.CONFIGURACION_PENDIENTE)
        return client.auth.sessionStatus.map { status ->
            when (status) {
                is io.github.jan.supabase.gotrue.SessionStatus.Authenticated -> EstadoSesion.AUTENTICADO
                is io.github.jan.supabase.gotrue.SessionStatus.LoadingFromStorage -> EstadoSesion.CARGANDO
                is io.github.jan.supabase.gotrue.SessionStatus.NetworkError -> EstadoSesion.NO_AUTENTICADO
                is io.github.jan.supabase.gotrue.SessionStatus.NotAuthenticated -> EstadoSesion.NO_AUTENTICADO
            }
        }
    }

    override fun obtenerSesionActual(): UserSession? {
        return try {
            supabase?.auth?.currentSessionOrNull()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun sincronizarPerfilLocal(): Result<Unit> {
        return try {
            val client = supabase ?: return Result.failure(IllegalStateException("Supabase no configurado"))
            val userId = client.auth.currentSessionOrNull()?.user?.id ?: return Result.failure(IllegalStateException("Sin sesión"))
            val perfilRemoto = client.postgrest["perfiles"].select {
                filter { eq("id", userId) }
            }.decodeSingle<DtoPerfilRemoto>()

            db.daoUsuarioPerfil().insertarPerfil(
                EntidadUsuarioPerfil(
                    id = perfilRemoto.id,
                    email = perfilRemoto.email,
                    nombre = perfilRemoto.nombre,
                    pesoObjetivoKg = perfilRemoto.pesoObjetivoKg,
                    createdAt = perfilRemoto.createdAt,
                    updatedAt = perfilRemoto.updatedAt
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
