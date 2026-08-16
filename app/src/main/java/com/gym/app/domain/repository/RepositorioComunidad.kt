package com.gym.app.domain.repository

import com.gym.app.domain.model.EventoCalendario
import com.gym.app.domain.model.Publicacion
import com.gym.app.domain.model.Reaccion
import kotlinx.coroutines.flow.Flow

interface RepositorioComunidad {
    fun observarPublicaciones(): Flow<List<Publicacion>>
    suspend fun crearPublicacion(publicacion: Publicacion): Result<Unit>
    suspend fun eliminarPublicacion(id: String): Result<Unit>
    suspend fun reaccionar(reaccion: Reaccion): Result<Unit>
    suspend fun quitarReaccion(publicacionId: String, userId: String): Result<Unit>

    fun observarEventosCalendario(): Flow<List<EventoCalendario>>
    suspend fun crearEventoCalendario(evento: EventoCalendario): Result<Unit>
    suspend fun eliminarEventoCalendario(id: String): Result<Unit>
}
