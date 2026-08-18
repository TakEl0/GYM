/**
 * @file RepositorioSerieRealizadaRoom.kt
 * @brief Implementación del repositorio de series realizadas con Room offline-first y sync Supabase.
 */
package com.gym.app.data.repository

import android.content.Context
import com.gym.app.data.local.BaseDeDatosGYM
import com.gym.app.data.mapper.aDominio
import com.gym.app.data.mapper.aDtoRemoto
import com.gym.app.data.mapper.aEntidad
import com.gym.app.data.remote.ClienteSupabase
import com.gym.app.domain.model.SerieRealizada
import com.gym.app.domain.repository.RepositorioSerieRealizada
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * @class RepositorioSerieRealizadaRoom
 * @brief Administra las series realizadas con patrón offline-first:
 * Room como fuente primaria y sincronización con la tabla remota `series_realizadas`.
 */
class RepositorioSerieRealizadaRoom(private val context: Context) : RepositorioSerieRealizada {

    private val db = BaseDeDatosGYM.obtenerInstancia(context)
    private val supabase = ClienteSupabase.inicializar(context)

    override fun observarPorSesion(sesionId: String): Flow<List<SerieRealizada>> {
        return db.daoSerieRealizada().observarPorSesion(sesionId)
            .map { lista -> lista.map { it.aDominio() } }
    }

    override suspend fun guardarSerie(serie: SerieRealizada) {
        val entidad = serie.aEntidad()
        db.daoSerieRealizada().insertar(entidad)
        intentarSubirRemoto(entidad)
    }

    override suspend fun eliminarSerie(id: String) {
        db.daoSerieRealizada().eliminar(id)
    }

    override suspend fun ultimoPesoPorEjercicio(ejercicioId: String): Double? {
        return db.daoSerieRealizada().ultimoPesoPorEjercicio(ejercicioId)
    }

    override suspend fun obtenerPorId(id: String): SerieRealizada? {
        return db.daoSerieRealizada().obtenerPorId(id)?.aDominio()
    }

    override suspend fun observarSeriesDeEjercicio(
        sesionId: String,
        ejercicioId: String
    ): List<SerieRealizada> {
        return observarPorSesion(sesionId)
            .first()
            .filter { it.ejercicioId == ejercicioId }
    }

    private suspend fun intentarSubirRemoto(entidad: com.gym.app.data.local.entidad.EntidadSerieRealizada) {
        try {
            val client = supabase ?: return
            val dto = entidad.aDtoRemoto()
            client.postgrest["series_realizadas"].upsert(dto)
            db.daoSerieRealizada().marcarSincronizado(entidad.id)
        } catch (_: Exception) {}
    }
}
