/**
 * @file ImportacionViewModelTest.kt
 * @brief Pruebas unitarias del ViewModel de importación de documentos Naturvitia.
 */
package com.gym.app.presentation.viewmodel

import android.content.Context
import android.net.Uri
import com.gym.app.data.parser.ParserDocumentosNaturvitia
import com.gym.app.domain.model.Rutina
import com.gym.app.domain.usecase.importacion.ImportarDocumentosNaturvitiaCasoUso
import com.gym.app.domain.usecase.importacion.ImportarRutinaNaturvitiaCasoUso
import com.gym.app.domain.usecase.importacion.ResultadoImportacionRutina
import com.gym.app.test.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
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
 * de la importación mediante [ImportarDocumentosNaturvitiaCasoUso] y la conversión
 * del plan de entrenamiento en rutinas con [ImportarRutinaNaturvitiaCasoUso]:
 * estados de éxito, error, el resultado del mapeo y la protección cuando no hay
 * ningún documento seleccionado.
 */
class ImportacionViewModelTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    private val importarDocumentosCasoUso = mockk<ImportarDocumentosNaturvitiaCasoUso>()
    private val importarRutinaCasoUso = mockk<ImportarRutinaNaturvitiaCasoUso>()

    private fun crearViewModel(): ImportacionViewModel =
        ImportacionViewModel(importarDocumentosCasoUso, importarRutinaCasoUso)

    /** Crea un URI simulado (MockK) que el ViewModel solo referencia. */
    private fun crearUri(): Uri = mockk<Uri>(relaxed = true)

    /**
     * Resultado de rutina de ejemplo: 2 rutinas (2 días), 12 ejercicios mapeados
     * y 3 ejercicios sin mapear pendientes de revisión.
     */
    private fun crearResultadoRutina(): ResultadoImportacionRutina = ResultadoImportacionRutina(
        rutinasCreadas = listOf(
            Rutina(id = "rutina-naturvitia-1", nombre = "Día 1 - Pierna", diasSemana = listOf(1)),
            Rutina(id = "rutina-naturvitia-2", nombre = "Día 2 - Pecho", diasSemana = listOf(2))
        ),
        ejerciciosMapeados = 12,
        ejerciciosSinMapear = listOf(
            "Ejercicio pendiente A",
            "Ejercicio pendiente B",
            "Ejercicio pendiente C"
        )
    )

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
        coVerify(exactly = 0) { importarRutinaCasoUso.ejecutar(any()) }
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
        assertNull(
            "Sin plan de entrenamiento no debe haber resultado de rutina.",
            viewModel.estado.value.resultadoRutina
        )
        coVerify(exactly = 1) { importarDocumentosCasoUso.ejecutar(any(), any(), any(), any()) }
        coVerify(exactly = 0) { importarRutinaCasoUso.ejecutar(any()) }
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
    fun `importa rutina del PDF y expone el resultado del mapeo`() = runTest {
        mockkObject(ParserDocumentosNaturvitia)
        try {
            // El parser devuelve el texto del plan para el URI de entrenamiento.
            every { ParserDocumentosNaturvitia.extraerTextoPdf(any(), any()) } returns "TEXTO DEL PLAN"
            coEvery { importarDocumentosCasoUso.ejecutar(any(), any(), any(), any()) } returns Result.success(Unit)
            val resultado = crearResultadoRutina()
            coEvery { importarRutinaCasoUso.ejecutar(any()) } returns Result.success(resultado)
            val viewModel = crearViewModel()
            viewModel.seleccionarDocumento(TipoDocumentoNaturvitia.ENTRENAMIENTO, crearUri())

            viewModel.importar(mockk<Context>(relaxed = true))

            assertFalse(viewModel.estado.value.importando)
            assertEquals(resultado, viewModel.estado.value.resultadoRutina)
            assertNull(viewModel.estado.value.avisoRutina)
            assertTrue(
                "El mensaje debe incluir el resumen del mapeo.",
                viewModel.estado.value.mensajeExito!!.contains(
                    "Rutina importada: 2 días, 12 ejercicios mapeados a tu gimnasio."
                )
            )
            assertTrue(
                "El mensaje debe avisar de los ejercicios pendientes.",
                viewModel.estado.value.mensajeExito!!.contains("3 ejercicios pendientes de revisar.")
            )
            coVerify(exactly = 1) { importarRutinaCasoUso.ejecutar(any()) }
        } finally {
            unmockkObject(ParserDocumentosNaturvitia)
        }
    }

    @Test
    fun `no rompe la importacion si la rutina falla por gimnasio no configurado`() = runTest {
        mockkObject(ParserDocumentosNaturvitia)
        try {
            every { ParserDocumentosNaturvitia.extraerTextoPdf(any(), any()) } returns "TEXTO DEL PLAN"
            coEvery { importarDocumentosCasoUso.ejecutar(any(), any(), any(), any()) } returns Result.success(Unit)
            coEvery { importarRutinaCasoUso.ejecutar(any()) } returns Result.failure(
                IllegalStateException(ImportarRutinaNaturvitiaCasoUso.MENSAJE_GIMNASIO_NO_CONFIGURADO)
            )
            val viewModel = crearViewModel()
            viewModel.seleccionarDocumento(TipoDocumentoNaturvitia.ENTRENAMIENTO, crearUri())

            viewModel.importar(mockk<Context>(relaxed = true))

            assertFalse(viewModel.estado.value.importando)
            assertEquals(
                "Los documentos se importan con normalidad aunque la rutina falle.",
                "Documentos importados correctamente. Ya puedes consultar tu plan del día.",
                viewModel.estado.value.mensajeExito
            )
            assertEquals(
                "El aviso debe contener el mensaje del fallo de la rutina.",
                ImportarRutinaNaturvitiaCasoUso.MENSAJE_GIMNASIO_NO_CONFIGURADO,
                viewModel.estado.value.avisoRutina
            )
            assertNull(viewModel.estado.value.resultadoRutina)
            assertNull(viewModel.estado.value.error)
            coVerify(exactly = 1) { importarRutinaCasoUso.ejecutar(any()) }
        } finally {
            unmockkObject(ParserDocumentosNaturvitia)
        }
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
        assertNull(viewModel.estado.value.resultadoRutina)
        assertNull(viewModel.estado.value.avisoRutina)
    }

    @Test
    fun `limpiarMensajes descarta tambien el resultado y el aviso de la rutina`() = runTest {
        mockkObject(ParserDocumentosNaturvitia)
        try {
            every { ParserDocumentosNaturvitia.extraerTextoPdf(any(), any()) } returns "TEXTO DEL PLAN"
            coEvery { importarDocumentosCasoUso.ejecutar(any(), any(), any(), any()) } returns Result.success(Unit)
            coEvery { importarRutinaCasoUso.ejecutar(any()) } returns Result.success(crearResultadoRutina())
            val viewModel = crearViewModel()
            viewModel.seleccionarDocumento(TipoDocumentoNaturvitia.ENTRENAMIENTO, crearUri())
            viewModel.importar(mockk<Context>(relaxed = true))
            assertTrue("El resultado de la rutina debe estar expuesto.", viewModel.estado.value.resultadoRutina != null)

            viewModel.limpiarMensajes()

            assertNull(viewModel.estado.value.mensajeExito)
            assertNull(viewModel.estado.value.error)
            assertNull(viewModel.estado.value.resultadoRutina)
            assertNull(viewModel.estado.value.avisoRutina)
        } finally {
            unmockkObject(ParserDocumentosNaturvitia)
        }
    }
}