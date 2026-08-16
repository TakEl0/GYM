/**
 * @file RepositorioRutinaFake.kt
 * @brief Implementación simulada del repositorio de rutinas.
 */
package com.gym.app.data.repository

import com.gym.app.domain.model.Rutina
import com.gym.app.domain.repository.RepositorioRutina
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @class RepositorioRutinaFake
 * @brief Repositorio de rutinas en memoria para desarrollo y tests.
 */
class RepositorioRutinaFake : RepositorioRutina {

    private val rutinas = MutableStateFlow<List<Rutina>>(emptyList())

    override fun observarRutinas(): Flow<List<Rutina>> = rutinas

    override suspend fun guardarRutina(rutina: Rutina) {
        rutinas.value = rutinas.value.filterNot { it.id == rutina.id } + rutina
    }

    override suspend fun eliminarRutina(id: String) {
        rutinas.value = rutinas.value.filterNot { it.id == id }
    }
}