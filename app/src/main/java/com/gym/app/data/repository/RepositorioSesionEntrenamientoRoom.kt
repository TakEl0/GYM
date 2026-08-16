/**
 * @file RepositorioSesionEntrenamientoRoom.kt
 * @brief Implementación del repositorio de sesiones con Room offline-first y sync Supabase.
 */
package com.gym.app.data.repository

import android.content.Context
import com.gym.app.data.local.BaseDeDatosGYM
import com.gym.app.data.mapper.aDominio
import com.gym.app.data.mapper.aEntidad
import com.gym.app.data.remote.ClienteSupabase
import com.gym.app.data.remote.dto.DtoEntrenamientoRemoto
import com.gym.app.domain.model.SesionEntrenamiento
import com.gym.app.domain.repository.RepositorioSesionEntrenamiento
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

/**
 * @class RepositorioSesionEntrenamientoRoom
 * @brief Administra las sesiones de entrenamiento con patrón offline-first:
 * Room como fuente primaria y sincronización con la tabla remota `entrenamientos`.
 */
class RepositorioSesionEntrenamientoRoom(private val context: Context) : RepositorioSesionEntrenamiento {

    private val db = BaseDeDatosGYM.obtenerInstancia(context)
    private val supabase = ClienteSupabase.inicializar(context)

    private fun obtenerUserIdActual(): String {
        return try {
            supabase?.auth?.currentSessionOrNull()?.user?.id ?: "local_user"
        } catch (_: Exception) {
            "local_user"
        }
    }

    override fun observarSesiones(inicio: LocalDate, fin: LocalDate): Flow<List<SesionEntrenamiento>> {
        val userId = obtenerUserIdActual()
        val inicioEpoch = inicio.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val finEpoch = fin.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return db.daoSesionEntrenamiento().observarSesiones(userId, inicioEpoch, finEpoch)
            .map { lista -> lista.map { it.aDominio() } }
    }

    override suspend fun guardarSesion(sesion: SesionEntrenamiento) {
        val entidad = sesion.aEntidad()
        db.daoSesionEntrenamiento().insertar(entidad)
        intentarSubirRemoto(entidad)
    }

    override suspend fun sesionesCompletadasSemana(fecha: LocalDate): Int {
        val userId = obtenerUserIdActual()
        val inicioSemana = fecha.minusDays((fecha.dayOfWeek.value - 1).toLong())
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val finSemana = fecha.plusDays((7 - fecha.dayOfWeek.value).toLong())
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return db.daoSesionEntrenamiento().contarCompletadasSemana(userId, inicioSemana, finSemana)
    }

    private suspend fun intentarSubirRemoto(entidad: com.gym.app.data.local.entidad.EntidadSesionEntrenamiento) {
        try {
            val client = supabase ?: return
            val dto = DtoEntrenamientoRemoto(
                id = entidad.id,
                userId = entidad.userId,
                nombre = entidad.nombreRutina,
                grupoMuscular = emptyList(),
                seriesTotales = entidad.serieRealizadas,
                ejerciciosRealizados = entidad.ejerciciosCompletados.split(",").size,
                totalEjercicios = entidad.ejerciciosCompletados.split(",").size,
                duracionMinutos = entidad.duracionMinutos,
                completo = entidad.completo,
                fecha = entidad.fecha
            )
            client.postgrest["entrenamientos"].upsert(dto)
            db.daoSesionEntrenamiento().marcarSincronizado(entidad.id)
        } catch (_: Exception) {}
    }
}