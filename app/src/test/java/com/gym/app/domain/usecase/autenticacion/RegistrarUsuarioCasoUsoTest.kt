/**
 * @file RegistrarUsuarioCasoUsoTest.kt
 * @brief Pruebas unitarias del caso de uso de registro de usuarios.
 */
package com.gym.app.domain.usecase.autenticacion

import com.gym.app.domain.repository.RepositorioAutenticacion
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class RegistrarUsuarioCasoUsoTest
 * @brief Verifica la validación de datos de registro y la delegación en el repositorio.
 */
class RegistrarUsuarioCasoUsoTest {

    private val repositorio = mockk<RepositorioAutenticacion>(relaxed = true)

    private val casoUso = RegistrarUsuarioCasoUso(
        repositorioAutenticacion = repositorio,
        dispatcher = Dispatchers.Unconfined
    )

    @Test
    fun `registro con datos validos delega en el repositorio y tiene exito`() = runTest {
        coEvery {
            repositorio.registrar("usuario@correo.com", "contrasena123", "Ana García")
        } returns Result.success(Unit)

        val resultado = casoUso.ejecutar("usuario@correo.com", "contrasena123", "Ana García")

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) {
            repositorio.registrar("usuario@correo.com", "contrasena123", "Ana García")
        }
    }

    @Test
    fun `registro con correo invalido devuelve error de formato`() = runTest {
        val resultado = casoUso.ejecutar("correo-sin-arroba", "contrasena123", "Ana García")

        assertTrue(resultado.isFailure)
        assertEquals(
            "El correo electrónico no tiene un formato válido.",
            resultado.exceptionOrNull()?.message
        )
        coVerify(exactly = 0) { repositorio.registrar(any(), any(), any()) }
    }

    @Test
    fun `registro con contrasena corta devuelve error de longitud`() = runTest {
        val resultado = casoUso.ejecutar("usuario@correo.com", "123", "Ana García")

        assertTrue(resultado.isFailure)
        assertEquals(
            "La contraseña debe tener al menos 8 caracteres.",
            resultado.exceptionOrNull()?.message
        )
        coVerify(exactly = 0) { repositorio.registrar(any(), any(), any()) }
    }

    @Test
    fun `registro con nombre vacio devuelve error de nombre`() = runTest {
        val resultado = casoUso.ejecutar("usuario@correo.com", "contrasena123", "  ")

        assertTrue(resultado.isFailure)
        assertEquals(
            "El nombre no puede estar vacío.",
            resultado.exceptionOrNull()?.message
        )
        coVerify(exactly = 0) { repositorio.registrar(any(), any(), any()) }
    }
}