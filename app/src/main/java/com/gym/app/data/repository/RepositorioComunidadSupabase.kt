package com.gym.app.data.repository

import android.content.Context
import com.gym.app.data.remote.ClienteSupabase
import com.gym.app.data.remote.dto.DtoEventoCalendarioRemoto
import com.gym.app.data.remote.dto.DtoPublicacionRemoto
import com.gym.app.data.remote.dto.DtoReaccionRemoto
import com.gym.app.domain.model.EventoCalendario
import com.gym.app.domain.model.Publicacion
import com.gym.app.domain.model.Reaccion
import com.gym.app.domain.repository.RepositorioComunidad
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RepositorioComunidadSupabase(private val context: Context) : RepositorioComunidad {

    private val supabase = ClienteSupabase.inicializar(context)

    override fun observarPublicaciones(): Flow<List<Publicacion>> = flow {
        try {
            val client = supabase
            if (client == null) {
                emit(emptyList())
                return@flow
            }
            val pubs = client.postgrest["publicaciones"].select().decodeList<DtoPublicacionRemoto>()
            val reacs = client.postgrest["reacciones"].select().decodeList<DtoReaccionRemoto>()

            val listaReacciones = reacs.map { r ->
                Reaccion(
                    id = r.id,
                    publicacionId = r.publicacionId,
                    userId = r.userId,
                    tipoReaccion = r.tipoReaccion
                )
            }

            val listaPubs = pubs.map { p ->
                Publicacion(
                    id = p.id,
                    userId = p.userId,
                    autorNombre = p.autorNombre,
                    contenido = p.contenido,
                    urlImagen = p.urlImagen,
                    tipo = p.tipo,
                    fecha = p.fecha,
                    reacciones = listaReacciones.filter { it.publicacionId == p.id }
                )
            }.sortedByDescending { it.fecha }

            emit(listaPubs)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun crearPublicacion(publicacion: Publicacion): Result<Unit> = try {
        val client = supabase ?: throw IllegalStateException("Supabase no inicializado")
        val dto = DtoPublicacionRemoto(
            id = publicacion.id,
            userId = publicacion.userId,
            autorNombre = publicacion.autorNombre,
            contenido = publicacion.contenido,
            urlImagen = publicacion.urlImagen,
            tipo = publicacion.tipo,
            fecha = publicacion.fecha
        )
        client.postgrest["publicaciones"].insert(dto)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun eliminarPublicacion(id: String): Result<Unit> = try {
        val client = supabase ?: throw IllegalStateException("Supabase no inicializado")
        client.postgrest["publicaciones"].delete {
            filter { eq("id", id) }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun reaccionar(reaccion: Reaccion): Result<Unit> = try {
        val client = supabase ?: throw IllegalStateException("Supabase no inicializado")
        val dto = DtoReaccionRemoto(
            id = reaccion.id,
            publicacionId = reaccion.publicacionId,
            userId = reaccion.userId,
            tipoReaccion = reaccion.tipoReaccion
        )
        client.postgrest["reacciones"].upsert(dto)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun quitarReaccion(publicacionId: String, userId: String): Result<Unit> = try {
        val client = supabase ?: throw IllegalStateException("Supabase no inicializado")
        client.postgrest["reacciones"].delete {
            filter {
                eq("publicacion_id", publicacionId)
                eq("user_id", userId)
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun observarEventosCalendario(): Flow<List<EventoCalendario>> = flow {
        try {
            val client = supabase
            if (client == null) {
                emit(emptyList())
                return@flow
            }
            val eventos = client.postgrest["calendario_eventos"].select().decodeList<DtoEventoCalendarioRemoto>()
            val lista = eventos.map { e ->
                EventoCalendario(
                    id = e.id,
                    userId = e.userId,
                    titulo = e.titulo,
                    descripcion = e.descripcion,
                    fechaInicio = e.fechaInicio,
                    fechaFin = e.fechaFin,
                    tipo = e.tipo
                )
            }.sortedBy { it.fechaInicio }
            emit(lista)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun crearEventoCalendario(evento: EventoCalendario): Result<Unit> = try {
        val client = supabase ?: throw IllegalStateException("Supabase no inicializado")
        val dto = DtoEventoCalendarioRemoto(
            id = evento.id,
            userId = evento.userId,
            titulo = evento.titulo,
            descripcion = evento.descripcion,
            fechaInicio = evento.fechaInicio,
            fechaFin = evento.fechaFin,
            tipo = evento.tipo
        )
        client.postgrest["calendario_eventos"].insert(dto)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun eliminarEventoCalendario(id: String): Result<Unit> = try {
        val client = supabase ?: throw IllegalStateException("Supabase no inicializado")
        client.postgrest["calendario_eventos"].delete {
            filter { eq("id", id) }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
