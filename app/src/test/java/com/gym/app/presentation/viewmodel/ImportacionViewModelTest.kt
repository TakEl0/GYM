/**
 * @file ImportacionViewModelTest.kt
 * @brief Pruebas unitarias del ViewModel de importación de documentos Naturvitia.
 */
package com.gym.app.presentation.viewmodel

import android.content.Context
import android.net.Uri
import com.gym.app.domain.usecase.importacion.ImportarDocumentosNaturvitiaCasoUso
import com.gym.app.test.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * @class ImportacionViewModelTest
 * @brief Verifica la gestión de selección de documentos (URIs) y la ejecución
 * de la importación mediante [ImportarDocumentosNaturvitiaCasoUso]: estados de
 * éxito, error y la protección cuando no hay ningún documento seleccionado.
 */
class ImportacionViewModelTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    private val importarDocumentosCasoUso = mockk<ImportarDocumentosNaturvitiaCasoUso>()

    private fun crearViewModel(): ImportacionViewModel = ImportacionViewModel(importarDocumentosCasoUso)

    /** Crea un URI simulado (MockK) que el ViewModel solo referencia. */
    private fun crearUri(): Uri = mockk<Uri>(relaxed = true)

    @Test
    fun `seleccionarDocumento anade el uri al mapa del tipo correcto`() {
        val viewModel = crearViewModel()
        val uri = crearUri()

        viewModel.seleccionarDocumento(TipoDocumentoNaturvitia.DIETA, uri)

        assertEquals(uri, viewModel.estado.value.uris[TipoDocumentoNaturvitia.DIETA])
        assertTrue(viewModel.hayDocumentosSeleccionados())
    }

    @Test
    fun `seleccionarDocumento con uri nulo no modifica la seleccion`() {
        val viewModel = crearViewModel()

        viewModel.seleccionarDocumento(TipoDocumentoNaturvitia.INBODY, null)

        assertFalse(viewModel.hayDocumentosSeleccionados())
        assertTrue(viewModel.estado.value.uris.isEmpty())
    }

    @Test
    fun `eliminarDocumento quita el uri del mapa`() {
        val viewModel = crearViewModel()
        val uri = crearUri()
        viewModel.seleccionarDocumento(TipoDocumentoNaturvitia.ENTRENAMIENTO, uri)

        viewModel.eliminarDocumento(TipoDocumentoNaturvitia.ENTRENAMIENTO)

        assertFalse(viewModel.hayDocumentosSeleccionados())
        assertNull(viewModel.estado.value.uris[TipoDocumentoNaturvitia.ENTRENAMIENTO])
    }

    @Test
    fun `hayDocumentosSeleccionados refleja la seleccion actual`() {
        val viewModel = crearViewModel()

        assertFalse("Sin selecciones no debe haber documentos.", viewModel.hayDocumentosSeleccionados())

        viewModel.seleccionarDocumento(TipoDocumentoNaturvitia.INBODY, crearUri())

        assertTrue(viewModel.hayDocumentosSeleccionados())
    }

    @Test
    fun `importar sin documentos seleccionados no invoca el caso de uso`() = runTest {
        val viewModel = crearViewModel()

        viewModel.importar(mockk<Context>(relaxed = true))

        coVerify(exactly = 0) { importarDocumentosCasoUso.ejecutar(any(), any(), any(), any()) }
        assertFalse(viewModel.estado.value.importando)
    }

    @Test
    fun `importar con exito limpia los uris y muestra mensaje de exito`() = runTest {
        coEvery { importarDocumentosCasoUso.ejecutar(any(), any(), any(), any()) } returns Result.success(Unit)
        val viewModel = crearViewModel()
        viewModel.seleccionarDocumento(TipoDocumentoNaturvitia.DIETA, crearUri())
        viewModel.seleccionarDocumento(TipoDocumentoNaturvitia.INBODY, crearUri())

        viewModel.importar(mockk<Context>(relaxed = true))

        assertFalse(viewModel.estado.value.importando)
        assertTrue("Tras el éxito deben limpiarse las selecciones.", viewModel.estado.value.uris.isEmpty())
        assertEquals(
            "Documentos importados correctamente. Ya puedes consultar tu plan del día.",
            viewModel.estado.value.mensajeExito
        )
        assertNull(viewModel.estado.value.error)
        coVerify(exactly = 1) { importarDocumentosCasoUso.ejecutar(any(), any(), any(), any()) }
    }

    @Test
    fun `importar con fallo muestra el error y conserva las selecciones`() = runTest {
        coEvery {
            importarDocumentosCasoUso.ejecutar(any(), any(), any(), any())
        } returns Result.failure(IllegalStateException("El PDF no es un documento Naturvitia válido"))
        val viewModel = crearViewModel()
        viewModel.seleccionarDocumento(TipoDocumentoNaturvitia.DIETA, crearUri())

        viewModel.importar(mockk<Context>(relaxed = true))

        assertFalse(viewModel.estado.value.importando)
        assertEquals("El PDF no es un documento Naturvitia válido", viewModel.estado.value.error)
        assertNull(viewModel.estado.value.mensajeExito)
        assertTrue(
            "Tras un error las selecciones deben conservarse para reintentar.",
            viewModel.hayDocumentosSeleccionados()
        )
    }

    @Test
    fun `limpiarMensajes descarta los mensajes de exito y error`() {
        val viewModel = crearViewModel()
        coEvery { importarDocumentosCasoUso.ejecutar(any(), any(), any(), any()) } returns Result.success(Unit)
        viewModel.seleccionarDocumento(TipoDocumentoNaturvitia.DIETA, crearUri())
        viewModel.importar(mockk<Context>(relaxed = true))

        viewModel.limpiarMensajes()

        assertNull(viewModel.estado.value.mensajeExito)
        assertNull(viewModel.estado.value.error)
    }
}