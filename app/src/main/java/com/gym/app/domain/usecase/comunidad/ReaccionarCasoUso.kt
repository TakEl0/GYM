package com.gym.app.domain.usecase.comunidad

import com.gym.app.domain.model.Reaccion
import com.gym.app.domain.repository.RepositorioComunidad

class ReaccionarCasoUso(private val repositorio: RepositorioComunidad) {
    suspend operator fun invoke(reaccion: Reaccion): Result<Unit> = repositorio.reaccionar(reaccion)
}
