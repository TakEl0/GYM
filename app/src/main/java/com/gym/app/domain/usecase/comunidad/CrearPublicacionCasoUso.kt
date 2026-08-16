package com.gym.app.domain.usecase.comunidad

import com.gym.app.domain.model.Publicacion
import com.gym.app.domain.repository.RepositorioComunidad

class CrearPublicacionCasoUso(private val repositorio: RepositorioComunidad) {
    suspend operator fun invoke(publicacion: Publicacion): Result<Unit> = repositorio.crearPublicacion(publicacion)
}
