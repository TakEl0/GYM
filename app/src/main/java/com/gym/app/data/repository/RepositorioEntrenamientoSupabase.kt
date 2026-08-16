/**
 * @file RepositorioEntrenamientoSupabase.kt
 * @brief Implementación real del repositorio de entrenamientos usando Room y Supabase.
 */
package com.gym.app.data.repository

import android.content.Context
import com.gym.app.data.local.BaseDeDatosGYM
import com.gym.app.data.mapper.aDominio
import com.gym.app.data.mapper.aDtoRemoto
import com.gym.app.data.mapper.aEntidad
import com.gym.app.data.remote.ClienteSupabase
import com.gym.app.data.remote.dto.DtoEntrenamientoRemoto
import com.gym.app.domain.model.Entrenamiento
import com.gym.app.domain.repository.RepositorioEntrenamiento
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @class RepositorioEntrenamientoSupabase
 * @brief Administra las rutinas de entrenamiento mediante persistencia local Room y sincronización con Supabase.
 */
class RepositorioEntrenamientoSupabase(private val context: Context) : RepositorioEntrenamiento {

    private val db = BaseDeDatosGYM.obtenerInstancia(context)
    private val supabase = ClienteSupabase.inicializar(context)

    private fun obtenerUserIdActual(): String {
        return try {
            supabase?.auth?.currentSessionOrNull()?.user?.id ?: "local_user"
        } catch (_: Exception) {
            "local_user"
        }
    }

    override fun observarEntrenamientos(userId: String?): Flow<List<Entrenamiento>> =
        db.daoEntrenamiento().observarPorUsuario(userId ?: obtenerUserIdActual()).map { lista ->
            lista.map { it.aDominio() }
        }

    override suspend fun obtenerEntrenamientoDeHoy(): Entrenamiento? {
        val userId = obtenerUserIdActual()
        sincronizarConRemoto()
        val entidad = db.daoEntrenamiento().obtenerEntrenamientoDeHoy(userId)
        return entidad?.aDominio()
    }

    override fun observarEntrenamientosEntre(inicio: Long, fin: Long): Flow<List<Entrenamiento>> {
        val userId = obtenerUserIdActual()
        return db.daoEntrenamiento().observarEntreFechas(userId, inicio, fin).map { lista ->
            lista.map { it.aDominio() }
        }
    }

    override suspend fun obtenerEntrenamientoEnFecha(fecha: Long): Entrenamiento? {
        val userId = obtenerUserIdActual()
        sincronizarConRemoto()
        return db.daoEntrenamiento().obtenerEnFecha(userId, fecha)?.aDominio()
    }

    override suspend fun obtenerSesionesCompletadasSemana(): Int {
        val userId = obtenerUserIdActual()
        val lista = db.daoEntrenamiento().obtenerPorUsuarioSync(userId)
        return lista.count { it.completo }
    }

    override suspend fun obtenerTotalSesionesSemana(): Int {
        val userId = obtenerUserIdActual()
        val lista = db.daoEntrenamiento().obtenerPorUsuarioSync(userId)
        return lista.size.coerceAtLeast(1)
    }

    override suspend fun actualizarProgreso(entrenamientoId: String, ejerciciosRealizados: Int) {
        db.daoEntrenamiento().actualizarProgreso(entrenamientoId, ejerciciosRealizados)
        try {
            val client = supabase ?: return
            val entrenamiento = db.daoEntrenamiento().obtenerPorUsuarioSync(obtenerUserIdActual())
                .firstOrNull { it.id == entrenamientoId }
            val completo = entrenamiento != null && ejerciciosRealizados >= entrenamiento.totalEjercicios
            client.postgrest["entrenamientos"].update(
                mapOf(
                    "ejercicios_realizados" to ejerciciosRealizados,
                    "completo" to completo
                )
            ) {
                filter { eq("id", entrenamientoId) }
            }
            db.daoEntrenamiento().marcarSincronizado(entrenamientoId)
        } catch (_: Exception) {}
    }

    override suspend fun guardarEntrenamiento(entrenamiento: Entrenamiento) {
        val userId = obtenerUserIdActual()
        db.daoEntrenamiento().insertar(entrenamiento.aEntidad(userId = userId, sincronizado = false))
        try {
            val client = supabase ?: return
            val entidad = entrenamiento.aEntidad(userId = userId, sincronizado = true)
            client.postgrest["entrenamientos"].upsert(entidad.aDtoRemoto())
            db.daoEntrenamiento().marcarSincronizado(entrenamiento.id)
        } catch (_: Exception) {}
    }

    /**
     * @brief Sincroniza los entrenamientos remotos desde Supabase.
     */
    suspend fun sincronizarConRemoto() {
        try {
            val client = supabase ?: return
            val userId = obtenerUserIdActual()
            val remotos = client.postgrest["entrenamientos"].select {
                filter { eq("user_id", userId) }
            }.decodeList<DtoEntrenamientoRemoto>()

            for (r in remotos) {
                db.daoEntrenamiento().insertar(r.aEntidad(sincronizado = true))
            }
        } catch (_: Exception) {}
    }
}
