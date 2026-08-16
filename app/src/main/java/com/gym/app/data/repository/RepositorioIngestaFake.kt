/**
 * @file RepositorioIngestaFake.kt
 * @brief Implementación simulada del repositorio de ingestas registradas.
 */
package com.gym.app.data.repository

import com.gym.app.domain.model.IngestaRegistrada
import com.gym.app.domain.repository.RepositorioIngesta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * @class RepositorioIngestaFake
 * @brief Repositorio de ingestas en memoria para desarrollo y tests.
 */
class RepositorioIngestaFake : RepositorioIngesta {

    private val ingestas = MutableStateFlow<List<IngestaRegistrada>>(emptyList())

    override fun observarIngestasDelDia(fecha: LocalDate): Flow<List<IngestaRegistrada>> =
        ingestas.map { lista -> lista.filter { it.fecha == fecha } }

    override suspend fun registrarIngesta(ingesta: IngestaRegistrada) {
        ingestas.value = ingestas.value + ingesta
    }

    override suspend fun eliminarIngesta(id: String) {
        ingestas.value = ingestas.value.filterNot { it.id == id }
    }

    override suspend fun sincronizarConRemoto(): Result<Unit> = Result.success(Unit)
}