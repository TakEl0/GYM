/**
 * @file RepositorioPesoSupabase.kt
 * @brief Implementación real del repositorio de peso corporal usando Room y Supabase.
 */
package com.gym.app.data.repository

import android.content.Context
import com.gym.app.data.local.BaseDeDatosGYM
import com.gym.app.data.local.entidad.EntidadRegistroPeso
import com.gym.app.data.mapper.aDominio
import com.gym.app.data.mapper.aEntidad
import com.gym.app.data.remote.ClienteSupabase
import com.gym.app.data.remote.dto.DtoRegistroPesoRemoto
import com.gym.app.domain.model.RegistroPeso
import com.gym.app.domain.repository.RepositorioPeso
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

/**
 * @class RepositorioPesoSupabase
 * @brief Administra la persistencia local en Room y la sincronización con Supabase para el peso corporal.
 */
class RepositorioPesoSupabase(private val context: Context) : RepositorioPeso {

    private val db = BaseDeDatosGYM.obtenerInstancia(context)
    private val supabase = ClienteSupabase.inicializar(context)

    private fun obtenerUserIdActual(): String {
        return try {
            supabase?.auth?.currentSessionOrNull()?.user?.id ?: "local_user"
        } catch (_: Exception) {
            "local_user"
        }
    }

    override fun observarPesos(userId: String): Flow<List<RegistroPeso>> =
        db.daoRegistroPeso().observarPorUsuario(userId).map { lista ->
            lista.map { it.aDominio() }
        }

    override suspend fun obtenerHistorial(): List<RegistroPeso> {
        val userId = obtenerUserIdActual()
        sincronizarConRemoto()
        return db.daoRegistroPeso().obtenerPorUsuarioSync(userId).map { it.aDominio() }
    }

    override suspend fun guardarRegistro(registro: RegistroPeso) {
        val userId = obtenerUserIdActual()
        val entidad = registro.aEntidad(userId)
        db.daoRegistroPeso().insertar(entidad)
        intentarSubirRemoto(entidad)
    }

    override suspend fun obtenerUltimoRegistro(): RegistroPeso? {
        val userId = obtenerUserIdActual()
        return db.daoRegistroPeso().obtenerUltimo(userId)?.aDominio()
    }

    override suspend fun obtenerPesoEnFecha(fecha: LocalDate): RegistroPeso? {
        val userId = obtenerUserIdActual()
        val epoch = fecha.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return db.daoRegistroPeso().obtenerEnFecha(userId, epoch)?.aDominio()
    }

    private suspend fun intentarSubirRemoto(entidad: EntidadRegistroPeso) {
        try {
            val client = supabase ?: return
            val dto = DtoRegistroPesoRemoto(
                id = entidad.id,
                userId = entidad.userId,
                pesoKg = entidad.pesoKg,
                grasaCorporal = entidad.grasaCorporal,
                fecha = entidad.fecha
            )
            client.postgrest["registros_peso"].upsert(dto)
            db.daoRegistroPeso().marcarSincronizado(entidad.id)
        } catch (_: Exception) {}
    }

    /**
     * @brief Sincroniza bidireccionalmente los registros de peso con Supabase.
     */
    suspend fun sincronizarConRemoto() {
        try {
            val client = supabase ?: return
            val userId = obtenerUserIdActual()
            val remotos = client.postgrest["registros_peso"].select {
                filter { eq("user_id", userId) }
            }.decodeList<DtoRegistroPesoRemoto>()

            for (r in remotos) {
                db.daoRegistroPeso().insertar(r.aEntidad(sincronizado = true))
            }

            val pendientes = db.daoRegistroPeso().obtenerPendientesSincronizar()
            for (p in pendientes) {
                intentarSubirRemoto(p)
            }
        } catch (_: Exception) {}
    }
}
