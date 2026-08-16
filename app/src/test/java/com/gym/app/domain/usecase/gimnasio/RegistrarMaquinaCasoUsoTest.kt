/**
 * @file RegistrarMaquinaCasoUsoTest.kt
 * @brief Pruebas unitarias del alta o actualización de máquinas del gimnasio.
 */
package com.gym.app.domain.usecase.gimnasio

import com.gym.app.domain.model.Gimnasio
import com.gym.app.domain.model.Maquina
import com.gym.app.domain.repository.RepositorioGimnasio
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class RegistrarMaquinaCasoUsoTest
 * @brief Verifica la validación del nombre de la máquina, la adición de máquinas
 * nuevas y el reemplazo de máquinas existentes por su identificador.
 */
class RegistrarMaquinaCasoUsoTest {

    private val repositorio = mockk<RepositorioGimnasio>(relaxed = true)

    private val casoUso = RegistrarMaquinaCasoUso(
        repositorioGimnasio = repositorio,
        dispatcher = Dispatchers.Unconfined
    )

    private fun crearMaquina(id: String, nombre: String): Maquina = Maquina(
        id = id,
        nombre = nombre,
        grupoMuscular = listOf("CUADRICEPS"),
        disponible = true
    )

    private fun crearGimnasio(maquinas: List<Maquina> = emptyList()): Gimnasio = Gimnasio(
        id = "gimnasio-1",
        nombre = "Power House",
        maquinas = maquinas
    )

    @Test
    fun `registra una maquina nueva anadiendola al gimnasio`() = runTest {
        val gimnasio = crearGimnasio()
        val maquina = crearMaquina("m-1", "Prensa de piernas")

        val resultado = casoUso.ejecutar(gimnasio, maquina)

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) {
            repositorio.guardarGimnasio(gimnasio.copy(maquinas = listOf(maquina)))
        }
    }

    @Test
    fun `reemplaza una maquina existente con el mismo identificador`() = runTest {
        val maquinaOriginal = crearMaquina("m-1", "Prensa antigua")
        val gimnasio = crearGimnasio(maquinas = listOf(maquinaOriginal))
        val maquinaActualizada = crearMaquina("m-1", "Prensa de piernas 45º")

        val resultado = casoUso.ejecutar(gimnasio, maquinaActualizada)

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) {
            repositorio.guardarGimnasio(
                gimnasio.copy(maquinas = listOf(maquinaActualizada))
            )
        }
    }

    @Test
    fun `nombre de maquina vacio devuelve error y no toca el repositorio`() = runTest {
        val maquina = crearMaquina("m-1", "   ")

        val resultado = casoUso.ejecutar(crearGimnasio(), maquina)

        assertTrue(resultado.isFailure)
        assertEquals(
            "El nombre de la máquina no puede estar vacío.",
            resultado.exceptionOrNull()?.message
        )
        coVerify(exactly = 0) { repositorio.guardarGimnasio(any()) }
    }

    @Test
    fun `si el repositorio falla se devuelve el error de persistencia`() = runTest {
        coEvery { repositorio.guardarGimnasio(any()) } throws IllegalStateException("Error de red")

        val resultado = casoUso.ejecutar(
            crearGimnasio(),
            crearMaquina("m-1", "Prensa de piernas")
        )

        assertTrue(resultado.isFailure)
        assertEquals("Error de red", resultado.exceptionOrNull()?.message)
    }
}