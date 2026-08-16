/**
 * @file MarcarItemCompradoCasoUsoTest.kt
 * @brief Pruebas unitarias del marcado de ítems de la lista de la compra.
 */
package com.gym.app.domain.usecase.compra

import com.gym.app.domain.repository.RepositorioListaCompra
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class MarcarItemCompradoCasoUsoTest
 * @brief Verifica la delegación del marcado de un ítem como comprado en el
 * [RepositorioListaCompra] y la propagación de errores de persistencia.
 */
class MarcarItemCompradoCasoUsoTest {

    private val repositorio = mockk<RepositorioListaCompra>(relaxed = true)

    private val casoUso = MarcarItemCompradoCasoUso(
        repositorioListaCompra = repositorio,
        dispatcher = Dispatchers.Unconfined
    )

    @Test
    fun `marcar item como comprado delega en el repositorio y tiene exito`() = runTest {
        val resultado = casoUso.ejecutar(
            listaId = "lista-1",
            itemId = "item-1",
            comprado = true
        )

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) {
            repositorio.marcarItemComprado("lista-1", "item-1", true)
        }
    }

    @Test
    fun `desmarcar item como comprado delega con comprado falso`() = runTest {
        val resultado = casoUso.ejecutar(
            listaId = "lista-1",
            itemId = "item-1",
            comprado = false
        )

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) {
            repositorio.marcarItemComprado("lista-1", "item-1", false)
        }
    }

    @Test
    fun `si el repositorio falla se devuelve el error de persistencia`() = runTest {
        coEvery { repositorio.marcarItemComprado(any(), any(), any()) } throws
            IllegalStateException("Error de base de datos")

        val resultado = casoUso.ejecutar("lista-1", "item-1", true)

        assertTrue(resultado.isFailure)
        assertEquals("Error de base de datos", resultado.exceptionOrNull()?.message)
    }
}