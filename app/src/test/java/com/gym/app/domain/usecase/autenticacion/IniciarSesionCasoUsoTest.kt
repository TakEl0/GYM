/**
 * @file IniciarSesionCasoUsoTest.kt
 * @brief Pruebas unitarias del caso de uso de inicio de sesión.
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
 * @class IniciarSesionCasoUsoTest
 * @brief Verifica la validación de credenciales y la delegación en el repositorio.
 */
class IniciarSesionCasoUsoTest {

    private val repositorio = mockk<RepositorioAutenticacion>(relaxed = true)

    private val casoUso = IniciarSesionCasoUso(
        repositorioAutenticacion = repositorio,
        dispatcher = Dispatchers.Unconfined
    )

    @Test
    fun `iniciar sesion con credenciales validas delega en el repositorio y tiene exito`() = runTest {
        coEvery { repositorio.iniciarSesion("usuario@correo.com", "contrasena123") } returns Result.success(Unit)

        val resultado = casoUso.ejecutar("usuario@correo.com", "contrasena123")

        assertTrue(resultado.isSuccess)
        coVerify(exactly = 1) { repositorio.iniciarSesion("usuario@correo.com", "contrasena123") }
    }

    @Test
    fun `iniciar sesion con correo invalido devuelve error de formato`() = runTest {
        val resultado = casoUso.ejecutar("correo-invalido", "contrasena123")

        assertTrue(resultado.isFailure)
        assertEquals(
            "El correo electrónico no tiene un formato válido.",
            resultado.exceptionOrNull()?.message
        )
        coVerify(exactly = 0) { repositorio.iniciarSesion(any(), any()) }
    }

    @Test
    fun `iniciar sesion con contrasena corta devuelve error de longitud`() = runTest {
        val resultado = casoUso.ejecutar("usuario@correo.com", "corta")

        assertTrue(resultado.isFailure)
        assertEquals(
            "La contraseña debe tener al menos 8 caracteres.",
            resultado.exceptionOrNull()?.message
        )
        coVerify(exactly = 0) { repositorio.iniciarSesion(any(), any()) }
    }

    @Test
    fun `iniciar sesion con repositorio con error propaga el fallo`() = runTest {
        coEvery { repositorio.iniciarSesion(any(), any()) } returns Result.failure(
            IllegalStateException("Credenciales incorrectas")
        )

        val resultado = casoUso.ejecutar("usuario@correo.com", "contrasena123")

        assertTrue(resultado.isFailure)
        assertEquals("Credenciales incorrectas", resultado.exceptionOrNull()?.message)
    }
}