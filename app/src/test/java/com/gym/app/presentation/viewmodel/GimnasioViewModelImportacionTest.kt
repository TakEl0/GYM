/**
 * @file GimnasioViewModelImportacionTest.kt
 * @brief Pruebas unitarias de la importación de maquinaria desde el catálogo
 * en el ViewModel de la pantalla de Gimnasio.
 */
package com.gym.app.presentation.viewmodel

import com.gym.app.domain.model.Ejercicio
import com.gym.app.domain.model.Gimnasio
import com.gym.app.domain.model.Maquina
import com.gym.app.domain.repository.RepositorioEjercicio
import com.gym.app.domain.repository.RepositorioGimnasio
import com.gym.app.test.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * @class GimnasioViewModelImportacionTest
 * @brief Verifica [GimnasioViewModel.importarMaquinasDelCatalogo]: la adición
 * de máquinas nuevas al gimnasio, la omisión de duplicados por identificador
 * estable, el caso sin selección y la creación del gimnasio "Mi gimnasio"
 * cuando aún no existe ninguno configurado.
 */
class GimnasioViewModelImportacionTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    private val repositorioGimnasio = mockk<RepositorioGimnasio>()
    private val repositorioEjercicio = mockk<RepositorioEjercicio>()

    private val flujoGimnasio = MutableStateFlow<Gimnasio?>(null)
    private val flujoEjercicios = MutableStateFlow<List<Ejercicio>>(emptyList())

    /**
     * @brief Crea el ViewModel con repositorios simulados (MockK) y un
     * dispatcher de operaciones inyectado para que el guardado sea síncrono.
     * @param gimnasioInicial Gimnasio observable al arrancar (null si no existe).
     */
    private fun crearViewModel(gimnasioInicial: Gimnasio? = null): GimnasioViewModel {
        flujoGimnasio.value = gimnasioInicial
        every { repositorioGimnasio.observarGimnasio() } returns flujoGimnasio
        every { repositorioEjercicio.observarEjercicios() } returns flujoEjercicios
        coEvery { repositorioGimnasio.guardarGimnasio(any()) } answers {
            flujoGimnasio.value = firstArg()
            Unit
        }
        return GimnasioViewModel(
            repositorioGimnasio = repositorioGimnasio,
            repositorioEjercicio = repositorioEjercicio,
            dispatcherOperaciones = Dispatchers.Unconfined
        )
    }

    /** Crea una máquina ya existente en el gimnasio con el identificador dado. */
    private fun maquinaExistente(id: String): Maquina = Maquina(
        id = id,
        nombre = "Máquina $id",
        grupoMuscular = listOf("CUADRICEPS"),
        disponible = true
    )

    @Test
    fun `importarMaquinasDelCatalogo anade las maquinas seleccionadas al gimnasio`() = runTest {
        val gimnasioInicial = Gimnasio(
            id = "g-1",
            nombre = "Power House",
            maquinas = listOf(maquinaExistente("prensa-45"))
        )
        val viewModel = crearViewModel(gimnasioInicial)

        viewModel.importarMaquinasDelCatalogo(listOf("extension-cuadriceps", "curl-femoral-sentado"))

        val idsResultantes = viewModel.estado.value.gimnasio?.maquinas?.map { it.id }.orEmpty()
        assertTrue(
            "El gimnasio debe conservar la máquina previa y añadir las dos nuevas.",
            idsResultantes.containsAll(listOf("prensa-45", "extension-cuadriceps", "curl-femoral-sentado"))
        )
        assertEquals(3, viewModel.estado.value.gimnasio?.maquinas?.size)
        coVerify(exactly = 1) { repositorioGimnasio.guardarGimnasio(any()) }
    }

    @Test
    fun `importar un id ya existente no duplica la maquina`() = runTest {
        val gimnasioInicial = Gimnasio(
            id = "g-1",
            nombre = "Power House",
            maquinas = listOf(maquinaExistente("prensa-45"))
        )
        val viewModel = crearViewModel(gimnasioInicial)

        viewModel.importarMaquinasDelCatalogo(listOf("prensa-45", "extension-cuadriceps"))

        val maquinas = viewModel.estado.value.gimnasio?.maquinas.orEmpty()
        assertEquals(
            "No debe añadirse una segunda copia de la máquina ya existente.",
            2,
            maquinas.size
        )
        assertEquals(
            setOf("prensa-45", "extension-cuadriceps"),
            maquinas.map { it.id }.toSet()
        )
        coVerify(exactly = 1) { repositorioGimnasio.guardarGimnasio(any()) }
    }

    @Test
    fun `importar con lista vacia no modifica nada ni persiste`() = runTest {
        val gimnasioInicial = Gimnasio(
            id = "g-1",
            nombre = "Power House",
            maquinas = listOf(maquinaExistente("prensa-45"))
        )
        val viewModel = crearViewModel(gimnasioInicial)

        viewModel.importarMaquinasDelCatalogo(emptyList())

        assertEquals(1, viewModel.estado.value.gimnasio?.maquinas?.size)
        coVerify(exactly = 0) { repositorioGimnasio.guardarGimnasio(any()) }
    }

    @Test
    fun `importar sin gimnasio crea uno llamado Mi gimnasio con las maquinas`() = runTest {
        val viewModel = crearViewModel(gimnasioInicial = null)

        viewModel.importarMaquinasDelCatalogo(listOf("prensa-45", "peck-deck"))

        val gimnasio = viewModel.estado.value.gimnasio
        assertNotNull("Debe crearse el gimnasio por defecto.", gimnasio)
        assertEquals("Mi gimnasio", gimnasio?.nombre)
        assertEquals(listOf("prensa-45", "peck-deck"), gimnasio?.maquinas?.map { it.id })
        coVerify(exactly = 1) { repositorioGimnasio.guardarGimnasio(any()) }
    }

    @Test
    fun `si la persistencia falla se muestra el error en el estado`() = runTest {
        val viewModel = crearViewModel(gimnasioInicial = null)
        coEvery { repositorioGimnasio.guardarGimnasio(any()) } throws IllegalStateException("Error de red")

        viewModel.importarMaquinasDelCatalogo(listOf("prensa-45"))

        assertNull("No debe crearse un gimnasio cuando falla la persistencia.", viewModel.estado.value.gimnasio)
        assertEquals("Error de red", viewModel.estado.value.error)
    }
}