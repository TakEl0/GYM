package com.gym.app.domain.usecase.comunidad

import com.gym.app.domain.model.Publicacion
import com.gym.app.domain.repository.RepositorioComunidad
import kotlinx.coroutines.flow.Flow

class ObservarPublicacionesCasoUso(private val repositorio: RepositorioComunidad) {
    operator fun invoke(): Flow<List<Publicacion>> = repositorio.observarPublicaciones()
}
