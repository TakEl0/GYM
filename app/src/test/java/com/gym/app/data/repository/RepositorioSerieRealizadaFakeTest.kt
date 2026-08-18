/**
 * @file RepositorioSerieRealizadaFakeTest.kt
 * @brief Pruebas unitarias del repositorio simulado de series realizadas.
 */
package com.gym.app.data.repository

import com.gym.app.data.mapper.aDominio
import com.gym.app.data.mapper.aEntidad
import com.gym.app.domain.model.SerieRealizada
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class RepositorioSerieRealizadaFakeTest
 * @brief Verifica la persistencia, observación ordenada, eliminación, último peso y mappers de series realizadas.
 */
class RepositorioSerieRealizadaFakeTest {

    @Test
    fun `guardarSerie persiste y observa las series ordenadas por numeroSerie`() = runTest {
        val repo = RepositorioSerieRealizadaFake()
        val sesionId = "sesion-1"
        val serie2 = SerieRealizada("s2", sesionId, "ej-1", 2, 80.0, 10, 1000L)
        val serie1 = SerieRealizada("s1", sesionId, "ej-1", 1, 75.0, 12, 900L)

        repo.guardarSerie(serie2)
        repo.guardarSerie(serie1)

        val series = repo.observarPorSesion(sesionId).first()
        assertEquals(2, series.size)
        assertEquals(1, series[0].numeroSerie)
        assertEquals(2, series[1].numeroSerie)
    }

    @Test
    fun `eliminarSerie quita la serie del flujo`() = runTest {
        val repo = RepositorioSerieRealizadaFake()
        val sesionId = "sesion-1"
        val serie = SerieRealizada("s1", sesionId, "ej-1", 1, 70.0, 10, 1000L)

        repo.guardarSerie(serie)
        assertEquals(1, repo.observarPorSesion(sesionId).first().size)

        repo.eliminarSerie("s1")
        assertTrue(repo.observarPorSesion(sesionId).first().isEmpty())
    }

    @Test
    fun `ultimoPesoPorEjercicio devuelve el ultimo kg registrado`() = runTest {
        val repo = RepositorioSerieRealizadaFake()
        val ejercicioId = "ej-bench"
        val serieAntigua = SerieRealizada("s1", "sesion-1", ejercicioId, 1, 80.0, 8, 1000L)
        val serieReciente = SerieRealizada("s2", "sesion-2", ejercicioId, 1, 85.0, 6, 2000L)

        repo.guardarSerie(serieAntigua)
        repo.guardarSerie(serieReciente)

        val ultimoPeso = repo.ultimoPesoPorEjercicio(ejercicioId)
        assertNotNull(ultimoPeso)
        assertEquals(85.0, ultimoPeso!!, 0.001)
    }

    @Test
    fun `mappers round trip de SerieRealizada y EntidadSerieRealizada`() {
        val original = SerieRealizada(
            id = "id-123",
            sesionId = "ses-1",
            ejercicioId = "ej-1",
            numeroSerie = 3,
            pesoKg = 65.5,
            repeticiones = 10,
            fecha = 1700000000L
        )

        val entidad = original.aEntidad()
        val mapeada = entidad.aDominio()

        assertEquals(original, mapeada)
    }
}
