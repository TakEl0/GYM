/**
 * @file RepositorioPesoFakeTest.kt
 * @brief Pruebas unitarias del repositorio simulado de peso.
 */
package com.gym.app.data.repository

import com.gym.app.domain.model.RegistroPeso
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * @class RepositorioPesoFakeTest
 * @brief Verifica el historial, el guardado, la consulta reactiva y el último registro.
 */
class RepositorioPesoFakeTest {

    @Test
    fun `obtenerHistorial devuelve los registros ordenados de mas reciente a mas antiguo`() = runTest {
        val repositorio = RepositorioPesoFake()

        val historial = repositorio.obtenerHistorial()

        assertTrue(historial.size >= 3)
        assertTrue(historial.zipWithNext().all { (a, b) -> !a.fecha.isBefore(b.fecha) })
    }

    @Test
    fun `guardarRegistro agrega un nuevo registro al historial`() = runTest {
        val repositorio = RepositorioPesoFake()
        val nuevo = RegistroPeso(
            fecha = LocalDate.now(),
            pesoKg = 78.0,
            grasaCorporalPorcentaje = 17.0
        )

        repositorio.guardarRegistro(nuevo)

        val ultimo = repositorio.obtenerUltimoRegistro()
        assertNotNull(ultimo)
        assertEquals(78.0, ultimo?.pesoKg ?: 0.0, 0.001)
    }

    @Test
    fun `obtenerPesoEnFecha devuelve el registro de la fecha indicada`() = runTest {
        val repositorio = RepositorioPesoFake()
        val fechaBuscada = LocalDate.now().minusDays(7)

        val registro = repositorio.obtenerPesoEnFecha(fechaBuscada)

        assertNotNull(registro)
        assertEquals(fechaBuscada, registro?.fecha)
    }

    @Test
    fun `obtenerPesoEnFecha devuelve null para fechas sin registro`() = runTest {
        val repositorio = RepositorioPesoFake()

        val registro = repositorio.obtenerPesoEnFecha(LocalDate.of(2000, 1, 1))

        assertEquals(null, registro)
    }

    @Test
    fun `observarPesos emite el historial ordenado`() = runTest {
        val repositorio = RepositorioPesoFake()

        val lista = repositorio.observarPesos("usuario-1").first()

        assertTrue(lista.isNotEmpty())
        assertTrue(lista.zipWithNext().all { (a, b) -> !a.fecha.isBefore(b.fecha) })
    }
}