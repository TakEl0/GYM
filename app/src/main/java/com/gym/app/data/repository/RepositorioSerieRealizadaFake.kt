/**
 * @file RepositorioSerieRealizadaFake.kt
 * @brief Implementación simulada del repositorio de series realizadas.
 */
package com.gym.app.data.repository

import com.gym.app.domain.model.SerieRealizada
import com.gym.app.domain.repository.RepositorioSerieRealizada
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * @class RepositorioSerieRealizadaFake
 * @brief Repositorio de series realizadas en memoria para tests y desarrollo.
 */
class RepositorioSerieRealizadaFake : RepositorioSerieRealizada {

    private val series = MutableStateFlow<List<SerieRealizada>>(emptyList())

    override fun observarPorSesion(sesionId: String): Flow<List<SerieRealizada>> =
        series.map { lista ->
            lista.filter { it.sesionId == sesionId }
                .sortedBy { it.numeroSerie }
        }

    override suspend fun guardarSerie(serie: SerieRealizada) {
        val listaActual = series.value.toMutableList()
        val index = listaActual.indexOfFirst { it.id == serie.id }
        if (index >= 0) {
            listaActual[index] = serie
        } else {
            listaActual.add(serie)
        }
        series.value = listaActual
    }

    override suspend fun eliminarSerie(id: String) {
        series.value = series.value.filter { it.id != id }
    }

    override suspend fun ultimoPesoPorEjercicio(ejercicioId: String): Double? {
        return series.value
            .filter { it.ejercicioId == ejercicioId }
            .maxByOrNull { it.fecha }
            ?.pesoKg
    }

    override suspend fun obtenerPorId(id: String): SerieRealizada? {
        return series.value.firstOrNull { it.id == id }
    }

    override suspend fun observarSeriesDeEjercicio(
        sesionId: String,
        ejercicioId: String
    ): List<SerieRealizada> {
        return series.value
            .filter { it.sesionId == sesionId && it.ejercicioId == ejercicioId }
            .sortedBy { it.numeroSerie }
    }
}
