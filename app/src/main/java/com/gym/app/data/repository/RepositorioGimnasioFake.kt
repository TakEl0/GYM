/**
 * @file RepositorioGimnasioFake.kt
 * @brief Implementación simulada del repositorio de gimnasio.
 */
package com.gym.app.data.repository

import com.gym.app.domain.model.Gimnasio
import com.gym.app.domain.repository.RepositorioGimnasio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @class RepositorioGimnasioFake
 * @brief Repositorio de gimnasio en memoria para desarrollo y tests.
 */
class RepositorioGimnasioFake : RepositorioGimnasio {

    private val gimnasio = MutableStateFlow<Gimnasio?>(null)

    override fun observarGimnasio(): Flow<Gimnasio?> = gimnasio

    override suspend fun guardarGimnasio(gimnasio: Gimnasio) {
        this.gimnasio.value = gimnasio
    }

    override suspend fun actualizarDisponibilidadMaquina(maquinaId: String, disponible: Boolean) {
        gimnasio.value = gimnasio.value?.copy(
            maquinas = gimnasio.value!!.maquinas.map {
                if (it.id == maquinaId) it.copy(disponible = disponible) else it
            }
        )
    }
}