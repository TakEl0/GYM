/**
 * @file ListaCompraViewModelTest.kt
 * @brief Pruebas unitarias del ViewModel de la pantalla de Lista de la Compra.
 */
package com.gym.app.presentation.viewmodel

import com.gym.app.data.repository.RepositorioListaCompraFake
import com.gym.app.data.repository.RepositorioPlanComidaFake
import com.gym.app.domain.model.IngredienteToma
import com.gym.app.domain.model.ItemListaCompra
import com.gym.app.domain.model.ListaCompra
import com.gym.app.domain.model.PlanComida
import com.gym.app.domain.model.Toma
import com.gym.app.test.MainDispatcherRule
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * @class ListaCompraViewModelTest
 * @brief Verifica la carga inicial de listas, la generación de la lista semanal
 * a partir de los planes y el marcado de ítems como comprados con Fakes.
 */
class ListaCompraViewModelTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    /** Lunes de la semana en curso (fecha determinista relativa a hoy). */
    private val lunesSemana: LocalDate =
        LocalDate.now().minusDays((LocalDate.now().dayOfWeek.value - 1).toLong())

    /**
     * @brief Construye un plan de comidas dentro de la semana en curso.
     */
    private fun crearPlanSemana(fecha: LocalDate): PlanComida = PlanComida(
        id = "plan-$fecha",
        nombre = "Plan de $fecha",
        fecha = fecha,
        tomas = listOf(
            Toma(
                id = "toma-$fecha",
                tipoIngesta = Toma.TIPO_DESAYUNO,
                orden = 1,
                ingredientes = listOf(
                    IngredienteToma(
                        id = "ingrediente-$fecha",
                        alimentoId = null,
                        nombre = "Panecillo integral",
                        cantidadGramos = 100.0,
                        pesaje = IngredienteToma.PESAJE_CRUDO,
                        alimentoResuelto = null
                    )
                )
            )
        )
    )

    @Test
    fun `estado inicial termina la carga sin listas`() {
        val viewModel = ListaCompraViewModel(RepositorioListaCompraFake(), RepositorioPlanComidaFake())

        assertEquals(false, viewModel.estado.value.cargando)
        assertTrue(viewModel.estado.value.listas.isEmpty())
        assertNull(viewModel.estado.value.listaMasReciente)
        assertNull(viewModel.estado.value.error)
    }

    @Test
    fun `generarListaSemanal consolida los planes de la semana y guarda la lista`() {
        val repositorioLista = RepositorioListaCompraFake()
        val repositorioPlan = RepositorioPlanComidaFake()
        val viewModel = ListaCompraViewModel(repositorioLista, repositorioPlan)

        runBlocking {
            repositorioPlan.guardarPlan(crearPlanSemana(lunesSemana))
            repositorioPlan.guardarPlan(crearPlanSemana(lunesSemana.plusDays(1)))
        }

        viewModel.generarListaSemanal()

        val estado = viewModel.estado.value
        assertEquals(false, estado.generando)
        assertNull(estado.error)
        assertTrue(estado.listas.isNotEmpty())
        assertNotNull(estado.listaMasReciente)
        // Consolidación: 100 g + 100 g = 200 g de panecillo en un único ítem.
        val lista = estado.listaMasReciente!!
        assertEquals(1, lista.items.size)
        assertEquals(200.0, lista.items[0].cantidadGramos, 0.001)
        assertEquals(lunesSemana, lista.semanaInicio)
    }

    @Test
    fun `marcarItem actualiza el estado de compra del item`() {
        val repositorioLista = RepositorioListaCompraFake()
        // Dispatchers.Unconfined hace síncrono el withContext del caso de uso
        // interno, de modo que la actualización del Fake y su propagación
        // reactiva se completan antes de la siguiente aserción.
        val viewModel = ListaCompraViewModel(
            repositorioLista,
            RepositorioPlanComidaFake(),
            dispatcher = Dispatchers.Unconfined
        )

        val item = ItemListaCompra(
            id = "item-1",
            nombreAlimento = "Panecillo integral",
            cantidadGramos = 100.0,
            supermercado = "Mercadona"
        )
        val lista = ListaCompra(
            id = "lista-1",
            semanaInicio = lunesSemana,
            items = listOf(item),
            supermercados = listOf("Mercadona")
        )
        runBlocking { repositorioLista.guardarLista(lista) }
        assertTrue(viewModel.estado.value.listaMasReciente!!.totalItemsPendientes == 1)

        viewModel.marcarItem(listaId = "lista-1", itemId = "item-1", comprado = true)

        val listaActualizada = viewModel.estado.value.listas.first { it.id == "lista-1" }
        assertTrue(listaActualizada.items.first { it.id == "item-1" }.comprado)
        assertEquals(0, listaActualizada.totalItemsPendientes)
    }

    @Test
    fun `limpiarError descarta el mensaje de error`() {
        val viewModel = ListaCompraViewModel(RepositorioListaCompraFake(), RepositorioPlanComidaFake())
        viewModel.limpiarError()

        assertNull(viewModel.estado.value.error)
    }
}