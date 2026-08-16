/**
 * @file RepositorioEjercicioRoom.kt
 * @brief Implementación del repositorio de ejercicios con Room local.
 */
package com.gym.app.data.repository

import android.content.Context
import com.gym.app.data.local.BaseDeDatosGYM
import com.gym.app.data.mapper.aDominio
import com.gym.app.data.mapper.aEntidad
import com.gym.app.domain.model.Ejercicio
import com.gym.app.domain.repository.RepositorioEjercicio
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @class RepositorioEjercicioRoom
 * @brief Administra el catálogo de ejercicios del usuario en Room local.
 */
class RepositorioEjercicioRoom(private val context: Context) : RepositorioEjercicio {

    private val db = BaseDeDatosGYM.obtenerInstancia(context)

    private fun obtenerUserIdActual(): String {
        return try {
            val supabase = com.gym.app.data.remote.ClienteSupabase.inicializar(context)
            supabase?.auth?.currentSessionOrNull()?.user?.id ?: "local_user"
        } catch (_: Exception) {
            "local_user"
        }
    }

    override fun observarEjercicios(): Flow<List<Ejercicio>> {
        val userId = obtenerUserIdActual()
        return db.daoEjercicio().observarEjercicios(userId).map { lista ->
            lista.map { it.aDominio() }
        }
    }

    override suspend fun guardarEjercicio(ejercicio: Ejercicio) {
        val userId = obtenerUserIdActual()
        db.daoEjercicio().insertar(ejercicio.aEntidad(userId))
    }

    override suspend fun guardarVarios(ejercicios: List<Ejercicio>) {
        val userId = obtenerUserIdActual()
        db.daoEjercicio().insertarVarios(ejercicios.map { it.aEntidad(userId) })
    }
}