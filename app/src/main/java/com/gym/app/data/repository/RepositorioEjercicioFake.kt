/**
 * @file RepositorioEjercicioFake.kt
 * @brief Implementación simulada del repositorio de ejercicios.
 */
package com.gym.app.data.repository

import com.gym.app.domain.model.Ejercicio
import com.gym.app.domain.repository.RepositorioEjercicio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @class RepositorioEjercicioFake
 * @brief Repositorio de ejercicios en memoria para desarrollo y tests.
 */
class RepositorioEjercicioFake : RepositorioEjercicio {

    private val ejercicios = MutableStateFlow<List<Ejercicio>>(emptyList())

    override fun observarEjercicios(): Flow<List<Ejercicio>> = ejercicios

    override suspend fun guardarEjercicio(ejercicio: Ejercicio) {
        ejercicios.value = ejercicios.value.filterNot { it.id == ejercicio.id } + ejercicio
    }

    override suspend fun guardarVarios(ejercicios: List<Ejercicio>) {
        this.ejercicios.value = ejercicios
    }
}