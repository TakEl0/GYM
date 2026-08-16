package com.gym.app.domain.usecase.comunidad

import com.gym.app.domain.model.EventoCalendario
import com.gym.app.domain.repository.RepositorioComunidad

class CrearEventoCalendarioCasoUso(private val repositorio: RepositorioComunidad) {
    suspend operator fun invoke(evento: EventoCalendario): Result<Unit> = repositorio.crearEventoCalendario(evento)
}
