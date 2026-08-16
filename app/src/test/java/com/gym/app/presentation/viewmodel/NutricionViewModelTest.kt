/**
 * @file NutricionViewModelTest.kt
 * @brief Pruebas unitarias del ViewModel de la pantalla de Nutrición.
 */
package com.gym.app.presentation.viewmodel

import com.gym.app.data.repository.RepositorioIngestaFake
import com.gym.app.data.repository.RepositorioPlanComidaFake
import com.gym.app.domain.model.Alimento
import com.gym.app.domain.model.IngestaRegistrada
import com.gym.app.domain.model.IngredienteToma
import com.gym.app.domain.model.PlanComida
import com.gym.app.domain.model.Toma
import com.gym.app.test.MainDispatcherRule
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * @class NutricionViewModelTest
 * @brief Verifica la carga del plan de hoy, la construcción del resumen
 * nutricional y el rebalanceo intra-día con los repositorios Fake.
 */
class NutricionViewModelTest {

    @get:Rule
    val reglaMain = MainDispatcherRule()

    /** Fecha de referencia: coincide con la fecha observada por el ViewModel. */
    private val hoy: LocalDate = LocalDate.now()

    /**
     * @brief Construye un plan de hoy con macros conocidos: 550 kcal totales,
     * 50 g de proteína, 75 g de CH y 0 g de grasa.
     */
    private fun crearPlan(): PlanComida {
        val pechuga = Alimento(
            id = "alimento-pechuga",
            nombre = "Pechuga de pollo",
            kcalPor100g = 120.0,
            proteinasPor100g = 25.0,
            carbohidratosPor100g = 0.0,
            grasasPor100g = 0.0
        )
        val panecillo = Alimento(
            id = "alimento-panecillo",
            nombre = "Panecillo integral",
            kcalPor100g = 200.0,
            proteinasPor100g = 0.0,
            carbohidratosPor100g = 50.0,
            grasasPor100g = 0.0
        )
        val arroz = Alimento(
            id = "alimento-arroz",
            nombre = "Arroz blanco",
            kcalPor100g = 110.0,
            proteinasPor100g = 0.0,
            carbohidratosPor100g = 25.0,
            grasasPor100g = 0.0
        )
        fun ingrediente(nombre: String, gramos: Double, alimento: Alimento) = IngredienteToma(
            id = "ingrediente-${nombre}",
            alimentoId = alimento.id,
            nombre = nombre,
            cantidadGramos = gramos,
            pesaje = IngredienteToma.PESAJE_CRUDO,
            origenPlan = true,
            alimentoResuelto = alimento
        )
        return PlanComida(
            id = "plan-hoy",
            nombre = "Plan de hoy",
            fecha = hoy,
            tomas = listOf(
                Toma(
                    id = "toma-desayuno",
                    tipoIngesta = Toma.TIPO_DESAYUNO,
                    orden = 1,
                    ingredientes = listOf(ingrediente("Pechuga de pollo", 100.0, pechuga))
                ),
                Toma(
                    id = "toma-comida",
                    tipoIngesta = Toma.TIPO_COMIDA,
                    orden = 2,
                    ingredientes = listOf(ingrediente("Pechuga de pollo", 100.0, pechuga))
                ),
                Toma(
                    id = "toma-merienda",
                    tipoIngesta = Toma.TIPO_MERIENDA,
                    orden = 3,
                    ingredientes = listOf(ingrediente("Panecillo integral", 100.0, panecillo))
                ),
                Toma(
                    id = "toma-cena",
                    tipoIngesta = Toma.TIPO_CENA,
                    orden = 4,
                    ingredientes = listOf(ingrediente("Arroz blanco", 100.0, arroz))
                )
            )
        )
    }

    /**
     * @brief Construye una ingesta consumida de hoy con los macros indicados.
     */
    private fun crearIngesta(
        proteinas: Double,
        carbohidratos: Double,
        grasas: Double,
        kcal: Double,
        tipoIngesta: String = Toma.TIPO_COMIDA
    ): IngestaRegistrada = IngestaRegistrada(
        id = "ingesta-${UUID.randomUUID()}",
        userId = "usuario-1",
        nombre = "Ingesta de prueba",
        kcal = kcal,
        proteinasG = proteinas,
        carbohidratosG = carbohidratos,
        grasasG = grasas,
        tipoIngesta = tipoIngesta,
        fecha = hoy,
        momentoDia = "TARDE",
        origen = IngestaRegistrada.ORIGEN_MANUAL
    )

