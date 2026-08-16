/**
 * @file RepositorioComidaSupabase.kt
 * @brief Implementación real del repositorio de comidas y nutrición usando Room y Supabase.
 */
package com.gym.app.data.repository

import android.content.Context
import com.gym.app.data.local.BaseDeDatosGYM
import com.gym.app.data.local.entidad.EntidadComida
import com.gym.app.data.mapper.aDominio
import com.gym.app.data.mapper.aEntidad
import com.gym.app.data.remote.ClienteSupabase
import com.gym.app.data.remote.dto.DtoComidaRemoto
import com.gym.app.domain.model.Comida
import com.gym.app.domain.repository.RepositorioComida
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

/**
 * @class RepositorioComidaSupabase
 * @brief Administra la persistencia de comidas del día y su sincronización con Supabase.
 */
class RepositorioComidaSupabase(private val context: Context) : RepositorioComida {

    private val db = BaseDeDatosGYM.obtenerInstancia(context)
    private val supabase = ClienteSupabase.inicializar(context)

    private fun obtenerUserIdActual(): String {
        return try {
            supabase?.auth?.currentSessionOrNull()?.user?.id ?: "local_user"
        } catch (_: Exception) {
            "local_user"
        }
    }

    override fun observarComidasPorFecha(fecha: LocalDate): Flow<List<Comida>> {
        val userId = obtenerUserIdActual()
        val inicioDia = fecha.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val finDia = fecha.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        return db.daoComida().observarComidasPorFecha(userId, inicioDia, finDia)
            .map { lista -> lista.map { it.aDominio() } }
    }

    override suspend fun guardarComida(comida: Comida) {
        val userId = obtenerUserIdActual()
        val entidad = comida.aEntidad(userId)
        db.daoComida().insertar(entidad)
        intentarSubirRemoto(entidad)
    }

    override suspend fun eliminarComida(id: String) {
        db.daoComida().eliminarPorId(id)
        try {
            val client = supabase ?: return
            client.postgrest["comidas"].delete {
                filter { eq("id", id) }
            }
        } catch (_: Exception) {}
    }

    override suspend fun sincronizarConRemoto(): Result<Unit> {
        return try {
            val client = supabase ?: return Result.failure(IllegalStateException("Supabase no configurado"))
            val userId = obtenerUserIdActual()
            val remotos = client.postgrest["comidas"].select {
                filter { eq("user_id", userId) }
            }.decodeList<DtoComidaRemoto>()

            for (r in remotos) {
                db.daoComida().insertar(r.aEntidad(sincronizado = true))
            }

            val pendientes = db.daoComida().obtenerPendientesSincronizar()
            for (p in pendientes) {
                val dto = DtoComidaRemoto(
                    id = p.id,
                    userId = p.userId,
                    nombre = p.nombre,
                    kcal = p.kcal,
                    proteinasG = p.proteinasG,
                    carbohidratosG = p.carbohidratosG,
                    grasasG = p.grasasG,
                    tipoIngesta = p.tipoIngesta,
                    fecha = p.fecha
                )
                client.postgrest["comidas"].upsert(dto)
                db.daoComida().marcarSincronizado(p.id)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun intentarSubirRemoto(entidad: EntidadComida) {
        try {
            val client = supabase ?: return
            val dto = DtoComidaRemoto(
                id = entidad.id,
                userId = entidad.userId,
                nombre = entidad.nombre,
                kcal = entidad.kcal,
                proteinasG = entidad.proteinasG,
                carbohidratosG = entidad.carbohidratosG,
                grasasG = entidad.grasasG,
                tipoIngesta = entidad.tipoIngesta,
                fecha = entidad.fecha
            )
            client.postgrest["comidas"].upsert(dto)
            db.daoComida().marcarSincronizado(entidad.id)
        } catch (_: Exception) {}
    }
}
