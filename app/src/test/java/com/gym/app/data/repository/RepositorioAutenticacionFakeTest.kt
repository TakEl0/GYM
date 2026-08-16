/**
 * @file RepositorioAutenticacionFakeTest.kt
 * @brief Pruebas unitarias del repositorio simulado de autenticación.
 */
package com.gym.app.data.repository

import com.gym.app.domain.model.EstadoSesion
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class RepositorioAutenticacionFakeTest
 * @brief Verifica el flujo de registro, inicio, cierre de sesión y observación de estado.
 */
class RepositorioAutenticacionFakeTest {

    @Test
    fun `registrar un usuario crea una sesion autenticada`() = runTest {
        val repositorio = RepositorioAutenticacionFake()

        val resultado = repositorio.registrar("usuario@correo.com", "contrasena123", "Ana García")

        assertTrue(resultado.isSuccess)
        assertNotNull(repositorio.obtenerSesionActual())
        assertEquals(EstadoSesion.AUTENTICADO, repositorio.observarEstadoSesion().first())
    }

    @Test
    fun `iniciar sesion crea una sesion autenticada con el correo indicado`() = runTest {
        val repositorio = RepositorioAutenticacionFake()

        val resultado = repositorio.iniciarSesion("usuario@correo.com", "contrasena123")

        assertTrue(resultado.isSuccess)
        assertEquals("usuario@correo.com", repositorio.obtenerSesionActual()?.user?.email)
        assertEquals(EstadoSesion.AUTENTICADO, repositorio.observarEstadoSesion().first())
    }

    @Test
    fun `cerrar sesion elimina la sesion y vuelve al estado no autenticado`() = runTest {
        val repositorio = RepositorioAutenticacionFake()
        repositorio.iniciarSesion("usuario@correo.com", "contrasena123")

        repositorio.cerrarSesion()

        assertNull(repositorio.obtenerSesionActual())
        assertEquals(EstadoSesion.NO_AUTENTICADO, repositorio.observarEstadoSesion().first())
    }
}