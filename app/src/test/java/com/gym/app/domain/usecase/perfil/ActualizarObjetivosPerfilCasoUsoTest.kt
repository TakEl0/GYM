/**
 * @file ActualizarObjetivosPerfilCasoUsoTest.kt
 * @brief Pruebas unitarias de la actualización de objetivos del perfil de usuario.
 */
package com.gym.app.domain.usecase.perfil

import com.gym.app.domain.model.PerfilUsuario
import com.gym.app.domain.repository.RepositorioPerfil
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class ActualizarObjetivosPerfilCasoUsoTest
 * @brief Verifica la validación de los datos antropométricos y de objetivos
 * (altura, edad, sexo, factor de actividad y objetivo) y la delegación en el
 * [RepositorioPerfil] únicamente cuando todos los valores son válidos.
 */
class ActualizarObjetivosPerfilCasoUsoTest {

    private val repositorio = mockk<RepositorioPerfil>(relaxed = true)

    private val casoUso = ActualizarObjetivosPerfilCasoUso(
        repositorioPerfil = repositorio,
        dispatcher = Dispatchers.Unconfined
    )

    /** Valores válidos de referencia para el perfil. */
    private val id = "perfil-1"
    private val peso = 80.0
    private val altura = 180.0
    private val edad = 30
    private val sexo = PerfilUsuario.SEXO_HOMBRE
    private val factor = "MODERADO"
    private val objetivo = PerfilUsuario.OBJETIVO_VOLUMEN

    @Test
    fun `valores validos delegan en el repositorio y tienen exito`() = runTest {
        val resultado = casoUso.ejecutar(id, peso, altura, edad, sexo, factor, objetivo)

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) {
            repositorio.actualizarObjetivos(id, peso, altura, edad, sexo, factor, objetivo)
        }
    }

    @Test
    fun `peso vacio devuelve error`() = runTest {
        val resultado = casoUso.ejecutar(id, null, altura, edad, sexo, factor, objetivo)

        assertTrue(resultado.isFailure)
        assertEquals("El peso objetivo no puede estar vacío.", resultado.exceptionOrNull()?.message)
        coVerify(exactly = 0) { repositorio.actualizarObjetivos(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `peso no positivo devuelve error`() = runTest {
        val resultado = casoUso.ejecutar(id, 0.0, altura, edad, sexo, factor, objetivo)

        assertTrue(resultado.isFailure)
        assertEquals("El peso objetivo debe ser mayor que 0 kg.", resultado.exceptionOrNull()?.message)
        coVerify(exactly = 0) { repositorio.actualizarObjetivos(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `altura vacia devuelve error`() = runTest {
        val resultado = casoUso.ejecutar(id, peso, null, edad, sexo, factor, objetivo)

        assertTrue(resultado.isFailure)
        assertEquals("La altura no puede estar vacía.", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun `altura fuera de rango devuelve error`() = runTest {
        val resultadoCero = casoUso.ejecutar(id, peso, 0.0, edad, sexo, factor, objetivo)
        val resultadoExcesiva = casoUso.ejecutar(id, peso, 251.0, edad, sexo, factor, objetivo)

        assertTrue(resultadoCero.isFailure)
        assertTrue(resultadoExcesiva.isFailure)
        assertEquals(
            "La altura debe estar entre 0 y 250 cm.",
            resultadoExcesiva.exceptionOrNull()?.message
        )
    }

    @Test
    fun `edad vacia devuelve error`() = runTest {
        val resultado = casoUso.ejecutar(id, peso, altura, null, sexo, factor, objetivo)

        assertTrue(resultado.isFailure)
        assertEquals("La edad no puede estar vacía.", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun `edad fuera de rango devuelve error`() = runTest {
        val resultadoBaja = casoUso.ejecutar(id, peso, altura, 9, sexo, factor, objetivo)
        val resultadoAlta = casoUso.ejecutar(id, peso, altura, 101, sexo, factor, objetivo)

        assertTrue(resultadoBaja.isFailure)
        assertTrue(resultadoAlta.isFailure)
        assertEquals(
            "La edad debe estar entre 10 y 100 años.",
            resultadoAlta.exceptionOrNull()?.message
        )
    }

    @Test
    fun `sexo invalido devuelve error`() = runTest {
        val resultado = casoUso.ejecutar(id, peso, altura, edad, "OTRO", factor, objetivo)

        assertTrue(resultado.isFailure)
        assertEquals("El sexo debe ser HOMBRE o MUJER.", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun `factor de actividad invalido devuelve error`() = runTest {
        val resultado = casoUso.ejecutar(id, peso, altura, edad, sexo, "MUY_FUERTE", objetivo)

        assertTrue(resultado.isFailure)
        assertEquals(
            "El factor de actividad debe ser SEDENTARIO, LIGERO, MODERADO o FUERTE.",
            resultado.exceptionOrNull()?.message
        )
    }

    @Test
    fun `objetivo invalido devuelve error`() = runTest {
        val resultado = casoUso.ejecutar(id, peso, altura, edad, sexo, factor, "RESISTENCIA")

        assertTrue(resultado.isFailure)
        assertEquals(
            "El objetivo debe ser VOLUMEN, DEFINICION o MANTENIMIENTO.",
            resultado.exceptionOrNull()?.message
        )
    }

    @Test
    fun `si el repositorio falla se devuelve el error de persistencia`() = runTest {
        coEvery {
            repositorio.actualizarObjetivos(any(), any(), any(), any(), any(), any(), any())
        } throws IllegalStateException("Error de base de datos")

        val resultado = casoUso.ejecutar(id, peso, altura, edad, sexo, factor, objetivo)

        assertTrue(resultado.isFailure)
        assertEquals("Error de base de datos", resultado.exceptionOrNull()?.message)
    }
}