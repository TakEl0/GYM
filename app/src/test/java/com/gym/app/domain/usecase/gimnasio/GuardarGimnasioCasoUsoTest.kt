/**
 * @file GuardarGimnasioCasoUsoTest.kt
 * @brief Pruebas unitarias del guardado de la información del gimnasio.
 */
package com.gym.app.domain.usecase.gimnasio

import com.gym.app.domain.model.Gimnasio
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
 * @class GuardarGimnasioCasoUsoTest
 * @brief Verifica la validación del nombre del gimnasio y la delegación en
 * [RepositorioGimnasio.guardarGimnasio].
 */
class GuardarGimnasioCasoUsoTest {

    private val repositorio = mockk<RepositorioGimnasio>(relaxed = true)

    private val casoUso = GuardarGimnasioCasoUso(
        repositorioGimnasio = repositorio,
        dispatcher = Dispatchers.Unconfined
    )

    private fun crearGimnasio(nombre: String = "Power House"): Gimnasio = Gimnasio(
        id = "gimnasio-1",
        nombre = nombre,
        direccion = "Calle Mayor 1"
    )

    @Test
    fun `gimnasio valido tiene exito y delega en el repositorio`() = runTest {
        val gimnasio = crearGimnasio()

        val resultado = casoUso.ejecutar(gimnasio)

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) { repositorio.guardarGimnasio(gimnasio) }
    }

    @Test
    fun `nombre de gimnasio vacio devuelve error`() = runTest {
        val gimnasio = crearGimnasio(nombre = "   ")

        val resultado = casoUso.ejecutar(gimnasio)

        assertTrue(resultado.isFailure)
        assertEquals(
            "El nombre del gimnasio no puede estar vacío.",
            resultado.exceptionOrNull()?.message
        )
        coVerify(exactly = 0) { repositorio.guardarGimnasio(any()) }
    }

    @Test
    fun `si el repositorio falla se devuelve el error de persistencia`() = runTest {
        coEvery { repositorio.guardarGimnasio(any()) } throws IllegalStateException("Error de red")

        val resultado = casoUso.ejecutar(crearGimnasio())

        assertTrue(resultado.isFailure)
        assertEquals("Error de red", resultado.exceptionOrNull()?.message)
    }
}