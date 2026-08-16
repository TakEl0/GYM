/**
 * @file RegistroPesoViewModelTest.kt
 * @brief Pruebas unitarias del ViewModel de registro de peso.
 */
package com.gym.app.presentation.viewmodel

import com.gym.app.data.repository.RepositorioPesoFake
import com.gym.app.test.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * @class RegistroPesoViewModelTest
 * @brief Verifica la carga del historial y el guardado de registros con el repositorio Fake.
 */
class RegistroPesoViewModelTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    @Test
    fun `cargarHistorial llena el historial y el ultimo registro`() {
        val viewModel = RegistroPesoViewModel(RepositorioPesoFake())

        viewModel.cargarHistorial()

        assertNotNull(viewModel.estado.value.ultimoRegistro)
        assertEquals(false, viewModel.estado.value.cargando)
    }

    @Test
    fun `guardarRegistro con peso valido guarda y limpia los campos`() {
        val viewModel = RegistroPesoViewModel(RepositorioPesoFake())
        viewModel.actualizarPeso("78,5")
        viewModel.actualizarGrasa("17,2")

        viewModel.guardarRegistro()

        assertEquals("", viewModel.estado.value.pesoActual)
        assertEquals("", viewModel.estado.value.grasaActual)
        assertNull(viewModel.estado.value.error)
        assertNotNull(viewModel.estado.value.mensajeGuardado)
        assertEquals(78.5, viewModel.estado.value.ultimoRegistro?.pesoKg ?: 0.0, 0.001)
    }

    @Test
    fun `guardarRegistro con peso no numerico muestra error`() {
        val viewModel = RegistroPesoViewModel(RepositorioPesoFake())
        viewModel.actualizarPeso("abc")

        viewModel.guardarRegistro()

        assertEquals("Introduce un peso válido en kilogramos.", viewModel.estado.value.error)
    }

    @Test
    fun `guardarRegistro con peso cero muestra error de rango`() {
        val viewModel = RegistroPesoViewModel(RepositorioPesoFake())
        viewModel.actualizarPeso("0")

        viewModel.guardarRegistro()

        assertEquals("El peso debe ser mayor que cero.", viewModel.estado.value.error)
    }
}