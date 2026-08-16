/**
 * @file RepositorioIngestaRoom.kt
 * @brief Implementación del repositorio de ingestas con Room offline-first y sync Supabase.
 */
package com.gym.app.data.repository

import android.content.Context
import com.gym.app.data.local.BaseDeDatosGYM
import com.gym.app.data.mapper.aDominio
import com.gym.app.data.mapper.aEntidad
import com.gym.app.data.remote.ClienteSupabase
import com.gym.app.data.remote.dto.DtoComidaRemoto
import com.gym.app.domain.model.IngestaRegistrada
import com.gym.app.domain.repository.RepositorioIngesta
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

/**
 * @class RepositorioIngestaRoom
 * @brief Administra las comidas realmente consumidas con patrón offline-first:
 * Room como fuente primaria y sincronización con la tabla remota `comidas`.
 */
class RepositorioIngestaRoom(private val context: Context) : RepositorioIngesta {

    private val db = BaseDeDatosGYM.obtenerInstancia(context)
    private val supabase = ClienteSupabase.inicializar(context)

    private fun obtenerUserIdActual(): String {
        return try {
            supabase?.auth?.currentSessionOrNull()?.user?.id ?: "local_user"
        } catch (_: Exception) {
            "local_user"
        }
    }

    override fun observarIngestasDelDia(fecha: LocalDate): Flow<List<IngestaRegistrada>> {
        val userId = obtenerUserIdActual()
        val inicioDia = fecha.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val finDia = fecha.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        return db.daoIngesta().observarIngestasDelDia(userId, inicioDia, finDia)
            .map { lista -> lista.map { it.aDominio() } }
    }

    override suspend fun registrarIngesta(ingesta: IngestaRegistrada) {
        val entidad = ingesta.aEntidad()
        db.daoIngesta().insertar(entidad)
        intentarSubirRemoto(entidad)
    }

    override suspend fun eliminarIngesta(id: String) {
        db.daoIngesta().eliminarPorId(id)
        try {
            val client = supabase ?: return
            client.postgrest["comidas"].delete { filter { eq("id", id) } }
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
                db.daoIngesta().insertar(
                    com.gym.app.data.local.entidad.EntidadIngestaRegistrada(
                        id = r.id,
                        userId = r.userId,
                        nombre = r.nombre,
                        kcal = r.kcal.toDouble(),
                        proteinasG = r.proteinasG,
                        carbohidratosG = r.carbohidratosG,
                        grasasG = r.grasasG,
                        tipoIngesta = r.tipoIngesta,
                        fecha = r.fecha,
                        momentoDia = r.tipoIngesta,
                        origen = "IMPORTADA",
                        sincronizado = true
                    )
                )
            }

            val pendientes = db.daoIngesta().obtenerPendientesSincronizar()
            for (p in pendientes) {
                val dto = DtoComidaRemoto(
                    id = p.id,
                    userId = p.userId,
                    nombre = p.nombre,
                    kcal = p.kcal.toInt(),
                    proteinasG = p.proteinasG,
                    carbohidratosG = p.carbohidratosG,
                    grasasG = p.grasasG,
                    tipoIngesta = p.tipoIngesta,
                    fecha = p.fecha
                )
                client.postgrest["comidas"].upsert(dto)
                db.daoIngesta().marcarSincronizado(p.id)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun intentarSubirRemoto(entidad: com.gym.app.data.local.entidad.EntidadIngestaRegistrada) {
        try {
            val client = supabase ?: return
            val dto = DtoComidaRemoto(
                id = entidad.id,
                userId = entidad.userId,
                nombre = entidad.nombre,
                kcal = entidad.kcal.toInt(),
                proteinasG = entidad.proteinasG,
                carbohidratosG = entidad.carbohidratosG,
                grasasG = entidad.grasasG,
                tipoIngesta = entidad.tipoIngesta,
                fecha = entidad.fecha
            )
            client.postgrest["comidas"].upsert(dto)
            db.daoIngesta().marcarSincronizado(entidad.id)
        } catch (_: Exception) {}
    }
}