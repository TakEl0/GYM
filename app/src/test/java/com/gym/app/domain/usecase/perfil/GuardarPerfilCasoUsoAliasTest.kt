/**
 * @file GuardarPerfilCasoUsoAliasTest.kt
 * @brief Pruebas unitarias de la validación del alias opcional del perfil.
 */
package com.gym.app.domain.usecase.perfil

import com.gym.app.domain.model.PerfilUsuario
import com.gym.app.domain.repository.RepositorioPerfil
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class GuardarPerfilCasoUsoAliasTest
 * @brief Verifica las reglas del alias del [PerfilUsuario] según el método
 * Naturvitia: opcional, de 2 a 24 caracteres, solo letras, números, guiones
 * y guiones bajos (sin espacios), y normalizado con trim antes de persistir.
 */
class GuardarPerfilCasoUsoAliasTest {

    private val repositorio = mockk<RepositorioPerfil>(relaxed = true)

    private val casoUso = GuardarPerfilCasoUso(
        repositorioPerfil = repositorio,
        dispatcher = Dispatchers.Unconfined
    )

    /** Mensaje de error de validación del alias definido en el caso de uso. */
    private val mensajeErrorAlias = "El alias debe tener entre 2 y 24 caracteres y solo letras, números, guiones o guiones bajos."

    /** Crea un perfil completo y válido, parametrizando solo el alias. */
    private fun crearPerfil(alias: String? = null): PerfilUsuario = PerfilUsuario(
        id = "perfil-1",
        email = "usuario@correo.com",
        nombre = "Ana García",
        alias = alias,
        pesoObjetivoKg = 80.0,
        alturaCm = 180.0,
        edad = 30,
        sexo = PerfilUsuario.SEXO_HOMBRE,
        factorActividad = "MODERADO",
        objetivo = PerfilUsuario.OBJETIVO_VOLUMEN
    )

    @Test
    fun `alias nulo es valido y se persiste como null`() = runTest {
        val resultado = casoUso.ejecutar(crearPerfil(alias = null))

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) { repositorio.guardarPerfil(match { it.alias == null }) }
    }

    @Test
    fun `alias vacio o solo espacios se normaliza a null y guarda con exito`() = runTest {
        val resultado = casoUso.ejecutar(crearPerfil(alias = "   "))

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) { repositorio.guardarPerfil(match { it.alias == null }) }
    }

    @Test
    fun `alias valido con letras numeros guiones y guiones bajos se persiste`() = runTest {
        val alias = "ana_gym-2026"

        val resultado = casoUso.ejecutar(crearPerfil(alias = alias))

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) { repositorio.guardarPerfil(match { it.alias == alias }) }
    }

    @Test
    fun `alias de 2 caracteres es valido`() = runTest {
        val resultado = casoUso.ejecutar(crearPerfil(alias = "ab"))

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) { repositorio.guardarPerfil(match { it.alias == "ab" }) }
    }

    @Test
    fun `alias de 24 caracteres es valido`() = runTest {
        val alias = "a".repeat(24)

        val resultado = casoUso.ejecutar(crearPerfil(alias = alias))

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) { repositorio.guardarPerfil(match { it.alias == alias }) }
    }

    @Test
    fun `alias con espacio o caracteres especiales devuelve error descriptivo`() = runTest {
        val aliasesInvalidos = listOf(
            "ana garcia",     // Contiene un espacio en blanco.
            "ana@correo",     // Carácter especial no permitido (@).
            "ana_gárcia"      // Acentuación no permitida.
        )

        aliasesInvalidos.forEach { aliasInvalido ->
            val resultado = casoUso.ejecutar(crearPerfil(alias = aliasInvalido))

            assertTrue("El alias '$aliasInvalido' debería ser rechazado.", resultado.isFailure)
            assertEquals(mensajeErrorAlias, resultado.exceptionOrNull()?.message)
        }
        coVerify(exactly = 0) { repositorio.guardarPerfil(any()) }
    }

    @Test
    fun `alias de un solo caracter devuelve error y no persiste`() = runTest {
        val resultado = casoUso.ejecutar(crearPerfil(alias = "a"))

        assertTrue(resultado.isFailure)
        assertEquals(mensajeErrorAlias, resultado.exceptionOrNull()?.message)
        coVerify(exactly = 0) { repositorio.guardarPerfil(any()) }
    }

    @Test
    fun `alias de mas de 24 caracteres devuelve error`() = runTest {
        val resultado = casoUso.ejecutar(crearPerfil(alias = "a".repeat(25)))

        assertTrue(resultado.isFailure)
        assertEquals(mensajeErrorAlias, resultado.exceptionOrNull()?.message)
        coVerify(exactly = 0) { repositorio.guardarPerfil(any()) }
    }

    @Test
    fun `el alias se limpia con trim antes de persistirse`() = runTest {
        val resultado = casoUso.ejecutar(crearPerfil(alias = "  ana_gym  "))

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) { repositorio.guardarPerfil(match { it.alias == "ana_gym" }) }
    }
}