    @Test
    fun `estado inicial termina la carga sin plan`() {
        val viewModel = NutricionViewModel(RepositorioPlanComidaFake(), RepositorioIngestaFake())

        assertEquals(false, viewModel.estado.value.cargando)
        assertNull(viewModel.estado.value.planHoy)
        assertTrue(viewModel.estado.value.ingestasHoy.isEmpty())
        assertNull(viewModel.estado.value.resumen)
    }

    @Test
    fun `carga el plan de hoy y calcula el resumen nutricional`() {
        val repositorioPlan = RepositorioPlanComidaFake()
        val repositorioIngesta = RepositorioIngestaFake()
        val viewModel = NutricionViewModel(repositorioPlan, repositorioIngesta)

        runBlocking {
            repositorioPlan.guardarPlan(crearPlan())
            repositorioIngesta.registrarIngesta(
                crearIngesta(proteinas = 50.0, carbohidratos = 75.0, grasas = 0.0, kcal = 550.0)
            )
        }

        assertEquals("plan-hoy", viewModel.estado.value.planHoy?.id)
        assertEquals(550.0, viewModel.estado.value.resumen?.kcalConsumidas ?: 0.0, 0.001)
        assertEquals(550.0, viewModel.estado.value.resumen?.kcalObjetivo ?: 0.0, 0.001)
        assertEquals(0.0, viewModel.estado.value.resumen?.kcalRestantes ?: 1.0, 0.001)
        assertEquals(false, viewModel.estado.value.cargando)
    }

    @Test
    fun `rebalancear con desvio propone ajustes y los expone en el estado`() {
        val repositorioPlan = RepositorioPlanComidaFake()
        val repositorioIngesta = RepositorioIngestaFake()
        val viewModel = NutricionViewModel(repositorioPlan, repositorioIngesta)

        runBlocking {
            repositorioPlan.guardarPlan(crearPlan())
            // Exceso de carbohidratos frente a lo planificado.
            repositorioIngesta.registrarIngesta(
                crearIngesta(proteinas = 60.0, carbohidratos = 120.0, grasas = 20.0, kcal = 950.0)
            )
        }

        viewModel.rebalancear()

        val ajustes = viewModel.estado.value.ajustesRebalanceo
        assertTrue(ajustes.isNotEmpty())
        assertEquals(Toma.TIPO_DESAYUNO, ajustes[0].tipoIngesta)
        assertTrue(ajustes[0].cambios.any { it.contains("panecillo") && it.contains("merienda") })
        assertNull(viewModel.estado.value.error)
        assertEquals(false, viewModel.estado.value.rebalanceando)
    }

    @Test
    fun `rebalancear sin plan no hace nada`() {
        val viewModel = NutricionViewModel(RepositorioPlanComidaFake(), RepositorioIngestaFake())

        viewModel.rebalancear()

        assertTrue(viewModel.estado.value.ajustesRebalanceo.isEmpty())
        assertNull(viewModel.estado.value.planHoy)
    }

    @Test
    fun `descartarAjustes limpia los ajustes propuestos`() {
        val repositorioPlan = RepositorioPlanComidaFake()
        val repositorioIngesta = RepositorioIngestaFake()
        val viewModel = NutricionViewModel(repositorioPlan, repositorioIngesta)

        runBlocking {
            repositorioPlan.guardarPlan(crearPlan())
            repositorioIngesta.registrarIngesta(
                crearIngesta(proteinas = 60.0, carbohidratos = 120.0, grasas = 20.0, kcal = 950.0)
            )
        }
        viewModel.rebalancear()
        assertTrue(viewModel.estado.value.ajustesRebalanceo.isNotEmpty())

        viewModel.descartarAjustes()

        assertTrue(viewModel.estado.value.ajustesRebalanceo.isEmpty())
    }

    @Test
    fun `registrar una ingesta en el repositorio actualiza el estado de forma reactiva`() {
        val repositorioPlan = RepositorioPlanComidaFake()
        val repositorioIngesta = RepositorioIngestaFake()
        val viewModel = NutricionViewModel(repositorioPlan, repositorioIngesta)

        runBlocking { repositorioPlan.guardarPlan(crearPlan()) }
        assertEquals(0.0, viewModel.estado.value.resumen?.kcalConsumidas ?: -1.0, 0.001)

        runBlocking {
            repositorioIngesta.registrarIngesta(
                crearIngesta(proteinas = 50.0, carbohidratos = 75.0, grasas = 0.0, kcal = 550.0)
            )
        }

        assertNotNull(viewModel.estado.value.resumen)
        assertEquals(550.0, viewModel.estado.value.resumen?.kcalConsumidas ?: 0.0, 0.001)
        assertEquals(1, viewModel.estado.value.ingestasHoy.size)
    }
}