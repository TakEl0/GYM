/**
 * @file GenerarListaCompraSemanalCasoUsoTest.kt
 * @brief Pruebas unitarias de la consolidación de la lista de la compra semanal.
 */
package com.gym.app.domain.usecase.compra

import com.gym.app.domain.model.Alimento
import com.gym.app.domain.model.IngredienteToma
import com.gym.app.domain.model.PlanComida
import com.gym.app.domain.model.Toma
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @class GenerarListaCompraSemanalCasoUsoTest
 * @brief Verifica la consolidación de gramos por ingrediente entre varios planes,
 * el escalado a paquetes comerciales (redondeo hacia arriba) y la resolución del
 * supermercado preferido.
 */
class GenerarListaCompraSemanalCasoUsoTest {

    private val casoUso = GenerarListaCompraSemanalCasoUso()

    /** Lunes 10 de agosto de 2026 (inicio de la semana de prueba). */
    private val lunes = LocalDate.of(2026, 8, 10)

    /** Martes 11 de agosto de 2026. */
    private val martes = LocalDate.of(2026, 8, 11)

    /**
     * @brief Construye un ingrediente con el nombre y gramaje indicados.
     */
    private fun crearIngrediente(nombre: String, gramos: Double): IngredienteToma =
        IngredienteToma(
            id = "ingrediente-${nombre}-$gramos",
            alimentoId = null,
            nombre = nombre,
            cantidadGramos = gramos,
            pesaje = IngredienteToma.PESAJE_CRUDO,
            origenPlan = true,
            alimentoResuelto = null
        )

    /**
     * @brief Construye un plan diario con una única toma (los ingredientes dados).
     */
    private fun crearPlan(fecha: LocalDate, toma: Toma): PlanComida = PlanComida(
        id = "plan-$fecha",
        nombre = "Plan de $fecha",
        fecha = fecha,
        tomas = listOf(toma)
    )

    /**
     * @brief Construye una toma con el tipo de ingesta y los ingredientes dados.
     */
    private fun crearToma(tipo: String, ingredientes: List<IngredienteToma>): Toma = Toma(
        id = "toma-$tipo",
        tipoIngesta = tipo,
        orden = 1,
        ingredientes = ingredientes
    )

    /**
     * @brief Crea los planes de la semana de prueba: el lunes y el martes repiten
     * "Panecillo integral" (225 g en total) y el martes añade "Pechuga de pollo".
     */
    private fun crearPlanesSemana(): List<PlanComida> {
        val desayunoLunes = crearToma(
            Toma.TIPO_DESAYUNO,
            listOf(crearIngrediente("Panecillo integral", 100.0))
        )
        val meriendaLunes = crearToma(
            Toma.TIPO_MERIENDA,
            listOf(crearIngrediente("Panecillo integral", 50.0))
        )
        val meriendaMartes = crearToma(
            Toma.TIPO_MERIENDA,
            listOf(crearIngrediente("Panecillo integral", 75.0))
        )
        val cenaMartes = crearToma(
            Toma.TIPO_CENA,
            listOf(crearIngrediente("Pechuga de pollo", 200.0))
        )
        return listOf(
            crearPlan(lunes, desayunoLunes),
            crearPlan(lunes, meriendaLunes),
            crearPlan(martes, meriendaMartes),
            crearPlan(martes, cenaMartes)
        )
    }

    @Test
    fun `consolida los gramos de un mismo ingrediente entre varios planes`() = runTest {
        val lista = casoUso.ejecutar(crearPlanesSemana()).getOrThrow()

        // 100 + 50 + 75 = 225 gramos de panecillo consolidados en un único ítem.
        val itemPanecillo = lista.items.first { it.nombreAlimento == "Panecillo integral" }
        assertEquals(225.0, itemPanecillo.cantidadGramos, 0.001)
        // Tipos de ingesta de origen consolidados (DESAYUNO y MERIENDA).
        assertTrue(itemPanecillo.tipoIngestaOrigen.containsAll(listOf("DESAYUNO", "MERIENDA")))
    }

    @Test
    fun `escala a paquetes comerciales con redondeo hacia arriba`() = runTest {
        val lista = casoUso.ejecutar(crearPlanesSemana()).getOrThrow()

        // "Panecillo" coincide con el catálogo (paquete de 180 g): ceil(225/180) = 2.
        val itemPanecillo = lista.items.first { it.nombreAlimento == "Panecillo integral" }
        assertEquals("paquete", itemPanecillo.unidadComercial)
        assertEquals(2, itemPanecillo.cantidadPaquetes)
    }

    @Test
    fun `agrupa los items por supermercado preferido`() = runTest {
        val lista = casoUso.ejecutar(crearPlanesSemana()).getOrThrow()

        // El panecillo del catálogo se adquiere en Mercadona.
        val itemPanecillo = lista.items.first { it.nombreAlimento == "Panecillo integral" }
        assertEquals("Mercadona", itemPanecillo.supermercado)
        // Los alimentos sin unidad comercial conocida usan Mercadona por defecto.
        val itemPechuga = lista.items.first { it.nombreAlimento == "Pechuga de pollo" }
        assertEquals("Mercadona", itemPechuga.supermercado)
        // Supermercados distintos implicados.
        assertEquals(listOf("Mercadona"), lista.supermercados)
        // Ambos ítems se adquieren en Mercadona.
        assertEquals(2, lista.itemsPorSupermercado("Mercadona").size)
    }

    @Test
    fun `resuelve el supermercado segun las preferencias del usuario`() = runTest {
        val preferencias = mapOf("Pechuga de pollo" to "Alcampo")

        val lista = casoUso.ejecutar(crearPlanesSemana(), preferencias).getOrThrow()

        val itemPechuga = lista.items.first { it.nombreAlimento == "Pechuga de pollo" }
        assertEquals("Alcampo", itemPechuga.supermercado)
        assertTrue(lista.supermercados.contains("Alcampo"))
    }

    @Test
    fun `los ingredientes sin unidad comercial conservan gramos y paquetes cero`() = runTest {
        val lista = casoUso.ejecutar(crearPlanesSemana()).getOrThrow()

        val itemPechuga = lista.items.first { it.nombreAlimento == "Pechuga de pollo" }
        assertEquals(200.0, itemPechuga.cantidadGramos, 0.001)
        assertEquals(null, itemPechuga.unidadComercial)
        assertEquals(0, itemPechuga.cantidadPaquetes)
    }

    @Test
    fun `con planes vacios devuelve una lista vacia con la semana actual`() = runTest {
        val lista = casoUso.ejecutar(emptyList()).getOrThrow()

        assertTrue(lista.items.isEmpty())
        assertTrue(lista.supermercados.isEmpty())
        assertNotNull(lista.id)
        assertEquals(LocalDate.now(), lista.semanaInicio)
    }
}