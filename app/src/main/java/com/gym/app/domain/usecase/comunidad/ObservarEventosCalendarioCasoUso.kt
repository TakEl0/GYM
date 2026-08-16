package com.gym.app.domain.usecase.comunidad

import com.gym.app.domain.model.EventoCalendario
import com.gym.app.domain.repository.RepositorioComunidad
import kotlinx.coroutines.flow.Flow

class ObservarEventosCalendarioCasoUso(private val repositorio: RepositorioComunidad) {
    operator fun invoke(): Flow<List<EventoCalendario>> = repositorio.observarEventosCalendario()
}
